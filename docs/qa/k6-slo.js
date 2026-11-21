import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 50 }, 
    { duration: '1m', target: 50 },  
    { duration: '30s', target: 0 },  
  ],
  thresholds: {
    http_req_duration: ['p(95)<300'], 
    http_req_failed: ['rate<0.01'],   
  },
};

export default function () {
  // ⚠️ LA URL VA AQUÍ ABAJO: !!!!!!!!!!!! VALE REVISAR
  const res = http.get('http://localhost:8080/actuator/health'); 
  check(res, {
    'status is 200': (r) => r.status === 200,
  });
  sleep(1);
}
