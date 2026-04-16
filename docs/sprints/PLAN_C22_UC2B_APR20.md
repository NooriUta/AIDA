# План: C.2.2 Mutations + Dali S2 UC2b + Cancel (end-to-end)

## Контекст

End-to-end поток UC2b: VERDANDI → SHUTTLE GraphQL mutation → DaliClient REST → Dali SessionService → JobRunr → ParseJob → YGG.

Два трека, которые реализуются вместе:
- **Track A (Dali)**: `cancelSession()` отсутствует в `SessionService`; нет cancel-эндпойнта в `SessionResource`; `ParseJob` и `ArcadeDbStorageProvider` уже полностью реализованы ✓
- **Track B (SHUTTLE)**: `startParseSession` и `cancelSession` — заглушки; `DaliClient` не существует; `askMimir`, `saveView`, `deleteView` мутации вообще отсутствуют

`resetDemoState` уже вызывает `HeimdallControlClient.reset()` и работает ✓.

---

## Track A — Dali Core

### 1. Создать `CancelResult` record

**Новый файл:** `services/dali/src/main/java/studio/seer/dali/service/CancelResult.java`

```java
package studio.seer.dali.service;
public record CancelResult(String status, String message) {}
```

### 2. Обновить `SessionService.java`

Файл: [`services/dali/src/main/java/studio/seer/dali/service/SessionService.java`](services/dali/src/main/java/studio/seer/dali/service/SessionService.java)

**Добавить поле** (после `sessions` map):
```java
private final ConcurrentMap<String, UUID> jobRunrIdMap = new ConcurrentHashMap<>();
```

**Обновить `enqueue()`** — захватить `JobId`, возвращаемый `jobScheduler.get().enqueue()`:
```java
// Заменить существующий вызов enqueue:
org.jobrunr.jobs.JobId jobRunrId =
    jobScheduler.get().<ParseJob>enqueue(j -> j.execute(sessionId, input));
jobRunrIdMap.put(sessionId, jobRunrId.asUUID());
```

**Добавить метод `cancelSession()`:**
```java
public CancelResult cancelSession(String sessionId) {
    Session session = sessions.get(sessionId);
    if (session == null)
        return new CancelResult("NOT_FOUND", "Session not found");
    if (session.status() == SessionStatus.COMPLETED
        || session.status() == SessionStatus.FAILED
        || session.status() == SessionStatus.CANCELLED)
        return new CancelResult("ALREADY_DONE",
            "Session already in terminal state: " + session.status());

    UUID jrId = jobRunrIdMap.remove(sessionId);
    if (jrId != null) {
        try { jobScheduler.get().delete(jrId); }
        catch (Exception e) {
            log.warn("[SessionService] JobRunr delete failed for {}: {}", sessionId, e.getMessage());
        }
    }

    sessions.computeIfPresent(sessionId, (k, s) -> new Session(
        s.id(), SessionStatus.CANCELLED, s.progress(), s.total(), s.batch(),
        s.clearBeforeWrite(), s.dialect(), s.source(), s.startedAt(), Instant.now(),
        s.atomCount(), s.vertexCount(), s.edgeCount(), s.droppedEdgeCount(),
        s.vertexStats(), s.resolutionRate(), s.durationMs(),
        s.warnings(), List.of("Cancelled by user"), s.fileResults(), false));

    Session cancelled = sessions.get(sessionId);
    if (cancelled != null) persist(cancelled);
    emitter.sessionCancelled(sessionId);
    return new CancelResult("CANCELLING", "Session marked CANCELLED; JobRunr deletion requested");
}
```

**Замечание:** `jobScheduler.get().delete(jrId)` — в JobRunr 7.3.0 метод `delete()` принимает `UUID`. Если компилятор требует `JobId`, заменить на `new org.jobrunr.jobs.JobId(jrId)`.

### 3. Обновить `HeimdallEmitter.java`

Файл: [`services/dali/src/main/java/studio/seer/dali/heimdall/HeimdallEmitter.java`](services/dali/src/main/java/studio/seer/dali/heimdall/HeimdallEmitter.java)

