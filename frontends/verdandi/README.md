# VERDANDI — SEER Studio Data Lineage Visualizer

VERDANDI is the primary module of **SEER Studio** (alongside URD and SKULD).
It provides interactive data lineage visualization through three sub-modules:

- **LOOM** — Graph-based lineage explorer (L1 > L2 > L3 > L4 drill-down)
- **KNOT** — Code-level analysis and SQL inspection
- **ANVIL** — Impact analysis (planned, H2)

## Prerequisites

- Node.js >= 18
- npm >= 9

## Quick Start

```bash
npm install
npm run dev         # http://localhost:5173
```

For full environment setup (SHUTTLE, Chur, ArcadeDB, Keycloak):
see [internal_docs/instructions/SETUP.md](../internal_docs/instructions/SETUP.md)

## Scripts

| Command | Description |
|---------|-------------|
| `npm run dev` | Start Vite dev server (port 5173) |
| `npm run build` | Production build |
| `npm test` | Run Vitest unit tests |
| `npm run test:ui` | Vitest UI mode |
| `npm run e2e` | Playwright E2E tests |
| `npm run e2e:ui` | Playwright UI mode |

## Architecture

React 19 + TypeScript 5.9 + Vite 8 + @xyflow/react + Zustand + React Query

- **Components:** `src/components/` — canvas nodes, inspector, profile, layout
- **Hooks:** `src/hooks/` — graph data, layout, hotkeys, search history
- **Store:** `src/stores/loomStore.ts` — Zustand with 11 typed slices
- **Utils:** `src/utils/` — display pipeline, transforms, layout
- **i18n:** `src/i18n/locales/{en,ru}/common.json` (~420 keys)

## Tests

- **Unit:** 199 tests via Vitest (pure functions + store slices + components)
- **E2E:** 5 smoke tests via Playwright (login, canvas render)

## Docker

```bash
cd .. && docker compose up --build verdandi -d   # http://localhost:15173
```

Port matrix: verdandi:15173 | Chur:13000 | SHUTTLE:18080 | Keycloak:18180 | ArcadeDB:2480
