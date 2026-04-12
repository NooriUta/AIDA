// src/main/java/com/hound/semantic/model/RecordInfo.java
package com.hound.semantic.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * PL/SQL коллекция/запись, наполняемая через BULK COLLECT INTO.
 *
 * Геоид: routineGeoid + ":RECORD:" + varName  (всегда upper-case)
 *
 * Связи (создаются RemoteWriter):
 *   DaliStatement(cursor SELECT) ─── BULK_COLLECTS_INTO ──► DaliRecord
 *   DaliRecord                   ─── RECORD_USED_IN      ──► DaliStatement(INSERT)
 */
public class RecordInfo {

    private final String geoid;
    /** Имя переменной коллекции, например L_TAB */
    private final String varName;
    /** Geoid routine-владельца */
    private final String routineGeoid;
    /** Ordered field names из курсорного SELECT (positional) */
    private final List<String> fields = new ArrayList<>();
    /** Geoid cursor-SELECT стейтмента, из которого BULK COLLECT заполняет эту запись */
    private String sourceStatementGeoid;

    public RecordInfo(String geoid, String varName, String routineGeoid) {
        this.geoid = geoid;
        this.varName = varName;
        this.routineGeoid = routineGeoid;
    }

    // ── Builders ────────────────────────────────────────────────────────────

    public void addField(String fieldName) {
        if (fieldName != null) fields.add(fieldName.toUpperCase());
    }

    public void setSourceStatementGeoid(String stmtGeoid) {
        this.sourceStatementGeoid = stmtGeoid;
    }

    // ── Getters ─────────────────────────────────────────────────────────────

    public String getGeoid()               { return geoid; }
    public String getVarName()             { return varName; }
    public String getRoutineGeoid()        { return routineGeoid; }
    public String getSourceStatementGeoid(){ return sourceStatementGeoid; }
    public List<String> getFields()        { return Collections.unmodifiableList(fields); }
}
