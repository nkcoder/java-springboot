## Summary

This plan will refactor your application from a layered architecture to Domain-Driven Design with:

1. Two bounded contexts: auth/ and user/ - each with full DDD layers (domain → application →
   infrastructure → interfaces)
2. Separate User representations:
    - AuthUser in auth context: id, email, password, role (focused on authentication)
    - User in user context: id, email, name, role, profile data (focused on management)
    - Both map to the same users database table
3. Clean architecture per context:
    - Domain layer: Pure business logic, no framework dependencies
    - Application layer: Use cases, commands/queries
    - Infrastructure layer: JPA, external adapters
    - Interfaces layer: REST controllers, gRPC
4. 6 migration phases - incremental approach, tests run after each phase
5. Test structure mirrors source - organized by domain and layer

This structure makes it straightforward to extract each bounded context into a microservice later -
just pull out the auth/ or user/ package with minimal changes.

## DDD Refactoring Plan

Overview

Refactor from layered architecture to Domain-Driven Design with:

- Full DDD tactical patterns (domain/application/infrastructure/interfaces layers)
- Separate domain models per bounded context (Auth and User have their own User representations)
- Mirrored test structure

Target Package Structure

org.nkcoder/
├── auth/ # Auth Bounded Context
│ ├── domain/
│ │ ├── model/
│ │ │ ├── AuthUser.java # Auth's user (id, email, password, role)
│ │ │ ├── RefreshToken.java
│ │ │ ├── TokenFamily.java # Value object
│ │ │ └── TokenPair.java # Value object
│ │ ├── repository/
│ │ │ ├── AuthUserRepository.java # Port (interface)
│ │ │ └── RefreshTokenRepository.java
│ │ ├── service/
│ │ │ ├── PasswordEncoder.java # Domain service interface
│ │ │ └── TokenGenerator.java # Domain service interface
│ │ └── event/
│ │ ├── UserRegisteredEvent.java
│ │ └── UserLoggedInEvent.java
│ │
│ ├── application/
│ │ ├── service/
│ │ │ └── AuthApplicationService.java
│ │ ├── dto/
│ │ │ ├── command/ # RegisterCommand, LoginCommand, etc.
│ │ │ └── response/ # AuthResult, TokenResponse
│ │ ├── mapper/
│ │ │ └── AuthDtoMapper.java
│ │ └── port/
│ │ └── UserContextPort.java # Cross-context communication
│ │
│ ├── infrastructure/
│ │ ├── persistence/
│ │ │ ├── entity/AuthUserJpaEntity.java
│ │ │ ├── repository/AuthUserJpaRepository.java
│ │ │ ├── repository/AuthUserRepositoryAdapter.java
│ │ │ └── mapper/AuthUserPersistenceMapper.java
│ │ ├── security/
│ │ │ ├── JwtTokenGenerator.java
│ │ │ ├── BcryptPasswordEncoder.java
│ │ │ └── JwtProperties.java
│ │ └── adapter/
│ │ └── UserContextAdapter.java
│ │
│ └── interfaces/
│ ├── rest/
│ │ ├── AuthController.java
│ │ ├── request/ # RegisterRequest, LoginRequest
│ │ └── response/AuthApiResponse.java
│ └── grpc/
│ ├── AuthGrpcService.java
│ └── GrpcAuthMapper.java
│
├── user/ # User Bounded Context
│ ├── domain/
│ │ ├── model/
│ │ │ ├── User.java # User's user (id, email, name, profile)
│ │ │ ├── UserId.java
│ │ │ ├── UserName.java # Value object
│ │ │ └── UserRole.java
│ │ ├── repository/
│ │ │ └── UserRepository.java # Port (interface)
│ │ └── event/
│ │ └── UserProfileUpdatedEvent.java
│ │
│ ├── application/
│ │ ├── service/
│ │ │ ├── UserQueryService.java
│ │ │ └── UserCommandService.java
│ │ ├── dto/
│ │ │ ├── command/ # UpdateProfileCommand, ChangePasswordCommand
│ │ │ └── response/UserDto.java
│ │ ├── mapper/
│ │ │ └── UserDtoMapper.java
│ │ └── port/
│ │ └── AuthContextPort.java # For password operations
│ │
│ ├── infrastructure/
│ │ ├── persistence/
│ │ │ ├── entity/UserJpaEntity.java
│ │ │ ├── repository/UserJpaRepository.java
│ │ │ ├── repository/UserRepositoryAdapter.java
│ │ │ └── mapper/UserPersistenceMapper.java
│ │ └── adapter/
│ │ └── AuthContextAdapter.java
│ │
│ └── interfaces/
│ └── rest/
│ ├── UserController.java
│ ├── AdminUserController.java
│ ├── request/ # UpdateProfileRequest, ChangePasswordRequest
│ └── response/UserApiResponse.java
│
├── shared/ # Shared Kernel
│ ├── domain/
│ │ ├── valueobject/
│ │ │ ├── Email.java # Shared value object
│ │ │ └── AggregateRoot.java
│ │ └── event/
│ │ ├── DomainEvent.java
│ │ └── DomainEventPublisher.java
│ │
│ ├── application/
│ │ └── validation/
│ │ ├── PasswordMatch.java
│ │ └── PasswordMatchValidator.java
│ │
│ ├── infrastructure/
│ │ └── event/
│ │ └── SpringDomainEventPublisher.java
│ │
│ └── interfaces/
│ └── rest/
│ ├── ApiResponse.java
│ ├── GlobalExceptionHandler.java
│ └── HealthController.java
│
└── infrastructure/ # Cross-cutting
├── config/
│ ├── SecurityConfig.java
│ ├── WebConfig.java
│ ├── OpenApiConfig.java
│ └── JpaAuditingConfig.java
├── security/
│ ├── JwtAuthenticationFilter.java
│ └── JwtAuthenticationEntryPoint.java
└── resolver/
└── CurrentUserArgumentResolver.java

