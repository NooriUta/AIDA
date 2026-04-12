# HEIMDALL Sprint 3 — Детальная постановка задач

**Документ:** `HEIMDALL_SPRINT3_TASK_SPEC`
**Версия:** 3.0
**Дата:** 12.04.2026
**Владелец трека:** Соавтор (Track B)

---

## Правило спринта

Только реальные данные от существующих сервисов.
Явно исключено: Dali page (нет Dali), Users page (нет real API), Demo mode (придут реальные данные).

| Задача | Источник данных | Неделя | Оценка |
|---|---|---|---|
| H3.5 EventLog 6-col + badges + payload | SHUTTLE (уже шлёт) | W1 | 2 ч |
| H3.9 Chur auth events | Chur login/logout | W1 | 1 ч |
| H3.4 Services health backend + page | ping 9 сервисов | W2 | 3 ч |
| H3.1 Dashboard: health strip + errors | /services/health + events | W2 | 2 ч |
| H3.7 Presentation mode | любые данные | W2 | 1.5 ч |
| H3.8 HoundHeimdallListener | Hound (после C.1.3) | W3 | 2 ч |

**Итого: ~11.5 ч**


---

## H3.5 — EventLog: 6-col grid + comp badges + payload (~2 ч)

**W1 Apr 20-25**

SHUTTLE уже шлёт `REQUEST_RECEIVED` и `REQUEST_COMPLETED`. Данные есть — отображение не информативно. После этой задачи каждое событие читается за одну секунду.

### 1. `src/utils/eventFormat.ts` (создать)

```typescript
import type { HeimdallEvent } from 'aida-shared';

export const EVENT_LABELS: Record<string, string> = {
  FILE_PARSING_STARTED:   'Parsing started',
  FILE_PARSING_COMPLETED: 'Parsing completed',
  FILE_PARSING_FAILED:    'Parsing failed',
  ATOM_EXTRACTED:         'Atoms extracted',
  RESOLUTION_COMPLETED:   'Names resolved',
  SESSION_STARTED:        'Session started',
  SESSION_COMPLETED:      'Session completed',
  WORKER_ASSIGNED:        'Worker assigned',
  JOB_COMPLETED:          'Job completed',
  QUERY_RECEIVED:         'Query received',
  TOOL_CALL_STARTED:      'Tool call',
  TOOL_CALL_COMPLETED:    'Tool completed',
  LLM_RESPONSE_READY:     'LLM response',
  TRAVERSAL_STARTED:      'Traversal started',
  TRAVERSAL_COMPLETED:    'Traversal done',
  REQUEST_RECEIVED:       'GraphQL request',
  REQUEST_COMPLETED:      'GraphQL done',
  SUBSCRIPTION_OPENED:    'Subscription',
  AUTH_LOGIN_SUCCESS:     'Login',
  AUTH_LOGIN_FAILED:      'Login failed',
  AUTH_LOGOUT:            'Logout',
  DEMO_RESET:             'Demo reset',
};

export function formatPayload(event: HeimdallEvent): string {
  const p = event.payload ?? {};
  switch (event.eventType) {
    case 'FILE_PARSING_STARTED':   return `file:"${p.file}"`;
    case 'FILE_PARSING_COMPLETED': return `file:"${p.file}" atoms:${p.atomCount} ${event.durationMs}ms`;
    case 'ATOM_EXTRACTED':         return `${p.atomCount} atoms · ${p.file}`;
    case 'RESOLUTION_COMPLETED':   return `${p.resolved}/${p.total} (${Math.round(Number(p.resolutionRate ?? 0) * 100)}%)`;
    case 'TOOL_CALL_COMPLETED':    return `tool:"${p.tool}" nodes:${p.nodes} ${event.durationMs}ms`;
    case 'TRAVERSAL_COMPLETED':    return `nodes:${p.nodes} edges:${p.edges} ${event.durationMs}ms`;
    case 'LLM_RESPONSE_READY':     return `${p.tokens_in}→${p.tokens_out} tokens ${event.durationMs}ms`;
    case 'REQUEST_COMPLETED':      return `${p.op ?? 'query'} ${event.durationMs}ms`;
    case 'AUTH_LOGIN_SUCCESS':     return `${p.username} (${p.role})`;
    case 'AUTH_LOGIN_FAILED':      return `${p.username} · invalid credentials`;
    default: {
      const keys = Object.keys(p).slice(0, 3);
      return keys.length ? keys.map(k => `${k}:${JSON.stringify(p[k])}`).join(' ') : '—';
    }
  }
}

export function levelClass(level: string): string {
  return { INFO: 'badge-info', WARN: 'badge-warn', ERROR: 'badge-err' }[level] ?? 'badge-neutral';
}
```

