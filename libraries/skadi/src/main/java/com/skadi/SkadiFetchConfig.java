package com.skadi;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

/**
 * Параметры соединения и фильтрации для одного вызова fetchScripts().
 * Immutable record — безопасен для параллельного использования.
 */
public record SkadiFetchConfig(

    /**
     * JDBC URL или HTTP URL (ClickHouse).
     * Oracle:  "jdbc:oracle:thin:@localhost:1521/XEPDB1"
     * PG:      "jdbc:postgresql://localhost:5432/aida_src"
     * CH HTTP: "http://localhost:8123"
     */
    String jdbcUrl,

    /** Пользователь БД. */
    String user,

    /** Пароль БД. Никогда не логируется — см. toSafeString(). */
    String password,

    /**
     * Schema / owner для выборки.
     * Oracle: имя owner'а (напр. "HR").
     * PG:     имя схемы (напр. "public").
     * CH:     имя database (напр. "default").
     * null = все доступные схемы.
     */
    String schema,

    /**
     * Типы объектов для сбора.
     * Пустое множество = все типы (эквивалент Set.of(ObjectType.values())).
     */
    Set<ObjectType> objectTypes,

    /**
     * Инкрементальный harvest: только объекты, изменённые после этого момента.
     * null = полный harvest.
     */
    Instant modifiedSince,

    /**
     * Максимальное количество объектов за один вызов.
     * 0 = без ограничения.
     */
    int limit,

    /**
     * Адаптер-специфичные параметры.
     * CH: "database" → имя БД.
     */
    Map<String, String> extra

) {

    /** Поддерживаемые типы SQL-объектов. */
    public enum ObjectType {
        PROCEDURE,
        FUNCTION,
        /** Oracle package spec. */
        PACKAGE,
        /** Oracle package body. */
        PACKAGE_BODY,
        VIEW,
        MATERIALIZED_VIEW,
        TABLE,
        TRIGGER
    }

    /** Полный harvest — все типы объектов, без временного фильтра. */
    public static SkadiFetchConfig fullHarvest(
            String jdbcUrl, String user, String password, String schema) {
        return new SkadiFetchConfig(
                jdbcUrl, user, password, schema,
                Set.of(ObjectType.values()), null, 0, Map.of());
    }

    /** Инкрементальный harvest — только объекты, изменённые после modifiedSince. */
    public static SkadiFetchConfig incrementalHarvest(
            String jdbcUrl, String user, String password,
            String schema, Instant modifiedSince) {
        return new SkadiFetchConfig(
                jdbcUrl, user, password, schema,
                Set.of(ObjectType.values()), modifiedSince, 0, Map.of());
    }

    /** Harvest конкретных типов объектов. */
    public static SkadiFetchConfig selectiveHarvest(
            String jdbcUrl, String user, String password,
            String schema, ObjectType... types) {
        return new SkadiFetchConfig(
                jdbcUrl, user, password, schema,
                Set.of(types), null, 0, Map.of());
    }

    /** Безопасное строковое представление — БЕЗ пароля. */
    public String toSafeString() {
        return "SkadiFetchConfig{url=%s, user=%s, schema=%s, types=%s, since=%s}"
                .formatted(jdbcUrl, user, schema, objectTypes, modifiedSince);
    }
}
