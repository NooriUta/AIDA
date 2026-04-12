# Sprint M1 — Apr 13 → May 9 (Core Engines)

**Документ:** `SPRINT_APR13_MAY9_M1`  
**Версия:** 1.0  
**Дата:** 12.04.2026  
**Milestone:** M1 — Core Engines (май 9)  

---

## Статус к старту спринта

| Задача | Статус |
|---|---|
| HEIMDALL backend Sprint 1 (event bus, ring buffer, /ws/events) | ✅ DONE |
| HEIMDALL backend Sprint 2 (AtomicLong metrics, ControlResource, snapshot) | ✅ DONE |
| Repo migration (aida-root/ monorepo, Phase 0-6) | ✅ DONE |
| C.2.1 SQL injection fix (SearchService.java:86-87) | ✅ DONE |

**Разблокировано:** HEIMDALL frontend может подключаться к реальному backend. SHUTTLE mutations можно строить на чистой кодовой базе.

---

## Зафиксированные решения на старте

### ADR-DA-012 — B2: Thin JS Shell + Module Federation (CONFIRMED)

```
frontends/shell/           ← host app (Module Federation host)
├── shared header           AidaNav component
├── auth context            Chur session via aida-shared
├── React Router            /verdandi/* /urd/* /skuld/* /heimdall/*
└── context store           Zustand shellStore (nodeId, currentApp, etc.)

frontends/verdandi/        ← remote, exposes ./App (lazy chunk)
frontends/urd/             ← remote, exposes ./App (lazy chunk, planned)
frontends/skuld/           ← remote, exposes ./App (lazy chunk, planned)
frontends/heimdall-frontend/ ← remote, exposes ./App (lazy chunk)
```

**aida-shared singleton** — единственный механизм shared state между shell и remotes. Ключи: `singleton: true` в MF конфиге у всех приложений.

### ADR-DA-013 — URL context passing (CONFIRMED)

Canonical ID = ArcadeDB geoid (`DaliTable:prod.orders`).
`navigateTo(app, context)` → B2: React Router без reload. B1 fallback: `window.location.href`.

---

## Треки спринта (4 параллельных)

### Track 1 — HEIMDALL Frontend

**Стек:** React 19 + Vite 6 + TypeScript + Tailwind 4 + @nivo/line + @nivo/bar + @nivo/pie + native WebSocket + react-virtuoso + zustand + aida-shared

**Порт:** 5174 (MF remote)

#### HF-01 · Scaffold + vite.config.ts (~2 ч) — W0 (Apr 13-18)

```bash
cd frontends/heimdall-frontend
npm install
```

**`package.json`:**
```json
{
  "name": "@seer/heimdall-frontend",
  "dependencies": {
    "react": "^19.2.4",
    "react-dom": "^19.2.4",
    "react-router-dom": "^7.0.0",
    "@nivo/line": "^0.87.0",
    "@nivo/bar": "^0.87.0",
    "@nivo/pie": "^0.87.0",
    "react-virtuoso": "^4.7.0",
    "zustand": "^5.0.0",
    "aida-shared": "file:../../packages/aida-shared"
  }
}
```

**`vite.config.ts`:**
```typescript
import federation from '@module-federation/vite';

export default defineConfig({
  plugins: [
    react(),
    federation({
      name: 'heimdall-frontend',
      filename: 'remoteEntry.js',
      exposes: { './App': './src/App.tsx' },
      shared: {
        react:              { singleton: true, requiredVersion: '^19.0.0' },
        'react-dom':        { singleton: true },
        'react-router-dom': { singleton: true },
        'aida-shared':      { singleton: true },
        zustand:            { singleton: true },
      },
    }),
  ],
  server: { port: 5174, cors: true },
});
```

#### HF-02 · useEventStream — native WebSocket (~2 ч) — W0