### 2. `src/styles/heimdall.css` — добавить (из прототипа)

```css
/* Component badges */
.comp { font-family:var(--mono); font-size:10px; padding:2px 6px;
        border-radius:var(--r-sm); font-weight:500; border:1px solid; }
.comp-hound   { color:var(--suc);  background:color-mix(in srgb,var(--suc)  10%,transparent); border-color:color-mix(in srgb,var(--suc)  25%,transparent); }
.comp-dali    { color:var(--acc);  background:color-mix(in srgb,var(--acc)  10%,transparent); border-color:color-mix(in srgb,var(--acc)  25%,transparent); }
.comp-mimir   { color:#a09ade;     background:color-mix(in srgb,#7F77DD     10%,transparent); border-color:color-mix(in srgb,#7F77DD    25%,transparent); }
.comp-anvil   { color:var(--inf);  background:color-mix(in srgb,var(--inf)  10%,transparent); border-color:color-mix(in srgb,var(--inf)  25%,transparent); }
.comp-shuttle { color:var(--wrn);  background:color-mix(in srgb,var(--wrn)  10%,transparent); border-color:color-mix(in srgb,var(--wrn)  25%,transparent); }
.comp-chur    { color:var(--t2);   background:color-mix(in srgb,var(--t2)   10%,transparent); border-color:color-mix(in srgb,var(--t2)   25%,transparent); }

/* Status badges */
.badge { display:inline-flex; align-items:center; padding:2px 7px;
         border-radius:var(--r-sm); font-size:10px; font-weight:600;
         font-family:var(--mono); letter-spacing:.04em; }
.badge-info    { background:color-mix(in srgb,var(--inf)   14%,transparent); color:var(--inf);    border:1px solid color-mix(in srgb,var(--inf)   30%,transparent); }
.badge-warn    { background:color-mix(in srgb,var(--wrn)   14%,transparent); color:var(--wrn);    border:1px solid color-mix(in srgb,var(--wrn)   30%,transparent); }
.badge-err     { background:color-mix(in srgb,var(--danger)14%,transparent); color:var(--danger); border:1px solid color-mix(in srgb,var(--danger)30%,transparent); }
.badge-neutral { background:var(--bg3); color:var(--t2); border:1px solid var(--bd); }

/* Event 6-col grid */
.event-grid-head, .event-row-grid {
  display:grid;
  grid-template-columns:88px 82px 190px 52px 56px 1fr;
  gap:8px; align-items:center;
}
.event-grid-head { padding:5px 14px; background:var(--bg2); border-bottom:1px solid var(--bd);
                   font-size:11px; font-family:var(--mono); color:var(--t3);
                   text-transform:uppercase; letter-spacing:.05em;
                   position:sticky; top:0; z-index:1; }
.event-row-grid  { padding:6px 14px; border-bottom:1px solid var(--bd);
                   font-size:12px; transition:background .1s; }
.event-row-grid:hover { background:var(--bg2); }
.evt-ts      { font-family:var(--mono); color:var(--t3); font-size:11px; }
.evt-type    { font-family:var(--mono); font-size:11px; color:var(--t1); }
.evt-dur     { font-family:var(--mono); font-size:11px; color:var(--t3); text-align:right; }
.evt-payload { font-family:var(--mono); font-size:11px; color:var(--t3);
               white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.evt-row-error .evt-type { color:var(--danger); }
.evt-row-warn  .evt-type { color:var(--wrn); }
```

### 3. `src/components/EventLog.tsx` — обновить

