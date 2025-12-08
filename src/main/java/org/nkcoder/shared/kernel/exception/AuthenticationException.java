package org.nkcoder.shared.kernel.exception;

/**
 * Exception thrown when authentication fails. Examples: invalid credentials, expired token, invalid
 * token.
 */
public class AuthenticationException extends DomainException {

  public AuthenticationException(String message) {
    super(message);
  }

  public AuthenticationException(String message, Throwable cause) {
    super(message, cause);
  }
}
