package org.nkcoder.shared.kernel.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserRegisteredEvent(UUID userId, String email, String userName, LocalDateTime occurredOn)
        implements DomainEvent {
    public UserRegisteredEvent(UUID userId, String email, String userName) {
        this(userId, email, userName, LocalDateTime.now());
    }

    @Override
    public String eventType() {
        return "user.registered";
    }

    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }
}
