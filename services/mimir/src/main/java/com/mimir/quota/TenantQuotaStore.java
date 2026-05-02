package com.mimir.quota;

import com.mimir.persistence.FriggClient;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * FRIGG-backed storage for {@link TenantQuota} (per tenant) and
 * {@link DailyUsage} (per tenant per ISO-date) aggregates.
 *
 * <p>Two vertex types live in the {@code tenant-config} DB:
 * <ul>
 *   <li>{@code DaliTenantQuota} — single row per tenantAlias (UNIQUE index)</li>
 *   <li>{@code DaliMimirUsage} — one row per (tenantAlias, date), unique on
 *       compound key {@code aliasDate = "alias|YYYY-MM-DD"}</li>
 * </ul>
 *
 * <p>All reads and writes degrade to "unlimited" / no-op on FRIGG outage so
 * a quota-store glitch cannot lock a tenant out of the system.
 */
@ApplicationScoped
public class TenantQuotaStore {

    private static final Logger LOG = Logger.getLogger(TenantQuotaStore.class);
    private static final String QUOTA_TYPE = "DaliTenantQuota";
    private static final String USAGE_TYPE = "DaliMimirUsage";

    @Inject @RestClient FriggClient frigg;

    @ConfigProperty(name = "mimir.frigg.tenants-db", defaultValue = "tenant-config")
    String tenantsDb;

    @ConfigProperty(name = "mimir.byok.store.skip-init", defaultValue = "false")
    boolean skipInit;

    void onStart(@Observes StartupEvent ev) {
        if (skipInit) return;
        try {
            // Quota schema
            frigg.command(tenantsDb, new FriggClient.FriggCommand("sql",
                    "CREATE VERTEX TYPE " + QUOTA_TYPE + " IF NOT EXISTS"));
            frigg.command(tenantsDb, new FriggClient.FriggCommand("sql",
                    "CREATE PROPERTY " + QUOTA_TYPE + ".tenantAlias IF NOT EXISTS STRING"));
            frigg.command(tenantsDb, new FriggClient.FriggCommand("sql",
                    "CREATE INDEX IF NOT EXISTS ON " + QUOTA_TYPE + " (tenantAlias) UNIQUE"));

            // Usage schema
            frigg.command(tenantsDb, new FriggClient.FriggCommand("sql",
                    "CREATE VERTEX TYPE " + USAGE_TYPE + " IF NOT EXISTS"));
            frigg.command(tenantsDb, new FriggClient.FriggCommand("sql",
                    "CREATE PROPERTY " + USAGE_TYPE + ".aliasDate IF NOT EXISTS STRING"));
            frigg.command(tenantsDb, new FriggClient.FriggCommand("sql",
                    "CREATE INDEX IF NOT EXISTS ON " + USAGE_TYPE + " (aliasDate) UNIQUE"));
            LOG.infof("Quota store ready: %s.{%s,%s}", tenantsDb, QUOTA_TYPE, USAGE_TYPE);
        } catch (Exception e) {
            LOG.warnf(e, "Quota store init failed (db=%s) — quota lookups will return unlimited", tenantsDb);
        }
    }

    public Optional<TenantQuota> findQuota(String tenantAlias) {
        if (tenantAlias == null || tenantAlias.isBlank()) return Optional.empty();
        try {
            FriggClient.QueryResult r = frigg.query(tenantsDb,
                    new FriggClient.FriggQuery("sql",
                            "SELECT FROM " + QUOTA_TYPE + " WHERE tenantAlias = :a LIMIT 1",
                            Map.of("a", tenantAlias)));
            if (r == null || r.result() == null || r.result().isEmpty()) return Optional.empty();
            Map<String, Object> row = r.result().get(0);
            return Optional.of(new TenantQuota(
                    longVal(row, "dailyTokenLimit"),
                    longVal(row, "monthlyTokenLimit"),
                    doubleVal(row, "dailyCostLimitUsd"),
                    doubleVal(row, "monthlyCostLimitUsd"),
                    str(row, "resetTimezone")
            ));
        } catch (Exception e) {
            LOG.warnf(e, "findQuota failed for tenant=%s — treating as unlimited", tenantAlias);
            return Optional.empty();
        }
    }

