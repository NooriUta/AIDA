# Plan: PostgreSQL + ClickHouse Semantic Listeners

## ⚡ Шаг 0 — Git: ветка и документация (ПЕРЕД любыми правками кода)

```bash
# 1. Убедиться что не в master (текущая: fix/arcadedb-mvcc-batch-size)
git branch --show-current

# 2. Создать новую feature-ветку от master
git checkout master
git pull origin master
git checkout -b feature/hound-pg-ch-semantic-listeners

# 3. Убедиться что документация маппинга уже в репозитории (создана до кода)
# Файлы уже существуют (создано в pre-plan фазе):
#   docs/architecture/hound/PGSQL_GRAMMAR_MAPPING.md
#   docs/architecture/hound/CLICKHOUSE_GRAMMAR_MAPPING.md

# 4. Сохранить план в docs/sprints/
cp ~/.claude/plans/cozy-spinning-pike.md docs/sprints/hound-pg-ch-semantic-listeners.md
git add docs/
git commit -m "docs: add PostgreSQL + ClickHouse grammar mapping docs and sprint plan"
```

> ❗ **Правило**: никогда не коммитить в `master` напрямую. Все изменения — только в `feature/*`.

---

## Документация (выполнено)

- ✅ `docs/architecture/hound/PGSQL_GRAMMAR_MAPPING.md` — 23 секции, маппинг PL/SQL → PostgreSQL
- ✅ `docs/architecture/hound/CLICKHOUSE_GRAMMAR_MAPPING.md` — 23 секции, маппинг PL/SQL → ClickHouse

---

## Context

Реализовать два семантических слушателя поверх существующего `UniversalSemanticEngine` + 5 компонентов.
Образец: `PlSqlSemanticListener` (composition pattern, `private final BaseSemanticListener base`).
Цель: resolution rate ≥ 70% на SQL-корпусах обоих диалектов. Блокирует Correctness Story.

**Dialect keys:** `"postgresql"` (уже в ParserFactory/ParserRegistry), `"clickhouse"` (уже в ParserFactory/ParserRegistry).  
**Start rules:** PostgreSQL = `root`, ClickHouse = `clickhouseFile` (❗ ParserRegistry ошибочно регистрирует `"statement"` — UniversalParser имеет fallback).

---

## Файлы: что создать и изменить

### Новые файлы (6 штук)

```
libraries/hound/src/main/java/com/hound/semantic/dialect/postgresql/
  PostgreSQLDialectAdapter.java       ~20 строк — реализует DialectAdapter
  PostgreSQLSemanticListener.java     ~600 строк — extends PostgreSQLParserBaseListener
  PostgreSQLTokenMapper.java          ~120 строк — token → CanonicalTokenType

libraries/hound/src/main/java/com/hound/semantic/dialect/clickhouse/
  ClickHouseDialectAdapter.java       ~20 строк — реализует DialectAdapter
  ClickHouseSemanticListener.java     ~500 строк — extends ClickHouseParserBaseListener
  ClickHouseTokenMapper.java          ~100 строк — token → CanonicalTokenType
```

### Изменяемые файлы (2 штуки)

```
libraries/hound/src/main/java/com/hound/semantic/dialect/DialectRegistry.java
libraries/hound/src/main/java/com/hound/HoundParserImpl.java
```

---

## Шаг 0 — Общая инфраструктура (сделать первым)

### DialectRegistry.java

Файл: `libraries/hound/src/main/java/com/hound/semantic/dialect/DialectRegistry.java`

Заменить закомментированный stub PostgreSQL (`"pgsql"`, неверный ключ) и добавить ClickHouse:

```java
// PostgreSQL
try {
    Class<?> c = Class.forName("com.hound.semantic.dialect.postgresql.PostgreSQLDialectAdapter");
    register("postgresql", (DialectAdapter) c.getDeclaredConstructor().newInstance());
} catch (Exception e) { logger.warn("PostgreSQLDialectAdapter not available: {}", e.getMessage()); }

// ClickHouse
try {
    Class<?> c = Class.forName("com.hound.semantic.dialect.clickhouse.ClickHouseDialectAdapter");
    register("clickhouse", (DialectAdapter) c.getDeclaredConstructor().newInstance());
} catch (Exception e) { logger.warn("ClickHouseDialectAdapter not available: {}", e.getMessage()); }
```

### HoundParserImpl.java

Файл: `libraries/hound/src/main/java/com/hound/HoundParserImpl.java`

В `createDialectListener()`:
```java
case "postgresql" -> {
    PostgreSQLSemanticListener l = new PostgreSQLSemanticListener(engine);
    if (defaultSchema != null) l.setDefaultSchema(defaultSchema);
    yield l;
}
case "clickhouse" -> {
    ClickHouseSemanticListener l = new ClickHouseSemanticListener(engine);
    if (defaultSchema != null) l.setDefaultSchema(defaultSchema);
    yield l;
}
```

