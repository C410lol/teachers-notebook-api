package com.api.notebook.services;

import com.api.notebook.models.AuthModel;
import com.api.notebook.models.entities.NotebookEntity;
import com.api.notebook.models.entities.TeacherEntity;
import com.api.notebook.repositories.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;

    public void saveTeacher(@NotNull TeacherEntity teacher) {
        if (teacher.getId() == null) { //Checks if this entity doesn't exist, if so it encodes its password
            teacher.setPassword(passwordEncoder.encode(teacher.getPassword()));
        }
        teacherRepository.save(teacher);
    }

    public List<TeacherEntity> findAllTeachers() {
        return teacherRepository.findAll();
    }

    public Optional<TeacherEntity> findTeacherById(UUID id) {
        return teacherRepository.findById(id);
    }

    public Optional<TeacherEntity> findTeacherByEmail(String email) {
        return teacherRepository.findByEmail(email);
    }

    public void deleteTeacherById(UUID id) {
        teacherRepository.deleteById(id);
    }

    //Try to authenticate teacher
    public String tryToAuthenticateTeacher(@NotNull AuthModel authModel, JwtService jwtService) {
        var teacherOptional = findTeacherByEmail(authModel.getEmail());
        if (teacherOptional.isPresent()) { //Verify if the teacher exists

            //Verify if the auth model password matches the found teacher entity
            if (passwordEncoder.matches(authModel.getPassword(), teacherOptional.get().getPassword())) {
                return jwtService.generateToken(teacherOptional.get().getEmail()); //If success it returns a token
            }
        }
        return null;
    }

    public void setNotebookToTeacher(UUID teacherId, @NotNull NotebookEntity notebook) { //Set notebook to a teacher
        var teacherOptional = findTeacherById(teacherId);
        teacherOptional.ifPresent(notebook::setTeacher);
    }

}
