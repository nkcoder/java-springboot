package org.nkcoder.shared.kernel.exception;

/**
 * Base exception for all domain-level exceptions. Provides a common hierarchy for exception
 * handling across bounded contexts.
 */
public abstract class DomainException extends RuntimeException {

  protected DomainException(String message) {
    super(message);
  }

  protected DomainException(String message, Throwable cause) {
    super(message, cause);
  }
}
