package com.api.notebook.repositories;

import com.api.notebook.enums.ClassEnum;
import com.api.notebook.models.entities.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<StudentEntity, UUID> {

    List<StudentEntity> findAllByClasse(ClassEnum classEnum);

}
