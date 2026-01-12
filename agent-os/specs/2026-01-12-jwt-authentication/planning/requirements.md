# Spec Requirements: JWT Authentication

## Initial Description
Implement token-based authentication with login endpoint, token generation, refresh tokens, and route protection using Spring Security (Java) and Passport.js/JWT (NestJS). Both frameworks must use the same JWT secret key for testing parity.

## Requirements Discussion

### First Round Questions

**Q1:** For the login endpoint, should users authenticate with email + password or username + password?
**Answer:** Users authenticate with email + password (email is the unique field in User entity).

**Q2:** What should the JWT token expiration time be?
**Answer:** 15 minutes (short-lived, more secure).

**Q3:** Should we implement refresh tokens for extending sessions, or just simple access tokens?
**Answer:** Implement refresh tokens for extending sessions.

**Q4:** Which routes should be protected vs. public?
**Answer:** Login and Register endpoints should be public. All other User CRUD operations should be protected.

**Q5:** For the JWT payload, what claims should be included?
**Answer:** Standard claims (sub, email, iat, exp) + role for authorization checks.

**Q6:** Should we enhance the existing NestJS auth code or replace it?
**Answer:** Enhance the existing code in `auth/` folder.

**Q7:** Should both frameworks use the same JWT secret key?
**Answer:** Yes, use same JWT secret key for testing parity.

### Existing Code to Reference

**NestJS Auth Module:**
- Location: `javascript/src/auth/auth.module.ts`
- Current: Basic JwtModule setup with 1h expiration, defaultSecret
- Needs: Update expiration to 15min, add refresh token support

**NestJS Auth Service:**
- Location: `javascript/src/auth/auth.service.ts`
- Current: Simple generateToken method with username/sub payload
- Needs: Add email, role to payload; add login, validateUser, refreshToken methods

**NestJS Auth Guard:**
- Location: `javascript/src/common/auth.guard.ts`
- Current: Placeholder with hardcoded "Bearer valid-token" check
- Needs: Proper JWT validation using JwtService

**Spring Boot:**
- Currently no auth implementation
- Needs: Full JWT authentication setup with Spring Security

### Follow-up Questions
None required - user provided comprehensive answers.

## Visual Assets

### Files Provided:
No visual assets provided.

## Requirements Summary

### Functional Requirements

**Authentication Endpoints:**
- `POST /auth/login` - Authenticate user with email/password, return access + refresh tokens
- `POST /auth/register` - Register new user (public endpoint)
- `POST /auth/refresh` - Exchange refresh token for new access token
- `POST /auth/logout` - Invalidate refresh token (optional)

**JWT Access Token:**
- Expiration: 15 minutes
- Payload claims:
  - `sub`: User ID (UUID)
  - `email`: User email
  - `role`: User role (USER/ADMIN)
  - `iat`: Issued at timestamp
  - `exp`: Expiration timestamp
- Algorithm: HS256 (HMAC with SHA-256)

**JWT Refresh Token:**
- Expiration: 7 days (longer-lived)
- Payload claims:
  - `sub`: User ID (UUID)
  - `type`: "refresh" (to distinguish from access tokens)
  - `iat`: Issued at timestamp
  - `exp`: Expiration timestamp

**Route Protection:**
- Public routes (no authentication required):
  - `POST /auth/login`
  - `POST /auth/register`
  - `POST /auth/refresh`
- Protected routes (require valid access token):
  - All User CRUD endpoints (`GET /users`, `GET /users/:id`, `PUT /users/:id`, `DELETE /users/:id`)

**Password Validation:**
- Compare provided password with BCrypt hash stored in database
- Use same BCrypt configuration from User Entity implementation

**Shared Configuration:**
- Both frameworks use identical JWT secret key
- Secret stored in environment variable: `JWT_SECRET`
- Fallback default for development: `tcc-microsservices-jwt-secret-2026`

### Response Formats

**Login Success Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

**Login Error Response:**
```json
{
  "statusCode": 401,
  "message": "Invalid credentials",
  "error": "Unauthorized"
}
```

**Token Refresh Success:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

### Reusability Opportunities
- Existing NestJS auth module/service as foundation
- BCrypt password encoder from User Entity implementation (both frameworks)
- UserRepository.findByEmail method (both frameworks)
- Existing AuthGuard structure in NestJS

### Scope Boundaries

**In Scope:**
- Login endpoint with email/password
- Register endpoint (create user with hashed password)
- Access token generation with 15min expiration
- Refresh token generation with 7-day expiration
- Token refresh endpoint
- JWT guard/filter for protected routes
- Same JWT secret across frameworks
- Password validation with BCrypt

**Out of Scope:**
- OAuth2 / Social login
- Multi-factor authentication (MFA)
- Password reset/recovery
- Email verification
- Rate limiting on login attempts
- Token blacklisting/revocation storage
- Role-based authorization (beyond including role in token)
- Session management

### Technical Considerations

**Spring Boot Stack:**
- Spring Security for authentication filter
- JJWT library (io.jsonwebtoken) for JWT operations
- BCryptPasswordEncoder (already configured)
- OncePerRequestFilter for JWT validation
- SecurityFilterChain configuration

**NestJS Stack:**
- @nestjs/jwt (already installed)
- @nestjs/passport with passport-jwt strategy
- Enhance existing AuthService with login/refresh methods
- Enhance existing AuthGuard with proper JWT validation
- bcrypt (already installed) for password validation

**Shared JWT Secret:**
- Environment variable: `JWT_SECRET`
- Default fallback: `tcc-microsservices-jwt-secret-2026`
- Key must be at least 256 bits for HS256

**Token Storage (Client-side):**
- Access token: Short-lived, sent in Authorization header
- Refresh token: Longer-lived, sent only to /auth/refresh endpoint
- Note: Backend is stateless, no server-side token storage required
