# Dali UI — Sprint S1 Task Spec

> Дата: 2026-04-13  
> Модуль: `frontends/heimdall-frontend/src/pages/DaliPage.tsx`  
> Бэкенд: `services/dali` (Quarkus, `:9090`)  
> Статус: DaliPage сейчас — заглушка "Coming Soon"

---

## 1. Текущий функционал бэкенда

### REST API (`services/dali`)

#### `POST /api/sessions`

Запускает фоновый парс через JobRunr.

```json
// Request
{ "dialect": "plsql", "source": "/path/to/file.pck", "preview": false }

// Response 202 Accepted
{
  "id":        "550e8400-e29b-41d4-a716-446655440000",
  "status":    "QUEUED",
  "dialect":   "plsql",
  "progress":  0,
  "total":     0,
  "startedAt": "2026-04-13T03:11:18Z",
  "updatedAt": "2026-04-13T03:11:18Z"
}
```

**Валидация:** `dialect` и `source` обязательны → 400 при отсутствии.

#### `GET /api/sessions/{id}`

Polling статуса сессии.

```json
// 200 OK (в процессе)
{ "id": "...", "status": "RUNNING", "progress": 12, "total": 0, ... }

// 200 OK (завершено)
{ "id": "...", "status": "COMPLETED", "progress": 0, "total": 0, ... }

// 404 Not Found (неизвестный id)
{ "error": "session not found" }
```

**Жизненный цикл:** `QUEUED → RUNNING → COMPLETED | FAILED`

> ⚠ `progress` и `total` сейчас всегда 0 — счётчик ещё не реализован в `ParseJob`.
> `ParseResult` содержит `atomCount`, `vertexCount`, `edgeCount`, `durationMs` — но
> они не возвращаются в `Session` (только логируются). Задача UI-S1 → расширить `Session`.

### Модели (`shared/dali-models`)

```java
record Session(
  String id, SessionStatus status,
  int progress, int total, String dialect,
  Instant startedAt, Instant updatedAt
)

record ParseSessionInput(String dialect, String source, boolean preview)

enum SessionStatus { QUEUED, RUNNING, COMPLETED, FAILED, CANCELLED }
```

### Что работает прямо сейчас

| Функция | Статус |
|---|---|
| `POST /api/sessions` → 202 + QUEUED | ✅ |
| `GET /api/sessions/{id}` → статус | ✅ |
| Фоновый парс (JobRunr, 3 retry) | ✅ |
| `HoundParserImpl` in-JVM | ✅ |
| `DaliHoundListener` → SLF4J | ✅ |
| `/q/health` | ✅ |
| `progress / total` в реальном времени | ❌ не реализован |
| Результаты парса в `Session` | ❌ только в логах |
| Список всех сессий `GET /api/sessions` | ❌ нет endpoint |
| Персистенция в FRIGG | ❌ InMemory |
| CORS для фронта | нужно проверить |

---

## 2. Задачи на разработку UI

### Задача UI-1: Форма запуска парса

**Файл:** `DaliPage.tsx`

Форма с тремя полями:

| Поле | Тип | Описание |
|---|---|---|
| `dialect` | select | `plsql` / `postgresql` / `clickhouse` |
| `source` | text | Путь к файлу или директории |
| `preview` | checkbox | Dry-run (без записи в YGG) |

- Кнопка **Parse** → `POST /api/sessions`
- При 400 — показать ошибку под полем
- При 202 — добавить сессию в список (Task UI-2) и начать polling

---

### Задача UI-2: Список сессий (текущей сессии)

**Файл:** `DaliPage.tsx`

Таблица/список активных и завершённых сессий. На старте — только in-memory в React state.

Колонки:

