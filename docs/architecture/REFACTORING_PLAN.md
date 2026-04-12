# AIDA — Refactoring Plan (перед началом Dali engineering)

**Документ:** `REFACTORING_PLAN`
**Версия:** 1.0
**Дата:** 11.04.2026
**Статус:** Working document — партнёр `MODULES_TECH_STACK.md` и `INTEGRATIONS_MATRIX.md`

Этот документ описывает **что нужно изменить в существующей работающей кодовой базе** прежде чем добавлять Dali Core и HEIMDALL. Refactoring идёт параллельно с новой разработкой, но часть задач **блокирует** Dali engineering.

---

## 0. Summary

| Компонент | Effort | Приоритет | Блокирует |
|---|---|---|---|
| **C.0 ArcadeDB — Hound тесты → network mode** | ~1-3 дня | 🔴 критический | Dali engineering |
| **C.1 Hound refactor** | ~6-7 дней | 🔴 высокий | Dali engineering |
| **C.2 SHUTTLE mutations + subscriptions + clients** | ~7-8 дней | 🔴 высокий | VERDANDI features |
| **C.2.1 SHUTTLE SQL injection fix** | ~2 часа | 🔴 критический | Prod stability |
| **C.3 Chur new routes + scopes + WS** | ~4 дня | 🟡 средний | HEIMDALL integration |
| **C.4.0 VERDANDI ELK Web Worker** | ~0.5 дня | 🟡 средний | LOOM 5K nodes |
| **C.4 VERDANDI new views (ANVIL, MIMIR, WS)** | ~10-12 дней | 🟡 средний | Demo functionality |
| **C.5 Infrastructure (Docker, CI, Keycloak)** | ~3 дня | 🟡 средний | Deployment |
| **Total refactoring** | **~27.5-33.5 дня** (пересмотрен: C.1 +2-3 дня за bug fixes, C.4.0 +0.5) | — | — |

Плюс параллельно **новая разработка**:
- Dali Core (Quarkus + JobRunr) ~4-5 недель
- HEIMDALL backend ~3-4 недели
- HEIMDALL frontend ~3-4 недели
- MIMIR backend ~2-3 недели
- ANVIL backend ~1-2 недели

**Grand total:** ~12-16 недель одного разработчика, или ~6-8 недель параллельно двумя co-founder. Укладывается в апрель-август с polish в сентябре.

---

## C.0 ArcadeDB — завершение upgrade для Hound тестов

**Приоритет:** 🔴 критический, **делается первым**, блокирует Dali engineering
**Effort:** ~1-3 дня (пересмотрен — см. контекст ниже)
**Блокирует:** Dali engineering, FRIGG custom adapter

### Контекст (зафиксировано 12.04.2026)

Ситуация значительно проще, чем изначально оценивалась:

| Компонент | Версия ArcadeDB | Режим | Статус |
|---|---|---|---|
| **HoundArcade** (network mode) | **26.3.2** | Remote / network | ✅ уже на latest |
| **SHUTTLE** (Quarkus REST client) | **26.3.2** | через HoundArcade | ✅ уже работает |
| **Hound** (embedded в тестах) | **25.12.1** | Embedded — только тесты | ⚠️ legacy, нужно мигрировать |

**Production-путь** (Hound → YGG через network mode) уже на 26.x. Embedded используется **только в тестах Hound** — не в production. Новые фичи 26.x (Bolt protocol, gRPC batch / GraphBatchLoad, GraphQL introspection) нужны.

**Выбранное решение: Вариант 1 — тесты Hound → network mode (26.x)**

Вариант выбран потому что:
- ANTLR conflict (embedded 25.x + remote 26.x) исчезает полностью
- Все тесты сразу получают Bolt, gRPC batch, GraphQL introspection
- Ноль изменений в production коде Hound
- Минимальный техдолг

**Не выбрано:**
- ~~Вариант 2 (ANTLR shading для embedded 26.x)~~ — возможно, но добавляет техдолг
- ~~Вариант 3 (mixed 25 embedded + 26 remote)~~ — риск after первого открытия БД 26.x сервером (auto-upgrade схемы, откат на 25.x может сломаться)

