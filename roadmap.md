# User Service Feature Extension Roadmap

This document provides a comprehensive roadmap for extending the current user authentication and management service into an enterprise-grade SaaS user module.

## Current State Analysis

### Strengths
- ✅ Solid JWT authentication with token rotation and breach detection
- ✅ Role-based access control foundation (ADMIN/MEMBER)
- ✅ PostgreSQL with Flyway migrations
- ✅ Docker containerization
- ✅ REST + gRPC dual APIs
- ✅ Spring Boot 3.5.3 with Java 21
- ✅ Comprehensive error handling and validation
- ✅ Basic audit timestamps (createdAt, updatedAt)

### Extension Points
- User entity has `isEmailVerified` field (ready for email verification)
- Security configuration extensible for OAuth2
- Repository pattern ready for advanced features
- Service layer can accommodate new business logic
- Flyway migrations ready for schema evolution

---

## Feature Extension Roadmap

### Phase 1: Email Infrastructure & Basic Security (Priority: HIGH)

#### 1.1 Email Service Foundation
**Goal**: Establish email sending infrastructure for all email-based features

**Features**:
- Email service abstraction (SMTP, SendGrid, AWS SES)
- HTML email templates with Thymeleaf/FreeMarker
- Email queuing and retry logic
- Template management system

**Dependencies**: Spring Boot Starter Mail, template engine

**Estimated Effort**: 2-3 days

---

#### 1.2 Email Verification
**Goal**: Verify user email addresses during registration

**Features**:
- Generate secure verification tokens
- Send verification email on registration
- Verify email endpoint with token validation
- Resend verification email (rate-limited)
- Optional: require verified email for login

**Database**: `email_verification_tokens` table (id, userId, token, expiresAt, createdAt)

**API Endpoints**:
- `POST /api/users/auth/verify-email` - verify with token
- `POST /api/users/auth/resend-verification` - resend email

**Estimated Effort**: 3-4 days

---

#### 1.3 Password Reset via Email
**Goal**: Allow users to reset forgotten passwords securely

**Features**:
- Request password reset (rate-limited)
- Send reset link via email
- Validate reset token
- Reset password with new credentials
- Invalidate all sessions after reset

**Database**: `password_reset_tokens` table (id, userId, token, expiresAt, used, createdAt)

**API Endpoints**:
- `POST /api/users/auth/forgot-password` - request reset
- `POST /api/users/auth/reset-password` - reset with token
- `GET /api/users/auth/validate-reset-token` - validate token

**Security**: 1-hour token expiration, one active token per user, rate limiting (3 per hour)

**Estimated Effort**: 2-3 days

---

#### 1.4 Passwordless Login (Email OTP)
**Goal**: Allow users to log in using one-time codes sent via email

**Features**:
- Request login code via email
- Generate 6-digit numeric OTP (5-minute expiration)
- Verify OTP and issue JWT tokens
- Rate limiting (3 requests per 15 minutes per email)
- Track failed attempts

**Database**: `login_codes` table (id, email, code, expiresAt, attempts, createdAt)

**API Endpoints**:
- `POST /api/users/auth/request-login-code` - send OTP to email
- `POST /api/users/auth/verify-login-code` - verify OTP and login

**Use Cases**:
- Passwordless authentication option
- Alternative to password for verified users
- Temporary access method

**Security Considerations**:
- 6-digit code with 5-minute expiration
- Maximum 3 verification attempts per code
- Rate limit: 3 code requests per 15 minutes per email
- Constant-time comparison to prevent timing attacks

**Estimated Effort**: 2-3 days

---

#### 1.5 Rate Limiting & Brute Force Protection
**Goal**: Protect against abuse and automated attacks

**Features**:
- Rate limiting with Redis + Bucket4j
- Per-endpoint rate limits (login, registration, password reset)
- IP-based and user-based limiting
- 429 Too Many Requests responses

**Rate Limits**:
- Login: 5 attempts per 15 minutes (per IP + email)
- Registration: 3 accounts per hour (per IP)
- Password reset: 3 requests per hour (per email)
- API calls: 100 requests per 15 minutes (per user)

