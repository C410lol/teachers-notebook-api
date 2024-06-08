package com.api.notebook.services;

import com.api.notebook.models.entities.FinishedStudentEntity;
import com.api.notebook.repositories.FinishedStudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FinishedStudentService {

    private final FinishedStudentRepository finishedStudentRepository;




    public void save(
            FinishedStudentEntity finishedStudent
    ) {
        finishedStudentRepository.save(finishedStudent);
    }




    public Optional<FinishedStudentEntity> findById(
            UUID id
    ) {
        return finishedStudentRepository.findById(id);
    }

}