## Domain Model Design

Auth Domain - AuthUser (minimal for authentication)

```java
public class AuthUser {
   private final AuthUserId id;
   private final Email email;
   private HashedPassword password;
   private final AuthRole role;
   private LocalDateTime lastLoginAt;
}
```

User Domain - User (rich for profile management)

```java
public class User extends AggregateRoot<UserId> {
   private final UserId id;
   private Email email;
   private UserName name;
   private final UserRole role;
   private boolean emailVerified;
   private LocalDateTime lastLoginAt;
   private final LocalDateTime createdAt;
   private LocalDateTime updatedAt;
}
```

Database Strategy

- Single users table shared by both contexts
- Auth reads: id, email, password, role, last_login_at
- User reads: id, email, name, role, email_verified, timestamps
- Each context has its own JPA entity mapping the same table

Test Structure

src/test/java/org/nkcoder/
├── auth/
│ ├── domain/model/ # AuthUserTest, RefreshTokenTest, EmailTest
│ ├── application/service/ # AuthApplicationServiceTest
│ ├── infrastructure/ # Repository adapter tests
│ └── interfaces/rest/ # AuthControllerTest
├── user/
│ ├── domain/model/ # UserTest, UserNameTest
│ ├── application/service/ # UserQueryServiceTest, UserCommandServiceTest
│ ├── infrastructure/ # Repository adapter tests
│ └── interfaces/rest/ # UserControllerTest, AdminUserControllerTest
├── shared/
│ └── domain/ # Value object tests
├── integration/
│ ├── AuthFlowIntegrationTest.java
│ └── UserFlowIntegrationTest.java
└── fixture/ # Test factories

Migration Phases

Phase 1: Foundation (No Breaking Changes)

1. Create shared kernel:

- shared/domain/valueobject/Email.java
- shared/domain/valueobject/AggregateRoot.java
- shared/domain/event/DomainEvent.java
- shared/domain/event/DomainEventPublisher.java

