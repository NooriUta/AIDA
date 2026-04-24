package studio.seer.dali.job;

import org.junit.jupiter.api.Test;
import studio.seer.shared.ParseSessionInput;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MTN-04: Verifies {@link ParseJob} refuses to run without a tenant alias.
 *
 * <p>Previously the job silently defaulted to {@code "default"} which was a
 * hidden cross-tenant leak path — if a cron dispatcher forgot to inject the
 * alias, the parse output was written into {@code hound_default} regardless of
 * who triggered it.
 *
 * <p>Reflective access to the private {@code requireTenantAlias} helper is OK
 * here because the surface we want to lock down is tiny and well-scoped; a
 * public API would leak implementation details into the package surface.
 */
class ParseJobTenantDispatchTest {

    @Test
    void requireTenantAlias_null_throwsFailFast() throws Exception {
        var input = inputWithAlias(null);
        Method m = reflectRequire();

        assertThatThrownBy(() -> invoke(m, input, "test-context"))
                .hasCauseInstanceOf(IllegalStateException.class)
                .hasRootCauseMessage(
                    "MTN-04 [test-context]: ParseSessionInput.tenantAlias is required " +
                    "(refusing to default). TenantAwareJobDispatcher must inject alias at enqueue time.");
    }

    @Test
    void requireTenantAlias_blank_throwsFailFast() throws Exception {
        var input = inputWithAlias("   ");
        Method m = reflectRequire();

        assertThatThrownBy(() -> invoke(m, input, "blank"))
                .hasCauseInstanceOf(IllegalStateException.class);
    }

    @Test
    void requireTenantAlias_valid_returnsAlias() throws Exception {
        var input = inputWithAlias("acme");
        Method m = reflectRequire();

        Object result = invoke(m, input, "ok");
        assertThat(result).isEqualTo("acme");
    }

    // ── helpers ────────────────────────────────────────────────────────────

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
        } catch (java.lang.reflect.InvocationTargetException ite) {
            // Unwrap to match assertThatThrownBy expectations on hasCauseInstanceOf
            throw new RuntimeException(ite.getCause());
        }
    }
}
