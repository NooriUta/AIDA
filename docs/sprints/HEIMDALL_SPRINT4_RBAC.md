# HEIMDALL — Sprint 4: RBAC & User Management

**Документ:** `HEIMDALL_SPRINT4_RBAC`
**Версия:** 1.3
**Дата:** 13.04.2026
**Владелец трека:** Track B
**Горизонт:** W6 May 19 → W8 Jun 6
**Предусловие:** Sprint 3 ✅ DONE · `RBAC_MULTITENANT.md` зафиксирован

---

## Правило спринта

**Phase 1 — single-tenant.** Multi-tenant Organizations в Keycloak — post-HighLoad (Q-MT1..Q-MT5 открыты).
Задачи спринта: scopes в realm, enforcement в Chur + HEIMDALL backend, Users UI.

---

## Цель спринта

```
Admin открывает HEIMDALL → Users
  → видит список пользователей с ролями, статусами и квотами
  → приглашает нового пользователя с ролью
  → редактирует роль, scopes, source bindings, квоты
  → редактирует профиль пользователя (должность, отдел, телефон)
  → настраивает персональные предпочтения (язык, тема, уведомления)
  → блокирует пользователя
  → Chur enforces scope на каждом маршруте
  → HEIMDALL backend проксирует в Keycloak Admin API
  → пользователь логинится — его настройки (тема, плотность, стартовая страница)
     применяются автоматически в HEIMDALL и Verdandi (sync через Keycloak attributes)
```

---

## Зависимости

| Зависимость | Статус | Примечание |
|---|---|---|
| Keycloak 26.2 :8180 running | ✅ | realm `seer`, client `aida-bff` |
| C.5.2 Keycloak rename `verdandi-bff` → `aida-bff` | ✅ DONE Sprint M1 | |
| Chur auth middleware (authenticate) | ✅ DONE | |
| HEIMDALL backend :9093 running | ✅ DONE Sprint 2 | |
| `RBAC_MULTITENANT.md` | ✅ | reference для всех решений |
| `UsersPage.tsx` (заглушка) | ⏳ | полная реализация в R4.5 |
| Прототип Users UI | ✅ | `docs/sprints/heimdall_users_prototype.html` |

---

## Задачи

| ID | Задача | Слой | Неделя | Оценка |
|---|---|---|---|---|
| **W6** | | | | |
| R4.1 | Keycloak scopes + realm export | Keycloak | W6 | 2 ч |
| R4.2 | Chur requireScope middleware | Chur | W6 | 3 ч |
| R4.3 | HEIMDALL backend TenantContext + AdminResource | Backend | W6–W7 | 4 ч |
| **W7** | | | | |
| R4.4 | KeycloakAdminClient (REST proxy) | Backend | W7 | 3 ч |
| R4.9 | UserProfile attributes — хранение в Keycloak (title, dept, phone) | Backend | W7 | 2 ч |
| R4.10 | UserPreferences — хранение в Keycloak + API endpoints | Backend | W7–W8 | 3 ч |
| R4.5 | UsersPage — полная реализация (заменить заглушку) | Frontend | W7 | 4 ч |
| R4.6 | UserModal — sidebar nav, 6 секций (Профиль/Роль/Разрешения/Квоты/Источники/Активность) | Frontend | W7–W8 | 6 ч |
| **W8** | | | | |
| R4.11 | Chur: proxy profile/prefs + self-service `/me/` routes | Chur | W8 | 2 ч |
| R4.12 | UserModal «Настройки» — wire API (load/save profile + prefs) | Frontend | W8 | 2 ч |
| R4.13 | Apply prefs on login — usePrefsStore (тема, язык, плотность, стартовая страница) | Frontend | W8 | 2 ч |
| R4.14 | Verdandi prefs sync — appearance + graph settings → Keycloak + restore on login | Verdandi | W8 | 3 ч |
| R4.7 | Navigation guards (scope-based) | Frontend | W8 | 1 ч |
| R4.8 | Tests (AdminResource, Chur rbac, UsersPage, usePrefsStore, usePrefsSync) | All | W8 | 3 ч |

**Итого: ~39 ч**

### Критический путь

```
R4.1 → R4.2 → R4.3 → R4.4 ──┬──→ R4.5 → R4.6 ──→ R4.7
                               ├──→ R4.9 ──→ R4.10 → R4.11 → R4.12
                               └──────────────────────────────→ R4.13
R4.10 → R4.11 → R4.14
R4.6 + R4.12 + R4.13 → R4.8
```

---

## R4.1 · Keycloak scopes + realm export (~2 ч)

### Добавить client scopes в Keycloak Admin UI

Восемь новых scopes для client `aida-bff`:

```
seer:read           viewer — базовый read
seer:write          editor — аннотации, saved views
aida:harvest        operator — запуск harvest
aida:audit          auditor — audit log
aida:tenant:admin   local-admin — управление пользователями
aida:tenant:owner   tenant-owner — billing, tenant settings
aida:admin          admin — HEIMDALL, Sources, restart
aida:superadmin     super-admin — платформа, cross-tenant
```

**В Keycloak Admin UI:**
```
Realm Settings → Client Scopes → Create client scope
  Name: seer:read
  Protocol: OpenID Connect
  Include in token scope: ON
```

**Protocol mapper для `seer_role` claim** (уже существует, проверить):
```
Mapper type: User Realm Role
Token Claim Name: seer_role
Add to ID token: ON
Add to access token: ON
```

**Обновить `keycloak/seer-realm.json`** — добавить секцию clientScopes из `RBAC_MULTITENANT.md §4.4`.

После изменений — **экспортировать realm** и закоммитить:
```bash
# Экспорт через Admin REST API:
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8180/admin/realms/seer/partial-export \
  > keycloak/seer-realm.json
```

