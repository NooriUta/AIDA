# Fix Report — 14.04.2026
## ANTLR4 Reclassification · Resolution Breakdown · Event Source Tagging

**Ветка:** `feature/h38-ss020-r43-apr14`  
**Коммит:** `ef248cd`  
**Дата:** 14.04.2026  
**Компоненты:** Hound · Dali · Heimdall UI

---

## Контекст

Данный отчёт покрывает три независимых исправления и одно UI-улучшение, реализованных в продолжение стабилизирующего спринта 13.04.2026. Все изменения идут в ветке `feature/h38-ss020-r43-apr14` перед мержем в `master`.

---

## 1. Hound: исправление классификации ANTLR4-ошибок

### Проблема

`PlSqlErrorCollector` ранее размещал сообщения `"no viable alternative at input '...'"` в список `grammarLimitations` (→ PARSE_WARNING, уровень INFO). Это было ошибочным: данный тип сообщения означает, что ANTLR4-парсер **не смог восстановиться** ни по одной грамматической альтернативе — оператор был полностью пропущен. Реальные примеры из prod-логов:

```
no viable alternative at input 'CREATE INDEX'
no viable alternative at input 'GRANT'
no viable alternative at input 'REVOKE'
no viable alternative at input 'ALTER TABLE'
```

Это **не** ограничение грамматики (grammar limitation) — это признак того, что парсер полностью потерял синтаксический контекст.

### Исправление

Файл: `libraries/hound/src/main/java/com/hound/parser/PlSqlErrorCollector.java`

Удалены все паттерны `"no viable alternative at input '...'"` из `GRAMMAR_LIMITATION_PATTERNS`. Теперь все сообщения этого типа попадают в список `errors` (→ `ParseResult.errors()`, → `PARSE_ERROR` событие, уровень WARN).

**Итоговая классификация:**

| ANTLR4-сообщение | Класс | Уровень |
|---|---|---|
| `token recognition error at: 'X'` | ERROR | WARN |
| `mismatched input '<EOF>' expecting {…}` | ERROR | WARN |
| `no viable alternative at input '<EOF>'` | ERROR | WARN |
| `no viable alternative at input 'X'` | **ERROR** (исправлено) | WARN |
| `extraneous input 'X' expecting {…}` | GRAMMAR (warning) | INFO |
| `missing 'X' at 'Y'` | GRAMMAR (warning) | INFO |
| `expecting {'DEFAULT', 'NOT', 'NULL', ':=', ';'}` | GRAMMAR (warning) | INFO |

### Влияние

- Файлы с `CREATE INDEX`, `GRANT`, `REVOKE`, `ALTER TABLE` и подобными неподдерживаемыми DDL-операторами теперь корректно показывают `success=false` в результатах сессии
- В EventStream HEIMDALL эти события появляются с `eventType=PARSE_ERROR` (красный badge) вместо `PARSE_WARNING` (серый)
- Метрика `resolutionRate` корректнее отражает качество парсинга: файлы с пропущенными операторами не завышают статистику

---

## 2. Dali + Hound: детализация resolution по файлам

### Проблема

Колонка `RATE` в таблице FILES в Heimdall SessionDetail показывала только итоговый процент (например, `87.3%`) — не было понятно, сколько атомов resolved, unresolved, и сколько не подлежат резолюции (константы, function calls).

### Реализация

**Backend — `ParseResult.java` (Hound API)**

```java
// Новые поля добавлены между resolutionRate и warnings
int atomsResolved,    // атомы успешно разрешены (status = "Обработано")
int atomsUnresolved,  // атомы, которые не удалось разрешить (status = "unresolved")
```

**Backend — `HoundParserImpl.java`**

Рефакторинг `calcResolutionRate(SemanticResult)` → `calcResStats(SemanticResult)` возвращающий `ResStats(rate, resolved, unresolved)`:

```java
private record ResStats(double rate, int resolved, int unresolved) {}

private static ResStats calcResStats(SemanticResult sem) {
    List<Map<String, Object>> log = sem.getResolutionLog();
    if (log == null || log.isEmpty()) return new ResStats(1.0, 0, 0);
    int resolved   = (int) log.stream()
            .filter(e -> AtomInfo.STATUS_RESOLVED.equals(e.get("result_kind")))
            .count();
    int unresolved = (int) log.stream()
            .filter(e -> "unresolved".equals(e.get("result_kind")))
            .count();
    int denominator = resolved + unresolved;
    double rate = denominator == 0 ? 1.0 : (double) resolved / denominator;
    return new ResStats(rate, resolved, unresolved);
}
```

**Семантика "pending" атомов:** `pending = atomCount - atomsResolved - atomsUnresolved`. Это атомы типа "константа" или "вызов функции", которые не являются column-reference и не попадают в denominator rate. Например: `SYSDATE`, `NVL(col, 0)`, `'literal'`.

**Backend — `FileResult.java` (shared DTO)**

