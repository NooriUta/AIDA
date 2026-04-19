package com.hound.semantic.dialect.postgresql;

import com.hound.parser.base.grammars.sql.postgresql.PostgreSQLParser;
import com.hound.parser.base.grammars.sql.postgresql.PostgreSQLParserBaseListener;
import com.hound.semantic.listener.BaseSemanticListener;
import com.hound.semantic.engine.UniversalSemanticEngine;
import org.antlr.v4.runtime.ParserRuleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * PostgreSQLSemanticListener — dialect listener for PostgreSQL SQL.
 *
 * Uses composition: holds {@code private final BaseSemanticListener base} and delegates
 * all engine/scope calls through {@code base.onXxx()} methods.
 *
 * Pattern mirrors PlSqlSemanticListener.
 * Scope boundary: {@code simple_select_pramary} (every SELECT body — UNION arms, subqueries, etc.)
 */
public class PostgreSQLSemanticListener extends PostgreSQLParserBaseListener {

    private static final Logger logger = LoggerFactory.getLogger(PostgreSQLSemanticListener.class);

    private final BaseSemanticListener base;

    // ─── State fields ─────────────────────────────────────────────

    /** SELECT scopes inside CTE bodies — suppressed from creating a duplicate scope. */
    private final Set<PostgreSQLParser.Simple_select_pramaryContext> suppressedSelects = new HashSet<>();

    /** True while we are inside a CTE definition, before entering its select body. */
    private boolean inCteDefinition = false;

    /** Table name accumulated in enterRelation_expr, consumed in exitTable_ref. */
    private String pendingTableName = null;

    /** Table alias accumulated in enterAlias_clause, consumed in exitTable_ref. */
    private String pendingTableAlias = null;

    /** JOIN type accumulated in enterJoin_type, consumed in exitJoin_qual or exitTable_ref. */
    private String pendingJoinType = null;

    /** JOIN condition accumulated in enterJoin_qual. */
    private String pendingJoinCondition = null;

    // ─── Constructor ──────────────────────────────────────────────

    public PostgreSQLSemanticListener(UniversalSemanticEngine engine) {
        this.base = new BaseSemanticListener(engine) {};
    }

    /** Sets the fallback schema when no explicit SCHEMA. prefix is found. */
    public void setDefaultSchema(String schema) {
        base.defaultSchema = schema;
    }

    // ─── Helpers ──────────────────────────────────────────────────

    private int    startLine(ParserRuleContext ctx) { return ctx.start.getLine(); }
    private int    endLine(ParserRuleContext ctx)   { return ctx.stop != null ? ctx.stop.getLine() : ctx.start.getLine(); }
    private int    startCol(ParserRuleContext ctx)  { return ctx.start.getCharPositionInLine(); }
    private int    endCol(ParserRuleContext ctx)    { return ctx.stop != null ? ctx.stop.getCharPositionInLine() : 0; }
    private String snippet(ParserRuleContext ctx)   { String t = ctx.getText(); return t.length() > 120 ? t.substring(0, 120) : t; }

    private static Map<String, String> td(String text, String type, int line, int col) {
        return Map.of("text", text, "type", type, "position", line + ":" + col);
    }

    // =========================================================================
    // P0 — SELECT scope
    // =========================================================================

    @Override
    public void enterSimple_select_pramary(PostgreSQLParser.Simple_select_pramaryContext ctx) {
        if (suppressedSelects.contains(ctx)) return;
        base.onStatementEnter("SELECT", snippet(ctx), startLine(ctx), endLine(ctx));
    }

    @Override
    public void exitSimple_select_pramary(PostgreSQLParser.Simple_select_pramaryContext ctx) {
        if (suppressedSelects.contains(ctx)) {
            suppressedSelects.remove(ctx);
            return;
        }
        base.onStatementExit();
    }

    @Override
    public void enterTarget_list(PostgreSQLParser.Target_listContext ctx) {
        base.onSelectedListEnter(startLine(ctx));
    }

    @Override
    public void exitTarget_list(PostgreSQLParser.Target_listContext ctx) {
        base.onSelectedListExit();
    }

