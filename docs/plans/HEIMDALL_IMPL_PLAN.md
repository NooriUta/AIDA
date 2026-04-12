# HEIMDALL — План реализации: Sprint 1 + Sprint 2

## Контекст

HEIMDALL — Admin Control Panel для всей платформы AIDA (ADR-DA-010). Спринт-план описан в `docs/sprints/HEIMDALL_SPRINT_PLAN.md`. Этот документ переводит план в конкретные файловые задачи с учётом архитектурных решений из `docs/architecture/MODULES_TECH_STACK.md` и `docs/architecture/REFACTORING_PLAN.md`.

**Текущее состояние:**
- Миграция монорепо (REPO_MIGRATION_PLAN фазы 1-4) **уже выполнена** (подтверждено git history)
- В `services/` только SHUTTLE (Quarkus 3.34.2, :8080)
- `shared/dali-models/` не существует (строка закомментирована в `settings.gradle`)
- HEIMDALL-сервис и фронтенд отсутствуют
- Порт :9093 свободен
- **FRIGG** = отдельный ArcadeDB на **:2481** (HoundArcade/YGG — это :2480, другой инстанс)

**Стек подтверждён:** Java 21 + Quarkus 3.34.2 (совпадает с SHUTTLE), WebSocket через `quarkus-websockets-next` API — **не** legacy `@ServerEndpoint` (в HEIMDALL_SPRINT_PLAN в примерах кода использован старый API, но задекларирован Next extension — исправлено в данном плане).

**Модель авторизации (MODULES_TECH_STACK §3.6, C.3.1):**
Chur — единственная граница безопасности. Chur проверяет JWT (Keycloak), scope (`aida:admin`, `aida:admin:destructive`) и проксирует запросы в HEIMDALL. HEIMDALL доверяет forwarded-заголовкам `X-Seer-Role` / `X-Seer-User` — **`quarkus-oidc` в HEIMDALL не нужен** для Sprint 1-2 (паттерн аналогичен SHUTTLE).

**Предварительные задачи из REFACTORING_PLAN (параллельно/до Sprint 1):**
- **C.5.2**: Переименовать Keycloak-клиент `verdandi-bff` → `aida-bff`, добавить scopes `aida:admin`, `aida:admin:destructive`
- **C.0**: Upgrade ArcadeDB (влияет на паттерн подключения FRIGG в SnapshotManager)

**Ключевое решение — Docker build context для HEIMDALL:**
HEIMDALL зависит от `shared:dali-models`. В отличие от SHUTTLE (self-contained), Docker-сборка должна включать оба модуля. Решение: Dockerfile лежит в `services/heimdall-backend/`, но docker-compose использует `context: .` (корень монорепо). Два Docker-only файла в `services/heimdall-backend/`:
- `docker-settings.gradle` — только нужные модули (`shared:dali-models` + `services:heimdall-backend`)
- `docker-build.gradle` — пустой root build (без Node-задач из основного `build.gradle`)

---

## Шаг -1 — Создать git feature branch

**Самое первое действие** — создать ветку для всей разработки HEIMDALL:

```bash
git checkout -b feature/heimdall-sprint1
```

Вся работа по Sprint 1 и Sprint 2 ведётся в этой ветке. PR в `master` — после Definition of Done Sprint 2 (или раньше, после Sprint 1, по решению).

---

## Шаг 0 — Сохранить план в docs/

**Первое действие после создания ветки** (до написания кода):
Скопировать этот план в `docs/plans/HEIMDALL_IMPL_PLAN.md` чтобы он был частью документации проекта наряду с `docs/sprints/HEIMDALL_SPRINT_PLAN.md`.

---

## Sprint 1 — Скелет + Event Pipeline

**Цель:** Принимать события по HTTP, хранить в ring buffer, стримить по WebSocket. Верифицируется в изоляции без других сервисов.

---

