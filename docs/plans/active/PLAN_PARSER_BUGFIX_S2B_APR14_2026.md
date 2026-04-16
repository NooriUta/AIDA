# Parser Bugfix Sprint — S2-B

> **Sprint:** S2-B | **Дата:** Apr 14, 2026 | **Статус:** IN PROGRESS
>
> Устранить два регрессионных бага парсера, выявленных при анализе
> `LOAD_FX_RATES` из `PKG_ETL_08_TREASURY.sql`.
> Работа ведётся параллельно с S2 (KI-DDL-1, KI-RETURN-1, …).

---

## Диагностический тест

**Файл:** `libraries/hound/src/test/java/com/hound/semantic/LoadFxRatesGeoidTest.java`

Тест создан 14.04.2026. Содержит полный текст процедуры `LOAD_FX_RATES` как
константу и 6 assertions:

| # | Тест | Что проверяет |
|---|------|---------------|
| T1 | `procedureIsRegistered` | Routine `LOAD_FX_RATES` зарегистрирована |
| T2 | `allTopLevelUpdatesMustCarryRoutineGeoid` | Оба UPDATE имеют `routineGeoid` ≠ null и geoid с префиксом routine |
| T3 | `noBareUpdateGeoidExists` | Нет голых geoid вида `UPDATE:\d+` |
| T4 | `parametersNotClassifiedAsColumns` | `p_rate_date` / `p_rate_type` не создают DaliColumn |
| T5 | `forallIndexVariableNotClassifiedAsColumn` | Индекс FORALL `i` не создаёт DaliColumn `.I` |
| T6 | `insertAndMergeHaveIndependentGeoids` | MERGE не является потомком INSERT |

Вспомогательный метод `dumpAllStatements()` печатает таблицу всех statement'ов
(sorted by lineStart) — тип, routineGeoid, geoid — для диагностики при падении.

---

## Bug A — FORALL index variable как DaliColumn

### Симптом

```
DaliColumn: DWH.STG_FX_RATES.I  ← spurious vertex
```

FORALL-переменная `i` в `FORALL i IN 1..v_rate_id.COUNT INSERT INTO ... VALUES
(v_rate_id(i), ...)` резолвилась как колонка целевой таблицы.

### Root cause

Отсутствовал handler `enterForall_statement`. Переменная `i` встречалась
как 1-токенный `atom` → `is_column_reference = true` → `resolveImplicitTable`
→ создавала `DWH.STG_FX_RATES.I`.

### Исправление ✅ (14.04.2026)

**Файл:** `libraries/hound/src/main/java/com/hound/semantic/dialect/plsql/PlSqlSemanticListener.java`

Добавлен handler после `exitCursor_loop_param`:

```java
@Override
public void enterForall_statement(PlSqlParser.Forall_statementContext ctx) {
    if (ctx == null || ctx.index_name() == null) return;
    String indexVar = BaseSemanticListener.cleanIdentifier(ctx.index_name().getText());
    if (!indexVar.isEmpty()) {
        base.onRoutineVariable(indexVar, "PLS_INTEGER");
    }
}
```

Регистрация index-переменной как `PLS_INTEGER` переменной рутины
предотвращает вход в `resolveImplicitTable`.

### Верификация

- T5 `forallIndexVariableNotClassifiedAsColumn` — должен быть GREEN
- Запустить `LoadFxRatesGeoidTest`

---

## Bug B — UPDATE:1307 — голый geoid без routine-префикса

### Симптом

UPDATE-statement в строке ~1307 процедуры получал geoid `UPDATE:1307`
вместо корректного `LOAD_FX_RATES:UPDATE:1307`.

Означает: `routineGeoid = null` в момент `onStatementEnter("UPDATE", ...)`,
т.е. `ScopeManager.currentRoutine()` возвращал `null` — вершина стека не
содержала routineGeoid.

### Root cause (гипотезы — требуют рантайм-диагностики)

Три возможных причины потери routine-контекста:

| # | Гипотеза | Вероятность |
|---|----------|-------------|
| H1 | Несбалансированный push/pop в одном из statement-handlers до UPDATE:1307 | Высокая |
| H2 | Anonymous PL/SQL block (строки 1198–1231) не имеет обработчика → его BEGIN/DECLARE/END сбрасывают `current`-HashMap | Средняя |
| H3 | `exitStatement()` ELSE-ветка (нет parentStmt) очищает `in_dml_target` и другие поля при выходе из неожиданного scope | Средняя |

