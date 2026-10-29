package com.clinic.appointment.patient.dto;

import java.util.UUID;

public record PatientResponse(
        UUID id,
        String name,
        String email,
        String phoneNumber,
        boolean active
) {
}
