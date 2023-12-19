package com.api.notebook.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class EmailModel {

    private String to;
    private String subject;
    private String text;

}
