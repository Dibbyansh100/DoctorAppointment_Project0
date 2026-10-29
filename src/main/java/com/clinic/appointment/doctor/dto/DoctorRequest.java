package com.clinic.appointment.doctor.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record DoctorRequest(
        @NotBlank(message = "Doctor name is required")
        @Size(max = 100, message = "Doctor name must not exceed 100 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 150, message = "Email must not exceed 150 characters")
        String email,

        @NotBlank(message = "Phone number is required")
        @Size(max = 15, message = "Phone number must not exceed 15 characters")
        String phoneNumber,

        @NotBlank(message = "Specialization is required")
        @Size(max = 100, message = "Specialization must not exceed 100 characters")
        String specialization,

        @NotNull(message = "Consultation fee is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Consultation fee must be non-negative")
        BigDecimal consultationFee
) {
}
