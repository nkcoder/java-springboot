package org.nkcoder.shared.kernel.domain.event;

import java.time.LocalDateTime;

/**
 * Base interface for all domain events. Domain events represent something significant that happened
 * in the domain.
 */
public interface DomainEvent {

  /** Returns the timestamp when the event occurred. */
  LocalDateTime occurredOn();

  /** Returns the type identifier for this event. */
  String eventType();
}
