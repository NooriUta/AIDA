package com.hound.semantic.dialect;

import com.hound.exception.HoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Реестр Dialect Adapters.
 * Аналог Python AnalyzerFactory + Hound ParserRegistry для семантики.
 *
 * Регистрация при старте:
 *   DialectRegistry.register("plsql", new PlSqlDialectAdapter());
 *
 * Использование:
 *   DialectAdapter adapter = DialectRegistry.getAdapter("plsql");
 *   ParseTreeListener listener = adapter.createListener(engine);
 */
public final class DialectRegistry {

    private static final Logger logger = LoggerFactory.getLogger(DialectRegistry.class);
    private static final Map<String, DialectAdapter> adapters = new ConcurrentHashMap<>();

    // Static initializer — регистрируем известные диалекты
    static {
        try {
            // PL/SQL — основной диалект
            Class<?> plsqlClass = Class.forName("com.hound.semantic.dialect.plsql.PlSqlDialectAdapter");
            register("plsql", (DialectAdapter) plsqlClass.getDeclaredConstructor().newInstance());
            logger.info("Registered dialect adapter: plsql");
        } catch (Exception e) {
            logger.warn("Could not register PlSqlDialectAdapter: {}", e.getMessage());
        }

        // PostgreSQL — будущее
        // try {
        //     Class<?> pgClass = Class.forName("com.hound.semantic.dialect.pgsql.PgSqlDialectAdapter");
        //     register("pgsql", (DialectAdapter) pgClass.getDeclaredConstructor().newInstance());
        // } catch (Exception e) { ... }
    }

    /**
     * Регистрирует адаптер для диалекта.
     */
    public static void register(String dialect, DialectAdapter adapter) {
        adapters.put(dialect.toLowerCase(), adapter);
        logger.info("Dialect adapter registered: {} → {}", dialect, adapter.getClass().getSimpleName());
    }

    /**
     * Возвращает адаптер для диалекта.
     * @throws HoundException если адаптер не найден
     */
    public static DialectAdapter getAdapter(String dialect) {
        if (dialect == null || dialect.isBlank()) {
            throw new HoundException("Dialect name is null or blank",
                    HoundException.ErrorCode.UNSUPPORTED_LANGUAGE);
        }

        DialectAdapter adapter = adapters.get(dialect.toLowerCase());
        if (adapter == null) {
            throw new HoundException("No dialect adapter registered for: " + dialect
                    + ". Available: " + adapters.keySet(),
                    HoundException.ErrorCode.UNSUPPORTED_LANGUAGE);
        }
        return adapter;
    }

    /**
     * Проверяет, есть ли адаптер для диалекта.
     */
    public static boolean hasAdapter(String dialect) {
        return dialect != null && adapters.containsKey(dialect.toLowerCase());
    }

    /**
     * Возвращает список зарегистрированных диалектов.
     */
    public static java.util.Set<String> getRegisteredDialects() {
        return java.util.Collections.unmodifiableSet(adapters.keySet());
    }
}