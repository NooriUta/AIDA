# SKADI Source Specification

**Version:** 1.0  
**Date:** 2026-04-20  
**Status:** ✅ Released (v1.2.0)

---

## Overview

SKADI is the SQL-fetching layer used by Dali's `FileParseJob` to pull stored procedures, functions, packages, and views from remote database sources via JDBC, prior to parsing by the Hound library.

---

## 1. Standard JDBC Grants per Dialect

### Oracle / PL-SQL

Minimum privileges required for Dali to harvest:

```sql
CREATE USER dali_harvest IDENTIFIED BY <password>;
GRANT CREATE SESSION TO dali_harvest;
GRANT SELECT ANY DICTIONARY TO dali_harvest;
GRANT SELECT_CATALOG_ROLE TO dali_harvest;

-- Or individually:
GRANT SELECT ON ALL_SOURCE TO dali_harvest;
GRANT SELECT ON ALL_PROCEDURES TO dali_harvest;
GRANT SELECT ON ALL_OBJECTS TO dali_harvest;
GRANT SELECT ON ALL_DEPENDENCIES TO dali_harvest;
```

### PostgreSQL

```sql
-- Grant connect on the target database
GRANT CONNECT ON DATABASE <db_name> TO dali_harvest;
-- Grant usage on target schemas
GRANT USAGE ON SCHEMA <schema_name> TO dali_harvest;
-- System catalog access (usually public by default)
GRANT SELECT ON information_schema.routines TO dali_harvest;
GRANT SELECT ON information_schema.routine_definition TO dali_harvest;
```

### ClickHouse

```sql
CREATE USER dali_harvest IDENTIFIED WITH sha256_password BY '<password>';
GRANT SELECT ON system.functions TO dali_harvest;
GRANT SELECT ON system.tables TO dali_harvest;
GRANT SELECT ON system.columns TO dali_harvest;
```

---

## 2. Default Schema Exclusion Lists

Schemas excluded from harvest by default (system schemas that contain no business SQL):

```java
// SourceConfig.java — DEFAULT_EXCLUSIONS
Map.of(
  "oracle", List.of(
    "SYS", "SYSTEM", "DBSNMP", "OUTLN", "MDSYS", "ORDSYS",
    "XDB", "WMSYS", "CTXSYS", "ANONYMOUS", "APPQOSSYS",
    "MGMT_VIEW", "EXFSYS", "DMSYS", "OJVMSYS", "GSMADMIN_INTERNAL"
  ),
  "postgresql", List.of(
    "information_schema", "pg_catalog", "pg_toast", "pg_temp"
  ),
  "clickhouse", List.of(
    "system", "information_schema", "INFORMATION_SCHEMA"
  )
)
```

---

## 3. Per-Source Schema Filter Structure

### SchemaFilter record

```java
record SchemaFilter(
    List<String> include,  // whitelist — empty = all except excluded
    List<String> exclude   // blacklist — defaults to DEFAULT_EXCLUSIONS[dialect]
) {
    static SchemaFilter defaults(String dialect) {
        return new SchemaFilter(
            List.of(),
            DEFAULT_EXCLUSIONS.getOrDefault(dialect, List.of())
        );
    }
}
```

### Storage in FRIGG (ArcadeDB vertex `DaliSource`)

| Property | Type | Example |
|----------|------|---------|
| `schemaFilterInclude` | STRING (JSON array) | `["HR","FINANCE"]` or `""` |
| `schemaFilterExclude` | STRING (JSON array) | `["SYS","SYSTEM",...]` |

Empty `include` means "all schemas except excluded."

---

## 4. Fetch Flow

```
HarvestJob.execute(sessionId)
  └─ for each DaliConfig.Source:
       BackgroundJob.enqueue(FileParseJob.execute(...))

FileParseJob.execute(sessionId, sourceName, dialect, jdbcUrl, workerIndex)
  1. SkadiFetcher.fetchScripts(SkadiFetchConfig)   ← JDBC pull
  2. List<SqlSource> = fetchResult.files()          ← file paths
  3. HoundConfig (dialect, schema, ArcadeDB target)
  4. HoundParser.parseSources(sqlSources, houndCfg) ← in-JVM parse
  5. DaliHeimdallEmitter.emit(JOB_COMPLETED, ...)   ← WS event
```

---

## 5. Source Modes

| Mode | Trigger | Endpoint | Scheduler |
|------|---------|----------|-----------|
| **UC-1a: SKADI Harvest (JDBC)** | API / cron | `POST /sessions` | `HarvestScheduler` (`0 0 2 * * ?`) |
| **UC-1b: File Upload** | Manual | `POST /api/sessions/upload` | none |

Both modes are independent; JDBC sources are stored persistently in FRIGG (`DaliSource`), file uploads are one-shot.

---

## 6. Cron Configuration

```properties
# Production: nightly at 02:00
dali.harvest.cron=0 0 2 * * ?

# Dev override: every 5 minutes
%dev.dali.harvest.cron=0 */5 * * * ?

# Test: disabled
%test.dali.harvest.cron=off
```

---

## 7. Retry Policy

- `HarvestJob` — `@Job(retries = 0)` (parent, non-retryable — re-trigger via API)
- `FileParseJob` — `@Job(retries = 2)` (per-source, retries 2× on exception)
- SKADI fetch is idempotent — safe to retry

---

## 8. Supported File Extensions

`.sql` `.pck` `.prc` `.pkb` `.pks` `.fnc` `.trg` `.vw` `.zip` `.rar`
