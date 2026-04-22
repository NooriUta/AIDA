#!/usr/bin/env bash
# init-arcadedb.sh — идемпотентная инициализация ArcadeDB баз для AIDA dev-стека.
#
# Запускается автоматически через `npm run predev` в bff/chur.
# Также пригоден для ручного запуска и CI/CD.
#
# Env-переменные (все опциональны — defaults рассчитаны на локальный dev):
#   FRIGG_URL   FRIGG_USER   FRIGG_PASS
#   INIT_ARCADEDB_TIMEOUT  (сек; default 30 — скрипт не блокирует если Docker не запущен)

set -euo pipefail

FRIGG_URL="${FRIGG_URL:-http://localhost:2481}"
FRIGG_USER="${FRIGG_USER:-root}"
FRIGG_PASS="${FRIGG_PASS:-playwithdata}"

TIMEOUT="${INIT_ARCADEDB_TIMEOUT:-30}"
INTERVAL=2

log() { echo "[init-arcadedb] $*"; }

wait_ready() {
  local url="$1" user="$2" pass="$3" label="$4"
  local elapsed=0
  log "Waiting for $label ($url)..."
  until curl -sf --max-time 3 -u "$user:$pass" "$url/api/v1/ready" -o /dev/null 2>/dev/null; do
    if [ "$elapsed" -ge "$TIMEOUT" ]; then
      log "SKIP: $label not available after ${TIMEOUT}s — databases not created."
      return 1
    fi
    sleep "$INTERVAL"
    elapsed=$((elapsed + INTERVAL))
  done
  log "$label is ready."
  return 0
}

ensure_db() {
  local base_url="$1" user="$2" pass="$3" db="$4"
  local dbs
  dbs=$(curl -sf --max-time 5 -u "$user:$pass" "$base_url/api/v1/databases" 2>/dev/null || echo "")
  if echo "$dbs" | grep -q "\"$db\""; then
    log "  '$db' — already exists, skip."
  else
    local result
    result=$(curl -sf --max-time 5 -u "$user:$pass" -X POST "$base_url/api/v1/server" \
      -H "Content-Type: application/json" \
      -d "{\"command\":\"create database $db\"}" 2>/dev/null || echo "error")
    if echo "$result" | grep -q '"ok"'; then
      log "  '$db' — created."
    else
      log "  '$db' — unexpected response: $result"
    fi
  fi
}

# ── FRIGG (port 2481 dev / frigg:2480 docker) ─────────────────────────────────
# Базы которые нужны в FRIGG:
#   frigg-sessions  — Chur session store (ArcadeDbSessionStore)
#   dali            — Dali async job store (JobRunr)
#   heimdall        — HEIMDALL snapshot persistence

run_sql() {
  local base_url="$1" user="$2" pass="$3" db="$4" cmd="$5"
  local body
  body=$(python -c 'import json,sys; print(json.dumps({"language":"sql","command":sys.argv[1]}))' "$cmd")
  curl -sf --max-time 10 -u "$user:$pass" -X POST "$base_url/api/v1/command/$db" \
    -H "Content-Type: application/json" --data "$body" >/dev/null 2>&1 || true
}

