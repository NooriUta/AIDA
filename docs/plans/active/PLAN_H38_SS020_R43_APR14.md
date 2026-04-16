# Sprint Plan: H3.8 + BUG-SS-020 + Dali Dashboard + R4.3
**Ветка:** `feature/h38-ss020-r43-apr14`  
**Дата:** 14.04.2026  
**Docs:** `docs/sprints/PLAN_H38_SS020_R43_APR14.md`

---

## Context

Четыре связанных задачи:
1. **H3.8** — HoundHeimdallListener: Hound при парсинге шлёт события в HEIMDALL → дашборд оживает во время demo
2. **BUG-SS-020** — KC realm не назначает `aida:admin` scope роли admin → Users page возвращает 403
3. **Dali metrics on Dashboard** — DaliPage показывает статистику сессий, главный дашборд — нет. Добавить виджет
4. **R4.3** — Users page: quotas и source bindings из Keycloak-атрибутов вместо mock-данных (FRIGG-вариант — M3 backlog)

---

## Step 0: Ветка + docs

```bash
git checkout -b feature/h38-ss020-r43-apr14
```

Сохранить этот документ в `docs/sprints/PLAN_H38_SS020_R43_APR14.md`.

---

## Task 1: H3.8 — HoundHeimdallListener

**Цель:** Hound при парсинге файлов отправляет события в `POST /events` HEIMDALL. Dashboard начинает показывать FILE_PARSING_* и ATOM_EXTRACTED в реальном времени.

### Ключевые файлы

| Роль | Путь |
|------|------|
| Существующий интерфейс | `libraries/hound/src/main/java/com/hound/api/HoundEventListener.java` |
| Эталонная реализация | `services/dali/src/main/java/studio/seer/dali/job/DaliHoundListener.java` |
| Эталонный HTTP-клиент | `services/dali/src/main/java/studio/seer/dali/heimdall/HeimdallEmitter.java` |
| EventType / EventLevel | `shared/dali-models/src/main/java/studio/seer/shared/EventType.java` |
| Parser (точка входа) | `libraries/hound/src/main/java/com/hound/HoundParserImpl.java` |
| HEIMDALL endpoint | `services/heimdall-backend/.../EventResource.java` → `POST /events` |

### Что создать

**`libraries/hound/src/main/java/com/hound/api/HoundHeimdallListener.java`**
- Implements `HoundEventListener`
- Читает URL из `System.getProperty("heimdall.url", "http://localhost:9093")`
- `java.net.http.HttpClient` — fire-and-forget, никогда не бросает
- Events:
  - `onFileParseStarted` → `FILE_PARSING_STARTED` INFO
  - `onAtomExtracted` → `ATOM_EXTRACTED` INFO, throttle: каждые 100 атомов (счётчик per-file в `ConcurrentHashMap`)
  - `onFileParseCompleted` → `FILE_PARSING_COMPLETED` INFO + `RESOLUTION_COMPLETED` если atomCount > 0
  - `onError` → `FILE_PARSING_FAILED` ERROR
- Payload compact: `file` (basename только, `Paths.get(f).getFileName()`), `atomCount`, `dialect`, `durationMs`, `resolutionRate`
- sourceComponent = `"hound"`

**`libraries/hound/src/main/java/com/hound/api/CompositeListener.java`**
- Chains two `HoundEventListener` — вызывает A затем B, каждый в try-catch

**`HoundParserImpl.java`** — добавить `buildEffectiveListener(HoundEventListener user)`
```java
private HoundEventListener buildEffectiveListener(HoundEventListener user) {
    String url = System.getProperty("heimdall.url");
    if (url == null) return user;
    return new CompositeListener(user, new HoundHeimdallListener(url));
}
```
Вызов в `parse()` и `parseBatch()` — обернуть переданный listener через `buildEffectiveListener`.

### Зависимости Gradle

`libraries/hound/build.gradle` — проверить что `shared:dali-models` уже в dependencies. Если нет — добавить:
```groovy
implementation project(':shared:dali-models')
```

---