**Dependencies**: spring-boot-starter-data-redis, bucket4j

**Estimated Effort**: 2-3 days

---

#### 1.6 Account Lockout & Security Monitoring
**Goal**: Automatic lockout after suspicious activity

**Features**:
- Lock account after 5 failed login attempts
- Automatic unlock after 30 minutes
- Manual admin unlock
- Email notification on lockout
- Track failed attempts and last failure time

**Database**: Add columns to `users` table (isLocked, lockedAt, lockedReason, failedLoginAttempts, lastFailedLoginAt)

**Estimated Effort**: 2 days

---

### Phase 2: Session Management & Device Tracking (Priority: HIGH)

#### 2.1 Session Management
**Goal**: Track and manage user sessions across devices

**Features**:
- Track active sessions with device info
- "Where you're logged in" feature
- Revoke specific sessions
- "Logout from all devices" (already exists, enhance with session details)
- Session activity monitoring

**Database**: `user_sessions` table (id, userId, refreshTokenId, deviceName, browser, os, ipAddress, location, lastActivityAt, createdAt)

**API Endpoints**:
- `GET /api/users/me/sessions` - list active sessions
- `DELETE /api/users/me/sessions/{sessionId}` - revoke specific session
- `DELETE /api/users/me/sessions` - revoke all sessions

**Device Detection**: Parse User-Agent header, GeoIP lookup for location

**Estimated Effort**: 3-4 days

---

#### 2.2 Security Alerts & Suspicious Activity Detection
**Goal**: Detect and alert users of unusual login patterns

**Features**:
- Detect logins from new locations
- Detect logins from new devices
- Alert via email for suspicious activity
- "Was this you?" confirmation flow
- Login history with timestamps and locations

**API Endpoints**:
- `GET /api/users/me/login-history` - view login history
- `POST /api/users/me/confirm-login/{loginId}` - confirm suspicious login

**Estimated Effort**: 2-3 days

---

### Phase 3: Social Authentication (Priority: HIGH)

#### 3.1 OAuth2 Social Login
**Goal**: Enable login via Google, GitHub, Facebook

**Features**:
- OAuth2 integration with Spring Security
- Account creation via social login
- Link/unlink social accounts to existing users
- Support multiple social accounts per user
- Handle email conflicts gracefully

**Providers**: Google, GitHub, Facebook (extensible to Twitter, LinkedIn, etc.)

**Database**: `social_accounts` table (id, userId, provider, providerId, email, profilePicture, accessToken, refreshToken, createdAt, updatedAt)

**API Endpoints**:
- `GET /oauth2/authorization/{provider}` - initiate OAuth2 flow
- `POST /api/users/me/social-accounts/{provider}` - link account
- `DELETE /api/users/me/social-accounts/{provider}` - unlink account
- `GET /api/users/me/social-accounts` - list linked accounts

**Security**: Handle email matching, require verification for account linking, make User.password nullable

**Dependencies**: spring-boot-starter-oauth2-client

**Estimated Effort**: 5-7 days

---

### Phase 4: Advanced Authorization (RBAC) (Priority: MEDIUM-HIGH)

#### 4.1 Enhanced Role-Based Access Control (RBAC)
**Goal**: Fine-grained permissions beyond simple roles

**Features**:
- Hierarchical roles with permissions
- Custom role creation per tenant
- Permission-based access control (`resource:action` format)
- System roles (SUPER_ADMIN, OWNER, ADMIN, MEMBER, GUEST)
- Wildcard permissions (`users:*`, `*:read`)
- Role assignment and management

**Database**:
- `roles` table (id, tenantId, name, description, isSystem, createdAt, updatedAt)
- `permissions` table (id, resource, action, description)
- `role_permissions` table (roleId, permissionId)
- `user_roles` table (userId, roleId, tenantId, grantedBy, grantedAt)

**Permission Examples**:
- `users:create`, `users:read`, `users:update`, `users:delete`
- `settings:read`, `settings:write`
- `billing:read`, `billing:write`

