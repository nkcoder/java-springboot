package org.nkcoder.user.domain.event;

import java.time.LocalDateTime;
import org.nkcoder.shared.kernel.domain.event.DomainEvent;
import org.nkcoder.user.domain.model.UserId;
import org.nkcoder.user.domain.model.UserName;

/** Domain event published when a user's profile is updated. */
public record UserProfileUpdatedEvent(
    LocalDateTime occurredOn, UserId userId, UserName oldName, UserName newName)
    implements DomainEvent {

  private static final String EVENT_TYPE = "user.profile.updated";

  public UserProfileUpdatedEvent(UserId userId, UserName oldName, UserName newName) {
    this(LocalDateTime.now(), userId, oldName, newName);
  }

  @Override
  public LocalDateTime occurredOn() {
    return occurredOn;
  }

  @Override
  public String eventType() {
    return EVENT_TYPE;
  }
}
