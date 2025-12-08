package org.nkcoder.auth.application.service;

import org.nkcoder.auth.application.dto.command.LoginCommand;
import org.nkcoder.auth.application.dto.command.RefreshTokenCommand;
import org.nkcoder.auth.application.dto.command.RegisterCommand;
import org.nkcoder.auth.application.dto.response.AuthResult;
import org.nkcoder.auth.domain.event.UserLoggedInEvent;
import org.nkcoder.auth.domain.event.UserRegisteredEvent;
import org.nkcoder.auth.domain.model.AuthUser;
import org.nkcoder.auth.domain.model.RefreshToken;
import org.nkcoder.auth.domain.model.TokenFamily;
import org.nkcoder.auth.domain.model.TokenPair;
import org.nkcoder.auth.domain.repository.AuthUserRepository;
import org.nkcoder.auth.domain.repository.RefreshTokenRepository;
import org.nkcoder.auth.domain.service.PasswordEncoder;
import org.nkcoder.auth.domain.service.TokenGenerator;
import org.nkcoder.shared.kernel.domain.event.DomainEventPublisher;
import org.nkcoder.shared.kernel.domain.valueobject.Email;
import org.nkcoder.shared.kernel.exception.AuthenticationException;
import org.nkcoder.shared.kernel.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for authentication use cases. Orchestrates domain objects and infrastructure
 * services.
 */
@Service
@Transactional
public class AuthApplicationService {

  private static final Logger logger = LoggerFactory.getLogger(AuthApplicationService.class);

  public static final String USER_ALREADY_EXISTS = "User already exists";
  public static final String INVALID_CREDENTIALS = "Invalid email or password";
  public static final String INVALID_REFRESH_TOKEN = "Invalid refresh token";
  public static final String REFRESH_TOKEN_EXPIRED = "Refresh token expired";
  public static final String USER_NOT_FOUND = "User not found";

  private final AuthUserRepository authUserRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final TokenGenerator tokenGenerator;
  private final DomainEventPublisher eventPublisher;

  public AuthApplicationService(
      AuthUserRepository authUserRepository,
      RefreshTokenRepository refreshTokenRepository,
      PasswordEncoder passwordEncoder,
      TokenGenerator tokenGenerator,
      DomainEventPublisher eventPublisher) {
    this.authUserRepository = authUserRepository;
    this.refreshTokenRepository = refreshTokenRepository;
    this.passwordEncoder = passwordEncoder;
    this.tokenGenerator = tokenGenerator;
    this.eventPublisher = eventPublisher;
  }

  public AuthResult register(RegisterCommand command) {
    logger.debug("Registering new user with email: {}", command.email());

    Email email = Email.of(command.email());

    // Check if user already exists
    if (authUserRepository.existsByEmail(email)) {
      throw new ValidationException(USER_ALREADY_EXISTS);
    }

    // Create auth user
    AuthUser authUser =
        AuthUser.register(
            email, passwordEncoder.encode(command.password()), command.name(), command.role());

    authUser = authUserRepository.save(authUser);
    logger.debug("Auth user registered with ID: {}", authUser.getId().value());

    // Publish domain event (replaces direct UserContextPort call for decoupled communication)
    eventPublisher.publish(
        new UserRegisteredEvent(authUser.getId(), email, command.name(), authUser.getRole()));

    // Generate tokens
    TokenFamily tokenFamily = TokenFamily.generate();
    TokenPair tokens =
        tokenGenerator.generateTokenPair(authUser.getId(), email, authUser.getRole(), tokenFamily);

    // Save refresh token
    saveRefreshToken(tokens.refreshToken(), authUser, tokenFamily);

    return AuthResult.of(
        authUser.getId().value(), authUser.getEmail().value(), authUser.getRole(), tokens);
  }

