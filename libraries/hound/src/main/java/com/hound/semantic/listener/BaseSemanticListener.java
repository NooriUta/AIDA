// src/main/java/com/hound/semantic/listener/BaseSemanticListener.java
package com.hound.semantic.listener;

import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.semantic.model.ConstraintInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * BaseSemanticListener — Java-аналог Python BaseSQLListener.
 *
 * ВСЕ поля и методы объявлены public — это необходимо потому что
 * PlSqlSemanticListener находится в другом пакете (dialect.plsql) и
 * использует composition (держит экземпляр BaseSemanticListener как поле base).
 *
 * В Java protected-члены недоступны из другого пакета через ссылку на объект —
 * только через наследование. Поэтому единственное решение при composition — public.
 */
public abstract class BaseSemanticListener {

    public final Logger logger = LoggerFactory.getLogger(getClass());
    public final UniversalSemanticEngine engine;

    // ====================== Структуры данных ======================
    public final Map<String, Map<String, Object>> databases  = new LinkedHashMap<>();
    public final Map<String, Map<String, Object>> schemas    = new LinkedHashMap<>();
    public final Map<String, Map<String, Object>> packages   = new LinkedHashMap<>();
    public final Map<String, Map<String, Object>> tables     = new LinkedHashMap<>();
    public final Map<String, Map<String, Object>> columns    = new LinkedHashMap<>();
    public final Map<String, Map<String, Object>> routines   = new LinkedHashMap<>();
    public final Map<String, Map<String, Object>> statements = new LinkedHashMap<>();

    // ====================== current-контекст ======================
    public final Map<String, Object> current = new HashMap<>();
    // Routine stack: каждый элемент = {parentRoutine, parentRoutineType}
    public final Deque<String[]> routineStack = new ArrayDeque<>();

    /**
     * Default schema: used as fallback when no explicit schema context is active.
     * Set to the DB folder name so that packages without "SCHEMA.PKG" prefix still
     * get schema_geoid = dbName → CONTAINS_ROUTINE edge is created.
     */
    public String defaultSchema = null;

    // ====================== Конструктор ======================
    public BaseSemanticListener(UniversalSemanticEngine engine) {
        this.engine = engine;
        initCurrentContext();
    }

    // =========================================================================
    // РАЗДЕЛ 1: ИНИЦИАЛИЗАЦИЯ КОНТЕКСТА
    // =========================================================================

    public void initCurrentContext() {
        current.put("database", null);
        current.put("schema", null);
        current.put("package", null);
        current.put("routine", null);
        current.put("routine_type", null);
        current.put("statement", null);
        current.put("statement_type", null);
        current.put("selected_list", null);
        current.put("from", null);
        current.put("join", null);
        current.put("where", null);
        current.put("order", null);
        current.put("group_by", null);
        current.put("having", null);
        current.put("subquery_alias", null);
        current.put("atom_context", null);
        current.put("q_block", null);
        current.put("is_first_subq", null);
        current.put("is_first_subqp", null);
        current.put("general_table", null);
        current.put("column_alias", null);
        current.put("table", null);
        current.put("target_table", null);
        current.put("alias", null);
        current.put("in_dml_target", false);
        current.put("is_union", false);
        current.put("Merge_insert_part", false);
        current.put("Merge_update_part", false);
        current.put("merge_insert_col_list_active", false);
        current.put("Values_clause", false);
        current.put("Values_clause_cnt", 0);
        current.put("in_join_context", false);
        current.put("parent_is_union", false);
        current.put("parent_statement", null);
        current.put("parent_statement_type", null);
        current.put("unattached_atoms", new HashMap<String, Object>());
        current.put("subquery_alias_stack", new ArrayList<String>());
        current.put("s_operation_part", false);
        current.put("column_output", null);
        current.put("column_affected", null);
        current.put("in_update_set_expr", false);
    }

    // =========================================================================
    // РАЗДЕЛ 2: ГЕТТЕРЫ И СЕТТЕРЫ current
    // =========================================================================

    public String currentStatement()     { return (String) current.get("statement"); }
    public String currentStatementType() { return (String) current.get("statement_type"); }
    public String currentRoutine()       { return (String) current.get("routine"); }
    public String currentRoutineType()   { return (String) current.get("routine_type"); }
    public String currentSchema() {
        String explicit = (String) current.get("schema");
        return explicit != null ? explicit : defaultSchema;
    }
    public String currentPackage()       { return (String) current.get("package"); }
    public String parentStatement()      { return (String) current.get("parent_statement"); }
    public String currentTable()         { return (String) current.get("table"); }
    public String currentTableAlias()    { return (String) current.get("table_alias"); }

    public void setCurrentTableAlias(String alias) { current.put("table_alias", alias); }

    public boolean isInDmlTarget()   { return Boolean.TRUE.equals(current.get("in_dml_target")); }
    public boolean isInJoinContext() { return Boolean.TRUE.equals(current.get("in_join_context")); }
    public boolean isMergeInsert()   { return Boolean.TRUE.equals(current.get("Merge_insert_part")); }
    public boolean isMergeUpdate()   { return Boolean.TRUE.equals(current.get("Merge_update_part")); }
    public boolean isMergeInsertColListActive() { return Boolean.TRUE.equals(current.get("merge_insert_col_list_active")); }
    public boolean isValuesClause()  { return Boolean.TRUE.equals(current.get("Values_clause")); }

    // Методы-сеттеры для PlSqlSemanticListener (вместо прямого current.put)
    public void setCurrentTable(String tableName)          { current.put("table", tableName); }
    public void setSubqueryAlias(String alias)             { current.put("subquery_alias", alias); }
    public void setMergeIntoSubqueryAlias(String alias)    { current.put("merge_into_subquery_alias", alias); }
    public String getMergeIntoSubqueryAlias()              { return (String) current.get("merge_into_subquery_alias"); }
    public void clearMergeIntoSubqueryAlias()              { current.remove("merge_into_subquery_alias"); }
    public void setGeneralTable(Object val)                { current.put("general_table", val); }
    public void setIsFirstSubq(Integer line)               { current.put("is_first_subq", line); }
    public void setIsFirstSubqp(Integer col)               { current.put("is_first_subqp", col); }
    public void setIsUnion(boolean val)                    { current.put("is_union", val); }
    public void setPackage(String pkgGeoid)                { current.put("package", pkgGeoid); }

    public Integer getIsFirstSubq()  { return (Integer) current.get("is_first_subq"); }
    public Integer getIsFirstSubqp() { return (Integer) current.get("is_first_subqp"); }

    @SuppressWarnings("unchecked")
    public List<String> subqueryAliasStack() {
        return (List<String>) current.get("subquery_alias_stack");
    }

    public String getCurrentParentContext() {
        if (current.get("selected_list") != null) return "SELECT";
        if (current.get("from") != null)          return "FROM";
        if (current.get("where") != null)         return "WHERE";
        if (current.get("order") != null)         return "ORDER";
        if (current.get("having") != null)        return "HAVING";
        if (current.get("group_by") != null)      return "GROUP BY";
        // DML source-expression guards — atoms inside VALUES or UPDATE SET expression
        // must NOT inherit the statement's DML type (that would give them poliage_update).
        // These flags are set by onValuesClauseEnter / onUpdateSetColumn / onMergeElementEnter.
        if (isValuesClause())                      return "INSERT_VALUES";
        if (Boolean.TRUE.equals(current.get("in_update_set_expr"))) return "SET_EXPR";
        if (isMergeInsert())                       return "INSERT";
        if (isMergeUpdate())                       return "UPDATE";
        if (currentStatement() != null)            return currentStatementType();
        if (currentRoutine() != null)              return currentRoutineType();
        return null;
    }

    // =========================================================================
    // РАЗДЕЛ 3: ИНИЦИАЛИЗАЦИЯ SQL-ОБЪЕКТОВ
    // =========================================================================

    public void initDatabase(String dbName) {
        if (dbName == null) return;
        databases.computeIfAbsent(dbName, k -> {
            Map<String, Object> db = new LinkedHashMap<>();
            db.put("db_name", dbName);
            db.put("schemas", new HashSet<String>());
            return db;
        });
    }

