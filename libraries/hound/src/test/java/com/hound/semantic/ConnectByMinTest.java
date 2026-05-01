package com.hound.semantic;

import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.semantic.dialect.plsql.PlSqlSemanticListener;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TC-CB-MIN-01 — минимальный repro для START WITH / CONNECT BY в CTE.
 * Использует тот же путь парсинга что и FactFinancePackageTest.
 */
@Tag("plsql_parse")
class ConnectByMinTest {

    private List<String> parseFixture(String resource) throws Exception {
        String sql;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(is, "fixture not on classpath: " + resource);
            sql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
        // No preprocessor — grammar handles SQL*Plus via sql_plus_command rule

        List<String> errors = new ArrayList<>();
        BaseErrorListener errListener = new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> r, Object o,
                                    int line, int charPos,
                                    String msg, RecognitionException ex) {
                errors.add("line " + line + ":" + charPos + " — " + msg);
            }
        };

        UniversalSemanticEngine engine = new UniversalSemanticEngine();
        PlSqlSemanticListener listener = new PlSqlSemanticListener(engine);
        listener.setDefaultSchema("TEST");

        PlSqlLexer lexer = new PlSqlLexer(CharStreams.fromString(sql));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errListener);

        PlSqlParser parser = new PlSqlParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(errListener);

        new ParseTreeWalker().walk(listener, parser.sql_script());

        System.out.println("[CB-MIN " + resource + "] errors=" + errors.size());
        errors.forEach(e -> System.out.println("  " + e));
        return errors;
    }

    @Test
    void minimalCteWithStartWithConnectBy_parsesWithoutErrors() throws Exception {
        List<String> errors = parseFixture("plsql/connectby/cte_hierarchy_min.pls");
        assertTrue(errors.isEmpty(),
                "minimal CTE with START WITH must parse without errors; got: " + errors);
    }

    @Test
    void selectOnlyStartWithConnectBy_parsesWithoutErrors() throws Exception {
        List<String> errors = parseFixture("plsql/connectby/select_only_min.pls");
        assertTrue(errors.isEmpty(),
                "minimal SELECT with START WITH (no CTE) must parse without errors; got: " + errors);
    }

    @Test
    void selectNoWhereStartWithConnectBy_parsesWithoutErrors() throws Exception {
        List<String> errors = parseFixture("plsql/connectby/no_where_min.pls");
        assertTrue(errors.isEmpty(),
                "minimal SELECT without WHERE but with START WITH must parse without errors; got: " + errors);
    }
}
