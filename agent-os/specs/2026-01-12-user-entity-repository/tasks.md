# Task Breakdown: User Entity & Repository Layer

## Overview
Total Tasks: 24 (across 4 task groups)

This feature enhances the User entity in both Spring Boot and NestJS microservices with additional fields, validation, and BCrypt password hashing. Tasks are organized to implement equivalent functionality in both frameworks for fair comparison.

## Task List

### Spring Boot Implementation

#### Task Group 1: Spring Boot Entity & Repository Enhancement
**Dependencies:** None

- [x] 1.0 Complete Spring Boot entity layer
  - [x] 1.1 Write 4-6 focused tests for User entity and repository
    - Test entity field validation (name, email, password constraints)
    - Test BCrypt password hashing
    - Test createdAt/updatedAt auto-population
    - Test existsByEmail repository method
  - [x] 1.2 Create UserRole and UserStatus enums
    - UserRole: USER, ADMIN (default: USER)
    - UserStatus: ACTIVE, INACTIVE (default: ACTIVE)
    - Location: `domain/` package
  - [x] 1.3 Enhance User entity with new fields and validations
    - Add `role` field with @Enumerated(EnumType.STRING)
    - Add `status` field with @Enumerated(EnumType.STRING)
    - Add `createdAt` with @CreationTimestamp
    - Add `updatedAt` with @UpdateTimestamp
    - Add @NotBlank to `name`
    - Add @NotBlank, @Email to `email`
    - Add @NotBlank, @Size(min=8) to `password`
    - Add @Column(unique=true) to `email`
  - [x] 1.4 Update UserRepository with any additional query methods
    - Verify existsByEmail works with enhanced entity
    - Add findByEmail method if needed for authentication
  - [x] 1.5 Ensure Spring Boot entity tests pass
    - Run ONLY the 4-6 tests written in 1.1
    - Verify entity validations work correctly

**Acceptance Criteria:**
- User entity has all 8 fields (id, name, email, password, role, status, createdAt, updatedAt)
- Bean Validation annotations enforce constraints
- Timestamps auto-populate correctly
- Enums persist as strings in database
- All 4-6 tests pass

#### Task Group 2: Spring Boot Service & DTO Layer
**Dependencies:** Task Group 1

- [x] 2.0 Complete Spring Boot service layer with BCrypt
  - [x] 2.1 Write 4-6 focused tests for service layer
    - Test password is hashed on user creation
    - Test password is hashed on user update
    - Test role and status are set correctly
    - Test DTO mapping includes new fields
  - [x] 2.2 Add BCrypt password encoding to UserService
    - Inject BCryptPasswordEncoder bean
    - Hash password in createUser method before save
    - Hash password in updateUser method if password changed
    - Use cost factor of 10
  - [x] 2.3 Update UserRequestDTO with new fields and validations
    - Add `role` field (optional, defaults to USER)
    - Add `status` field (optional, defaults to ACTIVE)
    - Add validation annotations matching entity
  - [x] 2.4 Update UserResponseDTO with new fields
    - Add `role`, `status`, `createdAt`, `updatedAt` fields
    - Ensure password is NOT included in response
  - [x] 2.5 Update UserMapper for new fields
    - Map role, status, createdAt, updatedAt in toDTO
    - Handle password hashing separately (not in mapper)
  - [x] 2.6 Ensure Spring Boot service tests pass
    - Run ONLY the 4-6 tests written in 2.1
    - Verify BCrypt hashing works correctly

**Acceptance Criteria:**
- Passwords are BCrypt hashed before storage
- DTOs include all new fields with proper validation
- Mapper correctly transforms between entity and DTOs
- Password never exposed in responses
- All 4-6 tests pass

### NestJS Implementation

#### Task Group 3: NestJS Entity & Repository Enhancement
**Dependencies:** None (can run parallel to Task Group 1)

- [x] 3.0 Complete NestJS entity layer
  - [x] 3.1 Write 4-6 focused tests for User entity and service
    - Test entity field validation (name, email, password constraints)
    - Test BCrypt password hashing
    - Test createdAt/updatedAt auto-population
    - Test existsByEmail functionality
  - [x] 3.2 Create UserRole and UserStatus enums
    - UserRole: USER, ADMIN (default: USER)
    - UserStatus: ACTIVE, INACTIVE (default: ACTIVE)
    - Location: `user/` module or shared enums file
  - [x] 3.3 Enhance User entity with new fields and decorators
    - Add `role` field with @Column({ type: 'enum', enum: UserRole, default: UserRole.USER })
    - Add `status` field with @Column({ type: 'enum', enum: UserStatus, default: UserStatus.ACTIVE })
    - Add `createdAt` with @CreateDateColumn()
    - Add `updatedAt` with @UpdateDateColumn()
    - Add @IsNotEmpty() to `name`
    - Add @IsNotEmpty(), @IsEmail() to `email`
    - Add @IsNotEmpty(), @MinLength(8) to `password`
  - [x] 3.4 Add existsByEmail method to UserService
    - Implement method to check email uniqueness
    - Mirror Spring Boot repository method functionality
  - [x] 3.5 Ensure NestJS entity tests pass
    - Run ONLY the 4-6 tests written in 3.1
    - Verify entity decorators work correctly

