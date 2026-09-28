package com.example.identityservice.config;

import com.example.identityservice.entity.User;
import com.example.identityservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("student1").isEmpty()) {
            User student = User.builder()
                    .username("student1")
                    .password(passwordEncoder.encode("123456"))
                    .role("STUDENT")
                    .build();
            userRepository.save(student);
        }

        if (userRepository.findByUsername("instructor1").isEmpty()) {
            User instructor = User.builder()
                    .username("instructor1")
                    .password(passwordEncoder.encode("123456"))
                    .role("INSTRUCTOR")
                    .build();
            userRepository.save(instructor);
        }
    }
}