**API Endpoints**:
- `GET /api/roles` - list available roles
- `POST /api/roles` - create custom role (admin only)
- `PATCH /api/roles/{roleId}` - update role permissions
- `DELETE /api/roles/{roleId}` - delete custom role
- `POST /api/users/{userId}/roles` - assign role
- `DELETE /api/users/{userId}/roles/{roleId}` - remove role
- `GET /api/permissions` - list all permissions

**Authorization**: `@RequirePermission("resource:action")` annotation, SpEL integration

**Estimated Effort**: 5-7 days

---

### Phase 5: API Keys & Service Accounts (Priority: MEDIUM-HIGH)

#### 5.1 API Key Management
**Goal**: Enable programmatic access with API keys

**Features**:
- Generate API keys for users/tenants
- Scoped permissions for API keys
- Key rotation and expiration
- Multiple keys per user
- Usage tracking and analytics

**Database**: `api_keys` table (id, userId, tenantId, name, keyHash, prefix, permissions, expiresAt, lastUsedAt, createdAt)

**API Endpoints**:
- `POST /api/users/me/api-keys` - create API key
- `GET /api/users/me/api-keys` - list API keys
- `DELETE /api/users/me/api-keys/{keyId}` - revoke key
- `PATCH /api/users/me/api-keys/{keyId}/rotate` - rotate key

**Format**: `sk_live_1234567890abcdef` (prefix + random string)

**Security**: Store hashed keys, show full key only on creation, support key prefixes for identification

**Estimated Effort**: 3-4 days

---

#### 5.2 Service Accounts
**Goal**: Machine-to-machine authentication

**Features**:
- Create service accounts (non-human users)
- Service account authentication via API keys
- Dedicated permissions for service accounts
- Audit service account actions

**Database**: Add `isServiceAccount` column to `users` table

**Estimated Effort**: 2 days

---

### Phase 6: Audit Logging (Priority: MEDIUM)

#### 6.1 Comprehensive Audit Trail
**Goal**: Track all significant user and system actions

**Features**:
- Automatic auditing via AOP annotations
- Track authentication events (login, logout, password changes)
- Track authorization events (permission denied, role changes)
- Track data access and modifications
- Track tenant operations
- Async logging for performance
- Query and export audit logs

**Database**: `audit_logs` table (id, tenantId, userId, action, resource, resourceId, status, ipAddress, userAgent, details JSONB, createdAt)

**Events**:
- Authentication: LOGIN, LOGOUT, REGISTER, PASSWORD_CHANGED, PASSWORD_RESET
- User management: USER_CREATED, USER_UPDATED, USER_DELETED
- Authorization: PERMISSION_DENIED, ROLE_CHANGED
- API: API_KEY_CREATED, API_KEY_REVOKED

**API Endpoints**:
- `GET /api/audit-logs` - query logs (admin only)
- `GET /api/users/me/audit-logs` - user's own audit trail
- `GET /api/audit-logs/export` - CSV export for compliance

**Implementation**: `@Audited` annotation, AOP aspect, asynchronous writes

**Estimated Effort**: 4-6 days

---

### Phase 7: Event-Driven Architecture (Priority: MEDIUM)

#### 7.1 Kafka Event Bus
**Goal**: Publish domain events for decoupling and scalability

**Features**:
- Apache Kafka 4 (KRaft mode - no Zookeeper)
- Publish domain events (user.registered, user.updated, user.login, etc.)
- Event schema with JSON
- Transactional outbox pattern for reliability
- Kafka listeners for internal consumption

**Topics**:
- `user.registered`, `user.updated`, `user.deleted`
- `user.login`, `user.logout`, `user.password-changed`
- `user.email-verified`, `session.created`, `session.revoked`

**Event Schema**:
```json
{
  "eventId": "uuid",
  "eventType": "user.registered",
  "timestamp": "ISO-8601",
  "tenantId": "uuid",
  "userId": "uuid",
  "payload": { ... }
}
```

