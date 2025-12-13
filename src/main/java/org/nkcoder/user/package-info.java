/**
 * The User module handles user authentication and management.
 *
 * <p>This module provides:
 *
 * <ul>
 *   <li>User registration and authentication
 *   <li>JWT token management with refresh token rotation
 *   <li>User profile management
 *   <li>Admin user management
 * </ul>
 *
 * <p>Events published:
 *
 * <ul>
 *   <li>{@code UserRegisteredEvent} - when a new user registers
 *   <li>{@code UserProfileUpdatedEvent} - when user profile changes
 * </ul>
 */
@ApplicationModule(allowedDependencies = {"shared", "infrastructure"})
package org.nkcoder.user;

import org.springframework.modulith.ApplicationModule;
