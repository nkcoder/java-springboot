package org.nkcoder.shared.kernel.exception;

/**
 * Exception thrown when business validation fails. Examples: duplicate email, password mismatch,
 * invalid state transitions.
 */
public class ValidationException extends DomainException {

  public ValidationException(String message) {
    super(message);
  }

  public ValidationException(String message, Throwable cause) {
    super(message, cause);
  }
}
