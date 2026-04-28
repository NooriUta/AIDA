#!/usr/bin/env bash
# MTN-18 / DMT-09 — одноразовый upgrade script для single-tenant инсталляций,
# созданных ДО multi-tenant фазы (Q1-Q2 2026, ArcadeDB ≤26.3).
#
# Задача: переименовать три legacy базы в per-tenant MT layout для "default":
#   hound      → hound_default
#   hound_src  → hound_src_default
#   dali       → dali_default
# + вставить строку DaliTenantConfig('default', ...) в frigg-tenants, если её нет.
#
# Идемпотентен: если новая база уже существует — пропускает. Если старая
# отсутствует — тихо skip.
#
# Usage:
#   bash scripts/migrate-hound-to-hound-default.sh                # live run
#   DRY_RUN=1 bash scripts/migrate-hound-to-hound-default.sh      # report only
#
# Env:
#   YGG_URL   (default http://localhost:2480)
#   FRIGG_URL (default http://localhost:2481)
#   *_USER / *_PASS — same as init-arcadedb.sh
#
# ArcadeDB 26.3+ поддерживает `ALTER DATABASE RENAME TO`; older — манyual
# dump/restore + drop. Скрипт делает ALTER и, если вернулось 400/405, подсказывает
# пользователю запустить fallback flow.

set -euo pipefail

YGG_URL="${YGG_URL:-http://localhost:2480}"
YGG_USER="${YGG_USER:-root}"
YGG_PASS="${YGG_PASS}"

FRIGG_URL="${FRIGG_URL:-http://localhost:2481}"
FRIGG_USER="${FRIGG_USER:-root}"
FRIGG_PASS="${FRIGG_PASS}"

DRY_RUN="${DRY_RUN:-0}"

log()  { echo "[migrate-mt] $*"; }
warn() { echo "[migrate-mt] WARN: $*" >&2; }

# ── helpers ───────────────────────────────────────────────────────────────────

db_exists() {
  local base_url="$1" user="$2" pass="$3" db="$4"
  local dbs
  dbs=$(curl -sf --max-time 5 -u "$user:$pass" "$base_url/api/v1/databases" 2>/dev/null || echo "")
  echo "$dbs" | grep -q "\"$db\""
}

run_server_command() {
  local base_url="$1" user="$2" pass="$3" cmd="$4"
  local body
  body=$(python -c 'import json,sys; print(json.dumps({"command":sys.argv[1]}))' "$cmd")
  curl -sf --max-time 30 -u "$user:$pass" -X POST "$base_url/api/v1/server" \
    -H "Content-Type: application/json" --data "$body"
}

run_sql() {
  local base_url="$1" user="$2" pass="$3" db="$4" cmd="$5"
  local body
  body=$(python -c 'import json,sys; print(json.dumps({"language":"sql","command":sys.argv[1]}))' "$cmd")
  curl -sf --max-time 15 -u "$user:$pass" -X POST "$base_url/api/v1/command/$db" \
    -H "Content-Type: application/json" --data "$body"
}

rename_db() {
  local base_url="$1" user="$2" pass="$3" label="$4" from="$5" to="$6"
  if db_exists "$base_url" "$user" "$pass" "$to"; then
    log "[$label] '$to' уже существует — skip"
    return 0
  fi
  if ! db_exists "$base_url" "$user" "$pass" "$from"; then
    log "[$label] '$from' отсутствует — skip (свежая MT инсталляция)"
    return 0
  fi
  if [ "$DRY_RUN" = "1" ]; then
    log "[$label] DRY_RUN: would rename '$from' → '$to'"
    return 0
  fi
  log "[$label] renaming '$from' → '$to' …"
  if run_server_command "$base_url" "$user" "$pass" "alter database $from rename to $to" >/dev/null 2>&1; then
    log "[$label] ок"
  else
    warn "[$label] rename через ALTER DATABASE не удался (ArcadeDB < 26.3?)."
    warn "    Fallback план:"
    warn "      1) export $from в /tmp/$from.tar.gz (admin UI или mcp arcadedb dump)"
    warn "      2) create database $to"
    warn "      3) import /tmp/$from.tar.gz в $to"
    warn "      4) drop database $from"
    return 1
  fi
}

# ── main ──────────────────────────────────────────────────────────────────────

if [ "$DRY_RUN" = "1" ]; then
  log "DRY-RUN mode — ничего не будет изменено."
fi

log "YGG: $YGG_URL"
log "FRIGG: $FRIGG_URL"

# 1. YGG: hound → hound_default, hound_src → hound_src_default
rename_db "$YGG_URL" "$YGG_USER" "$YGG_PASS" "YGG lineage"       "hound"     "hound_default"     || true
rename_db "$YGG_URL" "$YGG_USER" "$YGG_PASS" "YGG source-archive" "hound_src" "hound_src_default" || true

# 2. FRIGG: dali → dali_default
rename_db "$FRIGG_URL" "$FRIGG_USER" "$FRIGG_PASS" "FRIGG dali" "dali" "dali_default" || true

# 3. FRIGG: ensure DaliTenantConfig('default') row exists
if ! db_exists "$FRIGG_URL" "$FRIGG_USER" "$FRIGG_PASS" "frigg-tenants"; then
  log "frigg-tenants DB не существует — pre-condition не выполнен. Запусти init-arcadedb.sh сначала."
  exit 1
fi

existing=$(curl -sf --max-time 5 -u "$FRIGG_USER:$FRIGG_PASS" \
    -X POST "$FRIGG_URL/api/v1/query/frigg-tenants" \
    -H "Content-Type: application/json" \
    -d '{"language":"sql","command":"SELECT tenantAlias FROM DaliTenantConfig WHERE tenantAlias = '\''default'\'' LIMIT 1"}' \
    2>/dev/null || echo '{"result":[]}')
if echo "$existing" | grep -q '"default"'; then
  log "DaliTenantConfig('default') уже существует — skip"
else
  if [ "$DRY_RUN" = "1" ]; then
    log "DRY_RUN: would INSERT DaliTenantConfig('default', ACTIVE, cfgVer=1, hound_default, hound_src_default, dali_default, …)"
  else
    log "вставляем DaliTenantConfig('default')…"
    ts=$(date +%s000)
    run_sql "$FRIGG_URL" "$FRIGG_USER" "$FRIGG_PASS" "frigg-tenants" \
      "INSERT INTO DaliTenantConfig SET tenantAlias = 'default', status = 'ACTIVE', configVersion = 1, yggLineageDbName = 'hound_default', yggSourceArchiveDbName = 'hound_src_default', friggDaliDbName = 'dali_default', harvestCron = '0 0 */6 * * ?', llmMode = 'off', dataRetentionDays = 30, maxParseSessions = 10, maxAtoms = 10000, maxSources = 5, maxConcurrentJobs = 2, createdAt = $ts, updatedAt = $ts" >/dev/null
    log "DaliTenantConfig('default') inserted"
  fi
fi

log "done."
log "next steps:"
log "  1) restart dali + shuttle + anvil + mimir — MT routing вступает в силу"
log "  2) curl /api/admin/tenants/default — verify правильный route"
log "  3) sanity harvest → /api/query с dbName=hound_default"
