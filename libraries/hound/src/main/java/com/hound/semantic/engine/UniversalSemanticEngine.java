package com.hound.semantic.engine;

import com.hound.api.HoundEventListener;
import com.hound.api.NoOpHoundEventListener;
import com.hound.semantic.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map;

/**
 * UniversalSemanticEngine — главный оркестратор семантического анализа.
 * Аналог Python BaseSQLListener — центральная точка всех семантических событий.
 *
 * Все компоненты связываются через wire() при конструировании.
 */
public class UniversalSemanticEngine {

    private static final Logger logger = LoggerFactory.getLogger(UniversalSemanticEngine.class);

    private final ScopeManager scopeManager;
    private final NameResolver nameResolver;
    private final StructureAndLineageBuilder builder;
    private final AtomProcessor atomProcessor;
    private final JoinProcessor joinProcessor;
    private final HoundEventListener listener;
    private final String file;

    /** No-arg constructor — uses NoOp listener (backward-compatible). */
    public UniversalSemanticEngine() {
        this(NoOpHoundEventListener.INSTANCE, "");
    }

    /**
     * Constructor with event listener.
     *
     * @param listener receives onAtomExtracted / onRecordRegistered / onSemanticWarning events
     * @param file     file path forwarded to listener callbacks
     */
    public UniversalSemanticEngine(HoundEventListener listener, String file) {
        this.listener = listener;
        this.file = file;
        this.scopeManager = new ScopeManager();
        this.nameResolver = new NameResolver();
        this.builder = new StructureAndLineageBuilder(listener, file);
        this.atomProcessor = new AtomProcessor(listener, file);
        this.joinProcessor = new JoinProcessor();

        // Wire dependencies
        this.nameResolver.wire(builder, scopeManager);
        this.atomProcessor.wire(nameResolver, builder, scopeManager);
    }

    // ═══════ Accessors for BaseSemanticListener / DialectAdapter ═══════

    public ScopeManager getScopeManager() { return scopeManager; }
    public NameResolver getNameResolver() { return nameResolver; }
    public StructureAndLineageBuilder getBuilder() { return builder; }
    public AtomProcessor getAtomProcessor() { return atomProcessor; }
    public JoinProcessor getJoinProcessor() { return joinProcessor; }

    /** Returns the current statement geoid as known to the engine's scope manager.
     *  Use this (not BaseSemanticListener.currentStatement()) when looking up
     *  StatementInfo objects in the engine's StructureAndLineageBuilder. */
    public String currentStatementGeoid() { return scopeManager.currentStatement(); }

    // ═══════════════════════════════════════════════════════════════
    // Statement lifecycle
    // ═══════════════════════════════════════════════════════════════

    public void onStatementEnter(String type, String snippet, int lineStart, int lineEnd, String alias) {
        String parentStmt = scopeManager.currentStatement();
        String routineGeoid = scopeManager.currentRoutine();

        ScopeContext ctx = ScopeContext.forStatement(type, snippet, lineStart, lineEnd,
                parentStmt, routineGeoid, alias);

        // Register alias on PARENT scope so atoms in the parent statement can resolve it
        // (e.g., MERGE USING subquery aliases SOURCE/TARGET)
        if (alias != null && !alias.isBlank()) {
            ScopeContext parentScope = scopeManager.peek();
            if (parentScope != null) {
                parentScope.registerAlias(alias.toUpperCase(), ctx.getStatementGeoid());
            }
        }

        scopeManager.push(ctx);

        builder.addStatement(ctx.getStatementGeoid(), type, snippet, lineStart, lineEnd,
                parentStmt, routineGeoid);

        // S2.BUG-1: persist alias on StatementInfo so resolveSubqueryByAlias finds it
        // after the scope stack is unwound (e.g. MERGE USING (SELECT...) SOURCE).
        if (alias != null && !alias.isBlank()) {
            var newStmtInfo = builder.getStatements().get(ctx.getStatementGeoid());
            if (newStmtInfo != null) newStmtInfo.setAlias(alias);
        }

        if (parentStmt != null) {
            var parentInfo = builder.getStatements().get(parentStmt);
            if (parentInfo != null) {
                parentInfo.addChildStatement(ctx.getStatementGeoid());
            }
        }

        logger.debug("Statement ENTER: {} [{}] line {}-{}, parent={}",
                type, ctx.getStatementGeoid(), lineStart, lineEnd, parentStmt);
    }