В `parseAndWalk()`:
```java
case "postgresql" -> {
    PostgreSQLLexer lexer = new PostgreSQLLexer(CharStreams.fromString(sql));
    PostgreSQLParser parser = new PostgreSQLParser(new CommonTokenStream(lexer));
    ParseTreeWalker.DEFAULT.walk((ParseTreeListener) listener, parser.root());
    yield new ParseOutcome(List.of(), List.of());
}
case "clickhouse" -> {
    ClickHouseLexer lexer = new ClickHouseLexer(CharStreams.fromString(sql));
    ClickHouseParser parser = new ClickHouseParser(new CommonTokenStream(lexer));
    ParseTreeWalker.DEFAULT.walk((ParseTreeListener) listener, parser.clickhouseFile());
    yield new ParseOutcome(List.of(), List.of());
}
```

---

## Шаг 1 — DialectAdapter (шаблон одинаков)

### PostgreSQLDialectAdapter.java

```java
package com.hound.semantic.dialect.postgresql;

public class PostgreSQLDialectAdapter implements DialectAdapter {
    @Override
    public String getDialectName() { return "postgresql"; }

    @Override
    public ParseTreeListener createListener(UniversalSemanticEngine engine) {
        return new PostgreSQLSemanticListener(engine);
    }
}
```

### ClickHouseDialectAdapter.java — аналогично, ключ `"clickhouse"`.

---

## Шаг 2 — TokenMapper

### PostgreSQLTokenMapper.java

```java
public static CanonicalTokenType map(String tokenName) {
    CanonicalTokenType result = DIRECT.get(tokenName);
    if (result != null) return result;
    if (tokenName.contains("Identifier")) return IDENTIFIER;
    if (tokenName.contains("Constant") || tokenName.contains("Const")) return STRING_LITERAL;
    return UNKNOWN;
}
```

Ключевые маппинги:

| PostgreSQL token | CanonicalTokenType |
|---|---|
| `Identifier` | `IDENTIFIER` |
| `QuotedIdentifier`, `UnicodeQuotedIdentifier` | `QUOTED_IDENTIFIER` |
| `StringConstant`, `BeginEscapeStringConstant`, `UnicodeEscapeStringConstant`, `DollarQuotedStringConstant` | `STRING_LITERAL` |
| `Integral`, `BinaryIntegral`, `OctalIntegral`, `HexadecimalIntegral` | `INTEGER_LITERAL` |
| `Numeric` | `NUMERIC_LITERAL` |
| `DOT` | `PERIOD` |
| `OPEN_PAREN` | `LEFT_PAREN` |
| `CLOSE_PAREN` | `RIGHT_PAREN` |
| `COMMA` | `COMMA` |
| `PARAM` | `BIND_VARIABLE` |
| `NULL_P` | `NULL` |
| `TRUE_P` | `TRUE` |
| `FALSE_P` | `FALSE` |
| `TYPECAST` | `OPERATOR` |
| `PLUS`,`MINUS`,`STAR`,`SLASH`,`EQUAL`,`NOT_EQUALS`,`LT`,`GT`,`LTH`,`GTH` | `OPERATOR` |
| `AND`,`OR`,`NOT`,`IN_P`,`IS`,`BETWEEN`,`LIKE`,`ILIKE`,`CASE`,`WHEN`,`THEN`,`ELSE`,`END_P`,`EXISTS` | `SQL_KEYWORD` |
| `CURRENT_DATE` | `CURRENT_DATE` |

### ClickHouseTokenMapper.java

| ClickHouse token | CanonicalTokenType |
|---|---|
| `IDENTIFIER` | `IDENTIFIER` |
| `DECIMAL_LITERAL`, `OCTAL_LITERAL`, `HEXADECIMAL_NUMERIC_LITERAL`, `BINARY_NUMERIC_LITERAL` | `INTEGER_LITERAL` |
| `FLOATING_LITERAL` | `NUMERIC_LITERAL` |
| `STRING_LITERAL`, `HEXADECIMAL_STRING_LITERAL`, `BINARY_STRING_LITERAL` | `STRING_LITERAL` |
| `DOT` | `PERIOD` |
| `LPAREN` | `LEFT_PAREN` |
| `RPAREN` | `RIGHT_PAREN` |
| `COMMA` | `COMMA` |
| `NULL_SQL` | `NULL` |
| `JSON_TRUE` | `TRUE` |
| `JSON_FALSE` | `FALSE` |
| `INF`, `NAN_SQL` | `NUMERIC_LITERAL` |
| `DOUBLE_COLON`, `ARROW`, `ASTERISK`, `SLASH`, `DASH`, `PLUS`, `PERCENT`, `CONCAT`, `EQ_SINGLE`, `EQ_DOUBLE`, `NOT_EQ`, `LE`, `GE`, `LT`, `GT` | `OPERATOR` |
| `AND`,`OR`,`NOT`,`IN`,`IS`,`LIKE`,`ILIKE`,`BETWEEN`,`CASE`,`WHEN`,`THEN`,`ELSE`,`END`,`GLOBAL`,`ASOF`,`SEMI`,`ANTI` | `SQL_KEYWORD` |

