package org.nkcoder.auth.domain.event;

import java.time.LocalDateTime;
import org.nkcoder.auth.domain.model.AuthRole;
import org.nkcoder.auth.domain.model.AuthUserId;
import org.nkcoder.shared.kernel.domain.event.DomainEvent;
import org.nkcoder.shared.kernel.domain.valueobject.Email;

/** Domain event published when a new user registers. */
public record UserRegisteredEvent(
    AuthUserId userId, Email email, String name, AuthRole role, LocalDateTime occurredOn)
    implements DomainEvent {

  public UserRegisteredEvent(AuthUserId userId, Email email, String name, AuthRole role) {
    this(userId, email, name, role, LocalDateTime.now());
  }

  @Override
  public String eventType() {
    return "auth.user.registered";
  }
}
