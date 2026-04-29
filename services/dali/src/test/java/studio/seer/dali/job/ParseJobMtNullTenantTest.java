package studio.seer.dali.job;

import org.junit.jupiter.api.Test;
import studio.seer.shared.ParseSessionInput;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TC-DALI-30 / TC-DALI-31 — MTN-04: ParseJob MUST refuse to run without a tenant alias
 * and MUST NOT contain hard-coded fall-through to the {@code "default"} tenant.
 *
 * <ul>
 *   <li>TC-DALI-30: {@code requireTenantAlias} throws {@link IllegalStateException}
 *       with the MTN-04 marker when the alias is {@code null} or blank.</li>
 *   <li>TC-DALI-31: Static source-code scan — no Java file in the job package
 *       contains a string literal {@code "default"} adjacent to tenant-routing
 *       keywords that would indicate a silent fallback.</li>
 * </ul>
 */
class ParseJobMtNullTenantTest {

    // ── TC-DALI-30: fail-fast enforcement ────────────────────────────────────

    @Test
    void requireTenantAlias_null_throwsMtn04() throws Exception {
        Method m = reflectRequire();
        assertThatThrownBy(() -> invoke(m, inputWithAlias(null), "execute"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("MTN-04");
    }

    @Test
    void requireTenantAlias_blank_throwsIllegalState() throws Exception {
        Method m = reflectRequire();
        assertThatThrownBy(() -> invoke(m, inputWithAlias("   "), "execute"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void requireTenantAlias_emptyString_throwsIllegalState() throws Exception {
        Method m = reflectRequire();
        assertThatThrownBy(() -> invoke(m, inputWithAlias(""), "execute"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void requireTenantAlias_valid_returnsAlias() throws Exception {
        Method m = reflectRequire();
        Object result = invoke(m, inputWithAlias("acme"), "execute");
        assertThat(result).isEqualTo("acme");
    }

    // ── TC-DALI-31: no hardcoded "default" tenant alias in job sources ────────

    /**
     * Scans every {@code .java} file under {@code services/dali/src/main/java/studio/seer/dali/job}
     * for suspicious patterns that would silently fall through to the {@code "default"} tenant:
     * <ul>
     *   <li>{@code tenantAlias.*"default"} — direct assignment or comparison</li>
     *   <li>{@code "default".*tenantAlias} — reverse form</li>
     *   <li>{@code getOrDefault.*"default"} — map lookups with implicit default</li>
     *   <li>{@code orElse.*"default"} — Optional.orElse("default")</li>
     * </ul>
     * Comment lines (trimmed start with {@code //}) are excluded.
     */
    @Test
    void jobSourceFiles_noHardcodedDefaultTenantFallback() throws IOException {
        // Resolve relative to project root; works from both IDE and Gradle
        Path jobSrc = resolveJobSourceDir();

        if (!Files.exists(jobSrc)) {
            // Running outside of a full checkout (e.g. library-only CI) — skip gracefully
            return;
        }

        List<String> violations;
        try (Stream<Path> walk = Files.walk(jobSrc)) {
            violations = walk
                    .filter(p -> p.toString().endsWith(".java"))
                    .flatMap(p -> {
                        try {
                            return Files.lines(p)
                                    .map(String::stripLeading)
                                    .filter(line -> !line.startsWith("//") && !line.contains("MTN-04-EXEMPT")) // exclude comments and explicit exemptions
                                    .filter(ParseJobMtNullTenantTest::isSuspiciousDefaultFallback)
                                    .map(line -> p.getFileName() + ": " + line.strip());
                        } catch (IOException e) {
                            return Stream.empty();
                        }
                    })
                    .collect(Collectors.toList());
        }

        assertThat(violations)
                .withFailMessage(
                    "MTN-04: Found hard-coded 'default' tenant fallback in job sources.\n" +
                    "Use TenantAwareJobDispatcher.requireTenantAlias() instead.\n" +
                    "Violations:\n  " + String.join("\n  ", violations))
                .isEmpty();
    }

    // ── helpers ────────────────────────────────────────────────────────────

    private static boolean isSuspiciousDefaultFallback(String line) {
        // Lines that contain both a "default" string literal and a tenant-routing keyword
        boolean hasDefaultLiteral = line.contains("\"default\"") || line.contains("'default'");
        boolean hasTenantKeyword  = line.toLowerCase().contains("tenant");
        boolean hasOrElse         = line.contains(".orElse(\"default\")");
        boolean hasGetOrDefault   = line.contains("getOrDefault") && line.contains("\"default\"");
        return (hasDefaultLiteral && hasTenantKeyword) || hasOrElse || hasGetOrDefault;
    }

    private static Path resolveJobSourceDir() {
        // Try Gradle working directory (project root) first, then Maven-style
        for (String base : new String[]{".", "services/dali"}) {
            Path p = Path.of(base, "src/main/java/studio/seer/dali/job");
            if (Files.isDirectory(p)) return p;
        }
        return Path.of("services/dali/src/main/java/studio/seer/dali/job");
    }

    private static ParseSessionInput inputWithAlias(String alias) {
        return new ParseSessionInput(
                "plsql", "/tmp/test.sql", true, false, false,
                null, null, null, null, null, alias);
    }

    private static Method reflectRequire() throws NoSuchMethodException {
        Method m = ParseJob.class.getDeclaredMethod(
                "requireTenantAlias", ParseSessionInput.class, String.class);
        m.setAccessible(true);
        return m;
    }

    private static Object invoke(Method m, Object... args) throws Exception {
        try {
            return m.invoke(null, args);
        } catch (InvocationTargetException ite) {
            Throwable cause = ite.getCause();
            if (cause instanceof RuntimeException re) throw re;
            throw new RuntimeException(cause);
        }
    }
}
