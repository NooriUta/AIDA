# AIDA — RBAC & Multi-Tenant Architecture

**Документ:** `RBAC_MULTITENANT`
**Версия:** 1.0
**Дата:** 13.04.2026
**Статус:** PROPOSED — требует подтверждения Q-MT1, Q-MT2, Q-MT3
**Связанные документы:** `MODULES_TECH_STACK.md`, `DECISIONS_LOG.md`, `K8S_MIGRATION_TASKS.md`

---

## 1. Обзор модели

Восемь ролей в трёх тирах.

| Тир | Роли | Keycloak уровень |
|---|---|---|
| **user** | viewer, editor, operator, auditor | Organization roles |
| **tenant** | local admin, tenant owner | Organization roles (elevated) |
| **platform** | admin, super admin | Realm roles |

**Принцип наименьших привилегий:** каждая роль получает минимально необходимый набор scopes. Roles не наследуются автоматически.

---

## 2. Роли и scopes

| Роль | Keycloak уровень | Scopes |
|---|---|---|
| `viewer` | org-role | `seer:read` |
| `editor` | org-role | `seer:read` `seer:write` |
| `operator` | org-role | `seer:read` `aida:harvest` |
| `auditor` | org-role | `seer:read` `aida:audit` |
| `local-admin` | org-role | `seer:read` `seer:write` `aida:harvest` `aida:tenant:admin` |
| `tenant-owner` | org-role | `seer:read` `seer:write` `aida:harvest` `aida:tenant:admin` `aida:tenant:owner` |
| `admin` | realm-role | все выше + `aida:admin` |
| `super-admin` | realm-role | все выше + `aida:superadmin` |

### Описание scopes

| Scope | Назначение | Enforces |
|---|---|---|
| `seer:read` | Чтение lineage данных, LOOM, KNOT, ANVIL results | SHUTTLE GraphQL, YGG |
| `seer:write` | Аннотации, saved views, MIMIR full access | SHUTTLE GraphQL, FRIGG |
| `aida:harvest` | Запуск parse-сессий, просмотр Dali, event stream | Dali, HEIMDALL |
| `aida:audit` | Audit log просмотр + экспорт, tool call trace | HEIMDALL |
| `aida:tenant:admin` | Управление пользователями tenant, quotas | HEIMDALL Admin API, Keycloak Admin API |
| `aida:tenant:owner` | Настройки tenant, billing, назначение local-admin | HEIMDALL Admin API |
| `aida:admin` | Полный доступ к HEIMDALL, Sources, restart сервисов, DEMODEBUG | HEIMDALL, Chur |
| `aida:superadmin` | Все выше + платформенные настройки, cross-tenant, Keycloak realm | HEIMDALL, Chur, Keycloak |

---

## 3. Матрица доступа (обозначения: ✅ полный · ⚡ частичный · — нет)

### 3.1 VERDANDI

| Функция | viewer | editor | operator | auditor | local-admin | tenant-owner | admin | super-admin |
|---|---|---|---|---|---|---|---|---|
| LOOM граф (L1/L2/L3) | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| KNOT Inspector + drill-down | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Сохранять views, аннотации | — | ✅ | ⚡ views | — | ✅ | ✅ | ✅ | ✅ |
| Scope данных (multi-tenant) | свой tenant | свой tenant | свой tenant | свой tenant | свой tenant | свой tenant | все | все |

### 3.2 ANVIL + MIMIR

| Функция | viewer | editor | operator | auditor | local-admin | tenant-owner | admin | super-admin |
|---|---|---|---|---|---|---|---|---|
| Просмотр impact | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Запускать traversal | ⚡ квота | ✅ | ✅ | — | ✅ | ✅ | ✅ | ✅ |
| NL-вопросы MIMIR | 5/ч | 20/ч | 50/ч | — | ✅ | ✅ | ∞ | ∞ |
| Tool call trace | — | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |

### 3.3 Dali

| Функция | viewer | editor | operator | auditor | local-admin | tenant-owner | admin | super-admin |
|---|---|---|---|---|---|---|---|---|
| Просмотр сессий | — | — | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| On-demand harvest | — | — | ✅ | — | ✅ | ✅ | ✅ | ✅ |
| Отменять сессии | — | — | ⚡ свои | — | ✅ | ✅ | ✅ | ✅ |
| Управлять Sources | — | — | — | — | ⚡ просмотр | ✅ | ✅ | ✅ |

### 3.4 HEIMDALL