```tsx
import { Virtuoso } from 'react-virtuoso';
import type { HeimdallEvent } from 'aida-shared';
import { EVENT_LABELS, formatPayload, levelClass } from '../utils/eventFormat';

function EventRow({ event }: { event: HeimdallEvent }) {
  const time = new Date(event.timestamp).toTimeString().slice(0, 8);
  const comp = event.sourceComponent.toLowerCase();
  const rowCls = event.level === 'ERROR' ? 'event-row-grid evt-row-error'
               : event.level === 'WARN'  ? 'event-row-grid evt-row-warn'
               : 'event-row-grid';
  return (
    <div className={rowCls}>
      <span className="evt-ts">{time}</span>
      <span><span className={`comp comp-${comp}`}>{event.sourceComponent}</span></span>
      <span className="evt-type">{EVENT_LABELS[event.eventType] ?? event.eventType}</span>
      <span><span className={`badge ${levelClass(event.level)}`}>{event.level}</span></span>
      <span className="evt-dur">{event.durationMs > 0 ? `${event.durationMs}ms` : '—'}</span>
      <span className="evt-payload">{formatPayload(event)}</span>
    </div>
  );
}

export default function EventLog({ events, height = '100%', connected }: {
  events: HeimdallEvent[]; height?: number | string; connected?: boolean;
}) {
  return (
    <div style={{ display:'flex', flexDirection:'column', height,
                  border:'1px solid var(--bd)', borderRadius:'var(--r-md)', overflow:'hidden' }}>
      <div style={{ display:'flex', alignItems:'center', gap:6,
                    padding:'6px 14px', background:'var(--bg1)',
                    borderBottom:'1px solid var(--bd)', fontSize:11, color:'var(--t3)' }}>
        <span style={{ width:6, height:6, borderRadius:'50%',
                       background: connected ? 'var(--suc)' : 'var(--danger)',
                       display:'inline-block' }} />
        {connected ? `подключён · ${events.length} событий` : 'не подключён'}
      </div>
      <div className="event-grid-head">
        <span>Время</span><span>Компонент</span><span>Событие</span>
        <span>Уровень</span><span>Длит.</span><span>Payload</span>
      </div>
      <Virtuoso style={{ flex:1 }} data={events}
        itemContent={(_, e) => <EventRow event={e} />}
        followOutput="smooth" />
    </div>
  );
}
```

**Checklist H3.5**
```
[ ] eventFormat.ts создан
[ ] heimdall.css: .comp-* .badge-* .event-grid-head добавлены
[ ] EventLog.tsx: 6-col grid, comp badge, formatPayload
[ ] Тест: SHUTTLE query → EventLog: "GraphQL request" badge "shuttle"
```


---

## H3.9 — Chur auth events (~1 ч)

**W1 Apr 20-25**

### 1. `bff/chur/src/middleware/heimdallEmit.ts` (создать)

```typescript
const HEIMDALL_URL = process.env.HEIMDALL_URL ?? 'http://localhost:9093';

export function emitToHeimdall(
  eventType: string,
  level: 'INFO' | 'WARN' | 'ERROR',
  payload: Record<string, unknown>,
  sessionId?: string,
): void {
  fetch(`${HEIMDALL_URL}/events`, {
    method:  'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      timestamp: Date.now(), sourceComponent: 'chur',
      eventType, level,
      sessionId: sessionId ?? null, correlationId: null, durationMs: 0,
      payload,
    }),
    signal: AbortSignal.timeout(2000),
  }).catch(() => {});
}
```

### 2. `bff/chur/src/routes/auth.ts` — добавить 3 вызова

```typescript
import { emitToHeimdall } from '../middleware/heimdallEmit';

// После успешного login:
emitToHeimdall('AUTH_LOGIN_SUCCESS', 'INFO', { username: user.username, role: user.role }, session.id);

// После logout:
emitToHeimdall('AUTH_LOGOUT', 'INFO', { username: user.username }, sid);

// При неверном пароле:
emitToHeimdall('AUTH_LOGIN_FAILED', 'WARN', { username: body.username ?? 'unknown', reason: 'invalid_credentials' });
```

### 3. `EventType.java` — добавить 3 типа

```java
AUTH_LOGIN_SUCCESS, AUTH_LOGIN_FAILED, AUTH_LOGOUT,
```

**Checklist H3.9**
```
[ ] heimdallEmit.ts создан (fire-and-forget, timeout 2s)
[ ] auth.ts: 3 вызова добавлены
[ ] EventType.java: 3 типа добавлены
[ ] Тест: login → WS: AUTH_LOGIN_SUCCESS {username, role}
[ ] Тест: HEIMDALL down → Chur работает нормально
```

---

## H3.4 — Services health (~3 ч)

**W2 Apr 27**

### 1. `ServicesResource.java` (создать)

