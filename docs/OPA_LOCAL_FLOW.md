# OPA Local Flow (Gateway + OPA + Bundle)

## Purpose
This note summarizes how to rebuild and verify the gateway -> OPA flow locally
after policy/data changes, without relying on external tokens.

## Bundle Concepts (Why and How)
OPA does not execute Python scripts. The bundle is a packaged output that OPA
pulls from a URL or loads from disk.

- `scripts/opa/build_bundle.py`:
  Manual/local helper that packages policy + data into a bundle file
  (e.g. `baro-cloud/opa/bundles/gateway.tar.gz`).
- `opa-bundle` service:
  Spring Boot service that builds and serves the bundle via HTTP
  (endpoint: `/opa/bundle`).
- OPA server:
  Pulls the bundle on a schedule using `baro-cloud/opa/opa-config.yaml`.

In short: the Python script is optional for local/manual builds. OPA itself
never calls it automatically.

## What Changed (Context)
- Gateway: add `route.service` in OPA input so policy can be scoped by service.
- OPA policy: `rules_by_id` + `index.method` to avoid full rule scans.
- OPA bundle: rebuilt from updated policy/data.

## Step-by-step (Local Compose)
1) Rebuild OPA bundle from policy/data:
```
python scripts\opa\build_bundle.py
```

2) Rebuild gateway JAR:
```
cd C:\Users\mm206\devcourse\team02-project\beadv2_2_dogs_BE
set GRADLE_USER_HOME=C:\Users\mm206\devcourse\team02-project\beadv2_2_dogs_BE\.gradle-user-home
gradlew :baro-cloud:gateway:bootJar
```

3) Build and tag the gateway image:
```
docker build -f docker\gateway\Dockerfile -t baro-gateway:local .
docker tag baro-gateway:local ghcr.io/do-develop-space/gateway:local
```

4) Restart gateway with the local image:
```
set IMAGE_TAG=local
docker compose -f docker-compose.cloud.yml up -d --no-deps gateway
```

5) Ensure OPA container is running:
```
docker compose -f docker-compose.cloud.yml up -d --no-deps opa
```

6) Check gateway -> OPA connectivity:
```
docker exec baro-gateway wget -qO- http://opa:8181/health
```

7) Send a test request and confirm OPA input:
```
curl.exe -i http://localhost:8080/buyer-service/api/v1/products
docker logs --since 1m baro-opa | findstr "Decision Log"
```

## Expected Verification
OPA decision log should include:
- `input.route.service = "buyer"`
- `input.request.path = "/api/v1/products"`
- `input.subject.roles = ["ANONYMOUS"]` (unless Authorization header is provided)

## Optional: JWT-authenticated check (if you have a token)
```
curl.exe -i -H "Authorization: Bearer <TOKEN>" ^
  http://localhost:8080/buyer-service/api/v1/products
```

## Notes
- If gateway cannot resolve `opa`, confirm OPA container is on `be_baro-network`.
- If `baro-opa` is missing, recreate it:
```
docker rm -f baro-opa
set IMAGE_TAG=local
docker compose -f docker-compose.cloud.yml up -d --no-deps opa
```

## Example Logs
OPA decision log (route.service present, truncated):
```
{"client_addr":"172.19.0.2:58552","level":"info","msg":"Received request.","req_method":"POST","req_path":"/v1/data/gateway/authz/allow","time":"2026-01-11T10:04:00Z"}
{"decision_id":"dd0d1907-2166-4b11-9e60-13d233ccb5d1","level":"info","msg":"Decision Log","path":"gateway/authz/allow","input":{"request":{"method":"GET","path":"/api/v1/products"},"route":{"service":"buyer"},"schema_version":1,"subject":{"roles":["ANONYMOUS"],"user_status":"ACTIVE","seller_status":"UNKNOWN","flags":[]}},"result":false,"time":"2026-01-11T10:04:00Z"}
```

Gateway startup (local image running):
```
2026-01-11T09:49:33.493Z  INFO 1 --- [gateway-service] ... Started GatewayApplication in 7.444 seconds
```
