package com.hound.semantic.engine;

import com.hound.api.HoundEventListener;
import com.hound.api.NoOpHoundEventListener;
import com.hound.semantic.model.AtomInfo;
import com.hound.semantic.model.RecordInfo;
import com.hound.semantic.model.RoutineInfo;
import com.hound.semantic.model.StatementInfo;
import com.hound.semantic.model.TableInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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

    // HAL-08: pending qualified atom refs (alias.column where alias not yet known)
    public record PendingQualifiedAtom(
            Map<String, Object> atomData, String alias, String columnName,
            String statementGeoid, String atomKey) {}
    private final List<PendingQualifiedAtom> pendingQualifiedAtoms = new ArrayList<>();

    public List<PendingQualifiedAtom> getPendingQualifiedAtoms() { return pendingQualifiedAtoms; }

    // G3: current MERGE UPDATE target column (set at enterMerge_element, cleared at exit)
    private String currentMergeTargetColumn = null;

    // ═══════ Listener (C.1.3) ═══════
    private final HoundEventListener listener;
    private final String file;
    private final AtomicInteger atomCounter = new AtomicInteger(0);

    // External dependencies
    private NameResolver nameResolver;
    private StructureAndLineageBuilder builder;
    private ScopeManager scopeManager;

    /** Backward-compatible no-arg constructor. */
    public AtomProcessor() {
        this(NoOpHoundEventListener.INSTANCE, "");
    }

    public AtomProcessor(HoundEventListener listener, String file) {
        this.listener = listener != null ? listener : NoOpHoundEventListener.INSTANCE;
        this.file = file != null ? file : "";
    }

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
            atomData.put("primary_status", AtomInfo.STATUS_RESOLVED);
            atomData.put("is_complex_atom", true);
        }

        // Классификация по token details
        classifyAtom(atomData);

        stmtAtoms.put(atomKey, atomData);
        logger.debug("ATOM REGISTER '{}' → stmt={} ctx={} line={}",
                text, statementGeoid, parentContext, line);

        // C.1.3: notify listener
        listener.onAtomExtracted(file, atomCounter.incrementAndGet(), context != null ? context : "UNKNOWN");
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

        // --- G6: Collection field access: collection(index).field — e.g. L_TAB(I).ORDER_ID
        // Pattern: IDENT LEFT_PAREN ... RIGHT_PAREN PERIOD IDENT
        // Must be checked BEFORE the generic function_call branch.
        // Works for complex atoms too (nestedAtomCount > 0 due to the index sub-atom).
        if (isCollectionFieldAccess(tokens, tokenDetails)) {
            atomData.put("is_collection_field_access", true);
            atomData.put("collection_field_name", tokens.get(tokens.size() - 1).toUpperCase());
            atomData.put("collection_name",        tokens.get(0).toUpperCase());
            return;
        }

        // --- Вызов функции: ID LEFT_PAREN ... ---
        if (tokens.size() >= 3
                && getCanonical(tokenDetails.get(0)).isIdentifier()
                && getCanonical(tokenDetails.get(1)) == CanonicalTokenType.LEFT_PAREN) {
            String funcName = tokens.get(0).toUpperCase();
            atomData.put("is_function_call", true);
            atomData.put("function_name", funcName);
            // KI-DBMSSQL-1: mark DBMS_SQL dynamic SQL calls as a stub marker
            if (funcName.startsWith("DBMS_SQL")) {
                atomData.put("is_dbms_sql_call", true);
            }
            // KI-VARRAY-1: IDENT(expr) without .field might be indexed collection access (l_tab(i)).
            // Cannot distinguish from function call at token level — tag as candidate for
            // downstream resolution once variable registry is available.
            atomData.put("is_varray_access_candidate", true);
            atomData.put("collection_name_candidate", funcName);
            return;
        }

        // --- KI-DBMSSQL-1: qualified DBMS_SQL.method call (ID.PERIOD.ID.PAREN...) ---
        if (tokens.size() >= 5
                && "DBMS_SQL".equalsIgnoreCase(tokens.get(0))
                && getCanonical(tokenDetails.get(1)) == CanonicalTokenType.PERIOD
                && getCanonical(tokenDetails.get(2)).isIdentifier()) {
            atomData.put("is_function_call", true);
            atomData.put("is_dbms_sql_call", true);
            atomData.put("function_name", "DBMS_SQL." + tokens.get(2).toUpperCase());
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

        // HAL-07: capture routine_geoid before scope pops
        String routineGeoid = (scopeManager != null && scopeManager.currentRoutine() != null)
                ? scopeManager.currentRoutine() : null;

        int total = 0, resolved = 0, constants = 0, functions = 0, failed = 0;

        for (var entry : stmtAtoms.entrySet()) {
            total++;
            Map<String, Object> atomData = entry.getValue();

            // Пропускаем уже обработанные
            if (atomData.get("primary_status") != null) continue;

            // Пропускаем константы
            if (Boolean.TRUE.equals(atomData.get("is_constant"))) {
                atomData.put("status", AtomInfo.STATUS_CONSTANT);
                atomData.put("primary_status", AtomInfo.STATUS_CONSTANT);
                constants++;
                appendLog(statementGeoid, atomData, null);
                continue;
            }
            // Пропускаем вызовы функций
            if (Boolean.TRUE.equals(atomData.get("is_function_call"))) {
                atomData.put("status", AtomInfo.STATUS_FUNCTION_CALL);
                atomData.put("primary_status", AtomInfo.STATUS_FUNCTION_CALL);
                functions++;
                appendLog(statementGeoid, atomData, null);
                continue;
            }

            // Попытка resolve
            Map<String, Object> resolution = resolveAtomReference(entry.getKey(), atomData, statementGeoid);

            if (resolution != null && Boolean.TRUE.equals(resolution.get("resolved"))) {
                atomData.put("status", AtomInfo.STATUS_RESOLVED);
                atomData.put("primary_status", AtomInfo.STATUS_RESOLVED);
                atomData.put("resolution", resolution);
                atomData.put("table_geoid", resolution.get("table_geoid"));
                atomData.put("column_name", resolution.get("column_name"));
                atomData.put("table_name", resolution.get("table_name"));
                atomData.put("is_column_reference", Boolean.TRUE.equals(resolution.get("is_column_reference")));
                atomData.put("is_routine_param", Boolean.TRUE.equals(resolution.get("is_routine_param")));
                atomData.put("is_routine_var", Boolean.TRUE.equals(resolution.get("is_routine_var")));

                // HAL-01: derive qualifier from resolution source
                String resolveStrategy = (String) resolution.get("resolve_strategy");
                atomData.put("resolve_strategy", resolveStrategy);
                if (resolveStrategy != null) {
                    switch (resolveStrategy) {
                        case "3_cte"             -> atomData.put("qualifier", AtomInfo.QUALIFIER_CTE);
                        case "4_subquery_alias",
                             "5_child_subquery",
                             "6_source_subquery" -> atomData.put("qualifier", AtomInfo.QUALIFIER_SUBQUERY);
                        case "resolveImplicitTable" -> atomData.put("qualifier", AtomInfo.QUALIFIER_INFERRED);
                        case "cursorRecordAliases"  -> atomData.put("qualifier", AtomInfo.QUALIFIER_LINKED);
                        default                  -> atomData.put("qualifier", AtomInfo.QUALIFIER_LINKED);
                    }
                } else {
                    atomData.put("qualifier", AtomInfo.QUALIFIER_LINKED);
                }

                String tableGeoid = (String) resolution.get("table_geoid");
                String columnName = (String) resolution.get("column_name");
                if (tableGeoid != null && columnName != null && builder != null
                        && builder.getTables().containsKey(tableGeoid)) {
                    builder.addColumn(tableGeoid, columnName, (String) atomData.get("atom_text"), null);
                }

                resolved++;
                logger.debug("Atom resolved: {} → table={}, column={}", entry.getKey(), tableGeoid, columnName);

                boolean isVar   = Boolean.TRUE.equals(atomData.get("is_routine_var"));
                boolean isParam = Boolean.TRUE.equals(atomData.get("is_routine_param"));
                if ((isVar || isParam) && atomData.get("dml_target_ref") != null) {
                    atomData.put("primary_status", AtomInfo.STATUS_RECONSTRUCT_DIRECT);
                    atomData.put("status", AtomInfo.STATUS_RECONSTRUCT_DIRECT);
                }
            } else {
                failed++;
                String parentCtx = (String) atomData.get("parent_context");

                // HAL-08: qualified alias.column deferred to post-walk pending resolution
                if (resolution != null && Boolean.TRUE.equals(resolution.get("pending_qualified"))) {
                    String alias = (String) resolution.get("pending_alias");
                    String colName = (String) resolution.get("pending_column");
                    atomData.put("status", AtomInfo.STATUS_PENDING_INJECT);
                    atomData.put("primary_status", AtomInfo.STATUS_PENDING_INJECT);
                    atomData.put("column_name", colName);
                    pendingQualifiedAtoms.add(new PendingQualifiedAtom(
                            atomData, alias, colName, statementGeoid, entry.getKey()));
                    logger.debug("PENDING_QUALIFIED: {} alias={} col={} deferred",
                            entry.getKey(), alias, colName);
                } else {
                    // HAL2-01: check if atom references a pending source → PENDING_INJECT
                    String pendingKind = detectPendingKind(atomData, statementGeoid, resolution);
                    if (pendingKind != null) {
                        atomData.put("status", AtomInfo.STATUS_PENDING_INJECT);
                        atomData.put("primary_status", AtomInfo.STATUS_PENDING_INJECT);
                        atomData.put("pending_kind", pendingKind);
                        atomData.put("pending_since", System.currentTimeMillis());
                        String pendingRoutine = scopeManager != null ? scopeManager.currentRoutine() : null;
                        atomData.put("pending_snapshot", buildPendingSnapshot(
                                pendingRoutine, statementGeoid));
                        logger.debug("HAL2-01: PENDING_INJECT kind={} atom={}", pendingKind, entry.getKey());
                    } else {
                        if ("SELECT".equals(parentCtx) || "INSERT".equals(parentCtx)
                                || "UPDATE".equals(parentCtx) || "MERGE".equals(parentCtx)
                                || "SET_EXPR".equals(parentCtx)) {
                            logger.warn("Could not resolve atom: {} in context {}", entry.getKey(), parentCtx);
                            listener.onSemanticWarning(file, "ATOM_UNRESOLVED",
                                    "Could not resolve atom: " + entry.getKey()
                                            + " in context " + parentCtx);
                        }
                        atomData.put("status", AtomInfo.STATUS_UNRESOLVED);
                        atomData.put("primary_status", AtomInfo.STATUS_UNRESOLVED);
                        atomData.put("warning", AtomInfo.LEGACY_STATUS_UNRESOLVED);
                    }
                }
                if (resolution != null) {
                    atomData.put("resolution", resolution);
                }
            }
            appendLog(statementGeoid, atomData, resolution);
        }

        // HAL-02: derive kind; HAL-04: CTRL_FLOW + FN_VERIFIED; HAL-03: confidence
        for (var atomData : stmtAtoms.values()) {
            atomData.put("kind", deriveKind(atomData));

            // HAL-04: CTRL_FLOW qualifier for control-flow contexts (ADR-HND-004 §4A)
            String parentCtx = (String) atomData.get("parent_context");
            if (parentCtx != null) {
                switch (parentCtx) {
                    case "IF_COND", "WHILE_COND", "EXIT_WHEN", "CONTINUE_WHEN", "RETURN_EXPR"
                            -> atomData.put("qualifier", AtomInfo.QUALIFIER_CTRL_FLOW);
                    default -> {}
                }
            }

            // HAL-04: FN_VERIFIED / FN_UNVERIFIED for function calls
            if (AtomInfo.STATUS_FUNCTION_CALL.equals(atomData.get("primary_status"))) {
                boolean verified = false;
                if (builder != null) {
                    String atomText = (String) atomData.get("atom_text");
                    if (atomText != null) {
                        String funcName = atomText.contains("(")
                                ? atomText.substring(0, atomText.indexOf('(')).trim().toUpperCase()
                                : atomText.toUpperCase();
                        verified = builder.getRoutines().containsKey(funcName);
                    }
                }
                atomData.put("qualifier", verified
                        ? AtomInfo.QUALIFIER_FN_VERIFIED : AtomInfo.QUALIFIER_FN_UNVERIFIED);
            }

            atomData.put("confidence", deriveConfidence(atomData));

            // HAL-07: routine_geoid + pending_verification
            if (routineGeoid != null) atomData.put("routine_geoid", routineGeoid);
            atomData.put("pending_verification", true);
        }

        // B2.AR3 — atom resolution audit
        logger.info("DIAG Atoms [{}]: total={} resolved={} const={} func={} failed={}",
                statementGeoid, total, resolved, constants, functions, failed);

        // G1: build affected_columns on StatementInfo from all resolved atoms
        buildAffectedColumnsFromAtoms(statementGeoid, stmtAtoms);
    }

    /**
     * Post-resolution pass: builds affectedColumns from atoms.
     * Currently handles G6: INSERT VALUES collection(i).field → target column registration.
     */
    private void buildAffectedColumnsFromAtoms(String statementGeoid,
                                                Map<String, Map<String, Object>> stmtAtoms) {
        buildInsertValuesAffectedColumns(statementGeoid, stmtAtoms);
    }

    /**
     * G6: scans INSERT_VALUES atoms for collection field access patterns,
     * groups them by VALUES slot (output_column_sequence), and registers
     * each slot as an INSERT AffectedColumn.
     *
     * Positional matching: if the target table has DDL-derived ordinalPosition,
     * the N-th slot maps to the N-th column. Otherwise falls back to the field
     * name from the collection record (valid for %ROWTYPE / name-matching records).
     *
     * Guard: skipped if the INSERT already has an explicit column list (G5).
     * For slots with multiple collection fields (arithmetic), target column name
     * is resolved only from DDL; without DDL the slot is skipped.
     */
    @SuppressWarnings("unchecked")
    private void buildInsertValuesAffectedColumns(String statementGeoid,
                                                   Map<String, Map<String, Object>> stmtAtoms) {
        StatementInfo si = builder == null ? null : builder.getStatements().get(statementGeoid);
        if (si == null || !"INSERT".equals(si.getType())) return;
        if (!si.getInsertTargetColumns().isEmpty()) {
            // G5 explicit col list: column bindings already handled, but we still need to
            // record which collection variables appear in VALUES for FORALL INSERT patterns (G6-EXT).
            Set<String> collsFound = new java.util.LinkedHashSet<>();
            for (Map<String, Object> a : stmtAtoms.values()) {
                if (!Boolean.TRUE.equals(a.get("is_collection_field_access"))) continue;
                String pCtx = (String) a.get("parent_context");
                if (!"VALUES".equals(pCtx) && !"INSERT_VALUES".equals(pCtx)) continue;
                String collName = (String) a.get("collection_name");
                if (collName != null) { si.addBulkCollectSource(collName); collsFound.add(collName); }
            }
            // Populate RecordInfo.fields from cursor SELECT columnsOutput (non-wildcard case)
            if (builder != null && scopeManager != null) {
                for (String collName : collsFound) {
                    String cursorStmtGeoid = scopeManager.getCursorRecordStmt(collName);
                    if (cursorStmtGeoid == null) continue;
                    com.hound.semantic.model.RecordInfo rec =
                            builder.getRecords().values().stream()
                                    .filter(r -> collName.equals(r.getVarName()))
                                    .findFirst().orElse(null);
                    if (rec != null && rec.getFields().isEmpty())
                        populateRecordFields(rec, cursorStmtGeoid);
                }
            }
            return;
        }

        List<String> targetGeoids = si.getTargetTableGeoids();
        String targetGeoid = targetGeoids.isEmpty() ? null : targetGeoids.get(0);

        // slot (1-based VALUES position) → distinct field names from collection accesses
        Map<Integer, List<String>> slotToFields       = new TreeMap<>();
        // slot → collection variable name (first one wins; normally all atoms in a slot share one)
        Map<Integer, String>       slotToCollection   = new TreeMap<>();

        for (Map<String, Object> a : stmtAtoms.values()) {
            if (!Boolean.TRUE.equals(a.get("is_collection_field_access"))) continue;
            // parent_context is set from ScopeContext.getActiveClause() = "VALUES"
            // (not "INSERT_VALUES" — that string lives in BaseSemanticListener.getCurrentParentContext).
            // Statement type guard below already limits scope to INSERT only.
            String pCtx = (String) a.get("parent_context");
            if (!"VALUES".equals(pCtx) && !"INSERT_VALUES".equals(pCtx)) continue;
            Integer slot       = (Integer) a.get("output_column_sequence");
            String  field      = (String)  a.get("collection_field_name");
            String  collName   = (String)  a.get("collection_name");
            if (slot == null || field == null) continue;
            slotToFields.computeIfAbsent(slot, k -> new ArrayList<>()).add(field);
            slotToCollection.putIfAbsent(slot, collName);
        }

        if (slotToFields.isEmpty()) return;

        for (var entry : slotToFields.entrySet()) {
            int          slot       = entry.getKey();
            List<String> fields     = entry.getValue();
            String       collName   = slotToCollection.get(slot);

            // Positional matching against DDL-registered columns
            String targetColName = findColumnByOrdinal(targetGeoid, slot);

            // Fallback: single collection field in this slot → field name = target col name
            if (targetColName == null && fields.size() == 1) {
                targetColName = fields.get(0);
            }

            if (targetColName == null) {
                // Multi-field slot without DDL — cannot name target column; skip
                logger.debug("G6 slot={} multi-field={} → no DDL, skipping", slot, fields);
                continue;
            }

            String columnRef = targetGeoid != null ? targetGeoid + "." + targetColName : targetColName;
            // dataset_alias = collection variable name → used for FORALL INSERT column mapping
            si.addAffectedColumn(columnRef, targetColName, targetGeoid, collName,
                                 "INSERT", "target", slot);
            logger.debug("G6 INSERT_VALUES [{}] slot={} field(s)={} coll={} → target_col={}",
                         statementGeoid, slot, fields, collName, targetColName);
        }

        // Populate RecordInfo.fields from the cursor SELECT output columns (once per collection)
        if (builder != null && scopeManager != null) {
            Set<String> seen = new HashSet<>();
            for (String collName : slotToCollection.values()) {
                if (collName == null || !seen.add(collName)) continue;
                String cursorStmtGeoid = scopeManager.getCursorRecordStmt(collName);
                if (cursorStmtGeoid == null) continue;
                com.hound.semantic.model.RecordInfo rec =
                        builder.getRecords().values().stream()
                                .filter(r -> collName.equals(r.getVarName()))
                                .findFirst().orElse(null);
                if (rec != null && rec.getFields().isEmpty())
                    populateRecordFields(rec, cursorStmtGeoid);
            }
        }
    }

    /**
     * Populates RecordInfo.fields from the columnsOutput of the cursor SELECT statement.
     * Skips wildcard entries ("*"). Sorted by "order" attribute.
     */
    private void populateRecordFields(com.hound.semantic.model.RecordInfo rec, String cursorStmtGeoid) {
        if (builder == null || cursorStmtGeoid == null) return;
        StatementInfo cursorSi = builder.getStatements().get(cursorStmtGeoid);
        if (cursorSi == null) return;
        cursorSi.getColumnsOutput().entrySet().stream()
                .sorted(Comparator.comparingInt(e -> {
                    Object ord = e.getValue().get("order");
                    return ord instanceof Number n ? n.intValue() : 0;
                }))
                .forEach(e -> {
                    String name = (String) e.getValue().get("name");
                    // Skip wildcard — field names unknown without DDL
                    if (name != null && !"*".equals(name)) rec.addField(name);
                });
    }

    /**
     * G6: detects collection(index).field token pattern.
     * Requires ≥ 6 tokens: IDENT ( ... ) . IDENT
     * The last three tokens must be RIGHT_PAREN · PERIOD · IDENTIFIER.
     */
    private static boolean isCollectionFieldAccess(List<String> tokens,
                                                    List<Map<String, String>> tokenDetails) {
        int n = tokens.size();
        if (n < 6) return false;
        if (!getCanonical(tokenDetails.get(0)).isIdentifier()) return false;
        if (getCanonical(tokenDetails.get(1)) != CanonicalTokenType.LEFT_PAREN) return false;
        if (getCanonical(tokenDetails.get(n - 3)) != CanonicalTokenType.RIGHT_PAREN) return false;
        if (getCanonical(tokenDetails.get(n - 2)) != CanonicalTokenType.PERIOD) return false;
        if (!getCanonical(tokenDetails.get(n - 1)).isIdentifier()) return false;
        return true;
    }

    /** Returns the column name registered at the given 1-based ordinal for a table, or null. */
    private String findColumnByOrdinal(String tableGeoid, int ordinal) {
        if (builder == null || tableGeoid == null) return null;
        return builder.getColumns().values().stream()
                .filter(c -> tableGeoid.equals(c.getTableGeoid())
                          && c.getOrdinalPosition() == ordinal)
                .map(com.hound.semantic.model.ColumnInfo::getColumnName)
                .findFirst()
                .orElse(null);
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
                result.put("resolve_strategy", "sequence");
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
                    result.put("resolve_strategy", resolved.getStrategy());
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
                    result.put("resolve_strategy", resolved.getStrategy());
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
                            result.put("resolve_strategy", parentResolved.getStrategy());
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
                            result.put("resolve_strategy", "cursorRecordAliases");
                            result.put("reason", "cursor record: " + tableAlias + "." + columnName);
                        } else {
                            result.put("resolved", true);
                            result.put("reference_type", "cursor_record_expr");
                            result.put("column_name", columnName);
                            result.put("resolve_strategy", "cursorRecordAliases");
                            result.put("reason", "cursor record expr: " + tableAlias + "." + columnName);
                        }
                        return result;
                    }
                }

                // HAL-08: defer to pending — alias may appear in a later block
                result.put("pending_qualified", true);
                result.put("pending_alias", tableAlias);
                result.put("pending_column", columnName);
                result.put("reason", "Таблица/подзапрос не найден для: " + tableAlias + " (deferred)");
                return result;
            }

            // DATE 'string' (ANSI date constant) — IDENTIFIER followed by STRING_LITERAL
            if (getCanonical(tokenDetails.get(0)).isIdentifier()
                    && tokenDetails.size() >= 2
                    && getCanonical(tokenDetails.get(1)) == CanonicalTokenType.STRING_LITERAL) {
                String firstText = tokenDetails.get(0).get("text");
                if (firstText != null && "DATE".equalsIgnoreCase(firstText)) {
                    result.put("resolved", true);
                    result.put("resolve_strategy", "constant");
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
                            result.put("resolve_strategy", "hasVariable");
                            result.put("reason", "Variable of " + currentRoutine);
                            return result;
                        }
                        if (routineInfo.hasParameter(atomUpper)) {
                            result.put("resolved", true);
                            result.put("is_routine_param", true);
                            result.put("reference_type", "routine_parameter");
                            result.put("resolve_strategy", "hasParameter");
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
                            result.put("resolve_strategy", "source_table_direct");
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
                            result.put("resolve_strategy", "target_table_direct");
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
                result.put("resolve_strategy", "resolveImplicitTable");
                result.put("reason", "Простая ссылка resolved через implicit table");
                return result;
            }

            result.put("reason", "Простая ссылка: таблица не определена");
            return result;
        }

        // ═══ 3. CONSTANTS ═══
        if (!isComplex && tokens.size() == 1 && firstCanonical.isConstant()) {
            result.put("resolved", true);
            result.put("resolve_strategy", "constant");
            result.put("reason", "Константа: " + firstCanonical);
            return result;
        }

        // ═══ 4. SYSTEM PSEUDO-COLUMNS ═══
        if (tokens.size() == 1 && firstCanonical.isSystemPseudoColumn()) {
            result.put("resolved", true);
            result.put("resolve_strategy", "system_pseudo");
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
                result.put("resolve_strategy", tableRef.getStrategy());
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
                        result.put("resolve_strategy", parentRef.getStrategy());
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
                        result.put("resolve_strategy", "cursorRecordAliases");
                        result.put("reason", "cursor record (text): " + tablePart + "." + columnPart);
                    } else {
                        result.put("resolved", true);
                        result.put("reference_type", "cursor_record_expr");
                        result.put("column_name", columnPart);
                        result.put("resolve_strategy", "cursorRecordAliases");
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
            result.put("resolve_strategy", "resolveImplicitTable");
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

    public void classifyUnattachedAtoms() {
        for (var atomData : unattachedAtoms.values()) {
            if (Boolean.TRUE.equals(atomData.get("is_constant"))) {
                atomData.put("primary_status", AtomInfo.STATUS_CONSTANT_ORPHAN);
                atomData.put("status", AtomInfo.STATUS_CONSTANT_ORPHAN);
            } else if (atomData.get("primary_status") == null) {
                atomData.put("primary_status", AtomInfo.STATUS_UNRESOLVED);
                atomData.put("status", AtomInfo.STATUS_UNRESOLVED);
            }
            if (atomData.get("kind") == null) {
                atomData.put("kind", deriveKind(atomData));
            }
            if (atomData.get("confidence") == null && atomData.get("primary_status") != null) {
                atomData.put("confidence", deriveConfidence(atomData));
            }
        }
    }

    /**
     * Post-walk: resolve deferred alias.column atoms.
     * Called from resolvePendingColumns after all statements are parsed.
     * For each pending qualified atom:
     *   1. Re-try alias resolution (may succeed now that all CTEs/subqueries are registered)
     *   2. If still unresolved: alias = table_name → find or create table + inferred column
     *   3. Update atom status to RESOLVED or RECONSTRUCT_DIRECT
     */
    public void resolvePendingQualifiedAtoms() {
        if (pendingQualifiedAtoms.isEmpty()) return;
        int resolved = 0, reconstructed = 0;

        for (var pqa : pendingQualifiedAtoms) {
            var atomData = pqa.atomData();
            String alias = pqa.alias();
            String colName = pqa.columnName();
            String stmtGeoid = pqa.statementGeoid();

            // 1. Re-try alias resolution
            if (nameResolver != null) {
                ResolvedRef ref = nameResolver.resolve(alias, "any", stmtGeoid);
                if (ref.isResolved()) {
                    String tGeoid = ref.getGeoid();
                    atomData.put("table_geoid", tGeoid);
                    atomData.put("table_name", alias);
                    atomData.put("column_name", colName);
                    atomData.put("is_column_reference", true);
                    atomData.put("resolve_strategy", ref.getStrategy());
                    atomData.put("primary_status", AtomInfo.STATUS_RESOLVED);
                    atomData.put("status", AtomInfo.STATUS_RESOLVED);
                    atomData.put("qualifier", AtomInfo.QUALIFIER_LINKED);
                    if (builder != null && tGeoid != null
                            && !builder.getStatements().containsKey(tGeoid)) {
                        builder.addInferredColumn(tGeoid, colName, "L5Q");
                    }
                    atomData.put("kind", deriveKind(atomData));
                    atomData.put("confidence", deriveConfidence(atomData));
                    resolved++;
                    continue;
                }

                // 1b. Try parent statement scope
                if (builder != null) {
                    StatementInfo si = builder.getStatements().get(stmtGeoid);
                    if (si != null && si.getParentStatementGeoid() != null) {
                        ref = nameResolver.resolve(alias, "any", si.getParentStatementGeoid());
                        if (ref.isResolved()) {
                            String tGeoid = ref.getGeoid();
                            atomData.put("table_geoid", tGeoid);
                            atomData.put("table_name", alias);
                            atomData.put("column_name", colName);
                            atomData.put("is_column_reference", true);
                            atomData.put("resolve_strategy", ref.getStrategy());
                            atomData.put("primary_status", AtomInfo.STATUS_RESOLVED);
                            atomData.put("status", AtomInfo.STATUS_RESOLVED);
                            atomData.put("qualifier", AtomInfo.QUALIFIER_SUBQUERY);
                            if (!builder.getStatements().containsKey(tGeoid)) {
                                builder.addInferredColumn(tGeoid, colName, "L5Q");
                            }
                            atomData.put("kind", deriveKind(atomData));
                            atomData.put("confidence", deriveConfidence(atomData));
                            resolved++;
                            continue;
                        }
                    }
                }
            }

            // 2. alias = table_name → reconstruct table + inferred column
            if (builder != null) {
                String upperAlias = alias.toUpperCase();
                // Find existing table by name
                String tGeoid = null;
                for (var tEntry : builder.getTables().entrySet()) {
                    String tName = tEntry.getValue().tableName();
                    if (tName != null && tName.toUpperCase().endsWith(upperAlias)) {
                        tGeoid = tEntry.getKey();
                        break;
                    }
                }
                // Not found → register new table with alias as table_name
                if (tGeoid == null) {
                    tGeoid = builder.ensureTable(upperAlias, null);
                }
                builder.addInferredColumn(tGeoid, colName, "L5Q");
                atomData.put("table_geoid", tGeoid);
                atomData.put("table_name", upperAlias);
                atomData.put("column_name", colName);
                atomData.put("is_column_reference", true);
                atomData.put("primary_status", AtomInfo.STATUS_RECONSTRUCT_DIRECT);
                atomData.put("status", AtomInfo.STATUS_RECONSTRUCT_DIRECT);
                atomData.put("qualifier", AtomInfo.QUALIFIER_INFERRED);
                atomData.put("resolve_strategy", "reconstruct_qualified");
                atomData.put("kind", deriveKind(atomData));
                atomData.put("confidence", deriveConfidence(atomData));
                reconstructed++;
            } else {
                atomData.put("primary_status", AtomInfo.STATUS_UNRESOLVED);
                atomData.put("status", AtomInfo.STATUS_UNRESOLVED);
                atomData.put("kind", deriveKind(atomData));
            }
        }

        logger.info("Pending qualified atoms: {} total, {} resolved, {} reconstructed, {} unresolved",
                pendingQualifiedAtoms.size(), resolved, reconstructed,
                pendingQualifiedAtoms.size() - resolved - reconstructed);
        pendingQualifiedAtoms.clear();
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

    static String deriveKind(Map<String, Object> atomData) {
        if (Boolean.TRUE.equals(atomData.get("is_constant")))      return AtomInfo.KIND_CONSTANT;
        if (Boolean.TRUE.equals(atomData.get("is_function_call"))) return AtomInfo.KIND_FUNCTION_CALL;
        if (Boolean.TRUE.equals(atomData.get("is_routine_var")))   return AtomInfo.KIND_VARIABLE;
        if (Boolean.TRUE.equals(atomData.get("is_routine_param"))) return AtomInfo.KIND_PARAMETER;

        String refType = (String) atomData.get("reference_type");
        if (refType != null) {
            return switch (refType) {
                case "sequence"                            -> AtomInfo.KIND_SEQUENCE;
                case "cursor_record", "cursor_record_expr" -> AtomInfo.KIND_CURSOR_RECORD;
                case "routine_variable"                    -> AtomInfo.KIND_VARIABLE;
                case "routine_parameter"                   -> AtomInfo.KIND_PARAMETER;
                default                                    -> AtomInfo.KIND_COLUMN;
            };
        }

        if (Boolean.TRUE.equals(atomData.get("is_column_reference"))) return AtomInfo.KIND_COLUMN;

        String ps = (String) atomData.get("primary_status");
        if (AtomInfo.STATUS_UNRESOLVED.equals(ps)) return AtomInfo.KIND_UNKNOWN;
        return AtomInfo.KIND_UNKNOWN;
    }

    static String deriveConfidence(Map<String, Object> atomData) {
        String ps = (String) atomData.get("primary_status");
        if (ps == null) return AtomInfo.CONFIDENCE_LOW;

        // Unresolved atoms have no confidence
        if (AtomInfo.STATUS_UNRESOLVED.equals(ps)) return null;

        // Constants and system pseudo-columns are always HIGH
        if (AtomInfo.STATUS_CONSTANT.equals(ps)) return AtomInfo.CONFIDENCE_HIGH;

        // RECONSTRUCT_DIRECT: table found, column missing → MEDIUM
        if (AtomInfo.STATUS_RECONSTRUCT_DIRECT.equals(ps)) return AtomInfo.CONFIDENCE_MEDIUM;
        // RECONSTRUCT_INVERSE: reserved for V2, if set → MEDIUM
        if (AtomInfo.STATUS_RECONSTRUCT_INVERSE.equals(ps)) return AtomInfo.CONFIDENCE_MEDIUM;
        // PARTIAL: parent with mixed children → LOW
        if (AtomInfo.STATUS_PARTIAL.equals(ps)) return AtomInfo.CONFIDENCE_LOW;
        // PENDING_INJECT: deferred resolution → null (no confidence yet)
        if (AtomInfo.STATUS_PENDING_INJECT.equals(ps)) return null;

        // Function calls: verified → HIGH, unverified → LOW
        if (AtomInfo.STATUS_FUNCTION_CALL.equals(ps)) {
            String q = (String) atomData.get("qualifier");
            if (AtomInfo.QUALIFIER_FN_VERIFIED.equals(q)) return AtomInfo.CONFIDENCE_HIGH;
            return AtomInfo.CONFIDENCE_LOW;
        }

        // Pass 1: strategy-based mapping for RESOLVED atoms
        String strategy = (String) atomData.get("resolve_strategy");
        String base;
        if (strategy == null) {
            base = AtomInfo.CONFIDENCE_LOW;
        } else {
            base = switch (strategy) {
                case "1_exact_geoid", "2_alias_scope", "2b_table_name_only", "3_cte",
                     "hasVariable", "hasParameter",
                     "source_table_direct", "target_table_direct",
                     "constant", "sequence", "system_pseudo"
                        -> AtomInfo.CONFIDENCE_HIGH;
                case "4_subquery_alias", "5_child_subquery", "6_source_subquery",
                     "7_parent_recursive", "cursorRecordAliases"
                        -> AtomInfo.CONFIDENCE_MEDIUM;
                case "8_global_ddl_fallback", "resolveImplicitTable"
                        -> AtomInfo.CONFIDENCE_LOW;
                case "text_fallback"
                        -> AtomInfo.CONFIDENCE_LOW;
                default -> AtomInfo.CONFIDENCE_LOW;
            };
        }

        // Pass 2: kind-adjustment (ADR-HND-003 §Pass 2)
        String kind = (String) atomData.get("kind");
        if (AtomInfo.KIND_AMBIGUOUS.equals(kind)) {
            base = minConfidence(base, AtomInfo.CONFIDENCE_LOW);
        }

        return base;
    }

    private static String minConfidence(String a, String b) {
        return confidenceRank(a) <= confidenceRank(b) ? a : b;
    }

    private static int confidenceRank(String c) {
        if (c == null) return 0;
        return switch (c) {
            case "FUZZY"  -> 1;
            case "LOW"    -> 2;
            case "MEDIUM" -> 3;
            case "HIGH"   -> 4;
            default       -> 0;
        };
    }

    // ═══════ HAL2-01: PENDING_INJECT detection ═══════

    private String detectPendingKind(Map<String, Object> atomData,
                                     String statementGeoid,
                                     Map<String, Object> resolution) {
        if (builder == null) return null;

        // 1. PIPELINED: atom references a synthetic table from TABLE(fn()) where injection failed
        String tableGeoid = resolution != null ? (String) resolution.get("table_geoid") : null;
        if (tableGeoid == null) tableGeoid = (String) atomData.get("table_geoid");
        if (tableGeoid != null && builder.isPendingPipelined(tableGeoid)) {
            return AtomInfo.PENDING_KIND_PIPELINED;
        }

        // 2. ROWTYPE: atom references a table that was used in %ROWTYPE but had no DDL columns
        if (tableGeoid != null && builder.isPendingRowtype(tableGeoid)) {
            return AtomInfo.PENDING_KIND_ROWTYPE;
        }

        // 3. MULTISET: atom is in a statement containing CAST(MULTISET) with unresolved type
        if (statementGeoid != null && builder.isPendingMultiset(statementGeoid)) {
            return AtomInfo.PENDING_KIND_MULTISET;
        }

        return null;
    }

    private String buildPendingSnapshot(String routineGeoid, String statementGeoid) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"routine_geoid\":\"").append(routineGeoid != null ? routineGeoid : "").append("\"");
        sb.append(",\"statement_geoid\":\"").append(statementGeoid != null ? statementGeoid : "").append("\"");
        sb.append(",\"session_id\":\"").append(file != null ? file : "").append("\"");
        sb.append("}");
        return sb.toString();
    }

    public void clear() {
        atomsByStatement.clear();
        unattachedAtoms.clear();
        resolutionLog.clear();
    }
}
