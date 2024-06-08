package com.api.notebook.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.UUID;

@Data
@Entity
@Table(name = "finished_students")
public class FinishedStudentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "finished_notebook_id")
    private FinishedNotebookEntity finishedNotebook;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private StudentEntity student;

    @Column(name = "final_grade")
    private Double finalGrade;

    @Column(name = "absences")
    private Integer absences;

    @Column(name = "presence_percentage")
    private String presencePercentage;


}
