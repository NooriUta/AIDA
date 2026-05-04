package com.hound.semantic.model;

import java.util.*;

/**
 * Информация о таблице.
 * Аналог Python: self.tables[geoid] dict.
 */
public class TableInfo {

    private final String geoid;
    private final String tableName;
    private final String schemaGeoid;
    private String tableType;  // TABLE, VIEW, CTE, TEMP, VTABLE
    private final Set<String> aliases = new LinkedHashSet<>();
    private int columnCount = 0;
    /** HND-04: for VTABLE entries — geoid of the DaliPlType (COLLECTION) that backs this virtual table. */
    private String plTypeGeoid;

    public TableInfo(String geoid, String tableName, String schemaGeoid, String tableType) {
        this.geoid = geoid;
        this.tableName = tableName;
        this.schemaGeoid = schemaGeoid;
        this.tableType = tableType != null ? tableType : "TABLE";
    }

    public String geoid() { return geoid; }
    public String tableName() { return tableName; }
    public String schemaGeoid() { return schemaGeoid; }
    public String tableType() { return tableType; }
    public void setTableType(String type) { if (type != null) this.tableType = type; }
    public Set<String> aliases() { return aliases; }
    public int columnCount() { return columnCount; }

    public String getPlTypeGeoid()            { return plTypeGeoid; }
    public void setPlTypeGeoid(String v)      { this.plTypeGeoid = v; }

    public void addAlias(String alias) {
        if (alias != null) aliases.add(alias.toUpperCase());
    }

    public void incrementColumnCount() { columnCount++; }
}
 