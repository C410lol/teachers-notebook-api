package com.api.notebook.repositories;

import com.api.notebook.models.entities.NotebookEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotebookRepository extends JpaRepository<NotebookEntity, UUID> {

    Page<NotebookEntity> findByTeacherId(UUID teacherId, Pageable pageable);
    List<NotebookEntity> findByTeacherId(UUID teacherId);

}
