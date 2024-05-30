package com.api.notebook.repositories;

import com.api.notebook.models.entities.NotebookEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotebookRepository extends JpaRepository<NotebookEntity, UUID> {

    @Query(value = "SELECT * FROM notebooks WHERE teacher_id = :teacherId " +
            "AND bimester LIKE :bimester", nativeQuery = true)
    Page<NotebookEntity> findByTeacherId(
            @Param(value = "teacherId") UUID teacherId,
            @Param(value = "bimester") String bimesterFilter,
            Pageable pageable);

    List<NotebookEntity> findByTeacherId(UUID teacherId);

}