    @Override
    public void enterFrom_clause(PostgreSQLParser.From_clauseContext ctx) {
        base.onFromEnter(startLine(ctx));
    }

    @Override
    public void exitFrom_clause(PostgreSQLParser.From_clauseContext ctx) {
        base.onFromExit();
    }

    @Override
    public void enterWhere_clause(PostgreSQLParser.Where_clauseContext ctx) {
        base.onWhereEnter(startLine(ctx));
    }

    @Override
    public void exitWhere_clause(PostgreSQLParser.Where_clauseContext ctx) {
        base.onWhereExit();
    }

    @Override
    public void enterSort_clause(PostgreSQLParser.Sort_clauseContext ctx) {
        base.onOrderByEnter(startLine(ctx));
    }

    @Override
    public void exitSort_clause(PostgreSQLParser.Sort_clauseContext ctx) {
        base.onOrderByExit();
    }

    @Override
    public void enterGroup_clause(PostgreSQLParser.Group_clauseContext ctx) {
        base.onGroupByEnter(startLine(ctx));
    }

    @Override
    public void exitGroup_clause(PostgreSQLParser.Group_clauseContext ctx) {
        base.onGroupByExit();
    }

    @Override
    public void enterHaving_clause(PostgreSQLParser.Having_clauseContext ctx) {
        base.onHavingEnter(startLine(ctx));
    }

    @Override
    public void exitHaving_clause(PostgreSQLParser.Having_clauseContext ctx) {
        base.onHavingExit();
    }

    // =========================================================================
    // P0 — FROM: table refs and aliases (pending state pattern)
    // =========================================================================

    @Override
    public void enterRelation_expr(PostgreSQLParser.Relation_exprContext ctx) {
        if (ctx.qualified_name() != null) {
            pendingTableName = BaseSemanticListener.cleanIdentifier(ctx.qualified_name().getText());
        }
    }

    @Override
    public void enterAlias_clause(PostgreSQLParser.Alias_clauseContext ctx) {
        if (ctx.colid() != null) {
            pendingTableAlias = BaseSemanticListener.cleanIdentifier(ctx.colid().getText());
        }
    }

    @Override
    public void exitTable_ref(PostgreSQLParser.Table_refContext ctx) {
        if (pendingTableName != null) {
            base.onTableReference(pendingTableName, pendingTableAlias, startLine(ctx), endLine(ctx));
            pendingTableName  = null;
            pendingTableAlias = null;
        }
    }

    // =========================================================================
    // P0 — Atoms: column references
    // =========================================================================

