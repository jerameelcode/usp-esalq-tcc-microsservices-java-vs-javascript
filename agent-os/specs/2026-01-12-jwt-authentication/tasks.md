# Task Breakdown: JWT Authentication

## Overview
Total Tasks: 28 (across 5 task groups)

This feature implements JWT-based authentication in both Spring Boot and NestJS with login, register, refresh token endpoints, and route protection.

## Task List

### Spring Boot Implementation

#### Task Group 1: Spring Boot JWT Infrastructure
**Dependencies:** None

- [ ] 1.0 Set up Spring Boot JWT infrastructure
  - [ ] 1.1 Write 4-6 focused tests for JWT generation and validation
    - Test access token generation with correct claims
    - Test refresh token generation with correct claims
    - Test token validation (valid token)
    - Test token validation (expired token)
    - Test token validation (invalid signature)
  - [ ] 1.2 Add JJWT dependency to pom.xml
    - Add `jjwt-api`, `jjwt-impl`, `jjwt-jackson` dependencies
  - [ ] 1.3 Create JwtService class
    - Method: generateAccessToken(User user) - returns JWT string
    - Method: generateRefreshToken(User user) - returns JWT string
    - Method: validateToken(String token) - returns boolean
    - Method: extractUserId(String token) - returns UUID
    - Method: extractEmail(String token) - returns String
    - Method: isTokenExpired(String token) - returns boolean
    - Use JWT_SECRET env variable with default fallback
  - [ ] 1.4 Create JWT configuration properties
    - accessTokenExpiration: 900 (15 min)
    - refreshTokenExpiration: 604800 (7 days)
    - secret: from env or default
  - [ ] 1.5 Ensure JWT infrastructure tests pass

**Acceptance Criteria:**
- JwtService can generate valid access and refresh tokens
- Tokens contain correct claims (sub, email, role, iat, exp)
- Token validation correctly identifies valid/invalid/expired tokens
- All 4-6 tests pass

#### Task Group 2: Spring Boot Auth Endpoints
**Dependencies:** Task Group 1