### Трассировка scope до UPDATE:1307

Ниже перечислены все предполагаемые push/pop до строки 1307:

```
enterProcedure_body(LOAD_FX_RATES)          → push ROUTINE(LOAD_FX_RATES)      [depth=1]

# Cursor c_fx_rates (строки 946-1078)
enterCursor_declaration                     → push CURSOR scope               [depth=2]
  enterSelect_statement (WITH ...)          → push SELECT scope               [depth=3]
    ... CTEs, scalar subqueries ...         → nested push/pop balanced
  exitSelect_statement                      → pop SELECT                      [depth=2]
exitCursor_declaration                      → pop CURSOR                      [depth=1]

# FORALL INSERT (1103-1118) — НЕТ handler у FORALL самого
enterInsert_statement (строка 1103)         → push INSERT                     [depth=2]
exitInsert_statement                        → pop INSERT                      [depth=1]

# MERGE (1125-1189)
enterMerge_statement                        → push MERGE                      [depth=2]
  enterSelected_tableview (USING SELECT)   → push SELECT scope               [depth=3]
    ... 4 scalar subqueries ...            → nested balanced
  exitSelect_statement                     → pop SELECT                      [depth=2]
exitMerge_statement                        → pop MERGE                       [depth=1]

# Anonymous block (1198-1231) ← ПОДОЗРИТЕЛЬНАЯ ЗОНА
# Нет handler для enterBlock_statement / enterAnonymous_block
# SELECT statements внутри: 3 штуки
enterSelect_statement × 3                  → push × 3 → pop × 3             [depth stays 1]

# INSERT...SELECT (1233-1276)
enterInsert_statement                       → push INSERT                     [depth=2]
  enterSelect_statement (IS NOT EXISTS)    → push SELECT                     [depth=3]
    enterSubquery (NOT EXISTS)             → push SUBQUERY                   [depth=4]
    exitSubquery                           → pop                             [depth=3]
  exitSelect_statement                     → pop                             [depth=2]
exitInsert_statement                       → pop                             [depth=1]

# UPDATE 1 (1281-1305) — ожидается корректный
enterUpdate_statement                       → push UPDATE                     [depth=2]
  scalar subqueries: nested balanced
exitUpdate_statement                       → pop UPDATE                      [depth=1]

# UPDATE 2 (1307-1321) ← ЗДЕСЬ ДОЛЖЕН БЫТЬ routineGeoid
enterUpdate_statement                       → routineGeoid = currentRoutine() = ???
```

### Статус

**OPEN** — требует запуска `LoadFxRatesGeoidTest`.

### Следующие шаги

1. Запустить тест: `./gradlew :libraries:hound:test --tests "com.hound.semantic.LoadFxRatesGeoidTest"`
2. Если T3 (`noBareUpdateGeoidExists`) красный — прочитать вывод `dumpAllStatements()`
3. Найти первый statement с `routineGeoid=null` в дампе
4. Определить, какой handler нарушил баланс push/pop
5. Исправить

### Кандидат-исправление (anonymous block)

Если рантайм-диагностика укажет на анонимный блок (строки 1198–1231),
добавить в `PlSqlSemanticListener.java`:

```java
@Override
public void enterBlock_statement(PlSqlParser.Block_statementContext ctx) {
    // Anonymous DECLARE...BEGIN...END block inside procedure body
    // Does NOT push its own statement scope (no lineage semantics),
    // but needs special tracking if it fires exitStatement unexpectedly.
}
```

Либо — если проблема в несбалансированном USUBQUERY для scalar subquery
в UPDATE SET clause — добавить guard в `exitSubquery`:

```java
// Ensure saved_dml_target_for_subquery is set even for USUBQUERY paths
if (current.get("saved_dml_target_for_subquery") == null)
    current.put("saved_dml_target_for_subquery", current.get("in_dml_target"));
```

---

## Зависимости от S2 KI-items

Bug B блокирует верификацию KI-DDL-1 и KI-RETURN-1, поскольку любые
statement'ы без routineGeoid дадут некорректные geoid'ы для DDL-edges и
RETURNS_INTO.

Bug A уже исправлен — не блокирует S2.

---

## История изменений

| Дата | Что |
|------|-----|
| 2026-04-14 | v1.0 — план создан; Bug A исправлен (`enterForall_statement`); тест создан |
