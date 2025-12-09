package org.nkcoder.user.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nkcoder.shared.kernel.exception.AuthenticationException;
import org.nkcoder.shared.kernel.exception.ValidationException;
import org.nkcoder.user.application.dto.command.LoginCommand;
import org.nkcoder.user.application.dto.command.RefreshTokenCommand;
import org.nkcoder.user.application.dto.command.RegisterCommand;
import org.nkcoder.user.application.dto.response.AuthResult;
import org.nkcoder.user.domain.model.Email;
import org.nkcoder.user.domain.model.HashedPassword;
import org.nkcoder.user.domain.model.RefreshToken;
import org.nkcoder.user.domain.model.TokenFamily;
import org.nkcoder.user.domain.model.TokenPair;
import org.nkcoder.user.domain.model.User;
import org.nkcoder.user.domain.model.UserId;
import org.nkcoder.user.domain.model.UserName;
import org.nkcoder.user.domain.model.UserRole;
import org.nkcoder.user.domain.repository.RefreshTokenRepository;
import org.nkcoder.user.domain.repository.UserRepository;
import org.nkcoder.user.domain.service.AuthenticationService;
import org.nkcoder.user.domain.service.PasswordEncoder;
import org.nkcoder.user.domain.service.TokenGenerator;
import org.nkcoder.user.domain.service.TokenRotationService;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthApplicationService")
class AuthApplicationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenGenerator tokenGenerator;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private TokenRotationService tokenRotationService;

    private AuthApplicationService authApplicationService;

    @BeforeEach
    void setUp() {
        authApplicationService = new AuthApplicationService(
                userRepository,
                refreshTokenRepository,
                passwordEncoder,
                tokenGenerator,
                authenticationService,
                tokenRotationService);
    }

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("registers new user successfully")
        void registersNewUserSuccessfully() {
            RegisterCommand command =
                    new RegisterCommand("new@example.com", "Password123", "New User", UserRole.MEMBER);
            TokenPair tokenPair = new TokenPair("access-token", "refresh-token");

            given(userRepository.existsByEmail(any(Email.class))).willReturn(false);
            given(passwordEncoder.encode(any())).willReturn(HashedPassword.of("hashed"));
            given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));
            given(tokenRotationService.generateTokens(any(User.class), any(TokenFamily.class)))
                    .willReturn(tokenPair);
            given(tokenGenerator.getRefreshTokenExpiry())
                    .willReturn(LocalDateTime.now().plusDays(7));

            AuthResult result = authApplicationService.register(command);

            assertThat(result.email()).isEqualTo("new@example.com");
            assertThat(result.accessToken()).isEqualTo("access-token");
            assertThat(result.refreshToken()).isEqualTo("refresh-token");
            verify(userRepository).save(any(User.class));
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("throws ValidationException when email already exists")
        void throwsWhenEmailAlreadyExists() {
            RegisterCommand command =
                    new RegisterCommand("existing@example.com", "Password123", "User", UserRole.MEMBER);

            given(userRepository.existsByEmail(any(Email.class))).willReturn(true);

            assertThatThrownBy(() -> authApplicationService.register(command))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage(AuthApplicationService.USER_ALREADY_EXISTS);

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("saves refresh token after registration")
        void savesRefreshTokenAfterRegistration() {
            RegisterCommand command =
                    new RegisterCommand("new@example.com", "Password123", "New User", UserRole.MEMBER);
            TokenPair tokenPair = new TokenPair("access", "refresh-token-value");

            given(userRepository.existsByEmail(any(Email.class))).willReturn(false);
            given(passwordEncoder.encode(any())).willReturn(HashedPassword.of("hashed"));
            given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));
            given(tokenRotationService.generateTokens(any(), any())).willReturn(tokenPair);
            given(tokenGenerator.getRefreshTokenExpiry())
                    .willReturn(LocalDateTime.now().plusDays(7));

            authApplicationService.register(command);

            ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
            verify(refreshTokenRepository).save(tokenCaptor.capture());
            assertThat(tokenCaptor.getValue().getToken()).isEqualTo("refresh-token-value");
        }
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("logs in user successfully")
        void logsInUserSuccessfully() {
            LoginCommand command = new LoginCommand("user@example.com", "Password123");
            User user = createTestUser();
            TokenPair tokenPair = new TokenPair("access-token", "refresh-token");

            given(authenticationService.authenticate(any(Email.class), eq("Password123")))
                    .willReturn(user);
            given(tokenRotationService.generateTokens(any(User.class), any(TokenFamily.class)))
                    .willReturn(tokenPair);
            given(tokenGenerator.getRefreshTokenExpiry())
                    .willReturn(LocalDateTime.now().plusDays(7));

            AuthResult result = authApplicationService.login(command);

            assertThat(result.email()).isEqualTo(user.getEmail().value());
            assertThat(result.accessToken()).isEqualTo("access-token");
            verify(userRepository).updateLastLoginAt(any(UserId.class), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("throws AuthenticationException when credentials are invalid")
        void throwsWhenCredentialsAreInvalid() {
            LoginCommand command = new LoginCommand("user@example.com", "wrong-password");

            given(authenticationService.authenticate(any(Email.class), any()))
                    .willThrow(new AuthenticationException("Invalid email or password"));

            assertThatThrownBy(() -> authApplicationService.login(command)).isInstanceOf(AuthenticationException.class);
        }
    }

    @Nested
    @DisplayName("refreshTokens")
    class RefreshTokens {

        @Test
        @DisplayName("refreshes tokens successfully")
        void refreshesTokensSuccessfully() {
            RefreshTokenCommand command = new RefreshTokenCommand("valid-refresh-token");
            User user = createTestUser();
            TokenFamily family = TokenFamily.generate();
            RefreshToken storedToken = RefreshToken.create(
                    "valid-refresh-token",
                    family,
                    user.getId(),
                    LocalDateTime.now().plusDays(7));
            TokenPair newTokenPair = new TokenPair("new-access", "new-refresh");
            TokenGenerator.RefreshTokenClaims claims = new TokenGenerator.RefreshTokenClaims(user.getId(), family);

            given(tokenGenerator.validateRefreshToken("valid-refresh-token")).willReturn(claims);
            given(refreshTokenRepository.findByTokenExclusively("valid-refresh-token"))
                    .willReturn(Optional.of(storedToken));
            given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
            given(tokenRotationService.rotate(any(RefreshToken.class), any(User.class)))
                    .willReturn(newTokenPair);
            given(tokenGenerator.getRefreshTokenExpiry())
                    .willReturn(LocalDateTime.now().plusDays(7));

            AuthResult result = authApplicationService.refreshTokens(command);

            assertThat(result.accessToken()).isEqualTo("new-access");
            assertThat(result.refreshToken()).isEqualTo("new-refresh");
            verify(refreshTokenRepository).deleteByToken("valid-refresh-token");
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("throws AuthenticationException when refresh token not found")
        void throwsWhenRefreshTokenNotFound() {
            RefreshTokenCommand command = new RefreshTokenCommand("unknown-token");
            TokenFamily family = TokenFamily.generate();
            TokenGenerator.RefreshTokenClaims claims = new TokenGenerator.RefreshTokenClaims(UserId.generate(), family);

            given(tokenGenerator.validateRefreshToken("unknown-token")).willReturn(claims);
            given(refreshTokenRepository.findByTokenExclusively("unknown-token"))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> authApplicationService.refreshTokens(command))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage(AuthApplicationService.INVALID_REFRESH_TOKEN);
        }

        @Test
        @DisplayName("throws AuthenticationException when user not found")
        void throwsWhenUserNotFound() {
            RefreshTokenCommand command = new RefreshTokenCommand("valid-token");
            UserId userId = UserId.generate();
            TokenFamily family = TokenFamily.generate();
            RefreshToken storedToken = RefreshToken.create(
                    "valid-token", family, userId, LocalDateTime.now().plusDays(7));
            TokenGenerator.RefreshTokenClaims claims = new TokenGenerator.RefreshTokenClaims(userId, family);

            given(tokenGenerator.validateRefreshToken("valid-token")).willReturn(claims);
            given(refreshTokenRepository.findByTokenExclusively("valid-token")).willReturn(Optional.of(storedToken));
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> authApplicationService.refreshTokens(command))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage(AuthApplicationService.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("deletes token family when unexpected error occurs and token exists")
        void deletesTokenFamilyWhenUnexpectedErrorOccurs() {
            RefreshTokenCommand command = new RefreshTokenCommand("problematic-token");
            TokenFamily family = TokenFamily.generate();
            RefreshToken storedToken = RefreshToken.create(
                    "problematic-token",
                    family,
                    UserId.generate(),
                    LocalDateTime.now().plusDays(7));
            TokenGenerator.RefreshTokenClaims claims = new TokenGenerator.RefreshTokenClaims(UserId.generate(), family);

            given(tokenGenerator.validateRefreshToken("problematic-token")).willReturn(claims);
            given(refreshTokenRepository.findByTokenExclusively("problematic-token"))
                    .willReturn(Optional.of(storedToken));
            given(userRepository.findById(any(UserId.class))).willReturn(Optional.of(createTestUser()));
            given(tokenRotationService.rotate(any(RefreshToken.class), any(User.class)))
                    .willThrow(new RuntimeException("Unexpected error"));
            given(refreshTokenRepository.findByToken("problematic-token")).willReturn(Optional.of(storedToken));

            assertThatThrownBy(() -> authApplicationService.refreshTokens(command))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage(AuthApplicationService.INVALID_REFRESH_TOKEN);

            verify(refreshTokenRepository).deleteByTokenFamily(family);
        }

        @Test
        @DisplayName("handles unexpected error when token not found in cleanup")
        void handlesUnexpectedErrorWhenTokenNotFoundInCleanup() {
            RefreshTokenCommand command = new RefreshTokenCommand("problematic-token");
            TokenFamily family = TokenFamily.generate();
            RefreshToken storedToken = RefreshToken.create(
                    "problematic-token",
                    family,
                    UserId.generate(),
                    LocalDateTime.now().plusDays(7));
            TokenGenerator.RefreshTokenClaims claims = new TokenGenerator.RefreshTokenClaims(UserId.generate(), family);

            given(tokenGenerator.validateRefreshToken("problematic-token")).willReturn(claims);
            given(refreshTokenRepository.findByTokenExclusively("problematic-token"))
                    .willReturn(Optional.of(storedToken));
            given(userRepository.findById(any(UserId.class))).willReturn(Optional.of(createTestUser()));
            given(tokenRotationService.rotate(any(RefreshToken.class), any(User.class)))
                    .willThrow(new RuntimeException("Unexpected error"));
            given(refreshTokenRepository.findByToken("problematic-token")).willReturn(Optional.empty());

            assertThatThrownBy(() -> authApplicationService.refreshTokens(command))
                    .isInstanceOf(AuthenticationException.class)
                    .hasMessage(AuthApplicationService.INVALID_REFRESH_TOKEN);

            verify(refreshTokenRepository, never()).deleteByTokenFamily(any());
        }
    }

    @Nested
    @DisplayName("logout")
    class Logout {

        @Test
        @DisplayName("deletes entire token family")
        void deletesEntireTokenFamily() {
            String refreshToken = "refresh-token";
            TokenFamily family = TokenFamily.generate();
            RefreshToken storedToken = RefreshToken.create(
                    refreshToken, family, UserId.generate(), LocalDateTime.now().plusDays(7));

            given(refreshTokenRepository.findByToken(refreshToken)).willReturn(Optional.of(storedToken));

            authApplicationService.logout(refreshToken);

            verify(refreshTokenRepository).deleteByTokenFamily(family);
        }

        @Test
        @DisplayName("does nothing when token not found")
        void doesNothingWhenTokenNotFound() {
            given(refreshTokenRepository.findByToken("unknown")).willReturn(Optional.empty());

            authApplicationService.logout("unknown");

            verify(refreshTokenRepository, never()).deleteByTokenFamily(any());
        }
    }

    @Nested
    @DisplayName("logoutSingle")
    class LogoutSingle {

        @Test
        @DisplayName("deletes only the specified token")
        void deletesOnlyTheSpecifiedToken() {
            String refreshToken = "refresh-token";

            authApplicationService.logoutSingle(refreshToken);

            verify(refreshTokenRepository).deleteByToken(refreshToken);
            verify(refreshTokenRepository, never()).deleteByTokenFamily(any());
        }
    }

    @Nested
    @DisplayName("cleanupExpiredTokens")
    class CleanupExpiredTokens {

        @Test
        @DisplayName("deletes expired tokens")
        void deletesExpiredTokens() {
            authApplicationService.cleanupExpiredTokens();

            verify(refreshTokenRepository).deleteExpiredTokens(any(LocalDateTime.class));
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
}
