# Dali Core — Sprint 1

**Документ:** `SPRINT_DALI_CORE_S1`
**Версия:** 1.0
**Дата:** 12.04.2026
**Владелец:** Ты (Track A)
**Горизонт:** W2 Apr 27 → W5 May 18

---

## Зависимости (должны быть готовы до старта)

| Зависимость | Статус | Когда |
|---|---|---|
| C.1.1 HoundParser interface + ParseResult | ⏳ W1 | Apr 20-25 |
| C.1.2 HoundConfig + ArcadeWriteMode в `shared/dali-models/` | ⏳ W1 | Apr 20-25 |
| C.1.3 HoundEventListener | ⏳ W2 | Apr 27 |
| FRIGG :2481 running | ✅ DONE | — |

**Старт Dali Sprint 1:** Apr 27 (W2) — параллельно с завершением C.1.3-1.5.

---

## Цель спринта

UC1 работает end-to-end:
```
JobRunr cron trigger
  → HarvestJob запускает 12 subtask-ов
  → каждый Worker: JDBC fetch SQL → Hound.parseBatch() → YGG
  → HEIMDALL получает SESSION_STARTED / WORKER_ASSIGNED / JOB_COMPLETED
  → /metrics/snapshot: atomsExtracted > 0
```

---

## Структура проекта

```
services/dali/
├── build.gradle
└── src/main/java/studio/seer/dali/
    ├── DaliApplication.java
    ├── config/
    │   └── DaliConfig.java
    ├── job/
    │   ├── HarvestJob.java          ← главный job (parent)
    │   └── FileParseJob.java        ← subtask (один файл)
    ├── storage/
    │   └── ArcadeStorageProvider.java ← custom JobRunr StorageProvider
    ├── resource/
    │   ├── SessionResource.java     ← POST /sessions
    │   └── ControlResource.java     ← POST /control/reset|cancel
    ├── heimdall/
    │   └── DaliHeimdallEmitter.java ← fire-and-forget HTTP POST
    └── source/
        └── SqlSourceFetcher.java    ← JDBC / file system fetch
```

---

## DS-01 · Scaffold + build.gradle (~1 ч)

### `services/dali/build.gradle`

```groovy
plugins {
    id 'io.quarkus'
}

dependencies {
    // Hound как in-JVM library — прямой вызов, нулевой integration cost
    implementation project(':libraries:hound')
    implementation project(':shared:dali-models')

    // Quarkus
    implementation enforcedPlatform("io.quarkus.platform:quarkus-bom:3.34.2.Final")
    implementation 'io.quarkus:quarkus-rest-jackson'
    implementation 'io.quarkus:quarkus-arc'
    implementation 'io.quarkus:quarkus-scheduler'

    // JobRunr Open Source (не Pro)
    implementation 'org.jobrunr:quarkus-jobrunr:7.3.2'

    // ArcadeDB network client для FRIGG StorageProvider
    implementation 'com.arcadedb:arcadedb-network:26.3.2'

    testImplementation 'io.quarkus:quarkus-junit5'
}
```

### `src/main/resources/application.properties`

```properties
quarkus.http.port=9090
quarkus.application.name=dali

# JobRunr
org.jobrunr.background-job-server.enabled=true
org.jobrunr.background-job-server.worker-count=8
org.jobrunr.dashboard.enabled=true
org.jobrunr.dashboard.port=9091

# Dali config
dali.frigg.url=http://localhost:2481
dali.frigg.database=frigg
dali.frigg.user=root
dali.frigg.password=${FRIGG_ROOT_PASSWORD:playwithdata}

dali.ygg.url=http://localhost:2480
dali.ygg.database=hound
dali.ygg.user=root
dali.ygg.password=${YGG_ROOT_PASSWORD:playwithdata}

dali.heimdall.url=${HEIMDALL_URL:http://localhost:9093}

dali.sources.path=${SOURCES_PATH:/data/sql-sources}
```

---

## DS-02 · Custom ArcadeDB StorageProvider (~2 дня)

Самая сложная часть — JobRunr хранит состояние jobs в базе.
Нужно реализовать `StorageProvider` интерфейс поверх ArcadeDB.

### `storage/ArcadeStorageProvider.java`