```java
// services/heimdall-backend/src/main/java/studio/seer/heimdall/resource/ServicesResource.java
@Path("/services")
@Produces(MediaType.APPLICATION_JSON)
public class ServicesResource {

    record ServiceCfg(String name, int port, String healthUrl) {}
    record ServiceStatus(String name, int port, String status, long latencyMs) {}

    private static final List<ServiceCfg> SERVICES = List.of(
        new ServiceCfg("SHUTTLE",  8080, "http://localhost:8080/q/health"),
        new ServiceCfg("Dali",     9090, "http://localhost:9090/q/health"),
        new ServiceCfg("MIMIR",    9091, "http://localhost:9091/health"),
        new ServiceCfg("ANVIL",    9092, "http://localhost:9092/q/health"),
        new ServiceCfg("HEIMDALL", 9093, "http://localhost:9093/q/health"),
        new ServiceCfg("Chur",     3000, "http://localhost:3000/health"),
        new ServiceCfg("YGG",      2480, "http://localhost:2480/api/v1/ready"),
        new ServiceCfg("FRIGG",    2481, "http://localhost:2481/api/v1/ready"),
        new ServiceCfg("Keycloak", 8180, "http://localhost:8180/health/ready")
    );

    @Inject Vertx vertx;

    @GET @Path("/health")
    public Uni<List<ServiceStatus>> getHealth() {
        return Multi.createFrom().items(SERVICES.stream())
            .onItem().transformToUniAndMerge(this::ping)
            .collect().asList();
    }

    private Uni<ServiceStatus> ping(ServiceCfg svc) {
        long start = System.currentTimeMillis();
        return vertx.createHttpClient()
            .request(HttpMethod.GET, svc.port(), "localhost",
                     URI.create(svc.healthUrl()).getPath())
            .flatMap(req -> req.send())
            .map(resp -> new ServiceStatus(svc.name(), svc.port(),
                resp.statusCode() < 500 ? "up" : "degraded",
                System.currentTimeMillis() - start))
            .onFailure().recoverWithItem(
                new ServiceStatus(svc.name(), svc.port(), "down", -1L))
            .ifNoItem().after(Duration.ofSeconds(2))
            .recoverWithItem(new ServiceStatus(svc.name(), svc.port(), "down", -1L));
    }
}
```

### 2. `src/pages/ServicesPage.tsx` (создать)

```tsx
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

interface SvcStatus { name: string; port: number; status: string; latencyMs: number; }

const COMP_MAP: Record<string, string> = {
  SHUTTLE:'shuttle', Dali:'dali', MIMIR:'mimir', ANVIL:'anvil',
  HEIMDALL:'heimdall', Chur:'chur', YGG:'ygg', FRIGG:'frigg',
};

function Card({ svc }: { svc: SvcStatus }) {
  const navigate = useNavigate();
  const dotColor = svc.status==='up' ? 'var(--suc)' : svc.status==='degraded' ? 'var(--wrn)' : 'var(--danger)';
  const latColor = svc.latencyMs < 0 ? 'var(--t3)' : svc.latencyMs > 100 ? 'var(--wrn)' : 'var(--suc)';
  const comp = COMP_MAP[svc.name] ?? svc.name.toLowerCase();
  return (
    <div style={{ background:'var(--bg1)', border:'1px solid var(--bd)',
                  borderRadius:'var(--r-md)', overflow:'hidden' }}>
      <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center',
                    padding:'10px 14px', background:'var(--bg2)',
                    borderBottom:'1px solid var(--bd)' }}>
        <div style={{ display:'flex', alignItems:'center', gap:8 }}>
          <span style={{ width:8, height:8, borderRadius:'50%', background:dotColor, flexShrink:0 }} />
          <div>
            <div style={{ fontWeight:600, fontSize:13 }}>{svc.name}</div>
            <div style={{ fontSize:11, color:'var(--t3)', fontFamily:'var(--mono)' }}>:{svc.port}</div>
          </div>
        </div>
        <div style={{ textAlign:'right' }}>
          <div style={{ fontFamily:'var(--mono)', fontWeight:600, color:latColor, fontSize:13 }}>
            {svc.latencyMs < 0 ? '—' : `${svc.latencyMs}ms`}
          </div>
          <div style={{ fontSize:10, color:'var(--t3)' }}>latency</div>
        </div>
      </div>
      <div style={{ padding:'8px 14px', display:'flex', gap:8, alignItems:'center' }}>
        <span className={`badge badge-${svc.status==='up'?'suc':svc.status==='degraded'?'warn':'err'}`}>
          {svc.status}
        </span>
        <span className={`comp comp-${comp}`} style={{ marginLeft:4 }}>{svc.name}</span>
        <button onClick={() => navigate(`/events?comp=${comp}`)}
          style={{ marginLeft:'auto', fontSize:11, background:'none', border:'1px solid var(--bd)',
                   borderRadius:'var(--r-md)', padding:'3px 8px', cursor:'pointer',
                   color:'var(--t2)', fontFamily:'var(--font)' }}>
          → События
        </button>
      </div>
    </div>
  );
}

export default function ServicesPage() {
  const [services, setServices] = useState<SvcStatus[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetch_ = () =>
      fetch('http://localhost:9093/services/health')
        .then(r => r.json()).then(setServices)
        .catch(() => {}).finally(() => setLoading(false));
    fetch_();
    const id = setInterval(fetch_, 10_000);
    return () => clearInterval(id);
  }, []);

  return (
    <div style={{ padding:24 }}>
      <div style={{ marginBottom:20 }}>
        <div style={{ fontSize:18, fontWeight:700, marginBottom:4 }}>Сервисы</div>
        <div style={{ fontSize:13, color:'var(--t3)' }}>
          Health и конфигурация компонентов AIDA
        </div>
      </div>
      {loading ? (
        <div style={{ color:'var(--t3)', fontSize:13 }}>Подключение…</div>
      ) : (
        <div style={{ display:'grid', gridTemplateColumns:'1fr 1fr', gap:14 }}>
          {services.map(s => <Card key={s.name} svc={s} />)}
        </div>
      )}
    </div>
  );
}
```

