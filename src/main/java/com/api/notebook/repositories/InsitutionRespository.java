package com.api.notebook.repositories;

import com.api.notebook.models.entities.InstitutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InsitutionRespository extends JpaRepository<InstitutionEntity, UUID> {
}
