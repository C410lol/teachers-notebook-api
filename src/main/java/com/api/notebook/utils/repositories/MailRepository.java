package com.api.notebook.utils.repositories;

import com.api.notebook.models.entities.EmailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MailRepository extends JpaRepository<EmailEntity, UUID> {
}
