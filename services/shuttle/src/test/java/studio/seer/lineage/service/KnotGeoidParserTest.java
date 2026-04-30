package studio.seer.lineage.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link KnotGeoidParser} static utilities.
 * No CDI / DB access — all methods under test are pure functions.
 *
 * <p>Renamed from {@code KnotServiceTest} (LOC refactor — QG-ARCH-INVARIANTS §2.4).
 */
class KnotGeoidParserTest {

    // ── atomLine() ────────────────────────────────────────────────────────────

    @Test
    void atomLine_typicalFormat() {
        assertEquals(78, KnotGeoidParser.atomLine("CODE_FIELD~78:0"));
    }

    @Test
    void atomLine_multiSegment() {
        assertEquals(152, KnotGeoidParser.atomLine("SOME_NAME~45:3~152:7"));
    }

    @Test
    void atomLine_noTilde() {
        assertEquals(0, KnotGeoidParser.atomLine("CODE_FIELD"));
    }

    @Test
    void atomLine_null() {
        assertEquals(0, KnotGeoidParser.atomLine(null));
    }

    @Test
    void atomLine_empty() {
        assertEquals(0, KnotGeoidParser.atomLine(""));
    }

    @Test
    void atomLine_onlyTilde() {
        assertEquals(0, KnotGeoidParser.atomLine("~"));
    }

    @Test
    void atomLine_noColon() {
        // "NAME~42" — no colon means whole rest is line
        assertEquals(42, KnotGeoidParser.atomLine("NAME~42"));
    }

    // ── atomPos() ─────────────────────────────────────────────────────────────

    @Test
    void atomPos_typicalFormat() {
        assertEquals(0, KnotGeoidParser.atomPos("CODE_FIELD~78:0"));
    }

    @Test
    void atomPos_nonZeroPos() {
        assertEquals(7, KnotGeoidParser.atomPos("SOME~152:7"));
    }

    @Test
    void atomPos_noColon() {
        assertEquals(0, KnotGeoidParser.atomPos("NAME~42"));
    }

    @Test
    void atomPos_null() {
        assertEquals(0, KnotGeoidParser.atomPos(null));
    }

    // ── parseStmtType() ───────────────────────────────────────────────────────

    @Test
    void parseStmtType_insert() {
        assertEquals("INSERT", KnotGeoidParser.parseStmtType("DWH.PKG:PROCEDURE:MY_PROC:INSERT:152"));
    }

    @Test
    void parseStmtType_select() {
        assertEquals("SELECT", KnotGeoidParser.parseStmtType("DWH.PKG:PROCEDURE:MY_PROC:SELECT:10"));
    }

    @Test
    void parseStmtType_nested() {
        // Nested geoid has more than 5 parts — part[3] is still the stmt type
        assertEquals("INSERT", KnotGeoidParser.parseStmtType("DWH.PKG:PROCEDURE:MY_PROC:INSERT:152:SELECT:200"));
    }

    @Test
    void parseStmtType_null() {
        assertEquals("UNKNOWN", KnotGeoidParser.parseStmtType(null));
    }

    @Test
    void parseStmtType_empty() {
        assertEquals("UNKNOWN", KnotGeoidParser.parseStmtType(""));
    }

    @Test
    void parseStmtType_tooFewParts() {
        assertEquals("UNKNOWN", KnotGeoidParser.parseStmtType("DWH.PKG:PROCEDURE:MY_PROC"));
    }

    // ── parseLineNumber() ─────────────────────────────────────────────────────

    @Test
    void parseLineNumber_typical() {
        assertEquals(152, KnotGeoidParser.parseLineNumber("DWH.PKG:PROCEDURE:MY_PROC:INSERT:152"));
    }

    @Test
    void parseLineNumber_null() {
        assertEquals(0, KnotGeoidParser.parseLineNumber(null));
    }

    @Test
    void parseLineNumber_tooFewParts() {
        assertEquals(0, KnotGeoidParser.parseLineNumber("A:B:C:D"));
    }

    @Test
    void parseLineNumber_nonNumeric() {
        assertEquals(0, KnotGeoidParser.parseLineNumber("A:B:C:D:notanumber"));
    }

    // ── parsePackageName() ────────────────────────────────────────────────────

    @Test
    void parsePackageName_typical() {
        assertEquals("DWH.CALC_PKL_CRED", KnotGeoidParser.parsePackageName("DWH.CALC_PKL_CRED:PROCEDURE:MY_PROC:INSERT:152"));
    }

    @Test
    void parsePackageName_noColon() {
        assertEquals("DWH.PKG", KnotGeoidParser.parsePackageName("DWH.PKG"));
    }

    @Test
    void parsePackageName_null() {
        assertEquals("", KnotGeoidParser.parsePackageName(null));
    }

    @Test
    void parsePackageName_empty() {
        assertEquals("", KnotGeoidParser.parsePackageName(""));
    }

    // ── deriveName() ──────────────────────────────────────────────────────────

    @Test
    void deriveName_fromSessionName() {
        assertEquals("MY_SESSION", KnotGeoidParser.deriveName("MY_SESSION", "/some/path.pck"));
    }

    @Test
    void deriveName_fromFilePath_unix() {
        assertEquals("MY_PROC", KnotGeoidParser.deriveName(null, "/home/oracle/MY_PROC.pck"));
    }

    @Test
    void deriveName_fromFilePath_windows() {
        assertEquals("MY_PROC", KnotGeoidParser.deriveName("", "C:\\scripts\\MY_PROC.pck.html"));
    }

    @Test
    void deriveName_noExtension() {
        assertEquals("MY_PROC", KnotGeoidParser.deriveName(null, "/home/oracle/MY_PROC"));
    }

    @Test
    void deriveName_bothNull() {
        assertEquals("", KnotGeoidParser.deriveName(null, null));
    }

    @Test
    void deriveName_blankSessionName() {
        assertEquals("FILE", KnotGeoidParser.deriveName("   ", "/path/FILE.sql"));
    }
}
