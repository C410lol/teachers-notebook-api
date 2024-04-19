package com.api.notebook.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class MissingTaskModel {

    private UUID id;
    private String title;
    private UUID notebookId;

}
