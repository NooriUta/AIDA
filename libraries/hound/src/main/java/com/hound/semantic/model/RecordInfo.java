// src/main/java/com/hound/semantic/model/RecordInfo.java
package com.hound.semantic.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * PL/SQL коллекция/запись, наполняемая через BULK COLLECT INTO.
 *
 * Геоид: routineGeoid + ":RECORD:" + varName  (всегда upper-case)
 *
 * Связи (создаются RemoteWriter):
 *   DaliStatement(cursor SELECT) ─── BULK_COLLECTS_INTO ──► DaliRecord
 *   DaliRecord                   ─── HAS_RECORD_FIELD   ──► DaliRecordField
 *   DaliRecord                   ─── RECORD_USED_IN     ──► DaliStatement(INSERT)
 */
public class RecordInfo {

    /**
     * KI-RETURN-1: Rich field metadata.
     * Replaces bare String fields list; backward-compat getFields() is derived.
     */
    public record FieldInfo(
        String name,               // always UPPER_CASE
        String dataType,           // nullable — DDL-declared or %TYPE-resolved type
        int ordinalPosition,       // 1-based
        String sourceColumnGeoid   // nullable — populated for %TYPE / %ROWTYPE fields
    ) {}

    private final String geoid;
    /** Имя переменной коллекции, например L_TAB */
    private final String varName;
    /** Geoid routine-владельца */
    private final String routineGeoid;
    /** Ordered field info (replaces old List<String> fields) */
    private final List<FieldInfo> fieldInfos = new ArrayList<>();
    /** Geoid cursor-SELECT стейтмента, из которого BULK COLLECT заполняет эту запись */
    private String sourceStatementGeoid;
    /** HND-02: geoid of the PlTypeInfo template this record was instantiated from (nullable) */
    private String plTypeGeoid;

    public RecordInfo(String geoid, String varName, String routineGeoid) {
        this.geoid = geoid;
        this.varName = varName;
        this.routineGeoid = routineGeoid;
    }

    // ── Builders ────────────────────────────────────────────────────────────

    /** Backward-compat: adds a field with name only (no type/geoid). */
    public void addField(String fieldName) {
        if (fieldName != null) {
            int ordinal = fieldInfos.size() + 1;
            fieldInfos.add(new FieldInfo(fieldName.toUpperCase(), null, ordinal, null));
        }
    }

    /** KI-RETURN-1 / KI-ROWTYPE-1: adds a field with full metadata. */
    public void addField(String name, String dataType, int ordinal, String sourceColumnGeoid) {
        if (name != null)
            fieldInfos.add(new FieldInfo(name.toUpperCase(), dataType, ordinal, sourceColumnGeoid));
    }

    public void setSourceStatementGeoid(String stmtGeoid) {
        this.sourceStatementGeoid = stmtGeoid;
    }

    public void setPlTypeGeoid(String geoid) { this.plTypeGeoid = geoid; }

    // ── Getters ─────────────────────────────────────────────────────────────

    public String getGeoid()               { return geoid; }
    public String getVarName()             { return varName; }
    public String getRoutineGeoid()        { return routineGeoid; }
    public String getSourceStatementGeoid(){ return sourceStatementGeoid; }
    public String getPlTypeGeoid()         { return plTypeGeoid; }

    /** Returns ordered field names (backward compat). */
    public List<String> getFields() {
        return fieldInfos.stream().map(FieldInfo::name).collect(Collectors.toList());
    }

    /** KI-RETURN-1: Returns ordered FieldInfo list with full metadata. */
    public List<FieldInfo> getFieldInfos() { return Collections.unmodifiableList(fieldInfos); }
}