### Шаг 1.1 — Модуль `shared/dali-models/` (каноническая схема)

**Создать файлы:**

`shared/dali-models/build.gradle`
```groovy
plugins { id 'java' }
group   = 'studio.seer.shared'
version = '1.0.0-SNAPSHOT'
java { sourceCompatibility = JavaVersion.VERSION_21; targetCompatibility = JavaVersion.VERSION_21 }
repositories { mavenCentral() }
// Нет внешних зависимостей — чистые Java records и enums
```

`shared/dali-models/src/main/java/studio/seer/shared/EventLevel.java`
```java
package studio.seer.shared;
public enum EventLevel { INFO, WARN, ERROR }
```

`shared/dali-models/src/main/java/studio/seer/shared/EventType.java`
```java
package studio.seer.shared;
public enum EventType {
    FILE_PARSING_STARTED, FILE_PARSING_COMPLETED, FILE_PARSING_FAILED,
    ATOM_EXTRACTED, RESOLUTION_COMPLETED,
    SESSION_STARTED, SESSION_COMPLETED, SESSION_FAILED,
    WORKER_ASSIGNED, JOB_ENQUEUED, JOB_COMPLETED, COMPLEX_JOB_PROGRESS,
    QUERY_RECEIVED, TIER_SELECTED,
    TOOL_CALL_STARTED, TOOL_CALL_COMPLETED, LLM_RESPONSE_READY, CACHE_HIT,
    TRAVERSAL_STARTED, TRAVERSAL_PROGRESS, TRAVERSAL_COMPLETED,
    REQUEST_RECEIVED, REQUEST_COMPLETED, SUBSCRIPTION_OPENED,
    DEMO_RESET, SNAPSHOT_SAVED, REPLAY_STARTED
}
```

`shared/dali-models/src/main/java/studio/seer/shared/HeimdallEvent.java`
```java
package studio.seer.shared;
import java.util.Map;
public record HeimdallEvent(
    long                timestamp,       // unix ms
    String              sourceComponent, // "hound"|"dali"|"mimir"|"anvil"|"shuttle"|"verdandi"
    String              eventType,       // EventType.name()
    EventLevel          level,
    String              sessionId,       // nullable
    String              userId,          // nullable
    String              correlationId,   // nullable
    long                durationMs,      // 0 если не применимо
    Map<String, Object> payload
) {
    /** Фабрика для внутренних событий HEIMDALL */
    public static HeimdallEvent internal(EventType type, String message) {
        return new HeimdallEvent(
            System.currentTimeMillis(), "heimdall", type.name(),
            EventLevel.INFO, null, null, null, 0, Map.of("message", message)
        );
    }
}
```

**Изменить файл:**

`settings.gradle` — раскомментировать `shared:dali-models` И добавить HEIMDALL:
```groovy
include 'libraries:hound'
include 'services:shuttle'
include 'shared:dali-models'
include 'services:heimdall-backend'
```

---

### Шаг 1.2 — Скаффолд `services/heimdall-backend/` (Quarkus)

Структура директорий:
```
services/heimdall-backend/
├── build.gradle
├── gradle.properties          ← версия Quarkus (как у shuttle)
├── docker-settings.gradle     ← только для Docker: минимальный список модулей
├── docker-build.gradle        ← только для Docker: пустой root build
├── Dockerfile
├── .dockerignore
├── .gitignore
└── src/main/resources/
    └── application.properties
```

`services/heimdall-backend/gradle.properties`
```properties
quarkusPlatformGroupId=io.quarkus.platform
quarkusPlatformArtifactId=quarkus-bom
quarkusPlatformVersion=3.34.2
```

