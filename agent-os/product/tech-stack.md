# Product Tech Stack

This project implements two equivalent microservices for framework comparison purposes. Both stacks are documented below.

---

## Java / Spring Boot Microservice

### Framework & Runtime
- **Application Framework:** Spring Boot 4.0.0-M2
- **Language/Runtime:** Java 17
- **Package Manager:** Maven

### Backend
- **REST Framework:** Spring Web (spring-boot-starter-web)
- **REST Client:** Spring RestClient (spring-boot-starter-restclient)
- **Hypermedia:** Spring HATEOAS (spring-boot-starter-hateoas)
- **Code Generation:** Lombok (boilerplate reduction)
- **Object Mapping:** MapStruct 1.6.3 (DTO mapping)

### Database & Storage
- **Database:** PostgreSQL
- **ORM:** Spring Data JPA with Hibernate
- **Dialect:** PostgreSQLDialect
- **Environment Config:** spring-dotenv 4.0.0

### Security
- **Authentication:** JWT-based (to be implemented with Spring Security)
- **Input Validation:** Bean Validation (JSR-380)

### Testing & Quality
- **Test Framework:** Spring Boot Test (JUnit 5)
- **Load Testing:** k6

### Monitoring & Operations
- **Health Checks:** Spring Actuator (spring-boot-starter-actuator)
- **Development:** Spring DevTools

### Deployment & Infrastructure
- **Containerization:** Docker
- **Orchestration:** Kubernetes (planned)

---

## JavaScript / NestJS Microservice

### Framework & Runtime
- **Application Framework:** NestJS
- **Language/Runtime:** TypeScript / Node.js
- **Package Manager:** pnpm (with npm/package-lock.json support)

### Backend
- **REST Framework:** NestJS Controllers with Express
- **Object Mapping:** class-transformer, class-validator
- **Hypermedia:** Custom HATEOAS service

### Database & Storage
- **Database:** PostgreSQL
- **ORM:** TypeORM
- **Entity Management:** TypeORM decorators

### Security
- **Authentication:** JWT-based with Passport.js
- **Guards:** Custom Auth Guards
- **Input Validation:** class-validator with ValidationPipe

### Testing & Quality
- **Test Framework:** Jest
- **E2E Testing:** Supertest
- **Linting:** ESLint
- **Formatting:** Prettier
- **Load Testing:** k6

### Monitoring & Operations
- **Logging:** Custom Logger Middleware
- **Health Checks:** NestJS Terminus (planned)

### Deployment & Infrastructure
- **Containerization:** Docker
- **Orchestration:** Kubernetes (planned)

---

## Shared Infrastructure

### Database
- **Primary Database:** PostgreSQL (shared instance for development, separate for benchmarks)

### Testing Tools
- **Load Testing:** k6 (Grafana k6)
- **API Testing:** Insomnia, Postman
- **Benchmarking:** Apache Benchmark (ab)

### Development Environment
- **Containerization:** Docker Desktop
- **Container Orchestration:** Docker Compose (local), Kubernetes (production)
- **Version Control:** Git

### Monitoring & Observability (Planned)
- **Metrics:** Prometheus
- **Visualization:** Grafana
- **Logging:** Elastic Stack (ELK)

---

## Framework Comparison Summary

| Aspect | Spring Boot (Java) | NestJS (TypeScript) |
|--------|-------------------|---------------------|
| Language | Java 17 | TypeScript |
| Runtime | JVM | Node.js |
| ORM | Spring Data JPA / Hibernate | TypeORM |
| Security | Spring Security | Passport.js |
| Validation | Bean Validation | class-validator |
| DTO Mapping | MapStruct | class-transformer |
| Testing | JUnit 5 | Jest |
| Build Tool | Maven | pnpm/npm |
