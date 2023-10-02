package com.api.notebook.services;

import com.api.notebook.models.entities.AttendanceEntity;
import com.api.notebook.models.entities.LessonEntity;
import com.api.notebook.repositories.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;

    public void saveLesson(LessonEntity lesson) {
        lessonRepository.save(lesson);
    }

    public List<LessonEntity> findAllLessons() {
        var lessonList = lessonRepository.findAll();
        lessonList.sort(Comparator.comparing(LessonEntity::getDate).reversed());
        return lessonList;
    }

    public List<LessonEntity> findAllLessonsByNotebookId(@NotNull List<LessonEntity> lessons, Long notebookId) {
        List<LessonEntity> notebookLessons = new ArrayList<>();
        for (LessonEntity lesson:
                lessons) {
            if (lesson.getNotebook().getId().equals(notebookId)) {
                notebookLessons.add(lesson);
            }
        }
        return notebookLessons;
    }

    public Optional<LessonEntity> findLessonById(Long id) {
        return lessonRepository.findById(id);
    }

    public void deleteLessonById(Long id) {
        lessonRepository.deleteById(id);
    }

    //Set attendance to a lesson
    public void setAttendanceToLesson(Long lessonId, @NotNull AttendanceEntity attendance, int index) {
        var lessonOptional = findLessonById(lessonId);
        if (lessonOptional.isPresent()) {

            //Check if the lesson has attendances
            if(!lessonOptional.get().getAttendances().isEmpty()) {

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
