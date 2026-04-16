# HEIMDALL Sprint 3 — Prem2 Implementation

**Document:** `HEIMDALL_SPRINT3_PREM2`
**Version:** 1.0
**Date:** 12.04.2026
**Branch:** `feature/prem2-dali-core-stub`
**Track:** B (HEIMDALL frontend + backend)

---

## Sprint Goal

Extend HEIMDALL Admin Panel from stub scaffolding (T1–T7, 6 pages) to a live monitoring dashboard fed by real service data. Every feature must use actual API responses — no fake/stub data.

**Rule:** Дали, Users page, Demo mode — excluded (no real API).

---

## Scope

| Task | Description | Week | Estimate |
|---|---|---|---|
| H3.5 | EventLog 6-col grid + comp badges + smart payload | W1 | 2h |
| H3.9 | Chur auth events (LOGIN/LOGOUT → HEIMDALL WS) | W1 | 1h |
| H3.4 | ServicesResource.java backend + ServicesPage real health | W2 | 3h |
| H3.1 | Dashboard: ServiceHealthStrip (4 cards) + RecentErrors | W2 | 2h |
| H3.7 | Presentation mode fullscreen overlay | W2 | 1.5h |
| H3.8 | HoundHeimdallListener (deferred — after C.1.3) | W3 | 2h |
| VFX | Visual fixes: focus rings, i18n strings, responsive grid | — | 1h |
| TST | Vitest unit tests: eventFormat, useControl | — | 1.5h |

**Total W1–W2: ~9.5h | W3 (deferred): 2h**

---

## New Files

### Frontend (`frontends/heimdall-frontend/src/`)

| File | Purpose |
|---|---|
| `utils/eventFormat.ts` | EVENT_LABELS, formatPayload(), levelClass() |
| `components/ServiceHealthStrip.tsx` | 4-card health strip for Dashboard |
| `components/RecentErrors.tsx` | Last 5 ERROR events mini-list |
| `components/PresentationMode.tsx` | Fullscreen metrics overlay (⛶) |
| `test/setup.ts` | vitest + @testing-library/jest-dom setup |
| `utils/eventFormat.test.ts` | Unit tests for eventFormat |
| `hooks/useControl.test.ts` | Unit tests for useControl hook |
| `vitest.config.ts` | Vitest config (jsdom, globals) |

### Backend

| File | Purpose |
|---|---|
| `bff/chur/src/middleware/heimdallEmit.ts` | Fire-and-forget Heimdall emitter |
| `services/heimdall-backend/.../ServicesResource.java` | GET /services/health — parallel ping 9 services |
| `libraries/hound/.../HoundHeimdallListener.java` | (W3) Hound → Heimdall event bridge |
| `libraries/hound/.../CompositeListener.java` | (W3) Safe multi-listener dispatch |

---

## Modified Files

| File | Change |
|---|---|
| `src/styles/heimdall.css` | + comp/badge/event-grid CSS classes + focus-visible |
| `src/components/EventLog.tsx` | Rewritten: 6-col grid, comp badges, formatPayload |
| `src/pages/ServicesPage.tsx` | Replaced: uses /services/health, 2-col grid, real data |
| `src/pages/DashboardPage.tsx` | + ServiceHealthStrip + RecentErrors, layout update |
| `src/App.tsx` | + ⛶ presentation button in toolbar |
| `src/i18n/locales/en/common.json` | + dashboard.recentErrors/servicesStrip, eventStream.paused |
| `src/i18n/locales/ru/common.json` | Same keys in Russian |
| `src/pages/EventStreamPage.tsx` | Fix pause label (paused vs pause), remove outline:none |
| `src/pages/ControlsPage.tsx` | Remove outline:none from Input |
| `bff/chur/src/routes/auth.ts` | + 3 emitToHeimdall() calls |
| `services/heimdall-backend/.../EventType.java` | + AUTH_LOGIN_SUCCESS/FAILED/LOGOUT |
| `frontends/heimdall-frontend/package.json` | + vitest + @testing-library deps |

---

## Removed Files

| File | Reason |
|---|---|
| `src/hooks/useServices.ts` | Replaced by direct fetch to `/services/health` |

---

## New API Endpoints

| Method | URL | Description |
|---|---|---|
| GET | `/services/health` | Parallel health ping of 9 AIDA services |

---

## Deferred (W3 May 4-9)

