package org.nkcoder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
public class TestConfig {
  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    return new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .setPropertyNamingStrategy((PropertyNamingStrategies.LOWER_CAMEL_CASE));
  }

  @TestConfiguration
  @EnableWebSecurity
  public static class TestSecurityConfig {
    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
      return http.csrf(AbstractHttpConfigurer::disable)
          .authorizeHttpRequests(
              authorize ->
                  authorize
                      .requestMatchers(
                          "/api/users/auth/register",
                          "/api/users/auth/login",
                          "/api/users/auth/refresh",
                          "/api/users/auth/logout",
                          "/api/users/auth/logout-single")
                      .permitAll()
                      .anyRequest()
                      .authenticated())
          .build();
    }
  }
}
