package org.nkcoder.user.application.dto.command;

import java.util.UUID;

/** Command for updating a user's profile. */
public record UpdateProfileCommand(UUID userId, String name) {}