**Dependencies**: spring-kafka, Kafka 4.x broker

**Docker Compose**: Kafka 4 in KRaft mode (no Zookeeper required)

**Estimated Effort**: 5-7 days

---

#### 7.2 Notification Service
**Goal**: Send notifications via multiple channels

**Features**:
- Consume Kafka events for notifications
- Email notifications (welcome, verification, alerts)
- SMS notifications (future: 2FA, critical alerts)
- In-app notifications (future: notification center)
- Template-based notification content
- Notification preferences per user

**Notification Types**:
- Welcome email on registration
- Email verification reminders
- Password reset confirmation
- Security alerts (new login, password changed)
- Session revoked notifications

**Implementation**: Kafka listeners trigger notifications, template engine for content

**Estimated Effort**: 3-5 days

---

### Phase 8: User Preferences & Settings (Priority: MEDIUM)

#### 8.1 User Preferences
**Goal**: Allow users to customize their experience

**Features**:
- Language/locale preferences
- Timezone settings
- Date/time format preferences
- Email notification preferences
- In-app notification preferences
- Theme preferences (light/dark mode)

**Database**: `user_preferences` table (userId, language, timezone, dateFormat, emailNotifications JSONB, themeMode, updatedAt)

**API Endpoints**:
- `GET /api/users/me/preferences` - get preferences
- `PATCH /api/users/me/preferences` - update preferences

**Estimated Effort**: 2-3 days

---

### Phase 9: Advanced Security Features (Priority: MEDIUM)

#### 9.1 Two-Factor Authentication (2FA/MFA)
**Goal**: Add TOTP-based 2FA for enhanced security

**Features**:
- TOTP-based 2FA (Google Authenticator compatible)
- Setup flow with QR code generation
- Backup codes (10 single-use codes)
- 2FA enforcement options (optional, required for admins)
- Remember device option

**Database**: Add to `users` table (isMfaEnabled, mfaSecret encrypted, mfaBackupCodes encrypted, mfaEnabledAt)

**API Endpoints**:
- `POST /api/users/me/mfa/setup` - initialize 2FA
- `POST /api/users/me/mfa/enable` - enable with verification
- `POST /api/users/me/mfa/disable` - disable 2FA
- `GET /api/users/me/mfa/backup-codes` - get backup codes
- `POST /api/users/me/mfa/regenerate-backup-codes` - regenerate

**Authentication Flow**: Login returns `mfaRequired: true`, then verify with `POST /api/users/auth/mfa-verify`

**Dependencies**: google-authenticator, zxing (QR codes)

**Estimated Effort**: 4-5 days

---

#### 9.2 IP Whitelisting & Access Control
**Goal**: Restrict access to specific IP ranges (enterprise feature)

**Features**:
- Tenant-level IP whitelisting
- User-level IP restrictions
- IP range validation (CIDR notation)
- Bypass for specific roles (e.g., super admin)

**Database**: `ip_whitelist` table (id, tenantId, userId, ipRange CIDR, description, createdAt)

**API Endpoints**:
- `POST /api/tenants/{tenantId}/ip-whitelist` - add IP range
- `GET /api/tenants/{tenantId}/ip-whitelist` - list ranges
- `DELETE /api/tenants/{tenantId}/ip-whitelist/{id}` - remove range

**Estimated Effort**: 2-3 days

---

### Phase 10: Webhooks (Priority: MEDIUM)

#### 10.1 Webhook Management
**Goal**: Allow tenants to subscribe to events via webhooks

**Features**:
- Register webhook endpoints per tenant
- Subscribe to specific events
- Webhook signature verification (HMAC)
- Retry logic with exponential backoff
- Webhook delivery logs
- Test webhook endpoints

**Database**:
- `webhooks` table (id, tenantId, url, secret, events[], isActive, createdAt)
- `webhook_deliveries` table (id, webhookId, eventId, status, attempts, response, createdAt)

