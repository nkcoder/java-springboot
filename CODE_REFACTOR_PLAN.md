# Code Quality Improvement Plan
## Java Spring Boot User Authentication Service - Complete Refactor

### Overview
Complete refactoring of the user authentication service to adopt best practices, maximize immutability, embrace functional programming, and improve overall code quality. This plan addresses all identified issues with an aggressive approach to modernization.

---

## Phase 1: Foundation - Security & Configuration (Days 1-2)

### 1.1 JWT Security Hardening ⬜
**Files**: `JwtUtil.java`, `JwtProperties.java`

**Changes**:
- Add issuer validation in all token validation methods
- Validate JWT secret key strength (minimum 32 bytes) on startup
- Replace mutable HashMap with immutable claim builders
- Convert `JwtProperties` to validated record with nested records
- Add proper error handling without swallowing exceptions
- Extract validation constants

**Breaking Changes**: None (internal improvements)

### 1.2 Configuration as Records ⬜
**Files**: `JwtProperties.java`, New: `CorsProperties.java`

**Changes**:
- Convert `JwtProperties` to record with validation annotations
- Create `CorsProperties` record to externalize CORS config
- Add `@Validated` and constraints for security properties
- Use compact constructor for defaults and validation

**Breaking Changes**: Configuration structure in `application.yml` remains compatible

### 1.3 Security Configuration Improvements ⬜
**Files**: `SecurityConfig.java`

**Changes**:
- Require authentication for logout endpoints (currently public)
- Increase BCrypt strength from default 10 to 12 rounds
- Externalize CORS configuration using `CorsProperties`
- Add security headers (XSS, CSRF tokens for future)

**Breaking Changes**: Logout endpoints now require authentication (BREAKING)

---

## Phase 2: Entity Layer - Maximize Immutability (Days 3-4)

### 2.1 User Entity Refactoring ⬜
**File**: `User.java`

**Changes**:
- Make fields `final` where immutable (id, role, createdAt)
- Remove all setters, replace with business methods:
  - `updateEmail(String email)` - with validation
  - `updateName(String name)` - with validation
  - `changePassword(String encodedPassword)`
  - `markEmailVerified()`
  - `recordLogin()`
- Change `Boolean isEmailVerified` → `boolean emailVerified` (primitive)
- Make `refreshTokens` final, return unmodifiable list
- Add relationship helper methods (`addRefreshToken`, `removeRefreshToken`)
- Remove password from `toString()`
- Remove `setters` for audit fields (managed by JPA)

**Breaking Changes**: Entity API changes (affects service layer)

### 2.2 RefreshToken Entity Refactoring ⬜
**File**: `RefreshToken.java`

**Changes**:
- Make fields `final` (token, tokenFamily, userId, expiresAt)
- Remove all setters except for JPA relationship
- Use `Objects.requireNonNull()` in constructor
- Mask token in `toString()` (show first 4 + last 4 chars)
- Keep only getters and `isExpired()` method

**Breaking Changes**: Entity API changes (affects service layer)

### 2.3 Custom Validation Annotations ⬜
**New Files**: `PasswordMatch.java`, `PasswordMatchValidator.java`, `PasswordValidation.java`

**Changes**:
- Create `@PasswordMatch` annotation for DTO-level validation
- Move password confirmation logic from service to validator
- Extract password validation constants to dedicated class
- Apply to `ChangePasswordRequest` record

**Breaking Changes**: None (moves validation earlier in request pipeline)

---

## Phase 3: Service Layer - Functional & Transactional (Days 5-7)

### 3.1 AuthService Refactoring ⬜
**File**: `AuthService.java`

**Changes**:
- Remove class-level `@Transactional`, add method-level with proper scope
- Add `@Transactional(isolation = Isolation.SERIALIZABLE)` for token rotation
- Remove circular dependency on `UserService.updateLastLogin()`
  - Call `user.recordLogin()` + `userRepository.save()` directly
