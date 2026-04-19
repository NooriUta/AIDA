# CVE Remediation Plan — All Findings

## Context

CVE scanner flagged vulnerabilities across 3 surface areas in the Docker runtime. All are fixable
without restructuring the application.

| Package | Version | Severity | CVEs | Root cause |
|---------|---------|----------|------|------------|
| `fastify` | 4.29.1 (npm) | 7.5h, 6.1m, 3.7l | CVE-2026-25223, -3635, -25224 | Project dep — needs major upgrade |
| `tar` | npm-bundled | 8.2h × 2 | CVE-2026-31802, -29786 | Bundled inside npm binary |
| `minimatch` | npm-bundled | 7.5h × 2 | CVE-2026-27904, -27903 | Bundled inside npm binary |
| `picomatch` | 4.0.3 (npm-bundled) | 7.5h, 5.3m | CVE-2026-33671, -33672 | Bundled inside npm binary |
| `brace-expansion` | 5.0.4 (npm-bundled) | 6.5m | CVE-2026-33750 | Bundled inside npm binary |
| `busybox` | 1.37.0-r30 (Alpine APK) | 6.5m | CVE-2025-60876 | OS layer of Alpine base image |

> `tar`, `minimatch`, `picomatch`, and `brace-expansion` are all found at
> `/usr/local/lib/node_modules/npm/node_modules/` — bundled with npm, not project deps.
> Upgrading npm in the Dockerfile fixes all four in one step.

---

## Branch

```bash
git checkout master && git pull
git checkout -b fix/cve-remediation-2026-apr
```

Save this plan to `docs/plans/cve-remediation-2026-apr.md`.

---

## Fix 1 — npm upgrade in all Dockerfiles (fixes 6 CVEs: tar, minimatch, picomatch, brace-expansion)

**Affected Dockerfiles:**
- `bff/chur/Dockerfile`
- `frontends/verdandi/Dockerfile`
- `frontends/shell/Dockerfile`
- `frontends/heimdall-frontend/Dockerfile`

**Change:** Add `RUN npm install -g npm@latest` in **both** builder and runner stages, right after each `FROM` line.

```dockerfile
FROM node:24-alpine AS builder
RUN npm install -g npm@latest        # patches tar/minimatch/picomatch/brace-expansion in npm bundle
WORKDIR /app
...

FROM node:24-alpine AS runner
RUN npm install -g npm@latest        # same — replaces npm in runtime layer too
WORKDIR /app
...
```

> Once a specific npm version is confirmed to carry tar ≥ 7.5.11 and minimatch ≥ 10.2.3,
> pin to that exact version (e.g. `npm@11.x.y`) for reproducible builds.

---

## Fix 2 — busybox / Alpine APK upgrade (fixes CVE-2025-60876)

**Change:** Add `RUN apk upgrade --no-cache` in the **runner** stage of each Dockerfile, after the
npm upgrade line. This pulls the patched busybox from the Alpine package repo without changing the
base image tag.

```dockerfile
FROM node:24-alpine AS runner
RUN npm install -g npm@latest && apk upgrade --no-cache
WORKDIR /app
...
```

For the `nginx:alpine` runner stages in the frontend Dockerfiles, apply the same `apk upgrade`:

```dockerfile
FROM nginx:alpine
RUN apk upgrade --no-cache
...
```

---

## Fix 3 — Fastify v4 → v5 upgrade in bff/chur (fixes CVE-2026-25223, -3635, -25224)

**Scope:** `bff/chur/` only — no other service uses Fastify.

### 3a. Update package.json

**File:** `bff/chur/package.json`

```json
"fastify": "^5.8.3",
"@fastify/cookie": "^10.0.0",
"@fastify/websocket": "^11.0.0",
"fastify-plugin": "^5.0.0"
```

Remove `@fastify/cors` if present — it is unused (manual CORS is already in `server.ts`).

### 3b. Regenerate lockfile

```bash
cd bff/chur
rm package-lock.json
npm install
```

### 3c. Source code review

| File | Risk | Notes |
|------|------|-------|
| `src/server.ts` | Low | 1 `reply.status().send()` — compatible with v5 |
| `src/plugins/rbac.ts` | Low | `fp()` from fastify-plugin; v5 API unchanged |
| `src/routes/auth.ts` | Low | 3 `reply.send()` calls — compatible |
| `src/routes/query.ts` | Low | 2 `reply.send()` calls — compatible |
| `src/routes/graphql.ts` | Low | 3 `reply.send()` calls — compatible |
| `src/routes/heimdall.ts` | **Medium** | 9 `reply.send()` + WebSocket — verify `@fastify/websocket` v11 handler API; if `SocketStream` is no longer exported, replace with `WebSocket` from `ws` package |
| `src/routes/prefs.ts` | Low | 4 `reply.send()` calls — compatible |
| `src/types.ts` | Low | `FastifyRequest` augmentation unchanged in v5 |

**Key v5 facts:**
- `reply.send()` still works — no mass refactor needed
- `reply.status(N).send(data)` chaining still works
- Node ≥ 20 required — satisfied (Docker uses Node 24)
- `fastify-plugin` v5 `fp()` call signature is unchanged

### 3d. TypeScript build check

```bash
cd bff/chur && npm run build
```

Fix any type errors from v5 type definitions (expected: minimal — only 2 routes use generics).

### 3e. Run tests

```bash
cd bff/chur && npm test
```

---

## Verification

1. `docker build -t chur-test bff/chur/` — succeeds
2. `docker run --rm chur-test npm --version` — shows patched npm version
3. `docker run --rm chur-test apk info busybox` — confirms patched busybox version
4. Re-run CVE scanner — 0 findings across all 6 packages

---

## Critical Files

- `bff/chur/package.json` — fastify version bump
- `bff/chur/src/routes/heimdall.ts` — highest-risk (WebSocket + 9 reply.send calls)
- `bff/chur/src/plugins/rbac.ts` — fastify-plugin usage
- `bff/chur/Dockerfile` — npm upgrade + apk upgrade
- `frontends/verdandi/Dockerfile` — npm + apk upgrade
- `frontends/shell/Dockerfile` — npm + apk upgrade
- `frontends/heimdall-frontend/Dockerfile` — npm + apk upgrade
