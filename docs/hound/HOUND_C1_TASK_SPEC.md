# Hound C.1 — Детальная постановка задач

**Документ:** `HOUND_C1_TASK_SPEC`
**Версия:** 1.0
**Дата:** 12.04.2026
**Применимость:** `libraries/hound/` после repo migration
**Источники:** `HOUND_CODE_REVIEW.md`, `REFACTORING_PLAN.md §C.1`

---

## Порядок выполнения (строгий)

```
C.1.0 Bug fixes (B1-B7)   ← СНАЧАЛА, до любого рефакторинга
     ↓
C.1.1 HoundParser interface
C.1.2 HoundConfig typed POJO
C.1.3 HoundEventListener interface
C.1.4 java-library plugin (уже частично сделано при repo migration)
C.1.5 REMOTE_BATCH internal
```

**Почему порядок важен:** если сначала рефакторить API (C.1.1-1.5), а потом чинить баги — придётся чинить уже в рефакторнутом коде. Баги сначала, API потом.

---

## C.1.0 — Bug fixes B1-B7

**Оценка:** ~1 день (5-6 ч)
**Запускать тесты после каждого фикса:** `./gradlew :libraries:hound:test`

---

### B1/B2 — NPE в `ensureTable` и `ensureTableWithType`

**Файл:** `src/main/java/com/hound/semantic/engine/StructureAndLineageBuilder.java`
**Строки:** 44 и 88
**Приоритет:** 🔴 Critical

**Проблема:**
```java
// Строка 44 — метод ensureTable:
String upperName = tableName.toUpperCase();  // NPE если tableName == null

// Строка 88 — метод ensureTableWithType:
String upperName = tableName.toUpperCase();  // тот же сценарий
```
`tableName == null` возникает когда listener вызывает `ensureTable()` с нераспознанным токеном — например при парсинге анонимных subquery или временных таблиц без явного имени.

**Фикс:**

```java
// Добавить в НАЧАЛО обоих методов — ensureTable() и ensureTableWithType():
if (tableName == null || tableName.isBlank()) {
    logger.warn("ensureTable called with null/blank tableName — skipping. " +
                "Context: {}", currentScope());
    return "UNKNOWN";
}
// строка 44/88 теперь безопасна:
String upperName = tableName.toUpperCase();
```

**Тест (добавить в `StructureAndLineageBuilderTest`):**
```java
@Test
void ensureTable_nullName_returnsUnknown() {
    StructureAndLineageBuilder builder = createBuilder();
    String result = builder.ensureTable(null, "dbo", TableType.REGULAR);
    assertEquals("UNKNOWN", result);
}

@Test
void ensureTable_blankName_returnsUnknown() {
    StructureAndLineageBuilder builder = createBuilder();
    String result = builder.ensureTable("   ", "dbo", TableType.REGULAR);
    assertEquals("UNKNOWN", result);
}
```

---

### B3 — NPE в `ArcadeDBSemanticWriter`

**Файл:** `src/main/java/com/hound/storage/ArcadeDBSemanticWriter.java`
**Строка:** 122
**Приоритет:** 🔴 Critical

**Проблема:**
```java
// Строка ~122:
int cntLineage = result.getLineage().size();  // NPE если getLineage() == null
```
`SemanticResult.getLineage()` может вернуть `null` если парсинг файла завершился с ошибкой и lineage не был заполнен.

**Фикс:**
```java
// Заменить строку 122:
int cntLineage = result.getLineage() != null ? result.getLineage().size() : 0;

// Или через Optional (чище):
int cntLineage = Optional.ofNullable(result.getLineage())
                         .map(List::size)
                         .orElse(0);
```

**Также проверить в том же файле** — есть ли аналогичные `result.getAtoms().size()`, `result.getStatements().size()` без null-check. Применить тот же паттерн.

**Тест:**
```java
@Test
void write_nullLineage_doesNotThrow() {
    SemanticResult result = new SemanticResult();
    result.setLineage(null);  // явно null
    assertDoesNotThrow(() -> writer.write(result));
}
```

---

### B4 — Бесконечная рекурсия в `resolveImplicitTableInternal`

**Файл:** `src/main/java/com/hound/semantic/engine/NameResolver.java`
**Строки:** 441-483
**Приоритет:** 🟠 High