- Replace mutable HashMaps with builder pattern in JWT generation
- Add pessimistic locking for refresh token operations
- Return `Optional` from helper methods, use functional chains
- Improve error messages with structured logging (no PII)
- Add null checks with `Objects.requireNonNull()` or `Optional`

**Breaking Changes**: None (internal improvements)

### 3.2 UserService Refactoring ⬜
**File**: `UserService.java`

**Changes**:
- Remove class-level `@Transactional`
- Add `@Transactional(readOnly = true)` for query methods
- Add `@Transactional` for write methods only
- Eliminate duplicate queries in `updateProfile()`:
  - Single query to fetch user
  - Use `Optional.ofNullable()` chains for conditional updates
- Use functional approach with `Optional` and `Stream`
- Replace imperative validation with functional style
- Use entity business methods instead of setters

**Breaking Changes**: None (internal improvements)

### 3.3 Repository Enhancements ⬜
**Files**: `UserRepository.java`, `RefreshTokenRepository.java`

**Changes**:
- Add return types to all `@Modifying` queries (int for rows affected)
- Add `@Lock(LockModeType.PESSIMISTIC_WRITE)` query for token refresh
- Remove unused query methods or document usage
- Add `@Transactional` where needed for `@Modifying` queries

**Breaking Changes**: None (return type additions are backward compatible)

---

## Phase 4: API Layer - Modern REST Design (Days 8-9)

### 4.1 Custom Annotations & Argument Resolvers ⬜
**New Files**: `CurrentUser.java`, `CurrentUserArgumentResolver.java`, `WebConfig.java`

**Changes**:
- Create `@CurrentUser` annotation for controller parameters
- Implement `HandlerMethodArgumentResolver` to extract userId from request
- Register resolver in `WebConfig`
- Replace manual request attribute casting with clean annotation

**Breaking Changes**: Controller method signatures change (BREAKING but cleaner)

### 4.2 Controller Refactoring ⬜
**Files**: `AuthController.java`, `UserController.java`

**Changes**:
- Replace request attribute casting with `@CurrentUser UUID userId`
- Remove unnecessary `@Autowired` annotations
- Stop logging PII at INFO level (use DEBUG with masking)
- Add `@Valid` to all request parameters including admin endpoints
- Use functional Optional chains where applicable
- Standardize response messages

**Breaking Changes**: None (internal improvements)

### 4.3 Global Exception Handler Enhancement ⬜
**File**: `GlobalExceptionHandler.java`

**Changes**:
- Return validation error details to client (structured map)
- Split generic exception handlers into specific ones:
  - `JsonParseException` handler
  - `HttpMediaTypeNotSupportedException` handler
  - `HttpMessageNotReadableException` handler
- Remove instanceof checks
- Add structured error responses with field-level details

**Breaking Changes**: Error response structure changes (BREAKING but better UX)

---

## Phase 5: Security Enhancements (Days 10-11)

### 5.1 JWT Authentication Filter Improvements ⬜
**File**: `JwtAuthenticationFilter.java`

**Changes**:
- Extract constants (BEARER_PREFIX, AUTHORIZATION_HEADER)
- Remove redundant `isTokenExpired()` check
- Use functional `Optional` chain for token extraction
- Improve exception handling (catch specific exceptions)
- Add comprehensive logging without PII

**Breaking Changes**: None (internal improvements)

### 5.2 JWT Entry Point ⬜
**File**: `JwtAuthenticationEntryPoint.java`

**Changes**:
- Return structured error response matching ApiResponse format
- Add correlation ID for error tracking
- Improve error messages

**Breaking Changes**: Error response structure (BREAKING but consistent)

---

## Phase 6: Functional Programming Patterns (Days 12-13)

### 6.1 Mapper Enhancements ⬜
**File**: `UserMapper.java`

