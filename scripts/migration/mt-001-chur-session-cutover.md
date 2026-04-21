# CAP-13: Chur Session Store Cutover Runbook

## Goal
Migrate Chur from in-memory `Map<string, Session>` to ArcadeDB-backed `CachedSessionStore`
(writing to `frigg-sessions` database) with zero data loss (old in-memory sessions lost → users re-login once).

## Pre-flight

```bash
# Verify FRIGG is healthy
curl -s http://localhost:2481/api/v1/ready
# Snapshot ArcadeDB before any changes
# (use ArcadeDB backup endpoint or volume snapshot)
```

## Migration Steps

### Step 1 — Stop all Chur replicas

```bash
docker compose stop chur
```

> Old in-memory sessions are lost at this point. All active users will need to re-login.
> This is acceptable: no production tenants exist prior to this migration.

### Step 2 — Create `frigg-sessions` database in FRIGG

```bash
curl -s -X POST http://localhost:2481/api/v1/create/frigg-sessions \
  -H "Authorization: Basic $(echo -n root:playwithdata | base64)"
```

### Step 3 — Create `frigg-tenants` database in FRIGG (if not already created by DMT-09)

```bash
curl -s -X POST http://localhost:2481/api/v1/create/frigg-tenants \
  -H "Authorization: Basic $(echo -n root:playwithdata | base64)"
```

### Step 4 — Apply DaliTenantConfig schema

```bash
curl -s -X POST "http://localhost:2481/api/v1/command/frigg-tenants" \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic $(echo -n root:playwithdata | base64)" \
  -d '{"language":"sql","command":"CREATE DOCUMENT TYPE DaliTenantConfig IF NOT EXISTS"}'

curl -s -X POST "http://localhost:2481/api/v1/command/frigg-tenants" \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic $(echo -n root:playwithdata | base64)" \
  -d '{"language":"sql","command":"CREATE INDEX IF NOT EXISTS ON DaliTenantConfig (tenantAlias) UNIQUE"}'

# Seed default tenant
curl -s -X POST "http://localhost:2481/api/v1/command/frigg-tenants" \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic $(echo -n root:playwithdata | base64)" \
  -d '{"language":"sql","command":"INSERT INTO DaliTenantConfig SET tenantAlias='"'"'default'"'"', status='"'"'ACTIVE'"'"', configVersion=1, yggLineageDbName='"'"'hound_default'"'"', yggSourceArchiveDbName='"'"'hound_src_default'"'"', friggDaliDbName='"'"'dali_default'"'"' IF NOT EXISTS"}'
```

### Step 5 — Deploy new Chur version

```bash
docker compose up -d chur
```

The new version uses `ArcadeDbSessionStore` → `frigg-sessions` automatically.
Schema bootstrap runs on first request via `ensureSchema()`.

### Step 6 — Smoke test

```bash
# Login (creates session in ArcadeDB)
curl -s -X POST http://localhost:3000/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}' \
  -c /tmp/test-cookies.txt

# Verify session appears in ArcadeDB
curl -s -X POST "http://localhost:2481/api/v1/command/frigg-sessions" \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic $(echo -n root:playwithdata | base64)" \
  -d '{"language":"sql","command":"SELECT count() FROM DaliChurSession"}' \
  | jq '.result[0].count'

# Should return ≥ 1
```

## Rollback

```bash
# Stop new Chur
docker compose stop chur

# Restore ArcadeDB snapshot taken in pre-flight
# Then redeploy previous Chur version (with in-memory store)
docker compose up -d chur
```

## Success Criteria

1. Login creates a row in `DaliChurSession` (ArcadeDB `frigg-sessions`)
2. Chur restart does NOT log users out (session survives restart)
3. Two Chur replicas share sessions without conflict
4. Sweep job removes expired sessions (check after 5+ minutes idle)
