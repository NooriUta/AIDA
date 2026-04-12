# VERDANDI — Отчёт по сессии: KNOT Report + LOOM Stabilization

**Дата:** 07.04.2026
**Ветка:** `feature/knot-report` → смержена в `master` (PR #3)
**Коммиты:** `dde4418`, `1590188`
**Автор:** Claude Code

---

## 1. Сводка

За сессию реализован **KNOT Report Inspector** — полноценный 5-вкладочный инспектор разбора SQL-сессий, закрыты все задачи технического долга (S-01 — S-05) из плана недели 07–11 апреля, а также исправлен баг отображения L1 в light mode.

| Категория | Задач | Статус |
|-----------|-------|--------|
| KNOT Report (новая фича) | 1 крупная | ✅ |
| Критический долг S-01–S-05 | 5 | ✅ |
| Баг L1 light mode | 1 | ✅ |
| Unit-тесты | 24 теста | ✅ |

---

## 2. KNOT Report Inspector

### 2.1 Назначение

KNOT — отдельный модуль VERDANDI (маршрут `/knot`). Позволяет выбрать сессию разбора (KnotSession) и просмотреть детальный отчёт в 5 вкладках: Summary, Structure, Statements, Routines, Atoms.

### 2.2 Backend (SHUTTLE)

#### Новые модели

| Файл | Описание |
|------|----------|
| `KnotSession.java` | Сессия разбора: id, sessionName, filePath, dialect, processingMs, счётчики |
| `KnotReport.java` | Полный отчёт: session + tables + columns + statements + atoms + calls + snippets + outputColumns + parameters + variables |
| `KnotTable.java` | Таблица: name, schema, type, aliases, columnCount, sourceCount, targetCount |
| `KnotColumn.java` | Колонка-псевдоним с aliasName, dataType, position, atomRefCount |
| `KnotStatement.java` | Statement: geoid, stmtType, routineType, lineNumber, packageName, routineName, stmtAliases, atomCount |
| `KnotAtom.java` | Атом: atomText, columnName, tableName, position, status, isComplex, isRoutineParam, isRoutineVar, nestedAtomsCount |
| `KnotCall.java` | Вызов рутины: callerName, calleeName, callCount |
| `KnotSnippet.java` | SQL-сниппет: geoid → sqlText |
| `KnotOutputColumn.java` | Выходная колонка: stmtGeoid, columnName, expression, alias, tableRef |
| `KnotParameter.java` | Параметр рутины: routineName, paramName, dataType, direction (IN/OUT/IN OUT) |
| `KnotVariable.java` | Переменная рутины: routineName, varName, dataType |

#### KnotService — ключевые решения

**Параллельность через Tuple8:**
```java
return Uni.combine().all().unis(
    loadSession(sessionId),      // Item1
    loadTables(sessionId),       // Item2
    loadColumns(sessionId),      // Item3
    loadStatements(sessionId),   // Item4
    loadAtoms(sessionId),        // Item5
    loadCalls(sessionId),        // Item6
    loadSnippets(sessionId),     // Item7
    loadParamsAndVars(sessionId) // Item8 — KnotParamVars holder
).asTuple();
```

Mutiny поддерживает до 9 typed-tuple; объединение params+vars в один `KnotParamVars` record позволило уложиться в 8 слотов и избежать вложенных combine.

**sourceCount / targetCount (исправление):**
Ранее всегда было 0. Исправлено двумя дополнительными параллельными Cypher-запросами:
- `MATCH (s:DaliStatement)-[:READS_FROM]->(t:DaliTable) WHERE s.session_id = :sid RETURN t.table_name, count(*)`
- `MATCH (s:DaliStatement)-[:WRITES_TO]->(t:DaliTable) WHERE s.session_id = :sid RETURN t.table_name, count(*)`

Результаты преобразуются в `Map<String, Integer>` и применяются при сборке `KnotTable`.

**Error recovery:**
Каждый `Uni`-запрос покрыт `.onFailure().recoverWithItem(List.of())` — падение одного запроса не ломает весь отчёт.

#### KnotResource

```
GET /knot/report/{sessionId}  →  KnotReport (JSON)
```

SmallRye GraphQL query `knotReport(sessionId: String): KnotReport`.

#### Исправление SQL injection в SearchService

**До:**
```java
private Uni<List<SearchResult>> q(String template, String like, int n) {
    String sql = String.format(template, esc(like), n);  // УЯЗВИМОСТЬ
    return arcade.sql(sql, Map.of());
}
private static String esc(String s) { return s.replace("'", "''"); }
```

**После:**
```java
private Uni<List<SearchResult>> q(String template, String like, int n) {
    String sql = String.format(template, n);          // только integer — безопасно
    return arcade.sql(sql, Map.of("q", like));        // :q — параметризованный
}
// Шаблон: WHERE table_name LIKE :q LIMIT %d
```

`esc()` удалён. Все 12 SQL-шаблонов переведены с `'%s'` на `:q`.

### 2.3 Frontend (verdandi)

#### Маршрут и навигация

`App.tsx` — добавлен маршрут `/knot` → `<KnotPage />`.
`Header.tsx` — кнопка KNOT в навигации.

#### KnotPage.tsx

Главный контейнер: выбор сессии слева + 5-вкладочный Inspector справа.

```
┌─────────────────┬──────────────────────────────────────────┐
│  Session list   │  [Summary][Structure][Statements][Routines][Atoms] │
│  (scrollable)   │                                          │
│                 │  Tab content                             │
└─────────────────┴──────────────────────────────────────────┘
```

#### KnotSummary.tsx

Сводка по сессии: счётчики (tables, columns, schemas, packages, routines, params, vars), dialect, время разбора, session ID. Статистика statements по типу (SELECT/INSERT/UPDATE/DELETE/MERGE/CURSOR/Other) в виде горизонтальных bar-charts.

#### KnotStructure.tsx

Таблица таблиц с фильтром по имени. Для каждой таблицы: name, schema, type, aliases, кол-во колонок, sourceCount, targetCount. Раскрываемая строка → список колонок с типами данных.

#### KnotStatements.tsx

Двухпанельный layout: список statements слева + детальный просмотр справа.

Детали statement:
- Основная информация: тип, уровень, рутина, пакет, строка
- Source tables / Target tables
- Output columns с expression
- Source atoms
- SQL-сниппет (если есть)
- Дочерние subqueries (раскрываемые)

#### KnotRoutines.tsx

Таблица рутин с expand по строке.

**Expanded panel — динамическая сетка:**
- 2 фиксированных колонки (Routine, Package)
- + N дополнительных (Callees / Parameters / Variables) — добавляются только если есть данные

**Parameters panel:**
Direction badge: `I` = синий (IN), `O` = зелёный (OUT), `IO` = amber (IN OUT).
Columns: direction, paramName, dataType.

**Variables panel:**
Columns: varName, dataType.

Summary chips в заголовке строки: `+N callees`, `+N params`, `+N vars`.

#### KnotAtoms.tsx

**Секция статистики** (всегда открыта):
- Счётчики: Resolved / Failed / Constants / Func calls / Column refs / Unattached
- Breakdown по контексту (stmtType)
- Топ-5 failed атомов + топ-5 unattached

**Секция полной таблицы** (collapsible, default закрыта):
- Фильтр по статусу: All / Resolved / Failed / Constant / FuncCall
- Фильтр по флагу: All / Complex / Param / Var / Unattached
- Текстовый поиск (по atom, column, table)
- Пагинация: 100 строк / страница, навигация «‹ X/N ›»
- `FlagBadge`: ∑ (complex) / P (param) / V (var) / N (unattached) с цветными фонами
- `atomDisplayText()`: стрипает `~line:pos` суффикс для отображения

#### i18n

Добавлены ключи `knot.*` (en + ru):
- `knot.tabs.*` — названия вкладок
- `knot.session.*` — поля сессии
- `knot.statements.*` — типы SQL
- `knot.atoms.*` — фильтры атомов (incl. `allAtoms`, `searchPlaceholder`)
- `knot.stmt.*` — поля statement
- `knot.table.*`, `knot.column.*` — поля таблиц/колонок
- `knot.structure.*`, `knot.sections.*`, `knot.fields.*`

### 2.4 Unit-тесты

#### KnotServiceTest.java (22 теста)

Покрывают static helper-методы (нет DB-зависимостей):

| Метод | Кейсы |
|-------|-------|
| `atomLine(String)` | типичный формат, multi-segment, нет тильды, null, пусто, только тильда, нет двоеточия |
| `atomPos(String)` | типичный, nonZeroPos, нет двоеточия, null |
| `parseStmtType(String)` | INSERT, SELECT, nested, null, пусто, мало частей |
| `parseLineNumber(String)` | типичный, null, мало частей, не число |
| `parsePackageName(String)` | типичный, без двоеточия, null, пусто |
| `deriveName(String, String)` | из sessionName, unix path, windows path, без расширения, оба null, blank sessionName |

#### SearchServiceTest.java (2 теста)

- `sqlTemplates_doNotContainPercentS` — структурная проверка: все шаблоны содержат `:q`, не содержат `%s`
- `limitIsFormattedAsInteger` — `%d` заменяется целым числом, не остаётся в строке

---

## 3. LOOM Stabilization (S-03 / S-04 / S-05)

### S-03 — Error states в LoomCanvas

**Проблема:** При падении ELK layout (`applyELKLayout` throws) canvas оставался пустым без какого-либо сообщения пользователю — `statusKey` не срабатывал (граф существует, просто не отложен).

**Решение:**
```tsx
const [layoutError, setLayoutError] = useState(false);

// В layout useEffect:
setLayoutError(false);  // сброс при старте

.catch((err) => {
  console.error('[LOOM] ELK layout failed', err);
  if (!cancelled) setLayoutError(true);
})

// В statusKey:
if (layoutError) return 'status.error';
```

**Бонус:** Loading overlay background `rgba(20,17,8,0.75)` → `color-mix(in srgb, var(--bg0) 85%, transparent)` для корректного вида в light mode.

### S-04 — Удаление console.log

Удалены 4 лишних `console.log` из `loomStore.ts`:
- `drillDown` (строка 255 до правки)
- `jumpTo` (строка 283)
- `navigateBack` (строка 313)
- `selectNode` (строка 360)

`console.error` в LoomCanvas (ELK layout failure) оставлен — это реальная ошибка.

### S-05 — Expand error recovery

**Проблема:** При сетевой ошибке expand-запроса кнопка навсегда зависала в состоянии spinner. `clearExpandRequest()` вызывался только в `.then` (при успехе).

**Решение:**
```tsx
const { ..., isError: upstreamExpandError }   = useUpstream(upstreamExpandId);
const { ..., isError: downstreamExpandError } = useDownstream(downstreamExpandId);

useEffect(() => {
  if (upstreamExpandError && upstreamExpandId) clearExpandRequest();
}, [upstreamExpandError, upstreamExpandId]);

useEffect(() => {
  if (downstreamExpandError && downstreamExpandId) clearExpandRequest();
}, [downstreamExpandError, downstreamExpandId]);
```

### Баг L1 — ApplicationNode в light mode

**Проблема:** `background: 'rgba(20,17,8,0.55)'` — hardcoded тёмный overlay, работал только в dark mode. В Lichen light (`--bg0: #eff5ef`) контейнеры Applications выглядели как тёмные прямоугольники на светлом фоне.

**Решение:**
```tsx
// ApplicationNode container
background: 'color-mix(in srgb, var(--bg0) 80%, transparent)'

// AppIcon square
background: 'color-mix(in srgb, var(--bg0) 60%, transparent)'
```

---

## 4. Файлы изменений

### Новые файлы

| Файл | Назначение |
|------|------------|
| `SHUTTLE/.../model/Knot*.java` (11 файлов) | Java record-модели KNOT Report |
| `SHUTTLE/.../resource/KnotResource.java` | GraphQL endpoint |
| `SHUTTLE/.../service/KnotService.java` | Бизнес-логика KNOT |
| `SHUTTLE/src/test/.../KnotServiceTest.java` | 22 unit-теста |
| `SHUTTLE/src/test/.../SearchServiceTest.java` | 2 структурных теста |
| `verdandi/src/components/knot/KnotPage.tsx` | Главная страница KNOT |
| `verdandi/src/components/knot/KnotSummary.tsx` | Вкладка Summary |
| `verdandi/src/components/knot/KnotStructure.tsx` | Вкладка Structure |
| `verdandi/src/components/knot/KnotStatements.tsx` | Вкладка Statements |
| `verdandi/src/components/knot/KnotRoutines.tsx` | Вкладка Routines |
| `verdandi/src/components/knot/KnotAtoms.tsx` | Вкладка Atoms |

### Изменённые файлы

| Файл | Что изменено |
|------|--------------|
| `SHUTTLE/.../service/SearchService.java` | SQL injection fix, .onFailure() |
| `verdandi/src/App.tsx` | Маршрут /knot |
| `verdandi/src/services/lineage.ts` | KnotParameter, KnotVariable, GQL KNOT_REPORT |
| `verdandi/src/services/hooks.ts` | useKnotSessions, useKnotReport |
| `verdandi/src/i18n/locales/en/common.json` | knot.atoms.allAtoms, searchPlaceholder |
| `verdandi/src/i18n/locales/ru/common.json` | то же, ru |
| `verdandi/src/components/canvas/LoomCanvas.tsx` | layoutError state, S-05 expand recovery, overlay bg |
| `verdandi/src/components/canvas/nodes/ApplicationNode.tsx` | light mode background fix |
| `verdandi/src/stores/loomStore.ts` | удалены 4 console.log |

---

## 5. Известные ограничения (data not available)

Данные, которых нет в БД, и поэтому не реализованы:

| Поле | Причина |
|------|---------|
| Return type функции | Не хранится в DaliRoutine |
| Номер строки объявления рутины | Не хранится |
| Column geoid в atom display | Потребовал бы дополнительный JOIN |
| `atomRefCount` в KnotColumn | Всегда 0 — sourceCount/targetCount недоступны на уровне колонки |

---

## 6. Метрики сессии

| Метрика | Значение |
|---------|----------|
| Новых файлов | 17 |
| Изменённых файлов | 9 |
| Insertions | ~6 300 строк |
| Deletions | ~230 строк |
| Unit-тестов добавлено | 24 |
| Критических багов закрыто | 5 (S-01–S-05) |
| PR | NooriUta/Verdandi#3 (merged) |
