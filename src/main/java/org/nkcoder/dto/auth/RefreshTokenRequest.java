package org.nkcoder.dto.auth;

import static org.nkcoder.validation.ValidationMessages.REFRESH_TOKEN_REQUIRED;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
    @NotBlank(message = REFRESH_TOKEN_REQUIRED) String refreshToken) {}
