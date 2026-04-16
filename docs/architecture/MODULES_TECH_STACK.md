# AIDA — Карта модулей и технологического стека

**Документ:** `ARCH_11042026_MODULES_TECH_STACK`
**Версия:** 2.6
**Дата:** 15.04.2026
**Статус:** Working document

**История версий:**
- v1.0 (11.04.2026) — initial draft, написан "извне" без сверки с кодом
- v2.0 (11.04.2026) — полная переработка после reconciliation (K1-K20 + детальный review кодовой базы + 8 раундов уточнений)
- v2.1 (11.04.2026) — Dali Core зафиксирован (Quarkus + JobRunr)
- v2.2 (11.04.2026) — большой integration update: AIDA Gradle multi-module, FRIGG как unified persistence, SHUTTLE→Dali HTTP REST, Hound three modes policy, ArcadeDB upgrade (latest), Arrow Flight strategic note, матрица интеграций I1-I35, refactoring plan C, use case timelines
- v2.6 (15.04.2026) — актуализация по кодовой базе: HEIMDALL backend/frontend ✅ WORKING (12-15.04 сессии), Shell MF host добавлен, aida-shared ✅, FRIGG ✅ (snapshots в ArcadeDB), Chur добавлены HEIMDALL proxy routes + @fastify/websocket, ArcadeDB → 26.3.2 confirmed, Q1/Q3/Q5/Q12/Q13/Q16/Q18/Q25 closed.

