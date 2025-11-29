package org.nkcoder.validation;

public class PasswordValidation {
  private PasswordValidation() {
    // Utility class - prevent instantiation
  }

  public static final int MIN_LENGTH = 8;

  /**
   * Regex requiring at least one lowercase, one uppercase, and one digit. Pattern breakdown: -
   * (?=.*[a-z]) - at least one lowercase letter - (?=.*[A-Z]) - at least one uppercase letter -
   * (?=.*\d) - at least one digit - .+ - at least one character (combined with lookaheads)
   */
  public static final String COMPLEXITY_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$";

  // Validation messages
  public static final String CURRENT_PASSWORD_REQUIRED = "Password is required.";
  public static final String PASSWORD_REQUIRED = "Password is required.";
  public static final String PASSWORD_MIN_LENGTH =
      "Password must be at least " + MIN_LENGTH + " characters long";
  public static final String PASSWORD_COMPLEXITY =
      "Password must contain at least one lowercase letter, one uppercase letter, and one number";
  public static final String CONFIRM_PASSWORD_REQUIRED = "Password confirmation is required";
}
