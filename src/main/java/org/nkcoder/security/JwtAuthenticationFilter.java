package org.nkcoder.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.nkcoder.enums.Role;
import org.nkcoder.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";
  private static final String ATTRIBUTE_USER_ID = "userId";
  private static final String ATTRIBUTE_ROLE = "role";
  private static final String ATTRIBUTE_EMAIL = "email";

  private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

  private final JwtUtil jwtUtil;

  @Autowired
  public JwtAuthenticationFilter(JwtUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  @Override
  protected void doFilterInternal(
      @NotNull HttpServletRequest request,
      @NotNull HttpServletResponse response,
      @NotNull FilterChain filterChain)
      throws ServletException, IOException {
    logger.debug("Processing authentication for request: {}", request.getRequestURI());

    extractTokenFromRequest(request)
        .ifPresent(
            token -> {
              try {
                Claims claims = jwtUtil.validateAccessToken(token);

                UUID userId = UUID.fromString(claims.getSubject());
                String email = claims.get("email", String.class);
                String roleString = claims.get("role", String.class);
                Role role = Role.valueOf(roleString);

                // Create authorities
                List<GrantedAuthority> authorities =
                    List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));

                UserDetails userDetails = new User(email, "", authorities);
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

                authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));

                // Set custom attributes
                request.setAttribute(ATTRIBUTE_USER_ID, userId);
                request.setAttribute(ATTRIBUTE_EMAIL, email);
                request.setAttribute(ATTRIBUTE_ROLE, role);

                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.debug("Set authentication for userId: {}", userId);
              } catch (ExpiredJwtException e) {
                logger.error("JWT token expired: {}", e.getMessage());
              } catch (MalformedJwtException e) {
                logger.error("Malformed JWT token: {}", e.getMessage());
              } catch (UnsupportedJwtException e) {
                logger.error("Unsupported JWT token: {}", e.getMessage());
              } catch (SecurityException e) {
                logger.error("JWT signature validation failed: {}", e.getMessage());
              } catch (IllegalArgumentException e) {
                logger.error("JWT token compact of handler are invalid: {}", e.getMessage());
              }
            });

    filterChain.doFilter(request, response);
  }

  private Optional<String> extractTokenFromRequest(HttpServletRequest request) {
    return Optional.ofNullable(request.getHeader(AUTHORIZATION_HEADER))
        .filter(StringUtils::hasText)
        .filter(token -> token.startsWith(BEARER_PREFIX))
        .map(token -> token.substring(BEARER_PREFIX.length()));
  }
}
