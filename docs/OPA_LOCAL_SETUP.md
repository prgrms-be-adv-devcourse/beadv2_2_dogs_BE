# OPA Local Setup and Memory Tuning

This guide documents how the local OPA + opa-bundle stack was run and how memory
limits were tuned for smaller footprint.

## What is running

- `opa-bundle`: consumes hotlist events, builds an OPA bundle, and serves `/opa/bundle`.
- `opa`: pulls the bundle and answers gateway authorization queries.
- `gateway`: sends authorization requests to OPA.
- `buyer-service`: optional, used to confirm gateway forwarding.
- `eureka`, `config`: supporting infra for gateway/buyer.

## Build artifacts

```
./gradlew :baro-cloud:gateway:bootJar :baro-cloud:opa-bundle:bootJar
./gradlew :baro-buyer:bootJar
```

## Build Docker images (local tags)

```
docker build -f docker/gateway/Dockerfile -t ghcr.io/do-develop-space/gateway:local .
docker build -f docker/opa-bundle/Dockerfile -t ghcr.io/do-develop-space/opa-bundle:local .
docker build -f docker/opa/Dockerfile -t ghcr.io/do-develop-space/opa:local .
docker build -f docker/eureka/Dockerfile -t ghcr.io/do-develop-space/eureka:local .
docker build -f docker/config/Dockerfile -t ghcr.io/do-develop-space/config:local .
docker build -f docker/baro-buyer/Dockerfile -t ghcr.io/do-develop-space/buyer:local .
```

## Start the cloud stack (OPA + gateway)

```
docker network create be_baro-network
$env:IMAGE_TAG="local"
docker compose -f docker-compose.cloud.yml up -d eureka config gateway opa-bundle opa
```

## Start MySQL for buyer-service

The compose file exposes port 3306, which can clash with local MySQL. To avoid
port conflicts, run a temporary MySQL container without host port binding:

```
docker run -d --name baro-mysql-temp --network be_baro-network --network-alias baro-mysql `
  -e MYSQL_ROOT_PASSWORD=rootpassword `
  -e MYSQL_USER=barouser `
  -e MYSQL_PASSWORD=baropassword `
  -e MYSQL_DATABASE=barobuyer `
  mysql:8.0
```

## Start buyer-service

```
docker run -d --name baro-buyer --network be_baro-network `
  -e SPRING_PROFILES_ACTIVE=prod `
  -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka:8761/eureka/ `
  -e SPRING_CONFIG_IMPORT=optional:configserver:http://config:8888 `
  -e MYSQL_USER=barouser `
  -e MYSQL_PASSWORD=baropassword `
  -e MYSQL_DATABASE=barobuyer `
  ghcr.io/do-develop-space/buyer:local
```

## Health and routing checks

```
curl http://localhost:8181/health
```

```
curl -H "X-User-Role: ADMIN" -H "X-User-Id: test" http://localhost:8080/buyer-service/api/v1/products
```

Note: `/buyer-service/actuator/health` is not in policy rules, so it is denied.

## Memory tuning

### opa-bundle (Spring Boot)

`docker-compose.cloud.yml`:

```
environment:
  - JAVA_TOOL_OPTIONS=-Xms32m -Xmx96m -XX:MaxMetaspaceSize=96m -XX:MaxDirectMemorySize=64m -XX:ReservedCodeCacheSize=64m -XX:+UseSerialGC
mem_limit: 200m
```

Notes:
- `JAVA_TOOL_OPTIONS` is applied automatically by the JVM at startup.
- `mem_limit` caps container memory usage.

### opa (Go binary)

`docker-compose.cloud.yml`:

```
mem_limit: 64m
```

OPA memory use is affected by:
- Bundle size (policies + data)
- Query volume and decision log settings

If OPA fails to start under low memory, raise `mem_limit` in small steps (e.g. 96m).

## Monitoring memory

```
docker stats --no-stream --format "table {{.Name}}\t{{.MemUsage}}"
```

## Cleanup

```
docker rm -f baro-buyer baro-mysql-temp
docker compose -f docker-compose.cloud.yml down
```
