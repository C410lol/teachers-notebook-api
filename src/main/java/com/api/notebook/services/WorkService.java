package com.api.notebook.services;

import com.api.notebook.models.entities.GradeEntity;
import com.api.notebook.models.entities.WorkEntity;
import com.api.notebook.repositories.WorkRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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

    public List<WorkEntity> findAllWorksByNotebookId(@NotNull List<WorkEntity> works, Long notebookId) {
        List<WorkEntity> notebookWorks = new ArrayList<>();
        for (WorkEntity work:
                works) {
            if (work.getNotebook().getId().equals(notebookId)) {
                notebookWorks.add(work);
            }
        }
        return notebookWorks;
    }

    public Optional<WorkEntity> findWorkById(Long id) {
        return workRepository.findById(id);
    }

    public void deleteWorkById(Long id) {
        workRepository.deleteById(id);
    }

    public void setGradeToWork(Long workId, @NotNull GradeEntity grade) { //Set grade to a work
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
