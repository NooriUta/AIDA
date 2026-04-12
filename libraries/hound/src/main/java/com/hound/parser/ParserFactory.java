package com.hound.parser;

import com.hound.parser.registry.ParserRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Фабрика парсеров с поддержкой принудительного указания диалекта.
 */
public class ParserFactory {

    private static final Logger logger = LoggerFactory.getLogger(ParserFactory.class);

    /**
     * Основной метод получения парсера.
     */
    public static LanguageParser getParser(String fileName, String forcedLanguage) {
        String language = (forcedLanguage != null && !forcedLanguage.isBlank())
                ? forcedLanguage.trim().toLowerCase()
                : detectLanguage(fileName);

        logger.debug("File '{}' → language: {} (forced: {})",
                fileName, language.toUpperCase(), forcedLanguage);

        return ParserRegistry.getParser(language);
    }

    /**
     * Перегруженный метод для обратной совместимости
     */
    public static LanguageParser getParser(String fileName) {
        return getParser(fileName, null);
    }

    /**
     * Публичный метод определения языка по имени файла.
     * Используется из FileProcessor и других классов.
     */
    public static String detectLanguage(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "sql";
        }

        String lower = fileName.toLowerCase();

        // По расширению
        if (lower.endsWith(".plsql") || lower.endsWith(".pks") || lower.endsWith(".pkb")) {
            return "plsql";
        }

        // По содержимому имени файла
        if (lower.contains("plsql") || lower.contains("oracle") || lower.contains("package")) {
            return "plsql";
        }
        if (lower.contains("athena")) return "athena";
        if (lower.contains("hive")) return "hive";
        if (lower.contains("postgresql") || lower.contains("postgres")) return "postgresql";
        if (lower.contains("tsql") || lower.contains("mssql") || lower.contains("sqlserver")) return "tsql";
        if (lower.contains("starrocks")) return "starrocks";
        if (lower.contains("trino")) return "trino";
        if (lower.contains("clickhouse")) return "clickhouse";
        if (lower.contains("databricks")) return "databricks";
        if (lower.contains("snowflake")) return "snowflake";
        if (lower.contains("mariadb")) return "mariadb";
        if (lower.contains("db2")) return "db2";

        return "sql";
    }

    /**
     * Получить парсер по имени языка напрямую
     */
    public static LanguageParser getParserForLanguage(String language) {
        return ParserRegistry.getParser(language);
    }

    public static void registerParser(String language, LanguageParser parser) {
        ParserRegistry.register(language, parser);
        logger.info("Registered parser for language: {}", language.toUpperCase());
    }
}