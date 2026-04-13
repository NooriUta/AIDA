# Track B — Отчёт о выполнении

**Дата:** 13.04.2026  
**Ветка:** `feature/b-elk-wins-env-keycloak`  
**Коммит:** `bb22f58`  
**Файлов изменено:** 22 (+909 / -93)

---

## Итог по задачам

| Задача | Статус |
|--------|--------|
| ELK Quick Wins (M-1/3/5/6/7) | ✅ выполнено |
| T-K0.1 Env vars | ✅ выполнено |
| Keycloak scopes + requireScope RBAC | ✅ выполнено |

---

## Задача 1 — ELK Quick Wins (Verdandi)

### Проблема
Графы с 17 000+ рёбер (BMRT, HOUND) зависали в браузере на несколько минут. ELK пытался layoutировать весь граф, `graphFingerprint` делал `sort().join(',')` за O(E log E), дублирующиеся рёбра попадали в расчёт повторно.

### Изменения

#### M-1 — Дедупликация рёбер перед ELK (`layoutGraph.ts`)
```typescript
function deduplicateEdges<T extends { source: string; target: string }>(edges: T[]): T[] {
  const seen = new Set<string>();
  return edges.filter(e => {
    const key = `${e.source}→${e.target}`;
    return seen.has(key) ? false : (seen.add(key), true);
  });
}
```
Применяется в flat-path до передачи рёбер в ELK.

#### M-3 — Проактивный grid-layout для V > 800 (`layoutGraph.ts`, `useLoomLayout.ts`, `LoomCanvas.tsx`)
Вместо ожидания таймаута:
```typescript
if (!options.forceELK && nodes.length > LAYOUT.AUTO_GRID_THRESHOLD) {
  return { nodes: applyGridLayout(nodes), isGrid: true };
}
```
При `isGrid: true` отображается баннер:
> ⚠ Граф содержит N узлов — layout упрощён. [Вычислить полный layout]

Кнопка запускает `forceELK: true` с полным таймаутом.

#### M-5 — Авто-tableLevelView для V > 500 (`useDisplayGraph.ts`)
```typescript
const effectiveTLV = g.nodes.length > LAYOUT.TABLE_LEVEL_THRESHOLD ? true : filter.tableLevelView;
g = applyTableLevelView(g, viewLevel, effectiveTLV);
g = applyCfEdgeToggle(g, viewLevel, filter.showCfEdges, effectiveTLV);
```
Скрывает cf-рёбра (17 000+ штук) автоматически на больших графах.

#### M-6 — FNV-1a rolling hash вместо sort().join() (`layoutGraph.ts`)
```typescript
function fnv1a(s: string): number { /* ... */ }
function hashEdges(edges: LoomEdge[]): number { /* XOR rolling hash, O(E) */ }
```
Отпечаток графа теперь O(N log N) для узлов + O(E) для рёбер вместо O(E log E).

#### M-7 — Динамический таймаут ELK (`constants.ts`, `useLoomLayout.ts`)
```typescript
ELK_TIMEOUT_LARGE: 8_000,  // V > 1000
TIMEOUT_MS:       15_000,  // default
```
В `useLoomLayout` таймаут выбирается по размеру графа.

### Новые константы (`constants.ts`)
```typescript
AUTO_GRID_THRESHOLD:    800,   // M-3
TABLE_LEVEL_THRESHOLD:  500,   // M-5
ELK_TIMEOUT_LARGE:    8_000,   // M-7
```

### Проверка
- BMRT (152 узла) → ELK за ~14 сек, без баннера ✅
- Граф 2076 узлов (HOUND) → мгновенный grid + баннер в консоли ✅
- `[LOOM] Skipping ELK — 2076 nodes > 800 threshold, using grid` ✅

---

## Задача 2 — T-K0.1 Env vars

### Проблема
Несколько сервисов имели жёстко зашитые URL без переменных окружения. Отсутствовали `.env.example` для фронтендов.

### Изменения

#### `services/dali/src/main/resources/application.properties`
Добавлены rest-client записи для будущих вызовов Dali → Heimdall и Dali → YGG:
```properties
quarkus.rest-client.heimdall-api.url=${HEIMDALL_URL:http://localhost:9093}
quarkus.rest-client.ygg-api.url=${YGG_URL:http://localhost:2480}
ygg.url=${YGG_URL:http://localhost:2480}
ygg.db=${YGG_DB:hound}
ygg.user=${YGG_USER:root}
ygg.password=${YGG_PASSWORD:playwithdata}
```

#### `docker-compose.yml` — dali service
```yaml
environment:
  HEIMDALL_URL: http://heimdall-backend:9093
  YGG_URL:      http://HoundArcade:2480
```

