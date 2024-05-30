package com.api.notebook.models.entities;

import com.api.notebook.enums.RoleEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Inheritance(strategy = InheritanceType.JOINED)
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    private String email;

    private String password;

    @Enumerated(value = EnumType.STRING)
    private RoleEnum role;

    private Boolean verified;

    @ManyToOne
    @JoinColumn(name = "institution_id")
    private InstitutionEntity institution;




    public boolean isVerified() { return verified; }

}
