package com.api.notebook.controllers;

import com.api.notebook.enums.ClassEnum;
import com.api.notebook.models.dtos.StudentDto;
import com.api.notebook.models.entities.AttendanceEntity;
import com.api.notebook.models.entities.StudentEntity;
import com.api.notebook.services.AttendanceService;
import com.api.notebook.services.InstitutionService;
import com.api.notebook.services.NotebookService;
import com.api.notebook.services.StudentService;
import com.api.notebook.utils.StudentComparator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/students")
public class StudentController {

    private final InstitutionService institutionService;
    private final StudentService studentService;
    private final NotebookService notebookService;
    private final AttendanceService attendanceService;

    @PostMapping("/create") //POST endpoint to create a student entity
    @PreAuthorize("hasAnyRole('ROLE_ADM', 'ROLE_SUPER')")
    public ResponseEntity<Object> createStudent(
            @RequestBody @Valid @NotNull StudentDto studentDto,
            @RequestParam(value = "institutionId") UUID institutionId
    ) {
        var institutionOptional = institutionService.findById(institutionId);
        if (institutionOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Instituição não encontrada.");
        }

        var studentEntity = new StudentEntity();
        BeanUtils.copyProperties(studentDto, studentEntity);
        studentEntity.setInstitution(institutionOptional.get());

        var students = studentService.findAllStudentsByClasse(studentDto.getClasse());

        if (!studentDto.getIsOrder()) {
            studentEntity.setNumber(students.size() + 1);
            studentService.saveStudent(studentEntity);

            return ResponseEntity.ok("Aluno criado com sucesso!");
        }

        students.sort(Comparator.comparing(StudentEntity::getNumber));
        insertOrderedStudentInClass(students, studentEntity);

        return ResponseEntity.status(HttpStatus.CREATED).body("Aluno criado com sucesso!");
    }


    private void insertOrderedStudentInClass(
            @NotNull List<StudentEntity> students,
            @NotNull StudentEntity studentEntity
    ) {
        List<StudentEntity> orderedStudents = new ArrayList<>();
        List<StudentEntity> notOrderedStudents = new ArrayList<>();

        orderedStudents.add(studentEntity);
        students.forEach((element) -> {
            if (element.getIsOrder()) {
                orderedStudents.add(element);
            } else notOrderedStudents.add(element);
        });

        orderedStudents.sort(new StudentComparator());
        orderedStudents.addAll(notOrderedStudents);

        var studentNumberCount = 1;
        for (StudentEntity student:
                orderedStudents) {
            student.setNumber(studentNumberCount);
            studentService.saveStudent(student);

            studentNumberCount++;
        }
    }




    //READ

