// File: src/main/java/com/hound/semantic/engine/NameResolver.java
package com.hound.semantic.engine;

import com.hound.diagnostic.ResolutionLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * NameResolver — портирование логики разрешения имён из Python BaseSQLListener.
 *
 * 8 стратегий разрешения (в порядке приоритета):
 * 1. Таблица по точному geoid (schema.table)
 * 2. Таблица по алиасу в текущем scope
 * 3. CTE по имени в иерархии statements
 * 4. Подзапрос по алиасу
 * 5. Child subqueries
 * 6. Source subqueries
 * 7. Рекурсивный поиск по parent statements
 * 8. Неявная таблица (единственная в FROM)
 */
public class NameResolver {

    private static final Logger logger = LoggerFactory.getLogger(NameResolver.class);

    private final Map<String, ResolvedRef> resolutionCache = new HashMap<>();
    private int cacheHits = 0;
    private int cacheMisses = 0;

    // Внешние зависимости — устанавливаются через wire()
    private StructureAndLineageBuilder builder;
    private ScopeManager scopeManager;

    // Диагностика (STAB-3): null в prod-режиме — no-op
    private ResolutionLogger resolutionLogger;

    /**
     * Подключает зависимости. Вызывается из UniversalSemanticEngine при инициализации.
     */
    public void wire(StructureAndLineageBuilder builder, ScopeManager scopeManager) {
        this.builder = builder;
        this.scopeManager = scopeManager;
    }

    /** STAB-3: подключает диагностический логгер (только в --diag режиме) */
    public void setResolutionLogger(ResolutionLogger rl) {
        this.resolutionLogger = rl;
    }

    /**
     * Основной метод разрешения имени.
     * Аналог Python: _resolve_name_reference(name, context_type, parent_stmt, table_alias, only_local_context)
     *
     * @param name               имя для разрешения (таблица, алиас, CTE, подзапрос)
     * @param contextType        "any", "table", "subquery"
     * @param currentStatementGeoid текущий statement
     * @return ResolvedRef с geoid или isResolved()=false
     */
    public ResolvedRef resolve(String name, String contextType, String currentStatementGeoid) {
        if (name == null || name.isBlank()) {
            return ResolvedRef.unresolved("");
        }

        String cacheKey = name.toUpperCase() + "|" + contextType + "|" + currentStatementGeoid;
        ResolvedRef cached = resolutionCache.get(cacheKey);
        if (cached != null) {
            cacheHits++;
            return cached;
        }
        cacheMisses++;

        ResolvedRef result = resolveInternal(name.toUpperCase(), contextType, currentStatementGeoid);

        if (result.isResolved()) {
            resolutionCache.put(cacheKey, result);
        }

        logger.debug("NameResolver: '{}' → {} [{}] (cache: {}/{})",
                name, result.isResolved() ? result.getType() + ":" + result.getGeoid() : "NOT FOUND",
                result.getStrategy(),
                cacheHits, cacheHits + cacheMisses);

        // STAB-3: диагностическое логирование (только в --diag режиме)
        if (resolutionLogger != null && resolutionLogger.isEnabled()) {
            resolutionLogger.log(
                ResolutionLogger.InputKind.ATOM,
                name,
                currentStatementGeoid,
                result.isResolved() ? result.getStrategy() : "all_failed",
                result.isResolved()
                    ? ResolutionLogger.ResultKind.RESOLVED
                    : ResolutionLogger.ResultKind.UNRESOLVED,
                result.isResolved() ? result.getGeoid() : null,
                result.isResolved() ? result.getType()  : null,
                null
            );
        }

        return result;
    }

    /**
     * Разрешение неявной таблицы — когда колонка без префикса и в scope единственная таблица.
     * Аналог Python: _resolve_implicit_table_reference(current_stmt)
     */
    public ResolvedRef resolveImplicitTable(String currentStatementGeoid) {
        return resolveImplicitTableInternal(currentStatementGeoid);
    }

    // ═══════════════════════════════════════════════════════════════
    // Внутренняя логика: 8 стратегий
    // ═══════════════════════════════════════════════════════════════

