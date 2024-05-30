package com.api.notebook.repositories;

import com.api.notebook.enums.RoleEnum;
import com.api.notebook.models.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<? extends UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    List<? extends UserEntity> findAllByRole(RoleEnum role);

}
