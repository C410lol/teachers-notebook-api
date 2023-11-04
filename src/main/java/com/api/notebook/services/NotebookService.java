package com.api.notebook.services;

import com.api.notebook.enums.StatusEnum;
import com.api.notebook.models.FinalAverageModel;
import com.api.notebook.models.dtos.WorkTypeWeights;
import com.api.notebook.models.entities.*;
import com.api.notebook.repositories.NotebookRepository;
import com.api.notebook.utils.CalculationsUtils;
import com.api.notebook.utils.ExcelUtils;
import com.api.notebook.utils.NotebookUtils;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class NotebookService {

    private final NotebookRepository notebookRepository;

    public void saveNotebook(NotebookEntity notebook) {
        notebookRepository.save(notebook);
    }

    public List<NotebookEntity> findAllNotebooks() {
        return notebookRepository.findAll();
    }

    public Page<NotebookEntity> findAllNotebooksByTeacherId(UUID teacherId, Pageable pageable) {
        return notebookRepository.findByTeacherId(teacherId, pageable);
    }

    public Optional<NotebookEntity> findNotebookById(UUID id) {
        return notebookRepository.findById(id);
    }

    public void deleteNotebookById(UUID id) {
        notebookRepository.deleteById(id);
    }

    public void setLessonToNotebook(UUID notebookId, @NotNull LessonEntity lesson) { //Set lesson to a notebook
        var notebookOptional = findNotebookById(notebookId);
        notebookOptional.ifPresent(lesson::setNotebook);
    }

    public void setWorkToNotebook(UUID notebookId, @NotNull WorkEntity work) { //Set work to a notebook
        var notebookOptional = findNotebookById(notebookId);
        notebookOptional.ifPresent(work::setNotebook);
    }

    //Finish notebook and return all students average
    public ByteArrayResource finalizeNotebook(UUID notebookId, WorkTypeWeights workTypeWeights) {
        var notebook = findNotebookById(notebookId);
        if(notebook.isPresent()) {
            var allFinalAverageStudents = NotebookUtils.getAllFinalAverageStudents(notebook.get(), workTypeWeights);
            var byteArrayOutput = ExcelUtils.createFinalAverageExcelTable(allFinalAverageStudents);
            var byteArrayResource = new ByteArrayResource(byteArrayOutput.toByteArray());

            notebook.get().setStatus(StatusEnum.OFF);
            saveNotebook(notebook.get());

            return byteArrayResource;
        }
        return null;
    }



}
