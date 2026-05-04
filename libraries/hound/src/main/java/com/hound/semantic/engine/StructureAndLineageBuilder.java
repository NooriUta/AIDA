package com.hound.semantic.engine;

import com.hound.api.HoundEventListener;
import com.hound.api.NoOpHoundEventListener;
import com.hound.diagnostic.ResolutionLogger;
import com.hound.semantic.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * StructureAndLineageBuilder — builds structure (tables, columns, statements, routines)
 * and lineage edges.
 * Портирование Python: _init_table, _init_schema, _init_database, _add_column_to_table,
 * _mark_table_as_target/source, и т.д.
 */
public class StructureAndLineageBuilder {

    private static final Logger logger = LoggerFactory.getLogger(StructureAndLineageBuilder.class);

    private final Map<String, Object> databases = new LinkedHashMap<>();
    private final Map<String, Object> schemas = new LinkedHashMap<>();
    private final Map<String, Object> packages = new LinkedHashMap<>();
    private final Map<String, TableInfo> tables = new LinkedHashMap<>();
    private final Map<String, ColumnInfo> columns = new LinkedHashMap<>();
    private final Map<String, StatementInfo> statements = new LinkedHashMap<>();
    private final Map<String, RoutineInfo> routines = new LinkedHashMap<>();
    private final Map<String, RecordInfo> records = new LinkedHashMap<>();
    private final List<LineageEdge> lineageEdges = new ArrayList<>();

    /**
     * Geoids of tables/views explicitly *defined* in this session via DDL
     * (CREATE TABLE, ALTER TABLE, CREATE VIEW, etc.).
     * Used by WriteHelpers.isMasterTable() to assign data_source='master'.
     * Populated by markDdlTable() — does NOT depend on StatementInfo.targetTables.
     */
    private final Set<String> ddlTableGeoids = new LinkedHashSet<>();

    /**
     * FOREIGN KEY constraints parsed from DDL in this session.
     * Key = constraint geoid (e.g. "CRM.CUSTOMER_ADDRESSES#FK#FK_CRM_CADDR_CUST_ID").
     */
    private final Map<String, ConstraintInfo> constraints = new LinkedHashMap<>();

    /** HND-02: PL/SQL TYPE IS RECORD / TABLE OF templates. Key = type geoid. */
    private final Map<String, PlTypeInfo> plTypes = new LinkedHashMap<>();

    /** HAL3-01: write-side edges accumulated during parse, written after main batch. */
    private final List<CompensationStats> compensationStats = new ArrayList<>();

    /** HAL2-01: synthetic table geoids where PIPELINED column injection failed (PlType not yet available). */
    private final Set<String> pendingPipelinedTables = new LinkedHashSet<>();

    /** HAL2-01: table geoids referenced by %ROWTYPE where DDL columns were not found at parse time. */
    private final Set<String> pendingRowtypeTables = new LinkedHashSet<>();

    /** HAL2-01: statement geoids containing CAST(MULTISET) where target type is unresolved. */
    private final Set<String> pendingMultisetStmts = new LinkedHashSet<>();

    // STAB-2: диагностический логгер (null = prod-режим, no-op)
    private ResolutionLogger resolutionLogger;

    // S1.SCH: log of suspicious schema registrations with call backtrace
    private final List<Map<String, Object>> schemaRegistrationLog = new ArrayList<>();

    // C.1.3: event listener
    private final HoundEventListener listener;
    private final String file;

    /** Backward-compatible no-arg constructor. */
    public StructureAndLineageBuilder() {
        this(NoOpHoundEventListener.INSTANCE, "");
    }

    public StructureAndLineageBuilder(HoundEventListener listener, String file) {
        this.listener = listener != null ? listener : NoOpHoundEventListener.INSTANCE;
        this.file = file != null ? file : "";
    }

    public void setResolutionLogger(ResolutionLogger rl) { this.resolutionLogger = rl; }

    // ═══════ Tables ═══════

