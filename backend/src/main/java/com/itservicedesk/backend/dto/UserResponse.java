package com.itservicedesk.backend.dto;

import com.itservicedesk.backend.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String fullName;
    private String email;
    private UserRole role;
    private String department;
    private boolean active;
    private LocalDateTime createdAt;
}