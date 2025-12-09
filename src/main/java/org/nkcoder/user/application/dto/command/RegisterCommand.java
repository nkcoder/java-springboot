package org.nkcoder.user.application.dto.command;

import org.nkcoder.user.domain.model.UserRole;

/** Command for user registration. */
public record RegisterCommand(String email, String password, String name, UserRole role) {

    public RegisterCommand(String email, String password, String name) {
        this(email, password, name, UserRole.MEMBER);
    }
}
