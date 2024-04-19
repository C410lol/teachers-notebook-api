package com.api.notebook.controllers;

import com.api.notebook.models.dtos.StudentDto;
import com.api.notebook.models.entities.StudentEntity;
import com.api.notebook.services.NotebookService;
import com.api.notebook.services.StudentService;
import com.api.notebook.utils.StudentComparator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;
    private final NotebookService notebookService;

    @PostMapping("/create") //POST endpoint to create a student entity
    @PreAuthorize("hasAnyRole('ROLE_TCHR', 'ROLE_ADM')")
    public ResponseEntity<Object> createGrade(
            @RequestBody @Valid @NotNull StudentDto studentDto
    ) {
        var studentEntity = new StudentEntity();
        BeanUtils.copyProperties(studentDto, studentEntity);

        var allStudentsByClasse = studentService.findAllStudentsByClasse(studentDto.getClasse());

        if (!studentDto.getIsOrder()) {
            var studentEntity1 = allStudentsByClasse.get(allStudentsByClasse.size() - 1);
            studentEntity.setNumber(studentEntity1.getNumber() + 1);

            studentService.saveStudent(studentEntity);
            return ResponseEntity.status(HttpStatus.CREATED).body("Aluno criado com sucesso!");
        }

        List<StudentEntity> allStudents = new ArrayList<>();
        List<StudentEntity> notOrderStudents = new ArrayList<>();

        allStudents.add(studentEntity);
        allStudentsByClasse.forEach((element) -> {
            if (element.getIsOrder()) {
                allStudents.add(element);
            } else notOrderStudents.add(element);
        });
        allStudents.sort(new StudentComparator());
        allStudents.addAll(notOrderStudents);

        var newStudentIndex = allStudents.indexOf(studentEntity);
        for (int x = newStudentIndex; x < allStudents.size(); x++) {
            var studentEntity1 = allStudents.get(x);
            if (studentEntity1 == null) continue;

            studentEntity1.setNumber(x + 1);
            studentService.saveStudent(studentEntity1);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body("Aluno criado com sucesso!");
    }

    @GetMapping("/all") //GET endpoint to get all students
    @PreAuthorize("hasRole('ROLE_ADM')")
    public ResponseEntity<Object> getAllStudents() {
        var students = studentService.findAllStudents();
        if (students.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(students);
    }

    @GetMapping("/all/{notebookId}") //GET endpoint to get all students
    @PreAuthorize("hasAnyRole('ROLE_TCHR', 'ROLE_ADM')")
    public ResponseEntity<Object> getAllStudents(
            @PathVariable(value = "notebookId") UUID notebookId
    ) {
        var optionalNotebook = notebookService.findNotebookById(notebookId);
        if (optionalNotebook.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(studentService.findAllStudentsByClasse(optionalNotebook.get().getClasse()));
    }


    //DELETE

    @DeleteMapping("/{studentId}/delete")
    public ResponseEntity<?> deleteStudent(
            @PathVariable(value = "studentId") UUID studentId
    ) {
        var studentOptional = studentService.findStudentById(studentId);
        if (studentOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Aluno não encontrado!");
        }

        var allByClasse = studentService.findAllStudentsByClasse(studentOptional.get().getClasse());
        studentService.deleteStudentById(studentOptional.get().getId());

        for (int x = allByClasse.indexOf(studentOptional.get()) + 1; x < allByClasse.size(); x++) {
            var studentEntity1 = allByClasse.get(x);
            if (studentEntity1 == null) continue;

            studentEntity1.setNumber(x);
            studentService.saveStudent(studentEntity1);
        }

        return ResponseEntity.ok("Aluno deletado com sucesso!");
    }

    //DELETE

}