`services/heimdall-backend/build.gradle`
```groovy
plugins {
    id 'java'
    id 'io.quarkus' version "${quarkusPlatformVersion}"
}

group   = 'studio.seer.heimdall'
version = '1.0.0-SNAPSHOT'

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories { mavenCentral() }

dependencies {
    implementation enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}")
    implementation 'io.quarkus:quarkus-rest'
    implementation 'io.quarkus:quarkus-rest-jackson'
    implementation 'io.quarkus:quarkus-websockets-next'
    implementation 'io.quarkus:quarkus-micrometer'
    implementation 'io.quarkus:quarkus-arc'
    implementation project(':shared:dali-models')   // HeimdallEvent, EventType, EventLevel

    testImplementation 'io.quarkus:quarkus-junit5'
}

compileJava.options.encoding     = 'UTF-8'
compileTestJava.options.encoding = 'UTF-8'
```

`services/heimdall-backend/src/main/resources/application.properties`
```properties
quarkus.http.port=9093
quarkus.http.host=0.0.0.0
quarkus.application.name=heimdall-backend

# Ring buffer
heimdall.ring-buffer.capacity=10000

# CORS — Chur proxy и локальная разработка
quarkus.http.cors=true
quarkus.http.cors.origins=http://localhost:3000,http://localhost:13000

quarkus.log.level=INFO
quarkus.log.category."studio.seer".level=DEBUG
```

`services/heimdall-backend/docker-settings.gradle`
```groovy
pluginManagement {
    repositories { mavenCentral(); gradlePluginPortal() }
}
rootProject.name = 'aida-root'
include 'shared:dali-models'
include 'services:heimdall-backend'
```

`services/heimdall-backend/docker-build.gradle`
```groovy
// Намеренно пустой — исключает Node-задачи из root build.gradle
// при Docker-сборке, которой нужны только JVM-модули.
```

`services/heimdall-backend/Dockerfile` (context = корень монорепо)
```dockerfile
FROM eclipse-temurin:21-jdk-alpine AS builder
RUN apk add --no-cache dos2unix
WORKDIR /app
# Gradle-инфраструктура монорепо
COPY gradle                                            gradle
COPY gradlew                                           gradlew
COPY gradle.properties                                 gradle.properties
# Docker-only overrides
COPY services/heimdall-backend/docker-settings.gradle  settings.gradle
COPY services/heimdall-backend/docker-build.gradle     build.gradle
# Исходные модули, нужные сервису
COPY shared/dali-models                                shared/dali-models
COPY services/heimdall-backend/build.gradle            services/heimdall-backend/build.gradle
COPY services/heimdall-backend/gradle.properties       services/heimdall-backend/gradle.properties
COPY services/heimdall-backend/src                     services/heimdall-backend/src
RUN dos2unix gradlew && chmod +x gradlew
RUN ./gradlew :services:heimdall-backend:build -x test \
    -Dquarkus.package.jar.type=uber-jar

FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app
COPY --from=builder /app/services/heimdall-backend/build/*-runner.jar app.jar
EXPOSE 9093
ENV JAVA_OPTS="-Djava.util.logging.manager=org.jboss.logmanager.LogManager"
CMD ["java", "-jar", "app.jar"]
```

---

### Шаг 1.3 — RingBuffer

`services/heimdall-backend/src/main/java/studio/seer/heimdall/RingBuffer.java`

```java
package studio.seer.heimdall;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import studio.seer.shared.EventType;
import studio.seer.shared.HeimdallEvent;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@ApplicationScoped
public class RingBuffer {

    @ConfigProperty(name = "heimdall.ring-buffer.capacity", defaultValue = "10000")
    int capacity;

    private final ArrayDeque<HeimdallEvent> buffer = new ArrayDeque<>();
    private final List<Consumer<HeimdallEvent>> subscribers = new CopyOnWriteArrayList<>();

    public synchronized void push(HeimdallEvent event) {
        if (buffer.size() >= capacity) buffer.pollFirst();  // evict oldest
        buffer.addLast(event);
        subscribers.forEach(s -> s.accept(event));
    }

    public synchronized List<HeimdallEvent> snapshot() { return List.copyOf(buffer); }

    public synchronized List<HeimdallEvent> since(long timestampMs) {
        return buffer.stream().filter(e -> e.timestamp() >= timestampMs).toList();
    }

    public synchronized void clear() {
        buffer.clear();
        push(HeimdallEvent.internal(EventType.DEMO_RESET, "Ring buffer cleared"));
    }

    public void subscribe(Consumer<HeimdallEvent> subscriber)   { subscribers.add(subscriber); }
    public void unsubscribe(Consumer<HeimdallEvent> subscriber) { subscribers.remove(subscriber); }
}
```

