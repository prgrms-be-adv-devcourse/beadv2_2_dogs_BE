# OPA Hotlist and Bundle Workflow

This project uses a hybrid authorization model:
- JWT carries coarse entitlements (role/status/flags).
- OPA hotlist overrides stale tokens for immediate blocks.

## Hotlist data
Hotlist files live under:
- `baro-cloud/opa/policy/data/hotlist/users.json`
- `baro-cloud/opa/policy/data/hotlist/sellers.json`

Schema:
```json
{
  "users": {
    "USER_ID": {
      "status": "SUSPENDED",
      "flags": ["REVIEW_BLOCKED", "ORDER_BLOCKED"]
    }
  }
}
```

Note:
- `seller_status` defaults to `UNKNOWN` in tokens until seller status is wired.
- OPA treats `UNKNOWN` as allowed and only blocks explicit non-approved status or hotlist entries.

## Update hotlist (local)
```sh
python scripts/opa/update_hotlist.py --user-id 00000000-0000-0000-0000-000000000000 --status SUSPENDED
python scripts/opa/update_hotlist.py --user-id 00000000-0000-0000-0000-000000000000 --flags REVIEW_BLOCKED,ORDER_BLOCKED
python scripts/opa/update_hotlist.py --seller-id 00000000-0000-0000-0000-000000000000 --status SUSPENDED
```

## Build bundle
```sh
python scripts/opa/build_bundle.py
```

By default, the bundle is written to:
`baro-cloud/opa/bundles/gateway.tar.gz`

## Deployment idea (recommended)
- Domain services emit events on state/flag changes.
- A bundle builder consumes events and updates hotlist data.
- OPA pulls the updated bundle every ~5-10 seconds.
