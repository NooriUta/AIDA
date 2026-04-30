package com.hound.semantic.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * PL/SQL user-defined TYPE template — either a RECORD or a COLLECTION (TABLE OF).
 *
 * Geoid: scopeGeoid + ":TYPE:" + name.toUpperCase()
 *   e.g. "DWH.PKG_JOURNAL:TYPE:T_JOURNAL_STG_REC"
 *
 * Graph edges (created by RemoteWriter / JsonlBatchBuilder):
 *   DaliPackage/Routine ─── DECLARES_TYPE   ──► DaliPlType
 *   DaliPlType(COLL)    ─── OF_TYPE         ──► DaliPlType(RECORD)
 *   DaliRecord          ─── INSTANTIATES_TYPE──► DaliPlType
 */
public class PlTypeInfo {

    public enum Kind { RECORD, COLLECTION }

    private final String name;           // type name upper-case, e.g. "T_JOURNAL_STG_REC"
    private final Kind   kind;
    private final String scopeGeoid;     // declaring package or routine geoid
    private final String geoid;          // derived: scopeGeoid + ":TYPE:" + name
    private int          declaredAtLine;

    /** For COLLECTION: the element type name (resolved lazily by engine). */
    private String elementTypeName;
    /** For COLLECTION: geoid of the RECORD PlTypeInfo it wraps (resolved by engine). */
    private String elementTypeGeoid;

    private final List<PlTypeFieldInfo> fields = new ArrayList<>();

    public PlTypeInfo(String name, Kind kind, String scopeGeoid) {
        this.name       = name.toUpperCase();
        this.kind       = kind;
        this.scopeGeoid = scopeGeoid;
        this.geoid      = scopeGeoid + ":TYPE:" + this.name;
    }

    // ── Builders ─────────────────────────────────────────────────────────────

    public void addField(String fieldName, String dataType, int position) {
        if (fieldName != null)
            fields.add(new PlTypeFieldInfo(fieldName.toUpperCase(), dataType, position));
    }

    public void setElementTypeName(String name) {
        this.elementTypeName = name != null ? name.toUpperCase() : null;
    }

    public void setElementTypeGeoid(String geoid) {
        this.elementTypeGeoid = geoid;
    }

    public void setDeclaredAtLine(int line) {
        this.declaredAtLine = line;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getName()              { return name; }
    public Kind   getKind()              { return kind; }
    public String getScopeGeoid()        { return scopeGeoid; }
    public String getGeoid()             { return geoid; }
    public int    getDeclaredAtLine()    { return declaredAtLine; }
    public String getElementTypeName()   { return elementTypeName; }
    public String getElementTypeGeoid()  { return elementTypeGeoid; }

    public List<PlTypeFieldInfo> getFields() {
        return Collections.unmodifiableList(fields);
    }

    public boolean isRecord()     { return kind == Kind.RECORD; }
    public boolean isCollection() { return kind == Kind.COLLECTION; }
}
