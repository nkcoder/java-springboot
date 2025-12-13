package org.nkcoder.user.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.nkcoder.infrastructure.config.IntegrationTest;
import org.nkcoder.user.application.dto.response.AuthResult;
import org.nkcoder.user.application.service.AuthApplicationService;
import org.nkcoder.user.application.service.UserApplicationService;
import org.nkcoder.user.domain.model.UserRole;
import org.nkcoder.user.domain.service.TokenGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Use @SpringBootTest to load the full context to test the authentication of endpoints. Because @WebMvcTest won't load
 * SecurityConfig and JwtAuthenticationEntryPoint.
 */
@AutoConfigureMockMvc
@IntegrationTest
@DisplayName("AuthController Security Tests")
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthApplicationService authService;

    @MockitoBean
    private UserApplicationService userService;

    @MockitoBean
    private TokenGenerator tokenGenerator;

    @Nested
    @DisplayName("Public Endpoints")
    class PublicEndpoints {

        @Test
        @DisplayName("register endpoint is accessible without authentication")
        void registerIsPublic() throws Exception {
            AuthResult authResult = createAuthResult();
            given(authService.register(any())).willReturn(authResult);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                "email": "test@example.com",
                                 "password": "Password123",
                                 "name": "Test User",
                                 "role": "MEMBER"
                                }
                                """))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("login endpoint is accessible without authentication")
        void loginIsPublic() throws Exception {
            AuthResult authResult = createAuthResult();
            given(authService.login(any())).willReturn(authResult);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "email": "test@example.com",
                                  "password": "Password123"
                                }
                                """))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("refresh endpoint is accessible without authentication")
        void refreshIsPublic() throws Exception {
            AuthResult authResult = createAuthResult();
            given(authService.refreshTokens(any())).willReturn(authResult);

            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "refreshToken": "some-refresh-token"
                                }
                                """))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Protected Endpoints")
    class ProtectedEndpoints {

        @Test
        @DisplayName("GET /api/users/me returns 401 without token")
        void getMeRequiresAuth() throws Exception {
            mockMvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("PATCH /api/users/me returns 401 without token")
        void updateMeRequiresAuth() throws Exception {
            mockMvc.perform(patch("/api/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                 {"name": "New Name"}
                                """))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("PATCH /api/users/me/password returns 401 without token")
        void changePasswordRequiresAuth() throws Exception {
            mockMvc.perform(patch("/api/users/me/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "currentPassword": "Old123",
                                  "newPassword": "New123",
                                  "confirmPassword": "New123"
                                }
                                """))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Admin Endpoints")
    class AdminEndpoints {

        @Test
        @DisplayName("GET /api/admin/users/{id} returns 401 without token")
        void getUserByIdRequiresAuth() throws Exception {
            mockMvc.perform(get("/api/admin/users/{userId}", UUID.randomUUID())).andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("PATCH /api/admin/users/{id} returns 401 without token")
        void updateUserRequiresAuth() throws Exception {
            mockMvc.perform(patch("/api/admin/users/{userId}", UUID.randomUUID())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {"name": "Admin Update"}
                                """))
                    .andExpect(status().isUnauthorized());
        }
    }

    private AuthResult createAuthResult() {
        return new AuthResult(UUID.randomUUID(), "test@example.com", UserRole.MEMBER, "access-token", "refresh-token");
    }
}
