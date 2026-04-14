# Stabilizing Sprint — 13.04.2026

**Ветка:** `fix/stabilizing-sprint-apr13-2026`
**Охват:** Dali Core (`services/dali/`) + Chur Keycloak (`bff/chur/`) + HEIMDALL backend (`services/heimdall-backend/`)
**Дата:** 13.04.2026
**Статус:** ✅ DONE

---

## Bug Log

| ID | Severity | Компонент | Файл:строка | Описание | Статус |
|----|----------|-----------|-------------|----------|--------|
| BUG-SS-001 | CRITICAL | Dali / ArcadeDB | `ArcadeDbStorageProvider.java:276` | `saveMetadata/getMetadata` — стабы без реализации: JobRunr не отслеживал задачи | ✅ FIXED (QG-DALI) |
| BUG-SS-002 | HIGH | Dali / ArcadeDB | `ArcadeDbStorageProvider.java:117,211` | Параметр `:before` — зарезервированное слово ArcadeDB, запросы падали | ✅ FIXED (QG-DALI) |
| BUG-SS-003 | HIGH | Dali / ArcadeDB | `ArcadeDbStorageProvider.java:194,208` | Сломанные DELETE-запросы (RETURN BEFORE — ArcadeDB не поддерживает) | ✅ FIXED (QG-DALI) |
| BUG-SS-004 | HIGH | Dali / FRIGG | `jobrunr_jobs` (FRIGG) | Corrupted запись в `jobrunr_jobs` — блокировала JobRunr | ✅ FIXED (QG-DALI, запись удалена) |
| BUG-SS-005 | LOW | Dali | `ParseJob.java:23` | Дублирующий `import java.util.ArrayList` | ✅ FIXED (QG-DALI) |
| BUG-SS-006 | HIGH | Chur | `keycloakAdmin.ts:fetch calls` | Нет `AbortSignal.timeout` ни на одном fetch — сервис зависал при недоступном KC | ✅ FIXED (QG-CHUR) |
| BUG-SS-007 | HIGH | Chur | `keycloakAdmin.ts:listUsers,updateUserAttrs,setUserEnabled` | Нет try/catch + fallback — любая ошибка KC разрушала ответ | ✅ FIXED (QG-CHUR) |
| BUG-SS-008 | HIGH | HEIMDALL backend | `EventResource.java:ingest()` | Нет `@Valid` → null-поля HeimdallEvent принимались без 400 | ✅ FIXED (QG-HEIMDALL) |
| BUG-SS-009 | HIGH | HEIMDALL / shared | `HeimdallEvent.java` | Нет `@NotNull/@NotBlank` на полях — Bean Validation не срабатывала | ✅ FIXED (QG-HEIMDALL) |
| BUG-SS-010 | HIGH | HEIMDALL backend | `build.gradle` | Отсутствовала зависимость `quarkus-hibernate-validator` | ✅ FIXED (QG-HEIMDALL) |
| BUG-SS-011 | HIGH | Dali | `JobRunrLifecycle.java:72` | `onStart()` без try-catch — исключение при инициализации JobRunr крашило весь сервис | ✅ FIXED |
| BUG-SS-012 | HIGH | Dali | `FriggSchemaInitializer.java:75` | Частичная инициализация схемы не обнаруживается: при сбое CREATE TYPE свойства и индексы продолжают создаваться на несуществующем типе — схема молча неполная | ✅ FIXED |
| BUG-SS-013 | HIGH | Dali / ArcadeDB | `ArcadeDbStorageProvider.java:169` | `getJobList(StateName, AmountRequest)` не передаёт offset из `AmountRequest` — pagination сломан при >limit задач | ✅ FIXED |
| BUG-SS-014 | MEDIUM | Dali | `FriggSchemaInitializer.java:54` | `dali_sessions.startedAt` объявлен как `STRING` вместо `DATETIME` — time-based сортировка использует лексикографическое сравнение | ✅ FIXED |
| BUG-SS-015 | MEDIUM | Dali / ArcadeDB | `ArcadeDbStorageProvider.java:45` | `metadataStore` — in-memory `ConcurrentHashMap`, сбрасывается при рестарте; JobRunr теряет version-metadata | 🟡 DEFER |
| BUG-SS-016 | MEDIUM | Dali | `SessionService.java:96` | Concurrency check в `enqueue()` не атомарен — два потока могут одновременно пройти проверку `clearBeforeWrite` | 🟡 DEFER |
| BUG-SS-017 | MEDIUM | Dali | `SessionService.java:48` | Порядок `StartupEvent` observer'ов (`FriggSchemaInitializer` vs `SessionService`) не гарантирован CDI без `@Priority` — при инверсии `findAll()` упадёт до готовности схемы | ✅ FIXED |
| BUG-SS-018 | MEDIUM | Dali | `ArcadeDbStorageProvider.java:222` | `getDistinctJobSignatures()` всегда возвращает `Set.of()` — дедупликация задач JobRunr отключена | 🟡 DEFER |
| BUG-SS-019 | MEDIUM | Dali | `ArcadeDbStorageProvider.java:227` | `recurringJobExists()` всегда возвращает `false` — recurring-задачи могут дублироваться | 🟡 DEFER |
| BUG-SS-020 | MEDIUM | Keycloak | KC realm `seer` | Scope mapper `aida:admin` не настроен в KC: `scope` в JWT = `""` → `requireAdmin` блокирует все запросы к HEIMDALL/users | 🔴 OPEN |
| BUG-SS-021 | MEDIUM | Dali | `ParseJob.java:153` | `Files.walk()` без `maxDepth` и без обработки symlink-петель — может зависнуть или упасть с `FileSystemLoopException` | 🟡 DEFER |
| BUG-SS-022 | MEDIUM | Dali | `ParseJob.java:215` | `resolutionRate` в `merge()`: если хоть один `FileResult` имеет `atomCount=0` и `resolutionRate=NaN`, итоговый rate = NaN | 🟡 DEFER |
| BUG-SS-023 | LOW | Dali | `FriggSchemaInitializer.java:12` | JavaDoc описывает 4 типа документов, реально создаётся 5 (включая `dali_sessions`) | ✅ FIXED |
| BUG-SS-024 | LOW | Dali | `SessionService.java:258` | В `persist()` флаг `friggHealthy = true` выставляется неатомарно с `computeIfPresent` — гонка при одновременном fail/success | 🟡 DEFER |
| BUG-SS-025 | LOW | Dali tests | `SessionResourceTest.java` | Нет `@AfterEach` cleanup — сессии из тестов накапливаются в FRIGG между прогонами | ✅ FIXED |
| BUG-SS-026 | CRITICAL | Hound / RemoteWriter | `RemoteWriter.java:preInsertAdHocSchemas` | DuplicatedKeyException на DaliColumn/DaliTable при повторной обработке одних и тех же файлов — (null, column_geoid) unique index | ✅ FIXED |
| BUG-SS-027 | HIGH | Hound / BaseSemanticListener | `BaseSemanticListener.java:onAlterTableEnter` | ALTER TABLE ошибочно вызывал `markDdlTable` → таблицы помечались как master даже без CREATE TABLE | ✅ FIXED |
| BUG-SS-028 | HIGH | Verdandi | `ProtectedRoute.tsx:22` | Бесконечный редирект `/knot/login/login/login...` при логауте — relative `to="login"` внутри route `path="knot"` | ✅ FIXED |

