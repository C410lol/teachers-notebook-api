package com.api.notebook.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "institutions")
public class InstitutionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "name")
    private String name;

    @JsonIgnore
    @OneToMany(mappedBy = "institution")
    private List<AdminEntity> admins;

    @JsonIgnore
    @OneToMany(mappedBy = "institution")
    private List<TeacherEntity> teachers;

    @JsonIgnore
    @OneToMany(mappedBy = "institution")
    private List<StudentEntity> students;

}
