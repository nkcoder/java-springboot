package org.nkcoder.user.interfaces.rest;

import jakarta.validation.Valid;
import org.nkcoder.shared.local.rest.ApiResponse;
import org.nkcoder.user.application.dto.response.AuthResult;
import org.nkcoder.user.application.service.AuthApplicationService;
import org.nkcoder.user.interfaces.rest.mapper.AuthRequestMapper;
import org.nkcoder.user.interfaces.rest.request.LoginRequest;
import org.nkcoder.user.interfaces.rest.request.RefreshTokenRequest;
import org.nkcoder.user.interfaces.rest.request.RegisterRequest;
import org.nkcoder.user.interfaces.rest.response.AuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthApplicationService authService;
    private final AuthRequestMapper requestMapper;

    public AuthController(AuthApplicationService authService, AuthRequestMapper requestMapper) {
        this.authService = authService;
        this.requestMapper = requestMapper;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("Register request for email: {}", request.email());

        AuthResult result = authService.register(requestMapper.toCommand(request));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", AuthResponse.from(result)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Login request for email: {}", request.email());

        AuthResult result = authService.login(requestMapper.toCommand(request));

        return ResponseEntity.ok(ApiResponse.success("Login successful", AuthResponse.from(result)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshTokens(@Valid @RequestBody RefreshTokenRequest request) {
        logger.debug("Token refresh request");

        AuthResult result = authService.refreshTokens(requestMapper.toCommand(request));

        return ResponseEntity.ok(ApiResponse.success("Tokens refreshed", AuthResponse.from(result)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        logger.debug("Logout request (all devices)");

        authService.logout(request.refreshToken());

        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    @PostMapping("/logout-single")
    public ResponseEntity<ApiResponse<Void>> logoutSingle(@Valid @RequestBody RefreshTokenRequest request) {
        logger.debug("Logout request (single device)");

        authService.logoutSingle(request.refreshToken());

        return ResponseEntity.ok(ApiResponse.success("Logged out from current device"));
    }
}