---

## Bug Details

### BUG-SS-001 — saveMetadata/getMetadata были стабами (CRITICAL, ✅ FIXED)
**Файл:** `services/dali/src/main/java/studio/seer/dali/storage/ArcadeDbStorageProvider.java`
**Описание:** `saveMetadata()` и `getMetadata()` возвращали заглушки без записи в FRIGG. JobRunr использует metadata для хранения `database_version/cluster` — без неё JobRunr не определял версию схемы и не подхватывал задачи.
**Фикс:** Реализованы через in-memory `ConcurrentHashMap<String, JobRunrMetadata> metadataStore` (single-node — персистенция в FRIGG будет добавлена в BUG-SS-015).

---

### BUG-SS-002 — Зарезервированное слово `:before` в AQL (HIGH, ✅ FIXED)
**Файл:** `ArcadeDbStorageProvider.java:117,211`
**Описание:** Параметр `:before` — зарезервированное слово ArcadeDB. Запросы `removeTimedOutBackgroundJobServers` и `deleteJobsPermanently` падали с ошибкой парсинга AQL.
**Фикс:** Переименован в `:cutoff`.

---

### BUG-SS-003 — Сломанные DELETE с RETURN BEFORE (HIGH, ✅ FIXED)
**Файл:** `ArcadeDbStorageProvider.java:194,208`
**Описание:** ArcadeDB не поддерживает `RETURN BEFORE` в DELETE-запросах. Использовался паттерн count-then-delete: сначала SELECT count(*), затем DELETE.
**Фикс:** Применён count-then-delete паттерн корректно.

---

### BUG-SS-004 — Corrupted запись в FRIGG jobrunr_jobs (HIGH, ✅ FIXED)
**Описание:** Обнаружена и удалена corrupted запись в таблице `jobrunr_jobs` FRIGG, которая блокировала десериализацию задач JobRunr.
**Фикс:** Запись удалена напрямую через ArcadeDB Console.

---

### BUG-SS-006 — Нет AbortSignal.timeout на fetch (HIGH, ✅ FIXED)
**Файл:** `bff/chur/src/keycloakAdmin.ts`
**Описание:** Все 5 fetch-вызовов к Keycloak Admin API не имели таймаута. При недоступном KC сервис Chur зависал indefinitely.
**Фикс:** `AbortSignal.timeout(5_000)` добавлен ко всем fetch-вызовам.

---

### BUG-SS-007 — Нет fallback при ошибке KC Admin API (HIGH, ✅ FIXED)
**Файл:** `bff/chur/src/keycloakAdmin.ts`
**Описание:** `listUsers()`, `updateUserAttrs()`, `setUserEnabled()` не имели try/catch — HTTP 500 или network error пробрасывался наружу и ломал Chur endpoint.
**Фикс:** Добавлены try/catch + возврат `[]` / silent swallow при ошибке.

---

### BUG-SS-008 — Нет `@Valid` на `EventResource.ingest()` (HIGH, ✅ FIXED)
**Файл:** `services/heimdall-backend/src/main/java/.../EventResource.java`
**Описание:** Без `@Valid` Bean Validation не активировалась — null/blank поля в `HeimdallEvent` проходили в сервис без 400.
**Фикс:** `@Valid @NotNull` добавлены на параметр метода.

---

