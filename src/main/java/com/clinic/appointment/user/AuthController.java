package com.clinic.appointment.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.clinic.appointment.user.dto.AuthResponse;
import com.clinic.appointment.user.dto.LoginRequest;
import com.clinic.appointment.user.dto.RegisterRequest;
import com.clinic.appointment.user.dto.UserResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
	
	@Autowired
	private AuthService authService;
	
	@ResponseStatus(HttpStatus.CREATED)
	@RequestMapping(method = RequestMethod.POST,path = "/register")
	UserResponse register(@RequestBody @Valid  RegisterRequest request) {
		return authService.register(request);
	}
	
	@ResponseStatus(HttpStatus.OK)
	@PostMapping("/login")
	public AuthResponse login(@RequestBody @Valid LoginRequest request) {
		return authService.login(request);
	}
}
