# AIDA Platform — Roadmap APR–OCT 2026 (Revised)

**Version:** 1.4  
**Date:** 2026-04-20  
**M2 Progress:** ~65%

---

## M2 Milestones Overview

| Milestone | Target | Status |
|-----------|--------|--------|
| M2 Tech Tasks (core backend) | May 31, 2026 | 🟡 IN PROGRESS ~65% |
| M2 UI/UX (Verdandi mobile + LOOM S2) | Jun 30, 2026 | 🟡 IN PROGRESS |
| M3 Analytics + ELK 5K | Aug 31, 2026 | ⬜ PLANNED |
| M4 Production GA | Oct 31, 2026 | ⬜ PLANNED |

---

## M2 Tech Tasks — Week-by-Week

### Weeks 1–4 (Apr 1–20, 2026) — ✅ COMPLETED

| Task | Sprint | Estimate | Status |
|------|--------|----------|--------|
| HN-01..HN-05: HEIMDALL two-level nav redesign | HEIMDALL Nav Redesign | 3d | ✅ DONE |
| HN-03b: DaliSourcesPage — Sources CRUD + File Upload | HEIMDALL Nav Redesign | 2d | ✅ DONE |
| DS-03: HarvestJob + FileParseJob chain | Dali Core S1 | 2d | ✅ DONE |
| DS-06: HarvestScheduler cron | Dali Core S1 | 0.5d | ✅ DONE |
| Dali+Hound integration test | Dali Core S1 | 1d | ✅ DONE |
| C.3.2: aida:harvest scope routing → Dali | SHUTTLE C2X | 1d | ✅ DONE |
| C.4.1: saveView GraphQL → FRIGG | SHUTTLE C2X | 1d | ✅ DONE |
| SC-01 C.3.4: GraphQL-WS proxy | CHUR | 1d | ✅ DONE |
| LOOM-036: fetchMock* → real MemoryRouter | LOOM | 0.5d | ✅ DONE |
| Verdandi UX Mobile: FilterToolbar L1/L2 + ProfileModal | Verdandi UX | 3d | ✅ DONE |
| routineFilter dimension (procedures/functions pill) | Verdandi UX | 1d | ✅ DONE |

**M2 delivered in v1.2.0 (2026-04-20)** · hotfix v1.2.1 same day

---

### Weeks 5–8 (Apr 21 – May 17, 2026) — 🟡 PLANNED

| Task | Sprint | Estimate | Priority |
|------|--------|----------|----------|
| EK-01: ELK algorithm auto-switch for dense graphs | SPRINT_ELK_M2_KNOT | 2d | HIGH |
| EK-02: KNOT Inspector — Routines + Statements sections | SPRINT_ELK_M2_KNOT | 2d | HIGH |
| SKADI: schema filter UI in DaliSourcesPage (include/exclude tags) | Dali Core S2 | 1d | MEDIUM |
| SKADI: ⚡ Test Connection button | Dali Core S2 | 0.5d | MEDIUM |
| Coverage: verdandi functions ≥ 70% | QG | 1d | HIGH |
| Version bump: all services to 1.2.0 | Release hygiene | 0.5d | LOW |
| CVE remediation (fastify v4→v5, npm upgrade, apk upgrade) | Security | 1d | HIGH |

---

### Weeks 9–12 (May 18 – Jun 14, 2026) — ⬜ PLANNED

| Task | Sprint | Estimate | Priority |
|------|--------|----------|----------|
| LOOM S2: L2 aggregate view — full column-level dimming | LOOM S2 | 3d | HIGH |
| LOOM S2: L3/L4 statement drill — full graph | LOOM S2 | 2d | HIGH |
| E2E test: startParseSession → Dali → Hound → YGG → SHUTTLE | E2E | 2d | MEDIUM |
| HOUND 2076N performance: SKADI large dataset | Perf | 2d | MEDIUM |
| Verdandi: undo/redo (LOOM-030) | LOOM | 1d | LOW |

---

### Weeks 13–16 (Jun 15 – Jul 12, 2026) — ⬜ PLANNED (M3 start)

| Task | Sprint | Estimate | Priority |
|------|--------|----------|----------|
| ELK 5K graph rendering (SPRINT_ELK_M2_KNOT EK-01 large dataset) | ELK | 3d | HIGH |
| Analytics: DWH usage heatmap in Inspector | M3 Analytics | 3d | MEDIUM |
| Multi-schema lineage: cross-schema READS_FROM/WRITES_TO | M3 | 2d | MEDIUM |
| SHUTTLE: paging for large explore results | Perf | 1d | LOW |

---

## Quality Gates (current)

| Gate | Threshold | Current | Status |
|------|-----------|---------|--------|
| Verdandi TS errors | 0 | 0 | ✅ |
| Verdandi vitest pass rate | 100% | 216/216 | ✅ |
| Verdandi function coverage | ≥ 70% | 64.04% | 🔴 FAIL |
| Verdandi line coverage | ≥ 70% | TBD | — |
| Dali `./gradlew test` | green | green | ✅ |
| SHUTTLE `./gradlew test` | green | green | ✅ |

---

## Service Version Map (v1.2.0 release)

| Service | Version | Last changed |
|---------|---------|-------------|
| verdandi | 1.2.0 | v1.2.0 |
| heimdall-frontend | 1.0.0 | needs bump |
| chur (BFF) | 1.0.0 | needs bump |
| dali | 1.0.0 | needs bump |
| shuttle | 1.0.0 | needs bump |
| heimdall-backend | 1.0.0 | needs bump |
| hound | 1.0.0 | stable |
| skadi | 0.2.0 | pre-release |

---

## Open Risks

| Risk | Impact | Mitigation |
|------|--------|-----------|
| YGG (ArcadeDB) single-node, no replication | HIGH | Backup cronjob, planned ArcadeDB HA in M4 |
| SKADI JDBC harvester not yet tested on ClickHouse | MEDIUM | Add ClickHouse integration test in Weeks 5–8 |
| Verdandi function coverage below 70% | MEDIUM | Add tests for hooks + store actions (Weeks 5–6) |
| 7 active CVEs (fastify, npm chain, busybox) | HIGH | CVE remediation sprint Weeks 5–6 |
| skadi library at v0.2.0 (pre-release API) | LOW | Stabilize API before M3 |
