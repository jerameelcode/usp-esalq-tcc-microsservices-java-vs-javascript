# TCC Microservices Implementation Summary

## Project Overview

**Title:** Adoção de Microsserviços com Javascript (NestJS) e Java (Spring Boot): Um Estudo Prático

**Author:** Jerameel João Gonga

**Institution:** MBA em Engenharia de Software – USP/Esalq

**Objective:** Compare NestJS and Spring Boot frameworks for microservices development, analyzing performance, scalability, resource consumption, and security.

---

## Implementation Status

### Completed Features ✅

| # | Feature | Spring Boot | NestJS | Status |
|---|---------|-------------|--------|--------|
| 1 | User Entity & Repository | ✅ | ✅ | Complete |
| 3 | DTO Mapping Layer | ✅ MapStruct | ✅ class-transformer | Complete |
| 4 | PostgreSQL Integration | ✅ | ✅ | Complete |
| 5 | JWT Authentication | ✅ | ✅ | Complete |
| 6 | HATEOAS Implementation | ✅ | ✅ | Complete |
| 7 | Input Validation | ✅ Bean Validation | ✅ class-validator | Complete |
| 8 | Exception Handling | ✅ | ✅ | Complete |
| 10 | Docker Containerization | ✅ | ✅ | Complete |
| 11 | k6 Load Testing Scripts | ✅ | ✅ | Complete |

### Pending Features 📋

| # | Feature | Priority | Effort |
|---|---------|----------|--------|
| 9 | Health Check Endpoints | Low | XS |
| 12 | Performance Benchmark Suite | High | M |
| 14 | Security Hardening | Medium | S |
| 15 | Kubernetes Manifests | Medium | M |
| 16 | Comparative Analysis Report | High | M |

---

## Architecture Comparison

### User Entity Structure

Both frameworks implement identical entity structure:

```
User
├── id: UUID (auto-generated)
├── name: String (required)
├── email: String (required, unique, valid email)
├── password: String (required, min 8 chars, BCrypt hashed)
├── role: Enum (USER, ADMIN) - default: USER
├── status: Enum (ACTIVE, INACTIVE) - default: ACTIVE
├── createdAt: Timestamp (auto-generated)
└── updatedAt: Timestamp (auto-updated)
```

### API Endpoints

| Endpoint | Method | Auth | Spring Boot | NestJS |
|----------|--------|------|-------------|--------|
| `/auth/register` | POST | Public | ✅ | ✅ |
| `/auth/login` | POST | Public | ✅ | ✅ |
| `/auth/refresh` | POST | Public | ✅ | ✅ |
| `/users` | GET | JWT | ✅ | ✅ |
| `/users/:id` | GET | JWT | ✅ | ✅ |
| `/users` | POST | JWT | ✅ | ✅ |
| `/users/:id` | PUT | JWT | ✅ | ✅ |
| `/users/:id` | DELETE | JWT | ✅ | ✅ |
| `/actuator/health` | GET | Public | ✅ | - |
| `/health` | GET | Public | - | ✅ |

### JWT Token Configuration

| Parameter | Value |
|-----------|-------|
| Algorithm | HS256 |
| Access Token Expiration | 15 minutes (900s) |
| Refresh Token Expiration | 7 days (604800s) |
| Secret Key | Shared via `JWT_SECRET` env variable |

### Token Payload Structure

**Access Token:**
```json
{
  "sub": "user-uuid",
  "email": "user@example.com",
  "role": "USER",
  "iat": 1736648400,
  "exp": 1736649300
}
```

**Refresh Token:**
```json
{
  "sub": "user-uuid",
  "type": "refresh",
  "iat": 1736648400,
  "exp": 1737253200
}
```

### Error Response Format

Both frameworks return consistent error responses:

```json
{
  "statusCode": 400,
  "message": "Validation failed: email must be valid",
  "error": "Bad Request",
  "timestamp": "2026-01-12T03:30:00.000Z",
  "path": "/auth/register"
}
```

---

## Technology Stack

### Spring Boot (Java)

