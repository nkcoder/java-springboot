package org.nkcoder.user.application.dto.response;

import java.util.UUID;
import org.nkcoder.user.domain.model.TokenPair;
import org.nkcoder.user.domain.model.UserRole;

/** Result of authentication operations (register, login, refresh). */
public record AuthResult(UUID userId, String email, UserRole role, String accessToken, String refreshToken) {

    public static AuthResult of(UUID userId, String email, UserRole role, TokenPair tokens) {
        return new AuthResult(userId, email, role, tokens.accessToken(), tokens.refreshToken());
    }
}
