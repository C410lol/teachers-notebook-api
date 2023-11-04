package com.api.notebook.models.entities;

import com.api.notebook.enums.WorksEnum;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name = "works")
public class WorkEntity {

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

    @Column(name = "type")
    @Enumerated(value = EnumType.STRING)
    private WorksEnum type;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "notebook_id")
    private NotebookEntity notebook;

    @JsonIgnore
    @OneToMany(mappedBy = "work", cascade = {CascadeType.ALL})
    private List<GradeEntity> grades;

    @JsonGetter(value = "grades")
    public Integer getGradesQuantity() {
        return grades.size();
    }

}