**Добавить метод** (после `sessionFailed()`):
```java
public void sessionCancelled(String sessionId) {
    warn(EventType.SESSION_FAILED, sessionId,
        Map.of("reason", "CANCELLED_BY_USER"));
}
```

### 4. Обновить `SessionResource.java`

Файл: [`services/dali/src/main/java/studio/seer/dali/rest/SessionResource.java`](services/dali/src/main/java/studio/seer/dali/rest/SessionResource.java)

**Добавить эндпойнт** (после `health()`):
```java
@POST
@Path("/{id}/cancel")
public Response cancel(@PathParam("id") String id) {
    CancelResult result = sessionService.cancelSession(id);
    return switch (result.status()) {
        case "NOT_FOUND"    -> Response.status(Response.Status.NOT_FOUND)
                                   .entity(result).build();
        case "ALREADY_DONE" -> Response.status(Response.Status.CONFLICT)
                                   .entity(result).build();
        default             -> Response.accepted(result).build();
    };
}
```

**Добавить import:** `import studio.seer.dali.service.CancelResult;`

### 5. Обновить `SessionResourceTest.java`

Файл: [`services/dali/src/test/java/studio/seer/dali/rest/SessionResourceTest.java`](services/dali/src/test/java/studio/seer/dali/rest/SessionResourceTest.java)

Добавить два теста:
```java
@Test
void cancel_existingQueuedSession_returns202() {
    String id = given().contentType(ContentType.JSON)
        .body("""{"dialect":"plsql","source":"%s","preview":true}""".formatted(tempSrc()))
        .when().post(SESSIONS_URL).then().statusCode(202).extract().path("id");

    given().when().post(SESSIONS_URL + "/" + id + "/cancel")
        .then().statusCode(202).body("status", equalTo("CANCELLING"));
}

@Test
void cancel_unknownSession_returns404() {
    given().when().post(SESSIONS_URL + "/00000000-0000-0000-0000-000000000000/cancel")
        .then().statusCode(404);
}
```

---

## Track B — SHUTTLE C.2.2 + C.2.4

### 6. Добавить зависимость Fault Tolerance

Файл: [`services/shuttle/build.gradle`](services/shuttle/build.gradle)

```gradle
// Fault tolerance — @Timeout, @Retry, @Fallback для DaliClient
implementation 'io.quarkus:quarkus-smallrye-fault-tolerance'
```

### 7. Создать пакет DaliClient

**Пакет:** `studio.seer.lineage.client.dali`  
**Путь:** `services/shuttle/src/main/java/studio/seer/lineage/client/dali/`

#### 7a. Модели (`model/`)

**`DaliParseSessionInput.java`** — маппинг из GraphQL input в HTTP тело запроса к Dali:
```java
public record DaliParseSessionInput(
    String dialect, String source, boolean preview,
    Boolean clearBeforeWrite, String filePattern, Integer maxFiles
) {}
```

**`SessionInfo.java`** — ответ Dali REST + GraphQL output type:
```java
public record SessionInfo(
    String id, String status, String dialect, String source,
    boolean preview, Long startedAt, Long updatedAt,
    Integer atomCount, Integer vertexCount, Integer edgeCount,
    Double resolutionRate, Long durationMs,
    Integer progress, Integer total, Boolean friggPersisted,
    List<String> errors
) {
    public static SessionInfo unavailable() {
        return new SessionInfo(null,"UNAVAILABLE",null,null,false,
            null,null,null,null,null,null,null,null,null,null,
            List.of("Dali service unavailable"));
    }
}
```

**`CancelResponse.java`:** `public record CancelResponse(String status, String message) {}`

**`DaliStats.java`:** `public record DaliStats(long daliTables, long daliColumns, long daliRoutines, long daliStatements, long daliAtoms, long lastUpdated) { public static DaliStats empty() { return new DaliStats(0,0,0,0,0,0); } }`

**`DaliHealth.java`:** `public record DaliHealth(String frigg, String ygg, int sessions, int workers, int queued) { public static DaliHealth degraded(String reason) { return new DaliHealth("error","unknown",0,0,0); } }`

#### 7b. `DaliClient.java`

