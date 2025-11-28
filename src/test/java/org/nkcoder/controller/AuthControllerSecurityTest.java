package org.nkcoder.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nkcoder.service.AuthService;
import org.nkcoder.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = AuthController.class)
@DisplayName("AuthController Security Tests")
@ActiveProfiles("test")
public class AuthControllerSecurityTest extends BaseSecurityControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("Should allow access to public endpoints without authentication")
    void shouldAllowPublicEndpointsWithoutAuth() throws Exception {
        // Register endpoint should be accessible without authentication
        mockMvc.perform(get("/api/users/auth/register"))
                .andExpect(status().isMethodNotAllowed()); // POST expected, but no 401/403

        // Login endpoint should be accessible without authentication
        mockMvc.perform(get("/api/users/auth/login"))
                .andExpect(status().isMethodNotAllowed()); // POST expected, but no 401/403
    }
}
