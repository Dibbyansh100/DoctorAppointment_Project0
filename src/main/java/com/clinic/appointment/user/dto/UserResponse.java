package com.clinic.appointment.user.dto;

import java.util.UUID;

import com.clinic.appointment.user.UserRole;

public record UserResponse(
		UUID id,
		String email,
		UserRole userRole,
		Boolean active
		) {

}