### Definition of Done R4.1
```bash
# JWT после логина содержит scopes:
curl -X POST http://localhost:8180/realms/seer/protocol/openid-connect/token \
  -d "grant_type=password&client_id=aida-bff&username=admin&password=..."
# → access_token декодируется: "scope": "openid seer:read aida:admin ..."
```

---

## R4.2 · Chur requireScope middleware (~3 ч)

### `bff/chur/src/middleware/rbac.ts` (новый файл)

```typescript
import type { FastifyRequest, FastifyReply } from 'fastify';
import { getSession } from './auth.js';

export interface AidaSession {
  userId:     string;
  username:   string;
  scopes:     string[];
  tenantId:   string | null;
  tenantRole: string | null;
  realmRoles: string[];
}

/**
 * Проверяет наличие ВСЕХ указанных scopes у пользователя.
 * Использовать как preHandler в маршрутах.
 */
export function requireScope(...requiredScopes: string[]) {
  return async (req: FastifyRequest, reply: FastifyReply) => {
    const session = getSession(req);
    if (!session) return reply.code(401).send({ error: 'unauthorized' });

    const missing = requiredScopes.filter(s => !session.scopes.includes(s));
    if (missing.length > 0) {
      return reply.code(403).send({
        error: 'insufficient_scope',
        required: requiredScopes,
        missing,
      });
    }
  };
}

/**
 * Блокирует cross-tenant запросы.
 * superadmin проходит всегда.
 * Остальные — только если tenantId совпадает с :tenantId в path.
 */
export function requireSameTenant() {
  return async (req: FastifyRequest, reply: FastifyReply) => {
    const session = getSession(req);
    if (!session) return reply.code(401).send({ error: 'unauthorized' });
    if (session.scopes.includes('aida:superadmin')) return; // pass

    const targetTenant = (req.params as any).tenantId
      ?? (req.body as any)?.tenantId;

    if (targetTenant && targetTenant !== session.tenantId) {
      return reply.code(403).send({ error: 'cross_tenant_access_denied' });
    }
  };
}
```

### Обновить маршруты в Chur

```typescript
// bff/chur/src/routes/heimdall.ts — обновить

import { requireScope, requireSameTenant } from '../middleware/rbac.js';

// Event stream — operator+
app.get('/heimdall/ws/events', {
  websocket: true,
  preHandler: [authenticate, requireScope('aida:harvest')]
}, wsProxy);

// Control (reset) — admin
app.post('/heimdall/control/reset', {
  preHandler: [authenticate, requireScope('aida:admin')]
}, proxy);

// Control (snapshot) — admin
app.post('/heimdall/control/snapshot', {
  preHandler: [authenticate, requireScope('aida:admin')]
}, proxy);
```

```typescript
// bff/chur/src/routes/admin.ts — новый файл

import { requireScope, requireSameTenant } from '../middleware/rbac.js';

// Список tenants (admin+)
app.get('/admin/tenants', {
  preHandler: [authenticate, requireScope('aida:admin')]
}, proxy);

// Пользователи tenant (local-admin+, только свой tenant)
app.get('/admin/tenants/:tenantId/users', {
  preHandler: [authenticate, requireScope('aida:tenant:admin'), requireSameTenant()]
}, proxy);

// Пригласить пользователя
app.post('/admin/tenants/:tenantId/users/invite', {
  preHandler: [authenticate, requireScope('aida:tenant:admin'), requireSameTenant()]
}, proxy);

// Изменить роль/scopes
app.put('/admin/tenants/:tenantId/users/:userId/role', {
  preHandler: [authenticate, requireScope('aida:tenant:admin'), requireSameTenant()]
}, proxy);

// Заблокировать
app.put('/admin/tenants/:tenantId/users/:userId/disable', {
  preHandler: [authenticate, requireScope('aida:tenant:admin'), requireSameTenant()]
}, proxy);
```

### Добавить trusted headers к upstream

Chur добавляет после успешной валидации JWT:

```typescript
// В proxy handler — обогатить headers
req.headers['x-seer-user-id']    = session.userId;
req.headers['x-seer-scopes']     = session.scopes.join(' ');
req.headers['x-seer-tenant']     = session.tenantId ?? '';
req.headers['x-seer-tenant-role']= session.tenantRole ?? '';
```

### Definition of Done R4.2
```bash
# Без токена → 401
curl http://localhost:3000/admin/tenants  # 401

# viewer токен → 403 на admin маршруте
curl -H "Cookie: sid=viewer-sid" http://localhost:3000/admin/tenants  # 403

# admin токен → 200
curl -H "Cookie: sid=admin-sid" http://localhost:3000/admin/tenants  # 200
```

---

## R4.3 · HEIMDALL backend TenantContext + AdminResource (~4 ч)

### `TenantContext.java`

```java
package studio.seer.heimdall.tenant;

import jakarta.enterprise.context.RequestScoped;
import java.util.List;

@RequestScoped
public class TenantContext {
    private String       tenantId;
    private String       tenantRole;
    private List<String> scopes;
    private String       userId;

    // getters / setters

    public boolean isSuperAdmin()  { return scopes.contains("aida:superadmin"); }
    public boolean isAdmin()       { return scopes.contains("aida:admin"); }
    public boolean isTenantOwner() { return scopes.contains("aida:tenant:owner"); }
    public boolean isLocalAdmin()  { return scopes.contains("aida:tenant:admin"); }
    public boolean hasScope(String s) { return scopes != null && scopes.contains(s); }
}
```

### `TenantContextFilter.java`

```java
package studio.seer.heimdall.tenant;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.ext.Provider;
import java.util.Arrays;

@Provider
@PreMatching
public class TenantContextFilter implements ContainerRequestFilter {

    @Inject TenantContext ctx;

    @Override
    public void filter(ContainerRequestContext req) {
        ctx.setUserId(req.getHeaderString("X-Seer-User-Id"));
        ctx.setTenantId(req.getHeaderString("X-Seer-Tenant"));
        ctx.setTenantRole(req.getHeaderString("X-Seer-Tenant-Role"));

        String scopesHeader = req.getHeaderString("X-Seer-Scopes");
        ctx.setScopes(scopesHeader != null
            ? Arrays.asList(scopesHeader.split(" "))
            : List.of());
    }
}
```

