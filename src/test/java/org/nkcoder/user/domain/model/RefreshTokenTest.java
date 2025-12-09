package org.nkcoder.user.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RefreshToken Entity")
class RefreshTokenTest {

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("creates token with generated ID")
        void createsTokenWithGeneratedId() {
            RefreshToken token = RefreshToken.create(
                    "jwt-token-value",
                    TokenFamily.generate(),
                    UserId.generate(),
                    LocalDateTime.now().plusDays(7));

            assertThat(token.getId()).isNotNull();
            assertThat(token.getToken()).isEqualTo("jwt-token-value");
        }

        @Test
        @DisplayName("sets creation timestamp")
        void setsCreationTimestamp() {
            LocalDateTime before = LocalDateTime.now();

            RefreshToken token = RefreshToken.create(
                    "jwt-token",
                    TokenFamily.generate(),
                    UserId.generate(),
                    LocalDateTime.now().plusDays(7));

            LocalDateTime after = LocalDateTime.now();

            assertThat(token.getCreatedAt()).isBetween(before, after);
        }

        @Test
        @DisplayName("preserves token family")
        void preservesTokenFamily() {
            TokenFamily family = TokenFamily.generate();

            RefreshToken token = RefreshToken.create(
                    "jwt-token", family, UserId.generate(), LocalDateTime.now().plusDays(7));

            assertThat(token.getTokenFamily()).isEqualTo(family);
        }

        @Test
        @DisplayName("preserves user ID")
        void preservesUserId() {
            UserId userId = UserId.generate();

            RefreshToken token = RefreshToken.create(
                    "jwt-token",
                    TokenFamily.generate(),
                    userId,
                    LocalDateTime.now().plusDays(7));

            assertThat(token.getUserId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("throws when token is null")
        void throwsWhenTokenIsNull() {
            assertThatThrownBy(() -> RefreshToken.create(
                            null,
                            TokenFamily.generate(),
                            UserId.generate(),
                            LocalDateTime.now().plusDays(7)))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("token cannot be null");
        }

        @Test
        @DisplayName("throws when token family is null")
        void throwsWhenTokenFamilyIsNull() {
            assertThatThrownBy(() -> RefreshToken.create(
                            "jwt-token",
                            null,
                            UserId.generate(),
                            LocalDateTime.now().plusDays(7)))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("tokenFamily cannot be null");
        }

        @Test
        @DisplayName("throws when user ID is null")
        void throwsWhenUserIdIsNull() {
            assertThatThrownBy(() -> RefreshToken.create(
                            "jwt-token",
                            TokenFamily.generate(),
                            null,
                            LocalDateTime.now().plusDays(7)))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("userId cannot be null");
        }

        @Test
        @DisplayName("throws when expires at is null")
        void throwsWhenExpiresAtIsNull() {
            assertThatThrownBy(() -> RefreshToken.create("jwt-token", TokenFamily.generate(), UserId.generate(), null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("expiresAt cannot be null");
        }
    }

    @Nested
    @DisplayName("isExpired")
    class IsExpired {

        @Test
        @DisplayName("returns false when token is not expired")
        void returnsFalseWhenTokenIsNotExpired() {
            RefreshToken token = RefreshToken.create(
                    "jwt-token",
                    TokenFamily.generate(),
                    UserId.generate(),
                    LocalDateTime.now().plusDays(7));

            assertThat(token.isExpired()).isFalse();
        }

        @Test
        @DisplayName("returns true when token is expired")
        void returnsTrueWhenTokenIsExpired() {
            RefreshToken token = RefreshToken.create(
                    "jwt-token",
                    TokenFamily.generate(),
                    UserId.generate(),
                    LocalDateTime.now().minusSeconds(1));

            assertThat(token.isExpired()).isTrue();
        }

        @Test
        @DisplayName("returns true when expiry is exactly now")
        void returnsTrueWhenExpiryIsExactlyNow() {
            // Token that expired a moment ago
            RefreshToken token = RefreshToken.create(
                    "jwt-token",
                    TokenFamily.generate(),
                    UserId.generate(),
                    LocalDateTime.now().minusNanos(1));

            assertThat(token.isExpired()).isTrue();
        }
    }

    @Nested
    @DisplayName("reconstitute")
    class Reconstitute {

        @Test
        @DisplayName("reconstitutes token from persistence")
        void reconstitutesTokenFromPersistence() {
            UUID id = UUID.randomUUID();
            String tokenValue = "jwt-token";
            TokenFamily family = TokenFamily.generate();
            UserId userId = UserId.generate();
            LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);
            LocalDateTime createdAt = LocalDateTime.now().minusHours(1);

            RefreshToken token = RefreshToken.reconstitute(id, tokenValue, family, userId, expiresAt, createdAt);

            assertThat(token.getId()).isEqualTo(id);
            assertThat(token.getToken()).isEqualTo(tokenValue);
            assertThat(token.getTokenFamily()).isEqualTo(family);
            assertThat(token.getUserId()).isEqualTo(userId);
            assertThat(token.getExpiresAt()).isEqualTo(expiresAt);
            assertThat(token.getCreatedAt()).isEqualTo(createdAt);
        }
    }
}
