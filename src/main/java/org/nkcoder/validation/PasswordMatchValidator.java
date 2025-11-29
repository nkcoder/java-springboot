package org.nkcoder.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;

// Generic validator for any Record type
public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, Record> {
  private String passwordField;
  private String confirmField;

  @Override
  public void initialize(PasswordMatch annotation) {
    this.passwordField = annotation.passwordField();
    this.confirmField = annotation.confirmField();
  }

  @Override
  public boolean isValid(Record record, ConstraintValidatorContext context) {
    if (record == null) {
      return true;
    }

    try {
      String password = getFieldValue(record, passwordField);
      String confirmPassword = getFieldValue(record, confirmField);

      // Both null is valid (let @NotBlank handle individual field validation)
      if (password == null && confirmPassword == null) {
        return true;
      }

      if (password == null || confirmPassword == null) {
        return false;
      }

      return password.equals(confirmPassword);
    } catch (Exception e) {
      // If reflection fails, let validation pass and rely on other constraints
      return true;
    }
  }

  private String getFieldValue(Record record, String fieldName) throws Exception {
    RecordComponent component =
        Arrays.stream(record.getClass().getRecordComponents())
            .filter(c -> c.getName().equals(fieldName))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Field not found: " + fieldName));

    Object value = component.getAccessor().invoke(record);
    return value != null ? value.toString() : null;
  }
}
