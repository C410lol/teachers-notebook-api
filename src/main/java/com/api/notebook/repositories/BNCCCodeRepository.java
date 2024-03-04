package com.api.notebook.repositories;

import com.api.notebook.models.entities.BNCCCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BNCCCodeRepository extends JpaRepository<BNCCCodeEntity, UUID> {

    @Query(
            value = "SELECT * FROM bncc_codes WHERE (UPPER(code) LIKE " +
                    "UPPER(concat('%', :pattern, '%')) OR UPPER(description) " +
                    "LIKE UPPER(concat('%', :pattern, '%'))) " +
                    "AND " +
                    ":subject = ANY(subjects) " +
                    "AND " +
                    ":classe = ANY(classes)",
            nativeQuery = true
    )
    List<BNCCCodeEntity> findByMatchingPatternAndSubjectAndClasse(
            @Param(value = "pattern") String pattern,
            @Param(value = "subject") String subject,
            @Param(value = "classe") String classe
    );

    Optional<BNCCCodeEntity> findByCode(String code);

}
