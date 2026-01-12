# Specification: JWT Authentication

## Goal
Implement secure JWT-based authentication in both Spring Boot and NestJS microservices with login, register, and token refresh endpoints. Access tokens expire in 15 minutes, refresh tokens in 7 days. Both frameworks use identical JWT secret for testing parity.

## User Stories
- As a user, I want to register an account so that I can access the application
- As a user, I want to login with my email and password so that I receive authentication tokens
- As a user, I want to refresh my access token so that I can maintain my session without re-authenticating
- As a developer, I want protected routes to require valid JWT tokens so that the API is secure

## Specific Requirements

**Authentication Endpoints**
- Implement `POST /auth/register` - Create new user, return tokens (public)
- Implement `POST /auth/login` - Authenticate with email/password, return tokens (public)
- Implement `POST /auth/refresh` - Exchange refresh token for new access token (public)
- All endpoints must return identical response structure in both frameworks

**Access Token Configuration**
- Algorithm: HS256 (HMAC with SHA-256)
- Expiration: 15 minutes (900 seconds)
- Payload must include: `sub` (user ID), `email`, `role`, `iat`, `exp`
- Both frameworks use same secret key from `JWT_SECRET` env variable
- Default secret: `tcc-microsservices-jwt-secret-2026`

**Refresh Token Configuration**
- Algorithm: HS256 (same as access token)
- Expiration: 7 days (604800 seconds)
- Payload must include: `sub` (user ID), `type` ("refresh"), `iat`, `exp`
- Used only for obtaining new access tokens

**Login Response Format**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

**Password Validation**
- Use BCrypt to compare provided password with stored hash
- Reuse existing BCryptPasswordEncoder (Spring Boot) and bcrypt (NestJS)
- Return 401 Unauthorized for invalid credentials

**Route Protection**
- Public routes: `/auth/login`, `/auth/register`, `/auth/refresh`
- Protected routes: All `/users/**` endpoints
- Protected routes require `Authorization: Bearer <token>` header
- Return 401 if token missing, invalid, or expired

**Request DTOs**
- LoginRequestDTO: `email` (required, valid email), `password` (required)
- RegisterRequestDTO: `name` (required), `email` (required, valid email), `password` (required, min 8 chars), `role` (optional), `status` (optional)
- RefreshTokenRequestDTO: `refreshToken` (required)

**Error Responses**
- 400 Bad Request: Invalid input / validation errors
- 401 Unauthorized: Invalid credentials / Invalid token / Expired token
- 409 Conflict: Email already exists (registration)

## Visual Design
No visual assets provided.

## Existing Code to Leverage

**NestJS Auth Module (javascript/src/auth/auth.module.ts)**
- Already has JwtModule configured
- Update expiration from 1h to 15min
- Add Passport module integration

**NestJS Auth Service (javascript/src/auth/auth.service.ts)**
- Has basic generateToken method
- Extend with: login, register, validateUser, refreshToken methods
- Update token payload to include email and role

**NestJS Auth Guard (javascript/src/common/auth.guard.ts)**
- Has placeholder implementation
- Replace with proper JWT validation using JwtService

**Spring Boot Security Config (java/.../config/SecurityConfig.java)**
- Already has BCryptPasswordEncoder bean
- Extend with SecurityFilterChain and JWT filter

**User Repositories (both frameworks)**
- Both have `findByEmail` method for user lookup
- Both have `existsByEmail` for duplicate checking

**BCrypt Implementation (both frameworks)**
- Spring Boot: BCryptPasswordEncoder with cost factor 10
- NestJS: bcrypt with saltRounds 10
- Reuse for password validation

## Out of Scope
- OAuth2 / Social login providers
- Multi-factor authentication (MFA)
- Password reset/recovery flow
- Email verification
- Rate limiting on auth endpoints
- Token blacklisting/revocation
- Role-based route authorization (beyond role in token)
- Session management / remember me
- Account lockout after failed attempts
