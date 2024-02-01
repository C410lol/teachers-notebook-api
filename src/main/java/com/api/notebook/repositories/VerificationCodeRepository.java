package com.api.notebook.repositories;

import com.api.notebook.enums.VCodeEnum;
import com.api.notebook.models.entities.VCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VCodeEntity, Long> {

    Optional<VCodeEntity> findByUserIdAndType(UUID teacherId, VCodeEnum type);
    boolean existsByUserIdAndType(UUID teacherId, VCodeEnum type);

}
