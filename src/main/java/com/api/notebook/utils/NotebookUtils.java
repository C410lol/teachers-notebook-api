package com.api.notebook.utils;

import com.api.notebook.enums.PerformanceStatus;
import com.api.notebook.models.StudentPerformanceModel;
import com.api.notebook.models.entities.AttendanceEntity;
import com.api.notebook.models.entities.LessonEntity;
import com.api.notebook.models.entities.NotebookEntity;
import com.api.notebook.models.entities.StudentEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NotebookUtils {

    public static @NotNull List<StudentPerformanceModel> getAllStudentsPerformanceInLessons(
            NotebookEntity notebook,
            @NotNull List<StudentEntity> students
    ) {
        List<StudentPerformanceModel> studentsPerformance = new ArrayList<>();

        for (StudentEntity student:
                students) {
            studentsPerformance.add(getStudentPerformanceInLessons(notebook, student));
        }

        return studentsPerformance;
    }

    public static @NotNull StudentPerformanceModel getStudentPerformanceInLessons(
            @NotNull NotebookEntity notebook,
            StudentEntity student
    ) {
        var totalLessons = notebook.getLessons().size();
        var absences = 0;
        var absencesPercentage = 0.0;
        var absencesStatus = PerformanceStatus.EXCELENTE;

        for (LessonEntity lesson:
                notebook.getLessons()) {
            for (AttendanceEntity attendance:
                    lesson.getAttendances()) {
                if (attendance.getAbsentStudents().contains(student)) {
                    absences++;
                }
            }
        }

        absencesPercentage = (double) (absences * 100) / totalLessons;
        if (absencesPercentage <= 10) {
            absencesStatus = PerformanceStatus.EXCELENTE;
        } else if (absencesPercentage <= 20) {
            absencesStatus = PerformanceStatus.BOM;
        } else if (absencesPercentage <= 30) {
            absencesStatus = PerformanceStatus.CUIDADO;
        } else if (absencesPercentage <= 50) {
            absencesStatus = PerformanceStatus.RUIM;
        } else absencesStatus = PerformanceStatus.PÉSSIMO;

        return new StudentPerformanceModel(
                student,
                absences,
                Math.round(100 - absencesPercentage) + "%",
                absencesStatus
        );
    }

}
