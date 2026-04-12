package com.hound.semantic.engine;

import com.hound.semantic.model.AtomInfo;
import com.hound.semantic.model.RoutineInfo;
import com.hound.semantic.model.StatementInfo;
import com.hound.semantic.model.TableInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.*;

/**
 * AtomProcessor — сбор, классификация и разрешение атомарных выражений.
 * Портирование Python: enterAtom, _analyze_atom_structure, _resolve_atom_reference,
 * _process_atoms_on_exit, get_atoms_data.
 */
public class AtomProcessor {

    private static final Logger logger = LoggerFactory.getLogger(AtomProcessor.class);

    // ═══════ State ═══════

    // statement_geoid → { atom_key → atom_data }
    private final Map<String, Map<String, Map<String, Object>>> atomsByStatement = new LinkedHashMap<>();

    // Unattached atoms (outside any statement)
    private final Map<String, Map<String, Object>> unattachedAtoms = new LinkedHashMap<>();

    // S1.PRE: resolution log — one entry per resolved/unresolved column-reference atom
    private final List<Map<String, Object>> resolutionLog = new ArrayList<>();

    // G3: current MERGE UPDATE target column (set at enterMerge_element, cleared at exit)
    private String currentMergeTargetColumn = null;

    // External dependencies
    private NameResolver nameResolver;
    private StructureAndLineageBuilder builder;
    private ScopeManager scopeManager;

    public void wire(NameResolver nameResolver, StructureAndLineageBuilder builder, ScopeManager scopeManager) {
        this.nameResolver = nameResolver;
        this.builder = builder;
        this.scopeManager = scopeManager;
    }

    // =========================================================================
    // ATOM REGISTRATION
    // =========================================================================

    /**
     * Регистрирует атом с полными token details.
     * Порт Python: enterAtom → сохранение в self.statements[stmt]['atoms']
     */
    public void registerAtom(String text, int line, int col, int endLine, int endCol,
                             String context, String statementGeoid, String parentContext,
                             boolean isComplex, List<String> tokens,
                             List<Map<String, String>> tokenDetails, int nestedAtomCount) {
        if (text == null || text.isBlank()) return;

        if (statementGeoid == null) {
            registerUnattachedAtom(text, buildAtomData(text, line, col, endLine, endCol,
                    context, null, parentContext, isComplex, tokens, tokenDetails, nestedAtomCount));
            return;
        }

        Map<String, Map<String, Object>> stmtAtoms = atomsByStatement
                .computeIfAbsent(statementGeoid, k -> new LinkedHashMap<>());

        String atomKey = text.toUpperCase() + "~" + line + ":" + col;
        if (stmtAtoms.containsKey(atomKey)) return;

        Map<String, Object> atomData = buildAtomData(text, line, col, endLine, endCol,
                context, statementGeoid, parentContext, isComplex, tokens, tokenDetails, nestedAtomCount);

        // Если есть вложенные атомы — сразу помечаем как обработанный
        if (nestedAtomCount > 0) {
            atomData.put("status", AtomInfo.STATUS_RESOLVED);
            atomData.put("is_complex_atom", true);
        }

        // Классификация по token details
        classifyAtom(atomData);

        stmtAtoms.put(atomKey, atomData);
        logger.debug("ATOM REGISTER '{}' → stmt={} ctx={} line={}",
                text, statementGeoid, parentContext, line);
    }

    /**
     * Backward-compatible registerAtom без token details.
     */
    public void registerAtom(String text, int line, int col, int endLine, int endCol,
                             String context, String statementGeoid, String parentContext) {
        boolean isComplex = text != null && (text.contains(".") || text.contains("("));
        registerAtom(text, line, col, endLine, endCol, context, statementGeoid, parentContext,
                isComplex, List.of(), List.of(), 0);
    }

    private Map<String, Object> buildAtomData(String text, int line, int col, int endLine, int endCol,
                                               String context, String statementGeoid, String parentContext,
                                               boolean isComplex, List<String> tokens,
                                               List<Map<String, String>> tokenDetails, int nestedAtomCount) {
        Map<String, Object> atomData = new LinkedHashMap<>();
        atomData.put("atom_text", text);
        atomData.put("position", line + ":" + col);
        atomData.put("sposition", endLine + ":" + endCol);
        atomData.put("atom_context", context);
        atomData.put("parent_context", parentContext);
        atomData.put("is_column_reference", false);
        atomData.put("is_function_call", false);
        atomData.put("is_constant", false);
        atomData.put("is_routine_param", false);
        atomData.put("is_routine_var", false);
        atomData.put("table_name", null);
        atomData.put("column_name", null);
        atomData.put("table_geoid", null);
        atomData.put("statement_geoid", statementGeoid);
        atomData.put("status", null);
        atomData.put("resolution", null);
        atomData.put("is_complex", isComplex);
        atomData.put("nested_atoms_count", nestedAtomCount);
        atomData.put("tokens", tokens != null ? tokens : List.of());
        atomData.put("token_details", tokenDetails != null ? tokenDetails : List.of());
        atomData.put("output_column_sequence", null);
        return atomData;
    }

    public void registerUnattachedAtom(String text, Map<String, Object> atomData) {
        unattachedAtoms.put(text, atomData);
    }