```java
package studio.seer.dali.storage;

import org.jobrunr.storage.StorageProvider;
import org.jobrunr.jobs.*;
import org.jobrunr.jobs.states.StateName;
import org.jobrunr.storage.navigation.*;
import com.arcadedb.remote.RemoteDatabase;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.quarkus.runtime.Startup;

/**
 * Custom JobRunr StorageProvider для ArcadeDB (FRIGG).
 * Mirrors паттерн FriggGateway из HEIMDALL backend.
 *
 * Схема в FRIGG:
 *   DaliJob       — основная запись job (id, state, jobAsJson)
 *   DaliJobServer — registered background job servers
 *   DaliRecurringJob — cron/recurring jobs
 *   DaliMetadata  — metadata (total counts etc.)
 */
@ApplicationScoped
@Startup
public class ArcadeStorageProvider implements StorageProvider {

    @ConfigProperty(name = "dali.frigg.url")
    String friggUrl;

    @ConfigProperty(name = "dali.frigg.database")
    String database;

    @ConfigProperty(name = "dali.frigg.user")
    String user;

    @ConfigProperty(name = "dali.frigg.password")
    String password;

    private RemoteDatabase db;

    @jakarta.annotation.PostConstruct
    void init() {
        db = new RemoteDatabase(
            friggUrl.replace("http://", "").split(":")[0],
            Integer.parseInt(friggUrl.split(":")[2]),
            database, user, password
        );
        initSchema();
    }

    private void initSchema() {
        // Создать типы если не существуют
        executeIfNotExists("DaliJob",         "VERTEX", """
            CREATE PROPERTY DaliJob.jobId STRING;
            CREATE PROPERTY DaliJob.jobAsJson STRING;
            CREATE PROPERTY DaliJob.state STRING;
            CREATE PROPERTY DaliJob.createdAt LONG;
            CREATE PROPERTY DaliJob.updatedAt LONG;
            CREATE INDEX ON DaliJob (jobId) UNIQUE;
            CREATE INDEX ON DaliJob (state) NOTUNIQUE;
        """);
        executeIfNotExists("DaliJobServer",   "VERTEX", "");
        executeIfNotExists("DaliRecurringJob","VERTEX", """
            CREATE PROPERTY DaliRecurringJob.recurringJobId STRING;
            CREATE PROPERTY DaliRecurringJob.jobAsJson STRING;
            CREATE INDEX ON DaliRecurringJob (recurringJobId) UNIQUE;
        """);
        executeIfNotExists("DaliMetadata",    "DOCUMENT", "");
    }

    private void executeIfNotExists(String type, String kind, String props) {
        try {
            db.command("sql", "CREATE " + kind + " TYPE " + type);
            if (!props.isBlank()) {
                for (String stmt : props.split(";")) {
                    if (!stmt.isBlank()) db.command("sql", stmt.trim());
                }
            }
        } catch (Exception e) {
            // Тип уже существует — нормально
        }
    }

    @Override
    public Job save(Job job) {
        String json = jobMapper().serializeJob(job);
        db.command("sql",
            "UPDATE DaliJob SET jobAsJson = ?, state = ?, updatedAt = ? " +
            "UPSERT WHERE jobId = ?",
            json, job.getState().name(), System.currentTimeMillis(), job.getId().toString()
        );
        return job;
    }

    @Override
    public List<Job> save(List<Job> jobs) {
        db.begin();
        try {
            jobs.forEach(this::save);
            db.commit();
        } catch (Exception e) {
            db.rollback();
            throw e;
        }
        return jobs;
    }

    @Override
    public Job getJobById(UUID id) {
        var result = db.query("sql",
            "SELECT jobAsJson FROM DaliJob WHERE jobId = ?", id.toString());
        if (!result.hasNext()) throw new JobNotFoundException(id);
        String json = result.next().getProperty("jobAsJson");
        return jobMapper().deserializeJob(json);
    }

    @Override
    public List<Job> getJobs(StateName state, PageRequest page) {
        var result = db.query("sql",
            "SELECT jobAsJson FROM DaliJob WHERE state = ? ORDER BY createdAt SKIP ? LIMIT ?",
            state.name(), page.getOffset(), page.getLimit());
        List<Job> jobs = new ArrayList<>();
        while (result.hasNext())
            jobs.add(jobMapper().deserializeJob(result.next().getProperty("jobAsJson")));
        return jobs;
    }

    @Override
    public long countJobs(StateName state) {
        var result = db.query("sql",
            "SELECT count(*) as cnt FROM DaliJob WHERE state = ?", state.name());
        return result.hasNext() ? ((Number) result.next().getProperty("cnt")).longValue() : 0;
    }

    @Override
    public int deleteJobsByState(StateName state) {
        db.command("sql", "DELETE FROM DaliJob WHERE state = ?", state.name());
        return 0; // ArcadeDB не возвращает count — OK для JobRunr
    }

    @Override
    public void saveRecurringJob(RecurringJob job) {
        String json = jobMapper().serializeRecurringJob(job);
        db.command("sql",
            "UPDATE DaliRecurringJob SET jobAsJson = ? UPSERT WHERE recurringJobId = ?",
            json, job.getId());
    }

    @Override
    public List<RecurringJob> getRecurringJobs() {
        var result = db.query("sql", "SELECT jobAsJson FROM DaliRecurringJob");
        List<RecurringJob> jobs = new ArrayList<>();
        while (result.hasNext())
            jobs.add(jobMapper().deserializeRecurringJob(result.next().getProperty("jobAsJson")));
        return jobs;
    }

    @Override
    public void deleteRecurringJob(String id) {
        db.command("sql", "DELETE FROM DaliRecurringJob WHERE recurringJobId = ?", id);
    }

    // Остальные методы StorageProvider — базовые реализации
    @Override public void announceBackgroundJobServer(BackgroundJobServerStatus s) { /* TODO */ }
    @Override public boolean signalBackgroundJobServerAlive(BackgroundJobServerStatus s) { return true; }
    @Override public void signalBackgroundJobServerStopped(BackgroundJobServerStatus s) { /* TODO */ }
    @Override public List<BackgroundJobServerStatus> getBackgroundJobServers() { return List.of(); }
    @Override public UUID getLongestRunningBackgroundJobServerId() { return null; }
    @Override public int removeTimedOutBackgroundJobServers(Instant t) { return 0; }
    @Override public JobStats getJobStats() { return JobStats.empty(); }
    @Override public void publishToDashboard(int p, String d) { /* TODO */ }
    @Override public void close() { /* db.close() */ }
}
```

