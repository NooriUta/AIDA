#!/usr/bin/env bash
# smoke-test.sh — post-deploy smoke tests for staging/prod
# Usage: DOMAIN=test.seidrstudio.pro bash scripts/smoke-test.sh
# Exit code: 0 = all passed, 1 = one or more failed

set -euo pipefail

DOMAIN="${DOMAIN:-test.seidrstudio.pro}"
BASE="https://seer.${DOMAIN}"
HEIMDALL="https://heimdall.${DOMAIN}"
PASS=0
FAIL=0

RED='\033[0;31m'
GRN='\033[0;32m'
YLW='\033[1;33m'
NC='\033[0m'

ok()   { echo -e "${GRN}  ✓ $1${NC}"; PASS=$((PASS+1)); }
fail() { echo -e "${RED}  ✗ $1${NC}"; FAIL=$((FAIL+1)); }
info() { echo -e "${YLW}  → $1${NC}"; }

echo ""
echo "═══════════════════════════════════════════════"
echo "  Smoke Tests — ${BASE}"
echo "═══════════════════════════════════════════════"

# ── S-10: HTTP → HTTPS redirect ──────────────────────────────────────────────
info "S-10: HTTP→HTTPS redirect"
STATUS=$(curl -s -o /dev/null -w "%{http_code}" --max-time 10 "http://seer.${DOMAIN}/" || echo "000")
if [ "$STATUS" = "301" ] || [ "$STATUS" = "302" ]; then
  ok "S-10: HTTP redirect → ${STATUS}"
else
  fail "S-10: HTTP redirect expected 301/302, got ${STATUS}"
fi

# ── S-07: Verdandi SPA ────────────────────────────────────────────────────────
info "S-07: Verdandi SPA"
STATUS=$(curl -sk -o /dev/null -w "%{http_code}" --max-time 10 "${BASE}/" || echo "000")
if [ "$STATUS" = "200" ]; then
  ok "S-07: Verdandi 200"
else
  fail "S-07: Verdandi expected 200, got ${STATUS}"
fi

# ── S-12: Keycloak health ─────────────────────────────────────────────────────
# KC 26: /kc/health/ready не работает на основном порту — используем /kc/realms/master
info "S-12: Keycloak health"
KC_CODE=$(curl -sk -o /dev/null -w "%{http_code}" --max-time 10 "${BASE}/kc/realms/master" || echo "000")
if [ "$KC_CODE" = "200" ]; then
  ok "S-12: Keycloak UP (realms/master → 200)"
else
  fail "S-12: Keycloak not UP (got: ${KC_CODE})"
fi

# ── S-05: Chur health ────────────────────────────────────────────────────────
info "S-05: Chur health (local)"
STATUS=$(curl -s -o /dev/null -w "%{http_code}" --max-time 10 "http://localhost:13000/health" || echo "000")
if [ "$STATUS" = "200" ]; then
  ok "S-05: Chur 200"
else
  fail "S-05: Chur expected 200, got ${STATUS}"
fi

# ── S-06: Shuttle health ─────────────────────────────────────────────────────
info "S-06: Shuttle health (local)"
SHUTTLE=$(curl -s --max-time 10 "http://localhost:18080/q/health" | grep -o '"status":"[^"]*"' | head -1 || echo "")
if echo "$SHUTTLE" | grep -q "UP"; then
  ok "S-06: Shuttle UP"
else
  fail "S-06: Shuttle not UP (got: ${SHUTTLE})"
fi

# ── S-07b: Dali health ───────────────────────────────────────────────────────
info "S-07b: Dali health (local)"
DALI=$(curl -s --max-time 10 "http://localhost:19090/q/health" | grep -o '"status":"[^"]*"' | head -1 || echo "")
if echo "$DALI" | grep -q "UP"; then
  ok "S-07b: Dali UP"
else
  fail "S-07b: Dali not UP (got: ${DALI})"
fi