    // =========================================================================
    // ATOM CLASSIFICATION — Port from Python _analyze_atom_structure
    // =========================================================================

    /**
     * Порт Python: PlSqlAnalyzerListener._analyze_atom_structure()
     * Классифицирует атом по token types.
     */
    @SuppressWarnings("unchecked")
    private void classifyAtom(Map<String, Object> atomData) {
        List<String> tokens = (List<String>) atomData.get("tokens");
        List<Map<String, String>> tokenDetails = (List<Map<String, String>>) atomData.get("token_details");

        if (tokens == null || tokenDetails == null || tokens.isEmpty()) {
            classifyByText(atomData);
            return;
        }

        // --- Bind variables (:name, :1, :X) — check atom text first, regardless of token count ---
        // BINDVAR may be split into COLON + INTEGER by some parsers, so text check is authoritative
        String atomText = (String) atomData.get("atom_text");
        if (atomText != null && atomText.startsWith(":") && atomText.length() > 1) {
            atomData.put("is_constant", true);
            return;
        }

        // --- Простая ссылка на колонку (1 токен, identifier) ---
        if (tokens.size() == 1 && getCanonical(tokenDetails.get(0)).isIdentifier()) {
            atomData.put("is_column_reference", true);
            atomData.put("column_name", tokens.get(0));
            return;
        }

        // --- Квалифицированная ссылка table.column (3 токена: ID.PERIOD.ID) ---
        if (tokens.size() == 3
                && getCanonical(tokenDetails.get(0)).isIdentifier()
                && getCanonical(tokenDetails.get(1)) == CanonicalTokenType.PERIOD
                && getCanonical(tokenDetails.get(2)).isIdentifier()) {
            atomData.put("is_column_reference", true);
            atomData.put("table_name", tokens.get(0));
            atomData.put("column_name", tokens.get(2));
            return;
        }

        // --- Полностью квалифицированная schema.table.column (5 токенов) ---
        if (tokens.size() == 5
                && getCanonical(tokenDetails.get(0)).isIdentifier()
                && getCanonical(tokenDetails.get(1)) == CanonicalTokenType.PERIOD
                && getCanonical(tokenDetails.get(2)).isIdentifier()
                && getCanonical(tokenDetails.get(3)) == CanonicalTokenType.PERIOD
                && getCanonical(tokenDetails.get(4)).isIdentifier()) {
            atomData.put("is_column_reference", true);
            atomData.put("table_name", tokens.get(2));   // table — средний элемент
            atomData.put("column_name", tokens.get(4));
            return;
        }

        // --- INTERVAL literals (INTERVAL '20' DAY, INTERVAL '240' HOUR, etc.) ---
        // INTERVAL keyword maps to UNKNOWN canonical type in PlSqlTokenMapper, check text directly
        if (tokenDetails.size() >= 1) {
            String firstText = tokenDetails.get(0).get("text");
            if (firstText != null && "INTERVAL".equalsIgnoreCase(firstText)) {
                atomData.put("is_constant", true);
                return;
            }
        }

        // --- Вызов функции: ID LEFT_PAREN ... ---
        if (tokens.size() >= 3
                && getCanonical(tokenDetails.get(0)).isIdentifier()
                && getCanonical(tokenDetails.get(1)) == CanonicalTokenType.LEFT_PAREN) {
            atomData.put("is_function_call", true);
            atomData.put("function_name", tokens.get(0));
            return;
        }

        // --- Константы (1 токен) ---
        if (tokens.size() == 1 && getCanonical(tokenDetails.get(0)).isConstant()) {
            atomData.put("is_constant", true);
            return;
        }

        // --- DATE / TIMESTAMP 'string' (ANSI date/timestamp literal) ---
        // DATE maps to IDENTIFIER; TIMESTAMP maps to UNKNOWN — check text explicitly for both
        if (tokenDetails.size() >= 2
                && getCanonical(tokenDetails.get(1)) == CanonicalTokenType.STRING_LITERAL) {
            String firstText = tokenDetails.get(0).get("text");
            if (firstText != null
                    && ("DATE".equalsIgnoreCase(firstText) || "TIMESTAMP".equalsIgnoreCase(firstText))) {
                atomData.put("is_constant", true);
                return;
            }
        }

        // --- Системные псевдоколонки ---
        if (tokens.size() == 1 && getCanonical(tokenDetails.get(0)).isSystemPseudoColumn()) {
            atomData.put("is_constant", true);
            return;
        }
    }

    /**
     * Fallback classification from text when no token_details available.
     */
    private void classifyByText(Map<String, Object> atomData) {
        String text = (String) atomData.get("atom_text");
        if (text == null) return;

        if (text.length() >= 2 && text.startsWith("'") && text.endsWith("'")) {
            atomData.put("is_constant", true);
        } else if (text.matches("-?\\d+(\\.\\d+)?([eE][+-]?\\d+)?")) {
            atomData.put("is_constant", true);
        } else if (text.toUpperCase().startsWith("INTERVAL")) {
            atomData.put("is_constant", true);
        } else if (text.startsWith(":") && text.length() > 1) {
            atomData.put("is_constant", true);
        }
        if (text.contains("(") && text.contains(")")) {
            atomData.put("is_function_call", true);
        }
    }