**Политика версий (зафиксировано):**
> No mixed embedded/network ArcadeDB versions на одной БД. Если используется HoundArcade 26.x — все тесты идут через network 26.x.

---

### C.0.1 Перевод Hound тестов на network mode (~1 день)

**Сейчас:** тесты используют `new Database(embeddedPath)` c `arcadedb-engine:25.12.1`.
**Нужно:** переключить на `new RemoteDatabase(...)` против HoundArcade 26.3.2.

Gradle — убрать embedded зависимость из test scope:

```gradle
dependencies {
    // Убрать:
    // testImplementation "com.arcadedb:arcadedb-engine:25.12.1"

    // Добавить (уже есть в production scope или добавить в test):
    testImplementation "com.arcadedb:arcadedb-network:26.3.2"
}
```

Тест-конфиг — заменить embedded подключение:

```java
// Было:
Database db = new DatabaseFactory(tempDir.toString()).create();

// Стало (network mode против локального HoundArcade):
RemoteDatabase db = new RemoteDatabase(
    "localhost", 2480, "testdb",
    "root", testPassword
);
```

Тест-инфраструктура — поднять HoundArcade в тестах:

```java
@BeforeAll
static void startArcade() {
    // либо через Docker (testcontainers), либо через embedded ArcadeDBServer
    // на localhost:2480 с тестовой базой
}
```

### C.0.2 Обновить `arcadedb-engine` в Hound build.gradle (~полчаса)

Если `arcadedb-engine` остаётся как production зависимость (для embedded mode в UC3/UC4) — bump до 26.3.2:

```gradle
// production scope — для EMBEDDED writeMode (UC3/UC4/offline)
implementation "com.arcadedb:arcadedb-engine:26.3.2"
// network client
implementation "com.arcadedb:arcadedb-network:26.3.2"
```

Если embedded в production не нужен совсем — убрать `arcadedb-engine` из production зависимостей.

### C.0.3 Regression тесты Cypher queries после перехода (~0.5-1 день)

- **Риск:** Cypher engine в 26.x native OpenCypher vs 25.x Gremlin-translation — subtle behavioral differences
- Run SHUTTLE `LineageService` / `ExploreService` / `SearchService` queries на реальных данных
- Compare results, fix edge cases
- SHUTTLE уже работает против 26.3.2 — скорее всего regression минимальный

### C.0.4 Verify новые фичи 26.x доступны (~полдня)

После перехода убедиться что цель достигнута:
- Bolt protocol (`bolt://localhost:7687`) — проверить коннект
- gRPC batch / GraphBatchLoad — проверить что API доступен
- GraphQL introspection — проверить эндпоинт

### C.0.5 Совмещается с C.2.1 (SQL injection)

Regression SHUTTLE SQL queries проверяется одновременно с SQL injection fix. Делать вместе.

---

## C.1 Hound — подготовка к использованию как библиотеки

**Effort пересмотрен:** ~4 дня → **~6-7 дней** с учётом code review (11.04.2026).
**Источник:** `HOUND_CODE_REVIEW.md` — полный разбор с приоритетами.

Порядок выполнения строгий: C.1.0 (bugs) → C.1.1-C.1.5 (API) → C.1.6-C.1.7 (perf + debt).

---

### C.1.0 Bug fixes — исправить до любых других изменений (~1 день)

Три критических NPE и три высоких бага. **Запускать тесты после каждого фикса.**

**B1/B2 — NPE в ensureTable (`StructureAndLineageBuilder.java:44, :88`):**

```java
// Добавить в начало ensureTable() и ensureTableWithType():
if (tableName == null || tableName.isBlank()) {
    logger.warn("ensureTable called with null/blank tableName, skipping");
    return "UNKNOWN";
}
String upperName = tableName.toUpperCase();   // строка 44/88
```

**B3 — NPE в ArcadeDBSemanticWriter (`строка 122`):**

```java
// Было:
int cntLineage = result.getLineage().size();

// Стало:
int cntLineage = result.getLineage() != null ? result.getLineage().size() : 0;
```

