package com.api.notebook.services;

import com.api.notebook.enums.VerificationCodeEnum;
import com.api.notebook.models.entities.VerificationCodeEntity;
import com.api.notebook.repositories.VerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificationCodeService {

    private final VerificationCodeRepository verificationCodeRepository;

    public void save(VerificationCodeEntity verificationCode) {
        verificationCodeRepository.save(verificationCode);
    }

    public Optional<VerificationCodeEntity> findByTeacherId(UUID teacherId, VerificationCodeEnum type) {
        return verificationCodeRepository.findByTeacherIdAndType(teacherId, type);
    }

    public boolean existsByTeacherIdAndType(UUID teacherId, VerificationCodeEnum type) {
        return verificationCodeRepository.existsByTeacherIdAndType(teacherId, type);
    }

    public void deleteById(Long id) {
        verificationCodeRepository.deleteById(id);
    }

}