```java
@RegisterRestClient(configKey = "dali-client")
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface DaliClient {

    @POST @Path("/sessions")
    @Timeout(value = 5, unit = ChronoUnit.SECONDS)
    @Retry(maxRetries = 2, delay = 500, delayUnit = ChronoUnit.MILLIS,
           retryOn = ProcessingException.class)
    @Fallback(DaliClientFallback.class)
    SessionInfo createSession(DaliParseSessionInput input);

    @GET @Path("/sessions/{id}")
    @Timeout(value = 3, unit = ChronoUnit.SECONDS)
    @Retry(maxRetries = 3, delay = 200, delayUnit = ChronoUnit.MILLIS,
           retryOn = ProcessingException.class)
    SessionInfo getSession(@PathParam("id") String id);

    @POST @Path("/sessions/{id}/cancel")
    @Timeout(value = 5, unit = ChronoUnit.SECONDS)
    @Retry(maxRetries = 1)
    @Fallback(DaliClientFallback.class)
    CancelResponse cancelSession(@PathParam("id") String id);

    @GET @Path("/sessions")
    List<SessionInfo> listSessions(@QueryParam("limit") @DefaultValue("50") int limit);

    @GET @Path("/stats") DaliStats getStats();
    @GET @Path("/sessions/health") DaliHealth getHealth();
}
```

#### 7c. `DaliClientFallback.java`

```java
public class DaliClientFallback implements FallbackHandler<Object> {
    @Override
    public Object handle(ExecutionContext ctx) {
        Log.warnf("[DaliClient] Fallback for %s: %s",
            ctx.getMethod().getName(), ctx.getFailure().getMessage());
        return switch (ctx.getMethod().getName()) {
            case "createSession" -> SessionInfo.unavailable();
            case "getSession"    -> SessionInfo.unavailable();
            case "cancelSession" -> new CancelResponse("UNAVAILABLE", "Dali недоступен");
            case "listSessions"  -> List.of();
            case "getStats"      -> DaliStats.empty();
            case "getHealth"     -> DaliHealth.degraded("Dali недоступен");
            default              -> null;
        };
    }
}
```

### 8. Обновить `ParseSessionInput.java` в SHUTTLE

Файл: [`services/shuttle/src/main/java/studio/seer/lineage/heimdall/model/ParseSessionInput.java`](services/shuttle/src/main/java/studio/seer/lineage/heimdall/model/ParseSessionInput.java)

Заменить существующий record (сейчас только `path`, `dbType`) полной моделью:
```java
@Input("ParseSessionInput")
public record ParseSessionInput(
    String dialect,           // "PLSQL" | "GENERIC_SQL" | "POSTGRESQL"
    String source,            // путь к файлу или директории
    Boolean preview,          // null → Dali использует false
    Boolean clearBeforeWrite, // null → Dali использует true
    String filePattern,       // null → "*.sql"
    Integer maxFiles          // null → без лимита
) {}
```

### 9. Обновить `MutationResource.java`

Файл: [`services/shuttle/src/main/java/studio/seer/lineage/resource/MutationResource.java`](services/shuttle/src/main/java/studio/seer/lineage/resource/MutationResource.java)

**Добавить инжекцию:**
```java
@Inject @RestClient DaliClient daliClient;
```

**Заменить заглушку `startParseSession` (возвращаемый тип String → SessionInfo):**
```java
@Mutation("startParseSession")
@Description("Запустить сессию парсинга через Dali. Роль: admin")
public Uni<SessionInfo> startParseSession(
        @Name("input") @DefaultValue("{}") ParseSessionInput input) {
    heimdall.emit(EventType.SESSION_STARTED, EventLevel.INFO, null, null, 0,
        Map.of("source",  input.source()  != null ? input.source()  : "",
               "dialect", input.dialect() != null ? input.dialect() : "unknown"));
    return Uni.createFrom().item(() -> {
        DaliParseSessionInput daliInput = new DaliParseSessionInput(
            input.dialect(), input.source(),
            input.preview() != null ? input.preview() : false,
            input.clearBeforeWrite(), input.filePattern(), input.maxFiles());
        return daliClient.createSession(daliInput);
    }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
}
```