---

### Шаг 1.4 — EventResource (HTTP ingestion)

`services/heimdall-backend/src/main/java/studio/seer/heimdall/resource/EventResource.java`

```java
package studio.seer.heimdall.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import studio.seer.heimdall.RingBuffer;
import studio.seer.shared.HeimdallEvent;
import java.util.List;

@Path("/events")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class EventResource {

    @Inject RingBuffer ringBuffer;

    @POST
    public Response ingest(HeimdallEvent event) {
        if (event == null || event.sourceComponent() == null || event.eventType() == null)
            return Response.status(400).build();
        // Обогащаем timestamp если не передан
        var enriched = event.timestamp() > 0 ? event
            : new HeimdallEvent(System.currentTimeMillis(),
                event.sourceComponent(), event.eventType(), event.level(),
                event.sessionId(), event.userId(), event.correlationId(),
                event.durationMs(), event.payload());
        ringBuffer.push(enriched);
        return Response.accepted().build();  // 202 fire-and-forget
    }

    @POST @Path("/batch")
    public Response ingestBatch(List<HeimdallEvent> events) {
        if (events == null) return Response.status(400).build();
        events.stream()
            .filter(e -> e != null && e.sourceComponent() != null && e.eventType() != null)
            .forEach(ringBuffer::push);
        return Response.accepted().build();
    }
}
```

---

### Шаг 1.5 — EventStreamEndpoint (WebSocket, quarkus-websockets-next API)

`services/heimdall-backend/src/main/java/studio/seer/heimdall/ws/EventStreamEndpoint.java`

> Примечание: HEIMDALL_SPRINT_PLAN использует legacy `@ServerEndpoint` + `Session` (Jakarta WS),  
> но задекларирован `quarkus-websockets-next`. Здесь используется **правильный Next API**.

```java
package studio.seer.heimdall.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.websockets.next.*;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import studio.seer.heimdall.RingBuffer;
import studio.seer.shared.HeimdallEvent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@WebSocket(path = "/ws/events")
@ApplicationScoped
public class EventStreamEndpoint {

    @Inject RingBuffer ringBuffer;
    @Inject ObjectMapper mapper;

    private final ConcurrentHashMap<String, Consumer<HeimdallEvent>> subscribers =
        new ConcurrentHashMap<>();

    @OnOpen
    public Uni<Void> onOpen(WebSocketConnection connection) {
        Consumer<HeimdallEvent> subscriber = event -> {
            if (connection.isOpen()) {
                connection.sendText(toJson(event))
                    .subscribe().with(__ -> {}, err -> cleanup(connection.id()));
            }
        };
        subscribers.put(connection.id(), subscriber);
        ringBuffer.subscribe(subscriber);

        // Cold-start replay: последние 200 событий при подключении
        var replay = ringBuffer.snapshot().stream().limit(200).toList();
        if (replay.isEmpty()) return Uni.createFrom().voidItem();
        return Multi.createFrom().iterable(replay)
            .flatMap(e -> Multi.createFrom().uni(connection.sendText(toJson(e))))
            .toUni().replaceWithVoid();
    }

    @OnClose
    public void onClose(WebSocketConnection connection) {
        cleanup(connection.id());
    }

    private void cleanup(String connectionId) {
        Consumer<HeimdallEvent> sub = subscribers.remove(connectionId);
        if (sub != null) ringBuffer.unsubscribe(sub);
    }

    private String toJson(HeimdallEvent event) {
        try { return mapper.writeValueAsString(event); }
        catch (Exception e) { return "{}"; }
    }
}
```

