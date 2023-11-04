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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/works")
public class WorkController {

    private final WorkService workService;
    private final NotebookService notebookService;

    @PostMapping("/create") //POST endpoint to create a work entity
    public ResponseEntity<Object> createWork(@RequestParam(value = "notebookId") UUID notebookId,
                                               @RequestBody @Valid @NotNull WorkDto workDto) {
        var workEntity = new WorkEntity();
        BeanUtils.copyProperties(workDto, workEntity);
        notebookService.setWorkToNotebook(notebookId, workEntity);
        workService.saveWork(workEntity);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/all") //GET endpoint to get all works
    public ResponseEntity<Object> getAllWorks(
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
        if (notebookId != null) { //Check if the param exists
            //If exists, it returns a list based on this 'notebookId' param
            return ResponseEntity.ok(workService.findAllWorksByNotebookId(notebookId, pageable));
        }
        return ResponseEntity.ok(workService.findAllWorks());
    }

    @GetMapping("/{workId}")
    public ResponseEntity<Object> getWorkById(@PathVariable(value = "workId") UUID workId) {
        var workOptional = workService.findWorkById(workId);
        if (workOptional.isPresent()) {
            return ResponseEntity.ok(workOptional.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Trabalho/Tarefa não encontrada!");
    }

    @PutMapping("/edit/{workId}")
    public ResponseEntity<Object> editWork(@PathVariable(value = "workId") UUID workId,
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
    public ResponseEntity<Object> deleteWork(@PathVariable(value = "workId") UUID workId) {
        var workOptional = workService.findWorkById(workId);
        if (workOptional.isPresent()) {
            workService.deleteWorkById(workId);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Trabalho/Tarefa não encontrada!");
    }

}
