package org.nkcoder.infrastructure.security;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.apache.logging.log4j.util.Strings;
import org.nkcoder.shared.local.rest.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    private static final String CONTENT_TYPE_JSON = "application/json";

    private final ObjectMapper objectMapper;

    @Autowired
    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {

        logger.debug("Unauthorized access attempt to: {}", request.getRequestURI());

        response.setContentType(CONTENT_TYPE_JSON);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String errorMessage = determineErrorMessage(authException);
        ApiResponse<Void> apiResponse = ApiResponse.error(errorMessage);

        objectMapper.writeValue(response.getOutputStream(), apiResponse);
        response.getOutputStream().flush();
        // Do NOT close the stream - let the servlet container manage it
    }

    private String determineErrorMessage(AuthenticationException authException) {
        if (authException.getCause() instanceof ExpiredJwtException) {
            return "Token has expired";
        }

        if (Strings.isNotBlank(authException.getMessage())) {
            return "Authentication required: " + authException.getMessage();
        }

        return "Authentication required";
    }
}