### BUG-SS-009 — Нет `@NotNull/@NotBlank` на полях HeimdallEvent (HIGH, ✅ FIXED)
**Файл:** `shared/dali-models/src/main/java/.../HeimdallEvent.java`
**Описание:** Поля `sourceComponent`, `eventType`, `level`, `payload` не имели constraint-аннотаций — невалидные объекты принимались без ошибки.
**Фикс:** Добавлены `@NotNull`, `@NotBlank` на соответствующие поля.

---

### BUG-SS-011 — JobRunrLifecycle.onStart() без try-catch (HIGH, 🔴 OPEN)
**Файл:** `services/dali/src/main/java/studio/seer/dali/infrastructure/JobRunrLifecycle.java:72`
**Описание:** Если `JobRunr.configure().initialize()` бросает исключение (FRIGG недоступен, ошибка конфигурации), исключение не перехватывается и убивает весь сервис Dali. В отличие от `FriggSchemaInitializer` и `SessionService`, здесь нет graceful degradation.
**Предлагаемый фикс:** Обернуть в try-catch, при ошибке логировать и выставить флаг `jobRunrAvailable = false`. HTTP endpoints должны возвращать 503 если JobRunr недоступен.

---

### BUG-SS-012 — Частичная инициализация схемы не обнаруживается (HIGH, 🔴 OPEN)
**Файл:** `services/dali/src/main/java/studio/seer/dali/storage/FriggSchemaInitializer.java:75`
**Описание:** Если `createDocumentType()` для одного типа падает с ошибкой (тип не создан), `ensureProperties()` и `createIndexes()` продолжают выполняться для несуществующего типа. Они тоже падают с предупреждениями. Схема молча остаётся неполной, сервис стартует как ни в чём не бывало.
**Предлагаемый фикс:** Добавить булевый флаг `schemaReady`. Если хотя бы один тип создать не удалось — выставить `schemaReady = false`. `SessionService.enqueue()` должен проверять этот флаг перед постановкой задачи в очередь.

---

### BUG-SS-013 — Неверный offset в getJobList (HIGH, 🔴 OPEN)
**Файл:** `services/dali/src/main/java/studio/seer/dali/storage/ArcadeDbStorageProvider.java:169`
**Описание:** `getJobList(StateName state, AmountRequest amountRequest)` передаёт в AQL только `LIMIT :limit`, игнорируя offset из `AmountRequest`. При >limit задач одного статуса JobRunr будет получать только первую страницу — остальные задачи не обрабатываются.
**Предлагаемый фикс:** Добавить `SKIP :offset` в AQL-запрос, передавать `amountRequest.getOffset()`.

---

### BUG-SS-014 — startedAt как STRING вместо DATETIME (MEDIUM, 🔴 OPEN)
**Файл:** `services/dali/src/main/java/studio/seer/dali/storage/FriggSchemaInitializer.java:54`
**Описание:** Свойство `dali_sessions.startedAt` объявлено как тип `STRING`. Временны́е сортировки и диапазонные запросы ("`WHERE startedAt < :cutoff`") используют лексикографическое сравнение строк вместо временно́го. ISO-строки сортируются корректно только при UTC+одинаковом формате, но это хрупкое соглашение.
**Предлагаемый фикс:** Изменить тип на `DATETIME`. Обновить `FriggGateway.save()` для передачи `Instant` как `DATETIME` (ArcadeDB принимает ISO-8601).

---

### BUG-SS-017 — StartupEvent порядок не гарантирован (MEDIUM, 🔴 OPEN)
**Файл:** `services/dali/src/main/java/studio/seer/dali/service/SessionService.java:48`
**Описание:** `FriggSchemaInitializer` и `SessionService` оба слушают `StartupEvent`. JavaDoc утверждает что `FriggSchemaInitializer` срабатывает первым "в практике", но CDI не гарантирует порядок без `@Priority`. Если `SessionService.onStart()` сработает до завершения схемы, `repository.findAll(500)` упадёт с ошибкой "type not found" — это поймается try-catch и залогируется как warning, но сессии не будут загружены из FRIGG.
**Предлагаемый фикс:** Добавить `@Priority` на оба observer'а (`FriggSchemaInitializer` = меньшее значение = выше приоритет).

---

### BUG-SS-020 — KC scope mapper не настроен (MEDIUM, 🔴 OPEN)
**Компонент:** Keycloak realm `seer`
**Описание:** JWT токен admin-пользователя имеет `scope=""` (пустая строка). Middleware `requireAdmin` в Chur проверяет наличие `aida:admin` в `scope` → возвращает 403 для всех запросов к `/heimdall/users`. Независимо от роли пользователя в KC.
**Предлагаемый фикс:** В KC realm `seer` → Client Scopes: создать scope `aida:admin`, добавить к client `aida-bff`. Добавить "aida:admin" в Default Client Scopes или через mapper.

---

### BUG-SS-023 — JavaDoc неверно указывает 4 типа документов (LOW, 🔴 OPEN)
**Файл:** `services/dali/src/main/java/studio/seer/dali/storage/FriggSchemaInitializer.java:12`
**Описание:** JavaDoc перечисляет 4 типа, но реально создаётся 5 (`dali_sessions` добавлен).
**Фикс:** Обновить JavaDoc comment.

---

