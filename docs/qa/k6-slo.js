import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 50 }, // Rampa de subida a 50 usuarios
    { duration: '1m', target: 50 },  // Mantener 50 usuarios
    { duration: '30s', target: 0 },  // Rampa de bajada
  ],
  thresholds: {
    http_req_duration: ['p(95)<300'], // SLO: p95 menor a 300ms
    http_req_failed: ['rate<0.01'],   // SLO: menos del 1% de errores
  },
};

export default function () {
  // Cambia la URL por la de tu aplicación real
  const res = http.get('http://localhost:3000/api/health'); 
  check(res, {
    'status is 200': (r) => r.status === 200,
  });
  sleep(1);
}
