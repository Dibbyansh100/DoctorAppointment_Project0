package com.clinic.appointment.appointment;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    boolean existsByDoctorIdAndAppointmentDateTimeAndStatus(
            UUID doctorId,
            LocalDateTime appointmentDateTime,
            AppointmentStatus status
    );

    boolean existsByPatientIdAndAppointmentDateTimeAndStatus(
            UUID patientId,
            LocalDateTime appointmentDateTime,
            AppointmentStatus status
    );

    @EntityGraph(attributePaths = {"patient", "doctor"})
    Optional<Appointment> findWithDetailsById(UUID id);

    @EntityGraph(attributePaths = {"patient", "doctor"})
    List<Appointment> findAllByOrderByAppointmentDateTimeAscIdAsc();

    @EntityGraph(attributePaths = {"patient", "doctor"})
    List<Appointment> findByPatientIdOrderByAppointmentDateTimeAscIdAsc(UUID patientId);

    @EntityGraph(attributePaths = {"patient", "doctor"})
    List<Appointment> findByDoctorIdOrderByAppointmentDateTimeAscIdAsc(UUID doctorId);
}
