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
  const res = http.get('https://congenial-trout-4qj65jvr6rwr35p4-8080.app.github.dev/actuator/health'); 
  check(res, {
    'status is 200': (r) => r.status === 200,
  });
  sleep(1);
}
