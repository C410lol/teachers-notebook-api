package com.api.notebook.models;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class WorkTypeWeightsModel {

    @NotNull
    private Double tarefaWeight;

    @NotNull
    private Double provasWeight;

    @NotNull
    private Double participacaoWeight;

    @NotNull
    private Double simuladoWeight;

}