| Функция | viewer | editor | operator | auditor | local-admin | tenant-owner | admin | super-admin |
|---|---|---|---|---|---|---|---|---|
| Dashboard + метрики | — | — | ✅ | ✅ | ⚡ tenant | ⚡ tenant | ✅ | ✅ |
| Поток событий | — | — | ✅ | ✅ | ⚡ tenant | ⚡ tenant | ✅ | ✅ |
| Restart / Stop сервисов | — | — | — | — | — | — | ✅ | ✅ |
| DEMODEBUG reset/snapshot | — | — | — | — | — | — | ✅ | ✅ |
| Scope событий | — | — | tenant | tenant | tenant | tenant | все | все |

### 3.5 User Management

| Функция | viewer | editor | operator | auditor | local-admin | tenant-owner | admin | super-admin |
|---|---|---|---|---|---|---|---|---|
| Список пользователей tenant | — | — | — | — | ✅ | ✅ | ✅ | ✅ |
| Пригласить / заблокировать | — | — | — | — | ✅ | ✅ | ✅ | ✅ |
| Назначать роли user-тира | — | — | — | — | ✅ | ✅ | ✅ | ✅ |
| Назначить local-admin | — | — | — | — | — | ✅ | ✅ | ✅ |
| Назначить admin | — | — | — | — | — | — | — | ✅ |
| Квоты пользователей | — | — | — | — | ✅ | ✅ | ✅ | ✅ |

---

## 4. Keycloak — реализация

**Версия:** Keycloak 26.2 · **Feature:** Organizations (`features=organization`)
**Статус feature:** Preview в 26.x, stable в 27+ — см. **Q-MT3**

### JWT структура (Organization Membership mapper)

```json
{
  "sub": "user-uuid",
  "preferred_username": "alice",
  "organization": {
    "id": "org-id-1",
    "name": "Acme Corp",
    "alias": "acme-corp",
    "roles": ["editor"]
  },
  "scope": "openid seer:read seer:write"
}
```

### `keycloak/seer-realm.json` обновления

```json
{
  "realm": "seer",
  "organizationsEnabled": true,
  "roles": {
    "realm": [{"name": "aida-admin"}, {"name": "aida-superadmin"}]
  },
  "clientScopes": [
    {"name": "seer:read"},  {"name": "seer:write"},
    {"name": "aida:harvest"}, {"name": "aida:audit"},
    {"name": "aida:tenant:admin"}, {"name": "aida:tenant:owner"},
    {"name": "aida:admin"}, {"name": "aida:superadmin"}
  ]
}
```

---

## 5. Chur (BFF) — реализация

### Session interface

```typescript
export interface AidaSession {
  userId:     string;
  username:   string;
  scopes:     string[];
  tenantId:   string | null;   // organization.id из JWT
  tenantName: string | null;
  tenantRole: string | null;   // organization.roles[0]
  realmRoles: string[];
}
```

### Middleware

```typescript
export function requireScope(...scopes: string[]) { ... }
export function requireSameTenant() { ... }  // блокирует cross-tenant (кроме superadmin)
```

### Trusted headers к backend

```
X-Seer-User-Id:     <userId>
X-Seer-Scopes:      <scope1 scope2 ...>
X-Seer-Tenant:      <tenantId>
X-Seer-Tenant-Role: <tenantRole>
```

---

## 6. HEIMDALL backend — реализация

### TenantContext CDI bean

```java
@RequestScoped
public class TenantContext {
    private String tenantId;
    private List<String> scopes;

    public boolean isSuperAdmin()    { return scopes.contains("aida:superadmin"); }
    public boolean isAdmin()         { return scopes.contains("aida:admin"); }
    public boolean isTenantOwner()   { return scopes.contains("aida:tenant:owner"); }
    public boolean isLocalAdmin()    { return scopes.contains("aida:tenant:admin"); }
    public boolean hasScope(String s){ return scopes.contains(s); }
}
// TenantContextFilter заполняет из X-Seer-* headers (trusted, от Chur)
```

### HeimdallEvent schema v2

```java
public record HeimdallEvent(
    long       timestamp,
    String     sourceComponent,
    String     eventType,
    EventLevel level,
    String     sessionId,
    String     userId,
    String     tenantId,        // ← NEW — breaking change для всех эмиттеров
    String     correlationId,
    long       durationMs,
    Map<String, Object> payload
) {}
```

> ⚠️ Breaking change — координировать с Dali, SHUTTLE, MIMIR, ANVIL. См. Q-MT5.

### AdminResource endpoints

