package com.employee.springapp;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/signup")
	public ResponseEntity<AuthResponse> signup(@RequestBody Credentials credentials) {
		return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(credentials));
	}

	@PostMapping("/login")
	public AuthResponse login(@RequestBody Credentials credentials) {
		return authService.login(credentials);
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(
			@RequestHeader(name = "Authorization", required = false) String authorization) {
		authService.logout(bearerToken(authorization));
		return ResponseEntity.noContent().build();
	}

	private String bearerToken(String authorization) {
		if (authorization == null || !authorization.startsWith("Bearer ")) {
			return null;
		}
		return authorization.substring(7);
	}

	public record Credentials(String username, String password) {
	}

	public record AuthResponse(String accessToken, String tokenType, Instant expiresAt) {
	}
}
