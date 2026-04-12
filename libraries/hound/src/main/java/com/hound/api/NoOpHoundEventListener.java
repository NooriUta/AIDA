package com.hound.api;

/**
 * No-op implementation of {@link HoundEventListener}.
 *
 * <p>Use {@link #INSTANCE} as the default listener when no observation is needed:
 * <pre>{@code
 * ParseResult result = parser.parse(file, config, NoOpHoundEventListener.INSTANCE);
 * }</pre>
 */
public final class NoOpHoundEventListener implements HoundEventListener {

    /** Shared singleton — stateless and thread-safe. */
    public static final NoOpHoundEventListener INSTANCE = new NoOpHoundEventListener();

    private NoOpHoundEventListener() {}

    // All methods inherited as default no-ops from HoundEventListener.
}
