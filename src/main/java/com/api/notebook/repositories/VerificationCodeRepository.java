package com.api.notebook.repositories;

import com.api.notebook.enums.VerificationCodeEnum;
import com.api.notebook.models.entities.VerificationCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCodeEntity, Long> {

    Optional<VerificationCodeEntity> findByTeacherIdAndType(UUID teacherId, VerificationCodeEnum type);
    boolean existsByTeacherIdAndType(UUID teacherId, VerificationCodeEnum type);

}
