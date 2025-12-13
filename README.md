# User Service - Java/Spring Boot

A comprehensive user authentication and management service built with **Java 25** and **Spring Boot 4**, architected as a **modular monolith** using **Spring Modulith**.

## Features

| Category                                | Feature             | Description                                    |
|-----------------------------------------|---------------------|------------------------------------------------|
| **User Authentication & Authorization** | User Registration   | Email/password registration system             |
|                                         | Secure Login        | JWT token-based authentication                 |
|                                         | Token Management    | Refresh token rotation with token families     |
|                                         | Logout Options      | Single device and all devices logout           |
|                                         | Access Control      | Role-based permissions (MEMBER/ADMIN)          |
| **User Management**                     | Profile Operations  | Get and update user profiles                   |
|                                         | Password Management | Change password functionality                  |
|                                         | Admin Controls      | Administrative user management                 |
|                                         | Data Validation     | Email uniqueness validation                    |
| **Security**                            | JWT Tokens          | Access and refresh token system                |
|                                         | Password Security   | BCrypt password hashing                        |
|                                         | CORS Support        | Cross-origin resource sharing configuration    |
|                                         | Input Validation    | Bean Validation framework integration          |
|                                         | Error Handling      | Global exception handling                      |
| **Database**                            | Database Engine     | PostgreSQL with Spring Data JPA                |
|                                         | Primary Keys        | UUID-based primary keys                        |
|                                         | Auditing            | Automatic creation and modification timestamps |
|                                         | Migrations          | Automated database migrations with Flyway      |

## Tech Stack

- **Java 25** with Virtual Threads (Project Loom)
- **Spring Boot 4.0** with Spring Framework 7
- **Spring Modulith 2.0** for modular architecture
- **Spring Security 7**
- **Spring Data JPA**
- **PostgreSQL 17**
- **gRPC** alongside REST APIs
- **JWT (JJWT 0.13)**
- **Gradle 8+**
- **Docker & Docker Compose**
- **Swagger/OpenAPI 3** (springdoc-openapi)

## Quick Start

### Prerequisites

- Java 25
- Gradle 8+
- PostgreSQL 17+
- Docker & Docker Compose (optional)

### Environment Variables

Copy `.env.example` to `.env` and update the values:

```bash
# Database
DATABASE_URL=jdbc:postgresql://localhost:54321/timor_users
DATABASE_USERNAME=timor_users_rw
DATABASE_PASSWORD=Timor_password1

# JWT (must be at least 64 bytes for HS512)
JWT_ACCESS_SECRET=your-super-secret-jwt-access-key-here-must-be-at-least-64-bytes-long
JWT_REFRESH_SECRET=your-super-secret-jwt-refresh-key-here-must-be-at-least-64-bytes-long
JWT_ACCESS_EXPIRES_IN=15m
JWT_REFRESH_EXPIRES_IN=7d

# CORS
CLIENT_URL=http://localhost:3000
```

### Running with Docker

```bash
# Start all services
docker compose up -d

docker compose up --build

# Check logs
docker compose logs -f user-service

# Stop services
docker compose down
```

### Running Locally

```bash
# Build the application
./gradlew build

# Run the application
./gradlew bootRun

# Or run with specific profile
./gradlew bootRun --args='--spring.profiles.active=local'
```

### Health Check

```bash
curl http://localhost:3001/health
```

## API Documentation

### Swagger UI

Access the interactive API documentation at:

- **Local**: http://localhost:3001/swagger-ui.html
- **Docker**: http://localhost:3001/swagger-ui.html

### OpenAPI Specification

Download the OpenAPI spec at:

- http://localhost:3001/v3/api-docs

## Default Users

| Role   | Email           | Password    |
|--------|-----------------|-------------|
| Admin  | admin@timor.com | Admin12345! |
| Member | demo@timor.com  | Demo12345!  |

## API Endpoints

### Authentication

- `POST /api/users/auth/register` - User registration
- `POST /api/users/auth/login` - User login
- `POST /api/users/auth/refresh` - Refresh tokens
- `POST /api/users/auth/logout` - Logout all devices
- `POST /api/users/auth/logout-single` - Logout current device

### User Management

- `GET /api/users/me` - Get current user profile
- `PATCH /api/users/me` - Update current user profile
- `PATCH /api/users/me/password` - Change current user password

### Admin Endpoints

- `GET /api/users/{userId}` - Get user by ID (Admin only)
- `PATCH /api/users/{userId}` - Update user by ID (Admin only)
- `PATCH /api/users/{userId}/password` - Change user password (Admin only)

### Health Check

