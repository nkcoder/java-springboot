package org.nkcoder.auth.application.dto.command;

/** Command for refreshing access tokens. */
public record RefreshTokenCommand(String refreshToken) {}
