# Spec Requirements: User Entity & Repository Layer

## Initial Description
Implement the User domain entity with JPA annotations (Spring Boot) and TypeORM decorators (NestJS), including repository interfaces for database operations. This is the foundation feature for the microservices framework comparison study.

## Requirements Discussion

### First Round Questions

**Q1:** I assume the User entity should have basic fields like `id`, `name`, `email`, and `password`. Should we also include fields like `createdAt`, `updatedAt`, `role`, or `status`? What fields are essential for your comparison study?
**Answer:** We should also include fields like `createdAt`, `updatedAt`, `role`, and `status`. These fields are essential for the comparison.

**Q2:** For the ID strategy, I'm thinking auto-generated UUIDs would be best for microservices (avoiding conflicts across instances). Is that correct, or would you prefer sequential Long/BigInt IDs?
**Answer:** Auto-generated UUIDs is the preferred ID strategy.

**Q3:** I notice you already have a basic User entity in your Spring Boot project. Should we enhance the existing implementation with additional fields/validations, or keep it simple as-is since this is for benchmarking purposes?
**Answer:** Enhance the existing implementation with additional fields/validations.

**Q4:** For the NestJS side, should the TypeORM entity mirror the Spring Boot/JPA entity exactly (same field names, types, constraints) to ensure fair comparison?
**Answer:** Yes, the TypeORM entity should mirror the Spring Boot/JPA entity exactly for fair comparison.

**Q5:** Regarding password handling, I assume we should store hashed passwords (using BCrypt). Is that correct, or is password handling out of scope for this comparison layer?
**Answer:** Yes, we should store hashed passwords using BCrypt.

**Q6:** For validation, should we implement entity-level constraints (e.g., `@NotNull`, `@Email`, `@Size`) in the domain layer, or defer all validation to the DTO/controller layer?
**Answer:** Implement entity-level constraints in the domain layer.

**Q7:** Is there anything specific you want to exclude from this feature? For example: soft deletes, audit logging, or multi-tenancy support?
**Answer:** Nothing in particular. Since it's a benchmark project, keep it simple.

### Existing Code to Reference

**Similar Features Identified:**
- Feature: User Entity (Java) - Path: `java/springboot-microsservice/src/main/java/com/gonga/tcc/microsservice/domain/User.java`
  - Current fields: `id` (UUID), `name`, `email`, `password`
  - Uses Lombok annotations (@Getter, @Setter, @AllArgsConstructor, @NoArgsConstructor)
  - Entity mapped to `tb_user` table
  
- Feature: User Entity (TypeScript) - Path: `javascript/src/user/user.entity.ts`
  - Current fields: `id` (UUID), `name`, `email` (unique), `password`
  - Uses TypeORM decorators
  - Entity mapped to `tb_user` table

- Feature: UserRepository (Java) - Path: `java/springboot-microsservice/src/main/java/com/gonga/tcc/microsservice/repository/UserRepository.java`
  - Extends JpaRepository<User, UUID>
  - Has custom method: `existsByEmail(String email)`

- Feature: UserService (TypeScript) - Path: `javascript/src/user/user.service.ts`
  - Injects TypeORM Repository<User>
  - Has CRUD methods: findAll, findOne, create, createUser, update, delete

### Follow-up Questions
None required - user provided comprehensive answers.

## Visual Assets

### Files Provided:
No visual assets provided.

### Visual Insights:
N/A

## Requirements Summary

### Functional Requirements
- **User Entity Fields:**
  - `id`: UUID (auto-generated)
  - `name`: String (required)
  - `email`: String (required, unique, valid email format)
  - `password`: String (required, stored as BCrypt hash)
  - `role`: String/Enum (e.g., USER, ADMIN)
  - `status`: String/Enum (e.g., ACTIVE, INACTIVE)
  - `createdAt`: Timestamp (auto-generated on create)
  - `updatedAt`: Timestamp (auto-updated on modification)

- **Entity-Level Validation:**
  - Spring Boot: Use Bean Validation annotations (@NotNull, @Email, @Size, etc.)
  - NestJS: Use class-validator decorators (equivalent constraints)

- **Repository Operations:**
  - Standard CRUD operations (create, read, update, delete)
  - Find by ID
  - Find all users
  - Check if email exists (for uniqueness validation)
  - Both frameworks must have equivalent repository methods

- **Password Handling:**
  - Passwords must be hashed using BCrypt before storage
  - Never return plain-text passwords in responses

### Reusability Opportunities
- Existing User entity implementations in both frameworks serve as the foundation
- Lombok patterns (Java) already established
- TypeORM patterns (TypeScript) already established
- Repository interfaces already exist and can be extended
- Table name `tb_user` already consistent across both implementations

### Scope Boundaries

**In Scope:**
- Enhance User entity with additional fields (role, status, createdAt, updatedAt)
- Add entity-level validation constraints in both frameworks
- Implement BCrypt password hashing
- Mirror implementations exactly between Spring Boot and NestJS
- Ensure repository methods are equivalent

**Out of Scope:**
- Soft deletes
- Audit logging (beyond createdAt/updatedAt)
- Multi-tenancy support
- Complex query methods (keeping it simple for benchmarking)
- User authentication flow (separate feature)

### Technical Considerations
- **Spring Boot Stack:**
  - JPA/Hibernate for ORM
  - Lombok for boilerplate reduction
  - Bean Validation (JSR-380) for constraints
  - Spring Data JPA repository pattern

- **NestJS Stack:**
  - TypeORM for ORM
  - class-validator for validation decorators
  - class-transformer for DTO transformations
  - Repository pattern via TypeORM

- **Database:**
  - PostgreSQL (both frameworks)
  - Table name: `tb_user`
  - UUID primary key strategy

- **Consistency Requirements:**
  - Field names must match exactly
  - Validation constraints must be equivalent
  - Repository method signatures must be comparable
  - BCrypt implementation must use same cost factor for fair comparison