> **Примечание:** Полная реализация StorageProvider — 15+ методов. Выше показаны ключевые. Остальные (`getJobById(Set<UUID>)`, `getScheduledJobs()`, `getMetadata()` и т.д.) — аналогичный паттерн: SQL запрос → десериализация.

---

## DS-03 · HarvestJob + FileParseJob (~1.5 дня)

### `job/HarvestJob.java`

```java
package studio.seer.dali.job;

import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.BackgroundJob;
import studio.seer.dali.config.SourceConfig;
import studio.seer.dali.heimdall.DaliHeimdallEmitter;
import jakarta.inject.Inject;

/**
 * Главный job — parent для N subtask-ов.
 * Запускается по cron или вручную через REST.
 */
public class HarvestJob {

    @Inject DaliHeimdallEmitter heimdall;
    @Inject SourceConfig sources;

    @Job(name = "Harvest session %0", retries = 0)
    public void execute(String sessionId) {
        heimdall.emit("SESSION_STARTED", "INFO", sessionId,
            Map.of("sessionId", sessionId, "sources", sources.count(), "mode", "REMOTE_BATCH"));

        List<SourceConfig.Source> all = sources.all();
        for (int i = 0; i < all.size(); i++) {
            SourceConfig.Source src = all.get(i);
            // Enqueue subtask для каждого источника
            BackgroundJob.enqueue(
                () -> new FileParseJob().execute(sessionId, src.name(), src.dialect(), i)
            );
        }

        heimdall.emit("COMPLEX_JOB_PROGRESS", "INFO", sessionId,
            Map.of("done", 0, "total", all.size(), "atoms_so_far", 0));
    }
}
```

### `job/FileParseJob.java`

