# Specification: Exception Handling & Error Responses

## Goal
Implement centralized exception handling with consistent error response format across both Spring Boot and NestJS frameworks, ensuring identical error structures for fair comparison and better developer experience.

## User Stories
- As a developer, I want consistent error responses so that I can handle errors predictably
- As an API consumer, I want meaningful error messages so that I can debug issues quickly
- As a researcher, I want identical error formats in both frameworks so that benchmarks are comparable

## Specific Requirements

**Standard Error Response Format**
Both frameworks must return errors in this exact JSON structure:
```json
{
  "statusCode": 400,
  "message": "Validation failed",
  "error": "Bad Request",
  "timestamp": "2026-01-12T03:30:00.000Z",
  "path": "/users/123"
}
```

**Exception Types to Handle**
- 400 Bad Request: Validation errors, malformed JSON
- 401 Unauthorized: Invalid/missing/expired JWT token
- 403 Forbidden: Insufficient permissions
- 404 Not Found: Resource not found
- 409 Conflict: Duplicate resource (e.g., email already exists)
- 500 Internal Server Error: Unexpected errors

**Spring Boot Implementation**
- Create `@ControllerAdvice` global exception handler
- Handle `MethodArgumentNotValidException` for validation errors
- Handle `ResourceNotFoundException` for 404 errors
- Handle auth exceptions from `AuthService`
- Create standardized `ErrorResponse` record/class

**NestJS Implementation**
- Create global `ExceptionFilter`
- Handle `HttpException` and subclasses
- Handle `ValidationPipe` errors
- Register filter globally in `main.ts`
- Create standardized error response DTO

## Out of Scope
- Custom logging for exceptions (use existing logger)
- Exception tracking/monitoring services
- Internationalization of error messages
