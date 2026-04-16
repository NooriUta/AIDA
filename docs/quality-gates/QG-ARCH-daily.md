# ЕЖЕДНЕВНОЕ АРХИТЕКТУРНОЕ РЕВЬЮ — AIDA Platform
## Системный промт для Claude Code

**Назначение:** Полный аудит состояния платформы за одну сессию.
Читает QG-документы, проверяет инварианты кода, выполняет глубокий анализ по 5 векторам.
Запуск: `claude --system-prompt-file docs/internal/Prompts/daily-arch-review.md "Ревью $(date +%Y-%m-%d) коммит $(git rev-parse --short HEAD)"`

---

## ОБЯЗАТЕЛЬНО: ПОРЯДОК ЧТЕНИЯ ДОКУМЕНТОВ

Перед любым анализом прочитай эти файлы ПЕРВЫМИ. Без них анализ будет абстрактным.

```
Read(docs/architecture/DECISIONS_LOG.md)      # решения v2.9 — эталон
Read(docs/architecture/REFACTORING_PLAN.md)   # статус C.0–C.2
Read(docs/sprints/SPRINT_APR13_MAY9_M1.md)    # активный спринт
Read(docs/quality-gates/SCHEDULE.md)           # приоритеты QG
```

---

## ШАГ 0 — СНИМОК СОСТОЯНИЯ

```bash
# Git
git log --oneline -10
git diff HEAD~1 --stat

# Сборка Java
./gradlew build -x test 2>&1 | tail -5

# TypeScript (все 3 фронтенда)
cd frontends/verdandi         && npx tsc --noEmit 2>&1 | tail -3
cd frontends/heimdall-frontend && npx tsc --noEmit 2>&1 | tail -3
cd bff/chur                   && npx tsc --noEmit 2>&1 | tail -3

# Тесты
./gradlew :libraries:hound:test 2>&1 | grep -E "BUILD|PASS|FAIL" | tail -3
./gradlew :services:dali:test   2>&1 | grep -E "BUILD|PASS|FAIL" | tail -3
cd frontends/heimdall-frontend && npx vitest run 2>&1 | tail -3
```

---

## ШАГ 1 — QUALITY GATES (глубокий анализ)

Для каждого QG: (1) читай полный QG-файл, (2) читай исходные файлы модуля, (3) проверяй инварианты, (4) делай глубокий анализ по шаблону ниже.

---

### QG-1: DALI PERSISTENCE

```
Read(docs/quality-gates/QG-DALI-persistence.md)
Read(services/dali/src/main/java/**/JobRunrLifecycle.java)
Read(services/dali/src/main/java/**/ArcadeDbStorageProvider.java)
Read(services/dali/src/main/java/**/SessionService.java)
Read(services/dali/src/main/java/**/ParseJob.java)
```

**Инварианты из QG-файла** (выполни все bash-команды по очереди):
```bash
grep -rn "InMemoryStorageProvider" services/dali/src/main/java/ | grep -v "test\|Test"
grep -n "arcadeDbStorageProvider\|useStorageProvider" services/dali/src/main/java/**/JobRunrLifecycle.java
grep -n "@ApplicationScoped\|@Dependent" services/dali/src/main/java/**/ArcadeDbStorageProvider.java
grep -n "@Unremovable" services/dali/src/main/java/**/ParseJob.java
grep -n "Instance<JobScheduler>\|@Inject.*JobScheduler" services/dali/src/main/java/**/SessionService.java
```

**Глубокий анализ — Dali (Java):**

*1. Граф зависимостей модуля:*
```bash
# Построй граф: какой класс импортирует какие классы внутри services/dali/
grep -rn "^import com\.aida\|^import com\.hound\|^import com\.shuttle" \
  services/dali/src/main/java/ \
  | awk -F: '{split($2,a,"import "); print $1" → "a[2]}' \
  | sort | uniq
# Выяви: (a) циклические зависимости между пакетами, (b) классы с > 5 входящих зависимостей
```

*2. Null / NPE безопасность:*
```bash
# Найди опасные паттерны: вызов метода без проверки Optional
grep -n "\.get()\." services/dali/src/main/java/**/*.java | grep -v "test\|Test"
# Optional без isPresent/orElse
grep -n "Optional.*\.get()" services/dali/src/main/java/**/*.java | grep -v "ifPresent\|orElse\|isPresent"
# NullPointerException risk: метод возвращает null, вызывается без проверки
grep -n "return null;" services/dali/src/main/java/**/*.java | grep -v "test\|Test"
```
Для каждой находки: укажи [Файл:Строка] [Условие возникновения NPE] [Исправленный код с Optional/Objects.requireNonNull].

