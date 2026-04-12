# VERDANDI — План: оставшиеся задачи Phase 3

**Дата:** 07.04.2026
**Статус Phase 3:** 10/12 задач завершено (83%)
**Базовый коммит:** `1590188` (master)

---

## Сводка оставшегося

| # | Задача | Приоритет | Оценка | Зависимости |
|---|--------|-----------|--------|-------------|
| F-01 | **LOOM-028: KNOT Inspector** — правая панель деталей ноды в LOOM | 🔴 Высокий | 4–6 ч | — |
| F-02 | **LOOM-029: Context menu** — right-click меню на нодах | 🟡 Средний | 2–3 ч | F-01 |
| F-03 | **LOOM-030: Export PNG** — экспорт canvas | 🟡 Средний | 2–3 ч | — |
| R-01 | Рефакторинг LoomCanvas (663 LOC → ~200 + хуки) | 🟢 Низкий | 3–4 ч | — |
| R-02 | Split transformGraph.ts на 3 файла | 🟢 Низкий | 2–3 ч | — |
| R-03 | Unit-тесты transformGraph (Vitest) | 🟢 Низкий | 4–6 ч | R-02 |
| R-04 | Удалить L1NodesProto.tsx (устаревший прототип) | 🟢 Низкий | 15 мин | — |

---

## F-01 — LOOM-028: KNOT Inspector (правая панель)

### Описание

Правая боковая панель в LOOM, которая появляется при выборе ноды на canvas. **Не путать с KNOT Report** (отдельная страница `/knot` для разбора сессий). KNOT Inspector — это inline-инструмент контекстного просмотра метаданных выбранной ноды прямо внутри LOOM-визуализации.

### Макет

```
┌──────────────────────────────────────────────────────┬───────────────┐
│                   LOOM Canvas                        │   Inspector   │
│                                                      │   [≡]  [×]   │
│      [Node] ──── [Node] ──── [Node]                  │               │
│                                                      │  ▼ Properties │
│                                                      │  Name: ...    │
│                                                      │  Type: ...    │
│                                                      │  Schema: ...  │
│                                                      │               │
│                                                      │  ▼ Columns    │
│                                                      │  col1  INT    │
│                                                      │  col2  VARCHAR│
│                                                      │               │
│                                                      │  ▼ Relations  │
│                                                      │  ↑ 3 upstream │
│                                                      │  ↓ 5 downstream│
└──────────────────────────────────────────────────────┴───────────────┘
```

Ширина панели: 280px фиксированная. Открывается при `selectedNodeId !== null`, схлопывается при клике на pane или кнопке [×].

### Секции по типу ноды

#### DaliTable / tableNode

| Секция | Поля |
|--------|------|
| Properties | name, schema, type (source/target/lookup), depth |
| Columns | список колонок из `node.data.columns` (name, dataType, PK/FK badges) |
| Relations | upstream count, downstream count, ссылки «→ Expand» |

#### DaliRoutine / routineNode

| Секция | Поля |
|--------|------|
| Properties | routineName, packageName, language, type (procedure/function/trigger) |
| Statements | кол-во statements, кнопка «→ Open in KNOT» |
| Relations | upstream/downstream count |

#### DaliStatement / statementNode

| Секция | Поля |
|--------|------|
| Properties | stmtType (SELECT/INSERT/…), lineNumber, routineName, packageName |
| Source tables | список READS_FROM |
| Target tables | список WRITES_TO |
| Actions | «→ Open in KNOT» (если есть сессия) |

#### DaliColumn / columnNode

| Секция | Поля |
|--------|------|
| Properties | columnName, dataType, tableRef, position |
| Expression | выражение (если есть в data.metadata) |
| Lineage | «↑ Trace upstream», «↓ Impact downstream» |

#### DaliSchema / schemaNode, ApplicationNode, DatabaseNode

| Секция | Поля |
|--------|------|
| Properties | name, type, parent (app/db) |
| Stats | кол-во дочерних объектов |
| Actions | «Drill-down L2» / «Focus scope» |

#### По умолчанию (неизвестный тип)

Только Properties: label, nodeType, depth.

### Реализация

