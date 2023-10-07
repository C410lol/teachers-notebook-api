package com.api.notebook.utils;

import com.api.notebook.models.FinalAverageModel;
import com.api.notebook.models.dtos.WorkTypeWeights;
import com.api.notebook.models.entities.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class NotebookUtils {

    public static @NotNull List<FinalAverageModel> getAllFinalAverageStudents(
            @NotNull NotebookEntity notebook,
            WorkTypeWeights workTypeWeights
    ) {
        List<FinalAverageModel> allFinalAverage = new ArrayList<>();

        int currentPosition = 1;

        notebook.getStudents().sort(Comparator.comparing(StudentEntity::getName));
        for (StudentEntity student:
                notebook.getStudents()) {

            allFinalAverage.add(new FinalAverageModel(
                    currentPosition,
                    student.getName(),
                    setStudentFinalGrade(
                            notebook,
                            student,
                            workTypeWeights
                    ),
                    getStudentAbsences(notebook, student),
                    0));

            currentPosition++;
        }

        return allFinalAverage;
    }

    //Get all grades of the student by the given notebook's works
    private static @NotNull Double setStudentFinalGrade(@NotNull NotebookEntity notebook,
                                                 StudentEntity student,
                                                 WorkTypeWeights workTypeWeights) {
        List<Double> tarefaGrades = new ArrayList<>();
        List<Double> provasGrades = new ArrayList<>();
        List<Double> participacaoGrades = new ArrayList<>();
        List<Double> simuladoGrades = new ArrayList<>();
        for (WorkEntity currentWork:
                notebook.getWorks()) {
            for (GradeEntity currentGrade:
                    currentWork.getGrades()) {
                if (currentGrade.getStudent().equals(student)) {
                    var grade = currentGrade.getGrade();
                    switch (currentWork.getType()) {
                        case TRABALHO -> tarefaGrades.add(grade);
                        case PROVA -> provasGrades.add(grade);
                        case PARTIÇIPAÇÃO -> participacaoGrades.add(grade);
                        case SIMULADO -> simuladoGrades.add(grade);
                    }
                }
            }
        }
        List<Double> allGrades = new ArrayList<>();
        allGrades.add(getFinalGradeByWorkType(tarefaGrades, workTypeWeights.getTarefaWeight()));
        allGrades.add(getFinalGradeByWorkType(provasGrades, workTypeWeights.getProvasWeight()));
        allGrades.add(getFinalGradeByWorkType(participacaoGrades, workTypeWeights.getParticipacaoWeight()));
        allGrades.add(getFinalGradeByWorkType(simuladoGrades, workTypeWeights.getSimuladoWeight()));
        return CalculationsUtils.sumEveryItemOfNumberList(allGrades);
    }

    private static @NotNull Double getFinalGradeByWorkType(List<Double> workTypeGrades, Double workTypeWeight) {
        var finalGradeWorkType = CalculationsUtils.getFirstDecimalHouseDoubleAverage(workTypeGrades);
        var approximatedValue = (double) Math.round(finalGradeWorkType * 10) / 10;
        return (approximatedValue * workTypeWeight) / 10;
    }

    //Get all absences of the student by the given notebook's lessons
    private static int getStudentAbsences(@NotNull NotebookEntity notebook,
                                   StudentEntity student) {
        var absences = 0;
        for (LessonEntity lesson:
                notebook.getLessons()) {
            for (int i = 0; i < lesson.getQuantity(); i++) {
                //Search student in 'absentStudents' list
                for (StudentEntity absentStudent:
                        lesson.getAttendances().get(i).getAbsentStudents()) {
                    if (absentStudent.equals(student)) {
                        absences++;
                    }
                }
            }
        }
        return absences;
    }

}
