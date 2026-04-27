#!/usr/bin/env bash
# U0 SMOKE TEST — verify oidc-organization-group-membership-mapper in KC 26.6 Auth Code flow
#
# Setup: assumes KC 26.6 running on http://localhost:18181/kc (see docker-compose.smoke.yml).
#
# Steps:
#   1. Get admin token
#   2. Create realm "smoke" with Organizations enabled
#   3. Create OIDC client "smoke-app" with Auth Code + PKCE + direct grants (for ROPC compare)
#   4. Add oidc-organization-group-membership-mapper to client
#   5. Create user "alice" + set password
#   6. Create organization "acme" + add alice as member
#   7. Create organization-group "editor" inside acme + add alice
#   8. Test ROPC: get token via direct grant, decode → see if claim present
#   9. Test Auth Code: simulate browser flow via curl, exchange code for token, decode → claim
#  10. Compare: confirm claim appears in Auth Code (expected), and check ROPC behavior
#
# Output: JSON-formatted JWT claims for both flows + verdict.

set -euo pipefail

KC="http://localhost:18181/kc"
REALM="smoke"
CLIENT_ID="smoke-app"
CLIENT_SECRET="smoke-secret"
REDIRECT_URI="http://localhost:9999/callback"
USER="alice"
USER_PASS="alice123"
ORG_ALIAS="acme"
ORG_NAME="Acme Corp"
GROUP_NAME="editor"

log()  { echo -e "\033[36m[smoke]\033[0m $*" >&2; }
ok()   { echo -e "\033[32m[ok]\033[0m $*" >&2; }
fail() { echo -e "\033[31m[FAIL]\033[0m $*" >&2; exit 1; }

# ─────────────────────────────────────────────────────────────────
# 0. Wait for KC ready
# ─────────────────────────────────────────────────────────────────
log "Waiting for KC at $KC..."
for i in $(seq 1 30); do
  if curl -sf "$KC/health/ready" >/dev/null 2>&1; then
    ok "KC is ready"
    break
  fi
  sleep 2
done

# ─────────────────────────────────────────────────────────────────
# 1. Get admin token (master realm)
# ─────────────────────────────────────────────────────────────────
log "Authenticating as admin..."
ADMIN_TOKEN=$(curl -sf -X POST "$KC/realms/master/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=admin-cli" \
  -d "username=admin" \
  -d "password=admin" \
  -d "grant_type=password" | jq -r .access_token)
[[ -n "$ADMIN_TOKEN" && "$ADMIN_TOKEN" != "null" ]] || fail "admin token empty"
ok "Admin token obtained"

api() {
  local method=$1; shift
  local path=$1; shift
  curl -sf -X "$method" "$KC/admin/realms$path" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Content-Type: application/json" \
    "$@"
}

# ─────────────────────────────────────────────────────────────────
# 2. Create realm "smoke" with Organizations enabled
# ─────────────────────────────────────────────────────────────────
log "Creating realm $REALM..."
# Idempotent: delete if exists
curl -s -X DELETE "$KC/admin/realms/$REALM" -H "Authorization: Bearer $ADMIN_TOKEN" >/dev/null || true
sleep 1
api POST "" -d "{
  \"realm\": \"$REALM\",
  \"enabled\": true,
  \"organizationsEnabled\": true,
  \"sslRequired\": \"none\",
  \"accessTokenLifespan\": 600
}"
ok "Realm $REALM created"

# ─────────────────────────────────────────────────────────────────
# 3. Create client smoke-app with Auth Code + PKCE + direct grants
# ─────────────────────────────────────────────────────────────────
log "Creating client $CLIENT_ID..."
api POST "/$REALM/clients" -d "{
  \"clientId\": \"$CLIENT_ID\",
  \"secret\": \"$CLIENT_SECRET\",
  \"enabled\": true,
  \"protocol\": \"openid-connect\",
  \"clientAuthenticatorType\": \"client-secret\",
  \"standardFlowEnabled\": true,
  \"directAccessGrantsEnabled\": true,
  \"publicClient\": false,
  \"redirectUris\": [\"$REDIRECT_URI\"],
  \"webOrigins\": [\"+\"],
  \"attributes\": {
    \"pkce.code.challenge.method\": \"S256\"
  }
}"
CLIENT_UUID=$(api GET "/$REALM/clients?clientId=$CLIENT_ID" | jq -r '.[0].id')
ok "Client created: uuid=$CLIENT_UUID"