**Проблема:**
```java
private String resolveImplicitTableInternal(StatementInfo stmt) {
    // ...
    if (parent != null) {
        return resolveImplicitTableInternal(parent);  // рекурсия без depth guard
    }
    return null;
}
```
Если в `StatementInfo` есть цикл (`stmt.parent → X → stmt`), это StackOverflowError. Цикл может возникнуть из-за data bug при записи в ArcadeDB.

**Фикс:**
```java
private static final int MAX_RECURSION_DEPTH = 50;

// Публичный метод — точка входа:
public String resolveImplicitTable(StatementInfo stmt) {
    return resolveImplicitTableInternal(stmt, 0);
}

// Приватный — добавить параметр depth:
private String resolveImplicitTableInternal(StatementInfo stmt, int depth) {
    if (depth > MAX_RECURSION_DEPTH) {
        logger.warn("resolveImplicitTable: max depth {} exceeded. " +
                    "Possible cycle in StatementInfo hierarchy. stmt={}",
                    MAX_RECURSION_DEPTH, stmt.getGeoid());
        return null;
    }

    // ... существующая логика ...

    if (parent != null) {
        return resolveImplicitTableInternal(parent, depth + 1);  // depth + 1
    }
    return null;
}
```

**Аналогично проверить** `resolveParentRecursive` — по коду там уже есть depth guard (из комментария в code review). Убедиться что паттерн идентичен.

**Тест:**
```java
@Test
void resolveImplicitTable_cyclicHierarchy_doesNotThrow() {
    StatementInfo s1 = new StatementInfo("stmt1");
    StatementInfo s2 = new StatementInfo("stmt2");
    s1.setParent(s2);
    s2.setParent(s1);  // цикл

    NameResolver resolver = createResolver();
    assertDoesNotThrow(() -> resolver.resolveImplicitTable(s1));
}
```

---

### B5 — Quality > 1.0 (двойной подсчёт)

**Файл:** `src/main/java/com/hound/semantic/engine/StructureAndLineageBuilder.java`
**Строки:** 490-497
**Приоритет:** 🟡 Medium

**Проблема:**
```java
// Текущий код — независимые if, атом может попасть под оба:
if (atom.isResolved()) score += resolvedWeight;
if (atom.isConstant() || atom.isFunction()) score += constantWeight;
// Итог: resolved + constant → score > total → quality > 1.0
```
Пример: `SELECT NVL(col, 'default')` — `col` может быть одновременно resolved и constant (literal default).

**Фикс — два варианта:**

Вариант A (минимальный, быстрый):
```java
// Оставить логику как есть, но добавить cap:
double quality = Math.min(1.0, score / total);
```

Вариант B (правильный, если есть время):
```java
// Mutually exclusive — атом считается только по одной категории:
if (atom.isResolved()) {
    score += resolvedWeight;
} else if (atom.isConstant() || atom.isFunction()) {
    score += constantWeight;
} else if (atom.isUnresolved()) {
    // не добавляем ничего — штраф через total
}
double quality = score / total;  // теперь всегда ≤ 1.0
```

**Рекомендация:** Вариант A для этой задачи (быстро, безопасно). Вариант B — в C.1.7 (tech debt).

**Тест:**
```java
@Test
void computeQuality_resolvedAndConstant_doesNotExceedOne() {
    List<AtomInfo> atoms = List.of(
        atom().resolved(true).constant(true).build(),  // оба флага
        atom().resolved(false).constant(false).build()
    );
    double quality = builder.computeStatementQuality(atoms);
    assertTrue(quality <= 1.0, "Quality должна быть ≤ 1.0, было: " + quality);
}
```

---

### B6 — Thread naming (всегда `hound-worker-1`)

**Файл:** `src/main/java/com/hound/processor/ThreadPoolManager.java`
**Строка:** ~23
**Приоритет:** 🟡 Medium

**Проблема:**
```java
// Сейчас — new AtomicInteger(0) создаётся per-поток → всегда incrementAndGet() == 1:
t.setName("hound-worker-" + new AtomicInteger(0).incrementAndGet());
// Каждый поток называется "hound-worker-1"
```

**Фикс:**
```java
// На уровне класса (static — один счётчик на JVM):
private static final AtomicInteger THREAD_SEQ = new AtomicInteger(0);

// В лямбде factory:
t.setName("hound-worker-" + THREAD_SEQ.incrementAndGet());
// Потоки: hound-worker-1, hound-worker-2, hound-worker-3, ...
```

