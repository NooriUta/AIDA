# HEIMDALL Sprint 4: RBAC & User Management

**Version:** 1.5  
**Date:** 2026-04-21  
**Branch:** `feature/heimdall-s4-rbac` → `develop`  
**Status:** ✅ DONE (R4.1–R4.14 + R4.8 tests)

---

## Итог

| ID | Задача | Слой | Статус | Файл(ы) |
|----|--------|------|--------|---------|
| R4.1 | Keycloak scopes + realm export | Keycloak | ⏳ ручная | KC Admin UI |
| R4.2 | Chur requireScope middleware | Chur | ✅ | `middleware/requireAdmin.ts` |
| R4.3 | TenantContext + backend filter | Backend | ✅ | `tenant/TenantContext.java`, `tenant/TenantContextFilter.java` |
| R4.4 | KeycloakAdminClient | Chur | ✅ | `keycloakAdmin.ts` (getUser, inviteUser, setUserRole, getUserAttributes, setUserAttributes) |
| R4.5 | UsersPage — полная реализация | Frontend | ✅ | `pages/UsersPage.tsx` (уже была) |
| R4.6 | UserModal — sidebar nav, 6 секций | Frontend | ✅ | `components/users/UserEditModal.tsx` (уже была) |
| R4.7 | Navigation guards | Frontend | ✅ | `components/RoleGuard.tsx`, `hooks/useTenantContext.ts`, `App.tsx`, `HeimdallHeader.tsx` |
| R4.8 | Tests | All | ✅ | `admin.test.ts`, `useTenantContext.test.ts`, `RoleGuard.test.tsx`, `prefsStore.test.ts` |
| R4.9 | UserProfile attributes | Chur | ✅ | `routes/admin.ts` profile endpoints + `keycloakAdmin.ts` |
| R4.10 | UserPreferences — KC + API | Chur | ✅ | `routes/admin.ts` prefs endpoints + helpers |
| R4.11 | Chur: proxy profile/prefs + /me/ | Chur | ✅ | `routes/admin.ts` registered in `server.ts` |
| R4.12 | UserModal «Настройки» wire API | Frontend | ✅ | уже была в UserEditModal |
| R4.13 | Apply prefs on login — prefsStore | Frontend | ✅ | `stores/prefsStore.ts`, `App.tsx` SessionGuard |
| R4.14 | Verdandi prefs sync → KC | Verdandi | ✅ | `hooks/usePrefsSync.ts`, `App.tsx` |

---

## Коммиты

| Хэш | Описание |
|-----|----------|
| `20295f7` | feat: admin routes, prefs sync, nav guards (R4.2/3/9/10/11/13/14) |
| `0e7fcca` | test: admin route RBAC, role guard, prefs store (R4.8) |

---

## Архитектурные решения

### Chur-Direct KC (vs. proxy through HEIMDALL backend)

Admin routes в Chur вызывают Keycloak Admin API напрямую через `keycloakAdmin.ts`.
HEIMDALL Java backend создаёт только `TenantContext` + фильтр для чтения
`X-Seer-*` заголовков (нужно для `ControlResource`), но не дублирует user management.

### Phase 1 single-tenant

`requireSameTenant()` в Phase 1 — pass-through (tenant всегда "default").
Phase 2 enforcement: сравнение `session.tenantId` с `:tenantId` из URL params.

### Прежние prefs (FRIGG) vs. новые (KC)

- `GET/PUT /prefs` (FRIGG) — тема/палитра/шрифты (Verdandi-owned, per-device)
- `GET/PUT /admin/me/prefs` (KC) — lang/theme/density/startPage/notify/* + verdandi.*

Оба эндпоинта сосуществуют без конфликта.

---

## DoD (критерии готовности)

- [x] `bff/chur/src/routes/admin.ts` — все 10 маршрутов с RBAC
- [x] `bff/chur/src/keycloakAdmin.ts` — getUser, inviteUser, setUserRole, getUserAttributes, setUserAttributes
- [x] `bff/chur/src/server.ts` — adminRoutes зарегистрированы
- [x] `services/heimdall-backend/.../tenant/TenantContext.java` + `TenantContextFilter.java`
- [x] `frontends/heimdall-frontend/src/components/RoleGuard.tsx`
- [x] `frontends/heimdall-frontend/src/hooks/useTenantContext.ts`
- [x] `frontends/heimdall-frontend/src/stores/prefsStore.ts`
- [x] `App.tsx` — loads prefs on auth, `/users` protected by `RoleGuard require="local-admin"`
- [x] `HeimdallHeader.tsx` — Users nav item hidden for viewer/editor/operator/auditor
- [x] `frontends/verdandi/src/hooks/usePrefsSync.ts` + wire в `App.tsx`
- [x] `tsc --noEmit` — 0 ошибок (Chur, heimdall-frontend, Verdandi)
- [x] Chur: 47 тестов прошли (admin.test.ts: 21, auth.test.ts: 8, rbac.test.ts: 11, graphql.test.ts: 7)
- [x] heimdall-frontend: 65 тестов прошли (+27 новых: useTenantContext, RoleGuard, prefsStore)
- [ ] R4.1 — Keycloak scopes в KC Admin UI: ручной шаг при деплое

---

## Ручные шаги при деплое (R4.1)

```bash
# 1. В KC Admin UI: Clients → aida-bff → Client Scopes → Add
#    Добавить 8 scopes: seer:read, seer:write, aida:harvest, aida:audit,
#    aida:tenant:admin, aida:tenant:owner, aida:admin, aida:superadmin

# 2. Создать realm roles с теми же именами:
#    viewer, editor, operator, auditor, local-admin, tenant-owner, admin, super-admin

# 3. Экспорт realm для версионирования:
curl -H "Authorization: Bearer $ADMIN_TOKEN" \
  http://localhost:8180/admin/realms/seer/partial-export \
  > keycloak/seer-realm.json
```
