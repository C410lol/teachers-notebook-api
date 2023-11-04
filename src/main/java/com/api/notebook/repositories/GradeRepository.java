package com.api.notebook.repositories;

import com.api.notebook.models.entities.GradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GradeRepository extends JpaRepository<GradeEntity, UUID> {

    List<GradeEntity> findByWorkId(UUID workId);

}
