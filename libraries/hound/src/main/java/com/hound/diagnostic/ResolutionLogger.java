package com.hound.diagnostic;

import com.arcadedb.database.Database;
import com.arcadedb.remote.RemoteDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Пишет каждую попытку resolution в DaliResolutionLog.
 *
 * Используется ТОЛЬКО в режиме --diag для анализа качества resolution.
 * В prod-режиме — no-op (isEnabled = false).
 *
 * Sprint S0-STAB / STAB-1
 */
public class ResolutionLogger {

    private static final Logger log = LoggerFactory.getLogger(ResolutionLogger.class);

    public enum InputKind  { ATOM, COLUMN_REF, TABLE_REF, PENDING_COLUMN }
    public enum ResultKind { RESOLVED, UNRESOLVED, SKIPPED, ERROR }

    private final boolean enabled;
    private final String  sessionId;
    private final Object  db;  // Database (embedded) или RemoteDatabase

    public ResolutionLogger(boolean enabled, String sessionId, Object db) {
        this.enabled   = enabled;
        this.sessionId = sessionId;
        this.db        = db;
    }

    /** No-op instance — используется как default когда diag не активен */
    public static ResolutionLogger disabled() {
        return new ResolutionLogger(false, null, null);
    }

    /**
     * Главный метод — вызывается из NameResolver.resolve(), ensureTable(),
     * postProcessJoins(), resolvePendingColumns() и т.д.
     */
    public void log(
            InputKind  inputKind,
            String     rawInput,
            String     statementGeoid,
            String     strategy,
            ResultKind resultKind,
            String     resolvedGeoid,
            String     resolvedType,
            String     note
    ) {
        if (!enabled) return;

        boolean hasSpecialChars = rawInput != null
                && rawInput.matches(".*[()\\[\\]{}@#!%^&*].*");
        boolean isQualified     = rawInput != null && rawInput.contains(".");
        boolean isFunctionCall  = rawInput != null
                && rawInput.toUpperCase().matches("^[A-Z_]+\\(.*");

        String sql =
            "INSERT INTO DaliResolutionLog SET " +
            "session_id=?, ts=?, input_kind=?, raw_input=?, statement_geoid=?, " +
            "`strategy`=?, result_kind=?, resolved_geoid=?, resolved_type=?, " +
            "has_special_chars=?, is_qualified=?, is_function_call=?, note=?";

        Object[] params = {
            sessionId,
            System.currentTimeMillis(),
            inputKind.name(),
            rawInput,
            statementGeoid,
            strategy,
            resultKind.name(),
            resolvedGeoid,
            resolvedType,
            hasSpecialChars,
            isQualified,
            isFunctionCall,
            note
        };

        try {
            if (db instanceof Database d) {
                d.transaction(() -> d.command("sql", sql, params));
            } else if (db instanceof RemoteDatabase r) {
                r.command("sql", sql, params);
            }
        } catch (Exception e) {
            log.warn("ResolutionLogger write failed: {}", e.getMessage());
        }
    }

    public boolean isEnabled() { return enabled; }
    public String  getSessionId() { return sessionId; }
}
