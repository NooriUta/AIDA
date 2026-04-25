#!/usr/bin/env bash
# integration-test.sh — полный сценарий тестирования тенанта
#
# Тест покрывает полный жизненный цикл:
#   1. Создание тенанта acme
#   2. Загрузка SQL-файлов по модулям: CRM → HR → FIN → корневые
#   3. Проверка успешного парсинга каждой сессии
#   4. Очистка в обратном порядке: корневые → FIN → HR → CRM
#   5. Удаление тенанта
#
# Usage:
#   DOMAIN=test.seidrstudio.pro bash scripts/integration-test.sh
#   DOMAIN=test.seidrstudio.pro TENANT=acme bash scripts/integration-test.sh
#   DOMAIN=test.seidrstudio.pro SKIP_CLEANUP=1 bash scripts/integration-test.sh
#
# Env vars:
#   DOMAIN        — домен (default: test.seidrstudio.pro)
#   TENANT        — alias тенанта для теста (default: acme)
#   ADMIN_USER    — логин (default: admin)
#   ADMIN_PASS    — пароль (default: admin)
#   DATA_DIR      — путь к папке с SQL файлами (default: /opt/seer-studio/tests/data/erp)
#   SKIP_CLEANUP  — если 1, не удалять тенант после тестов
#   POLL_TIMEOUT  — таймаут ожидания сессии в секундах (default: 300)

set -euo pipefail

DOMAIN="${DOMAIN:-test.seidrstudio.pro}"
TENANT="${TENANT:-acme}"
ADMIN_USER="${ADMIN_USER:-admin}"
ADMIN_PASS="${ADMIN_PASS:-admin}"
DATA_DIR="${DATA_DIR:-/opt/seer-studio/tests/data/erp}"
SKIP_CLEANUP="${SKIP_CLEANUP:-0}"
POLL_TIMEOUT="${POLL_TIMEOUT:-300}"

BASE="https://seer.${DOMAIN}"
DALI_BASE="https://seer.${DOMAIN}/dali"
ORIGIN="https://seer.${DOMAIN}"

PASS=0
FAIL=0
COOKIE_JAR="/tmp/integration_cookies_$$.txt"

RED='\033[0;31m'
GRN='\033[0;32m'
YLW='\033[1;33m'
BLU='\033[0;34m'
NC='\033[0m'

ok()   { echo -e "${GRN}  ✓ $1${NC}"; PASS=$((PASS+1)); }
fail() { echo -e "${RED}  ✗ $1${NC}"; FAIL=$((FAIL+1)); }
info() { echo -e "${YLW}  → $1${NC}"; }
step() { echo -e "\n${BLU}══ $1 ══${NC}"; }

cleanup_files() { rm -f "$COOKIE_JAR" /tmp/int_test_*.zip; }
trap cleanup_files EXIT

summary() {
  echo ""
  echo "═══════════════════════════════════════════════════════"
  local TOTAL=$((PASS+FAIL))
  echo -e "  Результат: ${GRN}${PASS} passed${NC} / ${RED}${FAIL} failed${NC} (всего ${TOTAL})"
  echo "═══════════════════════════════════════════════════════"
  echo ""
  if [ "$FAIL" -gt 0 ]; then
    echo -e "${RED}INTEGRATION TESTS FAILED${NC}"
    exit 1
  fi
  echo -e "${GRN}INTEGRATION TESTS PASSED${NC}"
  exit 0
}

echo ""
echo "═══════════════════════════════════════════════════════"
echo "  Integration Test — ${BASE}"
echo "  Tenant: ${TENANT}  |  Data: ${DATA_DIR}"
echo "═══════════════════════════════════════════════════════"

# ── 1. LOGIN ──────────────────────────────────────────────────────────────────
step "1. Login"
info "POST /auth/login as ${ADMIN_USER}"

LOGIN_CODE="000"
LOGIN_BODY=""
for attempt in 1 2 3; do
  LOGIN_RESP=$(curl -sk --max-time 15 \
    -c "$COOKIE_JAR" \
    -X POST "${BASE}/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"${ADMIN_USER}\",\"password\":\"${ADMIN_PASS}\"}" \
    -w "\n%{http_code}" || echo -e "\n000")
  LOGIN_CODE=$(echo "$LOGIN_RESP" | tail -1)
  LOGIN_BODY=$(echo "$LOGIN_RESP" | head -1)

  if [ "$LOGIN_CODE" = "200" ]; then
    break
  elif [ "$LOGIN_CODE" = "429" ]; then
    info "Rate limited (попытка ${attempt}/3) — ожидаем 30s..."
    sleep 30
  else
    info "Login попытка ${attempt}/3 → ${LOGIN_CODE}: ${LOGIN_BODY}"
    sleep 5
  fi
