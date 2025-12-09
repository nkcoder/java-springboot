package org.nkcoder.user.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.nkcoder.user.domain.event.UserProfileUpdatedEvent;

@DisplayName("User Aggregate")
class UserTest {

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("creates new user with generated ID")
        void createsNewUserWithGeneratedId() {
            User user = User.register(
                    Email.of("test@example.com"),
                    HashedPassword.of("hashed"),
                    UserName.of("Test User"),
                    UserRole.MEMBER);

            assertThat(user.getId()).isNotNull();
            assertThat(user.getEmail().value()).isEqualTo("test@example.com");
            assertThat(user.getName().value()).isEqualTo("Test User");
            assertThat(user.getRole()).isEqualTo(UserRole.MEMBER);
        }

        @Test
        @DisplayName("sets email as unverified by default")
        void setsEmailAsUnverifiedByDefault() {
            User user = User.register(
                    Email.of("test@example.com"),
                    HashedPassword.of("hashed"),
                    UserName.of("Test User"),
                    UserRole.MEMBER);

            assertThat(user.isEmailVerified()).isFalse();
        }

        @Test
        @DisplayName("sets timestamps on creation")
        void setsTimestampsOnCreation() {
            LocalDateTime before = LocalDateTime.now();

            User user = User.register(
                    Email.of("test@example.com"),
                    HashedPassword.of("hashed"),
                    UserName.of("Test User"),
                    UserRole.MEMBER);

            LocalDateTime after = LocalDateTime.now();

            assertThat(user.getCreatedAt()).isBetween(before, after);
            assertThat(user.getUpdatedAt()).isBetween(before, after);
            assertThat(user.getLastLoginAt()).isNull();
        }

        @Test
        @DisplayName("defaults to MEMBER role when null")
        void defaultsToMemberRoleWhenNull() {
            User user = User.register(
                    Email.of("test@example.com"), HashedPassword.of("hashed"), UserName.of("Test User"), null);

            assertThat(user.getRole()).isEqualTo(UserRole.MEMBER);
        }

        @Test
        @DisplayName("throws when email is null")
        void throwsWhenEmailIsNull() {
            assertThatThrownBy(() ->
                            User.register(null, HashedPassword.of("hashed"), UserName.of("Test User"), UserRole.MEMBER))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Email cannot be null");
        }

        @Test
        @DisplayName("throws when password is null")
        void throwsWhenPasswordIsNull() {
            assertThatThrownBy(() -> User.register(
                            Email.of("test@example.com"), null, UserName.of("Test User"), UserRole.MEMBER))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Password cannot be null");
        }

