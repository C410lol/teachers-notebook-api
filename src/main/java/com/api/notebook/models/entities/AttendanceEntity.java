package com.api.notebook.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name = "attendances")
public class AttendanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "lesson_id")
    private LessonEntity lesson;

    @ManyToMany
    @JoinTable(
            name = "attendances_present_students",
            joinColumns = {@JoinColumn(name = "attendance_id")},
            inverseJoinColumns = {@JoinColumn(name = "student_id")})
    private List<StudentEntity> presentStudents;

    @ManyToMany
    @JoinTable(
            name = "attendances_absent_students",
            joinColumns = {@JoinColumn(name = "attendance_id")},
            inverseJoinColumns = {@JoinColumn(name = "student_id")})
    private List<StudentEntity> absentStudents;

}

