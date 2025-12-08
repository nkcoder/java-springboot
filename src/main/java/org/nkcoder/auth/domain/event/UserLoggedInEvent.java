package org.nkcoder.auth.domain.event;

import java.time.LocalDateTime;
import org.nkcoder.auth.domain.model.AuthUserId;
import org.nkcoder.shared.kernel.domain.event.DomainEvent;
import org.nkcoder.shared.kernel.domain.valueobject.Email;

/** Domain event published when a user logs in. */
public record UserLoggedInEvent(AuthUserId userId, Email email, LocalDateTime occurredOn)
    implements DomainEvent {

  public UserLoggedInEvent(AuthUserId userId, Email email) {
    this(userId, email, LocalDateTime.now());
  }

  @Override
  public String eventType() {
    return "auth.user.logged_in";
  }
}