2. Create shared interfaces:

- Move ApiResponse to shared/interfaces/rest/
- Move GlobalExceptionHandler to shared/interfaces/rest/

3. Run tests to verify

Phase 2: Auth Domain

1. Create auth domain model:

- auth/domain/model/AuthUser.java
- auth/domain/model/RefreshToken.java (move & refactor)
- auth/domain/model/TokenFamily.java
- auth/domain/repository/AuthUserRepository.java (interface)
- auth/domain/repository/RefreshTokenRepository.java (interface)
- auth/domain/service/PasswordEncoder.java (interface)
- auth/domain/service/TokenGenerator.java (interface)

2. Create auth application layer:

- auth/application/service/AuthApplicationService.java
- auth/application/dto/command/*
- auth/application/dto/response/*

3. Create auth infrastructure:

- auth/infrastructure/persistence/entity/AuthUserJpaEntity.java
- auth/infrastructure/persistence/repository/AuthUserRepositoryAdapter.java
- auth/infrastructure/security/JwtTokenGenerator.java
- auth/infrastructure/security/BcryptPasswordEncoder.java

4. Move AuthController to auth/interfaces/rest/
5. Move gRPC to auth/interfaces/grpc/
6. Migrate auth tests

Phase 3: User Domain

1. Create user domain model:

- user/domain/model/User.java
- user/domain/model/UserId.java
- user/domain/model/UserName.java
- user/domain/repository/UserRepository.java (interface)

2. Create user application layer:

- user/application/service/UserQueryService.java
- user/application/service/UserCommandService.java
- user/application/dto/*

3. Create user infrastructure:

- user/infrastructure/persistence/entity/UserJpaEntity.java
- user/infrastructure/persistence/repository/UserRepositoryAdapter.java

4. Move UserController, AdminUserController to user/interfaces/rest/
5. Migrate user tests

Phase 4: Cross-Context Communication

1. Implement ports and adapters for context communication:

- auth/application/port/UserContextPort.java
- auth/infrastructure/adapter/UserContextAdapter.java
- user/application/port/AuthContextPort.java

2. Add domain events:

- auth/domain/event/UserRegisteredEvent.java
- Event listeners in user context

3. Integration tests for cross-context flows

Phase 5: Infrastructure Consolidation

1. Move configs to infrastructure/config/
2. Move security filters to infrastructure/security/
3. Move resolvers to infrastructure/resolver/

Phase 6: Cleanup

1. Delete old packages: controller/, service/, repository/, entity/, dto/, mapper/, util/,
   validation/,
   annotation/, enums/, exception/
2. Update all imports
3. Update CLAUDE.md documentation
4. Full test suite verification

Key Files to Modify/Create

New Files (Core)

- auth/domain/model/AuthUser.java
- auth/domain/model/RefreshToken.java
- auth/application/service/AuthApplicationService.java
- auth/infrastructure/persistence/entity/AuthUserJpaEntity.java
- user/domain/model/User.java
- user/application/service/UserQueryService.java
- user/application/service/UserCommandService.java
- user/infrastructure/persistence/entity/UserJpaEntity.java
- shared/domain/valueobject/Email.java
- shared/domain/event/DomainEvent.java

Files to Move & Refactor

- entity/User.java → Split into AuthUser and User domain models
- entity/RefreshToken.java → auth/domain/model/RefreshToken.java
- service/AuthService.java → auth/application/service/AuthApplicationService.java
- service/UserService.java → Split into UserQueryService + UserCommandService
- repository/UserRepository.java → Split into domain ports + JPA adapters
- controller/AuthController.java → auth/interfaces/rest/AuthController.java
- controller/UserController.java → user/interfaces/rest/UserController.java
- util/JwtUtil.java → auth/infrastructure/security/JwtTokenGenerator.java

Files to Keep (with path changes)

- config/* → infrastructure/config/*
- security/* → infrastructure/security/*
- exception/GlobalExceptionHandler.java → shared/interfaces/rest/