// src/main/java/com/hound/semantic/model/RoutineInfo.java
package com.hound.semantic.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Информация о процедуре или функции.
 * Порт Python: routine dict в BaseSQLListener.
 */
public class RoutineInfo {

    public record ParameterInfo(String name, String type, String mode) {}
    public record VariableInfo(String name, String type) {}

    private final String geoid;
    private final String name;
    private final String routineType; // PROCEDURE, FUNCTION
    private final String packageGeoid;
    private final String schemaGeoid;
    private final String parentRoutineGeoid; // null for top-level, geoid for nested
    private final int lineStart;             // source line of PROCEDURE/FUNCTION declaration
    private String returnType;
    /** KI-PIPE-1: true if this function contains at least one PIPE ROW statement. */
    private boolean pipelined = false;
    /** KI-PRAGMA-1: true if PRAGMA AUTONOMOUS_TRANSACTION is declared. */
    private boolean autonomousTransaction = false;
    private final List<ParameterInfo> typedParameters = new ArrayList<>();
    private final List<VariableInfo> typedVariables = new ArrayList<>();

    public RoutineInfo(String geoid, String name, String routineType,
                       String packageGeoid, String schemaGeoid) {
        this(geoid, name, routineType, packageGeoid, schemaGeoid, null, 0);
    }

    public RoutineInfo(String geoid, String name, String routineType,
                       String packageGeoid, String schemaGeoid, String parentRoutineGeoid) {
        this(geoid, name, routineType, packageGeoid, schemaGeoid, parentRoutineGeoid, 0);
    }

    public RoutineInfo(String geoid, String name, String routineType,
                       String packageGeoid, String schemaGeoid,
                       String parentRoutineGeoid, int lineStart) {
        this.geoid = geoid;
        this.name = name;
        this.routineType = routineType != null ? routineType : "PROCEDURE";
        this.packageGeoid = packageGeoid;
        this.schemaGeoid = schemaGeoid;
        this.parentRoutineGeoid = parentRoutineGeoid;
        this.lineStart = lineStart;
    }

    public String getGeoid() { return geoid; }
    public String getName() { return name; }
    public String getRoutineType() { return routineType; }
    public String getPackageGeoid() { return packageGeoid; }
    public String getSchemaGeoid() { return schemaGeoid; }
    public String getParentRoutineGeoid() { return parentRoutineGeoid; }
    public int getLineStart() { return lineStart; }
    public String getReturnType() { return returnType; }
    public void setReturnType(String rt) { this.returnType = rt; }
    public boolean isPipelined()                  { return pipelined; }
    public void setPipelined(boolean v)           { this.pipelined = v; }
    public boolean isAutonomousTransaction()       { return autonomousTransaction; }
    public void setAutonomousTransaction(boolean v){ this.autonomousTransaction = v; }

    // ═══════ Parameters ═══════

    public void addTypedParameter(String name, String type, String mode) {
        typedParameters.add(new ParameterInfo(
                name != null ? name.toUpperCase() : "UNKNOWN",
                type != null ? type : "UNKNOWN",
                mode != null ? mode : "IN"));
    }

    public boolean hasParameter(String name) {
        if (name == null) return false;
        String upper = name.toUpperCase();
        return typedParameters.stream().anyMatch(p -> upper.equals(p.name()));
    }

    public List<ParameterInfo> getTypedParameters() { return typedParameters; }

    // ═══════ Variables ═══════

    public void addTypedVariable(String name, String type) {
        typedVariables.add(new VariableInfo(
                name != null ? name.toUpperCase() : "UNKNOWN",
                type != null ? type : "UNKNOWN"));
    }

    public boolean hasVariable(String name) {
        if (name == null) return false;
        String upper = name.toUpperCase();
        return typedVariables.stream().anyMatch(v -> upper.equals(v.name()));
    }

    public List<VariableInfo> getTypedVariables() { return typedVariables; }
}