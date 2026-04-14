// src/main/java/com/hound/semantic/model/ConstraintInfo.java
package com.hound.semantic.model;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

/**
 * Metadata about a table-level constraint (PRIMARY KEY, FOREIGN KEY, UNIQUE, CHECK).
 *
 * <p>Created by {@link com.hound.semantic.listener.BaseSemanticListener} when parsing
 * {@code ALTER TABLE … ADD CONSTRAINT …} or out_of_line_constraint inside CREATE TABLE.
 *
 * <h3>GEOID scheme</h3>
 * <pre>
 *   {tableGeoid}#{constraintType}[#{discriminator}]
 *
 *   PK → CRM.CUSTOMERS#PK
 *        (PK is unique per table — no discriminator needed)
 *
 *   FK → CRM.CUSTOMER_ADDRESSES#FK#FK_CRM_CADDR_CUST_ID
 *        (discriminator = constraintName.toUpperCase()
 *                      OR md5(fkColumnNames.join(","))[0:8] for unnamed)
 *
 *   UQ → CRM.ORDERS#UQ#UQ_ORDERS_EMAIL
 *   CH → CRM.EMPLOYEES#CH#CHK_EMP_AGE
 * </pre>
 *
 * <h3>Graph model</h3>
 * <pre>
 *   DaliTable  ──HAS_PRIMARY_KEY──►  DaliPrimaryKey (extends DaliConstraint)
 *   DaliTable  ──HAS_FOREIGN_KEY──►  DaliForeignKey (extends DaliConstraint)
 *
 *   DaliPrimaryKey  ──IS_PK_COLUMN────►  DaliColumn  (order_id = 1, 2, …)
 *   DaliForeignKey  ──IS_FK_COLUMN────►  DaliColumn  (order_id = 1, 2, …)
 *   DaliForeignKey  ──REFERENCES_TABLE►  DaliTable   (referenced / parent table)
 *   DaliForeignKey  ──REFERENCES_COLUMN► DaliColumn  (referenced columns, order_id = 1, 2, …)
 * </pre>
 */
public class ConstraintInfo {

    /** Constraint type constants. */
    public static final String TYPE_PK = "PK";
    public static final String TYPE_FK = "FK";
    public static final String TYPE_UQ = "UQ";
    public static final String TYPE_CH = "CH";

    // ─── Common fields ───────────────────────────────────────────────────────

    /** Constraint geoid (see GEOID scheme above). */
    private final String geoid;

    /** "PK" | "FK" | "UQ" | "CH". */
    private final String constraintType;

    /** Declared constraint name (e.g. "FK_CRM_CADDR_CUST_ID"). Null for unnamed constraints. */
    private final String constraintName;

    /** Geoid of the table that owns this constraint. */
    private final String hostTableGeoid;

    /**
     * Ordered column names in the host table.
     * For PK/UQ: the key columns. For FK: the FK-side columns.
     */
    private final List<String> columnNames;

    // ─── FK-specific fields (null for PK / UQ / CH) ──────────────────────────

    /** Geoid of the referenced table (FK only). */
    private final String refTableGeoid;

    /** Ordered column names in the referenced table (FK only). */
    private final List<String> refColumnNames;

    /** ON DELETE action: "CASCADE", "SET NULL", or null (FK only). */
    private final String onDelete;

    /** CHECK expression text (CH constraints only). Mutable — set after construction. */
    private String checkExpression;

    // ─── Constructor ─────────────────────────────────────────────────────────

    public ConstraintInfo(String geoid, String constraintType, String constraintName,
                          String hostTableGeoid, List<String> columnNames,
                          String refTableGeoid, List<String> refColumnNames, String onDelete) {
        this.geoid           = geoid;
        this.constraintType  = constraintType;
        this.constraintName  = constraintName;
        this.hostTableGeoid  = hostTableGeoid;
        this.columnNames     = columnNames    != null ? Collections.unmodifiableList(columnNames)    : List.of();
        this.refTableGeoid   = refTableGeoid;
        this.refColumnNames  = refColumnNames != null ? Collections.unmodifiableList(refColumnNames) : List.of();
        this.onDelete        = onDelete;
    }

    // ─── Getters ─────────────────────────────────────────────────────────────

    public String getGeoid()                 { return geoid; }
    public String getConstraintType()        { return constraintType; }
    public String getConstraintName()        { return constraintName; }
    public String getHostTableGeoid()        { return hostTableGeoid; }
    public List<String> getColumnNames()     { return columnNames; }
    public String getRefTableGeoid()         { return refTableGeoid; }
    public List<String> getRefColumnNames()  { return refColumnNames; }
    public String getOnDelete()              { return onDelete; }
    public String getCheckExpression()       { return checkExpression; }
    public void   setCheckExpression(String expr) { this.checkExpression = expr; }

    public boolean isForeignKey()       { return TYPE_FK.equals(constraintType); }
    public boolean isPrimaryKey()       { return TYPE_PK.equals(constraintType); }
    public boolean isUniqueConstraint() { return TYPE_UQ.equals(constraintType); }
    public boolean isCheckConstraint()  { return TYPE_CH.equals(constraintType); }

    // ─── Static GEOID builder ─────────────────────────────────────────────────

    /**
     * Builds a stable, unique constraint geoid.
     *
     * @param tableGeoid      host table geoid
     * @param constraintType  "PK", "FK", "UQ", or "CH"
     * @param constraintName  declared constraint name; may be null
     * @param columns         ordered column list (used as fallback discriminator for unnamed constraints)
     * @return deterministic geoid string
     */
    public static String buildGeoid(String tableGeoid, String constraintType,
                                    String constraintName, List<String> columns) {
        // PK: only one per table — no discriminator
        if (TYPE_PK.equals(constraintType)) {
            return tableGeoid + "#PK";
        }
        // Others: discriminator = name OR md5(colList)[0:8]
        String disc;
        if (constraintName != null && !constraintName.isBlank()) {
            disc = constraintName.toUpperCase();
        } else {
            disc = md5prefix(columns != null ? String.join(",", columns) : "");
        }
        return tableGeoid + "#" + constraintType + "#" + disc;
    }

    /** Returns first 8 hex chars of MD5 of the input string. */
    private static String md5prefix(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return String.format("%032x", new BigInteger(1, digest)).substring(0, 8).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(input.hashCode()).toUpperCase();
        }
    }
}
