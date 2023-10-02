package com.api.notebook.services;

import com.api.notebook.enums.StatusEnum;
import com.api.notebook.enums.WorksEnum;
import com.api.notebook.models.FinalAverageModel;
import com.api.notebook.models.dtos.WorkTypeWeights;
import com.api.notebook.models.entities.*;
import com.api.notebook.repositories.NotebookRepository;
import com.api.notebook.utils.CalculationsUtils;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class NotebookService {

    private final NotebookRepository notebookRepository;

    public void saveNotebook(NotebookEntity notebook) {
        notebookRepository.save(notebook);
    }

    public List<NotebookEntity> findAllNotebooks() {
        var notebookList = notebookRepository.findAll();
        notebookList.sort(Comparator.comparing(NotebookEntity::getClasse));
        notebookList.sort(Comparator.comparing(NotebookEntity::getStatus));
        return notebookList;
    }

    //Find all notebooks of this particularly 'teacherId'
    public List<NotebookEntity> findAllNotebooksByTeacherId(@NotNull List<NotebookEntity> notebooks, UUID teacherId) {
        List<NotebookEntity> teacherNotebooks = new ArrayList<>();
        for (NotebookEntity singleNotebook:
                notebooks) {
            if (singleNotebook.getTeacher().getId().equals(teacherId)) {
                teacherNotebooks.add(singleNotebook);
            }
        }
        return teacherNotebooks;
    }

    public Optional<NotebookEntity> findNotebookById(Long id) {
        return notebookRepository.findById(id);
    }

    public void deleteNotebookById(Long id) {
        notebookRepository.deleteById(id);
    }

    public void setLessonToNotebook(Long notebookId, @NotNull LessonEntity lesson) { //Set lesson to a notebook
        var notebookOptional = findNotebookById(notebookId);
        notebookOptional.ifPresent(lesson::setNotebook);
    }

    public void setWorkToNotebook(Long notebookId, @NotNull WorkEntity work) { //Set work to a notebook
        var notebookOptional = findNotebookById(notebookId);
        notebookOptional.ifPresent(work::setNotebook);
    }

    //Finish notebook and return all students average
    public ByteArrayResource finalizeNotebook(Long notebookId, WorkTypeWeights workTypeWeights) {
        var notebook = findNotebookById(notebookId);
        if(notebook.isPresent()) {

            List<FinalAverageModel> allFinalAverage = new ArrayList<>();

            int currentPosition = 1;

            notebook.get().getStudents().sort(Comparator.comparing(StudentEntity::getName));
            for (StudentEntity student:
                    notebook.get().getStudents()) {

                allFinalAverage.add(new FinalAverageModel(
                        currentPosition,
                        student.getName(),
                        setStudentFinalGrade(
                                notebook.get(),
                                student,
                                workTypeWeights
                        ),
                        getStudentAbsences(notebook.get(), student),
                        0));

                currentPosition++;
            }

            notebook.get().setStatus(StatusEnum.OFF);
            saveNotebook(notebook.get());

            var byteArrayOutputStream = ExcelService.createFinalAverageExcelTable(allFinalAverage);
            return new ByteArrayResource(byteArrayOutputStream.toByteArray());
        }
        return null;
    }

    //Get all grades of the student by the given notebook's works
    private @NotNull Double setStudentFinalGrade(@NotNull NotebookEntity notebook,
                                                 StudentEntity student,
                                                 WorkTypeWeights workTypeWeights) {
        List<Double> finalGrades = new ArrayList<>();
        for (WorksEnum currentWorkEnum:
                WorksEnum.values()) {
            int currentWeight = switch (currentWorkEnum) {
                case TAREFA -> workTypeWeights.getTarefaWeight();
                case TRABALHO -> workTypeWeights.getTrabalhoWeight();
                case PARTIÇIPAÇÃO -> workTypeWeights.getParticipacaoWeight();
                case SIMULADO -> workTypeWeights.getSimuladoWeight();
            };
            finalGrades.add(getGradeByWorkType(
                    notebook.getWorks(),
                    student,
                    currentWorkEnum,
                    currentWeight));
        }
        var finalGradeDouble = CalculationsUtils.getFirstDecimalHouseDoubleAverage(finalGrades);
        var factor = Math.pow(10, 1);
        return Math.round(finalGradeDouble * factor) / factor;
    }

    private @NotNull Double getGradeByWorkType(@NotNull List<WorkEntity> works,
                                               StudentEntity student,
                                               WorksEnum workType,
                                               Integer typeWeight) {
        List<Double> grades = new ArrayList<>();
        for (WorkEntity currentWork:
                works) {
            if (currentWork.getType().equals(workType)) {
                for (GradeEntity currentGrade:
                        currentWork.getGrades()) {
                    if(currentGrade.getStudent().equals(student)) {
                        grades.add(currentGrade.getGrade());
                    }
                }
            }
        }
        double finalTypeAverage = CalculationsUtils.getFirstDecimalHouseDoubleAverage(grades);
        return (finalTypeAverage * typeWeight) / 10;
    }

    //Get all absences of the student by the given notebook's lessons
    private int getStudentAbsences(@NotNull NotebookEntity notebook,
                                    StudentEntity student) {
        var absences = 0;
        for (LessonEntity lesson:
                notebook.getLessons()) {
            for (AttendanceEntity attendance:
                    lesson.getAttendances()) {

                //Search student in 'absentStudents' list
                for (StudentEntity absentStudent:
                        attendance.getAbsentStudents()) {
                    if (absentStudent.equals(student)) {
                        absences++;
                    }
                }
            }
        }
        return absences;
    }

}