    private ResolvedRef resolveInternal(String upperName, String contextType, String currentStatementGeoid) {

        // Стратегии для таблиц
        if ("any".equals(contextType) || "table".equals(contextType)) {

            // Стратегия 1: Таблица по точному geoid (schema.table или просто table)
            ResolvedRef byName = resolveTableByExactGeoid(upperName);
            if (byName.isResolved()) return tag(byName, "1_exact_geoid");

            // Стратегия 2: Таблица по алиасу в текущем scope
            ResolvedRef byAlias = resolveTableByAliasInScope(upperName, currentStatementGeoid);
            if (byAlias.isResolved()) return tag(byAlias, "2_alias_scope");

            // Стратегия 2b: Поиск по имени таблицы (без учёта схемы)
            ResolvedRef byTableName = resolveTableByNameOnly(upperName);
            if (byTableName.isResolved()) return tag(byTableName, "2b_table_name_only");
        }

        // Стратегии для подзапросов
        if ("any".equals(contextType) || "subquery".equals(contextType)) {

            // Стратегия 3: CTE по имени
            ResolvedRef cte = resolveCTE(upperName, currentStatementGeoid);
            if (cte.isResolved()) return tag(cte, "3_cte");

            // Стратегия 4: Подзапрос по алиасу в текущем statement
            ResolvedRef subqAlias = resolveSubqueryByAlias(upperName, currentStatementGeoid);
            if (subqAlias.isResolved()) return tag(subqAlias, "4_subquery_alias");

            // Стратегия 5: Child subqueries текущего statement
            ResolvedRef childSubq = resolveChildSubqueries(upperName, currentStatementGeoid);
            if (childSubq.isResolved()) return tag(childSubq, "5_child_subquery");

            // Стратегия 6: Source subqueries текущего statement
            ResolvedRef srcSubq = resolveSourceSubqueries(upperName, currentStatementGeoid);
            if (srcSubq.isResolved()) return tag(srcSubq, "6_source_subquery");
        }

        // Стратегия 7: Рекурсивный поиск по parent statements
        ResolvedRef parentRec = resolveParentRecursive(upperName, contextType, currentStatementGeoid, 0);
        if (parentRec.isResolved()) return tag(parentRec, "7_parent_recursive");

        return ResolvedRef.unresolved(upperName);
    }

    // ═══════════════════════════════════════════════════════════════
    // Стратегия 1: Таблица по точному geoid
    // Аналог Python: if full_geoid in self.tables
    // ═══════════════════════════════════════════════════════════════

    private ResolvedRef resolveTableByExactGeoid(String name) {
        if (builder == null) return ResolvedRef.unresolved(name);

        // Проверяем полный geoid (schema.table или table)
        var tables = builder.getTables();
        if (tables.containsKey(name)) {
            var t = tables.get(name);
            return new ResolvedRef(t.tableName(), "TABLE", name);
        }

        // Если имя содержит точку — пробуем schema.table
        if (name.contains(".")) {
            String[] parts = name.split("\\.");
            String tableName = parts[parts.length - 1];
            String schemaName = parts[parts.length - 2];

            // Поиск по данным таблицы (schema_geoid + table_name)
            for (var entry : tables.entrySet()) {
                var t = entry.getValue();
                String tSchema = t.schemaGeoid() != null ? t.schemaGeoid().toUpperCase() : "";
                String tName = t.tableName() != null ? t.tableName().toUpperCase() : "";
                if (tSchema.equals(schemaName) && tName.equals(tableName)) {
                    return new ResolvedRef(t.tableName(), "TABLE", entry.getKey());
                }
            }
        }

        return ResolvedRef.unresolved(name);
    }

    // ═══════════════════════════════════════════════════════════════
    // Стратегия 2: Таблица по алиасу в текущем scope
    // Аналог Python: поиск по aliases в source_tables/target_tables statement
    // ═══════════════════════════════════════════════════════════════