**Тест:**
```java
@Test
void threadPool_threadsHaveUniqueNames() throws InterruptedException {
    Set<String> names = ConcurrentHashMap.newKeySet();
    ExecutorService pool = ThreadPoolManager.createPool(3);
    CountDownLatch latch = new CountDownLatch(3);

    for (int i = 0; i < 3; i++) {
        pool.submit(() -> {
            names.add(Thread.currentThread().getName());
            latch.countDown();
        });
    }
    latch.await();
    assertEquals(3, names.size(), "Все потоки должны иметь уникальные имена");
    names.forEach(name -> assertTrue(name.startsWith("hound-worker-")));
}
```

---

### B7 — `InterruptedException` без `break`

**Файл:** `src/main/java/com/hound/HoundApplication.java`
**Строка:** ~348
**Приоритет:** 🟡 Medium

**Проблема:**
```java
for (Future<ParseResult> future : futures) {
    try {
        results.add(future.get());
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();  // восстанавливаем флаг
        // НО цикл продолжается — обрабатываем оставшиеся futures!
        // future.get() на interrupted thread сразу бросит InterruptedException снова
    } catch (ExecutionException e) {
        // ...
    }
}
```

**Фикс:**
```java
for (Future<ParseResult> future : futures) {
    try {
        results.add(future.get());
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        logger.warn("Processing interrupted. {} futures remaining.", futures.size());
        break;  // ← добавить — прекращаем обработку при прерывании
    } catch (ExecutionException e) {
        // ...
    }
}
```

**Тест:**
```java
@Test
void processFiles_interrupted_stopsGracefully() {
    // Создать несколько futures, прервать поток перед .get()
    // Проверить что метод завершается, не зависает
}
```

---

### Checklist C.1.0

```
[ ] B1 — ensureTable() строка 44: null-check добавлен
[ ] B2 — ensureTableWithType() строка 88: null-check добавлен
[ ] B3 — ArcadeDBSemanticWriter.java:122: null-check добавлен
[ ] B3 — проверены аналогичные вызовы .size() в том же файле
[ ] B4 — resolveImplicitTableInternal: depth counter добавлен (MAX=50)
[ ] B4 — resolveImplicitTable() — публичный метод делегирует с depth=0
[ ] B5 — Math.min(1.0, ...) в computeStatementQuality добавлен
[ ] B6 — THREAD_SEQ static final — уникальные имена потоков
[ ] B7 — break после interrupt() восстановления
[ ] ./gradlew :libraries:hound:test → GREEN
[ ] Новые тесты добавлены для каждого бага (или задокументировано почему нет)
```

---

## C.1.1 — HoundParser: Publish-ready API

**Оценка:** ~1 день
**Файлы:**
- Создать: `src/main/java/com/hound/api/HoundParser.java` (интерфейс)
- Создать: `src/main/java/com/hound/api/HoundParserImpl.java` (реализация)
- Создать: `src/main/java/com/hound/api/ParseResult.java` (результат)
- Изменить: `src/main/java/com/hound/HoundApplication.java` (делегировать в impl)

**Интерфейс:**

```java
// com/hound/api/HoundParser.java
package com.hound.api;

import java.nio.file.Path;
import java.util.List;

/**
 * Public API для программного использования Hound.
 * Импортируется Dali через: implementation project(':libraries:hound')
 *
 * Жизненный цикл: один экземпляр HoundParserImpl создаётся Dali при старте,
 * переиспользуется для всех сессий. Thread-safe (каждый parse создаёт
 * отдельный UniversalSemanticEngine).
 */
public interface HoundParser {

    /**
     * Парсить один файл.
     * @param file    путь к SQL файлу
     * @param config  конфигурация (dialect, writeMode, arcadeUrl, ...)
     * @return результат парсинга (никогда null; при ошибке — ParseResult.failed())
     */
    ParseResult parse(Path file, HoundConfig config);

    /**
     * Парсить один файл с listener для real-time событий.
     * @param listener может быть null (тогда без событий)
     */
    ParseResult parse(Path file, HoundConfig config, HoundEventListener listener);

    /**
     * Парсить список файлов параллельно (workerThreads из config).
     * @return результаты в том же порядке что входные файлы
     */
    List<ParseResult> parseBatch(List<Path> files, HoundConfig config);

    /**
     * Парсить список файлов с listener.
     */
    List<ParseResult> parseBatch(List<Path> files, HoundConfig config,
                                  HoundEventListener listener);
}
```