    /**
     * Создаёт таблицу если не существует. Возвращает geoid.
     * Аналог Python: _init_table / ensureTable
     */
    public String ensureTable(String tableName, String schemaGeoid) {
        String upperName = tableName.toUpperCase();
        String resolvedSchema = schemaGeoid;

        // Strip schema prefix from qualified name: "DWH.DIM_CUSTOMER" → table "DIM_CUSTOMER".
        // This applies unconditionally — even if resolvedSchema is already set — to prevent
        // double-schema geoids like "DWH.DWH.DIM_CUSTOMER" when the caller passes the fully-
        // qualified name AND a non-empty schemaGeoid at the same time.
        if (upperName.contains(".")) {
            String[] parts = upperName.split("\\.");
            upperName = parts[parts.length - 1];
            String embeddedSchema = String.join(".", Arrays.copyOf(parts, parts.length - 1));
            if (resolvedSchema == null || resolvedSchema.isBlank()) {
                resolvedSchema = embeddedSchema;
                ensureSchema(resolvedSchema, null);
            }
            // else: keep caller-supplied resolvedSchema (typically same value), just use clean table name
        }

        String geoid = (resolvedSchema != null && !resolvedSchema.isBlank())
                ? resolvedSchema.toUpperCase() + "." + upperName
                : upperName;

        // STAB-2: предупреждение и лог о подозрительных именах таблиц
        if (resolutionLogger != null && resolutionLogger.isEnabled()) {
            boolean hasSpecial = upperName.matches(".*[()\\[\\]{}!%^&*].*");
            boolean isFunc     = upperName.matches("^[A-Z_]+\\(.*");
            if (hasSpecial || isFunc) {
                resolutionLogger.log(
                    ResolutionLogger.InputKind.TABLE_REF,
                    upperName, null, "ensureTable",
                    isFunc ? ResolutionLogger.ResultKind.SKIPPED
                           : ResolutionLogger.ResultKind.UNRESOLVED,
                    null, null,
                    isFunc ? "function_call_as_table" : "special_chars_in_table_name"
                );
                logger.warn("STAB: suspicious table name: '{}' (func={}, special={})",
                        upperName, isFunc, hasSpecial);
                listener.onSemanticWarning(file, "STAB_SUSPICIOUS",
                        "Suspicious table name: '" + upperName
                                + "' (func=" + isFunc + ", special=" + hasSpecial + ")");
            }
        }

        String finalUpperName = upperName;
        String finalSchema = resolvedSchema;
        tables.computeIfAbsent(geoid, k -> {
            logger.debug("New table registered: {}", geoid);
            return new TableInfo(geoid, finalUpperName, finalSchema, "TABLE");
        });
        return geoid;
    }

    /** STAB-8: ensureTable с явным tableType (для VIEW, CTE, TEMP, …). */
    public String ensureTableWithType(String tableName, String schemaGeoid, String tableType) {
        String upperName = tableName.toUpperCase();
        String resolvedSchema = schemaGeoid;
        if (upperName.contains(".")) {
            String[] parts = upperName.split("\\.");
            upperName = parts[parts.length - 1];
            String embeddedSchema = String.join(".", Arrays.copyOf(parts, parts.length - 1));
            if (resolvedSchema == null || resolvedSchema.isBlank()) {
                resolvedSchema = embeddedSchema;
                ensureSchema(resolvedSchema, null);
            }
            // else: resolvedSchema supplied by caller — keep it, just use clean table name
        }
        String geoid = (resolvedSchema != null && !resolvedSchema.isBlank())
                ? resolvedSchema.toUpperCase() + "." + upperName
                : upperName;
        String finalUpperName = upperName;
        String finalSchema = resolvedSchema;
        String finalType = tableType != null ? tableType : "TABLE";
        tables.computeIfAbsent(geoid, k -> {
            logger.debug("New {} registered: {}", finalType, geoid);
            return new TableInfo(geoid, finalUpperName, finalSchema, finalType);
        });
        return geoid;
    }

    public void addTableAlias(String tableGeoid, String alias) {
        TableInfo t = tables.get(tableGeoid);
        if (t != null && alias != null) {
            t.addAlias(alias.toUpperCase());
        }
    }

    public Map<String, TableInfo> getTables() {
        return tables;
    }

    // ═══════ Columns ═══════

    public void addColumn(String tableGeoid, String columnName, String expression, String alias) {
        addColumn(tableGeoid, columnName, expression, alias, null);
    }

    public void addInferredColumn(String tableGeoid, String columnName, String sourcePass) {
        String upperCol = columnName.toUpperCase();
        String geoid = tableGeoid + "." + upperCol;
        boolean[] created = {false};
        columns.computeIfAbsent(geoid, k -> {
            created[0] = true;
            TableInfo t = tables.get(tableGeoid);
            int ordinal = 0;
            if (t != null) { t.incrementColumnCount(); ordinal = t.columnCount(); }
            return new ColumnInfo(geoid, tableGeoid, upperCol, null, null, false, 0, ordinal, null, false, null);
        });
        if (created[0]) {
            columns.get(geoid).markAsInferred(sourcePass);
        }
    }

    public void addColumn(String tableGeoid, String columnName, String expression, String alias,
                          String dataType) {
        addColumn(tableGeoid, columnName, expression, alias, dataType, false, null);
    }

    public void addColumn(String tableGeoid, String columnName, String expression, String alias,
                          String dataType, boolean isRequired, String defaultValue) {
        String upperCol = columnName.toUpperCase();
        String geoid = tableGeoid + "." + upperCol;
        columns.computeIfAbsent(geoid, k -> {
            TableInfo t = tables.get(tableGeoid);
            int ordinal = 0;
            if (t != null) {
                t.incrementColumnCount();
                ordinal = t.columnCount(); // 1-based: count after increment
            }
            return new ColumnInfo(geoid, tableGeoid, upperCol, expression, alias, false, 0,
                    ordinal, dataType, isRequired, defaultValue);
        });
    }

    /**
     * T14: Registers a column with an explicit ordinal position (e.g., from CREATE TABLE DDL).
     * If the column already exists, it is not overwritten — DDL registration wins first time only.
     */
    public void addColumnWithOrdinal(String tableGeoid, String columnName,
                                     String expression, String alias, int ordinalPosition) {
        addColumnWithOrdinal(tableGeoid, columnName, expression, alias, ordinalPosition, null, false, null);
    }

