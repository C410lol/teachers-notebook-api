package com.api.notebook.controllers;

import com.api.notebook.enums.RoleEnum;
import com.api.notebook.models.AuthReturnModel;
import com.api.notebook.models.dtos.UserDto;
import com.api.notebook.models.entities.AdminEntity;
import com.api.notebook.services.AdminService;
import com.api.notebook.services.InstitutionService;
import com.api.notebook.services.JwtService;
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
@RequestMapping("/admins")
public class AdminController {

    private final UserService userService;
    private final InstitutionService institutionService;
    private final JwtService jwtService;




    //CREATE

    @PostMapping("/create")
    public ResponseEntity<?> createAdmin(
            @RequestParam(value = "institutionId", required = false) UUID institutionId,
            @RequestBody @NotNull UserDto userDto
    ) {
        if (userService.existsByEmail(userDto.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email já cadastrado!");
        }

        var adminEntity = new AdminEntity();
        BeanUtils.copyProperties(userDto, adminEntity);
        adminEntity.setRole(RoleEnum.ROLE_ADM);
        adminEntity.setVerified(true);

        if (institutionId != null && !institutionId.toString().isBlank()) {
            var institutionOptional = institutionService.findById(institutionId);
            if (institutionOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Instituição não encontrada!");
            }
            adminEntity.setInstitution(institutionOptional.get());
        }

        var createdUser = userService.createUser(adminEntity);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthReturnModel(
                        createdUser.getId(),
                        jwtService.generateToken(createdUser.getEmail())
                ));
    }

    //CREATE

}
