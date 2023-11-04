package com.api.notebook.models.entities;

import com.api.notebook.enums.RoleEnum;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name = "teachers")
public class TeacherEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "role")
    @Enumerated(value = EnumType.STRING)
    private RoleEnum role;

    @JsonIgnore
    @OneToMany(mappedBy = "teacher", cascade = {CascadeType.ALL})
    private List<NotebookEntity> notebooks;

    @JsonGetter(value = "notebooks")
    public Integer getNotebooksQuantity() {
        return notebooks.size();
    }

}
