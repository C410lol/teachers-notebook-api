package com.api.notebook.repositories;

import com.api.notebook.models.entities.AdminEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AdminRepository extends JpaRepository<AdminEntity, UUID> {

    List<AdminEntity> findAllByInstitutionId(UUID institutionId);

}
