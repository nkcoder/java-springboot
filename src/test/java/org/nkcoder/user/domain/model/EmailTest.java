package org.nkcoder.user.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Email Value Object")
class EmailTest {

    @Nested
    @DisplayName("creation")
    class Creation {

        @Test
        @DisplayName("creates email with valid format")
        void createsEmailWithValidFormat() {
            Email email = Email.of("user@example.com");

            assertThat(email.value()).isEqualTo("user@example.com");
        }

        @Test
        @DisplayName("normalizes email to lowercase")
        void normalizesEmailToLowercase() {
            Email email = Email.of("USER@EXAMPLE.COM");

            assertThat(email.value()).isEqualTo("user@example.com");
        }

        @Test
        @DisplayName("trims whitespace")
        void trimsWhitespace() {
            Email email = Email.of("  user@example.com  ");

            assertThat(email.value()).isEqualTo("user@example.com");
        }

        @ParameterizedTest
        @DisplayName("accepts valid email formats")
        @ValueSource(
                strings = {
                    "user@example.com",
                    "user.name@example.com",
                    "user-name@example.com",
                    "user_name@example.com",
                    "user@sub.example.com",
                    "user123@example.co"
                })
        void acceptsValidEmailFormats(String validEmail) {
            Email email = Email.of(validEmail);

            assertThat(email.value()).isEqualTo(validEmail.toLowerCase().trim());
        }
    }

    @Nested
    @DisplayName("validation")
    class Validation {

        @Test
        @DisplayName("throws when email is null")
        void throwsWhenEmailIsNull() {
            assertThatThrownBy(() -> Email.of(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Email cannot be null");
        }

        @ParameterizedTest
        @DisplayName("rejects invalid email formats")
        @ValueSource(
                strings = {
                    "",
                    "invalid",
                    "invalid@",
                    "@example.com",
                    "user@",
                    "user@.com",
                    "user@example",
                    "user space@example.com"
                })
        void rejectsInvalidEmailFormats(String invalidEmail) {
            assertThatThrownBy(() -> Email.of(invalidEmail))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid email format");
        }
    }

    @Nested
    @DisplayName("isValid static method")
    class IsValidMethod {

        @Test
        @DisplayName("returns true for valid email")
        void returnsTrueForValidEmail() {
            assertThat(Email.isValid("user@example.com")).isTrue();
        }

        @Test
        @DisplayName("returns false for invalid email")
        void returnsFalseForInvalidEmail() {
            assertThat(Email.isValid("invalid")).isFalse();
        }

        @Test
        @DisplayName("returns false for null")
        void returnsFalseForNull() {
            assertThat(Email.isValid(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("equality")
    class Equality {

        @Test
        @DisplayName("emails with same value are equal")
        void emailsWithSameValueAreEqual() {
            Email email1 = Email.of("user@example.com");
            Email email2 = Email.of("user@example.com");

            assertThat(email1).isEqualTo(email2);
            assertThat(email1.hashCode()).isEqualTo(email2.hashCode());
        }

        @Test
        @DisplayName("emails with same value but different case are equal")
        void emailsWithDifferentCaseAreEqual() {
            Email email1 = Email.of("user@example.com");
            Email email2 = Email.of("USER@EXAMPLE.COM");

            assertThat(email1).isEqualTo(email2);
        }

        @Test
        @DisplayName("emails with different values are not equal")
        void emailsWithDifferentValuesAreNotEqual() {
            Email email1 = Email.of("user1@example.com");
            Email email2 = Email.of("user2@example.com");

            assertThat(email1).isNotEqualTo(email2);
        }
    }
}
