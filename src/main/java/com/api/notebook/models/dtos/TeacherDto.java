package com.api.notebook.models.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TeacherDto {

    @NotBlank
    @Size(min = 3, message = "O nome deve ser maior ou igual a 3 caracteres!")
    private String name;

    @NotBlank
    @Email(message = "Insira um email válido!")
    private String email;


    @NotBlank
    @Size(min = 8, message = "A senha deve ser maior ou igual a 8 caracteres!")
    private String password;

}
