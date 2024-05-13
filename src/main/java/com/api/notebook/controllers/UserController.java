package com.api.notebook.controllers;

import com.api.notebook.enums.RoleEnum;
import com.api.notebook.models.AuthModel;
import com.api.notebook.models.dtos.UserDto;
import com.api.notebook.models.dtos.UserWithoutPasswordDto;
import com.api.notebook.models.entities.AdminEntity;
import com.api.notebook.models.entities.TeacherEntity;
import com.api.notebook.models.entities.UserEntity;
import com.api.notebook.services.JwtService;
import com.api.notebook.services.MailService;
import com.api.notebook.services.UserService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;
    private final MailService mailService;




    //READ

    @GetMapping("/all") //GET endpoint to get all teachers
    @PreAuthorize("hasRole('ROLE_ADM')")
    public ResponseEntity<List<? extends UserEntity>> getAllUsers() {
        return ResponseEntity.ok(userService.findAllUsers());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUserById(
            @PathVariable(value = "userId") UUID userId
    ) {
        var userOptional = userService.findUserById(userId);
        if (userOptional.isPresent()) {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (
                    !authentication.getPrincipal().equals(userId) &&
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

    @PutMapping("/{userId}")
    public ResponseEntity<Object> editUser(
            @PathVariable(value = "userId") UUID userId,
            @RequestBody @Valid UserWithoutPasswordDto userWithoutPasswordDto
    ) {
        var userOptional = userService.findUserById(userId);
        if (userOptional.isPresent()) {

            var authenticationId = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (authenticationId.equals(userId)) {

                BeanUtils.copyProperties(userWithoutPasswordDto, userOptional.get());
                userService.editUser(userOptional.get());

                return ResponseEntity.ok().body("Usuário editado!");
            }

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Não é possível alterar a conta de outros usuários!");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado!");
    }

    @PutMapping("/{userId}/change-password")
    public ResponseEntity<Object> editUserPassword(
            @PathVariable(value = "userId") UUID userId,
            @RequestBody String newPassword
    ) {
        var userOptional = userService.findUserById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado!");
        }

        userOptional.get().setPassword(newPassword);
        userService.createUser(userOptional.get());

        return ResponseEntity.ok("Senha alterada!");
    }

    //EDIT


    //DELETE

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteUser(
            @PathVariable(value = "userId") UUID userId
    ) {
        var userOptional = userService.findUserById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado!");
        }

        var auth = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!userOptional.get().getId().equals(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Você não pode deletar a conta de outros usuários!");
        }

        userService.deleteUserById(userId);

        return ResponseEntity.ok("Conta deletada com sucesso!");

    }

    //DELETE


    //VERIFICATIONS

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
    public ResponseEntity<Object> authenticateUser(
            @RequestBody @Valid AuthModel authModel
    ) {
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
