package org.nkcoder.user.application.service;

import java.time.LocalDateTime;
import org.nkcoder.shared.kernel.domain.event.DomainEventPublisher;
import org.nkcoder.shared.kernel.domain.event.UserRegisteredEvent;
import org.nkcoder.shared.kernel.exception.AuthenticationException;
import org.nkcoder.shared.kernel.exception.ValidationException;
import org.nkcoder.user.application.dto.command.LoginCommand;
import org.nkcoder.user.application.dto.command.RefreshTokenCommand;
import org.nkcoder.user.application.dto.command.RegisterCommand;
import org.nkcoder.user.application.dto.response.AuthResult;
import org.nkcoder.user.domain.model.Email;
import org.nkcoder.user.domain.model.RefreshToken;
import org.nkcoder.user.domain.model.TokenFamily;
import org.nkcoder.user.domain.model.TokenPair;
import org.nkcoder.user.domain.model.User;
import org.nkcoder.user.domain.model.UserName;
import org.nkcoder.user.domain.repository.RefreshTokenRepository;
import org.nkcoder.user.domain.repository.UserRepository;
import org.nkcoder.user.domain.service.AuthenticationService;
import org.nkcoder.user.domain.service.PasswordEncoder;
import org.nkcoder.user.domain.service.TokenGenerator;
import org.nkcoder.user.domain.service.TokenRotationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/** Application service for authentication use cases. Orchestrates domain objects and infrastructure services. */
@Service
public class AuthApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthApplicationService.class);

    public static final String USER_ALREADY_EXISTS = "User already exists";
    public static final String INVALID_REFRESH_TOKEN = "Invalid refresh token";
    public static final String USER_NOT_FOUND = "User not found";

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenGenerator tokenGenerator;
    private final AuthenticationService authenticationService;
    private final TokenRotationService tokenRotationService;
    private final DomainEventPublisher eventPublisher;

    public AuthApplicationService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            TokenGenerator tokenGenerator,
            AuthenticationService authenticationService,
            TokenRotationService tokenRotationService,
            DomainEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenGenerator = tokenGenerator;
        this.authenticationService = authenticationService;
        this.tokenRotationService = tokenRotationService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public AuthResult register(RegisterCommand command) {
        logger.debug("Registering new user with email: {}", command.email());

        Email email = Email.of(command.email());

        // Check if user already exists
        if (userRepository.existsByEmail(email)) {
            throw new ValidationException(USER_ALREADY_EXISTS);
        }

        // Create user
        User user = User.register(
                email, passwordEncoder.encode(command.password()), UserName.of(command.name()), command.role());

        user = userRepository.save(user);
        logger.debug("User registered with ID: {}", user.getId().value());

        eventPublisher.publish(new UserRegisteredEvent(
                user.getId().value(), user.getEmail().value(), user.getName().value()));

        // Generate tokens
        TokenFamily tokenFamily = TokenFamily.generate();
        TokenPair tokens = tokenRotationService.generateTokens(user, tokenFamily);

        // Save refresh token
        saveRefreshToken(tokens.refreshToken(), user, tokenFamily);

        return AuthResult.of(user.getId().value(), user.getEmail().value(), user.getRole(), tokens);
    }

    @Transactional()
    public AuthResult login(LoginCommand command) {
        logger.debug("Logging in user with email: {}", command.email());

        Email email = Email.of(command.email());

        // Authenticate user
        User user = authenticationService.authenticate(email, command.password());

        // Update last login
        userRepository.updateLastLoginAt(user.getId(), LocalDateTime.now());

        // Generate tokens
        TokenFamily tokenFamily = TokenFamily.generate();
        TokenPair tokens = tokenRotationService.generateTokens(user, tokenFamily);

        // Save refresh token
        saveRefreshToken(tokens.refreshToken(), user, tokenFamily);

        logger.debug("User logged in successfully: {}", user.getId().value());
        return AuthResult.of(user.getId().value(), user.getEmail().value(), user.getRole(), tokens);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public AuthResult refreshTokens(RefreshTokenCommand command) {
        logger.debug("Refreshing tokens");

        try {
            // Validate refresh token
            TokenGenerator.RefreshTokenClaims claims = tokenGenerator.validateRefreshToken(command.refreshToken());

            // Get stored refresh token with lock
            RefreshToken storedToken = refreshTokenRepository
                    .findByTokenExclusively(command.refreshToken())
                    .orElseThrow(() -> new AuthenticationException(INVALID_REFRESH_TOKEN));

            User user = userRepository
                    .findById(claims.userId())
                    .orElseThrow(() -> new AuthenticationException(USER_NOT_FOUND));

            // Rotate tokens (validates expiry)
            TokenPair tokens = tokenRotationService.rotate(storedToken, user);

            // Delete old token
            refreshTokenRepository.deleteByToken(command.refreshToken());

            // Save new refresh token
            saveRefreshToken(tokens.refreshToken(), user, claims.tokenFamily());

            logger.debug(
                    "Tokens refreshed successfully for user: {}", user.getId().value());
            return AuthResult.of(user.getId().value(), user.getEmail().value(), user.getRole(), tokens);

        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Invalid refresh token: {}", e.getMessage());

            // If refresh token is invalid, try to delete the token family
            refreshTokenRepository
                    .findByToken(command.refreshToken())
                    .ifPresent(storedToken -> refreshTokenRepository.deleteByTokenFamily(storedToken.getTokenFamily()));

            throw new AuthenticationException(INVALID_REFRESH_TOKEN);
        }
    }

    @Transactional
    public void logout(String refreshToken) {
        logger.debug("Logging out user (all devices)");

        refreshTokenRepository.findByToken(refreshToken).ifPresent(storedToken -> {
            // Delete entire token family (logout from all devices)
            refreshTokenRepository.deleteByTokenFamily(storedToken.getTokenFamily());
            logger.debug(
                    "Logged out from all devices for token family: {}",
                    storedToken.getTokenFamily().value());
        });
    }

    @Transactional
    public void logoutSingle(String refreshToken) {
        logger.debug("Logging out user (single device)");

        // Delete only this refresh token (logout from current device)
        refreshTokenRepository.deleteByToken(refreshToken);
        logger.debug("Logged out from current device");
    }

    @Transactional
    public void cleanupExpiredTokens() {
        logger.debug("Cleaning up expired refresh tokens");
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }

    private void saveRefreshToken(String token, User user, TokenFamily tokenFamily) {
        RefreshToken refreshToken =
                RefreshToken.create(token, tokenFamily, user.getId(), tokenGenerator.getRefreshTokenExpiry());
        refreshTokenRepository.save(refreshToken);
    }
}
