// docker compose run --rm k6 run `
//   >>   --out influxdb=http://influxdb:8086/k6 `
//   >>   /scripts/orders_get_smoke.js

import http from "k6/http";
import { check, sleep } from "k6";

export const options = {
  stages: [
    { duration: "2m", target: 10 },  // 2분 동안 10명까지 증가
    { duration: "3m", target: 30 },  // 3분 동안 30명까지 증가
    { duration: "3m", target: 50 },  // 3분 동안 50명까지 증가
    { duration: "2m", target: 50 },  // 2분 유지
  ],
};

export default function () {
  const res = http.get("http://host.docker.internal:8081/api/v1/orders", {
    headers: { Accept: "application/json" },
  });

  check(res, {
    "status is 200": (r) => r.status === 200,
  });

  sleep(1);
}