ensure_default_tenant() {
  local base_url="$1" user="$2" pass="$3"
  log "Ensuring DaliTenantConfig schema in frigg-tenants:"
  run_sql "$base_url" "$user" "$pass" "frigg-tenants" "CREATE VERTEX TYPE DaliTenantConfig IF NOT EXISTS"
  run_sql "$base_url" "$user" "$pass" "frigg-tenants" "CREATE PROPERTY DaliTenantConfig.tenantAlias IF NOT EXISTS STRING"
  run_sql "$base_url" "$user" "$pass" "frigg-tenants" "CREATE INDEX IF NOT EXISTS ON DaliTenantConfig (tenantAlias) UNIQUE"

  # Idempotent upsert of the "default" tenant record for single-tenant dev deployments
  local row
  row=$(curl -sf --max-time 5 -u "$user:$pass" -X POST "$base_url/api/v1/query/frigg-tenants" \
    -H "Content-Type: application/json" \
    -d '{"language":"sql","command":"SELECT tenantAlias FROM DaliTenantConfig WHERE tenantAlias = '\''default'\'' LIMIT 1"}' 2>/dev/null || echo '{"result":[]}')
  if echo "$row" | grep -q '"default"'; then
    log "  DaliTenantConfig 'default' — already present, skip."
    return
  fi
  log "  DaliTenantConfig 'default' — inserting."
  local ts; ts=$(date +%s000)
  run_sql "$base_url" "$user" "$pass" "frigg-tenants" \
    "INSERT INTO DaliTenantConfig SET tenantAlias = 'default', status = 'ACTIVE', configVersion = 1, yggLineageDbName = 'hound_default', yggSourceArchiveDbName = 'hound_src_default', friggDaliDbName = 'dali_default', harvestCron = '0 0 */6 * * ?', llmMode = 'off', dataRetentionDays = 30, maxParseSessions = 10, maxAtoms = 10000, maxSources = 5, maxConcurrentJobs = 2, createdAt = $ts, updatedAt = $ts"
}

# MTN-51 / ADR-MT-004: heimdall.ControlEvent — durable append-only store for
# seer.control.* events (tenant_invalidated, suspend, archive, purge).
# Append-only enforced at the vertex type level (the trigger lands with MTN-60
# when the pipeline wires up). Idempotency via UNIQUE(id); consumer offsets
# are in-memory (per-JVM) and persisted by replay-from-id on restart.
ensure_heimdall_control_event_schema() {
  local base_url="$1" user="$2" pass="$3"
  local db="heimdall"
  log "Ensuring heimdall.ControlEvent schema (MTN-51):"
  run_sql "$base_url" "$user" "$pass" "$db" "CREATE VERTEX TYPE ControlEvent IF NOT EXISTS"
  run_sql "$base_url" "$user" "$pass" "$db" "CREATE PROPERTY ControlEvent.id IF NOT EXISTS STRING"
  run_sql "$base_url" "$user" "$pass" "$db" "CREATE PROPERTY ControlEvent.tenantAlias IF NOT EXISTS STRING"
  run_sql "$base_url" "$user" "$pass" "$db" "CREATE PROPERTY ControlEvent.eventType IF NOT EXISTS STRING"
  run_sql "$base_url" "$user" "$pass" "$db" "CREATE PROPERTY ControlEvent.fenceToken IF NOT EXISTS LONG"
  run_sql "$base_url" "$user" "$pass" "$db" "CREATE PROPERTY ControlEvent.schemaVersion IF NOT EXISTS INTEGER"
  run_sql "$base_url" "$user" "$pass" "$db" "CREATE PROPERTY ControlEvent.createdAt IF NOT EXISTS LONG"
  run_sql "$base_url" "$user" "$pass" "$db" "CREATE PROPERTY ControlEvent.payload IF NOT EXISTS STRING"
  run_sql "$base_url" "$user" "$pass" "$db" "CREATE INDEX IF NOT EXISTS ON ControlEvent (id) UNIQUE"
  run_sql "$base_url" "$user" "$pass" "$db" "CREATE INDEX IF NOT EXISTS ON ControlEvent (tenantAlias) NOTUNIQUE"
  run_sql "$base_url" "$user" "$pass" "$db" "CREATE INDEX IF NOT EXISTS ON ControlEvent (createdAt) NOTUNIQUE"
  run_sql "$base_url" "$user" "$pass" "$db" "CREATE INDEX IF NOT EXISTS ON ControlEvent (fenceToken) NOTUNIQUE"
}

# MTN-65: frigg-users schema — 8 vertex types for user-level application data.
# Per Q-UA-1 (2026-04-22): separate DB (not namespace) for backup granularity +
# IAM isolation + GDPR export. Per Q-UA-5: include nullable reserved_acl_v2 hook
# in every vertex for future per-resource ACL without later schema migration.
ensure_frigg_users_schema() {
  local base_url="$1" user="$2" pass="$3"
  local db="frigg-users"
  log "Ensuring frigg-users schema (8 vertex types + indices):"
  local types=(
    UserProfile
    UserPreferences
    UserNotifications
    UserConsents
    UserSourceBindings
    UserApplicationState
    UserLifecycle
    UserSessionEvents
  )
  for t in "${types[@]}"; do
    run_sql "$base_url" "$user" "$pass" "$db" "CREATE VERTEX TYPE $t IF NOT EXISTS"
    run_sql "$base_url" "$user" "$pass" "$db" "CREATE PROPERTY $t.userId IF NOT EXISTS STRING"
    run_sql "$base_url" "$user" "$pass" "$db" "CREATE PROPERTY $t.configVersion IF NOT EXISTS INTEGER"
    run_sql "$base_url" "$user" "$pass" "$db" "CREATE PROPERTY $t.updatedAt IF NOT EXISTS LONG"
    run_sql "$base_url" "$user" "$pass" "$db" "CREATE PROPERTY $t.reserved_acl_v2 IF NOT EXISTS STRING"
  done
  # UserProfile / UserPreferences / UserNotifications / UserLifecycle / UserApplicationState:
  #   one row per user → UNIQUE(userId)
  for t in UserProfile UserPreferences UserNotifications UserLifecycle UserApplicationState; do
    run_sql "$base_url" "$user" "$pass" "$db" "CREATE INDEX IF NOT EXISTS ON $t (userId) UNIQUE"
  done
  # UserConsents / UserSessionEvents: append-only, many rows per user → NOTUNIQUE
  for t in UserConsents UserSessionEvents; do
    run_sql "$base_url" "$user" "$pass" "$db" "CREATE INDEX IF NOT EXISTS ON $t (userId) NOTUNIQUE"
  done
  # UserSourceBindings: (userId, tenantAlias) composite — one binding per user per tenant
  run_sql "$base_url" "$user" "$pass" "$db" "CREATE PROPERTY UserSourceBindings.tenantAlias IF NOT EXISTS STRING"
  run_sql "$base_url" "$user" "$pass" "$db" "CREATE INDEX IF NOT EXISTS ON UserSourceBindings (userId, tenantAlias) UNIQUE"
  # UserSessionEvents: index ts for retention purge (MTN-64 180d) + tenantAlias for admin view
  run_sql "$base_url" "$user" "$pass" "$db" "CREATE PROPERTY UserSessionEvents.ts IF NOT EXISTS LONG"
  run_sql "$base_url" "$user" "$pass" "$db" "CREATE PROPERTY UserSessionEvents.tenantAlias IF NOT EXISTS STRING"
  run_sql "$base_url" "$user" "$pass" "$db" "CREATE INDEX IF NOT EXISTS ON UserSessionEvents (ts) NOTUNIQUE"
  # UserLifecycle: index dataRetentionUntil for hard-delete scheduler (MTN-61)
  run_sql "$base_url" "$user" "$pass" "$db" "CREATE PROPERTY UserLifecycle.dataRetentionUntil IF NOT EXISTS LONG"
  run_sql "$base_url" "$user" "$pass" "$db" "CREATE INDEX IF NOT EXISTS ON UserLifecycle (dataRetentionUntil) NOTUNIQUE"
}

# KC-ORG-02: sync keycloakOrgId from Keycloak Organizations API into DaliTenantConfig
# Requires KC Organizations feature enabled (KC-ORG-01: docker-compose KC_FEATURES=organization).
# Idempotent: skips if orgId already written to frigg-tenants.
sync_default_keycloak_org_id() {
  local frigg_url="$1" frigg_user="$2" frigg_pass="$3"
  local kc_url="${KC_URL:-http://localhost:18180/kc}"
  local kc_admin_user="${KC_ADMIN_USER:-admin}"
  local kc_admin_pass="${KC_ADMIN_PASS:-admin}"

  # Skip if already synced
  local existing
  existing=$(curl -sf --max-time 5 -u "$frigg_user:$frigg_pass" -X POST "$frigg_url/api/v1/query/frigg-tenants" \
    -H "Content-Type: application/json" \
    -d '{"language":"sql","command":"SELECT keycloakOrgId FROM DaliTenantConfig WHERE tenantAlias = '\''default'\''"}' 2>/dev/null || echo '{"result":[]}')
  if echo "$existing" | python -c 'import json,sys; r=json.load(sys.stdin)["result"]; sys.exit(0 if r and r[0].get("keycloakOrgId") else 1)' 2>/dev/null; then
    log "  keycloakOrgId for 'default' — already synced, skip."
    return
  fi

  # Get admin token
  local token
  token=$(curl -sf --max-time 5 -X POST "$kc_url/realms/master/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "grant_type=password&client_id=admin-cli&username=$kc_admin_user&password=$kc_admin_pass" 2>/dev/null \
    | python -c 'import json,sys; print(json.load(sys.stdin)["access_token"])' 2>/dev/null || echo "")
  if [ -z "$token" ]; then
    log "  Keycloak unavailable — skipping keycloakOrgId sync."
    return
  fi

  # Find default org
  local org_id
  org_id=$(curl -sf --max-time 5 -H "Authorization: Bearer $token" \
    "$kc_url/admin/realms/seer/organizations?search=default" 2>/dev/null \
    | python -c 'import json,sys; orgs=json.load(sys.stdin); d=[o for o in orgs if o.get("alias")=="default"]; print(d[0]["id"] if d else "")' 2>/dev/null || echo "")
  if [ -z "$org_id" ]; then
    log "  Keycloak org 'default' not found — skip (check KC-ORG-02 realm import)."
    return
  fi

  log "  Syncing keycloakOrgId=$org_id to DaliTenantConfig 'default'."
  run_sql "$frigg_url" "$frigg_user" "$frigg_pass" "frigg-tenants" \
    "UPDATE DaliTenantConfig SET keycloakOrgId = '$org_id' WHERE tenantAlias = 'default'"
}

if wait_ready "$FRIGG_URL" "$FRIGG_USER" "$FRIGG_PASS" "FRIGG"; then
  log "Ensuring FRIGG databases:"
  ensure_db "$FRIGG_URL" "$FRIGG_USER" "$FRIGG_PASS" "frigg-sessions"
  ensure_db "$FRIGG_URL" "$FRIGG_USER" "$FRIGG_PASS" "dali"
  ensure_db "$FRIGG_URL" "$FRIGG_USER" "$FRIGG_PASS" "heimdall"
  ensure_db "$FRIGG_URL" "$FRIGG_USER" "$FRIGG_PASS" "frigg-tenants"
  ensure_db "$FRIGG_URL" "$FRIGG_USER" "$FRIGG_PASS" "frigg-users"
  ensure_db "$FRIGG_URL" "$FRIGG_USER" "$FRIGG_PASS" "hound_default"
  ensure_db "$FRIGG_URL" "$FRIGG_USER" "$FRIGG_PASS" "hound_src_default"
  ensure_db "$FRIGG_URL" "$FRIGG_USER" "$FRIGG_PASS" "dali_default"
  ensure_default_tenant "$FRIGG_URL" "$FRIGG_USER" "$FRIGG_PASS"
  ensure_frigg_users_schema "$FRIGG_URL" "$FRIGG_USER" "$FRIGG_PASS"
  ensure_heimdall_control_event_schema "$FRIGG_URL" "$FRIGG_USER" "$FRIGG_PASS"
  sync_default_keycloak_org_id "$FRIGG_URL" "$FRIGG_USER" "$FRIGG_PASS"
fi

log "Done."
