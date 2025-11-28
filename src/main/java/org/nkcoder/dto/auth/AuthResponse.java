package org.nkcoder.dto.auth;

import org.nkcoder.dto.user.UserResponse;

public record AuthResponse(UserResponse user, AuthTokens tokens) {}
