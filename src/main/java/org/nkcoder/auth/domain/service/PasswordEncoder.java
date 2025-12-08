package org.nkcoder.auth.domain.service;

import org.nkcoder.auth.domain.model.HashedPassword;

/** Domain service interface for password encoding and verification. Implementations are in the infrastructure layer. */
public interface PasswordEncoder {

    /** Encodes a raw password into a hashed password. */
    HashedPassword encode(String rawPassword);

    /** Checks if a raw password matches a hashed password. */
    boolean matches(String rawPassword, HashedPassword hashedPassword);
}
