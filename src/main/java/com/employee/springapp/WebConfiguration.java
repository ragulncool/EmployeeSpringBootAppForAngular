package com.employee.springapp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

	private final AuthService authService;

	public WebConfiguration(AuthService authService) {
		this.authService = authService;
	}

	@Bean
	HandlerInterceptor bearerTokenInterceptor() {
		return new HandlerInterceptor() {
			@Override
			public boolean preHandle(
					HttpServletRequest request,
					HttpServletResponse response,
					Object handler) {
				String authorization = request.getHeader("Authorization");
				if (authorization == null || !authorization.startsWith("Bearer ")) {
					throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bearer token is required");
				}
				if (authService.usernameForToken(authorization.substring(7)).isEmpty()) {
					throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bearer token is invalid or expired");
				}
				return true;
			}
		};
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(bearerTokenInterceptor())
				.addPathPatterns("/api/**")
				.excludePathPatterns("/api/auth/login", "/api/auth/signup");
	}
}
