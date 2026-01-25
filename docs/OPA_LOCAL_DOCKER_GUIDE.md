# OPA Local Docker Guide (Windows PowerShell)

This guide documents a reproducible local Docker setup for running
MySQL + Redis + Kafka + Eureka/Config + OPA Bundle + OPA + Gateway +
Auth + Buyer on Windows. It avoids /mnt/s3 mount errors by using a local
override file.

## Local-only files (gitignored)

Create these files in the repo root. They are in .gitignore so every
developer creates their own.

1) .env.docker

```
IMAGE_TAG=local
MYSQL_ROOT_PASSWORD=rootpassword
MYSQL_USER=barouser
MYSQL_PASSWORD=baropassword
MYSQL_DATABASE=barofarm
MYSQL_DATABASE_AUTH=baroauth
MYSQL_DATABASE_BUYER=barobuyer
SPRING_MAIL_USERNAME=
SPRING_MAIL_PASSWORD=
AWS_ACCESS_KEY=
AWS_SECRET_KEY=
AWS_S3_BUCKET_NAME=
```

2) docker-compose.override.local.yml (data: mysql/redis)

```
services:
  redis:
    volumes: []
  mysql:
    volumes:
      - mysql-data:/var/lib/mysql
      - ./scripts/init-db:/docker-entrypoint-initdb.d
```

3) docker-compose.override.local.cloud.yml (cloud services)

```
services:
  eureka:
    volumes: []
  config:
    volumes: []
  gateway:
    volumes: []
  opa-bundle:
    volumes: []
```

4) docker-compose.override.local.kafka.yml (kafka)

```
services:
  kafka:
    volumes:
      - kafka-data:/var/lib/kafka/data
```

5) docker-compose.override.local.buyer.yml (buyer)

```
services:
  baro-buyer:
    volumes: []
```

## (Optional) Build local images

```
./gradlew :baro-cloud:gateway:bootJar :baro-cloud:opa-bundle:bootJar :baro-auth:bootJar :baro-buyer:bootJar

docker build -f docker/gateway/Dockerfile -t ghcr.io/do-develop-space/gateway:local .
docker build -f docker/opa-bundle/Dockerfile -t ghcr.io/do-develop-space/opa-bundle:local .
docker build -f docker/opa/Dockerfile -t ghcr.io/do-develop-space/opa:local .
docker build -f docker/eureka/Dockerfile -t ghcr.io/do-develop-space/eureka:local .
docker build -f docker/config/Dockerfile -t ghcr.io/do-develop-space/config:local .
docker build -f docker/baro-auth/Dockerfile -t ghcr.io/do-develop-space/baro-auth:local .
docker build -f docker/baro-buyer/Dockerfile -t ghcr.io/do-develop-space/baro-buyer:local .
```

```
$env:IMAGE_TAG="local"
```

## Startup order (Kafka included)

```
docker network create be_baro-network

# 1) Data layer: MySQL + Redis + Kafka
docker compose --env-file .env.docker -f docker-compose.data.yml -f docker-compose.override.local.yml up -d mysql redis
docker compose --env-file .env.docker -f docker-compose.kafka.yml -f docker-compose.override.local.kafka.yml up -d

# 2) Service discovery/config
docker compose --env-file .env.docker -f docker-compose.cloud.yml -f docker-compose.override.local.cloud.yml up -d eureka config

# 3) OPA Bundle -> OPA -> Gateway
docker compose --env-file .env.docker -f docker-compose.cloud.yml -f docker-compose.override.local.cloud.yml up -d opa-bundle opa gateway

# 4) Services
docker compose --env-file .env.docker -f docker-compose.auth.yml up -d baro-auth
docker compose --env-file .env.docker -f docker-compose.buyer.yml -f docker-compose.override.local.buyer.yml up -d baro-buyer
```

## Logs (run each in a separate PowerShell window)

```
docker compose -f docker-compose.cloud.yml -f docker-compose.override.local.cloud.yml logs -f opa
docker compose -f docker-compose.cloud.yml -f docker-compose.override.local.cloud.yml logs -f opa-bundle
docker compose -f docker-compose.cloud.yml -f docker-compose.override.local.cloud.yml logs -f gateway
docker compose -f docker-compose.auth.yml logs -f baro-auth
docker compose -f docker-compose.buyer.yml -f docker-compose.override.local.buyer.yml logs -f baro-buyer
docker compose -f docker-compose.kafka.yml -f docker-compose.override.local.kafka.yml logs -f
```

## Quick health check

```
curl.exe http://localhost:8181/health
```
