package com.api.notebook.controllers;

import com.api.notebook.models.dtos.AttendanceDto;
import com.api.notebook.models.entities.AttendanceEntity;
import com.api.notebook.services.AttendanceService;
import com.api.notebook.services.LessonService;
import com.api.notebook.services.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/attendances")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final StudentService studentService;
    private final LessonService lessonService;

    @PostMapping("/create") //POST endpoint to create an attendance entity
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_USER')")
    public ResponseEntity<Object> createAttendance(
            @RequestParam(value = "lessonId") UUID lessonId,
            @RequestBody @Valid @NotNull List<AttendanceDto> attendanceDtos
    ) {
        int currentIndex = 0;
        for(AttendanceDto attendanceDto:
                attendanceDtos) {
            var attendanceEntity = new AttendanceEntity();
            studentService.setPresentStudentsToAttendance(attendanceDto.getPresentStudentsIds(), attendanceEntity);
            studentService.setAbsentStudentsToAttendance(attendanceDto.getAbsentStudentsIds(), attendanceEntity);
            lessonService.setAttendanceToLesson(lessonId, attendanceEntity, currentIndex);
            attendanceService.saveAttendance(attendanceEntity);
            currentIndex++;
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/all") //GET endpoint to get all attendances
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Object> getAllAttendances() {
        var attendances = attendanceService.findAllAttendances();
        if (attendances.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(attendances);
    }

    @GetMapping("/all/{lessonId}") //GET endpoint to get all attendances
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_USER')")
    public ResponseEntity<Object> getAllAttendancesByLessonId(
            @PathVariable(value = "lessonId") UUID lessonId
    ) {
        var lessonAttendances = attendanceService.findAllAttendancesByLessonId(lessonId);
        if (lessonAttendances.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(lessonAttendances);
    }

}
