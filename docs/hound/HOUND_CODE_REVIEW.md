# Hound — Code Review Report

**Документ:** `HOUND_CODE_REVIEW`
**Версия:** 1.0
**Дата review:** 11.04.2026
**Статус:** Reference document → action items перенесены в `REFACTORING_PLAN.md §C.1`

---

## 1. Граф зависимостей модулей

| Модуль | Зависит от (внутренние пакеты) |
|---|---|
| HoundApplication | semantic.engine, semantic.model, semantic.dialect.plsql, storage, diagnostic, metrics, processor |
| processor.FileProcessor | semantic.engine, semantic.dialect.plsql, semantic.model, parser.core, graph |
| processor.DirectoryScanner | util.FileUtils |
| semantic.engine.UniversalSemanticEngine | semantic.model.* |
| semantic.engine.NameResolver | diagnostic.ResolutionLogger |
| semantic.engine.StructureAndLineageBuilder | diagnostic.ResolutionLogger, semantic.model.* |
| semantic.engine.AtomProcessor | semantic.model.{AtomInfo,RoutineInfo,StatementInfo,TableInfo} |
| semantic.dialect.DialectAdapter | semantic.engine.UniversalSemanticEngine |
| semantic.dialect.plsql.PlSqlSemanticListener | semantic.engine.UniversalSemanticEngine, semantic.engine.CanonicalTokenType, semantic.listener.BaseSemanticListener |
| semantic.listener.BaseSemanticListener | semantic.engine.UniversalSemanticEngine |
| storage.ArcadeDBSemanticWriter | metrics.PipelineTimer, semantic.model.* |
| storage.EmbeddedWriter | metrics.PipelineTimer, semantic.model.* |
| storage.RemoteWriter | metrics.PipelineTimer, semantic.model.* |
| storage.WriteHelpers | semantic.model.{AtomInfo,StatementInfo,Structure} |
| storage.JsonlBatchBuilder | semantic.model.* |
| storage.DataFlowProcessor | semantic.model.{AtomInfo,StatementInfo} |
| graph.GraphWriter | parser.core.AstCollector |
| parser.core.UniversalParser | graph, parser.AstListener, parser.LanguageParser, parser.registry.ParserRegistry |
| parser.registry.ParserRegistry | parser.LanguageParser, parser.core.UniversalParser |

**Модули с наибольшей входящей связанностью (fan-in):**

| Ранг | Модуль | Зависимых модулей |
|---|---|---|
| 1 | semantic.model.* | 8 |
| 2 | semantic.engine.UniversalSemanticEngine | 5 |
| 3 | storage.WriteHelpers (статический импорт) | 3 |
| 4 | metrics.PipelineTimer | 3 |

---

## 2. Циклические зависимости

**Найдено: 1 цикл**

```
parser.core.UniversalParser
    → parser.registry.ParserRegistry
        → parser.core.UniversalParser  ⚠️ ЦИКЛ
```

`ParserRegistry` хранит `UniversalParser` и одновременно создаётся `UniversalParser`-ом. Нарушение: «фабрика не знает о том, кого создаёт».

**Решение:** ввести интерфейс `IParserFactory` (реализует уже существующий `ParserFactory`). `ParserRegistry` зависит только от `IParserFactory`. `UniversalParser` получает фабрику через DI, не через прямой импорт реестра.

---

## 3. Потенциальные баги

| Приоритет | Файл:Строка | Проблема | Условие | Решение |
|---|---|---|---|---|
| ✅ FIXED | `StructureAndLineageBuilder.java:44` | NPE — `tableName.toUpperCase()` без null-проверки | `ensureTable(null, ...)` из любого listener | `if (tableName == null \|\| tableName.isBlank()) return "UNKNOWN";` |
| ✅ FIXED | `StructureAndLineageBuilder.java:88` | NPE — `tableName.toUpperCase()` в `ensureTableWithType` | Тот же сценарий | Та же защита |
| ✅ FIXED | `ArcadeDBSemanticWriter.java:122` | Potential NPE — `result.getLineage().size()` без проверки | `getLineage()` вернёт null при пустом `SemanticResult` | `int cnt = result.getLineage() != null ? result.getLineage().size() : 0;` |
| ✅ FIXED | `NameResolver.java:441-483` | Потенциальная бесконечная рекурсия в `resolveImplicitTableInternal` — нет depth guard | Циклическая иерархия `statement` в `StatementInfo` (data bug) | Добавить `depth` счётчик с `MAX_RECURSION_DEPTH`, аналогично `resolveParentRecursive` |
| 🟠 High | `StructureAndLineageBuilder.java:490-497` | Quality > 1.0 — атом может быть `resolved` и `constant/function` одновременно (двойной подсчёт) | `SELECT NVL(col, 'default')` — col resolved + 'default' constant | `Math.min(1.0, ...)` или `else if` вместо независимых `if` |
| ✅ FIXED | `ThreadPoolManager.java:23` | Все потоки называются `hound-worker-1` — `new AtomicInteger(0)` создаётся заново на каждый поток | Все сценарии с >1 потоком | `private static final AtomicInteger THREAD_SEQ = new AtomicInteger(0);` |
| ✅ FIXED | `HoundApplication.java:348` | `InterruptedException` перехватывается, `interrupt()` восстанавливается, но цикл продолжается | Оставшиеся futures могут быть пропущены без полного прерывания | После `Thread.currentThread().interrupt()` добавить `break;` |