**B4 — Бесконечная рекурсия в `resolveImplicitTableInternal` (`NameResolver.java:441-483`):**

```java
// Добавить depth guard:
private static final int MAX_RECURSION_DEPTH = 50;

private String resolveImplicitTableInternal(StatementInfo stmt, int depth) {
    if (depth > MAX_RECURSION_DEPTH) {
        logger.warn("resolveImplicitTable: max depth exceeded, possible cycle in StatementInfo");
        return null;
    }
    // ... рекурсивный вызов: resolveImplicitTableInternal(parent, depth + 1)
}
```

**B5 — Quality > 1.0 (`StructureAndLineageBuilder.java:490-497`):**

```java
// Было (двойной подсчёт):
if (atom.isResolved()) score += resolvedWeight;
if (atom.isConstant()) score += constantWeight;   // может суммироваться сверх total

// Стало:
if (atom.isResolved()) {
    score += resolvedWeight;
} else if (atom.isConstant() || atom.isFunction()) {
    score += constantWeight;
}
// ...
return Math.min(1.0, score / total);
```

**B6 — Thread naming (`ThreadPoolManager.java:23`):**

```java
// Было: new AtomicInteger(0) создаётся per-поток → всегда "hound-worker-1"
// Стало:
private static final AtomicInteger THREAD_SEQ = new AtomicInteger(0);
// в лямбде:
t.setName("hound-worker-" + THREAD_SEQ.incrementAndGet());
```

**B7 — InterruptedException без break (`HoundApplication.java:348`):**

```java
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
    break;   // ← добавить: не продолжать цикл по futures
}
```

---

### C.1.1 Publish-ready API (~1 день)

**Сейчас:** Hound запускается через `./gradlew run`, main class делает весь workflow.
**Нужно:** expose public API для программного использования Dali:

```java
public interface HoundParser {
    ParseResult parse(Path file, HoundConfig config);
    ParseResult parse(Path file, HoundConfig config, HoundEventListener listener);
    List<ParseResult> parseBatch(List<Path> files, HoundConfig config);
    List<ParseResult> parseBatch(List<Path> files, HoundConfig config,
                                  HoundEventListener listener);
}
```

Refactor `HoundApplication.main()` → `HoundParserImpl implements HoundParser`. main() вызывает impl.

---

### C.1.2 HoundConfig как typed POJO (~полдня)

**Сейчас:** configuration размазана по properties files, CLI args, env vars.
**Нужно:** single `HoundConfig` record — переезжает в `shared/dali-models/` (shared между Hound и Dali):

```java
public record HoundConfig(
    String dialect,              // "plsql", "postgresql", etc.
    String targetSchema,         // куда писать в YGG
    ArcadeWriteMode writeMode,   // EMBEDDED | REMOTE | REMOTE_BATCH
    String arcadeUrl,            // для REMOTE/REMOTE_BATCH
    int workerThreads,
    boolean strictResolution,
    int batchSize,               // для REMOTE_BATCH, default 5000
    Map<String, String> extra    // extensibility
) {}

public enum ArcadeWriteMode {
    EMBEDDED,      // UC3/UC4/preview/dev
    REMOTE,        // UC2b event-driven
    REMOTE_BATCH   // UC1/UC2a default (13.1× speedup)
}
```

Открытый вопрос Q27 — полная схема уточняется при реализации.

---

### C.1.3 HoundEventListener interface (~1 день)

**Нужно:** public interface для подписки Dali и HEIMDALL:

```java
public interface HoundEventListener {
    void onFileParseStarted(String file);
    void onAtomExtracted(String file, int atomCount);
    void onFileParseCompleted(String file, ParseResult result);
    void onError(String file, Throwable error);
}
```

Реализация в `FileProcessor` — заменить прямые лог-вызовы на `listener.on*()` (listener может быть `null` — для standalone mode).

---

### C.1.4 Gradle java-library plugin (~полдня)

```groovy
plugins {
    id 'java-library'   // ← добавить (уже сделано в REPO_MIGRATION_PLAN Phase 2)
    id 'application'    // ← оставить для standalone run
}
```