    /** Parses canonical token type from the "type" field in tokenDetails. */
    private static CanonicalTokenType getCanonical(Map<String, String> td) {
        return CanonicalTokenType.fromString(td.get("type"));
    }

    // =========================================================================
    // ATOM RESOLUTION — Port from Python _resolve_atom_reference
    // =========================================================================

    /**
     * Разрешает атомы при выходе из statement.
     * Аналог Python: _process_atoms_on_exit(statement_name)
     */
    public void resolveAtomsOnStatementExit(String statementGeoid) {
        Map<String, Map<String, Object>> stmtAtoms = atomsByStatement.get(statementGeoid);
        if (stmtAtoms == null || stmtAtoms.isEmpty()) return;
        if (nameResolver == null) return;

        logger.debug("Resolving {} atoms for statement {}", stmtAtoms.size(), statementGeoid);

        int total = 0, resolved = 0, constants = 0, functions = 0, failed = 0;

        for (var entry : stmtAtoms.entrySet()) {
            total++;
            Map<String, Object> atomData = entry.getValue();

            // Пропускаем уже обработанные
            if (atomData.get("status") != null) continue;

            // Пропускаем константы
            if (Boolean.TRUE.equals(atomData.get("is_constant"))) {
                atomData.put("status", "constant");
                constants++;
                appendLog(statementGeoid, atomData, null);
                continue;
            }
            // Пропускаем вызовы функций
            if (Boolean.TRUE.equals(atomData.get("is_function_call"))) {
                atomData.put("status", "function_call");
                functions++;
                appendLog(statementGeoid, atomData, null);
                continue;
            }

            // Попытка resolve
            Map<String, Object> resolution = resolveAtomReference(entry.getKey(), atomData, statementGeoid);

            if (resolution != null && Boolean.TRUE.equals(resolution.get("resolved"))) {
                atomData.put("status", AtomInfo.STATUS_RESOLVED);
                atomData.put("resolution", resolution);
                atomData.put("table_geoid", resolution.get("table_geoid"));
                atomData.put("column_name", resolution.get("column_name"));
                atomData.put("table_name", resolution.get("table_name"));
                atomData.put("is_column_reference", Boolean.TRUE.equals(resolution.get("is_column_reference")));
                atomData.put("is_routine_param", Boolean.TRUE.equals(resolution.get("is_routine_param")));
                atomData.put("is_routine_var", Boolean.TRUE.equals(resolution.get("is_routine_var")));

                // Reconstruct column only for physical tables.
                // SubQuery/CTE atoms resolve to a statement geoid — those are linked
                // via ATOM_REF_OUTPUT_COL, not via DaliColumn records.
                String tableGeoid = (String) resolution.get("table_geoid");
                String columnName = (String) resolution.get("column_name");
                if (tableGeoid != null && columnName != null && builder != null
                        && builder.getTables().containsKey(tableGeoid)) {
                    builder.addColumn(tableGeoid, columnName, (String) atomData.get("atom_text"), null);
                }

                resolved++;
                logger.debug("Atom resolved: {} → table={}, column={}", entry.getKey(), tableGeoid, columnName);

                // Routine variables / parameters cannot produce DATA_FLOW edges (no DaliColumn source).
                // Mark them so the UI can surface the gap rather than silently showing no lineage.
                boolean isVar   = Boolean.TRUE.equals(atomData.get("is_routine_var"));
                boolean isParam = Boolean.TRUE.equals(atomData.get("is_routine_param"));
                if ((isVar || isParam) && atomData.get("dml_target_ref") != null) {
                    atomData.put("warning", AtomInfo.STATUS_UNBOUND);
                }
            } else {
                failed++;
                String parentCtx = (String) atomData.get("parent_context");
                if ("SELECT".equals(parentCtx) || "INSERT".equals(parentCtx)
                        || "UPDATE".equals(parentCtx) || "MERGE".equals(parentCtx)
                        || "SET_EXPR".equals(parentCtx)) {
                    logger.warn("Could not resolve atom: {} in context {}", entry.getKey(), parentCtx);
                }
                atomData.put("status", "unresolved");
                atomData.put("warning", AtomInfo.STATUS_UNRESOLVED);
                if (resolution != null) {
                    atomData.put("resolution", resolution);
                }
            }
            appendLog(statementGeoid, atomData, resolution);
        }

        // B2.AR3 — atom resolution audit
        logger.info("DIAG Atoms [{}]: total={} resolved={} const={} func={} failed={}",
                statementGeoid, total, resolved, constants, functions, failed);

        // G1: build affected_columns on StatementInfo from all resolved atoms
        buildAffectedColumnsFromAtoms(statementGeoid, stmtAtoms);
    }

    /**
     * DaliAffectedColumn = only target table columns (INSERT col list, UPDATE SET cols,
     * MERGE INSERT/UPDATE targets). Those are registered explicitly during the grammar walk
     * via onInsertColumnList / onUpdateSetColumn / addMergeTargetColumn.
     * Atoms (SELECT, WHERE, JOIN, ORDER, HAVING, etc.) do NOT go into affectedColumns.
     */
    private void buildAffectedColumnsFromAtoms(String statementGeoid,
                                                Map<String, Map<String, Object>> stmtAtoms) {
        // Intentionally empty: all affectedColumns entries are registered explicitly.
    }

