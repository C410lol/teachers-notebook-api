package com.api.notebook.models.entities;

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

    @OneToOne
    @JoinColumn(name = "creator_id", referencedColumnName = "id")
    private AdminEntity creator;

    @OneToMany(mappedBy = "institution")
    private List<AdminEntity> admins;

    @OneToMany(mappedBy = "institution")
    private List<TeacherEntity> teachers;

    @OneToMany(mappedBy = "institution")
    private List<StudentEntity> students;

}
