package com.employee.springapp;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Service
public class AuthService {

	private static final Duration TOKEN_LIFETIME = Duration.ofHours(24);
	private final JdbcTemplate jdbcTemplate;
	private final PasswordEncoder passwordEncoder;
	private final SecureRandom secureRandom = new SecureRandom();

	public AuthService(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
		this.jdbcTemplate = jdbcTemplate;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public AuthController.AuthResponse signup(AuthController.Credentials credentials) {
		validateCredentials(credentials);
		try {
			jdbcTemplate.update(
					"INSERT INTO app_user (username, password_hash) VALUES (?, ?)",
					credentials.username(), passwordEncoder.encode(credentials.password()));
		} catch (DataIntegrityViolationException exception) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already registered");
		}
		return issueToken(credentials.username());
	}

	@Transactional
	public AuthController.AuthResponse login(AuthController.Credentials credentials) {
		validateCredentials(credentials);
		List<String> passwordHashes = jdbcTemplate.query(
				"SELECT password_hash FROM app_user WHERE username = ?",
				(rs, rowNum) -> rs.getString("password_hash"),
				credentials.username());
		if (passwordHashes.isEmpty() || !passwordEncoder.matches(credentials.password(), passwordHashes.get(0))) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
		}
		return issueToken(credentials.username());
	}

	public Optional<String> usernameForToken(String token) {
		if (token == null || token.isBlank()) {
			return Optional.empty();
		}
		List<String> usernames = jdbcTemplate.query(
				"SELECT username FROM auth_token WHERE token_hash = ? AND expires_at > CURRENT_TIMESTAMP",
				(rs, rowNum) -> rs.getString("username"),
				hashToken(token));
		return usernames.stream().findFirst();
	}

	public void logout(String token) {
		if (token != null && !token.isBlank()) {
			jdbcTemplate.update("DELETE FROM auth_token WHERE token_hash = ?", hashToken(token));
		}
	}

	private AuthController.AuthResponse issueToken(String username) {
		byte[] tokenBytes = new byte[32];
		secureRandom.nextBytes(tokenBytes);
		String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
		Instant expiresAt = Instant.now().plus(TOKEN_LIFETIME);
		jdbcTemplate.update(
				"INSERT INTO auth_token (token_hash, username, expires_at) VALUES (?, ?, ?)",
				hashToken(token), username, Timestamp.from(expiresAt));
		return new AuthController.AuthResponse(token, "Bearer", expiresAt);
	}

	private void validateCredentials(AuthController.Credentials credentials) {
		if (credentials == null
				|| credentials.username() == null
				|| !credentials.username().matches("[A-Za-z0-9._-]{3,64}")
				|| credentials.password() == null
				|| credentials.password().length() < 8
				|| credentials.password().getBytes(StandardCharsets.UTF_8).length > 72) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST,
					"Username must be 3-64 letters, numbers, dots, underscores, or hyphens; password must be 8-72 UTF-8 bytes");
		}
	}

	private String hashToken(String token) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256")
					.digest(token.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is not available", exception);
		}
	}
}