**Результат:**

```java
// com/hound/api/ParseResult.java
package com.hound.api;

import java.nio.file.Path;
import java.time.Duration;

public record ParseResult(
    Path file,
    boolean success,
    int atomCount,
    int statementCount,
    double resolutionRate,    // 0.0-1.0; NaN если atomCount == 0
    Duration elapsed,
    String errorMessage       // null если success == true
) {
    /** Создать результат об ошибке. */
    public static ParseResult failed(Path file, String error) {
        return new ParseResult(file, false, 0, 0, Double.NaN,
                               Duration.ZERO, error);
    }

    /** Удобная проверка. */
    public boolean failed() { return !success; }
}
```

**Реализация:**

```java
// com/hound/api/HoundParserImpl.java
package com.hound.api;

/**
 * Реализация HoundParser поверх существующего FileProcessor.
 * Не содержит собственной логики — делегирует в FileProcessor.
 */
public class HoundParserImpl implements HoundParser {

    @Override
    public ParseResult parse(Path file, HoundConfig config) {
        return parse(file, config, null);
    }

    @Override
    public ParseResult parse(Path file, HoundConfig config, HoundEventListener listener) {
        // Адаптировать FileProcessor.analyzeFile() → ParseResult
        // listener.onFileParseStarted() / onFileParseCompleted() / onError()
        var start = System.nanoTime();
        try {
            if (listener != null) listener.onFileParseStarted(file.toString());

            FileProcessor processor = new FileProcessor(config);
            var result = processor.analyzeFile(file);

            var parseResult = new ParseResult(
                file, true,
                result.getAtoms().size(),
                result.getStatements().size(),
                computeRate(result),
                Duration.ofNanos(System.nanoTime() - start),
                null
            );

            if (listener != null) listener.onFileParseCompleted(file.toString(), parseResult);
            return parseResult;

        } catch (Exception e) {
            var failed = ParseResult.failed(file, e.getMessage());
            if (listener != null) listener.onError(file.toString(), e);
            return failed;
        }
    }

    @Override
    public List<ParseResult> parseBatch(List<Path> files, HoundConfig config) {
        return parseBatch(files, config, null);
    }

    @Override
    public List<ParseResult> parseBatch(List<Path> files, HoundConfig config,
                                         HoundEventListener listener) {
        // Использует существующий ThreadPoolManager
        // Параллельность: config.workerThreads()
        // Возвращает в порядке входных файлов (preserve order)
        return files.stream()
            .parallel()  // или через ExecutorService с config.workerThreads()
            .map(f -> parse(f, config, listener))
            .toList();
    }

    private double computeRate(SemanticResult result) {
        int total = result.getAtoms().size();
        if (total == 0) return Double.NaN;
        long resolved = result.getAtoms().stream()
            .filter(AtomInfo::isResolved).count();
        return (double) resolved / total;
    }
}
```

**Обновить `HoundApplication.main()`:**

```java
// Было: всё в main()
// Стало:
public static void main(String[] args) {
    HoundConfig config = HoundConfig.fromArgs(args);  // парсинг CLI → C.1.2
    HoundParser parser = new HoundParserImpl();
    List<Path> files = collectFiles(config);
    List<ParseResult> results = parser.parseBatch(files, config);
    printSummary(results);
}
```

**Checklist C.1.1:**
```
[ ] HoundParser.java интерфейс создан (4 метода)
[ ] ParseResult.java record создан
[ ] HoundParserImpl.java реализация создана
[ ] HoundApplication.main() делегирует в HoundParserImpl
[ ] Standalone работает: ./gradlew :libraries:hound:run → те же результаты что раньше
[ ] ./gradlew :libraries:hound:test → GREEN
```

---

## C.1.2 — HoundConfig: Typed POJO

**Оценка:** ~полдня
**Файлы:**
- Создать: `shared/dali-models/src/main/java/com/aida/shared/HoundConfig.java`
- Создать: `shared/dali-models/src/main/java/com/aida/shared/ArcadeWriteMode.java`
- Изменить: `libraries/hound/build.gradle` — добавить `implementation project(':shared:dali-models')`
- Изменить: `HoundApplication` — использовать HoundConfig вместо старых properties

> ⚠️ Если `shared/dali-models/` ещё не создан — создать как минимальный Gradle проект с `java-library` plugin.