---

### Шаг 1.6 — Добавить сервис в docker-compose.yml

Добавить в `docker-compose.yml` после секции verdandi:
```yaml
  # ── HEIMDALL (admin observability backend) ────────────────────────────────
  heimdall-backend:
    build:
      context: .                                        # монорепо root
      dockerfile: services/heimdall-backend/Dockerfile
    ports:
      - "19093:9093"                                    # +10000 offset как у других
    environment:
      QUARKUS_HTTP_PORT: "9093"
    healthcheck:
      test: ["CMD-SHELL", "exec 3<>/dev/tcp/127.0.0.1/9093 && echo -e 'GET /q/health HTTP/1.1\\r\\nHost: 127.0.0.1\\r\\nConnection: close\\r\\n\\r\\n' >&3 && cat <&3 | grep -q UP"]
      interval: 10s
      timeout: 5s
      retries: 10
      start_period: 30s
```

---

### Шаг 1.7 — Дополнения в Makefile

```makefile
.PHONY: demo-start demo-reset demo-snapshot

demo-start: ## Поднять полный demo-стек через Docker
	docker compose up -d
	@sleep 5
	@curl -sf http://localhost:19093/q/health | grep -q UP \
	    && echo "✓ HEIMDALL ready" || echo "✗ HEIMDALL not yet ready"
	@echo "✓ Demo stack started"

demo-reset: ## Сбросить demo-состояние (clear ring buffer + cancel sessions)
	@curl -sf -X POST http://localhost:13000/heimdall/control/reset \
	     -H "Cookie: sid=$$ADMIN_SID" | cat
	@echo "✓ Demo state reset"

demo-snapshot: ## Сохранить snapshot текущего состояния в FRIGG
	@curl -sf -X POST "http://localhost:19093/control/snapshot?name=baseline" | cat
```

Обновить существующие цели:
```makefile
build: ## Build all projects
	./gradlew :libraries:hound:build :services:shuttle:build :services:heimdall-backend:build
	cd bff/chur && npm ci && npm run build
	cd frontends/verdandi && npm ci && npm run build

test: ## Run all tests
	./gradlew :libraries:hound:test :services:shuttle:test :services:heimdall-backend:test
	cd bff/chur && npm test
	cd frontends/verdandi && npm test
```

---

### Шаг 1.8 — Unit тесты

`services/heimdall-backend/src/test/java/studio/seer/heimdall/RingBufferTest.java`

```java
package studio.seer.heimdall;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import studio.seer.shared.*;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class RingBufferTest {

    @Inject RingBuffer ringBuffer;

    private HeimdallEvent event(String type) {
        return new HeimdallEvent(System.currentTimeMillis(), "test", type,
            EventLevel.INFO, null, null, null, 0, Map.of());
    }

    @Test void subscriberReceivesEvents() {
        AtomicInteger count = new AtomicInteger();
        ringBuffer.subscribe(e -> count.incrementAndGet());
        ringBuffer.push(event(EventType.ATOM_EXTRACTED.name()));
        assertEquals(1, count.get());
    }

    @Test void unsubscribeStopsDelivery() {
        AtomicInteger count = new AtomicInteger();
        var sub = (java.util.function.Consumer<HeimdallEvent>) e -> count.incrementAndGet();
        ringBuffer.subscribe(sub);
        ringBuffer.push(event(EventType.ATOM_EXTRACTED.name()));
        ringBuffer.unsubscribe(sub);
        ringBuffer.push(event(EventType.ATOM_EXTRACTED.name()));
        assertEquals(1, count.get());
    }

    @Test void sinceReturnsEventsAfterTimestamp() {
        long before = System.currentTimeMillis() - 1;
        ringBuffer.push(event(EventType.FILE_PARSING_STARTED.name()));
        assertFalse(ringBuffer.since(before).isEmpty());
    }
}
```

