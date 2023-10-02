package com.api.notebook.models.dtos;

import com.api.notebook.enums.BimesterEnum;
import com.api.notebook.enums.ClassEnum;
import com.api.notebook.enums.SubjectEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotebookDto {

    @NotNull
    private ClassEnum classe;

    @NotNull
    private SubjectEnum subject;

    @NotNull
    private BimesterEnum bimester;

}
