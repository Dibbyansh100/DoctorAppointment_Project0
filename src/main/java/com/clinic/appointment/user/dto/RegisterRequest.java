package com.clinic.appointment.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
		@NotBlank(message = "email is required")
		@Email(message = "email should be in xyz@domail.abc format")
		@Size(max = 150)
		String email,
		@NotBlank
		@Size(min = 8,max =100)
		String password
		) {

}
