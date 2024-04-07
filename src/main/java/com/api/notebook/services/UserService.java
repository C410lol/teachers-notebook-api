package com.api.notebook.services;

import com.api.notebook.enums.AuthTryEnum;
import com.api.notebook.enums.RoleEnum;
import com.api.notebook.models.AuthModel;
import com.api.notebook.models.AuthReturnModel;
import com.api.notebook.models.AuthTryModel;
import com.api.notebook.models.entities.NotebookEntity;
import com.api.notebook.models.entities.UserEntity;
import com.api.notebook.models.entities.VCodeEntity;
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

    public void user(UserEntity user) {
        userRepository.save(user);
    }

    public List<UserEntity> findAllUsers() {
        return userRepository.findAll();
    }

    public List<UserEntity> findAllUsersByRole(RoleEnum role) { return userRepository.findAllByRole(role); }

    public Optional<UserEntity> findUserById(UUID id) {
        return userRepository.findById(id);
    }

    public Optional<UserEntity> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
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

        if(userOptional.isEmpty()) {
            return new AuthTryModel(AuthTryEnum.NOT_FOUND, null);
        }

        if (!passwordEncoder.matches(authModel.getPassword(), userOptional.get().getPassword())) {
            return new AuthTryModel(AuthTryEnum.INCORRECT_PASSWORD, null);
        }

        var token = jwtService.generateToken(userOptional.get().getEmail());
        return new AuthTryModel(AuthTryEnum.OK, new AuthReturnModel(
            userOptional.get().getId(), 
            token,
            userOptional.get().getName(),
            userOptional.get().getEmail(),
            userOptional.get().getRole().name()
        ));
    }

    public void setVCodeToUser(UUID userId, @NotNull VCodeEntity verificationCode) {
        var userOptional = findUserById(userId);
        userOptional.ifPresent(verificationCode::setUser);
    }

    public void setNotebookToUser(UUID userId, @NotNull NotebookEntity notebook) { //Set notebook to an user
        var userOptional = findUserById(userId);
        userOptional.ifPresent(notebook::setUser);
    }

}
