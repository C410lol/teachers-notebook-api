package com.api.notebook.models.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Entity
@Table(name = "finished_notebooks")
public class FinishedNotebookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "total_lessons")
    private Integer totalLessons;

    @OneToOne
    @JoinColumn(name = "notebook_id")
    private NotebookEntity notebook;

    @OneToMany(mappedBy = "finishedNotebook", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<FinishedStudentEntity> finishedStudents;

}
