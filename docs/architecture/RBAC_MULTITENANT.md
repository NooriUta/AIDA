# AIDA — RBAC & Multi-Tenant Architecture

**Документ:** `RBAC_MULTITENANT`
**Версия:** 1.2
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


---

## 12. Keycloak vs FRIGG — Storage split (зафиксировано в `types.ts`)

### Keycloak (IAM — не дублировать)

| Данные | Хранилище | Обоснование |
|---|---|---|
| Identity, роль, enabled | Keycloak native | IAM |
| Profile: title, dept, phone | Keycloak attr | нужно admin-у в UsersPage |
| `lang` | Keycloak attr `pref.lang` | admin может менять; identity-adjacent |
| tz, dateFmt, startPage, avatarColor | Keycloak attr | сервер-сайд, cross-device |
| notify.* (5 флагов) | Keycloak attr | сервер-сайд |

### FRIGG (`UserPrefs` — источник истины для UI-prefs)

| Данные | Хранилище | Обоснование |
|---|---|---|
| theme, palette, density, uiFont, monoFont, fontSize | FRIGG UserPrefs | cross-device UI sync |
| quotas, sources, activity | FRIGG (R4.3) | операционные данные |

> **localStorage = кэш FRIGG, не хранилище.** Instant read, debounced write (1.5s). Новое устройство → fetchPrefs → FRIGG → всё на месте.

### Поток данных (финальный)

```
Логин
  └─► Chur /auth/login → KC Direct Grant → сессия
      └─► prefsStore.fetchPrefs()
          └─► GET /prefs → Chur → heimdall-backend → FRIGG UserPrefs
              └─► merge localStorage + applyDom ✅

Изменение темы (verdandi)
  └─► themeSlice.toggleTheme()
      ├─► localStorage.setItem('seer-theme', next)  ← мгновенно
      ├─► DOM data-theme = next
      └─► prefsStore.savePrefs({ theme: next })
          └─► debounce 1.5s → PUT /prefs → FRIGG     ← фоново

Новое устройство
  └─► нет localStorage → fetchPrefs → FRIGG → всё на месте ✅
```

- **Идентификация:** `username`, `email`, пароль, 2FA (built-in)
- **Роль:** realm role
- **Профиль:** `title`, `dept`, `phone`
- **UI-настройки** (shared с verdandi через localStorage):
  - `lang` → `seer-lang`
  - `theme` → `seer-theme` (`dark`/`light`, не `auto`)
  - `palette` → `seer-palette`
  - `density` → `compact`/`normal` (не `comfortable`)
  - `uiFont` → `seer-ui-font`
  - `monoFont` → `seer-mono-font`
  - `fontSize` → `seer-font-size`
  - `tz`, `dateFmt`, `startPage`, `avatarColor`
- **Уведомления:** `notify*`

### FRIGG (только HEIMDALL, operational data)

- **Квоты:** `mimir`, `sessions`, `atoms`, `workers`, `anvil`
- **Source bindings:** `sources[]`
- **Activity log**, история сессий, `lastActive`
- **Snapshots** (уже реализовано в ControlResource)

> **Ключевой вывод:** UI-настройки хранятся в Keycloak (не FRIGG) чтобы verdandi и heimdall-frontend видели одни и те же preferences. localStorage keys совпадают с verdandi: `seer-theme`, `seer-palette`, `seer-ui-font`, `seer-mono-font`, `seer-font-size`.

> **TypeScript:** `authUser.role` из `aida-shared` — `'viewer'|'editor'|'admin'` (3 значения legacy). `UserRole` в HEIMDALL — 8 значений. Не кастовать напрямую, использовать `string[]` проверку scopes.


---

## 13. Реализация (Sprint UsersPage + Prefs — Apr 13, 2026)

**Статус:** ✅ DONE · TypeScript все 3 проекта clean

### Keycloak (`seer-realm.json`)
- 8 realm-ролей (было 3)
- 7 пользователей с атрибутами: title, dept, phone, avatarColor, tz, dateFmt, startPage, pref.lang, notify.*
- sub-mapper + username-mapper в protocolMappers

### Chur BFF
- `keycloakAdmin.ts` — Admin REST API client (master realm): `listUsers()`, `updateUserAttrs()`, `setUserEnabled()`
- `routes/prefs.ts` — GET/PUT `/prefs` (proxy → heimdall-backend, sub из сессии)
- `routes/heimdall.ts` — GET `/heimdall/users`, PUT `/heimdall/users/:id/enabled`
- `keycloak.ts` + `plugins/rbac.ts` — ROLE_PRIORITY и ROLE_RANK обновлены до 8 ролей

### HEIMDALL backend
- `UserPrefsRecord.java` — record(sub, theme, palette, density, uiFont, monoFont, fontSize)
- `UserPrefsRepository.java` — FRIGG CRUD. Schema lazy init: `CREATE DOCUMENT TYPE UserPrefs IF NOT EXISTS` + UNIQUE index на sub. ArcadeDB UPSERT: `UPDATE UserPrefs SET ... UPSERT WHERE sub = :sub` — один round-trip
- `UserPrefsResource.java` — GET/PUT `/api/prefs/{sub}`, graceful degradation

### HEIMDALL frontend
- `components/users/types.ts` — UserRole (8), UserPrefs, AidaUser, ROLES, SOURCES
- `UserEditModal.tsx` — 820×580, sidebar nav, 7 секций
- `UsersPage.tsx` — fetch `/heimdall/users` + fallback mock, KcUserView → AidaUser маппинг
- `styles/heimdall.css` — ~50 новых классов

### Verdandi
- `stores/prefsStore.ts` — Zustand: `fetchPrefs()` (login) + `savePrefs(partial)` (debounced 1.5s)
- `stores/slices/themeSlice.ts` — toggleTheme/setPalette → savePrefs
- `stores/authStore.ts` — login()/checkSession() → fetchPrefs

### Беклог (следующие спринты)
| Задача | Sprint |
|---|---|
| FRIGG R4.3: UserQuota, SourceBinding — Java + endpoints | R4.3 |
| Chur R4.12: GET/PUT `/heimdall/users/:id/attrs` — KC атрибуты из UserEditModal | R4.12 |
| FRIGG R4.3: lastActive, activity log | R4.3 |
| Verdandi R4.8: density/fontSize/fonts через savePrefs | R4.8 |
| Тесты prefsStore + UserPrefsRepository | R4.x |

## История изменений

| Дата | Версия | Что |
|---|---|---|
| 13.04.2026 | 1.2 | **UsersPage Sprint DONE.** Final storage split: localStorage=кэш, FRIGG=источник истины UI-prefs, Keycloak=identity+lang+notify. 8 ролей во всех слоях. UserPrefsRepository (lazy schema, UPSERT). prefsStore verdandi (debounced). Поток данных задокументирован. Беклог R4.3/R4.8/R4.12. |
| 13.04.2026 | 1.1 | Keycloak vs FRIGG storage split зафиксирован (из types.ts UsersPage). UI-настройки → Keycloak (shared с verdandi localStorage keys). Operational data → FRIGG. TypeScript: authUser.role = 3 values legacy, UserRole = 8 values. |
| 13.04.2026 | 1.0 | Initial. 8 ролей × 3 тира × 6 функциональных областей. Keycloak Organizations model. Chur + HEIMDALL реализация. Q-MT1..Q-MT5. |
