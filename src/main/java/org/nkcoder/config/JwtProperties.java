package org.nkcoder.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "jwt")
@Validated
public record JwtProperties(
    @Valid Secret secret, @Valid Expiration expiration, @NotBlank String issuer) {

  public JwtProperties {
    // Compact constructor with default values
    if (issuer == null || issuer.isBlank()) {
      issuer = "user-service";
    }
  }

  public record Secret(
      @NotBlank @Size(min = 32, message = "Access token secret must be at least 32 characters") String access,
      @NotBlank @Size(min = 32, message = "Refresh token secret must be at least 32 characters") String refresh) {}

  public record Expiration(
      @NotBlank @Pattern(
              regexp = "\\d+[smhd]",
              message = "Access token expiration must match pattern: <number>[s|m|h|d]")
          String access,
      @NotBlank @Pattern(
              regexp = "\\d+[smhd]",
              message = "Refresh token expiration must match pattern: <number>[s|m|h|d]")
          String refresh) {

    public Expiration {
      // Compact constructor with default values
      if (access == null || access.isBlank()) {
        access = "15m";
      }
      if (refresh == null || refresh.isBlank()) {
        refresh = "7d";
      }
    }
  }
}