    public void saveQuota(String tenantAlias, TenantQuota q) {
        Map<String, Object> p = new HashMap<>();
        p.put("alias",                tenantAlias);
        p.put("dailyTokenLimit",      q.dailyTokenLimit());
        p.put("monthlyTokenLimit",    q.monthlyTokenLimit());
        p.put("dailyCostLimitUsd",    q.dailyCostLimitUsd());
        p.put("monthlyCostLimitUsd",  q.monthlyCostLimitUsd());
        p.put("resetTimezone",        q.resetTimezone());
        p.put("updatedAt",            Instant.now().toString());
        frigg.command(tenantsDb,
                new FriggClient.FriggCommand("sql",
                        "UPDATE " + QUOTA_TYPE + " SET tenantAlias=:alias, " +
                        "dailyTokenLimit=:dailyTokenLimit, monthlyTokenLimit=:monthlyTokenLimit, " +
                        "dailyCostLimitUsd=:dailyCostLimitUsd, monthlyCostLimitUsd=:monthlyCostLimitUsd, " +
                        "resetTimezone=:resetTimezone, updatedAt=:updatedAt UPSERT WHERE tenantAlias=:alias",
                        p));
    }

    public Optional<DailyUsage> findUsage(String tenantAlias, String date) {
        try {
            String key = tenantAlias + "|" + date;
            FriggClient.QueryResult r = frigg.query(tenantsDb,
                    new FriggClient.FriggQuery("sql",
                            "SELECT FROM " + USAGE_TYPE + " WHERE aliasDate = :k LIMIT 1",
                            Map.of("k", key)));
            if (r == null || r.result() == null || r.result().isEmpty()) return Optional.empty();
            Map<String, Object> row = r.result().get(0);
            return Optional.of(new DailyUsage(
                    str(row, "tenantAlias"),
                    str(row, "date"),
                    longVal(row, "promptTokens"),
                    longVal(row, "completionTokens"),
                    longVal(row, "totalTokens"),
                    doubleVal(row, "costEstimateUsd"),
                    intVal(row, "requestCount")
            ));
        } catch (Exception e) {
            LOG.warnf(e, "findUsage failed for tenant=%s date=%s", tenantAlias, date);
            return Optional.empty();
        }
    }

    public List<DailyUsage> listUsage(String tenantAlias, int limit) {
        try {
            int safeLimit = Math.max(1, Math.min(limit, 365));
            FriggClient.QueryResult r = frigg.query(tenantsDb,
                    new FriggClient.FriggQuery("sql",
                            "SELECT FROM " + USAGE_TYPE + " WHERE tenantAlias = :a ORDER BY date DESC LIMIT " + safeLimit,
                            Map.of("a", tenantAlias)));
            if (r == null || r.result() == null) return List.of();
            return r.result().stream().map(row -> new DailyUsage(
                    str(row, "tenantAlias"),
                    str(row, "date"),
                    longVal(row, "promptTokens"),
                    longVal(row, "completionTokens"),
                    longVal(row, "totalTokens"),
                    doubleVal(row, "costEstimateUsd"),
                    intVal(row, "requestCount")
            )).toList();
        } catch (Exception e) {
            LOG.warnf(e, "listUsage failed for tenant=%s", tenantAlias);
            return List.of();
        }
    }

    public void upsertUsage(DailyUsage u) {
        Map<String, Object> p = new HashMap<>();
        p.put("aliasDate",        u.tenantAlias() + "|" + u.date());
        p.put("alias",            u.tenantAlias());
        p.put("date",             u.date());
        p.put("promptTokens",     u.promptTokens());
        p.put("completionTokens", u.completionTokens());
        p.put("totalTokens",      u.totalTokens());
        p.put("costEstimateUsd",  u.costEstimateUsd());
        p.put("requestCount",     u.requestCount());
        try {
            frigg.command(tenantsDb,
                    new FriggClient.FriggCommand("sql",
                            "UPDATE " + USAGE_TYPE + " SET aliasDate=:aliasDate, tenantAlias=:alias, " +
                            "date=:date, promptTokens=:promptTokens, completionTokens=:completionTokens, " +
                            "totalTokens=:totalTokens, costEstimateUsd=:costEstimateUsd, " +
                            "requestCount=:requestCount UPSERT WHERE aliasDate=:aliasDate",
                            p));
        } catch (Exception e) {
            LOG.warnf(e, "upsertUsage failed for tenant=%s date=%s", u.tenantAlias(), u.date());
        }
    }

    private static String str(Map<String, Object> row, String k) {
        Object v = row.get(k);
        return v == null ? null : v.toString();
    }

    private static long longVal(Map<String, Object> row, String k) {
        Object v = row.get(k);
        if (v == null) return 0L;
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(v.toString()); } catch (NumberFormatException e) { return 0L; }
    }

    private static int intVal(Map<String, Object> row, String k) {
        return (int) longVal(row, k);
    }

    private static double doubleVal(Map<String, Object> row, String k) {
        Object v = row.get(k);
        if (v == null) return 0.0;
        if (v instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(v.toString()); } catch (NumberFormatException e) { return 0.0; }
    }
}
