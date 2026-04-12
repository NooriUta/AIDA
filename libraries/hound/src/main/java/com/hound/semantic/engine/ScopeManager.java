package com.hound.semantic.engine;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

/**
 * ScopeManager — управляет стеком контекстов и текущим состоянием разбора.
 * Заменяет Python _current dict + subquery_alias_stack.
 */
public class ScopeManager {

    private final Deque<ScopeContext> scopeStack = new ArrayDeque<>();

    /**
     * Вход в новый scope (statement, subquery, routine и т.д.)
     */
    public void push(ScopeContext context) {
        scopeStack.push(context);
    }

    /**
     * Выход из текущего scope
     */
    public ScopeContext pop() {
        return scopeStack.isEmpty() ? null : scopeStack.pop();
    }

    /**
     * Текущий контекст (без удаления)
     */
    public ScopeContext peek() {
        return scopeStack.isEmpty() ? null : scopeStack.peek();
    }

    public boolean isEmpty() {
        return scopeStack.isEmpty();
    }

    /**
     * Полная очистка стека (используется вместо пересоздания объекта)
     */
    public void clear() {
        scopeStack.clear();
    }

    // ====================== Удобные геттеры ======================

    public String currentStatement() {
        ScopeContext ctx = peek();
        return ctx != null ? ctx.getStatementGeoid() : null;
    }

    public String currentRoutine() {
        ScopeContext ctx = peek();
        return ctx != null ? ctx.getRoutineGeoid() : null;
    }

    public String currentSchema() {
        ScopeContext ctx = peek();
        return ctx != null ? ctx.getSchemaGeoid() : null;
    }

    public boolean isInDmlTarget() {
        ScopeContext ctx = peek();
        return ctx != null && ctx.isInDmlTarget();
    }

    public boolean isInJoinContext() {
        ScopeContext ctx = peek();
        return ctx != null && ctx.isInJoinContext();
    }

    public boolean isInValuesClause() {
        ScopeContext ctx = peek();
        return ctx != null && ctx.isInValuesClause();
    }

    public void setInValuesClause(boolean val) {
        ScopeContext ctx = peek();
        if (ctx != null) ctx.setInValuesClause(val);
    }

    public void setInUpdateSetExpr(boolean val) {
        ScopeContext ctx = peek();
        if (ctx != null) ctx.setInUpdateSetExpr(val);
    }

    public String getActiveClause() {
        ScopeContext ctx = peek();
        return ctx != null ? ctx.getActiveClause() : "UNKNOWN";
    }

    /**
     * Регистрация алиаса в текущем scope
     */
    public void registerAlias(String alias, String geoid) {
        ScopeContext ctx = peek();
        if (ctx != null) {
            ctx.registerAlias(alias, geoid);
        }
    }

    public String resolveAlias(String alias) {
        ScopeContext ctx = peek();
        return ctx != null ? ctx.resolveAlias(alias) : null;
    }

    /**
     * Регистрация алиаса на РОДИТЕЛЬСКОМ scope (второй элемент стека).
     * Используется для MERGE INTO (subquery) alias — алиас подзапроса
     * регистрируется на scope MERGE, а не на scope самого подзапроса.
     */
    public void registerAliasOnParent(String alias, String geoid) {
        if (scopeStack.size() < 2) return;
        var it = scopeStack.iterator();
        it.next(); // skip current (top)
        ScopeContext parent = it.next();
        parent.registerAlias(alias, geoid);
    }

    public int depth() {
        return scopeStack.size();
    }

    // ====================== Cursor registry (routine-scoped) ======================

    /** cursor_name_upper → cursor statement geoid */
    private final Map<String, String> cursorRegistry = new java.util.HashMap<>();

    /** record_var_upper → cursor statement geoid (rec is an alias for the cursor scope) */
    private final Map<String, String> cursorRecordAliases = new java.util.HashMap<>();

    /** Called on cursor declaration exit: registers cursor name → its statement geoid. */
    public void registerCursor(String cursorName, String statementGeoid) {
        if (cursorName != null && statementGeoid != null)
            cursorRegistry.put(cursorName.toUpperCase(), statementGeoid);
    }

    /**
     * Called when entering a cursor FOR loop.
     * For named cursor: resolves via cursorRegistry.
     * For inline cursor: stmtGeoid is the DINAMIC_CURSOR geoid passed directly.
     */
    public void registerCursorRecord(String recordVar, String cursorNameOrStmtGeoid, boolean isInline) {
        if (recordVar == null || cursorNameOrStmtGeoid == null) return;
        String stmtGeoid = isInline
                ? cursorNameOrStmtGeoid
                : cursorRegistry.get(cursorNameOrStmtGeoid.toUpperCase());
        if (stmtGeoid != null)
            cursorRecordAliases.put(recordVar.toUpperCase(), stmtGeoid);
    }

    /** Returns the cursor statement geoid for the given record variable, or null if not a cursor record. */
    public String getCursorRecordStmt(String recordVar) {
        return recordVar != null ? cursorRecordAliases.get(recordVar.toUpperCase()) : null;
    }

    /** Clears cursor state — called on routine exit. */
    public void clearCursorState() {
        cursorRegistry.clear();
        cursorRecordAliases.clear();
    }

    @Override
    public String toString() {
        return "ScopeManager{depth=" + depth() + ", currentStatement=" + currentStatement() + "}";
    }
}