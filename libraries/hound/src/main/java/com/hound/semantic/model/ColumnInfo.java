// src/main/java/com/hound/semantic/model/ColumnInfo.java
package com.hound.semantic.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Информация о колонке
 */
public class ColumnInfo {

    private final String geoid;
    private final String tableGeoid;
    private final String columnName;
    private final String expression;
    private final String alias;
    private final boolean isOutput;
    private final int order;
    /** T13: 1-based ordinal position within the table (order of first appearance). 0 = unknown. */
    private final int ordinalPosition;
    private final Set<String> usedInStatements = new HashSet<>();
    private final Set<String> usedInRoutines = new HashSet<>();

    /** Backward-compatible constructor — ordinalPosition defaults to 0 (unknown). */
    public ColumnInfo(String geoid, String tableGeoid, String columnName, String expression,
                      String alias, boolean isOutput, int order) {
        this(geoid, tableGeoid, columnName, expression, alias, isOutput, order, 0);
    }

    public ColumnInfo(String geoid, String tableGeoid, String columnName, String expression,
                      String alias, boolean isOutput, int order, int ordinalPosition) {
        this.geoid = geoid;
        this.tableGeoid = tableGeoid;
        this.columnName = columnName;
        this.expression = expression;
        this.alias = alias;
        this.isOutput = isOutput;
        this.order = order;
        this.ordinalPosition = ordinalPosition;
    }

    // Getters
    public String getGeoid() { return geoid; }
    public String getTableGeoid() { return tableGeoid; }
    public String getColumnName() { return columnName; }
    public String getExpression() { return expression; }
    public String getAlias() { return alias; }
    public boolean isOutput() { return isOutput; }
    public int getOrder() { return order; }
    /** T13: 1-based ordinal position within the table. 0 means not assigned. */
    public int getOrdinalPosition() { return ordinalPosition; }
    public Set<String> getUsedInStatements() { return new HashSet<>(usedInStatements); }
    public Set<String> getUsedInRoutines() { return new HashSet<>(usedInRoutines); }

    public void addUsedInStatement(String statementGeoid) {
        if (statementGeoid != null) usedInStatements.add(statementGeoid);
    }

    public void addUsedInRoutine(String routineGeoid) {
        if (routineGeoid != null) usedInRoutines.add(routineGeoid);
    }
}
