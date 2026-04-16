# Track B — ELK Quick Wins + T-K0.1 + Keycloak Scopes

## Context
Three independent prep tasks to be done before May 4:
1. **ELK Quick Wins (M-1/3/5/6/7)** — perf improvements for large Verdandi graphs (17K+ edges hang the browser)
2. **T-K0.1 env vars** — fill gaps in env parametrization; create missing .env.example files
3. **Keycloak scopes** — expand realm with 8 scopes + 2 roles; replace M1 role-check with scope-based RBAC in Chur

**Order:** ELK → T-K0.1 → Keycloak. ELK is fully isolated to Verdandi frontend. T-K0.1 and Keycloak touch backend/chur and can run in parallel after ELK.

---

## Step 0 — Branch + save plan
```bash
git checkout -b feature/b-elk-wins-env-keycloak
```
Save this plan to `docs/sprints/TRACK_B_MAY4.md`.

---

## Task 1 — ELK Quick Wins

### Files
- `frontends/verdandi/src/utils/constants.ts`
- `frontends/verdandi/src/utils/layoutGraph.ts`
- `frontends/verdandi/src/hooks/canvas/useLoomLayout.ts`
- `frontends/verdandi/src/hooks/canvas/useDisplayGraph.ts`

### Key findings from code exploration

**constants.ts** current state:
- `LARGE_GRAPH_THRESHOLD = 500` (switches ELK strategy to LINEAR_SEGMENTS)
- `TIMEOUT_MS = 15_000` (not 30s as spec assumed — keep 15s as the base)
- No `AUTO_GRID_THRESHOLD`, `TABLE_LEVEL_THRESHOLD`, or `ELK_TIMEOUT_LARGE`

**layoutGraph.ts** current state:
- `graphFingerprint()` (lines ~102–107): uses `sort().join(',')` for both node keys and edge IDs — M-6 target
- `runElkLayout()`: `Promise.race` with fixed `LAYOUT_TIMEOUT` constant — need to make dynamic for M-7
- Grid fallback exists but is **reactive** (on timeout/error). M-3 needs a **proactive** guard at V>800
- Flat layout path: edges passed to ELK — M-1 dedup goes here
- Compound layout already deduplicates cross-group edges (lines ~278–294) — M-1 is for flat path only
- Export is `applyELKLayout(nodes, edges): Promise<LoomNode[]>`

**useLoomLayout.ts** current state:
- Calls `applyELKLayout(displayGraph.nodes, displayGraph.edges).then(layoutedNodes => setNodes(layoutedNodes))`
- Has `layouting` + `layoutError` state; no `layoutWarning` state
- Timeout is internal to layoutGraph.ts — need to expose it as a parameter for M-7

**useDisplayGraph.ts** current state:
- 9-phase pipeline; phase 4: `applyTableLevelView(g, viewLevel, filter.tableLevelView)`, phase 6: `applyCfEdgeToggle(g, viewLevel, filter.showCfEdges, filter.tableLevelView)`
- `filter.tableLevelView` comes from store/filter context
- M-5: override to `true` when `g.nodes.length > TABLE_LEVEL_THRESHOLD` before phases 4 and 6

---

### Implementation

#### 1.1 `constants.ts` — add 3 constants to LAYOUT section
```typescript
AUTO_GRID_THRESHOLD: 800,    // M-3: skip ELK, use grid directly
TABLE_LEVEL_THRESHOLD: 500,  // M-5: auto tableLevelView
ELK_TIMEOUT_LARGE: 8_000,    // M-7: reduced timeout for V>1000
```

#### 1.2 `layoutGraph.ts` — four changes

