package com.api.notebook.controllers;

import com.api.notebook.enums.StatusEnum;
import com.api.notebook.models.WorkTypeWeightsModel;
import com.api.notebook.models.dtos.NotebookDto;
import com.api.notebook.models.entities.NotebookEntity;
import com.api.notebook.services.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notebooks")
public class NotebookController {

    private final NotebookService notebookService;
    private final TeacherService teacherService;
    private final StudentService studentService;

    @PostMapping("/create") //POST endpoint to create a notebook entity
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<Object> createNotebook(@RequestParam(value = "teacherId") UUID teacherId,
                                              @RequestBody @Valid @NotNull NotebookDto notebookDto) {
        var notebookEntity = new NotebookEntity();
        BeanUtils.copyProperties(notebookDto, notebookEntity);
        notebookEntity.setStatus(StatusEnum.ON);
        notebookEntity.setCreateDate(LocalDate.now(ZoneId.of("UTC-3")));
        teacherService.setNotebookToTeacher(teacherId, notebookEntity);
        studentService.setStudentsToNotebookByClass(notebookEntity.getClasse(), notebookEntity);
        notebookService.saveNotebook(notebookEntity);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/all") //GET endpoint to get all notebooks
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Object> getAllNotebooks() {
        var notebooks = notebookService.findAllNotebooks();
        if (notebooks.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(notebooks);
    }

    @GetMapping("/all/{teacherId}") //GET endpoint to get all notebooks
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<Object> getAllNotebooksByTeacherId(
            @PathVariable(value = "teacherId") UUID teacherId,
            @RequestParam(value = "pageNum", defaultValue = "0", required = false) String pageNum,
            @RequestParam(value = "direction", defaultValue = "desc", required = false) String direction,
            @RequestParam(value = "sortBy", defaultValue = "status", required = false) String sortBy
    ) {
        var authenticationId = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!authenticationId.equals(teacherId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        var pageable = PageRequest.of(
                Integer.parseInt(pageNum),
                10,
                Sort.Direction.fromString(direction),
                sortBy
        );
        var teacherNotebooks = notebookService.findAllNotebooksByTeacherId(teacherId, pageable);
        if (teacherNotebooks.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(teacherNotebooks);
    }

    @GetMapping("/{notebookId}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<Object> getNotebookById(@PathVariable(value = "notebookId") UUID notebookId) {
        var notebook = notebookService.findNotebookById(notebookId);
        if (notebook.isPresent()) {
            var authenticationId = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (notebook.get().getTeacher().getId().equals(authenticationId)) {
                return ResponseEntity.ok(notebook.get());
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Caderneta não encontrada!");
    }

    @PutMapping("/edit/{notebookId}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> editNotebook(@PathVariable(value = "notebookId") UUID notebookId,
                                               @RequestBody @Valid NotebookDto notebookDto) {
        var notebookOptional = notebookService.findNotebookById(notebookId);
        if (notebookOptional.isPresent()) {
            var authenticationId = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (notebookOptional.get().getTeacher().getId().equals(authenticationId)) {
                var notebookEntity = new NotebookEntity();
                BeanUtils.copyProperties(notebookOptional.get(), notebookEntity);
                BeanUtils.copyProperties(notebookDto, notebookEntity);
                studentService.setStudentsToNotebookByClass(notebookEntity.getClasse(), notebookEntity);
                notebookService.saveNotebook(notebookEntity);
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Caderneta não encontrada!");
    }

    @DeleteMapping("/delete/{notebookId}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> deleteNotebook(@PathVariable(value = "notebookId") UUID notebookId) {
        var notebookOptional = notebookService.findNotebookById(notebookId);
        if (notebookOptional.isPresent()) {
            var authenticationId = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (notebookOptional.get().getTeacher().getId().equals(authenticationId)) {
                notebookService.deleteNotebookById(notebookId);
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Caderneta não encontrada!");
    }

    @GetMapping("/{teacherId}/all-missing-tasks")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<Object> verifyAllMissingTasks(
            @PathVariable(value = "teacherId") @NotNull UUID teacherId
    ) {
        var authenticationId = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!teacherId.equals(authenticationId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        var allMissingTasks = notebookService.verifyAllMissingTasks(teacherId);
        if (allMissingTasks.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(allMissingTasks);
    }

    @GetMapping("/{notebookId}/missing-tasks")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<Object> verifyMissingTasks(
            @PathVariable(value = "notebookId") UUID notebookId
    ) {
        var notebookOptional = notebookService.findNotebookById(notebookId);
        if (notebookOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        var authenticationId = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!notebookOptional.get().getTeacher().getId().equals(authenticationId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        var missingTasks = notebookService.verifyMissingTasksByNotebook(notebookOptional.get());
        if (missingTasks.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(missingTasks);
    }

    @PutMapping("/finalize/{notebookId}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<Object> finalizeNotebook(
            @PathVariable(value = "notebookId") UUID notebookId,
            @RequestBody @Valid WorkTypeWeightsModel workTypeWeightsModel) {
        var notebookOptional = notebookService.findNotebookById(notebookId);
        if (notebookOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        var authenticationId = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!notebookOptional.get().getTeacher().getId().equals(authenticationId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        var file = notebookService.finalizeNotebook(notebookOptional.get(), workTypeWeightsModel);
        if (file != null) {

            var headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "caderneta.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(file);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Caderneta não encontrada!");
    }

}
