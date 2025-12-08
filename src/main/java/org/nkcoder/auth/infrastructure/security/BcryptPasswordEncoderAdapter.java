package org.nkcoder.auth.infrastructure.security;

import org.nkcoder.auth.domain.model.HashedPassword;
import org.nkcoder.auth.domain.service.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/** BCrypt implementation of the PasswordEncoder domain service. */
@Component
public class BcryptPasswordEncoderAdapter implements PasswordEncoder {

  private static final int BCRYPT_STRENGTH = 12;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;

  public BcryptPasswordEncoderAdapter() {
    this.bCryptPasswordEncoder = new BCryptPasswordEncoder(BCRYPT_STRENGTH);
  }

  @Override
  public HashedPassword encode(String rawPassword) {
    return HashedPassword.of(bCryptPasswordEncoder.encode(rawPassword));
  }

  @Override
  public boolean matches(String rawPassword, HashedPassword hashedPassword) {
    return bCryptPasswordEncoder.matches(rawPassword, hashedPassword.value());
  }
}