**Новые файлы:**
```
verdandi/src/components/layout/InspectorPanel.tsx     — главный контейнер
verdandi/src/components/inspector/InspectorTable.tsx  — секция для DaliTable
verdandi/src/components/inspector/InspectorRoutine.tsx
verdandi/src/components/inspector/InspectorStatement.tsx
verdandi/src/components/inspector/InspectorColumn.tsx
verdandi/src/components/inspector/InspectorDefault.tsx
```

**Изменяемые файлы:**
```
verdandi/src/App.tsx          — добавить InspectorPanel рядом с LoomCanvas
verdandi/src/stores/loomStore.ts — (возможно) inspectorOpen: boolean
```

**Данные:** инспектор читает ноды напрямую из ReactFlow через `useNodes()` или `useReactFlow().getNode(selectedNodeId)`. Дополнительных GQL-запросов не требует — все нужные данные уже в `node.data`.

**Паттерн:**
```tsx
const selectedNode = useReactFlow().getNode(selectedNodeId ?? '');
const nodeType = selectedNode?.data.nodeType;

return (
  <InspectorPanel>
    {nodeType === 'DaliTable'    && <InspectorTable node={selectedNode} />}
    {nodeType === 'DaliRoutine'  && <InspectorRoutine node={selectedNode} />}
    {nodeType === 'DaliStatement'&& <InspectorStatement node={selectedNode} />}
    {nodeType === 'DaliColumn'   && <InspectorColumn node={selectedNode} />}
    {/* ... */}
  </InspectorPanel>
);
```

**i18n ключи** (добавить в `knot.sections.*` / новый `inspector.*`):
```json
"inspector": {
  "title": "Inspector",
  "empty": "Select a node to inspect",
  "properties": "Properties",
  "columns": "Columns",
  "relations": "Relations",
  "statements": "Statements",
  "actions": "Actions",
  "upstream": "Upstream ({{count}})",
  "downstream": "Downstream ({{count}})",
  "openInKnot": "Open in KNOT",
  "drillDown": "Drill-down"
}
```

### Checklist

- [ ] `InspectorPanel.tsx` — контейнер с toggle, ширина 280px, `position: absolute right: 0`
- [ ] `InspectorTable.tsx` — Properties + Columns + Relations
- [ ] `InspectorRoutine.tsx` — Properties + Statements count
- [ ] `InspectorStatement.tsx` — Properties + src/tgt tables
- [ ] `InspectorColumn.tsx` — Properties + Expression
- [ ] `InspectorDefault.tsx` — fallback
- [ ] Интеграция в App.tsx / layout
- [ ] i18n keys (en + ru)
- [ ] Тест: инспектор открывается/закрывается при выборе ноды

---

## F-02 — LOOM-029: Context Menu

### Описание

Right-click на ноде → контекстное меню с набором действий. Контекстно-зависимые пункты по типу ноды.

### Пункты меню

| Действие | Доступно для | Реализация |
|----------|-------------|------------|
| Expand upstream | tableNode, routineNode, statementNode | `requestExpand(nodeId, 'upstream')` |
| Expand downstream | tableNode, routineNode, statementNode | `requestExpand(nodeId, 'downstream')` |
| Hide from canvas | все | `hideNode(nodeId)` |
| Focus (fit to node) | все | `requestFocusNode(nodeId)` |
| Copy ID | все | `navigator.clipboard.writeText(nodeId)` |
| Open in KNOT | routineNode, statementNode | `navigate('/knot')` |
| Drill-down L2 | tableNode, schemaNode (on L1) | `drillDown(...)` |
| Set as start object | tableNode, routineNode | `setStartObject(...)` |

### Реализация

**Новый файл:** `verdandi/src/components/canvas/ContextMenu.tsx`

```tsx
interface ContextMenuState {
  x: number; y: number;
  nodeId: string; nodeType: string;
}
const [contextMenu, setContextMenu] = useState<ContextMenuState | null>(null);

// В LoomCanvasInner:
const onNodeContextMenu = useCallback((e: React.MouseEvent, node: LoomNode) => {
  e.preventDefault();
  setContextMenu({ x: e.clientX, y: e.clientY, nodeId: node.id, nodeType: node.type ?? '' });
}, []);

// Закрытие:
// onPaneClick → setContextMenu(null)
// onKeyDown Escape → setContextMenu(null)
// onNodeClick → setContextMenu(null)
```