**Changes**:
- Return `Optional<UserResponse>` for nullable inputs
- Add `toResponseNonNull()` with `Objects.requireNonNull()`
- Use method references where possible
- Consider making mapper stateless with static methods

**Breaking Changes**: Return type changes to Optional (BREAKING)

### 6.2 Service Method Signatures ⬜
**Files**: All service classes

**Changes**:
- Return `Optional<T>` for methods that might not find entities
- Use `Stream API` for collection operations
- Replace null checks with `Optional.ofNullable()`
- Use `Optional.map()`, `Optional.flatMap()`, `Optional.filter()` chains
- Replace imperative loops with functional equivalents

**Breaking Changes**: Method signatures change (BREAKING)

### 6.3 DTO Enhancements ⬜
**Files**: All DTO records

**Changes**:
- Extract validation constants to dedicated classes
- Add custom validation annotations where needed
- Ensure all DTOs are records (already done)
- Add compact constructors for validation where needed

**Breaking Changes**: None (validation moves earlier)

---

## Phase 7: Testing Updates (Days 14-15)

### 7.1 Update Unit Tests ⬜
**Files**: All test files in `src/test/java`

**Changes**:
- Update tests for new entity business methods
- Update tests for Optional return types
- Update tests for @CurrentUser annotation
- Update tests for new error response structures
- Add tests for new validation annotations
- Add tests for pessimistic locking scenarios

### 7.2 Integration Tests ⬜
**Files**: Integration test classes

**Changes**:
- Update for breaking API changes
- Test transaction isolation levels
- Test concurrent token refresh scenarios
- Verify security improvements

---

## Critical Files to Modify

### High Priority (Core Changes)
1. `src/main/java/org/nkcoder/entity/User.java`
2. `src/main/java/org/nkcoder/entity/RefreshToken.java`
3. `src/main/java/org/nkcoder/service/AuthService.java`
4. `src/main/java/org/nkcoder/service/UserService.java`
5. `src/main/java/org/nkcoder/util/JwtUtil.java`
6. `src/main/java/org/nkcoder/config/SecurityConfig.java`
7. `src/main/java/org/nkcoder/config/JwtProperties.java`

### Medium Priority (API & Infrastructure)
8. `src/main/java/org/nkcoder/controller/AuthController.java`
9. `src/main/java/org/nkcoder/controller/UserController.java`
10. `src/main/java/org/nkcoder/exception/GlobalExceptionHandler.java`
11. `src/main/java/org/nkcoder/security/JwtAuthenticationFilter.java`
12. `src/main/java/org/nkcoder/repository/UserRepository.java`
13. `src/main/java/org/nkcoder/repository/RefreshTokenRepository.java`

### New Files to Create
14. `src/main/java/org/nkcoder/annotation/CurrentUser.java`
15. `src/main/java/org/nkcoder/resolver/CurrentUserArgumentResolver.java`
16. `src/main/java/org/nkcoder/config/WebConfig.java`
17. `src/main/java/org/nkcoder/validation/PasswordMatch.java`
18. `src/main/java/org/nkcoder/validation/PasswordMatchValidator.java`
19. `src/main/java/org/nkcoder/validation/PasswordValidation.java`
20. `src/main/java/org/nkcoder/config/CorsProperties.java`

### Test Files (Update)
21. All files in `src/test/java/org/nkcoder/`

---

## Breaking Changes Summary

### API Changes (Client Impact)
1. **Logout endpoints now require authentication** - clients must send Authorization header
2. **Error response structure** - validation errors now return field-level details
3. **Optional return types** - some service methods return Optional instead of nullable

### Internal API Changes (No Client Impact)
1. **Entity API changes** - setters removed, business methods added
2. **Controller parameter injection** - @CurrentUser replaces request attribute casting
3. **Service method signatures** - Optional return types
4. **Mapper return types** - Optional for nullable cases

---

## Implementation Strategy

