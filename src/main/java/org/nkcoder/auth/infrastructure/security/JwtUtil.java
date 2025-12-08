package org.nkcoder.auth.infrastructure.security;

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
import org.nkcoder.auth.domain.model.AuthRole;
import org.nkcoder.infrastructure.config.JwtProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

  private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
  private static final int MINIMUM_KEY_LENGTH_BYTES = 32;

  private final JwtProperties jwtProperties;
  private final SecretKey accessTokenKey;
  private final SecretKey refreshTokenKey;

  @Autowired
  public JwtUtil(JwtProperties jwtProperties) {
    this.jwtProperties = jwtProperties;
    this.accessTokenKey = Keys.hmacShaKeyFor(jwtProperties.secret().access().getBytes());
    this.refreshTokenKey = Keys.hmacShaKeyFor(jwtProperties.secret().refresh().getBytes());
  }

  @PostConstruct
  public void validateKeyStrength() {
    validateSecretKeyStrength(jwtProperties.secret().access(), "access");
    validateSecretKeyStrength(jwtProperties.secret().refresh(), "refresh");
    logger.info("JWT secret key strength validation passed for all tokens");
  }

  private void validateSecretKeyStrength(String secret, String tokenType) {
    if (secret == null || secret.getBytes().length < MINIMUM_KEY_LENGTH_BYTES) {
      throw new IllegalStateException(
          String.format(
              "JWT %s secret key must be at least %d bytes. Current length: %d bytes",
              tokenType, MINIMUM_KEY_LENGTH_BYTES, secret == null ? 0 : secret.getBytes().length));
    }
  }

  public String generateAccessToken(UUID userId, String email, AuthRole role) {
    Date now = new Date();
    Duration duration = parseDuration(jwtProperties.expiration().access());
    Date expiration = new Date(now.getTime() + duration.toMillis());

    return Jwts.builder()
        .subject(userId.toString())
        .issuer(jwtProperties.issuer())
        .issuedAt(now)
        .expiration(expiration)
        .claim("email", email)
        .claim("role", role.name())
        .claim("jti", UUID.randomUUID().toString())
        .signWith(accessTokenKey, Jwts.SIG.HS512)
        .compact();
  }

  public String generateRefreshToken(UUID userId, String tokenFamily) {
    Date now = new Date();
    Duration duration = parseDuration(jwtProperties.expiration().refresh());
    Date expiration = new Date(now.getTime() + duration.toMillis());

    return Jwts.builder()
        .subject(userId.toString())
        .issuer(jwtProperties.issuer())
        .issuedAt(now)
        .expiration(expiration)
        .claim("tokenFamily", tokenFamily)
        .claim("jti", UUID.randomUUID().toString())
        .signWith(refreshTokenKey, Jwts.SIG.HS512)
        .compact();
  }

  public Claims validateAccessToken(String token) {
    try {
      return Jwts.parser()
          .verifyWith(accessTokenKey)
          .requireIssuer(jwtProperties.issuer())
          .build()
          .parseSignedClaims(token)
          .getPayload();
    } catch (JwtException e) {
      logger.error("Access token validation failed: {}", e.getMessage());
      throw e;
    }
  }

  public Claims validateRefreshToken(String token) {
    try {
      return Jwts.parser()
          .verifyWith(refreshTokenKey)
          .requireIssuer(jwtProperties.issuer())
          .build()
          .parseSignedClaims(token)
          .getPayload();
    } catch (JwtException e) {
      logger.error("Refresh token validation failed: {}", e.getMessage());
      throw e;
    }
  }

  public boolean isTokenExpired(String token) {
    try {
      Claims claims =
          Jwts.parser().verifyWith(accessTokenKey).build().parseSignedClaims(token).getPayload();
      return claims.getExpiration().before(new Date());
    } catch (Exception e) {
      return true;
    }
  }

  public UUID getUserIdFromToken(String token) {
    Claims claims = validateAccessToken(token);
    return UUID.fromString(claims.getSubject());
  }

  public String getEmailFromToken(String token) {
    Claims claims = validateAccessToken(token);
    return claims.get("email", String.class);
  }

  public AuthRole getRoleFromToken(String token) {
    Claims claims = validateAccessToken(token);
    String roleString = claims.get("role", String.class);
    return AuthRole.valueOf(roleString);
  }

  public LocalDateTime getTokenExpiry(String durationString) {
    Duration duration = parseDuration(durationString);
    return LocalDateTime.now().plus(duration);
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
