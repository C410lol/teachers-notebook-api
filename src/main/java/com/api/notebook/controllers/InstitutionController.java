package com.api.notebook.controllers;

import com.api.notebook.models.dtos.InstitutionDto;
import com.api.notebook.models.entities.InstitutionEntity;
import com.api.notebook.services.InstitutionService;
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

    private final InstitutionService institutionService;




    //CREATE

    @PostMapping("/create")
    @PreAuthorize("hasRole('ROLE_ADM')")
    public ResponseEntity<?> createInstitution(
            @RequestBody InstitutionDto institutionDto
    ) {
        var institutionEntity = new InstitutionEntity();
        BeanUtils.copyProperties(institutionDto, institutionEntity);
        institutionService.save(institutionEntity);

        return ResponseEntity.status(HttpStatus.CREATED).body("Instituição criada com sucesso!");
    }

    //CREATE




    //READ

    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_ADM')")
    public ResponseEntity<?> findAllInstitutions() {
        return ResponseEntity.ok(institutionService.findAll());
    }

    @GetMapping("/{institutionId}")
    @PreAuthorize("hasRole('ROLE_ADM')")
    public ResponseEntity<?> findAllInstitutions(
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
