package com.api.notebook.models.dtos;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class WorkTypeWeights {

    @NotNull
    private Integer tarefaWeight;

    @NotNull
    private Integer trabalhoWeight;

    @NotNull
    private Integer participacaoWeight;

    @NotNull
    private Integer simuladoWeight;

}
