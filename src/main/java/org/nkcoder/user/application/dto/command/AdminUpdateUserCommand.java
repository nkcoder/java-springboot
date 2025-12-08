package org.nkcoder.user.application.dto.command;

import java.util.UUID;

/** Command for admin updating a user's information. */
public record AdminUpdateUserCommand(UUID targetUserId, String name, String email) {}
