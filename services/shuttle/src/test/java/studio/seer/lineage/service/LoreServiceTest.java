package studio.seer.lineage.service;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import studio.seer.lineage.client.ArcadeGateway;
import studio.seer.lineage.model.LoreEntry;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LoreService — shared singleton hound_lore DB queries.
 *
 * SHT-P3D-1: LoreService coverage
 */
@QuarkusTest
class LoreServiceTest {

    @InjectMock ArcadeGateway arcade;

    @Inject LoreService service;

    // ── findByGeoid ────────────────────────────────────────────────────────────

    @Test
    void findByGeoid_found_returnsMappedEntry() {
        when(arcade.sqlIn(eq(LoreService.LORE_DB), contains("geoid = :geoid"), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of(
                        Map.of("id", "#10:5", "geoid", "PKG.CALC_BONUS",
                               "kind", "FUNCTION", "label", "Calc Bonus",
                               "description", "Calculates quarterly bonus")
                )));

        LoreEntry entry = service.findByGeoid("PKG.CALC_BONUS").await().indefinitely();

        assertNotNull(entry);
        assertEquals("#10:5",            entry.id());
        assertEquals("PKG.CALC_BONUS",   entry.geoid());
        assertEquals("FUNCTION",         entry.kind());
        assertEquals("Calc Bonus",       entry.label());
        assertEquals("Calculates quarterly bonus", entry.description());
    }

    @Test
    void findByGeoid_notFound_returnsNull() {
        when(arcade.sqlIn(eq(LoreService.LORE_DB), contains("geoid = :geoid"), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of()));

        LoreEntry entry = service.findByGeoid("NONEXISTENT.PROC").await().indefinitely();

        assertNull(entry);
    }

    @Test
    void findByGeoid_partialRow_noNpe() {
        // Row with missing optional fields — getOrDefault("description","") should kick in
        when(arcade.sqlIn(eq(LoreService.LORE_DB), contains("geoid = :geoid"), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of(
                        Map.of("id", "#10:1", "geoid", "X")
                )));

        LoreEntry entry = service.findByGeoid("X").await().indefinitely();

        assertNotNull(entry);
        assertEquals("X", entry.geoid());
        // missing fields default to ""
        assertEquals("", entry.kind());
        assertEquals("", entry.label());
        assertEquals("", entry.description());
    }

    // ── search ─────────────────────────────────────────────────────────────────

    @Test
    void search_blankQuery_returnsEmptyWithoutArcadeCall() {
        List<LoreEntry> result = service.search("  ", 10).await().indefinitely();

        assertTrue(result.isEmpty());
        verifyNoInteractions(arcade);
    }

    @Test
    void search_nullQuery_returnsEmptyWithoutArcadeCall() {
        List<LoreEntry> result = service.search(null, 10).await().indefinitely();

        assertTrue(result.isEmpty());
        verifyNoInteractions(arcade);
    }

    @Test
    void search_validQuery_returnsMappedEntries() {
        when(arcade.sqlIn(eq(LoreService.LORE_DB), contains("LIKE :q"), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of(
                        Map.of("id", "#10:2", "geoid", "PKG.A", "kind", "PROCEDURE",
                               "label", "Proc A", "description", "desc A"),
                        Map.of("id", "#10:3", "geoid", "PKG.B", "kind", "FUNCTION",
                               "label", "Func B", "description", "desc B")
                )));

        List<LoreEntry> result = service.search("bonus", 50).await().indefinitely();

        assertEquals(2, result.size());
        assertEquals("PKG.A", result.get(0).geoid());
        assertEquals("PKG.B", result.get(1).geoid());
    }

    @Test
    void search_limitCappedAt100() {
        when(arcade.sqlIn(eq(LoreService.LORE_DB), contains("LIKE :q"), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of()));

        // Passing limit > 100: min(limit,100) should cap it
        service.search("x", 999).await().indefinitely();

        verify(arcade).sqlIn(eq(LoreService.LORE_DB), anyString(),
                argThat(params -> {
                    Object lim = params.get("lim");
                    return lim instanceof Number n && n.intValue() <= 100;
                }));
    }

    @Test
    void search_queryWithPercent_escapedInLike() {
        when(arcade.sqlIn(eq(LoreService.LORE_DB), contains("LIKE :q"), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of()));

        service.search("100%", 10).await().indefinitely();

        // The % in the query should be escaped to \%
        verify(arcade).sqlIn(eq(LoreService.LORE_DB), anyString(),
                argThat(params -> {
                    String q = (String) params.get("q");
                    return q != null && q.contains("\\%");
                }));
    }
}
