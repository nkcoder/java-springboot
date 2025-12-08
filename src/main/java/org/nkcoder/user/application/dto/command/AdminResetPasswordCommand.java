package org.nkcoder.user.application.dto.command;

import java.util.UUID;

/** Command for admin resetting a user's password. */
public record AdminResetPasswordCommand(UUID targetUserId, String newPassword) {}
