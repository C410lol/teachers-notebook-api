package com.api.notebook.services;

import com.api.notebook.models.entities.AttendanceEntity;
import com.api.notebook.repositories.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    public void saveAttendance(AttendanceEntity attendance) {
        attendanceRepository.save(attendance);
    }

    public List<AttendanceEntity> findAllAttendances() {
        return attendanceRepository.findAll();
    }

    public List<AttendanceEntity> findAllAttendancesByLessonId(UUID lessonId) {
        return attendanceRepository.findByLessonId(lessonId);
    }

    public Optional<AttendanceEntity> findAttendanceById(UUID id) {
        return attendanceRepository.findById(id);
    }

    public void deleteAttendanceById(UUID id) {
        attendanceRepository.deleteById(id);
    }

}
