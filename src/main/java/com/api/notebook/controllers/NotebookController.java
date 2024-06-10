package com.api.notebook.controllers;

import com.api.notebook.enums.StatusEnum;
import com.api.notebook.models.dtos.NotebookDto;
import com.api.notebook.models.entities.FinishedNotebookEntity;
import com.api.notebook.models.entities.FinishedStudentEntity;
import com.api.notebook.models.entities.NotebookEntity;
import com.api.notebook.models.entities.StudentEntity;
import com.api.notebook.services.*;
import com.api.notebook.utils.NotebookUtils;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notebooks")
public class    NotebookController {

    private final NotebookService notebookService;
    private final UserService userService;
    private final StudentService studentService;
    private final FinishedNotebookService finishedNotebookService;
    private final FinishedStudentService finishedStudentService;


    //CREATE

    @PostMapping("/create") //POST endpoint to create a notebook entity
    public ResponseEntity<Object> createNotebook(
            @RequestParam(value = "teacherId") UUID teacherId,
            @RequestBody @Valid @NotNull NotebookDto notebookDto
    ) {
        var notebookEntity = new NotebookEntity();
        BeanUtils.copyProperties(notebookDto, notebookEntity);
        notebookEntity.setStatus(StatusEnum.ON);
        notebookEntity.setCreateDate(LocalDate.now(ZoneId.of("UTC-3")));
        userService.setNotebookToUser(teacherId, notebookEntity);
        notebookService.saveNotebook(notebookEntity);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //CREATE


    //READ

//    @GetMapping("/all") //GET endpoint to get all notebooks
//    @PreAuthorize("hasRole('ROLE_ADM')")
//    public ResponseEntity<Object> getAllNotebooks() {
//        var notebooks = notebookService.findAllNotebooks();
//        if (notebooks.isEmpty()) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        }
//        return ResponseEntity.ok(notebooks);
//    }

    @GetMapping("/all/{teacherId}") //GET endpoint to get all notebooks
    public ResponseEntity<Object> getAllNotebooksByTeacherId(
            @PathVariable(value = "teacherId") UUID teacherId,

            @RequestParam(value = "bimester", defaultValue = "%", required = false) String bimesterFilter,

            @RequestParam(value = "pageNum", defaultValue = "0", required = false) String pageNum,
            @RequestParam(value = "direction", defaultValue = "desc", required = false) String direction,
            @RequestParam(value = "sortBy", defaultValue = "status", required = false) String sortBy
    ) {
        var pageable = PageRequest.of(
                Integer.parseInt(pageNum),
                20,
                Sort.Direction.fromString(direction),
                sortBy
        );
        var teacherNotebooks = notebookService.findAllNotebooksByTeacherId(teacherId, bimesterFilter, pageable);
        if (teacherNotebooks.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(teacherNotebooks);
    }

    @GetMapping("/{notebookId}")
    public ResponseEntity<Object> getNotebookById(@PathVariable(value = "notebookId") UUID notebookId) {
        var notebook = notebookService.findNotebookById(notebookId);
        if (notebook.isPresent()) {
            return ResponseEntity.ok(notebook.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Caderneta não encontrada!");
    }


    @GetMapping("/{notebookId}/students-performance")
    public ResponseEntity<Object> getStudentsPerformanceByNotebookId(
            @PathVariable(value = "notebookId") UUID notebookId
    ) {
        var notebookOptional = notebookService.findNotebookById(notebookId);
        if (notebookOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Caderneta não encontrada!");
        }

        var students = studentService.findAllStudentsByClasse(notebookOptional.get().getClasse());
        var studentsPerformance = NotebookUtils.getAllStudentsPerformanceInLessons(notebookOptional.get(), students);

        return ResponseEntity.ok(studentsPerformance);
    }

    //READ


    //EDIT

    @PutMapping("/edit/{notebookId}")
    public ResponseEntity<?> editNotebook(@PathVariable(value = "notebookId") UUID notebookId,
                                          @RequestBody @Valid NotebookDto notebookDto) {
        var notebookOptional = notebookService.findNotebookById(notebookId);
        if (notebookOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Caderneta não encontrada!");
        }
        var notebookEntity = new NotebookEntity();
        BeanUtils.copyProperties(notebookOptional.get(), notebookEntity);
        BeanUtils.copyProperties(notebookDto, notebookEntity);
        notebookService.saveNotebook(notebookEntity);
        return ResponseEntity.ok().build();
    }

    //EDIT


    //DELETE

    @DeleteMapping("/{notebookId}/delete")
    public ResponseEntity<?> deleteNotebook(
            @PathVariable(value = "notebookId") UUID notebookId
    ) {
        var notebookOptional = notebookService.findNotebookById(notebookId);
        if (notebookOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Caderneta não encontrada!");
        }

        notebookService.deleteNotebookById(notebookId);
        return ResponseEntity.ok("Caderneta deletada com sucesso!");
    }

    //DELETE


    //VERIFICATIONS

//    @GetMapping("/{teacherId}/all-missing-tasks")
//    public ResponseEntity<Object> verifyAllMissingTasks(
//            @PathVariable(value = "teacherId") @NotNull UUID teacherId
//    ) {
//        var authenticationId = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        if (!teacherId.equals(authenticationId)) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
//        }
//        var allMissingTasks = notebookService.verifyAllMissingTasks(teacherId);
//        if (allMissingTasks.isEmpty()) {
//            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
//        }
//        return ResponseEntity.ok(allMissingTasks);
//    }

    @GetMapping("/{notebookId}/missing-tasks")
    public ResponseEntity<Object> verifyMissingTasks(
            @PathVariable(value = "notebookId") UUID notebookId
    ) {
        var notebookOptional = notebookService.findNotebookById(notebookId);
        if (notebookOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        var missingTasks = notebookService.verifyMissingTasksByNotebook(
                notebookOptional.get(),
                studentService.getSizeOfStudentsByClasse(notebookOptional.get().getClasse())
        );
        if (missingTasks.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        return ResponseEntity.ok(missingTasks);
    }

    //VERIFICATIONS


    //FINALIZATION

    @PutMapping("/finalize/{notebookId}")
    public ResponseEntity<Object> finalizeNotebook(
            @PathVariable(value = "notebookId") UUID notebookId,
            @RequestBody Map<String, Double> workTypeWeights) throws IOException {
        var notebookOptional = notebookService.findNotebookById(notebookId);
        if (notebookOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        var students = studentService.findAllStudentsByClasse(notebookOptional.get().getClasse());

        UUID finishedNotebookId = null;
        if (notebookOptional.get().getFinishedNotebook() != null) {
            finishedNotebookId = notebookOptional.get().getFinishedNotebook().getId();
            clearFinishedNotebookStudents(finishedNotebookId);
        }
        setFinishedNotebook(
                finishedNotebookId,
                notebookOptional.get(),
                students,
                workTypeWeights
        );

        var file = notebookService.finalizeNotebook(notebookOptional.get(), students, workTypeWeights);
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

    private void setFinishedNotebook(
            @Nullable UUID finishedNotebookId,
            @NotNull NotebookEntity notebook,
            @NotNull List<StudentEntity> students,
            Map<String, Double> workTypeWeights
    ) {
        var finishedNotebookEntity = new FinishedNotebookEntity();
        finishedNotebookEntity.setId(finishedNotebookId);
        finishedNotebookEntity.setNotebook(notebook);
        finishedNotebookEntity.setTotalLessons(notebook.getLessonsQuantity());

        List<FinishedStudentEntity> finishedStudents = new ArrayList<>();

        students.forEach((student) -> {
            var finishedStudent = new FinishedStudentEntity();
            finishedStudent.setFinishedNotebook(finishedNotebookEntity);
            finishedStudent.setStudent(student);

            //SET STUDENT ABSENCES
            var studentPerformance = NotebookUtils.getStudentPerformanceInLessons(notebook, student);
            finishedStudent.setAbsences(studentPerformance.getAbsences());
            finishedStudent.setPresencePercentage(studentPerformance.getAbsencesPercentage());

            finishedNotebookEntity.setTotalLessons(studentPerformance.getTotalLessons());

            //SET STUDENT FINAL GRADE
            finishedStudent.setFinalGrade(NotebookUtils.getStudentPerformanceInWorks(
                    notebook,
                    student,
                    workTypeWeights
            ));

            finishedStudents.add(finishedStudent);
        });

        finishedNotebookEntity.setFinishedStudents(finishedStudents);

        finishedNotebookService.save(finishedNotebookEntity);
    }

    public void clearFinishedNotebookStudents(
            UUID finishedNotebookId
    ) {
        var finishedNotebookOptional = finishedNotebookService.findById(finishedNotebookId);
        finishedNotebookOptional.get().getFinishedStudents().clear();
        finishedNotebookService.save(finishedNotebookOptional.get());
    }

    //FINALIZATION


}