### `AdminResource.java`

```java
package studio.seer.heimdall.resource;

import studio.seer.heimdall.tenant.TenantContext;
import studio.seer.heimdall.keycloak.KeycloakAdminClient;
import studio.seer.heimdall.keycloak.model.*;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminResource {

    @Inject TenantContext ctx;
    @Inject KeycloakAdminClient keycloak;

    // ── Tenants ──────────────────────────────────────

    @GET @Path("/tenants")
    public List<TenantInfo> listTenants() {
        requireScope("aida:admin");
        // Phase 1: возвращаем один "дефолтный" tenant
        return List.of(TenantInfo.defaultTenant());
    }

    // ── Users ─────────────────────────────────────────

    @GET @Path("/tenants/{tenantId}/users")
    public List<UserInfo> listUsers(@PathParam("tenantId") String tenantId) {
        requireScope("aida:tenant:admin");
        requireSameTenant(tenantId);
        return keycloak.getUsers();
    }

    @POST @Path("/tenants/{tenantId}/users/invite")
    public Response invite(@PathParam("tenantId") String tenantId,
                           InviteRequest req) {
        requireScope("aida:tenant:admin");
        requireSameTenant(tenantId);
        // Validate: local-admin не может назначить admin/superadmin/tenant-owner
        if (!ctx.isAdmin() && isElevatedRole(req.role())) {
            return Response.status(403)
                .entity(Map.of("error", "cannot_assign_elevated_role")).build();
        }
        keycloak.inviteUser(req.email(), req.role());
        return Response.accepted().build();
    }

    @PUT @Path("/tenants/{tenantId}/users/{userId}/role")
    public Response assignRole(@PathParam("tenantId") String tenantId,
                                @PathParam("userId") String userId,
                                AssignRoleRequest req) {
        requireScope("aida:tenant:admin");
        requireSameTenant(tenantId);
        if (!ctx.isTenantOwner() && !ctx.isAdmin() && isElevatedRole(req.role())) {
            return Response.status(403)
                .entity(Map.of("error", "insufficient_privileges")).build();
        }
        keycloak.assignRole(userId, req.role());
        return Response.ok().build();
    }

    @PUT @Path("/tenants/{tenantId}/users/{userId}/disable")
    public Response disableUser(@PathParam("tenantId") String tenantId,
                                 @PathParam("userId") String userId) {
        requireScope("aida:tenant:admin");
        requireSameTenant(tenantId);
        keycloak.disableUser(userId);
        return Response.ok().build();
    }

    // ── Helpers ───────────────────────────────────────

    private void requireScope(String scope) {
        if (!ctx.hasScope(scope))
            throw new WebApplicationException(Response.status(403)
                .entity(Map.of("error", "insufficient_scope", "required", scope)).build());
    }

    private void requireSameTenant(String tenantId) {
        if (!ctx.isSuperAdmin()
                && ctx.getTenantId() != null
                && !ctx.getTenantId().equals(tenantId))
            throw new WebApplicationException(Response.status(403)
                .entity(Map.of("error", "cross_tenant_access_denied")).build());
    }

    private boolean isElevatedRole(String role) {
        return List.of("admin", "super-admin", "tenant-owner", "auditor").contains(role);
    }
}
```

### Definition of Done R4.3
```bash
# TenantContext заполняется из headers
# GET /admin/tenants без X-Seer-Scopes → 403
# GET /admin/tenants с X-Seer-Scopes: aida:admin → 200
# PUT /role с local-admin scope + elevated role → 403
```

---

## R4.4 · KeycloakAdminClient (~3 ч)

```java
package studio.seer.heimdall.keycloak;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import studio.seer.heimdall.keycloak.model.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@RegisterRestClient(configKey = "keycloak-admin")
@Path("/admin/realms/seer")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface KeycloakAdminClient {

    @GET @Path("/users")
    List<UserRepresentation> getUsers();

    @GET @Path("/users/{id}")
    UserRepresentation getUser(@PathParam("id") String id);

    @POST @Path("/users")
    void createUser(UserRepresentation user);

    @PUT @Path("/users/{id}")
    void updateUser(@PathParam("id") String id, UserRepresentation user);

    @GET @Path("/users/{id}/role-mappings/realm")
    List<RoleRepresentation> getUserRoles(@PathParam("id") String id);

    @POST @Path("/users/{id}/role-mappings/realm")
    void assignRealmRole(@PathParam("id") String id, List<RoleRepresentation> roles);
}
```

```properties
# application.properties
quarkus.rest-client.keycloak-admin.url=${KEYCLOAK_URL:http://keycloak:8180}
quarkus.rest-client.keycloak-admin.scope=openid
# Service account client для admin API:
quarkus.oidc-client.heimdall-admin.auth-server-url=${KEYCLOAK_URL}/realms/seer
quarkus.oidc-client.heimdall-admin.client-id=heimdall-service
quarkus.oidc-client.heimdall-admin.credentials.secret=${HEIMDALL_SERVICE_SECRET}
quarkus.oidc-client.heimdall-admin.grant.type=client_credentials
```

> **Примечание:** Нужен отдельный service account client `heimdall-service` в Keycloak с ролью `realm-management/manage-users`. Добавить в seer-realm.json.

---

## R4.5 · UsersPage — полная реализация (~4 ч)

> **Статус:** `src/pages/UsersPage.tsx` — чистая заглушка «Coming Soon».
> Требуется полная замена. Эталон: `docs/sprints/heimdall_users_prototype.html`.

### Структура страницы