| Category | Technology | Version |
|----------|------------|---------|
| Framework | Spring Boot | 4.0.0-M2 |
| Language | Java | 17 |
| Build Tool | Maven | 3.9+ |
| ORM | Spring Data JPA / Hibernate | - |
| Security | Spring Security | - |
| JWT | JJWT | 0.12.6 |
| Validation | Bean Validation (JSR-380) | - |
| Mapping | MapStruct | 1.6.3 |
| Password | BCryptPasswordEncoder | Cost: 10 |
| Database | PostgreSQL | 16 |

### NestJS (TypeScript)

| Category | Technology | Version |
|----------|------------|---------|
| Framework | NestJS | 11.0.1 |
| Language | TypeScript | 5.8+ |
| Runtime | Node.js | 20+ |
| Build Tool | npm/pnpm | - |
| ORM | TypeORM | 0.3.27 |
| Auth | Passport.js + passport-jwt | - |
| JWT | @nestjs/jwt | 10.2.0 |
| Validation | class-validator | 0.14.1 |
| Transformation | class-transformer | 0.5.1 |
| Password | bcrypt | Cost: 10 |
| Database | PostgreSQL | 16 |

---

## Docker Configuration

### Services

| Service | Container | Port | Image |
|---------|-----------|------|-------|
| PostgreSQL | tcc-postgres | 5432 | postgres:16-alpine |
| Spring Boot | tcc-springboot | 8080 | Custom (multi-stage) |
| NestJS | tcc-nestjs | 3000 | Custom (multi-stage) |

### Quick Start

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `PG_USER` | postgres | Database username |
| `PG_PASSWORD` | postgres | Database password |
| `PG_DATABASE` | usp-tcc-microsservices | Database name |
| `JWT_SECRET` | tcc-microsservices-jwt-secret-2026 | JWT signing key |

---

## Load Testing

### k6 Test Scenarios

| Scenario | VUs | Duration | Purpose |
|----------|-----|----------|---------|
| smoke | 1 | 10s | Sanity check |
| load | 10→50 | 3.5min | Normal load |
| stress | 50→200 | 5min | Breaking point |
| spike | 10→100→10 | 2min | Traffic spike |

### Running Benchmarks

```bash
cd k6

# Run both frameworks
./run-benchmarks.sh

# Run specific framework
./run-benchmarks.sh springboot
./run-benchmarks.sh nestjs

# Run stress test
SCENARIO=stress ./run-benchmarks.sh
```

### Metrics Collected

| Metric | Threshold | Description |
|--------|-----------|-------------|
| `http_req_duration p(95)` | < 500ms | 95th percentile latency |
| `http_req_duration p(99)` | < 1000ms | 99th percentile latency |
| `http_req_failed` | < 1% | Error rate |
| `http_reqs` | > 10 req/s | Throughput |

### Custom Metrics

- `springboot_auth_duration` / `nestjs_auth_duration` - Authentication latency
- `springboot_crud_duration` / `nestjs_crud_duration` - CRUD operation latency
- `springboot_errors` / `nestjs_errors` - Framework-specific error rates

---

## Test Coverage

### Spring Boot Tests (24 tests passing)

| Test Class | Tests | Coverage |
|------------|-------|----------|
| UserEntityTest | 6 | Entity, Repository, Timestamps |
| UserServiceTest | 4 | BCrypt, DTO mapping, CRUD |
| JwtServiceTest | 6 | Token generation, validation |
| AuthServiceTest | 7 | Login, register, refresh |
| MicrosserviceApplicationTests | 1 | Context loading |

### NestJS Tests

| Test File | Tests | Coverage |
|-----------|-------|----------|
| user.entity.spec.ts | 6 | Entity, Service, Repository |

---

## File Structure

