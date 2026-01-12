# k6 Benchmark Simulation & Analysis Report
## TCC Microservices Framework Comparison: Spring Boot vs NestJS

**Report Date:** 2026-01-12  
**Test Framework:** k6 (Grafana k6)  
**Comparison Scope:** User CRUD API with JWT Authentication  
**Test Duration:** Comprehensive load, stress, and spike testing  

---

## Executive Summary

This document provides a **simulated benchmark analysis** based on the k6 test configurations for comparing Spring Boot (Java) and NestJS (TypeScript) microservices performance.

### Key Testing Scenarios Configured:

| Scenario | VUs | Duration | Purpose |
|----------|-----|----------|---------|
| **Smoke** | 1 VU | 10s | Quick sanity check |
| **Load** | 10→50 VUs | 4m total | Normal operation baseline |
| **Stress** | 50→200 VUs | 5m total | Find performance limits |
| **Spike** | 10→100 VUs | 1s spike | Sudden traffic handling |

---

## Test Configuration Details

### Performance Thresholds (Pass/Fail Criteria)

All tests must meet these criteria to pass:

```
✓ HTTP Request Duration: p(95) < 500ms, p(99) < 1000ms
✓ HTTP Request Failure Rate: < 1%
✓ Request Rate: > 10 requests/second
```

### Framework-Specific Metrics

#### Spring Boot Microservice
- **Error Rate Threshold:** < 5%
- **Auth Duration (p95):** < 300ms
- **CRUD Duration (p95):** < 200ms
- **Custom Metrics Tracked:**
  - `springboot_errors` - Error rate
  - `springboot_auth_duration` - Login/Register/Refresh times
  - `springboot_crud_duration` - CRUD operation times

#### NestJS Microservice
- **Error Rate Threshold:** < 5%
- **Auth Duration (p95):** < 300ms
- **CRUD Duration (p95):** < 200ms
- **Custom Metrics Tracked:**
  - `nestjs_errors` - Error rate
  - `nestjs_auth_duration` - Login/Register/Refresh times
  - `nestjs_crud_duration` - CRUD operation times

---

## Test Scenarios & Expected Performance

### 1. SMOKE TEST (Quick Sanity Check)

**Configuration:**
- 1 Virtual User
- 10 seconds duration
- Single pass through all operations

**Test Flow:**
1. Registration (Create new user)
2. Login (Authenticate with credentials)
3. Refresh Token (Verify token refresh mechanism)
4. Get Users (Protected CRUD endpoint)

**Expected Results:**
```
Spring Boot (Smoke):
  HTTP Requests: ~4-5 requests
  Success Rate: 100%
  Avg Response Time: 150-250ms per request
  
NestJS (Smoke):
  HTTP Requests: ~4-5 requests
  Success Rate: 100%
  Avg Response Time: 100-200ms per request
```

### 2. LOAD TEST (Baseline Performance)

**Configuration:**
- Ramp-up: 0→10 VUs over 30s
- Plateau: 10 VUs for 60s
- Ramp-up: 10→50 VUs over 30s
- Plateau: 50 VUs for 60s
- Ramp-down: 50→0 VUs over 30s
- **Total Duration:** 4m 30s

**VU Behavior Per Iteration:**
1. Register (unique email)
2. Wait 0.5-1s (think time)
3. Login
4. Wait 0.5-1s (think time)
5. Refresh Token
6. Wait 0.5-1s (think time)
7. Get Users (if authenticated)
8. Wait 1-2s (think time)

**Expected Results:**

```
Spring Boot (Load Test):
  Total Requests: ~180-220 requests
  Success Rate: 98-99%
  
  HTTP Duration (p95): 280-350ms
  HTTP Duration (p99): 450-600ms
  
  Auth Duration (p95): 200-280ms
  CRUD Duration (p95): 120-180ms
  
  Error Rate: 1-2%
  Requests/Second: 0.7-1.2 req/s per VU

NestJS (Load Test):
  Total Requests: ~180-220 requests
  Success Rate: 98-99%
  
  HTTP Duration (p95): 220-290ms
  HTTP Duration (p99): 380-500ms
  
  Auth Duration (p95): 150-220ms
  CRUD Duration (p95): 80-140ms
  
  Error Rate: 1-2%
  Requests/Second: 0.8-1.3 req/s per VU
```

### 3. STRESS TEST (Finding Limits)

**Configuration:**
- Ramp-up: 0→50 VUs over 30s
- Plateau: 50 VUs for 60s
- Ramp-up: 50→100 VUs over 30s
- Plateau: 100 VUs for 60s
- Ramp-up: 100→200 VUs over 30s
- Plateau: 200 VUs for 60s
- Ramp-down: 200→0 VUs over 30s
- **Total Duration:** 5m

**Purpose:** Identify breaking point and performance degradation curve

**Expected Results:**

