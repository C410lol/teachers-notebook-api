package com.api.notebook.services;

import com.api.notebook.enums.VCodeEnum;
import com.api.notebook.models.entities.VCodeEntity;
import com.api.notebook.repositories.VCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VCodeService {

    private final VCodeRepository vCodeRepository;

    public void save(VCodeEntity verificationCode) {
        vCodeRepository.save(verificationCode);
    }

    public Optional<VCodeEntity> findByUserIdAndType(UUID userId, VCodeEnum type) {
        return vCodeRepository.findByUserIdAndType(userId, type);
    }

    public boolean existsByUserIdAndType(UUID userId, VCodeEnum type) {
        return vCodeRepository.existsByUserIdAndType(userId, type);
    }

    public void deleteById(Long id) {
        vCodeRepository.deleteById(id);
    }

}
