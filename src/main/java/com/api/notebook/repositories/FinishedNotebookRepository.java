package com.api.notebook.repositories;

import com.api.notebook.enums.BimesterEnum;
import com.api.notebook.enums.ClassEnum;
import com.api.notebook.models.entities.FinishedNotebookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FinishedNotebookRepository extends JpaRepository<FinishedNotebookEntity, UUID> {

    Optional<FinishedNotebookEntity> findByNotebookId(UUID notebookId);

    @Query(
            value = "SELECT finished_notebooks.* FROM finished_notebooks " +
                    "JOIN notebooks ON finished_notebooks.notebook_id = notebooks.id " +
                    "JOIN users ON notebooks.teacher_id = users.id " +
                    "JOIN institutions ON users.institution_id = institutions.id " +
                    "WHERE institutions.id = :institutionId " +
                    "AND notebooks.classe = :classe " +
                    "AND notebooks.bimester = :bimester",
            nativeQuery = true
    )
    List<FinishedNotebookEntity> findAllByInstitutionIdAndNotebookClasseAndNotebookBimester(
            @Param(value = "institutionId") UUID institutionId,
            @Param(value = "classe") String notebookClasse,
            @Param(value = "bimester") String notebookBimester
    );

}
