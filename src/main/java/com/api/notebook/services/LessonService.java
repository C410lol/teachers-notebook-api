package com.api.notebook.services;

import com.api.notebook.models.entities.AttendanceEntity;
import com.api.notebook.models.entities.LessonEntity;
import com.api.notebook.repositories.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;

    public void saveLesson(LessonEntity lesson) {
        lessonRepository.save(lesson);
    }

    public List<LessonEntity> findAllLessons() {
        return lessonRepository.findAll();
    }

    public Page<LessonEntity> findAllLessonsByNotebookId(UUID notebookId, Pageable pageable) {
        return lessonRepository.findByNotebookId(notebookId, pageable);
    }

    public Optional<LessonEntity> findLessonById(UUID id) {
        return lessonRepository.findById(id);
    }

    public void deleteLessonById(UUID id) {
        lessonRepository.deleteById(id);
    }

    //Set attendance to a lesson
    public void setAttendanceToLesson(UUID lessonId, @NotNull AttendanceEntity attendance, int index) {
        var lessonOptional = findLessonById(lessonId);
        if (lessonOptional.isPresent()) {

            //Check if the lesson has attendances
            if (!lessonOptional.get().getAttendances().isEmpty()) {

                //Take te attendance by the provided index
                AttendanceEntity attendanceByIndex = lessonOptional.get().getAttendances().get(index);

                //Check if the attendance exists
                if (attendanceByIndex != null) {

                    //If so it sets the provided attendance id as the attendance that was found
                    attendance.setId(attendanceByIndex.getId());
                }
            }
            attendance.setLesson(lessonOptional.get());
        }
    }

}
