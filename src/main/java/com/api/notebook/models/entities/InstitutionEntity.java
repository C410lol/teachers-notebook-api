package com.api.notebook.models.entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Entity
@Table(name = "institutions")
public class InstitutionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(name = "id")
    private UUID id;

    @Column(name = "name")
    private String name;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "creator_id", referencedColumnName = "id")
    private AdminEntity creator;

    @JsonIgnore
    @OneToMany(mappedBy = "institution")
    private List<AdminEntity> admins;

    @JsonIgnore
    @OneToMany(mappedBy = "institution")
    private List<TeacherEntity> teachers;

    @JsonIgnore
    @OneToMany(mappedBy = "institution")
    private List<StudentEntity> students;




    @JsonGetter(value = "admins")
    public int getAdminsQuantity() {
        return admins.size();
    }

    @JsonGetter(value = "teachers")
    public int getTeachersQuantity() {
        return teachers.size();
    }

    @JsonGetter(value = "students")
    public int getStudentsQuantity() {
        return students.size();
    }

}
