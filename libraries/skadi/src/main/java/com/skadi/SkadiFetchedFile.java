package com.skadi;

import java.time.Instant;
import java.util.Map;

/**
 * Один SQL-объект, извлечённый из источника.
 * Immutable record; sqlText содержит полный DDL.
 */
public record SkadiFetchedFile(

    /** Имя объекта, напр. "GET_EMPLOYEE_SALARY". */
    String name,

    /** Схема / owner, напр. "HR" или "public". null → "default". */
    String schema,

    /** Тип объекта. */
    SkadiFetchConfig.ObjectType objectType,

    /**
     * Полный DDL-текст объекта.
     * Oracle: результат DBMS_METADATA.GET_DDL() (CREATE OR REPLACE ...).
     * PG:     pg_get_functiondef() / CREATE VIEW ... AS ...
     * CH:     SHOW CREATE TABLE / VIEW.
     */
    String sqlText,

    /**
     * Время последнего изменения объекта в источнике.
     * Oracle: ALL_OBJECTS.last_ddl_time.
     * PG:     null в MVP (нет нативного поля).
     * CH:     null (нет нативного поля).
     */
    Instant lastModified,

    /**
     * Адаптер-специфичные метаданные.
     * Oracle: {"object_id": "12345", "status": "VALID"}
     * PG:     {"oid": "67890", "nspname": "public"}
     * CH:     {"database": "default", "engine": "View"}
     */
    Map<String, String> metadata

) {

    /**
     * Предлагаемое имя файла для логов и SqlSource.sourceName().
     * Формат: {schema}__{name}.{type}.sql (lower-case, sanitized)
     *
     * Пример: "hr__get_employee_salary.procedure.sql"
     */
    public String suggestedFilename() {
        String safeSchema = schema != null
                ? schema.toLowerCase().replaceAll("[^a-z0-9_]", "_")
                : "default";
        String safeName = name.toLowerCase().replaceAll("[^a-z0-9_]", "_");
        String ext = objectType.name().toLowerCase();
        return safeSchema + "__" + safeName + "." + ext + ".sql";
    }
}
