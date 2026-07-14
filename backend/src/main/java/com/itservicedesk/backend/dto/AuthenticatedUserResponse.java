package com.itservicedesk.backend.dto;

import com.itservicedesk.backend.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthenticatedUserResponse {

    private Long id;
    private String fullName;
    private String email;
    private UserRole role;
    private String department;
}