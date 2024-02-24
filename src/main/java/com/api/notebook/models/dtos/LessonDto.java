package com.api.notebook.models.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class LessonDto {

    @NotBlank
    private String title;

    @NotBlank
    private String details;

    private String observations;

    private List<String> bnccCodes;

    @NotNull
    private Integer quantity;

    private LocalDate date;

}