**Принцип v2:** факты берутся из реальной кодовой базы (`C:\AIDA\`), build-файлов (`build.gradle`, `gradle.properties`, `package.json`). Догадки помечены явно как 🔬 PROPOSED или ⚠️ TBD.

---

## 0. Терминология и иерархия продуктов

Это **критическая** секция — её надо прочитать первой, потому что многие документы (включая ARCH-spec и ARCH-review) написаны до того как иерархия была чётко зафиксирована, и в них AIDA и SEER Studio иногда смешиваются.

### Иерархия

```
AIDA  (зонтик-платформа, компания/бренд)
│
├── SEER Studio          ← первый продукт: Lineage Intelligence Platform
│
└── [Storage Automation] ← второй продукт, planned post-HighLoad
```

**AIDA** — зонтик-платформа. Включает:
- Все продукты SEER Studio
- Будущий продукт автоматизации хранилища (planned)
- Общую инфраструктуру (Keycloak, CI/CD, deployment, design system)

**SEER Studio** — первый продукт под зонтиком AIDA. Lineage Intelligence Platform для legacy SQL. Включает свой backend, свои frontend приложения, свой data layer.

**VERDANDI** — внутренний бренд для **главного frontend приложения** SEER Studio. Внутри VERDANDI живут несколько ипостасей (LOOM, KNOT, и в будущем ANVIL UI).

### Naming conventions

| Уровень | Префикс | Примеры |
|---|---|---|
| AIDA-уровень (общая инфраструктура для всех продуктов) | `aida-` | `aida-bff` (Keycloak client), `aida_net` (Docker network), `aida-shared` (общие npm packages) |
| SEER Studio (lineage product) | `seer-` или без префикса | `seer-realm` (Keycloak realm), Hound, SHUTTLE и т.д. |
| Storage Automation (будущий продукт) | TBD когда появится | — |

**Решено в этой версии документа:**
- ✅ Keycloak client `verdandi-bff` → переименован в **`aida-bff`** (was: `verdandi-bff`)
- ✅ Keycloak realm остаётся `seer` (это SEER Studio realm, не AIDA-уровень)
- ✅ Docker network `verdandi_net` → переименована в `aida_net` (Q18, выполнено в sprint cicd-docker-polish-apr15)

---

## 1. Сводная таблица "As-Is" (по состоянию на 11.04.2026)

Что уже существует и работает в кодовой базе SEER Studio:

| Компонент | Слой | Язык | Framework / Stack | Версия | Статус |
|---|---|---|---|---|---|
| **Hound** | Backend / Algorithms | Java 21 | Gradle + ANTLR 4.13.1 | 1.0.0 | 🟢 WORKING |
| **YGG (HoundArcade)** | Data | — | ArcadeDB engine + network | **latest** (24.11.1 → upgrade pending) | 🟢 WORKING |
| **SHUTTLE** | Backend / API | Java 21 | Gradle + Quarkus 3.34.2 + SmallRye GraphQL | 1.0.0-SNAPSHOT | 🟢 WORKING |
| **Chur** | Backend / BFF | TypeScript | Fastify 4.28.1 + Node.js 22 + jose | 0.1.0 | 🟢 WORKING |
| **VERDANDI** (LOOM + KNOT) | Frontend | TypeScript | React 19.2.4 + Vite 8.0.1 + Tailwind 4 | 0.1.0 | 🟢 WORKING |
| **Keycloak** | Auth Infrastructure | — | Keycloak 26.2 (realm: seer) | 26.2 | 🟢 WORKING |
| **CI/CD** | DevOps | YAML | GitHub Actions + GHCR + SSH deploy | — | 🟢 WORKING |
| **Docker Compose** | DevOps | — | dev + prod | — | 🟢 WORKING |
| **Dali Core** | Backend / Orchestrator | **Java 21** | **Quarkus 3.34.2 + JobRunr (Open Source)** | — | 🔵 IN PROGRESS |
| **HEIMDALL backend** | Cross-cutting / Observability | Java 21 | **Quarkus 3.34.2** (HB1 confirmed) | — | 🟢 WORKING (12-15.04) |
| **HEIMDALL frontend** | Frontend / Admin | TypeScript | React 19 + Vite 8 + shadcn/ui + Nivo + react-virtuoso | — | 🟢 WORKING (12.04) |
| **Shell** | Frontend / MF Host | TypeScript | React 19 + Vite 8 + Module Federation 1.14.2 | — | 🟢 WORKING |
| **aida-shared** | Shared npm package | TypeScript | tokens.css + types + navigation.ts + theme.ts | — | 🟢 WORKING |
| **FRIGG** | Data | — | **Отдельный ArcadeDB instance** (:2481, snapshots + state) | — | 🟢 WORKING |
| **MIMIR** | Backend / AI | TBD | Custom tool framework (Q29: возможен redesign через ArcadeDB MCP Server) | — | 🔵 NEW |
| **ANVIL backend** | Backend / Algorithms | TBD | Graph algorithm (Q30: 72 built-in ArcadeDB algorithms post-upgrade) | — | 🔵 NEW |
| **ANVIL UI** | Frontend (часть VERDANDI) | TypeScript | React (внутри VERDANDI) | — | 🔵 NEW |
| **URD** (frontend + backend) | Frontend + Backend | TBD | TBD | — | 🔵 PLANNED |
| **SKULD** (frontend + backend) | Frontend + Backend | TBD | TBD | — | 🔵 PLANNED |
| **Middleware modules** (application layer для URD/SKULD) | Backend | TBD | TBD | — | 🔵 PLANNED |

### Реальный поток запросов (текущая работающая архитектура)

```
Browser
    ↓  HTTPS
Shell (MF host :5175 dev / :25175 prod) — Module Federation host, AIDA nav
    ↓  lazy import remoteEntry.js
VERDANDI (:5173 dev) / HEIMDALL frontend (:5174 dev) — MF remotes
    ↓  POST /graphql + Cookie sid=<uuid>
Chur (BFF/Gateway :3000) — auth, rate limit, CORS, GraphQL proxy + HEIMDALL proxy
    ↓  POST /graphql + X-Seer-User + X-Seer-Role headers
SHUTTLE (Quarkus GraphQL API :8080) — 9 read queries + 2 KNOT + HeimdallEmitter
    │  ↓  HTTP REST
    │  HoundArcade (ArcadeDB network :2480) — YGG lineage graph
    │    ↑  ArcadeDB REMOTE_BATCH write
    │    Hound (Java 21 parser, batch mode)
    │
    └──► HTTP POST (fire-and-forget)
HEIMDALL backend (Quarkus :9093) — EventResource, ControlResource, MetricsResource
    ↓  WebSocket ws://:9093/ws/events (cold replay 200 + live stream)
HEIMDALL frontend (admin panel)
    ↓  ArcadeDB REST
FRIGG (ArcadeDB :2481) — snapshots + user prefs + JobRunr state
```

**Архитектурные принципы (работающие):**
- **Chur — это BFF / API Gateway** (а не SHUTTLE). SHUTTLE — это backend GraphQL service.
- **Hound пишет в YGG напрямую** (через REMOTE_BATCH), не через прокси. Dali Core НЕ является write proxy.
- **HoundArcade** — external ArcadeDB instance :2480, для SHUTTLE/ANVIL/MIMIR reads + Hound writes.
- **FRIGG** — отдельный ArcadeDB :2481, для HEIMDALL snapshots + user prefs.
- **Keycloak** — single source of truth для auth, realm `seer`, client `aida-bff`.
- **Shell** — Module Federation host, загружает VERDANDI + HEIMDALL как MF remotes.

---

## 2. Иерархия и архитектура SEER Studio

### 2.0 Полная карта компонентов SEER Studio

```
SEER Studio (lineage product внутри AIDA)
│
├── 📱 Frontends
│   │
│   ├── VERDANDI ✅ working (главное user-facing приложение)
│   │   ├── LOOM       — graph canvas (3-уровневый: L1/L2/L3)
│   │   ├── KNOT       — inspector с structure breakdown
│   │   ├── ANVIL UI   🔵 NEW — view для impact analysis результатов
│   │   └── MIMIR Chat 🔵 NEW — natural language interface
│   │
│   ├── HEIMDALL frontend 🔵 NEW (отдельный React проект, не часть VERDANDI)
│   │   └── Admin Control Panel всей AIDA
│   │
│   ├── URD frontend 🔵 PLANNED (отдельный проект, post-HighLoad)
│   │   └── Time-travel / history / audit UI
│   │
│   └── SKULD frontend 🔵 PLANNED (отдельный проект, post-HighLoad)
│       └── Flow design / future state / proposals UI
│
├── 🔧 Backend services
│   │
│   ├── Hound ✅ working — SQL/PL-SQL парсер
│   ├── SHUTTLE ✅ working — GraphQL API (для VERDANDI)
│   ├── Chur ✅ working — BFF / Auth gateway
│   │
│   ├── Dali Core 🔵 NEW — orchestrator парсинга
│   ├── MIMIR 🔵 NEW — LLM tool calling orchestrator
│   ├── ANVIL backend 🔵 NEW — graph algorithms (impact analysis)
│   ├── HEIMDALL backend 🔵 NEW — observability collector
│   │
│   ├── URD backend 🔵 PLANNED — versioning, history, audit
│   └── SKULD backend 🔵 PLANNED — validation, proposals, future state
│
├── 🔗 Middleware / Application Layer modules 🔵 PLANNED
│   └── ⚠️ TBD — отдельные application layer сервисы для URD/SKULD/других frontend проектов
│       (детали будут уточнены, отдельный список ожидается)
│
└── 💾 Data Layer
    ├── YGG / HoundArcade ✅ working — ArcadeDB 24.11.1 (graph + document + key-value)
    ├── FRIGG 🔵 NEW — saved views (отдельный YGG instance)
    └── File system — исходные SQL файлы для парсинга

AIDA platform (общая инфраструктура для всех продуктов)
│
├── 🔐 Keycloak 26.2 ✅ working
│   ├── realm: seer
│   └── client: aida-bff (был verdandi-bff)
│
├── 🚀 CI/CD ✅ working
│   ├── GitHub Actions: ci.yml + cd.yml
│   ├── GHCR (GitHub Container Registry)
│   └── SSH deploy + Telegram notifications
│
├── 🐳 Docker Compose ✅ working
│   ├── docker-compose.yml (dev)
│   ├── docker-compose.prod.yml (prod)
│   └── network: aida_net (переименовано из verdandi_net ✅)
│
└── 🎨 Design system
    ├── Seiðr brandbook (Unbounded + Manrope + IBM Plex Mono, amber palette)
    └── Shared via aida-shared npm package (planned)
```

### 2.1 Принцип «парный модуль» (frontend + backend)

Несколько модулей SEER Studio существуют как **парные**: frontend часть и backend часть, работающие вместе под одним именем. Это важно для понимания.

| Модуль | Frontend | Backend | Назначение |
|---|---|---|---|
| **VERDANDI** | весь VERDANDI SPA | через SHUTTLE | главное user-facing приложение |
| **LOOM** | часть VERDANDI | через SHUTTLE → YGG | graph canvas (lineage visualization) |
| **KNOT** | часть VERDANDI | через SHUTTLE → YGG | inspector |
| **ANVIL** | часть VERDANDI (новая view) | отдельный backend сервис (graph algo) | impact analysis |
| **MIMIR** | часть VERDANDI (chat UI) | отдельный backend сервис (LLM orchestrator) | natural language queries |
| **HEIMDALL** | отдельный React проект | отдельный backend сервис | admin control panel всей AIDA |
| **URD** | отдельный React проект (planned) | отдельный backend сервис (planned) + middleware | time-travel / history |
| **SKULD** | отдельный React проект (planned) | отдельный backend сервис (planned) + middleware | flow design / future state |

**Чтение этой таблицы:**
- LOOM/KNOT/ANVIL UI/MIMIR Chat живут **внутри VERDANDI** как routes/views
- HEIMDALL/URD/SKULD frontends — **отдельные** React приложения (отдельные репозитории)
- Каждый из этих имеет свой backend (SHUTTLE для LOOM/KNOT, отдельные сервисы для остальных)
- URD и SKULD требуют ещё **middleware modules** (application layer) — их структура TBD

### 2.2 Слои архитектуры

| # | Слой | Содержит |
|---|---|---|
| 1 | **Data Layer** | YGG (ArcadeDB), FRIGG, File system |
| 2 | **Backend Services + Algorithms** | Hound, ANVIL backend, Dali Core, Chur (auth) |
| 3 | **AI Orchestration** | MIMIR backend |
| 4 | **API Gateway** | SHUTTLE (для VERDANDI), отдельные API services для HEIMDALL/URD/SKULD |
| 5 | **Middleware / Application Layer** | TBD modules для URD/SKULD |
| 6 | **User Applications (Frontends)** | VERDANDI, HEIMDALL frontend, URD frontend, SKULD frontend |
| 7 | **Cross-cutting Observability** | HEIMDALL (получает события от всех слоёв 1-6, имеет обратный канал control) |

**Изменения от ARCH-spec v1.0:**
- Слой 5 «User Applications» → теперь слой 6 (был SEER Studio)
- Добавлен явный слой 5 «Middleware / Application Layer» для URD/SKULD modules
- Слой 7 (HEIMDALL) — управляет всей AIDA, не только Dali (см. § 3.10 ниже)

### 2.3 AIDA Gradle Multi-Module Project structure

Все JVM-сервисы (Hound, SHUTTLE, Dali, MIMIR backend, ANVIL backend, HEIMDALL backend) живут **в одном Gradle multi-module проекте** с shared dependencies и common build system. Но runtime — **отдельные процессы**, общаются через HTTP/gRPC.

Это решает сразу несколько задач:
- Общий lock-файл зависимостей — все сервисы используют одни версии ArcadeDB, Quarkus, Jackson
- Shared models без дублирования — `dali-models` module содержит `ParseSessionInput`, `Session`, `HoundConfig` которые импортируются и SHUTTLE и Dali
- `implementation project(':hound')` — Dali импортирует Hound напрямую как Java library
- Unified `./gradlew build` для всех сервисов
- Но: каждый сервис запускается отдельно, имеет свой health check, свой deployment, свой failure domain

**Структура:**

```
aida-root/
├── settings.gradle             ← объявляет все модули
├── build.gradle                ← common plugins, repositories
│
├── shared/
│   ├── dali-models/            ← ParseSessionInput, Session, HoundConfig, etc.
│   └── aida-common/            ← общие утилиты, constants
│
├── libraries/
│   └── hound/                  ← java-library plugin, без Quarkus main
│                                   Consumed by :dali через project(':hound')
│
└── services/
    ├── shuttle/                ← Quarkus application (:8080)
    ├── dali/                   ← Quarkus application (:9090), depends on :hound
    ├── mimir/                  ← Quarkus application (TBD port)
    ├── anvil/                  ← Quarkus application (TBD port)
    └── heimdall-backend/       ← Quarkus application (:9093 proposed)
```

**Исключения (не в Gradle проекте):**
- **Chur** — Node.js/TypeScript, свой `package.json`, отдельный репозиторий или подкаталог
- **VERDANDI** — React/Vite, свой `package.json`
- **HEIMDALL frontend** — отдельный React проект
- **URD / SKULD frontends** — отдельные React проекты (planned)

**Build команды:**
- `./gradlew build` — собрать все JVM сервисы
- `./gradlew :dali:quarkusDev` — запустить Dali в dev mode
- `./gradlew :shuttle:quarkusDev` — запустить SHUTTLE в dev mode
- `./gradlew devAll` — запустить все сервисы параллельно (custom task)

**Deployment:** каждый сервис = отдельный Docker image, отдельный контейнер в docker-compose, отдельный health check, независимый restart. Gradle multi-module — это **build-time** объединение, не runtime.

---

### 2.4 Frontend routing — single domain + path routing (ADR-DA-012)

**Зафиксировано:** 12.04.2026

Все frontend приложения SEER Studio живут на **одном домене** под разными путями. Nginx раздаёт разные SPA-бандлы по path prefix.

**URL структура:**

| Path | Приложение | Назначение |
|---|---|---|
| `seer.studio/` → redirect | VERDANDI | lineage visualization (основное) |
| `seer.studio/verdandi/*` | VERDANDI | LOOM + KNOT + ANVIL UI + MIMIR Chat |
| `seer.studio/urd/*` | URD | time-travel / history (planned) |
| `seer.studio/skuld/*` | SKULD | proposals / future state (planned) |
| `seer.studio/heimdall/*` | HEIMDALL frontend | admin control panel |

**Nginx конфигурация (целевая):**

```nginx
server {
    listen 80;
    server_name seer.studio;

    # VERDANDI — главное приложение
    location /verdandi {
        alias /var/www/verdandi/dist;
        try_files $uri $uri/ /verdandi/index.html;
    }

    # URD (когда появится)
    location /urd {
        alias /var/www/urd/dist;
        try_files $uri $uri/ /urd/index.html;
    }

    # SKULD (когда появится)
    location /skuld {
        alias /var/www/skuld/dist;
        try_files $uri $uri/ /skuld/index.html;
    }

    # HEIMDALL frontend
    location /heimdall {
        alias /var/www/heimdall-frontend/dist;
        try_files $uri $uri/ /heimdall/index.html;
    }

    # Root → VERDANDI
    location / {
        return 301 /verdandi;
    }
}
```

**Реализация — B2: Thin JS Shell + Module Federation (ADR-DA-012 CONFIRMED)**

Выбран B2 как архитектурно верное решение (зафиксировано 12.04.2026).

Shell app (`frontends/shell/`) — host в Module Federation терминологии:
- React Router: маршрутизирует `/verdandi/*`, `/urd/*`, `/skuld/*`, `/heimdall/*`
- Zustand store: shared context state (nodeId, currentApp, returnTo)
- `<AidaNav />`: общий navigation header
- Загружает remote apps через `React.lazy(() => import('verdandi/App'))`

Remotes (`frontends/verdandi/`, `frontends/heimdall-frontend/` и т.д.):
- Экспортируют `./App` через `remoteEntry.js`
- Могут запускаться standalone (собственный `main.tsx` + dev server)
- Не знают друг о друге — только о `aida-shared`

**Ключевой принцип:** `aida-shared` объявлен `singleton: true` в MF конфиге. Shell и все remotes используют одну копию модуля в памяти → shared Zustand store доступен везде без prop drilling.

---

### 2.5 Cross-app context passing protocol (ADR-DA-013)

**Зафиксировано:** 12.04.2026

**Принцип:** контекст передаётся через URL. URL = единственный источник правды для кросс-app state. Это работает одинаково в B1 и B2.

**Canonical context ID = ArcadeDB geoid:**

Каждый объект линиджа имеет уникальный geoid формата `{DaliType}:{schema}.{name}`:

```
DaliTable:prod.orders
DaliColumn:prod.orders.customer_id
DaliRoutine:pkg_billing.calculate_total
DaliSchema:prod
```

Geoid — это ключ для передачи контекста между приложениями.

**URL contract — обязательные параметры:**

| Параметр | Тип | Описание |
|---|---|---|
| `nodeId` | geoid string | Основной объект контекста |
| `schema` | string | Схема (опционально, дублирует nodeId) |
| `returnTo` | path string | Куда вернуться после действия |
| `highlight` | geoid string | Объект для подсветки при возврате |
| `sessionId` | uuid | Parse session (для URD — показать историю конкретной сессии) |

**Примеры навигационных переходов:**

```
VERDANDI → URD (посмотреть историю таблицы):
/urd?nodeId=DaliTable:prod.orders&returnTo=/verdandi

URD → VERDANDI (вернуться с подсветкой):
/verdandi?highlight=DaliTable:prod.orders&zoom=true

VERDANDI → SKULD (создать proposal для объекта):
/skuld?nodeId=DaliColumn:prod.orders.customer_id&returnTo=/verdandi

VERDANDI → HEIMDALL (admin действие):
/heimdall?context=session&sessionId=abc-123&returnTo=/verdandi
```

**`aida-shared` — utils для context passing:**

```typescript
// packages/aida-shared/src/navigation.ts

export type AppName = 'verdandi' | 'urd' | 'skuld' | 'heimdall';

export interface AppContext {
    nodeId?: string;        // geoid
    schema?: string;
    returnTo?: string;      // path для кнопки "назад"
    highlight?: string;     // geoid для подсветки при возврате
    sessionId?: string;
}

// Построить URL для перехода между приложениями
export function buildAppUrl(app: AppName, context: AppContext): string {
    const base = `/${app}`;
    const params = new URLSearchParams();
    if (context.nodeId)    params.set('nodeId', context.nodeId);
    if (context.schema)    params.set('schema', context.schema);
    if (context.returnTo)  params.set('returnTo', context.returnTo);
    if (context.highlight) params.set('highlight', context.highlight);
    if (context.sessionId) params.set('sessionId', context.sessionId);
    const qs = params.toString();
    return qs ? `${base}?${qs}` : base;
}

// Навигировать в другое приложение
export function navigateTo(app: AppName, context: AppContext): void {
    window.location.href = buildAppUrl(app, context);
}

// Hook — прочитать контекст из URL params при монтировании
export function useAppContext(): AppContext {
    const params = new URLSearchParams(window.location.search);
    return {
        nodeId:    params.get('nodeId')    ?? undefined,
        schema:    params.get('schema')    ?? undefined,
        returnTo:  params.get('returnTo')  ?? undefined,
        highlight: params.get('highlight') ?? undefined,
        sessionId: params.get('sessionId') ?? undefined,
    };
}
```

**Использование в VERDANDI:**

```typescript
// В LoomCanvas или другом компоненте:
import { navigateTo } from 'aida-shared';

function NodeContextMenu({ nodeId }: { nodeId: string }) {
    return (
        <>
            <button onClick={() => navigateTo('urd', {
                nodeId,
                returnTo: window.location.pathname
            })}>
                View history (URD)
            </button>
            <button onClick={() => navigateTo('skuld', {
                nodeId,
                returnTo: window.location.pathname
            })}>
                Create proposal (SKULD)
            </button>
        </>
    );
}
```

**При инициализации URD/SKULD:**

```typescript
// urd/src/App.tsx
import { useAppContext } from 'aida-shared';

function App() {
    const { nodeId, returnTo } = useAppContext();

    useEffect(() => {
        if (nodeId) {
            // Загрузить историю конкретного объекта
            loadHistory(nodeId);
        }
    }, [nodeId]);

    return (
        <>
            {returnTo && (
                <button onClick={() => navigateTo('verdandi', {
                    highlight: nodeId,
                    // returnTo уже не нужен — мы возвращаемся
                })}>
                    ← Back to lineage
                </button>
            )}
            {/* ... */}
        </>
    );
}
```

---

### 2.6 `aida-shared` npm package

**Зафиксировано:** 12.04.2026
**Путь в монорепо:** `packages/aida-shared/` (новая директория, не `frontends/`)

**Scope L2 (demo):**

```
packages/aida-shared/
├── package.json
├── src/
│   ├── navigation.ts     ← buildAppUrl, navigateTo, useAppContext (§2.5)
│   ├── auth.ts           ← useUser, useAuth, ChurAuthProvider
│   ├── tokens.css        ← CSS переменные (цвета, типографика, spacing)
│   └── index.ts          ← re-exports
```

**Scope L3 (post-HighLoad):**

```
├── components/
│   ├── AidaNav.tsx       ← shared navigation header
│   ├── Button.tsx
│   ├── Toast.tsx
│   └── Modal.tsx
```

**Установка в каждом приложении:**

```json
// frontends/verdandi/package.json
{
    "dependencies": {
        "aida-shared": "file:../../packages/aida-shared"
    }
}
```

**Обновление `aida-root/settings.gradle`** (Node.js оркестрация):

```groovy
// build.gradle — добавить путь для aida-shared
def sharedDir = file('packages/aida-shared')
```

---

## 3. Модули — детальные карточки

### 3.1 Hound

**Слой:** 2 — Backend / Algorithms
**Зрелость:** 🟢 **WORKING** (PL/SQL и обобщённый SQL парсятся ~70%, требуется доработка PostgreSQL и ClickHouse semantic listeners)
**Расположение:** `C:\AIDA\Dali4\HOUND\Hound\`

**Назначение:** Многодиалектный SQL парсер на основе ANTLR4 с pluggable архитектурой диалектов. Извлекает atoms и lineage из исходного кода, записывает в YGG.

**Implementation language:** ✅ **Java 21** (жёсткое требование из-за ArcadeDB embedded engine)

**Функции (As-Is):**
- Лексический и синтаксический анализ через ANTLR4
- Pluggable архитектура диалектов SQL — 16 грамматик загружены как plugins
- Атомарная декомпозиция кода (DaliAtom — атомарная единица lineage)
- Семантический анализ (SemanticEngine оркеструет 5 компонентов: ScopeManager, NameResolver, AtomProcessor, StructureAndLineageBuilder, JoinProcessor)
- Резолюция column-level dependencies для PL/SQL и обобщённого SQL
- Запись результатов в YGG (vertices + edges) — embedded или network mode
- Управление session lifecycle (DaliSession)
- REMOTE_BATCH parallel processing
- Benchmark runner (порт Python `comparison_runner`)

**Технологический стек:**

| Категория | Технология | Версия | Статус |
|---|---|---|---|
| Язык | Java | 21 | ✅ CONFIRMED |
| Build | Gradle (custom `generateGrammars` task) | — | ✅ CONFIRMED |
| Парсинг | ANTLR4 runtime | 4.13.1 | ✅ CONFIRMED |
| Storage engine | ArcadeDB engine | **latest** (post-upgrade, currently 24.11.1) | ✅ CONFIRMED |
| Storage network | ArcadeDB network adapter | **latest** | ✅ CONFIRMED |
| Concurrency | Java threads / executor (REMOTE_BATCH) | — | ✅ CONFIRMED |
| Testing | JUnit 5 | — | ✅ CONFIRMED (24 test files) |

**HoundConfig — three modes policy:**

Hound поддерживает **три режима записи в YGG**, управляемые через `HoundConfig.writeMode`:

```java
public enum ArcadeWriteMode {
    EMBEDDED,      // local ArcadeDB engine в том же JVM, zero network
    REMOTE,        // HTTP REST к HoundArcade :2480, single-row writes
    REMOTE_BATCH   // HTTP REST + buffered batch writes (13.1× speedup)
}
```

**Выбор режима по UC:**

| UC | Сценарий | Режим | Почему |
|---|---|---|---|
| UC1 | Scheduled harvest (много файлов) | **REMOTE_BATCH** (default) | Batch амортизирует network overhead; параллельные читатели работают |
| UC2a | On-demand user parse (preview) | **EMBEDDED** | Без commit в production; preview data stays local ⚠️ TBD implementation |
| UC2b | Event-driven update (один файл) | **REMOTE** (single) | Batching бессмысленно; сразу видно; простой code path |
| UC3 | Benchmark / parser testing | **EMBEDDED** | Pure parser speed, zero network noise |
| UC4 | Offline bulk import (первичная загрузка) | **EMBEDDED** | Максимальная скорость первого import |
| Dev | Local development | **EMBEDDED** / **REMOTE_BATCH** | По удобству |

Dali передаёт `HoundConfig` каждому worker job'у, включая `writeMode` — policy зависит от типа job.

**16 диалектов SQL (pluggable грамматики):**

| Диалект | Grammar статус | Semantic listener | Зрелость |
|---|---|---|---|
| **PL/SQL (Oracle)** | ✅ есть | ✅ ~70% | 🟢 PRODUCTION |
| **Generic SQL** | ✅ есть | ✅ ~70% | 🟢 PRODUCTION |
| **PostgreSQL** | ✅ есть (g4 + base classes зарегистрирован) | ❌ отсутствует | 🟡 NEXT (на основе существующего каркаса) |
| **ClickHouse** | ✅ есть | ❌ отсутствует | 🟡 NEXT (после PostgreSQL) |
| MySQL | ✅ есть | ❌ отсутствует | 🔵 LATER |
| Athena | ✅ есть | ❌ отсутствует | 🔵 LATER |
| Teradata | ✅ есть | ❌ отсутствует | 🔵 LATER |
| ... остальные ~9 диалектов | ✅ есть | ❌ отсутствует | 🔵 LATER |

> **Важно:** грамматики для PostgreSQL, ClickHouse и других — **уже загружены** в Hound как plugins из ANTLR4 grammars-v4 репозитория. Следующий план — **довести PostgreSQL и ClickHouse до уровня PL/SQL**, используя существующий каркас семантического анализа (SemanticEngine + 5 компонентов). Это **не** "писать с нуля", это **расширение working semantic listener** на новые диалекты.

**Текущие метрики (bench 2026-04-07):**
- 300,906 DaliAtom извлечено на 208 файлов
- REMOTE_BATCH speedup: 13.1×
- Среднее время на файл: 321 ms
- Resolution rate: ~70% (по PL/SQL и SQL)

**Производительность (working):**
- Single file 50K LoC: < 5 сек ✅
- Batch 500K LoC, 4 workers: < 30 сек ✅ (REMOTE_BATCH)

**Что нужно к October:**
- PostgreSQL semantic listener (используя существующий каркас)
- ClickHouse semantic listener (после PostgreSQL)
- Resolution rate ≥ 85% (текущий ~70% надо поднять)
- Эмиссия событий в HEIMDALL (event_emitter)
- Edge case handling

**Зависимости:**
- YGG (запись результатов)
- HEIMDALL (эмиссия событий — TBD)
- Dali Core (получение orchestration команд — TBD, см. ADR-DA-008)

---

### 3.2 YGG (HoundArcade)

**Слой:** 1 — Data Layer
**Зрелость:** 🟢 **WORKING**
**Назначение:** Multi-model хранилище для lineage графа. Single source of truth для всех persistent lineage данных SEER Studio. Отделяется по роли от FRIGG (который unified state store).

**Технологический стек:**

| Категория | Технология | Версия | Статус |
|---|---|---|---|
| Database | ArcadeDB (multi-model: graph + document + key-value) | **`arcadedata/arcadedb:latest`** (26.3.2) | ✅ CONFIRMED |
| Query languages | Cypher (native OpenCypher), SQL, GraphQL, Gremlin, MQL | — | ✅ CONFIRMED |
| Deployment | Network mode (HoundArcade :2480 — **primary + production**) | 26.3.2 | ✅ WORKING |
| Deployment | Embedded (в Hound JVM — **только тесты**) | 25.12.1 | ⚠️ MIGRATE (C.0) |
| Schema version | v26 | — | ✅ CONFIRMED |

**Docker tag:** `arcadedata/arcadedb:latest` — production тянет latest stable на каждом image rebuild. Currently resolves to 26.3.2.

> **Operational note:** latest tag даёт свежие features и bug fixes автоматически, но снижает reproducibility builds. Для production deploys при нестабильности можно pin на конкретную версию (e.g. `26.3.2`) и использовать Dependabot/Renovate для tracking.

**Version policy (зафиксировано 12.04.2026):**
> **No mixed embedded/network ArcadeDB versions на одной БД.** HoundArcade network mode — 26.3.2. Hound embedded (тесты) — мигрирует на network mode в C.0. После C.0 только одна версия на весь проект.
>
> Вариант 1 (тесты → network mode) выбран. Вариант 2 (ANTLR shading для embedded 26.x) отклонён — техдолг. Вариант 3 (mixed 25+26) отклонён — при первом открытии 26.x сервером происходит авто-апгрейд схемы, откат невозможен.

**Schema v26 (текущая):**
- **15 vertex types**: DaliApplication, DaliService, DaliDatabase, DaliSchema, DaliTable, DaliColumn, DaliPackage, DaliRoutine, DaliStatement, DaliSession, DaliOutputColumn, DaliParameter, DaliVariable, DaliAtom, DaliJoin
- **4 document types**: TBD (точный список нужно уточнить)
- **32 edge types**: CONTAINS_TABLE, CONTAINS_COLUMN, CONTAINS_ROUTINE, CONTAINS_SCHEMA, HAS_COLUMN, READS_FROM, WRITES_TO, CALLS, DEPENDS_ON, HAS_SERVICE, HAS_DATABASE, USES_DATABASE, BELONGS_TO_APP, ... (всего 32)
- **Indexes**: UNIQUE на geoid, FULL-TEXT на names, COMPOSITE на (schema_name, table_name, column_name)

**Двойная установка:**
- **Embedded** в Hound JVM — для UC3 benchmark, UC4 offline bulk import, UC2a preview ⚠️ TBD
- **Network mode** на :2480 — **primary**, для UC1 scheduled harvest (REMOTE_BATCH), UC2b event-driven (REMOTE single), читатели SHUTTLE/ANVIL/MIMIR

**Функции:**
- Графовые traversals через native OpenCypher (в latest — 25× faster чем Cypher-to-Gremlin translation)
- Document storage (метаданные сессий, JSON payloads)
- Full-text indexing (для search в KNOT/MIMIR)
- Transactional writes
- Schema evolution через ArcadeDB migration
- **Parallel query execution** (post-upgrade) — SQL queries используют multiple CPU cores
- **72 built-in graph algorithms** (post-upgrade) — PageRank, centrality, community detection, pathfinding — потенциал для упрощения ANVIL
- **Materialized views** (post-upgrade) — pre-computed query results для HEIMDALL metrics dashboards
- **Built-in MCP Server** (post-upgrade) — потенциальное упрощение MIMIR (возможность использовать ArcadeDB как MCP tool source для Claude напрямую, см. Q29)
- **Graph Analytical View (GAV)** (26.3.2+) — CSR-based OLAP acceleration layer

**Embedded Server pattern:** ArcadeDB официально поддерживает одновременный embedded + server mode на одной БД через `ArcadeDBServer` class внутри application JVM — подтверждено в документации. Это разблокирует UC2a preview реализацию (см. Q31).

**Multi-database в одном server:** ArcadeDB поддерживает multiple databases в одном server instance — потенциально полезно для preview databases, saved views isolation и будущей multi-tenancy.

**Что нужно к October:**
- **C.0: тесты Hound → network mode** (~1-3 дня) — убрать embedded 25.12.1 из test scope, переключить на HoundArcade 26.3.2
- Regression тесты Cypher queries (SHUTTLE LineageService/ExploreService/SearchService) — проверить совместимость с native OpenCypher
- Regression тесты SQL queries (SearchService `String.format()`) — **совмещается с C.2.1 SQL injection fix**
- Performance baseline для 50K vertices (target <100ms на 5-hop traversal)
- Backup/snapshot strategy для `make demo-reset`

**Зависимости:** никаких — pure storage.

---

### 3.3 SHUTTLE

**Слой:** 4 — API Gateway (для VERDANDI)
**Зрелость:** 🟢 **WORKING** (read API в production, нужны mutations + WebSocket)
**Расположение:** `VERDANDI/SHUTTLE/` (~2.5K LOC Java)

**Назначение:** Backend GraphQL API service для VERDANDI. Обслуживает 9 read queries + 2 KNOT queries.

**Implementation language:** ✅ **Java 21**

**Технологический стек:**

| Категория | Технология | Версия | Статус |
|---|---|---|---|
| Язык | Java | 21 | ✅ CONFIRMED |
| Framework | Quarkus | 3.34.2 | ✅ CONFIRMED |
| Build | Gradle | 9 | ✅ CONFIRMED |
| GraphQL | SmallRye GraphQL | — | ✅ CONFIRMED |
| HTTP client | Quarkus REST client (reactive) | — | ✅ CONFIRMED |
| ArcadeDB access | HTTP REST → HoundArcade :2480 | — | ✅ CONFIRMED |
| Reactive | Mutiny | — | ✅ CONFIRMED |
| Auth | SeerIdentity (trusted headers от Chur) | — | ✅ CONFIRMED |
| Testing | JUnit 5 + Mockito (planned) | — | ⏳ PENDING |
| Port | 8080 | — | ✅ CONFIRMED |

**GraphQL Queries (9 + 2 KNOT):**

| Query | Service | Описание |
|---|---|---|
| `overview` | OverviewService | L1 — DaliSchema list with counts |
| `explore(scope)` | ExploreService | L2 — graph внутри schema/package/db/rid |
| `stmtColumns(ids)` | ExploreService | Column enrichment for statements |
| `lineage(nodeId)` | LineageService | Bidirectional 1-hop |
| `upstream(nodeId)` | LineageService | Incoming edges |
| `downstream(nodeId)` | LineageService | Outgoing edges |
| `expandDeep(nodeId, depth)` | LineageService | Multi-hop 1-10 |
| `search(query, limit)` | SearchService | Full-text по 12 Dali types |
| `me` | SeerIdentity | Username + role |
| `knotSessions` | KnotService | All parsing sessions |
| `knotReport(sessionId)` | KnotService | Full report |

**Что нужно к October:**
- Mutations (startParseSession, askMimir, saveView, deleteView)
- WebSocket Subscriptions для HEIMDALL events (через SmallRye GraphQL Subscriptions или Vert.x WebSocket)
- Rate limiting
- Server-side batching для events (50/100ms chunks)

**Зависимости:**
- HoundArcade (читает graph data)
- Dali Core (для startParseSession mutation — TBD)
- MIMIR (для askMimir mutation — TBD)
- HEIMDALL backend (для WebSocket Subscription stream)

---

### 3.4 Chur

**Слой:** 4 — BFF / API Gateway
**Зрелость:** 🟢 **WORKING** (~600 LOC)
**Расположение:** `Chur/`

**Назначение:** **Backend for Frontend (BFF)** для VERDANDI. НЕ просто JWT middleware — это полноценный gateway между browser и backend services.

**Implementation language:** ✅ **TypeScript + Node.js 22**

**Технологический стек:**

| Категория | Технология | Версия | Статус |
|---|---|---|---|
| Язык | TypeScript | 5.9 | ✅ CONFIRMED |
| Runtime | Node.js | 22 | ✅ CONFIRMED |
| Framework | Fastify | 4.28.1 | ✅ CONFIRMED |
| JWT | jose | 6.2.2 | ✅ CONFIRMED |
| Cookies | @fastify/cookie (httpOnly) | — | ✅ CONFIRMED |
| Auth provider | Keycloak 26.x (Direct Access Grants) | — | ✅ CONFIRMED |
| Rate limiting | In-memory (5 req/15 min на login) | — | ✅ CONFIRMED |
| CORS | manual hook (Fastify 4 compat) | — | ✅ CONFIRMED |
| WebSocket proxy | @fastify/websocket ^9 (Fastify 4 compat) | — | ✅ CONFIRMED |
| Testing | Vitest | — | ✅ CONFIRMED |
| Port | 3000 (dev) / 13000 (prod) | — | ✅ CONFIRMED |

**Endpoints (актуальные):**

| Route | Method | Auth | Описание |
|---|---|---|---|
| `/auth/login` | POST | — | Username/password → Keycloak DAG → sid cookie |
| `/auth/me` | GET | cookie | Текущий пользователь (auto-refresh если expired) |
| `/auth/refresh` | POST | cookie | Refresh access token |
| `/auth/logout` | POST | cookie | Удаление session + sid cookie |
| `/graphql` | POST | cookie | Proxy → SHUTTLE с X-Seer-User + X-Seer-Role |
| `/api/query` | POST | admin | Direct SQL/Cypher → ArcadeDB (admin only) |
| `/health` | GET | — | Health check |
| `/heimdall/health` | GET | — | Proxy → HEIMDALL backend health |
| `/heimdall/metrics/snapshot` | GET | admin | Proxy → HEIMDALL MetricsResource |
| `/heimdall/control/:action` | POST | admin | Proxy → HEIMDALL ControlResource (reset/snapshot/cancel) |
| `/heimdall/control/snapshots` | GET | admin | Proxy → HEIMDALL ControlResource |
| `/heimdall/ws/events` | WS | cookie | WebSocket proxy → HEIMDALL :9093/ws/events |
| `/heimdall/users` | GET/PUT | admin | Keycloak users list + block/unblock |

**Security model:**
- JWT хранится в httpOnly cookies (XSS-safe)
- Session store: in-memory `Map<sid, Session>` с mutex для refresh race
- Rate limiting на /auth/login (anti brute-force)
- Trusted headers downstream (private network assumption)

**Миграция на Quarkus — рассматривается, но НЕ принята:**
- См. отдельную дискуссию в разделе § 4 (открытые вопросы)
- Зависит от backend-стратегии (JVM-first vs polyglot)

**Что нужно к October:**
- Возможные обновления для поддержки HEIMDALL frontend (отдельная route или общий endpoint)
- Scope checks для admin actions (`aida:admin`, `aida:admin:destructive`)

---

### 3.5 VERDANDI

**Слой:** 6 — User Applications (Frontend)
**Зрелость:** 🟢 **WORKING** (~19.7K LOC, 119 TS files, 76 components)
**Расположение:** `verdandi/`

**Назначение:** Главное user-facing приложение SEER Studio. Содержит несколько ипостасей: LOOM (graph canvas), KNOT (inspector), будущие ANVIL UI и MIMIR Chat.

**Implementation language:** ✅ **TypeScript 5.9 + React 19.2.4**

**Технологический стек:**

| Категория | Технология | Версия | Статус |
|---|---|---|---|
| Framework | React | 19.2.4 | ✅ CONFIRMED |
| Build | Vite | 8.0.1 | ✅ CONFIRMED |
| Language | TypeScript | 5.9 | ✅ CONFIRMED |
| Graph rendering | @xyflow/react (React Flow) | 12.10.2 | ✅ CONFIRMED |
| Layout engine | ELK.js (Web Worker) | 0.11.1 | ✅ CONFIRMED |
| State management | Zustand (13 slices) | 5.0.12 | ✅ CONFIRMED |
| Data fetching | TanStack React Query | 5.96.2 | ✅ CONFIRMED |
| GraphQL client | graphql-request | 7.4.0 | ✅ CONFIRMED |
| Routing | react-router-dom | 7 | ✅ CONFIRMED |
| Styling | Tailwind CSS | 4.2.2 | ✅ CONFIRMED |
| Form validation | Zod + react-hook-form | 4.3.6 | ✅ CONFIRMED |
| i18n | i18next + react-i18next | 26.0.3 / 17.0.2 | ✅ CONFIRMED (~420 keys EN/RU) |
| UI kit | shadcn/ui + Lucide icons | — | ✅ CONFIRMED |
| Testing | Vitest + Playwright | 4.1.3 / 1.50.0 | ✅ CONFIRMED |
| Package manager | npm | — | ✅ CONFIRMED |
| Port | 5173 (dev), 13000 (prod) | — | ✅ CONFIRMED |

**13 Zustand slices** (loomStore):
navigationSlice, l1Slice, selectionSlice, filterSlice, expansionSlice, visibilitySlice, viewportSlice, themeSlice, undoSlice, persistSlice, searchSlice, inspectorSlice + отдельный authStore

**Ипостаси внутри VERDANDI:**

| Ипостась | Зрелость | Что делает |
|---|---|---|
| **LOOM** | 🟢 WORKING | 3-уровневый граф (L1 Overview → L2 Explore → L3 Lineage), 5 палитр, темы dark/light, profile settings, command palette, search, undo/redo |
| **KNOT** | 🟢 WORKING | Inspector с structure breakdown, statistics, query types, drill-down |
| **ANVIL UI** | 🔵 NEW | Будет добавлен — view для impact analysis результатов |
| **MIMIR Chat** | 🔵 NEW | Будет добавлен — natural language interface |

**Дизайн система:**
- 5 palettes: Amber Forest, Lichen, Slate, Juniper, Warm Dark
- Dark/Light themes
- 6 UI fonts + 5 mono fonts
- Profile settings persist в localStorage
- WCAG AA contrast

**Что нужно к October:**
- Scale к 5-10K nodes через виртуализацию React Flow
- Level-of-detail rendering
- Presentation mode для проектора
- ANVIL UI integration
- MIMIR Chat integration
- WebSocket client для HEIMDALL events (если нужен в VERDANDI — TBD)

**Зависимости:**
- Chur (auth + GraphQL proxy)
- SHUTTLE (через Chur — graph data)

---

### 3.6 HEIMDALL

**Слой:** 7 — Cross-cutting Observability + Admin Control Panel **всей AIDA**
**Зрелость:** 🟢 **WORKING** (12–15.04.2026)

**Назначение:** Admin Control Panel для всей AIDA платформы. Объединяет две роли:
1. **Generic admin UI всей AIDA** — health dashboards, system overview, user management, audit log
2. **Control panel для оркестрации Dali** — sessions view, workers, throughput, control commands (reset/replay/snapshot)

**Архитектура — split на 2 независимых компонента:**

#### 3.6.1 HEIMDALL frontend

**Тип:** Отдельный React проект (не часть VERDANDI)

**Причины отдельности (зафиксированные):**
- Independent failure domain — HEIMDALL должен работать когда VERDANDI down (критично для admin tool)
- Отдельный security boundary
- Разная аудитория: operators / DevOps / on-call vs end users
- Разный технологический фокус: dashboards / real-time vs graph rendering
- Разный roadmap и release cycle
- Post-HighLoad возможность standalone deployment

**Технологический стек (подтверждён кодовой базой):**

| Категория | Технология | Версия | Статус |
|---|---|---|---|
| Framework | React | 19.1.0 | ✅ WORKING |
| Build | Vite | 8.x | ✅ WORKING |
| Language | TypeScript | 5.8.3 | ✅ WORKING |
| State | Zustand | 5.0.3 | ✅ WORKING |
| Data fetching | React hooks + fetch | — | ✅ WORKING |
| WebSocket | **Native WebSocket** `/ws/events` | — | ✅ Q13 CLOSED |
| Charting | **Nivo** (`@nivo/line`, `@nivo/pie`) | 0.99.0 | ✅ Q12 CLOSED |
| Virtualization | react-virtuoso | 4.12.5 | ✅ WORKING |
| Styling | CSS variables (monospace dark theme) | — | ✅ WORKING |
| Routing | react-router-dom | 7.5.1 | ✅ WORKING |
| MF | Module Federation (remote) | 1.14.2 | ✅ WORKING |
| Port | 5174 (dev MF remote) / 25174 (prod) | — | ✅ WORKING |

**Pages (реализованы):**
`DashboardPage` · `EventStreamPage` · `ControlsPage` · `ServicesPage` · `UsersPage` · `DaliPage` · `DocsPage` · `LoginPage`

**Module Federation:** HEIMDALL frontend = **MF remote**, экспортирует `./App`. Загружается через Shell host.

**Shared code:** через `aida-shared` (file:../../packages/aida-shared).

#### 3.6.2 HEIMDALL backend ✅ WORKING (HB1 Quarkus)

**Тип:** Отдельный Quarkus сервис (HB1 — Q3 CLOSED)

**Технологический стек:**

| Категория | Технология | Версия | Статус |
|---|---|---|---|
| Framework | Quarkus | 3.34.2 | ✅ CONFIRMED |
| Language | Java | 21 | ✅ CONFIRMED |
| Reactive | Mutiny + BroadcastProcessor | — | ✅ CONFIRMED |
| WebSocket | Vert.x WebSocket (Quarkus native) | — | ✅ CONFIRMED |
| Storage | FRIGG (ArcadeDB :2481) для snapshots | — | ✅ CONFIRMED |
| Port | 9093 | — | ✅ CONFIRMED |

**REST Endpoints (реализованы):**

| Endpoint | Метод | Описание |
|---|---|---|
| `/events` | POST | Accept single event (fire-and-forget, 202 Accepted) |
| `/events/batch` | POST | Accept event batch |
| `/metrics/snapshot` | GET | Live counters (events/sec, atoms, resolution rate) |
| `/control/reset` | POST | Reset ring buffer + metrics aggregators |
| `/control/snapshot` | POST | Persist current state в FRIGG |
| `/control/cancel/{sessionId}` | POST | Cancel running Dali session |
| `/control/snapshots` | GET | List saved snapshots from FRIGG |
| `/api/prefs/{sub}` | GET/PUT | User preferences in FRIGG |
| `/docs` | GET | List .md files from docs/ (dev only) |
| `/docs/{path}` | GET | Serve .md file content (path-traversal protected) |
| `ws://:9093/ws/events` | WebSocket | Live stream: cold replay (200 events) + real-time + optional filter |