**Заменить заглушку `cancelSession`:**
```java
@Mutation("cancelSession")
@Description("Отменить сессию. Роль: admin")
public Uni<Boolean> cancelSession(@Name("sessionId") String sessionId) {
    return Uni.createFrom().item(() -> {
        CancelResponse resp = daliClient.cancelSession(sessionId);
        return !"UNAVAILABLE".equals(resp.status());
    }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
}
```

**Добавить заглушки `askMimir`, `saveView`, `deleteView`:**
```java
@Mutation("askMimir")
@Description("Запрос к Mimir AI (stub — MimirClient не реализован). Роль: editor+")
public Uni<String> askMimir(@Name("question") String question) {
    return Uni.createFrom().failure(
        new GraphQLException("askMimir: MimirClient не реализован (C.2.3 pending)"));
}

@Mutation("saveView")
@Description("Сохранить конфигурацию LOOM view (stub — FRIGG pending). Роль: editor+")
public Uni<Boolean> saveView(@Name("viewId") String viewId, @Name("payload") String payload) {
    return Uni.createFrom().failure(
        new GraphQLException("saveView: FRIGG view storage не реализован"));
}

@Mutation("deleteView")
@Description("Удалить LOOM view (stub — FRIGG pending). Роль: editor+")
public Uni<Boolean> deleteView(@Name("viewId") String viewId) {
    return Uni.createFrom().failure(
        new GraphQLException("deleteView: FRIGG view storage не реализован"));
}
```

### 10. Обновить `application.properties` SHUTTLE

Файл: [`services/shuttle/src/main/resources/application.properties`](services/shuttle/src/main/resources/application.properties)

Добавить в конец:
```properties
# ── Dali REST client (C.2.4) ─────────────────────────────────────────────────
quarkus.rest-client.dali-client.url=${DALI_URL:http://localhost:9090}
quarkus.rest-client.dali-client.connect-timeout=3000
quarkus.rest-client.dali-client.read-timeout=10000

mp.fault-tolerance.enabled=true
```

### 11. Обновить `docker-compose.yml`

Файл: [`docker-compose.yml`](docker-compose.yml)

Добавить в блок `environment:` сервиса `shuttle`:
```yaml
DALI_URL: http://dali:9090
```

---

## Таблица файлов

| Файл | Действие |
|------|----------|
| `services/dali/.../service/CancelResult.java` | Создать |
| `services/dali/.../service/SessionService.java` | Изменить: `jobRunrIdMap`, `enqueue()`, `cancelSession()` |
| `services/dali/.../heimdall/HeimdallEmitter.java` | Изменить: добавить `sessionCancelled()` |
| `services/dali/.../rest/SessionResource.java` | Изменить: добавить `POST /{id}/cancel` |
| `services/dali/.../rest/SessionResourceTest.java` | Изменить: 2 новых теста |
| `services/shuttle/.../client/dali/DaliClient.java` | Создать |
| `services/shuttle/.../client/dali/DaliClientFallback.java` | Создать |
| `services/shuttle/.../client/dali/model/*.java` | Создать 5 record-классов |
| `services/shuttle/.../heimdall/model/ParseSessionInput.java` | Изменить: расширить поля |
| `services/shuttle/.../resource/MutationResource.java` | Изменить: DaliClient + 3 стаба |
| `services/shuttle/.../resources/application.properties` | Изменить: dali-client config |
| `services/shuttle/build.gradle` | Изменить: fault-tolerance dep |
| `docker-compose.yml` | Изменить: DALI_URL в shuttle env |

---

## Ветка

Создать перед началом: `feature/c22-mutations-uc2b-apr20`  
Сохранить план: `docs/sprints/PLAN_C22_UC2B_APR20.md`

---

## Верификация

1. **Тесты Dali:**
   ```bash
   cd services/dali && ./gradlew test
   ```
   Ожидается: `SessionResourceTest` — 5 тестов GREEN (3 старых + 2 новых cancel).

2. **Компиляция SHUTTLE:**
   ```bash
   cd services/shuttle && ./gradlew compileJava
   ```

