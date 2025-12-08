package org.nkcoder.auth.application.dto.response;

import java.util.UUID;
import org.nkcoder.auth.domain.model.AuthRole;
import org.nkcoder.auth.domain.model.TokenPair;

/** Result of authentication operations (register, login, refresh). */
public record AuthResult(UUID userId, String email, AuthRole role, String accessToken, String refreshToken) {

    public static AuthResult of(UUID userId, String email, AuthRole role, TokenPair tokens) {
        return new AuthResult(userId, email, role, tokens.accessToken(), tokens.refreshToken());
    }
}