**Функции:**
- Ring buffer 10K events in-memory (ADR-DA-003)
- Metrics aggregation (counters, resolution rate, throughput)
- WebSocket broadcast (BroadcastProcessor, multi-subscriber)
- Snapshot persist → FRIGG (ArcadeDB)
- CORS: разрешены Chur origins + dev ports
- Docs viewer (path-traversal guard, dev volume mount)

**HEIMDALL events иерархия:**

```
Level 1 — Orchestration (от Dali, для главного dashboard view):
  dali.session_started
  dali.workers_dispatched  { count, dialect }
  dali.worker_assigned     { worker_id, file_count }
  dali.results_collected   { worker_id, atoms }
  dali.persistence_completed { session_id, vertices, edges }
  dali.session_completed   { resolution_rate, total_atoms }

Level 2 — Execution detail (от Hound, MIMIR, ANVIL — для drill-down):
  hound.file_parsing_started
  hound.atom_extracted
  mimir.tool_call_started
  anvil.traversal_progress
  ...
```

**Controls (обратный канал HEIMDALL → Dali):**
- Reset state
- Snapshot demo state
- Replay recorded session
- Cancel running orchestration

**ADR-DA-001 пересматривается:** "HEIMDALL in-process module" больше не валидно. Теперь HEIMDALL — отдельный сервис, потому что (а) frontend отдельный, (б) admin scope шире чем один компонент.

