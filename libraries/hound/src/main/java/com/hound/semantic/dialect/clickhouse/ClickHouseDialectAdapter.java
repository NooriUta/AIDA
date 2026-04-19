package com.hound.semantic.dialect.clickhouse;

import com.hound.semantic.dialect.DialectAdapter;
import com.hound.semantic.engine.UniversalSemanticEngine;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * ClickHouse Dialect Adapter.
 * Создаёт ClickHouseSemanticListener, привязанный к engine.
 */
public class ClickHouseDialectAdapter implements DialectAdapter {

    @Override
    public String getDialectName() {
        return "clickhouse";
    }

    @Override
    public ParseTreeListener createListener(UniversalSemanticEngine engine) {
        return new ClickHouseSemanticListener(engine);
    }
}