```java
double               resolutionRate,
int                  atomsResolved,   // ← новое
int                  atomsUnresolved, // ← новое
long                 durationMs,
```

**Backend — `ParseJob.java`**

`toFileResult()` передаёт `r.atomsResolved(), r.atomsUnresolved()`. `merge()` суммирует поля по всем файлам батч-сессии.

**Frontend — `dali.ts`**

```typescript
atomsResolved?: number;    // optional — backward compat с старыми сессиями
atomsUnresolved?: number;
```

**Frontend — `SessionList.tsx`**

Колонка `RESOLUTION` (была `RATE`) теперь показывает:

```
✓ 1 204 (91.5%)    ← зелёный
✗  108 (8.5%)      ← красный (только если > 0)
…  456             ← серый (pending, только если > 0)
```

При `atomCount=0` → `—`. При отсутствии `atomsResolved` (старые данные) → fallback на старый процент.

---

## 3. HeimdallEmitter: корректная атрибуция событий

### Проблема

Все события, генерируемые в контексте парсинга Hound (FILE_PARSING_STARTED, ATOM_EXTRACTED, PARSE_ERROR и т.д.), отправлялись с `sourceComponent="dali"`. В EventStream фильтр по компоненту `dali` возвращал события парсера, а фильтр `hound` — ничего.

### Исправление

Файл: `services/dali/src/main/java/studio/seer/dali/heimdall/HeimdallEmitter.java`

- Добавлен параметр `sourceComponent` в приватный метод `build()`
- Добавлены `houndInfo()` / `houndWarn()` — аналоги `info()` / `warn()` с `sourceComponent="hound"`
- Все события Hound-происхождения переключены на `houndInfo()` / `houndWarn()`

**Маппинг sourceComponent после исправления:**

| Событие | sourceComponent |
|---|---|
| SESSION_STARTED | dali |
| SESSION_COMPLETED | dali |
| SESSION_FAILED | dali |
| FILE_PARSING_STARTED | **hound** |
| FILE_PARSING_COMPLETED | dali |
| FILE_PARSING_FAILED | **hound** |
| ATOM_EXTRACTED | **hound** |
| PARSE_ERROR | **hound** |
| PARSE_WARNING | **hound** |

**Дополнительно:** `SESSION_STARTED` теперь включает `threads` в payload — количество worker-потоков из `HoundConfig.workerThreads()`.

### HeimdallClient path fix

Исправлена ошибочная разбивка `@Path`:

```java
// было:
@Path("/api")        // на интерфейсе
@Path("/events")     // на методе
// → реальный URL: /api/events (верно)

// стало:
@Path("/events")     // на интерфейсе
// без @Path на методе
// → реальный URL: /events (верно — HEIMDALL endpoint: POST /events)
```

Предыдущий вариант работал только потому, что `quarkus.rest-client.heimdall-api.url` уже содержал `/api` в base URL. После стандартизации URL это могло вызвать дублирование пути.

---

## 4. EventStreamPage: улучшения фильтрации

### Добавлено

- **Фильтр по типу события**: dropdown динамически заполняется из текущего буфера. Показывает тип и количество вхождений (например, `PARSE_ERROR (12)`). Передаётся в SSE через `EventFilter.type`.
- **Pause / Resume**: кнопка замораживает отображаемые события без потери live-буфера. При pause отображается счётчик замороженных событий и желтый badge "PAUSED".
- **Stats breakdown**: строка с `N INFO / M WARN / K ERR` + chips с количеством событий по компонентам (отсортированы по убыванию).

### Изменённые файлы

| Файл | Изменение |
|---|---|
| `EventStreamPage.tsx` | Type filter, pause/resume, stats bar |
| `eventFormat.ts` | PARSE_ERROR/PARSE_WARNING labels + formatPayload cases |
| `i18n/en/common.json` | `eventType: "Type"` |
| `i18n/ru/common.json` | `eventType: "Тип"` |

---

## Известные проблемы и ограничения

### KP-1 — Quarkus hot reload не подхватывает изменения hound-библиотеки

**Статус:** известно, не баг  
**Описание:** `libraries/hound/` компилируется в JAR-зависимость для Dali. Quarkus dev mode может перекомпилировать только исходники самого сервиса, но не внешние JARы. После любых изменений в `HoundParserImpl`, `ParseResult`, `PlSqlErrorCollector` или любом другом классе `hound` — необходим полный перезапуск.

**Процедура перезапуска:**
```bash
# 1. Остановить Dali (Ctrl+C)
# 2. Пересобрать библиотеки
./gradlew :libraries:hound:build :shared:dali-models:build
# 3. Запустить Dali
./gradlew :services:dali:quarkusDev
```

### KP-2 — atomsResolved/Unresolved в старых сессиях