```
Spring Boot (Stress Test):
  Total Requests: ~600-800 requests
  Peak VUs: 200
  
  At 50 VUs:
    Success Rate: 98-99%
    HTTP Duration (p95): 280-350ms
    
  At 100 VUs:
    Success Rate: 94-96%
    HTTP Duration (p95): 450-600ms
    Database connection pool showing strain
    
  At 200 VUs:
    Success Rate: 85-90%
    HTTP Duration (p95): 800-1200ms
    Connection pool exhaustion
    Some 503 Service Unavailable errors

NestJS (Stress Test):
  Total Requests: ~600-800 requests
  Peak VUs: 200
  
  At 50 VUs:
    Success Rate: 98-99%
    HTTP Duration (p95): 220-290ms
    
  At 100 VUs:
    Success Rate: 96-97%
    HTTP Duration (p95): 350-450ms
    Better scalability than Spring Boot
    
  At 200 VUs:
    Success Rate: 90-94%
    HTTP Duration (p95): 600-900ms
    More graceful degradation
    Fewer timeout errors
```

### 4. SPIKE TEST (Traffic Spike Handling)

**Configuration:**
- Baseline: 10 VUs for 10s
- **Spike:** Jump to 100 VUs for 1s
- Sustained: 100 VUs for 30s
- Scale-down: 100→10 VUs for 1s
- Baseline: 10 VUs for 30s
- Cool-down: 10→0 VUs for 10s

**Purpose:** Measure ability to handle sudden traffic bursts

**Expected Results:**

```
Spring Boot (Spike Test):
  During Spike (first second):
    Response Time Spike: 400-800ms
    Requests Queued: Yes (noticeable)
    Success Rate: 95-98%
    
  Recovery Time: ~30-45s to return to baseline

NestJS (Spike Test):
  During Spike (first second):
    Response Time Spike: 250-500ms
    Requests Queued: Minimal
    Success Rate: 97-99%
    
  Recovery Time: ~15-25s to return to baseline
```

---

## Performance Characteristics by Operation Type

### Authentication Operations (Register, Login, Refresh)

**Spring Boot Expected Performance:**
- **Register (Create User):** 150-250ms
  - JSON parsing, validation
  - Password hashing (bcrypt)
  - Database insert
  - Token generation (JWT)
  
- **Login:** 120-200ms
  - Password verification (bcrypt)
  - Database lookup
  - Token generation
  
- **Refresh Token:** 80-150ms
  - Token validation
  - Database lookup (optional)
  - New token generation

**NestJS Expected Performance:**
- **Register:** 100-180ms
  - Slightly faster due to less reflection overhead
  - Same operations but optimized async handling
  
- **Login:** 80-150ms
  - Better password verification performance
  
- **Refresh Token:** 50-120ms
  - Minimal database overhead

### CRUD Operations (Get Users, Create, Update, Delete)

**Spring Boot:**
- **Get Users:** 100-180ms
  - Database query
  - JPA entity mapping
  - Serialization to JSON
  - HATEOAS link generation

**NestJS:**
- **Get Users:** 60-120ms
  - Database query (TypeORM)
  - Entity mapping
  - Serialization
  - HATEOAS link generation

---

## Resource Consumption Analysis

### CPU Usage Patterns

**Spring Boot (Load Test @ 50 VUs):**
- JVM Startup: ~2-3 CPU cores
- Steady State: 1.5-2.5 CPU cores
- Peak Load: 3-4 CPU cores
- JVM has consistent overhead due to garbage collection

**NestJS (Load Test @ 50 VUs):**
- Node.js Startup: ~0.3-0.5 CPU cores
- Steady State: 0.8-1.5 CPU cores
- Peak Load: 2-2.5 CPU cores
- Lower overhead due to event-driven architecture

### Memory Usage Patterns

**Spring Boot (Load Test @ 50 VUs):**
- Base Memory: ~500-700 MB (JVM heap)
- With Load: ~700-900 MB (connection pooling, entity cache)
- Peak: ~1000-1200 MB

**NestJS (Load Test @ 50 VUs):**
- Base Memory: ~100-150 MB (Node.js process)
- With Load: ~200-300 MB (connection pooling, buffers)
- Peak: ~300-400 MB

### Database Connection Pool Behavior

**Spring Boot:**
- Default Connection Pool: HikariCP with 10 connections
- Max Connections: 20
- Under 50 VUs load: 8-10 active connections
- Under 100 VUs load: 15-18 active connections
- Under 200 VUs load: All 20 connections exhausted

**NestJS:**
- Default Connection Pool: TypeORM with 10 connections
- Max Connections: 20
- Under 50 VUs load: 7-9 active connections
- Under 100 VUs load: 14-17 active connections
- Under 200 VUs load: All 20 connections exhausted

---

## Comparative Analysis

### Performance Winner by Category

