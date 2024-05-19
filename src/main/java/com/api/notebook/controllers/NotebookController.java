package com.api.notebook.controllers;

import com.api.notebook.enums.RoleEnum;
import com.api.notebook.enums.StatusEnum;
import com.api.notebook.models.dtos.NotebookDto;
import com.api.notebook.models.entities.NotebookEntity;
import com.api.notebook.services.MailService;
import com.api.notebook.services.NotebookService;
import com.api.notebook.services.StudentService;
import com.api.notebook.services.UserService;
import com.api.notebook.utils.NotebookUtils;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notebooks")
public class    NotebookController {

    private final NotebookService notebookService;
    private final UserService userService;
    private final StudentService studentService;
    private final MailService mailService;


    //CREATE

    @PostMapping("/create") //POST endpoint to create a notebook entity
    @PreAuthorize("hasAnyRole('ROLE_TCHR', 'ROLE_ADM')")
    public ResponseEntity<Object> createNotebook(
            @RequestParam(value = "teacherId") UUID teacherId,
            @RequestBody @Valid @NotNull NotebookDto notebookDto
    ) {
        var notebookEntity = new NotebookEntity();
        BeanUtils.copyProperties(notebookDto, notebookEntity);
        notebookEntity.setStatus(StatusEnum.ON);
        notebookEntity.setCreateDate(LocalDate.now(ZoneId.of("UTC-3")));
        userService.setNotebookToUser(teacherId, notebookEntity);
        studentService.setStudentsToNotebookByClass(notebookEntity.getClasse(), notebookEntity);
        notebookService.saveNotebook(notebookEntity);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //CREATE


    //READ

    @GetMapping("/all") //GET endpoint to get all notebooks
    @PreAuthorize("hasRole('ROLE_ADM')")
    public ResponseEntity<Object> getAllNotebooks() {
        var notebooks = notebookService.findAllNotebooks();
        if (notebooks.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(notebooks);
    }

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
    @PreAuthorize("hasAnyRole('ROLE_TCHR', 'ROLE_ADM')")
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

    @PutMapping("refresh-all")
    @PreAuthorize("hasAnyRole('ROLE_ADM')")
    public void refreshAllNotebooks() {
        var allNotebooks = notebookService.findAllNotebooks();
        for (NotebookEntity notebook :
                allNotebooks) {
            studentService.setStudentsToNotebookByClass(notebook.getClasse(), notebook);
            notebookService.saveNotebook(notebook);
        }
    }

    //EDIT


    //DELETE

    @DeleteMapping("/{notebookId}/delete")
    @PreAuthorize("hasAnyRole('ROLE_TCHR', 'ROLE_ADM')")
    public ResponseEntity<?> deleteNotebook(
            @PathVariable(value = "notebookId") UUID notebookId
    ) {

        var notebookOptional = notebookService.findNotebookById(notebookId);
        if (notebookOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Caderneta não encontrada!");
        }

        var auth = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!notebookOptional.get().getTeacher().getId().equals(auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Você não pode deletar a caderneta de outros usuários!");
        }

        notebookService.deleteNotebookById(notebookId);

        return ResponseEntity.ok("Caderneta deletada com sucesso!");

    }

    //DELETE


    //VERIFICATIONS

    @GetMapping("/{teacherId}/all-missing-tasks")
    @PreAuthorize("hasAnyRole('ROLE_TCHR', 'ROLE_ADM')")
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
    @PreAuthorize("hasAnyRole('ROLE_TCHR', 'ROLE_ADM')")
    public ResponseEntity<Object> verifyMissingTasks(
            @PathVariable(value = "notebookId") UUID notebookId
    ) {
        var notebookOptional = notebookService.findNotebookById(notebookId);
        if (notebookOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (
                !notebookOptional.get().getTeacher().getId().equals(authentication.getPrincipal()) &&
                        !authentication.getAuthorities().contains(new SimpleGrantedAuthority(RoleEnum.ROLE_ADM.name()))
        ) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        var missingTasks = notebookService.verifyMissingTasksByNotebook(notebookOptional.get());
        if (missingTasks.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(missingTasks);
    }

    //VERIFICATIONS


    //FINALIZATION

    @PutMapping("/finalize/{notebookId}")
    @PreAuthorize("hasAnyRole('ROLE_TCHR', 'ROLE_ADM')")
    public ResponseEntity<Object> finalizeNotebook(
            @PathVariable(value = "notebookId") UUID notebookId,
            @RequestBody Map<String, Double> workTypeWeights) throws IOException {
        var notebookOptional = notebookService.findNotebookById(notebookId);
        if (notebookOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (
                !notebookOptional.get().getTeacher().getId().equals(authentication.getPrincipal()) &&
                        !authentication.getAuthorities().contains(new SimpleGrantedAuthority(RoleEnum.ROLE_ADM.name()))
        ) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        var students = studentService.findAllStudentsByClasse(notebookOptional.get().getClasse());
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

    //FINALIZATION


}