---

## Шаг 3 — SemanticListener (обе реализации, P0 → P3)

### Шаблон класса (одинаков для обоих)

```java
public class PostgreSQLSemanticListener extends PostgreSQLParserBaseListener {
    private static final Logger logger = LoggerFactory.getLogger(PostgreSQLSemanticListener.class);
    private final BaseSemanticListener base;

    // State fields:
    private final Set<PostgreSQLParser.Simple_select_pramaryContext> suppressedSelects = new HashSet<>();
    private boolean inCteDefinition = false;
    private String  pendingTableAlias = null;
    private String  pendingTableName  = null;
    private String  pendingJoinType   = null;

    public PostgreSQLSemanticListener(UniversalSemanticEngine engine) {
        this.base = new BaseSemanticListener(engine) {};
    }
    public void setDefaultSchema(String schema) { base.defaultSchema = schema; }

    // === Helpers ===
    private int    getStartLine(ParserRuleContext ctx) { return ctx.start.getLine(); }
    private int    getEndLine(ParserRuleContext ctx)   { return ctx.stop != null ? ctx.stop.getLine() : ctx.start.getLine(); }
    private int    getStartCol(ParserRuleContext ctx)  { return ctx.start.getCharPositionInLine(); }
    private int    getEndCol(ParserRuleContext ctx)    { return ctx.stop != null ? ctx.stop.getCharPositionInLine() : 0; }
    private String extract(ParserRuleContext ctx)      { String t = ctx.getText(); return t.length() > 120 ? t.substring(0,120) : t; }
    private static Map<String,String> td(String text, String type, int line, int col) {
        return Map.of("text", text, "type", type, "position", line + ":" + col);
    }
}
```

---

### P0 — SELECT scope (PostgreSQL)

```java
@Override
public void enterSimple_select_pramary(PostgreSQLParser.Simple_select_pramaryContext ctx) {
    if (suppressedSelects.contains(ctx)) return;
    base.onStatementEnter("SELECT", extract(ctx), getStartLine(ctx), getEndLine(ctx));
}

@Override
public void exitSimple_select_pramary(PostgreSQLParser.Simple_select_pramaryContext ctx) {
    if (suppressedSelects.contains(ctx)) { suppressedSelects.remove(ctx); return; }
    base.onStatementExit();
}

@Override
public void enterTarget_list(PostgreSQLParser.Target_listContext ctx) {
    base.onSelectedListEnter(getStartLine(ctx));
}
@Override
public void exitTarget_list(PostgreSQLParser.Target_listContext ctx) {
    base.onSelectedListExit();
}
@Override
public void enterFrom_clause(PostgreSQLParser.From_clauseContext ctx) { base.onFromEnter(getStartLine(ctx)); }
@Override
public void exitFrom_clause(PostgreSQLParser.From_clauseContext ctx)  { base.onFromExit(); }
@Override
public void enterWhere_clause(PostgreSQLParser.Where_clauseContext ctx) { base.onWhereEnter(getStartLine(ctx)); }
@Override
public void exitWhere_clause(PostgreSQLParser.Where_clauseContext ctx)  { base.onWhereExit(); }
@Override
public void enterSort_clause(PostgreSQLParser.Sort_clauseContext ctx)  { base.onOrderByEnter(getStartLine(ctx)); }
@Override
public void exitSort_clause(PostgreSQLParser.Sort_clauseContext ctx)   { base.onOrderByExit(); }
@Override
public void enterGroup_clause(PostgreSQLParser.Group_clauseContext ctx)  { base.onGroupByEnter(getStartLine(ctx)); }
@Override
public void exitGroup_clause(PostgreSQLParser.Group_clauseContext ctx)   { base.onGroupByExit(); }
@Override
public void enterHaving_clause(PostgreSQLParser.Having_clauseContext ctx)  { base.onHavingEnter(getStartLine(ctx)); }
@Override
public void exitHaving_clause(PostgreSQLParser.Having_clauseContext ctx)   { base.onHavingExit(); }
```

### P0 — SELECT scope (ClickHouse)

