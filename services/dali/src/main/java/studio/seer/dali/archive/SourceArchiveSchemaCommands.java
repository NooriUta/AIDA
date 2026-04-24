package studio.seer.dali.archive;

/**
 * DDL constants for the hound_src_{tenant} schema in YGG.
 *
 * Three DOCUMENT types (no graph edges — archive only):
 *   DaliParseSession — one record per parse session (batch or single)
 *   DaliSourceFile   — one record per SQL file parsed
 *   DaliParseError   — one record per parse error (Heimdall-visible errors land here too)
 *
 * Datetime fields use ArcadeDB DATETIME type — stored as epoch ms, displayed as readable date in Studio.
 * sql_text is STRING with no size limit (ArcadeDB Java String — same as DaliSnippetScript.script).
 * sql_text is NOT indexed — whole-file field, full-text index would be prohibitively large.
 */
final class SourceArchiveSchemaCommands {

    private SourceArchiveSchemaCommands() {}

    static final String[] TYPES = {
        "DaliParseSession",
        "DaliSourceFile",
        "DaliParseError"
    };

    // { typeName, propertyName, arcadeDbType }
    static final String[][] PROPERTIES = {

        // ── DaliParseSession ──────────────────────────────────────────────────
        { "DaliParseSession", "session_id",         "STRING"  },
        { "DaliParseSession", "revision_name",      "STRING"  },
        { "DaliParseSession", "tenant_id",          "STRING"  },
        { "DaliParseSession", "dialect",            "STRING"  },
        { "DaliParseSession", "source_path",        "STRING"  },
        { "DaliParseSession", "is_batch",           "BOOLEAN" },
        { "DaliParseSession", "is_preview",         "BOOLEAN" },
        { "DaliParseSession", "clear_before_write", "BOOLEAN" },
        { "DaliParseSession", "datetime_start",     "DATETIME" },
        { "DaliParseSession", "datetime_stop",      "DATETIME" },
        { "DaliParseSession", "total_files",        "INTEGER" },
        { "DaliParseSession", "success_count",      "INTEGER" },
        { "DaliParseSession", "error_count",        "INTEGER" },
        { "DaliParseSession", "duration_ms",        "LONG"    },
        { "DaliParseSession", "atom_count",         "INTEGER" },
        { "DaliParseSession", "resolution_rate",    "DOUBLE"  },

        // ── DaliSourceFile ────────────────────────────────────────────────────
        { "DaliSourceFile", "source_file_id", "STRING"  },
        { "DaliSourceFile", "session_id",     "STRING"  },
        { "DaliSourceFile", "file_path",      "STRING"  },
        { "DaliSourceFile", "object_name",    "STRING"  },
        { "DaliSourceFile", "schema_name",    "STRING"  },
        { "DaliSourceFile", "adapter_name",   "STRING"  },
        { "DaliSourceFile", "sql_text",       "STRING"  },  // NOT indexed — whole-file field
        { "DaliSourceFile", "sql_text_hash",  "STRING"  },
        { "DaliSourceFile", "size_bytes",     "INTEGER" },
        { "DaliSourceFile", "datetime_start", "DATETIME" },
        { "DaliSourceFile", "datetime_stop",  "DATETIME" },
        { "DaliSourceFile", "duration_ms",    "LONG"    },
        { "DaliSourceFile", "success",        "BOOLEAN" },

        // ── DaliParseError ────────────────────────────────────────────────────
        { "DaliParseError", "error_id",       "STRING" },
        { "DaliParseError", "session_id",     "STRING" },
        { "DaliParseError", "source_file_id", "STRING" },  // null for session-level errors
        { "DaliParseError", "file_path",      "STRING" },
        { "DaliParseError", "error_text",     "STRING" },
        { "DaliParseError", "error_type",     "STRING" },  // PARSE_ERROR | WRITE_ERROR | SESSION_ERROR
        { "DaliParseError", "occurred_at",    "DATETIME" },
    };

    // { typeName, propertyName, unique }
    static final Object[][] INDEXES = {
        { "DaliParseSession", "session_id",    Boolean.TRUE  },
        { "DaliParseSession", "revision_name", Boolean.FALSE },
        { "DaliParseSession", "datetime_start",Boolean.FALSE },
        { "DaliSourceFile",   "source_file_id",Boolean.TRUE  },
        { "DaliSourceFile",   "session_id",    Boolean.FALSE },
        { "DaliSourceFile",   "sql_text_hash", Boolean.FALSE },
        { "DaliParseError",   "error_id",      Boolean.TRUE  },
        { "DaliParseError",   "session_id",    Boolean.FALSE },
    };
}