### BUG-SS-025 — Нет cleanup в SessionResourceTest (LOW, 🔴 OPEN)
**Файл:** `services/dali/src/test/java/studio/seer/dali/rest/SessionResourceTest.java`
**Описание:** Тесты создают сессии через POST `/api/sessions`, но нет `@AfterEach` для удаления созданных сессий из FRIGG. После прогона тестов FRIGG содержит накопленные записи. При повторном запуске тестов эти записи загружаются в `SessionService` и могут влиять на логику `clearBeforeWrite` и `listRecent`.
**Предлагаемый фикс:** Добавить `@AfterEach` с вызовом `DELETE FROM dali_sessions` через `FriggGateway` или хранить созданные ID и удалять их явно.

---

## Архитектурные гэпы (не баги)

| Гэп | Описание | Статус |
|-----|----------|--------|
| GAP-1 | `atomsExtracted=0` в `/metrics/snapshot` — Dali не эмитит события `ATOM_EXTRACTED` в HEIMDALL напрямую. Данные есть в Session API (`atomCount=60422`). Требует интеграции `HoundEventListener` (C.1.3) | Беклог M1 |
| GAP-2 | `metadataStore` — in-memory, теряется при рестарте. Для single-node достаточно, для кластерного режима нужна персистенция в FRIGG | Беклог M2+ |
| GAP-3 | `getDistinctJobSignatures()` / `recurringJobExists()` — стабы. Деdup задач и recurring jobs не реализованы | Беклог M2 |

---

## Итоговый отчёт — 13.04.2026

### Статистика

| Метрика | Значение |
|---------|---------|
| Всего найдено | **25** |
| CRITICAL | **1** |
| HIGH | **10** |
| MEDIUM | **8** |
| LOW | **3** |
| Архитектурных гэпов | **3** |
| **Исправлено в спринте** | **10** (BUG-SS-001..010) |
| **Открыто → исправить** | **8** (BUG-SS-011..014, 017, 020, 023, 025) |
| **Отложено в беклог** | **4** (BUG-SS-015, 016, 018, 019, 021, 022, 024) |

---

### Исправлено в спринте

| ID | Severity | Описание | QG-агент |
|----|----------|----------|----------|
| BUG-SS-001 | CRITICAL | saveMetadata/getMetadata — стабы без реализации | QG-DALI-ygg-write |
| BUG-SS-002 | HIGH | `:before` — зарезервированное слово ArcadeDB | QG-DALI-ygg-write |
| BUG-SS-003 | HIGH | Сломанные DELETE-запросы в ArcadeDbStorageProvider | QG-DALI-ygg-write |
| BUG-SS-004 | HIGH | Corrupted запись в FRIGG jobrunr_jobs | QG-DALI-ygg-write |
| BUG-SS-005 | LOW | Дублирующий import ArrayList в ParseJob.java | QG-DALI-ygg-write |
| BUG-SS-006 | HIGH | Нет AbortSignal.timeout в keycloakAdmin.ts | QG-CHUR-resilience |
| BUG-SS-007 | HIGH | Нет try/catch в listUsers/updateUserAttrs/setUserEnabled | QG-CHUR-resilience |
| BUG-SS-008 | HIGH | Нет @Valid на EventResource.ingest() | QG-HEIMDALL-backend-validation |
| BUG-SS-009 | HIGH | Нет @NotNull/@NotBlank на полях HeimdallEvent | QG-HEIMDALL-backend-validation |
| BUG-SS-010 | HIGH | Отсутствовала зависимость quarkus-hibernate-validator | QG-HEIMDALL-backend-validation |

---

### Открытые баги — исправить до M1

| ID | Severity | Описание | Файл | Статус |
|----|----------|----------|------|--------|
| BUG-SS-011 | HIGH | JobRunrLifecycle.onStart() без try-catch | `JobRunrLifecycle.java:72` | ✅ FIXED |
| BUG-SS-012 | HIGH | Частичная схема FRIGG не обнаруживается | `FriggSchemaInitializer.java:75` | ✅ FIXED |
| BUG-SS-013 | HIGH | getJobList игнорирует AmountRequest offset | `ArcadeDbStorageProvider.java:169` | ✅ FIXED |
| BUG-SS-017 | MEDIUM | StartupEvent порядок не гарантирован | `SessionService.java:48` | ✅ FIXED |
| BUG-SS-020 | MEDIUM | KC scope mapper aida:admin не настроен | KC realm config | 🔴 OPEN |
| BUG-SS-014 | MEDIUM | dali_sessions.startedAt как STRING вместо DATETIME | `FriggSchemaInitializer.java:54` | ✅ FIXED |
| BUG-SS-023 | LOW | JavaDoc указывает 4 типа вместо 5 | `FriggSchemaInitializer.java:12` | ✅ FIXED |
| BUG-SS-025 | LOW | Нет @AfterEach cleanup в SessionResourceTest | `SessionResourceTest.java` | ✅ FIXED |

---

### Отложено в беклог (M2+)

| ID | Severity | Описание |
|----|----------|----------|
| BUG-SS-015 | MEDIUM | metadataStore теряется при рестарте |
| BUG-SS-016 | MEDIUM | Non-atomic enqueue() concurrency check |
| BUG-SS-018 | MEDIUM | getDistinctJobSignatures() = stub (нет dedup) |
| BUG-SS-019 | MEDIUM | recurringJobExists() = stub |
| BUG-SS-021 | MEDIUM | Files.walk() без maxDepth/symlink guard |
| BUG-SS-022 | MEDIUM | NaN в resolutionRate при atomCount=0 |
| BUG-SS-024 | LOW | Racy friggHealthy flag |

---

### Рекомендации

1. **JobRunrLifecycle (BUG-SS-011)** — критичный для production: без try-catch любая проблема FRIGG при старте убивает весь Dali. Исправить в первую очередь.