  public AuthResult login(LoginCommand command) {
    logger.debug("Logging in user with email: {}", command.email());

    Email email = Email.of(command.email());

    AuthUser authUser =
        authUserRepository
            .findByEmail(email)
            .orElseThrow(() -> new AuthenticationException(INVALID_CREDENTIALS));

    // Check password
    if (!passwordEncoder.matches(command.password(), authUser.getPassword())) {
      throw new AuthenticationException(INVALID_CREDENTIALS);
    }

    // Update last login
    authUserRepository.updateLastLoginAt(authUser.getId(), java.time.LocalDateTime.now());

    // Publish domain event
    eventPublisher.publish(new UserLoggedInEvent(authUser.getId(), authUser.getEmail()));

    // Generate tokens
    TokenFamily tokenFamily = TokenFamily.generate();
    TokenPair tokens =
        tokenGenerator.generateTokenPair(authUser.getId(), email, authUser.getRole(), tokenFamily);

    // Save refresh token
    saveRefreshToken(tokens.refreshToken(), authUser, tokenFamily);

    logger.debug("User logged in successfully: {}", authUser.getId().value());
    return AuthResult.of(
        authUser.getId().value(), authUser.getEmail().value(), authUser.getRole(), tokens);
  }

  @Transactional(isolation = Isolation.SERIALIZABLE)
  public AuthResult refreshTokens(RefreshTokenCommand command) {
    logger.debug("Refreshing tokens");

    try {
      // Validate refresh token
      TokenGenerator.RefreshTokenClaims claims =
          tokenGenerator.validateRefreshToken(command.refreshToken());

      // Get stored refresh token with lock
      RefreshToken storedToken =
          refreshTokenRepository
              .findByTokenForUpdate(command.refreshToken())
              .orElseThrow(() -> new AuthenticationException(INVALID_REFRESH_TOKEN));

      // Check if token is expired
      if (storedToken.isExpired()) {
        refreshTokenRepository.deleteByToken(command.refreshToken());
        throw new AuthenticationException(REFRESH_TOKEN_EXPIRED);
      }

      AuthUser authUser =
          authUserRepository
              .findById(claims.userId())
              .orElseThrow(() -> new AuthenticationException(USER_NOT_FOUND));

      // Delete old token
      refreshTokenRepository.deleteByToken(command.refreshToken());

      // Generate new tokens with same token family
      TokenPair tokens =
          tokenGenerator.generateTokenPair(
              authUser.getId(), authUser.getEmail(), authUser.getRole(), claims.tokenFamily());

      // Save new refresh token
      saveRefreshToken(tokens.refreshToken(), authUser, claims.tokenFamily());

      logger.debug("Tokens refreshed successfully for user: {}", authUser.getId().value());
      return AuthResult.of(
          authUser.getId().value(), authUser.getEmail().value(), authUser.getRole(), tokens);

    } catch (AuthenticationException e) {
      throw e;
    } catch (Exception e) {
      logger.error("Invalid refresh token: {}", e.getMessage());

      // If refresh token is invalid, try to delete the token family
      refreshTokenRepository
          .findByToken(command.refreshToken())
          .ifPresent(
              storedToken ->
                  refreshTokenRepository.deleteByTokenFamily(storedToken.getTokenFamily()));

      throw new AuthenticationException(INVALID_REFRESH_TOKEN);
    }
  }

  public void logout(String refreshToken) {
    logger.debug("Logging out user (all devices)");

    refreshTokenRepository
        .findByToken(refreshToken)
        .ifPresent(
            storedToken -> {
              // Delete entire token family (logout from all devices)
              refreshTokenRepository.deleteByTokenFamily(storedToken.getTokenFamily());
              logger.debug(
                  "Logged out from all devices for token family: {}",
                  storedToken.getTokenFamily().value());
            });
  }

  public void logoutSingle(String refreshToken) {
    logger.debug("Logging out user (single device)");

    // Delete only this refresh token (logout from current device)
    refreshTokenRepository.deleteByToken(refreshToken);
    logger.debug("Logged out from current device");
  }

  public void cleanupExpiredTokens() {
    logger.debug("Cleaning up expired refresh tokens");
    refreshTokenRepository.deleteExpiredTokens(java.time.LocalDateTime.now());
  }

  private void saveRefreshToken(String token, AuthUser authUser, TokenFamily tokenFamily) {
    RefreshToken refreshToken =
        RefreshToken.create(
            token, tokenFamily, authUser.getId(), tokenGenerator.getRefreshTokenExpiry());
    refreshTokenRepository.save(refreshToken);
  }
}
