package org.nkcoder.entity;

import java.util.UUID;
import org.nkcoder.enums.Role;

public class UserTestFactory {
  private UserTestFactory() {}

  // backward compatibility
  public static User createWithId(
      UUID id, String email, String password, String name, Role role, Boolean emailVerified) {
    return new User(id, email, password, name, role, emailVerified);
  }

  public static UserBuilder aUser() {
    return new UserBuilder();
  }

  public static UserBuilder anAdmin() {
    return aUser().withRole(Role.ADMIN);
  }

  public static UserBuilder aVerifiedUser() {
    return aUser().withEmailVerified(true);
  }

  // Builder Class
  public static final class UserBuilder {
    private UUID id = UUID.randomUUID();
    private String email = "test-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    private String password = "encoded-password";
    private String name = "Test User";
    private Role role = Role.MEMBER;
    private boolean emailVerified = false;

    private UserBuilder() {}

    public UserBuilder withId(UUID id) {
      this.id = id;
      return this;
    }

    public UserBuilder withEmail(String email) {
      this.email = email;
      return this;
    }

    public UserBuilder withPassword(String password) {
      this.password = password;
      return this;
    }

    public UserBuilder withName(String name) {
      this.name = name;
      return this;
    }

    public UserBuilder withRole(Role role) {
      this.role = role;
      return this;
    }

    public UserBuilder withEmailVerified(boolean emailVerified) {
      this.emailVerified = emailVerified;
      return this;
    }

    /** Builds the User using the package-private test constructor. */
    public User build() {
      return new User(id, email, password, name, role, emailVerified);
    }
  }
}
