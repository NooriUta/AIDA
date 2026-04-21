# LOOM Layout Performance

**Версия:** 1.0  
**Дата:** 2026-04-20  
**Задачи:** EK-01, HOUND 2076N

---

## Алгоритмы раскладки

| Условие | Алгоритм | Описание |
|---------|----------|----------|
| `E/V > DENSE_GRAPH_RATIO (5)` | `stress` | Силовой алгоритм для плотных графов |
| Базовый случай | `layered` | Иерархический, направление RIGHT |
| `nodes > LARGE_GRAPH_THRESHOLD (500)` | `layered` + `LINEAR_SEGMENTS` | Экономичное размещение |
| `nodes > AUTO_GRID_THRESHOLD (800)` | Grid O(n) | ELK пропускается полностью |

---

## Константы (`constants.ts → LAYOUT`)

| Константа | Значение | Назначение |
|-----------|----------|------------|
| `LARGE_GRAPH_THRESHOLD` | 500 | Переключение стратегии ELK на LINEAR_SEGMENTS |
| `AUTO_GRID_THRESHOLD` | 800 | Пропустить ELK → grid |
| `DENSE_GRAPH_RATIO` | 5 | E/V > 5 → stress algorithm (EK-01) |
| `STRESS_EDGE_LENGTH` | 150 | Желаемая длина ребра для stress |
| `PERF_VIRTUALIZE_THRESHOLD` | 1500 | Включить `onlyRenderVisibleElements` в ReactFlow |
| `ELK_TIMEOUT_LARGE` | 8000 мс | Timeout при nodes > 1000 |
| `TIMEOUT_MS` | 15000 мс | Стандартный timeout ELK |

---

## EK-01: Dense Graph Detection

`applyELKLayout` вычисляет `edgeRatio = edges.length / nodes.length`. Если `edgeRatio > DENSE_GRAPH_RATIO (5)`, применяется `stress` вместо `layered`.

```typescript
const edgeRatio = edges.length / nodes.length;
const isDense   = edgeRatio > LAYOUT.DENSE_GRAPH_RATIO;
```

`isDense` возвращается в `LayoutResult` и логируется через `useLoomLayout`.

---

## HOUND 2076N: Виртуализация ReactFlow

При `nodes.length > PERF_VIRTUALIZE_THRESHOLD (1500)` в `LoomCanvas.tsx`:

```tsx
<ReactFlow
  onlyRenderVisibleElements={nodes.length > LAYOUT.PERF_VIRTUALIZE_THRESHOLD}
  ...
/>
```

Для 2076 нод: grid-раскладка (узлы > 800) + виртуализация DOM → рендер < 3s.

---

## Измерение производительности (DevTools)

В `applyELKLayout` добавлены performance marks:

```typescript
performance.mark('loom-layout-start');
// ... grid или ELK раскладка ...
performance.mark('loom-layout-end');
performance.measure('loom-layout', 'loom-layout-start', 'loom-layout-end');
```

Открыть DevTools → Performance → Timings → найти `loom-layout`.

---

## Fallback: Grid после ошибки ELK

Если ELK timeout или исключение — `runElkLayout` возвращает `null` → автоматический grid fallback:

```typescript
if (!result) {
  const laid = applyGridLayout(nodes);
  return { nodes: laid, isGrid: true, isDense: false };
}
```
