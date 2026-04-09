# Sprint 7 — "Keycloak + Фундамент" (08–13 апреля 2026)

**Создан:** 08.04.2026
**Контекст:** Phase 3 завершена (12/12). Sprint 5+6 закрыл все фичи + рефакторинг. Самописный auth (bcrypt + ArcadeDB User) заменяется на Keycloak.

---

## Архитектура: BFF (Backend-for-Frontend) через Direct Access Grants

```
Browser                    Chur (BFF)                 Keycloak              SHUTTLE
  │                          │                          │                     │
  ├─ POST /auth/login ──────►│                          │                     │
  │  {username, password}     ├─ POST /token ───────────►│                     │
  │                          │  grant_type=password      │                     │
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
  │                          │  X-Seer-User: admin       │                   │
```

**Принцип:** API-контракт для фронтенда НЕ меняется. Frontend — 0 изменений. SHUTTLE — 0 изменений.

---

## Блок 1: Keycloak Integration (обязательно) — 16–20 ч

| # | Задача | Оценка | Описание |
|---|--------|--------|----------|
| KC-01 | **Keycloak realm JSON** | 3 ч | Realm `seer`, client `verdandi-bff` (confidential, Direct Access Grants ON). Realm roles: viewer, editor, admin. Protocol mapper → claim `seer_role`. Seed users: admin/editor/viewer. Файл: `keycloak/seer-realm.json` |
| KC-02 | **Docker Compose + Keycloak** | 3 ч | Keycloak 26.x (`start-dev --import-realm`), порт 8180, healthcheck start_period 60s. + ArcadeDB, Chur, SHUTTLE, verdandi. Файлы: `docker-compose.yml`, Dockerfile для каждого сервиса |
| KC-03 | **keycloak.ts — HTTP-клиент** | 3 ч | `exchangeCredentials()`, `refreshAccessToken()`, `fetchJwks()` — чистый fetch, без SDK. Зависимость: `jose` для JWKS. Файл: `Chur/src/keycloak.ts` (новый) |
| KC-04 | **sessions.ts — session store** | 2 ч | In-memory `Map<sid, Session>`. CRUD + mutex для refresh race condition. Cleanup expired каждые 5 мин. Файл: `Chur/src/sessions.ts` (новый) |
| KC-05 | **auth.ts — login/logout/me** | 3 ч | Перезапись с сохранением API shape. Login: exchangeCredentials → createSession → setCookie("sid"). Me: getSession → refresh if expired → user. Logout: deleteSession → Keycloak /logout |
| KC-06 | **rbac.ts — session-based auth** | 2 ч | `app.authenticate`: cookie "sid" → getSession → refresh → request.user. `app.authorizeQuery`: без изменений |
| KC-07 | **server.ts + config.ts + cleanup** | 1 ч | Удалить @fastify/jwt. Env vars: KEYCLOAK_URL, REALM, CLIENT_ID, CLIENT_SECRET, COOKIE_SECRET. Удалить `users.ts`, `seed/`. +jose -bcryptjs -@fastify/jwt |

### Изменения по файлам Chur

| Файл | Действие |
|------|----------|
| `src/config.ts` | Изменить — keycloak env vars вместо jwt* |
| `src/keycloak.ts` | **Создать** — HTTP-клиент (token exchange, refresh, JWKS) |
| `src/sessions.ts` | **Создать** — In-memory session store с mutex |
| `src/routes/auth.ts` | Перезаписать — login/me/logout через Keycloak |
| `src/plugins/rbac.ts` | Изменить — cookie sid → session lookup |
| `src/server.ts` | Изменить — убрать @fastify/jwt, signed cookies |
| `src/types.ts` | Изменить — убрать JWT augmentation |
| `src/users.ts` | **Удалить** — пользователи в Keycloak |
| `package.json` | Изменить — +jose +@fastify/rate-limit -bcryptjs -@fastify/jwt |
| `seed/*` | **Удалить** — seed в realm JSON |

### Frontend: НЕТ изменений | SHUTTLE: НЕТ изменений

---

## Блок 2: Качество (обязательно) — 6–7 ч

| # | Задача | Оценка | Описание |
|---|--------|--------|----------|
| Q-01 | **ErrorBoundary** | 1 ч | React Error Boundary, fallback UI с retry. `ErrorBoundary.tsx` + обернуть в `App.tsx` |
| Q-02 | **Component tests** | 4 ч | RTL тесты: ExportPanel, SearchPanel, NodeContextMenu, InspectorPanel. jsdom в vitest |
| Q-03 | **Pre-commit hooks** | 1 ч | Husky + lint-staged: ESLint + tsc --noEmit + vitest на staged |

---

## Блок 3: DevOps (обязательно) — 3–4 ч

| # | Задача | Оценка | Описание |
|---|--------|--------|----------|
| D-01 | **GitHub Actions CI** | 2–3 ч | lint → type-check → unit tests → build. Кэш deps |
| D-02 | **Environment profiles** | 1 ч | `.env.example` + startup validation (fail-fast без секретов) |

---

## Блок 4: Безопасность (обязательно) — 1 ч