    // =========================================================================
    // G3: MERGE element target column tracking
    // =========================================================================

    /** Called at enterMerge_element — sets the target column name for subsequent atoms. */
    public void setMergeTargetColumn(String columnName) {
        this.currentMergeTargetColumn = columnName;
    }

    /** Called at exitMerge_element — clears the target column context. */
    public void clearMergeTargetColumn() {
        this.currentMergeTargetColumn = null;
    }

    /**
     * G3: Explicitly registers a MERGE UPDATE/INSERT target column as an affected column.
     * Called at exitMerge_element / enterMerge_insert_clause.
     *
     * @param sourceType "MERGE_UPDATE_TARGET" or "MERGE_INSERT_TARGET"
     */
    public void addMergeTargetColumn(String statementGeoid, String columnName,
                                      String targetTableGeoid, String sourceType) {
        if (statementGeoid == null || columnName == null) return;
        StatementInfo si = builder == null ? null : builder.getStatements().get(statementGeoid);
        if (si == null) return;

        String columnRef = (targetTableGeoid != null)
                ? targetTableGeoid + "." + columnName
                : columnName;
        si.addAffectedColumn(columnRef, columnName, targetTableGeoid, null, sourceType, "target");
    }

    /**
     * Порт Python: PlSqlAnalyzerListener._resolve_atom_reference()
     * ПОЛНАЯ логика разрешения атомов.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> resolveAtomReference(String atomKey, Map<String, Object> atomData,
                                                      String statementGeoid) {
        String atomText = (String) atomData.get("atom_text");
        if (atomText == null) atomText = atomKey;

        List<String> tokens = (List<String>) atomData.get("tokens");
        List<Map<String, String>> tokenDetails = (List<Map<String, String>>) atomData.get("token_details");
        boolean isComplex = Boolean.TRUE.equals(atomData.get("is_complex"));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("atom_text", atomText);
        result.put("is_complex", isComplex);
        result.put("resolved", false);
        result.put("table_geoid", null);
        result.put("statement_geoid", statementGeoid);
        result.put("column_name", null);
        result.put("reference_type", null);
        result.put("reason", null);
        result.put("table_name", null);

        // Если нет token details — fallback к text-based resolution
        if (tokens == null || tokenDetails == null || tokens.isEmpty()) {
            return resolveByText(atomText, statementGeoid, result);
        }

        // ═══ 1. COMPLEX ATOMS (>= 3 tokens) ═══
        if (isComplex && tokens.size() >= 3) {
            // Проверка: последний токен == NEXTVAL → sequence
            if ("NEXTVAL".equalsIgnoreCase(tokens.get(tokens.size() - 1))) {
                result.put("resolved", true);
                result.put("reference_type", "sequence");
                result.put("reason", "Выдача нового сиквенса");
                return result;
            }

            // Подсчёт точек
            long dotCount = tokenDetails.stream()
                    .filter(td -> getCanonical(td) == CanonicalTokenType.PERIOD).count();

            // schema.table.column (>= 2 точки, >= 5 токенов)
            if (dotCount >= 2 && tokens.size() >= 5) {
                String tableRef = tokens.get(0) + "." + tokens.get(2);
                String columnName = tokens.get(4);
                ResolvedRef resolved = nameResolver.resolve(tableRef, "table", statementGeoid);
                if (resolved.isResolved()) {
                    result.put("resolved", true);
                    result.put("table_geoid", resolved.getGeoid());
                    result.put("table_name", tableRef);
                    result.put("column_name", columnName);
                    result.put("reference_type", "tables");
                    result.put("is_column_reference", true);
                    result.put("reason", "schema.table.column resolved: " + tableRef);
                    return result;
                }
            }

            // table.column (1 точка, >= 3 токена)
            if (dotCount == 1 && tokens.size() >= 3) {
                String tableAlias = tokens.get(0);
                String columnName = tokens.get(2);
                ResolvedRef resolved = nameResolver.resolve(tableAlias, "any", statementGeoid);
                if (resolved.isResolved()) {
                    result.put("resolved", true);
                    result.put("table_geoid", resolved.getGeoid());
                    result.put("table_name", tableAlias);
                    result.put("column_name", columnName);
                    result.put("reference_type", resolved.getType().toLowerCase() + "s");
                    result.put("is_column_reference", true);
                    result.put("reason", "table.column resolved via " + resolved.getType());
                    return result;
                }
                // Fallback: parent statement scope — covers SOURCE.*/TARGET.* in MERGE WHEN clauses
                if (builder != null) {
                    StatementInfo si = builder.getStatements().get(statementGeoid);
                    if (si != null && si.getParentStatementGeoid() != null) {
                        ResolvedRef parentResolved = nameResolver.resolve(
                                tableAlias, "any", si.getParentStatementGeoid());
                        if (parentResolved.isResolved()) {
                            result.put("resolved", true);
                            result.put("table_geoid", parentResolved.getGeoid());
                            result.put("table_name", tableAlias);
                            result.put("column_name", columnName);
                            result.put("reference_type", parentResolved.getType().toLowerCase() + "s");
                            result.put("is_column_reference", true);
                            result.put("reason", "table.column resolved via parent scope: " + tableAlias);
                            return result;
                        }
                    }
                }

