# HEIMDALL — Sprint 4: RBAC & User Management

**Документ:** `HEIMDALL_SPRINT4_RBAC`
**Версия:** 1.0
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
  → видит список пользователей с ролями
  → приглашает нового пользователя с ролью
  → редактирует роль, scopes, source bindings, квоты
  → блокирует пользователя
  → Chur enforces scope на каждом маршруте
  → HEIMDALL backend проксирует в Keycloak Admin API
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

---

## Задачи

| ID | Задача | Слой | Неделя | Оценка |
|---|---|---|---|---|
| R4.1 | Keycloak scopes + realm export | Keycloak | W6 | 2 ч |
| R4.2 | Chur requireScope middleware | Chur | W6 | 3 ч |
| R4.3 | HEIMDALL backend TenantContext + AdminResource | Backend | W6–W7 | 4 ч |
| R4.4 | KeycloakAdminClient (REST proxy) | Backend | W7 | 3 ч |
| R4.5 | UsersPage — список + invite | Frontend | W7 | 3 ч |
| R4.6 | UserModal — Profile / Permissions / Quotas | Frontend | W7–W8 | 4 ч |
| R4.7 | Navigation guards (scope-based) | Frontend | W8 | 1 ч |
| R4.8 | Tests | All | W8 | 3 ч |

**Итого: ~23 ч**

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

## R4.5 · UsersPage — список + invite (~3 ч)

**`src/pages/UsersPage.tsx`** — расширение существующего скелета:

```typescript
export function UsersPage() {
  const { isAdmin, isSuperAdmin, isLocalAdmin, isTenantOwner, tenantId } =
    useTenantContext();

  const { data: users, refetch } = useQuery({
    queryKey: ['users', tenantId],
    queryFn: () => api.get(`/admin/tenants/${tenantId ?? 'default'}/users`),
  });

  return (
    <div>
      {/* Header */}
      <div style={{ display:'flex', justifyContent:'space-between' }}>
        <PageTitle>Пользователи</PageTitle>
        {(isLocalAdmin || isTenantOwner || isAdmin) && (
          <Button onClick={() => openInviteModal()}>+ Пригласить</Button>
        )}
      </div>

      {/* Table */}
      <UserTable
        users={users}
        canEdit={isLocalAdmin || isTenantOwner || isAdmin}
        canAssignElevated={isTenantOwner || isAdmin}
        onEdit={openEditModal}
        onDisable={handleDisable}
      />

      {/* Modals */}
      <UserModal ref={modalRef} onSave={refetch} />
    </div>
  );
}
```

---

## R4.6 · UserModal — три вкладки (~4 ч)

Три вкладки: **Профиль** · **Разрешения** · **Квоты**

```typescript
// Вкладка 1: Профиль
// - имя, email (readonly после создания), статус (active/disabled)
// - роль (select с видимыми только доступными ролями)

// Вкладка 2: Разрешения
// - Scopes checklist (только те что может назначить текущий пользователь)
// - Source bindings (checkbox list по источникам из Dali Sources)
// - Примечание: "Scopes дополняют роль"

// Вкладка 3: Квоты
// - MIMIR запросов / час (range slider, 0–100)
// - Параллельных сессий (range slider, 0–10)
// - Max atoms / сессия (range slider)
// - Воркеров из пула (range slider, 1–8)
```

**Ограничения видимости в Permissions tab:**

```typescript
// local-admin не видит и не может назначить elevated scopes
const assignableScopes = isAdmin || isSuperAdmin
  ? ALL_SCOPES
  : ALL_SCOPES.filter(s => !ELEVATED_SCOPES.includes(s));

const ELEVATED_SCOPES = [
  'aida:tenant:owner',
  'aida:admin',
  'aida:superadmin',
];
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
# - Edit modal: вкладки Profile / Permissions / Quotas работают
# - Scopes checklist: viewer видит только assignable scopes

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
