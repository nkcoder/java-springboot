package org.nkcoder.user.domain.service;

import org.nkcoder.shared.kernel.exception.AuthenticationException;
import org.nkcoder.user.domain.model.Email;
import org.nkcoder.user.domain.model.User;
import org.nkcoder.user.domain.repository.UserRepository;
import org.springframework.stereotype.Service;

/** Domain service for user authentication. Encapsulates credential verification logic. */
@Service
public class AuthenticationService {

    public static final String INVALID_CREDENTIALS = "Invalid email or password";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Authenticates a user by email and password.
     *
     * @param email the user's email
     * @param rawPassword the raw password to verify
     * @return the authenticated user
     * @throws AuthenticationException if credentials are invalid
     */
    public User authenticate(Email email, String rawPassword) {
        User user =
                userRepository.findByEmail(email).orElseThrow(() -> new AuthenticationException(INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new AuthenticationException(INVALID_CREDENTIALS);
        }

        return user;
    }

    /**
     * Verifies if a password matches the user's current password.
     *
     * @param user the user
     * @param rawPassword the password to verify
     * @return true if the password matches
     */
    public boolean verifyPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }
}
