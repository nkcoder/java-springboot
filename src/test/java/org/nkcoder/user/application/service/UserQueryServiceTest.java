package org.nkcoder.user.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

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
import org.nkcoder.shared.kernel.domain.valueobject.Email;
import org.nkcoder.shared.kernel.exception.ResourceNotFoundException;
import org.nkcoder.user.application.dto.response.UserDto;
import org.nkcoder.user.domain.model.User;
import org.nkcoder.user.domain.model.UserId;
import org.nkcoder.user.domain.model.UserName;
import org.nkcoder.user.domain.model.UserRole;
import org.nkcoder.user.domain.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserQueryService")
class UserQueryServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserQueryService userQueryService;

    @BeforeEach
    void setUp() {
        userQueryService = new UserQueryService(userRepository);
    }

    private User createTestUser(UUID userId, String email, String name) {
        return User.reconstitute(
                UserId.of(userId),
                Email.of(email),
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

            UserDto result = userQueryService.getUserById(userId);

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

            assertThatThrownBy(() -> userQueryService.getUserById(userId))
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

            List<UserDto> result = userQueryService.getAllUsers();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(UserDto::email).containsExactly("user1@example.com", "user2@example.com");
        }

        @Test
        @DisplayName("returns empty list when no users")
        void returnsEmptyListWhenNoUsers() {
            given(userRepository.findAll()).willReturn(List.of());

            List<UserDto> result = userQueryService.getAllUsers();

            assertThat(result).isEmpty();
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

            boolean result = userQueryService.userExists(userId);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("returns false when user does not exist")
        void returnsFalseWhenUserDoesNotExist() {
            UUID userId = UUID.randomUUID();

            given(userRepository.existsById(any(UserId.class))).willReturn(false);

            boolean result = userQueryService.userExists(userId);

            assertThat(result).isFalse();
        }
    }
}
