package org.nkcoder.user.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.util.Optional;
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
import org.nkcoder.user.domain.model.User;
import org.nkcoder.user.domain.model.UserId;
import org.nkcoder.user.domain.model.UserName;
import org.nkcoder.user.domain.model.UserRole;
import org.nkcoder.user.domain.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService")
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(userRepository, passwordEncoder);
    }

    @Nested
    @DisplayName("authenticate")
    class Authenticate {

        @Test
        @DisplayName("returns user when credentials are valid")
        void returnsUserWhenCredentialsAreValid() {
            Email email = Email.of("user@example.com");
            String rawPassword = "password123";
            User user = createTestUser(email);

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(eq(rawPassword), any(HashedPassword.class)))
                    .willReturn(true);

            User result = authenticationService.authenticate(email, rawPassword);

            assertThat(result).isEqualTo(user);
        }

        @Test
        @DisplayName("throws AuthenticationException when user not found")
        void throwsWhenUserNotFound() {
            Email email = Email.of("nonexistent@example.com");

            given(userRepository.findByEmail(email)).willReturn(Optional.empty());

            assertThatThrownBy(() -> authenticationService.authenticate(email, "password"))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage(AuthenticationService.INVALID_CREDENTIALS);
        }

        @Test
        @DisplayName("throws AuthenticationException when password is incorrect")
        void throwsWhenPasswordIsIncorrect() {
            Email email = Email.of("user@example.com");
            User user = createTestUser(email);

            given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(eq("wrong-password"), any(HashedPassword.class)))
                    .willReturn(false);

            assertThatThrownBy(() -> authenticationService.authenticate(email, "wrong-password"))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage(AuthenticationService.INVALID_CREDENTIALS);
        }

        @Test
        @DisplayName("does not reveal whether user exists or password is wrong")
        void doesNotRevealWhetherUserExistsOrPasswordIsWrong() {
            Email existingEmail = Email.of("exists@example.com");
            Email nonExistingEmail = Email.of("notexists@example.com");
            User user = createTestUser(existingEmail);

            given(userRepository.findByEmail(existingEmail)).willReturn(Optional.of(user));
            given(userRepository.findByEmail(nonExistingEmail)).willReturn(Optional.empty());
            given(passwordEncoder.matches(any(), any())).willReturn(false);

            // Both should throw the same error message
            assertThatThrownBy(() -> authenticationService.authenticate(existingEmail, "wrong"))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage(AuthenticationService.INVALID_CREDENTIALS);

            assertThatThrownBy(() -> authenticationService.authenticate(nonExistingEmail, "any"))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage(AuthenticationService.INVALID_CREDENTIALS);
        }
    }

    @Nested
    @DisplayName("verifyPassword")
    class VerifyPassword {

        @Test
        @DisplayName("returns true when password matches")
        void returnsTrueWhenPasswordMatches() {
            User user = createTestUser(Email.of("user@example.com"));

            given(passwordEncoder.matches(eq("correct-password"), any(HashedPassword.class)))
                    .willReturn(true);

            boolean result = authenticationService.verifyPassword(user, "correct-password");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("returns false when password does not match")
        void returnsFalseWhenPasswordDoesNotMatch() {
            User user = createTestUser(Email.of("user@example.com"));

            given(passwordEncoder.matches(eq("wrong-password"), any(HashedPassword.class)))
                    .willReturn(false);

            boolean result = authenticationService.verifyPassword(user, "wrong-password");

            assertThat(result).isFalse();
        }
    }

    private User createTestUser(Email email) {
        return User.reconstitute(
                UserId.generate(),
                email,
                HashedPassword.of("hashed-password"),
                UserName.of("Test User"),
                UserRole.MEMBER,
                false,
                null,
                LocalDateTime.now(),
                LocalDateTime.now());
    }
}
