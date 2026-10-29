package com.clinic.appointment.doctor;

import com.clinic.appointment.doctor.dto.DoctorRequest;
import com.clinic.appointment.doctor.dto.DoctorResponse;
import com.clinic.appointment.exception.BusinessRuleViolationException;
import com.clinic.appointment.exception.DuplicateResourceException;
import com.clinic.appointment.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
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
        Doctor updated = doctorRepository.save(doctor);
        return mapToResponse(updated);
    }

    @Transactional
    public DoctorResponse activateDoctor(UUID id) {
        Doctor doctor = getDoctorEntityById(id);
        doctor.setActive(true);
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
