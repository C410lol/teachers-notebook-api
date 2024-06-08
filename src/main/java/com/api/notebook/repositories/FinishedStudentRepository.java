package com.api.notebook.repositories;

import com.api.notebook.models.entities.FinishedStudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FinishedStudentRepository extends JpaRepository<FinishedStudentEntity, UUID> {
}
