# Hound — Public API Design (`com.hound.api`)

> Status: **STABLE** — Sprint C.1 (13.04.2026)  
> Implements: Q27 closed in `DECISIONS_LOG.md`

---

## Overview

Hound is a PL/SQL semantic parser that analyses SQL source files and extracts
structured lineage: tables, columns, routines, and data-flow edges.

Prior to C.1, Hound was a standalone CLI application (`HoundApplication.main()`).
C.1 refactors it into a **Java library** (`id 'java-library'`) with a clean public
API package (`com.hound.api`) so that Dali and other services can import it as a
Gradle dependency:

```gradle
implementation project(':libraries:hound')
```

---

## Package `com.hound.api`

### `HoundParser` (interface)

```java
public interface HoundParser {
    ParseResult parse(Path file, HoundConfig config);
    ParseResult parse(Path file, HoundConfig config, HoundEventListener listener);
    List<ParseResult> parseBatch(List<Path> files, HoundConfig config);
    List<ParseResult> parseBatch(List<Path> files, HoundConfig config, HoundEventListener listener);
}
```

Implemented by `com.hound.HoundParserImpl`.  
Thread-safe — each parse call creates its own ANTLR/semantic engine instances.  
`parseBatch` uses a two-phase strategy: parallel ANTLR parse + sequential DB write.

### `HoundConfig` (record)

All 11 fields with their defaults:

| Field             | Type                   | Default                  | Notes                              |
|-------------------|------------------------|--------------------------|------------------------------------|
| `dialect`         | `String`               | (required)               | `"plsql"` (only supported dialect) |
| `targetSchema`    | `String`               | `null`                   | Namespace isolation in YGG         |
| `writeMode`       | `ArcadeWriteMode`      | `DISABLED`               | See `ArcadeWriteMode` enum         |
| `arcadeUrl`       | `String`               | `null`                   | `http://host:port`                 |
| `arcadeDbName`    | `String`               | `"hound"`                | ArcadeDB database name             |
| `arcadeUser`      | `String`               | `"root"`                 |                                    |
| `arcadePassword`  | `String`               | `null`                   |                                    |
| `workerThreads`   | `int`                  | `Runtime.availableProcessors()` | Batch parallelism           |
| `strictResolution`| `boolean`              | `false`                  | Soft-fail on unresolved references |
| `batchSize`       | `int`                  | `5000`                   | Remote batch flush threshold       |
| `extra`           | `Map<String, String>`  | `{}`                     | Extensibility; defensively copied  |

**Factory methods:**

```java
HoundConfig.defaultDisabled("plsql")                              // no DB write
HoundConfig.defaultRemoteBatch("plsql", "http://db:2480")         // REMOTE_BATCH
HoundConfig.defaultRemote("plsql", "http://db:2480")              // REMOTE
cfg.withTargetSchema("MY_SCHEMA")
cfg.withCredentials("dbname", "user", "password")
cfg.withWorkerThreads(4)
```

### `ArcadeWriteMode` (enum)

| Value           | Description                                     |
|-----------------|-------------------------------------------------|
| `DISABLED`      | Parse only — nothing written to ArcadeDB        |
| `REMOTE`        | Write each record immediately via HTTP API      |
| `REMOTE_BATCH`  | Accumulate to `batchSize`, then flush           |
| `EMBEDDED`      | Reserved (embedded ArcadeDB engine, disabled)   |

### `ParseResult` (record)

```java
public record ParseResult(
    String file,           // absolute path
    int atomCount,         // semantic atoms extracted (deduplicated)
    int vertexCount,       // structure vertices (tables + columns + routines + …)
    int edgeCount,         // lineage edges
    double resolutionRate, // [0.0, 1.0] — resolved / total column references
    List<String> warnings,
    List<String> errors,
    long durationMs
) {
    public boolean isSuccess() { return errors.isEmpty(); }
}
```

### `HoundEventListener` (interface, all methods default no-op)

```java
public interface HoundEventListener {
    default void onFileParseStarted(String file, String dialect) {}
    default void onAtomExtracted(String file, int atomCount, String atomType) {}
    default void onRecordRegistered(String file, String varName) {}
    default void onFileParseCompleted(String file, ParseResult result) {}
    default void onError(String file, Throwable error) {}
}
```

**Event ordering guarantee:** `onFileParseStarted` → `onAtomExtracted`* → `onFileParseCompleted`.
`onError` fires only if an exception occurs (instead of `onFileParseCompleted`).

### `NoOpHoundEventListener`

```java
public final class NoOpHoundEventListener implements HoundEventListener {
    public static final NoOpHoundEventListener INSTANCE = new NoOpHoundEventListener();
}
```

Used as the default when no listener is provided.

---

## Wire-up in `HoundParserImpl`

```
HoundParserImpl
  └─ analyzeFile(file, config, listener)
       └─ UniversalSemanticEngine(listener, file)
            ├─ AtomProcessor(listener, file)     → fires onAtomExtracted
            └─ StructureAndLineageBuilder(listener, file) → fires onRecordRegistered
```

The listener is propagated through the constructor chain; no static state.

---

## Backward compatibility

`HoundApplication` (CLI entry point) continues to work via `RunConfig.toHoundConfig()`.
`./gradlew :libraries:hound:run` is unchanged.

---

## Tests

| Test class                        | Tests | What it covers                                  |
|-----------------------------------|-------|-------------------------------------------------|
| `HoundConfigTest`                 | 9     | Factory methods, immutability, defensive copy   |
| `HoundParserImplTest`             | 6     | parse/parseBatch, listener callbacks            |
| `HoundEventListenerOrderTest`     | 2     | Event ordering: started → atoms* → completed   |

All tests run against real `.pck` fixtures in `src/test/resources/plsql/`.
