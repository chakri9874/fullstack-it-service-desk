package com.itservicedesk.backend.bootstrap;

import com.itservicedesk.backend.entity.User;
import com.itservicedesk.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DemoUserPasswordInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.demo.reset-passwords:false}")
    private boolean resetDemoPasswords;

    public DemoUserPasswordInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        if (!resetDemoPasswords) {
            return;
        }

        resetPasswordIfUserExists(
                "john.smith@example.com",
                "Support@123"
        );

        resetPasswordIfUserExists(
                "user.one@example.com",
                "Employee@123"
        );

        resetPasswordIfUserExists(
                "it.support.one@example.com",
                "Support@123"
        );

        resetPasswordIfUserExists(
                "admin.one@example.com",
                "Admin@123"
        );
    }

    private void resetPasswordIfUserExists(
            String email,
            String temporaryPassword) {

        userRepository.findByEmail(email)
                .ifPresent(user -> updatePassword(
                        user,
                        temporaryPassword
                ));
    }

    private void updatePassword(
            User user,
            String temporaryPassword) {

        user.setPasswordHash(
                passwordEncoder.encode(temporaryPassword)
        );

        userRepository.save(user);
    }
}