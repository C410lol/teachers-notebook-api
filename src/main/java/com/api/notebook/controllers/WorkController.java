package com.api.notebook.controllers;

import com.api.notebook.models.dtos.LessonDto;
import com.api.notebook.models.dtos.WorkDto;
import com.api.notebook.models.entities.LessonEntity;
import com.api.notebook.models.entities.WorkEntity;
import com.api.notebook.services.NotebookService;
import com.api.notebook.services.WorkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/works")
public class WorkController {

    private final WorkService workService;
    private final NotebookService notebookService;

    @PostMapping("/create") //POST endpoint to create a work entity
    public ResponseEntity<Object> createWork(@RequestParam(value = "notebookId") Long notebookId,
                                               @RequestBody @Valid @NotNull WorkDto workDto) {
        var workEntity = new WorkEntity();
        BeanUtils.copyProperties(workDto, workEntity);
        notebookService.setWorkToNotebook(notebookId, workEntity);
        workService.saveWork(workEntity);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/all") //GET endpoint to get all works
    public ResponseEntity<Object> getAllWorks(
            @RequestParam(value = "notebookId", required = false) Long notebookId) {
        var workList = workService.findAllWorks();
        if (!workList.isEmpty()) {
            if (notebookId != null) { //Check if the param exists
                return ResponseEntity.ok(workService.findAllWorksByNotebookId(workList, notebookId));
            }
            return ResponseEntity.ok(workList);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @GetMapping("/{workId}")
    public ResponseEntity<Object> getWorkById(@PathVariable(value = "workId") Long workId) {
        var workOptional = workService.findWorkById(workId);
        if (workOptional.isPresent()) {
            return ResponseEntity.ok(workOptional.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Trabalho/Tarefa não encontrada!");
    }

    @PutMapping("/edit/{workId}")
    public ResponseEntity<Object> editWork(@PathVariable(value = "workId") Long workId,
                                             @RequestBody @Valid WorkDto workDto) {
        var workOptional = workService.findWorkById(workId);
        if (workOptional.isPresent()) {
            var workEntity = new WorkEntity();
            BeanUtils.copyProperties(workOptional.get(), workEntity);
            BeanUtils.copyProperties(workDto, workEntity);
            workService.saveWork(workEntity);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Trabalho/Tarefa não encontrada!");
    }

    @DeleteMapping("/delete/{workId}")
    public ResponseEntity<Object> deleteWork(@PathVariable(value = "workId") Long workId) {
        var workOptional = workService.findWorkById(workId);
        if (workOptional.isPresent()) {
            workService.deleteWorkById(workId);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Trabalho/Tarefa não encontrada!");
    }

}