```typescript
// src/hooks/useEventStream.ts
const WS_URL = import.meta.env.VITE_HEIMDALL_WS ?? 'ws://localhost:9093/ws/events';

export function useEventStream(filter: EventFilter = {}) {
  const [events, setEvents]       = useState<HeimdallEvent[]>([]);
  const [connected, setConnected] = useState(false);

  useEffect(() => {
    const params = new URLSearchParams();
    if (filter.component) params.set('filter', `component:${filter.component}`);
    const ws = new WebSocket(params.toString() ? `${WS_URL}?${params}` : WS_URL);

    ws.onopen    = () => setConnected(true);
    ws.onclose   = () => setConnected(false);
    ws.onmessage = ({ data }) => {
      try {
        const event: HeimdallEvent = JSON.parse(data);
        setEvents(prev => [event, ...prev].slice(0, 500));
      } catch {}
    };
    return () => ws.close();
  }, [filter.component, filter.sessionId]);

  return { events, connected, clear: () => setEvents([]) };
}
```

#### HF-03 · useMetrics — polling (~1 ч) — W0

```typescript
// src/hooks/useMetrics.ts
export function useMetrics(intervalMs = 2000) {
  const [metrics, setMetrics] = useState<MetricsSnapshot | null>(null);

  useEffect(() => {
    let active = true;
    const poll = () =>
      fetch(`${API}/metrics/snapshot`)
        .then(r => r.json())
        .then(d => { if (active) setMetrics(d); })
        .catch(() => {});
    poll();
    const id = setInterval(poll, intervalMs);
    return () => { active = false; clearInterval(id); };
  }, [intervalMs]);

  return { metrics };
}
```

#### HF-04 · App.tsx + routing (~1 ч) — W0

```typescript
// src/App.tsx
export default function App() {
  return (
    <div style={{ display:'flex', flexDirection:'column', height:'100vh' }}>
      <nav>...</nav>
      <Routes>
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/events"    element={<EventStreamPage />} />
        <Route path="/controls"  element={<ControlsPage />} />
        <Route path="*"          element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </div>
  );
}
```

**Конец W0:** `npm run dev` → localhost:5174, три страницы, WebSocket к backend.

---

#### HF-05 · MetricsBar (~1.5 ч) — W1 (Apr 20-25)

Live counters: atoms, files, tool calls, workers, queue depth, resolved %.
Цвет resolved: зелёный ≥85%, amber ≥70%, красный <70%.

#### HF-06 · ThroughputChart — @nivo/line (~2 ч) — W1

```typescript
// src/components/ThroughputChart.tsx
import { ResponsiveLine } from '@nivo/line';

// 30-секундное окно, bucket по секундам
// animate + motionConfig="gentle" → react-spring smooth updates
```

**Ключевой момент Nivo для real-time:** данные как пропсы → автоматическая анимация через react-spring при каждом update.

---

#### HF-07 · ResolutionGauge — @nivo/pie (~1.5 ч) — W2 (Apr 27)

```typescript
// src/components/ResolutionGauge.tsx
import { ResponsivePie } from '@nivo/pie';

// Donut chart: innerRadius=0.72, startAngle=-90, endAngle=270
// Центральный текст: абсолютно позиционированный div поверх SVG
// Цвет: зависит от resolution rate (зеленый/amber/красный)
```

#### HF-08 · EventLog — react-virtuoso (~2 ч) — W2

```typescript
// src/components/EventLog.tsx
import { Virtuoso } from 'react-virtuoso';

// followOutput="smooth" → авто-скролл к новым событиям
// Цвет по sourceComponent: hound=#7F77DD, dali=#1D9E75, mimir=#BA7517
// Цвет по level: INFO=secondary, WARN=#BA7517, ERROR=#D85A30
```

---

#### HF-09 · DashboardPage (~1.5 ч) — W3 (May 4-9)

MetricsBar + ThroughputChart + последние 50 событий в EventLog.

#### HF-10 · EventStreamPage (~1 ч) — W3

```typescript
// Полный EventLog + фильтр по component (select) и level (select)
// clear() кнопка
```

#### HF-11 · ControlPanel + ControlsPage (~1.5 ч) — W3

```typescript
// POST /control/reset — с confirm dialog
// POST /control/snapshot — сохранить снапшот в FRIGG
// GET /control/snapshots — список снапшотов
// Каждое destructive действие: confirm dialog перед выполнением
```