```java
@Override
public void enterSelectStmt(ClickHouseParser.SelectStmtContext ctx) {
    if (suppressedSelects.contains(ctx)) return;
    base.onStatementEnter("SELECT", extract(ctx), getStartLine(ctx), getEndLine(ctx));
}
@Override
public void exitSelectStmt(ClickHouseParser.SelectStmtContext ctx) {
    if (suppressedSelects.contains(ctx)) { suppressedSelects.remove(ctx); return; }
    base.onStatementExit();
}
// columnExprList / fromClause / whereClause / prewhereClause / groupByClause / havingClause / orderByClause
// → те же base.onSelectedListEnter/Exit, onFromEnter/Exit, onWhereEnter/Exit, etc.
// PREWHERE: обрабатывать как WHERE (те же base вызовы)
```

---

### P0 — FROM + таблицы (PostgreSQL)

```java
// Alias tracking через pending state:
@Override
public void enterAlias_clause(PostgreSQLParser.Alias_clauseContext ctx) {
    pendingTableAlias = BaseSemanticListener.cleanIdentifier(ctx.colid().getText());
}

@Override
public void exitTable_ref(PostgreSQLParser.Table_refContext ctx) {
    if (pendingTableName != null) {
        base.onTableReference(pendingTableName, pendingTableAlias, getStartLine(ctx), getEndLine(ctx));
        pendingTableName = null; pendingTableAlias = null;
    }
}

@Override
public void enterRelation_expr(PostgreSQLParser.Relation_exprContext ctx) {
    if (ctx.qualified_name() != null) {
        pendingTableName = BaseSemanticListener.cleanIdentifier(ctx.qualified_name().getText());
    }
}
```

### P0 — FROM + таблицы (ClickHouse)

```java
@Override
public void enterTableExprIdentifier(ClickHouseParser.TableExprIdentifierContext ctx) {
    String fullName = extractTableIdentifier(ctx.tableIdentifier());
    // Alias будет из вышестоящего TableExprAlias
    base.onTableReference(fullName, pendingTableAlias, getStartLine(ctx), getEndLine(ctx));
    pendingTableAlias = null;
}

@Override
public void exitTableExprAlias(ClickHouseParser.TableExprAliasContext ctx) {
    // Alias применяется к последней зарегистрированной таблице через base
    String alias = ctx.alias() != null
        ? ctx.alias().getText()
        : ctx.identifier().getText();
    // Использовать base.setCurrentTableAlias() или registrировать pending перед таблицей
}

private String extractTableIdentifier(ClickHouseParser.TableIdentifierContext ctx) {
    String table = cleanCHIdentifier(ctx.identifier().getText());
    if (ctx.databaseIdentifier() != null) {
        String db = cleanCHIdentifier(ctx.databaseIdentifier().identifier().getText());
        return db + "." + table;
    }
    return base.defaultSchema != null ? base.defaultSchema + "." + table : table;
}

// ClickHouse case-sensitive — НЕ toLowerCase:
private static String cleanCHIdentifier(String raw) {
    if (raw == null) return null;
    if ((raw.startsWith("`") && raw.endsWith("`")) ||
        (raw.startsWith("\"") && raw.endsWith("\"")))
        return raw.substring(1, raw.length() - 1);
    return raw;
}
```

---

### P0 — Atoms: enterColumnref (PostgreSQL)

```java
@Override
public void enterColumnref(PostgreSQLParser.ColumnrefContext ctx) {
    if (ctx == null) return;

    List<String> tokens = new ArrayList<>();
    List<Map<String,String>> tokenDetails = new ArrayList<>();

    String colId = ctx.colid().getText();
    tokens.add(colId);
    tokenDetails.add(td(colId, "IDENTIFIER", getStartLine(ctx), getStartCol(ctx)));

    if (ctx.indirection() != null) {
        for (var el : ctx.indirection().indirection_el()) {
            if (el.DOT() != null) {
                if (el.STAR() != null) return;  // tbl.* → не column ref, пропустить
                tokens.add(".");
                tokenDetails.add(td(".", "PERIOD", 0, 0));
                if (el.attr_name() != null) {
                    String attr = el.attr_name().getText();
                    tokens.add(attr);
                    tokenDetails.add(td(attr, "IDENTIFIER", 0, 0));
                }
            }
        }
    }

    base.onAtom(ctx.getText(), getStartLine(ctx), getStartCol(ctx),
                getEndLine(ctx), getEndCol(ctx),
                tokens.size() > 1, tokens, tokenDetails, 0);
}
```

### P0 — Atoms: enterColumnIdentifier (ClickHouse)