### 3. `App.tsx` — добавить route + tab

```tsx
import ServicesPage from './pages/ServicesPage';
// tabs: добавить { to: '/services', label: 'Сервисы' }
// routes: <Route path="/services" element={<ServicesPage />} />
```

**Checklist H3.4**
```
[ ] ServicesResource.java создан
[ ] GET /services/health возвращает все 9 сервисов
[ ] Недоступные → status:"down", latencyMs:-1
[ ] ServicesPage.tsx создана, route + tab добавлены
[ ] Тест: открыть /services → реальный up/down/latency
[ ] Тест: остановить SHUTTLE → карточка "down"
```


---

## H3.1 — Dashboard расширение (~2 ч)

**W2 Apr 27**

### 1. `src/components/ServiceHealthStrip.tsx` (создать)

```tsx
import { useState, useEffect } from 'react';

const STRIP = ['SHUTTLE', 'Chur', 'HEIMDALL', 'YGG'];

export default function ServiceHealthStrip() {
  const [svcs, setSvcs] = useState<{name:string;port:number;status:string;latencyMs:number}[]>([]);

  useEffect(() => {
    const fetch_ = () =>
      fetch('http://localhost:9093/services/health')
        .then(r => r.json())
        .then((all: any[]) => setSvcs(all.filter(s => STRIP.includes(s.name))))
        .catch(() => {});
    fetch_(); const id = setInterval(fetch_, 10_000);
    return () => clearInterval(id);
  }, []);

  if (!svcs.length) return null;

  return (
    <div style={{ display:'grid', gridTemplateColumns:`repeat(${svcs.length},1fr)`,
                  gap:10, marginBottom:20 }}>
      {svcs.map(s => {
        const c = s.status==='up'?'var(--suc)':s.status==='degraded'?'var(--wrn)':'var(--danger)';
        const lat = s.latencyMs < 0 ? '—'
                  : s.latencyMs > 100 ? `${s.latencyMs}ms` : `${s.latencyMs}ms`;
        const latColor = s.latencyMs < 0 ? 'var(--t3)' : s.latencyMs > 100 ? 'var(--wrn)' : 'var(--suc)';
        return (
          <div key={s.name} style={{ background:'var(--bg1)', border:'1px solid var(--bd)',
                borderRadius:'var(--r-md)', padding:'10px 14px',
                display:'flex', alignItems:'center', gap:10 }}>
            <span style={{ width:8, height:8, borderRadius:'50%', background:c, flexShrink:0 }} />
            <div style={{ flex:1, minWidth:0 }}>
              <div style={{ fontWeight:600, fontSize:12 }}>{s.name}</div>
              <div style={{ fontSize:11, color:'var(--t3)', fontFamily:'var(--mono)' }}>:{s.port}</div>
            </div>
            <div style={{ fontFamily:'var(--mono)', fontSize:12, color:latColor }}>{lat}</div>
          </div>
        );
      })}
    </div>
  );
}
```

### 2. `src/components/RecentErrors.tsx` (создать)

```tsx
import type { HeimdallEvent } from 'aida-shared';
import { formatPayload } from '../utils/eventFormat';
import { useNavigate } from 'react-router-dom';

export default function RecentErrors({ events }: { events: HeimdallEvent[] }) {
  const navigate = useNavigate();
  const errors = events.filter(e => e.level === 'ERROR').slice(0, 5);
  return (
    <div style={{ background:'var(--bg1)', border:'1px solid var(--bd)',
                  borderRadius:'var(--r-md)', overflow:'hidden' }}>
      <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center',
                    padding:'8px 14px', background:'var(--bg2)', borderBottom:'1px solid var(--bd)' }}>
        <span style={{ fontSize:11, color:'var(--t3)', textTransform:'uppercase', letterSpacing:'.06em' }}>
          Последние ошибки
        </span>
        <button onClick={() => navigate('/events')}
          style={{ fontSize:11, color:'var(--acc)', background:'none',
                   border:'none', cursor:'pointer' }}>
          → Все события
        </button>
      </div>
      {!errors.length ? (
        <div style={{ padding:'14px', fontSize:12, color:'var(--t3)' }}>Ошибок нет</div>
      ) : errors.map((e, i) => (
        <div key={i} style={{ display:'grid', gridTemplateColumns:'88px 82px 1fr',
                              gap:8, padding:'6px 14px',
                              borderBottom: i < errors.length-1 ? '1px solid var(--bd)' : 'none' }}>
          <span style={{ fontFamily:'var(--mono)', color:'var(--t3)', fontSize:11 }}>
            {new Date(e.timestamp).toTimeString().slice(0,8)}
          </span>
          <span><span className={`comp comp-${e.sourceComponent.toLowerCase()}`}>
            {e.sourceComponent}
          </span></span>
          <span style={{ color:'var(--danger)', fontFamily:'var(--mono)', fontSize:11,
                         overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap' }}>
            {formatPayload(e)}
          </span>
        </div>
      ))}
    </div>
  );
}
```

