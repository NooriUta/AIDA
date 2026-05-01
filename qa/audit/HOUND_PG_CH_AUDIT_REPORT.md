# HOUND PostgreSQL + ClickHouse Parser Audit Report

**Sprint:** SPRINT_HOUND_PG_CH_AUDIT  
**Branch:** `audit/hound-pg-ch-parser`  
**Дата старта:** 2026-05-01  
**Статус:** 🟡 IN PROGRESS

---

## Scope

Систематическая проверка PostgreSQL и ClickHouse listener'ов на дефекты, аналогичные найденным в PL/SQL во время `SPRINT_HOUND_PARSER_HARDENING`:
1. Preprocessor false-positives (PL/SQL: 2 бага в SQL*Plus stripping → удалён preprocessor)
2. Text-extraction heuristics в context handlers (`getText().contains()` вместо typed token access)
3. Grammar coverage gaps на production fixture

---

## Executive summary

| Item | PG | CH |
|---|---|---|
| **A. Preprocessing** | ✅ CLEAN — no preprocessor (verified `HoundParserImpl.java:486-501`) | ✅ CLEAN — no preprocessor (verified `:502-517`) |
| **B.2. Text-extraction (typed-rule violations)** | ✅ FIXED — 1 bug в `enterJoin_type` | ✅ FIXED — 1 bug в `enterJoinOpLeftRight` |
| **B.3. Production fixture coverage** | ⏳ PENDING — ждёт fixtures от пользователя | ⏳ PENDING — ждёт fixtures от пользователя |
| **D. Grammar files** | ℹ️ DOCUMENTED (PG MIT — Oleksii Kovalov, 2021-2023) | ℹ️ DOCUMENTED (CH без attribution) |
| **E. Dialect features** | ⏳ verify-by-test после B.3 | ⏳ verify-by-test после B.3 |
| **F. Error events emit** | ✅ FIXED (stubfix PR #88) — AntlrErrorCollector wired | ✅ FIXED (stubfix PR #88) |

---

## B.2 Findings — text-extraction heuristics in context handlers

### PG-AUDIT-02-01 — `enterJoin_type` использовал `getText().toUpperCase().contains()`

**Файл:** `libraries/hound/src/main/java/com/hound/semantic/dialect/postgresql/PostgreSQLSemanticListener.java`  
**Severity:** MEDIUM (стабильно работало, но фрагильно к whitespace/комментариям и обходило грамматику)

**До (anti-pattern):**
```java
String text = ctx.getText().toUpperCase();
if (text.contains("FULL"))        pendingJoinType = "FULL";
else if (text.contains("LEFT"))   pendingJoinType = "LEFT";
else if (text.contains("RIGHT"))  pendingJoinType = "RIGHT";
else if (text.contains("CROSS"))  pendingJoinType = "CROSS";  // dead code: CROSS не в join_type
else                              pendingJoinType = "INNER";
```

**После (typed):**
```java
// Grammar (PostgreSQLParser.g4:3231): join_type : (FULL | LEFT | RIGHT | INNER_P) OUTER_P? ;
if      (ctx.FULL()    != null) pendingJoinType = "FULL";
else if (ctx.LEFT()    != null) pendingJoinType = "LEFT";
else if (ctx.RIGHT()   != null) pendingJoinType = "RIGHT";
else if (ctx.INNER_P() != null) pendingJoinType = "INNER";
else                            pendingJoinType = "INNER";
```

**Покрытие тестом:** `PostgreSQLSemanticListenerTest.PG-JTYPE-1..4` (LEFT / RIGHT OUTER / FULL OUTER / INNER).

### CH-AUDIT-02-01 — `enterJoinOpLeftRight` использовал `getText().toUpperCase().contains()`

**Файл:** `libraries/hound/src/main/java/com/hound/semantic/dialect/clickhouse/ClickHouseSemanticListener.java`  
**Severity:** MEDIUM

**До:**
```java
String text = ctx.getText().toUpperCase();
if (text.contains("LEFT"))       pendingJoinType = "LEFT";
else if (text.contains("RIGHT")) pendingJoinType = "RIGHT";
else                             pendingJoinType = "LEFT";
```

**После:**
```java
// Grammar (ClickHouseParser.g4:474-477): JoinOpLeftRight has (LEFT | RIGHT) token.
if      (ctx.LEFT()  != null) pendingJoinType = "LEFT";
else if (ctx.RIGHT() != null) pendingJoinType = "RIGHT";
else                          pendingJoinType = "LEFT";
```

**Покрытие тестом:** `ClickHouseSemanticListenerTest.CH-JTYPE-1..3` (LEFT / RIGHT OUTER / FULL OUTER).

### PG/CH — другие `getText()` вызовы

**Аудит 2026-05-01:** все остальные `getText()` вызовы в обоих listener'ах вызываются:
- На **leaf rules** (`identifier()`, `qualified_name()`, `colid()`, `attr_name()`, `databaseIdentifier()`, `tableIdentifier()`, `columnTypeExpr()`, `colLabel()`, `bareColLabel()`, `name()`, `func_name()`, `typename()`) — допустимо ✅
- На **composite ctx для payload** (atom expression text → `onAtom`; join condition text → `pendingJoinCondition`; logging snippet) — допустимо ✅

Других structural-text-match антипаттернов **не обнаружено**.

---

## B.3 Production fixture coverage — TBD

**🛑 PAUSE-POINT:** ждёт production fixtures от пользователя (analog `PKG_ETL_FACT_FINANCE.sql` для PL/SQL). Ожидаемое содержимое:
- **PG:** PL/pgSQL функции + RECURSIVE CTE + CREATE TYPE + LATERAL + RETURNS TABLE + custom types
- **CH:** MATERIALIZED VIEW + Distributed engine + ARRAY JOIN + dictGet + Nested types

**После получения fixtures:**
- Положить в `libraries/hound/src/test/resources/sql/postgresql/` и `libraries/hound/src/test/resources/sql/clickhouse/`
- Создать comprehensive tests `PostgresPackageTest.java` (`@Tag("postgresql_parse")`) + `ClickHouseEtlTest.java` (`@Tag("clickhouse_parse")`)
- Acceptance: ≤ 5 recoverable ANTLR errors на production fixture; cross-schema lineage работает; routines/queries регистрируются

---

## E. Dialect features verify-by-test — TBD (after B.3)

Известные ограничения (документировать после прогона на production fixture):
- **CH:** `dictGet` — generic functionCall fallback (не специальная обработка)
- **CH:** `Distributed` engine — UNCLEAR без fixture
- **PG:** `LANGUAGE plpgsql` body parsing — depth coverage UNCLEAR без fixture

---

## Commits in this audit branch

| SHA | Description |
|---|---|
| (TBD) | feat(hound): typed JOIN type detection in PG and CH listeners |
| (TBD) | docs(audit): HOUND_PG_CH_AUDIT report skeleton |

---

## Related

- [SPRINT_HOUND_PG_CH_AUDIT](../../docs/current/sprints/SPRINT_HOUND_PG_CH_AUDIT.md) — sprint doc
- [SPRINT_HOUND_PARSER_HARDENING](../../docs/archive/sprints/SPRINT_HOUND_PARSER_HARDENING.md) — predecessor (PL/SQL)
- [feedback_typed_grammar_rules.md](../../C--AIDA-aida-root/memory/feedback_typed_grammar_rules.md) — typed-rule access standard
