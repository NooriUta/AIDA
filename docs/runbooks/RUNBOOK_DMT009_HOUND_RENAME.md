# Runbook: DMT-009 — Rename hound → hound_default

**Task:** DMT-09 (SPRINT_MT_NEXT_BACKLOG)
**Script:** `scripts/migration/mt-001-rename-dbs.sh`
**Applies to:** Single-tenant deployments upgrading to multi-tenant

---

## Background

Before multi-tenant support, AIDA used flat database names:
- `hound` — YGG lineage DB
- `hound_src` — YGG source archive DB
- `dali` — FRIGG Dali sessions DB

After the MT migration, names are scoped by tenant alias:
- `hound_default` — lineage DB for the "default" tenant
- `hound_src_default` — source archive DB for the "default" tenant
- `dali_default` — Dali sessions DB for the "default" tenant

`DaliTenantConfig` in FRIGG `frigg-tenants` stores the mapping. If old-named
DBs exist, the tenant registry lookup fails silently — sessions start but data
goes nowhere.

---

## When to run

Run this script once, after:
1. Deploying a Dali version with tenant-routing support
2. Before processing any parse sessions in production

**Skip if:** `hound_default` already exists in YGG (migration already ran).

---

## Pre-conditions

- [ ] ArcadeDB (YGG + FRIGG) reachable
- [ ] All Dali / Hound instances stopped
- [ ] ArcadeDB snapshots taken (rollback: restore snapshots)
- [ ] `jq` installed on the host running the script

---

## Execution

```bash
# 1. Dry-run first — verify expected output
DRY_RUN=true \
  YGG_HOST=localhost YGG_PORT=2480 \
  FRIGG_HOST=localhost FRIGG_PORT=2481 \
  DB_PASSWORD=playwithdata \
  ./scripts/migration/mt-001-rename-dbs.sh

# 2. Execute
DRY_RUN=false \
  YGG_HOST=localhost YGG_PORT=2480 \
  FRIGG_HOST=localhost FRIGG_PORT=2481 \
  DB_PASSWORD=<prod-password> \
  ./scripts/migration/mt-001-rename-dbs.sh
```

**Important:** ArcadeDB 26.x does not support `RENAME DATABASE` natively.
The script creates the new DB and seeds the tenant config. **Data migration**
(copy of existing `hound` contents to `hound_default`) requires manual steps:

```
# In ArcadeDB console (or via arcadedb-console.sh):
backup database hound TO /tmp/hound_backup.zip
restore database hound_default FROM /tmp/hound_backup.zip

backup database hound_src TO /tmp/hound_src_backup.zip
restore database hound_src_default FROM /tmp/hound_src_backup.zip
```

---

## What the script does

1. **Pre-flight** — checks if `hound` DB exists in YGG
2. **Creates** `hound_default` in YGG (idempotent)
3. **Creates** `hound_src_default` in YGG (idempotent)
4. **Creates** `dali_default` in FRIGG (idempotent)
5. **Creates** `frigg-tenants` DB in FRIGG + `DaliTenantConfig` schema (idempotent)
6. **Seeds** `DaliTenantConfig` row for `tenantAlias="default"`:
   - `yggLineageDbName = "hound_default"`
   - `yggSourceArchiveDbName = "hound_src_default"`
   - `friggDaliDbName = "dali_default"`
7. Prints smoke-test checklist

---

## Smoke test (after restart)

```bash
# 1. POST a test session
curl -s -X POST http://localhost:9090/api/sessions \
  -H "Content-Type: application/json" \
  -H "X-Seer-Tenant-Alias: default" \
  -d '{"sourceId":"test","dialect":"pg","clearBeforeWrite":false}' | jq

# 2. Verify session in FRIGG dali_default
curl -s "http://localhost:2481/api/v1/query/dali_default" \
  -H "Authorization: Basic $(echo -n 'root:playwithdata' | base64)" \
  -H "Content-Type: application/json" \
  -d '{"language":"sql","command":"SELECT id, status FROM dali_sessions LIMIT 5"}' | jq

# 3. Verify tenant config
curl -s "http://localhost:2481/api/v1/query/frigg-tenants" \
  -H "Authorization: Basic $(echo -n 'root:playwithdata' | base64)" \
  -H "Content-Type: application/json" \
  -d '{"language":"sql","command":"SELECT tenantAlias, yggLineageDbName FROM DaliTenantConfig"}' | jq
```

---

## Rollback

1. Stop all services
2. Restore ArcadeDB snapshots from before Step 2
3. Revert to pre-MT Dali version

---

## Related

- Script: `scripts/migration/mt-001-rename-dbs.sh`
- Config: `services/dali/src/main/resources/application.properties`
- Registry: `libraries/tenant-routing/src/main/java/studio/seer/tenantrouting/FriggYggSourceArchiveRegistry.java`
- ADR: `docs/current/adr/ADR-MT-010.md` (multi-tenant architecture)
