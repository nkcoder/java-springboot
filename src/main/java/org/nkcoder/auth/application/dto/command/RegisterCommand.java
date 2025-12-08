package org.nkcoder.auth.application.dto.command;

import org.nkcoder.auth.domain.model.AuthRole;

/** Command for user registration. */
public record RegisterCommand(String email, String password, String name, AuthRole role) {

  public RegisterCommand(String email, String password, String name) {
    this(email, password, name, AuthRole.MEMBER);
  }
}
