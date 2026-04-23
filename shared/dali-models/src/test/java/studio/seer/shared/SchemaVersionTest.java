package studio.seer.shared;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SchemaVersionTest {

    @Test
    void read_returnsOne_whenKeyAbsent() {
        assertEquals(1, SchemaVersion.read(Map.of()));
        assertEquals(1, SchemaVersion.read(null));
        assertEquals(1, SchemaVersion.read(Map.of("action", "USER_CREATE")));
    }

    @Test
    void read_returnsExplicitVersion() {
        assertEquals(2, SchemaVersion.read(Map.of(SchemaVersion.SCHEMA_VERSION_KEY, 2)));
        assertEquals(5, SchemaVersion.read(Map.of(SchemaVersion.SCHEMA_VERSION_KEY, 5L)));
        assertEquals(3, SchemaVersion.read(Map.of(SchemaVersion.SCHEMA_VERSION_KEY, "3")));
    }

    @Test
    void read_rejectsMalformedVersion() {
        assertThrows(IllegalArgumentException.class,
                () -> SchemaVersion.read(Map.of(SchemaVersion.SCHEMA_VERSION_KEY, 0)));
        assertThrows(IllegalArgumentException.class,
                () -> SchemaVersion.read(Map.of(SchemaVersion.SCHEMA_VERSION_KEY, -1)));
        assertThrows(IllegalArgumentException.class,
                () -> SchemaVersion.read(Map.of(SchemaVersion.SCHEMA_VERSION_KEY, "not-a-number")));
        assertThrows(IllegalArgumentException.class,
                () -> SchemaVersion.read(Map.of(SchemaVersion.SCHEMA_VERSION_KEY, 1.5)));  // not an int
    }

    @Test
    void withSchemaVersion_addsKeyAndPreservesOthers() {
        Map<String, Object> base = Map.of("action", "X", "target", "y");
        Map<String, Object> out = SchemaVersion.withSchemaVersion(base, 2);
        assertEquals(2, out.get(SchemaVersion.SCHEMA_VERSION_KEY));
        assertEquals("X", out.get("action"));
        assertEquals("y", out.get("target"));
    }

    @Test
    void withSchemaVersion_preservesInsertionOrder() {
        // LinkedHashMap ordering so that when JSON-serialized keys appear in order
        Map<String, Object> base = new LinkedHashMap<>();
        base.put("a", 1); base.put("b", 2);
        Map<String, Object> out = SchemaVersion.withSchemaVersion(base, 1);
        assertEquals("[a, b, _schemaVersion]", out.keySet().toString());
    }

    @Test
    void withSchemaVersion_rejectsNonPositive() {
        assertThrows(IllegalArgumentException.class,
                () -> SchemaVersion.withSchemaVersion(Map.of(), 0));
        assertThrows(IllegalArgumentException.class,
                () -> SchemaVersion.withSchemaVersion(Map.of(), -3));
    }

    @Test
    void withCurrentSchemaVersion_usesCURRENT() {
        Map<String, Object> out = SchemaVersion.withCurrentSchemaVersion(Map.of("x", 1));
        assertEquals(SchemaVersion.CURRENT, out.get(SchemaVersion.SCHEMA_VERSION_KEY));
    }

    @Test
    void isSupported_acceptsCurrentAndPrevious() {
        assertTrue(SchemaVersion.isSupported(Map.of(SchemaVersion.SCHEMA_VERSION_KEY, SchemaVersion.CURRENT)));
        assertTrue(SchemaVersion.isSupported(Map.of(SchemaVersion.SCHEMA_VERSION_KEY, SchemaVersion.PREVIOUS)));
        assertTrue(SchemaVersion.isSupported(Map.of()));  // implicit v1
    }

    @Test
    void isDeprecated_isFalseForCurrent() {
        assertFalse(SchemaVersion.isDeprecated(Map.of(SchemaVersion.SCHEMA_VERSION_KEY, SchemaVersion.CURRENT)));
        assertFalse(SchemaVersion.isDeprecated(Map.of()));
    }

    @Test
    void withoutSchemaVersion_removesKey() {
        Map<String, Object> in = Map.of("action", "X", SchemaVersion.SCHEMA_VERSION_KEY, 1);
        Map<String, Object> out = SchemaVersion.withoutSchemaVersion(in);
        assertFalse(out.containsKey(SchemaVersion.SCHEMA_VERSION_KEY));
        assertEquals("X", out.get("action"));
    }

    @Test
    void withoutSchemaVersion_handlesAbsentKey() {
        Map<String, Object> in = Map.of("action", "X");
        Map<String, Object> out = SchemaVersion.withoutSchemaVersion(in);
        assertEquals(in, out);
    }

    @Test
    void withoutSchemaVersion_handlesNull() {
        Map<String, Object> out = SchemaVersion.withoutSchemaVersion(null);
        assertNotNull(out);
        assertTrue(out.isEmpty());
    }

    @Test
    void constants_haveExpectedBootstrapValues() {
        assertEquals(1, SchemaVersion.CURRENT);
        assertEquals(1, SchemaVersion.PREVIOUS);
        assertEquals("_schemaVersion", SchemaVersion.SCHEMA_VERSION_KEY);
    }
}
