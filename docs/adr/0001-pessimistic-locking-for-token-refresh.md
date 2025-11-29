# ADR-0001: Pessimistic Locking for Token Refresh

## Status

Accepted

## Date

2025-11-30

## Context

Our JWT authentication system uses refresh token rotation for security. When a refresh token is used:
1. The old refresh token is invalidated (deleted)
2. A new access token and refresh token are issued
3. The new refresh token maintains the same `tokenFamily` for breach detection

This creates a critical requirement: **each refresh token must be used exactly once**.

Without concurrency control, a race condition exists:
- Two concurrent requests with the same refresh token could both succeed
- This violates the single-use security model
- An attacker who intercepts a token could race the legitimate user

### Race Condition Illustrated

**With Optimistic Locking (problematic):**
```
Thread A: SELECT token (version=1) ✓
Thread B: SELECT token (version=1) ✓  ← Both read the same valid token!
Thread A: DELETE token, issue new tokens ✓
Thread B: DELETE token → OptimisticLockException
```
Problem: Thread B already validated the token and might have partially processed before failing.

**With Pessimistic Locking (our solution):**
```
Thread A: SELECT token FOR UPDATE (locks row) ✓
Thread B: SELECT token FOR UPDATE → WAITS...
Thread A: DELETE token, issue new tokens, COMMIT (releases lock) ✓
Thread B: SELECT returns empty → "Invalid token"
```
Clean: Thread B never sees the token as valid.

## Decision

We will use **pessimistic locking** (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) when fetching refresh tokens for rotation.

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT rt FROM RefreshToken rt WHERE rt.token = :token")
Optional<RefreshToken> findByTokenForUpdate(@Param("token") String token);
```

Combined with `@Transactional(isolation = Isolation.SERIALIZABLE)` on the service method.

## Alternatives Considered

### Option 1: Optimistic Locking with @Version

```java
// Entity change
@Version
private Long version;
```

**Rejected because:**
- Requires retry logic for `OptimisticLockException`
- For refresh tokens, retry is semantically wrong - if another request already used the token, we should fail, not retry
- Both concurrent requests can read the same valid token before either writes, creating a window where both believe the token is valid

### Option 2: No Locking

**Rejected because:**
- Clear security vulnerability
- Allows token reuse attacks
- Violates the fundamental security model of refresh token rotation

### Option 3: Application-Level Distributed Lock (Redis)

```java
// Using Redis lock
try (var lock = redisLockRegistry.obtain(tokenHash)) {
    if (lock.tryLock(5, TimeUnit.SECONDS)) {
        // refresh logic
    }
}
```

**Rejected because:**
- Adds external dependency (Redis)
- More complex infrastructure
- Database-level locking is sufficient for our scale
- Could be reconsidered if we move to a distributed database without row-level locking

## Consequences

### Positive

1. **Security guarantee**: Token can only be consumed once, enforced at database level
2. **Simple implementation**: Single annotation, no retry logic needed
3. **Correct failure mode**: Second request fails cleanly with "invalid token"
4. **No external dependencies**: Uses PostgreSQL's native row locking

### Negative

1. **Potential for lock contention**: If many requests try to refresh the same token simultaneously
   - Mitigated by: Short lock duration (milliseconds), this is expected to be rare in legitimate use
2. **Database-specific behavior**: Lock behavior may vary slightly across databases
   - Mitigated by: Using standard JPA `LockModeType`, tested with PostgreSQL
3. **Deadlock potential**: If other operations also lock refresh tokens
   - Mitigated by: Token refresh is the only operation that locks this table

### Performance Impact

- Lock duration: ~10-50ms (query + delete + insert)
- Contention likelihood: Very low for legitimate traffic
- High contention indicates: Possible token theft/replay attack (which we want to block)

## Implementation

**Files changed:**
- `RefreshTokenRepository.java`: Add `findByTokenForUpdate()` method
- `AuthService.java`: Use locked query, add SERIALIZABLE isolation

## References

- [PostgreSQL Row Locking](https://www.postgresql.org/docs/current/explicit-locking.html)
- [JPA LockModeType Documentation](https://jakarta.ee/specifications/persistence/3.1/apidocs/jakarta.persistence/jakarta/persistence/lockmodetype)
- [OWASP Token Best Practices](https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_for_Java_Cheat_Sheet.html)
