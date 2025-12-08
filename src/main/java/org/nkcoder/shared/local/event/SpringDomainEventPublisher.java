package org.nkcoder.shared.local.event;

import org.nkcoder.shared.kernel.domain.event.DomainEvent;
import org.nkcoder.shared.kernel.domain.event.DomainEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Spring-based implementation of DomainEventPublisher. Uses Spring's ApplicationEventPublisher to
 * broadcast domain events within the application.
 */
@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {

  private final ApplicationEventPublisher applicationEventPublisher;

  public SpringDomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  @Override
  public void publish(DomainEvent event) {
    applicationEventPublisher.publishEvent(event);
  }
}