    public String initSchema(String schemaName, String databaseGeoid) {
        if (schemaName == null || schemaName.isBlank()) return null;
        String geoid = (databaseGeoid != null && !databaseGeoid.isBlank())
                ? databaseGeoid + "." + schemaName : schemaName;
        schemas.computeIfAbsent(geoid, k -> {
            Map<String, Object> s = new LinkedHashMap<>();
            s.put("schema_name", schemaName);
            s.put("tables", new HashSet<String>());
            s.put("routines", new HashSet<String>());
            s.put("packages", new HashSet<String>());
            if (databaseGeoid != null && databases.containsKey(databaseGeoid)) {
                @SuppressWarnings("unchecked")
                Set<String> dbSchemas = (Set<String>) databases.get(databaseGeoid).get("schemas");
                if (dbSchemas != null) dbSchemas.add(geoid);
            }
            return s;
        });
        return geoid;
    }

    public String initPackage(String packageName, String schemaGeoid) {
        if (packageName == null) return null;
        String geoid = (schemaGeoid != null && !schemaGeoid.isBlank())
                ? schemaGeoid + "." + packageName : packageName;
        packages.computeIfAbsent(geoid, k -> {
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("schema_geoid", schemaGeoid);
            p.put("package_name", packageName);
            p.put("routines", new HashSet<String>());
            return p;
        });
        if (schemaGeoid != null && schemas.containsKey(schemaGeoid)) {
            @SuppressWarnings("unchecked")
            Set<String> pkgs = (Set<String>) schemas.get(schemaGeoid).get("packages");
            if (pkgs != null) pkgs.add(geoid);
        }
        return geoid;
    }

    public String initTable(String tableName, String tableAlias,
                            String schemaGeoid, String tableType) {
        if (tableName == null || tableName.isBlank()) return null;
        String cleanName = cleanIdentifier(tableName);
        String resolvedSchema = schemaGeoid != null ? schemaGeoid : currentSchema();

        String geoid;
        if (cleanName.contains(".")) {
            geoid = cleanName;
        } else if (resolvedSchema != null && !resolvedSchema.isBlank()) {
            geoid = resolvedSchema + "." + cleanName;
        } else {
            geoid = cleanName;
        }

        tables.computeIfAbsent(geoid, k -> {
            Map<String, Object> t = new LinkedHashMap<>();
            t.put("table_name", cleanName);
            t.put("schema_geoid", resolvedSchema);
            t.put("table_type", tableType != null ? tableType : "TABLE");
            t.put("aliases", new HashSet<String>());
            t.put("columns", new HashSet<String>());
            Map<String, Object> usage = new LinkedHashMap<>();
            usage.put("routines", new HashSet<String>());
            usage.put("statements", new HashSet<String>());
            t.put("usage", usage);
            return t;
        });

        if (tableAlias != null && !tableAlias.isBlank()) {
            @SuppressWarnings("unchecked")
            Set<String> aliases = (Set<String>) tables.get(geoid).get("aliases");
            if (aliases != null) aliases.add(cleanIdentifier(tableAlias));
        }
        return geoid;
    }

    public String initRoutine(String routineName, String routineType,
                              String schemaGeoid, String packageGeoid, int sourceLine) {
        if (routineName == null || routineName.isBlank()) return null;
        String cleanName = cleanIdentifier(routineName);

        String geoid;
        if (packageGeoid != null && !packageGeoid.isBlank())
            geoid = packageGeoid + ":" + routineType + ":" + cleanName;
        else if (schemaGeoid != null && !schemaGeoid.isBlank())
            geoid = schemaGeoid + ":" + routineType + ":" + cleanName;
        else
            geoid = routineType + ":" + cleanName;

        // Save parent routine for restore on exit (аналог parent_statement)
        routineStack.push(new String[]{
                (String) current.get("routine"),
                (String) current.get("routine_type")
        });

        current.put("routine", geoid);
        current.put("routine_type", routineType);

        routines.computeIfAbsent(geoid, k -> {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("schema_geoid", schemaGeoid);
            r.put("package_geoid", packageGeoid);
            r.put("routine_type", routineType);
            r.put("routine_name", cleanName);
            r.put("source_line", sourceLine);
            r.put("columns_used", new LinkedHashMap<>());
            r.put("statements_used", new HashSet<String>());
            r.put("source_tables", new HashSet<String>());
            r.put("parameters", new ArrayList<>());
            r.put("variables", new ArrayList<>());
            r.put("return_type", null);
            r.put("called_routines", new ArrayList<>());
            return r;
        });

        if (packageGeoid != null && packages.containsKey(packageGeoid)) {
            @SuppressWarnings("unchecked")
            Set<String> rts = (Set<String>) packages.get(packageGeoid).get("routines");
            if (rts != null) rts.add(geoid);
        } else if (schemaGeoid != null && schemas.containsKey(schemaGeoid)) {
            @SuppressWarnings("unchecked")
            Set<String> rts = (Set<String>) schemas.get(schemaGeoid).get("routines");
            if (rts != null) rts.add(geoid);
        }
        return geoid;
    }

    public String initStatement(String stmtType, String snippet,
                                int lineStart, int lineEnd, String stmtAlias) {
        String stmtName = stmtType + ":" + lineStart + ":" + lineEnd + ":" + System.nanoTime();

        Map<String, Object> stmtInfo = new LinkedHashMap<>();
        stmtInfo.put("type", stmtType);
        stmtInfo.put("snippet", snippet != null && snippet.length() > 200
                ? snippet.substring(0, 200) + "..." : snippet);
        stmtInfo.put("line_start", lineStart);
        stmtInfo.put("line_end", lineEnd);
        Set<String> aliasSet = new HashSet<>();
        if (stmtAlias != null) aliasSet.add(stmtAlias);
        stmtInfo.put("alias", aliasSet);

        stmtInfo.put("parent_statement",           current.get("statement"));
        stmtInfo.put("parent_statement_type",      current.get("statement_type"));
        stmtInfo.put("parent_s_where",             current.get("where"));
        stmtInfo.put("parent_is_union",            current.get("is_union"));
        stmtInfo.put("parent_s_from",              current.get("from"));
        stmtInfo.put("parent_s_selected_list",     current.get("selected_list"));
        stmtInfo.put("parent_s_order",             current.get("order"));
        stmtInfo.put("parent_s_group_by",          current.get("group_by"));
        stmtInfo.put("parent_s_having",            current.get("having"));
        stmtInfo.put("parent_s_operation_part",    current.get("s_operation_part"));
        stmtInfo.put("parent_s_Merge_insert_part", current.get("Merge_insert_part"));
        stmtInfo.put("parent_s_Merge_update_part", current.get("Merge_update_part"));

        stmtInfo.put("child_statements",  new HashSet<String>());
        stmtInfo.put("used_in_routines",  new HashSet<String>());
        stmtInfo.put("source_tables",     new LinkedHashMap<>());
        stmtInfo.put("target_tables",     new LinkedHashMap<>());
        stmtInfo.put("columns_affected",  new HashSet<String>());
        stmtInfo.put("columns_output",    new LinkedHashMap<>());
        stmtInfo.put("joins",             new ArrayList<>());
        stmtInfo.put("atoms",             new LinkedHashMap<>());
        stmtInfo.put("source_subqueries", new LinkedHashMap<>());

        String parentStmt = currentStatement();
        if (parentStmt != null && statements.containsKey(parentStmt)) {
            @SuppressWarnings("unchecked")
            Set<String> children = (Set<String>) statements.get(parentStmt).get("child_statements");
            if (children != null) children.add(stmtName);

            current.put("parent_statement",      parentStmt);
            current.put("parent_statement_type", current.get("statement_type"));
            current.put("parent_is_union",       current.get("is_union"));
        }

        current.put("statement",      stmtName);
        current.put("statement_type", stmtType);

        statements.put(stmtName, stmtInfo);

        String routine = currentRoutine();
        if (routine != null && parentStmt == null && routines.containsKey(routine)) {
            @SuppressWarnings("unchecked")
            Set<String> stmtsUsed = (Set<String>) routines.get(routine).get("statements_used");
            if (stmtsUsed != null) stmtsUsed.add(stmtName);
        }
        return stmtName;
    }