---

### Definition of Done — Sprint 1

- [ ] `./gradlew :services:heimdall-backend:quarkusDev` стартует на :9093, `/q/health` → UP
- [ ] `POST localhost:9093/events` принимает событие, возвращает 202
- [ ] `POST localhost:9093/events/batch` принимает массив
- [ ] `wscat -c ws://localhost:9093/ws/events` подключается, получает live-события
- [ ] При подключении клиент получает последние 200 событий (cold-start replay)
- [ ] `./gradlew :services:heimdall-backend:test` — все тесты зелёные
- [ ] `docker compose up -d heimdall-backend` поднимается, healthcheck на :19093 → UP
- [ ] `HeimdallEvent` schema зафиксирована в `shared/dali-models/`, не меняется

---

## Sprint 2 — Метрики + Фильтрация + Контроль

### Шаг 2.1 — MetricsCollector + MetricsSnapshot

Новый файл: `services/heimdall-backend/src/main/java/studio/seer/heimdall/metrics/MetricsSnapshot.java`
```java
package studio.seer.heimdall.metrics;
public record MetricsSnapshot(
    double atomsExtracted, double filesParsed, double toolCallsTotal,
    long   activeWorkers,  long queueDepth,    double resolutionRate
) {}
```

Новый файл: `services/heimdall-backend/src/main/java/studio/seer/heimdall/metrics/MetricsCollector.java`
По паттерну из HEIMDALL_SPRINT_PLAN.md §3 (H2.1). Inject `MeterRegistry` в конструктор, switch по `EventType.valueOf(event.eventType())`.

Новый файл: `services/heimdall-backend/src/main/java/studio/seer/heimdall/resource/MetricsResource.java`
```java
@Path("/metrics")
public class MetricsResource {
    @Inject MetricsCollector collector;
    @GET @Path("/snapshot") @Produces(MediaType.APPLICATION_JSON)
    public MetricsSnapshot snapshot() { return collector.snapshot(); }
}
```

Обновить `EventResource.java`: добавить inject MetricsCollector, вызывать `collector.record(enriched)` после push в ring buffer.

---

### Шаг 2.2 — EventFilter + Фильтрованные WebSocket-подписки

Новый файл: `services/heimdall-backend/src/main/java/studio/seer/heimdall/ws/EventFilter.java`
```java
public record EventFilter(String sessionId, String component, EventLevel minLevel, String eventType) {
    public static EventFilter parse(String raw) { /* разбор "key:value" пар */ }
    public boolean matches(HeimdallEvent e) { /* проверка каждого ненулевого поля */ }
    public static EventFilter empty() { return new EventFilter(null, null, EventLevel.INFO, null); }
}
```

Обновить `EventStreamEndpoint.java`:
- Читать query-параметр `filter` через `connection.handshakeRequest().queryParam("filter")`
- Парсить `EventFilter.parse(raw)` в `@OnOpen`
- Применять фильтр при replay и в subscriber-лямбде

Форматы фильтра (из HEIMDALL_SPRINT_PLAN H2.2):
```
ws://localhost:9093/ws/events                        ← без фильтра = всё
ws://localhost:9093/ws/events?filter=session_id:abc123
ws://localhost:9093/ws/events?filter=component:mimir
ws://localhost:9093/ws/events?filter=level:ERROR
```

---

### Шаг 2.3 — ControlResource

Новый файл: `services/heimdall-backend/src/main/java/studio/seer/heimdall/resource/ControlResource.java`

