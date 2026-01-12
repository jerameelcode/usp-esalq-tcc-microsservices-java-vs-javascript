# k6 Load Testing for TCC Microservices

Performance benchmark tests for comparing Spring Boot (Java) and NestJS (TypeScript) microservices.

## Prerequisites

1. **Install k6**: https://k6.io/docs/getting-started/installation/
   ```bash
   # macOS
   brew install k6
   
   # Windows
   choco install k6
   
   # Linux
   sudo apt-get install k6
   ```

2. **Start the microservices** (using Docker Compose):
   ```bash
   docker-compose up -d
   ```

## Running Tests

### Quick Start

```bash
cd k6

# Run both frameworks (default: load test)
./run-benchmarks.sh

# Run specific framework
./run-benchmarks.sh springboot
./run-benchmarks.sh nestjs
```

### Test Scenarios

| Scenario | Description | Command |
|----------|-------------|---------|
| `smoke` | Quick sanity check (1 VU, 10s) | `SCENARIO=smoke ./run-benchmarks.sh` |
| `load` | Normal load test (10-50 VUs, 3.5min) | `SCENARIO=load ./run-benchmarks.sh` |
| `stress` | Find breaking point (50-200 VUs, 5min) | `SCENARIO=stress ./run-benchmarks.sh` |
| `spike` | Sudden traffic spike (10→100 VUs) | `SCENARIO=spike ./run-benchmarks.sh` |

### Custom URLs

```bash
SPRINGBOOT_URL=http://localhost:8080 \
NESTJS_URL=http://localhost:3000 \
SCENARIO=load \
./run-benchmarks.sh both
```

### Run Individual Tests Directly

```bash
# Spring Boot only
k6 run --env SCENARIO=smoke tests/springboot-api.js

# NestJS only
k6 run --env SCENARIO=smoke tests/nestjs-api.js
```

## Test Structure

```
k6/
├── lib/
│   ├── config.js      # Shared configuration (scenarios, thresholds)
│   └── helpers.js     # HTTP helpers (auth, CRUD operations)
├── tests/
│   ├── springboot-api.js  # Spring Boot load tests
│   └── nestjs-api.js      # NestJS load tests
├── results/               # Test results (JSON + summaries)
├── run-benchmarks.sh      # Benchmark runner script
└── README.md
```

## Metrics Collected

### Standard k6 Metrics
- `http_req_duration` - Request duration (p95, p99)
- `http_req_failed` - Failed request rate
- `http_reqs` - Requests per second

### Custom Metrics
- `springboot_auth_duration` / `nestjs_auth_duration` - Authentication endpoint latency
- `springboot_crud_duration` / `nestjs_crud_duration` - CRUD operation latency
- `springboot_errors` / `nestjs_errors` - Error rate per framework

## Test Flow

Each virtual user (VU) performs:

1. **Authentication Flow**
   - Register new user → Login → Refresh token

2. **CRUD Operations** (with JWT)
   - Get all users

3. **Think Time**
   - Random delays to simulate real user behavior

## Thresholds (Pass/Fail Criteria)

| Metric | Threshold |
|--------|-----------|
| `http_req_duration p(95)` | < 500ms |
| `http_req_duration p(99)` | < 1000ms |
| `http_req_failed` | < 1% |
| `http_reqs` | > 10 req/s |

## Results

Results are saved to `k6/results/` with timestamps:
- `springboot_load_20260112_120000.json` - Raw metrics
- `springboot_load_20260112_120000_summary.txt` - Human-readable summary

## Comparing Results

After running both tests, compare the results:

```bash
# View summaries
cat results/springboot_*_summary.txt
cat results/nestjs_*_summary.txt
```

Key metrics to compare:
- **Average response time** - Overall performance
- **p95 response time** - Performance under load
- **Requests per second** - Throughput
- **Error rate** - Reliability