### 3. `src/pages/DashboardPage.tsx` — обновить

```tsx
import ServiceHealthStrip from '../components/ServiceHealthStrip';
import RecentErrors        from '../components/RecentErrors';

// В JSX:
<ServiceHealthStrip />
<MetricsBar metrics={metrics} />
<div style={{ display:'grid', gridTemplateColumns:'1fr 1fr', gap:16 }}>
  <ThroughputChart events={events} />
  <RecentErrors events={events} />
</div>
<EventLog events={events.slice(0, 20)} connected={connected} height={280} />
```

**Checklist H3.1**
```
[ ] ServiceHealthStrip.tsx создан (4 карточки)
[ ] RecentErrors.tsx создан
[ ] DashboardPage.tsx обновлён
[ ] Тест: открыть dashboard → полоса сервисов вверху
[ ] Тест: ERROR событие → RecentErrors показывает его
```

---

## H3.7 — Presentation mode (~1.5 ч)

**W2 Apr 27**

### `src/components/PresentationMode.tsx` (создать)

```tsx
import { useEffect } from 'react';
import type { HeimdallEvent } from 'aida-shared';
import { EVENT_LABELS } from '../utils/eventFormat';

const COMP_COLORS: Record<string, string> = {
  hound:'var(--suc)', shuttle:'var(--wrn)', dali:'var(--acc)',
  mimir:'#a09ade', anvil:'var(--inf)', chur:'var(--t2)',
};

function BigMetric({ label, value, color }: { label:string; value:string|number; color?:string }) {
  return (
    <div style={{ textAlign:'center' }}>
      <div style={{ fontSize:72, fontWeight:700, fontFamily:'var(--mono)',
                    color: color ?? 'var(--t1)', lineHeight:1.1, letterSpacing:'-.02em' }}>
        {typeof value === 'number' ? value.toLocaleString() : value}
      </div>
      <div style={{ fontSize:13, color:'var(--t3)', textTransform:'uppercase',
                    letterSpacing:'.08em', marginTop:10 }}>
        {label}
      </div>
    </div>
  );
}

export default function PresentationMode({ metrics, events, onExit }: {
  metrics: any; events: HeimdallEvent[]; onExit: () => void;
}) {
  useEffect(() => {
    const h = (e: KeyboardEvent) => { if (e.key === 'Escape') onExit(); };
    window.addEventListener('keydown', h);
    return () => window.removeEventListener('keydown', h);
  }, [onExit]);

  const rate = metrics?.resolutionRate ?? 0;
  const rateColor = rate >= 0.85 ? 'var(--suc)' : rate >= 0.70 ? 'var(--wrn)' : 'var(--danger)';

  return (
    <div style={{ position:'fixed', inset:0, zIndex:1000, background:'var(--bg0)',
                  display:'grid', gridTemplateRows:'56px 1fr auto', padding:'0 48px 32px' }}>
      {/* Header */}
      <div style={{ display:'flex', alignItems:'center', justifyContent:'space-between' }}>
        <span style={{ fontFamily:'var(--font-display)', fontSize:16, fontWeight:800,
                       color:'var(--acc)', letterSpacing:'.08em' }}>
          HEIMDALL
        </span>
        <button onClick={onExit}
          style={{ background:'none', border:'1px solid var(--bd)', borderRadius:'var(--r-md)',
                   color:'var(--t3)', padding:'4px 12px', fontSize:12, cursor:'pointer',
                   fontFamily:'var(--font)' }}>
          ESC
        </button>
      </div>

      {/* Three big metrics */}
      <div style={{ display:'grid', gridTemplateColumns:'repeat(3,1fr)', gap:32, alignContent:'center' }}>
        <BigMetric label="Atoms extracted"  value={metrics?.atomsExtracted ?? 0} color="var(--acc)" />
        <BigMetric label="Resolution rate"  value={`${Math.round(rate*100)}%`}   color={rateColor} />
        <BigMetric label="Files parsed"     value={metrics?.filesParsed ?? 0}    color="var(--t2)" />
      </div>

      {/* Last 8 events — fade */}
      <div style={{ display:'flex', flexDirection:'column', gap:6 }}>
        {events.slice(0, 8).map((e, i) => {
          const comp = e.sourceComponent.toLowerCase();
          return (
            <div key={i} style={{ display:'flex', gap:16, alignItems:'baseline', opacity: 1 - i*0.1 }}>
              <span style={{ fontFamily:'var(--mono)', fontSize:12, color:'var(--t3)', minWidth:70 }}>
                {new Date(e.timestamp).toTimeString().slice(0,8)}
              </span>
              <span style={{ fontFamily:'var(--mono)', fontSize:12,
                             color: COMP_COLORS[comp] ?? 'var(--t3)', minWidth:80 }}>
                {e.sourceComponent}
              </span>
              <span style={{ fontFamily:'var(--mono)', fontSize:14, color:'var(--t1)' }}>
                {EVENT_LABELS[e.eventType] ?? e.eventType}
              </span>
            </div>
          );
        })}
      </div>
    </div>
  );
}
```

