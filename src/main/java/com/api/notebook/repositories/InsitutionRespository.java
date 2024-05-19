package com.api.notebook.repositories;

import com.api.notebook.models.entities.InstitutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InsitutionRespository extends JpaRepository<InstitutionEntity, UUID> {

    @Query(value = "SELECT * FROM institutions " +
            "WHERE UPPER(name) LIKE UPPER(concat('%', :pattern, '%'))", nativeQuery = true)
    List<InstitutionEntity> findAllByNamePattern(@Param(value = "pattern") String name);

}