                // Cursor record alias: rec.FIELD where rec = FOR loop variable
                // rec acts as an alias for the cursor's scope (its SELECT's source tables)
                if (scopeManager != null) {
                    String cursorStmtGeoid = scopeManager.getCursorRecordStmt(tableAlias);
                    if (cursorStmtGeoid != null) {
                        String sourceTableGeoid = findCursorSourceTable(cursorStmtGeoid);
                        if (sourceTableGeoid != null) {
                            result.put("resolved", true);
                            result.put("table_geoid", sourceTableGeoid);
                            result.put("table_name", tableAlias);
                            result.put("column_name", columnName);
                            result.put("reference_type", "cursor_record");
                            result.put("is_column_reference", true);
                            result.put("reason", "cursor record: " + tableAlias + "." + columnName);
                        } else {
                            // Cursor exists but source table unknown (e.g., computed column)
                            result.put("resolved", true);
                            result.put("reference_type", "cursor_record_expr");
                            result.put("column_name", columnName);
                            result.put("reason", "cursor record expr: " + tableAlias + "." + columnName);
                        }
                        return result;
                    }
                }

                result.put("reason", "Таблица/подзапрос не найден для: " + tableAlias);
                return result;
            }

            // DATE 'string' (ANSI date constant) — IDENTIFIER followed by STRING_LITERAL
            if (getCanonical(tokenDetails.get(0)).isIdentifier()
                    && tokenDetails.size() >= 2
                    && getCanonical(tokenDetails.get(1)) == CanonicalTokenType.STRING_LITERAL) {
                String firstText = tokenDetails.get(0).get("text");
                if (firstText != null && "DATE".equalsIgnoreCase(firstText)) {
                    result.put("resolved", true);
                    result.put("reason", "Заглушка для константы ANSI Дата");
                    return result;
                }
            }
        }

        // ═══ 2. SIMPLE ATOMS (1 token, identifier) ═══
        CanonicalTokenType firstCanonical = getCanonical(tokenDetails.get(0));

        if (!isComplex && tokens.size() == 1 && firstCanonical.isIdentifier()) {

            // 2a. Проверка routine variables/parameters
            if (scopeManager != null) {
                String currentRoutine = scopeManager.currentRoutine();
                if (currentRoutine != null && builder != null) {
                    RoutineInfo routineInfo = builder.getRoutines().get(currentRoutine);
                    if (routineInfo != null) {
                        String atomUpper = tokens.get(0).toUpperCase();
                        if (routineInfo.hasVariable(atomUpper)) {
                            result.put("resolved", true);
                            result.put("is_routine_var", true);
                            result.put("reference_type", "routine_variable");
                            result.put("reason", "Variable of " + currentRoutine);
                            return result;
                        }
                        if (routineInfo.hasParameter(atomUpper)) {
                            result.put("resolved", true);
                            result.put("is_routine_param", true);
                            result.put("reference_type", "routine_parameter");
                            result.put("reason", "Parameter of " + currentRoutine);
                            return result;
                        }
                    }
                }
            }

            // 2b. Поиск по таблицам текущего statement
            if (builder != null) {
                StatementInfo stmtInfo = builder.getStatements().get(statementGeoid);
                if (stmtInfo != null) {
                    String colName = tokens.get(0).toUpperCase();

                    // Ищем по source tables
                    for (String tblGeoid : stmtInfo.getSourceTableGeoids()) {
                        TableInfo tableInfo = builder.getTables().get(tblGeoid);
                        if (tableInfo != null) {
                            result.put("resolved", true);
                            result.put("table_geoid", tblGeoid);
                            result.put("table_name", tableInfo.tableName());
                            result.put("column_name", colName);
                            result.put("reference_type", "tables");
                            result.put("is_column_reference", true);
                            result.put("reason", "Простая ссылка resolved: таблица " + tableInfo.tableName());
                            return result;
                        }
                    }

                    // Также проверяем target tables (для UPDATE SET, INSERT)
                    for (String tblGeoid : stmtInfo.getTargetTableGeoids()) {
                        TableInfo tableInfo = builder.getTables().get(tblGeoid);
                        if (tableInfo != null) {
                            result.put("resolved", true);
                            result.put("table_geoid", tblGeoid);
                            result.put("table_name", tableInfo.tableName());
                            result.put("column_name", colName);
                            result.put("reference_type", "tables");
                            result.put("is_column_reference", true);
                            result.put("reason", "Простая ссылка resolved: target таблица " + tableInfo.tableName());
                            return result;
                        }
                    }
                }
            }

            // Fallback: implicit table resolution
            ResolvedRef implicitRef = nameResolver.resolveImplicitTable(statementGeoid);
            if (implicitRef.isResolved()) {
                result.put("resolved", true);
                result.put("table_geoid", implicitRef.getGeoid());
                result.put("column_name", tokens.get(0).toUpperCase());
                result.put("reference_type", "implicit");
                result.put("is_column_reference", true);
                result.put("reason", "Простая ссылка resolved через implicit table");
                return result;
            }

            result.put("reason", "Простая ссылка: таблица не определена");
            return result;
        }

        // ═══ 3. CONSTANTS ═══
        if (!isComplex && tokens.size() == 1 && firstCanonical.isConstant()) {
            result.put("resolved", true);
            result.put("reason", "Константа: " + firstCanonical);
            return result;
        }

        // ═══ 4. SYSTEM PSEUDO-COLUMNS ═══
        if (tokens.size() == 1 && firstCanonical.isSystemPseudoColumn()) {
            result.put("resolved", true);
            result.put("reason", "Системная псевдоколонка: " + tokens.get(0));
            return result;
        }

        result.put("reason", "Необрабатываемый тип атома");
        return result;
    }

    /**
     * Fallback resolution при отсутствии token_details (backward compat).
     * S2.BUG-1: added multi-level parent scope traversal for table.column case;
     * strategy is always set to "text_fallback" so the resolution log entry is complete.
     */
    private Map<String, Object> resolveByText(String atomText, String statementGeoid,
                                               Map<String, Object> result) {
        result.put("strategy", "text_fallback");
        String text = atomText.trim();
        String[] parts = text.split("\\.");
        String columnPart = parts[parts.length - 1];
        String tablePart = parts.length > 1 ? String.join(".", Arrays.copyOf(parts, parts.length - 1)) : null;

        if (tablePart != null) {
            // Try current statement scope first
            ResolvedRef tableRef = nameResolver.resolve(tablePart, "any", statementGeoid);
            if (tableRef.isResolved()) {
                result.put("resolved", true);
                result.put("table_geoid", tableRef.getGeoid());
                result.put("table_name", tablePart);
                result.put("column_name", columnPart);
                result.put("reference_type", tableRef.getType());
                result.put("is_column_reference", true);
                return result;
            }
            // Walk parent statement scopes (e.g. SOURCE.* in MERGE WHEN UPDATE SET)
            if (builder != null) {
                StatementInfo si = builder.getStatements().get(statementGeoid);
                while (si != null && si.getParentStatementGeoid() != null) {
                    String parentGeoid = si.getParentStatementGeoid();
                    ResolvedRef parentRef = nameResolver.resolve(tablePart, "any", parentGeoid);
                    if (parentRef.isResolved()) {
                        result.put("resolved", true);
                        result.put("table_geoid", parentRef.getGeoid());
                        result.put("table_name", tablePart);
                        result.put("column_name", columnPart);
                        result.put("reference_type", parentRef.getType());
                        result.put("is_column_reference", true);
                        result.put("reason", "resolved via parent scope: " + parentGeoid);
                        return result;
                    }
                    si = builder.getStatements().get(parentGeoid);
                }
            }
            // Cursor record alias: rec.FIELD in text-fallback path
            if (scopeManager != null) {
                String cursorStmtGeoid = scopeManager.getCursorRecordStmt(tablePart);
                if (cursorStmtGeoid != null) {
                    String sourceTableGeoid = findCursorSourceTable(cursorStmtGeoid);
                    if (sourceTableGeoid != null) {
                        result.put("resolved", true);
                        result.put("table_geoid", sourceTableGeoid);
                        result.put("table_name", tablePart);
                        result.put("column_name", columnPart);
                        result.put("reference_type", "cursor_record");
                        result.put("is_column_reference", true);
                        result.put("reason", "cursor record (text): " + tablePart + "." + columnPart);
                    } else {
                        result.put("resolved", true);
                        result.put("reference_type", "cursor_record_expr");
                        result.put("column_name", columnPart);
                        result.put("reason", "cursor record expr (text): " + tablePart + "." + columnPart);
                    }
                    return result;
                }
            }
        }

        ResolvedRef implicitRef = nameResolver.resolveImplicitTable(statementGeoid);
        if (implicitRef.isResolved()) {
            result.put("resolved", true);
            result.put("table_geoid", implicitRef.getGeoid());
            result.put("column_name", columnPart);
            result.put("reference_type", "implicit");
            result.put("is_column_reference", true);
            return result;
        }

        result.put("reason", "Could not resolve table for atom");
        return result;
    }

    /**
     * Returns the first source table geoid for a cursor's SELECT statement,
     * or null if the cursor has no registered source table (e.g. CURSOR IS SELECT expr).
     * Used for cursor FOR-loop record alias resolution (rec.FIELD).
     */
    private String findCursorSourceTable(String cursorStmtGeoid) {
        if (builder == null || cursorStmtGeoid == null) return null;
        StatementInfo si = builder.getStatements().get(cursorStmtGeoid);
        if (si == null) return null;
        // Direct source tables (unusual, but check first)
        if (!si.getSourceTables().isEmpty())
            return si.getSourceTables().keySet().iterator().next();
        // Cursor body is a child SELECT — look there
        for (String childGeoid : si.getChildStatements()) {
            StatementInfo child = builder.getStatements().get(childGeoid);
            if (child != null && !child.getSourceTables().isEmpty())
                return child.getSourceTables().keySet().iterator().next();
        }
        return null;
    }

    // =========================================================================
    // POSITION-BASED ATOM → COLUMN BINDING
    // Port from Python: exitSelect_list_elements → position-based atom binding
    // =========================================================================

    /**
     * Связывает atoms с output column по позиции (line:col range).
     */
    public void bindAtomsToOutputColumn(String statementGeoid,
                                         int startLine, int startCol,
                                         int endLine, int endCol, int columnOrder) {
        Map<String, Map<String, Object>> stmtAtoms = atomsByStatement.get(statementGeoid);
        if (stmtAtoms == null) return;

        for (var entry : stmtAtoms.entrySet()) {
            Map<String, Object> atomData = entry.getValue();

            // Уже привязан к другой колонке?
            if (atomData.get("output_column_sequence") != null) continue;

            String pos = (String) atomData.get("position");
            if (pos == null) continue;

            String[] parts = pos.split(":");
            if (parts.length < 2) continue;

            int atomLine;
            int atomCol;
            try {
                atomLine = Integer.parseInt(parts[0]);
                atomCol = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                continue;
            }

            if (isPositionInRange(atomLine, atomCol, startLine, startCol, endLine, endCol)) {
                atomData.put("output_column_sequence", columnOrder);
            }
        }
    }

    /**
     * G3-MERGE: Positionally binds atoms in the given source range to the N-th column
     * from the MERGE INSERT column list (StatementInfo.mergeInsertColumnOrder).
     * Sets dml_target_ref and merge_clause="INSERT" on matching atoms.
     * Called from BaseSemanticListener.onValuesExpressionExit when Merge_insert_part=true.
     */
    public void bindAtomsToMergeInsertTarget(String stmtGeoid,
            int startLine, int startCol, int endLine, int endCol, int position) {
        StatementInfo si = builder == null ? null : builder.getStatements().get(stmtGeoid);
        if (si == null) return;
        List<String> insertCols = si.getMergeInsertColumnOrder();
        if (position < 1 || position > insertCols.size()) return;
        String columnRef = insertCols.get(position - 1);
        if (columnRef == null) return;

        Map<String, Map<String, Object>> stmtAtoms = atomsByStatement.get(stmtGeoid);
        if (stmtAtoms == null) return;
        for (var entry : stmtAtoms.entrySet()) {
            Map<String, Object> atom = entry.getValue();
            String pos = (String) atom.get("position");
            if (pos == null) continue;
            String[] parts = pos.split(":");
            if (parts.length < 2) continue;
            try {
                int al = Integer.parseInt(parts[0]);
                int ac = Integer.parseInt(parts[1]);
                if (isPositionInRange(al, ac, startLine, startCol, endLine, endCol)) {
                    atom.putIfAbsent("dml_target_ref", columnRef);
                    atom.put("merge_clause", "INSERT");
                }
            } catch (NumberFormatException ignored) { /* skip */ }
        }
    }

    /**
     * G3-MERGE: Binds atoms in the RHS expression range of a MERGE UPDATE SET element
     * to the corresponding target affected column.
     * Sets dml_target_ref and merge_clause="UPDATE" on matching atoms.
     * Called from BaseSemanticListener.onMergeElementExit with the expression's position range.
     */
    public void bindAtomsToMergeUpdateTarget(String stmtGeoid,
            String columnRef, int startLine, int startCol, int endLine, int endCol) {
        if (stmtGeoid == null || columnRef == null) return;
        Map<String, Map<String, Object>> stmtAtoms = atomsByStatement.get(stmtGeoid);
        if (stmtAtoms == null) return;
        for (var entry : stmtAtoms.entrySet()) {
            Map<String, Object> atom = entry.getValue();
            String pos = (String) atom.get("position");
            if (pos == null) continue;
            String[] parts = pos.split(":");
            if (parts.length < 2) continue;
            try {
                int al = Integer.parseInt(parts[0]);
                int ac = Integer.parseInt(parts[1]);
                if (isPositionInRange(al, ac, startLine, startCol, endLine, endCol)) {
                    atom.putIfAbsent("dml_target_ref", columnRef);
                    atom.put("merge_clause", "UPDATE");
                }
            } catch (NumberFormatException ignored) { /* skip */ }
        }
    }

    private boolean isPositionInRange(int line, int col,
                                       int startLine, int startCol,
                                       int endLine, int endCol) {
        if (line < startLine || line > endLine) return false;
        if (line == startLine && col < startCol) return false;
        if (line == endLine && col > endCol) return false;
        return true;
    }

    // =========================================================================
    // DATA RETRIEVAL — Port from Python get_atoms_data
    // =========================================================================

    /**
     * Порт Python: BaseSQLListener.get_atoms_data()
     * Полная сборка atoms для передачи через Arrow Flight.
     */
    public Map<String, Object> getAtomsData() {
        Map<String, Object> atomsData = new LinkedHashMap<>();
        int totalAtomsCount = 0;

        // 1. Atoms по statements
        for (var stmtEntry : atomsByStatement.entrySet()) {
            String stmtGeoid = stmtEntry.getKey();
            Map<String, Map<String, Object>> stmtAtoms = stmtEntry.getValue();
            if (stmtAtoms.isEmpty()) continue;

            int stmtTotal = stmtAtoms.size();
            long stmtResolved = stmtAtoms.values().stream()
                    .filter(a -> AtomInfo.STATUS_RESOLVED.equals(a.get("status")))
                    .count();
            totalAtomsCount += stmtTotal;

            Map<String, Object> stmtData = new LinkedHashMap<>();
            stmtData.put("source_type", "statement");
            stmtData.put("source_geoid", stmtGeoid);
            stmtData.put("atoms", stmtAtoms);
            stmtData.put("total_atoms", stmtTotal);
            stmtData.put("resolved_atoms", stmtResolved);

            atomsData.put("statement:" + stmtGeoid, stmtData);
        }

        // 2. Atoms по routines (группировка statements, принадлежащих routine)
        if (builder != null) {
            Map<String, Map<String, Map<String, Object>>> routineAtoms = new LinkedHashMap<>();
            for (var stmtEntry : atomsByStatement.entrySet()) {
                StatementInfo stmtInfo = builder.getStatements().get(stmtEntry.getKey());
                if (stmtInfo != null && stmtInfo.getRoutineGeoid() != null) {
                    routineAtoms
                            .computeIfAbsent(stmtInfo.getRoutineGeoid(), k -> new LinkedHashMap<>())
                            .putAll(stmtEntry.getValue());
                }
            }
            for (var routineEntry : routineAtoms.entrySet()) {
                Map<String, Object> routineData = new LinkedHashMap<>();
                routineData.put("source_type", "routine");
                routineData.put("source_geoid", routineEntry.getKey());
                routineData.put("atoms", routineEntry.getValue());
                routineData.put("total_atoms", routineEntry.getValue().size());
                routineData.put("resolved_atoms",
                        routineEntry.getValue().values().stream()
                                .filter(a -> AtomInfo.STATUS_RESOLVED.equals(a.get("status")))
                                .count());
                atomsData.put("routine:" + routineEntry.getKey(), routineData);
            }
        }

        // 3. Unattached atoms
        if (!unattachedAtoms.isEmpty()) {
            totalAtomsCount += unattachedAtoms.size();
            Map<String, Object> unattachedData = new LinkedHashMap<>();
            unattachedData.put("source_type", "unattached");
            unattachedData.put("source_geoid", "unattached");
            unattachedData.put("atoms", unattachedAtoms);
            unattachedData.put("total_atoms", unattachedAtoms.size());
            unattachedData.put("resolved_atoms",
                    unattachedAtoms.values().stream()
                            .filter(a -> AtomInfo.STATUS_RESOLVED.equals(a.get("status")))
                            .count());
            atomsData.put("unattached", unattachedData);
        }

        // 4. Summary
        int statementsWithAtoms = (int) atomsData.keySet().stream()
                .filter(k -> k.startsWith("statement:")).count();
        int routinesWithAtoms = (int) atomsData.keySet().stream()
                .filter(k -> k.startsWith("routine:")).count();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("total_sources", atomsData.size());
        summary.put("total_atoms", totalAtomsCount);
        summary.put("statements_with_atoms", statementsWithAtoms);
        summary.put("routines_with_atoms", routinesWithAtoms);
        summary.put("has_unattached", atomsData.containsKey("unattached"));
        atomsData.put("summary", summary);

        return atomsData;
    }

    /**
     * Backward-compatible getAtoms — delegates to getAtomsData.
     */
    public Map<String, Object> getAtoms() {
        return getAtomsData();
    }

    /**
     * Возвращает атомы для конкретного statement.
     */
    public Map<String, Map<String, Object>> getAtomsForStatement(String statementGeoid) {
        return atomsByStatement.getOrDefault(statementGeoid, Map.of());
    }

    public Map<String, Map<String, Object>> getUnattachedAtoms() {
        return unattachedAtoms;
    }

    /** S1.PRE: one entry per processed atom — used to write DaliResolutionLog. */
    public List<Map<String, Object>> getResolutionLog() {
        return Collections.unmodifiableList(resolutionLog);
    }

    /** Appends one DaliResolutionLog entry. raw_input captures the exact atom text
     *  as collected by the listener — including any accidentally-grabbed brackets
     *  or schema prefixes (the primary use-case for this log).
     *  table_name, column_name, position are included for failed-resolution diagnosis. */
    private void appendLog(String stmtGeoid, Map<String, Object> atomData,
                           Map<String, Object> resolution) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("statement_geoid", stmtGeoid);
        entry.put("raw_input",        atomData.get("atom_text"));
        entry.put("result_kind",      atomData.get("status"));
        entry.put("is_function_call", Boolean.TRUE.equals(atomData.get("is_function_call")));
        entry.put("atom_context",     atomData.get("atom_context"));
        entry.put("parent_context",   atomData.get("parent_context"));
        entry.put("note",        resolution != null ? resolution.get("reason")         : null);
        entry.put("strategy",    resolution != null ? resolution.get("reference_type") : null);
        // Variables of the lookup attempt — critical for diagnosing failed resolutions
        entry.put("table_name",  atomData.get("table_name"));
        entry.put("column_name", atomData.get("column_name"));
        entry.put("position",    atomData.get("position"));
        resolutionLog.add(entry);
    }

    public void clear() {
        atomsByStatement.clear();
        unattachedAtoms.clear();
        resolutionLog.clear();
    }
}
