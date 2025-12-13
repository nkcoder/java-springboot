package org.nkcoder.user.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.nkcoder.shared.kernel.exception.AuthenticationException;
import org.nkcoder.user.domain.service.TokenGenerator;
import org.nkcoder.user.domain.service.TokenGenerator.AccessTokenClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final TokenGenerator tokenGenerator;

    public JwtAuthenticationFilter(TokenGenerator tokenGenerator) {
        this.tokenGenerator = tokenGenerator;
    }

    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain)
            throws ServletException, IOException {
        logger.debug("Processing authentication for request: {}", request.getRequestURI());

        extractTokenFromRequest(request).ifPresent(token -> {
            try {
                AccessTokenClaims claims = tokenGenerator.validateAccessToken(token);

                // Create authorities
                List<GrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + claims.role().name()));

                UserDetails userDetails = new User(claims.email().value(), "", authorities);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set custom attributes
                request.setAttribute(ATTRIBUTE_USER_ID, claims.userId().value());
                request.setAttribute(ATTRIBUTE_EMAIL, claims.email().value());
                request.setAttribute(ATTRIBUTE_ROLE, claims.role());

                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.debug(
                        "Set authentication for userId: {}", claims.userId().value());
            } catch (AuthenticationException e) {
                logger.error("JWT token validation failed: {}", e.getMessage());
            } catch (IllegalArgumentException e) {
                logger.error("JWT token parsing failed: {}", e.getMessage());
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
