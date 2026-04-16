# Plan: C.1 Hound → java-library + C.2 Dali Core skeleton

## Context
Рефакторинг Hound из standalone CLI-приложения в публичную java-library с чистым API пакетом (`hound.api`), чтобы Dali мог импортировать Hound как `implementation project(':libraries:hound')` без subprocess/HTTP. Параллельно — создание Dali Core skeleton (Quarkus + JobRunr + ArcadeDB StorageProvider).

**Текущее состояние (найдено при аудите):**
- `java-library` плагин уже есть в `libraries/hound/build.gradle` — частично выполнено
- `settings.gradle` уже включает `libraries:hound`, `shared:dali-models`, `services:shuttle`, `services:heimdall-backend`
- `shared/dali-models` существует, содержит `EventLevel`, `EventType`, `HeimdallEvent` — нужно добавить `ParseSessionInput`, `Session`, `SessionStatus`
- Нет `hound.api` пакета — все классы в `com.hound.*` функциональных пакетах
- Нет `HoundConfig`, `HoundParser`, `HoundEventListener`, `ParseResult` — только `SemanticResult`
- Конфиг сейчас: только CLI args через Apache Commons CLI (`RunConfig` inner class в `HoundApplication`)
- Тестов: 25 файлов (не 24)
- Сервиса `services:dali` ещё нет

---

## Перед началом (обязательно)

### 0.0 Пересобрать все Docker-контейнеры
```bash
cd C:/AIDA/aida-root
docker-compose down --rmi local && docker-compose build --no-cache && docker-compose up -d
```
Дождаться `docker-compose ps` — все сервисы `Up` / `healthy` — прежде чем начинать кодовые изменения.

### 0.1 Создать ветку
```bash
git checkout -b feature/c1-hound-java-library-dali-core
```

### 0.2 Сохранить план в docs/
```bash
cp <plan-file> docs/sprints/sprint-c1-c2-hound-dali-plan.md
git add docs/sprints/sprint-c1-c2-hound-dali-plan.md && git commit -m "docs(sprint): add C.1 Hound→java-library + C.2 Dali core plan"
```

---

## Фаза 1 · C.1 Hound → java-library (~4 дня, блокирует Dali)

### День 1 — Audit + HoundConfig

**Д1 · AM — Audit точки входа (~2 ч)**

Файл: `libraries/hound/src/main/java/com/hound/HoundApplication.java` (line ~58 — `main()`)

- Точка входа: `HoundApplication.main()` → `RunConfig` (inner static class) ← только CLI args
- Конфиг принимает: только CLI args (Commons CLI), без env vars, без properties файла
- Закрыть Q27: зафиксировать полный список полей `HoundConfig`

**Д1 · PM — C.1.2 HoundConfig record (~2–3 ч)**

Новый файл: `libraries/hound/src/main/java/com/hound/api/HoundConfig.java`

```java
public record HoundConfig(
    String dialect,            // "plsql" | "postgresql" | "clickhouse" | ...
    String targetSchema,       // namespace isolation в YGG
    ArcadeWriteMode writeMode, // EMBEDDED | REMOTE | REMOTE_BATCH
    String arcadeUrl,          // null для EMBEDDED
    int workerThreads,
    boolean strictResolution,  // false = soft-fail
    int batchSize,             // для REMOTE_BATCH, default 5000
    Map<String, String> extra  // extensibility
) {
    public static HoundConfig defaultEmbedded(String dialect) { ... }
    public static HoundConfig defaultRemoteBatch(String dialect, String url) { ... }
}
```

- Удалить `RunConfig` inner class из `HoundApplication` — только через `HoundConfig`
- Обновить точки инициализации → создают `HoundConfig`
- Прогнать 25 тестов

**Acceptance:** HoundConfig компилируется, фабричные методы работают, 25 тестов зелёные, Q27 закрыт

---

### День 2 — HoundParser interface + Gradle

**Д2 · AM — C.1.1 HoundParser interface (~3 ч)**

