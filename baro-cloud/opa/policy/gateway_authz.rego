package gateway.authz

# Input schema (from gateway):
# input.schema_version, input.request.method, input.request.path,
# input.subject.id, input.subject.roles, input.subject.user_status,
# input.subject.seller_status, input.subject.flags.



default allow := false

# Allow preflight(사전요청, 예비요청) CORS requests.
allow {
  upper(input.request.method) == "OPTIONS"
}

allow {
  rule := candidate_rules[_]
  path_matches(rule.path, input.request.path)
  method_matches(rule.methods, input.request.method)
  role_matches(rule.roles, user_roles)
  # Coarse-grained checks at the gateway (status/flags) before services run logic.
  status_allowed(rule)
  flags_allowed(rule)
}

# Service-scoped, method-indexed rule lookup.
candidate_rules := [rule |
  bucket := buckets[_]
  rule := bucket_rules(bucket)[_]
]

bucket_rules(bucket) := [rule |
  id := candidate_ids(bucket)[_]
  rule := bucket.rules_by_id[id]
]

candidate_ids(bucket) := ids {
  ids := bucket.index.method[upper(input.request.method)]
} else := ids {
  ids := bucket.index.method["*"]
} else := []

buckets := all_buckets {
  # [1] Recursion issue: buckets -> service_present -> policy_buckets -> buckets
  #     Fix by removing service_present and avoiding variable data lookups.
  service_buckets := policy_buckets
  common_buckets := common_policy_buckets
  all_buckets := array.concat(service_buckets, common_buckets)
} else := []

policy_buckets := service_buckets {
  # [2] Recursion issue: variable data lookups (data[service]) can resolve to
  #     policy packages like data.gateway.authz.* and create cycles.
  service := input.route.service
  svc_data := service_data(service)
  svc_data != null
  service_buckets := [bucket |
    name := object.keys(svc_data)[_]
    bucket := svc_data[name]
    bucket.rules_by_id
  ]
} else := []

common_policy_buckets := common_buckets {
  # [3] Recursion issue: keep common buckets independent from policy buckets.
  data.common
  common_buckets := [bucket |
    name := object.keys(data.common)[_]
    bucket := data.common[name]
    bucket.rules_by_id
  ]
} else := []

service_data(service) := data.auth {
  # [4] Use explicit data roots to avoid variable data lookups and recursion.
  service == "auth"
} else := data.buyer {
  service == "buyer"
} else := data.seller {
  service == "seller"
} else := data.order {
  service == "order"
} else := data.support {
  service == "support"
} else := null


user_roles := roles {
  roles := input.subject.roles
  count(roles) > 0
} else := ["ANONYMOUS"]

status_allowed(rule) {
  not requires_auth(rule)
} else {
  # Blocked users are denied regardless of route role.
  not user_blocked
  seller_allowed(rule)
}

requires_auth(rule) {
  not role_public(rule)
}

role_public(rule) {
  "*" == rule.roles[_]
}

user_blocked {
  blocked_status(subject_user_status)
}

user_blocked {
  blocked_status(hotlist_user_status)
}

blocked_status(status) {
  status == "SUSPENDED"
} else {
  status == "BLOCKED"
} else {
  status == "WITHDRAWN"
}

subject_user_status := status {
  status := input.subject.user_status
} else := "ACTIVE"

hotlist_user_status := status {
  input.subject.id != null
  status := data.hotlist.users[input.subject.id].status
}

seller_allowed(rule) {
  not requires_seller_approval(rule)
} else {
  # Seller routes require APPROVED status when known. UNKNOWN is treated as allowed
  # until seller status is wired from token or hotlist.
  allowed := {"APPROVED", "UNKNOWN"}
  allowed[effective_seller_status]
}

requires_seller_approval(rule) {
  has_role("SELLER")
  "SELLER" == rule.roles[_]
}

has_role(role) {
  role == input.subject.roles[_]
}

effective_seller_status := status {
  status := hotlist_seller_status
} else := subject_seller_status

subject_seller_status := status {
  status := input.subject.seller_status
} else := "UNKNOWN"

hotlist_seller_status := status {
  input.subject.id != null
  status := data.hotlist.sellers[input.subject.id].status
}

flags_allowed(rule) {
  not flag_denied(rule)
}

flag_denied(rule) {
  deny_flags := rule_deny_flags(rule)
  f := deny_flags[_]
  f == effective_flags[_]
}

rule_deny_flags(rule) := flags {
  flags := rule.deny_flags
} else := []

subject_flags := flags {
  flags := input.subject.flags
} else := []

hotlist_user_flags := flags {
  input.subject.id != null
  flags := data.hotlist.users[input.subject.id].flags
} else := []

effective_flags := array.concat(subject_flags, hotlist_user_flags)

path_matches(pattern, path) {
  re_match(pattern, path)
}

method_matches(methods, method) {
  m := methods[_]
  m == "*"
}

method_matches(methods, method) {
  m := methods[_]
  upper(m) == upper(method)
}

role_matches(roles, user_roles) {
  r := roles[_]
  r == "*"
}

role_matches(roles, user_roles) {
  r := roles[_]
  r == user_roles[_]
}

# hotlist에서 승인(APPROVED)된 판매자는 토큰 역할이 갱신되기 전에도 SELLER 권한을 인정한다.
role_matches(roles, user_roles) {
  r := roles[_]
  r == "SELLER"
  hotlist_seller_status == "APPROVED"
}