3. **E2E через docker compose:**
   ```bash
   docker compose up dali shuttle frigg ygg heimdall-backend -d
   
   # Создать сессию через Dali напрямую:
   curl -X POST http://localhost:19090/api/sessions \
     -H "Content-Type: application/json" \
     -d '{"dialect":"PLSQL","source":"/opt/sql","preview":false}'
   # → { "id": "...", "status": "QUEUED" }
   
   # Получить статус до завершения:
   curl http://localhost:19090/api/sessions/{id}
   # → status: COMPLETED, atomCount > 0
   
   # Тест cancel через SHUTTLE GraphQL:
   curl -X POST http://localhost:8080/graphql \
     -H "Content-Type: application/json" \
     -d '{"query":"mutation { startParseSession(input:{dialect:\"PLSQL\",source:\"/opt/sql\"}) { id status } }"}'
   ```

4. **QG-проверки после реализации:**
   - `QG-DALI-persistence`: `GET /api/sessions/archive` возвращает сессии → GREEN
   - `QG-DALI-ygg-write`: atomCount > 0 после E2E с `preview=false` → GREEN
   - `QG-HOUND-listener-chain`: `ParseJob` уже использует `CompositeListener` → GREEN ✓

---

## Актуализация тестов и документации

### Тесты

| Тест | Действие |
|------|----------|
| `SessionResourceTest.java` | Добавить `cancel_existingQueuedSession_returns202` и `cancel_unknownSession_returns404` |
| `SessionResourceTest.cancel_alreadyCompleted_returns409` | Добавить тест для terminal-состояния (опционально, если есть время) |
| SHUTTLE — `MutationResourceTest.java` | Если файл существует — добавить mock-тест `startParseSession` через `@InjectMock DaliClient`; проверить, что fallback возвращает `SessionInfo{status=UNAVAILABLE}` при недоступном Dali |

### Документация

| Документ | Действие |
|----------|----------|
| `docs/sprints/PLAN_C22_UC2B_APR20.md` | Создать: сохранить итоговый план в репо (этот файл) |
| `docs/architecture/Q7_HEIMDALL_DALI_API.md` | Дополнить: добавить раздел `POST /api/sessions/{id}/cancel` — запрос/ответ, статусы (`CANCELLING`, `NOT_FOUND`, `ALREADY_DONE`) |
| `UC2B_TRIGGER_SPEC.md` | Обновить статус: `❌ Не реализован` → `✅ Реализован` после прохождения QG |
| `DALI_CLIENT_C24_SPEC.md` | Обновить статус: `❌ Не реализован` → `✅ Реализован` |
| `.env.example` (dali, shuttle) | Добавить `DALI_URL=http://localhost:9090` в shuttle `.env.example` |

### Quality Gate — чек-лист

После реализации пройти вручную перед merge в master:

```
QG-DALI-persistence:
  □ POST /api/sessions → сессия появляется в /api/sessions/archive (FRIGG)
  □ Перезапустить dali → сессии восстанавливаются из FRIGG при старте
  □ QUEUED/RUNNING при рестарте сбрасываются в FAILED (логируется)

QG-DALI-ygg-write:
  □ preview=false → atomCount > 0 в ответе completed-сессии
  □ YGG: SELECT count(*) FROM DaliAtom → count > 0
  □ preview=true  → atomCount > 0, но YGG не изменился (count тот же)

QG-HOUND-listener-chain:
  □ ParseJob использует CompositeListener (DaliHoundListener + HoundHeimdallListener)
  □ HEIMDALL получает FILE_PARSING_STARTED и ATOM_EXTRACTED события

QG-CANCEL:
  □ cancelSession на QUEUED сессии → status=CANCELLING, сессия → CANCELLED в /api/sessions/{id}
  □ cancelSession на COMPLETED сессии → 409 ALREADY_DONE
  □ cancelSession на несуществующей сессии → 404 NOT_FOUND

QG-SHUTTLE-INTEGRATION:
  □ startParseSession через GraphQL → возвращает SessionInfo{status=QUEUED}
  □ cancelSession через GraphQL → возвращает true
  □ startParseSession при недоступном Dali → возвращает SessionInfo{status=UNAVAILABLE}
```

### Нагрузочные тесты (после стабилизации UC2b)

