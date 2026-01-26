package gateway.authz_test

import data.gateway.authz.allow

test_allow_customer_review_create {
  allow with input as {
    "request": {"method": "POST", "path": "/api/v1/products/1/reviews"},
    "subject": {"id": "u1", "roles": ["CUSTOMER"], "flags": []},
    "route": {"service": "support"}
  } with data as base_data
}

test_deny_review_blocked_flag {
  not allow with input as {
    "request": {"method": "POST", "path": "/api/v1/products/1/reviews"},
    "subject": {"id": "u1", "roles": ["CUSTOMER"], "flags": ["REVIEW_BLOCKED"]},
    "route": {"service": "support"}
  } with data as base_data
}

test_deny_user_suspended {
  not allow with input as {
    "request": {"method": "POST", "path": "/api/v1/orders"},
    "subject": {"id": "u1", "roles": ["CUSTOMER"], "user_status": "SUSPENDED"},
    "route": {"service": "order"}
  } with data as base_data
}

test_allow_seller_unknown_status {
  allow with input as {
    "request": {"method": "POST", "path": "/api/v1/products"},
    "subject": {"id": "s1", "roles": ["SELLER"]},
    "route": {"service": "buyer"}
  } with data as base_data
}

test_hotlist_override_blocks_user {
  not allow with input as {
    "request": {"method": "POST", "path": "/api/v1/orders"},
    "subject": {"id": "u2", "roles": ["CUSTOMER"], "user_status": "ACTIVE"},
    "route": {"service": "order"}
  } with data as hotlist_blocked
}

base_data := {
  "support": {
    "customer": {
      "rules_by_id": {
        "support:product-reviews-create": {
          "name": "support:product-reviews-create",
          "path": "^/api/v1/products/[^/]+/reviews$",
          "methods": ["POST"],
          "roles": ["CUSTOMER", "ADMIN"],
          "deny_flags": ["REVIEW_BLOCKED"]
        }
      },
      "index": {
        "method": {
          "POST": ["support:product-reviews-create"]
        }
      }
    }
  },
  "order": {
    "customer": {
      "rules_by_id": {
        "order:orders-create": {
          "name": "order:orders-create",
          "path": "^/api/v1/orders$",
          "methods": ["POST"],
          "roles": ["CUSTOMER", "ADMIN"],
          "deny_flags": ["ORDER_BLOCKED"]
        }
      },
      "index": {
        "method": {
          "POST": ["order:orders-create"]
        }
      }
    }
  },
  "buyer": {
    "seller": {
      "rules_by_id": {
        "buyer:products-create": {
          "name": "buyer:products-create",
          "path": "^/api/v1/products$",
          "methods": ["POST"],
          "roles": ["SELLER", "ADMIN"],
          "deny_flags": ["PRODUCT_PUBLISH_BLOCKED"]
        }
      },
      "index": {
        "method": {
          "POST": ["buyer:products-create"]
        }
      }
    }
  },
  "hotlist": {
    "users": {},
    "sellers": {}
  }
}

hotlist_blocked := {
  "support": base_data.support,
  "order": base_data.order,
  "buyer": base_data.buyer,
  "hotlist": {
    "users": {
      "u2": {"status": "BLOCKED", "flags": []}
    },
    "sellers": {}
  }
}
