package com.mimir.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mimir.model.MimirAnswer;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * Tier-3 demo cache (TIER2 MT-02) — fixture-backed fallback / forced demo mode.
 *
 * <p>Two activation modes:
 * <ul>
 *   <li><b>Forced</b> ({@code mimir.demo-mode=true}) — orchestrator skips DeepSeek and answers
 *       from cache when a pattern matches. For the demo stand without internet.</li>
 *   <li><b>Fallback</b> ({@code mimir.demo-mode=false}, default) — orchestrator first tries the
 *       live model; on timeout/429/error it asks the cache. Answers without a matching pattern
 *       still surface as {@link MimirAnswer#unavailable()}.</li>
 * </ul>
 *
 * <p>Matching is case-insensitive {@code contains} on {@code matchKeywords}; first hit wins.
 * Each entry carries {@code simulatedDelayMs} which is honoured via {@link Thread#sleep} so the
 * cached response is indistinguishable from a live LLM call (Q-MC4).
 *
 * <p>ADR-MIMIR-002: 2-tier fallback (DeepSeek → cache) is in effect until 2026-06-02; after
 * Ollama re-evaluation (Q-MT1) this becomes a 3-tier path.
 */
@ApplicationScoped
public class DemoCacheService {

    private static final Logger LOG = Logger.getLogger(DemoCacheService.class);
    private static final String CACHE_RESOURCE = "cache/mimir_responses.json";

    @ConfigProperty(name = "mimir.demo-mode", defaultValue = "false")
    boolean demoMode;

    @Inject ObjectMapper mapper;

    private List<DemoCacheEntry> entries = List.of();

    void onStart(@Observes StartupEvent ev) {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(CACHE_RESOURCE)) {
            if (in == null) {
                LOG.warnf("Demo cache resource not found: %s — cache disabled", CACHE_RESOURCE);
                return;
            }
            DemoCacheFile file = mapper.readValue(in, DemoCacheFile.class);
            this.entries = file.responses() == null ? List.of() : List.copyOf(file.responses());
            LOG.infof("Demo cache loaded: %d entries (demo-mode=%s)", entries.size(), demoMode);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to load demo cache from %s — cache disabled", CACHE_RESOURCE);
        }
    }

    public boolean isDemoMode() {
        return demoMode;
    }

    /**
     * Looks up a cached answer for the given question. Returns empty when no pattern matches
     * (caller decides what to do — usually surface {@link MimirAnswer#unavailable()}).
     *
     * <p>Sleeps {@code simulatedDelayMs} on a hit so the response is indistinguishable from a
     * live LLM call. {@link InterruptedException} restores interrupt status and returns empty.
     */
    public Optional<MimirAnswer> tryCache(String question) {
        if (question == null || question.isBlank() || entries.isEmpty()) {
            return Optional.empty();
        }
        String haystack = question.toLowerCase();
        for (DemoCacheEntry e : entries) {
            if (matches(haystack, e)) {
                long delay = Math.max(0L, e.simulatedDelayMs());
                if (delay > 0L) {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return Optional.empty();
                    }
                }
                LOG.debugf("Demo cache hit: pattern='%s' delay=%dms", e.questionPattern(), delay);
                return Optional.of(new MimirAnswer(
                        e.answer(),
                        e.toolCallsUsed() == null ? List.of() : e.toolCallsUsed(),
                        e.highlightNodeIds() == null ? List.of() : e.highlightNodeIds(),
                        e.confidence(),
                        delay
                ));
            }
        }
        return Optional.empty();
    }

    private static boolean matches(String haystackLower, DemoCacheEntry entry) {
        if (entry.matchKeywords() == null) return false;
        for (String kw : entry.matchKeywords()) {
            if (kw != null && !kw.isBlank() && haystackLower.contains(kw.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    int entryCount() {
        return entries.size();
    }
}
