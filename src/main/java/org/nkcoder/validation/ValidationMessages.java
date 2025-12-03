package org.nkcoder.validation;

public class ValidationMessages {
  private ValidationMessages() {
    // Utility class - prevent instantiation
  }

  /**
   * Regex requiring at least one lowercase, one uppercase, and one digit. Pattern breakdown: -
   * (?=.*[a-z]) - at least one lowercase letter - (?=.*[A-Z]) - at least one uppercase letter -
   * (?=.*\d) - at least one digit - .+ - at least one character (combined with lookaheads)
   */
  public static final int PASSWORD_MIN_LENGTH = 8;

  public static final String PASSWORD_COMPLEXITY_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$";
  public static final String PASSWORD_REQUIRED = "Password is required.";
  public static final String PASSWORD_SIZE =
      "Password must be at least " + PASSWORD_MIN_LENGTH + " characters long";
  public static final String PASSWORD_COMPLEXITY =
      "Password must contain at least one lowercase letter, one uppercase letter, and one number";
  public static final String CURRENT_PASSWORD_REQUIRED = "Current password is required";
  public static final String CONFIRM_PASSWORD_REQUIRED = "Password confirmation is required";

  // ===== Email Validation =====
  public static final String EMAIL_REQUIRED = "Email is required";
  public static final String EMAIL_INVALID = "Please provide a valid email";

  // ===== Name Validation =====
  public static final int NAME_MIN_LENGTH = 2;
  public static final int NAME_MAX_LENGTH = 50;
  public static final String NAME_REQUIRED = "Name is required";
  public static final String NAME_SIZE =
      "Name must be between " + NAME_MIN_LENGTH + " and " + NAME_MAX_LENGTH + " characters";

  // ===== Token Validation =====
  public static final String REFRESH_TOKEN_REQUIRED = "Refresh token is required";
}