# ─────────────────────────────────────────────────────────────────
# 4. Add oidc-organization-group-membership-mapper to client
# ─────────────────────────────────────────────────────────────────
log "Adding oidc-organization-group-membership-mapper to client..."
api POST "/$REALM/clients/$CLIENT_UUID/protocol-mappers/models" -d '{
  "name": "org-group-mapper",
  "protocol": "openid-connect",
  "protocolMapper": "oidc-organization-group-membership-mapper",
  "consentRequired": false,
  "config": {
    "id.token.claim": "true",
    "access.token.claim": "true",
    "userinfo.token.claim": "true",
    "introspection.token.claim": "true"
  }
}' && ok "Mapper attached" || fail "Mapper attach failed (provider id may be wrong, or KC version doesn't have mapper)"

# Also need standard organization mapper for organization claim itself
log "Adding standard organization mapper..."
api POST "/$REALM/clients/$CLIENT_UUID/protocol-mappers/models" -d '{
  "name": "org-mapper",
  "protocol": "openid-connect",
  "protocolMapper": "oidc-organization-membership-mapper",
  "consentRequired": false,
  "config": {
    "id.token.claim": "true",
    "access.token.claim": "true",
    "userinfo.token.claim": "true"
  }
}' && ok "Org mapper attached" || log "Org mapper attach failed (may not be needed in 26.6)"

# ─────────────────────────────────────────────────────────────────
# 5. Create user alice + set password
# ─────────────────────────────────────────────────────────────────
log "Creating user $USER..."
api POST "/$REALM/users" -d "{
  \"username\": \"$USER\",
  \"email\": \"alice@acme.test\",
  \"firstName\": \"Alice\",
  \"lastName\": \"Test\",
  \"enabled\": true,
  \"emailVerified\": true,
  \"credentials\": [{\"type\": \"password\", \"value\": \"$USER_PASS\", \"temporary\": false}]
}"
USER_ID=$(api GET "/$REALM/users?username=$USER" | jq -r '.[0].id')
ok "User created: id=$USER_ID"

# ─────────────────────────────────────────────────────────────────
# 6. Create organization acme + add alice as member
# ─────────────────────────────────────────────────────────────────
log "Creating organization $ORG_ALIAS..."
api POST "/$REALM/organizations" -d "{
  \"name\": \"$ORG_NAME\",
  \"alias\": \"$ORG_ALIAS\",
  \"enabled\": true,
  \"domains\": [{\"name\": \"acme.test\", \"verified\": true}]
}"
ORG_ID=$(api GET "/$REALM/organizations?search=$ORG_ALIAS" | jq -r '.[0].id')
ok "Organization created: id=$ORG_ID"

log "Adding $USER as org member..."
curl -sf -X POST "$KC/admin/realms/$REALM/organizations/$ORG_ID/members" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "\"$USER_ID\""
ok "Member added"

# ─────────────────────────────────────────────────────────────────
# 7. Create organization-group "editor" inside acme + add alice
# ─────────────────────────────────────────────────────────────────
log "Creating organization-group $GROUP_NAME inside $ORG_ALIAS..."
# 26.6 endpoint: POST /organizations/{orgId}/groups
api POST "/$REALM/organizations/$ORG_ID/groups" -d "{\"name\": \"$GROUP_NAME\"}" \
  && ok "Org-group created" || fail "Org-group endpoint failed (wrong API or 26.6 mismatch)"

# Get org-group id
GROUP_ID=$(api GET "/$REALM/organizations/$ORG_ID/groups" | jq -r ".[] | select(.name==\"$GROUP_NAME\") | .id")
[[ -n "$GROUP_ID" && "$GROUP_ID" != "null" ]] || fail "Org-group $GROUP_NAME not found"
ok "Org-group id=$GROUP_ID"

log "Adding $USER to org-group $GROUP_NAME..."
api PUT "/$REALM/users/$USER_ID/groups/$GROUP_ID"
ok "User added to group"

# ─────────────────────────────────────────────────────────────────
# 8. ROPC test (direct grant) — for comparison
# ─────────────────────────────────────────────────────────────────
log "===== ROPC FLOW TEST ====="
ROPC_RESP=$(curl -sf -X POST "$KC/realms/$REALM/protocol/openid-connect/token" \
  -d "grant_type=password" \
  -d "client_id=$CLIENT_ID" \
  -d "client_secret=$CLIENT_SECRET" \
  -d "username=$USER" \
  -d "password=$USER_PASS" \
  -d "scope=openid")
ROPC_TOKEN=$(echo "$ROPC_RESP" | jq -r .access_token)
[[ -n "$ROPC_TOKEN" && "$ROPC_TOKEN" != "null" ]] || fail "ROPC token failed"

