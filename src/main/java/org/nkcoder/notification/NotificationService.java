package org.nkcoder.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void sendWelcomeEmail(String email, String userName) {
        // TODO: implement email sending
        logger.info("Sending Welcome email to {}, for user: {}", email, userName);
    }

    public void sendPasswordResetEmail(String email, String userName) {
        // TODO: implement password reset email
        logger.info("Sending password reset email to {}, for user: {}", email, userName);
    }
}
