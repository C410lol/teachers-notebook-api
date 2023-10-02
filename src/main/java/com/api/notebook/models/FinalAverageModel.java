package com.api.notebook.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class FinalAverageModel {

    private Integer attendanceNumber;
    private String name;
    private Double finalGrade;
    private Integer absences;
    private Integer compensatedAbsence;

}