**API Endpoints**:
- `POST /api/tenants/{tenantId}/webhooks` - create webhook
- `GET /api/tenants/{tenantId}/webhooks` - list webhooks
- `PATCH /api/tenants/{tenantId}/webhooks/{id}` - update webhook
- `DELETE /api/tenants/{tenantId}/webhooks/{id}` - delete webhook
- `POST /api/tenants/{tenantId}/webhooks/{id}/test` - test webhook

**Events**: Subscribe to Kafka topics, deliver to registered webhooks

**Estimated Effort**: 4-5 days

---

### Phase 11: Data Management & Compliance (Priority: LOW-MEDIUM)

#### 11.1 Soft Delete & Data Retention
**Goal**: GDPR-compliant data deletion and retention

**Features**:
- Soft delete for users and tenants
- Configurable retention periods
- Hard delete after retention period
- Export user data (GDPR right to data portability)
- Complete data deletion (GDPR right to erasure)

**Database**: Add `deletedAt` column to `users`, `tenants` tables

**API Endpoints**:
- `DELETE /api/users/me` - soft delete account
- `POST /api/users/me/export-data` - export user data (JSON)
- `POST /api/users/me/delete-permanently` - confirm permanent deletion

**Implementation**: Scheduled job for permanent deletion, exclude soft-deleted from queries

**Estimated Effort**: 2-3 days

---

### Phase 12: Multi-Tenancy Support (Priority: LOW)

#### 12.1 Multi-Tenancy Implementation
**Goal**: Support multiple organizations with data isolation

**Features**:
- Tenant creation and management
- Tenant member invitations
- Tenant roles (OWNER, ADMIN, MEMBER, GUEST)
- Tenant context resolution (subdomain or header-based)
- Data isolation via Hibernate filters
- Tenant switching for users in multiple tenants

**Database**:
- `tenants` table (id, name, slug, status, subscriptionPlan, createdAt, updatedAt)
- `tenant_members` table (tenantId, userId, role, invitedBy, joinedAt)
- Add `tenantId` column to relevant tables

**API Endpoints**:
- `POST /api/tenants` - create tenant
- `GET /api/tenants/{tenantId}` - get tenant
- `PATCH /api/tenants/{tenantId}` - update tenant
- `POST /api/tenants/{tenantId}/members/invite` - invite member
- `GET /api/tenants/{tenantId}/members` - list members
- `DELETE /api/tenants/{tenantId}/members/{userId}` - remove member
- `GET /api/users/me/tenants` - list user's tenants

**Tenant Resolution**: Subdomain-based (tenant1.yourdomain.com) with fallback to X-Tenant-ID header

**Data Isolation**: Hibernate @Filter on entities, TenantContext (ThreadLocal), TenantInterceptor

**Estimated Effort**: 7-10 days

---

## Feature Priority Matrix

| Feature | Priority | Complexity | Effort (days) | Business Value |
|---------|----------|------------|---------------|----------------|
| **Email Service Foundation** | HIGH | Low | 2-3 | High - Enables all email features |
| **Email Verification** | HIGH | Medium | 3-4 | High - Security, trust |
| **Password Reset** | HIGH | Medium | 2-3 | High - User experience |
| **Passwordless Login (OTP)** | HIGH | Medium | 2-3 | High - Modern UX, convenience |
| **Rate Limiting** | HIGH | Low | 2-3 | High - Security, abuse prevention |
| **Account Lockout** | HIGH | Low | 2 | Medium - Security |
| **Session Management** | HIGH | Medium | 3-4 | High - Security, transparency |
| **Security Alerts** | HIGH | Medium | 2-3 | High - Security awareness |
| **OAuth2 Social Login** | HIGH | High | 5-7 | High - Conversion, UX |
| **Enhanced RBAC** | MEDIUM-HIGH | High | 5-7 | High - Enterprise readiness |
| **API Key Management** | MEDIUM-HIGH | Medium | 3-4 | High - Developer experience |
| **Service Accounts** | MEDIUM-HIGH | Low | 2 | Medium - Automation |
| **Audit Logging** | MEDIUM | Medium | 4-6 | High - Compliance, debugging |
| **Kafka Event Bus** | MEDIUM | High | 5-7 | Medium - Scalability, decoupling |
| **Notification Service** | MEDIUM | Medium | 3-5 | Medium - User engagement |
| **User Preferences** | MEDIUM | Low | 2-3 | Medium - Personalization |
| **Two-Factor Auth (2FA)** | MEDIUM | Medium | 4-5 | Medium - Security |
| **IP Whitelisting** | MEDIUM | Low | 2-3 | Medium - Enterprise security |
| **Webhooks** | MEDIUM | Medium | 4-5 | Medium - Integration, extensibility |
| **Soft Delete & Retention** | LOW-MEDIUM | Low | 2-3 | Medium - Compliance (GDPR) |
| **Multi-Tenancy** | LOW | Very High | 7-10 | High - SaaS foundation (long-term) |
| **ABAC (Optional)** | LOW | Very High | 5-7 | Low - Complex use cases |

