package com.mimir.quota;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads provider/model price snapshot from {@code llm-prices.json} (versioned
 * in repo). {@link #estimate} returns USD cost for a (provider, model,
 * promptTokens, completionTokens) tuple.
 *
 * <p>Unknown provider/model pair → returns 0 with WARN log so an unconfigured
 * model never accidentally bills the tenant. The list of known models is
 * exposed via {@link #knownModels()} for admin UIs.
 */
@ApplicationScoped
public class LlmPriceBook {

    private static final Logger LOG = Logger.getLogger(LlmPriceBook.class);
    private static final String PRICES_RESOURCE = "llm-prices.json";

    @Inject ObjectMapper mapper;

    private Map<String, ModelPrice> prices = Map.of();

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ModelPrice(
            String provider,
            String model,
            double inputPricePer1k,
            double outputPricePer1k
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record PriceFile(String version, List<ModelPrice> prices) {}

    void onStart(@Observes StartupEvent ev) {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(PRICES_RESOURCE)) {
            if (in == null) {
                LOG.warnf("Price book resource not found: %s", PRICES_RESOURCE);
                return;
            }
            PriceFile f = mapper.readValue(in, PriceFile.class);
            Map<String, ModelPrice> map = new HashMap<>();
            for (ModelPrice p : f.prices()) {
                map.put(key(p.provider(), p.model()), p);
            }
            this.prices = Map.copyOf(map);
            LOG.infof("LlmPriceBook loaded version=%s entries=%d", f.version(), prices.size());
        } catch (Exception e) {
            LOG.errorf(e, "Failed to load price book — all estimates will be 0 USD");
        }
    }

    public double estimate(String provider, String model, long promptTokens, long completionTokens) {
        ModelPrice p = lookup(provider, model);
        if (p == null) {
            LOG.debugf("No price for provider=%s model=%s — billing 0 USD", provider, model);
            return 0.0;
        }
        return p.inputPricePer1k() * promptTokens / 1000.0
             + p.outputPricePer1k() * completionTokens / 1000.0;
    }

    public ModelPrice lookup(String provider, String model) {
        if (provider == null || model == null) return null;
        return prices.get(key(provider, model));
    }

    public int size() {
        return prices.size();
    }

    public java.util.Set<String> knownModels() {
        return prices.keySet();
    }

    private static String key(String provider, String model) {
        return provider.toLowerCase() + "|" + model;
    }
}
