package com.api.notebook.repositories;

import com.api.notebook.models.entities.NotebookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotebookRepository extends JpaRepository<NotebookEntity, Long> {
}
