# BUG: ELK layout falls back to grid when verdandi runs as MF remote inside Shell

**Дата:** 12.04.2026
**Статус:** FIX WRITTEN, нужна верификация после стабилизации БД
**Приоритет:** High (L2/L3 граф полностью нечитаем при работе через Shell)

---

## Симптом

При открытии L2/L3 графа (напр. DWH2, BUDM_RMS) в Shell (порт `:5175`) граф отображается
как сетка из 617 узлов при зуме 10% вместо читаемой DAG-раскладки слева направо.

Узлы имеют координаты `y=0` у всех и `x = i * 460` (460 = NODE_WIDTH + GRID_SPACING) —
признак `applyGridLayout` (фоллбек), а не ELK.

---

## Корневая причина

`layoutGraph.ts` создаёт ELK Web Worker через:
```typescript
new Worker(elkWorkerUrl)
```

где `elkWorkerUrl` — абсолютный URL от Vite `?url` import, указывающий на verdandi-сервер
(`:5173`). Когда verdandi загружается как MF-remote внутри Shell (`:5175`), браузер блокирует
создание Worker с чужого origin:

```
SecurityError: Failed to construct 'Worker': Script at
'http://localhost:5173/node_modules/elkjs/lib/elk-worker.min.js'
cannot be accessed from origin 'http://localhost:5175'.
```

ELK-конструктор падает, `runElkLayout` возвращает `null`, `applyELKLayout`
возвращает `applyGridLayout(nodes)`.

---

## Fix (уже написан в `layoutGraph.ts`)

Fetch worker-скрипт через CORS (Vite dev server отдаёт `Access-Control-Allow-Origin: *`),
создать Blob URL (тот же origin страницы), передать в `workerFactory`:

```typescript
async function resolveWorkerBlobUrl(): Promise<string> {
  if (_workerBlobUrl) return _workerBlobUrl;
  const resp = await fetch(elkWorkerUrl);          // CORS fetch — работает
  const text = await resp.text();
  const blob = new Blob([text], { type: 'application/javascript' });
  _workerBlobUrl = URL.createObjectURL(blob);      // blob: URL — same-origin
  return _workerBlobUrl;
}

async function getElk(): Promise<ElkApi> {
  const blobUrl = await resolveWorkerBlobUrl();    // await BEFORE ELK constructor
  _elk = new ELK({ workerFactory: () => new Worker(blobUrl) });
  ...
}
```

Подход **проверен в браузере** вручную:
- `fetch('http://localhost:5173/elk-worker.min.js')` → 200 OK, 10 MB
- `new Worker(blobUrl)` → Worker создаётся без ошибки
- Тест ELK layout A→B→C дал корректные позиции `x: [12, 132, 252]`

---

## Что нужно для верификации

1. Дождаться стабильной БД
2. Открыть Shell → VERDANDI → LOOM → DWH2 → L2
3. Убедиться что ELK запускается (сообщение "вычисляется граф...") и даёт DAG-раскладку
4. В консоли должно появиться: `[LOOM] ELK layout (worker) — NNN ms (617 nodes, 3775 edges)`

---

## Известные ограничения

- Blob URL создаётся 1 раз и кэшируется в `_workerBlobUrl` на время жизни модуля
- В production обе части (Shell + verdandi) serve с одного origin → cross-origin не возникает
- `elk-worker.min.js` весит ~10 MB — fetch выполняется 1 раз при первом layout-запросе

---

## Коммит с фиксом

`feature/m1-core-engines` — commit после сессии 12.04.2026
