package com.hound.parser;

import java.util.regex.Pattern;

/**
 * KI-CONDCOMP-1: PL/SQL Conditional Compilation ($IF / $ELSIF / $ELSE / $END) preprocessor.
 *
 * <p>Strategy: <strong>conservative overcapture</strong> — all branches are included by stripping
 * the conditional compilation directives and retaining all branch bodies.
 * This means the parser sees every branch, which may produce spurious atoms in the "wrong" branch,
 * but avoids missing real lineage in the active branch (undercapture is worse).
 *
 * <p>Example:
 * <pre>
 *   $IF DBMS_DB_VERSION.VERSION >= 12 $THEN
 *       INSERT INTO t1 VALUES (1);
 *   $ELSE
 *       INSERT INTO t2 VALUES (2);
 *   $END
 * </pre>
 * becomes:
 * <pre>
 *       INSERT INTO t1 VALUES (1);
 *       INSERT INTO t2 VALUES (2);
 * </pre>
 */
public class ConditionalCompilationPreprocessor {

    /**
     * Matches conditional compilation directives.
     * Groups: $IF...THEN, $ELSIF...THEN, $ELSE, $END — all stripped to expose all branches.
     */
    private static final Pattern CC_PATTERN = Pattern.compile(
            "\\$IF\\b.*?\\$THEN|\\$ELSIF\\b.*?\\$THEN|\\$ELSE\\b|\\$END\\b",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /**
     * Removes all conditional compilation directives from PL/SQL source, exposing all branches.
     *
     * @param source raw PL/SQL source text
     * @return source with $IF/$ELSIF/$ELSE/$END directives removed (all branches retained)
     */
    public String expand(String source) {
        if (source == null || !source.contains("$")) return source;
        return CC_PATTERN.matcher(source).replaceAll("");
    }
}
