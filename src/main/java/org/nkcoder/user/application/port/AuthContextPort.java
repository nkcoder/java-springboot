package org.nkcoder.user.application.port;

import java.util.UUID;

/**
 * Port for communicating with the Auth bounded context. Used for password-related operations that
 * are owned by Auth.
 */
public interface AuthContextPort {

  /**
   * Verifies a user's current password.
   *
   * @param userId the user's ID
   * @param password the password to verify
   * @return true if the password matches
   */
  boolean verifyPassword(UUID userId, String password);

  /**
   * Changes a user's password.
   *
   * @param userId the user's ID
   * @param newPassword the new password to set
   */
  void changePassword(UUID userId, String newPassword);
}
