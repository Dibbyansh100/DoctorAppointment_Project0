package com.clinic.appointment.user;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.clinic.appointment.doctor.Doctor;
import com.clinic.appointment.doctor.DoctorRepository;
import com.clinic.appointment.patient.Patient;
import com.clinic.appointment.patient.PatientRepository;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;

@Configuration
@AllArgsConstructor
public class DataInitializer {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

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

            // Create DOCTOR User
            createUser(
                "Dr. Rahul",
                "doctor@gmail.com",
                "Doctor@123",
                UserRole.DOCTOR
            );

            // Create PATIENT User
            createUser(
                "Rahul Sharma",
                "rahul@gmail.com",
                "Rahul@123",
                UserRole.PATIENT
            );

            // Ensure default Doctor entity exists in doctors table
            if (!doctorRepository.existsByEmail("doctor@gmail.com")) {
                Doctor defaultDoc = new Doctor();
                defaultDoc.setName("Dr. Rahul");
                defaultDoc.setEmail("doctor@gmail.com");
                defaultDoc.setPhoneNumber("9876543211");
                defaultDoc.setSpecialization("General Medicine");
                defaultDoc.setConsultationFee(new BigDecimal("50.00"));
                defaultDoc.setActive(true);
                doctorRepository.save(defaultDoc);
            }

            // Ensure default Patient entity exists in patients table
            if (!patientRepository.existsByEmail("rahul@gmail.com")) {
                Patient defaultPatient = new Patient();
                defaultPatient.setName("Rahul Sharma");
                defaultPatient.setEmail("rahul@gmail.com");
                defaultPatient.setPhoneNumber("9876543210");
                defaultPatient.setActive(true);
                patientRepository.save(defaultPatient);
            }

            // Sync all existing Doctors in DB so every Doctor has a login User account
            for (Doctor doc : doctorRepository.findAll()) {
                String docEmail = doc.getEmail().toLowerCase();
                userRepository.findByEmailIgnoreCase(docEmail).ifPresentOrElse(
                    u -> {
                        if (u.getRole() != UserRole.DOCTOR) {
                            u.setRole(UserRole.DOCTOR);
                            userRepository.save(u);
                            System.out.println("Promoted existing user to DOCTOR: " + docEmail);
                        }
                    },
                    () -> {
                        User user = new User();
                        user.setEmail(docEmail);
                        user.setPasswordHash(passwordEncoder.encode("Doctor@123"));
                        user.setRole(UserRole.DOCTOR);
                        user.setActive(doc.isActive());
                        userRepository.save(user);
                        System.out.println("Auto-created login credentials for Doctor: " + docEmail + " (Password: Doctor@123)");
                    }
                );
            }
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
