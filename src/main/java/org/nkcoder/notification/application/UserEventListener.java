package org.nkcoder.notification.application;

import org.nkcoder.notification.NotificationService;
import org.nkcoder.shared.kernel.domain.event.UserRegisteredEvent;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class UserEventListener {
    private final NotificationService notificationService;

    public UserEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @ApplicationModuleListener
    public void onUserRegistered(UserRegisteredEvent event) {
        notificationService.sendWelcomeEmail(event.email(), event.userName());
    }
}