Проверка: `./gradlew :hound:build` → `implementation project(':hound')` в Dali компилируется.

---

### C.1.5 REMOTE_BATCH как внутренняя деталь (~1 день)

Dali передаёт `writeMode=REMOTE_BATCH`, Hound сам решает размер батча и таймауты. Public API принимает только `ArcadeWriteMode`, детали батчинга — hidden.

---

### C.1.6 Performance fixes (~1 день)

Два критических для больших кодовых баз (500K LoC demo):

**P1 — O(A×T) → O(A) в NameResolver (resolveTableByNameOnly + resolveTableByAliasInScope):**

```java
// В StructureAndLineageBuilder — добавить индексы:
private final Map<String, List<String>> tablesByName  = new HashMap<>();
private final Map<String, String>       aliasByGeoid  = new HashMap<>();

// Обновлять в ensureTable():
tablesByName.computeIfAbsent(upperName, k -> new ArrayList<>()).add(geoid);

// NameResolver.resolveTableByNameOnly — использовать индекс вместо сканирования:
List<String> candidates = structure.getTablesByName(upperName);
// O(1) lookup вместо O(T) перебора
```

**P2 — O(N²) → O(N) в StatementInfo.addChildStatement:**

```java
// Было:
private List<String> childStatements = new ArrayList<>();
if (!childStatements.contains(childGeoid)) { ... }  // O(n)

// Стало:
private LinkedHashSet<String> childStatements = new LinkedHashSet<>();
// contains() → O(1), порядок сохраняется
```

**P3 — кэшировать depth в StatementInfo:**

```java
// В addStatement() вычислять и сохранять depth:
int depth = parent != null ? parent.getDepth() + 1 : 0;
statement.setDepth(depth);

// serializeStatements() использует cached value — убрать O(N×D) пересчёт
```

---

### C.1.7 Technical debt — по возможности параллельно (~1 день)

**TD1 — Дедупликация computeStatementQuality / computeDepth / hasCte:**

Канонические реализации в `WriteHelpers`. Удалить копии из `StructureAndLineageBuilder` и `JsonlBatchBuilder`, заменить на делегирование:

```java
// StructureAndLineageBuilder — было своя реализация, стало:
double quality = WriteHelpers.computeStatementQuality(atoms);
int    depth   = WriteHelpers.computeDepth(stmt, allStatements);
```

**TD2 — Дублирование resolveFlowType в DataFlowProcessor vs WriteHelpers:**

Удалить из `DataFlowProcessor`, делегировать в `WriteHelpers.resolveFlowType()`.

**TD3 — Интерфейс ISemanticEngine (низкий приоритет):**

```java
public interface ISemanticEngine {
    void processToken(Token token, CanonicalTokenType type);
    SemanticResult getResult();
}
```

`DialectAdapter` и `BaseSemanticListener` зависят от `ISemanticEngine`, не от `UniversalSemanticEngine`. Разблокирует тестирование с mock.

**TD4 — Цикл UniversalParser ↔ ParserRegistry (низкий приоритет):**

Ввести `IParserFactory`. `ParserRegistry` зависит только от интерфейса. `UniversalParser` получает фабрику через DI.

---

## C.2 SHUTTLE — mutations, subscriptions, REST clients

### C.2.1 SQL injection fix (🔴 BLOCKING, ~2 часа)
Review от 05.04 показал SQL injection в `SearchService.java:86-87` через `String.format()`. **Исправить до любых других изменений в SHUTTLE.**

```java
// Было (vulnerable):
arcade.sql(String.format(template, like, n));

// Должно быть (parameterized):
arcade.sql(template, Map.of("like", like, "n", n));
```

### C.2.2 Mutation resolvers (~3 дня)
**Сейчас:** только Query в schema.
**Нужно:** добавить Mutation endpoints:

```graphql
type Mutation {
  startParseSession(input: ParseSessionInput!): Session!
  askMimir(question: String!, sessionId: ID!): MimirAnswer!
  saveView(input: SaveViewInput!): SavedView!
  deleteView(id: ID!): Boolean!
  # HEIMDALL control mutations (через scope check):
  resetDemoState: Boolean!
  cancelSession(sessionId: ID!): Boolean!
}
```

