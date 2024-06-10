package com.api.notebook.controllers;

import com.api.notebook.models.dtos.GradeDto;
import com.api.notebook.models.entities.GradeEntity;
import com.api.notebook.services.GradeService;
import com.api.notebook.services.StudentService;
import com.api.notebook.services.WorkService;
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
@RequestMapping("/grades")
public class GradeController {

    private final GradeService gradeService;
    private final WorkService workService;
    private final StudentService studentService;

    @PostMapping("/create") //POST endpoint to create a grade entity
    public ResponseEntity<Object> createGrade(@RequestParam(value = "workId") UUID workId,
                                              @RequestBody @Valid @NotNull GradeDto gradeDto) {
        var gradeEntity = new GradeEntity();
        BeanUtils.copyProperties(gradeDto, gradeEntity);
        studentService.setStudentToGrade(gradeDto.getStudentId(), gradeEntity);
        workService.setGradeToWork(workId, gradeEntity);
        gradeService.saveGrade(gradeEntity);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

//    @GetMapping("/all") //GET endpoint to get all grades
//    @PreAuthorize("hasRole('ROLE_ADM')")
//    public ResponseEntity<Object> getAllGrades() {
//        var grades = gradeService.findAllGrades();
//        if (grades.isEmpty()) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        }
//        return ResponseEntity.ok(grades);
//    }

    @GetMapping("/all/{workId}") //GET endpoint to get all grades
    public ResponseEntity<Object> getAllGradesByWorkId(
            @PathVariable(value = "workId") UUID workId
    ) {
        var workGrades = gradeService.findAllGradesByWorkId(workId);
        if (workGrades.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(workGrades);
    }

}
