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
    public ResponseEntity<Object> createAttendance(@RequestParam(value = "lessonId") UUID lessonId,
                                                   @RequestBody @Valid @NotNull List<AttendanceDto> attendanceDtos) {
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
    public ResponseEntity<Object> getAllAttendances(
            @RequestParam(value = "lessonId", required = false) UUID lessonId
    ) {
        var attendanceList = attendanceService.findAllAttendances();
        if (!attendanceList.isEmpty()) {
            if(lessonId != null) {
                return ResponseEntity.ok(attendanceService.findAllAttendancesByLessonId(attendanceList, lessonId));
            }
            return ResponseEntity.ok(attendanceList);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

}
