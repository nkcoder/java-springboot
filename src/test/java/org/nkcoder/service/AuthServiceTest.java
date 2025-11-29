package org.nkcoder.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.nkcoder.config.JwtProperties;
import org.nkcoder.dto.auth.AuthResponse;
import org.nkcoder.dto.auth.LoginRequest;
import org.nkcoder.dto.auth.RegisterRequest;
import org.nkcoder.dto.user.UserResponse;
import org.nkcoder.entity.RefreshToken;
import org.nkcoder.entity.User;
import org.nkcoder.entity.UserTestFactory;
import org.nkcoder.enums.Role;
import org.nkcoder.exception.AuthenticationException;
import org.nkcoder.exception.ValidationException;
import org.nkcoder.mapper.UserMapper;
import org.nkcoder.repository.RefreshTokenRepository;
import org.nkcoder.repository.UserRepository;
import org.nkcoder.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthService authService;

    @Captor
    private ArgumentCaptor<RefreshToken> refreshTokenCaptor;

    private final UUID userId = UUID.randomUUID();
    private final String email = "test@example.com";
    private final String password = "password";
    private final String encodedPassword = "encodedPassword";
    private final String name = "Test User";
    private final Role role = Role.MEMBER;
    private final String accessToken = "access.token";
    private final String refreshToken = "refresh.token";
    private final String tokenFamily = "token-family";
    private final LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        JwtProperties.Expiration expiration = new JwtProperties.Expiration("7m", "30m");
        when(jwtProperties.expiration()).thenReturn(expiration);
        when(jwtUtil.getTokenExpiry(anyString())).thenReturn(expiresAt);
    }

    @Test
    void register_success() {
        RegisterRequest request = new RegisterRequest(email, password, name, role);
        User user = UserTestFactory.createWithId(userId, email, encodedPassword, name, role, false);
        UserResponse userResponse = new UserResponse(userId, email, name, role, false, now, now, now);

        when(userRepository.existsByEmail(email.toLowerCase())).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtil.generateAccessToken(userId, email, role)).thenReturn(accessToken);
        when(jwtUtil.generateRefreshToken(eq(userId), anyString())).thenReturn(refreshToken);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        AuthResponse response = authService.register(request);

        assertEquals(userResponse, response.user());
        assertEquals(accessToken, response.tokens().accessToken());
        assertEquals(refreshToken, response.tokens().refreshToken());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        verify(userRepository)
                .save(argThat(u -> u.getEmail().equals(email.toLowerCase())
                        && u.getName().equals(name)
                        && u.getRole().equals(role)));
    }

    @Test
    void register_userAlreadyExists_throws() {
        RegisterRequest request = new RegisterRequest(email, password, name, role);
        when(userRepository.existsByEmail(email.toLowerCase())).thenReturn(true);

        ValidationException exception = assertThrows(ValidationException.class, () -> authService.register(request));

        assertEquals("User already exists", exception.getMessage());
        verify(userRepository, never()).save(any());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void login_success() {
        LoginRequest request = new LoginRequest(email, password);
        User user = UserTestFactory.createWithId(userId, email, encodedPassword, name, role, false);
        UserResponse userResponse = new UserResponse(userId, email, name, role, false, now, now, now);

        when(userRepository.findByEmail(email.toLowerCase())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(jwtUtil.generateAccessToken(userId, email, role)).thenReturn(accessToken);
        when(jwtUtil.generateRefreshToken(eq(userId), anyString())).thenReturn(refreshToken);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        AuthResponse response = authService.login(request);

        assertEquals(userResponse, response.user());
        assertEquals(accessToken, response.tokens().accessToken());
        assertEquals(refreshToken, response.tokens().refreshToken());
        verify(userService).updateLastLogin(userId);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void login_invalidEmail_throws() {
        LoginRequest request = new LoginRequest(email, password);
        when(userRepository.findByEmail(email.toLowerCase())).thenReturn(Optional.empty());

        AuthenticationException exception =
                assertThrows(AuthenticationException.class, () -> authService.login(request));

        assertEquals("Invalid email or password", exception.getMessage());
        verify(userService, never()).updateLastLogin(any());
    }

    @Test
    void login_invalidPassword_throws() {
        LoginRequest request = new LoginRequest(email, password);
        User user = UserTestFactory.createWithId(userId, email, encodedPassword, name, role, false);

        when(userRepository.findByEmail(email.toLowerCase())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

        AuthenticationException exception =
                assertThrows(AuthenticationException.class, () -> authService.login(request));

        assertEquals("Invalid email or password", exception.getMessage());
        verify(userService, never()).updateLastLogin(any());
    }

    @Test
    void refreshTokens_success() {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn(userId.toString());
        when(claims.get("tokenFamily", String.class)).thenReturn(tokenFamily);

        when(jwtUtil.validateRefreshToken(refreshToken)).thenReturn(claims);

        RefreshToken storedToken = mock(RefreshToken.class);
        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.of(storedToken));
        when(storedToken.isExpired()).thenReturn(false);
        when(storedToken.getTokenFamily()).thenReturn(tokenFamily);

        User user = UserTestFactory.createWithId(userId, email, encodedPassword, name, role, false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(jwtUtil.generateAccessToken(userId, email, role)).thenReturn(accessToken);
        when(jwtUtil.generateRefreshToken(userId, tokenFamily)).thenReturn("new.refresh.token");
        UserResponse userResponse = new UserResponse(userId, email, name, role, false, now, now, now);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        AuthResponse response = authService.refreshTokens(refreshToken);

        assertEquals(userResponse, response.user());
        assertEquals(accessToken, response.tokens().accessToken());
        assertEquals("new.refresh.token", response.tokens().refreshToken());
        verify(refreshTokenRepository).deleteByToken(refreshToken);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void refreshTokens_expiredToken_throws() {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn(userId.toString());
        when(claims.get("tokenFamily", String.class)).thenReturn(tokenFamily);

        when(jwtUtil.validateRefreshToken(refreshToken)).thenReturn(claims);

        RefreshToken storedToken = mock(RefreshToken.class);
        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.of(storedToken));
        when(storedToken.isExpired()).thenReturn(true);

        AuthenticationException exception =
                assertThrows(AuthenticationException.class, () -> authService.refreshTokens(refreshToken));

        assertEquals("Refresh token expired", exception.getMessage());
        verify(refreshTokenRepository).deleteByToken(refreshToken);
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void refreshTokens_invalidToken_throwsAndDeletesFamily() {
        when(jwtUtil.validateRefreshToken(refreshToken)).thenThrow(new JwtException("Invalid token"));
        RefreshToken storedToken = new RefreshToken(refreshToken, tokenFamily, userId, expiresAt);
        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.of(storedToken));

        AuthenticationException exception =
                assertThrows(AuthenticationException.class, () -> authService.refreshTokens(refreshToken));

        assertEquals("Invalid refresh token", exception.getMessage());
        verify(refreshTokenRepository).deleteByTokenFamily(tokenFamily);
    }

    @Test
    void refreshTokens_invalidToken_noStoredToken_throws() {
        when(jwtUtil.validateRefreshToken(refreshToken)).thenThrow(new JwtException("Invalid token"));
        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.empty());

        AuthenticationException exception =
                assertThrows(AuthenticationException.class, () -> authService.refreshTokens(refreshToken));

        assertEquals("Invalid refresh token", exception.getMessage());
        verify(refreshTokenRepository, never()).deleteByTokenFamily(any());
    }

    @Test
    void refreshTokens_tokenNotFound_throws() {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn(userId.toString());
        when(claims.get("tokenFamily", String.class)).thenReturn(tokenFamily);

        when(jwtUtil.validateRefreshToken(refreshToken)).thenReturn(claims);
        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.empty());

        AuthenticationException exception =
                assertThrows(AuthenticationException.class, () -> authService.refreshTokens(refreshToken));

        assertEquals("Invalid refresh token", exception.getMessage());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void refreshTokens_userNotFound_throws() {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn(userId.toString());
        when(claims.get("tokenFamily", String.class)).thenReturn(tokenFamily);

        when(jwtUtil.validateRefreshToken(refreshToken)).thenReturn(claims);

        RefreshToken storedToken = mock(RefreshToken.class);
        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.of(storedToken));
        when(storedToken.isExpired()).thenReturn(false);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        AuthenticationException exception =
                assertThrows(AuthenticationException.class, () -> authService.refreshTokens(refreshToken));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void logout_deletesTokenFamily() {
        RefreshToken storedToken = new RefreshToken(refreshToken, tokenFamily, userId, expiresAt);
        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.of(storedToken));

        authService.logout(refreshToken);

        verify(refreshTokenRepository).deleteByTokenFamily(tokenFamily);
    }

    @Test
    void logout_noToken_noop() {
        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.empty());

        authService.logout(refreshToken);

        verify(refreshTokenRepository, never()).deleteByTokenFamily(any());
    }

    @Test
    void logoutSingle_deletesSingleToken() {
        authService.logoutSingle(refreshToken);
        verify(refreshTokenRepository).deleteByToken(refreshToken);
    }

    @Test
    void cleanupExpiredTokens_deletesExpired() {
        authService.cleanupExpiredTokens();
        verify(refreshTokenRepository).deleteExpiredTokens(any(LocalDateTime.class));
    }

    @Test
    void login_caseInsensitiveEmail() {
        String upperCaseEmail = "TEST@EXAMPLE.COM";
        LoginRequest request = new LoginRequest(upperCaseEmail, password);
        User user = UserTestFactory.createWithId(userId, email.toLowerCase(), encodedPassword, name, role, false);
        UserResponse userResponse = new UserResponse(userId, email.toLowerCase(), name, role, false, now, now, now);

        when(userRepository.findByEmail(email.toLowerCase())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(jwtUtil.generateAccessToken(userId, email.toLowerCase(), role)).thenReturn(accessToken);
        when(jwtUtil.generateRefreshToken(eq(userId), anyString())).thenReturn(refreshToken);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        verify(userRepository).findByEmail(email.toLowerCase());
    }

    @Test
    void register_caseInsensitiveEmail() {
        String upperCaseEmail = "TEST@EXAMPLE.COM";
        RegisterRequest request = new RegisterRequest(upperCaseEmail, password, name, role);
        User user = UserTestFactory.createWithId(userId, email.toLowerCase(), encodedPassword, name, role, false);
        UserResponse userResponse = new UserResponse(userId, email.toLowerCase(), name, role, false, now, now, now);

        when(userRepository.existsByEmail(email.toLowerCase())).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtil.generateAccessToken(userId, email.toLowerCase(), role)).thenReturn(accessToken);
        when(jwtUtil.generateRefreshToken(eq(userId), anyString())).thenReturn(refreshToken);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        verify(userRepository).existsByEmail(email.toLowerCase());
        verify(userRepository).save(argThat(u -> u.getEmail().equals(email.toLowerCase())));
    }
}
