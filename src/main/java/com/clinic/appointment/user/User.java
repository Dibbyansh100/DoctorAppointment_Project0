package com.clinic.appointment.user;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
	@Id
	@UuidGenerator
	private UUID id;
	@Column(nullable = false,length = 150, unique = true)
	private String email;
	@Column(name = "password_hash" ,nullable = false)
	private String passwordHash;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private UserRole role;
	@Column(nullable = false)
	private boolean isActive = true;
	

}