# ── S-01: Login ───────────────────────────────────────────────────────────────
info "S-01: Login admin/admin"
LOGIN_RESP=$(curl -sk --max-time 15 -c /tmp/smoke_cookies.txt \
  -X POST "${BASE}/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}' \
  -w "\n%{http_code}" || echo -e "\n000")
LOGIN_CODE=$(echo "$LOGIN_RESP" | tail -1)
LOGIN_BODY=$(echo "$LOGIN_RESP" | head -1)

if [ "$LOGIN_CODE" = "200" ]; then
  ok "S-01: Login 200"
else
  fail "S-01: Login expected 200, got ${LOGIN_CODE}"
fi

# ── S-02: /auth/me ───────────────────────────────────────────────────────────
info "S-02: GET /auth/me"
ME_RESP=$(curl -sk --max-time 10 -b /tmp/smoke_cookies.txt \
  "${BASE}/auth/me" -w "\n%{http_code}" || echo -e "\n000")
ME_CODE=$(echo "$ME_RESP" | tail -1)
ME_BODY=$(echo "$ME_RESP" | head -1)

if [ "$ME_CODE" = "200" ]; then
  ok "S-02: /auth/me 200"
  # Проверяем наличие role и activeTenantAlias
  if echo "$ME_BODY" | grep -q '"role"'; then
    ok "S-02b: role присутствует в ответе"
  else
    fail "S-02b: role отсутствует в /auth/me"
  fi
  if echo "$ME_BODY" | grep -q '"activeTenantAlias"'; then
    ok "S-02c: activeTenantAlias присутствует"
  else
    fail "S-02c: activeTenantAlias отсутствует в /auth/me"
  fi
else
  fail "S-02: /auth/me expected 200, got ${ME_CODE}"
fi

# ── S-03: Неверный пароль → 401 ──────────────────────────────────────────────
info "S-03: Login с неверным паролем"
BAD_CODE=$(curl -sk --max-time 10 \
  -X POST "${BASE}/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"wrongpassword"}' \
  -o /dev/null -w "%{http_code}" || echo "000")
if [ "$BAD_CODE" = "401" ]; then
  ok "S-03: Неверный пароль → 401"
else
  fail "S-03: Неверный пароль ожидали 401, получили ${BAD_CODE}"
fi

# ── S-04: GraphQL ────────────────────────────────────────────────────────────
info "S-04: GraphQL query"
GQL_RESP=$(curl -sk --max-time 15 -b /tmp/smoke_cookies.txt \
  -X POST "${BASE}/graphql" \
  -H "Content-Type: application/json" \
  -d '{"query":"{ __typename }"}' \
  -w "\n%{http_code}" || echo -e "\n000")
GQL_CODE=$(echo "$GQL_RESP" | tail -1)
GQL_BODY=$(echo "$GQL_RESP" | head -1)

if [ "$GQL_CODE" = "200" ]; then
  if echo "$GQL_BODY" | grep -q '"errors"'; then
    fail "S-04: GraphQL 200 но содержит errors: ${GQL_BODY}"
  else
    ok "S-04: GraphQL 200 без errors"
  fi
else
  fail "S-04: GraphQL expected 200, got ${GQL_CODE}"
fi

# ── S-11: HSTS header ────────────────────────────────────────────────────────
info "S-11: HSTS header"
HSTS=$(curl -skI --max-time 10 "${BASE}/" | grep -i "strict-transport-security" || echo "")
if [ -n "$HSTS" ]; then
  ok "S-11: HSTS header присутствует"
else
  fail "S-11: HSTS header отсутствует"
fi

# ── S-09: Heimdall UI ────────────────────────────────────────────────────────
info "S-09: Heimdall UI"
STATUS=$(curl -sk -o /dev/null -w "%{http_code}" --max-time 10 "${HEIMDALL}/" || echo "000")
if [ "$STATUS" = "200" ]; then
  ok "S-09: Heimdall UI 200"
else
  fail "S-09: Heimdall UI expected 200, got ${STATUS} (проверь HEIMDALL_CIDR)"
fi

# ── Cleanup ───────────────────────────────────────────────────────────────────
rm -f /tmp/smoke_cookies.txt

# ── Итог ─────────────────────────────────────────────────────────────────────
echo ""
echo "═══════════════════════════════════════════════"
TOTAL=$((PASS+FAIL))
echo -e "  Результат: ${GRN}${PASS} passed${NC} / ${RED}${FAIL} failed${NC} (всего ${TOTAL})"
echo "═══════════════════════════════════════════════"
echo ""

if [ "$FAIL" -gt 0 ]; then
  echo -e "${RED}SMOKE TESTS FAILED${NC}"
  exit 1
fi

echo -e "${GRN}SMOKE TESTS PASSED${NC}"
exit 0