---

## Recommended Implementation Sequence

### Sprint 1: Email Foundation & Passwordless (8-12 days)
1. Email Service Foundation (2-3 days)
2. Email Verification (3-4 days)
3. Password Reset (2-3 days)
4. Passwordless Login - Email OTP (2-3 days)

**Outcome**: Complete email infrastructure with verification and passwordless login

---

### Sprint 2: Security Hardening (4-5 days)
1. Rate Limiting (2-3 days)
2. Account Lockout (2 days)

**Outcome**: Robust protection against brute force and abuse

---

### Sprint 3: Session Management (5-7 days)
1. Session Management & Device Tracking (3-4 days)
2. Security Alerts (2-3 days)

**Outcome**: Transparent session management with security monitoring

---

### Sprint 4: Social Authentication (5-7 days)
1. OAuth2 Social Login - Google, GitHub, Facebook (5-7 days)

**Outcome**: Modern social login experience

---

### Sprint 5: Advanced Authorization (5-7 days)
1. Enhanced RBAC with Permissions (5-7 days)

**Outcome**: Enterprise-grade permission system

---

### Sprint 6: API Access (5-6 days)
1. API Key Management (3-4 days)
2. Service Accounts (2 days)

**Outcome**: Programmatic access for developers and integrations

---

### Sprint 7: Observability (8-13 days)
1. Audit Logging (4-6 days)
2. Kafka Event Bus (5-7 days)

**Outcome**: Compliance-ready auditing, event-driven foundation

---

### Sprint 8: Notifications & Preferences (5-8 days)
1. Notification Service (3-5 days)
2. User Preferences (2-3 days)

**Outcome**: Multi-channel notifications with user control

---

### Sprint 9: Enhanced Security (6-8 days)
1. Two-Factor Authentication (4-5 days)
2. IP Whitelisting (2-3 days)

**Outcome**: Enterprise security features

---

### Sprint 10: Integration & Compliance (6-8 days)
1. Webhooks (4-5 days)
2. Soft Delete & Data Retention (2-3 days)

**Outcome**: External integrations and GDPR compliance

---

### Sprint 11: Multi-Tenancy (Optional - 7-10 days)
1. Multi-Tenancy Implementation (7-10 days)

**Outcome**: Full multi-tenant SaaS capabilities

---

## Technology Stack Additions

### Dependencies (build.gradle.kts)

```kotlin
dependencies {
    // Email
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    // OAuth2
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

    // Kafka
    implementation("org.springframework.kafka:spring-kafka")
    testImplementation("org.springframework.kafka:spring-kafka-test")

    // Redis (rate limiting)
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("com.bucket4j:bucket4j-core:8.10.1")
    implementation("com.bucket4j:bucket4j-redis:8.10.1")

    // 2FA
    implementation("com.warrenstrange:googleauth:1.5.0")
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.google.zxing:javase:3.5.3")

    // Encryption
    implementation("org.springframework.security:spring-security-crypto")
}
```

### Docker Compose Additions

