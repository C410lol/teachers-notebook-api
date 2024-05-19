package com.api.notebook.controllers;

import com.api.notebook.enums.RoleEnum;
import com.api.notebook.models.dtos.UserDto;
import com.api.notebook.models.entities.TeacherEntity;
import com.api.notebook.services.InstitutionService;
import com.api.notebook.services.UserService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/teachers")
public class TeacherController {

    private final UserService userService;
    private final InstitutionService institutionService;




    //CREATE

    @PostMapping("/create")
    public ResponseEntity<?> createTeacher(
            @RequestParam(value = "institutionId", required = false) UUID institutionId,
            @RequestBody @NotNull UserDto userDto
    ) {
        if (userService.existsByEmail(userDto.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email já cadastrado!");
        }

        var teacherEntity = new TeacherEntity();
        BeanUtils.copyProperties(userDto, teacherEntity);
        teacherEntity.setRole(RoleEnum.ROLE_TCHR);
        teacherEntity.setVerified(true);

        if (institutionId != null && !institutionId.toString().isBlank()) {
            var institutionOptional = institutionService.findById(institutionId);
            if (institutionOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Instituição não encontrada!");
            }
            teacherEntity.setInstitution(institutionOptional.get());
        }

        userService.createUser(teacherEntity);

        return ResponseEntity.status(HttpStatus.CREATED).body("Admin criado com sucesso!");
    }

}