```
UsersPage
├── Stats grid (4 карточки)
│   ├── Пользователей (всего / активных)
│   ├── Активных (заблокировано)
│   ├── С правами admin (local-admin и выше)
│   └── Source bindings (ограничены по источникам)
├── Filter bar
│   ├── select: Роль (viewer / editor / ... / super-admin)
│   ├── select: Статус (active / disabled)
│   ├── input: поиск по имени или email
│   └── кнопка «Сбросить» + счётчик «N из M»
├── Data table (8 колонок)
│   ├── Пользователь (аватар-инициалы + имя + email)
│   ├── Роль (цветной badge)
│   ├── Статус (active/disabled badge)
│   ├── Scopes (первые 2 + «+N»)
│   ├── Источники (первый + «+N» или «все»)
│   ├── Квоты (mimir/ч · sessions)
│   ├── Активность (lastActive)
│   └── Actions (Редакт. / Блок. / Разблок.)
├── UserEditModal (R4.6)
├── InviteModal (480px, email + имя + выбор роли)
└── ConfirmModal (блокировка пользователя)
```

### Управление доступом

```typescript
// Кнопка «Пригласить» видна только local-admin+
const canInvite = isLocalAdmin || isTenantOwner || isAdmin || isSuperAdmin;

// Кнопка «Блок./Редакт.» — только local-admin+
const canManage = isLocalAdmin || isTenantOwner || isAdmin || isSuperAdmin;
```

### Polling

```typescript
const POLL_MS = 30_000;
// refetch списка пользователей каждые 30с
```

### Definition of Done R4.5
```
- Страница /users показывает таблицу (не заглушку)
- Stats grid обновляется при изменении данных
- Фильтр по роли / статусу / поиску работает без перезагрузки
- Invite modal: email + роль → POST /admin/tenants/default/users/invite → toast «Приглашение отправлено»
- Confirm modal перед блокировкой
- viewer не видит кнопки Редакт./Блок.
```

---

## R4.6 · UserModal — sidebar nav, 6 секций (~6 ч)

> Эталон: `docs/sprints/heimdall_users_prototype.html` — modal 820×580, sidebar 192px.

### Структура модала

```
UserEditModal (820×580)
├── Header (48px) — аватар-инициалы + имя + email + статус-badge + close
├── Body
│   ├── Sidebar nav (192px)
│   │   ├── [Аккаунт]
│   │   │   ├── Профиль
│   │   │   ├── Роль
│   │   │   └── Разрешения
│   │   ├── [Ресурсы]
│   │   │   ├── Квоты
│   │   │   └── Источники
│   │   ├── [Мониторинг]
│   │   │   └── Активность
│   │   └── [Личный кабинет]
│   │       └── Настройки  ← реализуется в R4.12
│   └── Content area
│       ├── sec-profile   — Профиль
│       ├── sec-role      — Роль
│       ├── sec-permissions — Разрешения
│       ├── sec-quotas    — Квоты
│       ├── sec-sources   — Источники
│       ├── sec-activity  — Активность
│       └── sec-prefs     — Настройки (структура в R4.12)
└── Footer — [Заблокировать] ··· [Отмена] [Сохранить]
```

### sec-profile

```
Поля (2-колоночный grid):
  Имя пользователя | Email (readonly)
  Должность        | Отдел / Команда
Телефон (full-width, необязательно)

Блок «Статус аккаунта»:
  Активен  — toggle
  2FA      — badge «keycloak» (управляется KC)
```

### sec-role

```
Info banner: «Роль определяет базовый набор scopes»
Список role-карточек с tier-badge (user / tenant / platform):
  viewer · editor · operator · auditor
  local-admin · tenant-owner · admin · super-admin
Elevated роли (admin+) недоступны для local-admin
```

### sec-permissions

```
Info banner: «Scopes дополняют роль»
Таблица: Scope | Описание | Включён (✓ / —)
8 scopes из SCOPE_INFO
Elevated scopes (aida:admin, aida:superadmin, aida:tenant:owner)
  → disabled для local-admin
```

### sec-quotas

```
Блок «MIMIR & ANVIL»:
  Запросов MIMIR / час    [slider 0–100, step 5]
  Traversals ANVIL / час  [slider 0–200, step 10]

Блок «Dali — Парсинг»:
  Параллельных сессий     [slider 0–10]
  Max atoms / сессия      [slider 1K–100K, step 1K]
  Воркеров из пула (макс) [slider 1–8]
```

### sec-sources

```
Info banner: «Пустой список — доступ ко всем источникам по роли»
Toggle-список источников (из Dali Sources API)
```

### sec-activity

```
Stat cards (3): Сессий всего · MIMIR запросов · Последняя активность
Event log: timestamp | event_type | OK/FAIL badge
```

### Управление доступом в модале

```typescript
const ELEVATED_SCOPES = ['aida:tenant:owner', 'aida:admin', 'aida:superadmin'];
const ELEVATED_ROLES  = ['admin', 'super-admin', 'tenant-owner', 'auditor'];

// local-admin не видит и не назначает elevated
const assignableRoles  = isAdmin ? ALL_ROLES  : ALL_ROLES.filter(r => !ELEVATED_ROLES.includes(r.id));
const assignableScopes = isAdmin ? ALL_SCOPES : ALL_SCOPES.filter(s => !ELEVATED_SCOPES.includes(s));
```

### Definition of Done R4.6
```
- Открыть карточку любого пользователя → все 6 секций переключаются
- Профиль: name/email/title/dept/phone отображаются (заполнение из API — R4.12)
- Роль: выбрать другую → badge в header обновляется
- Разрешения: смена роли автоматически обновляет таблицу scopes
- Квоты: слайдеры двигаются, значения обновляются real-time
- Источники: toggle включает/выключает доступ
- Активность: лог событий виден
- Footer «Заблокировать» → confirm modal → пользователь заблокирован
- local-admin не видит карточки admin/super-admin/tenant-owner в sec-role
```

---