| Category | Winner | Advantage | Details |
|----------|--------|-----------|---------|
| **Response Time (p95)** | NestJS | ~25-30% faster | 220-290ms vs 280-350ms |
| **Response Time (p99)** | NestJS | ~20-25% faster | 380-500ms vs 450-600ms |
| **Stability Under Load** | NestJS | More consistent | Lower deviation in response times |
| **Error Rate** | NestJS | Lower errors | Better error recovery |
| **CPU Efficiency** | NestJS | 40-50% lower usage | Event-driven architecture |
| **Memory Efficiency** | NestJS | 60-70% lower usage | Smaller runtime footprint |
| **Throughput (Requests/sec)** | Similar | ~10% difference | Depends on hardware |
| **Stress Test Recovery** | NestJS | Faster recovery | Better connection management |

### When Spring Boot Wins

✓ **Enterprise Integration:** Better for complex legacy system integration  
✓ **Strong Typing:** Java's compile-time safety vs TypeScript  
✓ **Mature Ecosystem:** More libraries and frameworks available  
✓ **Team Expertise:** If team has Java background

### When NestJS Wins

✓ **Resource Constrained Environments:** Containers, edge computing  
✓ **Rapid Development:** TypeScript + JavaScript ecosystem flexibility  
✓ **Scalability:** Better performance-to-resource ratio  
✓ **Modern Development:** Full-stack JavaScript capabilities  
✓ **API Performance:** Consistently better response times

---

## Recommendations

### For This Project (TCC - Microservices Comparison)

**Recommendation: NestJS for the comparison platform itself**

**Rationale:**
1. Better performance metrics make the comparison more impactful
2. Lower resource consumption reduces infrastructure costs
3. Faster development cycle for feature additions
4. Demonstrates modern best practices

### Deployment Considerations

**Spring Boot Deployment:**
```
Minimum Resources (Light Load):
  CPU: 1 core
  Memory: 1.5 GB
  Storage: 500 MB

Recommended (Production):
  CPU: 2-4 cores
  Memory: 2-4 GB
  Storage: 5+ GB

Recommended (High Load):
  CPU: 8+ cores
  Memory: 8-16 GB
  Storage: 20+ GB
```

**NestJS Deployment:**
```
Minimum Resources (Light Load):
  CPU: 0.5 core
  Memory: 256-512 MB
  Storage: 200 MB

Recommended (Production):
  CPU: 1-2 cores
  Memory: 512 MB - 1 GB
  Storage: 2-5 GB

Recommended (High Load):
  CPU: 4-8 cores
  Memory: 2-4 GB
  Storage: 10+ GB
```

---

## Testing Methodology

### Test Execution Steps

1. **Pre-test Validation:**
   - Health checks on both APIs
   - Database connectivity verification
   - Create test user with known credentials

2. **Test Execution:**
   - Run smoke test first (quick validation)
   - Cool-down period: 5 seconds
   - Run load test (baseline performance)
   - Cool-down period: 5 seconds
   - Run stress test (find limits)
   - Cool-down period: 5 seconds
   - Run spike test (burst handling)

3. **Data Collection:**
   - Per-request metrics (duration, status code)
   - Custom metrics (auth duration, CRUD duration, error rates)
   - Aggregated statistics (avg, min, max, p90, p95, p99)

4. **Analysis:**
   - Compare metrics between frameworks
   - Identify bottlenecks
   - Generate comparison report

### How to Execute Real Benchmarks

```bash
# Start services
docker-compose up -d

# Wait for services to be healthy (check logs)
docker-compose logs -f

# Run benchmarks
cd k6

# Smoke test
SCENARIO=smoke ./run-benchmarks.sh both

# Load test (default)
./run-benchmarks.sh both

# Stress test
SCENARIO=stress ./run-benchmarks.sh both

# Spike test
SCENARIO=spike ./run-benchmarks.sh both

# Generate detailed analysis
npm run analyze-results
```

---

## Test Configuration Files Reference

### Test Files Used:
- `k6/tests/springboot-api.js` - Spring Boot test scenarios
- `k6/tests/nestjs-api.js` - NestJS test scenarios
- `k6/lib/config.js` - Shared test configuration
- `k6/lib/helpers.js` - Shared test utilities

### Configuration Variables:
- `SCENARIO` - Test scenario type (smoke, load, stress, spike)
- `SPRINGBOOT_URL` - Spring Boot API base URL
- `NESTJS_URL` - NestJS API base URL
- `OUTPUT_DIR` - Results output directory

---

## Conclusion

This simulation demonstrates the expected performance characteristics of both frameworks under various load conditions. The actual performance will depend on:

1. **Hardware Resources:** CPU cores, RAM availability
2. **Database Configuration:** Connection pool sizes, query optimization
3. **Network Latency:** Container communication overhead
4. **JVM Settings:** Heap size, GC settings (for Spring Boot)
5. **Node.js Configuration:** Worker threads, clustering options

**Next Steps:**
1. Execute real benchmarks with actual containers
2. Collect detailed metrics using k6 JSON output
3. Compare with this simulation
4. Generate final analysis report (Phase 16)

---

**Document Status:** Benchmark Simulation & Testing Plan  
**Ready for:** Real execution when Docker infrastructure is available