Каждая mutation делегирует в соответствующий backend сервис (Dali, MIMIR, HEIMDALL) через REST client.

### C.2.3 Subscription resolvers через SmallRye GraphQL (~2-3 дня)
**Сейчас:** нет Subscriptions.
**Нужно:** добавить subscriptions:

```graphql
type Subscription {
  heimdallEvents(filter: EventFilter): HeimdallEvent!
  sessionProgress(sessionId: ID!): SessionProgress!
}
```

Implementation: SmallRye GraphQL Subscriptions поверх Vert.x WebSocket. Или отдельный native WebSocket endpoint если SmallRye окажется сложным.

### C.2.4 REST client to Dali Core (~1 день)
**Сейчас:** SHUTTLE не знает о Dali.
**Нужно:** Quarkus REST client interface:

```java
@RegisterRestClient(configKey = "dali-api")
public interface DaliClient {
    @POST @Path("/sessions")
    Uni<Session> startParseSession(ParseSessionInput input);

    @POST @Path("/control/reset")
    Uni<Void> resetState();
}
```

Использует shared Gradle module `dali-models` для типов.

### C.2.5 REST client to MIMIR backend (~полдня)
Аналогичный REST client interface для MIMIR:

```java
@RegisterRestClient(configKey = "mimir-api")
public interface MimirClient {
    @POST @Path("/ask")
    Uni<MimirAnswer> ask(MimirRequest request);
}
```

### C.2.6 REST client to HEIMDALL backend (~полдня)
REST client для admin commands и event subscription:

```java
@RegisterRestClient(configKey = "heimdall-api")
public interface HeimdallClient {
    @POST @Path("/control/reset")
    Uni<Void> resetDemoState();

    @POST @Path("/control/cancel/{sessionId}")
    Uni<Void> cancelSession(@PathParam String sessionId);
}
```

---

## C.3 Chur — новые routes для HEIMDALL и scope checks

### C.3.1 Proxy для HEIMDALL backend (~1-2 дня)
**Сейчас:** Chur проксирует только `/graphql` в SHUTTLE.
**Нужно:** новая route `/heimdall/*` с admin scope checks:

```typescript
app.post('/heimdall/graphql', {
    preHandler: requireScope('aida:admin')
}, async (req, reply) => {
    // proxy to HEIMDALL backend с trusted headers
});

app.post('/heimdall/control/*', {
    preHandler: requireScope('aida:admin:destructive')
}, async (req, reply) => {
    // proxy destructive control commands
});
```

### C.3.2 Scope checks (~полдня)
**Сейчас:** Chur проверяет JWT, но не scopes.
**Нужно:** middleware для scope validation:

```typescript
function requireScope(scope: string): FastifyPluginCallback {
    return async (req, reply) => {
        const session = getSession(req.cookies.sid);
        if (!session.scopes?.includes(scope)) {
            reply.code(403).send({ error: 'Missing scope: ' + scope });
        }
    };
}
```

### C.3.3 Переименование `verdandi-bff` → `aida-bff` (~1 день)
**Сейчас:** Chur использует Keycloak client `verdandi-bff`.
**Нужно:**
1. Создать новый client `aida-bff` в Keycloak realm `seer` с теми же настройками + scopes `aida:admin`, `aida:admin:destructive`, `seer:read`, `seer:write`
2. Обновить Chur config (`KEYCLOAK_CLIENT_ID=aida-bff`)
3. Обновить protocol mapper для scope claims в JWT
4. Coordinated deploy VERDANDI и (будущего) HEIMDALL frontend
5. Удалить старый `verdandi-bff` после подтверждения

### C.3.4 WebSocket upgrade handler (~1 день)
**Сейчас:** только HTTP.
**Нужно:** поддержка WebSocket upgrade для `/graphql` (GraphQL Subscriptions через `@fastify/websocket`).

---

## C.4 VERDANDI — новые views

