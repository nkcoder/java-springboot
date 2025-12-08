package org.nkcoder.shared.local.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to inject the current authenticated user's ID into controller method parameters.
 *
 * <p>Usage: {@code public ResponseEntity<?> getMe(@CurrentUser UUID userId)}
 *
 * <p>The userId is extracted from the request attributes set by JwtAuthenticationFilter. If no
 * authenticated user is found, the resolver returns null (let Spring Security handle unauthorized
 * access via security configuration).
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {}
