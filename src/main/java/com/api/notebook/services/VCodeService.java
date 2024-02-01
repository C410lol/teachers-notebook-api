package com.api.notebook.services;

import com.api.notebook.enums.VCodeEnum;
import com.api.notebook.models.entities.VCodeEntity;
import com.api.notebook.repositories.VerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VCodeService {

    private final VerificationCodeRepository verificationCodeRepository;

    public void save(VCodeEntity verificationCode) {
        verificationCodeRepository.save(verificationCode);
    }

    public Optional<VCodeEntity> findByUserIdAndType(UUID userId, VCodeEnum type) {
        return verificationCodeRepository.findByUserIdAndType(userId, type);
    }

    public boolean existsByUserIdAndType(UUID userId, VCodeEnum type) {
        return verificationCodeRepository.existsByUserIdAndType(userId, type);
    }

    public void deleteById(Long id) {
        verificationCodeRepository.deleteById(id);
    }

}
