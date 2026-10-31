package com.clinic.appointment.user.dto;

public record AuthResponse(
		String accessToken,
		String tokenType,
		Long expiresIn,
		UserResponse user
		) {

}
