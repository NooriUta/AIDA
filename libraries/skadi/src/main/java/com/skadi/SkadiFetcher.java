package com.skadi;

/**
 * Pull-based SQL script extractor.
 *
 * Каждый вызов fetchScripts() открывает НОВОЕ соединение через
 * DriverManager.getConnection() и закрывает его в finally-блоке.
 * Никакого внутреннего состояния между вызовами — реализации stateless.
 *
 * close() — no-op для JDBC-адаптеров (нет пула).
 *           ClickHouseSkadiFetcher: close() завершает java.net.http.HttpClient.
 */
public interface SkadiFetcher extends AutoCloseable {

    /**
     * Извлечь SQL-объекты согласно конфигурации.
     *
     * @param config  параметры соединения + фильтры
     * @return результат с листом извлечённых файлов и статистикой
     * @throws SkadiFetchConnectionException  если источник недоступен
     * @throws SkadiFetchTimeoutException     если запрос превысил таймаут
     * @throws SkadiFetchPermissionException  если недостаточно привилегий
     * @throws SkadiFetchException            любая другая ошибка
     */
    SkadiFetchResult fetchScripts(SkadiFetchConfig config) throws SkadiFetchException;

    /**
     * Лёгкий ping — проверить доступность источника.
     * Не бросает исключения — возвращает false при любой ошибке.
     *
     * @param config  параметры соединения (только jdbcUrl, user, password)
     */
    boolean ping(SkadiFetchConfig config);

    /**
     * Читаемое имя адаптера: "oracle", "postgresql", "clickhouse", "arcadedb".
     * Используется в логах, HEIMDALL событиях, SkadiFetcherRegistry.
     */
    String adapterName();

    /**
     * Освободить ресурсы.
     * JDBC-адаптеры: no-op (соединение закрывается в fetchScripts).
     * ClickHouseSkadiFetcher: завершает java.net.http.HttpClient.
     */
    @Override
    void close();
}
