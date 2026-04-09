# Sprint 8 — "Keycloak + DevOps" (11–18 апреля 2026)

**Создан:** 09.04.2026
**Контекст:** Sprint 7 завершён (PR #7, +3282/-848). 161 тест, ErrorBoundary, rate limiting, JWT refresh на месте. Самописный auth (bcrypt + ArcadeDB User) заменяется на Keycloak.

---

## Архитектура: BFF через Direct Access Grants

```
Browser                    Chur (BFF)                 Keycloak              SHUTTLE
  │                          │                          │                     │
  ├─ POST /auth/login ──────►├─ POST /token ───────────►│                     │
  │  {username, password}     │  grant_type=password      │                     │
  │                          │◄─ {access_token,          │                     │
  │                          │    refresh_token}         │                     │
  │                          ├─ sessions.set(sid, tokens)│                     │
  │◄─ Set-Cookie: sid=uuid  ─┤                          │                     │
  │   {id, username, role}   │                          │                     │
  │                          │                          │                     │
  ├─ POST /graphql ─────────►├─ sessions.get(sid)        │                     │
  │  Cookie: sid=uuid        ├─ [if expired: refresh] ──►│                     │
  │                          ├─ POST /graphql ──────────────────────────────►│
  │                          │  X-Seer-Role: admin       │   (без изменений) │
```

**Frontend — 0 изменений. SHUTTLE — 0 изменений.** API shape сохраняется.

---

## Блок 1: Keycloak Integration (обязательно) — 17 ч

| # | Задача | Оценка | День | Описание |
|---|--------|--------|------|----------|
| KC-01 | **Keycloak realm JSON** | 3 ч | Пт 11 | Realm `seer`, client `verdandi-bff` (confidential, Direct Access Grants). Roles: viewer/editor/admin. Protocol mapper → claim `seer_role`. Users: admin/editor/viewer. `keycloak/seer-realm.json` |
| KC-02 | **Docker Compose (5 сервисов)** | 4 ч | Пт 11 | Keycloak 26.x (:8180, `start-dev --import-realm`), ArcadeDB (:2480), SHUTTLE (:8080), Chur (:3000), verdandi (:5173). Healthchecks, volumes, seed. Dockerfiles для SHUTTLE (JVM) и verdandi (nginx) |
| KC-03 | **keycloak.ts — HTTP-клиент** | 3 ч | Сб 12 | `exchangeCredentials()`, `refreshAccessToken()`, `fetchJwks()` через fetch. Зависимость: `jose`. `Chur/src/keycloak.ts` |
| KC-04 | **sessions.ts — session store** | 2 ч | Сб 12 | In-memory `Map<sid, Session>`. CRUD + mutex для race condition. Cleanup каждые 5 мин. `Chur/src/sessions.ts` |
| KC-05 | **auth.ts — login/logout/me** | 3 ч | Пн 14 | Перезапись с сохранением API shape. Login: exchangeCredentials → createSession → setCookie("sid"). Me: getSession → lazy refresh. Logout: deleteSession → Keycloak /logout |
| KC-06 | **rbac.ts — session-based auth** | 2 ч | Пн 14 | `app.authenticate`: cookie "sid" → getSession → refresh → request.user. `app.authorizeQuery`: без изменений |
| KC-07 | **cleanup: config + server + deps** | 1 ч | Пн 14 | Удалить @fastify/jwt, users.ts, seed/. Env vars: KEYCLOAK_URL/REALM/CLIENT_ID/CLIENT_SECRET/COOKIE_SECRET. +jose -bcryptjs -@fastify/jwt |

### Файлы Chur

| Файл | Действие |
|------|----------|
| `src/keycloak.ts` | **Создать** — HTTP-клиент Keycloak |
| `src/sessions.ts` | **Создать** — in-memory session store |
| `src/routes/auth.ts` | **Перезаписать** — login/me/logout через Keycloak |
| `src/plugins/rbac.ts` | **Изменить** — session lookup вместо jwtVerify |
| `src/config.ts` | **Изменить** — keycloak vars, startup validation |
| `src/server.ts` | **Изменить** — убрать @fastify/jwt, signed cookies |
| `src/types.ts` | **Изменить** — убрать JWT augmentation |
| `src/users.ts` | **Удалить** |
| `seed/*` | **Удалить** (пользователи в realm JSON) |
| `package.json` | **Изменить** — +jose -bcryptjs -@fastify/jwt |

---

## Блок 2: CI + Cleanup (обязательно) — 6 ч

| # | Задача | Оценка | День | Описание |
|---|--------|--------|------|----------|
| D-02 | **GitHub Actions CI** | 4.5 ч | Вт 15 | 3 параллельных job: verdandi (lint→tsc→vitest→build), chur (tsc→vitest→build), shuttle (gradle test→build). Job 4: docker-build. Кэш node + gradle |
| D-03 | **.env.example** | 1 ч | Вт 15 | Все env vars включая Keycloak. Startup validation: fail-fast в production без секретов |
| P-04 | **React Query verify** | 0.5 ч | Вт 15 | staleTime:30s, retry:2 — подтвердить, закрыть |

---

## Блок 3: Quick Wins + E2E (желательно) — 8 ч

| # | Задача | Оценка | День | Описание |
|---|--------|--------|------|----------|
| QW-01 | **Удалить proto файлы** | 15 мин | Ср 16 | `verdandi/src/components/layout/proto/` — 3 файла |
| QW-02 | **Удалить mockData.ts** | 10 мин | Ср 16 | Если не используется |
| QW-05 | **hasMore в ExploreResult** | 1 ч | Ср 16 | `hasMore = rows.size() >= LIMIT` + фронт + тест |
| T-04 | **E2E Playwright smoke** | 5.5 ч | Ср 16 | login → canvas → node visible. НЕ в CI |

---

## Блок 4: Tech Debt (stretch) — 6 ч

| # | Задача | Оценка | День | Описание |
|---|--------|--------|------|----------|
| TD-09 | **FilterToolbar дедупликация** | 4 ч | Чт 17 | Shared primitives → `toolbar/ToolbarPrimitives.tsx` |
| U-02 | **Constants extraction** | 2 ч | Чт 17 | Magic numbers → `verdandi/src/constants.ts` |

---

## Расписание

```
Пт  11.04:  KC-01 (Realm JSON)           3ч
            KC-02 (Docker Compose 5 srv)  4ч    → docker compose up → Keycloak :8180
                                          ─────  ~7ч

Сб  12.04:  KC-03 (keycloak.ts)          3ч
            KC-04 (sessions.ts)           2ч    → curl token exchange OK
                                          ─────  ~5ч

Пн  14.04:  KC-05 (auth.ts перезапись)   3ч
            KC-06 (rbac.ts session-auth)  2ч    → POST /auth/login → sid → /graphql OK
            KC-07 (cleanup + config)      1ч
                                          ─────  ~6ч

Вт  15.04:  D-02 (GitHub Actions CI)     4.5ч
            D-03 (.env.example)           1ч    → CI green
            P-04 (RQ verify)             0.5ч
                                          ─────  ~6ч

Ср  16.04:  QW-01/02 (cleanup)           25м
            QW-05 (hasMore)              1ч
            T-04 (E2E Playwright)        5.5ч   → npm run e2e → green
                                          ─────  ~7ч

Чт  17.04:  TD-09 (FilterToolbar dedup)  4ч
 (stretch) U-02 (constants.ts)           2ч
                                          ─────  ~6ч (stretch)

Пт  18.04:  Integration test + CI dry-run + буфер
 (буфер)                                 ─────  ~4ч
```

**Итого:** ~31 ч (обязательные) + ~10 ч (stretch/buffer) = ~41 ч

---

## Keycloak Realm Config

- **Realm:** `seer` | **Client:** `verdandi-bff` (confidential, ROPC ON, Standard Flow OFF)
- **Port:** 8180 | **Roles:** viewer (default), editor, admin
- **Protocol mapper:** `seer_role` → string claim
- **Users:** admin/admin, editor/editor, viewer/viewer
- **Access token:** 5 мин | **Refresh token:** 30 мин | **SSO Max:** 8 ч

## Token Strategy

**Lazy refresh** в `app.authenticate`:
1. Cookie "sid" → getSession(sid)
2. `accessExpiresAt > Date.now() + 30_000` → валиден
3. Иначе → refreshAccessToken() → обновить session
4. Refresh failed → deleteSession → 401
5. Mutex per session против race condition

**Storage:** In-memory Map (при рестарте → перелогин, useOnUnauthorized уже обрабатывает 401)

---

## Метрики

| Метрика | Сейчас | Цель Sprint 8 |
|---------|--------|---------------|
| Auth provider | bcrypt + ArcadeDB | **Keycloak 26.x** |
| Token refresh | JWT refresh endpoint | **Lazy refresh через Keycloak** |
| User management | SQL seed | **Keycloak Admin Console** |
| Docker Compose | Нет | **5 сервисов** |
| CI pipeline | Нет | **4 parallel jobs** |
| .env.example | Нет | **Полная документация** |
| E2E | 0 | **1 smoke** |
| Тесты | 161 | **165+** |

---

## Definition of Done

- [ ] `docker compose up` → 5 сервисов healthy, Keycloak Admin на :8180
- [ ] Login admin/admin через UI → canvas → KNOT работает
- [ ] GET /auth/me → `{ id, username, role }` из Keycloak token
- [ ] Lazy refresh прозрачен (5+ мин бездействия)
- [ ] Keycloak Admin → новый user → логин работает
- [ ] Keycloak Admin → viewer роль → write → 403
- [ ] PR → CI: 4 jobs зелёные
- [ ] `.env.example` покрывает все vars включая Keycloak
- [ ] `NODE_ENV=production` без KEYCLOAK_CLIENT_SECRET → crash
- [ ] `npm run e2e` при стеке → 1 smoke green
- [ ] Proto + mockData удалены
- [ ] P-04 verified

---

## Не входит в Sprint 8

- Authorization Code Flow + PKCE → Phase 5 (SSO/MFA)
- Redis session store → при горизонтальном масштабировании
- E2E в CI → Sprint 9
- Hook tests → Sprint 9
- Native image SHUTTLE → JVM mode достаточен
- HOUND-DB-001 — внешняя зависимость

---

## Риски

| Риск | Митигация |
|------|-----------|
| Keycloak стартует ~60s | healthcheck start_period: 60s |
| ROPC deprecated в OAuth 2.1 | KC поддерживает; Auth Code в Phase 5 |
| Race condition refresh | Mutex per session в sessions.ts |
| Sessions теряются при рестарте | useOnUnauthorized → auto redirect /login |
| SHUTTLE Gradle build медленный | Multi-stage + cache layers |
| nginx proxy_pass routing | Тестировать через docker build |

## Rollback

`AUTH_PROVIDER=local|keycloak` в config.ts. Keycloak под Docker profiles.
