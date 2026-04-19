package com.skadi.adapters;

import com.skadi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * SKADI adapter for ArcadeDB.
 * Extracts saved SQL queries via ArcadeDB REST API for self-lineage analysis.
 *
 * Full implementation: arcade_meta sprint (W4-W5).
 * This skeleton compiles and returns empty results.
 */
public class ArcadeDBSkadiFetcher implements SkadiFetcher {

    private static final Logger log = LoggerFactory.getLogger(ArcadeDBSkadiFetcher.class);

    @Override
    public SkadiFetchResult fetchScripts(SkadiFetchConfig config) throws SkadiFetchException {
        // TODO (arcade_meta sprint W4-W5):
        // POST /api/v1/query/{database}
        //   Body: {"language":"sql","command":"SELECT @rid, name, query FROM SavedQuery"}
        //   Auth: Basic user:password
        // Распарсить JSON ответ → List<SkadiFetchedFile>
        log.warn("ArcadeDBSkadiFetcher.fetchScripts() is a stub — not yet implemented");
        return new SkadiFetchResult(List.of(),
                new SkadiFetchResult.FetchStats(0, 0, 0, adapterName()));
    }

    @Override
    public boolean ping(SkadiFetchConfig config) {
        // TODO: GET /api/v1/server → 200 OK
        return false;
    }

    @Override
    public String adapterName() { return "arcadedb"; }

    @Override
    public void close() {
        // REST adapter — нет long-lived connection
    }
}
