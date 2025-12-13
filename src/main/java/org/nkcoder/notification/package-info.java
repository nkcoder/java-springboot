/**
 * The Notification module handles email and SMS notifications.
 *
 * <p>This module provides:
 *
 * <ul>
 *   <li>Email notifications
 *   <li>SMS notifications (future)
 * </ul>
 *
 * <p>Listens to events:
 *
 * <ul>
 *   <li>{@code UserRegisteredEvent} - sends welcome email
 * </ul>
 */
@ApplicationModule(allowedDependencies = {"shared", "infrastructure"})
package org.nkcoder.notification;

import org.springframework.modulith.ApplicationModule;
