package com.api.notebook.repositories;

import com.api.notebook.models.entities.TeacherEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeacherRepository extends JpaRepository<TeacherEntity, UUID> {

    Optional<TeacherEntity> findByEmail(String email);

}