---

### 3.6.3 Shell (Module Federation Host) ✅ WORKING

**Тип:** Отдельный React + Vite проект (`frontends/shell/`)
**Зрелость:** 🟢 WORKING

**Назначение:** AIDA platform entrypoint. Module Federation **host** — оркеструет загрузку VERDANDI и HEIMDALL frontend как MF remotes. Содержит общий navigation header, shared Zustand context state, routing.

**Технологический стек:**

| Категория | Технология | Версия | Статус |
|---|---|---|---|
| Framework | React | 19.1.0 | ✅ CONFIRMED |
| Build | Vite | 8.x | ✅ CONFIRMED |
| Module Federation | @module-federation/vite | 1.14.2 | ✅ CONFIRMED |
| State | Zustand (shellStore: shared context) | 5.x | ✅ CONFIRMED |
| Routing | react-router-dom | 7.x | ✅ CONFIRMED |
| Shared singletons | react, react-dom, react-router-dom, zustand, aida-shared | — | ✅ CONFIRMED (eager: true) |
| Port | 5175 (dev MF host) / 25175 (prod) | — | ✅ CONFIRMED |

**Remotes:**
- `verdandi` → `:5173` (dev) / `:15173` (prod)
- `heimdall-frontend` → `:5174` (dev) / `:25174` (prod)

**Dev proxy:** `/graphql` → SHUTTLE (:8080), `/auth` → Chur (:3000)

---

### 3.7 Dali Core

**Слой:** 2 — Backend Services
**Зрелость:** 🔵 NEW (decision fixed)
**Назначение:** Parsing orchestrator + system configuration. Управляет lifecycle парсинг-сессий.

**Use cases:**

| UC | Описание |
|---|---|
| **UC1** | Scheduled harvest (регламент) — раз в N часов извлекает SQL из БД, создаёт complex job из подзадач (по одной на БД) |
| **UC2a** | On-demand by user request (через UI → SHUTTLE → Dali) |
| **UC2b** | Event-driven by external update (CDC, webhook, file watcher) |
| **UC3** | Workload management — ограниченный pool Hound workers, очередь, балансировка, retry, persistence логов |
| **UC4** | Complex job tracking — job из N подзадач, прогресс «5 из 12 БД» |

**Hound сам пишет в YGG в зависимости от настроек переданных Dali.** Dali — НЕ write proxy, Dali оркеструет и передаёт config.

