package org.nkcoder.user.application.dto.command;

import java.util.UUID;

/** Command for changing a user's password. */
public record ChangePasswordCommand(UUID userId, String currentPassword, String newPassword) {}
