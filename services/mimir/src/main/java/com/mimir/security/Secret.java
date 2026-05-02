package com.mimir.security;

import java.util.Arrays;
import java.util.Objects;

/**
 * Sensitive string wrapper — passwords, API keys, tokens.
 *
 * <p>Why not plain {@code String}: passwords as String accidentally land in:
 * <ul>
 *     <li>{@code log.info("config: " + cfg)} — String concat dumps password</li>
 *     <li>Stacktrace messages including config record toString()</li>
 *     <li>Jackson serialization of records</li>
 *     <li>Mockito argument captors with verbose logging</li>
 *     <li>JVM heap dumps grep-able</li>
 * </ul>
 *
 * <p>This wrapper:
 * <ul>
 *     <li>Stores value as {@code char[]} (avoids String pool interning)</li>
 *     <li>{@code toString()} returns {@code "***"} — never raw</li>
 *     <li>Explicit {@link #reveal()} required to extract — visible in code review</li>
 *     <li>{@link #wipe()} zeros out memory (best effort — JVM may have copies)</li>
 *     <li>{@code equals}/{@code hashCode} based on contents, time-constant compare</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>{@code
 * @ConfigProperty(name = "mimir.ygg.password", defaultValue = "playwithdata")
 * String passwordRaw;  // Quarkiverse can't read Secret directly
 *
 * private Secret password() { return Secret.of(passwordRaw); }
 *
 * // At HTTP basic auth use site:
 * String creds = user + ":" + password().reveal();
 * }</pre>
 */
public final class Secret {

    private static final Secret EMPTY = new Secret(new char[0]);

    private final char[] value;

    private Secret(char[] value) {
        this.value = value;
    }

    /** Factory — null and empty strings produce {@link #empty()}. */
    public static Secret of(String s) {
        if (s == null || s.isEmpty()) return EMPTY;
        return new Secret(s.toCharArray());
    }

    public static Secret empty() {
        return EMPTY;
    }

    /** Reveal raw value as String. Use sparingly — only at HTTP/API call sites. */
    public String reveal() {
        return new String(value);
    }

    public boolean isEmpty() {
        return value.length == 0;
    }

    public int length() {
        return value.length;
    }

    /** Best-effort memory wipe. Call when secret no longer needed. */
    public void wipe() {
        Arrays.fill(value, '\0');
    }

    /** Time-constant equality to prevent timing-based oracles. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Secret other)) return false;
        if (this.value.length != other.value.length) return false;
        int diff = 0;
        for (int i = 0; i < value.length; i++) {
            diff |= value[i] ^ other.value[i];
        }
        return diff == 0;
    }

    @Override
    public int hashCode() {
        // Don't expose actual content via hash
        return Objects.hash(value.length);
    }

    /** ALWAYS masked — never reveals content. */
    @Override
    public String toString() {
        return "***";
    }

    /** Masked preview for audit logs: {@code sk-***-1234}. Returns "***" if too short. */
    public String mask() {
        if (value.length < 8) return "***";
        return new String(value, 0, 2) + "-***-" + new String(value, value.length - 4, 4);
    }
}
