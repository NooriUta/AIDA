// File: src/main/java/com/hound/semantic/engine/ScopeContext.java
package com.hound.semantic.engine;

import java.util.HashMap;
import java.util.Map;

/**
 * ScopeContext — текущее состояние разбора.
 *
 * GEOID — иерархический идентификатор.
 * Разделитель ":" для execution hierarchy (routine → statement → subquery).
 *
 * Routine geoid примеры:
 *   PACKAGE:  "TEST_PKG"
 *   В пакете: "TEST_PKG:PROCEDURE:HELLO"
 *   Вложенная: "TEST_PKG:PROCEDURE:OUTER:FUNCTION:INNER"
 *   Без пакета: "PROCEDURE:STANDALONE"
 *   Вложенная без пакета: "PROCEDURE:OUTER:FUNCTION:LOCAL_HELPER"
 *
 * Statement geoid примеры:
 *   DML:      "{routine_or_parent}:SELECT:48"
 *   CTE:      "{parent}:MONTHLY_REVENUE:58"
 *   SUBQUERY: "{parent}:SQ:ALIAS:89" или "{parent}:SQ:89"
 */
public class ScopeContext {

    private String statementGeoid;
    private String statementType;
    private String routineGeoid;
    private String schemaGeoid;
    private String packageGeoid;
    private String parentStatement;
    private String targetTable;
    private String alias;

    private boolean isInDmlTarget = false;
    private boolean isInJoinContext = false;
    private boolean isInValuesClause = false;
    private boolean isInUpdateSetExpr = false;   // right-hand side of UPDATE SET col=expr
    private boolean isMergeInsertPart = false;
    private boolean isMergeUpdatePart = false;
    private boolean isUnion = false;

    private final Map<String, String> aliases = new HashMap<>();

    // ═══════ Factory: Statement ═══════

    /**
     * Создаёт ScopeContext для statement с иерархическим geoid.
     *
     * @param type            SELECT, INSERT, UPDATE, DELETE, MERGE, CTE, SUBQUERY, CURSOR, VIEW, ...
     * @param snippet         SQL text (для отладки)
     * @param lineStart       строка начала
     * @param lineEnd         строка конца
     * @param parentStmtGeoid geoid родительского statement (nullable)
     * @param routineGeoid    geoid текущей routine (nullable)
     * @param alias           имя CTE / alias subquery / cursor name (nullable)
     */
    public static ScopeContext forStatement(String type, String snippet, int lineStart, int lineEnd,
                                            String parentStmtGeoid, String routineGeoid, String alias) {
        ScopeContext ctx = new ScopeContext();
        ctx.statementType = type;
        ctx.parentStatement = parentStmtGeoid;
        ctx.routineGeoid = routineGeoid;

        // general_geoid: parent_statement → routine → null
        String general = parentStmtGeoid != null ? parentStmtGeoid
                : routineGeoid != null ? routineGeoid
                : null;

        ctx.alias = alias;
        ctx.statementGeoid = buildStatementGeoid(type, lineStart, general, alias);
        return ctx;
    }

    /** Backward-compatible: без parent/routine/alias */
    public static ScopeContext forStatement(String type, String snippet, int lineStart, int lineEnd) {
        return forStatement(type, snippet, lineStart, lineEnd, null, null, null);
    }

    /**
     * Формула geoid для statement (портирование Python PlSqlAnalyzerListener._init_statement).
     */
    private static String buildStatementGeoid(String type, int line, String general, String alias) {
        return switch (type) {
            case "CTE" -> {
                String name = upper(alias, "CTE");
                yield join(general, name + ":" + line);
            }
            case "SUBQUERY", "MSUBQUERY" -> {
                String sq = upper(alias) != null
                        ? "SQ:" + upper(alias) + ":" + line
                        : "SQ:" + line;
                yield join(general, sq);
            }
            case "USUBQUERY" -> {
                String usq = upper(alias) != null
                        ? "USQ:" + upper(alias) + ":" + line
                        : "USQ:" + line;
                yield join(general, usq);
            }
            case "CURSOR", "REF CURSOR" -> {
                String name = upper(alias, "CURSOR");
                yield join(general, name + ":" + line);
            }
            case "VIEW" -> upper(alias) != null ? upper(alias) : "VIEW:" + line;
            default ->  // SELECT, INSERT, UPDATE, DELETE, MERGE
                    join(general, type + ":" + line);
        };
    }

