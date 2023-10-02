package com.api.notebook.models.entities;

import com.api.notebook.enums.WorksEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@Table(name = "works")
public class WorkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "details")
    private String details;

    @Column(name = "observations")
    private String observations;

    @Column(name = "type")
    @Enumerated(value = EnumType.STRING)
    private WorksEnum type;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "notebook_id")
    private NotebookEntity notebook;

    @OneToMany(mappedBy = "work", cascade = {CascadeType.ALL})
    private List<GradeEntity> grades;

}
