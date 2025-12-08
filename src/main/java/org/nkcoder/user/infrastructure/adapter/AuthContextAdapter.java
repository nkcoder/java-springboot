package org.nkcoder.user.infrastructure.adapter;

import java.util.UUID;
import org.nkcoder.auth.domain.model.AuthUserId;
import org.nkcoder.auth.domain.model.HashedPassword;
import org.nkcoder.auth.domain.repository.AuthUserRepository;
import org.nkcoder.auth.domain.service.PasswordEncoder;
import org.nkcoder.shared.kernel.exception.ResourceNotFoundException;
import org.nkcoder.user.application.port.AuthContextPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Adapter for communicating with the Auth bounded context. */
@Component
public class AuthContextAdapter implements AuthContextPort {

    private static final Logger logger = LoggerFactory.getLogger(AuthContextAdapter.class);

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthContextAdapter(AuthUserRepository authUserRepository, PasswordEncoder passwordEncoder) {
        this.authUserRepository = authUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean verifyPassword(UUID userId, String password) {
        logger.debug("Verifying password for user: {}", userId);

        var authUser = authUserRepository
                .findById(AuthUserId.of(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        return passwordEncoder.matches(password, authUser.getPassword());
    }

    @Override
    public void changePassword(UUID userId, String newPassword) {
        logger.debug("Changing password for user: {}", userId);

        var authUser = authUserRepository
                .findById(AuthUserId.of(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        HashedPassword encodedPassword = passwordEncoder.encode(newPassword);
        authUser.changePassword(encodedPassword);

        authUserRepository.save(authUser);
        logger.info("Password changed for user: {}", userId);
    }
}
