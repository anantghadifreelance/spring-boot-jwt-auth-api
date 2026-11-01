package com.anantghadi.jwtauth.config;

import com.anantghadi.jwtauth.user.Role;
import com.anantghadi.jwtauth.user.User;
import com.anantghadi.jwtauth.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Creates a demo admin account on startup so the ADMIN-only endpoint can be tried locally. */
@Component
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       @Value("${ADMIN_EMAIL:admin@example.com}") String adminEmail,
                       @Value("${ADMIN_PASSWORD:Admin@12345}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail(adminEmail)) {
            userRepository.save(new User("Admin", adminEmail, passwordEncoder.encode(adminPassword), Role.ADMIN));
        }
    }
}