### Детали B1/B2 — ensureTable NPE

```java
// Сейчас (NPE при tableName == null):
String upperName = tableName.toUpperCase();

// Исправление:
if (tableName == null || tableName.isBlank()) {
    logger.warn("ensureTable called with null/blank tableName, skipping");
    return "UNKNOWN";
}
String upperName = tableName.toUpperCase();
```

### Детали B6 — thread naming

```java
// Сейчас: new AtomicInteger(0) создаётся per-поток, всегда 1
// Исправление:
private static final AtomicInteger THREAD_SEQ = new AtomicInteger(0);
...
t.setName("hound-worker-" + THREAD_SEQ.incrementAndGet());
```

---

## 4. Производительность

| Текущая | Место | Сложность | Целевая | Plan |
|---|---|---|---|---|
| O(A × T) | `NameResolver.resolveTableByNameOnly:227-244` | Перебор всех таблиц для каждого resolve по имени | O(A) | Добавить `Map<String, List<String>> tablesByName` в `StructureAndLineageBuilder`, обновлять в `ensureTable()` |
| O(A × T) | `NameResolver.resolveTableByAliasInScope:210-217` | Перебор всех таблиц для поиска по alias | O(A) | Добавить `Map<String, String> aliasByTableGeoid` в `ScopeContext`/`ScopeManager` |
| O(N × D) | `StructureAndLineageBuilder.serializeStatements:447` | `computeDepth()` при каждой сериализации | O(N) | Кэшировать `depth` в `StatementInfo` при вызове `addStatement()` |
| O(N²) worst | `StatementInfo.addChildStatement:165` | `List.contains()` на children | O(N) | Заменить `List<String>` на `LinkedHashSet<String>` (порядок + O(1) contains) |
| 3× дублирование | `computeStatementQuality`, `computeDepth`, `hasCte` | В `StructureAndLineageBuilder` — 3 stream-прохода O(3A), в `WriteHelpers` — один цикл O(A) | O(A) | Удалить копии из `StructureAndLineageBuilder` и `JsonlBatchBuilder`, вызывать `WriteHelpers.*` |

---

## 5. Рекомендации по декомпозиции

### R1 — Унифицировать дублированные утилиты

`computeStatementQuality()`, `computeDepth()`, `hasCte()` присутствуют в трёх местах: `StructureAndLineageBuilder`, `WriteHelpers`, `JsonlBatchBuilder`. Канонической реализацией должна быть `WriteHelpers`. `StructureAndLineageBuilder` и `JsonlBatchBuilder` делегируют к ней.

### R2 — Разделить DialectAdapter / UniversalSemanticEngine

`DialectAdapter` зависит от конкретного `UniversalSemanticEngine`. Ввести интерфейс `ISemanticEngine` — `DialectAdapter` и `BaseSemanticListener` зависят от интерфейса, не от реализации.

### R3 — Вынести индексы имён из NameResolver

`NameResolver` выполняет O(T) сканирования при каждом resolve. Индексы `Map<String, List<String>> tablesByName` и `Map<String, String> tablesByAlias` должны жить в `StructureAndLineageBuilder` и обновляться инкрементально.

---

## 6. Асинхронный код — анализ

Проект использует Java `Future` + `ExecutorService`. Проблемы:

| Файл:Строка | Статус | Детали |
|---|---|---|
| `HoundApplication.java:348` | ⚠️ Bug | `InterruptedException` → `interrupt()` восстанавливается, но цикл не прерывается. Нужен `break;` |
| `RemoteWriter.java:1126-1127` | ✅ OK | `Thread.sleep(delay)` в retry-loop с `interrupt()` + `break` — корректный паттерн |
| `CacheManager.java:21,54` | ✅ OK | `ScheduledExecutorService` однопоточный — корректно для TTL-eviction |

**Гонок состояний нет:** каждый файл — отдельный `UniversalSemanticEngine`. `NameResolver.resolutionCache` — per-instance `HashMap`, не разделяется. `CanonicalPool` — `ConcurrentHashMap` (корректно).

---

## 7. Логическая корректность