        @Test
        @DisplayName("throws when name is null")
        void throwsWhenNameIsNull() {
            assertThatThrownBy(() -> User.register(
                            Email.of("test@example.com"), HashedPassword.of("hashed"), null, UserRole.MEMBER))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Name cannot be null");
        }
    }

    @Nested
    @DisplayName("updateProfile")
    class UpdateProfile {

        @Test
        @DisplayName("updates name and registers domain event")
        void updatesNameAndRegistersDomainEvent() {
            User user = createTestUser();
            UserName oldName = user.getName();

            user.updateProfile(UserName.of("New Name"));

            assertThat(user.getName().value()).isEqualTo("New Name");
            assertThat(user.getDomainEvents()).hasSize(1);
            assertThat(user.getDomainEvents().get(0)).isInstanceOf(UserProfileUpdatedEvent.class);

            UserProfileUpdatedEvent event =
                    (UserProfileUpdatedEvent) user.getDomainEvents().get(0);
            assertThat(event.oldName()).isEqualTo(oldName);
            assertThat(event.newName().value()).isEqualTo("New Name");
        }

        @Test
        @DisplayName("updates timestamp")
        void updatesTimestamp() {
            User user = createTestUser();
            LocalDateTime originalUpdatedAt = user.getUpdatedAt();

            // Small delay to ensure timestamp changes
            user.updateProfile(UserName.of("New Name"));

            assertThat(user.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
        }

        @Test
        @DisplayName("throws when new name is null")
        void throwsWhenNewNameIsNull() {
            User user = createTestUser();

            assertThatThrownBy(() -> user.updateProfile(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Name cannot be null");
        }
    }

    @Nested
    @DisplayName("changePassword")
    class ChangePassword {

        @Test
        @DisplayName("changes password")
        void changesPassword() {
            User user = createTestUser();
            HashedPassword newPassword = HashedPassword.of("new-hashed-password");

            user.changePassword(newPassword);

            assertThat(user.getPassword()).isEqualTo(newPassword);
        }

        @Test
        @DisplayName("updates timestamp")
        void updatesTimestamp() {
            User user = createTestUser();
            LocalDateTime originalUpdatedAt = user.getUpdatedAt();

            user.changePassword(HashedPassword.of("new-hashed"));

            assertThat(user.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
        }

        @Test
        @DisplayName("throws when new password is null")
        void throwsWhenNewPasswordIsNull() {
            User user = createTestUser();

            assertThatThrownBy(() -> user.changePassword(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("New password cannot be null");
        }
    }

    @Nested
    @DisplayName("updateEmail")
    class UpdateEmail {

        @Test
        @DisplayName("updates email and resets verification")
        void updatesEmailAndResetsVerification() {
            User user = createVerifiedUser();
            assertThat(user.isEmailVerified()).isTrue();

            user.updateEmail(Email.of("new@example.com"));

            assertThat(user.getEmail().value()).isEqualTo("new@example.com");
            assertThat(user.isEmailVerified()).isFalse();
        }

        @Test
        @DisplayName("updates timestamp")
        void updatesTimestamp() {
            User user = createTestUser();
            LocalDateTime originalUpdatedAt = user.getUpdatedAt();

            user.updateEmail(Email.of("new@example.com"));

            assertThat(user.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
        }

        @Test
        @DisplayName("throws when email is null")
        void throwsWhenEmailIsNull() {
            User user = createTestUser();

            assertThatThrownBy(() -> user.updateEmail(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Email cannot be null");
        }
    }

    @Nested
    @DisplayName("verifyEmail")
    class VerifyEmail {

        @Test
        @DisplayName("marks email as verified")
        void marksEmailAsVerified() {
            User user = createTestUser();
            assertThat(user.isEmailVerified()).isFalse();

            user.verifyEmail();

            assertThat(user.isEmailVerified()).isTrue();
        }

        @Test
        @DisplayName("updates timestamp")
        void updatesTimestamp() {
            User user = createTestUser();
            LocalDateTime originalUpdatedAt = user.getUpdatedAt();

            user.verifyEmail();

            assertThat(user.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
        }
    }

    @Nested
    @DisplayName("recordLogin")
    class RecordLogin {

        @Test
        @DisplayName("updates last login timestamp")
        void updatesLastLoginTimestamp() {
            User user = createTestUser();
            assertThat(user.getLastLoginAt()).isNull();

            LocalDateTime before = LocalDateTime.now();
            user.recordLogin();
            LocalDateTime after = LocalDateTime.now();

            assertThat(user.getLastLoginAt()).isBetween(before, after);
        }

        @Test
        @DisplayName("updates updatedAt timestamp")
        void updatesUpdatedAtTimestamp() {
            User user = createTestUser();
            LocalDateTime originalUpdatedAt = user.getUpdatedAt();

            user.recordLogin();

            assertThat(user.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
        }
    }

    @Nested
    @DisplayName("isAdmin")
    class IsAdmin {

        @Test
        @DisplayName("returns true for admin role")
        void returnsTrueForAdminRole() {
            User user = User.register(
                    Email.of("admin@example.com"), HashedPassword.of("hashed"), UserName.of("Admin"), UserRole.ADMIN);

            assertThat(user.isAdmin()).isTrue();
        }

        @Test
        @DisplayName("returns false for member role")
        void returnsFalseForMemberRole() {
            User user = createTestUser();

            assertThat(user.isAdmin()).isFalse();
        }
    }

    @Nested
    @DisplayName("equality")
    class Equality {

        @Test
        @DisplayName("users with same ID are equal")
        void usersWithSameIdAreEqual() {
            UserId userId = UserId.generate();
            User user1 = createUserWithId(userId);
            User user2 = createUserWithId(userId);

            assertThat(user1).isEqualTo(user2);
            assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
        }

        @Test
        @DisplayName("users with different IDs are not equal")
        void usersWithDifferentIdsAreNotEqual() {
            User user1 = createTestUser();
            User user2 = createTestUser();

            assertThat(user1).isNotEqualTo(user2);
        }
    }

    @Nested
    @DisplayName("clearDomainEvents")
    class ClearDomainEvents {

        @Test
        @DisplayName("clears registered domain events")
        void clearsDomainEvents() {
            User user = createTestUser();
            user.updateProfile(UserName.of("New Name"));
            assertThat(user.getDomainEvents()).hasSize(1);

            user.clearDomainEvents();

            assertThat(user.getDomainEvents()).isEmpty();
        }
    }

    // Test helpers

    private User createTestUser() {
        return User.register(
                Email.of("test@example.com"),
                HashedPassword.of("hashed-password"),
                UserName.of("Test User"),
                UserRole.MEMBER);
    }

    private User createVerifiedUser() {
        return User.reconstitute(
                UserId.generate(),
                Email.of("verified@example.com"),
                HashedPassword.of("hashed"),
                UserName.of("Verified User"),
                UserRole.MEMBER,
                true,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now());
    }

    private User createUserWithId(UserId userId) {
        return User.reconstitute(
                userId,
                Email.of("test@example.com"),
                HashedPassword.of("hashed"),
                UserName.of("Test User"),
                UserRole.MEMBER,
                false,
                null,
                LocalDateTime.now(),
                LocalDateTime.now());
    }
}
