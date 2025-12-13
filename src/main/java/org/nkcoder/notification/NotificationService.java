package org.nkcoder.notification;

import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    public void sendWelcomeEmail(String email, String userName) {
        // TODO: implement email sending
        System.out.println("Sending Welcome email to " + email + ", for user: " + userName);
    }

    public void sendPasswordResetEmail(String email, String userName) {
        // TODO: implement password reset email
        System.out.println("Sending password reset email to " + email + ", for user: " + userName);
    }
}
