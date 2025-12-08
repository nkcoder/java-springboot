package org.nkcoder.shared.kernel.domain.valueobject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.nkcoder.shared.kernel.domain.event.DomainEvent;

/**
 * Base class for aggregate roots. Provides domain event registration and retrieval.
 *
 * @param <ID> The type of the aggregate root's identifier
 */
public abstract class AggregateRoot<ID> {

  private final List<DomainEvent> domainEvents = new ArrayList<>();

  /** Returns the unique identifier of this aggregate root. */
  public abstract ID getId();

  /** Registers a domain event to be published after the aggregate is persisted. */
  protected void registerEvent(DomainEvent event) {
    domainEvents.add(event);
  }

  /** Returns an unmodifiable view of the registered domain events. */
  public List<DomainEvent> getDomainEvents() {
    return Collections.unmodifiableList(domainEvents);
  }

  /** Clears all registered domain events. Should be called after events are published. */
  public void clearDomainEvents() {
    domainEvents.clear();
  }
}
