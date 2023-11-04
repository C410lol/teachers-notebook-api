package com.api.notebook.controllers;

import com.api.notebook.models.dtos.LessonDto;
import com.api.notebook.models.dtos.NotebookDto;
import com.api.notebook.models.entities.LessonEntity;
import com.api.notebook.models.entities.NotebookEntity;
import com.api.notebook.services.LessonService;
import com.api.notebook.services.NotebookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lessons")
public class LessonController {

    private final LessonService lessonService;
    private final NotebookService notebookService;

    @PostMapping("/create") //POST endpoint to create a lesson entity
    public ResponseEntity<Object> createLesson(@RequestParam(value = "notebookId") UUID notebookId,
                                               @RequestBody @Valid @NotNull LessonDto lessonDto) {
        var lessonEntity = new LessonEntity();
        BeanUtils.copyProperties(lessonDto, lessonEntity);
        if (lessonEntity.getDate() == null) {
            lessonEntity.setDate(LocalDate.now(ZoneId.of("UTC-3")));
        }
        notebookService.setLessonToNotebook(notebookId, lessonEntity);
        lessonService.saveLesson(lessonEntity);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/all") //GET endpoint to get all lessons
    public ResponseEntity<Object> getAllLessons(
            @RequestParam(value = "notebookId", required = false) UUID notebookId,
            @RequestParam(value = "pageNum", defaultValue = "0", required = false) String pageNum,
            @RequestParam(value = "direction", defaultValue = "desc", required = false) String direction,
            @RequestParam(value = "sortBy", defaultValue = "status", required = false) String sortBy
    ) {
        var pageable = PageRequest.of(
                Integer.parseInt(pageNum),
                10,
                Sort.Direction.fromString(direction),
                sortBy
        );
        if (notebookId != null) { //Verify if the param exists
            //If so, it returns a list of lessons based on this notebook id
            return ResponseEntity.ok(lessonService.findAllLessonsByNotebookId(notebookId, pageable));
        }
        return ResponseEntity.ok(lessonService.findAllLessons());
    }

    @GetMapping("/{lessonId}")
    public ResponseEntity<Object> getLessonById(@PathVariable(value = "lessonId") UUID lessonId) {
        var lessonOptional = lessonService.findLessonById(lessonId);
        if (lessonOptional.isPresent()) {
            return ResponseEntity.ok(lessonOptional.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Aula não encontrada!");
    }

    @PutMapping("/edit/{lessonId}")
    public ResponseEntity<Object> editLesson(@PathVariable(value = "lessonId") UUID lessonId,
                                               @RequestBody @Valid LessonDto lessonDto) {
        var lessonOptional = lessonService.findLessonById(lessonId);
        if (lessonOptional.isPresent()) {
            var lessonEntity = new LessonEntity();
            BeanUtils.copyProperties(lessonOptional.get(), lessonEntity);
            BeanUtils.copyProperties(lessonDto, lessonEntity);
            lessonService.saveLesson(lessonEntity);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Aula não encontrada!");
    }

    @DeleteMapping("/delete/{lessonId}")
    public ResponseEntity<Object> deleteLesson(@PathVariable(value = "lessonId") UUID lessonId) {
        var lessonOptional = lessonService.findLessonById(lessonId);
        if (lessonOptional.isPresent()) {
            lessonService.deleteLessonById(lessonId);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Aula não encontrada!");
    }

}