    public void exitStatement() {
        String parentStmt = (String) current.get("parent_statement");
        String currentStmt = currentStatement();
        if (currentStmt == null) return;

        if (parentStmt != null && statements.containsKey(parentStmt)) {
            Map<String, Object> pra = statements.get(parentStmt);
            current.put("from",              pra.get("parent_s_from"));
            current.put("selected_list",     pra.get("parent_s_selected_list"));
            current.put("order",             pra.get("parent_s_order"));
            current.put("group_by",          pra.get("parent_s_group_by"));
            current.put("having",            pra.get("parent_s_having"));
            current.put("where",             pra.get("parent_s_where"));
            current.put("statement_type",    pra.get("type"));
            current.put("s_operation_part",  pra.get("parent_s_operation_part"));
            current.put("Merge_insert_part", pra.get("parent_s_Merge_insert_part"));
            current.put("Merge_update_part", pra.get("parent_s_Merge_update_part"));
            current.put("parent_statement",      pra.get("parent_statement"));
            current.put("parent_statement_type", pra.get("parent_statement_type"));
            current.put("statement", parentStmt);
        } else {
            subqueryAliasStack().clear();
            current.put("subquery_alias",          null);
            current.put("statement",               null);
            current.put("statement_type",          null);
            current.put("where",                   null);
            current.put("is_union",                false);
            current.put("from",                    null);
            current.put("selected_list",           null);
            current.put("order",                   null);
            current.put("group_by",                null);
            current.put("having",                  null);
            current.put("in_dml_target",           false);
            current.put("parent_statement",      null);
            current.put("parent_statement_type", null);
            current.put("parent_is_union",       false);
            current.put("Merge_insert_part",     false);
            current.put("Merge_update_part",       false);
        }
    }

    public void exitRoutine() {
        // Restore parent routine from stack (аналог exitStatement → parent_statement)
        if (!routineStack.isEmpty()) {
            String[] parent = routineStack.pop();
            current.put("routine",      parent[0]);
            current.put("routine_type", parent[1]);
        } else {
            current.put("routine",      null);
            current.put("routine_type", null);
        }
    }

    // =========================================================================
    // РАЗДЕЛ 4: ТАБЛИЦЫ
    // =========================================================================