### Подключить в `DashboardPage.tsx`

```tsx
import PresentationMode from '../components/PresentationMode';
const [presentation, setPresentation] = useState(false);

// В nav toolbar (рядом с theme кнопкой):
<button onClick={() => setPresentation(true)} title="Presentation mode"
  className="btn-tool">⛶</button>

// В конце JSX:
{presentation && (
  <PresentationMode metrics={metrics} events={events} onExit={() => setPresentation(false)} />
)}
```

**Checklist H3.7**
```
[ ] PresentationMode.tsx создан
[ ] Кнопка ⛶ в toolbar
[ ] ESC → выход
[ ] Три метрики 72px с живыми данными
[ ] 8 событий с fade opacity
```

---

## H3.8 — HoundHeimdallListener (~2 ч)

**W3 May 4-9 — разблокируется после C.1.3**

### 1. `HoundHeimdallListener.java` (создать)

```java
// libraries/hound/src/main/java/com/hound/heimdall/HoundHeimdallListener.java
package com.hound.heimdall;

import com.aida.shared.HoundEventListener;
import com.aida.shared.ParseResult;
import java.net.URI; import java.net.http.*;
import java.time.Duration; import java.util.Map;
import java.util.stream.Collectors;

public class HoundHeimdallListener implements HoundEventListener {

    private static final String URL =
        System.getProperty("heimdall.url", "http://localhost:9093") + "/events";
    private static final int THROTTLE = 100;  // emit ATOM_EXTRACTED каждые 100 атомов

    private final HttpClient http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(2)).build();

    @Override public void onFileParseStarted(String file) {
        emit("FILE_PARSING_STARTED", "INFO", Map.of("file", shortName(file), "dialect", "plsql"));
    }
    @Override public void onAtomExtracted(String file, int n) {
        if (n % THROTTLE != 0) return;
        emit("ATOM_EXTRACTED", "INFO", Map.of("file", shortName(file), "atomCount", n));
    }
    @Override public void onFileParseCompleted(String file, ParseResult r) {
        emit("FILE_PARSING_COMPLETED", r.success() ? "INFO" : "ERROR",
            Map.of("file", shortName(file), "atomCount", r.atomCount(),
                   "resolutionRate", Double.isNaN(r.resolutionRate()) ? 0.0 : r.resolutionRate(),
                   "success", r.success()));
        if (r.success() && r.atomCount() > 0) {
            int resolved = (int)(r.atomCount() * (Double.isNaN(r.resolutionRate()) ? 0 : r.resolutionRate()));
            emit("RESOLUTION_COMPLETED", "INFO",
                Map.of("resolved", resolved, "total", r.atomCount(), "resolutionRate", r.resolutionRate()));
        }
    }
    @Override public void onError(String file, Throwable ex) {
        emit("FILE_PARSING_FAILED", "ERROR",
            Map.of("file", shortName(file), "error", ex.getClass().getSimpleName(),
                   "message", truncate(ex.getMessage(), 200)));
    }

    private void emit(String type, String level, Map<String, Object> payload) {
        try {
            String body = "{\"timestamp\":" + System.currentTimeMillis()
                + ",\"sourceComponent\":\"hound\",\"eventType\":\"" + type
                + "\",\"level\":\"" + level + "\",\"correlationId\":null,\"durationMs\":0,"
                + "\"payload\":{"
                + payload.entrySet().stream()
                    .map(e -> "\"" + e.getKey() + "\":" + toJson(e.getValue()))
                    .collect(Collectors.joining(","))
                + "}}";
            http.sendAsync(
                HttpRequest.newBuilder().uri(URI.create(URL))
                    .header("Content-Type","application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(2)).build(),
                HttpResponse.BodyHandlers.discarding()
            ).whenComplete((r, ex) -> {
                if (ex != null)
                    System.getLogger(getClass().getName()).log(
                        System.Logger.Level.WARNING,
                        "HEIMDALL emit failed: {0}", ex.getMessage());
            });
        } catch (Exception ignored) { /* никогда не ломаем Hound */ }
    }

    private String toJson(Object v) {
        if (v instanceof String s) return "\"" + s.replace("\"","\\\"") + "\"";
        if (v instanceof Boolean b) return b.toString();
        return String.valueOf(v);
    }
    private String shortName(String p) {
        int i = Math.max(p.lastIndexOf('/'), p.lastIndexOf('\\'));
        return i >= 0 ? p.substring(i+1) : p;
    }
    private String truncate(String s, int max) {
        return s == null ? "" : s.length() > max ? s.substring(0,max) : s;
    }
}
```