```java
@Path("/control")
public class ControlResource {
    @Inject RingBuffer ringBuffer;
    @Inject SnapshotManager snapshots;

    @POST @Path("/reset")
    public Uni<Response> reset() {
        // TODO: daliClient.cancelAllSessions() — ЗАГЛУШКА до появления Dali
        ringBuffer.clear();
        return Uni.createFrom().item(Response.ok().build());
    }

    @POST @Path("/snapshot")
    public Uni<Response> saveSnapshot(@QueryParam("name") String name) {
        return snapshots.save(name, ringBuffer.snapshot())
            .map(id -> Response.ok(Map.of("snapshotId", id)).build());
    }

    @POST @Path("/cancel/{sessionId}")
    public Response cancelSession(@PathParam("sessionId") String sessionId) {
        // ЗАГЛУШКА до появления Dali
        return Response.ok(Map.of("status", "stub", "sessionId", sessionId)).build();
    }

    @GET @Path("/snapshots")
    public Uni<List<SnapshotInfo>> listSnapshots() { return snapshots.list(); }
}
```

**Авторизация**: `quarkus-oidc` в HEIMDALL **не нужен**. Chur проверяет JWT и scope, затем проксирует. HEIMDALL читает `X-Seer-Role` из forwarded-заголовка (тот же паттерн, что в SHUTTLE через `SeerIdentity`).

---

### Шаг 2.4 — SnapshotManager (персистентность в FRIGG)

Новые файлы:
- `services/heimdall-backend/src/main/java/studio/seer/heimdall/snapshot/SnapshotInfo.java`
- `services/heimdall-backend/src/main/java/studio/seer/heimdall/snapshot/SnapshotManager.java`

Использовать тот же паттерн ArcadeDB HTTP-клиента что в SHUTTLE (`services/shuttle/src/main/java/studio/seer/lineage/client/ArcadeDbClient.java`). Добавить `quarkus-rest-client-jackson` в `build.gradle`.

Добавить в `application.properties`:
```properties
# FRIGG = отдельный ArcadeDB на :2481 (НЕ HoundArcade :2480)
quarkus.rest-client.frigg.url=http://localhost:2481
frigg.db=heimdall
frigg.user=root
frigg.password=playwithdata
%prod.quarkus.rest-client.frigg.url=http://frigg:2481
```

---

### Шаг 2.5 — SHUTTLE HeimdallEmitter

Новые файлы в `services/shuttle/src/main/java/studio/seer/lineage/heimdall/`:
- `HeimdallClient.java` — `@RegisterRestClient(configKey = "heimdall-api")` REST-интерфейс
- `HeimdallEmitter.java` — fire-and-forget эмиттер, ошибки логируются WARN, не пробрасываются

**Временное решение**: SHUTTLE пока не зависит от `:shared:dali-models` через Gradle (это потребует Docker multi-module build для SHUTTLE — отдельный рефакторинг). Для Sprint 2: скопировать `HeimdallEvent.java`, `EventType.java`, `EventLevel.java` в `studio.seer.lineage.heimdall.model` внутри SHUTTLE.

Добавить в `services/shuttle/src/main/resources/application.properties`:
```properties
quarkus.rest-client.heimdall-api.url=http://localhost:9093
quarkus.rest-client.heimdall-api.connect-timeout=500
quarkus.rest-client.heimdall-api.read-timeout=1000
%prod.quarkus.rest-client.heimdall-api.url=http://heimdall-backend:9093
```

Inject `HeimdallEmitter` в `LineageResource.java` — emit `REQUEST_RECEIVED` / `REQUEST_COMPLETED`.

---

### Шаг 2.6 — Chur proxy routes для HEIMDALL (интеграционный шаг)

Новый файл: `bff/chur/src/routes/heimdall.ts`
По паттерну `bff/chur/src/routes/graphql.ts`.