decode_jwt() {
  local token=$1
  local payload=$(echo "$token" | cut -d. -f2)
  # base64url decode (pad if needed)
  local pad=$((4 - ${#payload} % 4))
  [[ $pad -lt 4 ]] && payload="${payload}$(printf '=%.0s' $(seq 1 $pad))"
  echo "$payload" | tr '_-' '/+' | base64 -d 2>/dev/null | jq .
}

echo "--- ROPC JWT claims ---"
decode_jwt "$ROPC_TOKEN"

# ─────────────────────────────────────────────────────────────────
# 9. Auth Code flow test (simulate browser)
# ─────────────────────────────────────────────────────────────────
log "===== AUTH CODE FLOW TEST ====="

# Generate PKCE
CODE_VERIFIER=$(openssl rand -hex 32)
CODE_CHALLENGE=$(echo -n "$CODE_VERIFIER" | openssl dgst -binary -sha256 | openssl base64 | tr -d '=' | tr '/+' '_-')
STATE=$(openssl rand -hex 8)
COOKIE_JAR=$(mktemp)

# Step 9a: GET /auth — fetch login form
log "Step 9a: fetching login page..."
LOGIN_HTML=$(curl -sf -c "$COOKIE_JAR" \
  "$KC/realms/$REALM/protocol/openid-connect/auth?response_type=code&client_id=$CLIENT_ID&redirect_uri=$REDIRECT_URI&scope=openid&state=$STATE&code_challenge=$CODE_CHALLENGE&code_challenge_method=S256")

FORM_ACTION=$(echo "$LOGIN_HTML" | grep -oP 'action="\K[^"]+' | head -1 | sed 's/&amp;/\&/g')
[[ -n "$FORM_ACTION" ]] || fail "Login form action URL not found"
log "Form action: $FORM_ACTION"

# Step 9b: POST credentials
log "Step 9b: posting credentials..."
REDIRECT_RESP=$(curl -s -i -b "$COOKIE_JAR" -c "$COOKIE_JAR" \
  -X POST "$FORM_ACTION" \
  --data-urlencode "username=$USER" \
  --data-urlencode "password=$USER_PASS" \
  -o /tmp/redirect_body.txt -w "%{http_code} %{redirect_url}\n")

STATUS=$(echo "$REDIRECT_RESP" | awk '{print $1}')
REDIRECT_URL=$(echo "$REDIRECT_RESP" | awk '{print $2}')

if [[ "$STATUS" != "302" && "$STATUS" != "303" ]] ; then
  log "Unexpected status: $STATUS"
  cat /tmp/redirect_body.txt | head -50
  fail "Login form did not redirect"
fi

CODE=$(echo "$REDIRECT_URL" | grep -oP 'code=\K[^&]+')
[[ -n "$CODE" ]] || fail "Authorization code not found in redirect: $REDIRECT_URL"
ok "Got authorization code"

# Step 9c: exchange code for token
log "Step 9c: exchanging code for token..."
TOKEN_RESP=$(curl -sf -X POST "$KC/realms/$REALM/protocol/openid-connect/token" \
  -d "grant_type=authorization_code" \
  -d "client_id=$CLIENT_ID" \
  -d "client_secret=$CLIENT_SECRET" \
  -d "code=$CODE" \
  -d "redirect_uri=$REDIRECT_URI" \
  -d "code_verifier=$CODE_VERIFIER")
AUTHCODE_TOKEN=$(echo "$TOKEN_RESP" | jq -r .access_token)
[[ -n "$AUTHCODE_TOKEN" && "$AUTHCODE_TOKEN" != "null" ]] || fail "Auth code token exchange failed: $TOKEN_RESP"

echo "--- AUTH CODE JWT claims ---"
decode_jwt "$AUTHCODE_TOKEN"

# ─────────────────────────────────────────────────────────────────
# 10. Verdict
# ─────────────────────────────────────────────────────────────────
echo ""
log "===== VERDICT ====="

ROPC_HAS_ORG=$(decode_jwt "$ROPC_TOKEN" | jq 'has("organization")')
AUTHCODE_HAS_ORG=$(decode_jwt "$AUTHCODE_TOKEN" | jq 'has("organization")')

echo "ROPC      → organization claim present: $ROPC_HAS_ORG"
echo "Auth Code → organization claim present: $AUTHCODE_HAS_ORG"

ROPC_GROUP=$(decode_jwt "$ROPC_TOKEN" | jq '.organization // {} | to_entries | .[0].value.groups // null')
AUTHCODE_GROUP=$(decode_jwt "$AUTHCODE_TOKEN" | jq '.organization // {} | to_entries | .[0].value.groups // null')

echo "ROPC      → org-group claim: $ROPC_GROUP"
echo "Auth Code → org-group claim: $AUTHCODE_GROUP"

if [[ "$AUTHCODE_HAS_ORG" == "true" && "$AUTHCODE_GROUP" != "null" ]]; then
  ok "🟢 PIVOT GREEN: org-group-membership-mapper triggers in Auth Code flow"
  echo ""
  echo "Decision: proceed with Phase 1-2 as planned (Option F core path)"
  exit 0
else
  fail "🔴 PIVOT RED: claim missing in Auth Code → fallback to Option E (manual realm groups)"
fi
