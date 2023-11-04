package com.api.notebook.services;

import com.api.notebook.models.entities.GradeEntity;
import com.api.notebook.repositories.GradeRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GradeService {

    private final GradeRepository gradeRepository;

    public void saveGrade(GradeEntity grade) {
        gradeRepository.save(grade);
    }

    public List<GradeEntity> findAllGrades() {
        return gradeRepository.findAll();
    }

    public List<GradeEntity> findAllGradesByWorkId(UUID workId) {
        return gradeRepository.findByWorkId(workId);
    }

    public Optional<GradeEntity> findGradeById(UUID id) {
        return gradeRepository.findById(id);
    }

    public void deleteGradeById(UUID id) {
        gradeRepository.deleteById(id);
    }

}