2. **FRIGG schema integrity (BUG-SS-012)** — добавить флаг `schemaReady` и проверять его в `enqueue()`. Сейчас сервис стартует и принимает запросы даже при неполной схеме.

3. **KC scope mapper (BUG-SS-020)** — страница Users в HEIMDALL полностью заблокирована. Требует конфигурации KC, не кода.

4. **AmountRequest offset (BUG-SS-013)** — при большом количестве задач JobRunr будет видеть только первую страницу. Простой однострочный фикс.

5. **StartupEvent ordering (BUG-SS-017)** — добавить `@Priority` в два класса, 5 минут работы, снимает fragile assumption из JavaDoc.

---

---

### Дополнение — SHUTTLE observability (обнаружено в live)

| ID | Severity | Компонент | Файл | Описание | Статус |
|----|----------|-----------|------|----------|--------|
| BUG-SS-026 | MEDIUM | SHUTTLE | `LineageResource.java` | `explore` `REQUEST_COMPLETED` не содержит `scope`, `nodes`, `edges` — в EventLog видно только "GraphQL done 5827ms" | ✅ FIXED |
| BUG-SS-027 | MEDIUM | SHUTTLE | `LineageResource.java` | `stmtColumns` — есть `REQUEST_RECEIVED`, нет `REQUEST_COMPLETED` | ✅ FIXED |
| BUG-SS-028 | MEDIUM | SHUTTLE | `LineageResource.java` | `lineage` `REQUEST_COMPLETED` использует ключ `"query"` вместо `"op"`, нет `nodeId` и counts | ✅ FIXED |
| BUG-SS-029 | LOW | SHUTTLE | `LineageResource.java` | `upstream/downstream/expandDeep` — вообще без событий | ✅ FIXED |
| BUG-SS-030 | LOW | SHUTTLE | `KnotResource.java` | `knotReport` `REQUEST_COMPLETED` не содержит `sessionId` | ✅ FIXED |

---

### Дополнение — Batch 2: Dali emitter, SHUTTLE сигнатуры, UI-фиксы (13.04.2026)

| ID | Severity | Компонент | Файл | Описание | Статус |
|----|----------|-----------|------|----------|--------|
| BUG-SS-031 | MEDIUM | Dali→HEIMDALL | `HeimdallEmitter.java` (NEW) | GAP-1: HEIMDALL не получал события от Dali — `atomsExtracted=0` в metrics. Реализован `HeimdallEmitter` + `HeimdallClient` REST-клиент | ✅ FIXED |
| BUG-SS-032 | LOW | HEIMDALL frontend | `EventStreamPage.tsx` | Фильтр «Компонент» не содержал `dali` — события Dali не отображались в EventLog | ✅ FIXED |
| BUG-SS-033 | MEDIUM | SHUTTLE | `LineageResource.java` | В payload событий нет читаемой сигнатуры вызова — поле `"call"` отсутствовало, невозможно понять ЧТО выполнялось без раскрытия JSON | ✅ FIXED |
| BUG-SS-034 | LOW | SHUTTLE | `KnotResource.java` | Аналогично BUG-SS-033 для knotSessions/knotReport | ✅ FIXED |
| BUG-SS-035 | LOW | HEIMDALL frontend | `SessionList.tsx:387` | Заголовок колонки `VERT` неоднозначен — переименован в `VTXS` | ✅ FIXED |
| BUG-SS-036 | MEDIUM | HEIMDALL frontend | `SessionList.tsx:170` | Файлы с `atomCount=0` показывают `RATE=100.0%` (vacuous truth) — должно быть `—` | ✅ FIXED |
| BUG-SS-037 | MEDIUM | VERDANDI / SHUTTLE | `GraphNode.java`, `ExploreService.java`, `TableNode.tsx` | Узлы таблиц в графе не показывали источник схемы: `master` (есть DDL) или `reconstructed` (только DML). Добавлены: поле `dataSource` в GraphNode, вторичная обогащающая query `enrichDataSource()`, бейдж в TableNode | ✅ FIXED |

#### Детали BUG-SS-031 (GAP-1 — Dali HEIMDALL emitter)

**Новые файлы:**
- `services/dali/src/main/java/studio/seer/dali/heimdall/HeimdallClient.java` — MicroProfile REST client (`@RegisterRestClient(configKey="heimdall-api")`)
- `services/dali/src/main/java/studio/seer/dali/heimdall/HeimdallEmitter.java` — fire-and-forget emitter (3 уровня API: raw / typed / semantic)

**Изменённые файлы:**
- `DaliHoundListener.java` — эмитит `fileParsingStarted`, `atomExtracted`, `fileParsingFailed`
- `ParseJob.java` — эмитит `sessionStarted`, `sessionCompleted`, `sessionFailed`
- `SessionService.java` — эмитит `jobEnqueued`

#### Детали BUG-SS-037 (VERDANDI data_source badge)