**Implementation:** ✅ **Java 21 + Quarkus + JobRunr** (decision fixed)

**Технологический стек:**

| Категория | Технология | Версия | Статус |
|---|---|---|---|
| Язык | Java | 21 | ✅ CONFIRMED |
| Framework | Quarkus | 3.34.2 (same as SHUTTLE) | ✅ CONFIRMED |
| Build | Gradle | 9 | ✅ CONFIRMED |
| Job processor | JobRunr (Open Source) + `quarkus-jobrunr` extension | latest | ✅ CONFIRMED |
| Job persistence | **FRIGG (ArcadeDB) через custom StorageProvider** | — | ✅ CONFIRMED (Вариант A) |
| Hound integration | **Прямой in-JVM call** через Gradle `implementation project(':hound')` | — | ✅ CONFIRMED |
| SHUTTLE integration | **HTTP REST** (Quarkus REST client + shared Gradle module `dali-models`) | — | ✅ CONFIRMED (Вариант A) |
| Gradle structure | В том же multi-module проекте что SHUTTLE, **но отдельный процесс** | — | ✅ CONFIRMED |
| Cron scheduling | JobRunr `scheduleRecurrently()` | — | ✅ CONFIRMED |
| Retry / backoff | JobRunr `@Job(retries = N)` | — | ✅ CONFIRMED |
| Concurrency | JobRunr ThreadPoolWorker (configurable) | — | ✅ CONFIRMED |
| HEIMDALL events | CDI events / SmallRye Reactive Messaging | — | 🔬 PROPOSED |
| Health / metrics | Quarkus Health + Micrometer (через JobRunr extension) | — | ✅ CONFIRMED |
| Dev dashboard | JobRunr встроенный web UI (для observability на этапе разработки) | — | ✅ CONFIRMED |
| Testing | JUnit 5 + Quarkus Test | — | 🔬 PROPOSED |

**Объём (estimate):** ~600-800 LoC бизнес-логики + ~200-400 LoC custom ArcadeDB StorageProvider

**Ключевые архитектурные решения:**

1. **Прямой in-JVM вызов Hound** — через `implementation project(':hound')` в Gradle. Zero serialization, zero network, zero subprocess overhead. Это главное преимущество Quarkus+JobRunr выбора.

2. **SHUTTLE → Dali = HTTP REST** (Вариант A, зафиксировано) — Dali и SHUTTLE отдельные процессы в одном Gradle multi-module проекте. Связь через Quarkus REST client. Типы через shared module `dali-models`. Это даёт:
   - Independent failure domain (Dali может упасть — SHUTTLE read API работает)
   - Independent deployment и scaling
   - Единый механизм вызова Dali для всех клиентов (SHUTTLE + HEIMDALL)
   - JobRunr один owner process (Dali), нет конфликтов queue ownership

3. **FRIGG как JobRunr persistence** (Вариант A, зафиксировано) — custom `StorageProvider` для ArcadeDB. JobRunr jobs, state, history живут в том же ArcadeDB instance что saved views и user preferences. Единый persistence layer для всего SEER Studio state.

**Покрытие требований через JobRunr:**

| # | Требование | Реализация в JobRunr |
|---|---|---|
| R1 | Scheduled (cron) | `BackgroundJob.scheduleRecurrently("nightly-harvest", "0 2 * * *", ...)` |
| R2 | Event-driven | `BackgroundJob.enqueue()` из любого event handler |
| R3 | On-demand REST | Quarkus REST endpoint + `BackgroundJob.enqueue()` |
| R4 | Worker pool concurrency | Configurable ThreadPoolWorker |
| R5 | Job queue + priorities | Built-in priority queues + dashboard |
| R6 | Retry with backoff | `@Job(retries = 3)` + exponential backoff |
| R7 | Persistent state | SQLite/Postgres storage adapter, recovery после рестарта |
| R8 | Complex jobs (parent → N subtasks) | Job listeners + parent-child relationships |
| R9 | Dynamic subtasks (runtime N) | Один job enqueues N других в runtime |
| R10 | Progress tracking «5 из 12» | `JobContext.progress()` + dashboard |
| R11 | Передача config | Jobs принимают serializable parameters |
| R12 | События в HEIMDALL | CDI events внутри job методов |
| R13 | Control commands от HEIMDALL | JobRunr API: delete, requeue, schedule из любого места |
| R14 | Запуск Hound | Прямой in-JVM вызов |
| R15 | Quarkus экосистема | Native через `quarkus-jobrunr` |

**Все 15 требований покрыты библиотекой.**

**Отклонены при выборе:**
- ❌ Prefect 2 (Python) — изолированный Python остров, subprocess/gRPC проблема для интеграции с Java Hound
- ❌ Custom Quarkus (~1500 LoC) — JobRunr дает то же самое короче и с готовым dashboard
- ❌ Python custom (asyncio) — переписывание Prefect руками
- ❌ Go + goroutines — нет Go-экспертизы в команде, плюс полиглот overhead
- ❌ Apache Airflow — heavy footprint, DAG-first не fit on-demand, дублирует HEIMDALL UI
- ❌ Dagster — asset-centric не fit для orchestration use case
- ❌ Temporal — категорически overkill, отдельный server cluster
- ❌ Conductor / Camunda 8 — server-based, heavy
- ❌ Copper / nFlow / Camunda 7 — Java embedded, но требуют отдельную DB

**Функции (планируемые):**
- Принимать запросы (scheduled / on-demand / event-driven)
- Извлекать SQL скрипты из источников (JDBC / file system / API — TBD, см. Q23)
- Создавать complex job из подзадач (parent → N children)
- Распределять подзадачи между Hound workers (in-JVM thread pool)
- Job queue с priorities, retry, persistent state — всё через JobRunr
- Передавать `HoundConfig` каждому worker'у (target schema, dialect, options)
- Tracking прогресса complex job через `JobContext.progress()`
- Эмиссия orchestration events (Level 1) в HEIMDALL
- Приём control commands от HEIMDALL (reset/replay через JobRunr API)
- Хранение системной конфигурации (Quarkus `application.properties` + БД)

**Известные риски:**
- **Quarkus expertise частичная** — SHUTTLE написан, но было сложно. Вторая итерация Quarkus будет легче первой, но первые недели Dali пойдут медленнее.
- **JobRunr Pro features** — не использовать для October scope (Pro = €999/год). Open Source покрывает все 15 требований.
- **Custom ArcadeDB StorageProvider maintenance** — следить за updates JobRunr API между версиями. First version ~1-1.5 недели; maintenance — периодически когда JobRunr выпускает новые версии.
- **ArcadeDB upgrade блокирующий** — C.0 upgrade выполняется перед Dali engineering. JobRunr custom adapter пишется сразу для новой version (не legacy code).

---

### 3.8 MIMIR

**Слой:** 3 — AI Orchestration
**Зрелость:** 🔵 NEW
**Назначение:** LLM orchestrator с algorithms-as-tools pattern. Бизнес-логика в ANVIL/Hound, MIMIR только маршрутизирует.

**Парный модуль:**
- **Frontend**: MIMIR Chat — компонент внутри VERDANDI (natural language interface)
- **Backend**: MIMIR backend — отдельный сервис

**Implementation language:** ⏳ PENDING (зависит от backend стратегии)
- Изначально предлагался Python (Anthropic SDK first-class)
- Может быть Java/Quarkus если выберется JVM-first стратегия

**Технологический стек:**

| Категория | Технология | Статус |
|---|---|---|
| LLM tier 1 | Anthropic Claude Sonnet (remote) | ✅ CONFIRMED |
| LLM tier 2 | Qwen via Ollama (local) | ✅ CONFIRMED |
| LLM tier 3 | Cached responses (JSON file) | ✅ CONFIRMED |
| Tool framework | Custom ~300 LoC (не LangChain — ADR-DA-002) | ✅ CONFIRMED |
| Conversation state | In-memory dict (demo scope) | ✅ CONFIRMED |
| Cache storage | JSON file | 🔬 PROPOSED |

**5 tools (October scope):**
- query_lineage → ANVIL/YGG
- find_impact → ANVIL
- search_nodes → SHUTTLE GraphQL
- get_procedure_source → YGG
- count_dependencies → YGG

---

### 3.9 ANVIL

**Слой:** 2 — Backend Algorithms (+ Frontend часть в VERDANDI)
**Зрелость:** 🔵 NEW
**Назначение:** Pure graph algorithm для impact analysis. Главный «tool» для MIMIR.

**Парный модуль:**
- **Frontend (ANVIL UI)**: компонент внутри VERDANDI — view для impact results
- **Backend**: отдельный сервис (большой) с graph algorithms

**Implementation language backend:** ⏳ PENDING (зависит от backend стратегии)

**Функции backend:**
- `find_downstream_impact(node_id, max_depth)` — что сломается при изменении
- `find_upstream_sources(node_id, max_depth)` — откуда приходят данные
- Pure Cypher queries на YGG (без state, без LLM)
- Эмиссия событий в HEIMDALL
- Возврат структурированного JSON для tool calling

---

### 3.10 FRIGG

**Слой:** 1 — Data Layer
**Зрелость:** 🔵 NEW
**Назначение:** **Unified SEER Studio state store** — отдельный ArcadeDB instance, хранящий ВСЁ state кроме lineage данных. Разделяется по роли от YGG (который хранит lineage).

**Scope (расширенный после decision 11.04.2026):**
- **Saved LOOM views** — пользовательские сохранённые виды графа (первоначальный use case)
- **User preferences** — palette, theme, language, UI settings
- **JobRunr jobs / state / history** — через custom ArcadeDB StorageProvider (см. ниже)
- **Potential future**: URD state (versioning), audit log, session history — когда будут

**Технологический стек:**

| Категория | Технология | Статус |
|---|---|---|
| Storage | Отдельный ArcadeDB instance (`arcadedata/arcadedb:latest`) | ✅ CONFIRMED |
| API for views / prefs | Через SHUTTLE GraphQL mutations / queries | ✅ CONFIRMED |
| API for JobRunr state | Custom `StorageProvider` implementation для JobRunr | 🔵 NEW (в scope Dali engineering) |
| Schema | Отдельная от YGG; v1 для saved views + JobRunr tables | 🔬 PROPOSED |

**Custom ArcadeDB StorageProvider для JobRunr:**

JobRunr из коробки поддерживает PostgreSQL, MySQL, SQLite, MongoDB, ElasticSearch, Redis — но **не ArcadeDB**. Для использования FRIGG (ArcadeDB) как JobRunr storage нужна custom реализация `StorageProvider` interface.

**Архитектурное решение:** Вариант A (custom adapter сразу) — осознанный выбор в пользу архитектурной чистоты над экономией времени. Один persistence backend для всего SEER Studio state вместо смешения ArcadeDB (views) + SQLite (jobs).

**Оценка работы:**
- ~200-400 LoC для первой версии adapter
- Реализовать JobRunr `StorageProvider` interface (jobrunr_jobs, jobrunr_recurring_jobs, jobrunr_backgroundjobservers, jobrunr_metadata tables)
- Тесты на совместимость с JobRunr test suite
- **Effort: ~1-1.5 недели** — включается в Dali engineering scope
- Maintenance burden: следить за updates JobRunr API между версиями

**Плюсы единого persistence:**
- Один backend tool, одна экспертиза (ArcadeDB)
- Единая backup strategy
- Консистентность с YGG technology choice
- Меньше процессов в deployment
- FRIGG логически становится «SEER Studio state store» — потенциально переиспользуется для URD/SKULD когда до них дойдёт

**Keycloak integration:** уже работает через realm `seer`. FRIGG получает user_id из JWT через Chur → SHUTTLE (для views CRUD).

**Deployment:** отдельный ArcadeDB container в docker-compose, свой port (e.g. :2481), свой volume для data persistence.

**Зависимости:** никаких — pure storage.

---

### 3.11 Object Storage — S3 (archives)

**Слой:** 1 — Data Layer
**Зрелость:** 🔵 PLANNED (cloud deployment)
**Назначение:** Хранение загружаемых SQL-архивов (ZIP + .sql файлы) для Dali File Upload. Заменяет `java.io.tmpdir` подход из MVP при переходе в облако.

**Технологический стек:**

| Среда | Технология | Port / Endpoint |
|---|---|---|
| Dev / Docker Compose | MinIO (`minio/minio:latest`) | `:9000` (S3 API), `:9001` (Console UI) |
| Prod (YC) | YC Object Storage | `storage.yandexcloud.net` (S3-compatible) |

Обе среды используют **S3-compatible API** — код Dali не меняется при смене бэкенда.

**Lifecycle архивов:**

| Событие | Действие |
|---|---|
| Загрузка файла | PUT в `uploads/{sessionId}/{filename}` |
| ParseJob завершён (COMPLETED / FAILED) | DELETE объекта сразу |
| Failsafe (ParseJob завис / не стартовал) | S3 lifecycle rule: auto-delete через 7 дней |

**Quota и мониторинг:**

Dali записывает в FRIGG метаданные каждого upload-а (`UploadUsage` vertex):

```
session_id  | file_name | file_size_bytes | uploaded_at | deleted_at | status
```

HEIMDALL backend читает из FRIGG и отдаёт:
- `GET /control/storage/usage` — суммарно + по сессиям
- `PUT /control/storage/quota` — установить лимит (bytes)
- `DELETE /control/storage/archives` — принудительная очистка