```java
package studio.seer.dali.job;

import com.hound.api.HoundParser;
import com.hound.api.HoundParserImpl;
import com.hound.heimdall.HoundHeimdallListener;
import com.aida.shared.HoundConfig;
import com.aida.shared.ArcadeWriteMode;
import org.jobrunr.jobs.annotations.Job;
import studio.seer.dali.heimdall.DaliHeimdallEmitter;
import studio.seer.dali.source.SqlSourceFetcher;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Inject;
import java.nio.file.Path;
import java.util.List;

/**
 * Subtask — парсит один источник данных.
 * Вызывает Hound как in-JVM library (прямой Java call).
 */
public class FileParseJob {

    @Inject DaliHeimdallEmitter heimdall;
    @Inject SqlSourceFetcher fetcher;

    @ConfigProperty(name = "dali.ygg.url")    String yggUrl;
    @ConfigProperty(name = "dali.ygg.user")   String yggUser;
    @ConfigProperty(name = "dali.ygg.password") String yggPassword;

    @Job(name = "Parse %1 (%0)", retries = 2)
    public void execute(String sessionId, String sourceName, String dialect, int workerIndex) {
        heimdall.emit("WORKER_ASSIGNED", "INFO", sessionId,
            Map.of("worker_id", workerIndex, "source", sourceName));

        long start = System.currentTimeMillis();
        try {
            // 1. Получить SQL файлы из источника
            List<Path> files = fetcher.fetchFiles(sourceName);

            // 2. Собрать HoundConfig
            HoundConfig config = HoundConfig.builder()
                .dialect(dialect)
                .targetSchema(sourceName.toLowerCase().replaceAll("[^a-z0-9]", "_"))
                .writeMode(ArcadeWriteMode.REMOTE_BATCH)
                .arcadeUrl(yggUrl)
                .arcadeUser(yggUser)
                .arcadePassword(yggPassword)
                .workerThreads(2)  // каждый worker использует 2 потока Hound
                .build();

            // 3. Вызвать Hound — in-JVM, HoundHeimdallListener уже внутри
            HoundParser parser = new HoundParserImpl();
            var results = parser.parseBatch(files, config);

            // 4. Агрегировать результаты
            int totalAtoms = results.stream().mapToInt(r -> r.atomCount()).sum();
            long successCount = results.stream().filter(r -> r.success()).count();

            heimdall.emit("JOB_COMPLETED", "INFO", sessionId,
                Map.of(
                    "source",      sourceName,
                    "atoms",       totalAtoms,
                    "files",       files.size(),
                    "success",     successCount,
                    "dur_ms",      System.currentTimeMillis() - start
                ));

        } catch (Exception e) {
            heimdall.emit("JOB_FAILED", "ERROR", sessionId,
                Map.of("source", sourceName, "error", e.getMessage()));
            throw new RuntimeException("Parse failed for " + sourceName, e);
        }
    }
}
```

---

## DS-04 · HEIMDALL emitter (~1 ч)

```java
package studio.seer.dali.heimdall;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.net.URI; import java.net.http.*;
import java.time.Duration; import java.util.Map;
import java.util.stream.Collectors;

/**
 * Fire-and-forget HTTP POST к HEIMDALL.
 * Никогда не бросает исключений — HEIMDALL down не ломает Dali.
 */
@ApplicationScoped
public class DaliHeimdallEmitter {

    @ConfigProperty(name = "dali.heimdall.url")
    String heimdallUrl;

    private final HttpClient http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(2)).build();

    public void emit(String eventType, String level, String sessionId,
                     Map<String, Object> payload) {
        try {
            String body = buildJson(eventType, level, sessionId, payload);
            http.sendAsync(
                HttpRequest.newBuilder()
                    .uri(URI.create(heimdallUrl + "/events"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(2)).build(),
                HttpResponse.BodyHandlers.discarding()
            ).whenComplete((r, ex) -> {
                if (ex != null)
                    Log.warnf("HEIMDALL emit failed (non-critical): %s", ex.getMessage());
            });
        } catch (Exception ignored) {}
    }

    private String buildJson(String type, String level, String sid,
                              Map<String, Object> payload) {
        String p = payload.entrySet().stream()
            .map(e -> "\"" + e.getKey() + "\":" + toJson(e.getValue()))
            .collect(Collectors.joining(","));
        return String.format(
            "{\"timestamp\":%d,\"sourceComponent\":\"dali\",\"eventType\":\"%s\"," +
            "\"level\":\"%s\",\"sessionId\":\"%s\",\"correlationId\":null," +
            "\"durationMs\":0,\"payload\":{%s}}",
            System.currentTimeMillis(), type, level,
            sid != null ? sid : "", p
        );
    }

    private String toJson(Object v) {
        if (v instanceof String s)  return "\"" + s.replace("\"", "\\\"") + "\"";
        if (v instanceof Boolean b) return b.toString();
        return String.valueOf(v);
    }
}
```

