package com.api.notebook.controllers;

import com.api.notebook.enums.RoleEnum;
import com.api.notebook.enums.VerificationCodeEnum;
import com.api.notebook.models.AuthModel;
import com.api.notebook.models.EmailModel;
import com.api.notebook.models.dtos.TeacherDto;
import com.api.notebook.models.dtos.TeacherWithoutPasswordDto;
import com.api.notebook.models.entities.TeacherEntity;
import com.api.notebook.models.entities.VerificationCodeEntity;
import com.api.notebook.producers.MailProducer;
import com.api.notebook.services.JwtService;
import com.api.notebook.services.TeacherService;
import com.api.notebook.services.VerificationCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/teachers")
public class TeacherController {

    private final TeacherService teacherService;
    private final JwtService jwtService;
    private final VerificationCodeService verificationCodeService;
    private final MailProducer mailProducer;
    private final Random random;

    @PostMapping("/create") //POST endpoint to create a teacher entity
    public ResponseEntity<Object> createTeacher(@RequestBody @Valid TeacherDto teacherDto) {
        var teacherEntity = new TeacherEntity();
        BeanUtils.copyProperties(teacherDto, teacherEntity);
        var createdTeacher = teacherService.createTeacher(teacherEntity);

        var code = random.nextInt(1000, 9999);
        var verificationCodeEntity = new VerificationCodeEntity();
        verificationCodeEntity.setCode(code);
        verificationCodeEntity.setType(VerificationCodeEnum.EMAIL_VERIFICATION);
        teacherService.setVerificationCodeToTeacher(createdTeacher.getId(), verificationCodeEntity);
        verificationCodeService.save(verificationCodeEntity);

        mailProducer.sendMailMessage(new EmailModel(
                teacherEntity.getEmail(),
                "Confirme Sua Conta No Site TeacherNotesHub!",
                String.format("Clique neste link para confirmar sua conta ou copie e cole no seu navegador: " +
                        "https://app.teachernoteshub.online/verify-account/%s?vCode=%s",
                        teacherEntity.getId(), code)
        ));

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/all") //GET endpoint to get all teachers
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Object> getAllTeachers() {
        return ResponseEntity.ok(teacherService.findAllTeachers());
    }

    @GetMapping("/{teacherId}")
    public ResponseEntity<Object> getTeacherById(@PathVariable(value = "teacherId") UUID teacherId) {
        var teacherOptional = teacherService.findTeacherById(teacherId);
        if (teacherOptional.isPresent()) {
            var authenticationId = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (authenticationId.equals(teacherId)) {
                return ResponseEntity.ok(teacherOptional.get());
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado!");
    }

    @PutMapping("/{teacherId}")
    public ResponseEntity<Object> editTeacher(@PathVariable(value = "teacherId") UUID teacherId,
                                              @RequestBody @Valid TeacherWithoutPasswordDto teacherWithoutPasswordDto) {
        var teacherOptional = teacherService.findTeacherById(teacherId);
        if (teacherOptional.isPresent()) {
            var authenticationId = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (authenticationId.equals(teacherId)) {
                var teacherEntity = new TeacherEntity();
                BeanUtils.copyProperties(teacherOptional.get(), teacherEntity);
                BeanUtils.copyProperties(teacherWithoutPasswordDto, teacherEntity);
                teacherService.editTeacher(teacherEntity);
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado!");
    }

    @PostMapping("/request-password-change")
    public ResponseEntity<Object> requestPasswordChange(
            @RequestParam(value = "id", required = false) UUID userId,
            @RequestParam(value = "email", required = false) String userEmail
    ) {
        Optional<TeacherEntity> teacherOptional;

        if (userId != null) {
            teacherOptional = teacherService.findTeacherById(userId);
        } else if (userEmail != null) {
            teacherOptional = teacherService.findTeacherByEmail(userEmail);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (teacherOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        var code = random.nextInt(1000, 9999);
        var verificationCodeEntity = new VerificationCodeEntity();
        verificationCodeEntity.setCode(code);
        verificationCodeEntity.setType(VerificationCodeEnum.PASSWORD_CHANGE);
        teacherService.setVerificationCodeToTeacher(teacherOptional.get().getId(), verificationCodeEntity);
        verificationCodeService.save(verificationCodeEntity);

        mailProducer.sendMailMessage(new EmailModel(
                teacherOptional.get().getEmail(),
                "Requisição Para Trocar de Senha No Site TeacherNotesHub.",
                String.format("Clique neste link para mudar sua senha ou copie e cole no seu navegador: " +
                                "https://app.teachernoteshub.online/change-password/%s?vCode=%s",
                        teacherOptional.get().getId(), code)
        ));

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{teacherId}/change-password")
    public ResponseEntity<Object> editTeacherPassword(
            @PathVariable(value = "teacherId") UUID teacherId,
            @RequestParam(value = "vCode") Integer vCode,
            @RequestBody String newPassword
    ) {
        var teacherOptional = teacherService.findTeacherById(teacherId);
        if (teacherOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        var vCodeOptional = verificationCodeService.findByTeacherId(teacherId, VerificationCodeEnum.PASSWORD_CHANGE);
        if (vCodeOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if (!vCodeOptional.get().getCode().equals(vCode)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        teacherOptional.get().setPassword(newPassword);
        teacherService.createTeacher(teacherOptional.get());


        verificationCodeService.deleteById(vCodeOptional.get().getId());

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{teacherId}")
    public ResponseEntity<Object> deleteTeacher(@PathVariable(value = "teacherId") UUID teacherId) {
        var teacherOptional = teacherService.findTeacherById(teacherId);
        if (teacherOptional.isPresent()) {
            var authenticationId = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (authenticationId.equals(teacherId)) {
                teacherService.deleteTeacherById(teacherId);
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado!");
    }

    @PutMapping("/verify-account/{teacherId}")
    public ResponseEntity<Object> verifyTeacherAccount(
            @PathVariable(value = "teacherId") UUID teacherId,
            @RequestParam(value = "vCode") Integer vCode
    ) {
        var teacherOptional = teacherService.findTeacherById(teacherId);
        if (teacherOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        var verificationCodeOptional = verificationCodeService.findByTeacherId(
                teacherId, VerificationCodeEnum.EMAIL_VERIFICATION);
        if (verificationCodeOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        if (!verificationCodeOptional.get().getCode().equals(vCode)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        teacherOptional.get().setRole(RoleEnum.ROLE_USER);
        teacherService.editTeacher(teacherOptional.get());

        verificationCodeService.deleteById(verificationCodeOptional.get().getId());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/resend-verification-email")
    public ResponseEntity<Object> resendVerificationEmail(
            @RequestParam(value = "teacherId", required = false) UUID teacherId,
            @RequestParam(value = "teacherEmail", required = false) String email
    ) {
        Optional<TeacherEntity> teacherOptional;

        if (teacherId != null) {
            teacherOptional = teacherService.findTeacherById(teacherId);
        } else if (email != null) {
            teacherOptional = teacherService.findTeacherByEmail(email);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (teacherOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        var verificationCodeOptional = verificationCodeService.findByTeacherId(
                teacherOptional.get().getId(), VerificationCodeEnum.EMAIL_VERIFICATION);
        if (verificationCodeOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        mailProducer.sendMailMessage(new EmailModel(
                teacherOptional.get().getEmail(),
                "Confirme Sua Conta No Site TeacherNotesHub!",
                String.format("Clique neste link para confirmar sua conta ou copie e cole no seu navegador: " +
                                "https://app.teachernoteshub.online/verify-account/%s?vCode=%s",
                        teacherId, verificationCodeOptional.get().getCode())
        ));

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{teacherId}/verified")
    public ResponseEntity<Object> isUserVerified(
            @PathVariable(value = "teacherId") UUID teacherId
    ) {
        var teacherOptional = teacherService.findTeacherById(teacherId);
        if (teacherOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        }
        if (teacherOptional.get().getRole() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
        }
        return ResponseEntity.ok(true);
    }

    @PostMapping("/login") //POST endpoint to authenticate a user
    public ResponseEntity<Object> authenticateUser(@RequestBody @Valid AuthModel authModel) {
        var authentication = teacherService.tryToAuthenticateTeacher(authModel, jwtService); //Try to authenticate user
        if (authentication != null) {
            return ResponseEntity.ok(authentication); //If success it returns a token
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou senha incorretos!");
    }

}