## R4.7 · Navigation guards (~1 ч)

```typescript
// src/components/AidaNav.tsx — обновить

const { isAdmin, isSuperAdmin, isLocalAdmin, isTenantOwner, hasScope } =
  useTenantContext();

// Users: local-admin и выше
const showUsers = isLocalAdmin || isTenantOwner || isAdmin || isSuperAdmin;

// HEIMDALL observability (Дашборд, Events): operator+
const showObservability = hasScope('aida:harvest') || isAdmin || isSuperAdmin;

// DEMODEBUG: только admin+
const showDemodebug = isAdmin || isSuperAdmin;
```

---

## R4.8 · Tests (~3 ч)

```typescript
// rbac.test.ts — Chur middleware

describe('requireScope', () => {
  it('passes when scope present',  async () => { /* 200 */ });
  it('blocks when scope missing',  async () => { /* 403 */ });
  it('superadmin passes all',      async () => { /* 200 */ });
});

describe('requireSameTenant', () => {
  it('blocks cross-tenant access', async () => { /* 403 */ });
  it('superadmin bypasses check',  async () => { /* 200 */ });
});
```

```java
// AdminResourceTest.java — HEIMDALL backend

@QuarkusTest
class AdminResourceTest {
    @Test void listUsers_withoutScope_returns403() { ... }
    @Test void listUsers_withAdminScope_returns200() { ... }
    @Test void assignElevatedRole_asLocalAdmin_returns403() { ... }
    @Test void assignNormalRole_asLocalAdmin_returns200() { ... }
}
```

---

---

## R4.9 · UserProfile attributes — Keycloak (~2 ч)

Хранение расширенных полей профиля как Keycloak user attributes (ключ-значение).

### Атрибуты в Keycloak

```
profile.title   → "Senior Data Engineer"
profile.dept    → "Analytics Team"
profile.phone   → "+7 900 000-00-00"
```

### `KeycloakAdminClient` — дополнить интерфейс

```java
// GET /admin/realms/seer/users/{id}  уже возвращает attributes map
// Добавить PUT для записи:
@PUT @Path("/users/{id}")
void updateUser(@PathParam("id") String id, UserRepresentation user);
// UserRepresentation.attributes = Map<String, List<String>>
```

### `AdminResource` — новые endpoints

```java
// GET /admin/tenants/{tenantId}/users/{userId}/profile
@GET @Path("/tenants/{tenantId}/users/{userId}/profile")
public UserProfileDto getProfile(...) {
    requireScope("aida:tenant:admin");
    requireSameTenant(tenantId);
    UserRepresentation u = keycloak.getUser(userId);
    return UserProfileDto.from(u.getAttributes());
}

// PUT /admin/tenants/{tenantId}/users/{userId}/profile
@PUT @Path("/tenants/{tenantId}/users/{userId}/profile")
public Response updateProfile(..., UserProfileDto dto) {
    requireScope("aida:tenant:admin");
    requireSameTenant(tenantId);
    keycloak.patchAttributes(userId, dto.toAttributes());
    return Response.ok().build();
}
```

### `UserProfileDto`

```java
public record UserProfileDto(String title, String dept, String phone) {
    static UserProfileDto from(Map<String, List<String>> attrs) { ... }
    Map<String, List<String>> toAttributes() {
        return Map.of(
            "profile.title", List.of(title != null ? title : ""),
            "profile.dept",  List.of(dept  != null ? dept  : ""),
            "profile.phone", List.of(phone != null ? phone : "")
        );
    }
}
```

### Definition of Done R4.9
```bash
curl -X PUT .../admin/tenants/default/users/{id}/profile \
  -H "X-Seer-Scopes: aida:tenant:admin" \
  -d '{"title":"Lead Engineer","dept":"Platform","phone":"+7 900 111"}'
# → 200

curl .../admin/tenants/default/users/{id}/profile \
  -H "X-Seer-Scopes: aida:tenant:admin"
# → {"title":"Lead Engineer","dept":"Platform","phone":"+7 900 111"}

# Keycloak Admin UI: пользователь → Attributes → profile.title присутствует
```

---

## R4.10 · UserPreferences — хранение + API (~3 ч)

Персональные настройки UI хранятся как Keycloak user attributes с префиксом `prefs.*`.
Endpoint доступен как admin (любой пользователь), так и в self-service (`/me/prefs`).

### Поля

| Ключ атрибута | Тип | Значения |
|---|---|---|
| `prefs.lang` | string | `ru` \| `en` |
| `prefs.theme` | string | `dark` \| `light` \| `auto` |
| `prefs.tz` | string | IANA timezone, напр. `Europe/Moscow` |
| `prefs.dateFmt` | string | `DD.MM.YYYY` \| `YYYY-MM-DD` \| `MM/DD/YYYY` |
| `prefs.density` | string | `compact` \| `normal` \| `comfortable` |
| `prefs.startPage` | string | `dashboard` \| `loom` \| `events` \| `services` |
| `prefs.avatarColor` | string | hex, напр. `#A8B860` |
| `prefs.notify.email` | bool | `true` \| `false` |
| `prefs.notify.browser` | bool | |
| `prefs.notify.harvest` | bool | |
| `prefs.notify.errors` | bool | |
| `prefs.notify.digest` | bool | |

### `UserPreferencesDto`

```java
public record UserPreferencesDto(
    String lang, String theme, String tz, String dateFmt,
    String density, String startPage, String avatarColor,
    boolean notifyEmail, boolean notifyBrowser,
    boolean notifyHarvest, boolean notifyErrors, boolean notifyDigest
) {
    static UserPreferencesDto from(Map<String, List<String>> attrs) { ... }
    Map<String, List<String>> toAttributes() { ... }

    /** Дефолтные значения для новых пользователей */
    static UserPreferencesDto defaults() {
        return new UserPreferencesDto("ru","dark","Europe/Moscow",
            "DD.MM.YYYY","normal","dashboard","#A8B860",
            true,false,true,true,false);
    }
}
```