HEIMDALL frontend — новая вкладка **Storage** на странице Dashboard:
- Gauge: использовано / квота
- Таблица: последние N загрузок с размером, статусом, временем
- Кнопка «Очистить старые»

**Docker Compose (dev):**

```yaml
minio:
  image: minio/minio:latest
  command: server /data --console-address ":9001"
  ports:
    - "127.0.0.1:9000:9000"
    - "127.0.0.1:9001:9001"
  environment:
    MINIO_ROOT_USER: ${MINIO_USER:-aida}
    MINIO_ROOT_PASSWORD: ${MINIO_PASSWORD:-aidapassword}
  volumes:
    - minio_data:/data
  healthcheck:
    test: ["CMD", "curl", "-f", "http://localhost:9000/minio/health/live"]
    interval: 10s
    timeout: 5s
    retries: 5
```

**Dali env:**

```env
S3_ENDPOINT=http://minio:9000       # prod: https://storage.yandexcloud.net
S3_BUCKET=dali-uploads
S3_ACCESS_KEY=aida
S3_SECRET_KEY=aidapassword
S3_REGION=us-east-1                 # MinIO: любой; YC: ru-central1
```

**SDK:** AWS SDK for Java v2 (`software.amazon.awssdk:s3`) — совместим с любым S3 endpoint.

**Зависимости:** FRIGG (запись usage), Dali (клиент S3), HEIMDALL backend (quota API).

**Связанные документы:** `docs/plans/pending/DALI_FILE_UPLOAD_SPEC.md`, решение #22.

---

### 3.12 URD (planned)

**Слой:** 6 — User Applications + парный backend
**Зрелость:** 🔵 PLANNED (post-HighLoad)
**Назначение:** Time-travel / history / audit trail для lineage.

**Парный модуль:**
- **URD frontend**: отдельный React проект (как HEIMDALL frontend)
- **URD backend**: отдельный backend сервис
- **URD middleware**: application layer (TBD)

**Все детали:** ⚠️ TBD до начала разработки.

**Технологический стек:** ожидается что frontend будет на том же стеке что VERDANDI/HEIMDALL frontend (React 19 + Vite + Tailwind).

---

### 3.12 SKULD (planned)

**Слой:** 6 — User Applications + парный backend
**Зрелость:** 🔵 PLANNED (post-HighLoad)
**Назначение:** Flow design / future state / validation / proposals.

**Парный модуль:**
- **SKULD frontend**: отдельный React проект
- **SKULD backend**: отдельный backend сервис
- **SKULD middleware**: application layer (TBD)

**Все детали:** ⚠️ TBD.

---

### 3.13 Middleware modules (planned)

**Слой:** 5 — Middleware / Application Layer
**Зрелость:** 🔵 PLANNED
**Статус:** ⚠️ **TBD — список модулей и их назначение**

**Контекст:** Для работы URD и SKULD понадобятся отдельные application layer модули, помимо SHUTTLE (который обслуживает VERDANDI lineage view). Это могут быть:
- Отдельные BFF сервисы для URD frontend / SKULD frontend
- Версионирование / event sourcing layer для URD
- Validation / proposal engines для SKULD
- Cross-product integration layer

**Имена и назначение каждого middleware module — ожидаются от owner SEER Studio.**

---

### 3.14 Keycloak (AIDA-уровень)

**Слой:** AIDA Infrastructure
**Зрелость:** 🟢 WORKING

**Назначение:** Auth provider для всей AIDA платформы.

**Технологический стек:**

| Категория | Значение | Статус |
|---|---|---|
| Версия | Keycloak 26.2 | ✅ CONFIRMED |
| Realm | `seer` | ✅ CONFIRMED |
| Client | `aida-bff` (был `verdandi-bff`) | 🟡 RENAME PENDING |
| Grant flow | Direct Access Grants (для Chur BFF) | ✅ CONFIRMED |
| Scopes | TBD (`aida:admin`, `aida:admin:destructive`, `seer:read`, `seer:write`) | 🔬 PROPOSED |
| Roles | viewer, editor, admin | ✅ CONFIRMED |
| Claim | `seer_role` (через protocol mapper) | ✅ CONFIRMED |

**Migration: `verdandi-bff` → `aida-bff`:**
- Будущепроверяемое имя
- Один client обслуживает все AIDA frontends (VERDANDI, HEIMDALL, URD, SKULD, будущие)
- HEIMDALL проверяет дополнительные scopes (`aida:admin`, `aida:admin:destructive`)
- Migration cost: ~1 час разработки + тесты + координация deployment

**Что нужно к October:**
- Переименование client
- Добавление новых scopes (`aida:admin`, `aida:admin:destructive`)
- Документация role/scope матрицы

---

### 3.15 CI/CD + Docker Compose (AIDA-уровень)

**Слой:** AIDA Infrastructure
**Зрелость:** 🟢 WORKING

**CI/CD:**

| Категория | Технология | Статус |
|---|---|---|
| CI workflow | GitHub Actions `ci.yml` (lint/test/build для всех 3 сервисов) | ✅ CONFIRMED |
| CD workflow | GitHub Actions `cd.yml` (Docker build → GHCR → SSH deploy) | ✅ CONFIRMED |
| Container registry | GHCR (GitHub Container Registry) | ✅ CONFIRMED |
| Deployment target | `/opt/seer-studio` via SSH | ✅ CONFIRMED |
| Notifications | Telegram + rollback | ✅ CONFIRMED |

**Docker Compose:**

| Файл | Назначение | Статус |
|---|---|---|
| `docker-compose.yml` | dev | ✅ CONFIRMED |
| `docker-compose.prod.yml` | prod | ✅ CONFIRMED |

**Dockerfiles:** 7 сервисов — Hound (embedded in SHUTTLE build), SHUTTLE, Chur, VERDANDI, HEIMDALL-frontend, Shell, Dali.

**CI/CD (актуальные jobs — sprint cicd-docker-polish-apr15):**

| Job | Что проверяет |
|---|---|
| `hound` | test + build (Gradle) |
| `shuttle` | test + build (Gradle) |
| `heimdall-backend` | test + build (Gradle) |
| `dali` | test + build (Gradle) |
| `dali-models` | build (Gradle, upstream lib) |
| `chur` | tsc --noEmit + npm run build |
| `verdandi` | npm run build (= tsc -b + vite build) |
| `heimdall-frontend` | npm run build |
| `shell` | npm run build |

Триггеры: `master`, `feature/**`, `fix/**` + `workflow_dispatch`.
Permissions: `contents: read`. Timeout: 15 min (Gradle) / 10 min (Node).

**Сетевая топология (порты):**

| Сервис | Dev port | Prod port | Описание |
|---|---|---|---|
| HoundArcade (YGG) | 12480 | 2480 | ArcadeDB network mode |
| FRIGG | 12481 | 2481 | ArcadeDB (state store) |
| Keycloak | 18180 | 8180 | auth (127.0.0.1 bound) |
| SHUTTLE | 18080 | 8080 | GraphQL API |
| Chur | 13000 | 3000 | BFF |
| VERDANDI | 15173 | 13000 | MF remote |
| HEIMDALL backend | 9093 | 9093 | Quarkus admin API |
| HEIMDALL frontend | 25174 | 25174 | MF remote |
| Shell | 25175 | 25175 | MF host |
| Dali | 19090 | 9090 | parse orchestrator |

**Network:** `aida_net` (переименовано из `verdandi_net` — Q18 ✅)

---

## 4. Технологический стек overview

Сводная таблица всех технологий по категориям.

### 4.1 Backend languages

| Язык | Где используется | Статус |
|---|---|---|
| **Java 21 + Quarkus 3.34.2** | Hound, SHUTTLE, HEIMDALL backend, Dali Core | ✅ CONFIRMED (JVM-first де-факто) |
| **TypeScript + Node.js 22** | Chur (BFF — исключение, не мигрирует до HighLoad) | ✅ CONFIRMED |
| **TBD** | MIMIR, ANVIL backend | ⏳ PENDING (Q8/Q9, зависит от ArcadeDB MCP Q29/Q30) |

### 4.2 Frontend languages

| Язык | Где используется | Статус |
|---|---|---|
| **TypeScript 5.9 + React 19.2.4** | VERDANDI, HEIMDALL frontend (planned), URD/SKULD frontends (planned) | ✅ CONFIRMED |

### 4.3 Storage

| Технология | Где используется | Версия | Статус |
|---|---|---|---|
| ArcadeDB (YGG/HoundArcade) | Lineage graph, SHUTTLE reads, Hound writes | **26.3.2** (latest) | ✅ CONFIRMED |
| ArcadeDB (FRIGG) | HEIMDALL snapshots, user prefs, JobRunr state | **26.3.2** (latest) | ✅ CONFIRMED |
| File system | Hound (исходные SQL) + MIMIR cache | — | ✅ CONFIRMED |

### 4.4 Frontend stack (VERDANDI и будущие frontends)

| Категория | Технология | Версия | Статус |
|---|---|---|---|
| Framework | React | 19.2.4 | ✅ |
| Build | Vite | 8.0.1 | ✅ |
| Graph rendering | @xyflow/react (React Flow) | 12.10.2 | ✅ |
| Layout | ELK.js (Web Worker) | 0.11.1 | ✅ |
| State | Zustand | 5.0.12 | ✅ |
| Data fetching | TanStack Query | 5.96.2 | ✅ |
| GraphQL client | graphql-request | 7.4.0 | ✅ |
| Routing | react-router-dom | 7 | ✅ |
| Styling | Tailwind CSS | 4.2.2 | ✅ |
| Validation | Zod + react-hook-form | 4.3.6 | ✅ |
| i18n | i18next + react-i18next | 26.0.3 / 17.0.2 | ✅ |
| UI kit | shadcn/ui + Lucide | — | ✅ |
| Testing (unit) | Vitest | 4.1.3 | ✅ |
| Testing (E2E) | Playwright | 1.50.0 | ✅ |
| Package manager | npm | — | ✅ |

### 4.5 Backend frameworks

| Технология | Где используется | Версия | Статус |
|---|---|---|---|
| Quarkus | SHUTTLE, HEIMDALL backend, Dali Core | 3.34.2 | ✅ |
| SmallRye GraphQL | SHUTTLE | — | ✅ |
| Mutiny + BroadcastProcessor | SHUTTLE (HeimdallEmitter), HEIMDALL backend | — | ✅ |
| Vert.x WebSocket | HEIMDALL backend | — | ✅ |
| Fastify | Chur | 4.28.1 | ✅ |
| @fastify/websocket | Chur (HEIMDALL WS proxy) | ^9 | ✅ |
| jose (JWT) | Chur | 6.2.2 | ✅ |
| Gradle | Hound + SHUTTLE + HEIMDALL backend + Dali | 9 | ✅ |
| ANTLR4 | Hound | 4.13.1 | ✅ |
| JUnit 5 | Hound (24 test files) | — | ✅ |
| JobRunr | Dali Core | Open Source | ✅ (decision confirmed) |

### 4.6 Auth / Security

| Технология | Назначение | Статус |
|---|---|---|
| Keycloak 26.2 | Auth provider (realm: seer) | ✅ |
| JWT | Token format | ✅ |
| Direct Access Grants | Chur ↔ Keycloak flow | ✅ |
| httpOnly cookies | Session storage | ✅ |
| Trusted headers | Chur → SHUTTLE downstream | ✅ |

### 4.7 LLM / AI

| Технология | Tier | Статус |
|---|---|---|
| Anthropic Claude Sonnet | tier 1 (primary) | ✅ |
| Qwen via Ollama (local) | tier 2 (fallback) | ✅ |
| Cached responses (JSON) | tier 3 (ultimate safety) | ✅ |

### 4.8 DevOps / Infrastructure

| Технология | Версия | Статус |
|---|---|---|
| Docker Compose (dev + prod) | — | ✅ |
| GitHub Actions (ci.yml + cd.yml) | — | ✅ |
| GHCR (GitHub Container Registry) | — | ✅ |
| SSH deploy + Telegram notifications | — | ✅ |

### 4.9 Observability

| Технология | Где используется | Статус |
|---|---|---|
| HEIMDALL (custom in-process) | Demo observability + admin | 🔵 NEW |
| Prometheus / Grafana / Loki / Jaeger | Production observability | ❌ DEFERRED (post-HighLoad) |

---

## 5. Открытые вопросы

Полный список того что не решено, сгруппирован по приоритету.

### 5.0 Закрытые решения (для reference)

| # | Вопрос | Решение | Где зафиксировано |
|---|---|---|---|
| ✅ Q2 | Dali Core implementation | **Java 21 + Quarkus 3.34.2 + JobRunr (Open Source)** | §3.7, ADR-DA-008 |
| ✅ Q23 | Dali процесс в том же Gradle что SHUTTLE | **Отдельный процесс в общем Gradle multi-module** | §2.3, §3.7 |
| ✅ Q28 | ArcadeDB upgrade | **Upgrade до `latest` перед Dali engineering** (C.0 в refactoring plan) | §3.2, ADR-DA-011 |
| ✅ — | SHUTTLE → Dali transport | **HTTP REST (Вариант A), shared `dali-models` module** | §3.7 |
| ✅ — | JobRunr persistence backend | **FRIGG через custom ArcadeDB StorageProvider (Вариант A)** | §3.10 |
| ✅ — | Hound → YGG write modes | **Three modes policy в HoundConfig**: REMOTE_BATCH default для UC1/UC2a, REMOTE single для UC2b, EMBEDDED для UC3/UC4/preview | §3.1 |

