# Expected ANTLR4 Parse Errors — Fixture Reference

Each fixture in this directory is an intentionally broken or edge-case PL/SQL file used to
verify that `PlSqlErrorCollector` surfaces errors correctly through `HoundEventListener.onParseError`
and into `ParseResult.errors()`.

---

## err_sqlplus_directives.sql

**Category:** SQL\*Plus client directives (not a grammar error)  
**Expected ANTLR4 errors:** 0  
**Reason:** The preprocessor (`HoundParserImpl.stripSqlPlusDirectives`) blanks out `SET`, `PROMPT`,
`WHENEVER`, `SPOOL`, etc. before ANTLR4 sees the file. After stripping the procedure body is
clean PL/SQL.

**Test:** `sqlplusDirectives_strippedByPreprocessor_zeroAntlrErrors`  
`parseErrors.isEmpty() == true`, `result.isSuccess() == true`

---

## err_unknown_token.sql

**Category:** Token recognition error — `$` character  
**Expected ANTLR4 errors:** ≥ 1  
**Observed error (actual ANTLR4 output):**
```
line 10:27 — token recognition error at: '$S'
```
**Reason:** `$SYS_VAR` is not a valid PL/SQL identifier token (dollar sign is not permitted at
start). ANTLR4 fires a token recognition error and then continues parsing.

**Test:** `unknownToken_dollarSign_atLeastOneAntlrError`  
`parseErrors` contains at least one entry starting with `"line 10:"`.

---

## err_update_alias.sql

**Category:** Known grammar limitation — `UPDATE tbl alias SET alias.col = val`  
**Expected ANTLR4 errors:** ≥ 1  
**Observed error (actual ANTLR4 output):**
```
line 15:11 — missing 'SET' at 'e'
```
**Reason:** The PL/SQL ANTLR4 grammar does not support table aliases in `UPDATE` DML.
Oracle allows `UPDATE HR.EMPLOYEES e SET e.SALARY = …` but the grammar expects `UPDATE tbl SET col = …`.
The parser sees the alias `e` as an unexpected token before `SET`.

**This is a known grammar limitation, not a bug in Oracle SQL.**

**Test:** `updateAlias_knownGrammarLimitation_atLeastOneAntlrError`  
`parseErrors` contains an entry with `"SET"` or `"="`.

---

## err_unclosed_block.sql

**Category:** Structural syntax error — missing `END`  
**Expected ANTLR4 errors:** ≥ 1  
**Observed error (actual ANTLR4 output):**
```
line 16:0 — mismatched input '<EOF>' expecting {'END', 'EXCEPTION', ...}
```
**Reason:** The procedure body opens `BEGIN` but never closes it with `END`. ANTLR4 reports
an EOF error when it runs out of tokens while expecting a closing keyword.

**Test:** `unclosedBlock_missingEnd_eofError`  
`parseErrors` is non-empty; `result.file()` is non-blank (file was registered despite error).

---

## err_mixed.sql

**Category:** Mixed — SQL\*Plus directives + `DELETE` with alias  
**Expected ANTLR4 errors:** 0 or ≥ 1  
**Reason:** The `SET SERVEROUTPUT` and `PROMPT` lines are stripped by the preprocessor.
The `DELETE FROM DWH.STAGING_DATA s WHERE s.created_at …` construct uses a table alias.
Depending on the grammar version this may or may not produce an error — both outcomes are
acceptable. The important guarantee is that no `RuntimeException` is thrown and that
`DWH.CLEANUP_OLD_DATA` is registered with a non-blank file path.

**Test:** `mixed_sqlplusAndDeleteAlias_procedureParsed`  
`result.file()` is non-blank; `result.durationMs() >= 0`; no exception thrown.

---

## Notes

- All errors in `ParseResult.errors()` mirror what was delivered to `HoundEventListener.onParseError`.
- Error level: **ERROR for the file** (goes into `ParseResult.errors()`), **WARN for the process**
  (batch continues; `HoundHeimdallListener` sends `PARSE_ERROR/WARN` event to HEIMDALL).
- ANTLR4 recovers from most syntax errors and continues parsing, so even files with errors
  may yield extracted atoms/procedures/tables.
- The preprocessor log entry format: `[SQLPlus-strip] filename.sql — removed N directive line(s)`.
- The error collector format: `line L:C — message` (matches ANTLR4's default format).
