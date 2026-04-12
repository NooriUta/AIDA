package com.hound.diagnostic;

import com.arcadedb.database.Database;
import com.arcadedb.database.DatabaseFactory;
import com.arcadedb.query.sql.executor.Result;
import com.arcadedb.query.sql.executor.ResultSet;
import com.arcadedb.remote.RemoteDatabase;

import java.util.*;
import java.util.function.Function;

/**
 * ArcadeDB Diagnostic Runner — проверочные запросы качества разбора.
 *
 * Поддерживает embedded (Database) и remote (RemoteDatabase) режимы.
 *
 * Использование:
 *   gradlew diagnostic -PdbPath=target/hound-diag
 *   hound --input ... --db-type arcadedb --diag
 *
 * 12 секций: общая статистика, atoms, output columns, routines,
 * tables/columns, statements, joins, lineage, geoid quality,
 * health check, drill-down, graph analytics.
 *
 * Embedded ArcadeDB API caveats (vs ArcadeDB Studio):
 *   - FROM V / FROM E not supported → per-type count loop
 *   - round() not available → percentages computed in Java
 *   - NOT LIKE not supported → computed by subtraction
 *   - alias + out()/in() in same query may fail → drop alias prefix
 */
public class DiagnosticRunner {

    /** Abstraction over Database.query() and RemoteDatabase.query() */
    @FunctionalInterface
    public interface QueryExecutor {
        ResultSet query(String language, String sql);
    }

    private final QueryExecutor queryFn;
    private int totalQueries = 0;
    private int failedQueries = 0;

    /** Embedded mode */
    public DiagnosticRunner(Database db) {
        this.queryFn = db::query;
    }

    /** Remote mode */
    public DiagnosticRunner(RemoteDatabase remoteDb) {
        this.queryFn = remoteDb::query;
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: DiagnosticRunner <arcade-db-path>");
            System.exit(1);
        }

        String dbPath = args[0];
        DatabaseFactory factory = new DatabaseFactory(dbPath);
        if (!factory.exists()) {
            System.err.println("Database not found: " + dbPath);
            System.exit(1);
        }

