package org.nkcoder.user.domain.service;

import org.nkcoder.shared.kernel.exception.AuthenticationException;
import org.nkcoder.user.domain.model.RefreshToken;
import org.nkcoder.user.domain.model.TokenPair;
import org.nkcoder.user.domain.model.User;
import org.springframework.stereotype.Service;

/** Domain service for token rotation. Encapsulates the logic of validating and rotating refresh tokens. */
@Service
public class TokenRotationService {

    public static final String REFRESH_TOKEN_EXPIRED = "Refresh token expired";

    private final TokenGenerator tokenGenerator;

    public TokenRotationService(TokenGenerator tokenGenerator) {
        this.tokenGenerator = tokenGenerator;
    }

    /**
     * Rotates a refresh token, generating a new token pair while maintaining the same token family.
     *
     * @param currentToken the current refresh token
     * @param user the user associated with the token
     * @return a new token pair with the same token family
     * @throws AuthenticationException if the token is expired
     */
    public TokenPair rotate(RefreshToken currentToken, User user) {
        if (currentToken.isExpired()) {
            throw new AuthenticationException(REFRESH_TOKEN_EXPIRED);
        }

        return tokenGenerator.generateTokenPair(
                user.getId(), user.getEmail(), user.getRole(), currentToken.getTokenFamily());
    }

    /**
     * Generates a new token pair for a user with a new token family.
     *
     * @param user the user
     * @param tokenFamily the token family to use
     * @return the generated token pair
     */
    public TokenPair generateTokens(User user, org.nkcoder.user.domain.model.TokenFamily tokenFamily) {
        return tokenGenerator.generateTokenPair(user.getId(), user.getEmail(), user.getRole(), tokenFamily);
    }
}