---

## DS-05 · REST endpoints (~1 ч)

```java
package studio.seer.dali.resource;

import org.jobrunr.scheduling.BackgroundJob;
import studio.seer.dali.job.HarvestJob;
import studio.seer.dali.heimdall.DaliHeimdallEmitter;
import jakarta.inject.Inject; import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.UUID;

@Path("/sessions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SessionResource {

    @Inject DaliHeimdallEmitter heimdall;

    /**
     * POST /sessions — запустить harvest вручную (on-demand).
     * Вызывается из SHUTTLE MutationResource.startParseSession()
     */
    @POST
    public SessionResponse startSession(SessionRequest req) {
        String sessionId = UUID.randomUUID().toString();

        BackgroundJob.enqueue(() ->
            new HarvestJob().execute(sessionId)
        );

        return new SessionResponse(sessionId, "enqueued");
    }

    record SessionRequest(String sourceFilter) {}
    record SessionResponse(String sessionId, String status) {}
}

// ─────────────────────────────────────────────────────────

@Path("/control")
@Produces(MediaType.APPLICATION_JSON)
public class ControlResource {

    @Inject DaliHeimdallEmitter heimdall;

    /**
     * POST /control/reset — отменить все активные сессии.
     * Вызывается из HEIMDALL ControlResource → HeimdallControlClient
     */
    @POST @Path("/reset")
    public Map<String, String> reset() {
        // Удалить все enqueued/processing jobs
        // JobRunr API: storageProvider.deleteJobsByState()
        heimdall.emit("SESSION_FAILED", "WARN", null,
            Map.of("reason", "demo_reset", "source", "control_api"));
        return Map.of("status", "reset");
    }

    @POST @Path("/cancel/{sessionId}")
    public Map<String, String> cancel(@PathParam("sessionId") String sessionId) {
        // TODO: lookup jobs by sessionId label, delete them
        heimdall.emit("SESSION_FAILED", "WARN", sessionId,
            Map.of("reason", "cancelled"));
        return Map.of("status", "cancelled", "sessionId", sessionId);
    }
}
```

---

## DS-06 · Cron trigger (~30 мин)

```java
package studio.seer.dali.job;

import io.quarkus.scheduler.Scheduled;
import org.jobrunr.scheduling.BackgroundJob;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class HarvestScheduler {

    /**
     * UC1: Scheduled harvest — каждую ночь в 02:00.
     * В dev: каждые 5 минут для тестирования.
     */
    @Scheduled(cron = "${dali.harvest.cron:0 2 * * *}")
    void triggerNightlyHarvest() {
        String sessionId = "scheduled-" + UUID.randomUUID();
        BackgroundJob.enqueue(() -> new HarvestJob().execute(sessionId));
    }
}
```

```properties
# application.properties — добавить для dev/test
%dev.dali.harvest.cron=0/5 * * * *    # каждые 5 минут в dev
%test.dali.harvest.cron=off
```

---

## DS-07 · SqlSourceFetcher (~1 ч)

```java
package studio.seer.dali.source;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.io.IOException; import java.nio.file.*; import java.util.List;

/**
 * Получить список SQL файлов для указанного источника.
 * M1: из file system (mount point).
 * M2+: JDBC fetch из external DB.
 */
@ApplicationScoped
public class SqlSourceFetcher {

    @ConfigProperty(name = "dali.sources.path")
    String sourcesPath;

    /**
     * Вернуть список .sql файлов для источника.
     * Структура: /data/sql-sources/{sourceName}/*.sql
     */
    public List<Path> fetchFiles(String sourceName) throws IOException {
        Path sourceDir = Path.of(sourcesPath, sanitize(sourceName));
        if (!Files.exists(sourceDir)) {
            throw new IOException("Source directory not found: " + sourceDir);
        }
        try (var stream = Files.walk(sourceDir)) {
            return stream
                .filter(p -> p.toString().endsWith(".sql"))
                .sorted()
                .toList();
        }
    }

    private String sanitize(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9_\\-]", "_");
    }
}
```

