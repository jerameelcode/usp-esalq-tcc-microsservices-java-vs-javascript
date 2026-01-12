import { CONFIG, generateUniqueEmail } from '../lib/config.js';
import {
  registerUser,
  loginUser,
  refreshToken,
  getUsers,
  createUser,
  healthCheck,
  thinkTime,
  authHeaders,
  jsonHeaders,
} from '../lib/helpers.js';
import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// Custom metrics for NestJS
const nestjsErrors = new Rate('nestjs_errors');
const nestjsAuthDuration = new Trend('nestjs_auth_duration');
const nestjsCrudDuration = new Trend('nestjs_crud_duration');

// Test configuration
const BASE_URL = CONFIG.nestjs.baseUrl;
const SCENARIO = __ENV.SCENARIO || 'load';

export const options = {
  scenarios: {
    nestjs_test: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: CONFIG.scenarios[SCENARIO].stages || [{ duration: '30s', target: 10 }],
      gracefulRampDown: '10s',
    },
  },
  thresholds: {
    ...CONFIG.thresholds,
    'nestjs_errors': ['rate<0.05'],
    'nestjs_auth_duration': ['p(95)<300'],
    'nestjs_crud_duration': ['p(95)<200'],
  },
  tags: {
    framework: 'nestjs',
    language: 'typescript',
  },
};

export function setup() {
  // Health check before starting tests
  console.log(`Testing NestJS API at ${BASE_URL}`);
  const healthResponse = healthCheck(BASE_URL, '/health');
  
  if (healthResponse.status !== 200) {
    throw new Error(`NestJS API is not healthy: ${healthResponse.status}`);
  }
  
  console.log('NestJS API is healthy, starting load test...');
  
  // Register a test user for the load test
  const testUser = {
    name: 'Load Test Admin',
    email: `admin_${Date.now()}@loadtest.com`,
    password: 'password123',
  };
  
  const tokens = registerUser(BASE_URL, testUser);
  
  return {
    adminToken: tokens ? tokens.accessToken : null,
    adminRefreshToken: tokens ? tokens.refreshToken : null,
    adminEmail: testUser.email,
    adminPassword: testUser.password,
  };
}

export default function(data) {
  // Each VU gets a unique email
  const uniqueEmail = generateUniqueEmail();
  const userData = {
    name: `Test User ${__VU}`,
    email: uniqueEmail,
    password: 'password123',
  };

  group('Authentication Flow', function() {
    // Register new user
    group('Register', function() {
      const startTime = Date.now();
      const payload = JSON.stringify(userData);
      
      const response = http.post(`${BASE_URL}/auth/register`, payload, {
        headers: jsonHeaders,
      });
      
      nestjsAuthDuration.add(Date.now() - startTime);
      
      const success = check(response, {
        'register status is 201': (r) => r.status === 201,
        'register has access token': (r) => r.json().accessToken !== undefined,
        'register has refresh token': (r) => r.json().refreshToken !== undefined,
      });
      
      nestjsErrors.add(!success);
      
      if (success) {
        userData.tokens = response.json();
      }
    });
    
    thinkTime(0.5, 1);
    
    // Login with registered user
    if (userData.tokens) {
      group('Login', function() {
        const startTime = Date.now();
        const payload = JSON.stringify({
          email: userData.email,
          password: userData.password,
        });
        
        const response = http.post(`${BASE_URL}/auth/login`, payload, {
          headers: jsonHeaders,
        });
        
        nestjsAuthDuration.add(Date.now() - startTime);
        
        const success = check(response, {
          'login status is 200': (r) => r.status === 200,
          'login has access token': (r) => r.json().accessToken !== undefined,
        });
        
        nestjsErrors.add(!success);
        
        if (success) {
          userData.tokens = response.json();
        }
      });
    }
    
    thinkTime(0.5, 1);
    
    // Refresh token
    if (userData.tokens && userData.tokens.refreshToken) {
      group('Refresh Token', function() {
        const startTime = Date.now();
        const payload = JSON.stringify({
          refreshToken: userData.tokens.refreshToken,
        });
        
        const response = http.post(`${BASE_URL}/auth/refresh`, payload, {
          headers: jsonHeaders,
        });
        
        nestjsAuthDuration.add(Date.now() - startTime);
        
        const success = check(response, {
          'refresh status is 200': (r) => r.status === 200,
          'refresh has new access token': (r) => r.json().accessToken !== undefined,
        });
        
        nestjsErrors.add(!success);
        
        if (success) {
          userData.tokens.accessToken = response.json().accessToken;
        }
      });
    }
  });

  thinkTime(0.5, 1);

  // CRUD Operations (protected routes)
  if (userData.tokens && userData.tokens.accessToken) {
    group('CRUD Operations', function() {
      const token = userData.tokens.accessToken;
      
      // Get all users
      group('Get Users', function() {
        const startTime = Date.now();
        
        const response = http.get(`${BASE_URL}/users`, {
          headers: authHeaders(token),
        });
        
        nestjsCrudDuration.add(Date.now() - startTime);
        
        const success = check(response, {
          'get users status is 200': (r) => r.status === 200,
        });
        
        nestjsErrors.add(!success);
      });
      
      thinkTime(0.3, 0.8);
    });
  }

  thinkTime(1, 2);
}

export function teardown(data) {
  console.log('NestJS load test completed');
  console.log(`Admin user: ${data.adminEmail}`);
}
