package com.api.notebook.models;

import com.api.notebook.enums.PerformanceStatus;
import com.api.notebook.models.entities.StudentEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class StudentPerformanceModel {

    private StudentEntity student;
    private Integer absences;
    private String absencesPercentage;
    private PerformanceStatus absencesStatus;

}
