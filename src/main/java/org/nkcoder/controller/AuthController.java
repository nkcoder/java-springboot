package org.nkcoder.controller;

import jakarta.validation.Valid;
import org.nkcoder.dto.auth.AuthResponse;
import org.nkcoder.dto.auth.LoginRequest;
import org.nkcoder.dto.auth.RefreshTokenRequest;
import org.nkcoder.dto.auth.RegisterRequest;
import org.nkcoder.dto.common.ApiResponse;
import org.nkcoder.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/auth")
public class AuthController {

  private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

  private final AuthService authService;

  @Autowired
  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public ResponseEntity<ApiResponse<AuthResponse>> register(
      @Valid @RequestBody RegisterRequest request) {
    logger.debug("Registration request received.");

    AuthResponse authResponse = authService.register(request);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("User registered successfully", authResponse));
  }

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
    logger.debug("Login request received.");

    AuthResponse authResponse = authService.login(request);

    return ResponseEntity.ok(ApiResponse.success("Login successfully", authResponse));
  }

  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<AuthResponse>> refreshTokens(
      @Valid @RequestBody RefreshTokenRequest request) {
    logger.info("Token refresh request");

    AuthResponse authResponse = authService.refreshTokens(request.refreshToken());

    return ResponseEntity.ok(ApiResponse.success("Tokens refreshed successfully", authResponse));
  }

  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshTokenRequest request) {
    logger.info("Logout request (all devices)");

    authService.logout(request.refreshToken());

    return ResponseEntity.ok(ApiResponse.success("Logout successful"));
  }

  @PostMapping("/logout-single")
  public ResponseEntity<ApiResponse<Void>> logoutSingle(
      @Valid @RequestBody RefreshTokenRequest request) {
    logger.info("Logout request (single device)");

    authService.logoutSingle(request.refreshToken());

    return ResponseEntity.ok(ApiResponse.success("Logout from current device successful"));
  }
}
