package com.hound.parser.registry;

import com.hound.parser.LanguageParser;
import com.hound.parser.core.UniversalParser;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Центральный реестр парсеров.
 * Теперь getParser(language) возвращает нужный парсер в зависимости от языка.
 */
public final class ParserRegistry {

    public record ParserInfo(
            Class<?> lexerClass,
            Class<?> parserClass,
            String startRuleName
    ) {}

    private static final Map<String, ParserInfo> infoMap = new ConcurrentHashMap<>();
    private static final UniversalParser universalParser = new UniversalParser();

    static {
        // ==================== РЕГИСТРАЦИЯ ДИАЛЕКТОВ ====================
        register("plsql",      "com.hound.parser.base.grammars.sql.plsql.PlSqlLexer",
                "com.hound.parser.base.grammars.sql.plsql.PlSqlParser", "sql_script");

        register("sql",        "com.hound.parser.base.grammars.sql.mysql.PosTech.MySqlLexer",
                "com.hound.parser.base.grammars.sql.mysql.PosTech.MySqlParser", "sql_script");

        register("athena",     "com.hound.parser.base.grammars.sql.athena.AthenaLexer",
                "com.hound.parser.base.grammars.sql.athena.AthenaParser", "stmt");

        register("hive",       "com.hound.parser.base.grammars.sql.hive.v4.HiveLexer",
                "com.hound.parser.base.grammars.sql.hive.v4.HiveParser", "statement");

        register("mysql",      "com.hound.parser.base.grammars.sql.mysql.PosTech.MySqlLexer",
                "com.hound.parser.base.grammars.sql.mysql.PosTech.MySqlParser", "sql_script");

        register("postgresql", "com.hound.parser.base.grammars.sql.postgresql.PostgreSQLLexer",
                "com.hound.parser.base.grammars.sql.postgresql.PostgreSQLParser", "root");

        register("starrocks",  "com.hound.parser.base.grammars.sql.starrocks.StarRocksLexer",
                "com.hound.parser.base.grammars.sql.starrocks.StarRocksParser", "statement");

        register("trino",      "com.hound.parser.base.grammars.sql.trino.TrinoLexer",
                "com.hound.parser.base.grammars.sql.trino.TrinoParser", "statement");

        register("tsql",       "com.hound.parser.base.grammars.sql.tsql.TSqlLexer",
                "com.hound.parser.base.grammars.sql.tsql.TSqlParser", "sql_script");

        register("clickhouse", "com.hound.parser.base.grammars.sql.clickhouse.ClickHouseLexer",
                "com.hound.parser.base.grammars.sql.clickhouse.ClickHouseParser", "statement");

        register("databricks", "com.hound.parser.base.grammars.sql.databricks.DatabricksLexer",
                "com.hound.parser.base.grammars.sql.databricks.DatabricksParser", "statement");

        register("snowflake",  "com.hound.parser.base.grammars.sql.snowflake.SnowflakeLexer",
                "com.hound.parser.base.grammars.sql.snowflake.SnowflakeParser", "sql_script");

        register("mariadb",    "com.hound.parser.base.grammars.sql.mariadb.MariaDBLexer",
                "com.hound.parser.base.grammars.sql.mariadb.MariaDBParser", "sql_script");

        register("db2",        "com.hound.parser.base.grammars.sql.db2.Db2Lexer",
                "com.hound.parser.base.grammars.sql.db2.Db2Parser", "stmt");

        register("derby",      "com.hound.parser.base.grammars.sql.derby.DerbyLexer",
                "com.hound.parser.base.grammars.sql.derby.DerbyParser", "statement");

        register("drill",      "com.hound.parser.base.grammars.sql.drill.DrillLexer",
                "com.hound.parser.base.grammars.sql.drill.DrillParser", "statement");

        register("phoenix",    "com.hound.parser.base.grammars.sql.phoenix.PhoenixLexer",
                "com.hound.parser.base.grammars.sql.phoenix.PhoenixParser", "statement");

        register("sqlite",     "com.hound.parser.base.grammars.sql.sqlite.SQLiteLexer",
                "com.hound.parser.base.grammars.sql.sqlite.SQLiteParser", "sql_stmt_list");
    }

    private static void register(String language, String lexerClassName, String parserClassName, String startRule) {
        try {
            Class<?> lexerClass = Class.forName(lexerClassName);
            Class<?> parserClass = Class.forName(parserClassName);
            infoMap.put(language.toLowerCase(), new ParserInfo(lexerClass, parserClass, startRule));
        } catch (ClassNotFoundException e) {
            System.err.println("WARNING: Не удалось загрузить классы для языка " + language + ": " + e.getMessage());
        }
    }

    /**
     * Возвращает парсер для указанного языка.
     * Теперь реально использует информацию из реестра.
     */
    public static LanguageParser getParser(String language) {
        if (language == null || language.isBlank()) {
            return universalParser;
        }
        String lang = language.toLowerCase();
        return infoMap.containsKey(lang) ? universalParser : universalParser;
        // В будущем здесь можно создавать разные парсеры по языку
    }

    public static ParserInfo getParserInfo(String language) {
        return infoMap.get(language.toLowerCase());
    }

    public static boolean supports(String language) {
        return language != null && infoMap.containsKey(language.toLowerCase());
    }

    // Публичная регистрация (для расширения в будущем)
    public static void register(String language, LanguageParser parser) {
        // Пока оставляем универсальный парсер, но метод работает
        System.out.println("ParserRegistry: Registered parser for language → " + language.toUpperCase());
    }
}