    public void addColumnWithOrdinal(String tableGeoid, String columnName,
                                     String expression, String alias, int ordinalPosition,
                                     String dataType) {
        addColumnWithOrdinal(tableGeoid, columnName, expression, alias, ordinalPosition, dataType, false, null);
    }

    public void addColumnWithOrdinal(String tableGeoid, String columnName,
                                     String expression, String alias, int ordinalPosition,
                                     String dataType, boolean isRequired, String defaultValue) {
        String upperCol = columnName.toUpperCase();
        String geoid = tableGeoid + "." + upperCol;
        columns.computeIfAbsent(geoid, k -> {
            TableInfo t = tables.get(tableGeoid);
            if (t != null) t.incrementColumnCount();
            return new ColumnInfo(geoid, tableGeoid, upperCol, expression, alias, false, 0,
                    ordinalPosition, dataType, isRequired, defaultValue);
        });
    }

    public Map<String, ColumnInfo> getColumns() { return columns; }

    // ═══════ DDL table registry ═══════

    /**
     * Marks a table geoid as DDL-defined (CREATE TABLE, ALTER TABLE, CREATE VIEW…).
     * Called by initDdlTable() in BaseSemanticListener — the single registration point
     * for all DDL table targets. WriteHelpers.isMasterTable() reads this set.
     */
    public void markDdlTable(String geoid) {
        if (geoid != null) ddlTableGeoids.add(geoid);
    }

    public Set<String> getDdlTableGeoids() {
        return Collections.unmodifiableSet(ddlTableGeoids);
    }

    // ═══════ Constraints (PK, FK, UQ, CH) ═══════

    /**
     * Registers a table constraint parsed from DDL.
     * Called by BaseSemanticListener.onConstraint().
     *
     * @param geoid           constraint geoid (use {@link ConstraintInfo#buildGeoid})
     * @param constraintType  "PK" | "FK" | "UQ" | "CH"
     * @param constraintName  declared name, or null for unnamed
     * @param hostTableGeoid  geoid of the table owning this constraint
     * @param columnNames     ordered FK/PK/UQ column names in the host table
     * @param refTableGeoid   referenced table geoid (FK only; null for PK/UQ/CH)
     * @param refColumnNames  referenced column names (FK only; empty for others)
     * @param onDelete        "CASCADE" | "SET NULL" | null (FK only)
     */
    public void addConstraint(String geoid, String constraintType, String constraintName,
                              String hostTableGeoid, List<String> columnNames,
                              String refTableGeoid, List<String> refColumnNames, String onDelete) {
        if (geoid == null) return;
        constraints.putIfAbsent(geoid, new ConstraintInfo(geoid, constraintType, constraintName,
                hostTableGeoid, columnNames, refTableGeoid, refColumnNames, onDelete));

        // Propagate PK/FK flags directly onto ColumnInfo for fast inspector access
        // (avoids graph traversal through DaliConstraint vertices at query time).
        if (ConstraintInfo.TYPE_PK.equals(constraintType) && columnNames != null) {
            for (String colName : columnNames) {
                ColumnInfo col = columns.get(hostTableGeoid + "." + colName.toUpperCase());
                if (col != null) col.markAsPk();
            }
        } else if (ConstraintInfo.TYPE_FK.equals(constraintType) && columnNames != null) {
            for (int i = 0; i < columnNames.size(); i++) {
                String colName = columnNames.get(i);
                String refCol  = (refColumnNames != null && i < refColumnNames.size())
                        ? refColumnNames.get(i) : null;
                ColumnInfo col = columns.get(hostTableGeoid + "." + colName.toUpperCase());
                if (col != null) col.markAsFk(refTableGeoid, refCol);
            }
        }
    }

    public Map<String, ConstraintInfo> getConstraints() {
        return Collections.unmodifiableMap(constraints);
    }

    // ═══════ Statements ═══════

    public void addStatement(String geoid, String type, String snippet, int lineStart, int lineEnd,
                             String parentStatementGeoid, String routineGeoid) {
        statements.computeIfAbsent(geoid, k -> {
            logger.debug("New statement registered: {} [{}]", type, geoid);
            return new StatementInfo(geoid, type, snippet, lineStart, lineEnd,
                    parentStatementGeoid, routineGeoid);
        });
    }

    public Map<String, StatementInfo> getStatements() {
        return statements;
    }

    public Map<String, RoutineInfo> getRoutines() {
        return routines;
    }

    // ═══════ Records (BULK COLLECT targets) ═══════

    /**
     * Registers a PL/SQL collection variable populated via BULK COLLECT INTO.
     * Geoid formula: routineGeoid + ":RECORD:" + varNameUpper[:LINE]
     * Idempotent — returns existing RecordInfo if already registered.
     */
    public RecordInfo ensureRecord(String varName, String routineGeoid, int line) {
        if (varName == null) return null;
        String upperVar = varName.toUpperCase();
        String rg = routineGeoid != null ? routineGeoid : "";
        String base = (rg.isBlank() ? "RECORD" : rg) + ":RECORD:" + upperVar;
        String geoid = line > 0 ? base + ":" + line : base;
        boolean[] isNew = {false};
        RecordInfo rec = records.computeIfAbsent(geoid, k -> {
            logger.debug("New record registered: {} [{}]", upperVar, geoid);
            isNew[0] = true;
            return new RecordInfo(geoid, upperVar, routineGeoid);
        });
        // C.1.3: notify listener on first registration only
        if (isNew[0]) {
            listener.onRecordRegistered(file, varName);
        }
        return rec;
    }

