package com.api.notebook.services;

import com.api.notebook.enums.StatusEnum;
import com.api.notebook.models.MissingTaskLessonModel;
import com.api.notebook.models.MissingTaskWorkModel;
import com.api.notebook.models.MissingTasksModel;
import com.api.notebook.models.entities.*;
import com.api.notebook.repositories.NotebookRepository;
import com.api.notebook.utils.NotebookUtils;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
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

    public MissingTasksModel verifyAllMissingTasks(UUID teacherId) {
        var notebooks = notebookRepository.findByTeacherId(teacherId);
        List<MissingTaskLessonModel> allMissingLessons = new ArrayList<>();
        List<MissingTaskWorkModel> allMissingWorks = new ArrayList<>();

        for (NotebookEntity notebook:
                notebooks) {
            var missingTasks = verifyMissingTasksByNotebook(notebook);
            allMissingLessons.addAll(missingTasks.getMissingLessons());
            allMissingWorks.addAll(missingTasks.getMissingWorks());
        }

        return new MissingTasksModel(allMissingLessons, allMissingWorks);
    }

    public MissingTasksModel verifyMissingTasksByNotebook(@NotNull NotebookEntity notebook) {
        List<MissingTaskLessonModel> missingLessons = new ArrayList<>();
        List<MissingTaskWorkModel> missingWorks = new ArrayList<>();
        for (LessonEntity lesson:
                notebook.getLessons()) {
            if (lesson.getAttendances().isEmpty()) {
                missingLessons.add(new MissingTaskLessonModel(
                        lesson.getId(),
                        lesson.getTitle(),
                        notebook.getId()
                ));
            }
        }
        for (WorkEntity work:
                notebook.getWorks()) {
            if (work.getGrades().size() != notebook.getStudents().size()) {
                missingWorks.add(new MissingTaskWorkModel(
                        work.getId(),
                        work.getTitle(),
                        notebook.getId()
                ));
            }
        }
        return new MissingTasksModel(missingLessons, missingWorks);
    }

    //Finish notebook and return all students average
    public ByteArrayResource finalizeNotebook(NotebookEntity notebook, Map<String, Integer> workTypeWeights) throws IOException {
        var finalizedNotebook = NotebookUtils.finalizeNotebook(notebook, workTypeWeights);
        var byteArrayResource = new ByteArrayResource(finalizedNotebook.toByteArray());

        notebook.setStatus(StatusEnum.OFF);
        notebook.setEndDate(LocalDate.now(ZoneId.of("UTC-3")));
        saveNotebook(notebook);

        return byteArrayResource;
    }

}