### 5.1 Блокирующие (стратегические)

| # | Вопрос | Блокирует | Дедлайн |
|---|---|---|---|
| Q1 | **Backend стратегия** — де-факто JVM-first (Hound/SHUTTLE/HEIMDALL/Dali = Quarkus). Формально открыт для MIMIR/ANVIL (Q8/Q9). | MIMIR, ANVIL backend | re-eval после Q29/Q30 May-June |
| **✅ Q3** | **HEIMDALL backend — HB1 Quarkus ПОДТВЕРЖДЁН.** Реализован и работает (12-15.04). | — | CLOSED |
| Q4 | **HighLoad CFP deadline** — тезисы нужно подать до 17.04.2026 (48ч). Открытие приёма заявок: 25 мая 2026. | весь roadmap | 17.04.2026 |

### 5.2 Важные

| # | Вопрос | Заметки |
|---|---|---|
| **✅ Q5** | **Co-founder split** — CLOSED. Ты → Track A (Hound+Dali+ANVIL). Соавтор → Track B (LOOM+MIMIR+HEIMDALL+demo). | — |
| Q6 | **HEIMDALL event schema** — v1 реализована (HeimdallEvent, HeimdallEventView). v2 с tenantId — post-HighLoad. | mid May |
| Q7 | **HEIMDALL ↔ Dali control commands API** — REST (POST /control/cancel/{sessionId} ✅). Dali-side API TBD. | end of April |
| Q8 | **MIMIR язык** — Python (Anthropic SDK first-class) или Java (если JVM-first). **Post-upgrade опция:** Q29 может сделать MIMIR тонкой обёрткой вокруг ArcadeDB MCP Server вместо custom framework | зависит от Q1 |
| Q9 | **ANVIL backend язык** — Python или Java. **Post-upgrade опция:** Q30 может использовать 72 built-in ArcadeDB algorithms вместо custom Cypher | зависит от Q1 |
| Q10 | **Chur migration на Quarkus** — да или нет? | зависит от Q1, не блокирует (Chur работает) |
| Q11 | **Список middleware modules** для URD/SKULD | от owner SEER Studio, post-HighLoad |
| ✅ Q12 | **HEIMDALL frontend charting library** | **Nivo** — `@nivo/line`, `@nivo/pie` — реализованы в коде. |
| ✅ Q13 | **HEIMDALL frontend WebSocket protocol** | **Native WebSocket** `ws://:9093/ws/events` — реализован в HEIMDALL backend (EventStreamEndpoint). Chur проксирует через @fastify/websocket. |
| ✅ Q16 | **Quarkus версия** | **3.34.2** — подтверждена в SHUTTLE, Dali, HEIMDALL backend. |
| ✅ Q18 | **Docker network rename** | `aida_net` — переименовано в обоих compose + очищено в docs. |
| ✅ Q25 | **Event bus transport** | **Internal: Mutiny BroadcastProcessor** (in-process, M1). External Kafka → D6 post-HighLoad. SHUTTLE→HEIMDALL = HTTP POST fire-and-forget (HeimdallEmitter). |
| Q14 | **PostgreSQL semantic listener** — план довести до production quality на основе существующего каркаса (SemanticEngine + 5 компонентов) | start of May |
| Q15 | **ClickHouse semantic listener** — после PostgreSQL | June |
| Q24 | **HEIMDALL backend deployment** — embedded в SHUTTLE или отдельный процесс | см. Q3 |
| Q25 | **Event bus transport между компонентами** — HTTP POST (simple) vs Kafka (durability) vs SmallRye Reactive Messaging (in-Quarkus) | mid May, влияет на HEIMDALL event collection (I26-I31) |
| Q26 | **ANVIL вызывается напрямую из SHUTTLE или только через MIMIR?** — оба пути (I13 + I22) могут сосуществовать | обсудить при MIMIR design |
| Q27 | **HoundConfig полная схема** — target schema, dialect, resolution options, write mode details, extra params | при Hound refactor (C.1.2) |

### 5.3 Post-upgrade opportunities (новые после ArcadeDB upgrade решения)

| # | Вопрос | Контекст |
|---|---|---|
| **Q29** | **MIMIR redesign через ArcadeDB MCP Server?** | В ArcadeDB 26.3.1+ built-in MCP Server. Потенциально можно использовать ArcadeDB как MCP tool source для Claude напрямую, **без** custom MIMIR tool framework (ADR-DA-002 пересмотреть). Оценить post-upgrade: упрощение дизайна MIMIR vs attachment к ArcadeDB-specific feature. Re-evaluation в May-June. |
| **Q30** | **ANVIL упрощение через 72 built-in algorithms?** | ArcadeDB 26.3.1+ имеет 72 встроенных graph algorithms (PageRank, centrality, community detection, pathfinding). Вместо custom Cypher для `find_downstream_impact` можно использовать built-in. Оценить post-upgrade: custom Cypher vs built-in для каждого use case. |
| **Q31** | **UC2a preview implementation** | Решение отложено пользователем. Embedded Server pattern в ArcadeDB **подтверждён** как supported (CR1 разблокирован). Варианты: 6α (Temporary Embedded Server per worker) vs 6β (Preview databases в HoundArcade через multi-database feature). Требует resolve в May prior to Dali preview job implementation. |
| **Q32** | **Arrow Flight scope и implementation timing** | Strategic vector связанный с ygg.db fork. Не прорабатывается для October. Подробнее см. §8 «Стратегические заметки на будущее». |

### 5.4 Желательные / детали

| # | Вопрос | Заметки |
|---|---|---|
| Q16 | **Quarkus версия точно** — 3.17.7 (K1) или 3.34.2 (review кодовой базы) | уточнить, в документе сейчас 3.34.2 |
| Q17 | **Hound LOC** — точная цифра (есть только ~24 test files) | при следующем reconciliation |
| Q18 | **Docker network rename** `verdandi_net` → `aida_net` | ✅ выполнено (sprint cicd-docker-polish-apr15) |
| Q19 | **MIMIR cache format** — JSON file vs SQLite | скорее всего JSON для простоты |
| Q20 | **Ollama deployment** — где, какие models pre-loaded | до August demo prep |
| Q21 | **YGG: 4 document types** — точный список (есть только цифра 4) | при следующем reconciliation |

### 5.5 Сознательно отложено

| # | Вопрос | Когда вернёмся |
|---|---|---|
| D1 | **URD frontend + backend + middleware** | post-HighLoad |
| D2 | **SKULD frontend + backend + middleware** | post-HighLoad |
| D3 | **MUNINN, HUGINN, BIFROST** | post-HighLoad |
| D4 | **Storage Automation product** (второй продукт AIDA) | post-HighLoad |
| D5 | **Multi-graph в YGG** (DEV/STAGE/PROD изоляция) | post-HighLoad (однако multi-database в ArcadeDB уже supports — можно использовать для UC2a preview без полного multi-graph scope) |
| D6 | **Production observability** (Prometheus, Grafana, Loki, Jaeger) | post-HighLoad |
| D7 | **Multi-tenancy, billing, SOC2** | commercial post-MVP |
| D8 | **Native интеграции** (dbt, Airflow, Snowflake) | 2027 |
| D9 | **Cloud deployment** | после first pilot customer |
| D10 | **ygg.db fork с Arrow Flight native layer** | H2 2027+ (strategic vector, см. §8) |

### 5.6 Зависимости между вопросами

```
Q4 (CFP) ──► весь roadmap

Q1 (Backend strategy) ─┬─► Q3  (HEIMDALL backend)
                       ├─► Q8  (MIMIR language)
                       ├─► Q9  (ANVIL language)
                       └─► Q10 (Chur migration)

Q3 (HEIMDALL backend) ─► Q7 (HEIMDALL ↔ Dali API)

Q5 (Co-founder split) ─► Track A/B work allocation

Q11 (middleware list) ─► URD/SKULD планирование

Q28 ArcadeDB upgrade ─┬─► Q29 (MIMIR через MCP?)
  (✅ CLOSED)          ├─► Q30 (ANVIL через built-in?)
                       └─► Q31 (UC2a preview — Embedded Server разблокирован)

Q32 Arrow Flight ──────► D10 (ygg.db fork)
```

**Q1 и Q4 — самые блокирующие вопросы** верхнего уровня.

---

## 6. ADR пересмотр

### ADR-DA-001 — HEIMDALL deployment как in-process module

**Старое решение:** HEIMDALL = in-process module (где-то)

**Новое решение:** ❌ **REVOKED** — HEIMDALL стал отдельным сервисом (frontend + backend) потому что:
1. Admin scope шире чем один компонент (admin всей AIDA, не одного Dali)
2. Frontend отдельный (см. § 3.6.1)
3. Backend независимость от Dali (HEIMDALL должен работать когда Dali down)

**Замена:** ADR-DA-010 (HEIMDALL = Admin Control Panel всей AIDA, отдельный frontend + отдельный backend)

### ADR-DA-002 — Custom MIMIR framework (~300 LoC, не LangChain)

**Статус:** ✅ ACTIVE

### ADR-DA-003 — In-process ring buffer (10K events) для HEIMDALL event bus

**Статус:** ✅ ACTIVE для backend HEIMDALL (работает в пределах одного процесса HEIMDALL backend)

### ADR-DA-004 — LOOM rendering library

**Старое решение:** PENDING (Sigma.js / Cytoscape / WebGL / custom)
**Новое решение:** ✅ **CLOSED** — выбран **React Flow 12.10.2** (используется в production VERDANDI)

### ADR-DA-005 — ClickHouse dialect drop trigger конец июня

**Статус:** ✅ ACTIVE — но обновлено: грамматика **уже есть**, нужен только semantic listener (как для PostgreSQL)

### ADR-DA-006 — LLM tier 1 = Anthropic Claude Sonnet

**Статус:** ✅ ACTIVE

### ADR-DA-007 — Algorithms-as-tools (унаследован из v4 ADR-014)

**Статус:** ✅ ACTIVE

### ADR-DA-008 — Dali Core implementation choice

**Статус:** ✅ **CONFIRMED** (11.04.2026)

**Решение:** **Java 21 + Quarkus 3.34.2 + JobRunr (Open Source)**

**Контекст:** Dali Core — orchestrator парсинга. Должен поддерживать scheduled / on-demand / event-driven triggers, worker pool с concurrency limits, job queue с retry, complex job tracking из N подзадач, persistence state, эмиссию events в HEIMDALL, приём control commands. Реальные use cases UC1-UC4 значительно сложнее простого on-demand парсинга.

**Главный архитектурный driver:** Hound — это Java library с ArcadeDB embedded engine. Любой не-JVM Dali требует обернуть Hound в long-running gRPC/HTTP сервер (дополнительная работа) или использовать subprocess (fragile, slow). **Java + Quarkus Dali может вызывать Hound как обычную Java библиотеку — без serialization overhead, без network latency, без subprocess management.**

**Почему JobRunr поверх Quarkus, а не custom Quarkus:**
- JobRunr покрывает все 15 R-требований готовой библиотекой (~600-800 LoC бизнес-логики vs ~1500 LoC custom)
- Готовый dev dashboard для observability на этапе разработки
- Persistence из коробки (через custom ArcadeDB StorageProvider для FRIGG)
- Job queue + retry + cron + complex jobs + progress tracking — всё библиотечное
- Quarkus extension `quarkus-jobrunr` — first-class integration

**Почему не Prefect 2 (Python):**
- Изолированный Python остров в JVM-доминантном backend
- Subprocess/gRPC проблема для интеграции с Java Hound (требует обернуть Hound в сервис = +1-2 недели работы на стороне Hound)
- Архитектурный fit с реальностью кодовой базы важнее «elegance» отдельного компонента

**Отклонены:**
- ❌ Apache Airflow — heavy footprint, DAG-first
- ❌ Dagster — asset-centric не fit
- ❌ Temporal — категорически overkill
- ❌ Conductor / Camunda 8 — server-based, heavy
- ❌ Copper / nFlow — Java embedded, но требуют отдельную DB
- ❌ Go + goroutines — нет Go-экспертизы
- ❌ Python custom — переписывание Prefect руками
- ❌ Custom Quarkus — JobRunr делает короче

**Связанные решения:**
- **SHUTTLE → Dali transport** = HTTP REST (Вариант A), отдельные процессы в одном Gradle multi-module проекте, shared types через `dali-models` module
- **JobRunr persistence** = FRIGG через custom ArcadeDB StorageProvider (Вариант A, осознанный выбор в пользу единого state store)
- **Hound integration** = прямой in-JVM call через Gradle `implementation project(':hound')` — zero integration cost

**Известные риски:**
- Quarkus expertise частичная (SHUTTLE сложно дался) — первые недели Dali медленнее
- JobRunr Pro features нельзя использовать (€999/год) — Open Source хватает
- Custom ArcadeDB StorageProvider maintenance burden — следить за JobRunr API updates

**Ссылки:**
- JobRunr docs: https://www.jobrunr.io/en/documentation/
- Quarkus extension: https://quarkus.io/extensions/org.jobrunr/quarkus-jobrunr/

### ADR-DA-009 — SHUTTLE backend framework choice

**Статус:** ❌ **CLOSED** — выбор уже сделан и в production: **Java 21 + Quarkus 3.34.2 + SmallRye GraphQL**.

ADR был некорректно введён в v1.0 документа, исходя из ошибочного представления что SHUTTLE — TBD. Реальный выбор уже был зафиксирован в коде.