Цель: убедиться, что Dali выдерживает конкурентные сессии и YGG не деградирует при пакетной записи.

**Минимальный сценарий (ручной, без инструментов):**
```bash
# 1. Запустить 3 последовательных сессии с preview=false
for i in 1 2 3; do
  curl -X POST http://localhost:19090/api/sessions \
    -H "Content-Type: application/json" \
    -d "{\"dialect\":\"PLSQL\",\"source\":\"/opt/sql/corpus\",\"preview\":false,\"clearBeforeWrite\":false}"
  sleep 2
done

# 2. Наблюдать за прогрессом
watch -n 2 'curl -s http://localhost:19090/api/sessions | jq "[.[] | {id:.id,status:.status,progress:.progress,total:.total}]"'

# 3. Проверить итоговый atomCount и resolutionRate
curl -s http://localhost:19090/api/sessions | jq '[.[] | {status:.status,atoms:.atomCount,rate:.resolutionRate}]'
```

**Тест конкурентности (2 параллельных сессии с preview=true):**
```bash
# clearBeforeWrite=false нужен чтобы не конфликтовали
SESSION1=$(curl -s -X POST http://localhost:19090/api/sessions \
  -H "Content-Type: application/json" \
  -d '{"dialect":"PLSQL","source":"/opt/sql","preview":true}' | jq -r .id)

SESSION2=$(curl -s -X POST http://localhost:19090/api/sessions \
  -H "Content-Type: application/json" \
  -d '{"dialect":"PLSQL","source":"/opt/sql","preview":true}' | jq -r .id)

# Обе должны завершиться COMPLETED (не конкурируют за YGG при preview=true)
sleep 30
curl -s http://localhost:19090/api/sessions/$SESSION1 | jq .status
curl -s http://localhost:19090/api/sessions/$SESSION2 | jq .status
```

**Критерии приёмки нагрузочного теста:**
- 3 последовательных сессии на 100+ SQL-файлах → все COMPLETED, нет FAILED
- Время обработки одного файла < 2 сек (среднее)
- JobRunr queue не накапливает застрявшие jobs: `GET /api/sessions/health` → `{"frigg":"ok"}`
- YGG отвечает на SELECT-запросы без деградации после трёх пакетных write-сессий

### Анализ оптимальности запросов YGG со стороны VERDANDI

Цель: убедиться, что GraphQL-запросы к lineage-данным, которые VERDANDI LOOM отправляет через SHUTTLE, не вызывают N+1 проблем и не перегружают YGG.

**Что проверить:**

1. **ArcadeDB query plans** (включить query profiling):
   ```bash
   # Включить профилирование в YGG (ArcadeDB)
   curl -X POST "http://localhost:2480/api/v1/command/hound" \
     -u root:playwithdata \
     -H "Content-Type: application/json" \
     -d '{"command":"ALTER DATABASE `QUERY_EXECUTION_PROFILE` = TRUE"}'
   
   # После нескольких GraphQL запросов от VERDANDI — посмотреть профили
   curl -X POST "http://localhost:2480/api/v1/command/hound" \
     -u root:playwithdata \
     -H "Content-Type: application/json" \
     -d '{"command":"SELECT @execution_plan FROM (TRAVERSE * FROM (SELECT FROM DaliAtom LIMIT 5)) LIMIT 1"}'
   ```

2. **Запросы с высоким fan-out** — проверить, что lineage-запросы LOOM используют LIMIT и не делают full-scan:
   ```bash
   # Смоделировать запрос, который делает VERDANDI LOOM (через SHUTTLE GraphQL)
   curl -X POST http://localhost:8080/graphql \
     -H "Content-Type: application/json" \
     -d '{
       "query": "{ lineage(schema: \"PUBLIC\") { nodes { id label type } edges { from to } } }"
     }'
   
   # Ожидается: ответ < 500ms для schema с < 1000 объектов
   # Если медленнее — проверить наличие индексов на DaliAtom.schema и DaliTable.schema
   ```

