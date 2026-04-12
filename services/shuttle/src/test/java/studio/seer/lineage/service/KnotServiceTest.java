package studio.seer.lineage.service;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for KnotService static helper methods.
 * No DB access — all methods under test are pure functions.
 */
@QuarkusTest
class KnotServiceTest {

    // ── atomLine() ────────────────────────────────────────────────────────────

    @Test
    void atomLine_typicalFormat() {
        assertEquals(78, KnotService.atomLine("CODE_FIELD~78:0"));
    }

    @Test
    void atomLine_multiSegment() {
        assertEquals(152, KnotService.atomLine("SOME_NAME~45:3~152:7"));
    }

    @Test
    void atomLine_noTilde() {
        assertEquals(0, KnotService.atomLine("CODE_FIELD"));
    }

    @Test
    void atomLine_null() {
        assertEquals(0, KnotService.atomLine(null));
    }

    @Test
    void atomLine_empty() {
        assertEquals(0, KnotService.atomLine(""));
    }

    @Test
    void atomLine_onlyTilde() {
        assertEquals(0, KnotService.atomLine("~"));
    }

    @Test
    void atomLine_noColon() {
        // "NAME~42" — no colon means whole rest is line
        assertEquals(42, KnotService.atomLine("NAME~42"));
    }

    // ── atomPos() ─────────────────────────────────────────────────────────────

    @Test
    void atomPos_typicalFormat() {
        assertEquals(0, KnotService.atomPos("CODE_FIELD~78:0"));
    }

    @Test
    void atomPos_nonZeroPos() {
        assertEquals(7, KnotService.atomPos("SOME~152:7"));
    }

    @Test
    void atomPos_noColon() {
        assertEquals(0, KnotService.atomPos("NAME~42"));
    }

    @Test
    void atomPos_null() {
        assertEquals(0, KnotService.atomPos(null));
    }

    // ── parseStmtType() ───────────────────────────────────────────────────────

    @Test
    void parseStmtType_insert() {
        assertEquals("INSERT", KnotService.parseStmtType("DWH.PKG:PROCEDURE:MY_PROC:INSERT:152"));
    }

    @Test
    void parseStmtType_select() {
        assertEquals("SELECT", KnotService.parseStmtType("DWH.PKG:PROCEDURE:MY_PROC:SELECT:10"));
    }

    @Test
    void parseStmtType_nested() {
        // Nested geoid has more than 5 parts — part[3] is still the stmt type
        assertEquals("INSERT", KnotService.parseStmtType("DWH.PKG:PROCEDURE:MY_PROC:INSERT:152:SELECT:200"));
    }

    @Test
    void parseStmtType_null() {
        assertEquals("UNKNOWN", KnotService.parseStmtType(null));
    }

    @Test
    void parseStmtType_empty() {
        assertEquals("UNKNOWN", KnotService.parseStmtType(""));
    }

    @Test
    void parseStmtType_tooFewParts() {
        assertEquals("UNKNOWN", KnotService.parseStmtType("DWH.PKG:PROCEDURE:MY_PROC"));
    }

    // ── parseLineNumber() ─────────────────────────────────────────────────────

    @Test
    void parseLineNumber_typical() {
        assertEquals(152, KnotService.parseLineNumber("DWH.PKG:PROCEDURE:MY_PROC:INSERT:152"));
    }

    @Test
    void parseLineNumber_null() {
        assertEquals(0, KnotService.parseLineNumber(null));
    }

    @Test
    void parseLineNumber_tooFewParts() {
        assertEquals(0, KnotService.parseLineNumber("A:B:C:D"));
    }

    @Test
    void parseLineNumber_nonNumeric() {
        assertEquals(0, KnotService.parseLineNumber("A:B:C:D:notanumber"));
    }

    // ── parsePackageName() ────────────────────────────────────────────────────

    @Test
    void parsePackageName_typical() {
        assertEquals("DWH.CALC_PKL_CRED", KnotService.parsePackageName("DWH.CALC_PKL_CRED:PROCEDURE:MY_PROC:INSERT:152"));
    }

    @Test
    void parsePackageName_noColon() {
        assertEquals("DWH.PKG", KnotService.parsePackageName("DWH.PKG"));
    }

    @Test
    void parsePackageName_null() {
        assertEquals("", KnotService.parsePackageName(null));
    }

    @Test
    void parsePackageName_empty() {
        assertEquals("", KnotService.parsePackageName(""));
    }

    // ── deriveName() ──────────────────────────────────────────────────────────

    @Test
    void deriveName_fromSessionName() {
        assertEquals("MY_SESSION", KnotService.deriveName("MY_SESSION", "/some/path.pck"));
    }

    @Test
    void deriveName_fromFilePath_unix() {
        assertEquals("MY_PROC", KnotService.deriveName(null, "/home/oracle/MY_PROC.pck"));
    }

    @Test
    void deriveName_fromFilePath_windows() {
        assertEquals("MY_PROC", KnotService.deriveName("", "C:\\scripts\\MY_PROC.pck.html"));
    }

    @Test
    void deriveName_noExtension() {
        assertEquals("MY_PROC", KnotService.deriveName(null, "/home/oracle/MY_PROC"));
    }

    @Test
    void deriveName_bothNull() {
        assertEquals("", KnotService.deriveName(null, null));
    }

    @Test
    void deriveName_blankSessionName() {
        assertEquals("FILE", KnotService.deriveName("   ", "/path/FILE.sql"));
    }
}
