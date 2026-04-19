package com.hound.semantic.dialect.postgresql;

import com.hound.semantic.dialect.DialectAdapter;
import com.hound.semantic.engine.UniversalSemanticEngine;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * PostgreSQL Dialect Adapter.
 * Создаёт PostgreSQLSemanticListener, привязанный к engine.
 */
public class PostgreSQLDialectAdapter implements DialectAdapter {

    @Override
    public String getDialectName() {
        return "postgresql";
    }

    @Override
    public ParseTreeListener createListener(UniversalSemanticEngine engine) {
        return new PostgreSQLSemanticListener(engine);
    }
}