### `AdminResource` — новые endpoints

```java
// Admin: GET/PUT любого пользователя
@GET  @Path("/tenants/{tenantId}/users/{userId}/prefs")
@PUT  @Path("/tenants/{tenantId}/users/{userId}/prefs")

// Self-service: пользователь читает/пишет свои настройки
@GET  @Path("/me/prefs")
@PUT  @Path("/me/prefs")
// Использует ctx.getUserId() вместо PathParam
```

### Definition of Done R4.10
```bash
# Admin читает prefs другого пользователя
curl .../admin/tenants/default/users/{id}/prefs \
  -H "X-Seer-Scopes: aida:tenant:admin"
# → {"lang":"ru","theme":"dark","tz":"Europe/Moscow", ...}

# Self-service: пользователь меняет свою тему
curl -X PUT .../admin/me/prefs \
  -H "X-Seer-User-Id: {myId}" \
  -d '{"theme":"light"}'  # partial update — merge with existing
# → 200
```

---

## R4.11 · Chur — proxy profile/prefs + self-service routes (~2 ч)

### `bff/chur/src/routes/admin.ts` — добавить маршруты

```typescript
// ── Profile ───────────────────────────────────────
// Читать профиль — local-admin+
app.get('/admin/tenants/:tenantId/users/:userId/profile', {
  preHandler: [authenticate, requireScope('aida:tenant:admin'), requireSameTenant()]
}, proxy);

// Обновить профиль — local-admin+
app.put('/admin/tenants/:tenantId/users/:userId/profile', {
  preHandler: [authenticate, requireScope('aida:tenant:admin'), requireSameTenant()]
}, proxy);

// ── Preferences ───────────────────────────────────
// Admin смотрит чужие prefs
app.get('/admin/tenants/:tenantId/users/:userId/prefs', {
  preHandler: [authenticate, requireScope('aida:tenant:admin'), requireSameTenant()]
}, proxy);

app.put('/admin/tenants/:tenantId/users/:userId/prefs', {
  preHandler: [authenticate, requireScope('aida:tenant:admin'), requireSameTenant()]
}, proxy);

// Self-service — любой авторизованный пользователь
app.get('/admin/me/prefs',  { preHandler: [authenticate] }, proxy);
app.put('/admin/me/prefs',  { preHandler: [authenticate] }, proxy);
app.get('/admin/me/profile',{ preHandler: [authenticate] }, proxy);
app.put('/admin/me/profile',{ preHandler: [authenticate] }, proxy);
```

### Definition of Done R4.11
```bash
# Viewer может читать свои prefs (self-service)
curl -H "Cookie: sid=viewer-sid" GET /admin/me/prefs  # 200

# Viewer не может читать чужие prefs (admin route)
curl -H "Cookie: sid=viewer-sid" \
  GET /admin/tenants/default/users/other-id/prefs     # 403
```

---

## R4.12 · UserModal «Настройки» — wire API (~2 ч)

### `src/pages/UsersPage.tsx` — загрузка profile + prefs

```typescript
// В openEdit — параллельная загрузка profile и prefs:
const [profileRes, prefsRes] = await Promise.all([
  api.get(`/admin/tenants/${tenantId}/users/${u.id}/profile`),
  api.get(`/admin/tenants/${tenantId}/users/${u.id}/prefs`),
]);

// Заполнить поля вкладки Профиль:
setField('e-title', profileRes.title);
setField('e-dept',  profileRes.dept);
setField('e-phone', profileRes.phone);

// Заполнить вкладку Настройки (уже реализовано через setVal/setTog):
populatePrefs(prefsRes);
```

### В saveUser — сохранение patch

```typescript
// PUT profile (если изменились поля)
await api.put(`/admin/tenants/${tenantId}/users/${u.id}/profile`, {
  title: getValue('e-title'),
  dept:  getValue('e-dept'),
  phone: getValue('e-phone'),
});

// PUT prefs
await api.put(`/admin/tenants/${tenantId}/users/${u.id}/prefs`, collectPrefs());
```

### `collectPrefs()` helper

```typescript
function collectPrefs(): UserPreferences {
  return {
    lang:          getSelectValue('e-lang'),
    theme:         getSelectValue('e-theme'),
    tz:            getSelectValue('e-tz'),
    dateFmt:       getSelectValue('e-datefmt'),
    density:       getSelectValue('e-density'),
    startPage:     getSelectValue('e-startpage'),
    avatarColor:   document.querySelector('#avatar-swatches .selected')?.dataset.color ?? '#A8B860',
    notifyEmail:   isToggleOn('tog-email'),
    notifyBrowser: isToggleOn('tog-browser'),
    notifyHarvest: isToggleOn('tog-harvest'),
    notifyErrors:  isToggleOn('tog-errors'),
    notifyDigest:  isToggleOn('tog-digest'),
  };
}
```

### Definition of Done R4.12
```
- Открыть карточку пользователя — вкладка Профиль показывает title/dept/phone из Keycloak
- Вкладка Настройки показывает сохранённые prefs
- Сохранить → GET prefs снова → значения совпадают
- Network tab: 2 параллельных запроса GET profile + GET prefs при открытии модала
```

---

## R4.13 · Apply prefs on login — usePrefsStore (~2 ч)

При успешном логине читаем prefs текущего пользователя (`/admin/me/prefs`) и применяем к UI.

### `src/stores/prefsStore.ts`