- **H3.8 HoundHeimdallListener** — blocked on C.1.3 (HoundEventListener interface from Track A)

---

## Verification Checklist

```bash
# Branch
git branch --show-current   # → feature/prem2-dali-core-stub

# Frontend build
cd frontends/heimdall-frontend
npx tsc --noEmit             # → 0 errors
npm test                     # → all tests GREEN

# Dev server
npm run dev                  # → :5174, no console errors

# EventLog
# Open Events tab → 6 columns: Time | Component | Event | Level | Dur | Payload
# Comp badge colored per component
# Level badge colored INFO/WARN/ERROR

# Services
curl http://localhost:9093/services/health
# → [{name:"SHUTTLE",status:"up",latencyMs:45}, ...]

# Auth events
# Login → wscat -c ws://localhost:9093/ws/events → AUTH_LOGIN_SUCCESS {username,role}

# Presentation mode
# ⛶ → fullscreen: 3 big metrics + 8 events
# ESC → exit
```

---

## Additional Deliverables (Apr 13)

| Item | Description |
|---|---|
| `components/ServiceTopology.tsx` | React Flow topology — IDE + Docker two-lane layout |
| `pages/ServicesPage.tsx` | + ServiceTopology at bottom; IDE/Docker rows in InstanceRow |
| `docker-compose.yml` | + `HEIMDALL_URL` for Shuttle → heimdall-backend (Docker network) |
| `EventStreamPage.tsx` | + live breakdown bar: INFO/WARN/ERR + per-component counts |
| `services/shuttle/.../LineageResource.java` | + REQUEST_RECEIVED/COMPLETED for overview, explore, search, stmtColumns |
| `services/shuttle/.../KnotResource.java` | + REQUEST_RECEIVED/COMPLETED for knotSessions, knotReport |

**Tests result:** 21/21 GREEN (`npm test`)

## Shuttle Event Stream Coverage (after Apr 13)

| Query | Events emitted |
|---|---|
| `lineage` | REQUEST_RECEIVED + REQUEST_COMPLETED |
| `overview` | REQUEST_RECEIVED + REQUEST_COMPLETED |
| `explore` | REQUEST_RECEIVED + REQUEST_COMPLETED |
| `stmtColumns` | REQUEST_RECEIVED |
| `search` | REQUEST_RECEIVED + REQUEST_COMPLETED (+ hits count) |
| `knotSessions` | REQUEST_RECEIVED + REQUEST_COMPLETED (+ session count) |
| `knotReport` | REQUEST_RECEIVED + REQUEST_COMPLETED |

**Note:** Docker Shuttle requires image rebuild to pick up new emission code:
```bash
docker compose up --build shuttle -d
```

---

## Additional Deliverables (Apr 13 — v1.2)

| Item | Description |
|---|---|
| `components/EventLog.tsx` | + click-to-expand row detail panel (full payload JSON, sessionId, correlationId, ISO timestamp) |
| `styles/heimdall.css` | + `.evt-row-selected` highlight (accent outline + tinted bg) |
| `pages/EventStreamPage.tsx` | Filter fix: `useMemo` prevents reconnect loop; client-side filter as fallback; component list updated to actual AIDA services |
| `hooks/useEventStream.ts` | Clear event buffer on reconnect; multi-filter URL (`component:x,level:y`) |
| `ws/EventFilter.java` | Multi-filter: comma-separated `key:value` pairs, AND-logic (deferred Sprint 2 item) |

---

## Backlog — Remaining Items

| Item | Status | Blocked on |
|---|---|---|
| H3.8 HoundHeimdallListener | ⏳ W3 deferred | C.1.3 — HoundEventListener interface (Track A) |
| H3.8 CompositeListener | ⏳ W3 deferred | C.1.3 |
| EventLog: export CSV/JSON | Not started | — |
| EventStream: persist replay beyond 200 events (Frigg) | Not started | — |

---

## History

| Date | Version | What |
|---|---|---|
| 12.04.2026 | 1.0 | Initial. Sprint 3 scope from HEIMDALL_SPRINT3_TASK_SPEC v3.0 |
| 13.04.2026 | 1.1 | ServiceTopology, EventStream stats, Shuttle full emission, tests 21/21 GREEN |
| 13.04.2026 | 1.2 | EventLog detail panel, filter reconnect fix, multi-filter backend, component list |