```java
@Override
public void enterColumnIdentifier(ClickHouseParser.ColumnIdentifierContext ctx) {
    if (ctx == null || inLambdaExpr) return;

    List<String> tokens = new ArrayList<>();
    List<Map<String,String>> tokenDetails = new ArrayList<>();

    if (ctx.tableIdentifier() != null) {
        var tid = ctx.tableIdentifier();
        if (tid.databaseIdentifier() != null) {
            String db  = cleanCHIdentifier(tid.databaseIdentifier().identifier().getText());
            String tbl = cleanCHIdentifier(tid.identifier().getText());
            tokens.add(db);  tokenDetails.add(td(db,  "IDENTIFIER", getStartLine(ctx), getStartCol(ctx)));
            tokens.add("."); tokenDetails.add(td(".", "PERIOD", 0, 0));
            tokens.add(tbl); tokenDetails.add(td(tbl, "IDENTIFIER", 0, 0));
            tokens.add("."); tokenDetails.add(td(".", "PERIOD", 0, 0));
        } else {
            String tbl = cleanCHIdentifier(tid.identifier().getText());
            tokens.add(tbl); tokenDetails.add(td(tbl, "IDENTIFIER", getStartLine(ctx), getStartCol(ctx)));
            tokens.add("."); tokenDetails.add(td(".", "PERIOD", 0, 0));
        }
    }

    var nested = ctx.nestedIdentifier();
    String first = cleanCHIdentifier(nested.identifier(0).getText());
    tokens.add(first); tokenDetails.add(td(first, "IDENTIFIER", 0, 0));
    if (nested.identifier().size() > 1) {
        String sub = cleanCHIdentifier(nested.identifier(1).getText());
        tokens.add("."); tokenDetails.add(td(".", "PERIOD", 0, 0));
        tokens.add(sub); tokenDetails.add(td(sub, "IDENTIFIER", 0, 0));
    }

    base.onAtom(ctx.getText(), getStartLine(ctx), getStartCol(ctx),
                getEndLine(ctx), getEndCol(ctx),
                tokens.size() > 1, tokens, tokenDetails, 0);
}

@Override
public void enterColumnLambdaExpr(ClickHouseParser.ColumnLambdaExprContext ctx) { inLambdaExpr = true; }
@Override
public void exitColumnLambdaExpr(ClickHouseParser.ColumnLambdaExprContext ctx)  { inLambdaExpr = false; }
```

---

### P0 — SELECT * (PostgreSQL)

```java
@Override
public void enterTarget_star(PostgreSQLParser.Target_starContext ctx) {
    base.onBareStar(getStartLine(ctx), getStartCol(ctx));
}

@Override
public void exitTarget_label(PostgreSQLParser.Target_labelContext ctx) {
    String expr = extract(ctx);
    boolean isTableStar = expr.endsWith(".*") && !expr.contains("(");
    String alias = null;
    if (ctx.colLabel() != null) alias = BaseSemanticListener.cleanIdentifier(ctx.colLabel().getText());
    else if (ctx.bareColLabel() != null) alias = BaseSemanticListener.cleanIdentifier(ctx.bareColLabel().getText());
    if (alias != null) base.onColumnAlias(alias);
    base.onOutputColumnExit(getStartLine(ctx), getStartCol(ctx), getEndLine(ctx), getEndCol(ctx), expr, isTableStar);
}
```

### P0 — SELECT * (ClickHouse)

```java
@Override
public void enterColumnsExprAsterisk(ClickHouseParser.ColumnsExprAsterisxContext ctx) {
    if (ctx.tableIdentifier() == null) {
        base.onBareStar(getStartLine(ctx), getStartCol(ctx));
    } else {
        String tbl = extractTableIdentifier(ctx.tableIdentifier());
        base.onOutputColumnExit(getStartLine(ctx), getStartCol(ctx), getEndLine(ctx), getEndCol(ctx), tbl + ".*", true);
    }
}

@Override
public void enterColumnExprAsterisk(ClickHouseParser.ColumnExprAsterisxContext ctx) {
    if (ctx.tableIdentifier() == null) base.onBareStar(getStartLine(ctx), getStartCol(ctx));
    else {
        String tbl = extractTableIdentifier(ctx.tableIdentifier());
        base.onOutputColumnExit(getStartLine(ctx), getStartCol(ctx), getEndLine(ctx), getEndCol(ctx), tbl + ".*", true);
    }
}
```

---

### P0 — CTE (PostgreSQL)

```java
@Override
public void enterCommon_table_expr(PostgreSQLParser.Common_table_exprContext ctx) {
    String cteName = BaseSemanticListener.cleanIdentifier(ctx.name().getText());
    base.onStatementEnter("CTE", extract(ctx), getStartLine(ctx), getEndLine(ctx), cteName);
    inCteDefinition = true;
}

@Override
public void exitCommon_table_expr(PostgreSQLParser.Common_table_exprContext ctx) {
    base.onStatementExit();
}
// Внутри CTE: первый enterSimple_select_pramary → suppress:
// В enterSimple_select_pramary: if (inCteDefinition) { suppressedSelects.add(ctx); inCteDefinition = false; return; }
```