### 2. `CompositeListener.java` (создать)

```java
// libraries/hound/src/main/java/com/hound/heimdall/CompositeListener.java
package com.hound.heimdall;
import com.aida.shared.HoundEventListener; import com.aida.shared.ParseResult;

public record CompositeListener(HoundEventListener a, HoundEventListener b)
    implements HoundEventListener {
    @Override public void onFileParseStarted(String f)          { safe(()->a.onFileParseStarted(f));          safe(()->b.onFileParseStarted(f)); }
    @Override public void onAtomExtracted(String f, int n)      { safe(()->a.onAtomExtracted(f,n));           safe(()->b.onAtomExtracted(f,n)); }
    @Override public void onFileParseCompleted(String f, ParseResult r) { safe(()->a.onFileParseCompleted(f,r)); safe(()->b.onFileParseCompleted(f,r)); }
    @Override public void onError(String f, Throwable e)        { safe(()->a.onError(f,e));                   safe(()->b.onError(f,e)); }
    private void safe(Runnable r) { try { r.run(); } catch (Exception ignored) {} }
}
```

### 3. `HoundParserImpl` — добавить `buildEffectiveListener`

```java
private HoundEventListener buildEffectiveListener(HoundEventListener external) {
    var heimdall = new HoundHeimdallListener();
    return external == null ? heimdall : new CompositeListener(external, heimdall);
}

// В parse() и parseBatch() — заменить listener на:
HoundEventListener effective = buildEffectiveListener(listener);
```

**Checklist H3.8**
```
[ ] HoundHeimdallListener.java создан
[ ] CompositeListener.java создан
[ ] HoundParserImpl.buildEffectiveListener() добавлен
[ ] ./gradlew :libraries:hound:runBatch → WS: FILE_PARSING_* + ATOM_EXTRACTED
[ ] /metrics/snapshot → atomsExtracted > 0
[ ] ResolutionGauge показывает реальный %
[ ] Тест: HEIMDALL down → Hound парсит, только WARN в лог
[ ] Тест: ATOM_EXTRACTED throttle — только каждые 100
```

---

## Итоговый чеклист спринта

```
W1 Apr 20-25:
  [ ] H3.5 EventLog 6-col + badges + payload
  [ ] H3.9 Chur auth events

W2 Apr 27:
  [ ] H3.4 Services health (backend + page)
  [ ] H3.1 Dashboard: ServiceHealthStrip + RecentErrors
  [ ] H3.7 Presentation mode

W3 May 4-9 (после C.1.3):
  [ ] H3.8 HoundHeimdallListener + CompositeListener

Integration:
  [ ] login → EventLog: AUTH_LOGIN_SUCCESS
  [ ] SHUTTLE query → EventLog: REQUEST_RECEIVED
  [ ] Hound run → MetricsBar обновляется
  [ ] /services → реальный up/down/latency всех сервисов
  [ ] ⛶ → fullscreen с живыми данными
```

---

## История изменений

| Дата | Версия | Что |
|---|---|---|
| 12.04.2026 | 3.0 | Полная перезапись. Только живые данные. Полный код 6 задач. Dali/Users/Demo mode явно исключены. |
| 12.04.2026 | 2.0 | Gap analysis с прототипом. |
| 12.04.2026 | 1.0 | Initial. |
