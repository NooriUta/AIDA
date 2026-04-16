# UC2b — Event-Driven Parse Trigger Spec

**Документ:** `UC2B_TRIGGER_SPEC`
**Версия:** 1.1
**Дата:** 16.04.2026
**Статус:** ✅ Реализован — Track A + Track B завершены (sprint Apr 16, 2026, PR #14)
**Трек:** Track A (Dali) + Track B (SHUTTLE, интеграция)
**Реализовано за:** 1 день (параллельные треки)
**PR:** https://github.com/NooriUta/AIDA/pull/14
**Зависит от:** DaliClient C.2.4 ✅ реализован

---

## Что такое UC2b

UC2b — event-driven запуск парсинга: SHUTTLE получает GraphQL мутацию от UI или внешнего триггера и вызывает Dali через HTTP REST. В отличие от UC1 (scheduled / cron), UC2b запускается по требованию.

UC2a (batch с preview, embedded ArcadeDB) — pending Q31, не в этом документе.

---

## Поток UC2b end-to-end

```
VERDANDI (UI)
  │  GraphQL mutation startParseSession(input)
  ▼
SHUTTLE MutationResource
  │  DaliClient.createSession(input)          ← C.2.4, HTTP POST /api/sessions
  ▼
Dali SessionService.createSession()
  │  JobRunr.enqueue(() -> parseJob.run(sessionId))
  │  emits JOB_ENQUEUED → HEIMDALL
  ▼
JobRunr Worker (4 потока)
  │  ParseJob.execute(sessionId)
  │  ├─ HoundConfig.defaultRemoteBatch(yggUrl, db, user, pwd)
  │  ├─ HoundParser.parse(config, listener)
  │  │    └─ HoundHeimdallListener → HEIMDALL (FILE_PARSING_*, ATOM_EXTRACTED)
  │  └─ SessionService.complete(sessionId, result)
  │       ├─ FRIGG.persist(session)
  │       └─ emits SESSION_COMPLETED → HEIMDALL
  ▼
YGG (ArcadeDB :2480, база hound)
  │  DaliTable + DaliColumn + DaliRoutine + DaliAtom vertices
  ▼
VERDANDI LOOM
  │  GraphQL query lineage(schema) → SHUTTLE → YGG
  └─ renders lineage graph
```

---

## Dali: что нужно реализовать

### SessionService.createSession()

```java
// services/dali/src/main/java/**/SessionService.java

@ApplicationScoped
public class SessionService {

    @Inject Instance<JobScheduler> jobScheduler;   // lazy, решение #24
    @Inject FriggGateway frigg;
    @Inject HeimdallEmitter heimdall;

    public SessionInfo createSession(ParseSessionInput input) {
        Session session = Session.builder()
            .id(UUID.randomUUID())
            .status(SessionStatus.QUEUED)
            .dialect(input.dialect())
            .source(input.source())
            .preview(input.preview())
            .clearBeforeWrite(input.clearBeforeWrite() != null
                ? input.clearBeforeWrite() : Boolean.TRUE)  // дефолт true, решение #INV-DALI-03
            .filePattern(input.filePattern() != null ? input.filePattern() : "*.sql")
            .tags(input.tags())
            .createdAt(System.currentTimeMillis())
            .build();

        // persist to FRIGG (fire-and-forget, не блокирует)
        persist(session);

        // enqueue in JobRunr
        UUID jobId = jobScheduler.get().enqueue(
            "parse-" + session.getId(),
            () -> parseJobBean.run(session.getId())
        );
        session.setJobRunrId(jobId);
        persist(session);

        // emit to HEIMDALL
        heimdall.emit(HeimdallEvent.builder()
            .eventType("JOB_ENQUEUED")
            .sourceComponent("dali")
            .sessionId(session.getId().toString())
            .level("INFO")
            .payload(Map.of(
                "dialect", session.getDialect(),
                "source", session.getSource(),
                "preview", session.isPreview()
            ))
            .build());

        return SessionInfo.from(session);
    }

    // persist() — graceful при FRIGG down (решение #INV-DALI-04)
    void persist(Session session) {
        try {
            frigg.save(session);
        } catch (Exception e) {
            Log.warnf("[SessionService] FRIGG persist failed (non-fatal): %s", e.getMessage());
        }
    }
}
```

### ParseJob.execute() — что должен делать

```java
// services/dali/src/main/java/**/ParseJob.java

@ApplicationScoped
@Unremovable  // решение #24
public class ParseJob implements Job {

    @Override
    public void run(JobContext context) {
        UUID sessionId = UUID.fromString(context.getJobParameter("sessionId", String.class));
        Session session = sessionService.getOrThrow(sessionId);

        // Обновить статус
        session.setStatus(SessionStatus.PROCESSING);
        session.setStartedAt(System.currentTimeMillis());
        sessionService.persist(session);
        heimdall.emit(sessionStarted(session));

        try {
            HoundConfig config = buildConfig(session);   // INV-DALI-02

            // Listener цепочка: DaliHoundListener + HoundHeimdallListener
            HoundEventListener listener = buildListener(session);

            ParseResult result = HoundParser.parse(config, listener);

            session.setStatus(SessionStatus.COMPLETED);
            session.setAtomCount(result.atomCount());
            session.setAtomsResolved(result.atomsResolved());
            session.setAtomsUnresolved(result.atomsUnresolved());
            session.setResolutionRate(result.resolutionRate());
            session.setFileCount(result.fileCount());
            session.setFilesProcessed(result.filesProcessed());
            session.setFilesFailed(result.filesFailed());
            session.setCompletedAt(System.currentTimeMillis());
            session.setDurationMs(session.getCompletedAt() - session.getStartedAt());
            sessionService.persist(session);

            heimdall.emit(sessionCompleted(session, result));

        } catch (Exception e) {
            session.setStatus(SessionStatus.FAILED);
            session.setErrorMessage(e.getMessage());
            session.setCompletedAt(System.currentTimeMillis());
            sessionService.persist(session);
            heimdall.emit(sessionFailed(session, e));
            throw new RuntimeException("Parse failed", e);  // JobRunr retry
        }
    }

    private HoundConfig buildConfig(Session session) {
        // INV-DALI-02: preview=false → REMOTE_BATCH, preview=true → DISABLED
        ArcadeWriteMode mode = session.isPreview()
            ? ArcadeWriteMode.DISABLED
            : ArcadeWriteMode.REMOTE_BATCH;

        return HoundConfig.builder()
            .dialect(session.getDialect())
            .source(session.getSource())
            .filePattern(session.getFilePattern())
            .maxFiles(session.getMaxFiles())
            .writeMode(mode)
            .clearBeforeWrite(!session.isPreview() && session.isClearBeforeWrite())
            .arcadeUrl(yggUrl)
            .arcadeDb(yggDb)
            .arcadeUser(yggUser)
            .arcadePassword(yggPassword)
            .build();
    }

    private HoundEventListener buildListener(Session session) {
        // CompositeListener изолирует сбои: исключение в одном не мешает другому
        return new CompositeListener(
            new DaliHoundListener(session, sessionService, heimdall),
            new HoundHeimdallListener(heimdallUrl, session.getId().toString())
        );
    }
}
```

### cancelSession() — реализация cancel stub

```java
// SessionService.java — добавить метод

public CancelResult cancelSession(UUID sessionId) {
    Session session = sessionService.get(sessionId);
    if (session == null) {
        return new CancelResult("NOT_FOUND", "Session not found");
    }

    if (session.getStatus() == SessionStatus.COMPLETED
        || session.getStatus() == SessionStatus.FAILED) {
        return new CancelResult("ALREADY_DONE", "Session already completed");
    }

    // JobRunr delete
    try {
        jobScheduler.get().delete(session.getJobRunrId());
    } catch (Exception e) {
        Log.warnf("[SessionService] JobRunr delete failed: %s", e.getMessage());
    }

    session.setStatus(SessionStatus.CANCELLED);
    session.setErrorMessage("Cancelled by user");
    session.setCompletedAt(System.currentTimeMillis());
    persist(session);

    heimdall.emit(HeimdallEvent.builder()
        .eventType("SESSION_FAILED")
        .sessionId(sessionId.toString())
        .level("WARN")
        .payload(Map.of("reason", "CANCELLED_BY_USER"))
        .build());

    return new CancelResult("CANCELLING", "JobRunr job deletion requested");
}
```

---

## SHUTTLE: интеграция после DaliClient C.2.4

```java
// MutationResource.java — замена заглушек

@Mutation
public SessionInfo startParseSession(ParseSessionInput input) {
    heimdall.emit(requestReceived("startParseSession"));
    long t = System.currentTimeMillis();
    try {
        SessionInfo result = daliClient.createSession(input);
        heimdall.emit(requestCompleted("startParseSession", System.currentTimeMillis() - t));
        return result;
    } catch (Exception e) {
        heimdall.emit(requestFailed("startParseSession", e));
        throw e;
    }
}

@Mutation
public Boolean cancelSession(String sessionId) {
    CancelResponse resp = daliClient.cancelSession(UUID.fromString(sessionId));
    return !"UNAVAILABLE".equals(resp.status());
}
```

---

## Минимальный ParseSessionInput для UC2b

```json
{
  "dialect": "PLSQL",
  "source":  "/opt/sql/corp-batch",
  "preview": false
}
```

Dali использует дефолты: `clearBeforeWrite=true`, `filePattern="*.sql"`, `maxFiles=null`.

---

## Конфигурация Dali

```properties
# application.properties (services/dali/)

# YGG (HoundArcade)
dali.ygg.url=${YGG_URL:http://localhost:2480}
dali.ygg.db=hound
dali.ygg.user=root
dali.ygg.password=${YGG_PASSWORD:playwithdata}

# HEIMDALL
dali.heimdall.url=${HEIMDALL_URL:http://localhost:9093}

# JobRunr
quarkus.jobrunr.background-job-server.worker-count=4
quarkus.jobrunr.background-job-server.poll-interval=PT5S
quarkus.jobrunr.jobs.default-number-of-retries=3
quarkus.jobrunr.jobs.retry-back-off-time-seed=PT30S
```

---

## Порядок реализации

```
Track A (Dali):
  1. SessionService.createSession() — persist + enqueue (~2 ч)
  2. ParseJob.execute() полная реализация (~3 ч)
     - buildConfig() (INV-DALI-02 гарантия)
     - buildListener() с CompositeListener
     - error handling + FRIGG persist
  3. cancelSession() — реализация stub (~1 ч)
  4. ControlResource.cancel endpoint (~30 мин)
  5. QuarkusTest для SessionService + ParseJob (~2 ч)

Track B (SHUTTLE, после DaliClient C.2.4):
  6. MutationResource — интеграция daliClient.createSession/cancel (~30 мин)
  7. E2E тест: startParseSession → polling → SessionInfo.isDone() (~1 ч)

QA:
  8. Прогон 300 PL/SQL файлов → DaliAtom count > 0 → QG-DALI-ygg-write GREEN
```

**Total estimate:** 9–10 ч, два дня параллельно при variable velocity.

---

## QG-зависимости

| QG | Что блокирует |
|---|---|
| QG-DALI-persistence | ArcadeDbStorageProvider активен (INV-DALI-01) — до UC2b |
| QG-DALI-ygg-write | Верифицируется E2E прогоном UC2b с preview=false |
| QG-HOUND-listener-chain | CompositeListener + HoundHeimdallListener в buildListener() |

После реализации UC2b все три QG должны стать GREEN.

---

## История изменений

| Дата | Версия | Что |
|---|---|---|
| 16.04.2026 | 1.1 | **✅ IMPLEMENTED.** Track A: `cancelSession()` + `jobRunrIdMap` + `POST /api/sessions/{id}/cancel` (202/404/409) + 3 новых теста (7/7 GREEN). Track B: DaliClient C.2.4 — полный пакет `studio.seer.lineage.client.dali` с Fault Tolerance. `startParseSession` и `cancelSession` mutations wire-up завершён. PR #14 смержен в master. |
| 14.04.2026 | 1.0 | Initial spec из Q7 contract + C.2.4 DaliClient. |