**M-6: Replace fingerprint sort with rolling hash**
Replace `graphFingerprint()`:
```typescript
// Replace sort().join(',') for edges with XOR rolling hash:
function hashEdges(edges: LoomEdge[]): number {
  let h = 0;
  for (const e of edges) {
    const s = fnv1a(e.source + e.target);
    h = h ^ s ^ (s << 7) ^ (s >>> 25);
  }
  return h;
}
function fnv1a(s: string): number {
  let h = 0x811c9dc5;
  for (let i = 0; i < s.length; i++) { h ^= s.charCodeAt(i); h = Math.imul(h, 0x01000193) >>> 0; }
  return h;
}
// Fingerprint becomes:
function graphFingerprint(nodes, edges): string {
  const nodeKey = nodes.map(n => `${n.id}:${getNodeHeight(n)}`).sort().join(','); // nodes still sorted (small array)
  return `${nodeKey}|${hashEdges(edges)}`;
}
```
Node keys still sorted (typically small array); only edge hash changes.

**M-1: Deduplicate edges before flat ELK call**
In the flat layout path, before edges are passed to `runElkLayout`:
```typescript
function deduplicateEdges(edges: LoomEdge[]): LoomEdge[] {
  const seen = new Set<string>();
  return edges.filter(e => {
    const key = `${e.source}→${e.target}`;
    return seen.has(key) ? false : (seen.add(key), true);
  });
}
// Apply: const elkEdges = deduplicateEdges(filteredEdges);
```

**M-3: Proactive grid guard + isGrid flag in return type**
Change `applyELKLayout` signature:
```typescript
// Return type changes from Promise<LoomNode[]> to Promise<{ nodes: LoomNode[], isGrid: boolean }>
export async function applyELKLayout(
  nodes: LoomNode[],
  edges: LoomEdge[],
  options: { timeout?: number } = {}
): Promise<{ nodes: LoomNode[]; isGrid: boolean }> {

  // M-3: skip ELK for very large graphs
  if (nodes.length > AUTO_GRID_THRESHOLD) {
    return { nodes: applyGridPositions(nodes), isGrid: true };
  }

  // ... existing ELK code, with timeout from options.timeout ?? TIMEOUT_MS
  // Existing grid fallback (on error/timeout): return { nodes: gridResult, isGrid: true }
  // Normal ELK success: return { nodes: layoutedNodes, isGrid: false }
}

// Grid helper (already exists as fallback — extract/reuse it):
function applyGridPositions(nodes: LoomNode[]): LoomNode[] {
  const COLS = Math.ceil(Math.sqrt(nodes.length));
  return nodes.map((n, i) => ({
    ...n,
    position: { x: (i % COLS) * 240, y: Math.floor(i / COLS) * 120 },
  }));
}
```

**M-7: Dynamic timeout parameter**
Pass `options.timeout` to `runElkLayout` (replace the internal `LAYOUT_TIMEOUT` reference with a parameter).

#### 1.3 `useLoomLayout.ts` — M-7 timeout selection + M-3 warning

```typescript
import { ELK_TIMEOUT_LARGE, LAYOUT, AUTO_GRID_THRESHOLD } from '../../utils/constants';

// Add state:
const [layoutWarning, setLayoutWarning] = useState<string | null>(null);

// In the layout effect, replace the applyELKLayout call:
const nodeCount = displayGraph.nodes.length;
const timeout = nodeCount > 1000 ? ELK_TIMEOUT_LARGE : LAYOUT.TIMEOUT_MS;

applyELKLayout(displayGraph.nodes, displayGraph.edges, { timeout })
  .then(({ nodes: layoutedNodes, isGrid }) => {
    setNodes(layoutedNodes);
    setEdges(displayGraph.edges);
    setGraphStats(layoutedNodes.length, displayGraph.edges.length);
    setLayoutWarning(isGrid ? `Граф содержит ${nodeCount} узлов — layout упрощён.` : null);
  })
  // ... rest unchanged
```

Add warning banner in the canvas JSX (wherever `layoutError` banner is rendered — same location):
```tsx
{layoutWarning && (
  <div className="layout-warning-banner">
    ⚠ {layoutWarning}
    <button onClick={() => applyELKLayout(nodes, edges, { timeout: LAYOUT.TIMEOUT_MS, forceELK: true })}>
      Вычислить полный layout
    </button>
  </div>
)}
```
Style using CSS variables (`--wrn`, `--acc`) consistent with existing canvas overlays.

