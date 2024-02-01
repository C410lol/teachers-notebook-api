package com.api.notebook.models.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserWithoutPasswordDto {

    @NotBlank
    @Size(min = 3, message = "O nome deve ser maior ou igual a 3 caracteres!")
    private String name;

    @NotBlank
    @Email(message = "Insira um email válido!")
    private String email;

}
