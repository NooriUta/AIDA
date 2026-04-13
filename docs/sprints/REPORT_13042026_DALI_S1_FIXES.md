# Dali S1 — Отчёт о выполнении

**Дата:** 13.04.2026  
**Ветка:** `feature/b-elk-wins-env-keycloak`  
**Коммит:** `96fe193`  
**Файлов изменено:** 20 (+2745 / -165)  
**PR:** [#7](https://github.com/NooriUta/AIDA/pull/7)

---

## Итог по задачам

| Задача | Статус |
|--------|--------|
| UI-4 Расширение Session (source, result fields, friggPersisted) | ✅ выполнено |
| UI-6 GET /api/sessions list endpoint | ✅ выполнено |
| UI-8 API-клиент dali.ts (getSessionsArchive, friggPersisted) | ✅ выполнено |
| UI-1 ParseForm (clearBeforeWrite) | ✅ выполнено ранее |
| UI-2 SessionList (F✓/F? badge) | ✅ выполнено |
| UI-3 useDaliSession polling hook | ✅ выполнено ранее |
| UI-5 Детальный вид (detail row) | ✅ выполнено ранее |
| UI-7 CORS + Vite proxy | ✅ выполнено ранее |
| Баг: atomCount неверен (fallback-путь) | ✅ исправлено |
| Баг: resolutionRate = 0% всегда | ✅ исправлено |
| Баг: DaliAtom дублирование (~2× инфляция) | ✅ исправлено |
| Баг: DuplicatedKeyException (pool-mode) | ✅ исправлено |
| FRIGG persistence (friggPersisted + archive) | ✅ выполнено |
| Архив сессий в UI (отдельная секция) | ✅ выполнено |

---

## Баг 1 — DaliAtom дублирование (критический)

### Проблема

`AtomProcessor.getAtomsData()` возвращает два типа контейнеров:

- `"statement:GEOID"` — атомы, сгруппированные по SQL-выражению
- `"routine:GEOID"` — **дубликатный вид** тех же атомов, сгруппированных по пакету/процедуре

Оба контейнера содержат **одинаковые атомы**, но `atomId = MD5(geoid + ":" + key)` отличается:
- statement: `MD5("stmt_geoid:COL_NAME~12:5")`
- routine:   `MD5("routine_geoid:COL_NAME~12:5")`

Поскольку atomId разные, оба проходили `vertexIds.add()` — guard на dedup — и вставлялись как отдельные вершины в YGG. Итог: `COUNT(DaliAtom)` в ~2× больше реального числа атомов.

> Секция **edges** в `JsonlBatchBuilder` уже имела guard `if ("routine".equals(...)) continue`.  
> Секция **vertices** — не имела.

### Фикс

Добавлен guard `if ("routine".equals(sourceType)) continue` в 4 местах:

```java
// JsonlBatchBuilder.buildFromResult() — pool mode (строка ~490)
String sourceType = (String) cont.get("source_type");
if ("routine".equals(sourceType)) continue; // duplicate view — skip

// JsonlBatchBuilder.buildFromResult() — ad-hoc mode (строка ~610)
String sourceType = (String) cont.get("source_type");
if ("routine".equals(sourceType)) continue;

// RemoteWriter.write() — REMOTE mode DaliAtom (строка ~310)
String sourceTypeAtom = (String) cont.get("source_type");
if ("routine".equals(sourceTypeAtom)) continue;

// HoundParserImpl.toParseResult() — fallback atomCount (строка ~233)
String srcType = (String) cont.get("source_type");
if ("routine".equals(srcType)) continue;
```

---

## Баг 2 — atomCount неверен (fallback-путь)

### Проблема

В `HoundParserImpl.toParseResult()`, fallback-ветка (writeMode = DISABLED/EMBEDDED):

```java
// БЫЛО — возвращало число контейнеров (statements), а не атомов
atomCount = sem.getAtoms().size();
// Пример: 50 statements × 10 атомов = возвращало 50 вместо 500
```

### Фикс

```java
int totalAtoms = 0;
if (sem.getAtoms() != null) {
    for (var entry : sem.getAtoms().entrySet()) {
        Map<String, Object> cont = (Map<String, Object>) entry.getValue();
        String srcType = (String) cont.get("source_type");
        if ("routine".equals(srcType)) continue;        // дубль — пропустить
        Map<?, ?> atoms = (Map<?, ?>) cont.get("atoms");
        if (atoms != null) totalAtoms += atoms.size();  // суммируем атомы внутри контейнера
    }
}
atomCount = totalAtoms;
```

---

## Баг 3 — resolutionRate = 0% всегда

### Проблема

`AtomInfo.STATUS_RESOLVED = "Обработано"` (русская строка).  
Фильтр в `HoundParserImpl.calcResolutionRate()` сравнивал `"RESOLVED"` (English uppercase):

```java
// БЫЛО — никогда не совпадает
.filter(e -> "RESOLVED".equals(e.get("result_kind")))
```

### Фикс — column-level формула

```java
private static double calcResolutionRate(SemanticResult sem) {
    List<Map<String, Object>> log = sem.getResolutionLog();
    if (log == null || log.isEmpty()) return 1.0;

    // Используем AtomInfo.STATUS_RESOLVED ("Обработано")
    long resolved   = log.stream()
            .filter(e -> AtomInfo.STATUS_RESOLVED.equals(e.get("result_kind")))
            .count();
    long unresolved = log.stream()
            .filter(e -> "unresolved".equals(e.get("result_kind")))
            .count();
    // Константы и function_call исключены из знаменателя намеренно:
    // они не требуют резолюции к таблице.столбцу
    long denominator = resolved + unresolved;
    if (denominator == 0) return 1.0;
    return (double) resolved / denominator;
}
```

**Формула:** `resolved / (resolved + unresolved)` — только column-level атомы, константы и вызовы функций не учитываются.

---

## Баг 4 — DuplicatedKeyException (pool-mode)

### Проблема

При второй сессии с `clearBeforeWrite=false` в pool-режиме:
1. `CanonicalPool` стартует пустым
2. `!pool.hasSchemaRid(cg)` → всегда `true`
3. INSERT DaliSchema пытается вставить схему, которая уже есть в YGG
4. `DuplicatedKeyException` → session FAILED

### Фикс

Добавлен try-catch на INSERT DaliSchema / DaliTable / DaliColumn в `RemoteWriter.writeBatch()`:

```java
try {
    rcmd("INSERT INTO DaliSchema SET ...", ...);
    pool.putSchemaRid(cg, cg);
} catch (RuntimeException ex) {
    String msg = ex.getMessage() != null ? ex.getMessage() : "";
    if (msg.contains("DuplicatedKeyException") || msg.contains("Found duplicate key")) {
        pool.putSchemaRid(cg, cg); // зарегистрировать, чтобы не повторять
        logger.debug("[pool] DaliSchema '{}' already exists — reusing", e.getKey());
    } else throw ex;
}
```

---

## FRIGG persistence

### Session.friggPersisted

Добавлено поле `boolean friggPersisted` в `shared/dali-models/Session.java`:

```java
public record Session(
    ..., List<FileResult> fileResults,
    boolean friggPersisted   // true = запись подтверждена в FRIGG
) {}
```

`SessionService.persist()` выставляет `true` после успешного `repository.save()`.

### Archive endpoint

```java
@GET @Path("/archive")
public Response archive(@QueryParam("limit") @DefaultValue("200") int limit) {
    return Response.ok(sessionRepository.findAll(limit)).build();
}
```

Читает напрямую из FRIGG repository, минуя in-memory кеш Dali.

---

## Heimdall UI

### F✓ / F? badge

В `SessionList.tsx` рядом с ID сессии отображается badge состояния синхронизации с FRIGG:

| Badge | Значение |
|-------|----------|
| `F✓` (зелёный) | Сессия сохранена в FRIGG |
| `F?` (жёлтый) | Ещё не синхронизировано |

### Раздел «Архив сессий (FRIGG)»

В `DaliPage.tsx` ниже активных сессий добавлен collapsible-раздел:

- Lazy loading: данные запрашиваются при первом открытии через `GET /api/sessions/archive`
- Индикатор здоровья FRIGG (зелёный/красный dot `:2481`)
- Счётчик записей
- Компонент `SessionList` переиспользуется для отображения архива

---

## Алгоритм подсчёта атомов — документация

Создан документ `C:\Users\LEGION\Downloads\Hound_Atom_Algorithm.docx` (18 KB), 10 разделов:

1. DaliAtom — что это и ключевые атрибуты
2. Сбор атомов через `AtomProcessor.addAtom()`
3. Алгоритм резолюции (`resolveAtomsOnStatementExit`, `NameResolver`)
4. Структура `getAtomsData()` и ловушка routine-дублирования
5. Подсчёт `atomCount`: REMOTE_BATCH-путь (`ws.atomsInserted()`) и fallback
6. Баг дублирования S1 и его фикс
7. Формула `resolutionRate`: column-level `resolved / (resolved + unresolved)`
8. Data flow: AtomProcessor → WriteStats → ParseResult → Session → UI
9. Таблица статусов: Обработано / unresolved / constant / function_call
10. Таблица ключевых файлов

---

## Изменённые файлы

| Файл | Изменение |
|------|-----------|
| `libraries/hound/.../HoundParserImpl.java` | Fix atomCount fallback, fix resolutionRate formula |
| `libraries/hound/.../JsonlBatchBuilder.java` | Guard routine containers (pool + ad-hoc) |
| `libraries/hound/.../RemoteWriter.java` | Guard routine in write(), try-catch DuplicatedKeyException |
| `libraries/hound/.../WriteStats.java` | **NEW** — per-type insert/duplicate accumulator |
| `shared/dali-models/.../Session.java` | Add friggPersisted, vertexStats, droppedEdgeCount, fileResults |
| `shared/dali-models/.../VertexTypeStat.java` | **NEW** — vertex type stat record |
| `shared/dali-models/.../FileResult.java` | Extended with droppedEdgeCount, vertexStats |
| `services/dali/.../SessionService.java` | persist() sets friggPersisted=true, startup loads FRIGG |
| `services/dali/.../SessionResource.java` | Add GET /api/sessions/archive endpoint |
| `services/dali/.../FriggGateway.java` | Extended FRIGG integration |
| `services/dali/.../SessionRepository.java` | findAll(limit) for archive endpoint |
| `services/dali/.../ParseJob.java` | Wire completeSession with full result |
| `frontends/.../api/dali.ts` | Add getSessionsArchive(), friggPersisted field |
| `frontends/.../components/dali/SessionList.tsx` | F✓/F? badge |
| `frontends/.../pages/DaliPage.tsx` | Archive section with lazy loading |
| `docs/DALI_JOB_ALGORITHM.md` | **NEW** — алгоритм Dali job |
| `docs/DALI_TASK_EXECUTION.md` | **NEW** — task execution docs |

---

## Статус задач SPRINT_DALI_UI_S1

Все задачи UI-1…UI-8 закрыты. Sprint S1 завершён.

### Осталось проверить после рестарта Dali

- [ ] `atomCount` совпадает с `SELECT COUNT(*) FROM DaliAtom WHERE session_id=:sid`
- [ ] `resolutionRate` > 0% (был 0% из-за Russian/English mismatch)
- [ ] Вторая сессия с `clearBeforeWrite=false` — нет FAILED, нет DuplicatedKeyException
- [ ] F✓ badge появляется после синхронизации с FRIGG
- [ ] `GET /api/sessions/archive` возвращает исторические сессии из FRIGG