    @GetMapping("/{institutionId}/all")
    public ResponseEntity<?> getAllByInstitutionAndClasse(
            @PathVariable(value = "institutionId") UUID institutionId,

            @RequestParam(value = "classe", defaultValue = "%", required = false) String classe,

            @RequestParam(value = "pageNum", defaultValue = "0", required = false) String pageNum,
            @RequestParam(value = "direction", defaultValue = "asc", required = false) String direction,
            @RequestParam(value = "sortBy", defaultValue = "number", required = false) String sortBy
    ) {
        var institutionOptional = institutionService.findById(institutionId);
        if (institutionOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Instituição não encontrada.");
        }

        var pageable = PageRequest.of(
                Integer.parseInt(pageNum),
                30,
                Sort.Direction.fromString(direction),
                sortBy
        );
        var students = studentService.findAllByInstitutionAndClasse(institutionId, classe, pageable);
        if (students.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Nenhum aluno encontrado");
        }

        return ResponseEntity.ok(students);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllByClasse(
            @RequestParam(value = "classe", required = false) String classe
    ) {
        List<StudentEntity> students;

        if (classe != null) {
            students = studentService.findAllStudentsByClasse(ClassEnum.valueOf(classe));
        } else students = studentService.findAllStudents();

        if (students.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhum aluno encontrado para essa turma!");
        }

        return ResponseEntity.ok(students);
    }


    @GetMapping("/all/{notebookId}") //GET endpoint to get all students
    public ResponseEntity<Object> getAllStudents(
            @PathVariable(value = "notebookId") UUID notebookId
    ) {
        var optionalNotebook = notebookService.findNotebookById(notebookId);
        if (optionalNotebook.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(studentService.findAllStudentsByClasse(optionalNotebook.get().getClasse()));
    }

    @GetMapping("/size")
    public ResponseEntity<?> getStudentsSizeByClasse(
            @RequestParam(value = "classe") ClassEnum classEnum
    ) {
        return ResponseEntity.ok(studentService.getSizeOfStudentsByClasse(classEnum));
    }

    //READ




    //EDIT


    @PutMapping("/{studentId}/edit")
    @PreAuthorize("hasAnyRole('ROLE_ADM', 'ROLE_SUPER')")
    public ResponseEntity<Object> editStudent(
            @PathVariable(value = "studentId") UUID studentId,
            @RequestBody @Valid StudentDto studentDto
    ) {
        var studentOptional = studentService.findStudentById(studentId);
        if (studentOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Aluno não encontrado!");
        }

        if (!studentOptional.get().getClasse().equals(studentDto.getClasse())) {
            editLastClasse(studentOptional.get().getClasse(), studentOptional.get().getNumber());

            BeanUtils.copyProperties(studentDto, studentOptional.get());

            var newClasseStudents = studentService.findAllStudentsByClasse(studentDto.getClasse());

            if (!studentDto.getIsOrder()) {
                studentOptional.get().setNumber(newClasseStudents.size() + 1);
                studentService.saveStudent(studentOptional.get());

                return ResponseEntity.ok("Aluno editado com sucesso!");
            }

            newClasseStudents.sort(Comparator.comparing(StudentEntity::getNumber));
            insertOrderedStudentInClass(newClasseStudents, studentOptional.get());

            return ResponseEntity.ok("Aluno editado com sucesso!");
        }

        var lastClasseStudents = studentService.findAllStudentsByClasse(studentOptional.get().getClasse());
        lastClasseStudents.sort(Comparator.comparing(StudentEntity::getNumber));

        BeanUtils.copyProperties(studentDto, studentOptional.get());

        if (!studentDto.getIsOrder()) {
            editNotOrderedStudentInClasse(lastClasseStudents, studentOptional.get());
            studentService.saveStudent(studentOptional.get());

            return ResponseEntity.ok("Aluno editado com sucesso!");
        }

        editOrderesStudentInClasse(lastClasseStudents, studentOptional.get());

        return ResponseEntity.ok("Aluno editado com sucesso!");
    }


    private void editNotOrderedStudentInClasse(
            @NotNull List<StudentEntity> students,
            @NotNull StudentEntity studentEntity
    ) {
        for (
                int index = studentEntity.getNumber();
                index < students.size();
                index++
        ) {
            var currentStudent = students.get(index);
            if (currentStudent == null) continue;

            currentStudent.setNumber(currentStudent.getNumber() - 1);
            studentService.saveStudent(currentStudent);
        }

        studentEntity.setNumber(students.size());
    }


    private void editOrderesStudentInClasse(
            @NotNull List<StudentEntity> students,
            @NotNull StudentEntity studentEntity
    ) {
        BeanUtils.copyProperties(studentEntity, students.get(students.indexOf(studentEntity)));

        List<StudentEntity> orderedStudents = new ArrayList<>();
        List<StudentEntity> notOrderedStudents = new ArrayList<>();

        for (StudentEntity student:
                students) {
            if (student.getIsOrder()) {
                orderedStudents.add(student);
                continue;
            }
            notOrderedStudents.add(student);
        }

        orderedStudents.sort(new StudentComparator());
        orderedStudents.addAll(notOrderedStudents);

        var studentNumberCount = 1;
        for (StudentEntity student:
                orderedStudents) {
            student.setNumber(studentNumberCount);
            studentService.saveStudent(student);

            studentNumberCount++;
        }
    }

    private void editLastClasse(
            ClassEnum classe,
            Integer studentNumber
    ) {
        var students = studentService.findAllStudentsByClasse(classe);
        students.sort(Comparator.comparing(StudentEntity::getNumber));

        for (int x = studentNumber; x < students.size(); x++) {
            StudentEntity student = students.get(x);
            if (student == null) continue;

            student.setNumber(student.getNumber() - 1);
            studentService.saveStudent(student);
        }
    }

    //EDIT




    //DELETE

    @DeleteMapping("/{studentId}/delete")
    @PreAuthorize("hasAnyRole('ROLE_ADM', 'ROLE_SUPER')")
    public ResponseEntity<?> deleteStudent(
            @PathVariable(value = "studentId") UUID studentId
    ) {
        var studentOptional = studentService.findStudentById(studentId);
        if (studentOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Aluno não encontrado!");
        }

        var allByClasse = studentService.findAllStudentsByClasse(studentOptional.get().getClasse());

        for (AttendanceEntity attendance:
                studentOptional.get().getPresences()) {
            attendance.getPresentStudents().remove(studentOptional.get());
        }
        for (AttendanceEntity attendance:
                studentOptional.get().getAbsences()) {
            attendance.getAbsentStudents().remove(studentOptional.get());
        }
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
