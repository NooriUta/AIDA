// src/main/java/com/hound/semantic/model/Structure.java
package com.hound.semantic.model;

import java.util.Map;
import java.util.Set;

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
    private final Map<String, RecordInfo> records;
    /**
     * Geoids of tables/views *defined* by DDL in this session (CREATE TABLE, ALTER TABLE,
     * CREATE VIEW…). Used by WriteHelpers.isMasterTable() to assign data_source='master'
     * without scanning StatementInfo.targetTables.
     */
    private final Set<String> ddlTableGeoids;
    /**
     * Constraints (PK, FK, UQ, CH) parsed from DDL in this session.
     * Key = constraint geoid (e.g. "CRM.CUSTOMERS#PK", "CRM.CUSTOMER_ADDRESSES#FK#FK_CRM_CADDR_CUST_ID").
     */
    private final Map<String, ConstraintInfo> constraints;
    /** HND-02: PL/SQL TYPE IS RECORD / TABLE OF templates. Key = type geoid. */
    private final Map<String, PlTypeInfo> plTypes;

    /** Backward-compatible constructor — records, ddlTableGeoids, constraints default to empty. */
    public Structure(Map<String, Object> databases,
                     Map<String, Object> schemas,
                     Map<String, Object> packages,
                     Map<String, TableInfo> tables,
                     Map<String, ColumnInfo> columns,
                     Map<String, RoutineInfo> routines,
                     Map<String, StatementInfo> statements) {
        this(databases, schemas, packages, tables, columns, routines, statements, null, null, null);
    }

    /** Backward-compatible constructor — ddlTableGeoids and constraints default to empty. */
    public Structure(Map<String, Object> databases,
                     Map<String, Object> schemas,
                     Map<String, Object> packages,
                     Map<String, TableInfo> tables,
                     Map<String, ColumnInfo> columns,
                     Map<String, RoutineInfo> routines,
                     Map<String, StatementInfo> statements,
                     Map<String, RecordInfo> records) {
        this(databases, schemas, packages, tables, columns, routines, statements, records, null, null);
    }

    /** Backward-compatible constructor — constraints defaults to empty. */
    public Structure(Map<String, Object> databases,
                     Map<String, Object> schemas,
                     Map<String, Object> packages,
                     Map<String, TableInfo> tables,
                     Map<String, ColumnInfo> columns,
                     Map<String, RoutineInfo> routines,
                     Map<String, StatementInfo> statements,
                     Map<String, RecordInfo> records,
                     Set<String> ddlTableGeoids) {
        this(databases, schemas, packages, tables, columns, routines, statements, records, ddlTableGeoids, null);
    }

    public Structure(Map<String, Object> databases,
                     Map<String, Object> schemas,
                     Map<String, Object> packages,
                     Map<String, TableInfo> tables,
                     Map<String, ColumnInfo> columns,
                     Map<String, RoutineInfo> routines,
                     Map<String, StatementInfo> statements,
                     Map<String, RecordInfo> records,
                     Set<String> ddlTableGeoids,
                     Map<String, ConstraintInfo> constraints) {
        this(databases, schemas, packages, tables, columns, routines, statements, records, ddlTableGeoids, constraints, null);
    }

    public Structure(Map<String, Object> databases,
                     Map<String, Object> schemas,
                     Map<String, Object> packages,
                     Map<String, TableInfo> tables,
                     Map<String, ColumnInfo> columns,
                     Map<String, RoutineInfo> routines,
                     Map<String, StatementInfo> statements,
                     Map<String, RecordInfo> records,
                     Set<String> ddlTableGeoids,
                     Map<String, ConstraintInfo> constraints,
                     Map<String, PlTypeInfo> plTypes) {
        this.databases      = databases      != null ? databases      : Map.of();
        this.schemas        = schemas        != null ? schemas        : Map.of();
        this.packages       = packages       != null ? packages       : Map.of();
        this.tables         = tables         != null ? tables         : Map.of();
        this.columns        = columns        != null ? columns        : Map.of();
        this.routines       = routines       != null ? routines       : Map.of();
        this.statements     = statements     != null ? statements     : Map.of();
        this.records        = records        != null ? records        : Map.of();
        this.ddlTableGeoids = ddlTableGeoids != null ? ddlTableGeoids : Set.of();
        this.constraints    = constraints    != null ? constraints    : Map.of();
        this.plTypes        = plTypes        != null ? plTypes        : Map.of();
    }

    public Map<String, Object> getDatabases()              { return databases; }
    public Map<String, Object> getSchemas()                { return schemas; }
    public Map<String, Object> getPackages()               { return packages; }
    public Map<String, TableInfo> getTables()              { return tables; }
    public Map<String, ColumnInfo> getColumns()            { return columns; }
    public Map<String, StatementInfo> getStatements()      { return statements; }
    public Map<String, RoutineInfo> getRoutines()          { return routines; }
    public Map<String, RecordInfo> getRecords()            { return records; }
    /** Tables/views defined by DDL in this session — data_source='master'. */
    public Set<String> getDdlTableGeoids()                 { return ddlTableGeoids; }
    /** Constraints (PK, FK, UQ, CH) parsed from DDL in this session. */
    public Map<String, ConstraintInfo> getConstraints()    { return constraints; }
    /** HND-02: PL/SQL TYPE templates (RECORD / COLLECTION) declared in this session. */
    public Map<String, PlTypeInfo> getPlTypes()            { return plTypes; }
}
