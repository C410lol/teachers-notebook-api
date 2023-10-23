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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;
    private final NotebookService notebookService;

    @PostMapping("/create") //POST endpoint to create a student entity
    public ResponseEntity<Object> createGrade(@RequestBody @Valid @NotNull StudentDto studentDto) {
        var studentEntity = new StudentEntity();
        BeanUtils.copyProperties(studentDto, studentEntity);
        studentService.saveStudent(studentEntity);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/all") //GET endpoint to get all students
    public ResponseEntity<Object> getAllStudents(
            @RequestParam(value = "notebookId", required = false) UUID notebookId) {
        var studentList = studentService.findAllStudents();
        if (!studentList.isEmpty()) {
            if (notebookId != null) {
                var optionalNotebook = notebookService.findNotebookById(notebookId);
                if (optionalNotebook.isPresent()) {
                    return ResponseEntity.ok(optionalNotebook.get().getStudents());
                } else return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.ok(studentList);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

}
