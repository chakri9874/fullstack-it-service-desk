package com.itservicedesk.backend.service;

import com.itservicedesk.backend.dto.AuthenticatedUserResponse;
import com.itservicedesk.backend.dto.LoginRequest;
import com.itservicedesk.backend.dto.LoginResponse;
import com.itservicedesk.backend.entity.User;
import com.itservicedesk.backend.exception.UnauthorizedException;
import com.itservicedesk.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(
                        request.getEmail().trim().toLowerCase()
                )
                .orElseThrow(() ->
                        new UnauthorizedException(
                                "Invalid email or password."
                        )
                );

        if (!user.isActive()) {
            throw new UnauthorizedException(
                    "User account is inactive."
            );
        }

        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        );

        if (!passwordMatches) {
            throw new UnauthorizedException(
                    "Invalid email or password."
            );
        }

        String token = jwtService.generateToken(user);

        return new LoginResponse(
                token,
                "Bearer",
                mapToAuthenticatedUserResponse(user)
        );
    }

    private AuthenticatedUserResponse mapToAuthenticatedUserResponse(User user) {

        return new AuthenticatedUserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getDepartment()
        );
    }
}