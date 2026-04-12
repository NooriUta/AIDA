package com.hound.semantic.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Информация об использовании подзапроса как источника в statement.
 * Аналог Python: statements[stmt]['source_subqueries'][geoid]
 */
public record SubqueryUsage(
        String subqueryStmt,
        List<String> subqueryAliases,
        String subqueryType
) {
    public SubqueryUsage {
        if (subqueryAliases == null) subqueryAliases = new ArrayList<>();
    }

    public SubqueryUsage(String subqueryStmt, String alias, String type) {
        this(subqueryStmt,
                alias != null ? new ArrayList<>(List.of(alias)) : new ArrayList<>(),
                type != null ? type : "SUBQUERY");
    }

    public void addAlias(String alias) {
        if (alias != null && !subqueryAliases.contains(alias)) {
            subqueryAliases.add(alias);
        }
    }
}