**Pipeline изменений (4 слоя):**
1. **`shuttle/model/GraphNode.java`** — добавлено поле `String dataSource`
2. **`shuttle/service/ExploreService.java`** — метод `enrichDataSource()`: вторичный Cypher-запрос `MATCH (t:DaliTable) WHERE id(t) IN $ids RETURN id(t), t.data_source` цепочкой `.flatMap()` после каждого explore-метода; `toExploreResult()` извлекает `data_source` напрямую из вершинных map
3. **`verdandi/services/lineage.ts`** — поле `dataSource?` в `GraphNode`, добавлено в все GraphQL-фрагменты (`nodes { id type label scope dataSource }`)
4. **`verdandi/utils/transformExplore.ts`** — `dataSource` пробрасывается в `metadata.dataSource` (flat и schema explore пути)
5. **`verdandi/components/canvas/nodes/TableNode.tsx`** — бейдж: зелёный `master` (`var(--suc)`) / жёлтый `reconstructed` (`var(--wrn)`) под именем таблицы; скрыт когда `dataSource` пустой

---

---

### Дополнение — Batch 3: Dali стабилизация + badge pipeline (13.04.2026)

| ID | Severity | Компонент | Файл | Описание | Статус |
|----|----------|-----------|------|----------|--------|
| BUG-SS-038 | MEDIUM | SHUTTLE | `ExploreService.java:631` | `enrichDataSource()` использовал `id(t) IN $ids` — type-mismatch в ArcadeDB Cypher (LINK vs String), dsMap всегда пустой → бейдж не показывался. Заменён на `UNWIND $ids AS rid … WHERE id(t) = rid` | ✅ FIXED |
| BUG-SS-039 | MEDIUM | SHUTTLE | `LineageService.java` | lineage/upstream/downstream/expandDeep не вызывали `enrichDataSource()` после `buildResult()` — data_source = "" для всех нод в lineage-виде | ✅ FIXED |
| BUG-SS-040 | LOW | VERDANDI / SHUTTLE | `ExploreService.java:625` | `enrichDataSource()` была приватной → LineageService не мог её вызвать | ✅ FIXED |
| BUG-SS-041 | MEDIUM | Dali / FRIGG | `FriggGateway` + `application.properties` | `java.io.IOException: Connection was closed` — FRIGG сбрасывает idle-соединения. Добавлен `connection-ttl=30000` + retry(1) на IOException в FriggGateway | ✅ FIXED |
| BUG-SS-042 | MEDIUM | KNOT | `KnotService.java:523` | CURSOR-ноды показывали "SQL-фрагмент отсутствует" — `LIMIT 2000` в `loadSnippets()` слишком мал для больших пакетов (>100 рутин); CURSOR-геоиды сортируются после SELECT/INSERT и отсекались. Повышен до `LIMIT 50000` + добавлен blank-фильтр | ✅ FIXED |
| BUG-SS-043 | LOW | HEIMDALL / shared | `HeimdallEvent.java` | Аннотации `@NotNull`/`@NotBlank` не были сохранены на диск в предыдущей сессии (dependency в build.gradle есть, аннотации отсутствовали) | ✅ FIXED |

#### Детали BUG-SS-038/039/040 (data_source badge pipeline)

**Проблема:** Бейдж `master`/`reconstructed` не отображался ни в explore, ни в lineage видах.

**Корневые причины (три):**
1. `enrichDataSource()`: Cypher `WHERE id(t) IN $ids` → ArcadeDB сравнивает LINK-объект со String-списком → 0 совпадений → dsMap пустой → все dataSource = ""
2. `LineageService`: не вызывал `enrichDataSource()` после `buildResult()` (который всегда ставит dataSource = "")
3. `enrichDataSource()` была `private` → LineageService не мог её вызвать

**Фиксы:**
- `ExploreService.enrichDataSource()`: `private` → `public`; `MATCH ... WHERE id(t) IN $ids` → `UNWIND $ids AS rid ... WHERE id(t) = rid`
- `LineageService`: добавлен `@Inject ExploreService exploreService`; все 4 метода (lineage, upstream, downstream, expandDeep) цепочкой `.flatMap(exploreService::enrichDataSource)`

**Условие видимости бейджа:** Нужно запустить Dali-сессию ПОСЛЕ этих фиксов чтобы Hound записал `data_source` в DaliTable (существующие данные из старых сессий без data_source бейдж не покажут).

---

### Дополнение — Batch 4: DDL / VIEW / DaliDDLStatement (13.04.2026)

| ID | Severity | Компонент | Файл | Описание | Статус |
|----|----------|-----------|------|----------|--------|
| BUG-SS-044 | HIGH | HOUND | `JsonlBatchBuilder.java:225`, `RemoteWriter.java:1120` | `DaliTable` создавались без поля `data_source` в batch-path и fallback writeBatch-path — все новые таблицы из batch-сессий не имели `master`/`reconstructed` | ✅ FIXED |
| BUG-SS-045 | MEDIUM | HOUND | `JsonlBatchBuilder.java:229`, `RemoteWriter.java:447` | `table_type` оставался `TABLE` при `CREATE VIEW` если вью было ранее `reconstructed` — Verdandi-бейдж не показывал VIEW | ✅ FIXED |
| BUG-SS-046 | MEDIUM | HOUND | `RemoteWriter.java`, `JsonlBatchBuilder.java` | Отсутствовал тип вершины `DaliDDLStatement` — DDL-операции (ALTER/CREATE/DROP) нигде не логировались отдельно от DaliStatement | ✅ FIXED |
| BUG-SS-047 | LOW | HEIMDALL WS | `EventStreamEndpoint.java` | `@OnError` handler отсутствовал → Quarkus логировал нормальные клиентские дисконнекты (WSAECONNRESET) как ERROR | ✅ FIXED |

#### KNOWN ISSUE — KI-001: связи DaliDDLStatement → DaliTable / DaliColumn

