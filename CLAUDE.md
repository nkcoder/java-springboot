# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Java 21 Spring Boot 3.5.3 microservice implementing user authentication and management with both REST and gRPC APIs. The service features JWT authentication with refresh token rotation, token families for multi-device support, role-based access control (ADMIN/MEMBER), and PostgreSQL persistence with Flyway migrations.

## Build & Development Commands

### Building
```bash
# Full build with tests
./gradlew build

# Build without tests
./gradlew build -x test

# Create production JAR
./gradlew bootJar
```

### Running
```bash
# Run locally (port 3001 REST, 9090 gRPC)
./gradlew bootRun

# Run with specific profile
./gradlew bootRun --args='--spring.profiles.active=local'

# Run via automation script
./auto/run

# Run with Docker Compose
docker compose up -d
```

### Testing
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "org.nkcoder.controller.AuthControllerTest"

# Run specific test method
./gradlew test --tests "org.nkcoder.controller.AuthControllerTest.shouldRegisterUser"

# Test with coverage
./gradlew test jacocoTestReport

# Run tests via automation script
./auto/test
```

### Code Formatting
```bash
# Apply Spotless formatting (Palantir Java Format)
./gradlew spotlessApply

# Check formatting without applying
./gradlew spotlessCheck

# Format via automation script
./auto/format
```

### gRPC Protobuf Generation
```bash
# Generate Java classes from proto files
./gradlew generateProto

# Use automation script
./auto/buf-gen
```

## Architecture

### Layered Structure

The codebase follows a strict layered architecture:

```
Controller → Service → Repository → Entity
     ↓          ↓
   DTO      Mapper
