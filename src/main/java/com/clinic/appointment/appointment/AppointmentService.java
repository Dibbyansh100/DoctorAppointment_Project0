package com.clinic.appointment.appointment;

import com.clinic.appointment.appointment.dto.AppointmentRequest;
import com.clinic.appointment.appointment.dto.AppointmentResponse;
import com.clinic.appointment.doctor.Doctor;
import com.clinic.appointment.doctor.DoctorService;
import com.clinic.appointment.exception.BusinessRuleViolationException;
import com.clinic.appointment.exception.ResourceNotFoundException;
import com.clinic.appointment.patient.Patient;
import com.clinic.appointment.patient.PatientService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientService patientService;
    private final DoctorService doctorService;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            PatientService patientService,
            DoctorService doctorService
    ) {
        this.appointmentRepository = appointmentRepository;
        this.patientService = patientService;
        this.doctorService = doctorService;
    }

    /**
     * Books a new appointment after validating business constraints and conflict checks.
     * <p>
     * Note on Concurrency: These application-level conflict checks operate within the current
     * transaction. Under high concurrent traffic with simultaneous requests for the exact same
     * doctor/patient and time slot, a race condition could occur unless backed by database-level
     * constraints (such as a PostgreSQL partial unique index on doctor_id, appointment_date_time
     * where status = 'SCHEDULED') or pessimistic/optimistic locking.
     */
    @Transactional
    public AppointmentResponse bookAppointment(AppointmentRequest request) {
        Patient patient = patientService.getPatientEntityById(request.patientId());
        if (!patient.isActive()) {
            throw new BusinessRuleViolationException("Inactive patient cannot book new appointments.");
        }

        Doctor doctor = doctorService.getDoctorEntityById(request.doctorId());
        if (!doctor.isActive()) {
            throw new BusinessRuleViolationException("Inactive doctor cannot receive new appointments.");
        }

        LocalDateTime requestedTime = request.appointmentDateTime();
        if (!requestedTime.isAfter(LocalDateTime.now())) {
            throw new BusinessRuleViolationException("Appointment date/time must be in the future.");
        }

        // Check for doctor schedule conflict (ignore CANCELLED and COMPLETED appointments)
        boolean doctorConflict = appointmentRepository.existsByDoctorIdAndAppointmentDateTimeAndStatus(
                doctor.getId(),
                requestedTime,
                AppointmentStatus.SCHEDULED
        );
        if (doctorConflict) {
            throw new BusinessRuleViolationException("Doctor already has a scheduled appointment at this date and time.");
        }

        // Check for patient schedule conflict (ignore CANCELLED and COMPLETED appointments)
        boolean patientConflict = appointmentRepository.existsByPatientIdAndAppointmentDateTimeAndStatus(
                patient.getId(),
                requestedTime,
                AppointmentStatus.SCHEDULED
        );
        if (patientConflict) {
            throw new BusinessRuleViolationException("Patient already has a scheduled appointment at this date and time.");
        }

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDateTime(requestedTime);
        appointment.setReason(request.reason().trim());
        appointment.setStatus(AppointmentStatus.SCHEDULED);

        Appointment saved = appointmentRepository.save(appointment);
        return mapToResponse(saved);
    }

    public List<AppointmentResponse> getAllAppointments() {
        return appointmentRepository.findAllByOrderByAppointmentDateTimeAscIdAsc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public AppointmentResponse getAppointmentById(UUID id) {
        Appointment appointment = appointmentRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + id));
        return mapToResponse(appointment);
    }

    public List<AppointmentResponse> getAppointmentsByPatient(UUID patientId) {
        // Verify patient exists; throws ResourceNotFoundException if missing
        patientService.getPatientEntityById(patientId);

        return appointmentRepository.findByPatientIdOrderByAppointmentDateTimeAscIdAsc(patientId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<AppointmentResponse> getAppointmentsByDoctor(UUID doctorId) {
        // Verify doctor exists; throws ResourceNotFoundException if missing
        doctorService.getDoctorEntityById(doctorId);

        return appointmentRepository.findByDoctorIdOrderByAppointmentDateTimeAscIdAsc(doctorId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public AppointmentResponse cancelAppointment(UUID id) {
        Appointment appointment = appointmentRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + id));

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BusinessRuleViolationException("Cannot cancel an appointment that has already been completed.");
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BusinessRuleViolationException("Appointment is already cancelled.");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        Appointment updated = appointmentRepository.save(appointment);
        return mapToResponse(updated);
    }

    @Transactional
    public AppointmentResponse completeAppointment(UUID id) {
        Appointment appointment = appointmentRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + id));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BusinessRuleViolationException("Cannot complete an appointment that has been cancelled.");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BusinessRuleViolationException("Appointment has already been marked completed.");
        }

        if (LocalDateTime.now().isBefore(appointment.getAppointmentDateTime())) {
            throw new BusinessRuleViolationException("Cannot complete an appointment before its scheduled date and time.");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        Appointment updated = appointmentRepository.save(appointment);
        return mapToResponse(updated);
    }

    public AppointmentResponse mapToResponse(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getPatient().getId(),
                appointment.getPatient().getName(),
                appointment.getDoctor().getId(),
                appointment.getDoctor().getName(),
                appointment.getAppointmentDateTime(),
                appointment.getReason(),
                appointment.getStatus(),
                appointment.getCreatedAt()
        );
    }
}
