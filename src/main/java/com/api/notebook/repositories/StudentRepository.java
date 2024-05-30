package com.api.notebook.repositories;

import com.api.notebook.enums.ClassEnum;
import com.api.notebook.models.entities.StudentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<StudentEntity, UUID> {

    @Query(
            value = "SELECT * FROM students " +
                    "WHERE institution_id = :institutionId " +
                    "AND " +
                    "classe = :classe",
            nativeQuery = true
    )
    Page<StudentEntity> findAllByInstitutionIdAndClasse(
            @Param(value = "institutionId") UUID institutionId,
            @Param(value = "classe") String classe,
            Pageable pageable
    );

    List<StudentEntity> findAllByClasse(ClassEnum classEnum);

    @Query(
            value = "SELECT COUNT(*) AS total_count FROM students " +
                    "WHERE classe = :classe",
            nativeQuery = true
    )
    int getSizeOfStudentsByClasse(@Param(value = "classe") String classe);

}
