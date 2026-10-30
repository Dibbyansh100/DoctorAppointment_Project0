package com.clinic.appointment.user;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;


@Service
@AllArgsConstructor
public class JwtService {
	private final JwtEncoder jwtEncoder;
	private final long expairationMinutes;
	@Value("${Spring.security.jwt.expairation-minutes}")
	public String generateToken(User user) {
		Instant now = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer("doctor-appointment-api")
				.issuedAt(now)
				.expiresAt(now.plus(expairationMinutes,  ChronoUnit.MINUTES))
				.subject(user.getEmail())
				.claim("role", List.of(user.getRole().name()))
				.build();
		JwsHeader header = JwsHeader
				.with(MacAlgorithm.HS256)
				.build();
		return jwtEncoder
				.encode(JwtEncoderParameters.from(header, claims))
				.getTokenValue();
	}

}