**HoundConfig:**

```java
// com/aida/shared/HoundConfig.java
package com.aida.shared;

import java.util.Map;

/**
 * Конфигурация запуска Hound.
 * Живёт в shared/dali-models — shared между Hound и Dali.
 * Передаётся от Dali к Hound при старте parse session.
 */
public record HoundConfig(
    /** SQL диалект. Обязательный. */
    String dialect,              // "plsql" | "postgresql" | "tsql" | "mysql"

    /** Схема назначения в YGG. */
    String targetSchema,         // например "prod"

    /** Режим записи в ArcadeDB. */
    ArcadeWriteMode writeMode,   // default: REMOTE_BATCH

    /** URL ArcadeDB для REMOTE и REMOTE_BATCH. */
    String arcadeUrl,            // "http://localhost:2480"

    /** Имя базы данных в ArcadeDB. */
    String arcadeDatabase,       // "hound"

    /** Имя пользователя ArcadeDB. */
    String arcadeUser,           // "root"

    /** Пароль ArcadeDB. */
    String arcadePassword,       // из .env

    /** Количество worker threads. */
    int workerThreads,           // default: Runtime.getRuntime().availableProcessors()

    /** Строгое разрешение имён (false = tolerate unresolved). */
    boolean strictResolution,    // default: false

    /** Размер батча для REMOTE_BATCH. */
    int batchSize,               // default: 5000

    /** Расширяемые параметры (для диалект-специфичных настроек). */
    Map<String, String> extra    // nullable
) {
    /** Builder-style factory для удобства. */
    public static Builder builder() { return new Builder(); }

    /** Значения по умолчанию для разработки. */
    public static HoundConfig devDefaults(String dialect) {
        return new HoundConfig(
            dialect, "dev", ArcadeWriteMode.REMOTE_BATCH,
            "http://localhost:2480", "hound", "root", "playwithdata",
            Runtime.getRuntime().availableProcessors(),
            false, 5000, null
        );
    }

    public static class Builder {
        private String dialect = "plsql";
        private String targetSchema = "dev";
        private ArcadeWriteMode writeMode = ArcadeWriteMode.REMOTE_BATCH;
        private String arcadeUrl = "http://localhost:2480";
        private String arcadeDatabase = "hound";
        private String arcadeUser = "root";
        private String arcadePassword = "playwithdata";
        private int workerThreads = Runtime.getRuntime().availableProcessors();
        private boolean strictResolution = false;
        private int batchSize = 5000;
        private Map<String, String> extra = null;

        public Builder dialect(String v)           { this.dialect = v; return this; }
        public Builder targetSchema(String v)      { this.targetSchema = v; return this; }
        public Builder writeMode(ArcadeWriteMode v){ this.writeMode = v; return this; }
        public Builder arcadeUrl(String v)         { this.arcadeUrl = v; return this; }
        public Builder arcadeDatabase(String v)    { this.arcadeDatabase = v; return this; }
        public Builder arcadeUser(String v)        { this.arcadeUser = v; return this; }
        public Builder arcadePassword(String v)    { this.arcadePassword = v; return this; }
        public Builder workerThreads(int v)        { this.workerThreads = v; return this; }
        public Builder strictResolution(boolean v) { this.strictResolution = v; return this; }
        public Builder batchSize(int v)            { this.batchSize = v; return this; }
        public Builder extra(Map<String, String> v){ this.extra = v; return this; }

        public HoundConfig build() {
            return new HoundConfig(dialect, targetSchema, writeMode, arcadeUrl,
                arcadeDatabase, arcadeUser, arcadePassword, workerThreads,
                strictResolution, batchSize, extra);
        }
    }
}
```

**ArcadeWriteMode:**

```java
// com/aida/shared/ArcadeWriteMode.java
package com.aida.shared;

public enum ArcadeWriteMode {
    /**
     * Embedded ArcadeDB — только для unit tests.
     * НЕ использовать в production (no mixed embedded/network versions).
     */
    EMBEDDED,

    /**
     * Single-row HTTP запросы к ArcadeDB.
     * UC2b: event-driven, real-time парсинг одного файла.
     */
    REMOTE,

    /**
     * Batch HTTP запросы (5000 записей за транзакцию).
     * UC1: scheduled harvest, UC2a: preview. 13.1× speedup vs REMOTE.
     * Default для Dali.
     */
    REMOTE_BATCH
}
```