    // ═══════ Helpers ═══════

    /** Соединяет general:suffix, пропуская null general */
    private static String join(String general, String suffix) {
        return general != null ? general + ":" + suffix : suffix;
    }

    private static String upper(String s) {
        return (s != null && !s.isBlank()) ? s.trim().toUpperCase() : null;
    }

    private static String upper(String s, String fallback) {
        String u = upper(s);
        return u != null ? u : fallback;
    }

    // ═══════ Factory: Routine ═══════

    /**
     * Создаёт ScopeContext для routine (procedure/function).
     * Statements внутри routine наследуют routineGeoid.
     */
    public static ScopeContext forRoutine(String routineGeoid) {
        ScopeContext ctx = new ScopeContext();
        ctx.routineGeoid = routineGeoid;
        return ctx;
    }

    // ═══════ Alias / Resolve ═══════

    public void registerAlias(String alias, String geoid) {
        if (alias != null && geoid != null) aliases.put(alias.toUpperCase(), geoid);
    }
    public String resolveAlias(String alias) {
        return alias != null ? aliases.get(alias.toUpperCase()) : null;
    }

    // ═══════ Getters ═══════

    public String getStatementGeoid()  { return statementGeoid; }
    public String getStatementType()   { return statementType; }
    public String getRoutineGeoid()    { return routineGeoid; }
    public String getSchemaGeoid()     { return schemaGeoid; }
    public String getPackageGeoid()    { return packageGeoid; }
    public String getParentStatement() { return parentStatement; }
    public String getTargetTable()     { return targetTable; }
    public String getAlias()           { return alias; }
    public boolean isInDmlTarget()      { return isInDmlTarget; }
    public boolean isInJoinContext()     { return isInJoinContext; }
    public boolean isInValuesClause()   { return isInValuesClause; }
    public boolean isInUpdateSetExpr()  { return isInUpdateSetExpr; }
    public boolean isMergeInsertPart()  { return isMergeInsertPart; }
    public boolean isMergeUpdatePart()  { return isMergeUpdatePart; }
    public boolean isUnion()            { return isUnion; }
    public Map<String, String> getAliases() { return aliases; }

    public String getActiveClause() {
        if (isMergeInsertPart && isInValuesClause) return "MERGE_INSERT_VALUES";
        if (isInValuesClause)   return "VALUES";
        if (isInJoinContext)    return "JOIN";
        if (isInUpdateSetExpr)  return "SET_EXPR";
        if (isMergeInsertPart)  return "MERGE_INSERT";
        if (isMergeUpdatePart)  return "MERGE_UPDATE";
        return statementType != null ? statementType : "UNKNOWN";
    }

    // ═══════ Setters ═══════

    public void setParentStatement(String v)  { this.parentStatement = v; }
    public void setTargetTable(String v)      { this.targetTable = v; }
    public void setRoutineGeoid(String v)     { this.routineGeoid = v; }
    public void setStatementGeoid(String v)   { this.statementGeoid = v; }
    public void setSchemaGeoid(String v)      { this.schemaGeoid = v; }
    public void setPackageGeoid(String v)     { this.packageGeoid = v; }
    public void setInDmlTarget(boolean v)     { this.isInDmlTarget = v; }
    public void setInJoinContext(boolean v)    { this.isInJoinContext = v; }
    public void setInValuesClause(boolean v)  { this.isInValuesClause = v; }
    public void setInUpdateSetExpr(boolean v) { this.isInUpdateSetExpr = v; }
    public void setMergeInsertPart(boolean v) { this.isMergeInsertPart = v; }
    public void setMergeUpdatePart(boolean v) { this.isMergeUpdatePart = v; }
    public void setUnion(boolean v)           { this.isUnion = v; }

    @Override
    public String toString() {
        return "ScopeContext{" + statementGeoid + ", type=" + statementType
                + ", routine=" + routineGeoid + ", parent=" + parentStatement + "}";
    }
}