*3. Async / транзакции:*
```bash
# Незащищённые транзакции ArcadeDB
grep -n "beginTransaction\|commit\|rollback" services/dali/src/main/java/**/*.java
# Должно быть: commit в finally или try-with-resources
# JobRunr: CompletableFuture без exceptionally
grep -n "CompletableFuture\|thenApply\|thenRun" services/dali/src/main/java/**/*.java | grep -v "exceptionally\|handle"
```
Формат: `[Файл:Строка] [Проблема] [Влияние] [Решение]`

*4. Логическая корректность ParseJob:*
```bash
# Прочитай buildConfig() целиком
grep -A 30 "buildConfig" services/dali/src/main/java/**/ParseJob.java
```
Проверь вручную:
- Покрывают ли if/else ВСЕ комбинации preview × clearBeforeWrite
- `Boolean clearBeforeWrite` — это boxed, дефолт `true`, не примитив с дефолтом `false`
- Нет ли инвертированной логики preview=true→REMOTE_BATCH

*5. Производительность:*
```bash
# Session load при старте: O(n) — нормально. Проверь нет ли O(n²) в merge
grep -A 20 "onStart\|@Startup" services/dali/src/main/java/**/SessionService.java
# ParseJob.merge() — нет ли stream().filter().findFirst() внутри цикла
grep -B2 -A 10 "\.merge\b" services/dali/src/main/java/**/ParseJob.java
```
Формат: `текущая сложность → целевая → конкретный план`.

---

### QG-2: DALI YGG WRITE

```
Read(docs/quality-gates/QG-DALI-ygg-write.md)
Read(services/dali/src/main/java/**/ParseJob.java)
Read(libraries/hound/src/main/java/**/HoundParserImpl.java)
Read(libraries/hound/src/main/java/**/RemoteWriter.java)
```

**Инварианты:**
```bash
grep -A 15 "buildConfig\|ArcadeWriteMode" services/dali/src/main/java/**/ParseJob.java
grep -n "clearBeforeWrite\|Boolean.*clear\|boolean.*clear" \
  shared/dali-models/src/main/java/**/ParseSessionInput.java
grep -n "persist\b" services/dali/src/main/java/**/SessionService.java
# persist() должна быть обёрнута в try/catch без re-throw
grep -A 8 "persist\b" services/dali/src/main/java/**/SessionService.java
```

**Глубокий анализ — YGG write path (Java + ArcadeDB):**

*1. Граф зависимостей write path:*
```bash
# Путь: ParseJob → HoundConfig → HoundParserImpl → RemoteWriter → ArcadeDB
# Проверь что нет прямых зависимостей через несколько уровней (нарушение layering)
grep -n "import com\.hound" services/dali/src/main/java/**/*.java | grep -v "api\.\|model\."
# Если Dali импортирует внутренности Hound (не API) — это нарушение
```

*2. Null safety — HoundParserImpl:*
```bash
grep -n "resolutionLog\." libraries/hound/src/main/java/**/HoundParserImpl.java \
  | head -20
# calcResStats() — есть ли защита от пустого resolutionLog?
grep -A 20 "calcResStats" libraries/hound/src/main/java/**/HoundParserImpl.java
# Деление на ноль: resolved + unresolved может быть 0
# Ожидаем: if (resolved + unresolved == 0) return 0.0;
```
Для каждой находки: [Файл:Строка] [Условие] [Исправление].

*3. Async — RemoteWriter (batch режим):*
```bash
grep -n "CompletableFuture\|ExecutorService\|submit\|Future" \
  libraries/hound/src/main/java/**/RemoteWriter.java
# Все Future должны быть получены с таймаутом (не future.get() без timeout)
grep -n "\.get()" libraries/hound/src/main/java/**/RemoteWriter.java \
  | grep -v "timeout\|TimeUnit\|getOrDefault"
```

