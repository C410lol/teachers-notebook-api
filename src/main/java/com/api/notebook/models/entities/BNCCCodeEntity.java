package com.api.notebook.models.entities;

import com.api.notebook.enums.ClassEnum;
import com.api.notebook.enums.SubjectEnum;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name = "bncc_codes")
public class BNCCCodeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "code")
    private String code;

    @Column(name = "description")
    private String description;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "subjects")
    private List<SubjectEnum> subjects;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "classes")
    private List<ClassEnum> classes;

    @JsonIgnore
    @ManyToMany(mappedBy = "bnccCodes")
    private List<LessonEntity> lessons;

    @JsonGetter(value = "description")
    public String getDescription() {
        return "(" + code + ") " + description;
    }

}