```
usp-esalq-tcc-microsservices-java-vs-javascript/
├── java/springboot-microsservice/
│   ├── src/main/java/com/gonga/tcc/microsservice/
│   │   ├── config/
│   │   │   ├── JwtConfig.java
│   │   │   └── SecurityConfig.java
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   └── UserController.java
│   │   ├── domain/
│   │   │   ├── User.java
│   │   │   ├── UserRole.java
│   │   │   └── UserStatus.java
│   │   ├── dtos/
│   │   │   ├── LoginRequestDTO.java
│   │   │   ├── RefreshTokenRequestDTO.java
│   │   │   ├── TokenResponseDTO.java
│   │   │   ├── UserRequestDTO.java
│   │   │   └── UserResponseDTO.java
│   │   ├── exceptions/
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   └── ResourceNotFoundException.java
│   │   ├── filter/
│   │   │   └── JwtAuthenticationFilter.java
│   │   ├── mapper/
│   │   │   └── UserMapper.java
│   │   ├── repository/
│   │   │   └── UserRepository.java
│   │   └── service/
│   │       ├── AuthService.java
│   │       ├── JwtService.java
│   │       └── UserService.java
│   ├── Dockerfile
│   └── pom.xml
│
├── javascript/
│   ├── src/
│   │   ├── auth/
│   │   │   ├── dto/
│   │   │   │   ├── login.dto.ts
│   │   │   │   ├── refresh-token.dto.ts
│   │   │   │   └── token-response.dto.ts
│   │   │   ├── strategies/
│   │   │   │   └── jwt.strategy.ts
│   │   │   ├── auth.controller.ts
│   │   │   ├── auth.module.ts
│   │   │   └── auth.service.ts
│   │   ├── common/
│   │   │   ├── auth.guard.ts
│   │   │   ├── hateoas.service.ts
│   │   │   ├── http-exception.filter.ts
│   │   │   └── logger.middleware.ts
│   │   ├── user/
│   │   │   ├── dto/
│   │   │   │   ├── create-user.dto.ts
│   │   │   │   └── user-response.dto.ts
│   │   │   ├── enums/
│   │   │   │   ├── user-role.enum.ts
│   │   │   │   └── user-status.enum.ts
│   │   │   ├── user.controller.ts
│   │   │   ├── user.entity.ts
│   │   │   ├── user.module.ts
│   │   │   └── user.service.ts
│   │   ├── app.controller.ts
│   │   ├── app.module.ts
│   │   └── main.ts
│   ├── Dockerfile
│   └── package.json
│
├── k6/
│   ├── lib/
│   │   ├── config.js
│   │   └── helpers.js
│   ├── tests/
│   │   ├── springboot-api.js
│   │   └── nestjs-api.js
│   ├── run-benchmarks.sh
│   └── README.md
│
├── agent-os/
│   ├── product/
│   │   ├── mission.md
│   │   ├── roadmap.md
│   │   └── tech-stack.md
│   └── specs/
│       ├── 2026-01-12-user-entity-repository/
│       ├── 2026-01-12-jwt-authentication/
│       └── 2026-01-12-exception-handling/
│
├── docker-compose.yml
├── .env.example
└── README.md
```

---

## Git History

### Feature Branch: `feature/microservices-enhancements`

| Commit | Description |
|--------|-------------|
| `eda41af` | Enhanced microservices with JWT auth, entity improvements, exception handling, Docker |
| `a87a62d` | Add k6 load testing scripts for benchmarking |

---

## Next Steps

### For Benchmarking (High Priority)
1. Start services with `docker-compose up -d`
2. Run k6 benchmarks: `cd k6 && ./run-benchmarks.sh`
3. Analyze results in `k6/results/`
4. Generate comparative analysis report

### For Production Readiness
1. Add Health Check Endpoints (Spring Actuator already configured)
2. Create Kubernetes deployment manifests
3. Add security hardening (CORS, rate limiting)
4. Implement monitoring with Prometheus/Grafana

---

## References

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [NestJS Documentation](https://docs.nestjs.com/)
- [k6 Documentation](https://k6.io/docs/)
- [Docker Documentation](https://docs.docker.com/)
- [JWT RFC 7519](https://tools.ietf.org/html/rfc7519)

---

*Document generated: January 12, 2026*

*Implementation by: Rovo Dev AI Assistant*
