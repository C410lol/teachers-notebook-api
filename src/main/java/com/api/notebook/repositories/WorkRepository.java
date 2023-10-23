package com.api.notebook.repositories;

import com.api.notebook.models.entities.WorkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WorkRepository extends JpaRepository<WorkEntity, UUID> {
}
