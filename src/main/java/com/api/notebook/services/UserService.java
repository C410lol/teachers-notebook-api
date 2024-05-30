package com.api.notebook.services;

import com.api.notebook.enums.AuthTryEnum;
import com.api.notebook.enums.RoleEnum;
import com.api.notebook.models.AuthModel;
import com.api.notebook.models.AuthReturnModel;
import com.api.notebook.models.AuthTryModel;
import com.api.notebook.models.entities.NotebookEntity;
import com.api.notebook.models.entities.TeacherEntity;
import com.api.notebook.models.entities.UserEntity;
import com.api.notebook.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;




    public UserEntity createUser(@NotNull UserEntity user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public void editUser(UserEntity user) {
        userRepository.save(user);
    }




    public List<? extends UserEntity> findAllUsers() {
        return userRepository.findAll();
    }

    public List<? extends UserEntity> findAllUsersByRole(RoleEnum role) {
        return userRepository.findAllByRole(role);
    }

    public Optional<? extends UserEntity> findUserById(UUID id) {
        return userRepository.findById(id);
    }

    public Optional<? extends UserEntity> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean existsById(
            UUID id
    ) {
        return userRepository.existsById(id);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }




    public void deleteUserById(UUID id) {
        userRepository.deleteById(id);
    }




    //Try to authenticate user
    public AuthTryModel tryToAuthenticate(@NotNull AuthModel authModel, JwtService jwtService) {
        var userOptional = findUserByEmail(authModel.getEmail());

        if (userOptional.isEmpty()) {
            return new AuthTryModel(AuthTryEnum.NOT_FOUND, null);
        }

        if (!passwordEncoder.matches(authModel.getPassword(), userOptional.get().getPassword())) {
            return new AuthTryModel(AuthTryEnum.INCORRECT_PASSWORD, null);
        }

        var token = jwtService.generateToken(userOptional.get().getEmail());

        return new AuthTryModel(AuthTryEnum.OK, new AuthReturnModel(
                userOptional.get().getId(),
                token
        ));
    }

    public void setNotebookToUser(UUID userId, @NotNull NotebookEntity notebook) { //Set notebook to a user
        var userOptional = findUserById(userId);
        if (userOptional.isPresent()) {
            notebook.setTeacher((TeacherEntity) userOptional.get());
        }
    }

}