Новый файл: `libraries/hound/src/main/java/com/hound/api/HoundParser.java`

```java
public interface HoundParser {
    ParseResult parse(Path file, HoundConfig config);
    ParseResult parse(Path file, HoundConfig config, HoundEventListener listener);
    List<ParseResult> parseBatch(List<Path> files, HoundConfig config);
    List<ParseResult> parseBatch(List<Path> files, HoundConfig config, HoundEventListener listener);
}
```

Новый файл: `libraries/hound/src/main/java/com/hound/api/ParseResult.java`

```java
public record ParseResult(
    String file, int atomCount, int vertexCount, int edgeCount,
    double resolutionRate, List<String> warnings, List<String> errors, long durationMs
) {}
```

- Рефакторить `HoundApplication` в `HoundParserImpl implements HoundParser`
- `HoundMain` (новый thin wrapper): парсит CLI args → `HoundConfig` → делегирует `HoundParserImpl`
- Все public-facing классы → пакет `com.hound.api`

**Д2 · PM — C.1.4 Gradle java-library (~2 ч)**

Файл: `libraries/hound/build.gradle` (java-library уже есть, нужно разделить api{}/implementation{})

```gradle
dependencies {
    api 'org.antlr:antlr4-runtime:4.13.2'          // транзитивно Dali
    implementation "com.arcadedb:arcadedb-engine:..."
    implementation "com.arcadedb:arcadedb-network:26.3.1"
    // ... остальное в implementation{}
}
jar {
    manifest { attributes 'Main-Class': 'com.hound.main.HoundMain' }
}
```

- `./gradlew :libraries:hound:build` — OK
- `./gradlew :libraries:hound:run` — standalone не сломан
- 25 тестов зелёные

---

### День 3 — HoundEventListener + wire-up

**Д3 · AM — C.1.3 HoundEventListener (~3 ч)**

Новый файл: `libraries/hound/src/main/java/com/hound/api/HoundEventListener.java`

```java
public interface HoundEventListener {
    void onFileParseStarted(String file, String dialect);
    void onAtomExtracted(String file, int atomCount, String atomType);
    void onFileParseCompleted(String file, ParseResult result);
    void onError(String file, Throwable error);
    default void onRecordRegistered(String file, String varName) {}
}
```

Новый файл: `libraries/hound/src/main/java/com/hound/api/NoOpHoundEventListener.java`

```java
public final class NoOpHoundEventListener implements HoundEventListener {
    public static final NoOpHoundEventListener INSTANCE = new NoOpHoundEventListener();
    // все методы — пустые реализации
}
```

Wire-up:
- `UniversalSemanticEngine` (`semantic/engine/`) — пробросить listener через конструктор (не статически)
- `AtomProcessor` — добавить `listener.onAtomExtracted()` в метод `registerAtom()`
- `StructureAndLineageBuilder` — добавить `listener.onRecordRegistered()` в `ensureRecord()`

**Д3 · PM — Gate проверка C.1 (~2 ч)**

- Написать smoke-test: модуль `dali-smoke` (временный), `implementation project(':libraries:hound')`, вызов `HoundParserImpl.parse()`
- Проверить порядок событий: `started → atomExtracted(N) → completed`
- Прогнать 25 JUnit + smoke-test

**Gate C.1:**
- ✓ `HoundParser`, `HoundConfig`, `HoundEventListener`, `ParseResult` — в `com.hound.api`
- ✓ Smoke-test из внешнего модуля: `implementation project(':libraries:hound')` + `parse()` → OK
- ✓ `./gradlew :libraries:hound:run` — не сломан
- ✓ 25 JUnit + smoke-test зелёные
- ✓ Q27 зафиксирован в `DECISIONS_LOG`

---

## Фаза 2 · C.2 Dali Core skeleton (~8–10 дней)

### День 4–5 — Gradle scaffold + dali-models

**Д4 — dali-models расширение (~полдня)**