- `GET /health` - Health check endpoint

## API Usage Examples

### Register a new user

```bash
curl -X POST http://localhost:3001/api/users/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "Password12345!",
    "name": "John Doe"
  }'
```

### Login

```bash
curl -X POST http://localhost:3001/api/users/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "Password12345!"
  }'
```

### Get current user profile

```bash
curl -X GET http://localhost:3001/api/users/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### Update profile

```bash
curl -X PATCH http://localhost:3001/api/users/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Smith",
    "email": "john.smith@example.com"
  }'
```

### Refresh tokens

```bash
curl -X POST http://localhost:3001/api/users/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN"
  }'
```

## Development

### Building and Testing

```bash
# Run tests
./gradlew test

# Run tests with coverage
./gradlew test jacocoTestReport

# Build without tests
./gradlew build -x test

# Create production JAR
./gradlew bootJar
```

### Code Quality

```bash
# Apply code formatting (Palantir Java Format)
./gradlew spotlessApply

# Check code formatting
./gradlew spotlessCheck

# Verify module architecture
./gradlew test --tests "org.nkcoder.ModulithArchitectureTest"

# Generate dependency report
./gradlew dependencyInsight --dependency <dependency-name>
```

## Project Structure

The application uses **Spring Modulith** for a modular monolith architecture:

```
src/main/java/org/nkcoder/
├── Application.java                 # Bootstrap (@Modulith entry point)
│
├── user/                            # User Module
│   ├── package-info.java            # @ApplicationModule definition
│   ├── interfaces/rest/             # REST controllers
│   │   ├── AuthController.java
│   │   ├── UserController.java
│   │   └── AdminUserController.java
│   ├── application/service/         # Application services
│   │   ├── AuthApplicationService.java
│   │   └── UserApplicationService.java
│   ├── domain/                      # Domain layer
│   │   ├── model/                   # Aggregates, entities, value objects
│   │   ├── service/                 # Domain services (ports)
│   │   └── repository/              # Repository interfaces (ports)
│   └── infrastructure/              # Infrastructure layer
│       ├── persistence/             # JPA entities & repository adapters
│       └── security/                # JWT filter, SecurityConfig
│
├── notification/                    # Notification Module
│   ├── package-info.java
│   ├── NotificationService.java     # Public API
│   └── application/                 # Event listeners
│
├── shared/                          # Shared Kernel (OPEN module)
│   ├── kernel/
│   │   ├── domain/event/            # Domain events
│   │   └── exception/               # Common exceptions
│   └── local/rest/                  # ApiResponse, GlobalExceptionHandler
│
└── infrastructure/                  # Infrastructure Module (OPEN module)
    └── config/                      # CORS, OpenAPI, JPA auditing
```

### Module Dependencies

```
notification ──→ shared ←── user
                   ↑
              infrastructure
```

- Modules communicate via **domain events** (not direct calls)
- `shared` and `infrastructure` are OPEN modules (accessible by all)

## Security Features

- **JWT Authentication**: Stateless authentication with HS512 algorithm
- **Token Rotation**: Refresh tokens rotated on each use
- **Token Families**: Grouped tokens for multi-device support
- **Password Security**: BCrypt hashing with configurable strength
- **Input Validation**: Bean Validation annotations
- **CORS**: Configurable cross-origin resource sharing
- **Audit Trail**: Automatic creation and modification timestamps

## Monitoring & Observability

- **Health Checks**: Spring Boot Actuator endpoints
- **Structured Logging**: SLF4J with Logback
- **API Documentation**: OpenAPI 3 with Swagger UI
- **Error Handling**: Global exception handling with proper HTTP status codes

## Configuration

### Application Profiles

- `local`: Local development with Docker Compose for PostgreSQL
- `dev`: Development environment with external database
- `prod`: Production environment
- `test`: Testing with TestContainers

### Key Configuration Properties

```yaml
# JWT Configuration
jwt:
  secret:
    access: ${JWT_ACCESS_SECRET}    # Must be 64+ bytes for HS512
    refresh: ${JWT_REFRESH_SECRET}  # Must be 64+ bytes for HS512
  expiration:
    access: ${JWT_ACCESS_EXPIRES_IN:15m}
    refresh: ${JWT_REFRESH_EXPIRES_IN:7d}

# Database Configuration
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
```

## References

- [Spring Modulith Documentation](https://docs.spring.io/spring-modulith/reference/)
- [Spring Boot 4.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)
- [Implementing Domain Driven Design with Spring](https://github.com/maciejwalkowiak/implementing-ddd-with-spring-talk)

## License

This project is licensed under the MIT License.