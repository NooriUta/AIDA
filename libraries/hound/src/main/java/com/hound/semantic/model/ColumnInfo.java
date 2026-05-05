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
    /** DDL-declared data type: "VARCHAR2(100)", "NUMBER(10,2)", "DATE", etc. Null for DML-only columns. */
    private final String dataType;
    /** T14: true if column has NOT NULL constraint in DDL. False for DML-only or nullable columns. */
    private final boolean isRequired;
    /** T14: text of the DEFAULT expression from DDL (e.g. "'N'", "SYSTIMESTAMP", "USER"). Null if absent. */
    private final String defaultValue;
    private final Set<String> usedInStatements = new HashSet<>();
    private final Set<String> usedInRoutines = new HashSet<>();

    // ── inferred / suspicious markers (ADR-HND-006) — set post-construction ──
    private boolean inferred = false;
    private String sourcePass = null;
    private boolean suspicious = false;

    public void markAsInferred(String sourcePass) { this.inferred = true; this.sourcePass = sourcePass; }
    public void markAsSuspicious() { this.suspicious = true; }
    public boolean isInferred() { return inferred; }
    public String getSourcePass() { return sourcePass; }
    public boolean isSuspicious() { return suspicious; }

    // ── PK / FK markers — set post-construction by StructureAndLineageBuilder.addConstraint() ──
    /** True if this column is part of the table's PRIMARY KEY. Mutable — set after parsing out_of_line_constraint. */
    private boolean isPk = false;
    /** True if this column is a FOREIGN KEY column. Mutable — set after parsing out_of_line_constraint. */
    private boolean isFk = false;
    /** Geoid of the referenced table (FK only). Null for PK / DML-only columns. */
    private String fkRefTable  = null;
    /** Name of the referenced column in the parent table (FK only). Null for PK / DML-only columns. */
    private String fkRefColumn = null;

    /** Marks this column as participating in the primary key. */
    public void markAsPk() { this.isPk = true; }

    /** Marks this column as a foreign-key column with the given referenced table / column. */
    public void markAsFk(String refTable, String refColumn) {
        this.isFk       = true;
        this.fkRefTable  = refTable;
        this.fkRefColumn = refColumn;
    }

    /** Backward-compatible constructor — ordinalPosition defaults to 0, dataType/isRequired/defaultValue to null/false/null. */
    public ColumnInfo(String geoid, String tableGeoid, String columnName, String expression,
                      String alias, boolean isOutput, int order) {
        this(geoid, tableGeoid, columnName, expression, alias, isOutput, order, 0, null, false, null);
    }

    /** Backward-compatible constructor — dataType/isRequired/defaultValue to null/false/null. */
    public ColumnInfo(String geoid, String tableGeoid, String columnName, String expression,
                      String alias, boolean isOutput, int order, int ordinalPosition) {
        this(geoid, tableGeoid, columnName, expression, alias, isOutput, order, ordinalPosition, null, false, null);
    }

    /** Backward-compatible constructor — isRequired/defaultValue to false/null. */
    public ColumnInfo(String geoid, String tableGeoid, String columnName, String expression,
                      String alias, boolean isOutput, int order, int ordinalPosition, String dataType) {
        this(geoid, tableGeoid, columnName, expression, alias, isOutput, order, ordinalPosition, dataType, false, null);
    }

    public ColumnInfo(String geoid, String tableGeoid, String columnName, String expression,
                      String alias, boolean isOutput, int order, int ordinalPosition, String dataType,
                      boolean isRequired, String defaultValue) {
        this.geoid = geoid;
        this.tableGeoid = tableGeoid;
        this.columnName = columnName;
        this.expression = expression;
        this.alias = alias;
        this.isOutput = isOutput;
        this.order = order;
        this.ordinalPosition = ordinalPosition;
        this.dataType = dataType;
        this.isRequired = isRequired;
        this.defaultValue = defaultValue;
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
    /** DDL-declared data type string, e.g. "VARCHAR2(100)". Null for DML-only columns. */
    public String getDataType() { return dataType; }
    /** T14: true if the column has NOT NULL constraint declared in DDL. */
    public boolean isRequired() { return isRequired; }
    /** T14: DEFAULT expression text from DDL, e.g. "'N'", "SYSTIMESTAMP". Null if no DEFAULT. */
    public String getDefaultValue() { return defaultValue; }
    /** True if this column participates in the table's PRIMARY KEY. */
    public boolean isPk()          { return isPk; }
    /** True if this column is a FOREIGN KEY column referencing another table. */
    public boolean isFk()          { return isFk; }
    /** Geoid of the referenced table (for FK columns); null otherwise. */
    public String  getFkRefTable() { return fkRefTable; }
    /** Name of the referenced column in the parent table (for FK columns); null otherwise. */
    public String  getFkRefColumn(){ return fkRefColumn; }
    public Set<String> getUsedInStatements() { return new HashSet<>(usedInStatements); }
    public Set<String> getUsedInRoutines() { return new HashSet<>(usedInRoutines); }

    public void addUsedInStatement(String statementGeoid) {
        if (statementGeoid != null) usedInStatements.add(statementGeoid);
    }

    public void addUsedInRoutine(String routineGeoid) {
        if (routineGeoid != null) usedInRoutines.add(routineGeoid);
    }
}