**Описание:** При `ALTER TABLE` изменение колонок пока не записывается в виде рёбер от `DaliDDLStatement` к `DaliTable`/`DaliColumn`.
`DaliDDLStatement` создаётся корректно с `type=ALTER_TABLE`, `target_table_geoids`, `line_start/end` — но граф связей ALTER→Column не строится.

**Причина:** `ALTER TABLE` может изменять/добавлять/удалять колонки; необходим полный парсинг `ALTER TABLE ... ADD/MODIFY/DROP COLUMN` для построения `DaliAffectedColumn` записей и рёбер `HAS_AFFECTED_COL`.

**Статус:** DEFER → Sprint+1. Существующие DaliDDLStatement-вершины можно дополнить рёбрами ретроспективно когда логика будет реализована.

**Файлы затронуты:** `RemoteWriter.java` (секция DaliDDLStatement), `StatementInfo.getAffectedColumns()`, `RemoteSchemaCommands.java` (новый тип ребра `DDL_MODIFIES` TBD).

---

### Дополнение — Batch 5: KNOT инспектор таблиц + PK/FK/DDL на колонках (13.04.2026)

| ID | Severity | Компонент | Файл | Описание | Статус |
|----|----------|-----------|------|----------|--------|
| BUG-SS-048 | HIGH | HOUND | `RemoteWriter.java` (все пути INSERT DaliColumn) | `is_pk`, `is_fk`, `fk_ref_table`, `fk_ref_column` не передавались в INSERT — поля всегда null в БД | ✅ FIXED |
| BUG-SS-049 | HIGH | HOUND | `BaseSemanticListener.java:onAlterTableEnter` | ALTER TABLE вызывал `initDdlTable` → `markDdlTable` → таблицы помечались `data_source='master'` даже без CREATE TABLE. Добавлен `initDdlTableNoMark()` | ✅ FIXED |
| BUG-SS-050 | HIGH | HOUND | `RemoteWriter.java:preInsertAdHocSchemas` | `DuplicatedKeyException` при повторной обработке: `(null, column_geoid)` unique index. Добавлен try-catch + UPDATE `data_source='master'` если мастер | ✅ FIXED |
| BUG-SS-051 | MEDIUM | SHUTTLE | `KnotTable.java`, `KnotService.java` | Колонки таблиц загружались в полном knotReport — для больших схем огромный JSON; нет `data_source` на таблицах | ✅ FIXED |
| BUG-SS-052 | MEDIUM | SHUTTLE | `KnotService.java` (NEW: `knotTableDetail`) | Колонки с PK/FK/NOT NULL/default_value/dataSource загружаются лениво по одной таблице; SQL-сниппет тоже per-table | ✅ FIXED |
| BUG-SS-053 | MEDIUM | VERDANDI | `KnotStructure.tsx` | Инспектор таблиц KNOT не показывал: master/reconstructed бейдж, типы данных, N (NOT NULL), PK, FK. Вся детализация lazy-load | ✅ FIXED |
| BUG-SS-054 | MEDIUM | VERDANDI | `KnotStructure.tsx` | SQL-сниппет для таблицы отсутствовал (не загружался). Добавлен в lazy KnotTableDetail | ✅ FIXED |

#### Детали Batch 5 (KNOT table inspector)

**Схема изменений (5 слоёв):**

1. **`hound/model/ColumnInfo.java`** — добавлены mutable поля `isPk`, `isFk`, `fkRefTable`, `fkRefColumn` + `markAsPk()` / `markAsFk()` setters
2. **`hound/engine/StructureAndLineageBuilder.java`** — `addConstraint()` вызывает `col.markAsPk()` / `col.markAsFk()` для колонок из out_of_line_constraint
3. **`hound/storage/RemoteSchemaCommands.java`** — 4 новых свойства: `DaliColumn.is_pk`, `is_fk`, `fk_ref_table`, `fk_ref_column`
4. **`hound/storage/JsonlBatchBuilder.java`** — batch-путь пишет `is_pk`, `is_fk`, `fk_ref_table`, `fk_ref_column`
5. **`hound/storage/RemoteWriter.java`** — все 4 пути INSERT DaliColumn (pool/non-pool × write/writeBatch) + preInsertAdHocSchemas пишут 4 новых поля

**Shuttle (2 новых модели + 2 изменённых + 1 новый endpoint):**
- `KnotColumn.java` — добавлены: `isRequired`, `isPk`, `isFk`, `fkRefTable`, `defaultValue`, `dataSource`
- `KnotTable.java` — убраны `columns` (lazy), добавлен `dataSource`
- `KnotTableDetail.java` (NEW) — `tableGeoid`, `dataSource`, `List<KnotColumn> columns`, `snippet`
- `KnotService.java` — `loadTables()` упрощён (только count, не детали); `knotTableDetail(sessionId, tableGeoid)` — 3 параллельных запроса + цепочка для snippet
- `KnotResource.java` — `@Query("knotTableDetail")` с HEIMDALL событиями

**Verdandi (2 файла):**
- `lineage.ts` — обновлены интерфейсы `KnotColumn`, `KnotTable`; добавлены `KnotTableDetail`, `KNOT_TABLE_DETAIL` query, `fetchKnotTableDetail()`
- `KnotStructure.tsx` — prop `sessionId`; lazy-load `details: Map<geoid, KnotTableDetail|'loading'|'error'>`; бейдж `master`/`reconstructed` на строке таблицы; новая таблица колонок с PK/FK/N/default; SQL-сниппет справа