### ADR-DA-010 — HEIMDALL = Admin Control Panel всей AIDA

**Старое (узкое) определение:** HEIMDALL = control panel конкретно для Dali

**Новое (широкое) определение:** HEIMDALL = **Admin Control Panel всей AIDA**, объединяет две роли:
1. Generic admin UI всей AIDA (health dashboards, user management, audit log)
2. Control panel для оркестрации Dali (как одна из views)

**Frontend:** отдельный React проект
**Backend:** отдельный сервис (HB1 Quarkus — proposed, ⏳ PENDING)

**Status:** 🔬 PROPOSED (нужно подтверждение)

### ADR-DA-011 — ArcadeDB upgrade 24.11.1 → latest

**Статус:** ✅ **CONFIRMED** (11.04.2026)

**Решение:** Upgrade ArcadeDB с 24.11.1 до `arcadedata/arcadedb:latest` tag перед началом Dali engineering (C.0 в refactoring plan).

**Контекст:** 24.11.1 была выпущена несколько месяцев назад. Latest (26.3.1+) включает значительные улучшения релевантные для AIDA.

**Мотивация:**

Бонусы upgrade:
- **Embedded Server pattern** официально документирован — разблокирует UC2a preview implementation (см. Q31)
- **72 встроенных graph algorithms** — потенциал упрощения ANVIL (см. Q30)
- **Parallel query execution** — bigger lineage graphs queries быстрее
- **Materialized views** — для HEIMDALL metrics dashboards
- **Built-in MCP Server** — потенциальное упрощение MIMIR через direct ArcadeDB MCP integration (см. Q29)
- **Graph Analytical View (GAV)** — CSR acceleration для OLAP queries
- **Native OpenCypher engine** — 25× faster graph queries vs Cypher-to-Gremlin translation
- **75.9% faster SQL parsing** (миграция с JavaCC на ANTLR4)
- **Multi-database support** — поддержка нескольких databases в одном ArcadeDB server (для UC2a preview, future multi-tenancy)

**Риски upgrade:**
- **SQL parser переписан** с JavaCC на ANTLR4 — reserved keywords могут расшириться, нужно regression тестирование SHUTTLE SQL queries
- **Cypher engine переписан** с Gremlin-translation на native OpenCypher — subtle behavioral differences, нужно regression тестирование LineageService/ExploreService
- **Unknown unknowns** — первый upgrade такого масштаба для AIDA
- **Effort ~5-8 дней** на critical path перед Dali engineering

**Почему сейчас, а не post-HighLoad:**
- Custom ArcadeDB StorageProvider для JobRunr пишется сразу для новой версии, не legacy code
- Dali engineering начинается с clean slate на latest
- Бонусы (особенно MCP Server для MIMIR) могут повлиять на архитектурный дизайн других компонентов — лучше знать до начала разработки
- Post-HighLoad freeze (сентябрь) — upgrade после сложно без простоя

**Почему `latest` tag, не pinned version:**
- User choice — готовы жить с свежими stable releases
- ArcadeDB Apache 2.0 forever commitment снижает license risks
- При нестабильностях можно pin на конкретную версию позже

**Operational recommendation (не блокирующий):**
- Для production стабильности в долгосрочной перспективе рассмотреть pin на конкретную версию + Dependabot/Renovate для auto-tracking новых релизов через CI/PR

**Ссылки:**
- Changelog 25.x-26.x: https://github.com/ArcadeData/arcadedb/releases
- Embedded Server pattern: https://arcadedb.com/embedded.html
- Client/Server mode: https://arcadedb.com/client-server.html

---

## 7. История изменений

| Дата | Версия | Что изменилось |
|---|---|---|
| 11.04.2026 | 1.0 | Initial draft. Зафиксированы Hound (Java), YGG (ArcadeDB), MIMIR proposal. **Содержал ~15 фактических ошибок** относительно реального кода (SHUTTLE TBD когда он Quarkus, Chur TBD когда он Fastify, и т.д.) |
| 11.04.2026 | 2.0 | **Полная переработка**. Reconciliation с реальной кодовой базой по K1-K20 + детальный review. Зафиксированы реальные стеки: Hound (Java 21 + Gradle + ANTLR 4.13.1 + ArcadeDB 24.11.1 embedded), SHUTTLE (Quarkus 3.34.2 + Gradle + SmallRye GraphQL), Chur (Fastify 4 + Node.js 22 + jose), VERDANDI (React 19.2.4 + Vite 8 + Tailwind 4 + Zustand + graphql-request + ELK.js). Установлена правильная иерархия: **AIDA = зонтик, SEER Studio = первый продукт**. HEIMDALL переопределён как admin control panel всей AIDA с отдельным frontend и backend. Добавлены URD/SKULD/middleware как PLANNED. Закрыты Q2 (SHUTTLE), Q7 (Hound build), Q8 (Chur), Q12 (state mgmt), Q17 (npm). Открыты новые: Q1 (backend strategy), Q3 (HEIMDALL backend), Q11 (middleware modules list). |
| 11.04.2026 | 2.1 | **Dali Core зафиксирован** — Java 21 + Quarkus 3.34.2 + JobRunr (Open Source). ADR-DA-008 → CONFIRMED. Q2 → CLOSED. Q1 (backend strategy) остаётся formally open, решения per-component. Главный driver выбора: прямой in-JVM вызов Hound без полиглотного overhead. JobRunr покрывает все 15 требований готовой библиотекой (~600-800 LoC vs ~1500 custom Quarkus). |
| 12.04.2026 | 2.3 | **ArcadeDB version policy зафиксирована (§3.2).** HoundArcade (network) уже на 26.3.2 — production актуален. Embedded в Hound только тесты (25.12.1). Выбран Вариант 1: тесты → network mode. Version policy: no mixed embedded/network на одной БД. C.0 effort пересмотрен: 5-8 дней → 1-3 дня. |
| 12.04.2026 | 2.4 | **Frontend architecture зафиксирована (§2.4-§2.6).** ADR-DA-012 CONFIRMED: B2 — thin JS shell + Module Federation. `frontends/shell/` — host app с React Router + Zustand context store. Remotes: verdandi, heimdall-frontend (exposes `./App`). `aida-shared` singleton = механизм shared state. ADR-DA-013: URL context protocol (geoid canonical ID) + `shell-context.ts` bridge. `packages/aida-shared/` + `frontends/shell/` добавлены в структуру. | Зафиксированы: (1) **AIDA Gradle multi-module structure** (§2.3) — все JVM сервисы в одном проекте, отдельные процессы, shared `dali-models`. (2) **FRIGG как unified persistence** (§3.10) — ArcadeDB с custom JobRunr StorageProvider, saved views + user prefs + jobs в одном backend. (3) **SHUTTLE → Dali = HTTP REST** (Вариант A) — shared Gradle module, independent processes. (4) **Hound three modes policy** (§3.1) — REMOTE_BATCH / REMOTE / EMBEDDED в HoundConfig, выбор по UC. (5) **ArcadeDB upgrade to `latest`** — ADR-DA-011 CONFIRMED, upgrade перед Dali engineering (C.0). Embedded Server pattern подтверждён в документации. (6) **Arrow Flight** — strategic note для ygg.db fork (§8), не прорабатывается для October. (7) **Новые open questions:** Q23 (closed), Q24-Q27 (важные), Q29 MIMIR MCP, Q30 ANVIL algorithms, Q31 UC2a preview, Q32 Arrow Flight timing. (8) Добавлены D10 (ygg.db fork в defer list). |

---

## 8. Стратегические заметки на будущее

Эта секция фиксирует **архитектурные векторы которые НЕ прорабатываются для October scope**, но должны быть зафиксированы как осознанные направления развития AIDA.

### 8.1 Arrow Flight как промежуточный transport layer

**Статус:** 🔵 Strategic note, не October scope. Не прорабатывается в рамках текущего engineering.

**Контекст:** Arrow Flight — high-throughput gRPC-based protocol для columnar data transfer. Интересен для AIDA как потенциал оптимизации передачи больших lineage datasets (300K+ atoms) между JVM сервисами.

**Связь с ygg.db fork:** Arrow Flight рассматривается как **strategic preparation** для долгосрочного плана **ygg.db fork** — собственного форка ArcadeDB с native Arrow Flight integration на уровне engine. В этом долгосрочном vision AIDA получает **killing feature** для работы с массивными lineage графами.

**Что известно:**
- Подходящие зоны интеграции после критического ревью: Hound bulk write в HoundArcade, SHUTTLE deep read queries, ANVIL graph traversal результаты, SHUTTLE → ANVIL direct calls
- **НЕ подходит:** frontend layer, BFF, events streaming, single commands, in-JVM calls, auth flows
- X1 (adapter sidecar) vs X2 (Flight в Hound/Dali) — variant не выбран, требует дополнительной проработки
- Client side: SHUTTLE/ANVIL/Hound должны поддерживать Arrow Java client (~1 неделя per service)

**Timeline:**
- **October 2026:** не в scope, не прорабатывается
- **Post-HighLoad (Nov-Dec 2026):** возможная первая оценка через proof-of-concept на одной интеграции (например Hound bulk write)
- **H2 2027+:** ygg.db fork с native Arrow Flight integration (далёкие планы)

**Важно:** когда время придёт, нужно провести повторное критическое ревью на актуальной архитектуре — use cases и bottlenecks могут измениться к тому моменту.

### 8.2 ygg.db — отдельный продукт-форк ArcadeDB

**Статус:** 🔵 Very long-term strategic vector. Не в AIDA October roadmap.

**Видение:** ygg.db — собственный форк ArcadeDB с domain-specific оптимизациями для lineage analytics:
- Native Arrow Flight layer
- Lineage-specific query optimizations
- Columnar storage hints для atoms/edges
- Potentially: custom SQL dialect оптимизированный для lineage queries

**Когда актуально:** H2 2027+ при наличии коммерческой тяги и ресурсов для поддержки своего fork.

**Риски долгосрочного vision:**
- Maintenance burden собственного форка (security patches, feature parity с upstream)
- ArcadeDB сам может реализовать похожую функциональность (Graph Analytical View в 26.3.2 — уже движение в направлении OLAP acceleration)
- Отвлечение от core product development

Это **осознанный vector**, но не commitment. Решение о фактическом старте ygg.db development должно быть сделано отдельно при наличии business case.

### 8.3 Post-upgrade opportunities

После ArcadeDB upgrade (C.0) открываются возможности которые стоит оценить **до final MIMIR/ANVIL design**:

**Q29 — MIMIR через ArcadeDB built-in MCP Server**

ArcadeDB 26.3.1+ имеет встроенный MCP (Model Context Protocol) Server. Это может радикально упростить MIMIR architecture:
- Вместо custom tool framework (~300 LoC, ADR-DA-002) — MIMIR становится тонкой обёрткой
- Claude напрямую использует ArcadeDB MCP для query_lineage, search_nodes, get_procedure_source
- MIMIR код сокращается в разы
- Trade-off: тесная связь с ArcadeDB-specific feature vs portability

**Требуется evaluation:** May-June, до начала MIMIR engineering.

**Q30 — ANVIL через 72 built-in graph algorithms**

ArcadeDB 26.3.1+ имеет 72 built-in graph algorithms (PageRank, centrality, community detection, pathfinding, link prediction). Для `find_downstream_impact` можно использовать built-in вместо написания custom Cypher:
- Упрощение ANVIL codebase
- Performance benefits от built-in implementation
- Trade-off: ArcadeDB-specific vs portability

**Требуется evaluation:** May-June, до начала ANVIL engineering.

### 8.4 UC2a preview implementation (Q31)

**Статус:** ⏸ PENDING user decision

**Контекст:** Reader может открыть файл «на посмотреть» через UI без commit в production lineage. CR1 (Embedded Server pattern support) разблокирован через документацию ArcadeDB. Варианты:

- **6α** — Temporary Embedded Server per worker (dynamic ports, per-session JVM embedded)
- **6β** — Preview databases в HoundArcade server (multi-database feature, single server instance)

Открытые вопросы CR2-CR6 (mapping storage, lifecycle, concurrency, Docker networking, MIMIR preview-aware) требуют resolve после выбора sub-варианта.

**Решение требуется:** до середины May (прежде чем начать preview implementation в Dali scope).

---

## 9. Что НЕ в этом документе (deliberately)

Чтобы избежать дублирования и confusion:

- **Полная матрица интеграций с конкретными смыслами событий I1-I35** — живёт в отдельном документе `INTEGRATIONS_MATRIX.md` (working doc)
- **Refactoring plan с детальным breakdown** — живёт в отдельном документе `REFACTORING_PLAN.md` (working doc)
- **Performance budgets** — в `ARCH-spec § 7` (требует обновления после reconciliation)
- **Detailed ADR rationale** — частично здесь, частично в `ARCH-spec § 6`
- **Roadmap по месяцам** — в `TARGET_ARCH § 3`
- **Risk register** — в `TARGET_ARCH § 4`
- **Demo narrative** — в `TARGET_ARCH § 2`

**Note:** Документы `ARCH_11042026_06-30_SPEC` и `ARCH_11042026_06-30_REVIEW` устарели по терминологии (везде где написано «AIDA architecture» имея в виду конкретный продукт SEER Studio). Их нужно либо обновить, либо добавить корректирующее примечание сверху.

---

*Этот документ — working source of truth для технических решений. Обновляется при принятии каждого нового ADR. Основывается на реальном состоянии кодовой базы по состоянию на 11.04.2026.*
