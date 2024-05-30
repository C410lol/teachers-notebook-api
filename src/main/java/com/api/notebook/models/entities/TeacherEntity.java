package com.api.notebook.models.entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "teachers")
public class TeacherEntity extends UserEntity {

    @JsonIgnore
    @OneToMany(mappedBy = "teacher", cascade = {CascadeType.ALL})
    private List<NotebookEntity> notebooks;




    @JsonGetter(value = "notebooks")
    public int getNotebooksSize() {
        return notebooks.size();
    }

}
