package com.clinic.appointment.user;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.AllArgsConstructor;

@Configuration
@AllArgsConstructor
public class DataInitializer {
	 private final UserRepository userRepository;
	    private final PasswordEncoder passwordEncoder;

	    @Bean
	    public CommandLineRunner initializeUsers() {
	        return args -> {

	            // Create ADMIN
	            createUser(
	                "Admin",
	                "admin@gmail.com",
	                "Admin@123",
	                UserRole.ADMIN
	            );

	            // Create DOCTOR
	            createUser(
	                "Dr. Rahul",
	                "doctor@gmail.com",
	                "Doctor@123",
	                UserRole.DOCTOR
	            );

	            // Create PATIENT
	            createUser(
	                "Rahul Sharma",
	                "rahul@gmail.com",
	                "Rahul@123",
	                UserRole.PATIENT
	            );
	        };
	    }

	    private void createUser(
	            String name,
	            String email,
	            String password,
	            UserRole role) {

	        if (!userRepository.existsByEmailIgnoreCase(email)) {

	            User user = new User();
	            user.setEmail(email);
	            user.setPasswordHash(
	                passwordEncoder.encode(password)
	            );
	            user.setRole(role);
	            user.setActive(true);

	            userRepository.save(user);

	            System.out.println(
	                role + " account created: " + email
	            );
	        }
	    }
	}
