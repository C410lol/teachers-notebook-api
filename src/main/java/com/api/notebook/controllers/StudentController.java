package com.api.notebook.controllers;

import com.api.notebook.models.dtos.StudentDto;
import com.api.notebook.models.entities.StudentEntity;
import com.api.notebook.services.NotebookService;
import com.api.notebook.services.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;
    private final NotebookService notebookService;

    @PostMapping("/create") //POST endpoint to create a student entity
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_USER')")
    public ResponseEntity<Object> createGrade(@RequestBody @Valid @NotNull StudentDto studentDto) {
        var studentEntity = new StudentEntity();
        BeanUtils.copyProperties(studentDto, studentEntity);
        studentService.saveStudent(studentEntity);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/all") //GET endpoint to get all students
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Object> getAllStudents() {
        var students = studentService.findAllStudents();
        if (students.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(students);
    }

    @GetMapping("/all/{notebookId}") //GET endpoint to get all students
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_USER')")
    public ResponseEntity<Object> getAllStudents(
            @PathVariable(value = "notebookId") UUID notebookId
    ) {
        var optionalNotebook = notebookService.findNotebookById(notebookId);
        if (optionalNotebook.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(optionalNotebook.get().getStudents());
    }

}