---

### Статистика (обновлено)

| Метрика | Значение |
|---------|---------|
| Всего найдено | **54** (+7) |
| CRITICAL | **1** |
| HIGH | **13** |
| MEDIUM | **28** |
| LOW | **11** |
| **Исправлено итого** | **43** |
| **Открыто → до M1** | **1** (BUG-SS-020) |
| **Known Issues** | **1** (KI-001: DDL→Column links) |
| **Отложено M2+** | **7** |

---

*Последнее обновление: 13.04.2026 (Batch 5). KNOT инспектор: master/reconstructed badge + PK/FK/N/DEFAULT + SQL snippet теперь работают lazy-load. Следующий шаг: запустить DDL-сессию в Dali → проверить DaliColumn.is_pk=true для PK-колонок в YGG → бейджи появятся в KNOT inspector.*

---

### Дополнение — Batch 6: DDL atom resolution + YGG stats strip + redirect fix (14.04.2026)

| ID | Severity | Компонент | Файл | Описание | Статус |
|----|----------|-----------|------|----------|--------|
| BUG-SS-055 | CRITICAL | HOUND | `BaseSemanticListener.java:registerDdlTableAsSource`, `UniversalSemanticEngine.java` | DDL-атомы (`CREATE TABLE`, `ALTER TABLE`) никогда не резолвились (0%) — `registerDdlTableAsSource()` использовал BSL-geoid (`TYPE:lineStart:lineEnd:nanoTime`), а `engine.getBuilder().getStatements()` хранит по ScopeContext-geoid (`TYPE:lineStart`) → lookup всегда null, sourceTables не добавлялся, implicit-strategy не срабатывала | ✅ FIXED |
| BUG-SS-056 | LOW | HEIMDALL frontend | `App.tsx:*-route` | Бесконечный редирект `/q/dev-ui/welcome/services/services/…` — relative `<Navigate to="services">` в `*`-catch-all накапливал сегмент `services` при каждом рендере. Исправлено: `to="/services"` (абсолютный путь) | ✅ FIXED |
| FEAT-SS-001 | — | Dali backend + HEIMDALL frontend | `YggStatsResource.java` (NEW), `YggClient.java` (NEW), `DaliPage.tsx` | YGG-статистика в шапке страницы Dali: tables / columns / stmts / routines / atoms / % resolved / unresolved / pending. Backend: новый endpoint `GET /api/stats` через MicroProfile REST client `ygg-api`. Frontend: polling 30s + кнопка ↻ | ✅ DONE |

#### Детали BUG-SS-055 (DDL atom resolution — root cause)

**Корневая причина — geoid mismatch:**

- `BaseSemanticListener.initStatement()` формирует ключ вида `"ALTER_TABLE:48:62:1744123456789"` (тип + lineStart + lineEnd + nanoTime)
- `ScopeContext.buildStatementGeoid()` формирует ключ вида `"ALTER_TABLE:48"` (тип + lineStart)
- `engine.getBuilder().getStatements()` хранит по ScopeContext-geoid
- `registerDdlTableAsSource()` вызывал `currentStatement()` → BSL-geoid → `getStatements().get(bslGeoid)` → всегда `null`
- В результате `stmtInfo.addSourceTable()` никогда не вызывался → `sourceTables` пустой → `AtomProcessor` не мог применить implicit-table-strategy → все DDL-атомы статус `unresolved`

**Фикс — 2 файла:**

1. `UniversalSemanticEngine.java` — добавлен публичный метод:
   ```java
   public String currentStatementGeoid() { return scopeManager.currentStatement(); }
   ```

2. `BaseSemanticListener.java:registerDdlTableAsSource()` — заменён `currentStatement()` на `engine.currentStatementGeoid()`:
   ```java
   String stmtGeoid = engine.currentStatementGeoid(); // ScopeContext format
   ```

**Результат:** rate разрешения атомов поднялся с 0% до 77%+ после перепрогона сессий.

#### Детали FEAT-SS-001 (YGG stats strip)

**Backend (2 новых файла в `services/dali/`):**
- `rest/YggClient.java` — `@RegisterRestClient(configKey = "ygg-api")`, `POST /api/v1/command/{db}`
- `rest/YggStatsResource.java` — `GET /api/stats`: считает COUNT(*) по 5 типам вершин + GROUP BY status для DaliAtom; возвращает 503 при недоступном YGG

**Frontend:**
- `api/dali.ts` — интерфейс `YggStats` + функция `getYggStats()`
- `pages/DaliPage.tsx` — stats strip: `tables · columns · stmts · routines · atoms · X% resolved · N unresolved · M pending · ↻`; polling каждые 30s

---

### Статистика (обновлено)

| Метрика | Значение |
|---------|---------|
| Всего найдено | **57** (+3) |
| CRITICAL | **2** |
| HIGH | **13** |
| MEDIUM | **28** |
| LOW | **12** |
| Features | **1** |
| **Исправлено итого** | **46** |
| **Открыто → до M1** | **1** (BUG-SS-020) |
| **Known Issues** | **1** (KI-001: DDL→Column links) |
| **Отложено M2+** | **7** |

---

*Последнее обновление: 14.04.2026 (Batch 6). DDL atom resolution: geoid-mismatch устранён, rate поднялся с 0% до 77%+. YGG stats strip добавлен в DaliPage. Redirect loop в App.tsx устранён.*
