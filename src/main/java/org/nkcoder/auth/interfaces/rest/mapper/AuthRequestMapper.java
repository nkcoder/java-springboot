package org.nkcoder.auth.interfaces.rest.mapper;

import org.nkcoder.auth.application.dto.command.LoginCommand;
import org.nkcoder.auth.application.dto.command.RefreshTokenCommand;
import org.nkcoder.auth.application.dto.command.RegisterCommand;
import org.nkcoder.auth.interfaces.rest.request.LoginRequest;
import org.nkcoder.auth.interfaces.rest.request.RefreshTokenRequest;
import org.nkcoder.auth.interfaces.rest.request.RegisterRequest;
import org.springframework.stereotype.Component;

/** Mapper for converting REST requests to application commands. */
@Component
public class AuthRequestMapper {

  public RegisterCommand toCommand(RegisterRequest request) {
    return new RegisterCommand(request.email(), request.password(), request.name(), request.role());
  }

  public LoginCommand toCommand(LoginRequest request) {
    return new LoginCommand(request.email(), request.password());
  }

  public RefreshTokenCommand toCommand(RefreshTokenRequest request) {
    return new RefreshTokenCommand(request.refreshToken());
  }
}