| Колонка | Источник | Примечание |
|---|---|---|
| ID (сокращённый) | `session.id.slice(0, 8)` | с tooltip полного UUID |
| Dialect | `session.dialect` | |
| Source | `session.source` | сокращённый путь |
| Status | `session.status` | цветной badge |
| Progress | `session.progress / session.total` | прогресс-бар (пока 0/0) |
| Duration | `updatedAt - startedAt` | только если COMPLETED |
| Actions | — | кнопка Cancel (будущее) |

---

### Задача UI-3: Polling статуса

**Файл:** `hooks/useDaliSession.ts` (новый)

```ts
function useDaliSession(id: string, enabled: boolean): Session

// Polling каждые 1500ms пока status === QUEUED | RUNNING
// Останавливается при COMPLETED | FAILED
// GET /api/sessions/{id}
```

Связать с UI-2 — при переходе в COMPLETED/FAILED обновить строку в списке.

---

### Задача UI-4: Расширение `Session` на бэкенде (нужно до UI-5)

**Файл:** `shared/dali-models/Session.java`

Добавить поля результата:

```java
record Session(
  String id, SessionStatus status,
  int progress, int total, String dialect,
  Instant startedAt, Instant updatedAt,
  // новые:
  Integer atomCount,    // null пока не COMPLETED
  Integer vertexCount,
  Integer edgeCount,
  Double resolutionRate,
  Long durationMs,
  List<String> warnings,
  List<String> errors
)
```

В `ParseJob.execute()` — передавать `ParseResult` в `SessionService.completeSession(id, result)`.

---

### Задача UI-5: Детальный вид сессии

**Файл:** `DaliPage.tsx` (боковая панель или раскрывающаяся строка)

После COMPLETED показывать `ParseResult`:

```
Atoms:           1 243
Vertices:        418
Edges:           892
Resolution rate: 94.2%
Duration:        1 840ms
Warnings:        2   [раскрыть]
Errors:          0
```

---

### Задача UI-6: `GET /api/sessions` (список на бэкенде)

**Файл:** `services/dali/src/main/java/studio/seer/dali/rest/SessionResource.java`

```java
@GET
public Response list(@QueryParam("limit") @DefaultValue("20") int limit) {
    return Response.ok(sessionService.listRecent(limit)).build();
}
```

Нужно чтобы при перезагрузке страницы история не терялась (пока — только пока Dali не перезапущен, так как InMemory).

---

### Задача UI-7: CORS

**Файл:** `services/dali/src/main/resources/application.properties`

```properties
quarkus.http.cors.enabled=true
quarkus.http.cors.origins=${CORS_ORIGINS:http://localhost:5174,http://localhost:3000}
quarkus.http.cors.methods=GET,POST,OPTIONS
quarkus.http.cors.headers=Content-Type
```

Без этого фронт не сможет напрямую вызывать `:9090` (нужно если без прокси).

---

### Задача UI-8: API-клиент

**Файл:** `frontends/heimdall-frontend/src/api.ts` (дополнить)

```ts
const DALI_API = (import.meta.env.VITE_DALI_URL as string | undefined) ?? 'http://localhost:9090';

export async function postSession(input: ParseSessionInput): Promise<Session>
export async function getSession(id: string): Promise<Session>
export async function getSessions(limit?: number): Promise<Session[]>
```

---

## 3. Порядок реализации

```
UI-7 (CORS)  →  UI-8 (api client)  →  UI-1 (форма)
                                    ↘
                  UI-4 (Session расширение)  →  UI-3 (polling hook)  →  UI-2 (список)  →  UI-5 (детали)
                                                                                         ↘
                                                                                          UI-6 (GET /sessions endpoint)
```

**Минимальный рабочий MVP:** UI-7 + UI-8 + UI-1 + UI-3 + UI-2 (без прогресс-бара и деталей).

---

## 4. Что НЕ входит в S1

- Интеграция с HEIMDALL event stream (`DaliHoundListener` → WebSocket)
- Персистенция сессий в FRIGG (Д10)
- Парсинг директорий (сейчас только один файл)
- Cancel/retry сессии через UI
- Фильтрация/поиск в списке сессий
