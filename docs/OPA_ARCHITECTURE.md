# OPA Architecture

## Overview
This document describes how the gateway builds OPA inputs, how policies and data are bundled,
and how hotlist updates flow from services to OPA.[^1]

## Decision API Input Schema
The gateway posts a fixed input schema to the OPA Decision API (`/v1/data/gateway/authz/allow`).[^2]

Example input payload:
```json
{
  "input": {
    "schema_version": 1,
    "request": {
      "method": "GET",
      "path": "/api/orders/123"
    },
    "subject": {
      "id": "user-123",
      "email": "user@example.com",
      "roles": ["CUSTOMER"],
      "user_status": "ACTIVE",
      "seller_status": "UNKNOWN",
      "flags": []
    }
  }
}
```

## Gateway + OPA + Hotlist Flow
```mermaid
flowchart LR
  C[Client] -->|HTTP Request| G

  subgraph GatewayLayer[API Gateway]
    G[Spring Cloud Gateway\nPEP Enforce]
  end

  G -->|POST /v1/data/gateway/authz/allow\ninput: method,path,subject| OPA[OPA PDP]

  OPA -->|result allow/deny| G
  G -->|allow| S1[buyer-service]
  G -->|allow| S2[seller-service]
  G -->|allow| S3[order-service]
  G -->|deny 403| C

  subgraph UpdatePath[Hotlist Update Path Async]
    ADMIN[Admin/Operator] -->|Block/Unblock Command| SS[seller-service SoT]
    SS -->|DB Update| DB[(MySQL SoT)]
    SS -->|Publish HotlistEvent| K[(Kafka Topic opa-hotlist-events)]
    K -->|Consume| BS[opa-bundle-service]
    BS -->|Rebuild bundle.tar.gz| BFILE[(Shared Storage or Local Disk)]
    BS -->|Serve /opa/bundle| BHTTP[Bundle HTTP Endpoint]
    OPA -->|Poll bundle| BHTTP
  end
```

## Scenarios (E-H)
```mermaid
sequenceDiagram
  autonumber
  participant BS as opa-bundle-service
  participant O as OPA
  participant G as Gateway
  participant C as Client

  Note over BS,O: Bundle update exists, download temporarily fails

  O->>BS: GET /opa/bundle (poll)
  BS--x O: 500 or network timeout

  Note over O: OPA keeps last successful bundle in cache for evaluation

  C->>G: HTTP Request (with JWT)
  G->>O: POST /v1/data/gateway/authz/allow
  O-->>G: result = true/false (cached bundle)
  alt allowed
    G-->>C: 200 OK
  else denied
    G-->>C: 403 Forbidden
  end
```

```mermaid
sequenceDiagram
  autonumber
  participant BS as opa-bundle-service
  participant FS as Shared Disk (data.json)
  participant O as OPA

  BS->>FS: Read persisted data.json on startup
  FS-->>BS: data.json content
  BS->>BS: Rebuild bundle.tar.gz
  O->>BS: GET /opa/bundle (poll)
  BS-->>O: 200 bundle.tar.gz (fresh)
  O->>O: Activate new bundle
```

```mermaid
sequenceDiagram
  autonumber
  participant K as Kafka
  participant BS as opa-bundle-service
  participant HL as HotlistStore

  Note over K,BS: Duplicate delivery is possible (at-least-once)[^3]

  K-->>BS: HotlistEvent(eventId=aaa, users:123 BLOCKED)
  BS->>HL: Apply eventId=aaa
  HL-->>BS: applied

  K-->>BS: HotlistEvent(eventId=aaa, users:123 BLOCKED) duplicated
  BS->>HL: Apply eventId=aaa
  HL-->>BS: ignored (already applied)
```

```mermaid
sequenceDiagram
  autonumber
  participant C as Client
  participant G as Gateway
  participant O as OPA

  C->>G: HTTP Request (with JWT)
  G->>O: POST /v1/data/gateway/authz/allow
  O--x G: timeout / connection refused

  alt fail-close (conservative)
    G-->>C: 503 Service Unavailable or 403
  else fail-open (availability)
    G-->>C: 200 OK (bypass)
  end

  Note over G: Critical APIs should still enforce downstream checks
```

[^1]: Bundle polling settings live in `baro-cloud/opa/opa-config.yaml`.
[^2]: Input is built in `baro-cloud/gateway/src/main/java/com/barofarm/gateway/filter/OpaAuthorizationGatewayFilterFactory.java`.
[^3]: `eventId` is optional but recommended for idempotent processing.