    /** Backward-compatible overload — delegates to ensureRecord(varName, routineGeoid, 0). */
    public RecordInfo ensureRecord(String varName, String routineGeoid) {
        return ensureRecord(varName, routineGeoid, 0);
    }

    public Map<String, RecordInfo> getRecords() { return records; }

    /**
     * HND-04: Creates (or returns existing) a virtual-table entry in the tables map for a
     * COLLECTION/VARRAY variable.  Geoid format: {@code routineGeoid:VTABLE:VAR_NAME:LINE}.
     */
    public TableInfo ensureVirtualTable(String varName, String routineGeoid, int line) {
        if (varName == null) return null;
        String upper = varName.toUpperCase();
        String rg    = routineGeoid != null ? routineGeoid : "";
        String base  = (rg.isBlank() ? "VTABLE" : rg) + ":VTABLE:" + upper;
        String geoid = line > 0 ? base + ":" + line : base;
        return tables.computeIfAbsent(geoid, k -> {
            logger.debug("New virtual table registered: {} [{}]", upper, geoid);
            return new TableInfo(geoid, upper, routineGeoid, "VTABLE");
        });
    }

    // ═══════ Routines ═══════

    /** Backward-compatible (без parentRoutine) */
    public String addRoutine(String name, String routineType, String schemaGeoid,
                             String packageGeoid, int lineStart) {
        return addRoutine(name, routineType, schemaGeoid, packageGeoid, lineStart, null);
    }

    /**
     * Регистрирует routine с правильным geoid.
     *
     * Формула geoid (портирование Python _init_routine):
     *   Package-контейнер:     packageName (просто имя, без типа в geoid)
     *   Routine в пакете:      PKG_NAME:ROUTINE_TYPE:ROUTINE_NAME
     *   Вложенная routine:     PARENT_ROUTINE_GEOID:ROUTINE_TYPE:ROUTINE_NAME
     *   Routine со схемой:     SCHEMA:ROUTINE_TYPE:ROUTINE_NAME
     *   Routine без контекста: ROUTINE_TYPE:ROUTINE_NAME
     *
     * Разделитель: ":" (двоеточие)
     *
     * @param parentRoutineGeoid geoid parent routine (для вложенности), nullable
     */
    public String addRoutine(String name, String routineType, String schemaGeoid,
                             String packageGeoid, int lineStart, String parentRoutineGeoid) {
        String upperName = name != null ? name.toUpperCase() : "UNKNOWN";

        // Normalise spec types → base type so spec and body share the same geoid.
        // PROCEDURE_SPEC / FUNCTION_SPEC → PROCEDURE / FUNCTION.
        boolean isSpec = routineType != null && routineType.endsWith("_SPEC");
        String effectiveType = isSpec
                ? routineType.substring(0, routineType.length() - "_SPEC".length())
                : (routineType != null ? routineType : "PROCEDURE");

        String geoid;
        if (parentRoutineGeoid != null && !parentRoutineGeoid.isBlank()) {
            geoid = parentRoutineGeoid + ":" + effectiveType + ":" + upperName;
        } else if (packageGeoid != null && !packageGeoid.isBlank()) {
            geoid = packageGeoid + ":" + effectiveType + ":" + upperName;
        } else if (schemaGeoid != null && !schemaGeoid.isBlank()) {
            geoid = schemaGeoid + ":" + effectiveType + ":" + upperName;
        } else {
            geoid = effectiveType + ":" + upperName;
        }

        // Ensure the owning schema exists for schema-routed standalone routines.
        if (packageGeoid == null || packageGeoid.isBlank()) {
            if (schemaGeoid != null && !schemaGeoid.isBlank()) {
                ensureSchema(schemaGeoid, null);
            }
        }

        RoutineInfo ri = routines.computeIfAbsent(geoid, k -> {
            logger.debug("New routine registered: {} {} [{}]", effectiveType, upperName, geoid);
            return new RoutineInfo(geoid, upperName, effectiveType, packageGeoid, schemaGeoid,
                                   parentRoutineGeoid, lineStart);
        });
        // Track whether spec and/or body have been seen for this routine.
        if (isSpec) ri.setHasSpec(true);
        else        ri.setHasBody(true);

        return geoid;
    }

    // ═══════ Packages ═══════

    public void ensurePackage(String name, String schemaGeoid) {
        if (name == null || name.isBlank()) return;
        // 'name' is already the full package geoid (e.g. "DWH.PK_TEST"), NOT a bare name.
        // Do NOT prepend schemaGeoid again — that would produce "DWH.DWH.PK_TEST".
        String geoid = name.toUpperCase();
        String bareName = geoid.contains(".")
                ? geoid.substring(geoid.lastIndexOf('.') + 1) : geoid;
        // Ensure the owning schema is registered so the writer can create Schema→Package edges
        // even for packages whose body contains no DML table references.
        if (schemaGeoid != null && !schemaGeoid.isBlank()) {
            ensureSchema(schemaGeoid, null);
        }
        packages.putIfAbsent(geoid, Map.of(
                "package_name", bareName,
                "schema_geoid", schemaGeoid != null ? schemaGeoid : ""));
    }