## Task 2: BUG-SS-020 — KC scope fix

**Файл:** `infra/keycloak/seer-realm.json`

**Проблема:** `aida:admin` clientScope определён, но не включён в defaultClientScopes и не замаплен на роль `admin`. JWT не содержит claim `aida:admin` → `requireScope('aida:admin')` возвращает 403.

**Фикс:** В `aida-bff` client добавить `aida:admin` в `defaultClientScopes` (рядом с `openid`, `profile`, `email`, `roles`):

```json
"defaultClientScopes": ["openid", "profile", "email", "roles", "aida:admin"]
```

Но это даст `aida:admin` всем пользователям. Правильный вариант — добавить **scope mapper на роль**:

В секцию `protocolMappers` клиента `aida-bff` добавить mapper, который включает `aida:admin` в scope-claim только для пользователей с ролью `admin`:

```json
{
  "name": "aida-admin-scope",
  "protocol": "openid-connect",
  "protocolMapper": "oidc-hardcoded-claim-mapper",
  "consentRequired": false,
  "config": {
    "claim.value": "aida:admin",
    "userinfo.token.claim": "false",
    "id.token.claim": "false",
    "access.token.claim": "true",
    "claim.name": "scope",
    "jsonType.label": "String"
  }
}
```

> **NB:** Keycloak не поддерживает role-conditional hardcoded mappers из коробки через JSON. Самый надёжный путь — в seer-realm.json добавить `aida:admin` в `defaultClientScopes` и оставить fallback-логику в `keycloak.ts` (она уже есть: `deriveAidaScopes(roles)` для admin возвращает `aida:admin`).

**Реальный фикс:** В `bff/chur/src/keycloak.ts` — fallback уже реализован. Проблема в другом: `requireScope` вернёт 403 если `session.scopes` пуст. Проверить что `createSession` вызывается с реальными scopes. Добавить логирование в `requireScope` для диагностики.

**Файлы:**
- `bff/chur/src/middleware/requireAdmin.ts` — добавить `console.warn` при 403 с указанием `sessionScopes`
- `infra/keycloak/seer-realm.json` — добавить `aida:admin` в `optionalClientScopes` → `defaultClientScopes` для `aida-bff` client

---

## Task 3: Dali Stats Widget на Dashboard

**Цель:** Главный дашборд показывает итоговые Dali-метрики (running/completed/failed, total atoms, avg resolution).

### Новый компонент

**`frontends/heimdall-frontend/src/components/DaliStatsWidget.tsx`**
- Polls `DALI_API + '/api/sessions?limit=50'` каждые 10s (через Chur proxy: `HEIMDALL_API + '/dali/sessions?limit=50'` или прямо на DALI_API)
- Считает: `running`, `completed`, `failed`, `totalAtoms`, `avgResolution`
- Отображает: 3-4 карточки в стиле MetricsBar (`bg1`, border, mono font)
- При DALI offline — показывает "—" без ошибки

**Структура карточек:**
| Карточка | Значение |
|----------|---------|
| Sessions (running) | `N running / M total` |
| Atoms parsed | sum atomCount completed |
| Avg resolution | avg resolutionRate% |
| Dali status | online/offline badge |

### Интеграция в DashboardPage

`frontends/heimdall-frontend/src/pages/DashboardPage.tsx` — добавить после `<MetricsBar>`:
```tsx
<DaliStatsWidget />
```

### i18n

`src/i18n/locales/en/common.json` и `ru/common.json`:
```json
"dali": {
  "statsTitle": "Dali Parse Engine",
  "runningSessions": "Running",
  "completedSessions": "Completed",
  "totalAtoms": "Atoms Total",
  "avgResolution": "Avg Resolution"
}
```

### Proxy route (если нужно)

Если Dali API не проксируется через Chur, проверить `bff/chur/src/routes/` — есть ли `/dali/*` прокси. Если нет — виджет обращается напрямую к `DALI_API` (env var `VITE_DALI_API=http://localhost:9090`).

---

## Task 4: R4.3 — Users Page: real quotas из KC атрибутов