**Позиционирование:** `position: fixed; left: x; top: y; zIndex: 1000`. Проверять выход за правый/нижний край viewport.

**Стиль:** `background: var(--bg2); border: 1px solid var(--bd); border-radius: 6px; box-shadow: ...`. Пункты: `padding: 6px 12px; cursor: pointer; font-size: 12px;`. Hover: `background: var(--bg3)`.

### Checklist

- [ ] `ContextMenu.tsx` — компонент меню с позиционированием
- [ ] `onNodeContextMenu` в LoomCanvas
- [ ] Закрытие: pane click, Escape, повторный right-click
- [ ] Все действия подключены к store/actions
- [ ] i18n ключи `contextMenu.*`
- [ ] Не отображается на L3 (read-only режим глубокой детализации)

---

## F-03 — LOOM-030: Export PNG

### Описание

Кнопка экспорта текущего canvas в PNG. Сохраняет всю видимую область (с учётом zoom).

### Реализация

**Зависимость:** `html-to-image` (уже в npm-экосистеме React Flow).

```tsx
import { toPng } from 'html-to-image';
import { useReactFlow } from '@xyflow/react';

const { getViewport } = useReactFlow();

const exportPng = useCallback(async () => {
  const rfElement = document.querySelector('.react-flow') as HTMLElement;
  if (!rfElement) return;
  const dataUrl = await toPng(rfElement, {
    backgroundColor: theme === 'dark' ? '#0b0e0c' : '#eff5ef',
    quality: 0.95,
  });
  const link = document.createElement('a');
  link.download = `loom-${viewLevel}-${Date.now()}.png`;
  link.href = dataUrl;
  link.click();
}, [theme, viewLevel]);
```

**Размещение:** кнопка в `Header.tsx` или отдельная `ExportButton.tsx` в StatusBar.

**Замечание:** `html-to-image` может не воспроизвести custom шрифты/иконки корректно — нужна проверка. Альтернатива: `@xyflow/react` `toImage()` (если появится в используемой версии).

### Checklist

- [ ] Установить `html-to-image` (если не установлен: `npm install html-to-image`)
- [ ] `ExportButton.tsx` или интеграция в Header
- [ ] Корректный цвет фона по теме
- [ ] Имя файла: `seer-loom-{level}-{timestamp}.png`
- [ ] Кнопка недоступна пока `isLoading || layouting`
- [ ] i18n: `actions.exportPng` (уже есть в en/ru)

---

## R-01 — Рефакторинг LoomCanvas

### Проблема

`LoomCanvas.tsx` — 866 строк (после изменений сессии). Компонент совмещает:
- Данные (3 query + stmtCols + expand)
- Трансформации (rawGraph, scopedGraph, displayGraph — 9 фаз)
- Layout (ELK + L1)
- Viewport (fitView, zoom, focus)
- UI (nodes, edges, handlers, minimap)

### Целевая структура

```
verdandi/src/hooks/loom/
  useGraphData.ts      — overviewQ, exploreQ, lineageQ, stmtColsQ + rawGraph
  useExpansion.ts      — expand queries + addExpansionData + error recovery
  useDisplayGraph.ts   — scopedGraph + displayGraph (все 9 фаз фильтрации)
  useLoomLayout.ts     — ELK + L1 layout + layoutError + layouting
  useFitView.ts        — fitView requests + zoom tracking

LoomCanvas.tsx         — ~200 строк (только RF providers, handlers, JSX)
```

### Checklist

- [ ] Извлечь `useGraphData` (overviewQ, exploreQ, lineageQ, stmtColsQ, rawGraph useMemo)
- [ ] Извлечь `useExpansion` (expand useEffects, S-05 error recovery)
- [ ] Извлечь `useDisplayGraph` (scopedGraph + displayGraph 9 фаз)
- [ ] Извлечь `useLoomLayout` (ELK, L1, layoutError, layouting, setNodes, setEdges)
- [ ] Извлечь `useFitView` (fitViewRequest effect + onMoveEnd)
- [ ] LoomCanvas: только ReactFlow JSX + handlers
- [ ] Убедиться, что поведение не изменилось (ручное тестирование L1/L2/L3)

---