### P0 — CTE (ClickHouse)

```java
// ctes → WITH namedQuery (',' namedQuery)*
// namedQuery: name=identifier (columnAliases)? AS '(' query ')'

@Override
public void enterNamedQuery(ClickHouseParser.NamedQueryContext ctx) {
    String cteName = cleanCHIdentifier(ctx.name.getText());
    base.onStatementEnter("CTE", extract(ctx), getStartLine(ctx), getEndLine(ctx), cteName);
    inCteDefinition = true;
}

@Override
public void exitNamedQuery(ClickHouseParser.NamedQueryContext ctx) {
    base.onStatementExit();
}
// В enterSelectStmt: if (inCteDefinition) { suppressedSelects.add(ctx); inCteDefinition = false; return; }
```

---

### P1 — INSERT (PostgreSQL)

```java
@Override
public void enterInsertstmt(PostgreSQLParser.InsertstmtContext ctx) {
    base.onDmlTargetEnter();
    base.onStatementEnter("INSERT", extract(ctx), getStartLine(ctx), getEndLine(ctx));
    if (ctx.insert_target() != null && ctx.insert_target().qualified_name() != null) {
        String tbl = BaseSemanticListener.cleanIdentifier(ctx.insert_target().qualified_name().getText());
        base.onTableReference(tbl, null, getStartLine(ctx), getEndLine(ctx));
    }
    if (ctx.insert_column_list() != null) {
        List<String> cols = ctx.insert_column_list().insert_column_item().stream()
            .map(i -> BaseSemanticListener.cleanIdentifier(i.colid().getText()))
            .collect(Collectors.toList());
        base.onInsertColumnList(cols);
    }
}

@Override
public void exitInsertstmt(PostgreSQLParser.InsertstmtContext ctx) {
    base.onStatementExit(); base.onDmlTargetExit();
}
```

### P1 — INSERT (ClickHouse)

```java
@Override
public void enterInsertStmt(ClickHouseParser.InsertStmtContext ctx) {
    base.onDmlTargetEnter();
    base.onStatementEnter("INSERT", extract(ctx), getStartLine(ctx), getEndLine(ctx));
    if (ctx.tableIdentifier() != null) {
        base.onTableReference(extractTableIdentifier(ctx.tableIdentifier()), null, getStartLine(ctx), getEndLine(ctx));
    }
    if (ctx.columnsClause() != null) {
        List<String> cols = ctx.columnsClause().nestedIdentifier().stream()
            .map(n -> cleanCHIdentifier(n.getText()))
            .collect(Collectors.toList());
        base.onInsertColumnList(cols);
    }
}
@Override
public void exitInsertStmt(ClickHouseParser.InsertStmtContext ctx) {
    base.onStatementExit(); base.onDmlTargetExit();
}
```

---

### P1 — UPDATE / DELETE (PostgreSQL)

```java
// UPDATE: enterUpdatestmt → base.onDmlTargetEnter + onStatementEnter("UPDATE")
//   + ctx.relation_expr_opt_alias() → onTableReference
//   + enterSet_clause → nestedIdentifier → onUpdateSetColumn
// DELETE: enterDeletestmt → base.onDmlTargetEnter + onStatementEnter("DELETE")
//   + ctx.relation_expr_opt_alias() → onTableReference
```

### P1 — UPDATE / DELETE (ClickHouse)

```java
// UPDATE: enterUpdateStmt → onDmlTargetEnter + onStatementEnter("UPDATE")
//   + ctx.nestedIdentifier() → onTableReference (не tableIdentifier!)
//   + enterAssignmentExpr → ctx.nestedIdentifier() → onUpdateSetColumn
// DELETE: enterDeleteStmt → onDmlTargetEnter + onStatementEnter("DELETE")
//   + ctx.nestedIdentifier() → onTableReference
```

---

### P1 — JOINs (PostgreSQL)

```java
// State: pendingJoinType, pendingJoinCondition
// enterJoin_type(ctx) → pendingJoinType = detectType(ctx)  // FULL/LEFT/RIGHT/INNER/CROSS
// enterJoin_qual(ctx) → pendingJoinCondition = ctx.getText()
// exitTable_ref(ctx)  → если pendingJoinType != null:
//   base.onJoinComplete(pendingJoinType, List.of(pendingJoinCondition), null, getStartLine(ctx))
//   pendingJoinType = null; pendingJoinCondition = null;
```

### P1 — JOINs (ClickHouse)

