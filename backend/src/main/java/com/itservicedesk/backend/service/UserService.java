package com.itservicedesk.backend.service;

import com.itservicedesk.backend.dto.UserCreateRequest;
import com.itservicedesk.backend.dto.UserResponse;
import com.itservicedesk.backend.dto.UserUpdateRequest;
import com.itservicedesk.backend.entity.User;
import com.itservicedesk.backend.enums.UserRole;
import com.itservicedesk.backend.exception.DuplicateResourceException;
import com.itservicedesk.backend.exception.ResourceNotFoundException;
import com.itservicedesk.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse createUser(UserCreateRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "User already exists with email: " + request.getEmail()
            );
        }

        User user = new User();

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        if (request.getRole() == null) {
            user.setRole(UserRole.EMPLOYEE);
        } else {
            user.setRole(request.getRole());
        }

        user.setDepartment(request.getDepartment());

        User savedUser = userRepository.save(user);

        return mapToUserResponse(savedUser);
    }

    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::mapToUserResponse)
                .toList();
    }

    public UserResponse getUserById(Long id) {

        User user = findUserEntityById(id);

        return mapToUserResponse(user);
    }

    public UserResponse updateUser(
            Long id,
            UserUpdateRequest request) {

        User user = findUserEntityById(id);

        user.setFullName(request.getFullName());

        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        user.setDepartment(request.getDepartment());
        user.setActive(request.isActive());

        User updatedUser = userRepository.save(user);

        return mapToUserResponse(updatedUser);
    }

    public User findUserEntityById(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: " + id
                        )
                );
    }

    public UserResponse deactivateUser(Long id) {

        User user = findUserEntityById(id);

        user.setActive(false);

        User updatedUser = userRepository.save(user);

        return mapToUserResponse(updatedUser);
    }

    private UserResponse mapToUserResponse(User user) {

        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getDepartment(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}