    public void onStatementExit() {
        String stmt = scopeManager.currentStatement();
        if (stmt != null) {
            StatementInfo si = builder.getStatements().get(stmt);

            // B2.OC1 — diagnostics
            if (si != null) {
                int directAtoms = atomProcessor.getAtomsForStatement(stmt).size();
                logger.info("DIAG EXIT [{}] type={} outputCols={} atoms={}",
                        stmt, si.getType(), si.getColumnsOutput().size(), directAtoms);

                // CTE-specific: atoms always live in the child SUBQUERY scope,
                // not in the CTE wrapper itself. Log this clearly so it's not mistaken
                // for a bug.
                if ("CTE".equals(si.getType()) && directAtoms == 0) {
                    int childAtomTotal = si.getChildStatements().stream()
                            .mapToInt(child -> atomProcessor.getAtomsForStatement(child).size())
                            .sum();
                    logger.info("CTE SCOPE [{}]: 0 direct atoms — expected. Child scopes: {} atoms in {} child(ren) {}. "
                            + "Atoms register to the innermost active scope (SUBQUERY inside CTE), not the CTE wrapper.",
                            stmt, childAtomTotal, si.getChildStatements().size(),
                            si.getChildStatements());
                }
            }

            // 1. Resolve star subquery columns
            resolveStarSubqueryColumnsForStmt(stmt);
            // 2. Atoms resolution
            atomProcessor.resolveAtomsOnStatementExit(stmt);
            // 3. Post-process joins (check unresolved sources)
            postProcessJoins(stmt);
            // 4. UNION column merging + flag transfer
            ScopeContext current = scopeManager.peek();
            if (current != null && current.isUnion()) {
                mergeUnionColumns(stmt);
                if (si != null) si.setIsUnion(true);
            }

            // B2.SQ1+SQ2 — subquery source registration in parent
            if (si != null && !si.getSourceSubqueries().isEmpty()) {
                logger.info("DIAG SUBQUERY [{}]: {} sources", stmt, si.getSourceSubqueries().size());
            }
            if (current != null) {
                String type = current.getStatementType();
                String alias = current.getAlias();
                // STAB-13 Part B: aliased SELECT scopes (FROM (SELECT ...) t) are also source subqueries
                boolean isInlineSelect = "SELECT".equals(type) && alias != null && !alias.isBlank();
                if ("SUBQUERY".equals(type) || "CTE".equals(type) || "USUBQUERY".equals(type)
                        || isInlineSelect) {
                    String parentStmt = current.getParentStatement();
                    if (parentStmt != null) {
                        StatementInfo parentInfo = builder.getStatements().get(parentStmt);
                        if (parentInfo != null) {
                            parentInfo.addSourceSubquery(stmt, alias, type);
                        }
                    }
                }
            }

            // STAB-8: SUBQUERY/SELECT that is a direct child of CREATE_VIEW
            //         → copy output columns as view ColumnInfo (HAS_COLUMN edges)
            if (si != null && currentViewTargetGeoid != null
                    && ("SELECT".equals(si.getType()) || "SUBQUERY".equals(si.getType()))) {
                // Guard: only for the direct view body — parent must be CREATE_VIEW
                String parentGeoid = si.getParentStatementGeoid();
                StatementInfo parentSi = parentGeoid != null
                        ? builder.getStatements().get(parentGeoid) : null;
                if (parentSi != null && "CREATE_VIEW".equals(parentSi.getType())) {
                    for (var entry : si.getColumnsOutput().entrySet()) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> col = (Map<String, Object>) entry.getValue();
                        String colName = (String) col.getOrDefault("name", entry.getKey());
                        if (colName != null && !colName.isBlank() && !"*".equals(colName)) {
                            builder.addColumn(currentViewTargetGeoid, colName,
                                    (String) col.get("expression"), (String) col.get("alias"));
                        }
                    }
                    logger.debug("VIEW [{}]: {} output cols from inner SUBQUERY",
                            currentViewTargetGeoid, si.getColumnsOutput().size());
                }
            }
            // STAB-8: clear view target when CREATE_VIEW statement itself exits
            if (si != null && "CREATE_VIEW".equals(si.getType())) {
                currentViewTargetGeoid = null;
            }

            // OC propagation: lift output columns up the scope chain so that DaliOutputColumn
            // records are written under the geoid that atoms reference as their table_geoid.
            //
            // CTE chain: SELECT → SUBQUERY → CTE
            //   Atoms resolve table_geoid to the CTE geoid.
            //   Output cols are registered on the inner SELECT.
            //   We must propagate: SELECT → SUBQUERY, then SUBQUERY → CTE.
            //
            // CURSOR chain: SELECT → CURSOR (direct child)
            //   Propagate SELECT → CURSOR.
            if (si != null) {
                String parentGeoid = si.getParentStatementGeoid();
                if (parentGeoid != null) {
                    StatementInfo parentSi = builder.getStatements().get(parentGeoid);
                    if (parentSi != null) {
                        String siType     = si.getType();
                        String parentType = parentSi.getType();
                        boolean shouldPropagate =
                                // SELECT → CURSOR
                                ("SELECT".equals(siType) && isCursorStatementType(parentType))
                                // SELECT → SUBQUERY (middle of CTE chain)
                             || ("SELECT".equals(siType) && "SUBQUERY".equals(parentType))
                                // SUBQUERY → CTE (top of CTE chain)
                             || ("SUBQUERY".equals(siType) && "CTE".equals(parentType));
                        if (shouldPropagate) {
                            for (var entry : si.getColumnsOutput().entrySet()) {
                                parentSi.getColumnsOutput().putIfAbsent(entry.getKey(), entry.getValue());
                            }
                            logger.debug("OC-propagate: {} {} → {} {}",
                                    siType, si.getGeoid(),
                                    parentType, parentGeoid);
                        }
                    }
                }
            }

            // READS_FROM propagation: when a SELECT/SUBQUERY exits as a direct child of a CTE,
            // lift its physical-table source references to the CTE so that the CTE has its own
            // READS_FROM edges to the tables it reads from (directly or through inline subqueries).
            if (si != null) {
                String parentGeoid = si.getParentStatementGeoid();
                if (parentGeoid != null) {
                    StatementInfo parentSi = builder.getStatements().get(parentGeoid);
                    if (parentSi != null && "CTE".equals(parentSi.getType())) {
                        String siType = si.getType();
                        if ("SELECT".equals(siType) || "SUBQUERY".equals(siType)) {
                            for (var srcEntry : si.getSourceTables().entrySet()) {
                                // Only propagate physical tables, not sub-statement geoids
                                if (builder.getTables().containsKey(srcEntry.getKey())) {
                                    parentSi.addSourceTable(srcEntry.getKey(), null);
                                }
                            }
                        }
                    }
                }
            }

            // H1.2-INS: INSERT INTO t (cols) SELECT … — bind SELECT output cols to INSERT
            // target cols by position so table_ref carries the target column reference.
            // Only fires when the INSERT has an explicit column list (insertTargetColumns non-empty).
            if (si != null && "SELECT".equals(si.getType())) {
                String parentGeoid = si.getParentStatementGeoid();
                if (parentGeoid != null) {
                    StatementInfo parentSi = builder.getStatements().get(parentGeoid);
                    if (parentSi != null && "INSERT".equals(parentSi.getType())
                            && !parentSi.getInsertTargetColumns().isEmpty()) {
                        List<String> insertCols = parentSi.getInsertTargetColumns();
                        List<String> tgtGeoids  = parentSi.getTargetTableGeoids();
                        String tgtGeoid = tgtGeoids.isEmpty() ? null : tgtGeoids.get(0);
                        if (tgtGeoid != null) {
                            var ocEntries = new java.util.ArrayList<>(si.getColumnsOutput().entrySet());
                            for (int i = 0; i < Math.min(ocEntries.size(), insertCols.size()); i++) {
                                ocEntries.get(i).getValue().put("table_ref",
                                        tgtGeoid + "." + insertCols.get(i).toUpperCase());
                            }
                            parentSi.setSubtype("INSERT_SELECT");
                            logger.debug("H1.2-INS: {} SELECT cols → INSERT target {}", insertCols.size(), tgtGeoid);
                        }
                    }
                }
            }

            // H1.2-UPD: UPDATE SET col = (subquery) — bind USUBQUERY output cols to the
            // UPDATE target column(s) registered just before this subquery was opened.
            // Handles both single: SET col=(SELECT…) and multi: SET (c1,c2)=(SELECT…).
            if (si != null && "USUBQUERY".equals(si.getType())) {
                String parentGeoid = si.getParentStatementGeoid();
                if (parentGeoid != null) {
                    StatementInfo parentSi = builder.getStatements().get(parentGeoid);
                    if (parentSi != null && "UPDATE".equals(parentSi.getType())) {
                        int ocCount = si.getColumnsOutput().size();
                        if (ocCount > 0) {
                            // Collect UPDATE-type affected cols registered so far (in order)
                            var updateTargets = new java.util.ArrayList<Map<String, Object>>();
                            for (Map<String, Object> ac : parentSi.getAffectedColumns()) {
                                if ("UPDATE".equals(ac.get("source_type"))) updateTargets.add(ac);
                            }
                            // The last ocCount entries correspond to this subquery's targets
                            int from = Math.max(0, updateTargets.size() - ocCount);
                            var relevant = updateTargets.subList(from, updateTargets.size());
                            var ocEntries = new java.util.ArrayList<>(si.getColumnsOutput().entrySet());
                            for (int i = 0; i < Math.min(ocEntries.size(), relevant.size()); i++) {
                                Map<String, Object> ac = relevant.get(i);
                                String tblGeoid = (String) ac.get("table_geoid");
                                String colName  = (String) ac.get("column_name");
                                if (tblGeoid != null && colName != null)
                                    ocEntries.get(i).getValue().putIfAbsent("table_ref",
                                            tblGeoid + "." + colName.toUpperCase());
                            }
                            logger.debug("H1.2-UPD: USUBQUERY {} cols → UPDATE target", ocCount);
                        }
                    }
                }
            }
        }
        // STAB-4: уровень 1 — resolve pending columns пока scope ещё открыт
        if (stmt != null) resolvePartialPendingForStatement(stmt);