### C.4.0 ELK Web Worker activation (~0.5 дня) ✅ DONE
**Сейчас:** ELK запускается на main thread → freeze UI 2-5 сек при 500-1000 нодах. Для HighLoad demo нужно 5K нодов.
**Что сделано:**
- `elkWorker.ts` (уже существовал) содержит `(self as any).Worker = undefined` — фикс Vite CJS→ESM трансформа
- `vite.config.ts` уже настроен: `worker: { format: 'es' }` + `optimizeDeps.include: ['elkjs/lib/elk.bundled.js']`
- `layoutGraph.ts` — заменён main-thread singleton на worker singleton + `_pending` Map + `cancelPendingLayouts()` имплементирован

**Верификация:** console должен показывать `[LOOM] ELK layout (worker) — Xms` (не "main-thread").
**Load test** 500+ нодов — после C.1 Hound + C.0 ArcadeDB → Dali.
**Reference:** `docs/sprints/SPIKE_ELK_WORKER.md`, branch `feat/elk-web-worker`

---

### C.4.1 WebSocket client для HEIMDALL events (~1 день)
**Сейчас:** только graphql-request (HTTP).
**Нужно:** добавить WebSocket client:

```typescript
import { createClient } from 'graphql-ws';
const wsClient = createClient({
    url: 'ws://localhost:3000/graphql',
});
```

### C.4.2 ANVIL UI view (~3-4 дня)
**Сейчас:** только LOOM/KNOT.
**Нужно:** новый React компонент для impact analysis результатов:
- Route `/anvil/:nodeId`
- Query к SHUTTLE: `findImpact(nodeId)`
- Visualization: таблица affected nodes + interactive highlight в LOOM
- Integration button из LOOM context menu: "Find Impact"

### C.4.3 MIMIR Chat view (~4-5 дней)
**Сейчас:** нет.
**Нужно:** chat UI компонент:
- Text input + conversation history
- Send question → `askMimir` mutation
- Display response + `highlight_ids` → отправить в LOOM как selection
- Tool trace visualization (showing which tools were called)

### C.4.4 Subscription handler для `heimdallEvents` (опционально, ~2 дня)
Нужно если VERDANDI должен показывать live events (notification toasts, progress bars для parsing jobs).

---

## C.5 Infrastructure — Docker, CI/CD, Keycloak

### C.5.1 Docker Compose дополнения (~1 день)
Новые сервисы для добавления:

```yaml
services:
  dali:
    image: ghcr.io/.../dali:latest
    ports: ["9090:9090"]
  mimir:
    image: ghcr.io/.../mimir:latest
    ports: ["9091:9091"]
  anvil:
    image: ghcr.io/.../anvil:latest
    ports: ["9092:9092"]
  heimdall-backend:
    image: ghcr.io/.../heimdall-backend:latest
    ports: ["9093:9093"]
  heimdall-frontend:
    image: ghcr.io/.../heimdall-frontend:latest
    ports: ["14000:14000"]
  frigg:
    image: arcadedata/arcadedb:latest
    ports: ["2481:2480"]   # отдельный instance для FRIGG
    volumes: ["frigg_data:/home/arcadedb/databases"]
```

Health checks + depends_on + volumes для persistence.

### C.5.2 Keycloak realm export с новым client (~полдня)
Обновить `keycloak/seer-realm.json`:
- Переименовать `verdandi-bff` → `aida-bff`
- Добавить scopes: `aida:admin`, `aida:admin:destructive`, `seer:read`, `seer:write`
- Protocol mapper для scope claims в JWT
- Role mappings для admin/editor/viewer users

### C.5.3 CI pipeline для новых сервисов (~1 день)
Добавить в `ci.yml`:
- Build + test job для Dali
- Build + test job для MIMIR backend
- Build + test job для ANVIL backend
- Build + test job для HEIMDALL backend + frontend

### C.5.4 Network rename `verdandi_net` → `aida_net` (~1 час)
Cosmetic но consistent с aida-bff renaming. Обновить все `docker-compose.yml` и `docker-compose.prod.yml`.

---