```typescript
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface PrefsState {
  lang:      string;
  theme:     'dark' | 'light' | 'auto';
  density:   'compact' | 'normal' | 'comfortable';
  startPage: string;
  avatarColor: string;
  loaded:    boolean;
  load: () => Promise<void>;
  apply: () => void;
}

export const usePrefsStore = create<PrefsState>()(
  persist((set, get) => ({
    lang: 'ru', theme: 'dark', density: 'normal',
    startPage: 'dashboard', avatarColor: '#A8B860', loaded: false,

    load: async () => {
      const prefs = await fetch('/admin/me/prefs').then(r => r.json());
      set({ ...prefs, loaded: true });
      get().apply();
    },

    apply: () => {
      const { theme, density, lang } = get();
      // Тема
      document.documentElement.setAttribute('data-theme',
        theme === 'auto'
          ? (window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light')
          : theme);
      // Плотность
      document.documentElement.setAttribute('data-density', density);
      // Язык — передать в i18n
      import('../i18n').then(({ default: i18n }) => i18n.changeLanguage(lang));
    },
  }), { name: 'aida-prefs' })
);
```

### Вызов при логине

```typescript
// src/App.tsx — после успешной аутентификации
useEffect(() => {
  if (user) usePrefsStore.getState().load();
}, [user]);
```

### `data-density` CSS переменные

```css
/* src/index.css */
[data-density="compact"]     { --row-py: 6px;  --card-p: 12px; }
[data-density="normal"]      { --row-py: 10px; --card-p: 16px; }
[data-density="comfortable"] { --row-py: 14px; --card-p: 22px; }
```

### Стартовая страница

```typescript
// src/App.tsx — после load()
const { startPage, loaded } = usePrefsStore();
if (loaded && location.pathname === '/') {
  navigate(`/${startPage}`);
}
```

### Definition of Done R4.13
```
- Войти как пользователь с theme=light → UI переключается в светлую тему
- Войти с density=compact → строки таблицы сжимаются
- Войти с startPage=events → редирект на /events после логина
- Обновить страницу → настройки сохранены (zustand persist)
- Сменить prefs через admin → при следующем логине применяются новые значения
```

---

---

## R4.14 · Verdandi prefs sync — appearance + graph → Keycloak (~3 ч)

Сейчас все настройки Verdandi (тема, палитра, шрифты, graph prefs) живут только в
`localStorage` под ключами `seer-*`. При смене браузера или устройства они теряются.
Задача — синхронизировать их с Keycloak user attributes через уже реализованный
`/admin/me/prefs` endpoint (R4.10/R4.11).

### Ключи localStorage → атрибуты Keycloak

| localStorage key | Keycloak attribute | Источник |
|---|---|---|
| `seer-theme` | `prefs.theme` | `themeSlice.ts` |
| `seer-palette` | `verdandi.palette` | `themeSlice.ts` |
| `seer-ui-font` | `verdandi.uiFont` | `ProfileTabAppearance.tsx` |
| `seer-mono-font` | `verdandi.monoFont` | `ProfileTabAppearance.tsx` |
| `seer-font-size` | `verdandi.fontSize` | `ProfileTabAppearance.tsx` |
| `seer-density` | `prefs.density` | `ProfileTabAppearance.tsx` (уже в R4.10) |
| `seer-graph-prefs` | `verdandi.graphPrefs` | `ProfileTabGraph.tsx` (JSON string) |

> `prefs.theme` и `prefs.density` уже описаны в R4.10 — переиспользуются.

### Расширить `UserPreferencesDto` (Backend, R4.10)

```java
public record UserPreferencesDto(
    // ... поля из R4.10 ...
    // Verdandi-specific:
    String verdandiPalette,    // "amber-forest" | "slate" | "lichen" | "juniper" | "warm-dark"
    String verdandiUiFont,     // "Manrope" | "DM Sans" | "Inter" | "IBM Plex Sans" | "Geist" | ...
    String verdandiMonoFont,   // "IBM Plex Mono" | "Fira Code" | "JetBrains Mono" | ...
    String verdandiFontSize,   // "13" (px as string)
    String verdandiGraphPrefs  // JSON string: {"autoLayout":true,"drillAnimation":true,...}
) { ... }
```

**Keycloak attributes:**
```
verdandi.palette      = "amber-forest"
verdandi.uiFont       = "Manrope"
verdandi.monoFont     = "IBM Plex Mono"
verdandi.fontSize     = "13"
verdandi.graphPrefs   = "{\"autoLayout\":true,\"drillAnimation\":true,\"hoverHighlight\":true,\"showEdgeLabels\":false,\"colLevelDefault\":false,\"startLevel\":\"L2\",\"nodeLimit\":\"400\"}"
```

### `usePrefsSync.ts` — новый хук, Verdandi

```typescript
// frontends/verdandi/src/hooks/usePrefsSync.ts

/**
 * Синхронизирует localStorage seer-* настройки с /admin/me/prefs.
 * Вызывается один раз при монтировании (restore on login) и
 * подписывается на изменения настроек (debounce 1500ms save).
 */
export function usePrefsSync() {
  // ── RESTORE on login ──────────────────────────────────
  useEffect(() => {
    fetch('/auth/me/prefs')            // проксируется через Chur nginx
      .then(r => r.ok ? r.json() : null)
      .then(prefs => {
        if (!prefs) return;
        // Применить к localStorage и живому DOM
        if (prefs.theme)               applyTheme(prefs.theme);
        if (prefs.verdandiPalette)     applyPalette(prefs.verdandiPalette);
        if (prefs.verdandiUiFont)      applyUiFont(prefs.verdandiUiFont);
        if (prefs.verdandiMonoFont)    applyMonoFont(prefs.verdandiMonoFont);
        if (prefs.verdandiFontSize)    applyFontSize(prefs.verdandiFontSize);
        if (prefs.density)             applyDensity(prefs.density);
        if (prefs.verdandiGraphPrefs)  restoreGraphPrefs(prefs.verdandiGraphPrefs);
      });
  }, []);

  // ── SAVE on change (debounced) ────────────────────────
  // Подписка на storage-события от ProfileTabAppearance / ProfileTabGraph
  useEffect(() => {
    const handler = debounce(() => {
      const payload = buildPrefsPayload();  // читает текущие seer-* из localStorage
      fetch('/auth/me/prefs', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });
    }, 1500);

    window.addEventListener('storage', handler);
    return () => window.removeEventListener('storage', handler);
  }, []);
}

function buildPrefsPayload() {
  return {
    theme:              localStorage.getItem('seer-theme') ?? 'dark',
    density:            localStorage.getItem('seer-density') ?? 'compact',
    verdandiPalette:    localStorage.getItem('seer-palette') ?? 'amber-forest',
    verdandiUiFont:     localStorage.getItem('seer-ui-font') ?? 'Manrope',
    verdandiMonoFont:   localStorage.getItem('seer-mono-font') ?? 'IBM Plex Mono',
    verdandiFontSize:   localStorage.getItem('seer-font-size') ?? '13',
    verdandiGraphPrefs: localStorage.getItem('seer-graph-prefs') ?? null,
  };
}
```

