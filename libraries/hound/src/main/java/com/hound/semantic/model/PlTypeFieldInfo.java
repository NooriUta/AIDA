package com.hound.semantic.model;

/**
 * One field of a PL/SQL TYPE IS RECORD definition.
 * Immutable — created once when the TYPE declaration is parsed.
 */
public record PlTypeFieldInfo(
    String name,      // field name, always UPPER_CASE
    String dataType,  // declared type, e.g. "NUMBER(19)", "VARCHAR2(255)"
    int position      // 1-based declaration order
) {}