---

### Track 2 — SHUTTLE (C.2.2 + C.2.3)

> C.2.1 SQL injection fix уже сделан ✅

#### C.2.2 · Mutation resolvers (~2 дня) — W1-W2

```graphql
type Mutation {
  startParseSession(input: ParseSessionInput!): ParseSession!
  cancelSession(sessionId: ID!): Boolean!
  askMimir(sessionId: ID!, query: String!): MimirResponse!
  saveView(input: SaveViewInput!): SavedView!
  deleteView(viewId: ID!): Boolean!
  resetDemoState: Boolean!
}
```

Реализация:
- `startParseSession` → Dali REST call (когда DaliClient появится) или заглушка → SessionInfo
- `resetDemoState` → HTTP POST к HEIMDALL /control/reset (HeimdallClient)
- `saveView` → FRIGG через ArcadeDB

#### C.2.3 · Subscription resolvers (~1.5 дня) — W2-W3

```graphql
type Subscription {
  heimdallEvents(filter: EventFilter): HeimdallEvent!
  sessionProgress(sessionId: ID!): SessionProgress!
}
```

Реализация: `@Channel` + SmallRye Reactive Messaging (in-process для MVP).
При наличии HEIMDALL backend → проксировать через HeimdallClient WebSocket.

---

### Track 3 — Chur (C.3.1 + C.3.4)

#### C.3.1 · Chur scopes + HEIMDALL proxy (~1.5 дня) — W2-W3

```typescript
// src/middleware/requireScope.ts
export function requireScope(scope: string): preHandlerAsyncHookHandler {
  return async (req, reply) => {
    const session = await getSession(req.cookies.sid);
    if (!session?.scopes?.includes(scope)) {
      reply.code(403).send({ error: `Missing scope: ${scope}` });
    }
  };
}

// Routes:
// POST /heimdall/graphql → requireScope('aida:admin') + proxy → :9093/graphql
// POST /heimdall/control/* → requireScope('aida:admin:destructive') + proxy
```

#### C.3.4 · WebSocket upgrade handler (~1 день) — W3

```bash
npm install @fastify/websocket  # R6 из HEIMDALL_SPRINT_PLAN.md
```

```typescript
// WebSocket upgrade для /graphql (GraphQL Subscriptions через graphql-ws)
server.register(fastifyWebsocket);
server.get('/graphql', { websocket: true }, async (connection, req) => {
  // graphql-ws protocol handler
  // Проксирует к SHUTTLE :8080/graphql WebSocket
});
```

---

### Track 4 — Shell app (B2 Module Federation)

#### SH-01 · Shell scaffold + vite.config.ts (~2 ч) — W2-W3

> Файлы уже в архиве: `frontends/shell/`

```bash
cd frontends/shell
npm install
```

Проверить что remotes в vite.config.ts совпадают с реальными портами:
```typescript
remotes: {
  verdandi:            'http://localhost:5173/remoteEntry.js',
  'heimdall-frontend': 'http://localhost:5174/remoteEntry.js',
}
```

#### SH-02 · shellStore.ts (~1 ч) — W2

Уже в архиве. Проверить:
- `useShellStore()` экспортируется корректно
- `_setNavigate(fn)` вызывается в `App.tsx useEffect`
- `navigateTo(app, context)` обновляет `currentApp` + вызывает React Router navigate

#### SH-03 · App.tsx — lazy remotes (~1 ч) — W2

```typescript
// Lazy imports — Module Federation загружает в runtime:
const VerdandiApp    = React.lazy(() => import('verdandi/App'));
const HeimdallApp    = React.lazy(() => import('heimdall-frontend/App'));
// Будущие (закомментировать пока не готовы):
// const UrdApp      = React.lazy(() => import('urd/App'));
// const SkuldApp    = React.lazy(() => import('skuld/App'));
```

#### SH-04 · AidaNav.tsx (~1 ч) — W3

Уже в архиве. Проверить:
- Активный app подсвечивается (`currentApp === app.id`)
- Клик → `navigateTo(app)` без параметров
- App-identity цвета из tokens.css (`--aida-app-verdandi`, `--aida-app-heimdall`)

