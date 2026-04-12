package com.hound.semantic;

import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.semantic.dialect.plsql.PlSqlSemanticListener;
import com.hound.semantic.model.StatementInfo;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that sequential DML statements in a procedure get independent GEOIDs.
 * Bug: INSERT with WITH-clause containing nested subqueries caused scope stack
 * corruption — the MERGE after it inherited INSERT's GEOID as parent.
 */
class GeoidParentChainTest {

    private UniversalSemanticEngine parse(String sql) {
        UniversalSemanticEngine engine = new UniversalSemanticEngine();
        PlSqlSemanticListener listener = new PlSqlSemanticListener(engine);
        listener.setDefaultSchema("DWH");
        PlSqlLexer lexer = new PlSqlLexer(CharStreams.fromString(sql));
        PlSqlParser parser = new PlSqlParser(new CommonTokenStream(lexer));
        new ParseTreeWalker().walk(listener, parser.sql_script());
        engine.resolvePendingColumns();
        return engine;
    }

    @Test
    void insertWithCte_thenMerge_geoidsMustBeIndependent() {
        // Reproduces the pattern from DWH.PK_PKL_CRD_CALC.DEALACCPCTCOM:
        // INSERT with WITH clause (nested subqueries in CTE), followed by MERGE.
        String sql = """
            CREATE OR REPLACE PACKAGE BODY PK_TEST IS
              PROCEDURE PROC1 IS
              BEGIN
                INSERT INTO target_tbl(col1, col2)
                  WITH c AS (
                    SELECT id FROM src1 WHERE id > 0
                  ),
                  a AS (
                    SELECT c.id, x.val
                      FROM c
                      JOIN (SELECT id, val FROM src2) x ON x.id = c.id
                  )
                  SELECT c.id, a.val FROM c JOIN a ON a.id = c.id;

                MERGE INTO target_tbl t
                  USING (
                    WITH d AS (
                      SELECT id FROM src3
                    )
                    SELECT d.id, s.val FROM d JOIN src4 s ON s.id = d.id
                  ) src ON (t.col1 = src.id)
                  WHEN MATCHED THEN UPDATE SET t.col2 = src.val
                  WHEN NOT MATCHED THEN INSERT (col1, col2) VALUES (src.id, src.val);
              END;
            END;
            """;

        var engine = parse(sql);
        var stmts = engine.getBuilder().getStatements();

        // Find the top-level INSERT and MERGE
        String insertGeoid = null, mergeGeoid = null;
        for (var e : stmts.entrySet()) {
            String g = e.getKey();
            String type = e.getValue().getType();
            if ("INSERT".equals(type) && !g.contains("SQ:") && !g.contains("USQ:")) {
                insertGeoid = g;
            }
            if ("MERGE".equals(type)) {
                mergeGeoid = g;
            }
        }

        assertNotNull(insertGeoid, "INSERT statement must exist");
        assertNotNull(mergeGeoid, "MERGE statement must exist");

        // The critical assertion: MERGE GEOID must NOT contain INSERT
        assertFalse(mergeGeoid.contains("INSERT"),
            "MERGE geoid must not contain INSERT. Got: " + mergeGeoid);

        // MERGE parent must be null (top-level in routine), not the INSERT
        StatementInfo mergeInfo = stmts.get(mergeGeoid);
        assertNull(mergeInfo.getParentStatementGeoid(),
            "MERGE parent must be null (sibling to INSERT, not child). Got: "
            + mergeInfo.getParentStatementGeoid());

        System.out.println("INSERT geoid: " + insertGeoid);
        System.out.println("MERGE  geoid: " + mergeGeoid);
    }
}
