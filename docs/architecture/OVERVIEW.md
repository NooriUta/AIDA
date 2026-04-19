# AIDA Architecture

## System overview
AIDA is a data lineage analysis platform. It parses SQL code, builds
semantic graphs, and provides interactive visualization.

## Data flow

```
SQL files → [Hound] → ArcadeDB ← [SHUTTLE] ← [Chur] ← [verdandi]
```

1. **Hound** parses SQL files (17+ dialects via ANTLR), performs semantic
   analysis (scope resolution, lineage extraction), writes graph to ArcadeDB
2. **SHUTTLE** reads the graph from ArcadeDB via HTTP REST, exposes
   GraphQL API for lineage queries
3. **Chur** is the BFF/auth gateway: validates Keycloak JWT tokens,
   enforces RBAC (viewer/editor/admin), proxies GraphQL to SHUTTLE
4. **verdandi** is the React SPA: renders interactive lineage graphs
   using @xyflow/react + ELK layout engine

## Authentication

```
Keycloak (seer realm) → Chur (jose JWT validation) → X-Seer-Role headers → SHUTTLE
```

Roles: `viewer` (read-only), `editor` (read + limited write), `admin` (full access)

## Key ports

| Service      | Dev   | Docker |
|-------------|-------|--------|
| SHUTTLE     | 8080  | 18080  |
| Chur        | 3000  | 13000  |
| verdandi    | 5173  | 15173  |
| Keycloak    | 8180  | 18180  |
| ArcadeDB    | 2480  | 2480   |

## Technology stack

| Component | Runtime | Framework | Key Libraries |
|-----------|---------|-----------|---------------|
| Hound | Java 21 | — | ANTLR 4.13.2, ArcadeDB 25.12.1, Jackson, Guava |
| SHUTTLE | Java 21 | Quarkus 3.34.2 | SmallRye GraphQL, REST Client, Vert.x |
| Chur | Node 24 | Fastify 4.28 | jose (JWT), pino (logging) |
| verdandi | — | React 19 + Vite 8 | @xyflow/react, ELK, TanStack Query, Zustand, i18next |
| IAM | — | Keycloak 26.2 | OpenID Connect |
| Graph DB | — | ArcadeDB 25.12.1 | HTTP REST API |
