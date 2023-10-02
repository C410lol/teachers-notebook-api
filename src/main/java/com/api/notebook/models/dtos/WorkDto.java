package com.api.notebook.models.dtos;

import com.api.notebook.enums.WorksEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class WorkDto {

    @NotBlank
    private String title;

    @NotBlank
    private String details;

    private String observations;

    @NotBlank
    private WorksEnum type;

    @NotNull
    private LocalDate deliveryDate;

}
