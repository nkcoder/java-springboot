package org.nkcoder.user.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.nkcoder.shared.kernel.exception.ValidationException;

@DisplayName("UserName Value Object")
class UserNameTest {

    @Nested
    @DisplayName("creation")
    class Creation {

        @Test
        @DisplayName("creates username with valid value")
        void createsUsernameWithValidValue() {
            UserName name = UserName.of("John Doe");

            assertThat(name.value()).isEqualTo("John Doe");
        }

        @Test
        @DisplayName("trims whitespace")
        void trimsWhitespace() {
            UserName name = UserName.of("  John Doe  ");

            assertThat(name.value()).isEqualTo("John Doe");
        }

        @Test
        @DisplayName("accepts single character name")
        void acceptsSingleCharacterName() {
            UserName name = UserName.of("A");

            assertThat(name.value()).isEqualTo("A");
        }

        @Test
        @DisplayName("accepts name at max length")
        void acceptsNameAtMaxLength() {
            String longName = "A".repeat(100);
            UserName name = UserName.of(longName);

            assertThat(name.value()).isEqualTo(longName);
        }
    }

    @Nested
    @DisplayName("validation")
    class Validation {

        @Test
        @DisplayName("throws when name is null")
        void throwsWhenNameIsNull() {
            assertThatThrownBy(() -> UserName.of(null)).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("throws when name is empty")
        void throwsWhenNameIsEmpty() {
            assertThatThrownBy(() -> UserName.of(""))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Name must be between 1 and 100 characters");
        }

        @Test
        @DisplayName("throws when name is only whitespace")
        void throwsWhenNameIsOnlyWhitespace() {
            assertThatThrownBy(() -> UserName.of("   "))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Name must be between 1 and 100 characters");
        }

        @Test
        @DisplayName("throws when name exceeds max length")
        void throwsWhenNameExceedsMaxLength() {
            String tooLongName = "A".repeat(101);

            assertThatThrownBy(() -> UserName.of(tooLongName))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Name must be between 1 and 100 characters");
        }
    }

    @Nested
    @DisplayName("equality")
    class Equality {

        @Test
        @DisplayName("usernames with same value are equal")
        void usernamesWithSameValueAreEqual() {
            UserName name1 = UserName.of("John Doe");
            UserName name2 = UserName.of("John Doe");

            assertThat(name1).isEqualTo(name2);
            assertThat(name1.hashCode()).isEqualTo(name2.hashCode());
        }

        @Test
        @DisplayName("usernames with different values are not equal")
        void usernamesWithDifferentValuesAreNotEqual() {
            UserName name1 = UserName.of("John Doe");
            UserName name2 = UserName.of("Jane Doe");

            assertThat(name1).isNotEqualTo(name2);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringMethod {

        @Test
        @DisplayName("returns the trimmed value")
        void returnsTheTrimmedValue() {
            UserName name = UserName.of("  John Doe  ");

            assertThat(name.toString()).isEqualTo("John Doe");
        }
    }
}
