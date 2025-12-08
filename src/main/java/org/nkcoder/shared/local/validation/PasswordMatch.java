package org.nkcoder.shared.local.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordMatchValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordMatch {
    String message() default "New password and confirmation password do not match";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String passwordField() default "newPassword";

    String confirmField() default "confirmPassword";
}