```java
// enterJoinExprOp → base.onJoinEnter(...)
// enterJoinOpInner / enterJoinOpLeftRight / enterJoinOpFull → pendingJoinType
//   + check ctx.GLOBAL() → pendingGlobal = true
// enterJoinConstraintClause → pendingJoinCondition = ctx.getText()
// exitJoinExprOp → base.onJoinComplete(pendingGlobal ? "GLOBAL_" + pendingJoinType : pendingJoinType, ...)
```

---

### P2 — CREATE TABLE (PostgreSQL)

```java
@Override
public void enterCreatestmt(PostgreSQLParser.CreatestmtContext ctx) {
    if (ctx.qualified_name() != null) {
        String tblRef = BaseSemanticListener.cleanIdentifier(ctx.qualified_name().getText());
        String[] parts = tblRef.split("\\.", 2);
        String schema = parts.length > 1 ? parts[0] : base.defaultSchema;
        String table  = parts.length > 1 ? parts[1] : parts[0];
        base.onCreateTableEnter(schema, table, extract(ctx), getStartLine(ctx), getEndLine(ctx));
    }
}
@Override
public void exitCreatestmt(PostgreSQLParser.CreatestmtContext ctx) { base.onCreateTableExit(); }
@Override
public void enterColumnDef(PostgreSQLParser.ColumnDefContext ctx) {
    base.onDdlColumnDefinition(
        BaseSemanticListener.cleanIdentifier(ctx.colid().getText()),
        ctx.typename() != null ? ctx.typename().getText() : "UNKNOWN", false, null);
}
```

### P2 — CREATE TABLE (ClickHouse)

```java
// CreateTableStmt: enterCreateTableStmt → ctx.tableIdentifier() → onCreateTableEnter(db, table, ...)
// TableElementExprColumn → enterTableColumnDfnt:
//   ctx.nestedIdentifier(0).getText() → column name
//   ctx.columnTypeExpr().getText() → column type
// TableElementExprConstraint → enterTableElementExprConstraint:
//   CONSTRAINT ... CHECK → onCheckConstraint()
// engineClause / primaryKeyClause → onPrimaryKeyConstraint() (v2)
```

---

### P2 — CREATE VIEW (оба диалекта)

```java
// PostgreSQL:
// enterViewstmt → ctx.qualified_name() → base.onViewDeclaration(name, schema, line)
// exitViewstmt  → base.onStatementExit()

// ClickHouse:
// enterCreateViewStmt → ctx.tableIdentifier() → base.onViewDeclaration(name, db, line)
// exitCreateViewStmt  → base.onStatementExit()
// enterCreateMaterializedViewStmt → аналогично + "MVIEW"
```

---

### P3 — Routines (только PostgreSQL, нет в ClickHouse)

```java
@Override
public void enterCreatefunctionstmt(PostgreSQLParser.CreatefunctionstmtContext ctx) {
    String type = ctx.PROCEDURE() != null ? "PROCEDURE" : "FUNCTION";
    String name = ctx.func_name() != null
        ? BaseSemanticListener.cleanIdentifier(ctx.func_name().getText()) : "unknown";
    base.onRoutineEnter(name, type, base.defaultSchema, null, getStartLine(ctx));
    // extract parameters from ctx.func_args_with_defaults()
}
@Override
public void exitCreatefunctionstmt(PostgreSQLParser.CreatefunctionstmtContext ctx) { base.onRoutineExit(); }
```

---

## Критические watchlist-пункты

| # | Проблема | Решение |
|---|---|---|
| W-1 | ClickHouse `"statement"` в ParserRegistry ≠ `clickhouseFile` | В `parseAndWalk()` явно вызывать `parser.clickhouseFile()` |
| W-2 | PG alias = отдельный `alias_clause` | pending state `pendingTableName`/`pendingTableAlias` |
| W-3 | PG `tbl.*` → `enterColumnref` срабатывает | Guard: `if (el.STAR() != null) return;` |
| W-4 | CH идентификаторы регистро-чувствительны | `cleanCHIdentifier` без toLowerCase |
| W-5 | CH lambda параметры ≠ column refs | `inLambdaExpr` flag guard |
| W-6 | DialectRegistry ключ `"pgsql"` → исправить на `"postgresql"` | Правильный ключ при регистрации |
| W-7 | CH `withClause` в `selectStmt` ≠ CTE | CTE = `ctes` над `selectStmt` в `query`, не `withClause` |
| W-8 | CH UPDATE/DELETE target = `nestedIdentifier` (не `tableIdentifier`) | `ctx.nestedIdentifier()` в обоих |
| W-9 | PG UNION → suppression не нужен для каждого `simple_select_pramary` | Каждый `simple_select_pramary` = отдельный scope |
| W-10 | CH `ColumnsExprAsterisk` и `ColumnExprAsterisk` — два разных альтернатива | Обработать оба |

---

## Переиспользуемые компоненты (не переписывать)

