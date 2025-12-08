package org.nkcoder.shared.kernel.exception;

/** Exception thrown when a requested resource cannot be found. Examples: user not found, token not found. */
public class ResourceNotFoundException extends DomainException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
