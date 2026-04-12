package com.hound.semantic;

import com.arcadedb.database.Database;
import com.arcadedb.database.DatabaseFactory;
import com.arcadedb.query.sql.executor.Result;
import com.arcadedb.query.sql.executor.ResultSet;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

/**
 * Прямой замер качества атомов по DaliResolutionLog в embedded ArcadeDB.
 * Запуск: ./gradlew test --tests "*AtomQualityDbTest*" --info
 */
class AtomQualityDbTest {

    private static final String DB_PATH = "data/hound-db";

    @Test
    void qualityFromResolutionLog() {
        Path dbPath = Path.of(DB_PATH);
        DatabaseFactory factory = new DatabaseFactory(dbPath.toAbsolutePath().toString());

        if (!factory.exists()) {
            System.out.println("[SKIP] БД не найдена: " + dbPath.toAbsolutePath());
            return;
        }

        try (Database db = factory.open()) {
            System.out.println("\n══════════════════════════════════════════════");
            System.out.println("  ATOM QUALITY — DaliResolutionLog");
            System.out.println("══════════════════════════════════════════════");

            // Определяем источник данных: DaliResolutionLog (S1+) или DaliAtom.status (pre-S1)
            boolean hasResLog = db.getSchema().existsType("DaliResolutionLog");
            System.out.println("  Источник: " + (hasResLog ? "DaliResolutionLog" : "DaliAtom.status"));

            // 1. Разбивка по status/result_kind — count manually to avoid ArcadeDB alias cast issues
            long total = 0, resolved = 0, unresolved = 0, constants = 0, functions = 0;
            Map<String, Long> counts = new java.util.LinkedHashMap<>();

            String scanSql = hasResLog
                    ? "SELECT result_kind FROM DaliResolutionLog"
                    : "SELECT status FROM DaliAtom";
            String kindProp = hasResLog ? "result_kind" : "status";

            try (ResultSet rs = db.query("SQL", scanSql)) {
                while (rs.hasNext()) {
                    Object val = rs.next().<Object>getProperty(kindProp);
                    String kind = val != null ? val.toString() : "null";
                    counts.merge(kind, 1L, Long::sum);
                }
            }

            System.out.printf("%-20s %8s%n", "status", "count");
            System.out.println("-".repeat(32));
            // sort by count desc
            counts.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .forEach(e -> System.out.printf("%-20s %8d%n", e.getKey(), e.getValue()));

            for (Map.Entry<String, Long> e : counts.entrySet()) {
                String kind = e.getKey();
                long cnt = e.getValue();
                total += cnt;
                switch (kind) {
                    case "Обработано"    -> resolved   += cnt;
                    case "constant"      -> constants  += cnt;
                    case "function_call" -> functions  += cnt;
                    default              -> unresolved += cnt;  // "unresolved" + legacy error messages
                }
            }

            System.out.println("-".repeat(32));
            System.out.printf("%-20s %8d%n", "TOTAL", total);
            if (total > 0) {
                double quality   = (resolved + constants + functions) / (double) total * 100;
                double unresPct  = unresolved / (double) total * 100;
                System.out.println();
                System.out.printf("  Resolved    : %6d  (%5.1f%%)%n", resolved,  resolved  / (double) total * 100);
                System.out.printf("  Constants   : %6d  (%5.1f%%)%n", constants, constants / (double) total * 100);
                System.out.printf("  Functions   : %6d  (%5.1f%%)%n", functions, functions / (double) total * 100);
                System.out.printf("  Unresolved  : %6d  (%5.1f%%)%n", unresolved, unresPct);
                System.out.println();
                System.out.printf("  ► QUALITY   : %.1f%%  (не разобрано: %.1f%%)%n", quality, unresPct);
            }

            // 2. Топ-15 нерешённых atom_text (только если есть DaliAtom)
            System.out.println("\n── Топ-15 нерешённых atom_text ─────────────────");
            System.out.printf("%-40s %-15s%n", "atom_text", "parent_context");
            System.out.println("-".repeat(58));
            Map<String, Long> unresCounts = new java.util.LinkedHashMap<>();
            Map<String, String> unresCtx = new java.util.LinkedHashMap<>();
            try (ResultSet rs = db.query("SQL",
                    "SELECT atom_text, parent_context FROM DaliAtom " +
                    "WHERE status <> 'Обработано' AND status <> 'constant' AND status <> 'function_call'")) {
                while (rs.hasNext()) {
                    Result r = rs.next();
                    Object atV  = r.<Object>getProperty("atom_text");
                    Object ctxV = r.<Object>getProperty("parent_context");
                    String at  = atV  != null ? atV.toString()  : "null";
                    String ctx = ctxV != null ? ctxV.toString() : "null";
                    String key = at + "\0" + ctx;
                    unresCounts.merge(key, 1L, Long::sum);
                    unresCtx.putIfAbsent(key, ctx);
                }
            }
            unresCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(15)
                    .forEach(e -> {
                        String key = e.getKey();
                        String at  = key.substring(0, key.indexOf('\0'));
                        String ctx = unresCtx.get(key);
                        System.out.printf("%-40s %-15s  %6d%n",
                                trunc(at, 40), trunc(ctx, 15), e.getValue());
                    });
        }
    }

