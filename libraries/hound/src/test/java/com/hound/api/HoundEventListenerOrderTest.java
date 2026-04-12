package com.hound.api;

import com.hound.HoundParserImpl;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies listener event order: onFileParseStarted → onAtomExtracted(N≥0) → onFileParseCompleted.
 * Uses a small PL/SQL fixture from test resources — no DB write needed.
 */
class HoundEventListenerOrderTest {

    /** Ordered log of events captured during parse. */
    private record Event(String type, String file) {}

    @Test
    void listenerEventOrder_startedThenAtomsCompletedNoErrors() throws Exception {
        URL fixture = getClass().getClassLoader().getResource("plsql/pkg_dml_basic.pck");
        assertNotNull(fixture, "test fixture plsql/pkg_dml_basic.pck not found");
        Path sqlFile = Paths.get(fixture.toURI());

        List<Event> events = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        HoundEventListener listener = new HoundEventListener() {
            @Override
            public void onFileParseStarted(String file, String dialect) {
                events.add(new Event("started", file));
            }

            @Override
            public void onAtomExtracted(String file, int atomCount, String atomType) {
                events.add(new Event("atom:" + atomType, file));
            }

            @Override
            public void onFileParseCompleted(String file, ParseResult result) {
                events.add(new Event("completed", file));
            }

            @Override
            public void onError(String file, Throwable error) {
                errors.add(file + ": " + error.getMessage());
            }
        };

        HoundConfig config = HoundConfig.defaultDisabled("plsql");
        ParseResult result = new HoundParserImpl().parse(sqlFile, config, listener);

        // No errors
        assertTrue(errors.isEmpty(), "Expected no errors but got: " + errors);

        // At least: started + completed
        assertTrue(events.size() >= 2, "Expected at least 2 events, got " + events.size());

        // First event: started
        assertEquals("started", events.get(0).type(),
                "First event must be onFileParseStarted");

        // Last event: completed
        assertEquals("completed", events.get(events.size() - 1).type(),
                "Last event must be onFileParseCompleted");

        // All intermediate events: atom events (order: started → atoms* → completed)
        for (int i = 1; i < events.size() - 1; i++) {
            assertTrue(events.get(i).type().startsWith("atom:"),
                    "Middle events must be atom events, got: " + events.get(i).type());
        }

        // ParseResult sanity
        assertNotNull(result);
        assertTrue(result.isSuccess(), "ParseResult should be success");
        assertTrue(result.atomCount() >= 0);
        assertTrue(result.durationMs() >= 0);
    }

    @Test
    void noOpListener_doesNotThrow() throws Exception {
        URL fixture = getClass().getClassLoader().getResource("plsql/pkg_dml_basic.pck");
        assertNotNull(fixture, "test fixture not found");
        Path sqlFile = Paths.get(fixture.toURI());

        HoundConfig config = HoundConfig.defaultDisabled("plsql");
        ParseResult result = new HoundParserImpl().parse(
                sqlFile, config, NoOpHoundEventListener.INSTANCE);

        assertNotNull(result);
        assertTrue(result.isSuccess());
    }
}
