// src/main/java/com/hound/semantic/model/Structure.java
package com.hound.semantic.model;

import java.util.Map;

/**
 * Контейнер всей структуры данных
 */
public class Structure {

    private final Map<String, Object> databases;
    private final Map<String, Object> schemas;
    private final Map<String, Object> packages;
    private final Map<String, TableInfo> tables;
    private final Map<String, ColumnInfo> columns;
    private final Map<String, RoutineInfo> routines;
    private final Map<String, StatementInfo> statements;

    public Structure(Map<String, Object> databases,
                     Map<String, Object> schemas,
                     Map<String, Object> packages,
                     Map<String, TableInfo> tables,
                     Map<String, ColumnInfo> columns,
                     Map<String, RoutineInfo> routines,
                     Map<String, StatementInfo> statements) {
        this.databases = databases != null ? databases : Map.of();
        this.schemas = schemas != null ? schemas : Map.of();
        this.packages = packages != null ? packages : Map.of();
        this.tables = tables != null ? tables : Map.of();
        this.columns = columns != null ? columns : Map.of();
        this.routines = routines != null ? routines : Map.of();
        this.statements = statements != null ? statements : Map.of();
    }

    public Map<String, Object> getDatabases() { return databases; }
    public Map<String, Object> getSchemas() { return schemas; }
    public Map<String, Object> getPackages() { return packages; }
    public Map<String, TableInfo> getTables() { return tables; }
    public Map<String, ColumnInfo> getColumns() { return columns; }
    public Map<String, StatementInfo> getStatements() { return statements; }
    public Map<String, RoutineInfo> getRoutines() { return routines; }
}