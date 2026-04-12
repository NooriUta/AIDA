package com.hound.semantic.dialect.plsql;

import com.hound.semantic.dialect.DialectAdapter;
import com.hound.semantic.engine.UniversalSemanticEngine;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * PL/SQL Dialect Adapter.
 * Создаёт PlSqlSemanticListener, привязанный к engine.
 */
public class PlSqlDialectAdapter implements DialectAdapter {

    @Override
    public String getDialectName() {
        return "plsql";
    }

    @Override
    public ParseTreeListener createListener(UniversalSemanticEngine engine) {
        return new PlSqlSemanticListener(engine);
    }
}