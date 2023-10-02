package com.api.notebook.services;

import com.api.notebook.enums.ClassEnum;
import com.api.notebook.models.entities.AttendanceEntity;
import com.api.notebook.models.entities.GradeEntity;
import com.api.notebook.models.entities.NotebookEntity;
import com.api.notebook.models.entities.StudentEntity;
import com.api.notebook.repositories.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;

    public void saveStudent(StudentEntity student) {
        studentRepository.save(student);
    }

    public List<StudentEntity> findAllStudents() {
        return studentRepository.findAll();
    }

    public Optional<StudentEntity> findStudentById(UUID id) {
        return studentRepository.findById(id);
    }

    public void deleteStudentById(UUID id) {
        studentRepository.deleteById(id);
    }

    //Set present students to an attendance
    public void setPresentStudentsToAttendance(@NotNull List<UUID> presentStudentsIds, AttendanceEntity attendance) {
        List<StudentEntity> presentStudents = new ArrayList<>();
        for (UUID studentId:
                presentStudentsIds) {
            var studentOptional = findStudentById(studentId);
            if (studentOptional.isPresent()) {
                presentStudents.add(studentOptional.get());
            }
        }
        attendance.setPresentStudents(presentStudents);
    }

    //Set absent students to an attendance
    public void setAbsentStudentsToAttendance(@NotNull List<UUID> absentStudentsIds, AttendanceEntity attendance) {
        List<StudentEntity> absentStudents = new ArrayList<>();
        for (UUID studentId:
                absentStudentsIds) {
            var studentOptional = findStudentById(studentId);
            if (studentOptional.isPresent()) {
                absentStudents.add(studentOptional.get());
            }
        }
        attendance.setAbsentStudents(absentStudents);
    }

    public void setStudentToGrade(UUID studentId, @NotNull GradeEntity grade) { //Set student to a grade
        var studentOptional = findStudentById(studentId);
        studentOptional.ifPresent(grade::setStudent);
    }

    //Set students to notebook by class enum
    public void setStudentsToNotebookByClass(ClassEnum classEnum, NotebookEntity notebook) {
        List<StudentEntity> students = new ArrayList<>();
        for (StudentEntity student:
                findAllStudents()) {
            if (student.getClasse().equals(classEnum)) {
                students.add(student);
            }
        }
        notebook.setStudents(students);
    }

}
