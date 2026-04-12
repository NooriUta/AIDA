package com.hound.semantic.dialect;

import com.hound.semantic.engine.UniversalSemanticEngine;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * Интерфейс адаптера для SQL-диалекта.
 *
 * Каждый диалект реализует этот интерфейс.
 * Адаптер создаёт ANTLR ParseTreeListener, который маппит
 * ANTLR events → вызовы UniversalSemanticEngine.
 *
 * Для нового диалекта: реализовать этот интерфейс + listener.
 */
public interface DialectAdapter {

    /**
     * Имя диалекта (plsql, pgsql, mysql, tsql, ...)
     */
    String getDialectName();

    /**
     * Создаёт ANTLR ParseTreeListener, привязанный к engine.
     * Listener маппит ANTLR enter/exit events → engine.onXxx() вызовы.
     *
     * @param engine семантический движок (передаётся при каждом вызове,
     *               потому что engine создаётся per-file)
     * @return ParseTreeListener для ANTLR ParseTreeWalker
     */
    ParseTreeListener createListener(UniversalSemanticEngine engine);
}
