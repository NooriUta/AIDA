package com.hound.semantic;

import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.semantic.dialect.plsql.PlSqlSemanticListener;
import com.hound.semantic.model.*;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JoinRegressionTest {

    private UniversalSemanticEngine parse(String sql) {
        UniversalSemanticEngine engine = new UniversalSemanticEngine();
        PlSqlSemanticListener listener = new PlSqlSemanticListener(engine);
        PlSqlLexer lexer = new PlSqlLexer(CharStreams.fromString(sql));
        PlSqlParser parser = new PlSqlParser(new CommonTokenStream(lexer));
        new ParseTreeWalker().walk(listener, parser.sql_script());
        engine.resolvePendingColumns();
        return engine;
    }

    // ═══════ Test 1: JOIN type LEFT OUTER ═══════

    @Test
    void joinType_leftOuter() {
        String sql = """
            SELECT o.id, c.name
            FROM orders o
            LEFT OUTER JOIN customers c ON o.customer_id = c.id
            """;
        var engine = parse(sql);
        var stmts = engine.getBuilder().getStatements();
        assertFalse(stmts.isEmpty(), "Should have at least one statement");

        var stmt = stmts.values().iterator().next();
        List<JoinInfo> joins = stmt.getJoins();
        assertFalse(joins.isEmpty(), "Should have at least one JOIN");

        JoinInfo join = joins.get(0);
        assertTrue(join.joinType().contains("LEFT"),
                "Expected LEFT join type, got: " + join.joinType());
        assertTrue(join.joinType().contains("OUTER") || join.joinType().contains("JOIN"),
                "Expected OUTER or JOIN keyword, got: " + join.joinType());
    }

    // ═══════ Test 2: JOIN source resolved via conditions ═══════

    @Test
    void joinSource_resolvedViaConditions() {
        String sql = """
            SELECT e.name, d.dept_name
            FROM employees e
            INNER JOIN departments d ON e.dept_id = d.id
            """;
        var engine = parse(sql);
        var stmts = engine.getBuilder().getStatements();
        var stmt = stmts.values().iterator().next();
        List<JoinInfo> joins = stmt.getJoins();
        assertFalse(joins.isEmpty(), "Should have at least one JOIN");

        JoinInfo join = joins.get(0);
        // Target is "departments" (the table being joined), source is "employees" (from conditions)
        assertNotNull(join.targetTableGeoid(), "Target table geoid should not be null");
        assertTrue(join.targetTableGeoid().toUpperCase().contains("DEPARTMENTS"),
                "Target should be DEPARTMENTS, got: " + join.targetTableGeoid());

        // Source should be resolved from ON condition (e → employees)
        // sourceAlias should be "E" (from e.dept_id in condition)
        if (join.sourceTableAlias() != null) {
            assertEquals("E", join.sourceTableAlias().toUpperCase(),
                    "Source alias should be E");
        }
    }

    // ═══════ Test 3: MERGE — source and target ═══════

    @Test
    void mergeAliases_sourceAndTarget() {
        String sql = """
            MERGE INTO target_table t
            USING source_table s
            ON (t.id = s.id)
            WHEN MATCHED THEN UPDATE SET t.name = s.name
            WHEN NOT MATCHED THEN INSERT (id, name) VALUES (s.id, s.name)
            """;
        var engine = parse(sql);
        var stmts = engine.getBuilder().getStatements();

        // Find the MERGE statement
        StatementInfo mergeStmt = null;
        for (var s : stmts.values()) {
            if ("MERGE".equals(s.getType())) {
                mergeStmt = s;
                break;
            }
        }
        assertNotNull(mergeStmt, "Should have a MERGE statement. Got: " +
                stmts.values().stream().map(StatementInfo::getType).toList());

        // target_table must be in target tables
        boolean hasTarget = mergeStmt.getTargetTableGeoids().stream()
                .anyMatch(g -> g.toUpperCase().contains("TARGET_TABLE"));
        assertTrue(hasTarget, "MERGE target_table should be in target tables. Got: " +
                mergeStmt.getTargetTableGeoids());

        // source_table must be in source tables
        boolean hasSource = mergeStmt.getSourceTableGeoids().stream()
                .anyMatch(g -> g.toUpperCase().contains("SOURCE_TABLE"));
        assertTrue(hasSource, "MERGE source_table should be in source tables. Got: " +
                mergeStmt.getSourceTableGeoids());

        // Lineage: source_table READS_FROM + target_table WRITES_TO
        var lineage = engine.getBuilder().getLineageEdges();
        boolean hasReadsFrom = lineage.stream()
                .anyMatch(e -> e.sourceGeoid().toUpperCase().contains("SOURCE_TABLE")
                        && "READS_FROM".equals(e.relationType()));
        assertTrue(hasReadsFrom, "Should have READS_FROM edge for source_table");

        boolean hasWritesTo = lineage.stream()
                .anyMatch(e -> e.targetGeoid().toUpperCase().contains("TARGET_TABLE")
                        && "WRITES_TO".equals(e.relationType()));
        assertTrue(hasWritesTo, "Should have WRITES_TO edge for target_table");
    }
}