**Checklist C.1.2:**
```
[ ] shared/dali-models/ — Gradle проект создан (settings.gradle, build.gradle)
[ ] HoundConfig.java создан с Builder
[ ] ArcadeWriteMode.java создан (3 значения с javadoc)
[ ] libraries/hound/build.gradle: implementation project(':shared:dali-models')
[ ] services/shuttle/build.gradle: implementation project(':shared:dali-models') (для будущего)
[ ] HoundApplication адаптирован к новому HoundConfig
[ ] ./gradlew :shared:dali-models:build → GREEN
[ ] ./gradlew :libraries:hound:test → GREEN
```

---

## C.1.3 — HoundEventListener: Interface

**Оценка:** ~1 день
**Файлы:**
- Создать: `shared/dali-models/src/main/java/com/aida/shared/HoundEventListener.java`
- Изменить: `HoundParserImpl` — вызывать listener events
- Изменить: `FileProcessor` — делегировать в listener

**Интерфейс:**

```java
// com/aida/shared/HoundEventListener.java
package com.aida.shared;

/**
 * Listener для событий парсинга Hound.
 * Реализуется Dali Core для real-time отслеживания прогресса.
 * Все методы вызываются из worker threads — реализация должна быть thread-safe.
 *
 * Принцип: fire-and-forget. Если listener бросает исключение — Hound логирует
 * и продолжает парсинг (listener не должен ломать основной процесс).
 */
public interface HoundEventListener {

    /**
     * Вызывается перед началом парсинга файла.
     * @param file абсолютный путь к файлу
     */
    void onFileParseStarted(String file);

    /**
     * Вызывается после извлечения очередной порции атомов.
     * Может вызываться несколько раз за файл.
     * @param file      путь к файлу
     * @param atomCount количество атомов извлечённых к этому моменту
     */
    void onAtomExtracted(String file, int atomCount);

    /**
     * Вызывается после завершения парсинга файла (успех или ошибка).
     * @param file   путь к файлу
     * @param result результат (success=false если была ошибка)
     */
    void onFileParseCompleted(String file, ParseResult result);

    /**
     * Вызывается при фатальной ошибке парсинга файла.
     * Вызывается ДО onFileParseCompleted.
     * @param file  путь к файлу
     * @param error исключение
     */
    void onError(String file, Throwable error);
}
```

**Adapter (для Dali — минимальная реализация):**

```java
// com/aida/shared/HoundEventListenerAdapter.java
package com.aida.shared;

/**
 * No-op adapter — переопределяй только нужные методы.
 */
public class HoundEventListenerAdapter implements HoundEventListener {
    @Override public void onFileParseStarted(String file) {}
    @Override public void onAtomExtracted(String file, int atomCount) {}
    @Override public void onFileParseCompleted(String file, ParseResult result) {}
    @Override public void onError(String file, Throwable error) {}
}
```

**Интеграция в HoundParserImpl:**

```java
// Обернуть вызовы listener в try-catch:
private void safeNotify(Runnable notification) {
    try {
        notification.run();
    } catch (Exception e) {
        logger.warn("HoundEventListener threw exception — ignoring: {}", e.getMessage());
    }
}

// Использование:
if (listener != null) safeNotify(() -> listener.onFileParseStarted(file.toString()));
```

**Checklist C.1.3:**
```
[ ] HoundEventListener.java создан (4 метода с javadoc)
[ ] HoundEventListenerAdapter.java создан (no-op defaults)
[ ] HoundParserImpl вызывает все 4 события
[ ] Listener exceptions не ломают парсинг (try-catch в safeNotify)
[ ] Тест: listener exceptions не прерывают parseBatch
[ ] ./gradlew :libraries:hound:test → GREEN
```

---

## C.1.4 — Gradle java-library plugin

**Оценка:** ~полдня
**Файлы:** `libraries/hound/build.gradle`

> ⚠️ Этот шаг уже частично выполнен в рамках REPO_MIGRATION_PLAN Phase 2. Проверить перед реализацией.

**Что нужно:**

```groovy
// libraries/hound/build.gradle
plugins {
    id 'java-library'   // ← добавить если нет
    id 'application'    // ← оставить для standalone run
    // остальные плагины без изменений
}

// Изменить dependencies:
// Было:
implementation '...'

// Стало — разделить на api и implementation:
// api — транзитивно доступны потребителям (Dali)
api 'com.arcadedb:arcadedb-network:26.3.2'  // нужен Dali для HoundConfig URLs

// implementation — внутренние, не экспортируются
implementation 'org.antlr:antlr4-runtime:4.13.2'
implementation 'com.arcadedb:arcadedb-engine:26.3.2'  // только если embedded tests
```