| # | Задача | Оценка | Описание |
|---|--------|--------|----------|
| SEC-01 | **Rate limiting /auth/login** | 1 ч | @fastify/rate-limit: 5 req/мин на IP, 100 req/мин общий |

---

## Блок 5: Тех.долг (stretch) — 4–5 ч

| # | Задача | Оценка | Описание |
|---|--------|--------|----------|
| TD-08 | **React Query retry** | 1 ч | retry: 2 + exponential delay |
| TD-09 | **FilterToolbar dedup** | 2–3 ч | Извлечь BaseFilterToolbar |
| U-02 | **Constants extraction** | 1 ч | Magic numbers → `constants.ts` |

---

## Расписание

```
Вторник 08.04:  KC-01 (Realm JSON)          3ч
                KC-02 (Docker Compose)       3ч       → docker compose up → Keycloak :8180
                                             ─────  ~6ч

Среда 09.04:    KC-03 (keycloak.ts)          3ч
                KC-04 (sessions.ts)          2ч       → curl token endpoint → OK
                KC-07 (config + cleanup)     1ч
                                             ─────  ~6ч

Четверг 10.04:  KC-05 (auth.ts)              3ч
                KC-06 (rbac.ts)              2ч       → POST /auth/login → sid cookie → /graphql OK
                SEC-01 (Rate limiting)       1ч
                                             ─────  ~6ч

Пятница 11.04:  Q-01 (ErrorBoundary)         1ч
                Q-02 (Component tests)       4ч       → npm test — 40+ green
                Q-03 (Pre-commit hooks)      1ч
                                             ─────  ~6ч

Суббота 12.04:  D-01 (GitHub Actions CI)     2–3ч
 (буфер)       D-02 (Env profiles)          1ч       → CI green
                TD-08 (React Query retry)    1ч
                                             ─────  ~5ч (stretch)
```

**Итого:** ~26–28 ч (обязательные) + ~5 ч (stretch)

---

## Keycloak Realm Config

- **Realm:** `seer`
- **Client:** `verdandi-bff` (confidential, Direct Access Grants ON, Standard Flow OFF)
- **Port:** 8180
- **Realm roles:** viewer (default), editor, admin
- **Protocol mapper:** `seer_role` → string claim из realm role
- **Users:** admin/admin (admin), editor/editor (editor), viewer/viewer (viewer)
- **Access token lifetime:** 5 мин
- **Refresh token lifetime:** 30 мин
- **SSO Session Max:** 8 ч

---

## Token Refresh Strategy

**Lazy refresh** в `app.authenticate` preHandler:
1. Cookie "sid" → getSession(sid)
2. Если `accessExpiresAt > Date.now() + 30_000` → токен валиден
3. Иначе → `refreshAccessToken(session.refreshToken)`
4. Если refresh failed (400 invalid_grant) → deleteSession → 401
5. Mutex per session ID предотвращает параллельный refresh

---

## Метрики

| Метрика | Сейчас | Цель |
|---------|--------|------|
| Auth provider | bcrypt + ArcadeDB | **Keycloak 26.x** |
| Token refresh | Нет (8h JWT) | **Lazy refresh** |
| User management | SQL seed | **Keycloak Admin** |
| Тесты | 26 | **40+** |
| CI | Нет | **GitHub Actions** |
| Docker | Нет | **5 сервисов** |
| ErrorBoundary | Нет | **Canvas + panels** |
| Rate limiting | Нет | **5 req/min** |
| Secrets | Plaintext | **Env vars** |

---

## Definition of Done

- [ ] `docker compose up` → 5 сервисов healthy, Keycloak UI на :8180
- [ ] Логин admin/admin через UI → canvas → KNOT работает
- [ ] GET /auth/me → `{ id, username, role }` из Keycloak
- [ ] Lazy refresh прозрачен (5+ мин бездействия)
- [ ] Keycloak Admin → новый user → логин работает
- [ ] Keycloak Admin → viewer роль → write → 403
- [ ] `npm test` — 40+ green
- [ ] CI green на push
- [ ] Pre-commit блокирует lint/type ошибки
- [ ] 6-й login за минуту → 429
- [ ] Production без KEYCLOAK_CLIENT_SECRET → crash

---

## Не входит в Sprint 7

- Authorization Code Flow + PKCE (SSO/MFA) → Phase 5
- Redis session store → при горизонтальном масштабировании
- E2E Playwright → Sprint 8
- Hook tests → Sprint 8
- Performance / virtualization → Phase 4
- ANVIL → Phase 5

---

## Риски

| Риск | Митигация |
|------|-----------|
| Keycloak стартует ~60s | healthcheck start_period: 60s |
| ROPC deprecated в OAuth 2.1 | KC поддерживает; миграция на Auth Code в Phase 5 |
| Race condition refresh | Mutex per session в sessions.ts |
| Sessions теряются при рестарте Chur | useOnUnauthorized → auto redirect /login |
| Realm JSON сложно собрать | Создать через Admin UI → Export |

## Rollback

`AUTH_PROVIDER=local` в config.ts → старый bcrypt flow (users.ts в git history).
Docker: Keycloak под `profiles: [auth]`.