#### `frontends/verdandi/src/.env.example` (новый файл)
```env
VITE_AUTH_URL=http://localhost:3000/auth
VITE_GRAPHQL_URL=http://localhost:3000/graphql
```

#### `frontends/heimdall-frontend/src/.env.example` (новый файл)
```env
VITE_AUTH_URL=http://localhost:3000/auth
VITE_HEIMDALL_API=http://localhost:9093
VITE_HEIMDALL_WS=ws://localhost:9093/ws/events
```

#### `bff/chur/.env.example`
Добавлены переменные KC Admin API:
```env
KC_ADMIN_USER=admin
KC_ADMIN_PASS=admin
```

---

## Задача 3 — Keycloak scopes + requireScope RBAC

### Проблема
- Роли в KC: только `viewer`, `editor`, `admin` (3 шт.)
- RBAC в Chur: проверка `role === 'admin'` (временная M1-заглушка)
- JWT не содержал `sub` и `preferred_username`
- Нет `scope`-claim в токенах

### Изменения

#### `infra/keycloak/seer-realm.json`
**8 realm-ролей:**

| Роль | Описание |
|------|----------|
| `viewer` | Read-only access to lineage data |
| `editor` | Read + limited write, MIMIR with quota |
| `operator` | Run harvest, monitor Dali, event stream |
| `auditor` | Audit log and tool call trace, read-only |
| `local-admin` | Manage tenant users and quotas |
| `tenant-owner` | Tenant settings, billing, assign local-admin |
| `admin` | Full HEIMDALL, Sources, restart services |
| `super-admin` | Platform, cross-tenant, Keycloak realm |

**9 clientScopes** (+ добавлен `aida:admin:destructive` сохранён):

| Scope | Назначение |
|-------|-----------|
| `seer:read` | Read lineage, LOOM, KNOT, ANVIL |
| `seer:write` | Save views, annotations, MIMIR full |
| `aida:harvest` | Run parse sessions, view Dali |
| `aida:audit` | Audit log view and export |
| `aida:tenant:admin` | Manage tenant users and quotas |
| `aida:tenant:owner` | Tenant settings, billing |
| `aida:admin` | HEIMDALL admin access |
| `aida:admin:destructive` | HEIMDALL destructive ops |
| `aida:superadmin` | Platform settings, cross-tenant |

**Protocol mappers** (добавлены напрямую в `aida-bff` client):
- `oidc-sub-mapper` → claim `sub`
- `oidc-usermodel-attribute-mapper` → claim `preferred_username`
- `oidc-usermodel-realm-role-mapper` → claim `seer_roles` (multivalued)

**7 тестовых пользователей** с полными атрибутами (title, dept, phone, avatarColor, tz, dateFmt, startPage, pref.lang, notify.*):

| Username | Роль | Пароль |
|----------|------|--------|
| `admin` | admin | `admin` |
| `editor` | editor | `editor` |
| `viewer` | viewer | `viewer` |
| `alexey.petrov` | operator | `operator123` (temp) |
| `sergey.ivanov` | auditor | `auditor123` (temp) |
| `tenant.owner` | tenant-owner | `owner123` (temp) |
| `local.admin` | local-admin | `localadmin123` (temp) |

#### Chur BFF — scope pipeline

Полная цепочка `KC JWT → scopes в request`:

```
KC JWT scope claim
  → extractUserInfo() [keycloak.ts]
  → scopes: string[] на KeycloakUserInfo
  → createSession(..., scopes) [auth.ts → sessions.ts]
  → session.scopes
  → rbac plugin decorator: request.user.scopes = session.scopes
  → requireScope() middleware
```

**`bff/chur/src/middleware/requireAdmin.ts`** — полный рефакторинг:
```typescript
export function requireScope(...scopes: string[]) {
  return async (request: FastifyRequest, reply: FastifyReply): Promise<void> => {
    const sessionScopes: string[] = request.user?.scopes ?? [];
    const hasAll = scopes.every(s => sessionScopes.includes(s));
    if (!hasAll) {
      return reply.status(403).send({
        error: 'Forbidden', required: scopes,
        message: `Missing required scope(s): ${scopes.join(', ')}`,
      });
    }
  };
}
export const requireAdmin      = requireScope('aida:admin');
export const requireDestructive = requireScope('aida:admin', 'aida:admin:destructive');
```

**`bff/chur/src/types.ts`** — `UserRole` расширен до 8 значений + `scopes: string[]` в `SeerUser`.

**`bff/chur/src/routes/heimdall.ts`** — WS handler заменён на scope-проверку:
```typescript
const hasAdminScope = sessionUser?.scopes?.includes('aida:admin') ?? false;
if (!sessionUser || !hasAdminScope) { ws.close(1008, 'Forbidden'); return; }
```

