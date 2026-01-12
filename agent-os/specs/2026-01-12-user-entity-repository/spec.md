# Specification: User Entity & Repository Layer

## Goal
Enhance the existing User entity in both Spring Boot and NestJS microservices with additional fields (role, status, createdAt, updatedAt), entity-level validation constraints, and BCrypt password hashing to enable comprehensive framework comparison benchmarks.

## User Stories
- As a developer, I want equivalent User entities in both frameworks so that I can fairly compare their performance and patterns
- As a researcher, I want complete CRUD operations with proper validation so that benchmark tests reflect real-world scenarios
- As a system, I want passwords hashed with BCrypt so that security best practices are demonstrated in both implementations

## Specific Requirements

**User Entity Fields**
- Add `role` field as String/Enum with values: USER, ADMIN (default: USER)
- Add `status` field as String/Enum with values: ACTIVE, INACTIVE (default: ACTIVE)
- Add `createdAt` field as Timestamp, auto-populated on entity creation
- Add `updatedAt` field as Timestamp, auto-updated on entity modification
- Keep existing fields: `id` (UUID), `name`, `email`, `password`
- Both frameworks must use identical field names and types for fair comparison

**UUID Primary Key Strategy**
- Spring Boot: Use `@GeneratedValue(strategy = GenerationType.UUID)` (already implemented)
- NestJS: Use `@PrimaryGeneratedColumn('uuid')` (already implemented)
- Ensure UUID generation is handled by the database/framework, not application code

**Entity-Level Validation (Spring Boot)**
- Add `@NotBlank` to `name` field with message
- Add `@NotBlank` and `@Email` to `email` field
- Add `@NotBlank` and `@Size(min=8)` to `password` field
- Add `@Column(unique = true)` to `email` field
- Add `@Enumerated(EnumType.STRING)` for role and status fields

**Entity-Level Validation (NestJS)**
- Add `@IsNotEmpty()` to `name` field
- Add `@IsNotEmpty()` and `@IsEmail()` to `email` field
- Add `@IsNotEmpty()` and `@MinLength(8)` to `password` field
- Add `@Column({ unique: true })` to `email` field (already present)
- Add `@Column({ type: 'enum', enum: UserRole })` for role and status

**BCrypt Password Hashing**
- Implement password hashing before persisting to database
- Spring Boot: Use `BCryptPasswordEncoder` from Spring Security
- NestJS: Use `bcrypt` npm package
- Use cost factor of 10 for both implementations (fair comparison)
- Hash password in service layer before entity creation/update

**Timestamp Auto-Population**
- Spring Boot: Use `@CreationTimestamp` and `@UpdateTimestamp` from Hibernate
- NestJS: Use `@CreateDateColumn()` and `@UpdateDateColumn()` from TypeORM
- Timestamps should be in UTC timezone

**Repository Methods**
- Ensure both repositories have equivalent methods for fair comparison
- Spring Boot: `existsByEmail(String email)` (already exists)
- NestJS: Add `existsByEmail(email: string)` method to service
- Both must support: findAll, findById, save, delete operations

**DTO Updates**
- Update `UserRequestDTO` (Java) to include role and status fields
- Update `CreateUserDto` (TypeScript) to include role and status fields
- Update `UserResponseDTO` (Java) to include all new fields except password
- Add validation decorators to DTOs matching entity constraints
- MapStruct mapper (Java) must map new fields correctly

## Visual Design
No visual assets provided.

## Existing Code to Leverage

**User.java (Spring Boot Entity)**
- Location: `java/springboot-microsservice/src/main/java/com/gonga/tcc/microsservice/domain/User.java`
- Already has UUID id, name, email, password fields with Lombok annotations
- Enhance by adding role, status, createdAt, updatedAt fields
- Add Bean Validation annotations to existing and new fields

**user.entity.ts (NestJS Entity)**
- Location: `javascript/src/user/user.entity.ts`
- Already has UUID id, name, email (unique), password fields
- Enhance by adding role, status, createdAt, updatedAt fields
- Add class-validator decorators and TypeORM column configurations

**UserMapper.java (MapStruct)**
- Location: `java/springboot-microsservice/src/main/java/com/gonga/tcc/microsservice/mapper/UserMapper.java`
- Already maps between User and DTOs
- Update mappings to include new fields (role, status, timestamps)
- Keep password ignored in toEntity mapping

**UserService.java (Spring Boot)**
- Location: `java/springboot-microsservice/src/main/java/com/gonga/tcc/microsservice/service/UserService.java`
- Has createUser, updateUser, partialUpdateUser methods
- Add BCrypt password encoding in createUser and updateUser methods
- Existing existsByEmail check can be reused

**user.service.ts (NestJS)**
- Location: `javascript/src/user/user.service.ts`
- Has CRUD methods: findAll, findOne, create, update, delete
- Add BCrypt password hashing in create and update methods
- Add existsByEmail helper method

## Out of Scope
- Soft delete functionality (no deletedAt field)
- Advanced audit logging beyond createdAt/updatedAt
- Multi-tenancy support
- Password reset or recovery features
- Email verification flow
- User authentication endpoints (separate feature)
- Complex query methods or filtering
- Pagination enhancements (already exists in Spring Boot)
- Database migration scripts (using ddl-auto for development)
- Frontend components or API documentation
