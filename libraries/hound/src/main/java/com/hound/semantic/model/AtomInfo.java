package com.hound.semantic.model;

public class AtomInfo {
    /** Atom successfully resolved to a physical table/column. */
    public static final String STATUS_RESOLVED   = "Обработано";
    /** Atom classification failed (unknown syntax or type). */
    public static final String STATUS_UNRESOLVED = "Не разобрано";
    /** Atom resolved structurally but not bound to a schema column. */
    public static final String STATUS_UNBOUND    = "Не связано";

    private AtomInfo() {}
}
