package com.api.notebook.repositories;

import com.api.notebook.models.entities.LessonEntity;
import com.api.notebook.models.entities.WorkEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WorkRepository extends JpaRepository<WorkEntity, UUID> {
    Page<WorkEntity> findByNotebookId(UUID notebookId, Pageable pageable);

}
