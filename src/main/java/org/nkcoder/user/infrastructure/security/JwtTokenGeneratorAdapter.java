package org.nkcoder.user.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.nkcoder.infrastructure.config.JwtProperties;
import org.nkcoder.shared.kernel.exception.AuthenticationException;
import org.nkcoder.user.domain.model.Email;
import org.nkcoder.user.domain.model.TokenFamily;
import org.nkcoder.user.domain.model.TokenPair;
import org.nkcoder.user.domain.model.UserId;
import org.nkcoder.user.domain.model.UserRole;
import org.nkcoder.user.domain.service.TokenGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** JWT implementation of the TokenGenerator domain service. */
@Component
public class JwtTokenGeneratorAdapter implements TokenGenerator {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenGeneratorAdapter.class);
    private static final int MINIMUM_KEY_LENGTH_BYTES = 32;

    private final JwtProperties jwtProperties;
    private final SecretKey accessTokenKey;
    private final SecretKey refreshTokenKey;

    public JwtTokenGeneratorAdapter(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.accessTokenKey = Keys.hmacShaKeyFor(jwtProperties.secret().access().getBytes());
        this.refreshTokenKey =
                Keys.hmacShaKeyFor(jwtProperties.secret().refresh().getBytes());
    }

    @PostConstruct
    public void validateKeyStrength() {
        validateSecretKeyStrength(jwtProperties.secret().access(), "access");
        validateSecretKeyStrength(jwtProperties.secret().refresh(), "refresh");
        logger.info("JWT secret key strength validation passed for all tokens");
    }

    private void validateSecretKeyStrength(String secret, String tokenType) {
        if (secret == null || secret.getBytes().length < MINIMUM_KEY_LENGTH_BYTES) {
            throw new IllegalStateException(String.format(
                    "JWT %s secret key must be at least %d bytes. Current length: %d bytes",
                    tokenType, MINIMUM_KEY_LENGTH_BYTES, secret == null ? 0 : secret.getBytes().length));
        }
    }

    @Override
    public TokenPair generateTokenPair(UserId userId, Email email, UserRole role, TokenFamily tokenFamily) {
        String accessToken = generateAccessToken(userId, email, role);
        String refreshToken = generateRefreshToken(userId, tokenFamily);
        return new TokenPair(accessToken, refreshToken);
    }

    @Override
    public LocalDateTime getRefreshTokenExpiry() {
        Duration duration = parseDuration(jwtProperties.expiration().refresh());
        return LocalDateTime.now().plus(duration);
    }

    @Override
    public RefreshTokenClaims validateRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(refreshTokenKey)
                    .requireIssuer(jwtProperties.issuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            UserId userId = UserId.of(claims.getSubject());
            TokenFamily tokenFamily = TokenFamily.of(claims.get("tokenFamily", String.class));

            return new RefreshTokenClaims(userId, tokenFamily);
        } catch (JwtException e) {
            logger.error("Refresh token validation failed: {}", e.getMessage());
            throw new AuthenticationException("Invalid refresh token");
        }
    }

    private String generateAccessToken(UserId userId, Email email, UserRole role) {
        Date now = new Date();
        Duration duration = parseDuration(jwtProperties.expiration().access());
        Date expiration = new Date(now.getTime() + duration.toMillis());

        return Jwts.builder()
                .subject(userId.value().toString())
                .issuer(jwtProperties.issuer())
                .issuedAt(now)
                .expiration(expiration)
                .claim("email", email.value())
                .claim("role", role.name())
                .claim("jti", UUID.randomUUID().toString())
                .signWith(accessTokenKey, Jwts.SIG.HS512)
                .compact();
    }

    private String generateRefreshToken(UserId userId, TokenFamily tokenFamily) {
        Date now = new Date();
        Duration duration = parseDuration(jwtProperties.expiration().refresh());
        Date expiration = new Date(now.getTime() + duration.toMillis());

        return Jwts.builder()
                .subject(userId.value().toString())
                .issuer(jwtProperties.issuer())
                .issuedAt(now)
                .expiration(expiration)
                .claim("tokenFamily", tokenFamily.value())
                .claim("jti", UUID.randomUUID().toString())
                .signWith(refreshTokenKey, Jwts.SIG.HS512)
                .compact();
    }

    private Duration parseDuration(String durationString) {
        if (durationString == null || durationString.isEmpty()) {
            throw new IllegalArgumentException("Duration string cannot be null or empty");
        }

        String value = durationString.substring(0, durationString.length() - 1);
        String unit = durationString.substring(durationString.length() - 1);

        long amount = Long.parseLong(value);

        return switch (unit) {
            case "s" -> Duration.ofSeconds(amount);
            case "m" -> Duration.ofMinutes(amount);
            case "h" -> Duration.ofHours(amount);
            case "d" -> Duration.ofDays(amount);
            default -> throw new IllegalArgumentException("Invalid duration unit: " + unit);
        };
    }
}