- [ ] 2.0 Implement Spring Boot auth endpoints
  - [ ] 2.1 Write 4-6 focused tests for auth endpoints
    - Test successful login returns tokens
    - Test login with invalid credentials returns 401
    - Test register creates user and returns tokens
    - Test register with existing email returns 409
    - Test refresh token returns new access token
    - Test refresh with invalid token returns 401
  - [ ] 2.2 Create AuthController with endpoints
    - POST /auth/login - LoginRequestDTO -> TokenResponseDTO
    - POST /auth/register - RegisterRequestDTO -> TokenResponseDTO
    - POST /auth/refresh - RefreshTokenRequestDTO -> TokenResponseDTO
  - [ ] 2.3 Create auth DTOs
    - LoginRequestDTO: email, password (with validation)
    - RegisterRequestDTO: name, email, password, role?, status?
    - RefreshTokenRequestDTO: refreshToken
    - TokenResponseDTO: accessToken, refreshToken, tokenType, expiresIn
  - [ ] 2.4 Create AuthService
    - Method: login(LoginRequestDTO) - validate credentials, return tokens
    - Method: register(RegisterRequestDTO) - create user, return tokens
    - Method: refreshToken(String refreshToken) - validate, return new access token
    - Use BCryptPasswordEncoder for password validation
  - [ ] 2.5 Configure public routes in SecurityConfig
    - Permit /auth/** endpoints without authentication
  - [ ] 2.6 Ensure auth endpoint tests pass

**Acceptance Criteria:**
- Login endpoint authenticates user and returns tokens
- Register endpoint creates user with hashed password and returns tokens
- Refresh endpoint exchanges valid refresh token for new access token
- Proper error responses for invalid credentials/tokens
- All 4-6 tests pass

#### Task Group 3: Spring Boot Route Protection
**Dependencies:** Task Group 2

- [ ] 3.0 Implement Spring Boot route protection
  - [ ] 3.1 Write 3-4 focused tests for route protection
    - Test protected route with valid token succeeds
    - Test protected route without token returns 401
    - Test protected route with expired token returns 401
    - Test protected route with invalid token returns 401
  - [ ] 3.2 Create JwtAuthenticationFilter
    - Extends OncePerRequestFilter
    - Extract token from Authorization header
    - Validate token using JwtService
    - Set authentication in SecurityContext
  - [ ] 3.3 Update SecurityConfig
    - Add JwtAuthenticationFilter to filter chain
    - Configure /users/** as protected routes
    - Configure /auth/** as public routes
  - [ ] 3.4 Ensure route protection tests pass

**Acceptance Criteria:**
- User CRUD endpoints require valid JWT token
- Auth endpoints are publicly accessible
- Invalid/expired tokens result in 401 response
- All 3-4 tests pass

### NestJS Implementation

#### Task Group 4: NestJS JWT Enhancement
**Dependencies:** None (can run parallel to Task Groups 1-3)

- [ ] 4.0 Enhance NestJS JWT infrastructure
  - [ ] 4.1 Write 4-6 focused tests for JWT and auth
    - Test access token generation with correct claims
    - Test refresh token generation
    - Test login with valid credentials
    - Test login with invalid credentials
    - Test token refresh
  - [ ] 4.2 Install @nestjs/passport and passport-jwt
    - Add dependencies to package.json
  - [ ] 4.3 Update AuthModule configuration
    - Update JwtModule expiration to 15min (900s)
    - Add PassportModule import
    - Use JWT_SECRET env variable with default fallback
  - [ ] 4.4 Enhance AuthService
    - Method: login(email, password) - validate and return tokens
    - Method: register(createUserDto) - create user and return tokens
    - Method: refreshToken(refreshToken) - return new access token
    - Method: validateUser(email, password) - verify credentials
    - Update generateToken to include email, role in payload
    - Add generateRefreshToken method
  - [ ] 4.5 Create auth DTOs
    - LoginDto: email, password (with class-validator)
    - RefreshTokenDto: refreshToken
    - TokenResponseDto: accessToken, refreshToken, tokenType, expiresIn
  - [ ] 4.6 Ensure NestJS auth tests pass

**Acceptance Criteria:**
- AuthService generates tokens with correct claims
- Login validates credentials using bcrypt
- Register creates user with hashed password
- Refresh token generates new access token
- All 4-6 tests pass

#### Task Group 5: NestJS Auth Controller & Guard
**Dependencies:** Task Group 4

- [ ] 5.0 Implement NestJS auth controller and guard
  - [ ] 5.1 Write 3-4 focused tests for controller and guard
    - Test auth endpoints return correct responses
    - Test protected routes require authentication
    - Test guard rejects invalid tokens
  - [ ] 5.2 Create AuthController
    - POST /auth/login - LoginDto -> TokenResponseDto
    - POST /auth/register - CreateUserDto -> TokenResponseDto
    - POST /auth/refresh - RefreshTokenDto -> TokenResponseDto
  - [ ] 5.3 Update AuthGuard
    - Inject JwtService
    - Extract token from Authorization header
    - Validate token and extract user info
    - Throw UnauthorizedException for invalid tokens
  - [ ] 5.4 Create JwtStrategy for Passport
    - Validate JWT and attach user to request
  - [ ] 5.5 Apply guards to UserController
    - Add @UseGuards(AuthGuard) to protected routes
    - Keep auth endpoints public
  - [ ] 5.6 Ensure controller and guard tests pass

**Acceptance Criteria:**
- Auth endpoints accessible without authentication
- User endpoints require valid JWT
- Invalid tokens result in 401 response
- All 3-4 tests pass

### Cross-Framework Validation

#### Task Group 6: Integration Testing & Parity Verification
**Dependencies:** Task Groups 1-5

- [ ] 6.0 Verify framework parity
  - [ ] 6.1 Run all auth tests for both frameworks
  - [ ] 6.2 Verify token response format matches
    - Both return same JSON structure
    - Both use same expiration times
  - [ ] 6.3 Verify JWT payload parity
    - Generate token in Spring Boot, verify claims
    - Generate token in NestJS, verify claims
    - Both should have identical claim structure
  - [ ] 6.4 Test cross-framework token validation (manual)
    - Token generated by Spring Boot should be verifiable by NestJS
    - Token generated by NestJS should be verifiable by Spring Boot
    - (Uses same secret key)

**Acceptance Criteria:**
- All tests pass in both frameworks
- Response formats are identical
- JWT payloads have same structure
- Tokens are theoretically interchangeable

## Execution Order

Recommended implementation sequence:

1. **Task Group 1** (Spring Boot JWT Infrastructure) - Start here
2. **Task Group 4** (NestJS JWT Enhancement) - Can run parallel to Group 1
3. **Task Group 2** (Spring Boot Auth Endpoints) - After Group 1
4. **Task Group 5** (NestJS Auth Controller) - After Group 4
5. **Task Group 3** (Spring Boot Route Protection) - After Group 2
6. **Task Group 6** (Parity Verification) - After all groups complete

## Files to Create/Modify

### Spring Boot
- `config/JwtConfig.java` - JWT configuration properties (new)
- `service/JwtService.java` - JWT generation and validation (new)
- `controller/AuthController.java` - Auth endpoints (new)
- `service/AuthService.java` - Auth business logic (new)
- `dtos/LoginRequestDTO.java` - Login request (new)
- `dtos/RegisterRequestDTO.java` - Register request (new)
- `dtos/RefreshTokenRequestDTO.java` - Refresh request (new)
- `dtos/TokenResponseDTO.java` - Token response (new)
- `filter/JwtAuthenticationFilter.java` - Request filter (new)
- `config/SecurityConfig.java` - Update with filter chain
- `pom.xml` - Add JJWT dependencies

### NestJS
- `auth/auth.module.ts` - Update configuration
- `auth/auth.service.ts` - Enhance with full auth logic
- `auth/auth.controller.ts` - Auth endpoints (new)
- `auth/dto/login.dto.ts` - Login request (new)
- `auth/dto/refresh-token.dto.ts` - Refresh request (new)
- `auth/dto/token-response.dto.ts` - Token response (new)
- `auth/strategies/jwt.strategy.ts` - Passport strategy (new)
- `common/auth.guard.ts` - Update with real JWT validation
- `user/user.controller.ts` - Add guards to routes
- `package.json` - Add passport dependencies