Файл: `shared/dali-models/src/main/java/aida/models/` (уже содержит EventLevel, EventType, HeimdallEvent)

Добавить:
```java
record ParseSessionInput(String dialect, String source, boolean preview) {}
record Session(String id, SessionStatus status, int progress, int total,
               String dialect, Instant startedAt, Instant updatedAt) {}
enum SessionStatus { QUEUED, RUNNING, COMPLETED, FAILED, CANCELLED }
```

Обновить `shared/dali-models/build.gradle`: `id 'java'` → `id 'java-library'`

Добавить в settings.gradle: `include 'services:dali'` (если ещё нет)

**Д5 — Dali Quarkus scaffold (~1 день)**

Новый модуль: `services/dali/`

```gradle
// services/dali/build.gradle
plugins { id 'io.quarkus' version "${quarkusVersion}" }
dependencies {
    implementation project(':libraries:hound')
    implementation project(':shared:dali-models')
    implementation 'io.quarkus:quarkus-rest-jackson'
    implementation 'io.quarkus:quarkus-arc'
    implementation 'org.jobrunr:quarkus-jobrunr'
    implementation 'io.quarkus:quarkus-micrometer'
    testImplementation 'io.quarkus:quarkus-junit5'
}
```

- `DaliApplication.java` — пустой Quarkus entry point
- `./gradlew :services:dali:quarkusDev` → стартует на `:9090`
- `curl :9090/q/health` → `UP`

> ⚠ **Риск:** Из SHUTTLE известно что Quarkus "сложно дался". +1 день буфера. CDI injection строгий — не Spring-style.

---

### День 6–7 — REST API + ParseJob

**Д6 — SessionResource + JobRunr enqueue (~1 день)**

```
services/dali/src/main/java/.../rest/SessionResource.java
services/dali/src/main/java/.../service/SessionService.java
```

- `POST /api/sessions` → `202 Accepted` + `Session{status: QUEUED}`
- `GET /api/sessions/{id}` → статус `QUEUED → RUNNING → COMPLETED`
- JobRunr dashboard на `:9090/dashboard`

**Д7 — ParseJob + HoundParser in-JVM call (~1 день)**

```
services/dali/src/main/java/.../job/ParseJob.java
```

- `HoundParserImpl` зарегистрирован как CDI `@ApplicationScoped` bean
- `DaliHoundListener` — форвардит события Hound в лог (HEIMDALL ещё нет)
- End-to-end smoke: `POST /api/sessions` → `ParseJob` → Hound разобрал тестовый `.sql` → `COMPLETED`
- ✓ Без subprocess, без HTTP к Hound
- ✓ JobRunr retry: exception → перезапуск до 3 раз

---

### День 8–10 — FRIGG StorageProvider

**Д8 — FriggSchemaInitializer (~1 день)**

ArcadeDB document types в FRIGG (создаются при первом старте Dali):
- `jobrunr_jobs`, `jobrunr_recurring_jobs`, `jobrunr_servers`, `jobrunr_metadata`

**Д9 — StorageProvider core methods (~1 день, ~150 LoC)**

```
services/dali/src/main/java/.../storage/ArcadeDbStorageProvider.java
```

Обязательные методы:
- `save(Job)`, `getJobById`, `getJobs(state, page)`, `delete(Job)`
- `announceBackgroundJobServer`, `signalBackgroundJobServerStopped`

Сериализация через `JobMapper` (предоставляет JobRunr):
```java
String jobJson = jobMapper.serialize(job);
frigg.command("INSERT INTO jobrunr_jobs SET id=?, state=?, jobAsJson=?, createdAt=?",
              job.getId(), job.getState().name(), jobJson, now());
```

Зарегистрировать как `@ApplicationScoped` CDI bean в Quarkus.

> ⚠ **Flush order:** При завершении `ParseJob` FRIGG получает статус job. YGG в это время должен уже иметь все DaliRecord vertices. Прямой зависимости нет, но проверить timing в логах.

**Д10 — Интеграционный тест + JobRunr compatibility (~1 день)**