## Критический путь — порядок операций

**Week 1-2 (prerequisite для всего):**
1. **C.0 ArcadeDB upgrade** (~5-8 дней, blocks everything)
2. **C.2.1 SHUTTLE SQL injection fix** (~2 часа, critical prod — делать немедленно)

**Week 2-3:**
3. **C.1 Hound library refactor** (~4 дня, blocks Dali)
4. **C.5.2 Keycloak client rename** (~полдня, лучше рано)

**Week 3-4:**
5. **Dali Core skeleton** (новая разработка, not refactoring) — Quarkus project, JobRunr setup, custom FRIGG StorageProvider
6. **HEIMDALL backend skeleton** — Quarkus project, event collector, ring buffer
7. **C.2.2-C.2.3 SHUTTLE mutations + subscriptions** (параллельно с Dali)

**Week 5-8:**
8. **Dali полная функциональность** — UC1-UC4 implementation, integration с Hound через in-JVM calls
9. **HEIMDALL backend полная функциональность** — metrics aggregation, WebSocket streaming, control commands
10. **HEIMDALL frontend skeleton** (новый React repo)
11. **MIMIR backend** (параллельно)

**Week 9-12:**
12. **C.4.2 VERDANDI ANVIL UI view**
13. **C.4.3 VERDANDI MIMIR Chat view**
14. **HEIMDALL frontend полная функциональность**
15. **ANVIL backend**
16. **C.2.4-C.2.6 SHUTTLE REST clients** к Dali/MIMIR/HEIMDALL
17. **C.3 Chur HEIMDALL routes**
18. **Integration testing end-to-end**

**Week 13-16:**
19. **Demo rehearsals**
20. **Polish, bug fixes**
21. **Performance tuning**

---

## Открытые вопросы по refactoring plan

| # | Вопрос | Контекст |
|---|---|---|
| ⚠️ | Co-founder split на Track A (data) vs Track B (UI/orchestration) | Q5, влияет на параллелизм |
| ⚠️ | Arrow Flight preparation layer (если да — добавить в C.0 или отдельный C.6) | Q32, не в October scope |
| ⚠️ | PostgreSQL semantic listener как параллельный workstream | Q14, планируется start of May |

---

## История изменений

| Дата | Версия | Что изменилось |
|---|---|---|
| 11.04.2026 | 1.0 | Initial draft refactoring plan. C.0 ArcadeDB upgrade добавлен как blocking first task. C.1 Hound library refactor. C.2 SHUTTLE с SQL injection fix + mutations/subscriptions/REST clients. C.3 Chur с HEIMDALL proxy и aida-bff rename. C.4 VERDANDI с ANVIL UI и MIMIR Chat. C.5 Infrastructure updates. Critical path по неделям. |
| 12.04.2026 | 1.1 | **C.0 пересмотрен.** HoundArcade (remote) уже на 26.3.2 — SHUTTLE/production уже на 26.x. Embedded только в Hound тестах (25.12.1). C.0 переформулирован как «перевод тестов Hound на network mode». Effort C.0: 5-8 дней → 1-3 дня. Total refactoring effort пересмотрен: 30-35 → 25-30 дней. Политика зафиксирована: no mixed embedded/network versions на одной БД. Вариант 1 (тесты → network mode) выбран над Вариантом 2 (ANTLR shading) — меньше техдолга. |
| 12.04.2026 | 1.2 | **C.1 расширен по code review (11.04.2026).** Добавлен C.1.0: 7 bug fixes (3 critical NPE в ensureTable/ArcadeDBSemanticWriter, бесконечная рекурсия в resolveImplicitTableInternal, quality > 1.0, thread naming, InterruptedException без break). Добавлен C.1.6: performance (O(A×T)→O(A) в NameResolver, O(N²)→O(N) в addChildStatement, depth caching). Добавлен C.1.7: technical debt (дедупликация 3 методов, ISemanticEngine интерфейс, цикл UniversalParser↔ParserRegistry). Effort C.1: ~4 дня → ~6-7 дней. Источник: `HOUND_CODE_REVIEW.md`. |
