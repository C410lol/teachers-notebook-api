package com.api.notebook.services;

import com.api.notebook.models.entities.GradeEntity;
import com.api.notebook.models.entities.WorkEntity;
import com.api.notebook.repositories.WorkRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class WorkService {

    private final WorkRepository workRepository;

    public void saveWork(WorkEntity work) {
        workRepository.save(work);
    }

    public List<WorkEntity> findAllWorks() {
        var workList = workRepository.findAll();
        workList.sort(Comparator.comparing(WorkEntity::getDeliveryDate).reversed());
        return workList;
    }

    public Page<WorkEntity> findAllWorksByNotebookId(UUID notebookId, Pageable pageable) {
        return workRepository.findByNotebookId(notebookId, pageable);
    }

    public Optional<WorkEntity> findWorkById(UUID id) {
        return workRepository.findById(id);
    }

    public void deleteWorkById(UUID id) {
        workRepository.deleteById(id);
    }

    public void setGradeToWork(UUID workId, @NotNull GradeEntity grade) { //Set grade to a work
        var workOptional = findWorkById(workId);
        if(workOptional.isPresent()) {
            for (GradeEntity gradeLoop:
                    workOptional.get().getGrades()) {
                if (gradeLoop.getStudent().equals(grade.getStudent())) {
                    grade.setId(gradeLoop.getId());
                }
            }
            grade.setWork(workOptional.get());
        }
    }

}