- Поднять `docker-compose up frigg` → Dali с FRIGG (ArcadeDB `:2481`)
- `POST /api/sessions` → job выполнился → статус в FRIGG → рестарт Dali → статус сохранился
- JobRunr dashboard: completed job с историей
- Retry test: намеренно уронить `ParseJob` → 3 retry → `FAILED` → статус в FRIGG = `FAILED`

**Acceptance criteria StorageProvider:**
- ✓ Jobs персистируются в FRIGG между рестартами Dali
- ✓ `getJobs(QUEUED)` → ожидающие jobs при старте
- ✓ Heartbeat обновляется каждые ~5 сек (JobRunr default)
- ✓ JobRunr dashboard работает с ArcadeDB backend
- ✓ FRIGG schema (4 document types) создаётся автоматически при первом старте

---

## Документация и отчёт (по завершении)

### Документы к созданию

| Файл | Содержание |
|---|---|
| `docs/sprints/sprint-c1-c2-hound-dali-plan.md` | Этот план (сохраняется в начале) |
| `docs/architecture/hound-api-design.md` | Описание `hound.api` пакета, все 4 класса |
| `docs/architecture/dali-overview.md` | Dali architecture, JobRunr + ArcadeDB StorageProvider |
| `DECISIONS_LOG.md` | Q27 закрыт: полный список полей `HoundConfig` |

### Тесты к написанию

| Тест | Где | Что проверяет |
|---|---|---|
| `HoundConfigTest` | `libraries/hound/test` | Фабричные методы, сериализация |
| `HoundParserImplTest` | `libraries/hound/test` | `parse()` + `parseBatch()` с mock listener |
| `HoundEventListenerOrderTest` | `libraries/hound/test` | Порядок: started → atomExtracted(N) → completed |
| `dali-smoke` (temp module) | `tests/dali-smoke` | `implementation project(':libraries:hound')` + вызов parse |
| `SessionResourceTest` | `services/dali/test` | POST/GET /api/sessions (QuarkusTest) |
| `ParseJobTest` | `services/dali/test` | Job execution + listener events |
| `ArcadeDbStorageProviderTest` | `services/dali/test` | CRUD JobRunr jobs в FRIGG |

### Отчёт в диалог (финальный)

По завершении фаз выдать в чат:
```
✅ C.1 GATE:
  - hound.api: HoundParser, HoundConfig, HoundEventListener, ParseResult
  - ./gradlew :libraries:hound:build — OK
  - ./gradlew :libraries:hound:run — OK
  - 25 JUnit + smoke-test — зелёные
  - Q27 закрыт в DECISIONS_LOG

✅ C.2 GATE:
  - services/dali стартует на :9090
  - POST /api/sessions → 202, job в JobRunr
  - ParseJob вызывает Hound in-JVM
  - ArcadeDbStorageProvider персистит jobs в FRIGG
  - Все тесты зелёные

📄 Документация: docs/architecture/hound-api-design.md, dali-overview.md
🔀 PR: feature/c1-hound-java-library-dali-core → master
```

---

## Критические файлы

| Файл | Действие |
|---|---|
| `libraries/hound/src/main/java/com/hound/HoundApplication.java` | Рефакторить → `HoundParserImpl` + `HoundMain` |
| `libraries/hound/src/main/java/com/hound/semantic/engine/UniversalSemanticEngine.java` | Пробросить listener через конструктор |
| `libraries/hound/src/main/java/com/hound/semantic/engine/AtomProcessor.java` | Добавить `listener.onAtomExtracted()` |
| `libraries/hound/build.gradle` | Разделить `api{}`/`implementation{}` |
| `settings.gradle` | Добавить `include 'services:dali'` |
| `shared/dali-models/build.gradle` | `java` → `java-library` |
| `shared/dali-models/src/.../aida/models/` | Добавить `ParseSessionInput`, `Session`, `SessionStatus` |
| `services/dali/` | Создать весь модуль |
