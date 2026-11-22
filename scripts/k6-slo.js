// Prueba de carga corta para el SLO de latencia de evaluaciones (docs/sre/slos.md).
// Uso (en el Codespace, con la app arriba):
//   docker run --rm -i --network host -e BASE=http://localhost:8080 -e DEMO_PASSWORD -e EVAL_ID \
//     grafana/k6 run - < scripts/k6-slo.js | tee docs/sre/k6-resultado.txt
import http from 'k6/http';
import { check } from 'k6';
import encoding from 'k6/encoding';

export const options = {
  vus: 10,
  duration: '2m',
  thresholds: {
    'http_req_duration{grupo:eval}': ['p(95)<500'],
    http_req_failed: ['rate<0.005'],
  },
};

const auth = { Authorization: 'Basic ' + encoding.b64encode(`estudiante.a:${__ENV.DEMO_PASSWORD}`) };

export default function () {
  const r = http.get(`${__ENV.BASE}/v1/evaluations/${__ENV.EVAL_ID}`, { headers: auth, tags: { grupo: 'eval' } });
  check(r, { 'status 200': (x) => x.status === 200 });
}