**Acceptance Criteria:**
- User entity has all 8 fields matching Spring Boot exactly
- class-validator decorators enforce constraints
- TypeORM timestamps auto-populate correctly
- Enums persist correctly in PostgreSQL
- All 4-6 tests pass

#### Task Group 4: NestJS Service & DTO Layer
**Dependencies:** Task Group 3

- [x] 4.0 Complete NestJS service layer with BCrypt
  - [x] 4.1 Write 4-6 focused tests for service layer
    - Test password is hashed on user creation
    - Test password is hashed on user update
    - Test role and status are set correctly
    - Test DTO transformation includes new fields
  - [x] 4.2 Install and configure bcrypt package
    - Install `bcrypt` and `@types/bcrypt`
    - Use cost factor of 10 (matching Spring Boot)
  - [x] 4.3 Add BCrypt password hashing to UserService
    - Hash password in create method before save
    - Hash password in update method if password changed
    - Import and use bcrypt.hash() with saltRounds=10
  - [x] 4.4 Update CreateUserDto with new fields and validations
    - Add `role` field (optional, defaults to USER)
    - Add `status` field (optional, defaults to ACTIVE)
    - Add class-validator decorators matching entity
  - [x] 4.5 Create UserResponseDto (if not exists)
    - Include all fields except password
    - Add `role`, `status`, `createdAt`, `updatedAt` fields
    - Use class-transformer @Exclude() for password
  - [x] 4.6 Ensure NestJS service tests pass
    - Run ONLY the 4-6 tests written in 4.1
    - Verify BCrypt hashing works correctly

**Acceptance Criteria:**
- Passwords are BCrypt hashed before storage (cost factor 10)
- DTOs include all new fields with proper validation
- Password excluded from responses using class-transformer
- Implementation mirrors Spring Boot exactly
- All 4-6 tests pass

### Cross-Framework Validation

#### Task Group 5: Integration Testing & Parity Verification
**Dependencies:** Task Groups 1-4

- [ ] 5.0 Verify framework parity and integration
  - [ ] 5.1 Run all feature tests for both frameworks
    - Run Spring Boot tests (8-12 tests from groups 1-2)
    - Run NestJS tests (8-12 tests from groups 3-4)
    - Verify all tests pass
  - [ ] 5.2 Verify database schema parity
    - Compare tb_user table structure in both databases
    - Ensure column names, types, and constraints match
    - Verify enum values are stored identically
  - [ ] 5.3 Verify API behavior parity
    - Test create user with same payload in both frameworks
    - Verify response structure matches
    - Confirm BCrypt hash format is compatible
  - [ ] 5.4 Document any implementation differences
    - Note any framework-specific patterns used
    - Document in implementation/ folder if significant

**Acceptance Criteria:**
- All 16-24 tests pass across both frameworks
- Database schemas are equivalent
- API responses have identical structure
- BCrypt implementation uses same cost factor
- Both frameworks handle validation errors consistently

## Execution Order

Recommended implementation sequence:

1. **Task Group 1** (Spring Boot Entity) - Can start immediately
2. **Task Group 3** (NestJS Entity) - Can run parallel to Group 1
3. **Task Group 2** (Spring Boot Service) - After Group 1 complete
4. **Task Group 4** (NestJS Service) - After Group 3 complete
5. **Task Group 5** (Parity Verification) - After Groups 1-4 complete

**Parallel Execution Opportunity:**
- Groups 1 & 3 can be implemented simultaneously
- Groups 2 & 4 can be implemented simultaneously
- This reduces total implementation time significantly

## Files to Modify

### Spring Boot
- `domain/User.java` - Add fields, validations, timestamps
- `domain/UserRole.java` - New enum file
- `domain/UserStatus.java` - New enum file
- `dtos/UserRequestDTO.java` - Add fields, validations
- `dtos/UserResponseDTO.java` - Add new fields
- `mapper/UserMapper.java` - Update mappings
- `service/UserService.java` - Add BCrypt encoding
- `MicrosserviceApplication.java` or config - Add BCryptPasswordEncoder bean

### NestJS
- `user/user.entity.ts` - Add fields, decorators, timestamps
- `user/enums/user-role.enum.ts` - New enum file
- `user/enums/user-status.enum.ts` - New enum file
- `user/dto/create-user.dto.ts` - Add fields, validations
- `user/dto/user-response.dto.ts` - New DTO file
- `user/user.service.ts` - Add BCrypt hashing, existsByEmail
- `package.json` - Add bcrypt dependency