    public Map<String, Object> getPackages() { return packages; }


    // ═══════ PlType registry (HND-02) ═══════

    /**
     * Registers a PL/SQL TYPE template (RECORD or COLLECTION).
     * Idempotent — a second registration with the same geoid is ignored.
     */
    public void registerPlType(PlTypeInfo pt) {
        if (pt == null) return;
        plTypes.putIfAbsent(pt.getGeoid(), pt);
        // After any new type is added, sweep all unresolved COLLECTION/VARRAY types
        // so OF_TYPE edges are emitted even when no variable of the collection type is declared.
        plTypes.values().forEach(this::tryResolveElementTypeGeoid);
    }

    /**
     * Resolves elementTypeGeoid for a COLLECTION/VARRAY type eagerly.
     * Called at registration time and again after every new type is added.
     */
    private void tryResolveElementTypeGeoid(PlTypeInfo pt) {
        if (pt.getElementTypeName() == null || pt.getElementTypeGeoid() != null) return;
        if (!pt.isCollection() && !pt.isVarray()) return;
        PlTypeInfo elem = resolvePlTypeByName(pt.getElementTypeName(), pt.getScopeGeoid());
        if (elem != null) {
            pt.setElementTypeGeoid(elem.getGeoid());
            logger.debug("Resolved elementTypeGeoid {} → {}", pt.getGeoid(), elem.getGeoid());
        }
    }

    public PlTypeInfo getPlType(String geoid) {
        return geoid != null ? plTypes.get(geoid) : null;
    }

    /**
     * Resolves a PL/SQL type by name using a smallest-to-largest scope chain:
     *   routine-local → package → schema
     *
     * Resolution order:
     *   1. Schema-qualified name ("CRM.T_PRICE_BREAK") — direct geoid lookup.
     *   2. Walk the scope chain from the given scopeGeoid up to the root:
     *      each step strips one ":KEYWORD:NAME" segment, moving from routine → package → schema.
     *   3. Schema-level fallback: scan types whose scopeGeoid has no ':' (bare schema name).
     *
     * This means a type declared locally in a procedure shadows a same-named type in the
     * enclosing package, which in turn shadows a schema-level type — standard PL/SQL visibility.
     */
    public PlTypeInfo resolvePlTypeByName(String typeName, String scopeGeoid) {
        if (typeName == null) return null;
        String upper = typeName.toUpperCase();

        // 1. Schema-qualified: "CRM.T_PRICE_BREAK" → lookup at schema scope directly
        if (upper.contains(".")) {
            int dot = upper.lastIndexOf('.');
            String schemaPrefix = upper.substring(0, dot);
            String bareType    = upper.substring(dot + 1);
            PlTypeInfo pt = plTypes.get(schemaPrefix + ":TYPE:" + bareType);
            if (pt != null) return pt;
        }

        // 2. Scope chain: routine → package → (schema handled in step 3)
        String scope = scopeGeoid;
        while (scope != null && !scope.isBlank()) {
            PlTypeInfo pt = plTypes.get(scope + ":TYPE:" + upper);
            if (pt != null) return pt;
            scope = parentScopeGeoid(scope);
        }

        // 3. Schema-level fallback: types whose scopeGeoid is a bare name (no ':')
        //    — "TESTSCHEMA", "_GLOBAL_", etc.  Returns null if ambiguous rather than guessing.
        return plTypes.values().stream()
                .filter(pt -> !pt.getScopeGeoid().contains(":")
                        && (upper.equals(pt.getName())
                            || (upper.contains(".") && upper.endsWith("." + pt.getName()))))
                .findFirst().orElse(null);
    }

    /**
     * Strips the last ":KEYWORD:NAME" segment from a scope geoid to move one level up.
     * Examples:
     *   "PKG:PROCEDURE:PROC_A"            → "PKG"
     *   "PKG:PROCEDURE:PROC_A:FUNCTION:F" → "PKG:PROCEDURE:PROC_A"
     *   "PKG"  (single-word)              → null  (already at top)
     */
    private static String parentScopeGeoid(String scopeGeoid) {
        if (scopeGeoid == null) return null;
        int last = scopeGeoid.lastIndexOf(':');
        if (last <= 0) return null;          // single-word scope — no parent
        int prev = scopeGeoid.lastIndexOf(':', last - 1);
        if (prev <= 0) return null;          // two-segment "A:B" — no useful parent to climb to
        return scopeGeoid.substring(0, prev);
    }

    /**
     * HND-14: Injects virtual columns from a PlTypeInfo(RECORD/OBJECT) into a synthetic table geoid.
     * Used when TABLE(function()) creates a virtual relation backed by a PIPELINED return type.
     */
    public void injectColumnsFromPlType(String tableGeoid, PlTypeInfo recordType) {
        if (tableGeoid == null || recordType == null) return;
        int pos = 1;
        for (com.hound.semantic.model.PlTypeFieldInfo f : recordType.getFields()) {
            addColumnWithOrdinal(tableGeoid, f.name(), null, null, pos++, f.dataType());
        }
    }