| Место | Проблема |
|---|---|
| `NameResolver.resolveInternal` (strategy 8) | Стратегия 8 (implicit table) не вызывается из `resolveInternal()` — отдельный публичный метод `resolveImplicitTable()`. Комментарий «8 стратегий» вводит в заблуждение. Вероятно намеренно, но документация расходится с кодом. |
| `DataFlowProcessor.resolveFlowType` vs `WriteHelpers.resolveFlowType` | Идентичный метод дублирован — правки в одной копии не попадают в другую. |
| `resolveFlowType` — порядок условий | AGGREGATE проверяется до `is_function_call`. `COUNT(*) GROUP BY` → "AGGREGATE", не "TRANSFORM". Возможно правильно, но не задокументировано. |
| `StatementInfo.addChildStatement:165` | `childStatements.contains(childGeoid)` — O(n) поиск в `List`. При многих child → O(n²). |

---

## 8. Приоритеты (сводная таблица)

| Приоритет | # | Задача | Effort | Место в C.1 |
|---|---|---|---|---|
| ✅ FIXED | B1 | NPE в `ensureTable` (строки 44, 88) | ~1 ч | C.1.0 |
| ✅ FIXED | B3 | NPE в `ArcadeDBSemanticWriter:122` | ~30 мин | C.1.0 |
| ✅ FIXED | B4 | Бесконечная рекурсия в `resolveImplicitTableInternal` | ~2 ч | C.1.0 |
| 🟠 High | P1 | O(A×T) → O(A) в `resolveTableByNameOnly` | ~3 ч | C.1.6 |
| 🟠 High | P2 | O(N²) → O(N) в `addChildStatement` (`LinkedHashSet`) | ~1 ч | C.1.6 |
| ✅ FIXED | B5 | Quality > 1.0 двойной подсчёт | ~1 ч | C.1.0 |
| ✅ FIXED | B6 | Thread naming всегда `hound-worker-1` | ~30 мин | C.1.0 |
| ✅ FIXED | B7 | `InterruptedException` без `break` | ~30 мин | C.1.0 |
| 🟡 Medium | R1 | Дублирование `computeStatementQuality/Depth/hasCte` | ~2 ч | C.1.7 |
| 🟢 Low | R2 | `ISemanticEngine` интерфейс | ~3 ч | C.1.7 |
| 🟢 Low | Cycle | `UniversalParser ↔ ParserRegistry` цикл | ~2 ч | C.1.7 |

---

## Дополнительные фиксы (обнаружены в процессе, не были в review)

> Полное описание: `HOUND_BUGFIX_REPORT.md`

| # | Файл | Проблема | Статус |
|---|---|---|---|
| **Fix 1** | `UniversalSemanticEngine.java` (3 точки) | Orphaned DaliColumn без HAS_COLUMN — `addColumn()` вызывался для SubQuery/CTE geoid | ✅ FIXED |
| **Fix 2** | `StructureAndLineageBuilder.java` | Double-schema geoid: `ensureTable("DWH.DIM_CUSTOMER","DWH")` → `DWH.DWH.DIM_CUSTOMER` | ✅ FIXED |
| **Fix 3** | `RemoteWriter.java` (`write` + `writeBatch`) | HAS_COLUMN не создавался для pool-cached колонок новых таблиц | ✅ FIXED |
| **Fix 4** | `JsonlBatchBuilder.java` | UNBOUND false-positive для атомов в SubQuery/CTE geoid | ✅ FIXED |
| **Fix 5** | 5 файлов схемы | Удалены неиспользуемые типы: DaliPerfStats, DaliResolutionLog, DaliSchemaLog, DaliMeta | ✅ DONE |
| **Fix 6** | `EmbeddedWriter.java`, `RemoteWriter.java` | `--clean` медленный (N×500 DELETE loops) → `TRUNCATE TYPE ... UNSAFE` | ✅ FIXED |

**Fix 1 — Guard pattern (применён в 3 точках):**
```java
if (!builder.getStatements().containsKey(tableGeoid)) {
    builder.addColumn(tableGeoid, ...);
}
```

**Fix 2 — Безусловный strip схемы:**
```java
if (upperName.contains(".")) {
    upperName = parts[parts.length - 1];  // "DIM_CUSTOMER" (не "DWH.DIM_CUSTOMER")
}
```

**Fix 3 — Sweep по rid.columns через prefix:**
```java
String prefix = tblGeoid + ".";
for (var e : rid.columns.entrySet()) {
    if (!skipGeoids.contains(e.getKey()) && e.getKey().startsWith(prefix))
        edgeByRid("HAS_COLUMN", fromRid, e.getValue(), sid);
}
```

**Fix 4 — UNBOUND только для физических таблиц:**
```java
&& str.getTables().containsKey(atomTblForWarn)  // ← добавлено
&& !str.getColumns().containsKey(tblGeoid + "." + colName)
```

**Fix 6 — TRUNCATE:**
```java
db.command("sql", "TRUNCATE TYPE `" + typeName + "` UNSAFE");
// было: while loop SELECT count(*) + DELETE LIMIT 500
```
