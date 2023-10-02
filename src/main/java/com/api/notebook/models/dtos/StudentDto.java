package com.api.notebook.models.dtos;

import com.api.notebook.enums.ClassEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StudentDto {

    @NotBlank
    @Size(min = 3, message = "O nome deve ser maior ou igual a 3 caracteres!")
    private String name;

    @NotNull
    private ClassEnum classe;

}