Маршруты:
- `GET /heimdall/health` → `http://heimdall-backend:9093/q/health` (без аутентификации)
- `POST /heimdall/control/:action` → `http://heimdall-backend:9093/control/:action` (scope: `aida:admin:destructive`)
- `GET /heimdall/metrics/snapshot` → `http://heimdall-backend:9093/metrics/snapshot` (scope: `aida:admin`)
- WebSocket `/heimdall/ws/events` → `ws://heimdall-backend:9093/ws/events` (scope: `aida:admin`)

Зарегистрировать в `bff/chur/src/server.ts`.

---

### Definition of Done — Sprint 2

- [ ] `GET localhost:9093/metrics/snapshot` возвращает live-счётчики
- [ ] `wscat -c 'ws://localhost:9093/ws/events?filter=component:shuttle'` получает только SHUTTLE-события
- [ ] `POST localhost:9093/control/reset` — ring buffer очищается, DEMO_RESET-событие появляется в WebSocket
- [ ] `POST localhost:9093/control/snapshot?name=baseline` — возвращает snapshotId
- [ ] SHUTTLE эмитит события → появляются в HEIMDALL WebSocket в пределах 100 мс
- [ ] Упавший HEIMDALL не роняет SHUTTLE (fire-and-forget подтверждён)

---

## Порядок создания файлов (по зависимостям)

```
0.  git checkout -b feature/heimdall-sprint1
1.  shared/dali-models/{build.gradle, EventLevel.java, EventType.java, HeimdallEvent.java}
2.  settings.gradle (добавить оба новых include)
3.  docs/plans/HEIMDALL_IMPL_PLAN.md (скопировать этот план)
4.  services/heimdall-backend/{gradle.properties, build.gradle, docker-*.gradle, Dockerfile, .dockerignore, application.properties}
5.  services/heimdall-backend/src/.../RingBuffer.java
6.  services/heimdall-backend/src/.../EventResource.java
7.  services/heimdall-backend/src/.../EventStreamEndpoint.java
8.  services/heimdall-backend/src/test/.../RingBufferTest.java
9.  docker-compose.yml (добавить heimdall-backend)
10. Makefile (demo-* цели, обновить build/test)
─── Sprint 2 ──────────────────────────────────────────────────────────────────
11. MetricsSnapshot.java, MetricsCollector.java, MetricsResource.java
12. EventFilter.java, обновить EventStreamEndpoint.java
13. ControlResource.java (Dali-вызовы — заглушки)
14. SnapshotManager.java, SnapshotInfo.java
15. shuttle/heimdall/{HeimdallClient.java, HeimdallEmitter.java} + копии моделей
16. bff/chur/src/routes/heimdall.ts + регистрация в server.ts
```

---

## Открытые вопросы (нужно подтвердить до Sprint 2)

| # | Вопрос | Срок |
|---|---|---|
| **Q3** | HB1 Quarkus — принято в этом плане, формальное подтверждение | mid-May |
| **Q25** | HTTP POST (Variant A) — принято в этом плане, формальное подтверждение | mid-May |
| **Q7** | Формат REST API `POST /control/reset` → Dali endpoint (сейчас заглушка) | конец апреля |
| **Q13** | WebSocket протокол: этот план использует native `quarkus-websockets-next`. Если победит graphql-ws — меняется endpoint, ring buffer остаётся | mid-May |
| **C.5.2** | Keycloak scopes `aida:admin` / `aida:admin:destructive` нужны до тестирования ControlResource через Chur | до Sprint 1 |
| **SHUTTLE + dali-models** | Docker multi-module build для SHUTTLE — выделить в отдельный рефакторинг-спринт | после Sprint 2 |
| **HEIMDALL frontend** | Вне скоупа Sprint 1-2: порт 14000 dev / 24000 Docker, `frontends/heimdall-frontend/` | июнь |

---

*Документ версии 1.1. Создан на основе `docs/sprints/HEIMDALL_SPRINT_PLAN.md` v1.0 и `docs/architecture/MODULES_TECH_STACK.md` v2.3.*
