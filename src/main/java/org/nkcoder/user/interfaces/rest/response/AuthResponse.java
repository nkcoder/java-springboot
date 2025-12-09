package org.nkcoder.user.interfaces.rest.response;

import java.util.UUID;
import org.nkcoder.user.application.dto.response.AuthResult;

/** REST API response for authentication operations. */
public record AuthResponse(UserInfo user, TokenInfo tokens) {

    public static AuthResponse from(AuthResult result) {
        return new AuthResponse(
                new UserInfo(result.userId(), result.email(), result.role().name()),
                new TokenInfo(result.accessToken(), result.refreshToken()));
    }

    public record UserInfo(UUID id, String email, String role) {}

    public record TokenInfo(String accessToken, String refreshToken) {}
}