    private ResolvedRef resolveTableByAliasInScope(String alias, String currentStatementGeoid) {
        // a) Проверяем ScopeManager (текущий контекст)
        if (scopeManager != null) {
            String geoid = scopeManager.resolveAlias(alias);
            if (geoid != null) {
                return new ResolvedRef(alias, "TABLE", geoid);
            }
        }

        // b) Проверяем aliases в зарегистрированных таблицах
        if (builder != null) {
            for (var entry : builder.getTables().entrySet()) {
                var t = entry.getValue();
                if (t.aliases() != null && t.aliases().contains(alias)) {
                    return new ResolvedRef(alias, "TABLE", entry.getKey());
                }
            }
        }

        return ResolvedRef.unresolved(alias);
    }

    // ═══════════════════════════════════════════════════════════════
    // Стратегия 2b: Таблица по имени (без схемы) — одно совпадение
    // Аналог Python: matching_tables = [t for t in self.tables if t.table_name == name]
    // ═══════════════════════════════════════════════════════════════

    private ResolvedRef resolveTableByNameOnly(String name) {
        if (builder == null) return ResolvedRef.unresolved(name);

        List<Map.Entry<String, com.hound.semantic.model.TableInfo>> matches = new ArrayList<>();
        for (var entry : builder.getTables().entrySet()) {
            var t = entry.getValue();
            if (t.tableName() != null && t.tableName().toUpperCase().equals(name)) {
                matches.add(entry);
            }
        }

        if (matches.size() == 1) {
            var match = matches.get(0);
            return new ResolvedRef(match.getValue().tableName(), "TABLE", match.getKey());
        }

        return ResolvedRef.unresolved(name);
    }

    // ═══════════════════════════════════════════════════════════════
    // Стратегия 3: CTE по имени
    // Аналог Python: has_cte_with_name(parent_stmt, clean_name)
    // ═══════════════════════════════════════════════════════════════

    private ResolvedRef resolveCTE(String name, String currentStatementGeoid) {
        if (builder == null) return ResolvedRef.unresolved(name);

        // Ищем root statement
        String rootStmt = findRootStatement(currentStatementGeoid);

        var statements = builder.getStatements();
        for (var entry : statements.entrySet()) {
            var stmt = entry.getValue();
            if (!"CTE".equals(stmt.getType())) continue;

            // CTE должен принадлежать текущей иерархии
            if (rootStmt != null && !entry.getKey().startsWith(rootStmt)) continue;

            // Проверяем по alias или short_name
            String stmtAlias = stmt.getAlias();
            String stmtShortName = stmt.getShortName();
            if (name.equals(toUpper(stmtAlias)) || name.equals(toUpper(stmtShortName))
                    || name.equals(entry.getKey().toUpperCase())) {
                return new ResolvedRef(name, "CTE", entry.getKey());
            }
        }

        return ResolvedRef.unresolved(name);
    }

    // ═══════════════════════════════════════════════════════════════
    // Стратегия 4: Подзапрос по алиасу
    // ═══════════════════════════════════════════════════════════════

    private ResolvedRef resolveSubqueryByAlias(String alias, String currentStatementGeoid) {
        if (builder == null || currentStatementGeoid == null) return ResolvedRef.unresolved(alias);

        var statements = builder.getStatements();
        var parentStmt = statements.get(currentStatementGeoid);
        if (parentStmt == null) return ResolvedRef.unresolved(alias);

        // Проверяем child_statements
        for (String childGeoid : parentStmt.getChildStatements()) {
            var childStmt = statements.get(childGeoid);
            if (childStmt == null) continue;

            Set<String> childAliases = childStmt.getAliases();
            String childShortName = childStmt.getShortName();
            if (childAliases.contains(alias) || alias.equals(toUpper(childShortName))
                    || alias.equals(childGeoid.toUpperCase())) {
                return new ResolvedRef(alias, "SUBQUERY", childGeoid);
            }
        }

        return ResolvedRef.unresolved(alias);
    }

    // ═══════════════════════════════════════════════════════════════
    // Стратегия 5: Child subqueries
    // ═══════════════════════════════════════════════════════════════