### Интеграция в Verdandi App

```typescript
// frontends/verdandi/src/App.tsx
import { usePrefsSync } from './hooks/usePrefsSync';

function App() {
  const { isAuthenticated } = useAuthStore();
  usePrefsSync();   // ← добавить здесь (no-op если не авторизован)
  // ...
}
```

### Nginx proxy в Verdandi (если нет)

Verdandi пингует `/auth/me/prefs` — это роутится через Chur (уже есть прокси `/auth/`).
Chur добавляет `X-Seer-User-Id` header и форвардит в HEIMDALL backend `/admin/me/prefs`.

```nginx
# frontends/verdandi/nginx.conf (если отдельный)
location /auth/ {
    proxy_pass http://chur:3000;
    proxy_set_header Host $host;
}
```

### ProfileTabAppearance + ProfileTabGraph — fire storage event

Текущий код пишет в localStorage напрямую без `StorageEvent`. Нужно добавить:

```typescript
// После записи в localStorage:
localStorage.setItem('seer-palette', selected);
window.dispatchEvent(new StorageEvent('storage', {
  key: 'seer-palette', newValue: selected
}));
```

Это триггерит debounced save в `usePrefsSync`.

### Definition of Done R4.14
```
- Войти в Verdandi на браузере A, выбрать палитру "slate" и граф-пресет autoLayout=false
- Открыть Verdandi на браузере B (другой браузер / инкогнито)
- → palette = "slate", autoLayout = false применились без ручной настройки

- Network: при смене палитры — PUT /auth/me/prefs через ~1500ms debounce
- При логине — GET /auth/me/prefs до первого рендера (no flash of default theme)

- Keycloak Admin: пользователь → Attributes:
    verdandi.palette = "slate"
    verdandi.graphPrefs = "{\"autoLayout\":false,...}"
```

---

## Definition of Done Sprint 4

```bash
# Keycloak scopes
curl -X POST .../token -d "username=editor&password=..." \
  | jq .access_token | jwt decode | grep scope
# → "seer:read seer:write"

# Chur enforcement
curl -H "Cookie: sid=viewer-sid" POST /heimdall/control/reset  # 403
curl -H "Cookie: sid=admin-sid"  POST /heimdall/control/reset  # 200

# Backend AdminResource
curl -H "X-Seer-Scopes: aida:tenant:admin" \
     -H "X-Seer-Tenant: default" \
     GET /admin/tenants/default/users  # 200 + user list

# Frontend
# - Users tab видна admin/local-admin, скрыта viewer/editor
# - Invite modal открывается, создаёт пользователя в Keycloak
# - Edit modal: вкладки Profile / Permissions / Quotas / Настройки работают
# - Scopes checklist: viewer видит только assignable scopes
# - Поля title/dept/phone сохраняются в Keycloak attributes
# - Prefs (тема, язык, плотность) применяются сразу после логина

# Tests
./gradlew :services:heimdall-backend:test  # GREEN
npm test --workspace=bff/chur              # GREEN (rbac.test.ts)
npm test --workspace=frontends/heimdall-frontend  # GREEN
```

---

## Открытые вопросы (не блокируют Sprint 4)

| # | Вопрос | Когда |
|---|---|---|
| Q-MT1 | YGG: shared vs per-tenant database | post-HighLoad |
| Q-MT2 | FRIGG: shared vs per-tenant database | post-HighLoad |
| Q-MT3 | Keycloak Organizations preview stability | post-HighLoad |
| Q-MT4 | Billing integration | post-HighLoad |
| Q-MT5 | HeimdallEvent schema v2 + tenantId во всех эмиттерах | post-HighLoad |

---

## История изменений

| Дата | Версия | Что |
|---|---|---|
| 13.04.2026 | 1.0 | Initial. Phase 1 single-tenant. R4.1 Keycloak scopes, R4.2 Chur middleware, R4.3 TenantContext + AdminResource, R4.4 KeycloakAdminClient, R4.5–R4.7 Frontend, R4.8 Tests. |
| 13.04.2026 | 1.1 | Добавлены R4.9–R4.13: UserProfile attributes (title/dept/phone), UserPreferences (lang/theme/tz/density/startPage/avatarColor/notifications), Chur self-service /me/ routes, UserModal wire API, usePrefsStore apply on login. Итого +11 ч → 34 ч. |
| 13.04.2026 | 1.3 | R4.5 переписана: полная замена заглушки, структура страницы из прототипа (+1 ч → 4 ч). R4.6 переписана: modal 820×580 sidebar-nav, 6 секций вместо 3 вкладок (+2 ч → 6 ч). Итого → 39 ч. |
| 13.04.2026 | 1.2 | Добавлена R4.14: Verdandi prefs sync — palette, uiFont, monoFont, fontSize, graphPrefs (autoLayout/drillAnimation/hoverHighlight/showEdgeLabels/colLevelDefault/startLevel/nodeLimit) → Keycloak user attributes + restore on login через usePrefsSync hook. +3 ч → 37 ч. |
