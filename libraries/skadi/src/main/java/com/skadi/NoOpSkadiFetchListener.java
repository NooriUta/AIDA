package com.skadi;

/**
 * No-op реализация SkadiFetchListener.
 * Используется в тестах и когда HEIMDALL недоступен.
 */
public final class NoOpSkadiFetchListener implements SkadiFetchListener {

    public static final NoOpSkadiFetchListener INSTANCE = new NoOpSkadiFetchListener();

    private NoOpSkadiFetchListener() {}
}