---

## DS-08 · docker-compose + Docker (~30 мин)

### `frontends/dali/Dockerfile`

```dockerfile
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY . .
RUN ./gradlew :services:dali:quarkusBuild -Dquarkus.package.type=uber-jar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/services/dali/build/quarkus-app/ ./
EXPOSE 9090 9091
CMD ["java", "-jar", "quarkus-run.jar"]
```

### `docker-compose.yml` — добавить:

```yaml
  dali:
    build:
      context: .
      dockerfile: services/dali/Dockerfile
    ports:
      - "9090:9090"   # REST API
      - "29091:9091"  # JobRunr dashboard
    environment:
      - FRIGG_ROOT_PASSWORD=${FRIGG_ROOT_PASSWORD:-playwithdata}
      - YGG_ROOT_PASSWORD=${YGG_ROOT_PASSWORD:-playwithdata}
      - HEIMDALL_URL=http://heimdall-backend:9093
      - SOURCES_PATH=/data/sql-sources
    volumes:
      - ./data/sql-sources:/data/sql-sources:ro
    depends_on:
      frigg:
        condition: service_healthy
      heimdall-backend:
        condition: service_started
```

---

## Расписание

```
W2 Apr 27:
  [ ] DS-01 Scaffold + build.gradle
  [ ] DS-04 DaliHeimdallEmitter
  [ ] DS-07 SqlSourceFetcher (file mode)
  Итог: ./gradlew :services:dali:quarkusDev → стартует на :9090

W3 May 4:
  [ ] DS-02 ArcadeStorageProvider (сложная часть, ~2 дня)
  [ ] DS-06 HarvestScheduler (cron)
  Итог: JobRunr dashboard открывается, cron job регистрируется

W4 May 11:
  [ ] DS-03 HarvestJob + FileParseJob
  [ ] DS-05 SessionResource + ControlResource
  Итог: POST /sessions → job запускается, Hound парсит файлы

W5 May 18:
  [ ] DS-08 Docker + docker-compose
  [ ] Integration: SHUTTLE → POST /sessions → Dali → Hound → YGG
  [ ] HEIMDALL WS показывает SESSION_STARTED + WORKER_ASSIGNED + JOB_COMPLETED
```

---

## Definition of Done Sprint 1

```bash
# Сервис стартует
./gradlew :services:dali:quarkusDev
curl http://localhost:9090/q/health  → {"status":"UP"}

# JobRunr dashboard
open http://localhost:29091  → видны jobs

# Manual harvest
curl -X POST http://localhost:9090/sessions \
  -H "Content-Type: application/json" \
  -d '{"sourceFilter": null}'
# → {"sessionId":"...", "status":"enqueued"}

# Проверить что Hound вызвался
curl http://localhost:9093/metrics/snapshot
# → atomsExtracted > 0

# HEIMDALL events
wscat -c ws://localhost:9093/ws/events
# → SESSION_STARTED, WORKER_ASSIGNED, JOB_COMPLETED от "dali"

# FRIGG хранит job state
# JobRunr dashboard: job перешёл в SUCCEEDED
```

---

## Ключевые решения (зафиксированы ранее)

| Решение | Источник |
|---|---|
| Hound = in-JVM library call (не subprocess) | ADR-DA-008, INTEGRATIONS_MATRIX I14 |
| FRIGG = custom ArcadeDB StorageProvider (Вариант A) | MODULES_TECH_STACK §3.10 |
| JobRunr Open Source (не Pro) | ADR-DA-008 |
| REMOTE_BATCH как default writeMode | HoundConfig.devDefaults() |
| Fire-and-forget HEIMDALL emit | Q25 Variant A |

---

## История изменений

| Дата | Версия | Что |
|---|---|---|
| 12.04.2026 | 1.0 | Initial. UC1 Scheduled harvest. DS-01..DS-08. Старт W2 Apr 27 (после C.1.1-1.2). |
