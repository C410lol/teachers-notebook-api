package com.api.notebook.controllers;

import com.api.notebook.enums.RoleEnum;
import com.api.notebook.enums.VCodeEnum;
import com.api.notebook.models.AuthModel;
import com.api.notebook.models.EmailModel;
import com.api.notebook.models.dtos.UserDto;
import com.api.notebook.models.dtos.UserWithoutPasswordDto;
import com.api.notebook.models.entities.UserEntity;
import com.api.notebook.models.entities.VCodeEntity;
import com.api.notebook.producers.MailProducer;
import com.api.notebook.services.JwtService;
import com.api.notebook.services.UserService;
import com.api.notebook.services.VCodeService;
import com.api.notebook.utils.CodeGenerator;
import com.api.notebook.utils.Constants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;
    private final VCodeService vCodeService;
    private final MailProducer mailProducer;



    //CREATE

    @PostMapping("/create") //POST endpoint to create a teacher entity
    public ResponseEntity<Object> createUser(@RequestBody @Valid @NotNull UserDto userDto) {
        if (userService.existsByEmail(userDto.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email já registrado!");
        }

        var teacherEntity = new UserEntity();
        BeanUtils.copyProperties(userDto, teacherEntity);
        teacherEntity.setVerified(false);
        var createdUser = userService.createUser(teacherEntity);

        var code = CodeGenerator.generateCode();
        var verificationCodeEntity = new VCodeEntity();
        verificationCodeEntity.setCode(code);
        verificationCodeEntity.setType(VCodeEnum.EMAIL_VERIFICATION);
        userService.setVCodeToUser(createdUser.getId(), verificationCodeEntity);
        vCodeService.save(verificationCodeEntity);

        mailProducer.sendMailMessage(new EmailModel(
                teacherEntity.getEmail(),
                "Confirme Sua Conta No Site TeacherNotesHub!",
                String.format("Clique neste link para confirmar sua conta ou copie e cole no seu navegador: " +
                        "%s/verify-account/%s?vCode=%s",
                        Constants.APP_URL, teacherEntity.getId(), code)
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body("Usuário criado!");
    }

    //CREATE




    //READ

    @GetMapping("/all") //GET endpoint to get all teachers
    @PreAuthorize("hasRole('ROLE_ADM')")
    public ResponseEntity<Object> getAllUsers() {
        return ResponseEntity.ok(userService.findAllUsers());
    }

    @GetMapping("/teachers")
    @PreAuthorize("hasRole('ROLE_ADM')")
    public ResponseEntity<Object> getAllTeachers() {
        return ResponseEntity.ok(userService.findAllUsersByRole(RoleEnum.ROLE_TCHR));
    }

    @GetMapping("/{teacherId}")
    public ResponseEntity<Object> getUserById(@PathVariable(value = "teacherId") UUID teacherId) {
        var userOptional = userService.findUserById(teacherId);
        if (userOptional.isPresent()) {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (
                    !authentication.getPrincipal().equals(teacherId) &&
                    !authentication.getAuthorities().contains(new SimpleGrantedAuthority(RoleEnum.ROLE_ADM.name()))
            ) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Não é possível visualizar a conta de outros usuários!");
            }
            return ResponseEntity.ok(userOptional.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado!");
    }

    //READ




    //EDIT

    @PutMapping("/{teacherId}")
    public ResponseEntity<Object> editUser(@PathVariable(value = "teacherId") UUID teacherId,
                                              @RequestBody @Valid UserWithoutPasswordDto userWithoutPasswordDto) {
        var userOptional = userService.findUserById(teacherId);
        if (userOptional.isPresent()) {
            var authenticationId = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (authenticationId.equals(teacherId)) {
                var teacherEntity = new UserEntity();
                BeanUtils.copyProperties(userOptional.get(), teacherEntity);
                BeanUtils.copyProperties(userWithoutPasswordDto, teacherEntity);
                userService.user(teacherEntity);
                return ResponseEntity.ok().body("Usuário editado!");
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Não é possível alterar a conta de outros usuários!");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado!");
    }

    @PostMapping("/request-password-change")
    public ResponseEntity<Object> requestPasswordChange(
            @RequestParam(value = "id", required = false) UUID userId,
            @RequestParam(value = "email", required = false) String userEmail
    ) {
        Optional<UserEntity> userOptional;

        if (userId != null) {
            userOptional = userService.findUserById(userId);
        } else if (userEmail != null) {
            userOptional = userService.findUserByEmail(userEmail);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado!");
        }

        if (vCodeService.existsByUserIdAndType(
                userOptional.get().getId(), VCodeEnum.PASSWORD_CHANGE)) {
            var codeId = vCodeService.findByUserIdAndType(
                    userOptional.get().getId(), VCodeEnum.PASSWORD_CHANGE).get().getId();
            vCodeService.deleteById(codeId);
        }

        var code = CodeGenerator.generateCode();
        var verificationCodeEntity = new VCodeEntity();
        verificationCodeEntity.setCode(code);
        verificationCodeEntity.setType(VCodeEnum.PASSWORD_CHANGE);
        userService.setVCodeToUser(userOptional.get().getId(), verificationCodeEntity);
        vCodeService.save(verificationCodeEntity);

        mailProducer.sendMailMessage(new EmailModel(
                userOptional.get().getEmail(),
                "Requisição Para Trocar de Senha No Site TeacherNotesHub.",
                String.format("Clique neste link para mudar sua senha ou copie e cole no seu navegador: " +
                                "%s/change-password/%s?vCode=%s",
                        Constants.APP_URL, userOptional.get().getId(), code)
        ));

        return ResponseEntity.ok("Requisição para trocar de senha enviada!");
    }

    @PutMapping("/{teacherId}/change-password")
    public ResponseEntity<Object> editUserPassword(
            @PathVariable(value = "teacherId") UUID teacherId,
            @RequestParam(value = "vCode") Integer vCode,
            @RequestBody String newPassword
    ) {
        var userOptional = userService.findUserById(teacherId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado!");
        }

        var vCodeOptional = vCodeService.findByUserIdAndType(teacherId, VCodeEnum.PASSWORD_CHANGE);
        if (vCodeOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Código de verificação não registrado para essa conta!");
        }

        if (!vCodeOptional.get().getCode().equals(vCode)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Código de verificação errado!");
        }

        userOptional.get().setPassword(newPassword);
        userService.createUser(userOptional.get());

        vCodeService.deleteById(vCodeOptional.get().getId());

        return ResponseEntity.ok("Senha alterada!");
    }

    //EDIT




    //DELETE

    @PostMapping("/{teacherId}/delete-request")
    public ResponseEntity<Object> deleteUserRequest(
            @PathVariable(value = "teacherId") UUID teacherId
    ) {

        var userOptional = userService.findUserById(teacherId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado!");
        }

        if (vCodeService.existsByUserIdAndType(
                teacherId, VCodeEnum.ACCOUNT_DELETE)) {
             var codeId = vCodeService.findByUserIdAndType(
                     teacherId, VCodeEnum.ACCOUNT_DELETE).get().getId();
             vCodeService.deleteById(codeId);
        }

        var code = CodeGenerator.generateCode();
        var verificationCodeEntity = new VCodeEntity();
        verificationCodeEntity.setCode(code);
        verificationCodeEntity.setType(VCodeEnum.ACCOUNT_DELETE);
        userService.setVCodeToUser(teacherId, verificationCodeEntity);
        vCodeService.save(verificationCodeEntity);

        mailProducer.sendMailMessage(new EmailModel(
                userOptional.get().getEmail(),
                "Requisição Para Deletar Sua Conta",
                "Para confirmar a deleção de sua conta clique neste link ou copie e cole em seu navegador:" +
                        String.format("%s/delete-confirm/%s?vCode=%s",
                                Constants.APP_URL, teacherId, code)
        ));

        return ResponseEntity.ok("Requisição para deletar sua conta enviada!");

    }

    @DeleteMapping("/{teacherId}")
    public ResponseEntity<Object> deleteUser(
            @PathVariable(value = "teacherId") UUID teacherId,
            @RequestParam(value = "vCode") Integer vCode
    ) {

        var userOptional = userService.findUserById(teacherId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado!");
        }

        var auth = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!userOptional.get().getId().equals(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Você não pode deletar a conta de outros usuários!");
        }

        var vCodeOptional = vCodeService.findByUserIdAndType(teacherId, VCodeEnum.ACCOUNT_DELETE);
        if (vCodeOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Nenhuma requisição para deletar conta encontrada!");
        }

        if (!vCodeOptional.get().getCode().equals(vCode)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Código de verificação errado!");
        }

        userService.deleteUserById(teacherId);
        vCodeService.deleteById(vCodeOptional.get().getId());

        return ResponseEntity.ok("Conta deletada com sucesso!");

    }

    //DELETE




    //VERIFICATIONS

    @PutMapping("/verify-account/{teacherId}")
    public ResponseEntity<Object> verifyUserAccount(
            @PathVariable(value = "teacherId") UUID teacherId,
            @RequestParam(value = "vCode") Integer vCode
    ) {
        var userOptional = userService.findUserById(teacherId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Conta não encontrada!");
        }

        var verificationCodeOptional = vCodeService.findByUserIdAndType(
                teacherId, VCodeEnum.EMAIL_VERIFICATION);
        if (verificationCodeOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Código de verificação não registrado para essa conta!");
        }

        if (!verificationCodeOptional.get().getCode().equals(vCode)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Código de verificação errado!");
        }

        userOptional.get().setRole(RoleEnum.ROLE_TCHR);
        userOptional.get().setVerified(true);
        userService.user(userOptional.get());

        vCodeService.deleteById(verificationCodeOptional.get().getId());

        return ResponseEntity.ok("Conta verificada!");
    }

    @PostMapping("/resend-verification-email")
    public ResponseEntity<Object> resendVerificationEmail(
            @RequestParam(value = "teacherId", required = false) UUID teacherId,
            @RequestParam(value = "teacherEmail", required = false) String email
    ) {
        Optional<UserEntity> userOptional;

        if (teacherId != null) {
            userOptional = userService.findUserById(teacherId);
        } else if (email != null) {
            userOptional = userService.findUserByEmail(email);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Conta não encontrada!");
        }

        var verificationCodeOptional = vCodeService.findByUserIdAndType(
                userOptional.get().getId(), VCodeEnum.EMAIL_VERIFICATION);
        if (verificationCodeOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Código de verificação não registrado para essa conta!");
        }

        mailProducer.sendMailMessage(new EmailModel(
                userOptional.get().getEmail(),
                "Confirme Sua Conta No Site TeacherNotesHub!",
                String.format("Clique neste link para confirmar sua conta ou copie e cole no seu navegador: " +
                                "%s/verify-account/%s?vCode=%s",
                        Constants.APP_URL, teacherId, verificationCodeOptional.get().getCode())
        ));

        return ResponseEntity.ok("Email de verificação reenviado!");
    }

    @GetMapping("/{teacherId}/verified")
    public ResponseEntity<Object> isUserVerified(
            @PathVariable(value = "teacherId") UUID teacherId
    ) {
        var userOptional = userService.findUserById(teacherId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        }
        if (!userOptional.get().isVerified()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
        }
        return ResponseEntity.ok(true);
    }

    //VERIFICATIONS




    //AUTHENTICATION

    @PostMapping("/login") //POST endpoint to authenticate a user
    public ResponseEntity<Object> authenticateUser(@RequestBody @Valid AuthModel authModel) {
        var authentication = userService.tryToAuthenticate(authModel, jwtService);

        switch (authentication.getStatus()) {
            case OK -> {
                return ResponseEntity.ok(authentication.getAuthReturnModel());
            }
            case NOT_FOUND -> {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email não encontrado!");
            }
            case INCORRECT_PASSWORD -> {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Senha incorreta!");
            }
            default -> {
                return ResponseEntity.badRequest().build();
            }
        }
    }

    //AUTHENTICATION




}
