package org.nkcoder.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI userServiceOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("User Service API")
                .description("User authentication and management service for Timor platform")
                .version("v0.1.0")
                .contact(
                    new Contact()
                        .name("Development Team")
                        .email("dev@timor.com")
                        .url("https://github.com/timor/user-service"))
                .license(
                    new License().name("ISC License").url("https://opensource.org/licenses/ISC")))
        .servers(
            List.of(
                new Server().url("http://localhost:3001").description("Development server"),
                new Server().url("https://api.timor.com").description("Production server")))
        .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
        .components(
            new io.swagger.v3.oas.models.Components()
                .addSecuritySchemes(
                    "Bearer Authentication",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT token for authentication")));
  }
}
