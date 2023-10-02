package com.api.notebook.services;

import com.api.notebook.models.entities.AttendanceEntity;
import com.api.notebook.repositories.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public List<AttendanceEntity> findAllAttendancesByLessonId(
            @NotNull List<AttendanceEntity> attendances,
            Long lessonId
    ) {
        List<AttendanceEntity> lessonAttendances = new ArrayList<>();
        for (AttendanceEntity attendance:
                attendances) {
            if (attendance.getLesson().getId().equals(lessonId)) {
                lessonAttendances.add(attendance);
            }
        }
        return lessonAttendances;
    }

    public Optional<AttendanceEntity> findAttendanceById(Long id) {
        return attendanceRepository.findById(id);
    }

    public void deleteAttendanceById(Long id) {
        attendanceRepository.deleteById(id);
    }

}