```

**Key Directories** (under `src/main/java/org/nkcoder/`):
- `controller/` - REST endpoints (AuthController, UserController, HealthController)
- `service/` - Business logic (AuthService, UserService)
- `repository/` - JPA repositories (UserRepository, RefreshTokenRepository)
- `entity/` - JPA entities (User, RefreshToken)
- `dto/` - Data Transfer Objects organized by feature (auth/, user/, common/)
- `mapper/` - Entity-to-DTO mappers (UserMapper)
- `security/` - Security components (JwtAuthenticationFilter, JwtAuthenticationEntryPoint)
- `config/` - Configuration classes (SecurityConfig, JwtProperties, OpenApiConfig, etc.)
- `util/` - Utilities (JwtUtil)
- `grpc/` - gRPC service implementations (AuthGrpcService, GrpcMapper)
- `exception/` - Custom exceptions (AuthenticationException, ValidationException, ResourceNotFoundException)

### Security & Authentication

**JWT Implementation**: Dual-token system with separate HMAC-SHA512 keys:
- **Access tokens**: 15-minute expiration, contains userId, email, role
- **Refresh tokens**: 7-day expiration, contains userId, tokenFamily

**Token Rotation Mechanism**:
- Each token refresh generates new access + refresh tokens
- New refresh token maintains same `tokenFamily` (UUID) for breach detection
- Old refresh token is deleted from database (prevents replay attacks)

**Token Family Pattern**:
- Each login session creates a new token family
- All refresh operations within a session share the same family
- If an invalid/expired token from a family is used, entire family is deleted (logout all devices)

**Multi-Device Logout**:
- `POST /api/users/auth/logout` - Deletes entire token family (all devices)
- `POST /api/users/auth/logout-single` - Deletes only current refresh token (single device)

**JWT Authentication Flow**:
1. JwtAuthenticationFilter extracts token from `Authorization: Bearer {token}` header
2. Token validated and claims extracted via JwtUtil
3. UsernamePasswordAuthenticationToken created with role authorities (ROLE_MEMBER/ROLE_ADMIN)
4. Context stored in SecurityContextHolder
5. Request attributes set: userId, email, role (accessible in controllers)

**Security Configuration**:
- Stateless session management (no server-side session)
- BCrypt password encoding
- CORS enabled for CLIENT_URL (default: http://localhost:3000)
- Public endpoints: /api/users/auth/*, /health, /swagger-ui/*, /api-docs/*
- Protected endpoints: /api/users/me*, require authentication
- Admin endpoints: /api/users/{userId}*, require ROLE_ADMIN

### Data Access Patterns

**Entities**:
- `User`: UUID primary key, unique email index, one-to-many with RefreshToken
- `RefreshToken`: UUID primary key, unique token, many-to-one with User, indexed on token/tokenFamily/userId

**Audit Timestamps**:
- Entities use `@CreatedDate` and `@LastModifiedDate` from Spring Data JPA auditing
- Enabled via `@EnableJpaAuditing` in JpaAuditingConfig

**Repository Methods**:
- Custom @Query methods for complex operations (e.g., `updateLastLoginAt`, `findByEmailExcludingId`)
- @Modifying queries for deletes/updates (e.g., `deleteByTokenFamily`, `deleteExpiredTokens`)
- Boolean existence checks for validation (e.g., `existsByEmail`)

**Transaction Management**:
- Service classes use `@Transactional` at class level
- Query methods marked with `@Transactional(readOnly=true)` for optimization
- Repository operations participate in service-level transactions

### API Design

**REST Endpoints** (port 3001):
```
POST   /api/users/auth/register        - User registration
POST   /api/users/auth/login           - User login
POST   /api/users/auth/refresh         - Refresh access token
POST   /api/users/auth/logout          - Logout all devices
POST   /api/users/auth/logout-single   - Logout current device
GET    /api/users/me                   - Get current user
PATCH  /api/users/me                   - Update current user
PATCH  /api/users/me/password          - Change password
GET    /api/users/{userId}             - Get user (admin only)
PATCH  /api/users/{userId}             - Update user (admin only)
PATCH  /api/users/{userId}/password    - Reset password (admin only)
```

**gRPC Endpoints** (port 9090):
- `AuthService.Register` - User registration
- `AuthService.Login` - User login
- Proto file: `src/main/proto/auth.proto`

**Response Wrapping**: All responses use `ApiResponse<T>` record:
```json
{
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2025-11-28T10:30:45.123"
}
```

**DTO Patterns**:
- Request DTOs are immutable records with Jakarta validation annotations
- Response DTOs are records (UserResponse, AuthResponse, AuthTokens)
- Validation errors return field-level details via MethodArgumentNotValidException handling

### Error Handling

**Global Exception Handler** (`GlobalExceptionHandler.java`):
- Maps custom exceptions to appropriate HTTP status codes
- ValidationException → 400 Bad Request
- ResourceNotFoundException → 404 Not Found
- AuthenticationException → 401 Unauthorized
- AccessDeniedException → 403 Forbidden
- Bean validation errors → 400 with field details
- Generic exceptions → 500 Internal Server Error

**Custom Exceptions** (all extend RuntimeException):
- `AuthenticationException` - Authentication failures (invalid credentials, expired tokens)
- `ValidationException` - Business validation failures (duplicate email, password mismatch)
- `ResourceNotFoundException` - Entity not found by ID

### Configuration Management

**Profiles**:
- `local` - Local development with Docker Compose
- `docker` - Docker container environment
- `prod` - Production with environment variables
- `test` - Test profile with in-memory/test database

**Environment Variables** (required for production):
```bash
DATABASE_URL=jdbc:postgresql://host:port/database
DATABASE_USERNAME=username
DATABASE_PASSWORD=password
JWT_ACCESS_SECRET=<min 64 bytes for HS512>
JWT_REFRESH_SECRET=<min 64 bytes for HS512>
JWT_ACCESS_EXPIRES_IN=15m
JWT_REFRESH_EXPIRES_IN=7d
CLIENT_URL=http://localhost:3000
```

**Configuration Binding**:
- JWT settings bound via `@ConfigurationProperties` in JwtProperties.java
- Nested structure: jwt.secret.access, jwt.secret.refresh, jwt.expiration.access, etc.
- Use `${VARIABLE:defaultValue}` syntax in application.yml

### Database Migrations

**Flyway Migration Files** (in `src/main/resources/db/migration/`):
- `V1.1__create_tables.sql` - Initial schema (users, refresh_tokens tables)
- `V1.2__seeding_users.sql` - Seed data (admin@timor.com, demo@timor.com)
- `V1.3__update_users_role.sql` - Schema updates

**Migration Best Practices**:
- Never modify existing migrations (create new ones)
- Use sequential versioning: V1.1, V1.2, V1.3, etc.
- Validate migrations with `validate-on-migrate: true`
- Baseline existing databases with `baseline-on-migrate: true`

### gRPC Integration

**Dual Protocol Support**:
- REST API (port 3001) uses Undertow server
- gRPC API (port 9090) uses Netty server
- Both protocols share same business logic (AuthService)
- Protocol-specific marshaling via GrpcMapper

**Proto Files**:
- Located in `src/main/proto/`
- Generated classes: `buf-gen/generated/sources/proto/main/java/`
- Regenerate after proto changes: `./gradlew generateProto`

**gRPC Service Implementation**:
- Classes in `grpc/` package extend generated service base classes
- Decorated with `@GrpcService` annotation
- Use `StreamObserver<T>` for async responses
- Map exceptions to gRPC Status codes

### Testing Infrastructure

**Test Types**:
- Unit tests: `@WebMvcTest` for controllers, `@MockBean` for services
- Integration tests: `@SpringBootTest` with TestContainers for PostgreSQL
- Security tests: Use `@WithMockUser` or custom security setup

**Base Test Classes**:
- `BaseControllerTest` - Base for controller unit tests (MockMvc setup)
- `BaseSecurityControllerTest` - Base with security context
- `IntegrationTestingBase` - Base for integration tests (TestContainers)

**Test Configuration**:
- `TestConfig.java` provides test-specific beans
- `application-test.yml` configures test profile
- TestContainers automatically manages PostgreSQL instance

**Default Test Users** (seeded in test profile):
- admin@timor.com / Admin12345! (ROLE_ADMIN)
- demo@timor.com / Demo12345! (ROLE_MEMBER)

## Code Style & Conventions

**Formatting**:
- Palantir Java Format via Spotless plugin
- 2-space indentation (no tabs)
- Auto-import ordering and unused import removal
- Leading tabs converted to spaces
- Trailing whitespace trimmed
- Files end with newline

**Naming Conventions**:
- Classes: PascalCase (e.g., AuthService, UserController)
- Methods/Fields: camelCase (e.g., findByEmail, refreshToken)
- Constants: UPPER_SNAKE_CASE (e.g., DEFAULT_ROLE)
- Packages: lowercase (e.g., org.nkcoder.service)

**Package Organization**:
- DTOs grouped by feature: `dto/auth/`, `dto/user/`, `dto/common/`
- One entity per file
- One controller per resource domain (Auth, User)

**Dependency Injection**:
- Prefer constructor injection over field injection
- Use `@Autowired` on constructor (optional in single-constructor classes)
- Inject interfaces, not implementations (e.g., UserRepository, not JpaRepository)

**Validation**:
- Use Jakarta validation annotations on DTOs (@NotBlank, @Email, @Size, @Pattern)
- Business validation in service layer (throw ValidationException)
- Method-level validation with @Valid on controller parameters

**Logging**:
- Use SLF4J with class-level static final logger: `private static final Logger log = LoggerFactory.getLogger(ClassName.class);`
- Log levels: DEBUG for app code, INFO for Spring/Hibernate, ERROR for exceptions
- Log format includes timestamp, thread, level, logger name, message

**Error Handling**:
- Throw custom exceptions from services (AuthenticationException, ValidationException, ResourceNotFoundException)
- Let GlobalExceptionHandler convert to HTTP responses
- Include descriptive error messages
- Never expose stack traces or sensitive data in production responses

## Important Implementation Notes

1. **JWT Secret Length**: Must be at least 64 bytes for HS512 algorithm (enforced in JwtUtil constructor)

2. **Email Normalization**: All emails converted to lowercase before storage/comparison

3. **Token Family Tracking**: Critical for security - same family UUID must persist across refresh operations within a session

4. **Lazy Loading**: RefreshToken relationship on User is FetchType.LAZY to prevent N+1 queries

5. **Transaction Boundaries**: Service methods are transactional; repository method execution participates in service transaction

6. **Password Validation**: Custom @Pattern regex enforces lowercase, uppercase, and digit requirements

7. **Role-Based Access**: Controllers use @PreAuthorize("hasRole('ADMIN')") for admin-only endpoints

8. **Database Indexes**: Critical indexes on users.email, refresh_tokens.token, refresh_tokens.token_family, refresh_tokens.user_id

9. **Actuator Endpoints**: Health checks, metrics, and Prometheus scraping on /actuator/* (configured in application.yml)

10. **API Documentation**: Swagger UI available at http://localhost:3001/swagger-ui.html (configured via OpenApiConfig)

## Common Development Tasks

**Adding a New Endpoint**:
1. Create request/response DTOs in appropriate `dto/` subdirectory
2. Add method to service layer with business logic
3. Add controller method with @GetMapping/@PostMapping/@PatchMapping
4. Add validation annotations to request DTO
5. Add test cases in controller test class
6. Run `./gradlew test` to verify

**Database Schema Change**:
1. Create new migration file: `V{next_version}__{description}.sql` in `src/main/resources/db/migration/`
2. Update entity class if needed
3. Run `./gradlew bootRun` to apply migration
4. Verify with database client or integration test

**Adding gRPC Endpoint**:
1. Update `src/main/proto/auth.proto` with new RPC method
2. Run `./gradlew generateProto` to regenerate Java classes
3. Implement method in appropriate gRPC service (e.g., AuthGrpcService)
4. Add mapping logic in GrpcMapper if needed
5. Test with gRPC client (e.g., grpcurl)

**Password Requirements**: Must contain at least one lowercase letter, one uppercase letter, and one digit (enforced via @Pattern regex)

**Role Assignment**: New users default to MEMBER role; ADMIN role must be explicitly assigned or seeded

