package com.api.notebook.models.entities;

import com.api.notebook.enums.ClassEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name = "students")
public class StudentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "number")
    private Integer number;

    @Column(name = "is_order")
    private Boolean isOrder;

    @Column(name = "classe")
    @Enumerated(value = EnumType.STRING)
    private ClassEnum classe;

    @JsonIgnore
    @ManyToMany(mappedBy = "students", cascade = {CascadeType.ALL})
    private List<NotebookEntity> notebooks;

    @JsonIgnore
    @ManyToMany(mappedBy = "presentStudents", cascade = {CascadeType.ALL})
    private List<AttendanceEntity> presences;

    @JsonIgnore
    @ManyToMany(mappedBy = "absentStudents", cascade = {CascadeType.ALL})
    private List<AttendanceEntity> absences;

    @JsonIgnore
    @OneToMany(mappedBy = "student", cascade = {CascadeType.ALL})
    private List<GradeEntity> grades;

}