**Scope:** Quotas из Keycloak user attributes (FRIGG-хранение — M3 backlog).

**Keycloak атрибуты** уже поддерживаются в `keycloakAdmin.ts`. Добавить маппинг:

```
quota_mimir    → quotas.mimir
quota_sessions → quotas.sessions
quota_atoms    → quotas.atoms
quota_workers  → quotas.workers
quota_anvil    → quotas.anvil
source_bindings → sources[] (split by ',')
```

### Backend: `bff/chur/src/keycloakAdmin.ts`

В `listUsers()` — при маппинге KC user → KcUserView добавить:
```typescript
quotas: {
  mimir:    parseInt(attrs['quota_mimir']?.[0]   ?? '20'),
  sessions: parseInt(attrs['quota_sessions']?.[0] ?? '2'),
  atoms:    parseInt(attrs['quota_atoms']?.[0]    ?? '50000'),
  workers:  parseInt(attrs['quota_workers']?.[0]  ?? '4'),
  anvil:    parseInt(attrs['quota_anvil']?.[0]    ?? '50'),
},
sources: (attrs['source_bindings']?.[0] ?? '').split(',').filter(Boolean),
```

### KC realm: тестовые пользователи

В `infra/keycloak/seer-realm.json` — для пользователей добавить атрибуты:
```json
"attributes": {
  "quota_mimir": ["100"],
  "quota_sessions": ["10"],
  "quota_atoms": ["100000"],
  "quota_workers": ["8"],
  "quota_anvil": ["200"],
  "source_bindings": ["hound,dali"]
}
```

### Frontend: `frontends/heimdall-frontend/src/pages/UsersPage.tsx`

Убрать mock-заглушки из строк ~208-211, читать `user.quotas` и `user.sources` из API-ответа (они теперь реальные).

### `bff/chur/src/routes/heimdall.ts` + типы

Расширить `KcUserView` интерфейс (в `types.ts`) — добавить `quotas` и `sources`.

---

## Порядок выполнения

1. **BUG-SS-020** — быстрый fix, разблокирует ручное тестирование Users page
2. **R4.3** — после fix KC можно видеть реальных пользователей
3. **Dali Dashboard widget** — только frontend, независимо
4. **H3.8** — backend (Hound library), самая долгая

---

## Verification

```bash
# H3.8
./gradlew :libraries:hound:test
# Запустить парсинг → в Heimdall WS должны прийти FILE_PARSING_* + ATOM_EXTRACTED

# BUG-SS-020
# Логин как admin → GET /heimdall/users → 200 (не 403)

# Dali widget
# Открыть Dashboard → виджет показывает running/completed/atoms

# R4.3
# Users page → квоты admin: mimir=100, sessions=10 (не mock 20/2)
```

---

## Critical Files

| Task | Файл |
|------|------|
| H3.8 new | `libraries/hound/src/main/java/com/hound/api/HoundHeimdallListener.java` |
| H3.8 new | `libraries/hound/src/main/java/com/hound/api/CompositeListener.java` |
| H3.8 modify | `libraries/hound/src/main/java/com/hound/HoundParserImpl.java` |
| BUG-SS-020 | `infra/keycloak/seer-realm.json` |
| BUG-SS-020 | `bff/chur/src/middleware/requireAdmin.ts` |
| Dali widget new | `frontends/heimdall-frontend/src/components/DaliStatsWidget.tsx` |
| Dali widget modify | `frontends/heimdall-frontend/src/pages/DashboardPage.tsx` |
| Dali widget modify | `frontends/heimdall-frontend/src/i18n/locales/en/common.json` |
| Dali widget modify | `frontends/heimdall-frontend/src/i18n/locales/ru/common.json` |
| R4.3 modify | `bff/chur/src/keycloakAdmin.ts` |
| R4.3 modify | `bff/chur/src/types.ts` |
| R4.3 modify | `infra/keycloak/seer-realm.json` |
| R4.3 modify | `frontends/heimdall-frontend/src/pages/UsersPage.tsx` |
