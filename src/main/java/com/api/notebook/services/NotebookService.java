package com.api.notebook.services;

import com.api.notebook.enums.StatusEnum;
import com.api.notebook.models.MissingTaskModel;
import com.api.notebook.models.MissingTasksModel;
import com.api.notebook.models.entities.LessonEntity;
import com.api.notebook.models.entities.NotebookEntity;
import com.api.notebook.models.entities.StudentEntity;
import com.api.notebook.models.entities.WorkEntity;
import com.api.notebook.utils.NotebookUtils;
import com.api.notebook.utils.repositories.NotebookRepository;
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

    public Page<NotebookEntity> findAllNotebooksByTeacherId(
            UUID teacherId, String bimesterFilter, Pageable pageable
    ) {
        return notebookRepository.findByUserId(teacherId, bimesterFilter, pageable);
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
        var notebooks = notebookRepository.findByUserId(teacherId);
        List<MissingTaskModel> allMissingLessons = new ArrayList<>();
        List<MissingTaskModel> allMissingWorks = new ArrayList<>();

        for (NotebookEntity notebook :
                notebooks) {
            var missingTasks = verifyMissingTasksByNotebook(notebook);
            allMissingLessons.addAll(missingTasks.getMissingLessons());
            allMissingWorks.addAll(missingTasks.getMissingWorks());
        }

        return new MissingTasksModel(allMissingLessons, allMissingWorks);
    }

    public MissingTasksModel verifyMissingTasksByNotebook(@NotNull NotebookEntity notebook) {
        List<MissingTaskModel> missingLessons = new ArrayList<>();
        List<MissingTaskModel> missingWorks = new ArrayList<>();
        for (LessonEntity lesson :
                notebook.getLessons()) {
            if (lesson.getAttendances().isEmpty()) {
                missingLessons.add(new MissingTaskModel(
                        lesson.getId(),
                        lesson.getTitle(),
                        notebook.getId()
                ));
            }
        }
        for (WorkEntity work :
                notebook.getWorks()) {
            if (work.getGrades().size() < notebook.getStudents().size()) {
                missingWorks.add(new MissingTaskModel(
                        work.getId(),
                        work.getTitle(),
                        notebook.getId()
                ));
            }
        }
        return new MissingTasksModel(missingLessons, missingWorks);
    }

    //Finish notebook and return all students average
    public ByteArrayResource finalizeNotebook(
            NotebookEntity notebook,
            List<StudentEntity> students,
            Map<String, Integer> workTypeWeights
    ) throws IOException {
        var finalizedNotebook = NotebookUtils.finalizeNotebook(notebook, students, workTypeWeights);
        var byteArrayResource = new ByteArrayResource(finalizedNotebook.toByteArray());

        notebook.setStatus(StatusEnum.OFF);
        notebook.setEndDate(LocalDate.now(ZoneId.of("UTC-3")));
        saveNotebook(notebook);

        return byteArrayResource;
    }

}
