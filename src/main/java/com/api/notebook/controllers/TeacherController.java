package com.api.notebook.controllers;

import com.api.notebook.enums.RoleEnum;
import com.api.notebook.models.AuthModel;
import com.api.notebook.models.dtos.TeacherDto;
import com.api.notebook.models.entities.TeacherEntity;
import com.api.notebook.services.JwtService;
import com.api.notebook.services.TeacherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/teachers")
public class TeacherController {

    private final TeacherService teacherService;
    private final JwtService jwtService;

    @PostMapping("/create") //POST endpoint to create a teacher entity
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_USER')")
    public ResponseEntity<Object> createTeacher(@RequestBody @Valid TeacherDto teacherDto) {
        var teacherEntity = new TeacherEntity();
        BeanUtils.copyProperties(teacherDto, teacherEntity);
        teacherEntity.setRole(RoleEnum.ROLE_USER);
        teacherService.saveTeacher(teacherEntity);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/all") //GET endpoint to get all teachers
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Object> getAllTeachers() {
        return ResponseEntity.ok(teacherService.findAllTeachers());
    }

    @GetMapping("/{teacherId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_USER')")
    public ResponseEntity<Object> getTeacherById(@PathVariable(value = "teacherId") UUID teacherId) {
        var teacherOptional = teacherService.findTeacherById(teacherId);
        if (teacherOptional.isPresent()) {
            var authenticationId = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (authenticationId.equals(teacherId)) {
                return ResponseEntity.ok(teacherOptional.get());
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado!");
    }

    @PutMapping("/{teacherId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_USER')")
    public ResponseEntity<Object> editTeacher(@PathVariable(value = "teacherId") UUID teacherId,
                                              @RequestBody @Valid TeacherDto teacherDto) {
        var teacherOptional = teacherService.findTeacherById(teacherId);
        if (teacherOptional.isPresent()) {
            var authenticationId = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (authenticationId.equals(teacherId)) {
                var teacherEntity = new TeacherEntity();
                BeanUtils.copyProperties(teacherOptional.get(), teacherEntity);
                BeanUtils.copyProperties(teacherDto, teacherEntity);
                teacherService.saveTeacher(teacherEntity);
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado!");
    }

    @DeleteMapping("/{teacherId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_USER')")
    public ResponseEntity<Object> deleteTeacher(@PathVariable(value = "teacherId") UUID teacherId) {
        var teacherOptional = teacherService.findTeacherById(teacherId);
        if (teacherOptional.isPresent()) {
            var authenticationId = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (authenticationId.equals(teacherId)) {
                teacherService.deleteTeacherById(teacherId);
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado!");
    }

    @PostMapping("/login") //POST endpoint to authenticate a user
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_USER')")
    public ResponseEntity<Object> authenticateUser(@RequestBody @Valid AuthModel authModel) {
        var authentication = teacherService.tryToAuthenticateTeacher(authModel, jwtService); //Try to authenticate user
        if (authentication != null) {
            return ResponseEntity.ok(authentication); //If success it returns a token
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou senha incorretos!");
    }

    @PostMapping("/get-by-token") //Get endpoint to get user by token
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_USER')")
    public ResponseEntity<Object> getUserByToken(@RequestBody @NotNull String token) {
        var userEmail = jwtService.getEmailByToken(token.substring(7));
        var teacherOptional = teacherService.findTeacherByEmail(userEmail);
        if (teacherOptional.isPresent()) {
            return ResponseEntity.ok(teacherOptional.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado!");
    }

}
