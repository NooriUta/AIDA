package com.skadi;

import java.util.List;

/**
 * Результат одного вызова SkadiFetcher.fetchScripts().
 *
 * Dali передаёт содержимое в HoundParser через SqlSource.FromText —
 * никаких временных файлов. SQL-тексты хранятся в памяти только на
 * время одного ParseJob. Долгосрочный архив — DaliSourceFile в YGG
 * (hound_src_&lt;tenantId&gt;).
 *
 * @see AIDA_DB_TOPOLOGY.md § DaliSourceFile
 * @see SKADI_SPEC.md § 8.3 SqlSource sealed interface
 */
public record SkadiFetchResult(

    /** Извлечённые SQL-объекты. */
    List<SkadiFetchedFile> files,

    /** Статистика выполнения. */
    FetchStats stats

) {

    /**
     * Статистика извлечения — для HEIMDALL событий и логирования.
     *
     * @param totalFetched   число успешно извлечённых объектов
     * @param errors         число ошибок при извлечении отдельных объектов
     * @param durationMs     полное время выполнения fetchScripts() в мс
     * @param sourceAdapter  имя адаптера ("oracle", "postgresql", "clickhouse")
     */
    public record FetchStats(
            int totalFetched,
            int errors,
            long durationMs,
            String sourceAdapter
    ) {
        public boolean hasErrors() { return errors > 0; }

        @Override
        public String toString() {
            return "FetchStats{fetched=%d, errors=%d, duration=%dms, adapter=%s}"
                    .formatted(totalFetched, errors, durationMs, sourceAdapter);
        }
    }
}
