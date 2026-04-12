# SPIKE: ELK Web Worker — активация готового воркера

**Документ:** `SPIKE_ELK_WORKER`
**Версия:** 1.0
**Дата:** 12.04.2026
**Ветка:** `feat/elk-web-worker`
**Статус:** In progress

---

## Контекст

ELK запускается на main thread, вызывая freeze UI 2-5с при 500-1000 нодах. Для HighLoad++ demo нужно 5K нодов (performance gap: сейчас тестируется на ~10, нужно ~5K). Spike из PROJECT_ROADMAP риск #2: "ELK Web Worker spike в апреле".

Воркер уже написан и инфраструктура готова:
- `elkWorker.ts` — содержит fix Vite CJS→ESM (`(self as any).Worker = undefined`), протокол `{ id, graph } → { id, result/error }` готов
- `vite.config.ts` — `worker: { format: 'es' }` + `optimizeDeps.include: ['elkjs/lib/elk.bundled.js']` уже настроены

Единственное что отсутствует — подключение воркера в `layoutGraph.ts`. Комментарий про "Vite CJS→ESM limitation" устарел.

---

## Задача

Модифицировать **`frontends/verdandi/src/utils/layoutGraph.ts`**:

### 1. Заменить singleton `_elkMain` на worker

Убрать:
```typescript
let _elkMain: ElkApi | null = null;
async function getElk(): Promise<ElkApi> { ... }
```

Добавить:
```typescript
let _elkWorker: Worker | null = null;
let _reqCounter = 0;
const _pending = new Map<number, {
  resolve: (r: ElkGraph & { children: ElkNode[] }) => void;
  reject: (e: Error) => void;
}>();

function getElkWorker(): Worker {
  if (!_elkWorker) {
    _elkWorker = new Worker(
      new URL('../workers/elkWorker.ts', import.meta.url),
      { type: 'module' },
    );
    _elkWorker.onmessage = (e: MessageEvent<{ id: number; result?: ElkGraph & { children: ElkNode[] }; error?: string }>) => {
      const { id, result, error } = e.data;
      const p = _pending.get(id);
      if (!p) return;
      _pending.delete(id);
      if (error) p.reject(new Error(error));
      else p.resolve(result!);
    };
  }
  return _elkWorker;
}
```

### 2. Обновить `cancelPendingLayouts`

```typescript
export function cancelPendingLayouts(): void {
  _pending.forEach(({ reject }) => reject(new Error('cancelled')));
  _pending.clear();
}
```

### 3. Обновить `runElkLayout` — postMessage в worker

```typescript
async function runElkLayout(graph: ElkGraph): Promise<(ElkGraph & { children: ElkNode[] }) | null> {
  const t0 = performance.now();
  try {
    const id = ++_reqCounter;
    const worker = getElkWorker();
    const result = await Promise.race([
      new Promise<ElkGraph & { children: ElkNode[] }>((resolve, reject) => {
        _pending.set(id, { resolve, reject });
        worker.postMessage({ id, graph });
      }),
      new Promise<never>((_, reject) =>
        setTimeout(() => {
          _pending.delete(id);
          reject(new Error(`ELK layout timed out after ${LAYOUT_TIMEOUT / 1000}s`));
        }, LAYOUT_TIMEOUT),
      ),
    ]);
    const ms = (performance.now() - t0).toFixed(0);
    console.info(`[LOOM] ELK layout (worker) — ${ms} ms  (${graph.children.length} nodes, ${graph.edges.length} edges)`);
    return result;
  } catch (err) {
    const ms = (performance.now() - t0).toFixed(0);
    console.warn(`[LOOM] ELK layout failed after ${ms} ms, using grid fallback`, err);
    return null;
  }
}
```

### 4. Убрать устаревший комментарий (строки 111-122 в layoutGraph.ts)

Заменить блок "ELK engine" на актуальный — воркер активен.

---

## Критические файлы

- `frontends/verdandi/src/utils/layoutGraph.ts` — основная правка
- `frontends/verdandi/src/workers/elkWorker.ts` — без изменений, уже готов
- `frontends/verdandi/vite.config.ts` — без изменений, уже настроен

---

## Верификация

1. Открыть LOOM в dev-режиме, загрузить граф
2. В console: `[LOOM] ELK layout (worker) — Xms` (не "main-thread")
3. DevTools → Performance: layout не блокирует main thread
4. Load test — нужен датасет 500+ нодов (после C.1 Hound + C.0 ArcadeDB → Dali)
5. После успешной верификации: обновить WARN-01 `🟡 → ✅` в `docs/reviews/METRICS_LOG.md`

---

## Документы к обновлению после выполнения

| Файл | Что |
|---|---|
| `docs/architecture/REFACTORING_PLAN.md` | C.4.0 ELK Worker activation — отметить DONE |
| `docs/reviews/METRICS_LOG.md` | WARN-01 статус `🟡 → ✅` |