    public Map<String, PlTypeInfo> getPlTypes() { return plTypes; }

    // ═══════ HAL3-01: CompensationStats (write-side edges) ═══════
    public void addCompensationStat(CompensationStats stat) { compensationStats.add(stat); }
    public List<CompensationStats> getCompensationStats() { return compensationStats; }

    // ═══════ HAL2-01: Pending INJECT tracking ═══════
    public void markPendingPipelined(String syntheticTableGeoid) { pendingPipelinedTables.add(syntheticTableGeoid); }
    public boolean isPendingPipelined(String tableGeoid) { return pendingPipelinedTables.contains(tableGeoid); }
    public void markPendingRowtype(String tableGeoid) { pendingRowtypeTables.add(tableGeoid); }
    public boolean isPendingRowtype(String tableGeoid) { return pendingRowtypeTables.contains(tableGeoid); }
    public void markPendingMultiset(String stmtGeoid) { pendingMultisetStmts.add(stmtGeoid); }
    public boolean isPendingMultiset(String stmtGeoid) { return pendingMultisetStmts.contains(stmtGeoid); }
    public Set<String> getPendingPipelinedTables() { return pendingPipelinedTables; }
    public Set<String> getPendingRowtypeTables() { return pendingRowtypeTables; }

    // ═══════ Lineage ═══════

    public void addLineageEdge(String source, String target, String type, String statementGeoid) {
        lineageEdges.add(new LineageEdge(source, target, type, statementGeoid, null));
    }

    public List<LineageEdge> getLineageEdges() {
        return new ArrayList<>(lineageEdges);
    }

    // ═══════ Structure ═══════

    public Structure getStructure() {
        return new Structure(databases, schemas, packages, tables, columns, routines, statements, records,
                Collections.unmodifiableSet(ddlTableGeoids),
                Collections.unmodifiableMap(constraints),
                Collections.unmodifiableMap(plTypes),
                Collections.unmodifiableList(compensationStats));
    }

    // ═══════ Schemas / Databases ═══════

    public void ensureDatabase(String name) {
        if (name != null && !name.isBlank()) {
            databases.putIfAbsent(name.toUpperCase(), Map.of("name", name.toUpperCase()));
        }
    }

