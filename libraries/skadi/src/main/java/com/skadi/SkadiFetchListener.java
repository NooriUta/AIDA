package com.skadi;

import java.util.Set;

/**
 * Listener для событий SKADI fetch.
 * Все методы имеют default no-op реализацию.
 *
 * Dali подключает DaliSkadiFetchListener, который транслирует события в HEIMDALL.
 */
public interface SkadiFetchListener {

    /** Вызывается в начале fetchScripts(), до первого запроса к источнику. */
    default void onFetchStarted(String sourceAdapter, String schema,
                                Set<SkadiFetchConfig.ObjectType> objectTypes) {}

    /** Вызывается после успешного извлечения каждого объекта. */
    default void onObjectFetched(String name,
                                 SkadiFetchConfig.ObjectType objectType,
                                 long sizeBytes) {}

    /** Вызывается при ошибке извлечения конкретного объекта (per-object fallback). */
    default void onFetchError(String name,
                              SkadiFetchConfig.ObjectType objectType,
                              Exception error) {}

    /** Вызывается после завершения всех объектов (включая объекты с ошибками). */
    default void onFetchCompleted(SkadiFetchResult.FetchStats stats) {}
}
