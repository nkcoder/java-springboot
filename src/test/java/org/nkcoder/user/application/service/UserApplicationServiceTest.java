package org.nkcoder.user.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nkcoder.shared.kernel.domain.event.DomainEvent;
import org.nkcoder.shared.kernel.domain.event.DomainEventPublisher;
import org.nkcoder.shared.kernel.exception.ResourceNotFoundException;
import org.nkcoder.shared.kernel.exception.ValidationException;
import org.nkcoder.user.application.dto.command.AdminResetPasswordCommand;
import org.nkcoder.user.application.dto.command.AdminUpdateUserCommand;
import org.nkcoder.user.application.dto.command.ChangePasswordCommand;
import org.nkcoder.user.application.dto.command.UpdateProfileCommand;
import org.nkcoder.user.application.dto.response.UserDto;
import org.nkcoder.user.domain.model.Email;
import org.nkcoder.user.domain.model.HashedPassword;
import org.nkcoder.user.domain.model.User;
import org.nkcoder.user.domain.model.UserId;
import org.nkcoder.user.domain.model.UserName;
import org.nkcoder.user.domain.model.UserRole;
import org.nkcoder.user.domain.repository.UserRepository;
import org.nkcoder.user.domain.service.AuthenticationService;
import org.nkcoder.user.domain.service.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserApplicationService")
class UserApplicationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private DomainEventPublisher eventPublisher;

    private UserApplicationService userApplicationService;

    @BeforeEach
    void setUp() {
        userApplicationService =
                new UserApplicationService(userRepository, passwordEncoder, authenticationService, eventPublisher);
    }

    private User createTestUser(UUID userId, String email, String name) {
        return User.reconstitute(
                UserId.of(userId),
                Email.of(email),
                HashedPassword.of("hashed-password"),
                UserName.of(name),
                UserRole.MEMBER,
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now());
    }

    @Nested
    @DisplayName("getUserById")
    class GetUserById {

        @Test
        @DisplayName("returns user when found")
        void returnsUserWhenFound() {
            UUID userId = UUID.randomUUID();
            User user = createTestUser(userId, "test@example.com", "Test User");

            given(userRepository.findById(any(UserId.class))).willReturn(Optional.of(user));

            UserDto result = userApplicationService.getUserById(userId);

            assertThat(result.id()).isEqualTo(userId);
            assertThat(result.email()).isEqualTo("test@example.com");
            assertThat(result.name()).isEqualTo("Test User");
            assertThat(result.role()).isEqualTo("MEMBER");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when user not found")
        void throwsWhenUserNotFound() {
            UUID userId = UUID.randomUUID();

            given(userRepository.findById(any(UserId.class))).willReturn(Optional.empty());

            assertThatThrownBy(() -> userApplicationService.getUserById(userId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("getAllUsers")
    class GetAllUsers {

        @Test
        @DisplayName("returns all users")
        void returnsAllUsers() {
            User user1 = createTestUser(UUID.randomUUID(), "user1@example.com", "User One");
            User user2 = createTestUser(UUID.randomUUID(), "user2@example.com", "User Two");

            given(userRepository.findAll()).willReturn(List.of(user1, user2));

            List<UserDto> result = userApplicationService.getAllUsers();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(UserDto::email).containsExactly("user1@example.com", "user2@example.com");
        }

        @Test
        @DisplayName("returns empty list when no users")
        void returnsEmptyListWhenNoUsers() {
            given(userRepository.findAll()).willReturn(List.of());

            List<UserDto> result = userApplicationService.getAllUsers();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateProfile")
    class UpdateProfile {

        @Test
        @DisplayName("updates profile successfully")
        void updatesProfileSuccessfully() {
            UUID userId = UUID.randomUUID();
            User user = createTestUser(userId, "test@example.com", "Old Name");
            UpdateProfileCommand command = new UpdateProfileCommand(userId, "New Name");

            given(userRepository.findById(any(UserId.class))).willReturn(Optional.of(user));
            given(userRepository.save(any(User.class))).willReturn(user);

            UserDto result = userApplicationService.updateProfile(command);

            assertThat(result.name()).isEqualTo("New Name");
            verify(eventPublisher).publish(any(DomainEvent.class));
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when user not found")
        void throwsWhenUserNotFound() {
            UUID userId = UUID.randomUUID();
            UpdateProfileCommand command = new UpdateProfileCommand(userId, "New Name");

            given(userRepository.findById(any(UserId.class))).willReturn(Optional.empty());

            assertThatThrownBy(() -> userApplicationService.updateProfile(command))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("changePassword")
    class ChangePassword {

        @Test
        @DisplayName("changes password successfully")
        void changesPasswordSuccessfully() {
            UUID userId = UUID.randomUUID();
            User user = createTestUser(userId, "test@example.com", "Test User");
            ChangePasswordCommand command = new ChangePasswordCommand(userId, "oldPass", "newPass");

            given(userRepository.findById(any(UserId.class))).willReturn(Optional.of(user));
            given(authenticationService.verifyPassword(any(User.class), any())).willReturn(true);
            given(passwordEncoder.encode(any())).willReturn(HashedPassword.of("new-hashed"));
            given(userRepository.save(any(User.class))).willReturn(user);

            userApplicationService.changePassword(command);

            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when user not found")
        void throwsWhenUserNotFound() {
            UUID userId = UUID.randomUUID();
            ChangePasswordCommand command = new ChangePasswordCommand(userId, "oldPass", "newPass");

            given(userRepository.findById(any(UserId.class))).willReturn(Optional.empty());

            assertThatThrownBy(() -> userApplicationService.changePassword(command))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");
        }

        @Test
        @DisplayName("throws ValidationException when current password is incorrect")
        void throwsWhenCurrentPasswordIncorrect() {
            UUID userId = UUID.randomUUID();
            User user = createTestUser(userId, "test@example.com", "Test User");
            ChangePasswordCommand command = new ChangePasswordCommand(userId, "wrongPass", "newPass");

            given(userRepository.findById(any(UserId.class))).willReturn(Optional.of(user));
            given(authenticationService.verifyPassword(any(User.class), any())).willReturn(false);

            assertThatThrownBy(() -> userApplicationService.changePassword(command))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Current password is incorrect");

            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("adminUpdateUser")
    class AdminUpdateUser {

        @Test
        @DisplayName("updates user name successfully")
        void updatesUserNameSuccessfully() {
            UUID userId = UUID.randomUUID();
            User user = createTestUser(userId, "test@example.com", "Old Name");
            AdminUpdateUserCommand command = new AdminUpdateUserCommand(userId, "New Name", null);

            given(userRepository.findById(any(UserId.class))).willReturn(Optional.of(user));
            given(userRepository.save(any(User.class))).willReturn(user);

            UserDto result = userApplicationService.adminUpdateUser(command);

            assertThat(result.name()).isEqualTo("New Name");
        }

        @Test
        @DisplayName("updates user email successfully")
        void updatesUserEmailSuccessfully() {
            UUID userId = UUID.randomUUID();
            User user = createTestUser(userId, "old@example.com", "Test User");
            AdminUpdateUserCommand command = new AdminUpdateUserCommand(userId, null, "new@example.com");

            given(userRepository.findById(any(UserId.class))).willReturn(Optional.of(user));
            given(userRepository.existsByEmailExcludingId(any(Email.class), any(UserId.class)))
                    .willReturn(false);
            given(userRepository.save(any(User.class))).willReturn(user);

            UserDto result = userApplicationService.adminUpdateUser(command);

            assertThat(result.email()).isEqualTo("new@example.com");
        }

        @Test
        @DisplayName("throws ValidationException when email already in use")
        void throwsWhenEmailAlreadyInUse() {
            UUID userId = UUID.randomUUID();
            User user = createTestUser(userId, "old@example.com", "Test User");
            AdminUpdateUserCommand command = new AdminUpdateUserCommand(userId, null, "taken@example.com");

            given(userRepository.findById(any(UserId.class))).willReturn(Optional.of(user));
            given(userRepository.existsByEmailExcludingId(any(Email.class), any(UserId.class)))
                    .willReturn(true);

            assertThatThrownBy(() -> userApplicationService.adminUpdateUser(command))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Email already in use");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when user not found")
        void throwsWhenUserNotFound() {
            UUID userId = UUID.randomUUID();
            AdminUpdateUserCommand command = new AdminUpdateUserCommand(userId, "Name", null);

            given(userRepository.findById(any(UserId.class))).willReturn(Optional.empty());

            assertThatThrownBy(() -> userApplicationService.adminUpdateUser(command))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("adminResetPassword")
    class AdminResetPassword {

        @Test
        @DisplayName("resets password successfully")
        void resetsPasswordSuccessfully() {
            UUID userId = UUID.randomUUID();
            User user = createTestUser(userId, "test@example.com", "Test User");
            AdminResetPasswordCommand command = new AdminResetPasswordCommand(userId, "newPassword");

            given(userRepository.findById(any(UserId.class))).willReturn(Optional.of(user));
            given(passwordEncoder.encode(any())).willReturn(HashedPassword.of("new-hashed"));
            given(userRepository.save(any(User.class))).willReturn(user);

            userApplicationService.adminResetPassword(command);

            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when user not found")
        void throwsWhenUserNotFound() {
            UUID userId = UUID.randomUUID();
            AdminResetPasswordCommand command = new AdminResetPasswordCommand(userId, "newPassword");

            given(userRepository.findById(any(UserId.class))).willReturn(Optional.empty());

            assertThatThrownBy(() -> userApplicationService.adminResetPassword(command))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("userExists")
    class UserExists {

        @Test
        @DisplayName("returns true when user exists")
        void returnsTrueWhenUserExists() {
            UUID userId = UUID.randomUUID();

            given(userRepository.existsById(any(UserId.class))).willReturn(true);

            boolean result = userApplicationService.userExists(userId);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("returns false when user does not exist")
        void returnsFalseWhenUserDoesNotExist() {
            UUID userId = UUID.randomUUID();

            given(userRepository.existsById(any(UserId.class))).willReturn(false);

            boolean result = userApplicationService.userExists(userId);

            assertThat(result).isFalse();
        }
    }
}
