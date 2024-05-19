package com.api.notebook.services;

import com.api.notebook.models.entities.AdminEntity;
import com.api.notebook.repositories.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;




    public Optional<AdminEntity> findById(
            UUID adminId
    ) {
        return adminRepository.findById(adminId);
    }

}
