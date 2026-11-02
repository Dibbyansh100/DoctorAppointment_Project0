package com.clinic.appointment.user;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.clinic.appointment.exception.BusinessRuleViolationException;
import com.clinic.appointment.user.dto.AuthResponse;
import com.clinic.appointment.user.dto.LoginRequest;
import com.clinic.appointment.user.dto.RegisterRequest;
import com.clinic.appointment.user.dto.UserResponse;


@Service
public class AuthService {
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private JwtService jwtService;
	
	@Transactional
	public UserResponse register(RegisterRequest request) {
		if(userRepository.existsByEmailIgnoreCase(request.email().trim())) {
			throw new BusinessRuleViolationException("Email already registered");
		}
		User user = new User();
		user.setEmail(request.email().trim().toLowerCase());
		user.setPasswordHash(passwordEncoder.encode(request.password()));
		user.setRole(UserRole.PATIENT);
		user.setActive(true);
		User savedUser = userRepository.save(user);
		return new UserResponse(
				savedUser.getId(),
				savedUser.getEmail(),
				savedUser.getRole(),
				savedUser.isActive()
				);
		
	}
	@Transactional(readOnly = true)
	public AuthResponse login(LoginRequest request) {
		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(
							request.email().trim().toLowerCase(),
							request.password()
							)
					);
		} catch (AuthenticationException e) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid credentials");
		}
		User user = userRepository.findByEmailIgnoreCase(request.email().trim().toLowerCase())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid credentials")
					);
		String accessToken = jwtService.generateToken(user);
		return new AuthResponse(
				accessToken,
				"Bearer",
				jwtService.getExpirationSeconds(),
				 new UserResponse(
						 user.getId(),
						 user.getEmail(),
						 user.getRole(),
						 user.isActive()
				)
			);
	}

}
