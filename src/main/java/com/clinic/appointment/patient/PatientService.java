package com.clinic.appointment.patient;

import com.clinic.appointment.exception.DuplicateResourceException;
import com.clinic.appointment.exception.ResourceNotFoundException;
import com.clinic.appointment.patient.dto.PatientRequest;
import com.clinic.appointment.patient.dto.PatientResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Transactional
    public PatientResponse createPatient(PatientRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        if (patientRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("Patient already exists with email: " + request.email());
        }

        Patient patient = new Patient();
        patient.setName(request.name().trim());
        patient.setEmail(normalizedEmail);
        patient.setPhoneNumber(request.phoneNumber().trim());
        patient.setActive(true);

        Patient saved = patientRepository.save(patient);
        return mapToResponse(saved);
    }

    public List<PatientResponse> getAllPatients() {
        return patientRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public PatientResponse getPatientById(UUID id) {
        Patient patient = getPatientEntityById(id);
        return mapToResponse(patient);
    }

    @Transactional
    public PatientResponse updatePatient(UUID id, PatientRequest request) {
        Patient patient = getPatientEntityById(id);

        String normalizedEmail = request.email().trim().toLowerCase();
        if (patientRepository.existsByEmailAndIdNot(normalizedEmail, id)) {
            throw new DuplicateResourceException("Email is already in use by another patient: " + request.email());
        }

        // Updating details must not change the ID and must NOT reactivate a deactivated patient
        patient.setName(request.name().trim());
        patient.setEmail(normalizedEmail);
        patient.setPhoneNumber(request.phoneNumber().trim());

        Patient updated = patientRepository.save(patient);
        return mapToResponse(updated);
    }

    @Transactional
    public PatientResponse deactivatePatient(UUID id) {
        Patient patient = getPatientEntityById(id);
        patient.setActive(false);
        Patient updated = patientRepository.save(patient);
        return mapToResponse(updated);
    }

    @Transactional
    public PatientResponse activatePatient(UUID id) {
        Patient patient = getPatientEntityById(id);
        patient.setActive(true);
        Patient updated = patientRepository.save(patient);
        return mapToResponse(updated);
    }

    public Patient getPatientEntityById(UUID id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + id));
    }

    public PatientResponse mapToResponse(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getName(),
                patient.getEmail(),
                patient.getPhoneNumber(),
                patient.isActive()
        );
    }
}
