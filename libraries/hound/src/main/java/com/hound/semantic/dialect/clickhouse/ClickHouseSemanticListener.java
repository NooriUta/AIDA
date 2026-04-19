package com.hound.semantic.dialect.clickhouse;

import com.hound.parser.base.grammars.sql.clickhouse.ClickHouseParser;
import com.hound.parser.base.grammars.sql.clickhouse.ClickHouseParserBaseListener;
import com.hound.semantic.listener.BaseSemanticListener;
import com.hound.semantic.engine.UniversalSemanticEngine;
import org.antlr.v4.runtime.ParserRuleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ClickHouseSemanticListener — dialect listener for ClickHouse SQL.
 *
 * Uses composition: holds {@code private final BaseSemanticListener base} and delegates
 * all engine/scope calls through {@code base.onXxx()} methods.
 *
 * Pattern mirrors PlSqlSemanticListener.
 * Scope boundary: {@code selectStmt} (every SELECT body).
 *
 * NOTE: ClickHouse identifiers ARE case-sensitive.
 * Use {@link #cleanCHIdentifier} instead of {@link BaseSemanticListener#cleanIdentifier}
 * which applies toLowerCase().
 */
public class ClickHouseSemanticListener extends ClickHouseParserBaseListener {

    private static final Logger logger = LoggerFactory.getLogger(ClickHouseSemanticListener.class);

    private final BaseSemanticListener base;

    // ─── State fields ─────────────────────────────────────────────

    /** SELECT scopes inside CTE bodies — suppressed from creating a duplicate scope. */
    private final Set<ClickHouseParser.SelectStmtContext> suppressedSelects = new HashSet<>();

    /** True while we are inside a CTE definition, before entering its select body. */
    private boolean inCteDefinition = false;

    /** Table alias accumulated in enterTableExprAlias, consumed in enterTableExprIdentifier. */
    private String pendingTableAlias = null;

    /** JOIN type accumulated in enterJoinOpXxx, consumed in exitJoinExprOp. */
    private String pendingJoinType = null;

    /** JOIN condition text accumulated in enterJoinConstraintClause. */
    private String pendingJoinCondition = null;

    /** Guard: lambda parameter identifiers must not be registered as column refs. */
    private boolean inLambdaExpr = false;

    // ─── Constructor ──────────────────────────────────────────────

    public ClickHouseSemanticListener(UniversalSemanticEngine engine) {
        this.base = new BaseSemanticListener(engine) {};
    }

    /** Sets the fallback schema / database when no explicit prefix is found. */
    public void setDefaultSchema(String schema) {
        base.defaultSchema = schema;
    }

    // ─── Helpers ──────────────────────────────────────────────────

    private int    startLine(ParserRuleContext ctx) { return ctx.start.getLine(); }
    private int    endLine(ParserRuleContext ctx)   { return ctx.stop != null ? ctx.stop.getLine() : ctx.start.getLine(); }
    private int    startCol(ParserRuleContext ctx)  { return ctx.start.getCharPositionInLine(); }
    private int    endCol(ParserRuleContext ctx)    { return ctx.stop != null ? ctx.stop.getCharPositionInLine() : 0; }
    private String snippet(ParserRuleContext ctx)   { String t = ctx.getText(); return t.length() > 120 ? t.substring(0, 120) : t; }

    /**
     * Strips surrounding backtick or double-quote but preserves original case.
     * Unlike BaseSemanticListener.cleanIdentifier which also applies toLowerCase().
     */
    static String cleanCHIdentifier(String raw) {
        if (raw == null) return null;
        if (raw.length() >= 2
                && ((raw.startsWith("`") && raw.endsWith("`"))
                        || (raw.startsWith("\"") && raw.endsWith("\"")))) {
            return raw.substring(1, raw.length() - 1);
        }
        return raw;
    }

    private String extractTableIdentifier(ClickHouseParser.TableIdentifierContext ctx) {
        if (ctx == null) return null;
        String table = cleanCHIdentifier(ctx.identifier().getText());
        if (ctx.databaseIdentifier() != null) {
            String db = cleanCHIdentifier(ctx.databaseIdentifier().identifier().getText());
            return db + "." + table;
        }
        return base.defaultSchema != null ? base.defaultSchema + "." + table : table;
    }

    private String extractNestedIdentifier(ClickHouseParser.NestedIdentifierContext ctx) {
        if (ctx == null) return null;
        if (ctx.identifier().size() == 1) {
            return cleanCHIdentifier(ctx.identifier(0).getText());
        }
        return cleanCHIdentifier(ctx.identifier(0).getText())
                + "." + cleanCHIdentifier(ctx.identifier(1).getText());
    }

    private static Map<String, String> td(String text, String type, int line, int col) {
        return Map.of("text", text, "type", type, "position", line + ":" + col);
    }

    // =========================================================================
    // P0 — SELECT scope
    // =========================================================================

    @Override
    public void enterSelectStmt(ClickHouseParser.SelectStmtContext ctx) {
        if (suppressedSelects.contains(ctx)) return;
        base.onStatementEnter("SELECT", snippet(ctx), startLine(ctx), endLine(ctx));
    }

    @Override
    public void exitSelectStmt(ClickHouseParser.SelectStmtContext ctx) {
        if (suppressedSelects.contains(ctx)) {
            suppressedSelects.remove(ctx);
            return;
        }
        base.onStatementExit();
    }

    @Override
    public void enterColumnExprList(ClickHouseParser.ColumnExprListContext ctx) {
        base.onSelectedListEnter(startLine(ctx));
    }

    @Override
    public void exitColumnExprList(ClickHouseParser.ColumnExprListContext ctx) {
        base.onSelectedListExit();
    }

    @Override
    public void enterFromClause(ClickHouseParser.FromClauseContext ctx) {
        base.onFromEnter(startLine(ctx));
    }

    @Override
    public void exitFromClause(ClickHouseParser.FromClauseContext ctx) {
        base.onFromExit();
    }

    @Override
    public void enterWhereClause(ClickHouseParser.WhereClauseContext ctx) {
        base.onWhereEnter(startLine(ctx));
    }

    @Override
    public void exitWhereClause(ClickHouseParser.WhereClauseContext ctx) {
        base.onWhereExit();
    }

    @Override
    public void enterPrewhereClause(ClickHouseParser.PrewhereClauseContext ctx) {
        // PREWHERE is ClickHouse-specific pre-filter; treat like WHERE
        base.onWhereEnter(startLine(ctx));
    }

    @Override
    public void exitPrewhereClause(ClickHouseParser.PrewhereClauseContext ctx) {
        base.onWhereExit();
    }

    @Override
    public void enterGroupByClause(ClickHouseParser.GroupByClauseContext ctx) {
        base.onGroupByEnter(startLine(ctx));
    }

    @Override
    public void exitGroupByClause(ClickHouseParser.GroupByClauseContext ctx) {
        base.onGroupByExit();
    }

    @Override
    public void enterHavingClause(ClickHouseParser.HavingClauseContext ctx) {
        base.onHavingEnter(startLine(ctx));
    }

    @Override
    public void exitHavingClause(ClickHouseParser.HavingClauseContext ctx) {
        base.onHavingExit();
    }

    @Override
    public void enterOrderByClause(ClickHouseParser.OrderByClauseContext ctx) {
        base.onOrderByEnter(startLine(ctx));
    }

    @Override
    public void exitOrderByClause(ClickHouseParser.OrderByClauseContext ctx) {
        base.onOrderByExit();
    }

    // =========================================================================
    // P0 — FROM: table refs and aliases (pending state pattern)
    // =========================================================================

    /**
     * TableExprAlias fires before TableExprIdentifier because the alias node wraps it.
     * Extract the alias here so it is available when the inner table is registered.
     */
    @Override
    public void enterTableExprAlias(ClickHouseParser.TableExprAliasContext ctx) {
        // alias() covers IDENTIFIER and keywordForAlias; identifier() covers bare identifier
        if (ctx.alias() != null) {
            pendingTableAlias = cleanCHIdentifier(ctx.alias().getText());
        } else if (ctx.identifier() != null) {
            pendingTableAlias = cleanCHIdentifier(ctx.identifier().getText());
        }
    }

    @Override
    public void exitTableExprAlias(ClickHouseParser.TableExprAliasContext ctx) {
        // Alias was consumed in enterTableExprIdentifier; clear any remnant
        pendingTableAlias = null;
    }

    @Override
    public void enterTableExprIdentifier(ClickHouseParser.TableExprIdentifierContext ctx) {
        if (ctx.tableIdentifier() == null) return;
        String fullName = extractTableIdentifier(ctx.tableIdentifier());
        base.onTableReference(fullName, pendingTableAlias, startLine(ctx), endLine(ctx));
        pendingTableAlias = null;
    }

    // =========================================================================
    // P0 — Atoms: column references (with lambda guard)
    // =========================================================================

    @Override
    public void enterColumnLambdaExpr(ClickHouseParser.ColumnLambdaExprContext ctx) {
        inLambdaExpr = true;
    }

    @Override
    public void exitColumnLambdaExpr(ClickHouseParser.ColumnLambdaExprContext ctx) {
        inLambdaExpr = false;
    }

    @Override
    public void enterColumnIdentifier(ClickHouseParser.ColumnIdentifierContext ctx) {
        if (ctx == null || inLambdaExpr) return;

        List<String> tokens = new ArrayList<>();
        List<Map<String, String>> tokenDetails = new ArrayList<>();

        // Optional table prefix: [db.]table.
        if (ctx.tableIdentifier() != null) {
            var tid = ctx.tableIdentifier();
            if (tid.databaseIdentifier() != null) {
                String db  = cleanCHIdentifier(tid.databaseIdentifier().identifier().getText());
                String tbl = cleanCHIdentifier(tid.identifier().getText());
                tokens.add(db);  tokenDetails.add(td(db,  "IDENTIFIER", startLine(ctx), startCol(ctx)));
                tokens.add("."); tokenDetails.add(td(".", "PERIOD", 0, 0));
                tokens.add(tbl); tokenDetails.add(td(tbl, "IDENTIFIER", 0, 0));
                tokens.add("."); tokenDetails.add(td(".", "PERIOD", 0, 0));
            } else {
                String tbl = cleanCHIdentifier(tid.identifier().getText());
                tokens.add(tbl); tokenDetails.add(td(tbl, "IDENTIFIER", startLine(ctx), startCol(ctx)));
                tokens.add("."); tokenDetails.add(td(".", "PERIOD", 0, 0));
            }
        }

        // nestedIdentifier: identifier ('.' identifier)?
        var nested = ctx.nestedIdentifier();
        String first = cleanCHIdentifier(nested.identifier(0).getText());
        int firstLine = tokens.isEmpty() ? startLine(ctx) : 0;
        int firstCol  = tokens.isEmpty() ? startCol(ctx)  : 0;
        tokens.add(first); tokenDetails.add(td(first, "IDENTIFIER", firstLine, firstCol));

        if (nested.identifier().size() > 1) {
            String sub = cleanCHIdentifier(nested.identifier(1).getText());
            tokens.add("."); tokenDetails.add(td(".", "PERIOD", 0, 0));
            tokens.add(sub); tokenDetails.add(td(sub, "IDENTIFIER", 0, 0));
        }

        base.onAtom(ctx.getText(), startLine(ctx), startCol(ctx),
                    endLine(ctx), endCol(ctx),
                    tokens.size() > 1, tokens, tokenDetails, 0);
    }

    // =========================================================================
    // P0 — SELECT * and table.*
    // =========================================================================

    @Override
    public void enterColumnsExprAsterisk(ClickHouseParser.ColumnsExprAsteriskContext ctx) {
        if (ctx.tableIdentifier() == null) {
            base.onBareStar(startLine(ctx), startCol(ctx));
        } else {
            String tbl = extractTableIdentifier(ctx.tableIdentifier());
            base.onOutputColumnExit(startLine(ctx), startCol(ctx), endLine(ctx), endCol(ctx),
                                    tbl + ".*", true);
        }
    }

    @Override
    public void enterColumnExprAsterisk(ClickHouseParser.ColumnExprAsteriskContext ctx) {
        if (ctx.tableIdentifier() == null) {
            base.onBareStar(startLine(ctx), startCol(ctx));
        } else {
            String tbl = extractTableIdentifier(ctx.tableIdentifier());
            base.onOutputColumnExit(startLine(ctx), startCol(ctx), endLine(ctx), endCol(ctx),
                                    tbl + ".*", true);
        }
    }

    // =========================================================================
    // P0 — CTE
    // =========================================================================

    @Override
    public void enterNamedQuery(ClickHouseParser.NamedQueryContext ctx) {
        String cteName = ctx.name != null
                ? cleanCHIdentifier(ctx.name.getText())
                : "unknown_cte";
        base.onStatementEnter("CTE", snippet(ctx), startLine(ctx), endLine(ctx), cteName);
        inCteDefinition = true;
    }

    @Override
    public void exitNamedQuery(ClickHouseParser.NamedQueryContext ctx) {
        base.onStatementExit();
    }

    // =========================================================================
    // P1 — INSERT
    // =========================================================================

    @Override
    public void enterInsertStmt(ClickHouseParser.InsertStmtContext ctx) {
        base.onDmlTargetEnter();
        base.onStatementEnter("INSERT", snippet(ctx), startLine(ctx), endLine(ctx));

        if (ctx.tableIdentifier() != null) {
            base.onTableReference(
                    extractTableIdentifier(ctx.tableIdentifier()), null,
                    startLine(ctx), endLine(ctx));
        }

        if (ctx.columnsClause() != null) {
            List<String> cols = ctx.columnsClause().nestedIdentifier().stream()
                    .map(n -> cleanCHIdentifier(n.identifier(0).getText()))
                    .collect(Collectors.toList());
            base.onInsertColumnList(cols);
        }
    }

    @Override
    public void exitInsertStmt(ClickHouseParser.InsertStmtContext ctx) {
        base.onStatementExit();
        base.onDmlTargetExit();
    }

    // =========================================================================
    // P1 — UPDATE (MutationStatement — ALTER TABLE ... UPDATE is different; this is direct UPDATE)
    // =========================================================================

    @Override
    public void enterUpdateStmt(ClickHouseParser.UpdateStmtContext ctx) {
        base.onDmlTargetEnter();
        base.onStatementEnter("UPDATE", snippet(ctx), startLine(ctx), endLine(ctx));

        // UPDATE target is a nestedIdentifier (schema.table or just table)
        if (ctx.nestedIdentifier() != null) {
            String tbl = extractNestedIdentifier(ctx.nestedIdentifier());
            if (tbl != null) {
                base.onTableReference(tbl, null, startLine(ctx), endLine(ctx));
            }
        }
    }

    @Override
    public void exitUpdateStmt(ClickHouseParser.UpdateStmtContext ctx) {
        base.onStatementExit();
        base.onDmlTargetExit();
    }

    /** Each SET assignment: nestedIdentifier = columnExpr */
    @Override
    public void enterAssignmentExpr(ClickHouseParser.AssignmentExprContext ctx) {
        if (ctx.nestedIdentifier() != null) {
            String colName = cleanCHIdentifier(ctx.nestedIdentifier().identifier(0).getText());
            base.onUpdateSetColumn(colName);
        }
    }

    // =========================================================================
    // P1 — DELETE
    // =========================================================================

    @Override
    public void enterDeleteStmt(ClickHouseParser.DeleteStmtContext ctx) {
        base.onDmlTargetEnter();
        base.onStatementEnter("DELETE", snippet(ctx), startLine(ctx), endLine(ctx));

        // DELETE target is a nestedIdentifier
        if (ctx.nestedIdentifier() != null) {
            String tbl = extractNestedIdentifier(ctx.nestedIdentifier());
            if (tbl != null) {
                base.onTableReference(tbl, null, startLine(ctx), endLine(ctx));
            }
        }
    }

    @Override
    public void exitDeleteStmt(ClickHouseParser.DeleteStmtContext ctx) {
        base.onStatementExit();
        base.onDmlTargetExit();
    }

    // =========================================================================
    // P1 — JOINs
    // =========================================================================

    @Override
    public void enterJoinOpInner(ClickHouseParser.JoinOpInnerContext ctx) {
        pendingJoinType = "INNER";
    }

    @Override
    public void enterJoinOpLeftRight(ClickHouseParser.JoinOpLeftRightContext ctx) {
        String text = ctx.getText().toUpperCase();
        if (text.contains("LEFT"))       pendingJoinType = "LEFT";
        else if (text.contains("RIGHT")) pendingJoinType = "RIGHT";
        else                             pendingJoinType = "LEFT";
    }

    @Override
    public void enterJoinOpFull(ClickHouseParser.JoinOpFullContext ctx) {
        pendingJoinType = "FULL";
    }

    @Override
    public void enterJoinConstraintClause(ClickHouseParser.JoinConstraintClauseContext ctx) {
        pendingJoinCondition = ctx.getText();
    }

    @Override
    public void exitJoinExprOp(ClickHouseParser.JoinExprOpContext ctx) {
        if (pendingJoinType == null) pendingJoinType = "INNER"; // bare JOIN
        // Check GLOBAL modifier
        String joinType = ctx.GLOBAL() != null ? "GLOBAL_" + pendingJoinType : pendingJoinType;
        List<String> conditions = pendingJoinCondition != null
                ? List.of(pendingJoinCondition) : List.of();
        base.onJoinComplete(joinType, conditions, null, startLine(ctx));
        pendingJoinType      = null;
        pendingJoinCondition = null;
    }

    // =========================================================================
    // P2 — CREATE TABLE
    // =========================================================================

    @Override
    public void enterCreateTableStmt(ClickHouseParser.CreateTableStmtContext ctx) {
        if (ctx.tableIdentifier() == null) return;

        String db    = ctx.tableIdentifier().databaseIdentifier() != null
                ? cleanCHIdentifier(ctx.tableIdentifier().databaseIdentifier().identifier().getText())
                : base.defaultSchema;
        String table = cleanCHIdentifier(ctx.tableIdentifier().identifier().getText());

        base.onCreateTableEnter(db, table, snippet(ctx), startLine(ctx), endLine(ctx));
    }

    @Override
    public void exitCreateTableStmt(ClickHouseParser.CreateTableStmtContext ctx) {
        base.onCreateTableExit();
    }

    @Override
    public void enterTableColumnDfnt(ClickHouseParser.TableColumnDfntContext ctx) {
        if (ctx.nestedIdentifier() == null) return;
        String colName = cleanCHIdentifier(ctx.nestedIdentifier().identifier(0).getText());
        String colType = ctx.columnTypeExpr() != null ? ctx.columnTypeExpr().getText() : "UNKNOWN";
        base.onDdlColumnDefinition(colName, colType, false, null);
    }

    // =========================================================================
    // P2 — CREATE VIEW / CREATE MATERIALIZED VIEW
    // =========================================================================

    @Override
    public void enterCreateViewStmt(ClickHouseParser.CreateViewStmtContext ctx) {
        if (ctx.tableIdentifier() == null) return;

        String db    = ctx.tableIdentifier().databaseIdentifier() != null
                ? cleanCHIdentifier(ctx.tableIdentifier().databaseIdentifier().identifier().getText())
                : base.defaultSchema;
        String view  = cleanCHIdentifier(ctx.tableIdentifier().identifier().getText());

        base.onViewDeclaration(view, db, startLine(ctx));
        base.onStatementEnter("SELECT", snippet(ctx), startLine(ctx), endLine(ctx));
    }

    @Override
    public void exitCreateViewStmt(ClickHouseParser.CreateViewStmtContext ctx) {
        base.onStatementExit();
    }

    @Override
    public void enterCreateMaterializedViewStmt(ClickHouseParser.CreateMaterializedViewStmtContext ctx) {
        if (ctx.tableIdentifier() == null) return;

        String db    = ctx.tableIdentifier().databaseIdentifier() != null
                ? cleanCHIdentifier(ctx.tableIdentifier().databaseIdentifier().identifier().getText())
                : base.defaultSchema;
        String view  = cleanCHIdentifier(ctx.tableIdentifier().identifier().getText());

        base.onViewDeclaration(view, db, startLine(ctx));
        base.onStatementEnter("SELECT", snippet(ctx), startLine(ctx), endLine(ctx));
    }

    @Override
    public void exitCreateMaterializedViewStmt(ClickHouseParser.CreateMaterializedViewStmtContext ctx) {
        base.onStatementExit();
    }
}
