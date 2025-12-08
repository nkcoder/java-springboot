package org.nkcoder.auth.application.dto.command;

/** Command for user login. */
public record LoginCommand(String email, String password) {}
