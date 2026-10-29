package com.clinic.appointment.doctor.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record DoctorResponse(
        UUID id,
        String name,
        String email,
        String phoneNumber,
        String specialization,
        BigDecimal consultationFee,
        boolean active
) {
}
