package com.api.notebook.models.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InstitutionDto {

    @NotBlank
    private String name;

}