    @Override
    public void enterColumnref(PostgreSQLParser.ColumnrefContext ctx) {
        if (ctx == null) return;

        List<String> tokens = new ArrayList<>();
        List<Map<String, String>> tokenDetails = new ArrayList<>();

        String colId = ctx.colid().getText();
        tokens.add(colId);
        tokenDetails.add(td(colId, "IDENTIFIER", startLine(ctx), startCol(ctx)));

        if (ctx.indirection() != null) {
            for (var el : ctx.indirection().indirection_el()) {
                if (el.DOT() != null) {
                    if (el.STAR() != null) return; // tbl.* — not a column ref, skip
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

        base.onAtom(ctx.getText(), startLine(ctx), startCol(ctx),
                    endLine(ctx), endCol(ctx),
                    tokens.size() > 1, tokens, tokenDetails, 0);
    }

    // =========================================================================
    // P0 — SELECT * and output columns
    // =========================================================================

    @Override
    public void enterTarget_star(PostgreSQLParser.Target_starContext ctx) {
        base.onBareStar(startLine(ctx), startCol(ctx));
    }

    @Override
    public void exitTarget_label(PostgreSQLParser.Target_labelContext ctx) {
        String expr = snippet(ctx);
        boolean isTableStar = expr.endsWith(".*") && !expr.contains("(");

        String alias = null;
        if (ctx.colLabel() != null) {
            alias = BaseSemanticListener.cleanIdentifier(ctx.colLabel().getText());
        } else if (ctx.bareColLabel() != null) {
            alias = BaseSemanticListener.cleanIdentifier(ctx.bareColLabel().getText());
        }
        if (alias != null) base.onColumnAlias(alias);

        base.onOutputColumnExit(startLine(ctx), startCol(ctx), endLine(ctx), endCol(ctx),
                                expr, isTableStar);
    }

    // =========================================================================
    // P0 — CTE
    // =========================================================================

    @Override
    public void enterCommon_table_expr(PostgreSQLParser.Common_table_exprContext ctx) {
        String cteName = ctx.name() != null
                ? BaseSemanticListener.cleanIdentifier(ctx.name().getText())
                : "unknown_cte";
        base.onStatementEnter("CTE", snippet(ctx), startLine(ctx), endLine(ctx), cteName);
        inCteDefinition = true;
    }

    @Override
    public void exitCommon_table_expr(PostgreSQLParser.Common_table_exprContext ctx) {
        base.onStatementExit();
    }

    // =========================================================================
    // P1 — INSERT
    // =========================================================================

    @Override
    public void enterInsertstmt(PostgreSQLParser.InsertstmtContext ctx) {
        base.onDmlTargetEnter();
        base.onStatementEnter("INSERT", snippet(ctx), startLine(ctx), endLine(ctx));

        if (ctx.insert_target() != null && ctx.insert_target().qualified_name() != null) {
            String tbl = BaseSemanticListener.cleanIdentifier(
                    ctx.insert_target().qualified_name().getText());
            base.onTableReference(tbl, null, startLine(ctx), endLine(ctx));
        }

        if (ctx.insert_rest() != null && ctx.insert_rest().insert_column_list() != null) {
            List<String> cols = ctx.insert_rest().insert_column_list()
                    .insert_column_item().stream()
                    .map(i -> BaseSemanticListener.cleanIdentifier(i.colid().getText()))
                    .collect(Collectors.toList());
            base.onInsertColumnList(cols);
        }
    }

    @Override
    public void exitInsertstmt(PostgreSQLParser.InsertstmtContext ctx) {
        base.onStatementExit();
        base.onDmlTargetExit();
    }

    // =========================================================================
    // P1 — UPDATE
    // =========================================================================

    @Override
    public void enterUpdatestmt(PostgreSQLParser.UpdatestmtContext ctx) {
        base.onDmlTargetEnter();
        base.onStatementEnter("UPDATE", snippet(ctx), startLine(ctx), endLine(ctx));

        if (ctx.relation_expr_opt_alias() != null
                && ctx.relation_expr_opt_alias().relation_expr() != null
                && ctx.relation_expr_opt_alias().relation_expr().qualified_name() != null) {
            String tbl = BaseSemanticListener.cleanIdentifier(
                    ctx.relation_expr_opt_alias().relation_expr().qualified_name().getText());
            String alias = null;
            if (ctx.relation_expr_opt_alias().colid() != null) {
                alias = BaseSemanticListener.cleanIdentifier(
                        ctx.relation_expr_opt_alias().colid().getText());
            }
            base.onTableReference(tbl, alias, startLine(ctx), endLine(ctx));
        }
    }

    @Override
    public void exitUpdatestmt(PostgreSQLParser.UpdatestmtContext ctx) {
        base.onStatementExit();
        base.onDmlTargetExit();
    }

    // =========================================================================
    // P1 — DELETE
    // =========================================================================

    @Override
    public void enterDeletestmt(PostgreSQLParser.DeletestmtContext ctx) {
        base.onDmlTargetEnter();
        base.onStatementEnter("DELETE", snippet(ctx), startLine(ctx), endLine(ctx));

        if (ctx.relation_expr_opt_alias() != null
                && ctx.relation_expr_opt_alias().relation_expr() != null
                && ctx.relation_expr_opt_alias().relation_expr().qualified_name() != null) {
            String tbl = BaseSemanticListener.cleanIdentifier(
                    ctx.relation_expr_opt_alias().relation_expr().qualified_name().getText());
            String alias = null;
            if (ctx.relation_expr_opt_alias().colid() != null) {
                alias = BaseSemanticListener.cleanIdentifier(
                        ctx.relation_expr_opt_alias().colid().getText());
            }
            base.onTableReference(tbl, alias, startLine(ctx), endLine(ctx));
        }
    }

    @Override
    public void exitDeletestmt(PostgreSQLParser.DeletestmtContext ctx) {
        base.onStatementExit();
        base.onDmlTargetExit();
    }

    // =========================================================================
    // P1 — JOINs
    // =========================================================================

    @Override
    public void enterJoin_type(PostgreSQLParser.Join_typeContext ctx) {
        String text = ctx.getText().toUpperCase();
        if (text.contains("FULL"))        pendingJoinType = "FULL";
        else if (text.contains("LEFT"))   pendingJoinType = "LEFT";
        else if (text.contains("RIGHT"))  pendingJoinType = "RIGHT";
        else if (text.contains("CROSS"))  pendingJoinType = "CROSS";
        else                              pendingJoinType = "INNER";
    }

    @Override
    public void enterJoin_qual(PostgreSQLParser.Join_qualContext ctx) {
        pendingJoinCondition = ctx.getText();
    }

    @Override
    public void exitJoin_qual(PostgreSQLParser.Join_qualContext ctx) {
        if (pendingJoinType != null) {
            List<String> conditions = pendingJoinCondition != null
                    ? List.of(pendingJoinCondition) : List.of();
            base.onJoinComplete(pendingJoinType, conditions, null, startLine(ctx));
            pendingJoinType      = null;
            pendingJoinCondition = null;
        }
    }

    // =========================================================================
    // P2 — CREATE TABLE
    // =========================================================================

    @Override
    public void enterCreatestmt(PostgreSQLParser.CreatestmtContext ctx) {
        if (ctx.qualified_name() == null || ctx.qualified_name().isEmpty()) return;

        String tblRef = BaseSemanticListener.cleanIdentifier(ctx.qualified_name(0).getText());
        String[] parts = tblRef.split("\\.", 2);
        String schema = parts.length > 1 ? parts[0] : base.defaultSchema;
        String table  = parts.length > 1 ? parts[1] : parts[0];

        base.onCreateTableEnter(schema, table, snippet(ctx), startLine(ctx), endLine(ctx));
    }

    @Override
    public void exitCreatestmt(PostgreSQLParser.CreatestmtContext ctx) {
        base.onCreateTableExit();
    }

    @Override
    public void enterColumnDef(PostgreSQLParser.ColumnDefContext ctx) {
        if (ctx.colid() == null) return;
        String colName = BaseSemanticListener.cleanIdentifier(ctx.colid().getText());
        String colType = ctx.typename() != null ? ctx.typename().getText() : "UNKNOWN";
        base.onDdlColumnDefinition(colName, colType, false, null);
    }

    // =========================================================================
    // P2 — CREATE VIEW
    // =========================================================================

    @Override
    public void enterViewstmt(PostgreSQLParser.ViewstmtContext ctx) {
        if (ctx.qualified_name() == null) return;

        String viewRef = BaseSemanticListener.cleanIdentifier(ctx.qualified_name().getText());
        String[] parts = viewRef.split("\\.", 2);
        String schema = parts.length > 1 ? parts[0] : base.defaultSchema;
        String view   = parts.length > 1 ? parts[1] : parts[0];

        base.onViewDeclaration(view, schema, startLine(ctx));
        base.onStatementEnter("SELECT", snippet(ctx), startLine(ctx), endLine(ctx));
    }

    @Override
    public void exitViewstmt(PostgreSQLParser.ViewstmtContext ctx) {
        base.onStatementExit();
    }

    // =========================================================================
    // P3 — Routines (FUNCTION / PROCEDURE)
    // =========================================================================

    @Override
    public void enterCreatefunctionstmt(PostgreSQLParser.CreatefunctionstmtContext ctx) {
        String type = ctx.PROCEDURE() != null ? "PROCEDURE" : "FUNCTION";
        String name = ctx.func_name() != null
                ? BaseSemanticListener.cleanIdentifier(ctx.func_name().getText())
                : "unknown";
        base.onRoutineEnter(name, type, base.defaultSchema, null, startLine(ctx));
    }

    @Override
    public void exitCreatefunctionstmt(PostgreSQLParser.CreatefunctionstmtContext ctx) {
        base.onRoutineExit();
    }
}