    @SuppressWarnings("unchecked")
    public void markTableAsTarget(String tableGeoid, String statementGeoid, String tableAlias) {
        if (tableGeoid == null || statementGeoid == null) return;
        if (!statements.containsKey(statementGeoid)) return;
        Map<String, Object> stmt = statements.get(statementGeoid);
        Map<String, Object> targets = (Map<String, Object>) stmt.computeIfAbsent(
                "target_tables", k -> new LinkedHashMap<>());
        if (!targets.containsKey(tableGeoid)) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("table_stmt", tableGeoid);
            entry.put("table_name", tableGeoid);
            List<String> al = new ArrayList<>();
            if (tableAlias != null) al.add(tableAlias.toUpperCase());
            entry.put("table_alias", al);
            targets.put(tableGeoid, entry);
        } else if (tableAlias != null) {
            List<String> al = (List<String>) ((Map<?, ?>) targets.get(tableGeoid)).get("table_alias");
            if (al != null && !al.contains(tableAlias.toUpperCase())) al.add(tableAlias.toUpperCase());
        }
        current.put("target_table", tableGeoid);
    }

    @SuppressWarnings("unchecked")
    public void markDatasetAsSource(String tableGeoid, String statementGeoid, String tableAlias) {
        if (tableGeoid == null || statementGeoid == null) return;
        if (!statements.containsKey(statementGeoid)) return;
        Map<String, Object> stmt = statements.get(statementGeoid);
        Map<String, Object> sources = (Map<String, Object>) stmt.computeIfAbsent(
                "source_tables", k -> new LinkedHashMap<>());
        if (!sources.containsKey(tableGeoid)) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("table_stmt", tableGeoid);
            entry.put("table_name", tableGeoid);
            List<String> al = new ArrayList<>();
            if (tableAlias != null) al.add(tableAlias.toUpperCase());
            entry.put("table_alias", al);
            sources.put(tableGeoid, entry);
        } else if (tableAlias != null) {
            List<String> al = (List<String>) ((Map<?, ?>) sources.get(tableGeoid)).get("table_alias");
            if (al != null && !al.contains(tableAlias.toUpperCase())) al.add(tableAlias.toUpperCase());
        }
    }

    public void processTableReference(String tableName, String tableAlias) {
        if (tableName == null || tableName.isBlank()) return;
        String tableGeoid = initTable(tableName, tableAlias, currentSchema(), "TABLE");
        if (tableGeoid == null) return;
        String stmt = currentStatement();
        if (stmt == null) return;

        List<String> dmlTypes = List.of("UPDATE", "INSERT", "DELETE", "MERGE");
        if (isInDmlTarget() && dmlTypes.contains(currentStatementType())) {
            markTableAsTarget(tableGeoid, stmt, tableAlias);
        } else {
            markDatasetAsSource(tableGeoid, stmt, tableAlias);
        }
    }

    // =========================================================================
    // РАЗДЕЛ 5: АТОМЫ
    // =========================================================================

    @SuppressWarnings("unchecked")
    public void addAtom(String text, int line, int col, int endLine, int endCol, boolean isComplex,
                        List<String> tokens, List<Map<String, String>> tokenDetails, int nestedAtomCount) {
        // T14: suppress atom processing in specific DDL sub-contexts.
        // "SUPPRESS" is set by enterColumn_definition (DEFAULT, NOT NULL, inline constraints)
        // and by enterReferences_clause (FK target columns — already captured via ConstraintInfo).
        // Out-of-line FK host columns, CHECK constraint columns, and CREATE INDEX columns are allowed.
        if ("SUPPRESS".equals(current.get("ddl_atom_ctx"))) return;

        String parentCtx = getCurrentParentContext();
        Map<String, Object> atomContext = new LinkedHashMap<>();
        atomContext.put("text", text);
        atomContext.put("position", line + ":" + col);
        atomContext.put("sposition", endLine + ":" + endCol);
        atomContext.put("parent_context", parentCtx);
        atomContext.put("is_complex", isComplex);
        atomContext.put("is_column_reference", false);
        atomContext.put("status", null);
        atomContext.put("output_column_sequence", null);
        atomContext.put("tokens", tokens);
        atomContext.put("token_details", tokenDetails);
        atomContext.put("nested_atoms_count", nestedAtomCount);

        // DML lineage: atoms in UPDATE SET / MERGE UPDATE SET expressions carry a reference
        // to the target affected column so the writer can create DATA_FLOW edges.
        @SuppressWarnings("unchecked")
        Map<String, Object> colAffected = (Map<String, Object>) current.get("column_affected");
        if (colAffected != null) {
            String ref = (String) colAffected.get("column_ref");
            if (ref != null) atomContext.put("dml_target_ref", ref);
        }

        String atomKey = text + "~" + line + ":" + col;
        String stmt = currentStatement();
        if (stmt != null && statements.containsKey(stmt)) {
            Map<String, Object> atoms = (Map<String, Object>) statements.get(stmt).get("atoms");
            if (atoms != null) atoms.put(atomKey, atomContext);
        } else {
            Map<String, Object> unattached = (Map<String, Object>) current.get("unattached_atoms");
            if (unattached != null) unattached.put(atomKey, atomContext);
        }
        current.put("atom_context", atomContext);
        engine.onAtom(text, line, col, endLine, endCol, parentCtx != null ? parentCtx : "UNKNOWN",
                      isComplex, tokens, tokenDetails, nestedAtomCount);

        // Inject dml_target_ref into AtomProcessor's atom data.
        // AtomProcessor registers atoms independently (atomsByStatement), so without this
        // injection the writers would never find dml_target_ref and DATA_FLOW edges to
        // DaliAffectedColumn would never be created for UPDATE/MERGE SET expressions.
        @SuppressWarnings("unchecked")
        Map<String, Object> colAffected2 = (Map<String, Object>) current.get("column_affected");
        if (colAffected2 != null) {
            String ref2 = (String) colAffected2.get("column_ref");
            if (ref2 != null) {
                String atomKeyUpper = text.toUpperCase() + "~" + line + ":" + col;
                String currentStmt = currentStatement();
                if (currentStmt != null) {
                    Map<String, Object> processorAtom = engine.getAtomProcessor()
                            .getAtomsForStatement(currentStmt).get(atomKeyUpper);
                    if (processorAtom != null) {
                        processorAtom.put("dml_target_ref", ref2);
                        // G3-MERGE: MERGE UPDATE SET atoms get merge_clause="UPDATE"
                        if (Boolean.TRUE.equals(current.get("Merge_update_part"))) {
                            processorAtom.put("merge_clause", "UPDATE");
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // РАЗДЕЛ 6: УТИЛИТЫ
    // =========================================================================

    public static String cleanIdentifier(String name) {
        if (name == null) return null;
        String clean = name.replaceAll("[\"'`\\[\\]]", "").trim().toUpperCase();
        // KI-DBLINK-1: strip @DBLINK suffix so "ORDERS@PROD_DB" → "ORDERS" for geoid computation
        int atIdx = clean.indexOf('@');
        if (atIdx > 0) clean = clean.substring(0, atIdx);
        return clean;
    }

    /**
     * Cleans a column_name grammar node text: strips quotes and strips any
     * leading table-alias or schema prefix (takes only the last dot-segment).
     *
     * Grammar rule: column_name : identifier ('.' id_expression)*
     * So SET t.col1 = ... gives getText() = "t.col1"; we want "COL1".
     */
    public static String cleanColumnName(String name) {
        if (name == null) return null;
        String cleaned = name.replaceAll("[\"'`\\[\\]]", "").trim().toUpperCase();
        int dot = cleaned.lastIndexOf('.');
        return dot >= 0 ? cleaned.substring(dot + 1) : cleaned;
    }

    // =========================================================================
    // РАЗДЕЛ 7: СОБЫТИЯ (вызываются из диалект-листенеров)
    // =========================================================================

    public void onStatementEnter(String type, String snippet, int lineStart, int lineEnd) {
        initStatement(type, snippet, lineStart, lineEnd, null);
        engine.onStatementEnter(type, snippet, lineStart, lineEnd, null);
    }

    /** STAB-13 Part B: variant with explicit alias (for inline FROM subqueries). */
    public void onStatementEnter(String type, String snippet, int lineStart, int lineEnd, String alias) {
        initStatement(type, snippet, lineStart, lineEnd, alias);
        engine.onStatementEnter(type, snippet, lineStart, lineEnd, alias);
    }

    public void onStatementExit() {
        exitStatement();
        engine.onStatementExit();
    }

    public void onRoutineEnter(String name, String type, String schemaGeoid,
                               String packageGeoid, int sourceLine) {
        initRoutine(name, type, schemaGeoid, packageGeoid, sourceLine);
        engine.onRoutineEnter(name, type, schemaGeoid, packageGeoid, sourceLine);
    }

    public void onRoutineExit() {
        exitRoutine();
        engine.onRoutineExit();
    }

    public void onRoutineParameter(String name, String type, String mode) {
        engine.onRoutineParameter(name, type, mode);
    }

    public void onRoutineVariable(String name, String type) {
        engine.onRoutineVariable(name, type);
    }

    /**
     * KI-ROWTYPE-1: Called for variables declared with %ROWTYPE.
     * Creates a DaliRecord and schedules field population from the referenced table's columns.
     *
     * @param varName  variable name
     * @param tableRef raw table reference (e.g. "ORDERS" or "HR.ORDERS") without the %ROWTYPE suffix
     */
    /** HND-04: Called for variables declared with a PL/SQL user-defined COLLECTION type. */
    public void onPlTypeVariable(String varName, String typeName) {
        engine.onPlTypeVariable(varName, typeName);
    }

    public void onPlTypeVariable(String varName, String typeName, int line) {
        engine.onPlTypeVariable(varName, typeName, line);
    }

    public void onRowtypeVariable(String varName, String tableRef) {
        engine.onRowtypeVariable(varName, tableRef);
    }

    public void onRoutineReturnType(String returnType) {
        engine.onRoutineReturnType(returnType);
    }

    /** Called just before exitCursor_declaration: registers cursor name → its statement geoid. */
    public void onCursorDeclared(String cursorName) {
        engine.onCursorDeclared(cursorName);
    }

    /** Called at exitCursor_loop_param for named cursor: FOR rec IN cursor_name LOOP. */
    public void onCursorRecordNamed(String recordVar, String cursorName) {
        engine.onCursorRecordNamed(recordVar, cursorName);
    }

    /** Called at exitCursor_loop_param for inline cursor: FOR rec IN (SELECT ...) LOOP. */
    public void onCursorRecordInline(String recordVar) {
        String stmtGeoid = currentStatement();
        if (stmtGeoid != null) engine.onCursorRecordInline(recordVar, stmtGeoid);
    }

    /**
     * STAB-9: регистрирует вызов процедуры/функции из текущей routine.
     * Вызывается из PlSqlSemanticListener.enterCall_statement().
     */
    public void onCallStatement(String calledName, int line) {
        String callerGeoid = currentRoutine();
        if (callerGeoid == null || calledName == null || calledName.isBlank()) return;
        engine.onCallStatement(callerGeoid, calledName, line);
    }

    public void onTableReference(String tableName, String tableAlias, int line, int endLine) {
        String role = isInDmlTarget() ? "TARGET" : "SOURCE";
        processTableReference(tableName, tableAlias);
        engine.onTableReference(tableName, tableAlias, role, line, endLine);

        // MERGE INTO (subquery) alias — propagate subquery alias → target table geoid on parent scope
        if ("TARGET".equals(role)) {
            String mergeSubqAlias = getMergeIntoSubqueryAlias();
            if (mergeSubqAlias != null) {
                engine.registerMergeIntoSubqueryAlias(mergeSubqAlias, tableName);
                clearMergeIntoSubqueryAlias();
            }
        }
    }

    public void onColumnRef(String columnName, int line, int col, int endLine) {
        // G3-FIX: MERGE INSERT column list — resolve to target table, not as unattached atom
        if (isMergeInsertColListActive()) {
            engine.onMergeInsertColRef(columnName, line, col, endLine);
            return;
        }
        engine.onColumnRef(columnName, line, col, endLine);
    }

    public void onColumnRef(String columnName, int line, int endLine) {
        // G3-FIX: MERGE INSERT column list — resolve to target table
        if (isMergeInsertColListActive()) {
            engine.onMergeInsertColRef(columnName, line, 0, endLine);
            return;
        }
        engine.onColumnRef(columnName, line, 0, endLine);
    }

    public void onJoinEnter(String joinText, int line, int endLine) {
        current.put("in_join_context", true);
        engine.onJoinStart(joinText, line, endLine);
    }

    public void onJoinExit() {
        current.put("in_join_context", false);
        engine.onJoinExit();
    }

    // CTE — alias = имя CTE (из ANTLR ctx.query_name()):
    public void onCTEEnter(String cteText, int line, int endLine) {
        String cteName = (String) current.get("subquery_alias");  // ← имя CTE уже в current
        initStatement("CTE", cteText, line, endLine, null);
        subqueryAliasStack().add(" ");
        engine.onCTEStart(cteName, cteText, line, endLine);       // ← передаём имя
    }

    public void onCTEExit() {
        List<String> stack = subqueryAliasStack();
        if (!stack.isEmpty()) stack.remove(stack.size() - 1);
        exitStatement();
        engine.onCTEExit();
    }

    // Subquery — alias = alias подзапроса (если есть):
    public void onSubqueryEnter(String subqueryText, int line, int endLine) {
        // Сохраняем in_dml_target — обычный subquery всегда читает (SOURCE)
        // Исключение: MERGE INTO (subquery) — там in_dml_target остаётся true
        current.put("saved_dml_target_for_subquery", isInDmlTarget());
        if (getMergeIntoSubqueryAlias() == null) {
            current.put("in_dml_target", false);
        }

        List<String> stack = subqueryAliasStack();
        String alias = !stack.isEmpty() ? stack.get(stack.size() - 1) : null;
        initStatement("SUBQUERY", subqueryText, line, endLine, null);
        engine.onSubqueryStart(alias, subqueryText, line, endLine);  // ← передаём alias
    }

    public void onSubqueryExit() {
        exitStatement();
        engine.onSubqueryExit();

        // Восстанавливаем in_dml_target
        Boolean saved = (Boolean) current.get("saved_dml_target_for_subquery");
        if (saved != null) {
            current.put("in_dml_target", saved);
            current.remove("saved_dml_target_for_subquery");
        }
    }

    public void onFromEnter(int line)    { current.put("from", line);           engine.onFromStart(); }
    public void onFromExit()             { current.put("from", null);            engine.onFromExit(); }

    public void onSelectedListEnter(int line) { current.put("selected_list", line); }
    public void onSelectedListExit()          { current.put("selected_list", null); }

    /**
     * STAB-11: голый SELECT * (без table prefix) — нет select_list_elements событий.
     * Регистрируем wildcard output column "*" с order=1.
     * Аналог Python: exitSelected_list when ctx.getText() == '*'.
     */
    public void onBareStar(int line, int col) {
        String stmt = engine.getScopeManager().currentStatement();
        if (stmt == null) return;
        var stmtInfo = engine.getBuilder().getStatements().get(stmt);
        if (stmtInfo == null) return;
        if (stmtInfo.getColumnsOutput().containsKey("*")) return; // уже есть

        Map<String, Object> starCol = new java.util.LinkedHashMap<>();
        starCol.put("order",       1);
        starCol.put("name",        "*");
        starCol.put("expression",  "*");
        starCol.put("source_type", "star");
        starCol.put("alias",       null);
        stmtInfo.addColumnOutput("*", starCol);
        logger.debug("Bare SELECT * registered for stmt '{}'", stmt);
    }

    public void onWhereEnter(int line)   { current.put("where", line); }
    public void onWhereExit()            { current.put("where", null); }
    public void onHavingEnter(int line)  { current.put("having", line); }
    public void onHavingExit()           { current.put("having", null); }
    public void onOrderByEnter(int line) { current.put("order", line); }
    public void onOrderByExit()          { current.put("order", null); }
    public void onGroupByEnter(int line) {
        current.put("group_by", line);
        engine.flagCurrentStatementAggregation();
    }
    public void onGroupByExit()          { current.put("group_by", null); }
    public void onAnalyticEnter()        { engine.flagCurrentStatementWindow(); }

    public void onMergeInsertEnter() { current.put("Merge_insert_part", true); }
    public void onMergeInsertExit()  {
        current.put("Merge_insert_part", false);
        current.put("merge_insert_col_list_active", false);  // safety reset
    }
    public void onMergeUpdateEnter() { current.put("Merge_update_part", true); }
    public void onMergeUpdateExit()  { current.put("Merge_update_part", false); }

    /** G3: Called at enterMerge_element — track target column for MERGE UPDATE SET col=expr. */
    public void onMergeElementEnter(String columnName) {
        engine.onMergeElementEnter(columnName);
        // Expose the just-registered MERGE_UPDATE_TARGET as column_affected so that
        // addAtom can attach dml_target_ref to atoms in this expression.
        String stmt = engine.getScopeManager().currentStatement();
        if (stmt != null) {
            var si = engine.getBuilder().getStatements().get(stmt);
            if (si != null) {
                var acList = si.getAffectedColumns();
                if (!acList.isEmpty()) current.put("column_affected", acList.get(acList.size() - 1));
            }
        }
        current.put("in_update_set_expr", true);
        // Propagate to ScopeContext so ScopeManager.getActiveClause() returns "SET_EXPR"
        // for atoms inside this element's expression (same as regular UPDATE SET does).
        engine.setUpdateSetExpr(true);
    }

    /**
     * G3: Called at exitMerge_element — clear target column context and post-bind atoms.
     * Receives the position range of the RHS expression so that bindAtomsToMergeUpdateTarget
     * can reliably find all atoms in the expression and stamp merge_clause="UPDATE" on them.
     */
    public void onMergeElementExit(int exprStartLine, int exprStartCol,
                                   int exprEndLine,   int exprEndCol) {
        current.put("in_update_set_expr", false);
        engine.setUpdateSetExpr(false);  // mirror of onMergeElementEnter
        // Read column_ref BEFORE clearing column_affected
        @SuppressWarnings("unchecked")
        Map<String, Object> colAff = (Map<String, Object>) current.get("column_affected");
        current.put("column_affected", null);
        engine.onMergeElementExit();
        // Post-processing: stamp merge_clause="UPDATE" + dml_target_ref on atoms in expression.
        // This is analogous to bindAtomsToMergeInsertTarget for the INSERT path.
        String stmt = engine.getScopeManager().currentStatement();
        if (stmt != null && colAff != null) {
            String colRef = (String) colAff.get("column_ref");
            if (colRef != null) {
                engine.getAtomProcessor().bindAtomsToMergeUpdateTarget(
                        stmt, colRef, exprStartLine, exprStartCol, exprEndLine, exprEndCol);
            }
        }
    }

    /**
     * G3: Called at enterMerge_insert_clause when a column list (col1, col2, …) is present.
     * Registers each column as a MERGE_INSERT_TARGET affected column.
     * Also activates merge_insert_col_list_active flag so that subsequent column_name nodes
     * (walked by ANTLR inside paren_column_list) are auto-resolved to the MERGE target table.
     */
    public void onMergeInsertColumns(List<String> columnNames) {
        engine.onMergeInsertColumns(columnNames);
        // Activate flag: column_name nodes inside paren_column_list will be resolved to target table
        current.put("merge_insert_col_list_active", true);
    }

    public void onValuesClauseEnter() {
        current.put("Values_clause", true);
        current.put("Values_clause_cnt", 0);
        // Deactivate merge insert column list flag — VALUES clause follows paren_column_list
        current.put("merge_insert_col_list_active", false);
        engine.setValuesClause(true);   // propagate to ScopeContext so atoms get parent_context="VALUES"
    }
    public void onValuesClauseExit() {
        current.put("Values_clause", false);
        engine.setValuesClause(false);
    }

    /**
     * STAB-6: вызывается из exitExpression когда expression является дочерним
     * элементом values_clause. Инкрементирует позиционный счётчик и привязывает
     * атомы в диапазоне позиции к output_column_sequence = cnt.
     *
     * Аналог Python: exitExpression VALUES position counter + atom binding.
     */
    public void onValuesExpressionExit(int startLine, int startCol,
                                       int endLine,   int endCol) {
        int cnt = (int) current.getOrDefault("Values_clause_cnt", 0) + 1;
        current.put("Values_clause_cnt", cnt);

        String stmt = engine.getScopeManager().currentStatement();
        if (stmt == null) return;

        engine.getAtomProcessor().bindAtomsToOutputColumn(
                stmt, startLine, startCol, endLine, endCol, cnt);
        // G3-MERGE: if inside MERGE INSERT VALUES, bind atoms to the N-th MERGE INSERT target
        // column and set merge_clause="INSERT" (bindAtomsToMergeInsertTarget does both)
        if (Boolean.TRUE.equals(current.get("Merge_insert_part"))) {
            engine.getAtomProcessor().bindAtomsToMergeInsertTarget(
                    stmt, startLine, startCol, endLine, endCol, cnt);
        }
        logger.debug("VALUES expr #{}: lines {}-{} bound in stmt '{}'",
                cnt, startLine, endLine, stmt);
    }

    public void onDmlTargetEnter() { current.put("in_dml_target", true); }
    public void onDmlTargetExit()  { current.put("in_dml_target", false); }

    /**
     * UPDATE SET target column registration (left-hand side of SET col = expr).
     *
     * Called from enterColumn_based_update_set_clause for each single-column assignment.
     * Registers the column as a DML target (UPDATE affected column with poliage_update),
     * then sets in_update_set_expr=true so that subsequent atoms in the expression
     * get parent_context="SET_EXPR" instead of "UPDATE" — preventing them from
     * also receiving a poliage_update entry (fixing Python Bug B4 analog).
     *
     * For multi-column assignments (paren_column_list = subquery) each column
     * is registered via onUpdateSetColumnList; the expr-guard is still set.
     */
    public void onUpdateSetColumn(String colName) {
        if (colName == null || colName.isBlank()) return;
        String stmt = engine.getScopeManager().currentStatement();
        if (stmt == null) return;
        var si = engine.getBuilder().getStatements().get(stmt);
        if (si == null) return;

        List<String> targets = si.getTargetTableGeoids();
        String targetGeoid = targets.isEmpty() ? null : targets.get(0);
        String columnRef   = targetGeoid != null ? targetGeoid + "." + colName : colName;
        si.addAffectedColumn(columnRef, colName, targetGeoid, null, "UPDATE", "target");

        // Expose as column_affected for atom binding (mirrors Python _current['column_affected'])
        List<Map<String, Object>> acList = si.getAffectedColumns();
        if (!acList.isEmpty()) current.put("column_affected", acList.get(acList.size() - 1));

        // Switch context: atoms in the expression must NOT get poliage_update.
        // Both current (BaseSemanticListener) and ScopeContext flags are set so that
        // ScopeContext.getActiveClause() returns "SET_EXPR" for subsequent atoms.
        current.put("in_update_set_expr", true);
        engine.setUpdateSetExpr(true);
        logger.debug("UPDATE SET target: {} table={}", colName, targetGeoid);
    }

    /** Multi-column UPDATE SET: (col1, col2) = subquery. */
    public void onUpdateSetColumnList(List<String> colNames) {
        if (colNames == null || colNames.isEmpty()) return;
        String stmt = engine.getScopeManager().currentStatement();
        if (stmt == null) return;
        var si = engine.getBuilder().getStatements().get(stmt);
        if (si == null) return;

        List<String> targets = si.getTargetTableGeoids();
        String targetGeoid = targets.isEmpty() ? null : targets.get(0);
        for (String col : colNames) {
            if (col == null || col.isBlank()) continue;
            String columnRef = targetGeoid != null ? targetGeoid + "." + col : col;
            si.addAffectedColumn(columnRef, col, targetGeoid, null, "UPDATE", "target");
        }
        current.put("in_update_set_expr", true);
        engine.setUpdateSetExpr(true);
    }

    /** Clears UPDATE SET expression context at exitColumn_based_update_set_clause. */
    public void onUpdateSetExit() {
        current.put("in_update_set_expr", false);
        engine.setUpdateSetExpr(false);
    }

    /**
     * G5: called from exitInsert_into_clause when INSERT has explicit column list.
     * Registers columns in StatementInfo (ordered), adds them to the builder,
     * and registers each as a DaliAffectedColumn target with poliage_update (type_affect=INSERT).
     */
    public void onInsertColumnList(List<String> columns) {
        String stmt = engine.getScopeManager().currentStatement();
        if (stmt == null || columns == null || columns.isEmpty()) return;
        var si = engine.getBuilder().getStatements().get(stmt);
        if (si == null) return;
        List<String> targetGeoids = si.getTargetTableGeoids();
        String targetGeoid = targetGeoids.isEmpty() ? null : targetGeoids.get(0);
        int order = 0;
        for (String col : columns) {
            si.addInsertTargetColumn(col);
            if (targetGeoid != null) {
                engine.getBuilder().addColumn(targetGeoid, col, null, null);
            }
            order++;
            String columnRef = targetGeoid != null ? targetGeoid + "." + col : col;
            si.addAffectedColumn(columnRef, col, targetGeoid, null, "INSERT", "target", order);
        }
        logger.debug("G5 INSERT columns [{}]: {}", stmt, columns);
    }

    public void onSchemaEnter(String schemaName) {
        if (schemaName == null || schemaName.isBlank()) return;
        initDatabase("");
        current.put("schema", initSchema(schemaName, null));
    }
    public void onSchemaExit() { current.put("schema", null); }

    /**
     * STAB-8: вызывается из enterCreate_view до onStatementEnter.
     * Регистрирует view как DaliTable и помечает как текущий view-target
     * для копирования output columns внутреннего SELECT.
     */
    public void onViewDeclaration(String viewName, String schemaGeoid, int line) {
        engine.onViewDeclaration(viewName, schemaGeoid, line);
    }

    /**
     * G10: called from enterView_alias_constraint when CREATE VIEW has explicit column list.
     * Registers each column name directly on the view vertex.
     */
    public void onViewColumnAliases(List<String> columns) {
        engine.onViewColumnAliases(columns);
    }

    // ═══ T14: CREATE TABLE / ALTER TABLE — DDL column registration ═══

    /**
     * Single registration point for DDL-defined tables.
     * Analogous to processTableReference() → builder.ensureTable() for DML (reconstructed),
     * but for DDL (master):
     *   initTable()             → BaseSemanticListener.tables  (local alias/name tracking)
     *   ensureTableWithType()   → StructureAndLineageBuilder.tables → str.getTables()
     *   markDdlTable()          → StructureAndLineageBuilder.ddlTableGeoids → str.getDdlTableGeoids()
     *                             → WriteHelpers.isMasterTable() = true → data_source='master'
     */
    private String initDdlTable(String tableName, String schema) {
        String geoid = initTable(tableName, null, schema, "TABLE");
        engine.getBuilder().ensureTableWithType(geoid, null, "TABLE");
        engine.getBuilder().markDdlTable(geoid);
        return geoid;
    }

    /**
     * Like initDdlTable() but does NOT call markDdlTable() — used for ALTER TABLE.
     * ALTER TABLE does not *define* the table (CREATE TABLE does), so the table/columns
     * keep data_source='reconstructed' unless a CREATE TABLE for the same geoid appears
     * in the same session (which would have already called markDdlTable via initDdlTable).
     * Still sets ddl_table_geoid to enable atom suppression and constraint registration.
     */
    private String initDdlTableNoMark(String tableName, String schema) {
        String geoid = initTable(tableName, null, schema, "TABLE");
        engine.getBuilder().ensureTableWithType(geoid, null, "TABLE");
        // markDdlTable NOT called — ALTER TABLE doesn't own the table definition
        return geoid;
    }

    /**
     * T14: Called at enterCreate_table — registers the table, sets DDL column context.
     * Subsequent enterColumn_definition events will assign ordinal positions in DDL order.
     */
    public void onCreateTableEnter(String schemaName, String tableName, String snippet,
                                   int lineStart, int lineEnd) {
        String schema = (schemaName != null && !schemaName.isBlank()) ? schemaName : currentSchema();
        String geoid  = initDdlTable(tableName, schema);
        current.put("ddl_table_geoid", geoid);
        current.put("ddl_col_ordinal", 0);  // explicit ordinal tracking for CREATE TABLE
        onStatementEnter("CREATE_TABLE", snippet, lineStart, lineEnd);
        registerDdlTableAsSource();  // allow bare column atoms in out-of-line constraints to resolve
    }

    /** T14: Called at exitCreate_table — clears DDL column context. */
    public void onCreateTableExit() {
        current.put("ddl_table_geoid", null);
        current.put("ddl_col_ordinal", null);
        onStatementExit();
    }

    /**
     * T14: Called at enterAlter_table — sets DDL context for ALTER TABLE ADD column handling.
     * Does not track explicit ordinals (uses auto-increment to append after existing columns).
     */
    public void onAlterTableEnter(String tableRef) {
        String geoid = initDdlTableNoMark(tableRef, currentSchema());
        current.put("ddl_table_geoid", geoid);
        // No ddl_col_ordinal — ALTER TABLE uses addColumn auto-increment
    }

    /** T14: Called at exitAlter_table — clears DDL column context. */
    public void onAlterTableExit() {
        current.put("ddl_table_geoid", null);
    }

    /**
     * KI-RETURN-1: Called from enterStatic_returning_clause for each INTO target.
     * Classifies the target (VARIABLE / PARAMETER / RECORD_FIELD / RECORD),
     * computes its geoid, and registers a ReturningTarget on the current statement.
     *
     * @param varName   raw target name (uppercased), e.g. "V_ID", "L_REC.STATUS", "P_OUT"
     * @param retExprs  list of returned column expressions, e.g. ["ID", "STATUS"]
     * @param stmtGeoid engine geoid of the enclosing DML statement
     * @param isBulk    true when BULK COLLECT INTO — target is treated as RECORD
     */
    public void onReturningTarget(String varName, List<String> retExprs,
                                  String stmtGeoid, boolean isBulk) {
        String routineGeoid = currentRoutine();
        if (routineGeoid == null) return;
        String kind = classifyReturningTarget(varName, routineGeoid, isBulk);
        String targetGeoid = switch (kind) {
            case "RECORD_FIELD" -> {
                String[] parts = varName.split("\\.", 2);
                yield routineGeoid + ":RECORD:" + parts[0] + ":" + parts[1];
            }
            case "PARAMETER" -> routineGeoid + ":PARAM:" + varName;
            case "RECORD"    -> routineGeoid + ":RECORD:" + varName;
            default          -> routineGeoid + ":VAR:" + varName; // VARIABLE
        };
        var si = engine.getBuilder().getStatements().get(stmtGeoid);
        if (si != null) si.addReturningTarget(kind, targetGeoid, retExprs);
    }

    private String classifyReturningTarget(String varName, String routineGeoid, boolean isBulk) {
        if (varName.contains(".")) return "RECORD_FIELD";
        if (isBulk) return "RECORD";
        var ri = engine.getBuilder().getRoutines().get(routineGeoid);
        if (ri != null && ri.hasParameter(varName)) return "PARAMETER";
        if (engine.getBuilder().getRecords()
                  .containsKey(routineGeoid + ":RECORD:" + varName)) return "RECORD";
        return "VARIABLE";
    }

    /**
     * KI-DDL-1: Records a column affected by ALTER TABLE ADD / MODIFY / DROP.
     * Computes the column geoid from the current ddl_table_geoid and registers it
     * on the current statement so RemoteWriter can emit DDL_MODIFIES edges (target_kind='column'; F-2 Sprint 0.1).
     *
     * @param colName   bare column name (will be uppercased)
     * @param operation "ADD" | "MODIFY" | "DROP"
     */
    public void onDdlColumnChange(String colName, String operation) {
        String tableGeoid = (String) current.get("ddl_table_geoid");
        if (tableGeoid == null || colName == null || colName.isBlank()) return;
        String colGeoid = tableGeoid + "." + colName.toUpperCase();
        String stmtGeoid = engine.currentStatementGeoid();
        if (stmtGeoid == null) return;
        var si = engine.getBuilder().getStatements().get(stmtGeoid);
        if (si != null) si.addAffectedColumnGeoid(colGeoid, operation);
    }

    /**
     * T14: Sets or clears the fine-grained DDL atom suppression context.
     * "SUPPRESS" prevents addAtom() from registering atoms in the current sub-tree.
     * null restores normal (DDL-aware) atom creation.
     * Called by PlSqlSemanticListener at enterColumn_definition / exitColumn_definition
     * and enterReferences_clause / exitReferences_clause.
     */
    public void setDdlAtomCtx(String ctx) {
        if (ctx == null) current.remove("ddl_atom_ctx");
        else current.put("ddl_atom_ctx", ctx);
    }

    /** Returns true when the listener is currently inside a CREATE/ALTER TABLE context. */
    public boolean isInDdlContext() {
        return current.get("ddl_table_geoid") != null;
    }

    /**
     * T14: Registers the current DDL table (ddl_table_geoid) as a source table of the current
     * statement so that bare column-name atoms in out-of-line constraints and CREATE INDEX can
     * resolve via NameResolver's implicit-table strategy.
     * Called immediately after onStatementEnter in CREATE TABLE / ALTER TABLE / CREATE INDEX flows.
     */
    public void registerDdlTableAsSource() {
        // IMPORTANT: use engine.currentStatementGeoid() not currentStatement().
        // BaseSemanticListener.currentStatement() returns a BSL-internal geoid
        // (format: "TYPE:lineStart:lineEnd:nanoTime"), while engine.getBuilder().getStatements()
        // is keyed by the engine's ScopeContext geoid (format: "TYPE:lineStart").
        // Using the BSL geoid always returns null — the table source is never registered.
        String stmtGeoid  = engine.currentStatementGeoid();
        String tableGeoid = (String) current.get("ddl_table_geoid");
        if (stmtGeoid == null || tableGeoid == null) return;
        var stmtInfo = engine.getBuilder().getStatements().get(stmtGeoid);
        if (stmtInfo == null) return;
        var tableInfo = engine.getBuilder().getTables().get(tableGeoid);
        String tableName = tableInfo != null ? tableInfo.tableName() : tableGeoid;
        stmtInfo.addSourceTable(tableGeoid, tableName, tableName, "TABLE");
    }

    /**
     * T14 (Step 1): Enters a CREATE INDEX statement scope and registers the indexed table
     * as source, so column-name atoms from the index column list can resolve via implicit table.
     *
     * @param tableRef  raw table reference (may include schema prefix, e.g. "FIN.INVOICES")
     * @param snippet   SQL text for the statement
     * @param lineStart start line in source file
     * @param lineEnd   end line in source file
     */
    public void onCreateIndexEnter(String tableRef, String snippet, int lineStart, int lineEnd) {
        onStatementEnter("CREATE_INDEX", snippet, lineStart, lineEnd);
        if (tableRef == null || tableRef.isBlank()) return;
        String schemaName = null;
        String tableName  = tableRef;
        int dot = tableRef.lastIndexOf('.');
        if (dot >= 0) {
            schemaName = tableRef.substring(0, dot);
            tableName  = tableRef.substring(dot + 1);
        }
        String schema = (schemaName != null && !schemaName.isBlank()) ? schemaName : currentSchema();
        String tableGeoid = initTable(tableName, null, schema, "TABLE");
        if (tableGeoid == null) return;
        // Ensure the indexed table exists in the engine's builder (may be absent if index
        // is in a separate file and the CREATE TABLE hasn't been seen in this session yet).
        engine.getBuilder().ensureTableWithType(tableGeoid, null, "TABLE");
        current.put("ddl_table_geoid", tableGeoid);  // track for registerDdlTableAsSource
        registerDdlTableAsSource();
        current.remove("ddl_table_geoid");            // CREATE INDEX does not set long-lived DDL context
    }

    /**
     * T14: Called at enterColumn_definition when inside CREATE TABLE or ALTER TABLE ADD context.
     * For CREATE TABLE: registers column with explicit ordinal_position from DDL declaration order.
     * For ALTER TABLE ADD: registers column with auto-increment ordinal_position.
     *
     * @param columnName    column identifier (already cleaned)
     * @param dataType      DDL type string, e.g. "VARCHAR2(100)", "NUMBER(10,2)", "DATE"; null if absent
     * @param isRequired    true if the column has NOT NULL constraint in DDL
     * @param defaultValue  text of the DEFAULT expression (e.g. "'N'", "SYSTIMESTAMP"); null if absent
     */
    public void onDdlColumnDefinition(String columnName, String dataType,
                                      boolean isRequired, String defaultValue) {
        String tableGeoid = (String) current.get("ddl_table_geoid");
        if (tableGeoid == null || columnName == null || columnName.isBlank()) return;
        Object ordinalObj = current.get("ddl_col_ordinal");
        if (ordinalObj != null) {
            // CREATE TABLE: explicit ordinal from DDL declaration order
            int ordinal = (Integer) ordinalObj + 1;
            current.put("ddl_col_ordinal", ordinal);
            engine.getBuilder().addColumnWithOrdinal(tableGeoid, columnName, null, null, ordinal,
                    dataType, isRequired, defaultValue);
        } else {
            // ALTER TABLE ADD: auto-increment ordinal (appended after existing columns)
            engine.getBuilder().addColumn(tableGeoid, columnName, null, null, dataType, isRequired, defaultValue);
        }
    }

    // ─── Constraint registration ──────────────────────────────────────────────

    /**
     * Called from PlSqlSemanticListener when a PRIMARY KEY out_of_line_constraint is encountered.
     *
     * @param constraintName declared name, or null
     * @param pkColumns      ordered column names in the host table
     */
    public void onPrimaryKeyConstraint(String constraintName, List<String> pkColumns) {
        String tableGeoid = (String) current.get("ddl_table_geoid");
        if (tableGeoid == null) return;
        String geoid = ConstraintInfo.buildGeoid(tableGeoid, ConstraintInfo.TYPE_PK, constraintName, pkColumns);
        engine.getBuilder().addConstraint(geoid, ConstraintInfo.TYPE_PK, constraintName,
                tableGeoid, pkColumns, null, null, null);
    }

    /**
     * Called from PlSqlSemanticListener when a FOREIGN KEY out_of_line_constraint is encountered.
     *
     * @param constraintName declared name, or null
     * @param fkColumns      ordered FK column names in the host table (uppercase)
     * @param refTableRaw    raw referenced table text (e.g. "CRM.CUSTOMERS")
     * @param refColumns     ordered referenced column names (uppercase); may be empty
     * @param onDelete       "CASCADE", "SET NULL", or null
     */
    /**
     * KI-005: Records a UNIQUE constraint found inside CREATE TABLE or ALTER TABLE.
     */
    public void onUniqueConstraint(String constraintName, List<String> cols) {
        String tableGeoid = (String) current.get("ddl_table_geoid");
        if (tableGeoid == null) return;
        String geoid = ConstraintInfo.buildGeoid(tableGeoid, ConstraintInfo.TYPE_UQ, constraintName, cols);
        engine.getBuilder().addConstraint(geoid, ConstraintInfo.TYPE_UQ, constraintName,
                tableGeoid, cols, null, null, null);
    }

    /**
     * KI-005: Records a CHECK constraint found inside CREATE TABLE or ALTER TABLE.
     */
    public void onCheckConstraint(String constraintName, String expr) {
        String tableGeoid = (String) current.get("ddl_table_geoid");
        if (tableGeoid == null) return;
        List<String> disc = expr != null ? List.of(expr) : List.of();
        String geoid = ConstraintInfo.buildGeoid(tableGeoid, ConstraintInfo.TYPE_CH, constraintName, disc);
        engine.getBuilder().addConstraint(geoid, ConstraintInfo.TYPE_CH, constraintName,
                tableGeoid, List.of(), null, null, null);
        ConstraintInfo ci = engine.getBuilder().getConstraints().get(geoid);
        if (ci != null) ci.setCheckExpression(expr);
    }

    public void onForeignKeyConstraint(String constraintName, List<String> fkColumns,
                                       String refTableRaw, List<String> refColumns,
                                       String onDelete) {
        String tableGeoid = (String) current.get("ddl_table_geoid");
        if (tableGeoid == null) return;

        // Resolve referenced table geoid — reconstruct the referenced table (and columns)
        // just like DML statements do, so FK targets appear as DaliTable/DaliColumn in the graph.
        String refTableGeoid = null;
        if (refTableRaw != null && !refTableRaw.isBlank()) {
            String clean = cleanIdentifier(refTableRaw);
            if (clean != null) {
                String schemaForRef = null;
                String tableNameForRef = clean;
                if (clean.contains(".")) {
                    int dot = clean.lastIndexOf('.');
                    schemaForRef = clean.substring(0, dot);
                    tableNameForRef = clean.substring(dot + 1);
                } else if (currentSchema() != null) {
                    schemaForRef = currentSchema();
                }
                // Reconstruct the referenced table (creates DaliTable if absent)
                refTableGeoid = engine.getBuilder().ensureTable(tableNameForRef, schemaForRef);
            }
        }

        // Reconstruct referenced columns on the target table
        if (refTableGeoid != null && refColumns != null) {
            for (String refCol : refColumns) {
                if (refCol != null && !refCol.isBlank()) {
                    engine.getBuilder().addColumn(refTableGeoid, refCol.toUpperCase(), null, null);
                }
            }
        }

        String geoid = ConstraintInfo.buildGeoid(tableGeoid, ConstraintInfo.TYPE_FK, constraintName, fkColumns);
        engine.getBuilder().addConstraint(geoid, ConstraintInfo.TYPE_FK, constraintName,
                tableGeoid, fkColumns, refTableGeoid, refColumns, onDelete);
    }

    public void onColumnAlias(String alias) {
        current.put("column_alias", alias != null ? cleanIdentifier(alias) : null);
    }

    public void onAtom(String text, int line, int col, int endLine, int endCol, boolean isComplex,
                       List<String> tokens, List<Map<String, String>> tokenDetails, int nestedAtomCount) {
        addAtom(text, line, col, endLine, endCol, isComplex, tokens, tokenDetails, nestedAtomCount);
    }

    // ═══ Output column binding (port from Python exitSelect_list_elements) ═══

    public void onOutputColumnEnter(int line, int col) {
        current.put("column_output_start_line", line);
        current.put("column_output_start_col", col);
    }

    public void onOutputColumnExit(int startLine, int startCol, int endLine, int endCol,
                                    String expressionText) {
        onOutputColumnExit(startLine, startCol, endLine, endCol, expressionText, false);
    }

    @SuppressWarnings("unchecked")
    public void onOutputColumnExit(int startLine, int startCol, int endLine, int endCol,
                                    String expressionText, boolean isTableStar) {
        String stmt = currentStatement();
        if (stmt == null) return;
        String engineStmt = engine.getScopeManager().currentStatement();
        if (engineStmt == null) return;

        String alias = (String) current.get("column_alias");

        var stmtInfo = engine.getBuilder().getStatements().get(engineStmt);
        if (stmtInfo == null) return;

        int order = stmtInfo.getColumnsOutput().size() + 1;
        // G13: generate synthetic name when alias and expression are both absent.
        // For unaliased simple column references (e.g. "t1.col"), strip the table
        // alias prefix so the name stores just the bare column name ("col").
        String colName = (alias != null && !alias.isBlank()) ? alias
                       : (expressionText != null && !expressionText.isBlank())
                               ? bareColumnName(expressionText, order)
                       : ("EXPR_" + order);
        Map<String, Object> columnInfo = new LinkedHashMap<>();
        columnInfo.put("order", order);
        columnInfo.put("name", colName);
        columnInfo.put("expression", expressionText);
        columnInfo.put("alias", alias);
        columnInfo.put("source_type", isTableStar ? "table_star" : "expression");

        stmtInfo.addColumnOutput(colName, columnInfo);

        // Position-based atom binding
        engine.getAtomProcessor().bindAtomsToOutputColumn(
                engineStmt, startLine, startCol, endLine, endCol, order);

        current.put("column_alias", null);
        current.put("column_output", columnInfo);
    }

    // ═══ JOIN complete (port from Python exitJoin_clause) ═══

    public void onJoinComplete(String joinType, List<String> conditions, String sourceAlias, int lineStart) {
        String currentStmt = currentStatement();
        if (currentStmt == null) return;

        // Target = table being joined (set by enterTable_ref_aux within join)
        String targetName = currentTable();
        String targetAlias = currentTableAlias();

        // Resolve source
        String sourceGeoid = null;
        String sourceType = "table";
        if (sourceAlias != null && !sourceAlias.isBlank()) {
            var resolved = engine.getNameResolver().resolve(sourceAlias, "any", currentStmt);
            if (resolved.isResolved()) {
                sourceGeoid = resolved.getGeoid();
                sourceType = resolved.getType().toLowerCase();
            }
        }

        // Resolve target
        String targetGeoid = null;
        String targetType = "table";
        if (targetName != null && !targetName.isBlank()) {
            var resolved = engine.getNameResolver().resolve(targetName, "any", currentStmt);
            if (resolved.isResolved()) {
                targetGeoid = resolved.getGeoid();
                targetType = resolved.getType().toLowerCase();
            }
        }

        String conditionText = conditions != null ? String.join(" AND ", conditions) : "";
        engine.onJoinComplete(joinType, targetGeoid, targetAlias, targetType,
                sourceGeoid, sourceAlias, sourceType, conditionText, lineStart);
    }

    // =========================================================================
    // РАЗДЕЛ 8: РЕЗУЛЬТАТ
    // =========================================================================

    /**
     * Extracts the bare column name from a SELECT list expression when no alias is present.
     *
     * For simple column references like "t1.col1" → returns "col1".
     * For complex expressions (containing spaces, parens, operators) → returns the expression as-is
     * so that a synthetic EXPR_{order} name is not generated unnecessarily.
     *
     * Collision note: if two unaliased columns resolve to the same bare name (e.g. "t1.id" and
     * "t2.id" both become "id"), the second column overwrites the first in columnsOutput.
     * Users should use explicit aliases to disambiguate such cases.
     */
    private static String bareColumnName(String expr, int order) {
        if (expr == null || expr.isBlank()) return "EXPR_" + order;
        // Complex expression — keep as-is to avoid misleading names
        if (expr.contains(" ") || expr.contains("(") || expr.contains(")")
                || expr.contains("'") || expr.contains("+") || expr.contains("-")
                || expr.contains("*") || expr.contains("/") || expr.contains("||")) {
            return expr;
        }
        // Simple qualified reference: strip schema/table prefix, keep last component
        int dot = expr.lastIndexOf('.');
        return dot >= 0 ? expr.substring(dot + 1) : expr;
    }

    public Map<String, Map<String, Object>> getStatements() { return Collections.unmodifiableMap(statements); }
    public Map<String, Map<String, Object>> getRoutines()   { return Collections.unmodifiableMap(routines); }
    public Map<String, Map<String, Object>> getTables()     { return Collections.unmodifiableMap(tables); }
    public Map<String, Map<String, Object>> getSchemas()    { return Collections.unmodifiableMap(schemas); }
    public Map<String, Map<String, Object>> getDatabases()  { return Collections.unmodifiableMap(databases); }
}