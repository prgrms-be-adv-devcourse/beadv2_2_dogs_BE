# OPA Bundle Service

This module consumes hotlist events from Kafka, builds an OPA bundle (policy + data),
and serves it over an internal HTTP endpoint.

## Internal access

The bundle endpoint is intended for internal use only. Access is restricted by:

- Private IPv4 ranges or loopback by default
- Optional CIDR allowlist
- Optional shared token (header `X-Internal-Token`)

Example configuration:

```
opa:
  access:
    enabled: true
    allow-private: true
    allowed-cidrs:
      - 10.0.0.0/8
      - 172.16.0.0/12
      - 192.168.0.0/16
    require-token: true
    token: change-me
```

## OPA pull configuration

Example OPA config to pull the bundle (see `baro-cloud/opa/opa-config.yaml`):

```
services:
  opa-bundle:
    url: http://opa-bundle-service:8095
    headers:
      X-Internal-Token: change-me

bundles:
  baro:
    service: opa-bundle
    resource: /opa/bundle
    polling:
      min_delay_seconds: 10
      max_delay_seconds: 60
```

## Kafka event schema

```
{
  "eventId": "evt-20260108-001",
  "subjectType": "user",
  "subjectId": "123",
  "active": true,
  "status": "BLOCKED",
  "flags": ["SUSPENDED"],
  "reason": "manual",
  "updatedAt": "2026-01-08T12:00:00Z"
}
```

## Hotlist data (static bootstrap)

Static hotlist files can seed initial entries and are merged into the generated
`data.json` when the bundle is built:

- `baro-cloud/opa/policy/data/hotlist/users.json`
- `baro-cloud/opa/policy/data/hotlist/sellers.json`

Format (top-level key must match the filename):

```
{
  "users": {
    "123": { "status": "BLOCKED", "flags": ["SUSPENDED"], "reason": "manual", "updatedAt": "2026-01-08T12:00:00Z" }
  }
}
```

Merge rule: dynamic hotlist entries from events take precedence. Static entries
only fill missing subject IDs and do not override existing entries.
