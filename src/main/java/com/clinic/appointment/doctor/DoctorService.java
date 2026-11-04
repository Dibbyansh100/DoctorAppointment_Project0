package com.clinic.appointment.doctor;

import com.clinic.appointment.doctor.dto.DoctorRequest;
import com.clinic.appointment.doctor.dto.DoctorResponse;
import com.clinic.appointment.exception.BusinessRuleViolationException;
import com.clinic.appointment.exception.DuplicateResourceException;
import com.clinic.appointment.exception.ResourceNotFoundException;
import com.clinic.appointment.user.User;
import com.clinic.appointment.user.UserRepository;
import com.clinic.appointment.user.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DoctorService(DoctorRepository doctorRepository,
                         UserRepository userRepository,
                         PasswordEncoder passwordEncoder) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public DoctorResponse createDoctor(DoctorRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        if (doctorRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("Doctor already exists with email: " + request.email());
        }

        if (request.consultationFee().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleViolationException("Consultation fee cannot be negative.");
        }

        Doctor doctor = new Doctor();
        doctor.setName(request.name().trim());
        doctor.setEmail(normalizedEmail);
        doctor.setPhoneNumber(request.phoneNumber().trim());
        doctor.setSpecialization(request.specialization().trim());
        doctor.setConsultationFee(request.consultationFee());
        doctor.setActive(true);

        Doctor saved = doctorRepository.save(doctor);

        // Auto-create or promote User account so Doctor can log in immediately
        userRepository.findByEmailIgnoreCase(normalizedEmail).ifPresentOrElse(
                existingUser -> {
                    existingUser.setRole(UserRole.DOCTOR);
                    existingUser.setActive(true);
                    userRepository.save(existingUser);
                },
                () -> {
                    User doctorUser = new User();
                    doctorUser.setEmail(normalizedEmail);
                    doctorUser.setPasswordHash(passwordEncoder.encode("Doctor@123"));
                    doctorUser.setRole(UserRole.DOCTOR);
                    doctorUser.setActive(true);
                    userRepository.save(doctorUser);
                }
        );

        return mapToResponse(saved);
    }

    public List<DoctorResponse> getDoctors(String specialization) {
        List<Doctor> doctors;
        if (specialization != null && !specialization.isBlank()) {
            doctors = doctorRepository.findBySpecializationIgnoreCaseOrderByNameAsc(specialization.trim());
        } else {
            doctors = doctorRepository.findAllByOrderByNameAsc();
        }
        return doctors.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public DoctorResponse getDoctorById(UUID id) {
        Doctor doctor = getDoctorEntityById(id);
        return mapToResponse(doctor);
    }

    @Transactional
    public DoctorResponse updateDoctor(UUID id, DoctorRequest request) {
        Doctor doctor = getDoctorEntityById(id);

        String normalizedEmail = request.email().trim().toLowerCase();
        if (doctorRepository.existsByEmailAndIdNot(normalizedEmail, id)) {
            throw new DuplicateResourceException("Email is already in use by another doctor: " + request.email());
        }

        if (request.consultationFee().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleViolationException("Consultation fee cannot be negative.");
        }

        // Updating doctor details must not change the ID and must not reactivate a deactivated doctor
        doctor.setName(request.name().trim());
        doctor.setEmail(normalizedEmail);
        doctor.setPhoneNumber(request.phoneNumber().trim());
        doctor.setSpecialization(request.specialization().trim());
        doctor.setConsultationFee(request.consultationFee());

        Doctor updated = doctorRepository.save(doctor);
        return mapToResponse(updated);
    }

    @Transactional
    public DoctorResponse deactivateDoctor(UUID id) {
        Doctor doctor = getDoctorEntityById(id);
        doctor.setActive(false);
        userRepository.findByEmailIgnoreCase(doctor.getEmail()).ifPresent(u -> {
            u.setActive(false);
            userRepository.save(u);
        });
        Doctor updated = doctorRepository.save(doctor);
        return mapToResponse(updated);
    }

    @Transactional
    public DoctorResponse activateDoctor(UUID id) {
        Doctor doctor = getDoctorEntityById(id);
        doctor.setActive(true);
        userRepository.findByEmailIgnoreCase(doctor.getEmail()).ifPresent(u -> {
            u.setActive(true);
            userRepository.save(u);
        });
        Doctor updated = doctorRepository.save(doctor);
        return mapToResponse(updated);
    }

    public Doctor getDoctorEntityById(UUID id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + id));
    }

    public DoctorResponse mapToResponse(Doctor doctor) {
        return new DoctorResponse(
                doctor.getId(),
                doctor.getName(),
                doctor.getEmail(),
                doctor.getPhoneNumber(),
                doctor.getSpecialization(),
                doctor.getConsultationFee(),
                doctor.isActive()
        );
    }
}