    private ResolvedRef resolveChildSubqueries(String name, String currentStatementGeoid) {
        // Аналогично стратегии 4, но проверяет по имени, а не только алиасу
        if (builder == null || currentStatementGeoid == null) return ResolvedRef.unresolved(name);

        var statements = builder.getStatements();
        var parentStmt = statements.get(currentStatementGeoid);
        if (parentStmt == null) return ResolvedRef.unresolved(name);

        for (String childGeoid : parentStmt.getChildStatements()) {
            // Use colon-delimited segment match to avoid false positives:
            // name "X" must not match geoid "PARENT:MAX:10" via raw substring.
            String upper   = childGeoid.toUpperCase();
            String segment = ":" + name.toUpperCase() + ":";
            String suffix  = ":" + name.toUpperCase();
            if (upper.equals(name.toUpperCase()) || upper.contains(segment) || upper.endsWith(suffix)) {
                return new ResolvedRef(name, "SUBQUERY", childGeoid);
            }
        }

        return ResolvedRef.unresolved(name);
    }

    // ═══════════════════════════════════════════════════════════════
    // Стратегия 6: Source subqueries
    // Аналог Python: self.statements[parent_stmt].get('source_subqueries', {})
    // ═══════════════════════════════════════════════════════════════

    private ResolvedRef resolveSourceSubqueries(String name, String currentStatementGeoid) {
        if (builder == null || currentStatementGeoid == null) return ResolvedRef.unresolved(name);

        var statements = builder.getStatements();
        var parentStmt = statements.get(currentStatementGeoid);
        if (parentStmt == null) return ResolvedRef.unresolved(name);

        for (var entry : parentStmt.getSourceSubqueries().entrySet()) {
            String subqGeoid = entry.getKey();
            var subqInfo = entry.getValue();

            // Проверяем по алиасам
            List<String> subqAliases = subqInfo.subqueryAliases();
            if (subqAliases != null) {
                for (String a : subqAliases) {
                    if (name.equals(toUpper(a))) {
                        // Resolve к реальному statement
                        String resolvedGeoid = subqInfo.subqueryStmt() != null ? subqInfo.subqueryStmt() : subqGeoid;
                        return new ResolvedRef(name, "SUBQUERY", resolvedGeoid);
                    }
                }
            }

            // Проверяем по geoid
            if (name.equals(subqGeoid.toUpperCase())) {
                return new ResolvedRef(name, "SUBQUERY", subqGeoid);
            }
        }

        return ResolvedRef.unresolved(name);
    }

    // ═══════════════════════════════════════════════════════════════
    // Стратегия 7: Рекурсивный поиск по parent statements
    // Аналог Python: return self._resolve_subquery_reference(clean_name, parent_statement)
    // ═══════════════════════════════════════════════════════════════

    private static final int MAX_RECURSION_DEPTH = 20;

    private ResolvedRef resolveParentRecursive(String name, String contextType,
                                               String currentStatementGeoid, int depth) {
        if (depth > MAX_RECURSION_DEPTH || currentStatementGeoid == null || builder == null) {
            return ResolvedRef.unresolved(name);
        }

        var statements = builder.getStatements();
        var stmt = statements.get(currentStatementGeoid);
        if (stmt == null) return ResolvedRef.unresolved(name);

        String parentGeoid = stmt.getParentStatementGeoid();
        if (parentGeoid == null) return ResolvedRef.unresolved(name);

        // Пробуем разрешить в контексте parent statement (без рекурсии стратегии 7)
        // — стратегии 1-6
        ResolvedRef result = resolveInParentContext(name, contextType, parentGeoid);
        if (result.isResolved()) return result;

        // Рекурсия вверх
        return resolveParentRecursive(name, contextType, parentGeoid, depth + 1);
    }