3. **Проверить индексы в YGG** (должны быть созданы `FriggSchemaInitializer` Hound):
   ```bash
   curl -X POST "http://localhost:2480/api/v1/command/hound" \
     -u root:playwithdata \
     -H "Content-Type: application/json" \
     -d '{"command":"SELECT name, type FROM schema:indexes"}'
   # Ожидаемые индексы: DaliAtom.qualifiedName, DaliTable.schema, DaliRoutine.schema
   ```

4. **Критерии оптимальности:**
   - Линейный запрос `lineage(schema)` при 10k атомов → < 1 сек
   - Нет full-scan без WHERE (проверить через execution_plan: `fullScan: false`)
   - Количество round-trips к YGG на один GraphQL запрос ≤ 3 (нет N+1)
   - HEIMDALL не получает потока ошибок YGG_TIMEOUT при нагрузке

5. **Если найдены проблемы — завести инвентарный тикет** (формат INV-YGG-NN) с описанием запроса и планом.

---

---

## Актуализация docs/ в конце спринта

Выполнить **после** прохождения всех QG и перед merge PR в master.

### Архитектурные документы

| Документ | Что обновить |
|----------|-------------|
| `docs/architecture/Q7_HEIMDALL_DALI_API.md` | Добавить эндпойнт `POST /api/sessions/{id}/cancel`: запрос (пустое тело), ответ `{"status":"CANCELLING"\|"NOT_FOUND"\|"ALREADY_DONE","message":"..."}`, HTTP-коды 202/404/409 |
| `docs/architecture/REFACTORING_PLAN.md` | Отметить C.2.2 и C.2.4 как ✅ реализованы; обновить статус UC2b: `design ready` → `implemented` |
| `docs/architecture/RBAC_MULTITENANT.md` | Добавить строку в таблицу мутаций: `startParseSession`, `cancelSession` → роль `admin`; `askMimir`, `saveView`, `deleteView` → роль `editor+` (stub) |

### Спринтовые документы

| Документ | Что сделать |
|----------|-------------|
| `docs/sprints/UC2B_TRIGGER_SPEC.md` | Изменить статус: `❌ Не реализован` → `✅ Реализован (Track A + B завершены, QG GREEN)`; добавить дату реализации |
| `docs/sprints/DALI_CLIENT_C24_SPEC.md` | Изменить статус: `❌ Не реализован` → `✅ Реализован`; добавить ссылку на PR |
| `docs/sprints/PLAN_C22_UC2B_APR20.md` | Создать из этого плана — зафиксировать факт в репо |

### Инструкции (новые файлы)

| Файл | Содержание |
|------|------------|
| `docs/internal/HOW_TO_RUN_UC2B.md` | Инструкция по запуску UC2b локально (секция ниже) |
| `docs/quality-gates/QG_DALI_CHECKLIST.md` | Чек-лист QG-DALI-persistence, QG-DALI-ygg-write, QG-HOUND-listener-chain, QG-CANCEL — с командами проверки |

---

### Инструкция по запуску UC2b локально

После реализации добавить в `docs/` (или в комментарий PR):

```markdown
## Запуск UC2b локально

### Предварительные условия
- Docker Desktop запущен
- `docker compose up frigg ygg heimdall-backend dali shuttle -d`

### Шаг 1: Проверить готовность Dali
curl http://localhost:19090/api/sessions/health
# Ожидается: {"frigg":"ok","sessions":0}

### Шаг 2: Запустить сессию парсинга (через Dali напрямую)
curl -X POST http://localhost:19090/api/sessions \
  -H "Content-Type: application/json" \
  -d '{"dialect":"PLSQL","source":"/opt/sql/corpus","preview":false}'
# Ответ: {"id":"<uuid>","status":"QUEUED",...}

### Шаг 3: Запустить сессию через SHUTTLE GraphQL
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation StartSession($input: ParseSessionInput!) { startParseSession(input: $input) { id status dialect source } }",
    "variables": { "input": { "dialect": "PLSQL", "source": "/opt/sql/corpus", "preview": false } }
  }'

### Шаг 4: Отменить сессию
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"mutation { cancelSession(sessionId: \"<uuid>\") }"}'

### QG-проверка: atomCount > 0
curl http://localhost:19090/api/sessions/<uuid>
# Ожидается: "status":"COMPLETED", "atomCount":>0
```
