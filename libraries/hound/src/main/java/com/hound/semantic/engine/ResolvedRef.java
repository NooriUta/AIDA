package com.hound.semantic.engine;

/**
 * Результат одной попытки resolution имени.
 *
 * strategy — идентификатор стратегии которая дала результат:
 *   "1_exact_geoid", "2a_scope_context", "2b_table_aliases",
 *   "3_cte", "4_subquery_alias", "5_child_subquery",
 *   "6_source_subquery", "7_parent_recursive", "8_implicit_table"
 *
 * STAB-3: добавлено поле strategy для диагностики ResolutionLogger.
 */
public class ResolvedRef {

    private final String name;
    private final String type;      // TABLE, SUBQUERY, CTE, ALIAS, UNKNOWN
    private final String geoid;
    private final String strategy;  // см. комментарий выше; null если unresolved

    public ResolvedRef(String name, String type, String geoid) {
        this(name, type, geoid, null);
    }

    public ResolvedRef(String name, String type, String geoid, String strategy) {
        this.name     = name != null ? name : "";
        this.type     = type != null ? type : "UNKNOWN";
        this.geoid    = geoid;
        this.strategy = strategy;
    }

    public static ResolvedRef unresolved(String name) {
        return new ResolvedRef(name, "UNKNOWN", null, null);
    }

    public String getName()     { return name; }
    public String getType()     { return type; }
    public String getGeoid()    { return geoid; }
    public String getStrategy() { return strategy; }

    public boolean isTable()    { return "TABLE".equals(type); }
    public boolean isSubquery() { return "SUBQUERY".equals(type); }
    public boolean isCTE()      { return "CTE".equals(type); }
    public boolean isResolved() { return geoid != null; }

    @Override
    public String toString() {
        return "ResolvedRef{name='" + name + "', type='" + type
                + "', geoid='" + geoid + "', strategy='" + strategy + "'}";
    }
}
