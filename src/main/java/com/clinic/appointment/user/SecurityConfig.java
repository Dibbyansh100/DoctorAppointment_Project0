package com.clinic.appointment.user;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jose.jwk.source.ImmutableSecret;


@Configuration(proxyBeanMethods = false)
public class SecurityConfig {
	@Value("${spring.security.jwt.secret}")
	String secret;
	@Bean
	public SecretKey jwtSecretKey() {
		byte[] keyBites = secret.getBytes(StandardCharsets.UTF_8);
		return new SecretKeySpec(keyBites, "HmacSHA256");
		
	}
	@Bean
	public JwtEncoder jwtEncoder() {
		return new NimbusJwtEncoder(
				new ImmutableSecret<>(jwtSecretKey()));
	}
	@Bean
	public JwtDecoder jwtDecoder() {
		return  NimbusJwtDecoder.withSecretKey(jwtSecretKey()).macAlgorithm(MacAlgorithm.HS256)
				.build();
	}
	@Bean
	public PasswordEncoder passwordencoder() {
		return new BCryptPasswordEncoder();
	}
	@Bean
	public UserDetailsService userDetailsService(UserRepository userRepository) {
		
		return email-> userRepository.findByEmailIgnoreCase(email)
				.map(user -> User.withUsername(email)
								.password(user.getPasswordHash())
								.roles(user.getRole().name())
								.disabled(!user.isActive())
								.build())
				.orElseThrow(() ->new UsernameNotFoundException("Invalid credentials"));
	}
	
	@Bean
	public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder);
		return provider;
	}
	
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) {
		return configuration.getAuthenticationManager();
	}
	@Bean
	public JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
	    authoritiesConverter.setAuthoritiesClaimName("roles");
		authoritiesConverter.setAuthorityPrefix("ROLE_");
		JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
				authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
				return authenticationConverter;
	}
	@Bean
	public SecurityFilterChain chain(HttpSecurity http,
	        AuthenticationProvider authenticationProvider,
	        JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
	    http
	        .csrf(csrf -> csrf.disable())
	        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	        .authenticationProvider(authenticationProvider)
	        .authorizeHttpRequests(auth -> auth
	            .requestMatchers("/api/auth/**").permitAll()
	            .requestMatchers("/api/admin/**").hasRole("ADMIN")
	            .anyRequest().authenticated())
	        .oauth2ResourceServer(oauth2 -> oauth2
	            .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));
	    return http.build();
	}

}
