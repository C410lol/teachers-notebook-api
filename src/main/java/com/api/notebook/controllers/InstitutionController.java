package com.api.notebook.controllers;

import com.api.notebook.enums.RoleEnum;
import com.api.notebook.models.dtos.InstitutionDto;
import com.api.notebook.models.entities.InstitutionEntity;
import com.api.notebook.services.AdminService;
import com.api.notebook.services.InstitutionService;
import com.api.notebook.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/institutions")
public class InstitutionController {

    private final AdminService adminService;
    private final UserService userService;
    private final InstitutionService institutionService;




    //CREATE

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ROLE_ADM', 'ROLE_SUPER')")
    public ResponseEntity<?> createInstitution(
            @RequestBody InstitutionDto institutionDto,
            @RequestParam(value = "adminId") UUID adminId
    ) {
        var adminOptional = adminService.findById(adminId);
        if (adminOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Admin não encontrado.");
        }

        var institutionEntity = new InstitutionEntity();
        BeanUtils.copyProperties(institutionDto, institutionEntity);
        institutionEntity.setCreator(adminOptional.get());
        institutionService.save(institutionEntity);

        adminOptional.get().setRole(RoleEnum.ROLE_SUPER);

        userService.editUser(adminOptional.get());

        return ResponseEntity.status(HttpStatus.CREATED).body("Instituição criada com sucesso!");
    }

    //CREATE




    //READ

    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_SUPER')")
    public ResponseEntity<?> findAllInstitutions() {
        return ResponseEntity.ok(institutionService.findAll());
    }

    @GetMapping("/{institutionId}")
    @PreAuthorize("hasAnyRole('ROLE_TCHR', 'ROLE_ADM', 'ROLE_SUPER')")
    public ResponseEntity<?> findInstitutionById(
            @PathVariable(value = "institutionId") UUID institutionId
    ) {
        var institutionOptional = institutionService.findById(institutionId);
        if (institutionOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Instituição não encontrada.");
        }

        return ResponseEntity.ok(institutionService.findAll());
    }

    //READ
}