### Исправленная ошибка — JWT без sub/preferred_username
**Причина:** `defaultClientScopes: ["openid","profile","email","roles"]` в realm JSON не применялся при `--import-realm` (клиент показывал пустой `defaultClientScopes` через Admin API).  
**Решение:** Protocol mappers добавлены непосредственно в `aida-bff` client.  
**Процедура переимпорта:** `DELETE /admin/realms/seer` → `docker restart keycloak` → автоматический reimport из mounted JSON.

---

## Состояние сервисов на 13.04.2026

### Docker-контейнеры (prod-подобный запуск)

| Контейнер | Внешний порт | Статус |
|-----------|-------------|--------|
| `aida-root-houndarcade-1` (YGG ArcadeDB) | 2480, 2424 | ✅ healthy (5h) |
| `aida-root-frigg-1` (FRIGG ArcadeDB) | 2481 | ✅ healthy (5h) |
| `aida-root-keycloak-1` | 18180 | ✅ healthy (1h) |
| `aida-root-shuttle-1` | 18080 | ✅ Up (5h) |
| `aida-root-chur-1` | 13000 | ✅ Up (5h) |
| `aida-root-heimdall-backend-1` | 19093 | ✅ healthy (5h) |
| `aida-root-heimdall-frontend-1` | 25174 | ✅ Up (5h) |
| `aida-root-verdandi-1` | 15173 | ✅ Up (5h) |
| `aida-root-shell-1` | 25175 | ✅ Up (5h) |

### Dev-сервисы (localhost, нативный запуск)

| Сервис | Порт | Endpoint | Статус |
|--------|------|---------|--------|
| Chur BFF | 3000 | `GET /health` → `{"ok":true}` | ✅ |
| Shuttle (GraphQL) | 8080 | `POST /graphql {"query":"{__typename}"}` → `{"data":{"__typename":"Query"}}` | ✅ |
| Heimdall Backend | 9093 | `GET /q/health` → `{"status":"UP"}` | ✅ |
| Dali | 9090 | `GET /q/health` → `{"status":"UP"}` | ✅ |
| Heimdall Frontend (Vite) | 5174 | — | ✅ |
| YGG ArcadeDB | 2480 | `GET /api/v1/server` → `v26.3.2` | ✅ |
| FRIGG ArcadeDB | 2481 | `GET /api/v1/server` → `v26.3.2` | ✅ |
| Keycloak (Docker) | 18180 | `GET /realms/seer` → public_key present | ✅ |

> Verdandi (5173) и Shell (5175) в dev-режиме работают через Docker-контейнеры.

---

## Структура изменённых файлов

```
bff/chur/
  .env.example                        ← KC_ADMIN_USER/PASS добавлены
  src/
    keycloak.ts                       ← scopes из JWT scope claim
    sessions.ts                       ← Session.scopes, createSession(scopes)
    types.ts                          ← UserRole: 8 значений, SeerUser.scopes
    plugins/rbac.ts                   ← request.user.scopes из session
    routes/auth.ts                    ← передача scopes в createSession
    routes/auth.test.ts               ← scopes в mock-декораторе
    routes/heimdall.ts                ← WS: scope-check вместо role-check
    middleware/requireAdmin.ts        ← requireScope() + алиасы

docker-compose.yml                    ← dali: HEIMDALL_URL, YGG_URL

frontends/
  verdandi/src/
    .env.example                      ← VITE_AUTH_URL, VITE_GRAPHQL_URL (новый)
    utils/constants.ts                ← +3 LAYOUT константы
    utils/layoutGraph.ts              ← LayoutResult, FNV-1a, dedup, grid guard
    hooks/canvas/useLoomLayout.ts     ← layoutWarning, triggerFullLayout
    hooks/canvas/useDisplayGraph.ts   ← effectiveTLV для M-5
    components/canvas/LoomCanvas.tsx  ← баннер + кнопка
    i18n/locales/{en,ru}/common.json  ← computeFullLayout i18n-ключ
  heimdall-frontend/src/
    .env.example                      ← VITE_AUTH_URL/HEIMDALL_API/WS (новый)

infra/keycloak/
  seer-realm.json                     ← 8 ролей, 9 scope, 7 пользователей, mappers

services/dali/src/main/resources/
  application.properties              ← HEIMDALL_URL, YGG_URL rest-clients
```

---

## Следующие шаги (Sprint 4 backlog)

- **R4.1** — Keycloak scope → role-to-scope mapping table (assign scopes to roles via client scope mapping)
- **R4.2** — `requireScope` применить к остальным Chur-маршрутам (query, sources, harvest)
- **R4.3** — FRIGG user quotas + source bindings (wired в `GET /heimdall/users`)
- **R4.4** — Heimdall RBAC endpoint guard с `X-Seer-Role` → scope validation
- **ELK** — тест `forceELK` кнопки на живом графе >800 узлов