#### 1.4 `useDisplayGraph.ts` — M-5 auto tableLevelView

In the pipeline, before phase 4, compute effective value:
```typescript
// After phase 3 (hidden nodes removed), before phase 4:
const effectiveTLV = g.nodes.length > TABLE_LEVEL_THRESHOLD ? true : filter.tableLevelView;

// Phase 4:
g = applyTableLevelView(g, viewLevel, effectiveTLV);
// ...
// Phase 6:
g = applyCfEdgeToggle(g, viewLevel, filter.showCfEdges, effectiveTLV);
```
Wrap in `useMemo` only if the pipeline is already memoized (follow existing patterns).

---

## Task 2 — T-K0.1 env vars

### Key findings from code exploration

**Already done (no changes needed):**
- `heimdall-backend/application.properties`: FRIGG_URL, CORS_ORIGINS — already parametrized
- `shuttle/application.properties`: YGG_URL (via `QUARKUS_REST_CLIENT_ARCADE_URL` in compose), HEIMDALL_URL — already parametrized
- `dali/application.properties`: FRIGG_URL — already parametrized
- `bff/chur/src/config.ts`: all URLs use `process.env` with fallbacks
- `bff/chur/src/middleware/heimdallEmit.ts`, `routes/heimdall.ts`: use `process.env.HEIMDALL_URL`
- `bff/chur/.env.example`: already exists and complete

**Actual gaps to fix:**

1. **`dali/application.properties`** — add HEIMDALL_URL and YGG_URL for future Dali→Heimdall/Ygg calls (prep):
```properties
quarkus.rest-client.heimdall-api.url=${HEIMDALL_URL:http://localhost:9093}
quarkus.rest-client.ygg-api.url=${YGG_URL:http://localhost:2480}
```

2. **`docker-compose.yml` dali service** — add env vars matching above:
```yaml
dali:
  environment:
    HEIMDALL_URL: http://heimdall-backend:9093
    YGG_URL:      http://HoundArcade:2480
    # existing FRIGG_URL stays
```

3. **`frontends/verdandi/src/.env.example`** — create new file:
```env
VITE_CHUR_URL=http://localhost:3000
VITE_SHUTTLE_URL=http://localhost:8080
```
(Search verdandi/src for `import.meta.env.VITE_` to confirm exact var names before creating)

4. **`frontends/heimdall-frontend/src/.env.example`** — create new file:
```env
VITE_HEIMDALL_API=http://localhost:9093
VITE_HEIMDALL_WS=ws://localhost:9093/ws/events
```
(Search heimdall-frontend/src for `import.meta.env.VITE_` to confirm exact var names)

> ⚠️ Before creating .env.example files: grep each frontend for `import.meta.env.VITE_` to get the actual variable names used.

---

## Task 3 — Keycloak scopes

### Files
- `infra/keycloak/seer-realm.json` (actual path — not `keycloak/`)
- `bff/chur/src/middleware/requireAdmin.ts`
- `bff/chur/src/` — find auth plugin / session type to verify `request.user` shape

### Key findings

**`infra/keycloak/seer-realm.json`** current state (116 lines):
- Has `aida:admin` and `aida:admin:destructive` clientScopes already
- Has realm roles: viewer, editor, admin (3 existing)
- `aida-bff` client has no `optionalClientScopes` array
- No `organizationsEnabled` field (need to add explicitly as false)

**`bff/chur/src/middleware/requireAdmin.ts`**: checks `request.user?.role !== 'admin'` (M1 temp pattern)

### Implementation

#### 3.1 `infra/keycloak/seer-realm.json`

Add to realm root:
```json
"organizationsEnabled": false
```

Add to `roles.realm` array (keep existing viewer/editor/admin):
```json
{ "name": "aida-admin",      "description": "Platform administrator" },
{ "name": "aida-superadmin", "description": "Super administrator, cross-tenant" }
```

