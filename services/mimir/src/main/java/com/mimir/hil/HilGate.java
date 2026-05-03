package com.mimir.hil;

import com.mimir.model.AskRequest;
import com.mimir.quota.LlmPriceBook;
import com.mimir.quota.TenantUsageTracker;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Pre-LLM gate: pauses a request when a destructive keyword is found in the
 * question, when the estimated cost exceeds the configured ceiling, or when
 * the tenant has {@code mimir.hil.always-for-tenants} listing them.
 *
 * <p>Configuration (application.properties):
 * <pre>
 * mimir.hil.enabled=true
 * mimir.hil.max-cost-without-approval-usd=0.50
 * mimir.hil.destructive-keywords=DROP TABLE,DELETE FROM,TRUNCATE,UPDATE *
 * mimir.hil.always-for-tenants=    # CSV of tenant aliases requiring approval on every call
 * </pre>
 *
 * <p>Per-tenant overrides via FRIGG (DaliTenantConfig.hilPolicy) are out of
 * scope here — added in TIER2.5 once Heimdall-FE provides the admin UI.
 */
@ApplicationScoped
public class HilGate {

    private static final Logger LOG = Logger.getLogger(HilGate.class);

    @Inject LlmPriceBook prices;

    @ConfigProperty(name = "mimir.hil.enabled", defaultValue = "false")
    boolean enabled;

    @ConfigProperty(name = "mimir.hil.max-cost-without-approval-usd", defaultValue = "0.50")
    double maxCostWithoutApproval;

    @ConfigProperty(name = "mimir.hil.destructive-keywords",
            defaultValue = "DROP TABLE,DELETE FROM,TRUNCATE,UPDATE *")
    String destructiveKeywordsCsv;

    @ConfigProperty(name = "mimir.hil.always-for-tenants")
    java.util.Optional<String> alwaysForTenantsCsvOpt;

    private String alwaysForTenantsCsv() {
        return alwaysForTenantsCsvOpt == null ? null : alwaysForTenantsCsvOpt.orElse(null);
    }

    @ConfigProperty(name = "mimir.hil.estimate-provider", defaultValue = "anthropic")
    String estimateProvider;

    @ConfigProperty(name = "mimir.hil.estimate-model", defaultValue = "claude-sonnet-4-6")
    String estimateModel;

    public HilDecision check(AskRequest request, String tenantAlias) {
        if (!enabled) return HilDecision.allow();
        String question = request == null ? null : request.question();

        if (containsDestructive(question)) {
            double cost = estimateCost(question);
            LOG.warnf("HIL pause: destructive keyword in tenant=%s question", tenantAlias);
            return HilDecision.require("destructive_op", cost);
        }

        if (alwaysForTenant(tenantAlias)) {
            double cost = estimateCost(question);
            LOG.warnf("HIL pause: tenant policy mandates approval (tenant=%s)", tenantAlias);
            return HilDecision.require("tenant_policy", cost);
        }

        double cost = estimateCost(question);
        if (cost > maxCostWithoutApproval) {
            LOG.warnf("HIL pause: estimated cost $%.4f > %.2f for tenant=%s",
                    cost, maxCostWithoutApproval, tenantAlias);
            return HilDecision.require("high_cost", cost);
        }
        return HilDecision.allow();
    }

    boolean containsDestructive(String question) {
        if (question == null || question.isBlank()) return false;
        String upper = question.toUpperCase(Locale.ROOT);
        for (String kw : destructiveKeywords()) {
            if (kw.isBlank()) continue;
            if (upper.contains(kw.toUpperCase(Locale.ROOT))) return true;
        }
        return false;
    }

    boolean alwaysForTenant(String tenantAlias) {
        if (tenantAlias == null || tenantAlias.isBlank()) return false;
        String csv = alwaysForTenantsCsv();
        if (csv == null || csv.isBlank()) return false;
        for (String t : csv.split(",")) {
            if (tenantAlias.equalsIgnoreCase(t.trim())) return true;
        }
        return false;
    }

    /**
     * Coarse pre-call cost estimate: assume completion ≈ prompt length.
     * Right-of-the-decimal precision is good enough for a hard threshold.
     */
    double estimateCost(String question) {
        long promptTokens = TenantUsageTracker.estimateTokens(question);
        long completionTokens = promptTokens; // assume similar length response
        return prices.estimate(estimateProvider, estimateModel, promptTokens, completionTokens);
    }

    private List<String> destructiveKeywords() {
        if (destructiveKeywordsCsv == null || destructiveKeywordsCsv.isBlank()) return List.of();
        return List.of(destructiveKeywordsCsv.split(","));
    }

    Set<String> destructiveKeywordsForTest() {
        return Set.copyOf(destructiveKeywords());
    }
}