    /**
     * Детальный разбор нерешённых атомов: группировка по причине + примеры с routine/statement_type.
     * Запуск: ./gradlew test --tests "*AtomQualityDbTest.debugUnresolved*" --info
     */
    @Test
    void debugUnresolved() {
        Path dbPath = Path.of(DB_PATH);
        DatabaseFactory factory = new DatabaseFactory(dbPath.toAbsolutePath().toString());
        if (!factory.exists()) { System.out.println("[SKIP] БД не найдена"); return; }

        try (Database db = factory.open()) {
            System.out.println("\n══════════════════════════════════════════════════════════════════");
            System.out.println("  UNRESOLVED ATOMS — detailed breakdown");
            System.out.println("══════════════════════════════════════════════════════════════════");

            // ── 1. Разбивка по причине (status message prefix) ──────────────────
            Map<String, Long> byCause = new java.util.LinkedHashMap<>();
            try (ResultSet rs = db.query("SQL",
                    "SELECT status FROM DaliAtom " +
                    "WHERE status <> 'Обработано' AND status <> 'constant' AND status <> 'function_call'")) {
                while (rs.hasNext()) {
                    Object v = rs.next().<Object>getProperty("status");
                    String raw = v != null ? v.toString() : "null";
                    // Normalize: strip atom value to get the pattern
                    String cause = raw.replaceAll("'[^']*'", "<LITERAL>")
                                      .replaceAll(":\\w+", "<BIND>")
                                      .replaceAll("\\b[A-Z_][A-Z0-9_]*\\b", "<ID>")
                                      .trim();
                    byCause.merge(cause, 1L, Long::sum);
                }
            }
            System.out.println("\n── По причине ──────────────────────────────────────────────────────");
            byCause.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .forEach(e -> System.out.printf("%6d  %s%n", e.getValue(), e.getKey()));

            // ── 2. Разбивка по parent_context × statement_type ──────────────────
            Map<String, Long> byCtxType = new java.util.LinkedHashMap<>();
            Map<String, String> ctxTypeExample = new java.util.LinkedHashMap<>();
            try (ResultSet rs = db.query("SQL",
                    "SELECT atom_text, parent_context, statement_type FROM DaliAtom " +
                    "WHERE status <> 'Обработано' AND status <> 'constant' AND status <> 'function_call'")) {
                while (rs.hasNext()) {
                    Result r = rs.next();
                    Object ctxV  = r.<Object>getProperty("parent_context");
                    Object typeV = r.<Object>getProperty("statement_type");
                    Object atV   = r.<Object>getProperty("atom_text");
                    String ctx   = ctxV  != null ? ctxV.toString()  : "(null)";
                    String type  = typeV != null ? typeV.toString() : "(null)";
                    String at    = atV   != null ? atV.toString()   : "(null)";
                    String key   = ctx + "  |  stmt=" + type;
                    byCtxType.merge(key, 1L, Long::sum);
                    ctxTypeExample.putIfAbsent(key, at);
                }
            }
            System.out.println("\n── По parent_context × statement_type ──────────────────────────────");
            System.out.printf("%-6s  %-45s  %s%n", "cnt", "parent_ctx | stmt_type", "example atom");
            System.out.println("-".repeat(95));
            byCtxType.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .forEach(e -> System.out.printf("%6d  %-45s  %s%n",
                            e.getValue(), trunc(e.getKey(), 45), trunc(ctxTypeExample.get(e.getKey()), 35)));

            // ── 3. Примеры с routine_name ────────────────────────────────────────
            System.out.println("\n── 30 примеров (atom_text, parent_ctx, stmt_type, routine) ─────────");
            System.out.printf("%-30s %-12s %-18s %-25s%n",
                    "atom_text", "parent_ctx", "stmt_type", "routine");
            System.out.println("-".repeat(90));
            int[] cnt = {0};
            try (ResultSet rs = db.query("SQL",
                    "SELECT atom_text, parent_context, statement_type, routine_name FROM DaliAtom " +
                    "WHERE status <> 'Обработано' AND status <> 'constant' AND status <> 'function_call' " +
                    "LIMIT 30")) {
                while (rs.hasNext()) {
                    Result r = rs.next();
                    String at    = str(r, "atom_text");
                    String ctx   = str(r, "parent_context");
                    String type  = str(r, "statement_type");
                    String rout  = str(r, "routine_name");
                    System.out.printf("%-30s %-12s %-18s %-25s%n",
                            trunc(at, 30), trunc(ctx, 12), trunc(type, 18), trunc(rout, 25));
                    cnt[0]++;
                }
            }
            System.out.println("(" + cnt[0] + " rows)");
        }
    }

    private static String str(Result r, String prop) {
        Object v = r.<Object>getProperty(prop);
        return v != null ? v.toString() : "(null)";
    }

    private static String trunc(String s, int max) {
        if (s == null || s.equals("null")) return "(null)";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
