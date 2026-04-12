# AIDA Development Guide

## Prerequisites
- Java 21 (Eclipse Temurin recommended)
- Node.js 24+
- Docker & Docker Compose
- Gradle 9.0 (included via wrapper — no manual install needed)
- GNU Make (optional, for Makefile targets)

## Repository structure

```
aida-root/
├── libraries/hound/        # SQL parser & semantic engine (Java 21, ANTLR)
├── services/shuttle/        # Data lineage GraphQL API (Quarkus 3, Java 21)
├── bff/chur/                # Auth gateway & BFF (Fastify, TypeScript)
├── frontends/verdandi/      # Data lineage UI (React 19, Vite)
├── infra/keycloak/          # IAM realm config
├── docker-compose.yml       # Full dev stack
└── Makefile                 # Quick commands
```

## Opening projects in IntelliJ IDEA

### Full monorepo (all modules)
Open `aida-root/` → IDEA detects root `settings.gradle` → all Gradle modules loaded.

### Individual module (standalone)
Open any module directory directly:
- `libraries/hound/` → standalone Gradle project (own `gradlew` + `settings.gradle`)
- `services/shuttle/` → standalone Gradle project (own `gradlew` + `settings.gradle`)
- `bff/chur/` → Node.js project (`package.json`)
- `frontends/verdandi/` → Node.js project (`package.json`)

Each JVM module has its own gradle wrapper and `settings.gradle`,
so IDEA recognizes it as an independent project.

## Quick start — all services

### Option A: Gradle (recommended on Windows)
```bash
./gradlew devAll
# Opens SHUTTLE (:8080), Chur (:3000), verdandi (:5173) in separate windows
```

### Option B: Makefile
```bash
make dev
```

### Option C: Docker Compose (full stack with Keycloak + ArcadeDB)
```bash
docker compose up -d
# Ports: Keycloak :18180, SHUTTLE :18080, Chur :13000, verdandi :15173
```

## Building individual services

### Hound (SQL parser library)
```bash
cd libraries/hound
./gradlew build           # compile + test
./gradlew fatJar          # standalone JAR → build/libs/hound-1.0.0-all.jar
./gradlew run --args='--help'  # CLI usage

# Or from monorepo root:
./gradlew :libraries:hound:build
```

### SHUTTLE (GraphQL API)
```bash
cd services/shuttle
./gradlew quarkusDev      # dev mode with live reload on :8080
./gradlew build           # uber-jar

# Or from monorepo root:
./gradlew :services:shuttle:quarkusDev
```

### Chur (auth gateway)
```bash
cd bff/chur
npm ci
npm run dev               # tsx watch on :3000
npm test                  # vitest
npm run build             # tsc → dist/
```

### verdandi (frontend)
```bash
cd frontends/verdandi
npm ci
npm run dev               # Vite HMR on :5173
npm test                  # vitest
npm run build             # production build → dist/
```

## Running Hound batch processing
```bash
# Against local ArcadeDB:
cd libraries/hound
./gradlew runBatchLocal   # hardcoded test params (localhost:2480, db=hound)

# With custom params:
./gradlew runBatch -Pinput=C:/sql -Pdb=mydb -Phost=arcadedb.local
```

## Testing
```bash
# All tests (from root):
make test
# Or:
./gradlew :libraries:hound:test :services:shuttle:test
cd bff/chur && npm test
cd frontends/verdandi && npm test

# E2E tests (requires full Docker stack):
docker compose up -d
cd frontends/verdandi && npm run e2e
```

## Docker
```bash
# Dev stack:
docker compose up -d --build

# Production:
docker compose -f docker-compose.prod.yml up -d

# Rollback to specific SHA:
./scripts/rollback.sh <git-sha>
```

## Environment variables
Copy `.env.example` to `.env` and configure:
- `KEYCLOAK_CLIENT_SECRET` (required in prod)
- `COOKIE_SECRET` (required in prod)
- `ARCADEDB_ROOT_PASSWORD` (required in prod)

See `.env.example` for full list with defaults.

## CI/CD
- CI runs on push to `master`/`feature/**` and PRs to `master`
- CD deploys to production on push to `master`
- Images published to GHCR (SHUTTLE, Chur, verdandi)