    private ResolvedRef resolveInParentContext(String name, String contextType, String parentGeoid) {
        if ("any".equals(contextType) || "table".equals(contextType)) {
            // Проверяем source/target tables parent statement
            var parentStmt = builder.getStatements().get(parentGeoid);
            if (parentStmt != null) {
                // Ищем в source_tables
                for (String tableGeoid : parentStmt.getSourceTableGeoids()) {
                    var t = builder.getTables().get(tableGeoid);
                    if (t != null) {
                        if (name.equals(toUpper(t.tableName())) || (t.aliases() != null && t.aliases().contains(name))) {
                            return new ResolvedRef(name, "TABLE", tableGeoid);
                        }
                    }
                }
                // Ищем в target_tables
                for (String tableGeoid : parentStmt.getTargetTableGeoids()) {
                    var t = builder.getTables().get(tableGeoid);
                    if (t != null) {
                        if (name.equals(toUpper(t.tableName())) || (t.aliases() != null && t.aliases().contains(name))) {
                            return new ResolvedRef(name, "TABLE", tableGeoid);
                        }
                    }
                }
            }
        }

        if ("any".equals(contextType) || "subquery".equals(contextType)) {
            ResolvedRef cte = resolveCTE(name, parentGeoid);
            if (cte.isResolved()) return cte;

            ResolvedRef subq = resolveSubqueryByAlias(name, parentGeoid);
            if (subq.isResolved()) return subq;

            ResolvedRef srcSubq = resolveSourceSubqueries(name, parentGeoid);
            if (srcSubq.isResolved()) return srcSubq;
        }

        return ResolvedRef.unresolved(name);
    }

    // ═══════════════════════════════════════════════════════════════
    // Стратегия 8: Неявная таблица
    // Аналог Python: _resolve_implicit_table_reference(current_stmt)
    // ═══════════════════════════════════════════════════════════════

    private ResolvedRef resolveImplicitTableInternal(String currentStatementGeoid) {
        if (builder == null || currentStatementGeoid == null) return ResolvedRef.unresolved("implicit");

        var statements = builder.getStatements();
        var stmt = statements.get(currentStatementGeoid);
        if (stmt == null) return ResolvedRef.unresolved("implicit");

        List<String> sourceTables = stmt.getSourceTableGeoids();
        List<String> sourceSubqueries = new ArrayList<>(stmt.getSourceSubqueries().keySet());
        List<String> targetTables = stmt.getTargetTableGeoids();

        // Единственный source
        if (sourceTables.size() == 1 && sourceSubqueries.isEmpty()) {
            return new ResolvedRef("implicit", "TABLE", sourceTables.get(0));
        }

        // Единственный source subquery
        if (sourceSubqueries.size() == 1 && sourceTables.isEmpty()) {
            return new ResolvedRef("implicit", "SUBQUERY", sourceSubqueries.get(0));
        }

        // Единственный target (для UPDATE/DELETE)
        if (targetTables.size() == 1 && sourceTables.isEmpty() && sourceSubqueries.isEmpty()) {
            return new ResolvedRef("implicit", "TABLE", targetTables.get(0));
        }

        // Нет источников — пробуем parent statement
        int totalSources = sourceTables.size() + sourceSubqueries.size() + targetTables.size();
        if (totalSources == 0) {
            String parentGeoid = stmt.getParentStatementGeoid();
            if (parentGeoid != null) {
                return resolveImplicitTableInternal(parentGeoid);
            }
        }

        // Для UPDATE/DELETE — предпочитаем target
        String stmtType = stmt.getType();
        if (("UPDATE".equals(stmtType) || "DELETE".equals(stmtType)) && !targetTables.isEmpty()) {
            return new ResolvedRef("implicit", "TABLE", targetTables.get(0));
        }

        return ResolvedRef.unresolved("implicit");
    }

    // ═══════════════════════════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════════════════════════

    /**
     * Находит root statement в иерархии (верхний parent).
     * Аналог Python: _find_root_statement(statement_name)
     */
    private String findRootStatement(String statementGeoid) {
        if (builder == null || statementGeoid == null) return statementGeoid;

        var statements = builder.getStatements();
        String current = statementGeoid;
        int depth = 0;

        while (depth < MAX_RECURSION_DEPTH) {
            var stmt = statements.get(current);
            if (stmt == null) return current;

            String parent = stmt.getParentStatementGeoid();
            if (parent == null) return current;

            current = parent;
            depth++;
        }

        return current;
    }

    /** STAB-3: оборачивает ResolvedRef в новый с проставленной strategy */
    private static ResolvedRef tag(ResolvedRef r, String strategy) {
        return new ResolvedRef(r.getName(), r.getType(), r.getGeoid(), strategy);
    }

    private static String toUpper(String s) {
        return s != null ? s.toUpperCase() : null;
    }

    public void clearCache() {
        resolutionCache.clear();
        cacheHits = 0;
        cacheMisses = 0;
    }
}