## R-02 — Split transformGraph.ts

### Проблема

`transformGraph.ts` — монолитный файл с трансформациями для L1 и L2/L3. После изменений сессии: ~700+ строк.

### Целевая структура

```
verdandi/src/utils/
  transformOverview.ts   — transformGqlOverview (L1)
  transformExplore.ts    — transformGqlExplore + applyStmtColumns (L2/L3)
  transformHelpers.ts    — extractStatementType, parseStmtLabel, SCOPE_FILTER_TYPES, etc.
```

**Важно:** обновить все import в LoomCanvas.tsx, hooks, тестах.

### Checklist

- [ ] Создать 3 файла, перенести функции по смысловым группам
- [ ] Обновить импорты в `LoomCanvas.tsx`
- [ ] Обновить импорты в любых других местах (grep `from.*transformGraph`)
- [ ] Удалить старый `transformGraph.ts`

---

## R-03 — Unit-тесты transformGraph (Vitest)

### Что покрыть

| Функция | Тест-кейсы |
|---------|------------|
| `transformGqlOverview` | пустые данные, 1 App + 1 DB + 2 Schema, несколько App, системный уровень |
| `transformGqlExplore` | пустые данные, Table + Statement + Column, рутина с пакетом |
| `extractStatementType` | SELECT, INSERT, UPDATE, DELETE, MERGE, CURSOR, unknown |
| `parseStmtLabel` | стандартный geoid, короткий geoid, null |
| `applyStmtColumns` | добавление колонок в ноды, cf edges |

**Инструмент:** Vitest (уже в проекте как dev-зависимость).

**Расположение:** `verdandi/src/utils/__tests__/transformOverview.test.ts` и т.д.

### Checklist

- [ ] Настроить тестовый окружение (если не настроено): `vite.config.ts` → `test: { environment: 'jsdom' }`
- [ ] Mock данные: создать `__fixtures__/gqlOverview.ts`, `gqlExplore.ts`
- [ ] Написать тесты для каждой функции
- [ ] `npm run test` проходит без ошибок
- [ ] Покрытие: не менее 60% строк transformHelpers

---

## R-04 — Удалить L1NodesProto.tsx

### Файл

```
verdandi/src/components/canvas/nodes/L1NodesProto.tsx  (~618 LOC)
```

Прототип L1-нод, созданный на раннем этапе Phase 3. Заменён рабочими `ApplicationNode.tsx`, `DatabaseNode.tsx`, `L1SchemaNode.tsx`. Более нигде не импортируется.

### Checklist

- [ ] Проверить: `grep -r "L1NodesProto" verdandi/src/` → нет импортов
- [ ] Удалить файл
- [ ] Коммит: `chore: remove obsolete L1NodesProto prototype`

---

## Рекомендуемый порядок выполнения

```
Среда 09.04   F-01 (KNOT Inspector)
Четверг 10.04 F-02 (Context menu) + F-03 (Export PNG)
Пятница 11.04 R-04 (15 мин) + R-01 (LoomCanvas refactor) + R-02 (split transformGraph)
              R-03 (тесты transformGraph) — если останется время
```

---

## Блокеры и зависимости

| Блокер | Влияет на | Статус |
|--------|----------|--------|
| HOUND-DB-001 (schema alignment) | Полнота L1 данных | ⬜ Ожидает Hound |
| `ColumnInfo.type` не заполняется | Типы колонок в TableNode/Inspector | ⬜ Требует Hound/SHUTTLE |
| ArcadeDB Cypher UNION баг | Качество данных ExploreService | 🔄 Обходится parallel queries |
| `html-to-image` совместимость | F-03 Export PNG | ❓ Требует проверки |

---

## Метрики по завершении Phase 3

| Метрика | Сейчас | Цель (конец недели) |
|---------|--------|---------------------|
| Phase 3 задачи | 10/12 (83%) | 12/12 (100%) |
| Критические баги | 0 | 0 |
| Test coverage (backend) | KnotService + SearchService | + ExploreService |
| Test coverage (frontend) | 0% | ≥ 40% (transformGraph) |
| LOC LoomCanvas | ~866 | ≤ 250 (если R-01) |
| LOC transformGraph | ~700 | ≤ 200/файл (если R-02) |
