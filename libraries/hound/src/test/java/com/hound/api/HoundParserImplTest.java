package com.hound.api;

import com.hound.HoundParserImpl;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link HoundParserImpl} — parse() and parseBatch() in DISABLED mode.
 */
class HoundParserImplTest {

    private static Path fixture(String name) throws Exception {
        URL url = HoundParserImplTest.class.getClassLoader().getResource("plsql/" + name);
        assertNotNull(url, "Fixture not found: plsql/" + name);
        return Paths.get(url.toURI());
    }

    @Test
    void parse_singleFile_returnsSuccessResult() throws Exception {
        HoundConfig config = HoundConfig.defaultDisabled("plsql");
        ParseResult result = new HoundParserImpl().parse(fixture("pkg_dml_basic.pck"), config);

        assertNotNull(result);
        assertTrue(result.isSuccess(), "Expected success but got errors: " + result.errors());
        assertTrue(result.atomCount() >= 0);
        assertTrue(result.durationMs() >= 0);
        assertFalse(result.file().isBlank());
    }

    @Test
    void parse_withListener_atomExtractedEventsFire() throws Exception {
        HoundConfig config = HoundConfig.defaultDisabled("plsql");
        AtomicInteger eventCount = new AtomicInteger(0);

        HoundEventListener listener = new HoundEventListener() {
            @Override
            public void onAtomExtracted(String file, int atomCount, String atomType) {
                eventCount.set(atomCount); // last value = running total of registerAtom calls
            }
        };

        ParseResult result = new HoundParserImpl().parse(fixture("pkg_dml_basic.pck"), config, listener);

        // At least one atom event fired for a non-trivial SQL file
        assertTrue(eventCount.get() > 0,
                "Expected onAtomExtracted to fire at least once for pkg_dml_basic.pck");
        // ParseResult.atomCount() >= 0 (semantic atoms, a deduplicated subset)
        assertTrue(result.atomCount() >= 0);
    }

    @Test
    void parseBatch_multipleFiles_returnsOneResultPerFile() throws Exception {
        HoundConfig config = HoundConfig.defaultDisabled("plsql");
        List<Path> files = List.of(
                fixture("pkg_dml_basic.pck"),
                fixture("pkg_cursor_loops.pck")
        );

        List<ParseResult> results = new HoundParserImpl().parseBatch(files, config);

        assertEquals(2, results.size(), "Should return one result per file");
        for (ParseResult r : results) {
            assertTrue(r.isSuccess(), "All files should parse successfully: " + r.errors());
        }
    }

    @Test
    void parseBatch_emptyList_returnsEmptyList() {
        HoundConfig config = HoundConfig.defaultDisabled("plsql");
        List<ParseResult> results = new HoundParserImpl().parseBatch(List.of(), config);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void parseBatch_withListener_firesStartedAndCompletedForEachFile() throws Exception {
        HoundConfig config = HoundConfig.defaultDisabled("plsql");
        AtomicInteger started = new AtomicInteger(0);
        AtomicInteger completed = new AtomicInteger(0);

        HoundEventListener listener = new HoundEventListener() {
            @Override
            public void onFileParseStarted(String file, String dialect) {
                started.incrementAndGet();
            }

            @Override
            public void onFileParseCompleted(String file, ParseResult result) {
                completed.incrementAndGet();
            }
        };

        List<Path> files = List.of(
                fixture("pkg_dml_basic.pck"),
                fixture("pkg_cursor_loops.pck")
        );

        new HoundParserImpl().parseBatch(files, config, listener);

        assertEquals(2, started.get(), "Should fire onFileParseStarted for each file");
        assertEquals(2, completed.get(), "Should fire onFileParseCompleted for each file");
    }

    @Test
    void parseResult_resolutionRateBetweenZeroAndOne() throws Exception {
        HoundConfig config = HoundConfig.defaultDisabled("plsql");
        ParseResult result = new HoundParserImpl().parse(fixture("pkg_dml_basic.pck"), config);

        assertTrue(result.resolutionRate() >= 0.0 && result.resolutionRate() <= 1.0,
                "resolutionRate must be in [0.0, 1.0] but was: " + result.resolutionRate());
    }
}
