package org.nkcoder.entity;

import java.util.UUID;
import org.nkcoder.enums.Role;

public class UserTestFactory {
  public static User createWithId(
      UUID id, String email, String password, String name, Role role, Boolean emailVerified) {
    return new User(id, email, password, name, role, emailVerified);
  }
}