        ScopeContext popped = scopeManager.pop();
        logger.debug("Statement EXIT: {} [{}]",
                popped != null ? popped.getStatementType() : "?",
                popped != null ? popped.getStatementGeoid() : "?");
    }

    // ═══════════════════════════════════════════════════════════════
    // VIEW declaration (STAB-8)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Вызывается из enterCreate_view.
     * Регистрирует view как DaliTable с типом "VIEW" и запоминает geoid
     * для последующего копирования output columns внутреннего SELECT.
     */
    public void onViewDeclaration(String viewName, String schemaGeoid, int line) {
        if (viewName == null || viewName.isBlank()) return;
        String viewGeoid = builder.ensureTableWithType(viewName, schemaGeoid, "VIEW");
        currentViewTargetGeoid = viewGeoid;
        logger.debug("VIEW declared: '{}' schema='{}' line={}", viewGeoid, schemaGeoid, line);
    }

    /**
     * G10: called when CREATE VIEW has explicit column alias list — (col1, col2, ...).
     * Registers each alias as a column directly on the view vertex (before the inner SELECT runs).
     */
    public void onViewColumnAliases(List<String> columns) {
        if (currentViewTargetGeoid == null || columns == null) return;
        for (String col : columns) {
            if (col != null && !col.isBlank()) {
                builder.addColumn(currentViewTargetGeoid, col, null, null);
            }
        }
        logger.debug("G10 VIEW columns [{}]: {}", currentViewTargetGeoid, columns);
    }

    // ═══════════════════════════════════════════════════════════════
    // Table references
    // ═══════════════════════════════════════════════════════════════

    /**
     * Вызывается из Dialect Listener при обнаружении ссылки на таблицу.
     * @param tableName имя таблицы (может быть schema.table)
     * @param alias алиас (nullable)
     * @param role "SOURCE" или "TARGET"
     * @param line строка в SQL
     * @param endLine конечная строка
     */
    public void onTableReference(String tableName, String alias, String role,
                                 int line, int endLine) {
        if (tableName == null || tableName.isBlank()) return;

        String currentStmt = scopeManager.currentStatement();

        // G8: detect CTE / inline-view reference in FROM — route to sourceSubquery
        // instead of creating a spurious physical-table vertex.
        if ("SOURCE".equals(role) && currentStmt != null) {
            ResolvedRef cteRef = nameResolver.resolve(tableName, "subquery", currentStmt);
            if (cteRef.isResolved()
                    && ("CTE".equals(cteRef.getType()) || "SUBQUERY".equals(cteRef.getType()))) {
                var stmtInfo = builder.getStatements().get(currentStmt);
                if (stmtInfo != null) {
                    stmtInfo.addSourceSubquery(cteRef.getGeoid(), alias, cteRef.getGeoid());
                    builder.addLineageEdge(cteRef.getGeoid(), currentStmt, "READS_FROM", currentStmt);
                }
                if (alias != null && !alias.isBlank()) {
                    scopeManager.registerAlias(alias.toUpperCase(), cteRef.getGeoid());
                }
                scopeManager.registerAlias(tableName.toUpperCase(), cteRef.getGeoid());
                logger.debug("Table REF (CTE/SQ): {} alias={} role={} → {}",
                        tableName, alias, role, cteRef.getGeoid());
                return;
            }
        }

        String schemaGeoid = scopeManager.currentSchema();
        String tableGeoid = builder.ensureTable(tableName, schemaGeoid);

        // Register alias
        if (alias != null && !alias.isBlank()) {
            scopeManager.registerAlias(alias.toUpperCase(), tableGeoid);
            builder.addTableAlias(tableGeoid, alias);
        }
        // Also register table name as alias
        scopeManager.registerAlias(tableName.toUpperCase(), tableGeoid);

        // Source/Target в текущем statement
        if (currentStmt != null) {
            var stmtInfo = builder.getStatements().get(currentStmt);
            if (stmtInfo != null) {
                TableInfo tbl = builder.getTables().get(tableGeoid);
                String tblName = tbl != null ? tbl.tableName() : tableName;
                String tblType = tbl != null ? tbl.tableType() : "table";
                if ("TARGET".equals(role)) {
                    stmtInfo.addTargetTable(tableGeoid, alias, tblName, tblType);
                    scopeManager.peek().setTargetTable(tableGeoid);
                    builder.addLineageEdge(currentStmt, tableGeoid, "WRITES_TO", currentStmt);
                } else {
                    stmtInfo.addSourceTable(tableGeoid, alias, tblName, tblType);
                    builder.addLineageEdge(tableGeoid, currentStmt, "READS_FROM", currentStmt);
                }
            }
        }

        logger.debug("Table REF: {} alias={} role={} stmt={}", tableName, alias, role, currentStmt);
    }

    /** Backward-compatible version without role */
    public void onTableReference(String tableName, int line, int endLine) {
        onTableReference(tableName, null, "SOURCE", line, endLine);
    }

    /**
     * MERGE INTO (SELECT * FROM target_table) msubquery —
     * регистрирует алиас подзапроса (msubquery) → geoid целевой таблицы
     * на РОДИТЕЛЬСКОМ scope (MERGE), чтобы msubquery.column резолвился в target_table.column.
     */
    public void registerMergeIntoSubqueryAlias(String subqueryAlias, String tableName) {
        String schemaGeoid = scopeManager.currentSchema();
        String tableGeoid = builder.ensureTable(tableName, schemaGeoid);
        scopeManager.registerAliasOnParent(subqueryAlias.toUpperCase(), tableGeoid);
        logger.debug("MERGE INTO subquery alias: {} → {} (table {})", subqueryAlias, tableGeoid, tableName);
    }

    // ═══════════════════════════════════════════════════════════════
    // Column references
    // ═══════════════════════════════════════════════════════════════

    public void onColumnRef(String fullRef, int line, int col, int endLine) {
        if (fullRef == null || fullRef.isBlank()) return;

        String currentStmt = scopeManager.currentStatement();
        String parentContext = scopeManager.getActiveClause();

        // G11: parse "schema.table.column", "table.column" or "column"
        String tablePart;
        String columnPart;
        long dotCount = fullRef.chars().filter(c -> c == '.').count();
        if (dotCount >= 2) {
            // schema.table.column — treat "schema.table" as the table part
            int lastDot = fullRef.lastIndexOf('.');
            tablePart = fullRef.substring(0, lastDot);
            columnPart = fullRef.substring(lastDot + 1);
        } else {
            String[] parts = fullRef.split("\\.", 2);
            tablePart = parts.length > 1 ? parts[0] : null;
            columnPart = parts.length > 1 ? parts[1] : parts[0];
        }

        // Resolve table
        String tableGeoid = null;
        if (tablePart != null) {
            ResolvedRef resolved = nameResolver.resolve(tablePart, "any", currentStmt);
            if (resolved.isResolved()) {
                tableGeoid = resolved.getGeoid();
            }
        } else {
            ResolvedRef implicit = nameResolver.resolveImplicitTable(currentStmt);
            if (implicit.isResolved()) {
                tableGeoid = implicit.getGeoid();
            }
        }

        // G9: queue unresolved column for post-walk pending resolution
        if (tableGeoid == null && currentStmt != null) {
            addPendingColumn(fullRef, currentStmt);
        }

        // Add column to structure + usage tracking.
        // Guard: skip SubQuery/CTE/MERGE-USING SELECT statement geoids — those atoms link via
        // ATOM_REF_OUTPUT_COL, not DaliColumn. Physical table geoids are NOT in getStatements().
        if (tableGeoid != null) {
            if (!builder.getStatements().containsKey(tableGeoid)) {
                builder.addColumn(tableGeoid, columnPart, null, null);
            }
            String colGeoid = tableGeoid + "." + columnPart.toUpperCase();
            ColumnInfo colInfo = builder.getColumns().get(colGeoid);
            if (colInfo != null) {
                colInfo.addUsedInStatement(currentStmt);
                colInfo.addUsedInRoutine(scopeManager.currentRoutine());
            }
        }

        // Register as atom
        atomProcessor.registerAtom(fullRef, line, col, endLine, 0,
                "COLUMN", currentStmt, parentContext);
    }

    public void onColumnRef(String fullRef) {
        onColumnRef(fullRef, 0, 0, 0);
    }

    // ═══════════════════════════════════════════════════════════════
    // JOIN
    // ═══════════════════════════════════════════════════════════════

    public void onJoinStart(String joinText, int line, int endLine) {
        ScopeContext ctx = scopeManager.peek();
        if (ctx != null) {
            ctx.setInJoinContext(true);
        }
    }

    /**
     * Регистрирует JOIN-операцию с полной информацией.
     */
    public void onJoinComplete(String joinType, String targetTableGeoid, String targetAlias,
                               String targetType, String sourceTableGeoid, String sourceAlias,
                               String sourceType, String conditions, int lineStart) {
        String currentStmt = scopeManager.currentStatement();
        JoinInfo joinInfo = new JoinInfo(
                joinType, targetTableGeoid, targetAlias, targetType,
                sourceTableGeoid, sourceAlias, sourceType,
                conditions, currentStmt, lineStart
        );
        joinProcessor.registerJoin(currentStmt, joinInfo);

        // Also register in statement info
        var stmtInfo = builder.getStatements().get(currentStmt);
        if (stmtInfo != null) {
            stmtInfo.addJoin(joinInfo);
        }
    }

    public void onJoinExit() {
        ScopeContext ctx = scopeManager.peek();
        if (ctx != null) {
            ctx.setInJoinContext(false);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Routine lifecycle
    // ═══════════════════════════════════════════════════════════════

    /**
     * Вход в routine. Geoid формируется через цепочку:
     *   Package (контейнер):   "PKG_NAME"
     *   В пакете:              "PKG_NAME:PROCEDURE:HELLO"
     *   Вложенная:             "PKG_NAME:PROCEDURE:OUTER:FUNCTION:INNER"
     *   Без пакета:            "PROCEDURE:STANDALONE"
     */
    public void onRoutineEnter(String name, String routineType, String schemaGeoid,
                               String packageGeoid, int lineStart) {
        if (packageGeoid != null && !packageGeoid.isBlank()) {
            builder.ensurePackage(packageGeoid, schemaGeoid);
        }
        // currentRoutine() = geoid parent routine (для вложенности)
        String parentRoutine = scopeManager.currentRoutine();
        String geoid = builder.addRoutine(name, routineType, schemaGeoid, packageGeoid,
                lineStart, parentRoutine);

        // Push routine scope so nested statements inherit routineGeoid
        ScopeContext routineScope = ScopeContext.forRoutine(geoid);
        scopeManager.push(routineScope);

        logger.debug("Routine ENTER: {} {} [{}] parentRoutine={}",
                routineType, name, geoid, parentRoutine);
    }

    public void onRoutineExit() {
        // Pop scopes until we find and remove the routine scope.
        // Routine scopes have statementType == null (created by ScopeContext.forRoutine).
        // This handles cases where statement scopes inside the routine
        // weren't properly closed (e.g., push/pop mismatch).
        ScopeContext popped = null;
        while (!scopeManager.isEmpty()) {
            popped = scopeManager.pop();
            if (popped.getStatementType() == null) {
                break;
            }
            logger.warn("Routine EXIT: auto-closing orphan statement scope [{}]",
                    popped.getStatementGeoid());
            listener.onSemanticWarning(file, "SCOPE_ORPHAN",
                    "Auto-closing orphan statement scope: " + popped.getStatementGeoid());
        }
        // Clear cursor state — cursors and record vars are routine-scoped
        scopeManager.clearCursorState();
        logger.debug("Routine EXIT: {}",
                popped != null ? popped.getRoutineGeoid() : "?");
    }

    public void onRoutineParameter(String name, String type, String mode) {
        String routine = scopeManager.currentRoutine();
        if (routine != null) {
            RoutineInfo ri = builder.getRoutines().get(routine);
            if (ri != null) ri.addTypedParameter(name, type, mode);
        }
    }

    public void onRoutineVariable(String name, String type) {
        String routine = scopeManager.currentRoutine();
        if (routine != null) {
            RoutineInfo ri = builder.getRoutines().get(routine);
            if (ri != null) ri.addTypedVariable(name, type);
        }
    }

    /**
     * KI-ROWTYPE-1: Handles a variable declared with %ROWTYPE.
     * Creates a DaliRecord for the variable and schedules post-walk field population
     * from the referenced table's columns.
     *
     * @param varName  variable name (e.g. "L_ROW")
     * @param tableRef raw table reference before %ROWTYPE (e.g. "ORDERS" or "HR.ORDERS")
     */
    public void onRowtypeVariable(String varName, String tableRef) {
        String routine = scopeManager.currentRoutine();
        if (routine == null || varName == null || varName.isBlank()) return;

        // Register as a variable for scope resolution (with %ROWTYPE suffix retained for type info)
        RoutineInfo ri = builder.getRoutines().get(routine);
        if (ri != null) ri.addTypedVariable(varName, tableRef + "%ROWTYPE");

        // Create a DaliRecord so RETURNING INTO / FETCH BULK COLLECT can reference it
        RecordInfo rec = builder.ensureRecord(varName.toUpperCase(), routine);
        if (rec == null) return;

        // Resolve the table ref to a geoid (while scope is active)
        String cleanRef = tableRef.trim().toUpperCase();
        String tableGeoid;
        if (cleanRef.contains(".")) {
            tableGeoid = builder.ensureTable(cleanRef, null); // embedded schema
        } else {
            tableGeoid = builder.ensureTable(cleanRef, scopeManager.currentSchema());
        }

        pendingRowtypes.add(new PendingRowtype(rec.getGeoid(), tableGeoid));
        logger.debug("KI-ROWTYPE-1: registered {} → table {}", rec.getGeoid(), tableGeoid);
    }

    /**
     * HND-04: Handles a variable declared with a PL/SQL user-defined COLLECTION type
     * (e.g. {@code l_tab t_journal_stg_tab}).
     *
     * <p>Resolves the collection's element RECORD type, creates a DaliRecord for the
     * variable, and propagates the RECORD's field definitions into it.  The
     * {@link com.hound.semantic.model.RecordInfo#setPlTypeGeoid} back-reference is set
     * so that RemoteWriter can emit the INSTANTIATES_TYPE edge.
     *
     * @param varName  variable name (e.g. "L_TAB")
     * @param typeName raw type name as it appears in source (e.g. "t_journal_stg_tab")
     */
    public void onPlTypeVariable(String varName, String typeName, int line) {
        String routine = scopeManager.currentRoutine();
        if (routine == null || varName == null || varName.isBlank()) return;

        // Also register as ordinary variable for scope resolution
        RoutineInfo ri = builder.getRoutines().get(routine);
        if (ri != null) ri.addTypedVariable(varName, typeName);

        // Resolve the PL type (try routine scope first, then schema-qualified / bare-name fallback)
        com.hound.semantic.model.PlTypeInfo collType =
                builder.resolvePlTypeByName(typeName, routine);
        if (collType == null) return;

        if (collType.isCollection() || collType.isVarray()) {
            // COLLECTION / VARRAY: materialise as DaliTable (virtual) — not DaliRecord.
            // A COLLECTION variable represents a set of rows; individual-row access
            // (loop variable) creates a separate DaliRecord at the FOR-loop site.
            String elemName = collType.getElementTypeName();
            if (elemName == null) return;
            com.hound.semantic.model.PlTypeInfo recType =
                    builder.resolvePlTypeByName(elemName, collType.getScopeGeoid());
            if (recType == null || !recType.hasFields()) return;

            // Link element type geoid on collection (deferred — may have been null at registration)
            if (collType.getElementTypeGeoid() == null) {
                collType.setElementTypeGeoid(recType.getGeoid());
            }

            TableInfo vtable =
                    builder.ensureVirtualTable(varName.toUpperCase(), routine, line);
            if (vtable == null) return;
            vtable.setPlTypeGeoid(collType.getGeoid());
            builder.injectColumnsFromPlType(vtable.geoid(), recType);
            logger.debug("HND-04: materialised COLLECTION {} as VirtualTable {} → {} cols from {}",
                    varName, vtable.geoid(), recType.getFields().size(), recType.getName());

        } else if (collType.isObject()) {
            // HND-08: OBJECT variable — materialise DaliRecord directly from OBJECT fields
            RecordInfo rec = builder.ensureRecord(varName.toUpperCase(), routine, line);
            if (rec == null) return;
            rec.setPlTypeGeoid(collType.getGeoid());
            int pos = 1;
            for (com.hound.semantic.model.PlTypeFieldInfo f : collType.getFields()) {
                rec.addField(f.name(), f.dataType(), pos++, null);
            }
            logger.debug("HND-08: materialised OBJECT {} → {} fields from {}",
                    varName, collType.getFields().size(), collType.getName());

        }
        // VARRAY scalar / REF_CURSOR: just leave as typed variable (already registered above)
    }

    /** Backward-compatible overload — delegates to onPlTypeVariable(varName, typeName, 0). */
    public void onPlTypeVariable(String varName, String typeName) {
        onPlTypeVariable(varName, typeName, 0);
    }

    public void onRoutineReturnType(String returnType) {
        String routine = scopeManager.currentRoutine();
        if (routine != null) {
            RoutineInfo ri = builder.getRoutines().get(routine);
            if (ri != null) ri.setReturnType(returnType);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Cursors
    // ═══════════════════════════════════════════════════════════════

    /**
     * Called on exitCursor_declaration (before onStatementExit).
     * Registers cursor_name → current statement geoid so FOR loops can look it up.
     */
    public void onCursorDeclared(String cursorName) {
        String stmtGeoid = scopeManager.currentStatement();
        if (cursorName != null && stmtGeoid != null) {
            scopeManager.registerCursor(cursorName, stmtGeoid);
            logger.debug("Cursor declared: {} → {}", cursorName, stmtGeoid);
        }
    }

    /**
     * Called on exitCursor_loop_param for a NAMED cursor (FOR rec IN cursor_name LOOP).
     * rec becomes an alias for the cursor's statement scope.
     */
    public void onCursorRecordNamed(String recordVar, String cursorName) {
        scopeManager.registerCursorRecord(recordVar, cursorName, false);
        logger.debug("Cursor record (named): {} → cursor {}", recordVar, cursorName);
    }

    /**
     * Called on exitCursor_loop_param for an INLINE cursor (FOR rec IN (SELECT ...) LOOP).
     * The DINAMIC_CURSOR statement geoid is passed directly.
     */
    public void onCursorRecordInline(String recordVar, String dinamicCursorStmtGeoid) {
        scopeManager.registerCursorRecord(recordVar, dinamicCursorStmtGeoid, true);
        logger.debug("Cursor record (inline): {} → stmt {}", recordVar, dinamicCursorStmtGeoid);
    }

    // ═══════════════════════════════════════════════════════════════
    // CTE
    // ═══════════════════════════════════════════════════════════════

    public void onCTEStart(String cteName, String cteText, int line, int endLine) {
        onStatementEnter("CTE", cteText, line, endLine, cteName);
    }

    public void onCTEExit() {
        onStatementExit();
    }

    // ═══════════════════════════════════════════════════════════════
    // Subquery
    // ═══════════════════════════════════════════════════════════════

    public void onSubqueryStart(String subqueryAlias, String subqueryText, int line, int endLine) {
        onStatementEnter("SUBQUERY", subqueryText, line, endLine, subqueryAlias);
    }

    public void onSubqueryExit() {
        onStatementExit();
    }

    // ═══════════════════════════════════════════════════════════════
    // Clauses (FROM, WHERE, HAVING, GROUP BY, ORDER BY)
    // ═══════════════════════════════════════════════════════════════

    public void onFromStart()  { /* marker for scope */ }

    /**
     * STAB-10: exitFrom — сбрасывает target_table для UPDATE/DELETE
     * после того как FROM обработан, чтобы он не загрязнял следующий statement.
     * Аналог Python exitFrom_clause DML cleanup.
     */
    public void onFromExit() {
        ScopeContext ctx = scopeManager.peek();
        if (ctx == null) return;
        String stmtType = ctx.getStatementType();
        if ("UPDATE".equals(stmtType) || "DELETE".equals(stmtType)) {
            ctx.setTargetTable(null);
            logger.debug("FROM exit: cleared target_table for {}", stmtType);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Atom
    // ═══════════════════════════════════════════════════════════════

    public void onAtom(String text, int line, int col, int endLine, int endCol, String context,
                       boolean isComplex, List<String> tokens,
                       List<Map<String, String>> tokenDetails, int nestedAtomCount) {
        String currentStmt = scopeManager.currentStatement();
        String parentContext = scopeManager.getActiveClause();
        atomProcessor.registerAtom(text, line, col, endLine, endCol, context, currentStmt, parentContext,
                isComplex, tokens, tokenDetails, nestedAtomCount);
    }

    // ═══════════════════════════════════════════════════════════════
    // Select list items
    // ═══════════════════════════════════════════════════════════════

    public void onSelectListItem(String itemText, int line, int endLine) {
        if (itemText == null || itemText.isBlank()) return;
        String currentStmt = scopeManager.currentStatement();
        atomProcessor.registerAtom(itemText, line, 0, endLine, 0,
                "SELECT_LIST", currentStmt, "SELECT");
    }

    // ═══════════════════════════════════════════════════════════════
    // DML target
    // ═══════════════════════════════════════════════════════════════

    public boolean isInDmlTarget() {
        return scopeManager.isInDmlTarget();
    }

    public String getCurrentParentContext() {
        return scopeManager.getActiveClause();
    }

    /** Propagates VALUES clause enter/exit to ScopeContext so atoms get parent_context="VALUES". */
    public void setValuesClause(boolean val) {
        scopeManager.setInValuesClause(val);
    }

    /** Propagates UPDATE SET expression context to ScopeContext so atoms get parent_context="SET_EXPR". */
    public void setUpdateSetExpr(boolean val) {
        scopeManager.setInUpdateSetExpr(val);
    }

    // ═══════════════════════════════════════════════════════════════
    // H1.4 — Statement semantic flags
    // ═══════════════════════════════════════════════════════════════

    /** Called when GROUP BY clause is entered — marks current statement as having aggregation. */
    public void flagCurrentStatementAggregation() {
        String stmt = scopeManager.currentStatement();
        if (stmt == null) return;
        StatementInfo si = builder.getStatements().get(stmt);
        if (si != null) si.setHasAggregation(true);
    }

    /** Called when analytic OVER() clause is entered — marks current statement as having window functions. */
    public void flagCurrentStatementWindow() {
        String stmt = scopeManager.currentStatement();
        if (stmt == null) return;
        StatementInfo si = builder.getStatements().get(stmt);
        if (si != null) si.setHasWindow(true);
    }

    // ═══════════════════════════════════════════════════════════════
    // Result
    // ═══════════════════════════════════════════════════════════════

    public SemanticResult getResult() {
        return new SemanticResult(
                "session-" + System.currentTimeMillis(),
                "unknown-file.sql",
                "plsql",
                0,
                builder.getStructure(),
                builder.getLineageEdges(),
                atomProcessor.getAtoms(),
                atomProcessor.getResolutionLog(),
                calledRoutines,
                builder.getSchemaRegistrationLog()
        );
    }

    public SemanticResult getResult(String sessionId, String filePath, String dialect, long processingTimeMs) {
        return new SemanticResult(sessionId, filePath, dialect, processingTimeMs,
                builder.getStructure(), builder.getLineageEdges(), atomProcessor.getAtoms(),
                atomProcessor.getResolutionLog(), calledRoutines,
                builder.getSchemaRegistrationLog());
    }

    // ═══════════════════════════════════════════════════════════════
    // Post-walk resolution
    // ═══════════════════════════════════════════════════════════════

    /** Вызывается из onStatementExit для текущего statement */
    private void resolveStarSubqueryColumnsForStmt(String stmtGeoid) {
        StatementInfo si = builder.getStatements().get(stmtGeoid);
        if (si != null) resolveStarSubqueryColumns(stmtGeoid, si);
    }

    /**
     * STAB-5: postProcessJoins теперь СОХРАНЯЕТ resolved geoids в JoinInfo
     * через JoinInfo.withResolved(). Старая версия только логировала.
     */
    private void postProcessJoins(String stmtGeoid) {
        StatementInfo si = builder.getStatements().get(stmtGeoid);
        if (si == null) return;

        var updatedJoins = new ArrayList<JoinInfo>();
        boolean changed = false;

        for (JoinInfo j : si.getJoins()) {
            String srcGeoid = j.resolvedSourceGeoid();
            String tgtGeoid = j.resolvedTargetGeoid();

            // Пробуем resolve source если ещё не resolved
            if (srcGeoid == null && j.sourceTableAlias() != null) {
                ResolvedRef ref = nameResolver.resolve(j.sourceTableAlias(), "any", stmtGeoid);
                if (ref.isResolved()) {
                    srcGeoid = ref.getGeoid();
                    logger.debug("JOIN source post-resolved: '{}' → '{}' [{}]",
                            j.sourceTableAlias(), srcGeoid, ref.getStrategy());
                    changed = true;
                } else {
                    logger.warn("JOIN source UNRESOLVED: alias='{}' stmt='{}'",
                            j.sourceTableAlias(), stmtGeoid);
                    listener.onSemanticWarning(file, "JOIN_UNRESOLVED",
                            "JOIN source unresolved: alias=" + j.sourceTableAlias()
                                    + " stmt=" + stmtGeoid);
                }
            }

            // Пробуем resolve target если ещё не resolved
            if (tgtGeoid == null && j.targetTableAlias() != null) {
                ResolvedRef ref = nameResolver.resolve(j.targetTableAlias(), "any", stmtGeoid);
                if (ref.isResolved()) {
                    tgtGeoid = ref.getGeoid();
                    logger.debug("JOIN target post-resolved: '{}' → '{}' [{}]",
                            j.targetTableAlias(), tgtGeoid, ref.getStrategy());
                    changed = true;
                }
            }

            updatedJoins.add((srcGeoid != null || tgtGeoid != null)
                    ? j.withResolved(srcGeoid, tgtGeoid)
                    : j);
        }

        if (changed) si.setJoins(updatedJoins);
    }

    private static boolean isCursorStatementType(String type) {
        return "CURSOR".equals(type) || "REF CURSOR".equals(type) || "DINAMIC_CURSOR".equals(type);
    }

    private void mergeUnionColumns(String stmtGeoid) {
        StatementInfo si = builder.getStatements().get(stmtGeoid);
        if (si == null) return;
        for (String childGeoid : si.getChildStatements()) {
            StatementInfo child = builder.getStatements().get(childGeoid);
            if (child != null && child.getColumnsOutput() != null) {
                for (var ce : child.getColumnsOutput().entrySet()) {
                    si.getColumnsOutput().putIfAbsent(ce.getKey(), ce.getValue());
                }
            }
        }
    }

    /**
     * Порт Python: resolve_pending_columns()
     * Вызывается ПОСЛЕ полного walk AST.
     * Разрешает columns, которые не могли быть resolved inline.
     */
    // ═══════════════════════════════════════════════════════════════
    // CALLS tracking (STAB-9)
    // ═══════════════════════════════════════════════════════════════

    /** callerGeoid → список {name, line, type} вызовов */
    private final Map<String, List<Map<String, String>>> calledRoutines = new LinkedHashMap<>();

    // STAB-8: geoid view-а, для которого сейчас обрабатывается внутренний SELECT
    private String currentViewTargetGeoid = null;

    /**
     * Регистрирует вызов процедуры/функции из тела routine.
     * Вызывается из BaseSemanticListener.onCallStatement().
     * Дедупликация по name+line.
     */
    public void onCallStatement(String callerGeoid, String calledName, int line) {
        if (callerGeoid == null || calledName == null || calledName.isBlank()) return;
        var calls = calledRoutines.computeIfAbsent(callerGeoid, k -> new ArrayList<>());
        boolean exists = calls.stream().anyMatch(c ->
                calledName.equalsIgnoreCase(c.get("name"))
                && String.valueOf(line).equals(c.get("line")));
        if (!exists) {
            calls.add(Map.of("name", calledName, "line", String.valueOf(line),
                             "type", determineCallType(calledName)));
            logger.debug("CALL registered: {} → {} [line {}]", callerGeoid, calledName, line);
        }
    }

    private String determineCallType(String name) {
        for (var entry : builder.getRoutines().entrySet()) {
            String geoid = entry.getKey().toUpperCase();
            String rType = entry.getValue().getRoutineType();
            if (geoid.endsWith(":" + name.toUpperCase()) || geoid.equals(name.toUpperCase()))
                return rType != null ? rType : "UNKNOWN";
        }
        return "UNKNOWN";
    }

    /** Возвращает собранные вызовы для последующей записи CALLS рёбер */
    public Map<String, List<Map<String, String>>> getCalledRoutines() {
        return calledRoutines;
    }

    // ═══════════════════════════════════════════════════════════════
    // G3: MERGE element target column tracking
    // ═══════════════════════════════════════════════════════════════

    /**
     * Called at enterMerge_element (MERGE UPDATE SET col = expr).
     * Stores the target column so subsequent atoms in the expression can be correlated.
     * Also immediately creates an affected_column "MERGE_UPDATE_TARGET" entry for that column.
     */
    public void onMergeElementEnter(String targetColumnName) {
        atomProcessor.setMergeTargetColumn(targetColumnName);
        // Eagerly register the target column in the current statement's affected_columns.
        // source_type="MERGE" — unified type for both UPDATE and INSERT paths; dedup is in StatementInfo.
        String stmtGeoid = scopeManager.currentStatement();
        if (stmtGeoid != null && targetColumnName != null) {
            String targetTableGeoid = resolveTargetTableGeoid(stmtGeoid);
            atomProcessor.addMergeTargetColumn(stmtGeoid, targetColumnName,
                    targetTableGeoid, "MERGE");
        }
    }

    /** Called at exitMerge_element — clears target column context. */
    public void onMergeElementExit() {
        atomProcessor.clearMergeTargetColumn();
    }

    /**
     * Called from enterMerge_insert_clause when column list is present.
     * Registers each column as a MERGE affected column (dedup in StatementInfo) and records
     * the ordered column ref list for VALUES positional binding.
     *
     * @param columnNames ordered list of target column names
     */
    public void onMergeInsertColumns(List<String> columnNames) {
        String stmtGeoid = scopeManager.currentStatement();
        if (stmtGeoid == null || columnNames == null) return;
        String targetTableGeoid = resolveTargetTableGeoid(stmtGeoid);
        var si = builder.getStatements().get(stmtGeoid);
        for (String col : columnNames) {
            if (col != null && !col.isBlank()) {
                atomProcessor.addMergeTargetColumn(stmtGeoid, col, targetTableGeoid, "MERGE");
                // Record ordered column ref for VALUES positional binding in AtomProcessor
                if (si != null) {
                    String colRef = targetTableGeoid != null
                            ? targetTableGeoid + "." + col.toUpperCase()
                            : col.toUpperCase();
                    si.addMergeInsertColumnRef(colRef);
                }
            }
        }
    }

    /** Returns the first target table geoid for a statement, or null. */
    private String resolveTargetTableGeoid(String stmtGeoid) {
        var si = builder.getStatements().get(stmtGeoid);
        if (si == null) return null;
        return si.getTargetTableGeoids().stream().findFirst().orElse(null);
    }

    /** S1.0: shortcut for diagnostic tests — delegates to AtomProcessor */
    public List<Map<String, Object>> getResolutionLog() {
        return atomProcessor.getResolutionLog();
    }

    // ═══════════════════════════════════════════════════════════════
    // Pending columns
    // ═══════════════════════════════════════════════════════════════

    private final List<Map<String, String>> pendingColumns = new ArrayList<>();

    // KI-ROWTYPE-1: pending %ROWTYPE record → table column population
    private record PendingRowtype(String recGeoid, String tableGeoid) {}
    private final List<PendingRowtype> pendingRowtypes = new ArrayList<>();

    public void addPendingColumn(String columnRef, String statementGeoid) {
        pendingColumns.add(Map.of("ref", columnRef, "stmt", statementGeoid));
    }

    public int getPendingColumnsCount() { return pendingColumns.size(); }

    /**
     * STAB-4: уровень 1 — пробуем resolve pending columns принадлежащие stmtGeoid
     * ДО pop(). Scope ещё открыт → aliases, CTE, source_tables этого statement видны.
     * Аналог Python: частичный _process_atoms_on_exit() при exitStatement.
     */
    private void resolvePartialPendingForStatement(String stmtGeoid) {
        if (pendingColumns.isEmpty()) return;

        var stillPending = new ArrayList<Map<String, String>>();
        int resolvedNow = 0;

        for (var p : pendingColumns) {
            // Только pending из этого statement
            if (!stmtGeoid.equals(p.get("stmt"))) {
                stillPending.add(p);
                continue;
            }

            String ref   = p.get("ref");
            String[] parts = ref.split("\\.");
            String cPart = parts[parts.length - 1];
            String tPart = parts.length > 1 ? String.join(".", Arrays.copyOf(parts, parts.length - 1)) : null;

            String tGeoid = null;
            if (tPart != null) {
                var r = nameResolver.resolve(tPart, "any", stmtGeoid);
                if (r.isResolved()) tGeoid = r.getGeoid();
            } else {
                var r = nameResolver.resolveImplicitTable(stmtGeoid);
                if (r.isResolved()) tGeoid = r.getGeoid();
            }

            if (tGeoid != null) {
                if (!builder.getStatements().containsKey(tGeoid)) {
                    builder.addInferredColumn(tGeoid, cPart, "L1");
                }
                resolvedNow++;
            } else {
                stillPending.add(p);
            }
        }

        if (resolvedNow > 0) {
            pendingColumns.clear();
            pendingColumns.addAll(stillPending);
            logger.debug("Pending on exitStatement [{}]: resolved={}, remaining={}",
                    stmtGeoid, resolvedNow, pendingColumns.size());
        }
    }

    public void resolvePendingColumns() {
        // 1. Star subquery columns
        for (var entry : builder.getStatements().entrySet()) {
            resolveStarSubqueryColumns(entry.getKey(), entry.getValue());
        }
        // 2. Pending column refs (STAB-4: уровень 2 — post-walk)
        int pendingBefore = pendingColumns.size();
        logger.info("Pending columns post-walk: {} to resolve", pendingBefore);
        var resolved = new ArrayList<Integer>();
        for (int i = 0; i < pendingColumns.size(); i++) {
            var p = pendingColumns.get(i);
            String[] parts = p.get("ref").split("\\.");
            String cPart = parts[parts.length - 1];
            String tPart = parts.length > 1 ? String.join(".", Arrays.copyOf(parts, parts.length - 1)) : null;
            String tGeoid = null;
            if (tPart != null) {
                var ref = nameResolver.resolve(tPart, "any", p.get("stmt"));
                if (ref.isResolved()) tGeoid = ref.getGeoid();
            } else {
                var ref = nameResolver.resolveImplicitTable(p.get("stmt"));
                if (ref.isResolved()) tGeoid = ref.getGeoid();
            }
            if (tGeoid != null) {
                if (!builder.getStatements().containsKey(tGeoid)) {
                    builder.addInferredColumn(tGeoid, cPart, "L2");
                }
                resolved.add(i);
            }
        }
        for (int i = resolved.size() - 1; i >= 0; i--) pendingColumns.remove((int) resolved.get(i));
        logger.info("Pending columns: {} resolved, {} remaining",
                pendingBefore - pendingColumns.size(), pendingColumns.size());

        // Pass 3 (KI-PENDING-1): parent chain depth=1 — correlated subquery outer-table columns
        if (!pendingColumns.isEmpty()) {
            var stillAfterP3 = new ArrayList<Map<String, String>>();
            int p3Resolved = 0;
            for (var p : pendingColumns) {
                String ref = p.get("ref");
                String stmtGeoid = p.get("stmt");
                String[] parts = ref.split("\\.");
                // Only bare column names — qualified refs already failed exact resolution
                if (parts.length > 1) { stillAfterP3.add(p); continue; }
                String tGeoid = resolveViaParentChain(ref, stmtGeoid);
                if (tGeoid != null) {
                    builder.addInferredColumn(tGeoid, ref, "L3");
                    p3Resolved++;
                } else {
                    stillAfterP3.add(p);
                }
            }
            pendingColumns.clear();
            pendingColumns.addAll(stillAfterP3);
            logger.info("Pass 3 (parent chain): {} resolved, {} remaining",
                    p3Resolved, pendingColumns.size());
        }

        // Pass 4 (KI-PENDING-1): single-table fuzzy (quality=LOW) — bare column, one source table
        if (!pendingColumns.isEmpty()) {
            var stillAfterP4 = new ArrayList<Map<String, String>>();
            int p4Resolved = 0;
            for (var p : pendingColumns) {
                String ref = p.get("ref");
                String stmtGeoid = p.get("stmt");
                String[] parts = ref.split("\\.");
                if (parts.length > 1) { stillAfterP4.add(p); continue; }
                var si = builder.getStatements().get(stmtGeoid);
                if (si == null) { stillAfterP4.add(p); continue; }
                List<String> sources = si.getSourceTableGeoids().stream()
                        .filter(g -> !builder.getStatements().containsKey(g))
                        .toList();
                if (sources.size() != 1) { stillAfterP4.add(p); continue; }
                builder.addInferredColumn(sources.get(0), ref, "L4");
                p4Resolved++;
            }
            pendingColumns.clear();
            pendingColumns.addAll(stillAfterP4);
            logger.info("Pass 4 (single-table fuzzy): {} resolved, {} remaining",
                    p4Resolved, pendingColumns.size());
        }

        // Pass 5Q: resolve deferred alias.column atoms → table + inferred column
        atomProcessor.resolvePendingQualifiedAtoms();

        // Pass 6 (KI-ROWTYPE-1): populate %ROWTYPE record fields from resolved table columns
        if (!pendingRowtypes.isEmpty()) {  // previously Pass 3, renumbered after KI-PENDING-1 passes
            int rowtypesFilled = 0;
            for (PendingRowtype pr : pendingRowtypes) {
                RecordInfo rec = builder.getRecords().get(pr.recGeoid());
                if (rec == null) continue;
                long count = builder.getColumns().values().stream()
                        .filter(c -> pr.tableGeoid().equals(c.getTableGeoid()))
                        .sorted(Comparator.comparingInt(ColumnInfo::getOrdinalPosition))
                        .peek(col -> rec.addField(
                                col.getColumnName(), col.getDataType(),
                                col.getOrdinalPosition(), col.getGeoid()))
                        .count();
                if (count > 0) rowtypesFilled++;
                else logger.debug("KI-ROWTYPE-1: no columns found for table {} (record {})",
                        pr.tableGeoid(), pr.recGeoid());
            }
            logger.info("KI-ROWTYPE-1: {} of {} %ROWTYPE records populated with column fields",
                    rowtypesFilled, pendingRowtypes.size());
            pendingRowtypes.clear();
        }

        // HAL-05: classify unattached atoms (CONSTANT_ORPHAN detection)
        atomProcessor.classifyUnattachedAtoms();
    }

    /**
     * KI-PENDING-1 Pass 3: Tries to resolve a bare column name via the parent statement's
     * source tables (depth=1). Covers correlated subqueries where an outer-FROM table
     * already has the column registered in the builder.
     *
     * @return table geoid if found, null otherwise
     */
    private String resolveViaParentChain(String columnName, String stmtGeoid) {
        var si = builder.getStatements().get(stmtGeoid);
        if (si == null || si.getParentStatementGeoid() == null) return null;
        var parentSi = builder.getStatements().get(si.getParentStatementGeoid());
        if (parentSi == null) return null;
        // Search parent's source tables (not subqueries) for the column
        for (String srcGeoid : parentSi.getSourceTableGeoids()) {
            if (builder.getStatements().containsKey(srcGeoid)) continue; // subquery, skip
            String colGeoid = srcGeoid + "." + columnName.toUpperCase();
            if (builder.getColumns().containsKey(colGeoid)) return srcGeoid;
        }
        return null;
    }

    /**
     * Порт Python: resolve_star_subquery_columns()
     * Восстановление columns из подзапросов при SELECT *.
     */
    private void resolveStarSubqueryColumns(String stmtGeoid, StatementInfo stmtInfo) {
        var outputCols = stmtInfo.getColumnsOutput();
        if (outputCols == null || outputCols.isEmpty()) return;

        for (var colEntry : new ArrayList<>(outputCols.entrySet())) {
            Map<String, Object> colInfo = colEntry.getValue();
            String colName = (String) colInfo.get("name");
            if (!"*".equals(colName)) continue;

            // SELECT * — берём columns из source tables/subqueries
            for (String sourceGeoid : stmtInfo.getSourceTableGeoids()) {
                var sourceStmt = builder.getStatements().get(sourceGeoid);
                if (sourceStmt != null && sourceStmt.getColumnsOutput() != null) {
                    for (var srcCol : sourceStmt.getColumnsOutput().entrySet()) {
                        String newColName = srcCol.getKey();
                        if (!outputCols.containsKey(newColName)) {
                            Map<String, Object> newCol = new LinkedHashMap<>(srcCol.getValue());
                            newCol.put("source_type", "subquery_star");
                            newCol.put("source_subquery", sourceGeoid);
                            outputCols.put(newColName, newCol);
                        }
                    }
                }
            }
        }
    }

    public void clear() {
        scopeManager.clear();
        nameResolver.clearCache();
        builder.clear();
        atomProcessor.clear();
        joinProcessor.clear();
    }
}