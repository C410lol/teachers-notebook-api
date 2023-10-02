package com.api.notebook.controllers;

import com.api.notebook.models.dtos.AttendanceDto;
import com.api.notebook.models.dtos.GradeDto;
import com.api.notebook.models.entities.AttendanceEntity;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/grades")
public class GradeController {

    private final GradeService gradeService;
    private final WorkService workService;
    private final StudentService studentService;

    @PostMapping("/create") //POST endpoint to create a grade entity
    public ResponseEntity<Object> createGrade(@RequestParam(value = "workId") Long workId,
                                                   @RequestBody @Valid @NotNull GradeDto gradeDto) {
        var gradeEntity = new GradeEntity();
        BeanUtils.copyProperties(gradeDto, gradeEntity);
        studentService.setStudentToGrade(gradeDto.getStudentId(), gradeEntity);
        workService.setGradeToWork(workId, gradeEntity);
        gradeService.saveGrade(gradeEntity);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/all") //GET endpoint to get all grades
    public ResponseEntity<Object> getAllGrades(
            @RequestParam(value = "workId", required = false) Long workId) {
        var gradeList = gradeService.findAllGrades();
        if (!gradeList.isEmpty()) {
            if (workId != null) {
                return ResponseEntity.ok(gradeService.findAllGradesByWorkId(gradeList, workId));
            }
            return ResponseEntity.ok(gradeList);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

}