```yaml
services:
  postgres:
    # Existing configuration...

  redis:
    image: redis:7.4-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - user-network

  kafka:
    image: apache/kafka:4.0.1
    ports:
      - "9092:9092"
      - "9093:9093"
    environment:
      KAFKA_NODE_ID: 1
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_LISTENERS: PLAINTEXT://localhost:9092,CONTROLLER://localhost:9093
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@localhost:9093
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_LOG_DIRS: /tmp/kraft-combined-logs
      CLUSTER_ID: MkU3OEVBNTcwNTJENDM2Qk
    volumes:
      - kafka_data:/tmp/kraft-combined-logs
    networks:
      - user-network

  kafka-ui:
    image: provectuslabs/kafka-ui:latest
    depends_on:
      - kafka
    ports:
      - "8080:8080"
    environment:
      KAFKA_CLUSTERS_0_NAME: local
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:9092
    networks:
      - user-network

volumes:
  postgres_data:
  redis_data:
  kafka_data:

networks:
  user-network:
    driver: bridge
```

---

## Success Metrics

### Technical Metrics
- API response time: p95 < 200ms
- Email delivery rate: > 99%
- Event publishing latency: < 100ms
- Audit log write latency: < 50ms
- Session management overhead: < 10ms per request

### Business Metrics
- Email verification completion rate: > 70%
- Social login adoption rate: > 40%
- Passwordless login usage: > 20%
- 2FA adoption rate: > 15% (optional), > 90% (required)
- API key usage vs traditional auth: track ratio

### Security Metrics
- Failed login attempts blocked: track count
- Account lockouts: monitor frequency
- Suspicious logins detected: track and alert
- 2FA bypass attempts: zero tolerance

---

## Migration Strategy

### For Existing Deployments
1. **Feature Flags**: Use Spring Boot profiles or feature flag library to enable features gradually
2. **Backward Compatibility**: All schema changes nullable initially
3. **Data Migration**: Provide scripts for existing user data
4. **Rollback Plan**: Each migration includes DOWN script
5. **Gradual Rollout**: Percentage-based feature rollout per tenant

---

## Testing Strategy

### Test Coverage
- Unit tests: 80%+ coverage for services
- Integration tests: Critical authentication flows
- Security tests: OWASP Top 10, penetration testing
- Performance tests: Rate limiting, audit logging, event publishing
- E2E tests: Complete user journeys (registration → verification → login)

### Test Environments
- Local: Docker Compose with all services
- CI/CD: TestContainers (PostgreSQL, Redis, Kafka)
- Staging: Full production replica
- Production: Canary deployments with feature flags

---

## Risk Mitigation

### High-Risk Areas
1. **OAuth2 Account Linking**: Risk of account takeover
   - Mitigation: Require email verification, additional confirmation for account linking

2. **Kafka Message Loss**: Events lost during failures
   - Mitigation: Transactional outbox pattern, at-least-once delivery, idempotent consumers

3. **Rate Limiting Failures**: Redis unavailable
   - Mitigation: Fail-open with logging, circuit breaker pattern

4. **Performance Degradation**: Audit logging overhead
   - Mitigation: Async logging, database partitioning, archival strategy

5. **Session Hijacking**: Session tokens compromised
   - Mitigation: Short-lived tokens, IP validation, device fingerprinting

---

## Documentation Requirements

### For Each Feature
1. Architecture decision record (ADR)
2. API documentation (OpenAPI/Swagger)
3. Database schema diagrams
4. Configuration guide
5. Migration guide

### Update CLAUDE.md
- New build commands
- New environment variables
- Architecture changes
- Testing guidelines

---

## Next Steps

1. **Review & Approve**: Stakeholder review of roadmap priorities
2. **Setup Infrastructure**: Provision Redis, Kafka, email provider (SendGrid/AWS SES)
3. **Configure CI/CD**: Update pipeline for TestContainers
4. **Begin Sprint 1**: Email foundation and passwordless authentication
5. **Iterate**: Review after each sprint, adjust priorities based on feedback

---

**Document Version**: 2.0
**Last Updated**: 2025-11-28
**Status**: Ready for Implementation