Add to `clientScopes` array (keep existing `aida:admin` and `aida:admin:destructive`):
```json
{ "name": "seer:read",          "description": "Read lineage data, LOOM, KNOT, ANVIL", ... },
{ "name": "seer:write",         "description": "Save views, annotations, MIMIR full access", ... },
{ "name": "aida:harvest",       "description": "Run parse sessions, view Dali, event stream", ... },
{ "name": "aida:audit",         "description": "Audit log view and export", ... },
{ "name": "aida:tenant:admin",  "description": "Manage tenant users and quotas", ... },
{ "name": "aida:tenant:owner",  "description": "Tenant settings, billing, assign local-admin", ... },
{ "name": "aida:superadmin",    "description": "Platform settings, cross-tenant, Keycloak realm", ... }
```
All with `"protocol": "openid-connect"`, `"attributes": { "include.in.token.scope": "true" }`.

Update `aida-bff` client — add `optionalClientScopes`:
```json
"optionalClientScopes": [
  "seer:read", "seer:write",
  "aida:harvest", "aida:audit",
  "aida:tenant:admin", "aida:tenant:owner",
  "aida:admin", "aida:superadmin"
]
```

#### 3.2 `bff/chur/src/middleware/requireAdmin.ts` → requireScope

Before rewriting: **check auth plugin/session type** to verify:
- Does `request.user` have a `scopes: string[]` field?
- Where is scopes populated from the JWT `scope` claim?

If scopes aren't stored yet, add to session creation (in the auth plugin / OIDC callback):
```typescript
scopes: tokenPayload.scope?.split(' ') ?? [],
```

Then rewrite requireAdmin.ts:
```typescript
export function requireScope(...scopes: string[]) {
  return async (req: FastifyRequest, reply: FastifyReply) => {
    const sessionScopes: string[] = req.user?.scopes ?? [];
    const hasAll = scopes.every(s => sessionScopes.includes(s));
    if (!hasAll) {
      return reply.status(403).send({
        error: 'Forbidden',
        required: scopes,
        message: `Missing required scope(s): ${scopes.join(', ')}`,
      });
    }
  };
}

// Backward-compat aliases — routes don't need to change:
export const requireAdmin = requireScope('aida:admin');
export const requireDestructive = requireScope('aida:admin', 'aida:admin:destructive');
```

Existing route usages of `requireAdmin` (heimdall.ts, auth.ts) stay unchanged — the alias handles it.

---

## Verification

### Task 1 — ELK
```
[ ] npm run dev in frontends/verdandi
[ ] Open graph with ~400 nodes → ELK runs, no warning banner
[ ] Open graph with ~900 nodes → instant grid + warning banner appears
[ ] Click "Вычислить полный layout" button → ELK runs (with full timeout)
[ ] Modify graph → fingerprint changes → layout re-runs
[ ] Same graph reloaded → fingerprint matches → layout NOT re-run (cache hit)
[ ] Graph with >1000 nodes → timeout is 8s not 15s (check console logs)
[ ] tableLevelView auto-enabled for graph >500 nodes (column edges hidden)
```

### Task 2 — env vars
```
[ ] grep -r "import.meta.env.VITE_" frontends/verdandi/src → all vars covered in .env.example
[ ] grep -r "import.meta.env.VITE_" frontends/heimdall-frontend/src → all vars covered in .env.example
[ ] docker compose up → dali service starts, env vars logged at startup
[ ] dali can reach heimdall/ygg once those REST clients are implemented
```

### Task 3 — Keycloak
```
[ ] docker compose up (restart keycloak with updated realm)
[ ] Login as admin → JWT scope claim contains "aida:admin"
[ ] Login as viewer → JWT scope claim does NOT contain "aida:admin"
[ ] POST /heimdall/control/reset with viewer session → 403 with { required: ["aida:admin"] }
[ ] POST /heimdall/control/reset with admin session → 200
[ ] keycloak admin UI → aida-bff client → Scopes tab → 8 optional scopes visible
```
