# Sprints Index

**Version:** 1.2  
**Date:** 2026-04-21  

---

## Active / Recently Completed Sprints

| Sprint | Status | Branch | Release | Date |
|--------|--------|--------|---------|------|
| [Verdandi UX Mobile](SPRINT_VERDANDI_UX_MOBILE.md) | ✅ DONE | `develop` → `release` | v1.2.0 | 2026-04-20 |
| Dali Core S1 (DS-03, DS-06) | ✅ DONE | `feature/dali-core-s1` | v1.2.0 | 2026-04-20 |
| HEIMDALL Nav Redesign (HN-01..HN-05) | ✅ DONE | `feature/heimdall-nav-redesign` | v1.2.0 | 2026-04-20 |
| HEIMDALL Nav Redesign HN-03b (DaliSourcesPage) | ✅ DONE | `feature/heimdall-nav-redesign` | v1.2.0 | 2026-04-20 |
| SHUTTLE C2X (C.3.2, C.4.1) | ✅ DONE | `feature/shuttle-c2x` | v1.2.0 | 2026-04-20 |
| LOOM-036 nav hooks | ✅ DONE | `feature/loom-036-nav-hooks` | v1.2.0 | 2026-04-20 |
| [ELK M2 KNOT](SPRINT_ELK_M2_KNOT.md) (EK-01, EK-02, HOUND 2076N, BUG-EK-01) | ✅ DONE | `feature/elk-m2-knot` | v1.3.0 | 2026-04-20 |
| Infra Backlog (IF-01, IF-04, IF-06) | ✅ DONE | `fix/infra-backlog` → `develop` | → v1.3.0 | 2026-04-21 |
| [HEIMDALL Sprint 4 RBAC](HEIMDALL_SPRINT4_RBAC.md) (R4.2–R4.14 + Shell integration) | ✅ DONE | `feature/heimdall-s4-rbac` → `develop` | → v1.3.0 | 2026-04-21 |
| Version bump 1.0.0 → 1.2.0 | ✅ DONE | `fix/version-bump-1.2.0` | v1.2.0 | 2026-04-20 |

---

## v1.2.0 Release — What's Included

### Verdandi (frontend)
- FilterToolbar L1 two-row layout with App/DB/Schema CascadePills
- FilterToolbar L2/L3 full style parity with L1
- routineFilter dimension (procedures/functions filter pill in L2)
- ProfileModal full mobile responsive
- useIsMobile hook, MobileInspectorDrawer
- i18n: showFilters, hideFilters, noSqlText

### Dali (backend service)
- HarvestJob + FileParseJob job chain (DS-03)
- HarvestScheduler cron trigger (DS-06)
- JobRunr 8.5.2 upgrade
- JobRunr dashboard page in HEIMDALL
- HarvestJobIntegrationTest

### HEIMDALL (frontend)
- Two-level nav redesign (HN-01..HN-05)
- DaliSourcesPage — Sources CRUD + File Upload (HN-03b)
- JobRunr dashboard embed

### SHUTTLE (backend service)
- `aida:harvest` scope routing → Dali (C.3.2)
- `saveView` GraphQL mutation → FRIGG (C.4.1)
- GraphQL-WS subscription proxy (SC-01 C.3.4)

### Documentation
- `docs/current/specs/SKADI_SOURCE_SPEC.md`
- `docs/current/specs/heimdall/HEIMDALL_SOURCES_UI.md`
- `docs/current/specs/heimdall/HEIMDALL_DALI_API.md`

---

## v1.3.0 Release — What's Included (develop, not yet released)

### Verdandi (frontend)
- ELK auto-switch to stress algorithm for dense graphs (E/V > 5)
- KNOT Inspector — Statements section with dedup by stmtGeoid
- ReactFlow virtualization for large graphs (> 1500 nodes)
- DB node single-click drill-down fix (BUG-EK-01)
- usePrefsSync — Keycloak prefs sync on login

### HEIMDALL (frontend)
- RBAC: RoleGuard, useTenantContext, prefsStore
- UsersPage + UserEditModal wired to Chur admin API
- Shell integration: unified AuthGate/LoginPage, appBase/go() MF-safe navigation
- FENRIR section active-state + visibility gating by role
- heimdall.css import in App.tsx (Fenrir styles in MF remote mode)
- AidaNav height normalized to 42px

### Shell (frontend)
- Unified AuthGate + ShellAuthStore + LoginPage
- NavigateBridge for cross-app routing
- i18n auth keys (EN + RU)

### Chur (BFF)
- requireAdmin middleware (R4.2)
- Full admin routes: users CRUD, profile, prefs (R4.9–R4.11)
- keycloakAdmin.ts: getUser, inviteUser, setUserRole, getUserAttributes, setUserAttributes

### HEIMDALL backend
- TenantContext + TenantContextFilter (X-Seer-* headers)

### Infra
- Dockerfile dependency cache layers — shuttle + heimdall-backend (IF-01)
- systemd `--pull missing` + `Restart=on-failure` — stack survives cold boot (IF-04)
- nginx HEIMDALL CIDR whitelist via `${HEIMDALL_CIDR}` env var in cd.yml (IF-06)

### Documentation
- `docs/current/sprints/HEIMDALL_SPRINT4_RBAC.md`
