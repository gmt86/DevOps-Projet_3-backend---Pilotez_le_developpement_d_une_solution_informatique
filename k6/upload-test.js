import http from 'k6/http';
import { check, sleep } from 'k6';

/**
 * Test de performance — endpoint upload fichier
 */
export const options = {
  vus: 10,
  duration: '30s',
  thresholds: {
    http_req_duration: ['p(95)<2000'],
    http_req_failed: ['rate<0.1'],
  },
};

const BASE_URL = 'http://localhost:8080';

/**
 * Setup — création du compte de test.
 */
export function setup() {
  http.post(`${BASE_URL}/api/auth/register`, JSON.stringify({
    email: 'perf@datashare.com',
    password: 'password123'
  }), {
    headers: { 'Content-Type': 'application/json' }
  });
}

/**
 * Test principal — login + upload.
 */
export default function () {
  // Connexion à chaque itération
  const loginResponse = http.post(`${BASE_URL}/api/auth/login`, JSON.stringify({
    email: 'perf@datashare.com',
    password: 'password123'
  }), {
    headers: { 'Content-Type': 'application/json' }
  });

  check(loginResponse, {
    'login status 200': (r) => r.status === 200,
  });

  const token = JSON.parse(loginResponse.body).accessToken;

  // Date expiration
  const dateExpiration = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000)
    .toISOString()
    .slice(0, 19);

  // Upload fichier via multipart
const data = {
  fichier: http.file('contenu test performance', 'test.txt', 'text/plain'),
  request: http.file(
    JSON.stringify({ dateExpiration: dateExpiration }), 
    'request.json', 
    'application/json'  
  )
};

  const uploadResponse = http.post(`${BASE_URL}/api/fichiers`, data, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });

  check(uploadResponse, {
    'upload status 201': (r) => r.status === 201,
    'response time < 2s': (r) => r.timings.duration < 2000,
  });

  sleep(1);
}