        try (Database db = factory.open()) {
            DiagnosticRunner runner = new DiagnosticRunner(db);
            runner.runAll();
        }
    }

    public void runAll() {
        long t0 = System.currentTimeMillis();

        section0_Namespaces();
        section1_GeneralStats();
        section2_AtomsResolution();
        section3_OutputColumns();
        section4_Routines();
        section5_TablesColumns();
        section6_Statements();
        section7_Joins();
        section8_LineageEdges();
        section9_GeoidQuality();
        section10_HealthCheck();
        section11_DrillDown();
        section12_GraphAnalytics();
        section13_ResolutionLog();
        section14_SchemaLog();

        long elapsed = System.currentTimeMillis() - t0;
        println("");
        println("═══════════════════════════════════════════════════════════════");
        println("  DIAGNOSTICS COMPLETE: %d queries (%d failed) in %dms",
                totalQueries, failedQueries, elapsed);
        println("═══════════════════════════════════════════════════════════════");
    }

    // ═══════════════════════════════════════════════════════════════
    // 0. Namespaces / Database overview (ADR-014)
    // ═══════════════════════════════════════════════════════════════

    private void section0_Namespaces() {
        header("0. NAMESPACES (ADR-014: папка = база данных)");

        query("Приложения (DaliApplication)",
                "SELECT app_name, app_geoid FROM DaliApplication ORDER BY app_name");

        query("BELONGS_TO_APP рёбра: DaliDatabase → DaliApplication",
                "SELECT out.db_name as db, in.app_name as app FROM BELONGS_TO_APP ORDER BY app, db");

        query("Зарегистрированные базы данных (DaliDatabase)",
                "SELECT db_name, db_geoid, created_at FROM DaliDatabase ORDER BY db_name");

        query("Сессии по базам данных",
                "SELECT db_name, count(*) as session_count FROM DaliSession GROUP BY db_name ORDER BY session_count DESC");

        query("Канонические схемы по базам (DaliSchema с db_name)",
                "SELECT db_name, schema_name, schema_geoid FROM DaliSchema WHERE db_name IS NOT NULL ORDER BY db_name, schema_name");

        query("CONTAINS_SCHEMA рёбра: DaliDatabase → DaliSchema",
                "SELECT out.db_name as db, in.schema_name as schema, in.schema_geoid as geoid " +
                "FROM CONTAINS_SCHEMA ORDER BY db, schema");

        query("Схемы без db_name (ad-hoc)",
                "SELECT count(*) as adhoc_schemas FROM DaliSchema WHERE db_name IS NULL");

        query("Канонические таблицы по базам (DaliTable с db_name)",
                "SELECT db_name, count(*) as table_count FROM DaliTable WHERE db_name IS NOT NULL GROUP BY db_name ORDER BY table_count DESC");

        query("Канонические колонки по базам",
                "SELECT db_name, count(*) as col_count FROM DaliColumn WHERE db_name IS NOT NULL GROUP BY db_name ORDER BY col_count DESC");

        query("Ad-hoc таблицы (без namespace, db_name IS NULL)",
                "SELECT count(*) as adhoc_tables FROM DaliTable WHERE db_name IS NULL");

        query("Топ-10 таблиц по числу использований (READS_FROM)",
                "SELECT out.table_geoid as table_geoid, out.db_name as db_name, count(*) as reads " +
                "FROM READS_FROM GROUP BY out.table_geoid ORDER BY reads DESC LIMIT 10");

        query("Pipeline-статистика по базам данных (DaliPerfStats)",
                "SELECT db_name, count(*) as files, " +
                "sum(ms_parse) as total_parse_ms, sum(ms_walk) as total_walk_ms, " +
                "sum(cnt_statements) as total_stmts, sum(cnt_atoms) as total_atoms " +
                "FROM DaliPerfStats WHERE db_name IS NOT NULL GROUP BY db_name ORDER BY db_name");
    }

    // ═══════════════════════════════════════════════════════════════
    // 1. Общая статистика
    // ═══════════════════════════════════════════════════════════════

    private void section1_GeneralStats() {
        header("1. ОБЩАЯ СТАТИСТИКА");

        // Vertex counts (FROM V not supported in embedded — enumerate types)
        String[] vtxTypes = {"DaliSession","DaliDatabase","DaliApplication","DaliSchema",
                "DaliPackage","DaliTable","DaliColumn","DaliRoutine","DaliStatement",
                "DaliAtom","DaliOutputColumn","DaliJoin","DaliParameter","DaliVariable","DaliSnippet"};
        println("");
        println("  ▸ Vertex counts по типам");
        totalQueries++;
        for (String vt : vtxTypes) {
            try {
                long cnt = countQuery("SELECT count(*) as v FROM " + vt);
                if (cnt > 0) println("    %-22s %d", vt, cnt);
            } catch (Exception ignored) {}
        }

        // Edge counts
        String[] edgTypes = {"BELONGS_TO_SESSION","CONTAINS_SCHEMA","CONTAINS_TABLE",
                "CONTAINS_ROUTINE","BELONGS_TO_APP",
                "HAS_COLUMN","HAS_PARAMETER","HAS_VARIABLE","CHILD_OF","CONTAINS_STMT",
                "HAS_OUTPUT_COL","HAS_ATOM","HAS_JOIN","READS_FROM","WRITES_TO",
                "USES_SUBQUERY","ROUTINE_USES_TABLE","CALLS","ATOM_REF_TABLE",
                "ATOM_REF_COLUMN","ATOM_PRODUCES","DATA_FLOW","FILTER_FLOW",
                "JOIN_FLOW","UNION_FLOW","NESTED_IN"};
        println("");
        println("  ▸ Edge counts по типам");
        totalQueries++;
        for (String et : edgTypes) {
            try {
                long cnt = countQuery("SELECT count(*) as v FROM " + et);
                if (cnt > 0) println("    %-22s %d", et, cnt);
            } catch (Exception ignored) {}
        }

        query("Сессии и файлы",
                "SELECT session_id, file_path, dialect, processing_time_ms " +
                "FROM DaliSession ORDER BY processing_time_ms DESC");

        query("Pipeline timing по сессиям (DaliPerfStats)",
                "SELECT session_id, file_path, dialect, " +
                "ms_parse, ms_walk, ms_resolve, ms_write_vtx, ms_write_edge, ms_total, " +
                "count_tokens, " +
                "cnt_tables, cnt_statements, cnt_routines, cnt_atoms, " +
                "cnt_atoms_resolved, cnt_atoms_failed, cnt_output_cols, cnt_joins " +
                "FROM DaliPerfStats ORDER BY created_at DESC");
    }

    // ═══════════════════════════════════════════════════════════════
    // 2. Atoms — качество resolution
    // ═══════════════════════════════════════════════════════════════

    private void section2_AtomsResolution() {
        header("2. ATOMS — КАЧЕСТВО RESOLUTION");

        // round() not available in embedded — just show counts
        query("Статус resolution",
                "SELECT status, count(*) as cnt " +
                "FROM DaliAtom GROUP BY status ORDER BY cnt DESC");

        query("Resolved atoms с привязкой к таблице",
                "SELECT count(*) as resolved_with_table FROM DaliAtom " +
                "WHERE table_geoid IS NOT NULL AND table_geoid != 'null'");

        query("Топ-20 нерешённых атомов",
                "SELECT atom_text, parent_context, count(*) as cnt FROM DaliAtom " +
                "WHERE status != 'Обработано' AND status != 'constant' AND status != 'function_call' " +
                "GROUP BY atom_text, parent_context ORDER BY cnt DESC LIMIT 20");

        query("Нерешённые атомы по контексту",
                "SELECT parent_context, count(*) as failed_cnt FROM DaliAtom " +
                "WHERE status NOT IN ['Обработано', 'constant', 'function_call'] " +
                "GROUP BY parent_context ORDER BY failed_cnt DESC");

        query("SOURCE.* / TARGET.* — MERGE bug",
                "SELECT atom_text, statement_geoid FROM DaliAtom " +
                "WHERE atom_text LIKE 'SOURCE.%' OR atom_text LIKE 'TARGET.%' LIMIT 30");

        query("Atoms без output_column_sequence (SELECT resolved)",
                "SELECT count(*) as no_output_col FROM DaliAtom " +
                "WHERE output_column_sequence IS NULL AND parent_context = 'SELECT' " +
                "AND status = 'Обработано'");
    }

    // ═══════════════════════════════════════════════════════════════
    // 3. Output Columns — покрытие SELECT
    // ═══════════════════════════════════════════════════════════════

    private void section3_OutputColumns() {
        header("3. OUTPUT COLUMNS — ПОКРЫТИЕ SELECT");

        query("Топ statements по output columns",
                "SELECT out('HAS_OUTPUT_COL').size() as oc_count, stmt_geoid, type " +
                "FROM DaliStatement WHERE type IN ['SELECT', 'CTE'] " +
                "ORDER BY oc_count DESC LIMIT 20");

        query("Output columns без привязанного атома (DATA_FLOW не будет)",
                "SELECT col_key, name, expression, statement_geoid " +
                "FROM DaliOutputColumn WHERE in('ATOM_PRODUCES').size() = 0 LIMIT 20");
    }

    // ═══════════════════════════════════════════════════════════════
    // 4. Routines — полнота структуры
    // ═══════════════════════════════════════════════════════════════

    private void section4_Routines() {
        header("4. ROUTINES — ПОЛНОТА СТРУКТУРЫ");

        query("Routines total (excluding packages)",
                "SELECT count(*) as total FROM DaliRoutine WHERE routine_type != 'PACKAGE'");
        query("Parameters count",
                "SELECT count(*) as total_params FROM DaliParameter");
        query("Variables count",
                "SELECT count(*) as total_vars FROM DaliVariable");

        query("Packages (IS-A DaliRoutine, ADR-012)",
                "SELECT routine_geoid, routine_type, schema_geoid FROM DaliPackage ORDER BY schema_geoid");

        query("Routines по типам",
                "SELECT routine_type, count(*) as cnt FROM DaliRoutine " +
                "GROUP BY routine_type ORDER BY cnt DESC");

        query("Routines без statements (пустые тела)",
                "SELECT routine_geoid, routine_type FROM DaliRoutine " +
                "WHERE out('CONTAINS_STMT').size() = 0 ORDER BY routine_geoid LIMIT 20");

        query("Вложенные routines (NESTED_IN)",
                "SELECT out.routine_geoid as parent, in.routine_geoid as child FROM NESTED_IN LIMIT 20");
    }

    // ═══════════════════════════════════════════════════════════════
    // 5. Tables & Columns — полнота схемы
    // ═══════════════════════════════════════════════════════════════

    private void section5_TablesColumns() {
        header("5. TABLES & COLUMNS — ПОЛНОТА СХЕМЫ");

        query("Таблицы по схемам",
                "SELECT schema_geoid, count(*) as table_cnt FROM DaliTable " +
                "GROUP BY schema_geoid ORDER BY table_cnt DESC");

        query("Таблицы без колонок",
                "SELECT table_geoid, table_name, table_type FROM DaliTable " +
                "WHERE out('HAS_COLUMN').size() = 0 ORDER BY table_name LIMIT 20");

        query("Топ таблиц по колонкам",
                "SELECT table_geoid, out('HAS_COLUMN').size() as col_cnt FROM DaliTable " +
                "ORDER BY col_cnt DESC LIMIT 20");

        query("Таблицы без lineage (не source и не target)",
                "SELECT table_geoid, table_name FROM DaliTable " +
                "WHERE in('READS_FROM').size() = 0 AND in('WRITES_TO').size() = 0 LIMIT 20");

        query("Таблицы: READS_FROM vs WRITES_TO (топ write targets)",
                "SELECT table_geoid, table_name, " +
                "in('READS_FROM').size() as reads, in('WRITES_TO').size() as writes " +
                "FROM DaliTable WHERE in('WRITES_TO').size() > 0 " +
                "ORDER BY writes DESC LIMIT 20");
    }

    // ═══════════════════════════════════════════════════════════════
    // 6. Statements — иерархия и покрытие
    // ═══════════════════════════════════════════════════════════════

    private void section6_Statements() {
        header("6. STATEMENTS — ИЕРАРХИЯ И ПОКРЫТИЕ");

        query("Типы statements",
                "SELECT type, count(*) as cnt FROM DaliStatement " +
                "GROUP BY type ORDER BY cnt DESC");

        query("Глубина вложенности",
                "SELECT stmt_geoid, type, " +
                "in('CHILD_OF').size() as parent_count, out('CHILD_OF').size() as child_count " +
                "FROM DaliStatement ORDER BY child_count DESC LIMIT 20");

        query("Statements без source tables (DML only)",
                "SELECT stmt_geoid, type FROM DaliStatement " +
                "WHERE type IN ['SELECT', 'UPDATE', 'DELETE', 'MERGE'] " +
                "AND out('READS_FROM').size() = 0 AND out('WRITES_TO').size() = 0 " +
                "LIMIT 20");

        query("MERGE: READS_FROM vs WRITES_TO",
                "SELECT stmt_geoid, " +
                "out('READS_FROM').size() as reads, out('WRITES_TO').size() as writes " +
                "FROM DaliStatement WHERE type = 'MERGE' ORDER BY stmt_geoid");

        query("CTE statements count",
                "SELECT count(*) as cte_stmts FROM DaliStatement WHERE type = 'CTE'");

        query("USES_SUBQUERY edges count",
                "SELECT count(*) as uses_subquery_edges FROM USES_SUBQUERY");
    }

    // ═══════════════════════════════════════════════════════════════
    // 7. Joins — полнота
    // ═══════════════════════════════════════════════════════════════

    private void section7_Joins() {
        header("7. JOINS — ПОЛНОТА");

        query("Join типы",
                "SELECT join_type, count(*) as cnt FROM DaliJoin " +
                "GROUP BY join_type ORDER BY cnt DESC");

        query("Joins без source table",
                "SELECT join_type, statement_geoid, target_alias FROM DaliJoin " +
                "WHERE source_table_geoid IS NULL OR source_table_geoid = 'null' LIMIT 20");

        query("Joins без target table",
                "SELECT join_type, statement_geoid, source_alias FROM DaliJoin " +
                "WHERE target_table_geoid IS NULL OR target_table_geoid = 'null' LIMIT 20");
    }

    // ═══════════════════════════════════════════════════════════════
    // 8. Lineage edges — связность графа
    // ═══════════════════════════════════════════════════════════════

    private void section8_LineageEdges() {
        header("8. LINEAGE EDGES — СВЯЗНОСТЬ ГРАФА");

        query("ATOM_REF_TABLE", "SELECT count(*) as cnt FROM ATOM_REF_TABLE");
        query("ATOM_REF_COLUMN", "SELECT count(*) as cnt FROM ATOM_REF_COLUMN");
        query("ATOM_PRODUCES", "SELECT count(*) as cnt FROM ATOM_PRODUCES");
        query("DATA_FLOW", "SELECT count(*) as cnt FROM DATA_FLOW");
        query("FILTER_FLOW", "SELECT count(*) as cnt FROM FILTER_FLOW");

        // Drop alias prefix 'a.' — embedded doesn't support alias + out() combo
        query("Resolved atoms БЕЗ ATOM_REF edges (should be 0)",
                "SELECT atom_text, status, table_geoid FROM DaliAtom " +
                "WHERE status = 'Обработано' AND table_geoid IS NOT NULL " +
                "AND table_geoid != 'null' AND out('ATOM_REF_TABLE').size() = 0 LIMIT 20");

        query("FILTER_FLOW candidates (WHERE/HAVING resolved)",
                "SELECT count(*) as filter_flow_candidates FROM DaliAtom " +
                "WHERE parent_context IN ['WHERE', 'HAVING'] AND status = 'Обработано' " +
                "AND table_geoid IS NOT NULL");
    }

    // ═══════════════════════════════════════════════════════════════
    // 9. Качество GEOID
    // ═══════════════════════════════════════════════════════════════

    private void section9_GeoidQuality() {
        header("9. КАЧЕСТВО GEOID");

        query("Routines с точкой в geoid (старый баг S0.1)",
                "SELECT routine_geoid FROM DaliRoutine WHERE routine_geoid LIKE '%.%' LIMIT 10");

        query("Tables с qualified names (SCHEMA.TABLE)",
                "SELECT count(*) as qualified_tables FROM DaliTable WHERE table_geoid LIKE '%.%'");

        // NOT LIKE not supported — compute via subtraction
        println("");
        println("  ▸ Tables без qualified names");
        totalQueries++;
        try {
            long total = countQuery("SELECT count(*) as v FROM DaliTable");
            long qualified = countQuery("SELECT count(*) as v FROM DaliTable WHERE table_geoid LIKE '%.%'");
            println("    unqualified_tables: %d", total - qualified);
        } catch (Exception e) {
            failedQueries++;
            println("    ✗ FAILED: %s", e.getMessage());
        }

        query("Hierarchical stmt geoids (4+ levels)",
                "SELECT count(*) as hierarchical FROM DaliStatement WHERE stmt_geoid LIKE '%:%:%:%'");
    }

    // ═══════════════════════════════════════════════════════════════
    // 10. Итоговый health check
    // ═══════════════════════════════════════════════════════════════

    private void section10_HealthCheck() {
        header("10. ИТОГОВЫЙ HEALTH CHECK");

        Map<String, String> metrics = new LinkedHashMap<>();
        metrics.put("sessions",         "SELECT count(*) as v FROM DaliSession");
        metrics.put("routines",         "SELECT count(*) as v FROM DaliRoutine");
        metrics.put("packages",         "SELECT count(*) as v FROM DaliPackage");
        metrics.put("statements",       "SELECT count(*) as v FROM DaliStatement");
        metrics.put("tables",           "SELECT count(*) as v FROM DaliTable");
        metrics.put("columns",          "SELECT count(*) as v FROM DaliColumn");
        metrics.put("atoms_total",      "SELECT count(*) as v FROM DaliAtom");
        metrics.put("atoms_resolved",   "SELECT count(*) as v FROM DaliAtom WHERE status = 'Обработано'");
        metrics.put("atoms_failed",     "SELECT count(*) as v FROM DaliAtom WHERE status NOT IN ['Обработано','constant','function_call']");
        metrics.put("output_cols",      "SELECT count(*) as v FROM DaliOutputColumn");
        metrics.put("joins",            "SELECT count(*) as v FROM DaliJoin");
        metrics.put("parameters",       "SELECT count(*) as v FROM DaliParameter");
        metrics.put("variables",        "SELECT count(*) as v FROM DaliVariable");
        metrics.put("READS_FROM",       "SELECT count(*) as v FROM READS_FROM");
        metrics.put("WRITES_TO",        "SELECT count(*) as v FROM WRITES_TO");
        metrics.put("ATOM_REF_TABLE",   "SELECT count(*) as v FROM ATOM_REF_TABLE");
        metrics.put("ATOM_REF_COLUMN",  "SELECT count(*) as v FROM ATOM_REF_COLUMN");
        metrics.put("NESTED_IN",        "SELECT count(*) as v FROM NESTED_IN");
        metrics.put("USES_SUBQUERY",    "SELECT count(*) as v FROM USES_SUBQUERY");
        metrics.put("DATA_FLOW",        "SELECT count(*) as v FROM DATA_FLOW");
        metrics.put("FILTER_FLOW",      "SELECT count(*) as v FROM FILTER_FLOW");
        metrics.put("CONTAINS_ROUTINE", "SELECT count(*) as v FROM CONTAINS_ROUTINE");

        println("  ┌──────────────────────┬──────────┐");
        println("  │ Metric               │ Count    │");
        println("  ├──────────────────────┼──────────┤");

        for (var e : metrics.entrySet()) {
            try {
                long val = countQuery(e.getValue());
                println("  │ %-20s │ %8d │", e.getKey(), val);
            } catch (Exception ex) {
                println("  │ %-20s │ %8s │", e.getKey(), "ERROR");
            }
        }

        println("  └──────────────────────┴──────────┘");

        // Resolution percentage
        try {
            long total = countQuery("SELECT count(*) as v FROM DaliAtom");
            long resolved = countQuery("SELECT count(*) as v FROM DaliAtom WHERE status = 'Обработано'");
            long constants = countQuery("SELECT count(*) as v FROM DaliAtom WHERE status = 'constant'");
            long funcCalls = countQuery("SELECT count(*) as v FROM DaliAtom WHERE status = 'function_call'");
            long failed = total - resolved - constants - funcCalls;

            if (total > 0) {
                println("");
                println("  Atom Resolution:");
                println("    Total:     %d", total);
                println("    Resolved:  %d (%.1f%%)", resolved, 100.0 * resolved / total);
                println("    Constants: %d (%.1f%%)", constants, 100.0 * constants / total);
                println("    Functions: %d (%.1f%%)", funcCalls, 100.0 * funcCalls / total);
                println("    Failed:    %d (%.1f%%)", failed, 100.0 * failed / total);

                // Quality = (resolved + constants + functions) / total
                // Constants and function_calls are correctly handled — not failures
                double qualityPct = 100.0 * (resolved + constants + funcCalls) / total;
                String quality = qualityPct > 90 ? "ОТЛИЧНО (>90%%)" :
                                 qualityPct > 80 ? "ХОРОШО (>80%%)" :
                                 qualityPct > 70 ? "МИНИМУМ (>70%%)" : "НИЖЕ НОРМЫ (<70%%)";
                println("    Quality:   %.1f%% %s", qualityPct, quality);
            }
        } catch (Exception ignored) {}
    }

    // ═══════════════════════════════════════════════════════════════
    // 11. Диагностика конкретного файла / процедуры
    // ═══════════════════════════════════════════════════════════════

    private void section11_DrillDown() {
        header("11. DRILL-DOWN ПРИМЕРЫ");

        query("Топ statements по atom count",
                "SELECT stmt_geoid, type, line_start, " +
                "out('HAS_ATOM').size() as atoms, " +
                "out('HAS_OUTPUT_COL').size() as output_cols " +
                "FROM DaliStatement ORDER BY atoms DESC LIMIT 10");

        query("Lineage: DATA_FLOW edges",
                "SELECT out.column_geoid as source_col, out.column_name as source_name, " +
                "in.name as output_col, in.statement_geoid as statement " +
                "FROM DATA_FLOW LIMIT 20");

        query("Граф вызовов (CALLS)",
                "SELECT out.routine_geoid as caller, in.routine_geoid as callee " +
                "FROM CALLS ORDER BY caller LIMIT 30");
    }

    // ═══════════════════════════════════════════════════════════════
    // 12. Graph Analytics
    // ═══════════════════════════════════════════════════════════════

    private void section12_GraphAnalytics() {
        header("12. GRAPH ANALYTICS");

        // 12.3 Hotspot Analysis
        query("Топ таблиц по использованию (hotspot)",
                "SELECT table_geoid, table_name, " +
                "in('READS_FROM').size() as read_by, " +
                "in('WRITES_TO').size() as written_by " +
                "FROM DaliTable " +
                "ORDER BY read_by DESC LIMIT 15");

        query("Топ колонок по числу атомов (самые используемые)",
                "SELECT column_geoid, column_name, table_geoid, " +
                "in('ATOM_REF_COLUMN').size() as usage_count " +
                "FROM DaliColumn ORDER BY usage_count DESC LIMIT 15");

        query("Топ statements по числу атомов (самые сложные)",
                "SELECT stmt_geoid, type, line_start, " +
                "out('HAS_ATOM').size() as atom_count " +
                "FROM DaliStatement ORDER BY atom_count DESC LIMIT 15");

        // 12.5 Dead Code Detection
        query("Таблицы без атомов и без lineage (dead tables)",
                "SELECT table_geoid, table_name, table_type FROM DaliTable " +
                "WHERE in('ATOM_REF_TABLE').size() = 0 " +
                "AND in('READS_FROM').size() = 0 AND in('WRITES_TO').size() = 0 " +
                "ORDER BY table_name LIMIT 20");

        query("Колонки без ATOM_REF_COLUMN (никогда не читаются)",
                "SELECT column_geoid, column_name, table_geoid FROM DaliColumn " +
                "WHERE in('ATOM_REF_COLUMN').size() = 0 " +
                "ORDER BY table_geoid, column_name LIMIT 20");

        query("Packages без routines (пустые пакеты)",
                "SELECT package_geoid, schema_geoid FROM DaliPackage " +
                "WHERE out('CONTAINS_ROUTINE').size() = 0");

        // 12.3 Hotspot: read+write crossover
        query("Таблицы-перекрёстки (и source и target — UPSERT кандидаты)",
                "SELECT table_geoid, table_name, " +
                "in('READS_FROM').size() as reads, in('WRITES_TO').size() as writes " +
                "FROM DaliTable " +
                "WHERE in('READS_FROM').size() > 0 AND in('WRITES_TO').size() > 0 " +
                "ORDER BY writes DESC LIMIT 20");

        // 12.6 Subquery & CTE depth
        query("USES_SUBQUERY граф",
                "SELECT out.stmt_geoid as parent_stmt, out.type as parent_type, " +
                "in.stmt_geoid as subquery, in.type as subquery_type " +
                "FROM USES_SUBQUERY ORDER BY parent_stmt LIMIT 20");

        // 12.4 Routine call graph
        query("Листовые routines (не вызывают никого)",
                "SELECT routine_geoid, routine_type FROM DaliRoutine " +
                "WHERE out('CALLS').size() = 0 AND routine_type != 'PACKAGE' " +
                "ORDER BY routine_geoid LIMIT 20");

        query("Корневые routines (никто не вызывает их)",
                "SELECT routine_geoid, routine_type FROM DaliRoutine " +
                "WHERE in('CALLS').size() = 0 AND routine_type != 'PACKAGE' " +
                "ORDER BY routine_geoid LIMIT 20");
    }

    // ═══════════════════════════════════════════════════════════════
    // 13. DaliResolutionLog — S1.PRE atom quality audit
    // ═══════════════════════════════════════════════════════════════

    private void section13_ResolutionLog() {
        header("13. DALIRESOLUTIONLOG — ATOM QUALITY AUDIT (S1.PRE)");

        query("Всего записей в DaliResolutionLog",
                "SELECT count(*) as total FROM DaliResolutionLog");

        query("Разбивка по result_kind",
                "SELECT result_kind, count(*) as cnt FROM DaliResolutionLog " +
                "GROUP BY result_kind ORDER BY cnt DESC");

        query("Топ-30 нерешённых raw_input (S1.BUG candidates)",
                "SELECT raw_input, parent_context, count(*) as cnt FROM DaliResolutionLog " +
                "WHERE result_kind = 'unresolved' " +
                "GROUP BY raw_input, parent_context ORDER BY cnt DESC LIMIT 30");

        query("Нерешённые по parent_context",
                "SELECT parent_context, count(*) as cnt FROM DaliResolutionLog " +
                "WHERE result_kind = 'unresolved' " +
                "GROUP BY parent_context ORDER BY cnt DESC");

        query("raw_input содержит '(' — неправильный парсинг скобки",
                "SELECT raw_input, parent_context, note, count(*) as cnt FROM DaliResolutionLog " +
                "WHERE raw_input LIKE '%(%' " +
                "GROUP BY raw_input, parent_context, note ORDER BY cnt DESC LIMIT 20");

        query("raw_input содержит '.' — подозрение на schema-prefix",
                "SELECT raw_input, parent_context, note, count(*) as cnt FROM DaliResolutionLog " +
                "WHERE raw_input LIKE '%.%' AND result_kind = 'unresolved' " +
                "GROUP BY raw_input, parent_context, note ORDER BY cnt DESC LIMIT 20");

        query("Нерешённые по note (стратегия разрешения)",
                "SELECT note, count(*) as cnt FROM DaliResolutionLog " +
                "WHERE result_kind = 'unresolved' " +
                "GROUP BY note ORDER BY cnt DESC LIMIT 20");

        query("Нерешённые function_call атомы (unexpected)",
                "SELECT raw_input, parent_context, count(*) as cnt FROM DaliResolutionLog " +
                "WHERE result_kind = 'function_call' AND parent_context IN ['SELECT','WHERE','HAVING'] " +
                "GROUP BY raw_input, parent_context ORDER BY cnt DESC LIMIT 20");

        query("Разбивка unresolved по сессиям (топ проблемные файлы)",
                "SELECT statement_geoid, count(*) as unresolved_cnt FROM DaliResolutionLog " +
                "WHERE result_kind = 'unresolved' " +
                "GROUP BY statement_geoid ORDER BY unresolved_cnt DESC LIMIT 20");
    }

    // ═══════════════════════════════════════════════════════════════
    // 14. DaliSchemaLog — suspicious schema registrations
    // ═══════════════════════════════════════════════════════════════

    private void section14_SchemaLog() {
        header("14. DALIESCHEMALOG — SUSPICIOUS SCHEMA REGISTRATIONS");

        query("Всего записей в DaliSchemaLog",
                "SELECT count(*) as total FROM DaliSchemaLog");

        query("Разбивка по reason",
                "SELECT reason, count(*) as cnt FROM DaliSchemaLog " +
                "GROUP BY reason ORDER BY cnt DESC");

        query("Топ-30 уникальных schema_name по frequency",
                "SELECT schema_name, reason, count(*) as cnt FROM DaliSchemaLog " +
                "GROUP BY schema_name, reason ORDER BY cnt DESC LIMIT 30");
    }

    // ═══════════════════════════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════════════════════════

    private void header(String title) {
        println("");
        println("═══════════════════════════════════════════════════════════════");
        println("  %s", title);
        println("═══════════════════════════════════════════════════════════════");
    }

    private void query(String label, String sql) {
        totalQueries++;
        println("");
        println("  ▸ %s", label);
        try {
            ResultSet rs = queryFn.query("sql", sql);
            List<Map<String, Object>> rows = new ArrayList<>();
            Set<String> allKeys = new LinkedHashSet<>();

            while (rs.hasNext()) {
                Result r = rs.next();
                Map<String, Object> row = new LinkedHashMap<>(r.toMap());
                row.remove("@rid");
                row.remove("@cat");
                allKeys.addAll(row.keySet());
                rows.add(row);
            }

            if (rows.isEmpty()) {
                println("    (нет данных)");
                return;
            }

            // Column widths
            Map<String, Integer> widths = new LinkedHashMap<>();
            for (String key : allKeys) widths.put(key, Math.max(key.length(), 4));
            for (Map<String, Object> row : rows) {
                for (String key : allKeys) {
                    Object val = row.get(key);
                    int len = val != null ? trunc(String.valueOf(val), 60).length() : 4;
                    widths.merge(key, len, Math::max);
                }
            }

            // Header
            StringBuilder hdr = new StringBuilder("    ");
            StringBuilder sep = new StringBuilder("    ");
            for (String key : allKeys) {
                int w = widths.get(key);
                hdr.append(String.format("%-" + w + "s  ", key));
                sep.append("-".repeat(w)).append("  ");
            }
            println(hdr.toString());
            println(sep.toString());

            // Rows (max 30)
            int printed = 0;
            for (Map<String, Object> row : rows) {
                if (printed >= 30) {
                    println("    ... (%d more rows)", rows.size() - 30);
                    break;
                }
                StringBuilder line = new StringBuilder("    ");
                for (String key : allKeys) {
                    int w = widths.get(key);
                    Object val = row.get(key);
                    String s = val != null ? trunc(String.valueOf(val), 60) : "null";
                    line.append(String.format("%-" + w + "s  ", s));
                }
                println(line.toString());
                printed++;
            }
            println("    [%d rows]", rows.size());

        } catch (Exception e) {
            failedQueries++;
            println("    ✗ FAILED: %s", e.getMessage());
        }
    }

    private long countQuery(String sql) {
        ResultSet rs = queryFn.query("sql", sql);
        if (rs.hasNext()) {
            Object v = rs.next().getProperty("v");
            if (v instanceof Number) return ((Number) v).longValue();
        }
        return 0;
    }

    private static String trunc(String s, int max) {
        if (s == null) return "null";
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }

    private static void println(String fmt, Object... args) {
        if (args.length == 0) System.out.println(fmt);
        else System.out.printf(fmt + "%n", args);
    }
}
