package org.nkcoder.infrastructure.resolver;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.nkcoder.shared.local.annotation.CurrentUser;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Resolves method parameters annotated with {@link CurrentUser} by extracting the user ID from
 * request attributes set by {@link org.nkcoder.infrastructure.security.JwtAuthenticationFilter}
 */
@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

  private static final String USER_ID_ATTRIBUTE = "userId";

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(CurrentUser.class)
        && parameter.getParameterType().equals(UUID.class);
  }

  @Override
  public Object resolveArgument(
      @NotNull MethodParameter parameter,
      ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory) {
    HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
    if (request == null) {
      return null;
    }

    Object userId = request.getAttribute(USER_ID_ATTRIBUTE);
    if (userId instanceof UUID) {
      return userId;
    }

    return null;
  }
}
