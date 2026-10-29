package com.clinic.appointment.appointment.dto;

import com.clinic.appointment.appointment.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentResponse(
        UUID id,
        UUID patientId,
        String patientName,
        UUID doctorId,
        String doctorName,
        LocalDateTime appointmentDateTime,
        String reason,
        AppointmentStatus status,
        LocalDateTime createdAt
) {
}
