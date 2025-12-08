package org.nkcoder.shared.kernel.domain.event;

/**
 * Interface for publishing domain events. Implementations may use Spring's ApplicationEventPublisher, a message queue,
 * or other mechanisms.
 */
public interface DomainEventPublisher {

    /** Publishes a domain event. */
    void publish(DomainEvent event);
}
