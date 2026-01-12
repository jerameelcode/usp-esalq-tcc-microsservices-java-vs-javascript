import http from 'k6/http';
import { check, sleep } from 'k6';

// Common HTTP headers
export const jsonHeaders = {
  'Content-Type': 'application/json',
};

// Get auth headers with JWT token
export function authHeaders(token) {
  return {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`,
  };
}

// Register a new user and return tokens
export function registerUser(baseUrl, userData) {
  const payload = JSON.stringify({
    name: userData.name,
    email: userData.email,
    password: userData.password,
  });

  const response = http.post(`${baseUrl}/auth/register`, payload, {
    headers: jsonHeaders,
  });

  const success = check(response, {
    'register status is 201': (r) => r.status === 201,
    'register returns tokens': (r) => {
      const body = r.json();
      return body.accessToken && body.refreshToken;
    },
  });

  if (success) {
    return response.json();
  }
  return null;
}

// Login and return tokens
export function loginUser(baseUrl, email, password) {
  const payload = JSON.stringify({ email, password });

  const response = http.post(`${baseUrl}/auth/login`, payload, {
    headers: jsonHeaders,
  });

  const success = check(response, {
    'login status is 200': (r) => r.status === 200,
    'login returns tokens': (r) => {
      const body = r.json();
      return body.accessToken && body.refreshToken;
    },
  });

  if (success) {
    return response.json();
  }
  return null;
}

// Refresh token
export function refreshToken(baseUrl, refreshToken) {
  const payload = JSON.stringify({ refreshToken });

  const response = http.post(`${baseUrl}/auth/refresh`, payload, {
    headers: jsonHeaders,
  });

  check(response, {
    'refresh status is 200': (r) => r.status === 200,
    'refresh returns new access token': (r) => r.json().accessToken,
  });

  return response;
}

// Get all users (protected route)
export function getUsers(baseUrl, token) {
  const response = http.get(`${baseUrl}/users`, {
    headers: authHeaders(token),
  });

  check(response, {
    'get users status is 200': (r) => r.status === 200,
    'get users returns array': (r) => Array.isArray(r.json()),
  });

  return response;
}

// Get single user (protected route)
export function getUser(baseUrl, token, userId) {
  const response = http.get(`${baseUrl}/users/${userId}`, {
    headers: authHeaders(token),
  });

  check(response, {
    'get user status is 200': (r) => r.status === 200,
    'get user returns user object': (r) => r.json().id === userId,
  });

  return response;
}

// Create user (protected route)
export function createUser(baseUrl, token, userData) {
  const payload = JSON.stringify(userData);

  const response = http.post(`${baseUrl}/users`, payload, {
    headers: authHeaders(token),
  });

  check(response, {
    'create user status is 201 or 200': (r) => r.status === 201 || r.status === 200,
  });

  return response;
}

// Update user (protected route)
export function updateUser(baseUrl, token, userId, userData) {
  const payload = JSON.stringify(userData);

  const response = http.put(`${baseUrl}/users/${userId}`, payload, {
    headers: authHeaders(token),
  });

  check(response, {
    'update user status is 200': (r) => r.status === 200,
  });

  return response;
}

// Delete user (protected route)
export function deleteUser(baseUrl, token, userId) {
  const response = http.del(`${baseUrl}/users/${userId}`, null, {
    headers: authHeaders(token),
  });

  check(response, {
    'delete user status is 200': (r) => r.status === 200,
  });

  return response;
}

// Health check
export function healthCheck(baseUrl, endpoint = '/actuator/health') {
  const response = http.get(`${baseUrl}${endpoint}`);

  check(response, {
    'health check status is 200': (r) => r.status === 200,
  });

  return response;
}

// Random sleep between requests (simulates real user behavior)
export function thinkTime(min = 0.5, max = 2) {
  sleep(Math.random() * (max - min) + min);
}
