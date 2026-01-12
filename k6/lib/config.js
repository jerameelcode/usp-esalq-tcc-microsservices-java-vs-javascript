// Shared configuration for k6 load tests
// Used for benchmarking Spring Boot vs NestJS microservices

export const CONFIG = {
  // API Base URLs
  springboot: {
    baseUrl: __ENV.SPRINGBOOT_URL || 'http://localhost:8080',
    name: 'Spring Boot (Java)',
  },
  nestjs: {
    baseUrl: __ENV.NESTJS_URL || 'http://localhost:3000',
    name: 'NestJS (TypeScript)',
  },

  // Test user credentials
  testUser: {
    name: 'Load Test User',
    email: `loadtest_${Date.now()}@example.com`,
    password: 'password123',
  },

  // Load test scenarios
  scenarios: {
    // Smoke test - quick sanity check
    smoke: {
      vus: 1,
      duration: '10s',
    },
    // Load test - normal load
    load: {
      stages: [
        { duration: '30s', target: 10 },  // Ramp up to 10 users
        { duration: '1m', target: 10 },   // Stay at 10 users
        { duration: '30s', target: 50 },  // Ramp up to 50 users
        { duration: '1m', target: 50 },   // Stay at 50 users
        { duration: '30s', target: 0 },   // Ramp down
      ],
    },
    // Stress test - find breaking point
    stress: {
      stages: [
        { duration: '30s', target: 50 },
        { duration: '1m', target: 50 },
        { duration: '30s', target: 100 },
        { duration: '1m', target: 100 },
        { duration: '30s', target: 200 },
        { duration: '1m', target: 200 },
        { duration: '30s', target: 0 },
      ],
    },
    // Spike test - sudden traffic spike
    spike: {
      stages: [
        { duration: '10s', target: 10 },
        { duration: '1s', target: 100 },  // Spike!
        { duration: '30s', target: 100 },
        { duration: '1s', target: 10 },   // Scale down
        { duration: '30s', target: 10 },
        { duration: '10s', target: 0 },
      ],
    },
  },

  // Thresholds for pass/fail criteria
  thresholds: {
    http_req_duration: ['p(95)<500', 'p(99)<1000'], // 95% under 500ms, 99% under 1s
    http_req_failed: ['rate<0.01'],                  // Less than 1% failures
    http_reqs: ['rate>10'],                          // More than 10 requests/sec
  },
};

// Helper to generate unique email for each VU
export function generateUniqueEmail() {
  return `loadtest_${__VU}_${Date.now()}@example.com`;
}
