package com.api.notebook.models.entities;

import com.fasterxml.jackson.annotation.JsonGetter;
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

    @ManyToMany
    @JoinTable(
            name = "lessons_bncc_codes",
            joinColumns = {@JoinColumn(name = "lesson_id")},
            inverseJoinColumns = {@JoinColumn(name = "bncc_code_id")}
    )
    private List<BNCCCodeEntity> bnccCodes;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "notebook_id")
    private NotebookEntity notebook;

    @JsonIgnore
    @OneToMany(mappedBy = "lesson", cascade = {CascadeType.ALL})
    private List<AttendanceEntity> attendances;

    @JsonGetter(value = "attendances")
    public Integer getAttendancesQuantity() {
        return attendances.size();
    }

}