#### SH-05 · verdandi → MF remote (~1 ч) — W3

Добавить в `frontends/verdandi/vite.config.ts`:
```typescript
import federation from '@module-federation/vite';

federation({
  name: 'verdandi',
  filename: 'remoteEntry.js',
  exposes: { './App': './src/App.tsx' },
  shared: {
    react:              { singleton: true, requiredVersion: '^19.0.0' },
    'react-dom':        { singleton: true },
    'react-router-dom': { singleton: true },
    'aida-shared':      { singleton: true },
    zustand:            { singleton: true },
  },
})
```

Добавить `base: '/verdandi/'` в vite.config.ts.

---

## Расписание по неделям

```
W0 Apr 13-18   HF-01..04 (scaffold + hooks)          ← ЭТА НЕДЕЛЯ
               SQL injection fix ✅ (done)
               ELK quick wins M-3,5,7 (XS, 1 день)

W1 Apr 20-25   HF-05..06 (MetricsBar + ThroughputChart Nivo)
               C.2.2 Mutations start
               Shell SH-01..02 scaffold (параллельно)

W2 Apr 27      HF-07..08 (ResolutionGauge + EventLog)
               C.2.2 Mutations complete
               C.2.3 Subscriptions start
               C.3.1 Chur scopes + HEIMDALL proxy
               Shell SH-03..04 (App.tsx + AidaNav)

W3 May 4-9     HF-09..11 (DashboardPage + EventStreamPage + ControlsPage)
               C.2.3 Subscriptions complete
               C.3.4 Chur WebSocket upgrade
               Shell SH-05 (verdandi → MF remote)
               Integration: shell + verdandi + heimdall-frontend

M1 May 9       ✅ Milestone: Core Engines
               HEIMDALL frontend работает, подключён к backend
               SHUTTLE mutations + subscriptions
               Chur scopes + WebSocket
               Shell запускает verdandi + heimdall-frontend
```

---

## Definition of Done для M1

- [ ] `npm run dev` в `frontends/shell/` → открывает verdandi и heimdall-frontend без ошибок
- [ ] HEIMDALL dashboard показывает live events от backend
- [ ] ResolutionGauge показывает % из `/metrics/snapshot`
- [ ] `/controls` → кнопка Reset → confirm → POST /control/reset → backend resets
- [ ] SHUTTLE `startParseSession` mutation возвращает результат
- [ ] SHUTTLE `heimdallEvents` subscription получает events
- [ ] Chur `/heimdall/graphql` требует `aida:admin` scope
- [ ] `navigateTo('heimdall', { returnTo: '/verdandi' })` → переход без reload
- [ ] docker compose up → все сервисы стартуют
- [ ] `cd frontends/shell && npm run build` → builds без ошибок

---

## Риски

| ID | Риск | Вероятность | Митигация |
|---|---|---|---|
| R-S1 | Vite MF dev mode: remotes не подхватываются без `cors: true` | 🟠 Высокая | `server.cors: true` в каждом remote vite.config.ts |
| R-S2 | `aida-shared` singleton не работает если версии в shell и remote расходятся | 🟡 Средняя | Одинаковый `requiredVersion` везде, single `npm install` из root |
| R-S3 | graphql-ws subscriptions в SHUTTLE требуют WebSocket в Chur (R6) | 🟠 Высокая | Делать C.3.4 до C.2.3 |
| R-S4 | HEIMDALL backend порт 9093 не в docker-compose | 🟡 Средняя | Добавить в docker-compose.yml до интеграции |
| R2   | FRIGG (:2481) не запущен | 🟡 Средняя | Добавить второй ArcadeDB контейнер или skip snapshot в MVP |

---

## История изменений

| Дата | Версия | Что |
|---|---|---|
| 12.04.2026 | 1.0 | Initial. Объединённый спринт Apr 13–May 9. Учтён статус DONE: HEIMDALL back S1+S2, repo migration, SQL fix. Shell B2 MF решение задокументировано. |