done

if [ "$LOGIN_CODE" = "200" ]; then
  ok "Login → 200"
else
  fail "Login → ${LOGIN_CODE} (ожидали 200): ${LOGIN_BODY}"
  echo "ABORT: не удалось войти"
  echo ""
  echo "Диагностика:"
  echo "  Проверьте KC brute-force: docker exec seer-studio-keycloak-1 \\"
  echo "    /opt/keycloak/bin/kcadm.sh get attack-detection/brute-force/users \\"
  echo "    -r seer --server http://localhost:8180/kc --user admin --password admin"
  echo ""
  echo "  Или перезапустите Chur: docker restart seer-studio-chur-1"
  exit 1
fi

# ── 1b. YGG DIAGNOSTICS ──────────────────────────────────────────────────────
step "1b. YGG connectivity diagnostics (localhost:2480)"
# ygg maps port 2480 to the host — probe it directly from the VM
YGG_PASS_DIAG=$(grep -oP '(?<=ARCADEDB_ADMIN_PASSWORD=).*' /opt/seer-studio/.env.prod 2>/dev/null | head -1 || echo "playwithdata")

YGG_READY=$(curl -s -o /dev/null -w "%{http_code}" --max-time 5 \
  http://localhost:2480/api/v1/ready 2>/dev/null || echo "CONN_ERR")
info "YGG /api/v1/ready → ${YGG_READY}"

YGG_SERVER_DIAG=$(curl -s --max-time 5 \
  -u "root:${YGG_PASS_DIAG}" \
  http://localhost:2480/api/v1/server 2>/dev/null | head -c 200 || echo "CONN_ERR")
info "YGG /api/v1/server (auth) → ${YGG_SERVER_DIAG}"

YGG_CREATE_CODE=$(curl -s -o /tmp/ygg_create.out -w "%{http_code}" --max-time 10 \
  -X POST -u "root:${YGG_PASS_DIAG}" \
  http://localhost:2480/api/v1/create/diag_ci_test 2>/dev/null || echo "CONN_ERR")
YGG_CREATE_BODY=$(cat /tmp/ygg_create.out 2>/dev/null || echo "")
info "YGG /api/v1/create/diag_ci_test → HTTP ${YGG_CREATE_CODE} | ${YGG_CREATE_BODY}"

# Drop test db best-effort
curl -s -o /dev/null -X DELETE -u "root:${YGG_PASS_DIAG}" \
  http://localhost:2480/api/v1/drop/diag_ci_test 2>/dev/null || true

# ── 2. CREATE TENANT ──────────────────────────────────────────────────────────
step "2. Create tenant '${TENANT}'"
info "POST /api/admin/tenants"
CREATE_RESP=$(curl -sk --max-time 30 \
  -b "$COOKIE_JAR" -c "$COOKIE_JAR" \
  -X POST "${BASE}/api/admin/tenants" \
  -H "Content-Type: application/json" \
  -H "Origin: ${ORIGIN}" \
  -d "{\"alias\":\"${TENANT}\"}" \
  -w "\n%{http_code}" || echo -e "\n000")
CREATE_CODE=$(echo "$CREATE_RESP" | tail -1)
CREATE_BODY=$(echo "$CREATE_RESP" | head -1)

if [ "$CREATE_CODE" = "201" ] || [ "$CREATE_CODE" = "200" ]; then
  ok "Create tenant → ${CREATE_CODE}"
elif echo "$CREATE_BODY" | grep -qi "already exists\|duplicate\|conflict"; then
  info "Tenant '${TENANT}' уже существует — продолжаем"
else
  fail "Create tenant → ${CREATE_CODE}: ${CREATE_BODY}"
  echo "ABORT: не удалось создать тенант"
  exit 1
fi

# ── 3. WAIT FOR TENANT ACTIVE ─────────────────────────────────────────────────
step "3. Waiting for tenant ACTIVE"
TENANT_ACTIVE=0
for i in $(seq 1 30); do
  STATUS_RESP=$(curl -sk --max-time 10 \
    -b "$COOKIE_JAR" \
    "${BASE}/api/admin/tenants/${TENANT}" \
    -w "\n%{http_code}" || echo -e "\n000")
  STATUS_CODE=$(echo "$STATUS_RESP" | tail -1)
  STATUS_BODY=$(echo "$STATUS_RESP" | head -1)
  TENANT_STATUS=$(echo "$STATUS_BODY" | grep -oE '"status":"[^"]*"' | head -1 | grep -oE '[A-Z]+' || echo "")
  if [ "$STATUS_CODE" = "200" ] && echo "$STATUS_BODY" | grep -q '"ACTIVE"'; then
    ok "Tenant ACTIVE после ${i}×5s"
    TENANT_ACTIVE=1
    break
  fi
  info "Статус: ${TENANT_STATUS} (попытка ${i}/30)..."
  sleep 5
done
if [ "$TENANT_ACTIVE" = "0" ]; then
  fail "Tenant не стал ACTIVE за 150s"
  exit 1
fi

# ── helper: zip и загрузить директорию ────────────────────────────────────────
# upload_dir <dir_path> <session_name> <clear_before_write>
upload_dir() {
  local dir="$1" name="$2" clear="$3"
  local zip_file="/tmp/int_test_${name}_$$.zip"

  # Создаём ZIP из SQL файлов
  local file_count
  file_count=$(find "$dir" -maxdepth 1 -name "*.sql" | wc -l)
  if [ "$file_count" -eq 0 ]; then
    info "  ${name}: нет SQL файлов в ${dir} — пропуск"
    return 0
  fi

  info "  Пакуем ${file_count} файлов из ${dir} → ${name}.zip"
  (cd "$dir" && zip -q "$zip_file" *.sql)

  info "  POST /dali/api/sessions/upload (${name}, clearBeforeWrite=${clear})"
  UPLOAD_RESP=$(curl -sk --max-time 60 \
    -b "$COOKIE_JAR" \
    -X POST "${DALI_BASE}/api/sessions/upload" \
    -H "Origin: ${ORIGIN}" \
    -H "X-Seer-Tenant-Alias: ${TENANT}" \
    -F "file=@${zip_file};type=application/zip" \
    -F "dialect=plsql" \
    -F "clearBeforeWrite=${clear}" \
    -F "appName=${name}" \
    -w "\n%{http_code}" || echo -e "\n000")
  UPLOAD_CODE=$(echo "$UPLOAD_RESP" | tail -1)
  UPLOAD_BODY=$(echo "$UPLOAD_RESP" | head -1)
  rm -f "$zip_file"

  if [ "$UPLOAD_CODE" != "202" ] && [ "$UPLOAD_CODE" != "200" ]; then
    fail "${name}: upload → ${UPLOAD_CODE}: ${UPLOAD_BODY}"
    return 1
  fi

  SESSION_ID=$(echo "$UPLOAD_BODY" | grep -oE '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
  if [ -z "$SESSION_ID" ]; then
    fail "${name}: не удалось извлечь session id из ответа"
    return 1
  fi
  ok "${name}: сессия создана id=${SESSION_ID}"

  # poll до COMPLETED/FAILED
  local elapsed=0
  while [ "$elapsed" -lt "$POLL_TIMEOUT" ]; do
    sleep 5
    elapsed=$((elapsed+5))
    POLL_RESP=$(curl -sk --max-time 10 \
      -b "$COOKIE_JAR" \
      "${DALI_BASE}/api/sessions/${SESSION_ID}" \
      -H "X-Seer-Tenant-Alias: ${TENANT}" \
      -H "Origin: ${ORIGIN}" \
      -w "\n%{http_code}" || echo -e "\n000")
    POLL_CODE=$(echo "$POLL_RESP" | tail -1)
    POLL_BODY=$(echo "$POLL_RESP" | head -1)
    SESSION_STATUS=$(echo "$POLL_BODY" | grep -oE '"status":"[^"]*"' | head -1 | cut -d'"' -f4 || echo "?")

    if echo "$POLL_BODY" | grep -q '"COMPLETED"'; then
      local parsed
      parsed=$(echo "$POLL_BODY" | grep -oE '"parsedCount":[0-9]+' | head -1 | grep -oE '[0-9]+' || echo "?")
      ok "${name}: COMPLETED (parsedCount=${parsed}, ${elapsed}s)"
      return 0
    elif echo "$POLL_BODY" | grep -q '"FAILED"'; then
      local err
      err=$(echo "$POLL_BODY" | grep -oE '"error":"[^"]*"' | head -1 || echo "")
      fail "${name}: FAILED — ${err}"
      return 1
    fi
    info "  ${name}: ${SESSION_STATUS} (${elapsed}s)..."
  done

  fail "${name}: таймаут ${POLL_TIMEOUT}s (последний статус: ${SESSION_STATUS})"
  return 1
}

# ── 4. UPLOAD: CRM → HR → FIN → ROOT ─────────────────────────────────────────
step "4. Upload SQL (CRM → HR → FIN → ROOT)"

info "4.1 CRM (clearBeforeWrite=true — первая загрузка)"
upload_dir "${DATA_DIR}/CRM" "CRM" "true"

info "4.2 HR (clearBeforeWrite=false — добавляем к CRM)"
upload_dir "${DATA_DIR}/HR" "HR" "false"

info "4.3 FIN (clearBeforeWrite=false — добавляем к HR)"
upload_dir "${DATA_DIR}/FIN" "FIN" "false"

info "4.4 ROOT files (clearBeforeWrite=false — пакеты и вьюхи)"
upload_dir "${DATA_DIR}/root" "ROOT" "false"

# ── 5. VERIFY SESSIONS ────────────────────────────────────────────────────────
step "5. Verify sessions"
SESSIONS_RESP=$(curl -sk --max-time 10 \
  -b "$COOKIE_JAR" \
  "${DALI_BASE}/api/sessions?limit=10" \
  -H "X-Seer-Tenant-Alias: ${TENANT}" \
  -H "Origin: ${ORIGIN}" || echo "")
SESSION_COUNT=$(echo "$SESSIONS_RESP" | python3 -c "import json,sys; d=json.load(sys.stdin); print(len(d))" 2>/dev/null || echo "0")
COMPLETED_COUNT=$(echo "$SESSIONS_RESP" | python3 -c "import json,sys; d=json.load(sys.stdin); print(sum(1 for s in d if s.get('status')=='COMPLETED'))" 2>/dev/null || echo "0")
if [ "$COMPLETED_COUNT" -ge 4 ]; then
  ok "Сессий завершено: ${COMPLETED_COUNT}/${SESSION_COUNT}"
else
  fail "Ожидали 4+ COMPLETED сессии, получили ${COMPLETED_COUNT}/${SESSION_COUNT}"
fi

if [ "$SKIP_CLEANUP" = "1" ]; then
  info "SKIP_CLEANUP=1 — пропускаем очистку и удаление тенанта"
  summary
fi

# ── 6. CLEANUP: ROOT → FIN → HR → CRM (clearBeforeWrite=true) ────────────────
step "6. Cleanup (ROOT → FIN → HR → CRM)"

info "6.1 ROOT — очищаем верхний уровень"
upload_dir "${DATA_DIR}/root" "ROOT_CLEAR" "true"

info "6.2 FIN — очищаем финансовый модуль"
upload_dir "${DATA_DIR}/FIN" "FIN_CLEAR" "true"

info "6.3 HR — очищаем HR модуль"
upload_dir "${DATA_DIR}/HR" "HR_CLEAR" "true"

info "6.4 CRM — финальная очистка всего линейного графа"
upload_dir "${DATA_DIR}/CRM" "CRM_CLEAR" "true"

# ── 7. DELETE TENANT ──────────────────────────────────────────────────────────
step "7. Delete tenant '${TENANT}'"
DEL_RESP=$(curl -sk --max-time 30 \
  -b "$COOKIE_JAR" \
  -X DELETE "${BASE}/api/admin/tenants/${TENANT}" \
  -H "Content-Type: application/json" \
  -H "Origin: ${ORIGIN}" \
  -w "\n%{http_code}" || echo -e "\n000")
DEL_CODE=$(echo "$DEL_RESP" | tail -1)
DEL_BODY=$(echo "$DEL_RESP" | head -1)

if [ "$DEL_CODE" = "200" ] || [ "$DEL_CODE" = "204" ]; then
  ok "Tenant удалён → ${DEL_CODE}"
  # Проверяем что тенант недоступен
  sleep 3
  CHECK_RESP=$(curl -sk --max-time 10 \
    -b "$COOKIE_JAR" \
    "${BASE}/api/admin/tenants/${TENANT}" \
    -w "\n%{http_code}" || echo -e "\n000")
  CHECK_CODE=$(echo "$CHECK_RESP" | tail -1)
  CHECK_BODY=$(echo "$CHECK_RESP" | head -1)
  if [ "$CHECK_CODE" = "404" ] || echo "$CHECK_BODY" | grep -qiE '"SUSPENDED"|"PURGED"'; then
    ok "Tenant недоступен после удаления (${CHECK_CODE})"
  else
    fail "Tenant всё ещё доступен после удаления: ${CHECK_CODE}"
  fi
else
  fail "Delete tenant → ${DEL_CODE}: ${DEL_BODY}"
fi

# ── ИТОГ ──────────────────────────────────────────────────────────────────────
summary
