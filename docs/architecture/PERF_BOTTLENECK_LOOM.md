# LOOM L2/L3 — Анализ быстродействия и узкие места

**Дата:** 12.04.2026  
**Контекст:** Тормоза начинаются при ~1600 узлов (без колонок) / 17K рёбер  
**Измеренные данные:** 430N / 709E → ELK 524ms (кэш) / 1800ms (холодный)  
**Оценка проблемного сценария:** 1600N / 17KE → ELK 15–60s + рендер 2–4s

---

## Таблица 1 — Стадии пайплайна

| # | Стадия | Поток | @430N/709E | @1.6KN/17KE (оценка) | Сложность |
|---|---|---|---|---|---|
| 1 | ArcadeDB gremlin query | JVM | ~100ms | ~400ms | O(V+E) scan |
| 2 | Chur proxy + React Query | Main | ~50ms | ~200ms | O(V+E) JSON parse |
| 3 | `transformExplore` | Main | ~20ms | ~80ms | O(V+E) alloc |
| 4 | `useDisplayGraph` 7 фаз | Main (memoized) | ~10ms | ~40ms | 7×O(V+E) |
| 5 | `graphFingerprint` | Main | ~2ms | ~35ms | O(E log E) sort |
| 6 | ELK graph construction | Main | ~2ms | ~10ms | O(V+E) filter |
| 7 | **ELK layout** | **Worker** | **524–1800ms** | **🔴 15–60s** | см. Таблицу 2 |
| 8 | posMap merge | Main | ~1ms | ~3ms | O(V) |
| 9 | `setNodes/setEdges` React | Main | ~5ms | ~20ms | O(V+E) diff |
| 10 | **React Flow render** | Renderer | ~50ms | **🔴 2–4s** | см. Таблицу 3 |
| 11 | BFS dimming (на фильтр) | Main | ~5ms | ~50ms | O(V+E) BFS |

---

## Таблица 2 — ELK алгоритм (Worker)

Граф для ELK — только `DATA_FLOW_FOR_LAYOUT` рёбра:
`READS_FROM`, `WRITES_TO`, `DATA_FLOW`, `FILTER_FLOW`, `JOIN_FLOW`, `UNION_FLOW`, `ATOM_PRODUCES`.  
Из 17K рёбер подходят ~30–50%, т.е. ELK получает ориентировочно **5–9K рёбер**.

| Фаза ELK | Алгоритм | Сложность | @1.6KN/9KE | Статус |
|---|---|---|---|---|
| Cycle removal | DFS-based | O(V+E) | ~5ms | ✅ |
| Layer assignment | Longest path | O(V+E) | ~5ms | ✅ |
| **Crossing minimization** | **LAYER_SWEEP** | **O(E × L² × sweeps)** | **~10–50s** | **🔴 главный bottleneck** |
| Node placement | LINEAR_SEGMENTS (>500N) | O(V²) | ~500ms | 🟡 |
| Edge routing | Orthogonal splines | O(E) | ~50ms | ✅ |

> **Ключевая проблема — плотность графа:**  
> При 430N/709E плотность = **1.65 рёбер/узел**.  
> При 1600N/17KE плотность = **10.6 рёбер/узел**.  
> LAYER_SWEEP работает на порядок хуже на плотных графах — слои шире,  
> скрещиваний на порядок больше, число sweeps растёт.  
>  
> `LARGE_GRAPH_THRESHOLD = 500` активирует LINEAR_SEGMENTS для node placement,  
> но crossing minimization это **не облегчает**.

---

## Таблица 3 — React Flow рендер

| Элемент | @430N/709E | @1.6KN/17KE | Примечание |
|---|---|---|---|
| Node DOM elements | 430 | 1600 | ReactFlow v12 virtualizates nodes (только viewport) ✅ |
| **Edge SVG `<path>`** | 709 | **17K** | **НЕТ виртуализации** — все пути в DOM 🔴 |
| Initial paint | ~50ms | **2–4s** | Layout thrash: 17K getBBox + path calc |
| Re-render (zoom/pan) | ~16ms | **~200ms** | Каждое движение → 17K path пересчёт |

---

## Таблица 4 — Mitigation план (по приоритету)

| # | Bottleneck | Решение | Speedup | Усилие |
|---|---|---|---|---|
| **M-1** | ELK: лишние рёбра | Дедупликация до 1 ребра на (src, tgt) перед ELK — уже есть в compound path, добавить в flat path тоже | 3–5× ELK | XS |
| **M-2** | ELK: плотный граф | Auto-detect: если `E/V > 5` → переключить на `elk.algorithm: stress` или `mrtree` (нет crossing min шага) | 10× ELK | S |
| **M-3** | ELK: large graph | Auto-fallback на grid при V>800 без ожидания timeout — показать `⚠ Граф усечён, layout не вычислен. [Запустить полный layout]` | UX win | XS |
| **M-4** | React Flow: 17K edges | Edge virtualization: `useEdgesVisible()` — рендерить только рёбра с обоими endpoints в текущем viewport | 10× render | L |
| **M-5** | React Flow: edge count | `tableLevelView: true` авто при V>500 — убрать CF-рёбра, оставить только table-flow (~17K → ~3.5K) | 5× edges | XS |
| **M-6** | fingerprint: sort | Rolling hash вместо `.sort().join(',')` для 17K строк | 5× fingerprint | XS |
| **M-7** | ELK: timeout | Снизить `TIMEOUT_MS` до 8s при V>1000 + grid + уведомление пользователю | UX | XS |

---

## Рекомендуемая очередь реализации

### Быстрые победы (XS — один день)
1. **M-3** — авто-grid + кнопка "вычислить layout" при V>800
2. **M-7** — снизить TIMEOUT_MS для больших графов
3. **M-5** — авто `tableLevelView` при V>500 (убрает CF edges из React Flow)
4. **M-1** — дедупликация рёбер перед ELK в flat path
5. **M-6** — rolling hash для fingerprint

### Среднесрочно (S — 2–3 дня)
6. **M-2** — auto-detect плотности и переключение ELK алгоритма

### Архитектурно (L — отдельный спринт)
7. **M-4** — edge virtualization в React Flow (требует кастомного EdgeRenderer)

---

## Файлы для изменений

| Файл | Что менять |
|---|---|
| `frontends/verdandi/src/utils/constants.ts` | `LARGE_GRAPH_THRESHOLD`, `TIMEOUT_MS`, добавить `AUTO_GRID_THRESHOLD`, `DENSE_GRAPH_RATIO` |
| `frontends/verdandi/src/utils/layoutGraph.ts` | M-1 дедупликация, M-2 алгоритм, M-3 авто-grid, M-6 fingerprint |
| `frontends/verdandi/src/hooks/canvas/useLoomLayout.ts` | M-3 уведомление, M-7 timeout |
| `frontends/verdandi/src/hooks/canvas/useDisplayGraph.ts` | M-5 авто tableLevelView |
