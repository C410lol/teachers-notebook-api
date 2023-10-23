package com.api.notebook.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name = "lessons")
public class LessonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "title")
    private String title;

    @Column(name = "details")
    private String details;

    @Column(name = "observations")
    private String observations;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "date")
    private LocalDate date;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "notebook_id")
    private NotebookEntity notebook;

    @OneToMany(mappedBy = "lesson", cascade = {CascadeType.ALL})
    private List<AttendanceEntity> attendances;

}
