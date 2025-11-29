package org.nkcoder.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.time.LocalDateTime;
import java.util.UUID;
import org.nkcoder.config.JwtProperties;
import org.nkcoder.dto.auth.AuthResponse;
import org.nkcoder.dto.auth.AuthTokens;
import org.nkcoder.dto.auth.LoginRequest;
import org.nkcoder.dto.auth.RegisterRequest;
import org.nkcoder.entity.RefreshToken;
import org.nkcoder.entity.User;
import org.nkcoder.exception.AuthenticationException;
import org.nkcoder.exception.ValidationException;
import org.nkcoder.mapper.UserMapper;
import org.nkcoder.repository.RefreshTokenRepository;
import org.nkcoder.repository.UserRepository;
import org.nkcoder.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

  private final UserRepository userRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;
  private final JwtProperties jwtProperties;
  private final UserMapper userMapper;

  @Autowired
  public AuthService(
      UserRepository userRepository,
      RefreshTokenRepository refreshTokenRepository,
      PasswordEncoder passwordEncoder,
      JwtUtil jwtUtil,
      JwtProperties jwtProperties,
      UserMapper userMapper) {
    this.userRepository = userRepository;
    this.refreshTokenRepository = refreshTokenRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtil = jwtUtil;
    this.jwtProperties = jwtProperties;
    this.userMapper = userMapper;
  }

  @Transactional
  public AuthResponse register(RegisterRequest request) {
    logger.debug("Registering new user with email: {}", request.email());

    // Check if user already exists
    if (userRepository.existsByEmail(request.email().toLowerCase())) {
      throw new ValidationException("User already exists");
    }

    // Create new user
    User user =
        new User(
            request.email().toLowerCase(),
            passwordEncoder.encode(request.password()),
            request.name(),
            request.role(),
            false);

    User savedUser = userRepository.save(user);
    logger.debug("User registered successfully with ID: {}", savedUser.getId());

    // Generate tokens
    String tokenFamily = UUID.randomUUID().toString();
    AuthTokens tokens = generateAuthTokens(savedUser, tokenFamily);

    // Save refresh token
    saveRefreshToken(tokens.refreshToken(), savedUser.getId(), tokenFamily);

    return new AuthResponse(userMapper.toResponse(savedUser), tokens);
  }

  @Transactional
  public AuthResponse login(LoginRequest request) {
    logger.debug("Logging in user with email: {}", request.email());

    // Find user by email
    User user =
        userRepository
            .findByEmail(request.email().toLowerCase())
            .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

    // Check password
    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new AuthenticationException("Invalid email or password");
    }

    // Update last login
    userRepository.updateLastLoginAt(user.getId(), LocalDateTime.now());

    // Generate tokens
    String tokenFamily = UUID.randomUUID().toString();
    AuthTokens tokens = generateAuthTokens(user, tokenFamily);

    // Save refresh token
    saveRefreshToken(tokens.refreshToken(), user.getId(), tokenFamily);

    logger.debug("User logged in successfully: {}", user.getId());
    return new AuthResponse(userMapper.toResponse(user), tokens);
  }

  @Transactional(isolation = Isolation.SERIALIZABLE)
  public AuthResponse refreshTokens(String refreshToken) {
    logger.debug("Refreshing tokens");

    try {
      // Validate refresh token
      Claims claims = jwtUtil.validateRefreshToken(refreshToken);
      UUID userId = UUID.fromString(claims.getSubject());
      String tokenFamily = claims.get("tokenFamily", String.class);

      // Get stored refresh token
      RefreshToken storedToken =
          refreshTokenRepository
              .findByTokenForUpdate(refreshToken)
              .orElseThrow(() -> new AuthenticationException("Invalid refresh token"));

      // Check if token is expired
      if (storedToken.isExpired()) {
        refreshTokenRepository.deleteByToken(refreshToken);
        throw new AuthenticationException("Refresh token expired");
      }

      // Get user
      User user =
          userRepository
              .findById(userId)
              .orElseThrow(() -> new AuthenticationException("User not found"));

      // Delete old refresh token
      refreshTokenRepository.deleteByToken(refreshToken);

      // Generate new tokens with same token family
      AuthTokens tokens = generateAuthTokens(user, tokenFamily);

      // Save new refresh token
      saveRefreshToken(tokens.refreshToken(), user.getId(), tokenFamily);

      logger.debug("Tokens refreshed successfully for user: {}", userId);
      return new AuthResponse(userMapper.toResponse(user), tokens);

    } catch (JwtException e) {
      logger.error("Invalid refresh token: {}", e.getMessage());

      // If refresh token is invalid, try to delete the token family
      refreshTokenRepository
          .findByToken(refreshToken)
          .ifPresent(
              storedToken ->
                  refreshTokenRepository.deleteByTokenFamily(storedToken.getTokenFamily()));

      throw new AuthenticationException("Invalid refresh token");
    }
  }

  @Transactional
  public void logout(String refreshToken) {
    logger.debug("Logging out user (all devices)");

    // Get token data
    RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken).orElse(null);
    if (storedToken != null) {
      // Delete entire token family (logout from all devices)
      refreshTokenRepository.deleteByTokenFamily(storedToken.getTokenFamily());
      logger.debug(
          "Logged out from all devices for token family: {}", storedToken.getTokenFamily());
    }
  }

  @Transactional
  public void logoutSingle(String refreshToken) {
    logger.debug("Logging out user (single device)");

    // Delete only this refresh token (logout from current device)
    refreshTokenRepository.deleteByToken(refreshToken);
    logger.debug("Logged out from current device");
  }

  private AuthTokens generateAuthTokens(User user, String tokenFamily) {
    String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
    String refreshToken = jwtUtil.generateRefreshToken(user.getId(), tokenFamily);
    return new AuthTokens(accessToken, refreshToken);
  }

  private void saveRefreshToken(String token, UUID userId, String tokenFamily) {
    LocalDateTime expiresAt = jwtUtil.getTokenExpiry(jwtProperties.expiration().refresh());
    RefreshToken refreshToken = new RefreshToken(token, tokenFamily, userId, expiresAt);
    refreshTokenRepository.save(refreshToken);
  }

  @Transactional
  public void cleanupExpiredTokens() {
    logger.debug("Cleaning up expired refresh tokens");
    refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
  }
}