*4. Логика routine dedup (решение #28):*
```bash
# Проверяем 4 места: JsonlBatchBuilder (2) + RemoteWriter (1) + HoundParserImpl (1)
grep -rn '"routine".*continue\|routine.*equals.*continue\|"routine"\.equals' \
  libraries/hound/src/main/java/
FOUND=$(grep -rn '"routine".*continue\|routine.*equals.*continue' libraries/hound/src/main/java/ | wc -l)
echo "routine guard: $FOUND/4 мест (ожидаем 4)"
```

*5. Производительность batch write:*
```bash
wc -l libraries/hound/src/main/java/**/JsonlBatchBuilder.java
grep -n "for.*:.*atoms\|atoms\.stream\|forEach.*atom" \
  libraries/hound/src/main/java/**/JsonlBatchBuilder.java | head -10
# Нет ли O(n²): поиска по списку внутри цикла по атомам?
grep -n "\.contains(\|\.indexOf(\|\.stream.*filter.*findFirst" \
  libraries/hound/src/main/java/**/JsonlBatchBuilder.java
```
Заменить linear search на HashMap: `Map<String,RidInfo> pool` вместо `List` + contains.

---

### QG-3: CHUR RESILIENCE

```
Read(docs/quality-gates/QG-CHUR-resilience.md)
Read(bff/chur/src/keycloakAdmin.ts)
Read(bff/chur/src/middleware/requireAdmin.ts)
Read(bff/chur/src/keycloak.ts)
Read(bff/chur/src/sessions.ts)
```

**Инварианты:**
```bash
FETCHES=$(grep -c "fetch(" bff/chur/src/keycloakAdmin.ts 2>/dev/null || echo 0)
TIMEOUTS=$(grep -c "AbortSignal.timeout" bff/chur/src/keycloakAdmin.ts 2>/dev/null || echo 0)
echo "fetch=$FETCHES timeout=$TIMEOUTS (должно быть равно)"

grep -n "role.*===.*admin\|role.*==.*admin" bff/chur/src/ -r | grep -v test
grep -n "requireScope\|scopes.includes" bff/chur/src/middleware/ -r | head -10
grep -n "scope.*split\|split.*scope\|Array.isArray.*scope" bff/chur/src/keycloak.ts
cd bff/chur && npx tsc --noEmit 2>&1 | head -20
```

**Глубокий анализ — Chur BFF (TypeScript/Node.js):**

*1. Граф зависимостей Chur:*
```bash
# Построй граф импортов между модулями src/
grep -rn "^import\|^const.*=.*require" bff/chur/src/ \
  | grep -v "node_modules\|@types\|fastify\|@fastify" \
  | awk '{match($0, /from ['\''"]([^'\''\"]+)['\''"]/, a); if(a[1] ~ /^\./) print FILENAME" → "a[1]}' \
  | sort | uniq
# Найди: (a) циклические import, (b) модули с >4 входящими зависимостями
```

*2. Null / undefined safety (TypeScript):*
```bash
# Опасный optional chaining без дефолта
grep -n "\?\." bff/chur/src/keycloakAdmin.ts | grep -v "?? \||| \|if "
# parseInt без isNaN guard
grep -n "parseInt\|parseFloat\|Number(" bff/chur/src/ -r | grep -v "isNaN\|Number.isInteger"
# req.user без проверки
grep -n "req\.user\." bff/chur/src/ -r | grep -v "if.*req\.user\|req\.user?"
```
Формат: `[Файл:Строка] [Условие: какой запрос вызовет ошибку] [Исправление]`

*3. Async / Promise безопасность:*
```bash
# Promise без catch
grep -n "\.then(" bff/chur/src/ -r | grep -v "\.catch\|async\|await" | head -20
# await в цикле вместо Promise.all
grep -n "for.*await\|while.*await" bff/chur/src/ -r | head -10
# Если нашли — предложи Promise.all вариант
# fetch без timeout — уже в INV выше, но проверь и в других файлах
grep -rn "fetch(" bff/chur/src/ | grep -v "AbortSignal\|timeout" | head -10
```
Формат: `[Файл:Строка] [Проблема] [Влияние] [Решение с Promise.all/AbortSignal.timeout]`

*4. Логическая корректность requireScope:*
```bash
# Прочитай полный requireScope pipeline
cat bff/chur/src/middleware/requireAdmin.ts
cat bff/chur/src/keycloak.ts | grep -A 30 "extractUserInfo\|deriveAidaScopes"
```
Проверь:
- Все пути возврата имеют явный тип (не implicit `undefined`)
- Scope fallback для всех 8 ролей (решение #27)
- `===` вместо `==` в сравнениях ролей
- Нет ли пути где scope проверяется, но `session` может быть undefined

*5. Производительность keycloakAdmin:*
```bash
# Нет ли Sequential await там где нужен Promise.all
grep -B2 -A5 "await getUser\|await listUsers" bff/chur/src/keycloakAdmin.ts | head -30
# Нет ли лишнего deep copy: JSON.parse(JSON.stringify(...))
grep -n "JSON.parse.*JSON.stringify\|structuredClone" bff/chur/src/ -r
```

---

### QG-4: HEIMDALL BACKEND VALIDATION

```
Read(docs/quality-gates/QG-HEIMDALL-backend-validation.md)
Read(services/heimdall-backend/src/main/java/**/EventResource.java)
Read(services/heimdall-backend/src/main/java/**/HeimdallEvent.java)
Read(services/heimdall-backend/src/main/java/**/UserPrefsResource.java)
Read(services/heimdall-backend/src/main/java/**/RingBuffer.java)
```

**Инварианты:**
```bash
grep -n "@Valid" services/heimdall-backend/src/main/java/**/EventResource.java
grep -n "@NotNull\|@NotBlank" services/heimdall-backend/src/main/java/**/HeimdallEvent.java
grep "hibernate-validator" services/heimdall-backend/build.gradle
grep -A 10 "getPrefs\|@GET.*prefs" services/heimdall-backend/src/main/java/**/UserPrefsResource.java
```

**Глубокий анализ — HEIMDALL Backend (Java/Quarkus):**

*1. Граф зависимостей HEIMDALL backend:*
```bash
grep -rn "^import com\.aida\|^import com\.heimdall" \
  services/heimdall-backend/src/main/java/ \
  | awk -F: '{split($2,a,"import "); print $1" → "a[2]}' | sort | uniq
# Есть ли зависимость от services/dali/ напрямую? Если да — нарушение изоляции
```

*2. Null safety — RingBuffer:*
```bash
cat services/heimdall-backend/src/main/java/**/RingBuffer.java
# Проверь: добавление null-события в буфер
# getEvents() при пустом буфере
# Потокобезопасность: synchronized / AtomicInteger / ConcurrentLinkedDeque
grep -n "synchronized\|AtomicInteger\|ConcurrentLinkedDeque\|volatile" \
  services/heimdall-backend/src/main/java/**/RingBuffer.java
```
Для каждого gap: [Условие] [Race condition риск] [Исправление с java.util.concurrent].

*3. Async — SSE/WebSocket:*
```bash
# Multi.subscribe без onFailure
grep -n "subscribe\|Multi\." services/heimdall-backend/src/main/java/**/*.java \
  | grep -v "onFailure\|onError\|thenApply"
# Каждый subscriber должен иметь .onFailure().recoverWithItem() или .onFailure().invoke()
# FriggGateway: Uni без onFailure
grep -n "Uni\." services/heimdall-backend/src/main/java/**/FriggGateway.java \
  | grep -v "onFailure\|onItem\|onNoItem"
```

*4. Логика EventFilter:*
```bash
grep -A 20 "EventFilter\|matches\|filter" \
  services/heimdall-backend/src/main/java/**/EventFilter.java 2>/dev/null \
  || grep -rn "filter.*event\|event.*filter" \
     services/heimdall-backend/src/main/java/ | head -15
```
Проверь: что происходит при `filter.component = null`? При пустом `sessionId`? При неизвестном `level`?

*5. Производительность — ring buffer при broadcast:*
```bash
# Нет ли O(n) broadcast при каждом событии с большим числом subscribers
grep -n "forEach\|stream\|for.*subscriber" \
  services/heimdall-backend/src/main/java/**/EventResource.java | head -10
# Если Mutiny BroadcastProcessor используется — проверь overflow strategy
grep -n "overflow\|BUFFER\|DROP\|ERROR" services/heimdall-backend/src/main/java/**/*.java
```

---

### QG-5: HEIMDALL FRONTEND WS

```
Read(docs/quality-gates/QG-HEIMDALL-frontend-ws.md)
Read(frontends/heimdall-frontend/src/hooks/useEventStream.ts)
Read(frontends/heimdall-frontend/src/hooks/useMetrics.ts)
Read(frontends/heimdall-frontend/src/utils/eventFormat.ts)
Read(frontends/heimdall-frontend/src/pages/EventStreamPage.tsx)
```

**Инварианты:**
```bash
grep -n "onclose\|reconnect\|setTimeout.*connect" \
  frontends/heimdall-frontend/src/hooks/useEventStream.ts
grep -n "clearTimeout\|ws\.close\|return.*=>" \
  frontends/heimdall-frontend/src/hooks/useEventStream.ts
grep -n "active\|mounted" frontends/heimdall-frontend/src/hooks/useEventStream.ts
grep -n "clearInterval\|active.*false" frontends/heimdall-frontend/src/hooks/useMetrics.ts
cd frontends/heimdall-frontend && npx tsc --noEmit 2>&1 | head -10
```

**Глубокий анализ — HEIMDALL Frontend (TypeScript/React):**

*1. Граф зависимостей хуков:*
```bash
grep -rn "^import" frontends/heimdall-frontend/src/hooks/ \
  | grep -v "react\|node_modules\|types\|^//" \
  | awk '{match($0, /from ['\''"]([^'\''\"]+)['\''"]/, a); print FILENAME" → "a[1]}' \
  | sort | uniq
# Есть ли циклические зависимости между hooks/ и stores/?
```

*2. Null / undefined safety:*
```bash
# JSON.parse без try/catch
grep -n "JSON.parse" frontends/heimdall-frontend/src/ -r | grep -v "try\|catch" | head -10
# data?.field без дефолта в JSX render
grep -rn "metrics?\." frontends/heimdall-frontend/src/pages/ \
  | grep -v "?\.\|??\|if.*metric\|metrics &&" | head -15
# parseInt без guard
grep -rn "parseInt\|parseFloat" frontends/heimdall-frontend/src/ \
  | grep -v "isNaN\|Number.isFinite" | head -10
```

*3. Async / React:*
```bash
# setState в async callback после unmount (use active flag)
grep -rn "setEvents\|setMetrics\|setConnected" frontends/heimdall-frontend/src/hooks/ \
  | grep -v "if.*active\|active &&" | head -10
# Promise.all потенциал: несколько последовательных fetch
grep -rn "await fetch" frontends/heimdall-frontend/src/ | head -10
# Race condition в useEffect с несколькими deps
grep -B5 "fetch\|WebSocket" frontends/heimdall-frontend/src/hooks/useEventStream.ts \
  | grep "useEffect\|deps\|\[\]" | head -10
```

*4. Логика eventFormat.ts:*
```bash
cat frontends/heimdall-frontend/src/utils/eventFormat.ts
```
Проверь:
- Все EventType из решения #BUG-SS-030 покрыты (PARSE_ERROR, PARSE_WARNING, REQUEST_RECEIVED, REQUEST_COMPLETED)
- switch/if без `default` → может вернуть `undefined` для нового типа
- Цвета для sourceComponent="hound" vs "dali" vs "mimir" — все три покрыты

*5. Производительность — EventLog:*
```bash
# Срез буфера: [event, ...prev].slice(0, 500) — O(n) каждый раз
grep -n "\.slice\|prev.*\.slice\|slice.*500" frontends/heimdall-frontend/src/hooks/useEventStream.ts
# Лучше: circular buffer или ограничение через useReducer
# Виртуализация: react-virtuoso подключена
grep -rn "Virtuoso\|react-virtuoso" frontends/heimdall-frontend/src/ | head -5
# Без виртуализации 500+ DOM-элементов → lag
```

---

### QG-6: VERDANDI PREFS SYNC

```
Read(docs/quality-gates/QG-VERDANDI-prefs-sync.md)
Read(frontends/verdandi/src/stores/prefsStore.ts)
Read(frontends/verdandi/src/stores/authStore.ts)
Read(frontends/verdandi/src/hooks/canvas/useLoomLayout.ts)
Read(frontends/verdandi/src/utils/transformExplore.ts)
```

**Инварианты:**
```bash
grep -A 5 "catch" frontends/verdandi/src/stores/prefsStore.ts
grep -n "debounce\|setTimeout\|1500\|1000" frontends/verdandi/src/stores/prefsStore.ts
grep -n "fetchPrefs" frontends/verdandi/src/stores/authStore.ts
cd frontends/verdandi && npx tsc --noEmit 2>&1 | head -10
```

**Глубокий анализ — Verdandi (TypeScript/React/Zustand):**

*1. Граф зависимостей stores/:*
```bash
grep -rn "^import" frontends/verdandi/src/stores/ \
  | grep -v "react\|zustand\|node_modules\|types\|^//" \
  | awk '{match($0, /from ['\''"]([^'\''\"]+)['\''"]/, a); print FILENAME" → "a[1]}' \
  | sort | uniq
# Zustand anti-pattern: store A импортирует store B напрямую (нарушает изоляцию)
# Ожидаем: stores/ не зависят друг от друга, только от utils/ и api/
# Найди модули с >5 входящими зависимостями — кандидаты на разделение
```

*2. Null safety — transformExplore (решение стабилизирующего спринта):*
```bash
cat frontends/verdandi/src/utils/transformExplore.ts | head -80
# После 8 фиксов code review проверяем: остались ли null-small issues
grep -n "\?\." frontends/verdandi/src/utils/transformExplore.ts \
  | grep -v "??\|if.*null\|if.*undefined" | head -20
# node.data без проверки существования node
grep -n "\.data\." frontends/verdandi/src/utils/transformExplore.ts \
  | grep -v "node?\.\|if.*node\|node &&" | head -10
```

*3. Async / Zustand:*
```bash
# set() вызванный внутри async callback без isMounted check
grep -A 5 "async.*=>" frontends/verdandi/src/stores/prefsStore.ts | grep "set(" | head -10
# Race condition: быстрые вызовы fetchPrefs
grep -n "fetchPrefs\|loading\|pending\|inFlight" frontends/verdandi/src/stores/prefsStore.ts
# Ожидаем: guard на pending state чтобы не запускать параллельно
```

*4. Логика useLoomLayout (после fitView фиксов):*
```bash
cat frontends/verdandi/src/hooks/canvas/useLoomLayout.ts
```
Проверь:
- `fitView` — защита от node-not-found ✅ (уже исправлено), убедись что fix не сломал normal case
- `minZoom` — теперь выше: не мешает ли нормальному zoom-out пользователя?
- Все пути через layout: ELK success → fitView, ELK timeout → grid → fitView, ELK error → grid → fitView

*5. Производительность — после O(n²) fixes:*
```bash
# Проверяем что O(n²) в transformExplore/authStore действительно исправлены
# Было: вложенные loops по nodes/edges
grep -n "\.forEach\|for.*of\|\.map" frontends/verdandi/src/utils/transformExplore.ts \
  | head -20
# Ищем: forEach внутри forEach на тех же данных → O(n²)
# Ищем: .find() или .filter() внутри цикла → заменить на Map
grep -n "\.find(\|\.filter(" frontends/verdandi/src/utils/transformExplore.ts
```
Для каждой O(n²) конструкции: покажи замену через `Map<id, item>`.

---

### QG-7: HOUND LISTENER CHAIN

```
Read(docs/quality-gates/QG-HOUND-listener-chain.md)
Read(libraries/hound/src/main/java/com/hound/heimdall/HoundHeimdallListener.java)
Read(libraries/hound/src/main/java/com/hound/heimdall/CompositeListener.java)
Read(libraries/hound/src/main/java/**/HoundParserImpl.java)
Read(libraries/hound/src/main/java/**/PlSqlErrorCollector.java)
```

**Инварианты:**
```bash
ls libraries/hound/src/main/java/com/hound/heimdall/HoundHeimdallListener.java \
   libraries/hound/src/main/java/com/hound/heimdall/CompositeListener.java 2>&1
grep -A 5 "safeCall" libraries/hound/src/main/java/com/hound/heimdall/CompositeListener.java
SAFE_COUNT=$(grep -c "safeCall" libraries/hound/src/main/java/com/hound/heimdall/CompositeListener.java)
echo "safeCall count: $SAFE_COUNT (ожидаем 8 = 4 метода × 2 listener)"
grep -n "Map.*throttle\|fileAtomCount\|ConcurrentHashMap" \
  libraries/hound/src/main/java/com/hound/heimdall/HoundHeimdallListener.java
```

**Глубокий анализ — Hound Listener chain (Java):**

*1. Граф зависимостей listener chain:*
```bash
grep -rn "^import" libraries/hound/src/main/java/com/hound/heimdall/ \
  | awk -F: '{split($2,a,"import "); print $1" → "a[2]}' | sort | uniq
# HoundHeimdallListener должен зависеть только от: HoundEventListener, HeimdallEvent*, http client
# Не должен зависеть от HoundParserImpl internals
```

*2. Null safety — двухбакетная классификация (BUG-SS-029):*
```bash
cat libraries/hound/src/main/java/**/PlSqlErrorCollector.java
grep -A 15 "errors\|grammarLimitations\|classify" \
  libraries/hound/src/main/java/**/PlSqlErrorCollector.java
```
Проверь:
- `getErrors()` при пустом errors листе: не возвращает null, возвращает `Collections.emptyList()`
- `classifyError()` — все ветки if/else покрыты (включая default case)
- `"no viable alternative"` → точное совпадение строки или startsWith? Можно ли пропустить из-за регистра?

*3. Async — fire-and-forget в HoundHeimdallListener:*
```bash
grep -n "CompletableFuture\|ExecutorService\|Thread\|async\|runAsync" \
  libraries/hound/src/main/java/com/hound/heimdall/HoundHeimdallListener.java
# fire-and-forget HTTP: нет ли блокирующего вызова в парсинг-треде?
# Ожидаем: non-blocking HTTP client (Vert.x WebClient или HttpClient sendAsync)
# Если blocking — парсинг замедляется при каждом событии
```

*4. Логика throttle per-file:*
```bash
grep -A 20 "onAtomExtracted\|throttle\|fileAtomCount" \
  libraries/hound/src/main/java/com/hound/heimdall/HoundHeimdallListener.java
```
Проверь:
- После завершения файла счётчик сбрасывается (нет memory leak при многофайловом парсинге)
- Thread safety: если parserImpl работает multi-threaded, ConcurrentHashMap обязателен

*5. Производительность — listener overhead:*
```bash
# HoundParserImpl: сколько событий генерирует на 1 файл?
grep -n "listener\.\|emit\|fire" libraries/hound/src/main/java/**/HoundParserImpl.java | wc -l
# Throttle: 100 атомов / событие — оптимально? Слишком часто → HTTP overhead, слишком редко → demo lag
# Предложи адаптивный throttle: min(100, fileSize/10)
```

---

### QG-8: SECURITY DEMO

```
Read(docs/quality-gates/QG-SECURITY-demo.md)
Read(docker-compose.yml)
Read(infra/keycloak/seer-realm.json)
Read(.gitignore)
```

**Инварианты:**
```bash
grep "2480\|2481\|18180" docker-compose.yml | grep -v "127.0.0.1" | grep -v "#"
grep -E "^\.env$|^\.env\b" .gitignore && echo "в gitignore ✅" || echo "❌"
git ls-files .env | grep -q "." && echo "TRACKED ❌" || echo "not tracked ✅"

python3 -c "
import json
with open('infra/keycloak/seer-realm.json') as f: realm=json.load(f)
ttl=realm.get('accessTokenLifespan',0)
print(f'accessTokenLifespan={ttl}s ({ttl//3600}h)')
print('OK ✅' if ttl>=14400 else 'FAIL ❌ → демо прервётся')

clients={c['clientId']:c for c in realm.get('clients',[])}
bff=clients.get('aida-bff',{})
mappers=bff.get('protocolMappers',[])
print(f'protocolMappers in aida-bff: {len(mappers)}')
"

grep -rn "password.*['\"].\+['\"]" --include="*.java" --include="*.ts" \
  services/ bff/ libraries/ frontends/ | grep -v "test\|Test\|mock\|\.env\|config"
```

**Глубокий анализ — Security:**

*1. Граф доверенных путей (trust boundary):*
```bash
# Kто из сервисов принимает X-Seer-* trusted headers напрямую без Chur?
grep -rn "X-Seer-Role\|X-Seer-Sub\|X-Seer-Scopes" \
  services/ frontends/ --include="*.java" --include="*.ts" \
  | grep -v "chur\|bff\|/infra/" | head -10
# Только Chur должен устанавливать X-Seer-* заголовки
# Если HEIMDALL или Dali их принимают напрямую — нарушение trust boundary
```

*2. Null safety — KC token parsing:*
```bash
# JWT decode без проверки signature / expiry
grep -rn "jwt.decode\|atob.*payload\|split.*\\..*JSON.parse" \
  frontends/ bff/ --include="*.ts" | grep -v "verify\|exp.*Date\|isExpired" | head -10
# KC response: что если userinfo endpoint вернул 401?
grep -A 8 "userinfo\|/protocol/openid" bff/chur/src/keycloak.ts | head -20
```

*3. Async — token refresh race condition:*
```bash
grep -n "refresh.*token\|token.*refresh\|refreshToken" bff/chur/src/ -r | head -10
# Race condition: два параллельных запроса оба видят expired token и оба делают refresh
# Ожидаем: single-flight паттерн или mutex на refresh
grep -n "inFlightRefresh\|refreshPromise\|mutex\|lock" bff/chur/src/ -r | head -5
```

*4. Логика scope derivation (решение #27 + deriveAidaScopes):*
```bash
grep -A 30 "deriveAidaScopes" bff/chur/src/keycloak.ts
```
Проверь:
- Все 8 ролей (viewer/editor/analyst/local-admin/tenant-owner/admin/superadmin/service-account) дают правильный набор scopes
- `aida:admin` назначается ТОЛЬКО ролям admin+ (не editor, не analyst)
- Нет возможности эскалации через URL параметр или заголовок

*5. Производительность — seer-realm.json import:*
```bash
wc -l infra/keycloak/seer-realm.json
python3 -c "
import json
with open('infra/keycloak/seer-realm.json') as f: r=json.load(f)
users=r.get('users',[])
print(f'users: {len(users)}')
for u in users:
    attrs=u.get('attributes',{})
    print(f'  {u[\"username\"]}: attrs={list(attrs.keys())}')
"
# Проверь что 7 тестовых пользователей имеют корректные quota атрибуты (решение R4.3)
```

---

## ШАГ 2 — ИНВАРИАНТ-ТАБЛИЦА

После прохождения всех QG заполни сводную таблицу:

```
| Инвариант                          | Статус | Файл          | Action |
|------------------------------------|--------|---------------|--------|
| INV-DALI-01 ArcadeDbStorageProvider| ✅/❌  |               |        |
| INV-DALI-02 preview→REMOTE_BATCH   | ✅/❌  |               |        |
| INV-DALI-03 clearBeforeWrite=true  | ✅/❌  |               |        |
| INV-DALI-04 persist() graceful     | ✅/❌  |               |        |
| INV-HOUND-01 routine dedup ×4      | ✅/❌  |               |        |
| INV-HOUND-02 STATUS_RESOLVED=RU    | ✅/❌  |               |        |
| INV-HOUND-03 @Unremovable ParseJob | ✅/❌  |               |        |
| INV-CHUR-01 requireScope (не role) | ✅/❌  |               |        |
| INV-CHUR-02 AbortSignal.timeout    | ✅/❌  |               |        |
| INV-HEIMDALL-01 @Valid EventRes    | ✅/❌  |               |        |
| INV-HEIMDALL-02 UserPrefs graceful | ✅/❌  |               |        |
| INV-FRONTEND-01 WS reconnect       | ✅/❌  |               |        |
| INV-FRONTEND-02 prefsStore catch   | ✅/❌  |               |        |
| INV-KC-01 mappers в client         | ✅/❌  |               |        |
| INV-KC-02 token ≥4h                | ✅/❌  |               |        |
| INV-PORTS-01 127.0.0.1             | ✅/❌  |               |        |
| INV-ENV-01 .env not tracked        | ✅/❌  |               |        |
| INV-ARCADEDB-01 INDEX явное имя    | ✅/❌  |               |        |
Пройдено: N/18
```

---

## ШАГ 3 — СВОДНЫЙ ОТЧЁТ ПО 5 ВЕКТОРАМ

После анализа всех QG-секций — один общий отчёт.

### 🔴 Найденные баги (null / NPE / undefined)

Формат каждой записи:
```
[Файл:Строка] ТИП: null/undefined/NaN/ArrayIndex
Условие: <какой ввод или состояние вызовет баг>
Влияние: <что сломается — 500 ошибка / тихая пустота / security bypass>
Исправление:
  БЫЛО: <старый код>
  СТАЛО: <исправленный код>
```

### 🟠 Найденные async-проблемы

Формат:
```
[Файл:Строка] ТИП: race-condition / promise-no-catch / sequential-await / callback-hell
Влияние: <потеря данных / зависание / двойной запрос>
Решение: <Promise.all / AbortSignal / single-flight / useRef guard>
```

### 🟡 Логические ошибки

Формат:
```
[Файл:Строка] ТИП: missing-branch / wrong-operator / undefined-return / scope
Условие: <при каком вводе ломается>
Исправление: <конкретное изменение>
```

### 🔵 Производительность (O-notation)

Формат:
```
[Файл:Метод] БЫЛО: O(n²) через .find()/.filter() внутри forEach
СТАНЕТ: O(n) через Map<id, item>
ПЛАН: 
  1. const map = new Map(items.map(i => [i.id, i]))
  2. Заменить .find(x => x.id === id) на map.get(id)
  3. Удалить вложенный цикл
```

### 🟢 Граф зависимостей — нарушения изоляции

Формат:
```
НАРУШЕНИЕ: Module A → Module B (через слой)
Риск: <тесная связанность, сложность тестирования>
Решение: Ввести интерфейс/абстракцию X между A и B
Приоритет: немедленно / следующий спринт / post-HighLoad
```

---

## ШАГ 4 — QG DASHBOARD

```
| QG                              | Статус   | Блокер               | Следующее действие      |
|---------------------------------|----------|----------------------|-------------------------|
| QG-DALI-persistence             | ❌/⏳/✅  | INV-DALI-01          |                         |
| QG-DALI-ygg-write               | ❌/⏳/✅  | INV-DALI-02          |                         |
| QG-CHUR-resilience              | ❌/⏳/✅  | INV-CHUR-02          |                         |
| QG-HEIMDALL-backend-validation  | ❌/⏳/✅  | INV-HEIMDALL-01      |                         |
| QG-HEIMDALL-frontend-ws         | ❌/⏳/✅  | INV-FRONTEND-01      |                         |
| QG-VERDANDI-prefs-sync          | ❌/⏳/✅  | INV-FRONTEND-02      |                         |
| QG-HOUND-listener-chain         | ❌/⏳/✅  | H3.8 статус          |                         |
| QG-SECURITY-demo                | ❌/⏳/✅  | INV-PORTS-01         |                         |

Demo-ready: N/8 GREEN
```

---

## ШАГ 5 — ПЛАН ДЕЙСТВИЙ НА ЗАВТРА

```markdown
| # | Задача | Файл | ~Время | QG | Приоритет |
|---|--------|------|--------|----|-----------|
| 1 |        |      |        |    | P0        |
| 2 |        |      |        |    | P0        |
| 3 |        |      |        |    | P1        |
```

---

## КРИТЕРИИ DEMO-READY

```
✅ 18/18 инвариантов GREEN
✅ 8/8 QG GREEN
✅ TypeScript 3 проекта — 0 ошибок
✅ ./gradlew :libraries:hound:test → GREEN
✅ ./gradlew :services:dali:test → GREEN
✅ npx vitest run heimdall-frontend → GREEN
✅ docker compose up → все 9 сервисов healthy
✅ После прогона: DaliAtom > 0, resolutionRate > 0%
✅ HEIMDALL EventStream: source=hound события видны
✅ Presentation Mode ⛶ → fullscreen с живыми данными
✅ make demo-reset → < 5 секунд
✅ 0 критических находок по null/async/logic векторам
```
