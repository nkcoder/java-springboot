package org.nkcoder.user.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nkcoder.shared.kernel.exception.AuthenticationException;
import org.nkcoder.user.domain.model.Email;
import org.nkcoder.user.domain.model.HashedPassword;
import org.nkcoder.user.domain.model.RefreshToken;
import org.nkcoder.user.domain.model.TokenFamily;
import org.nkcoder.user.domain.model.TokenPair;
import org.nkcoder.user.domain.model.User;
import org.nkcoder.user.domain.model.UserId;
import org.nkcoder.user.domain.model.UserName;
import org.nkcoder.user.domain.model.UserRole;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenRotationService")
class TokenRotationServiceTest {

    @Mock
    private TokenGenerator tokenGenerator;

    private TokenRotationService tokenRotationService;

    @BeforeEach
    void setUp() {
        tokenRotationService = new TokenRotationService(tokenGenerator);
    }

    @Nested
    @DisplayName("rotate")
    class Rotate {

        @Test
        @DisplayName("returns new token pair when token is valid")
        void returnsNewTokenPairWhenTokenIsValid() {
            User user = createTestUser();
            TokenFamily family = TokenFamily.generate();
            RefreshToken currentToken = createValidToken(user.getId(), family);
            TokenPair expectedTokenPair = new TokenPair("new-access-token", "new-refresh-token");

            given(tokenGenerator.generateTokenPair(any(), any(), any(), any())).willReturn(expectedTokenPair);

            TokenPair result = tokenRotationService.rotate(currentToken, user);

            assertThat(result).isEqualTo(expectedTokenPair);
        }

        @Test
        @DisplayName("preserves token family during rotation")
        void preservesTokenFamilyDuringRotation() {
            User user = createTestUser();
            TokenFamily family = TokenFamily.generate();
            RefreshToken currentToken = createValidToken(user.getId(), family);
            TokenPair expectedTokenPair = new TokenPair("access", "refresh");

            given(tokenGenerator.generateTokenPair(user.getId(), user.getEmail(), user.getRole(), family))
                    .willReturn(expectedTokenPair);

            TokenPair result = tokenRotationService.rotate(currentToken, user);

            assertThat(result).isEqualTo(expectedTokenPair);
        }

        @Test
        @DisplayName("throws AuthenticationException when token is expired")
        void throwsWhenTokenIsExpired() {
            User user = createTestUser();
            RefreshToken expiredToken = createExpiredToken(user.getId());

            assertThatThrownBy(() -> tokenRotationService.rotate(expiredToken, user))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage(TokenRotationService.REFRESH_TOKEN_EXPIRED);
        }
    }

    @Nested
    @DisplayName("generateTokens")
    class GenerateTokens {

        @Test
        @DisplayName("generates new token pair with specified family")
        void generatesNewTokenPairWithSpecifiedFamily() {
            User user = createTestUser();
            TokenFamily family = TokenFamily.generate();
            TokenPair expectedTokenPair = new TokenPair("access-token", "refresh-token");

            given(tokenGenerator.generateTokenPair(user.getId(), user.getEmail(), user.getRole(), family))
                    .willReturn(expectedTokenPair);

            TokenPair result = tokenRotationService.generateTokens(user, family);

            assertThat(result).isEqualTo(expectedTokenPair);
        }
    }

    private User createTestUser() {
        return User.reconstitute(
                UserId.generate(),
                Email.of("user@example.com"),
                HashedPassword.of("hashed"),
                UserName.of("Test User"),
                UserRole.MEMBER,
                false,
                null,
                LocalDateTime.now(),
                LocalDateTime.now());
    }

    private RefreshToken createValidToken(UserId userId, TokenFamily family) {
        return RefreshToken.create(
                "valid-token", family, userId, LocalDateTime.now().plusDays(7));
    }

    private RefreshToken createExpiredToken(UserId userId) {
        return RefreshToken.create(
                "expired-token",
                TokenFamily.generate(),
                userId,
                LocalDateTime.now().minusSeconds(1));
    }
}
