package com.api.notebook.services;

import com.api.notebook.models.entities.InstitutionEntity;
import com.api.notebook.repositories.InsitutionRespository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InstitutionService {

    private final InsitutionRespository insitutionRespository;




    public void save(InstitutionEntity institution) {
        insitutionRespository.save(institution);
    }


    public List<InstitutionEntity> findAll() {
        return insitutionRespository.findAll();
    }

    public Optional<InstitutionEntity> findById(UUID institutionId) {
        return insitutionRespository.findById(institutionId);
    }

}