    public void ensureSchema(String name, String dbGeoid) {
        if (name != null && !name.isBlank()) {
            // STAB-2: лог невалидных имён схем (diag mode only)
            if (resolutionLogger != null && resolutionLogger.isEnabled()) {
                if (!com.hound.util.ValidationUtils.isValidIdentifier(name)) {
                    resolutionLogger.log(
                        ResolutionLogger.InputKind.TABLE_REF, name, null, "ensureSchema",
                        ResolutionLogger.ResultKind.UNRESOLVED, null, null,
                        "invalid_schema_name: " + name
                    );
                    logger.warn("STAB: invalid schema name: '{}'", name);
                    listener.onSemanticWarning(file, "STAB_INVALID_SCHEMA",
                            "Invalid schema name: '" + name + "'");
                }
            }
            // S1.SCH: always log suspicious schema names to DB (quotes, $, :, parens, etc.)
            if (isSuspiciousSchemaName(name) && !schemas.containsKey(name.toUpperCase())) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("schema_name", name);
                entry.put("reason",     classifySuspiciousReason(name));
                entry.put("backtrace",  captureHoundBacktrace());
                schemaRegistrationLog.add(entry);
                logger.warn("S1.SCH: suspicious schema name registered: '{}' reason={}", name, entry.get("reason"));
                listener.onSemanticWarning(file, "STAB_SUSPICIOUS_SCHEMA",
                        "Suspicious schema name: '" + name + "' reason=" + entry.get("reason"));
            }
            Map<String, Object> schemaData = new LinkedHashMap<>();
            schemaData.put("name", name.toUpperCase());
            schemaData.put("db", dbGeoid);
            schemas.putIfAbsent(name.toUpperCase(), schemaData);
        }
    }

    private static boolean isSuspiciousSchemaName(String name) {
        return name.contains("\"") || name.contains("'")
                || name.contains("$") || name.contains(":")
                || name.contains("(") || name.contains(")")
                || name.contains(".") || name.contains(" ");
    }

    private static String classifySuspiciousReason(String name) {
        if (name.contains("\"") || name.contains("'")) return "quoted_identifier_not_stripped";
        if (name.contains("(") || name.contains(")"))  return "parenthesis_in_schema_name";
        if (name.contains("."))                         return "dot_in_schema_name";
        if (name.contains("$"))                         return "dollar_sign_in_schema_name";
        if (name.contains(":"))                         return "colon_in_schema_name";
        if (name.contains(" "))                         return "space_in_schema_name";
        return "special_chars";
    }

    /** Captures only com.hound.* frames from the current call stack. */
    private static String captureHoundBacktrace() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement e : stack) {
            if (e.getClassName().startsWith("com.hound")) {
                sb.append(e.getClassName().replaceFirst("com\\.hound\\.", ""))
                  .append('.').append(e.getMethodName())
                  .append(':').append(e.getLineNumber()).append('\n');
            }
        }
        return sb.toString().trim();
    }

    /** S1.SCH: returns suspicious schema registration log for DB persistence. */
    public List<Map<String, Object>> getSchemaRegistrationLog() {
        return Collections.unmodifiableList(schemaRegistrationLog);
    }

    // ═══════ Aggregators (порт Python get_structure / get_lineage) ═══════

    /**
     * Сериализация таблиц → List<Map>.
     * Порт Python: serialize_tables()
     */
    public List<Map<String, Object>> serializeTables() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (var entry : tables.entrySet()) {
            TableInfo t = entry.getValue();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("geoid", t.geoid());
            m.put("table_name", t.tableName());
            m.put("schema_geoid", t.schemaGeoid());
            m.put("table_type", t.tableType());
            m.put("aliases", new ArrayList<>(t.aliases()));
            m.put("column_count", t.columnCount());
            result.add(m);
        }
        return result;
    }

    /**
     * Сериализация колонок → List<Map>.
     */
    public List<Map<String, Object>> serializeColumns() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (var entry : columns.entrySet()) {
            ColumnInfo c = entry.getValue();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("geoid", c.getGeoid());
            m.put("table_geoid", c.getTableGeoid());
            m.put("column_name", c.getColumnName());
            m.put("expression", c.getExpression());
            m.put("alias", c.getAlias());
            m.put("is_output", c.isOutput());
            m.put("order", c.getOrder());
            m.put("ordinal_position", c.getOrdinalPosition());
            m.put("used_in_statements", new ArrayList<>(c.getUsedInStatements()));
            m.put("used_in_routines", new ArrayList<>(c.getUsedInRoutines()));
            result.add(m);
        }
        return result;
    }

    /**
     * Сериализация routines → List<Map>.
     */
    public List<Map<String, Object>> serializeRoutines() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (var entry : routines.entrySet()) {
            RoutineInfo r = entry.getValue();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("geoid", r.getGeoid());
            m.put("name", r.getName());
            m.put("routine_type", r.getRoutineType());
            m.put("package_geoid", r.getPackageGeoid());
            m.put("schema_geoid", r.getSchemaGeoid());
            m.put("return_type", r.getReturnType());

            List<Map<String, String>> params = new ArrayList<>();
            for (var p : r.getTypedParameters()) {
                params.add(Map.of("name", p.name(), "type", p.type(), "mode", p.mode()));
            }
            m.put("parameters", params);

            List<Map<String, String>> vars = new ArrayList<>();
            for (var v : r.getTypedVariables()) {
                vars.add(Map.of("name", v.name(), "type", v.type()));
            }
            m.put("variables", vars);
            result.add(m);
        }
        return result;
    }

    /**
     * Сериализация statements → List<Map>.
     */
    public List<Map<String, Object>> serializeStatements() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (var entry : statements.entrySet()) {
            StatementInfo s = entry.getValue();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("geoid",              s.getGeoid());
            m.put("type",               s.getType());
            m.put("snippet",            s.getSnippet());
            m.put("line_start",         s.getLineStart());
            m.put("line_end",           s.getLineEnd());
            m.put("parent_statement",   s.getParentStatementGeoid());
            m.put("routine_geoid",      s.getRoutineGeoid());
            m.put("alias",              s.getAlias());
            m.put("source_tables",      new ArrayList<>(s.getSourceTables().values()));
            m.put("target_tables",      new ArrayList<>(s.getTargetTables().values()));
            m.put("child_statements",   new ArrayList<>(s.getChildStatements()));
            m.put("source_subquery_geoids", new ArrayList<>(s.getSourceSubqueries().keySet()));
            m.put("columns_output",     new ArrayList<>(s.getColumnsOutput().values()));
            m.put("join_count",         s.getJoins().size());
            m.put("col_count_output",   s.getColumnsOutput().size());
            m.put("has_aggregation",    s.isHasAggregation());
            m.put("has_window",         s.isHasWindow());
            m.put("is_union",           s.isUnion());
            m.put("subtype",            s.getSubtype());
            m.put("is_dml",             isDml(s.getType()));
            m.put("is_ddl",             isDdl(s.getType()));
            m.put("has_cte",            hasCte(s, statements));
            m.put("depth",              computeDepth(s.getParentStatementGeoid(), statements));
            m.put("quality",            computeStatementQuality(s));
            result.add(m);
        }
        return result;
    }

    private static boolean isDml(String type) {
        return type != null && switch (type) {
            case "INSERT", "UPDATE", "DELETE", "MERGE" -> true;
            default -> false;
        };
    }

    private static boolean isDdl(String type) {
        return type != null && switch (type) {
            case "CREATE_VIEW", "CREATE_TABLE", "ALTER_TABLE", "DROP_TABLE",
                 "CREATE_INDEX", "CREATE_SEQUENCE", "CREATE_PROCEDURE",
                 "CREATE_FUNCTION", "CREATE_PACKAGE", "CREATE_TRIGGER" -> true;
            default -> type.startsWith("CREATE") || type.startsWith("ALTER") || type.startsWith("DROP");
        };
    }

    private static boolean hasCte(StatementInfo s, Map<String, StatementInfo> allStatements) {
        for (String childGeoid : s.getChildStatements()) {
            StatementInfo child = allStatements.get(childGeoid);
            if (child != null && "CTE".equals(child.getType())) return true;
        }
        return false;
    }

    private static int computeDepth(String parentGeoid, Map<String, StatementInfo> allStatements) {
        int depth = 0;
        String current = parentGeoid;
        while (current != null && depth < 50) {
            StatementInfo parent = allStatements.get(current);
            if (parent == null) break;
            depth++;
            current = parent.getParentStatementGeoid();
        }
        if (depth >= 50 && current != null) {
            logger.warn("STAB: computeDepth hit limit of 50 starting from '{}' — possible cycle or deep nesting", parentGeoid);
        }
        return depth;
    }

    private static double computeStatementQuality(StatementInfo s) {
        Map<String, Map<String, Object>> atoms = s.getAtoms();
        if (atoms.isEmpty()) return 0.0;
        int total = atoms.size();
        long resolved  = atoms.values().stream().filter(a -> AtomInfo.STATUS_RESOLVED.equals(a.get("status"))).count();
        long constants = atoms.values().stream().filter(a -> Boolean.TRUE.equals(a.get("is_constant"))).count();
        long functions = atoms.values().stream().filter(a -> Boolean.TRUE.equals(a.get("is_function_call"))).count();
        return (resolved + constants + functions) / (double) total;
    }

    /**
     * Построение tables_usage — сводная таблица использования (source/target) по statement'ам.
     * Порт Python: build_tables_usage()
     */
    public List<Map<String, Object>> buildTablesUsage() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (var stEntry : statements.entrySet()) {
            StatementInfo s = stEntry.getValue();
            for (var srcEntry : s.getSourceTables().entrySet()) {
                Map<String, Object> usage = new LinkedHashMap<>();
                usage.put("statement_geoid", s.getGeoid());
                usage.put("statement_type", s.getType());
                usage.put("table_geoid", srcEntry.getKey());
                usage.put("role", "SOURCE");
                usage.put("table_info", srcEntry.getValue());
                result.add(usage);
            }
            for (var tgtEntry : s.getTargetTables().entrySet()) {
                Map<String, Object> usage = new LinkedHashMap<>();
                usage.put("statement_geoid", s.getGeoid());
                usage.put("statement_type", s.getType());
                usage.put("table_geoid", tgtEntry.getKey());
                usage.put("role", "TARGET");
                usage.put("table_info", tgtEntry.getValue());
                result.add(usage);
            }
        }
        return result;
    }

    /**
     * Полная структура — единый dict для Arrow/JSON сериализации.
     * Порт Python: get_structure()
     */
    public Map<String, Object> getFullStructure() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("databases",  new ArrayList<>(databases.values()));
        result.put("schemas",    new ArrayList<>(schemas.values()));
        result.put("packages",   new ArrayList<>(packages.values()));
        result.put("tables",     serializeTables());
        result.put("columns",    serializeColumns());
        result.put("routines",   serializeRoutines());
        result.put("statements", serializeStatements());
        return result;
    }

    /**
     * Полный lineage — {atoms, joins, tables_usage} для Arrow/JSON сериал��зации.
     * Порт Python: get_lineage()
     */
    public Map<String, Object> getFullLineage() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("atoms",        buildAtomsLineage());
        result.put("joins",        buildJoinsLineage());
        result.put("tables_usage", buildTablesUsage());
        return result;
    }

    /** Aggregates all atoms from all statements for lineage output. */
    private List<Map<String, Object>> buildAtomsLineage() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (var entry : statements.entrySet()) {
            String stmtGeoid = entry.getKey();
            for (var atomEntry : entry.getValue().getAtoms().entrySet()) {
                Map<String, Object> source = atomEntry.getValue();
                Map<String, Object> a = new LinkedHashMap<>(source.size() + 2);
                a.putAll(source);
                a.put("atom_text",       atomEntry.getKey());
                a.put("statement_geoid", stmtGeoid);
                result.add(a);
            }
        }
        return result;
    }

    /** Aggregates all joins from all statements for lineage output. */
    private List<Map<String, Object>> buildJoinsLineage() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (var entry : statements.entrySet()) {
            String stmtGeoid = entry.getKey();
            for (JoinInfo j : entry.getValue().getJoins()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("statement_geoid",     stmtGeoid);
                m.put("join_type",           j.joinType());
                m.put("source_table_geoid",  j.sourceTableGeoid());
                m.put("source_alias",        j.sourceTableAlias());
                m.put("target_table_geoid",  j.targetTableGeoid());
                m.put("target_alias",        j.targetTableAlias());
                m.put("conditions",          j.conditions());
                result.add(m);
            }
        }
        return result;
    }

    // ═══════ Clear ═══════

    public void clear() {
        databases.clear();
        schemas.clear();
        packages.clear();
        tables.clear();
        columns.clear();
        statements.clear();
        routines.clear();
        lineageEdges.clear();
    }
}