**Статус:** не баг — намеренное backward-compatible поведение  
**Описание:** Сессии, запущенные до внедрения этого коммита, не содержат `atomsResolved` и `atomsUnresolved` в ответе API. Frontend корректно определяет это через `fr.atomsResolved !== undefined` и показывает старый формат `XX.X%`.

### KP-3 — BUG-SS-022: resolutionRate=NaN при atomCount=0 в merge()

**Статус:** 🟡 DEFER (задокументировано в STABILIZING_SPRINT_APR13_2026.md)  
**Описание:** Если в батч-сессии есть файл с `atomCount=0` и `resolutionRate=NaN`, итоговый weighted average в `merge()` становится NaN. Не исправлен в данном коммите.  
**Обходной путь:** Frontend показывает `—` для файлов с `atomCount=0` — визуально проблема не проявляется.

### KP-4 — Счётчик pending в UI включает константы и function calls

**Статус:** намеренное поведение  
**Описание:** `pending = atomCount - atomsResolved - atomsUnresolved`. В это число входят атомы `SYSDATE`, `NVL(col, 0)`, строковые литералы — они намеренно исключены из rate-denominator. Это не "необработанные" атомы, а атомы, не требующие column resolution.

### KP-5 — EventStream Type filter сбрасывается при очистке буфера

**Статус:** minor UX  
**Описание:** При нажатии кнопки "Clear" буфер событий очищается → список seen types в dropdown становится пустым → выбранный фильтр по типу теряется из dropdown (но значение остаётся в state и продолжает фильтровать входящие события).

---

## Инструкция по проверке

### 1. Проверить ANTLR4 reclassification

1. Запустить сессию Dali с SQL-файлом содержащим `CREATE INDEX` или `GRANT`
2. В таблице FILES: файл должен показывать `success=false` (✗) если только эти операторы и есть
3. В EventStream HEIMDALL: события `PARSE_ERROR` должны иметь красный badge ERR
4. `sourceComponent` фильтр `hound` должен показывать эти события

### 2. Проверить Resolution breakdown

1. Запустить batch-сессию с несколькими SQL-файлами, содержащими SELECT-запросы с column references
2. В SessionDetail → FILES таблица: колонка RESOLUTION должна показывать:
   - `✓ N (X%)` зелёным
   - `✗ M (Y%)` красным (если M > 0)
   - `… K` серым (если есть константы/functions)
3. Для файлов с `atomCount=0` должен показываться `—`

### 3. Проверить Event source tagging

1. Запустить сессию Dali
2. В EventStream → фильтр Component = `hound`: должны видеть FILE_PARSING_STARTED, ATOM_EXTRACTED, PARSE_ERROR/WARNING
3. Component = `dali`: SESSION_STARTED, SESSION_COMPLETED, SESSION_FAILED
4. SESSION_STARTED payload: должен содержать `threads: N`

### 4. Проверить EventStream improvements

1. Запустить активную сессию, открыть EventStream
2. В dropdown "Type" должны появляться типы событий по мере их прихода
3. Нажать "⏸ Pause" — новые события перестают отображаться (но буфер продолжает заполняться)
4. Нажать "▶ Resume" — все накопленные события появляются
5. Stats bar должен показывать `N INFO / M WARN / K ERR` и chips компонентов

---

## Файлы изменены

| Компонент | Файл | Изменение |
|---|---|---|
| Hound | `libraries/hound/src/main/java/com/hound/parser/PlSqlErrorCollector.java` | ANTLR4 reclassification |
| Hound | `libraries/hound/src/main/java/com/hound/api/ParseResult.java` | +atomsResolved, +atomsUnresolved |
| Hound | `libraries/hound/src/main/java/com/hound/HoundParserImpl.java` | calcResStats() refactor |
| Dali shared | `shared/dali-models/src/main/java/studio/seer/shared/FileResult.java` | +atomsResolved, +atomsUnresolved |
| Dali | `services/dali/src/main/java/studio/seer/dali/job/ParseJob.java` | toFileResult() + merge() |
| Dali | `services/dali/src/main/java/studio/seer/dali/heimdall/HeimdallEmitter.java` | hound/dali sourceComponent split |
| Dali | `services/dali/src/main/java/studio/seer/dali/heimdall/HeimdallClient.java` | @Path fix |
| Heimdall UI | `frontends/heimdall-frontend/src/components/dali/SessionList.tsx` | RESOLUTION column, vertex collapsible |
| Heimdall UI | `frontends/heimdall-frontend/src/api/dali.ts` | atomsResolved? / atomsUnresolved? |
| Heimdall UI | `frontends/heimdall-frontend/src/pages/EventStreamPage.tsx` | type filter, pause, stats |
| Heimdall UI | `frontends/heimdall-frontend/src/utils/eventFormat.ts` | PARSE_ERROR/WARNING labels |
| Heimdall UI | `frontends/heimdall-frontend/src/i18n/locales/en/common.json` | eventType key |
| Heimdall UI | `frontends/heimdall-frontend/src/i18n/locales/ru/common.json` | eventType key |