```
GET    /admin/tenants                      → список tenants (admin+)
POST   /admin/tenants                      → создать (superadmin)
GET    /admin/tenants/{id}/users           → пользователи tenant
POST   /admin/tenants/{id}/users/invite    → пригласить
PUT    /admin/tenants/{id}/users/{uid}/role → назначить роль
GET    /admin/tenants/{id}/usage           → usage stats
```

Проксируют в Keycloak Admin API через `KeycloakAdminClient` (Quarkus REST client).

---

## 7. HEIMDALL frontend

### useTenantContext hook

```typescript
export function useTenantContext() {
  const { session } = useAuthStore();
  return {
    tenantId:      session.organization?.id ?? null,
    tenantName:    session.organization?.name ?? null,
    tenantRole:    session.organization?.roles?.[0] ?? null,
    scopes:        session.scopes ?? [],
    isSuperAdmin:  session.scopes.includes('aida:superadmin'),
    isAdmin:       session.scopes.includes('aida:admin'),
    isLocalAdmin:  session.scopes.includes('aida:tenant:admin'),
    isTenantOwner: session.scopes.includes('aida:tenant:owner'),
  };
}
```

### Navigation guards

```typescript
// "Tenants" — только admin+
{(isAdmin || isSuperAdmin) && <NavTab to="/tenants">Tenants</NavTab>}

// "Пользователи" — local-admin+
{(isLocalAdmin || isTenantOwner || isAdmin || isSuperAdmin) &&
  <NavTab to="/users">Пользователи</NavTab>}
```

---

## 8. Изоляция данных (multi-tenant)

### YGG (lineage данные)

| Вариант | Описание | Статус |
|---|---|---|
| **A** | Один database, `tenant_id` на каждой вершине/ребре | Проще в ops, риск утечки через Cypher без WHERE |
| **B** | Отдельный database per tenant в ArcadeDB | Полная изоляция, N баз, ArcadeDB multi-database поддерживает |

**Рекомендация:** Вариант B для production. Вариант A допустим для single-tenant demo.
**Решение:** Q-MT1 — открыт.

### FRIGG (state, views, preferences) — аналогично YGG. Q-MT2 — открыт.

---

## 9. Открытые вопросы

| # | Вопрос | Влияет на | Срок |
|---|---|---|---|
| **Q-MT1** | YGG: shared database + tenant_id vs per-tenant database | Архитектура данных, SHUTTLE queries, Hound config | до start multi-tenant impl |
| **Q-MT2** | FRIGG: аналогично | FRIGG schema, JobRunr scope | до start multi-tenant impl |
| **Q-MT3** | Keycloak Organizations 26.2 — preview. Fallback: Groups API | Auth flow, JWT claims | до start Keycloak config |
| **Q-MT4** | Billing: собственная модель в FRIGG vs внешняя (Stripe) | Tenant owner features | post-HighLoad |
| **Q-MT5** | HeimdallEvent schema v2 + tenantId — когда координировать обновление всех эмиттеров | Dali, SHUTTLE, MIMIR, ANVIL | до start multi-tenant impl |

---

## 10. Что НЕ реализуется до HighLoad

- Multi-tenancy как продуктовая функция — post-HighLoad
- Billing / subscription management — post-HighLoad
- SOC2 compliance — commercial post-MVP
- Tenant isolation на уровне YGG (Q-MT1) — post-HighLoad
- Organizations в Keycloak — post-HighLoad (достаточно realm roles + scopes)

**До HighLoad:** только single-tenant. Матрица ролей + scopes реализуется в полном объёме.

---

## 11. Критический путь

```
Phase 1 — Single-tenant (до HighLoad):
  ✅ Keycloak scopes в realm (seer:read, seer:write и т.д.)
  ✅ Chur requireScope middleware (M1 done — role === 'admin')
  ⏳ Chur requireScope полный (по JWT scopes, не role)
  ⏳ HEIMDALL backend AdminResource (Keycloak Admin API proxy)
  ⏳ Users page — управление через HEIMDALL Admin API
  ⏳ useTenantContext + navigation guards

Phase 2 — Multi-tenant (post-HighLoad):
  Q-MT1 → YGG isolation · Q-MT2 → FRIGG isolation
  Q-MT3 → Keycloak Organizations · Q-MT5 → HeimdallEvent v2
  TenantsPage · TenantDetailPage · Billing (Q-MT4)
```

---

## История изменений

| Дата | Версия | Что |
|---|---|---|
| 13.04.2026 | 1.0 | Initial. 8 ролей × 3 тира × 6 функциональных областей. Keycloak Organizations model. Chur + HEIMDALL реализация. Q-MT1..Q-MT5. |