**Проверка:**
```bash
# Из aida-root/:
./gradlew :libraries:hound:build
./gradlew :libraries:hound:jar   # должен создать hound-*.jar

# Из services/dali/ (когда создан):
./gradlew :services:dali:compileJava  # должен видеть HoundParser, HoundConfig
```

**Checklist C.1.4:**
```
[ ] java-library plugin в plugins {} блоке
[ ] api vs implementation разделены корректно
[ ] ./gradlew :libraries:hound:build → GREEN (jar создан)
[ ] Standalone: cd libraries/hound && ./gradlew build → GREEN
[ ] IDEA: Open libraries/hound/ → признаётся как standalone Gradle project
```

---

## C.1.5 — REMOTE_BATCH как внутренняя деталь

**Оценка:** ~1 день
**Принцип:** Dali передаёт `writeMode = REMOTE_BATCH`. Детали батчинга (размер, таймауты, retry) — внутри Hound. Наружу не торчит.

**Файлы:**
- Изменить: `src/main/java/com/hound/storage/ArcadeDBSemanticWriter.java`
- Возможно: `src/main/java/com/hound/storage/RemoteWriter.java`

**Текущее состояние (нужно проверить):**
REMOTE_BATCH уже работает как отдельный mode. Задача — убедиться что:
1. `batchSize` берётся из `HoundConfig.batchSize()` (не из hardcode или системного property)
2. Retry логика (если есть) — internal, не конфигурируется снаружи
3. Public API принимает только `ArcadeWriteMode` — никаких `batchSize` параметров в `HoundParser`

**Проверить в `RemoteWriter.java`:**
```java
// Должно быть:
private final int batchSize;  // из config
// Не должно быть:
private final int batchSize = 5000;  // hardcode
// Не должно быть:
private final int batchSize = Integer.parseInt(System.getProperty("hound.batch.size", "5000"));
```

**Если нужно рефакторить:**
```java
// RemoteWriter constructor:
public RemoteWriter(HoundConfig config) {
    this.batchSize = config.batchSize();  // берём из config
    this.arcadeUrl = config.arcadeUrl();
    // ...
}
```

**Checklist C.1.5:**
```
[ ] batchSize из HoundConfig.batchSize() (default 5000)
[ ] Нет системных property для batch конфигурации
[ ] HoundParser API не принимает batchSize параметры
[ ] ./gradlew :libraries:hound:test → GREEN
[ ] Тест: REMOTE_BATCH использует batchSize из config (не hardcode)
```

---

## Итого: время и порядок

| Задача | Оценка | Неделя | Файлов |
|---|---|---|---|
| C.1.0 Bug fixes B1-B7 | ~1 день | W1 Apr 20-25 | 4 файла + тесты |
| C.1.1 HoundParser API | ~1 день | W2 Apr 27 | 3 новых файла |
| C.1.2 HoundConfig POJO | ~полдня | W2 Apr 27 | 2 новых файла + build.gradle |
| C.1.3 HoundEventListener | ~1 день | W2-W3 | 2 новых файла |
| C.1.4 java-library plugin | ~полдня | W2 | build.gradle |
| C.1.5 REMOTE_BATCH internal | ~1 день | W3 | 1-2 файла |
| **Итого** | **~5.5 дней** | **W1-W3** | |

**Главный acceptance test после C.1.1-1.5:**
```java
// Этот код должен работать из Dali (когда будет создан):
HoundConfig config = HoundConfig.builder()
    .dialect("plsql")
    .writeMode(ArcadeWriteMode.REMOTE_BATCH)
    .arcadeUrl("http://localhost:2480")
    .build();

HoundParser parser = new HoundParserImpl();
List<ParseResult> results = parser.parseBatch(
    List.of(Path.of("/sql/procedure.sql")),
    config,
    new HoundEventListenerAdapter() {
        @Override
        public void onAtomExtracted(String file, int count) {
            System.out.println("Extracted: " + count);
        }
    }
);
results.forEach(r -> System.out.println(r.file() + ": " + r.atomCount() + " atoms"));
```