| Компонент | Файл | Как использовать |
|---|---|---|
| `BaseSemanticListener` | `semantic/listener/BaseSemanticListener.java` | `private final BaseSemanticListener base` |
| `UniversalSemanticEngine` | `semantic/engine/UniversalSemanticEngine.java` | constructor param |
| `NameResolver` (8 стратегий) | `semantic/engine/NameResolver.java` | автоматически через engine |
| `AtomProcessor.classifyAtom()` | `semantic/engine/AtomProcessor.java` | автоматически через `base.onAtom()` |
| `cleanIdentifier()` | `BaseSemanticListener` static | PG unquoted identifiers |
| `onStatementEnter/Exit` | `BaseSemanticListener` | scope push/pop |
| `onTableReference` | `BaseSemanticListener` | table + alias |
| `onAtom` | `BaseSemanticListener` | column ref atom |
| `onBareStar` | `BaseSemanticListener` | SELECT * |
| `onJoinComplete` | `BaseSemanticListener` | join registration |
| `onCreateTableEnter/Exit` | `BaseSemanticListener` | DDL |
| `onDdlColumnDefinition` | `BaseSemanticListener` | DDL column |

---

## Порядок реализации (итерации)

```
Итерация 1 — Skeleton (компилируется, ничего не делает):
  DialectRegistry + HoundParserImpl + оба DialectAdapter + пустые SemanticListener

Итерация 2 — Core P0 (PostgreSQL первым):
  enterSimple_select_pramary + enterRelation_expr + enterAlias_clause + enterColumnref
  → простые SELECT разрешаются

Итерация 3 — Core P0 (ClickHouse):
  enterSelectStmt + enterTableExprIdentifier + exitTableExprAlias + enterColumnIdentifier
  → простые SELECT разрешаются

Итерация 4 — CTE + suppression (оба):
  enterCommon_table_expr (PG) + enterNamedQuery (CH) + suppression logic

Итерация 5 — SELECT * + output columns (оба):
  enterTarget_star / exitTarget_label (PG)
  enterColumnsExprAsterisk / enterColumnExprAsterisk (CH)

Итерация 6 — DML P1 (INSERT/UPDATE/DELETE, оба):

Итерация 7 — JOINs P1 (оба):

Итерация 8 — DDL P2 (CREATE TABLE, VIEW, оба):

Итерация 9 — Routines P3 (только PG):

Итерация 10 — Tests + resolution rate tuning:
  PostgreSQLSemanticListenerTest + ClickHouseSemanticListenerTest
  resolution_rate = count(RESOLVED) / count(is_column_reference=true) ≥ 0.70
```

---

## Верификация

```bash
# Компиляция
./gradlew :libraries:hound:compileJava

# Unit tests
./gradlew :libraries:hound:test --tests "*PostgreSQL*" --info
./gradlew :libraries:hound:test --tests "*ClickHouse*" --info

# Regression (убедиться что PL/SQL не сломан)
./gradlew :libraries:hound:test --info

# Resolution rate (цель ≥ 70%)
# Запустить против корпуса SQL-файлов и проверить процент RESOLVED
```

### Минимальные тест-кейсы для PostgreSQL (7 штук)
- `PG-T1`: `SELECT a, b FROM t` → 2 atoms, 1 source table
- `PG-T2`: `SELECT a FROM t1 JOIN t2 ON t1.id = t2.id` → 2 tables, 4 atoms
- `PG-T3`: `WITH cte AS (SELECT x FROM t) SELECT col FROM cte` → CTE scope, 2 stmts
- `PG-T4`: `INSERT INTO t (a,b) SELECT x,y FROM s` → INSERT stmt, target=t, source=s
- `PG-T5`: `UPDATE t SET a = b WHERE id = 1` → UPDATE stmt, target=t
- `PG-T6`: `CREATE TABLE s.t (id INT, name TEXT)` → DDL table, 2 columns
- `PG-T7`: `SELECT *` → onBareStar, нет column ref atoms

### Минимальные тест-кейсы для ClickHouse (7 штук)
- `CH-T1`: `SELECT a, b FROM t` → 2 atoms, 1 source table
- `CH-T2`: `SELECT t.col FROM t1 AS t JOIN t2 b ON t.id = b.id` → 2 tables (case-sensitive aliases)
- `CH-T3`: `WITH cte AS (SELECT x FROM t) SELECT col FROM cte` → CTE scope
- `CH-T4`: `INSERT INTO db.t (a, b) VALUES (1, 2)` → INSERT, target=db.t
- `CH-T5`: `SELECT * FROM t` → onBareStar
- `CH-T6`: `SELECT tbl.* FROM t tbl` → isTableStar=true
- `CH-T7`: `SELECT arrayMap(x -> x * 2, arr) FROM t` → lambda guard, `arr` = atom, `x` = NOT atom
