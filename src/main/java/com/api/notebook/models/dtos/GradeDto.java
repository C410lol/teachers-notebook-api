package com.api.notebook.models.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class GradeDto {

    @NotNull
    private Double grade;

    @NotNull
    private UUID studentId;

}
