package com.itservicedesk.backend.repository;

import com.itservicedesk.backend.entity.User;
import com.itservicedesk.backend.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByActiveTrue();

    List<User> findByRoleAndActiveTrue(UserRole role);
}