### Approach: Bottom-Up Refactoring
1. **Start with entities** - foundation of immutability
2. **Update repositories** - add locking and return types
3. **Refactor services** - use new entity API, add transactions
4. **Update security layer** - JWT validation and configuration
5. **Refactor controllers** - use new annotations and patterns
6. **Update tests** - ensure all changes work correctly

### Code Review Checkpoints
After each phase:
- Run `./gradlew build` - ensure compilation
- Run `./gradlew test` - ensure tests pass
- Run `./gradlew spotlessApply` - format code
- Manual code review of changes

---

## Rollback Strategy

### Git Strategy
- Create feature branch: `feature/code-quality-refactor`
- Commit after each phase with descriptive messages
- Can cherry-pick individual phases if needed
- Tag before major breaking changes

### Testing Strategy
- Run full test suite after each phase
- Integration tests with TestContainers
- Manual testing of auth flows
- Performance testing of transaction isolation

---

## Migration Guide (For Documentation)

### For Developers
1. **Entity usage**: Replace setters with business methods
2. **Service calls**: Handle Optional return types
3. **Controller params**: Use @CurrentUser instead of request attributes
4. **Error handling**: Parse new error response structure

### For API Clients
1. **Logout**: Now requires authentication header
2. **Error responses**: Check for field-level validation errors in response

---

## Estimated Timeline

- **Phase 1 (Security & Config)**: 2 days ⬜
- **Phase 2 (Entities)**: 2 days ⬜
- **Phase 3 (Services)**: 3 days ⬜
- **Phase 4 (API Layer)**: 2 days ⬜
- **Phase 5 (Security Enhancement)**: 2 days ⬜
- **Phase 6 (Functional Programming)**: 2 days ⬜
- **Phase 7 (Testing)**: 2 days ⬜

**Total**: 15 days (3 weeks)

---

## Success Criteria

### Code Quality
- [ ] All entities maximize immutability (final fields where possible)
- [ ] All null checks replaced with Optional
- [ ] All service methods use functional programming patterns
- [ ] No PII logged at INFO level
- [ ] All @Modifying queries have return types
- [ ] All write operations have proper transaction boundaries

### Security
- [ ] JWT issuer validation in all token checks
- [ ] Key strength validation on startup
- [ ] Logout requires authentication
- [ ] BCrypt strength increased to 12
- [ ] Pessimistic locking for token refresh
- [ ] No passwords in toString() or logs

### Testing
- [ ] All unit tests pass
- [ ] All integration tests pass
- [ ] Test coverage > 80%
- [ ] Concurrent token refresh tested
- [ ] Security improvements verified

### Documentation
- [ ] CLAUDE.md updated with new patterns
- [ ] Migration guide created
- [ ] Breaking changes documented
- [ ] New annotations documented

---

## Progress Tracking

Use ⬜ for pending, ✅ for completed tasks. Update this file as you complete each phase.

### Quick Reference
- **Start Date**: _________
- **Current Phase**: Phase 1
- **Completed Phases**: None yet
- **Blockers**: None

---

## Notes

### Why This Approach?
- **Bottom-up** ensures foundation is solid before building on it
- **Entity immutability** prevents data corruption and bugs
- **Functional programming** reduces side effects and improves testability
- **Breaking changes** allowed means we can fix API design flaws
- **Aggressive FP** aligns with modern Java best practices

### Trade-offs Accepted
- More verbose entity constructors (worth it for immutability)
- Optional everywhere may initially feel awkward (improves null safety)
- Breaking changes require client updates (improves API quality)
- Transaction isolation may have minor performance impact (security first)

### Key Principles Applied
- **SOLID**: Single responsibility, Open/closed, Interface segregation
- **DRY**: Extract validation constants and common patterns
- **YAGNI**: Remove unused code (e.g., unused repository methods)
- **Clean Code**: Meaningful names, small functions, clear intent
- **Immutability**: Prefer final fields, immutable collections
- **Functional**: Optional, Stream, method references, pure functions
