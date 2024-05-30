package com.api.notebook.services;

import com.api.notebook.models.entities.TeacherEntity;
import com.api.notebook.repositories.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;




    public List<TeacherEntity> findAllByInstitutionId(
            UUID institutionId
    ) {
        return teacherRepository.findAllByInstitutionId(institutionId);
    }

}
