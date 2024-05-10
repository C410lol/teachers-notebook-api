package com.api.notebook.controllers;

import com.api.notebook.enums.RoleEnum;
import com.api.notebook.models.dtos.LessonDto;
import com.api.notebook.models.entities.BNCCCodeEntity;
import com.api.notebook.models.entities.LessonEntity;
import com.api.notebook.services.BNCCCodeService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lessons")
public class LessonController {

    private final LessonService lessonService;
    private final NotebookService notebookService;
    private final BNCCCodeService bnccCodeService;

    @PostMapping("/create") //POST endpoint to create a lesson entity
    @PreAuthorize("hasAnyRole('ROLE_TCHR', 'ROLE_ADM')")
    public ResponseEntity<Object> createLesson(
            @RequestParam(value = "notebookId") UUID notebookId,
            @RequestBody @Valid @NotNull LessonDto lessonDto
    ) {
        var lessonEntity = new LessonEntity();
        lessonEntity.setTitle(lessonDto.getTitle());
        lessonEntity.setDetails(lessonDto.getDetails());
        lessonEntity.setObservations(lessonDto.getObservations());
        lessonEntity.setQuantity(lessonDto.getQuantity());
        lessonEntity.setDate(lessonDto.getDate());

        if (lessonEntity.getDate() == null) {
            lessonEntity.setDate(LocalDate.now(ZoneId.of("UTC-3")));
        }
        if (lessonDto.getBnccCodes() != null && !lessonDto.getBnccCodes().isEmpty()) {
            if (!bnccCodeService.setBnccCodesToLesson(lessonDto.getBnccCodes(), lessonEntity)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Código BNCC não reconhecido!");
            }
        }
        notebookService.setLessonToNotebook(notebookId, lessonEntity);
        lessonService.saveLesson(lessonEntity);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @PostMapping("/{lessonId}/duplicate")
    @PreAuthorize("hasAnyRole('ROLE_TCHR', 'ROLE_ADM')")
    public ResponseEntity<Object> duplicateLesson(
            @PathVariable(value = "lessonId") UUID lessonId
    ) {
        var lessonOptions = lessonService.findLessonById(lessonId);
        if (lessonOptions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Aula não encontrada!");
        }

        var duplicatedLesson = new LessonEntity();
        BeanUtils.copyProperties(lessonOptions.get(), duplicatedLesson);
        duplicatedLesson.setDate(LocalDate.now(ZoneId.of("UTC-3")));
        duplicatedLesson.setId(null);
        duplicatedLesson.setBnccCodes(null);
        duplicatedLesson.setAttendances(null);

        List<String> codes = new ArrayList<>();
        for (BNCCCodeEntity bnccCode:
                lessonOptions.get().getBnccCodes()) {
            codes.add(bnccCode.getCode());
        }
        bnccCodeService.setBnccCodesToLesson(codes, duplicatedLesson);
        lessonService.saveLesson(duplicatedLesson);

        return ResponseEntity.ok("Aula duplicada com sucesso!");
    }




    @GetMapping("/all") //GET endpoint to get all lessons
    @PreAuthorize("hasRole('ROLE_ADM')")
    public ResponseEntity<Object> getAllLessons() {
        var lessons = lessonService.findAllLessons();
        if (lessons.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(lessons);
    }

    @GetMapping("/all/{notebookId}") //GET endpoint to get all lessons
    @PreAuthorize("hasAnyRole('ROLE_TCHR', 'ROLE_ADM')")
    public ResponseEntity<Object> getAllLessonsByNotebookId(
            @PathVariable(value = "notebookId") UUID notebookId,
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
        var notebookLessons = lessonService.findAllLessonsByNotebookId(notebookId, pageable);
        if (notebookLessons.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(notebookLessons);
    }

    @GetMapping("/{lessonId}")
    @PreAuthorize("hasAnyRole('ROLE_TCHR', 'ROLE_ADM')")
    public ResponseEntity<Object> getLessonById(@PathVariable(value = "lessonId") UUID lessonId) {
        var lessonOptional = lessonService.findLessonById(lessonId);
        if (lessonOptional.isPresent()) {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (
                    !lessonOptional.get().getNotebook().getTeacher().getId().equals(authentication.getPrincipal()) &&
                            !authentication.getAuthorities().contains(new SimpleGrantedAuthority(RoleEnum.ROLE_ADM.name()))
            ) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.ok(lessonOptional.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Aula não encontrada!");
    }

    @PutMapping("/edit/{lessonId}")
    @PreAuthorize("hasAnyRole('ROLE_TCHR', 'ROLE_ADM')")
    public ResponseEntity<Object> editLesson(
            @PathVariable(value = "lessonId") UUID lessonId,
            @RequestBody @Valid LessonDto lessonDto) {
        var lessonOptional = lessonService.findLessonById(lessonId);
        if (lessonOptional.isPresent()) {
            var authenticationId = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (lessonOptional.get().getNotebook().getTeacher().getId().equals(authenticationId)) {
                var lessonEntity = new LessonEntity();
                BeanUtils.copyProperties(lessonOptional.get(), lessonEntity);
                BeanUtils.copyProperties(lessonDto, lessonEntity);
                if (lessonDto.getBnccCodes() != null && !lessonDto.getBnccCodes().isEmpty()) {
                    if (!bnccCodeService.setBnccCodesToLesson(lessonDto.getBnccCodes(), lessonEntity)) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Código BNCC não reconhecido!");
                    }
                }
                lessonService.saveLesson(lessonEntity);
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Aula não encontrada!");
    }

    @DeleteMapping("/delete/{lessonId}")
    @PreAuthorize("hasAnyRole('ROLE_TCHR', 'ROLE_ADM')")
    public ResponseEntity<Object> deleteLesson(@PathVariable(value = "lessonId") UUID lessonId) {
        var lessonOptional = lessonService.findLessonById(lessonId);
        if (lessonOptional.isPresent()) {
            var authenticationId = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (lessonOptional.get().getNotebook().getTeacher().getId().equals(authenticationId)) {
                lessonService.deleteLessonById(lessonId);
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Aula não encontrada!");
    }

}
