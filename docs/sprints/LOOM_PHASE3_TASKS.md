# VERDANDI LOOM — Phase 3: Core Features
# Детальная декомпозиция задач

**Дата:** 04.04.2026  
**Статус:** TODO · следует за Phase 2 (LOOM-015 – LOOM-022)  
**Срок:** ~4–5 недель  
**Принцип:** каждая задача закрывает конкретный User Story — проверяемый результат в браузере.

---

## Контекст из прототипирования

По итогам сессии прототипирования (04.04.2026) зафиксированы:
- **Раскладка Вариант B:** Search слева / Filter toolbar над canvas / KNOT Inspector справа
- **TableNode:** три состояния collapsed/partial/expanded, кнопки окна ОС 🔴🟡🟢
- **Column mapping:** рёбра от конкретных output_columns к конкретным колонкам target table
- **StatementGroupNode:** Вариант C — визуальная рамка без `extent:'parent'`
- **Фильтр поля:** toolbar — select поля + depth + direction + start object pill
- **`nodesDraggable=false`** по умолчанию

---

## Персоны и роли

| Роль | Кто | Главный вопрос |
|------|-----|----------------|
| **Data Engineer** | Строит пайплайны, отвечает за ETL | «Что сломается если я изменю X?» |
| **Data Analyst** | Проверяет качество данных, строит отчёты | «Откуда берётся это поле?» |
| **DBA / Architect** | Понимает общую структуру БД | «Как схемы связаны между собой?» |

---

## Принцип: Prototype First

**Каждая задача Phase 3 проходит два шага:**

```
1. PROTO  →  React-компонент в проекте, без обработчиков событий
             Только визуальное решение: layout, состояния, цвета, типографика
             Хардкод данных — никакого store, никакого API
             Утверждается до написания логики

2. IMPL   →  добавление обработчиков, подключение store и хуков
             Строится поверх утверждённого прототипа
```

**Что даёт прототип:**
- Находит визуальные проблемы до написания логики (как с grouped edges и depth)
- Фиксирует точные состояния: hover-классы, selected, dimmed, collapsed, expanded
- Все состояния показываются одновременно на одном экране — легко сравнивать
- Служит живым референсом: компонент уже в проекте, в реальной дизайн-системе

**Формат:**
- Обычный `.tsx` файл в `src/components/canvas/proto/` или рядом с целевым компонентом
- Хардкод mock-данных прямо в файле — никаких импортов из store/hooks
- CSS-переменные SEER дизайн-системы (`--bg2`, `--acc`, `--t1`, `--bd` и т.д.)
- Lucide иконки — да, shadcn/ui компоненты — да
- `onClick={undefined}` или просто без обработчиков — кнопки видны, но не работают
- Все визуальные состояния рядом: например, три TableNode (collapsed / partial / expanded) в одном файле

**Структура файла прототипа:**
```tsx
// src/components/canvas/proto/TableNodeProto.tsx
// PROTO: статичный визуальный прототип — без логики
// Удалить или переработать после утверждения → LOOM-026

export function TableNodeProto() {
  return (
    <div style={{ display: 'flex', gap: 24, padding: 24, background: 'var(--bg1)' }}>
      {/* State 1: collapsed */}
      <TableNodeCollapsed />
      {/* State 2: partial (default) */}
      <TableNodePartial />
      {/* State 3: expanded */}
      <TableNodeExpanded />
    </div>
  );
}
```

**Когда прототип не нужен:**
- Задача чисто техническая без UI (LOOM-023 read-only, LOOM-034 performance)
- Визуальное решение уже полностью закрыто предыдущим прототипом и утверждено

---

## LOOM-023: Canvas read-only mode

**Приоритет:** 🔴 Критический | **Оценка:** 30 мин | **Зависимости:** LOOM-006

### User Story
> Как **любой пользователь**, я хочу чтобы граф не сдвигался случайно при клике на ноды, потому что **lineage — это документ для чтения**, а не whiteboard для рисования.

### Acceptance Criteria
- Ноды не перетаскиваются мышью
- Рёбра нельзя создать вручную
- Pan canvas по пустому месту — работает
- Zoom колесом / pinch — работает
- Клик на ноду — выделяет (нужно для KNOT Inspector)

### Реализация
```tsx
<ReactFlow
  nodesDraggable={false}
  nodesConnectable={false}
  elementsSelectable={true}
  panOnDrag={true}
  zoomOnScroll={true}
/>
```

### Таблица выбора: когда включать dragging

| Сценарий | Включать? | Условие |
|----------|-----------|---------|
| Просмотр lineage | ❌ | Всегда false |
| Экспорт PNG/SVG | ✅ | Только в режиме экспорта |
| Аннотирование (editor/admin) | ✅ | Будущая фича — отдельное решение |
| Обучение / онбординг | ❌ | Документ, не редактор |

**i18n:** нет новых ключей — техническая задача.

**Готово когда:** ноды не тащатся, pan/zoom работает, клик выделяет.

---

## PROTO-023b: Filter Toolbar
**Файл:** `src/components/layout/proto/FilterToolbarProto.tsx`  
**Показывает:** start object pill, field select с вариантами, depth кнопки (1/2/3/∞), direction toggle ↑↓, table-level view toggle в двух состояниях (off/on).  
**Все состояния рядом:** toolbar без фильтра + toolbar с активным полем + toolbar в table-level режиме — три варианта в одном файле.  
**Статус:** ✅ Визуальное решение утверждено в сессии 04.04.2026, прототип в проекте нужен перед IMPL

## LOOM-023b: Filter Toolbar

**Приоритет:** 🔴 Критический | **Оценка:** 3–4 ч | **Зависимости:** LOOM-006, LOOM-020

### User Story
> Как **Data Analyst**, я хочу сфокусироваться на одном поле (например `total_amount`) и видеть **только его путь** от источников через трансформацию к целевой таблице — без всего остального шума на canvas.

### Use Case: Фильтр одного поля

```
Триггер: аналитик открыл L2 canvas для рутины calc_order_revenue()

1. В toolbar видит: [⊙ calc_order_revenue()] [Поле: все колонки ▾] [Глубина: 1 2 3 5 ∞] [↑ Upstream] [↓ Downstream]
2. Кликает на dropdown «Поле» → видит список output_columns трансформации
3. Выбирает «total_amount»
4. Canvas мгновенно: нерелевантные колонки в нодах затухают (opacity 0.2)
5. Нерелевантные source tables затухают целиком
6. Рёбра показывают только путь: order_items.quantity → SUM → total_amount → orders.total_amount
7. KNOT Inspector справа автоматически переключается на детали total_amount
8. Breadcrumb обновляется: «public › calc_order_revenue() · total_amount»
9. ✕ в toolbar — сбросить фильтр, показать всё
```

### Use Case: Смена начального объекта

```
1. Пользователь видит pill [⊙ calc_order_revenue() ⇄] в toolbar
2. Клик на ⇄ → открывается command palette с поиском
3. Вводит «get_sales» → видит get_sales_report() в результатах
4. Выбирает → canvas перезагружается с новым start object
5. Поле и глубина сбрасываются до defaults
```

### Компоненты toolbar (слева направо)

| Элемент | Тип | Поведение |
|---------|-----|-----------|
| Start object pill | Кнопка с иконкой рутины/таблицы + имя + ⇄ | Клик ⇄ → command palette для смены |
| Разделитель | 1px вертикальная линия | — |
| «Поле:» + select | Dropdown список output_columns | onChange → applyField() |
| ✕ | Кнопка сброса | clearField() |
| Разделитель | | |
| «Глубина:» + 1/2/3/5/∞ | Кнопки-переключатели | setDepth() |
| Разделитель | | |
| ↑ Upstream / ↓ Downstream | Кнопки-toggle | toggleDir() |
| (пространство) | flex: auto | |
| Table-level view | Toggle-кнопка | Скрыть column edges, показать table-level. Ноды сворачиваются |
| Level badge | Readonly | «L2 · глубина 2» |

### Store actions
```typescript
// loomStore additions:
setStartObject: (nodeId: string, nodeType: DaliNodeType) => void;
setFieldFilter: (columnName: string | null) => void;
setDepth: (depth: number) => void;
setDirection: (up: boolean, down: boolean) => void;
```

**i18n:** `toolbar.*` — все подписи toolbar. Секция уже в словаре.

**Готово когда:** выбор поля затемняет нерелевантные элементы, рёбра перестраиваются, KNOT Inspector обновляется.

---

## PROTO-024: L1 — ApplicationNode + ServiceNode
**Файл:** `src/components/canvas/nodes/proto/L1NodesProto.tsx`  
**Показывает:**
- `ApplicationNode` — крупная нода с иконкой, имя приложения, счётчик сервисов/БД
- `ServiceNode` — средняя нода, имя сервиса, язык/технология
- Четыре уровня рядом на статичном canvas: Application → Service → Database → Schema
- Два типа рёбер: HAS_DATABASE (сплошная --t2) vs USES_DATABASE (пунктир --t3)
- Breadcrumb в двух состояниях: «Overview» и «Overview › MyApp › OrderService»
**Статус:** ⬜ Нужен перед реализацией LOOM-024

## LOOM-024: L1 расширение — ApplicationNode + ServiceNode

**Приоритет:** 🔴 Критический | **Оценка:** 4–5 ч | **Зависимости:** LOOM-007, LOOM-020

### User Story
> Как **DBA / Architect**, я хочу видеть на L1 карту приложений — какой сервис использует какую базу данных — чтобы понять **границы ответственности** и потенциальные конфликты при изменениях.

### Use Case: Обзор приложений

```
1. Пользователь входит на L1 Overview
2. Видит иерархию: [OrderApp] → [OrderService] → [orders_db] → [public / reporting]
3. Может сузить scope: double-click на [OrderApp] → breadcrumb «Overview › OrderApp»
4. Canvas показывает только сервисы и БД этого приложения
5. Double-click на [OrderService] → «Overview › OrderApp › OrderService»
6. Видит только БД и схемы этого сервиса
7. Double-click на [orders_db] → переход на L2 (полная смена уровня)
```

### Use Case: Поиск общих ресурсов

```
1. DBA хочет найти БД, которую используют несколько сервисов
2. На L1 видит ребро USES_DATABASE (пунктир) от двух сервисов к одной БД
3. Клик на БД → KNOT Inspector показывает: «Used by: OrderService, ReportService»
4. Это сигнал: изменение в этой БД затронет оба сервиса
```

### Поведение double-click на L1

| Тип ноды | Double-click | Результат |
|----------|-------------|-----------|
| DaliApplication | Scope filter | L1 остаётся, breadcrumb: «Overview › App» |
| DaliService | Scope filter | L1 остаётся, breadcrumb: «Overview › App › Service» |
| DaliDatabase | Drill-down | → L2 этой БД |
| DaliSchema | Drill-down | → L2 этой схемы |
| DaliPackage | Drill-down | → L2 этого пакета |

### Новые типы в domain.ts
```typescript
| 'DaliApplication'
| 'DaliService'

// Рёбра:
| 'HAS_SERVICE'    // Application → Service
| 'HAS_DATABASE'   // Service → DaliDatabase (владение)
| 'USES_DATABASE'  // Service → DaliDatabase (использование, пунктир)
```

### Визуальное отличие HAS_DATABASE vs USES_DATABASE

| Ребро | Стиль | Значение |
|-------|-------|----------|
| HAS_DATABASE | Сплошная линия, --acc | Сервис владеет БД |
| USES_DATABASE | Пунктир, --t3 | Сервис использует чужую БД |

**i18n:** `nodeTypes.DaliApplication`, `nodeTypes.DaliService`, `nodes.services`, `nodes.databases`.

**Готово когда:** L1 показывает 4 уровня, scope filter работает, переход на L2 через Database/Schema не сломан.

---

## PROTO-025: Column Mapping L2
**Файл:** `src/components/canvas/proto/ColumnMappingProto.tsx`  
**Показывает три варианта рядом:**
1. Без фильтра — все column-level рёбра (все яркие)
2. С фильтром `total_amount` — рёбра dim кроме нужных, строки в нодах dim
3. Table-level view — ноды свёрнуты, только table-level рёбра
**Хардкод:** order_items + customers → calc_order_revenue() → orders, 6 output_columns  
**Статус:** ✅ Визуальное решение утверждено, прототип в проекте нужен перед IMPL

## LOOM-025: Column Mapping на L2

**Приоритет:** 🔴 Критический | **Оценка:** 4–5 ч | **Зависимости:** LOOM-020, LOOM-023b

### User Story
> Как **Data Engineer**, я хочу видеть **конкретное поле откуда берётся** в целевой таблице — не просто «orders зависит от order_items», а «`orders.total_amount` вычисляется из `order_items.quantity * order_items.unit_price` через рутину `calc_order_revenue()`».

### Use Case: Полный column mapping

```
Стартовый объект: calc_order_revenue() (DaliRoutine)

1. L2 canvas показывает:
   - Слева: source tables (order_items, products, ...) с колонками
   - По центру: TransformNode — список output_columns с типом и источником
   - Справа: target table (orders) с колонками

2. Рёбра: от конкретной строки source table → к строке output_column трансформации
   order_items.quantity  ──────────────────→ [total_amount | SUM]
   order_items.unit_price ─────────────────→ [total_amount | SUM]
                                              [total_amount | SUM] ──→ orders.total_amount

3. 20+ колонок 1:1: рёбра thin (#665c48), grouped — от TransformNode к целевой таблице
   одна толстая линия с badge «18 mapped 1:1» вместо 18 отдельных линий

4. Сложные маппинги (SUM, CASE, subq) — отдельные тонкие линии с цветом --wrn

5. При выборе поля в toolbar — показываются только рёбра этого поля
```

### Use Case: Обнаружение сложного маппинга

```
1. Аналитик видит [total_amount | SUM] с оранжевым badge
2. Наводит → tooltip: «SUM(quantity × unit_price) · 6 subqueries · drill-down L3»
3. Кликает «drill-down L3 →» → переход в L3 с полным деревом атомов
```

### Таблица типов маппинга и их визуализация

| Тип маппинга | Badge | Цвет ребра | Пример |
|-------------|-------|-----------|--------|
| Passthrough 1:1 | — | --t3 тонкая | `order_id → order_id` |
| Вычисляемое | SUM / AVG / COUNT | --wrn | `SUM(qty * price)` |
| Оконная функция | RANK / ROW_NUM | --dan / pink | `RANK() OVER region` |
| Подзапрос | ⊂ subq | --inf | `WHERE id IN (SELECT...)` |
| Литерал | lit | --t3 пунктир | `'active' → status` |
| CASE WHEN | CASE | --wrn | `CASE WHEN ... END` |
| NULL | null | --t3 очень тонкая | `NULL → order_month` (в UNION) |


### Правила отображения рёбер

**Без фильтра по полю** — показываются ВСЕ column-level рёбра для всех колонок одновременно. Да, линий будет много — это нормально и правильно: пользователь видит полную картину маппинга. Группировки нет.

**С фильтром по полю** — все рёбра остаются на canvas, но нерелевантные уходят в opacity 0.08 (dim). Рёбра выбранного поля остаются яркими. Нерелевантные строки в нодах тоже dimmed.

**Кнопка «Table-level view»** в toolbar — отдельный toggle-режим:
- Скрывает все column-level рёбра
- Показывает одну линию на каждую source table → TransformNode → target
- Ноды сворачиваются (только header, без колонок)
- Удобно для обзора структуры без деталей маппинга
- Переключение обратно → снова все column-level рёбра

```typescript
// toolbar toggle:
tableLevelView: boolean;  // default: false
setTableLevelView: (v: boolean) => void;
```

### TransformNode: обязательные поля

```typescript
interface TransformNodeData extends DaliNodeData {
  nodeType: 'DaliStatement' | 'DaliRoutine';
  statementType: 'Query' | 'Insert' | 'Update' | 'Cursor' | 'RefCursor';
  outputColumns: OutputColumnInfo[];
  subqueryCount: number;
  sourceTablesCount: number;
  isRootStatement: boolean;
}

interface OutputColumnInfo {
  id: string;
  name: string;
  sourceExpression: string;     // «qty × price» или «order_items.order_id»
  mappingType: MappingType;
  hasSubquery: boolean;
  subqueryStatementId?: string;
}
```

**i18n:** `mapping.*`, `nodes.outputColumns`, `subquery.badge`, `actions.drilldownL3`.

**Готово когда:** canvas показывает column-level рёбра, badges для сложных маппингов, drill-down L3 из TransformNode.

---

## PROTO-026: TableNode три состояния
**Файл:** `src/components/canvas/nodes/proto/TableNodeProto.tsx`  
**Показывает три состояния рядом:**
1. `collapsed` — 44px, только header: иконка + имя + «N col» + кнопки 🔴🟡🟢
2. `partial` — header + 7 строк колонок (PK/FK/обычные) + footer «+N more»
3. `expanded` — header + filter input + все колонки + scroll indicator  
**Хардкод:** таблица `orders` с 22 колонками (первые 7 реальных имён)  
**Статус:** ✅ Визуальное решение утверждено, прототип в проекте нужен перед IMPL

## LOOM-026: TableNode — три состояния

**Приоритет:** 🔴 Критический | **Оценка:** 3–4 ч | **Зависимости:** LOOM-008

### User Story
> Как **Data Engineer**, я хочу управлять плотностью информации в нодах — свернуть таблицы которые меня не интересуют и развернуть ту одну что важна — не прокручивая canvas горизонтально.

### Use Case: Работа с 20-колоночной таблицей

```
1. Открыт L2. orders появился в partial: заголовок + первые 7 колонок + «+13 more»
2. Engineer ищет поле total_amount — не видит его в первых 7
3. Нажимает 🟢 (зелёная кнопка окна) → expanded state
4. Появляются все 22 колонки + поисковый input вверху списка
5. Вводит «total» → мгновенно остаются только total_amount, total_discount
6. Кликает total_amount → drill-down L3 только по этому полю
7. После drill-down нажимает 🟡 → partial state, занимает меньше места
```

### Use Case: Быстрое сравнение двух таблиц

```
1. На canvas: orders (expanded) и order_items (partial)
2. Нужно сравнить структуру — сворачивает orders до collapsed (🟡)
3. Разворачивает order_items до expanded (🟢)
4. Видит FK: order_items.order_id — кликает, KNOT показывает связь с orders
```

### Три состояния в деталях

| Состояние | Высота | Содержимое | Переход |
|-----------|--------|------------|---------|
| `collapsed` | 44px | Header только: иконка + имя + schema + «N col» | 🟡 или drag up handle |
| `partial` | auto | Header + первые 7 колонок + «+N more» footer | По умолчанию при появлении ноды |
| `expanded` | auto + max 400px | Header + filter input + ВСЕ колонки + scroll | 🟢 или double-click на «+N more» |

### Кнопки окна (top-right)

| Кнопка | Цвет | Действие | Аналогия в ОС |
|--------|------|----------|---------------|
| 🔴 | #C85848 | Убрать ноду с canvas | Закрыть окно |
| 🟡 | #D4922A | Переключить в `partial` | Свернуть в dock |
| 🟢 | #7DBF78 | Переключить в `expanded` | Развернуть на весь экран |

> **Важно:** 🔴 не удаляет из БД — только скрывает с текущего canvas view. Undo через «Показать скрытые ноды» в левой панели.

### Behaviour при filter в expanded

```typescript
// src/components/canvas/nodes/TableNode.tsx
const [filterQuery, setFilterQuery] = useState('');
const fuse = useMemo(() => new Fuse(data.columns, { keys: ['name', 'type'] }), [data.columns]);
const visibleColumns = filterQuery
  ? fuse.search(filterQuery).map(r => r.item)
  : data.columns;
```

**i18n:** `tableNode.*` — collapse, expand, hide, moreColumns, filterPlaceholder, pkLabel, fkLabel.

**Готово когда:** 3 состояния переключаются кнопками, filter в expanded работает, клик на колонку → L3.

---

## PROTO-027: Expand buttons upstream / downstream
**Файл:** `src/components/canvas/nodes/proto/ExpandButtonsProto.tsx`  
**Показывает четыре состояния кнопки expand:**
1. Нода без hover — кнопок нет
2. Нода в hover — кнопки [↑ +4] и [↓ +7] за границей
3. Частично загружено — [↑ +20/47]
4. Все загружены — [↑ ✓] приглушённая
**Хардкод:** одна центральная нода `orders`, статичные кнопки в нужных состояниях рядом  
**Статус:** ⬜ Нужен перед реализацией LOOM-027

## LOOM-027: Expand buttons — Upstream / Downstream

**Приоритет:** 🟡 Важный | **Оценка:** 3–4 ч | **Зависимости:** LOOM-019, LOOM-020, LOOM-023b

### User Story
> Как **Data Engineer**, я хочу **постепенно наращивать граф** вокруг нужной ноды — добавлять источники или потребителей по одному слою — не загружая весь lineage сразу.

### Use Case: Impact check перед изменением

```
1. Engineer находит таблицу orders на canvas (через search или drill-down)
2. Наводит на ноду → появляются кнопки [↑ +3] и [↓ +2]
   (числа prefetch'нуты при hover через TanStack Query prefetchQuery)
3. Кликает [↓ +2] (downstream) → загружает 2 таблицы которые читают из orders
4. Видит: fact_sales и order_summary зависят от orders
5. Наводит на fact_sales → [↓ +5] → кликает → ещё 5 нод
6. Граф вырос органически, Engineer видит полный downstream chain
7. Решает: «изменение в orders затронет 7 таблиц» — фиксирует в KNOT
```

### Use Case: Поиск источника данных

```
1. Аналитик видит таблицу fact_sales
2. Кликает [↑ +4] upstream → появляются 4 таблицы-источника
3. Одна из них — stg_orders — не очевидна. Наводит → tooltip: «staging table»
4. Кликает [↑ +2] на stg_orders → появляются raw_events и orders
5. Теперь ясно: fact_sales ← stg_orders ← orders
6. Нажимает «Collapse upstream» на stg_orders → убирает ноды которые добавил
```

### Визуальное поведение кнопок

```
Нода в normal state:
┌─────────────────┐
│   TableNode     │
└─────────────────┘

Нода при hover (expand buttons появляются):
      [↑ +4]
┌─────────────────┐
│   TableNode     │
└─────────────────┘
      [↓ +7]

Если N > 20:
      [↑ +20/47]   ← «20 загружено, 47 всего»
```

### Состояния кнопки expand

| Состояние | Вид | Значение |
|-----------|-----|----------|
| Загрузка числа | `[↑ …]` spinner | prefetch в процессе |
| Есть соседи | `[↑ +N]` | N нод доступно |
| Частично загружено | `[↑ +20/47]` | 20 из 47 показаны |
| Все загружены | `[↑ ✓]` тусклая | Все соседи уже на canvas |
| Нет соседей | кнопка скрыта | — |

### Store actions
```typescript
expandNeighbors: (
  nodeId: string,
  direction: 'upstream' | 'downstream',
  depth: number
) => Promise<void>;

collapseNeighbors: (
  nodeId: string,
  direction: 'upstream' | 'downstream'
) => void;

// В loomStore state:
expandedNeighbors: Map<string, Set<string>>; // nodeId → Set<addedNodeIds>
```

### Взаимодействие с toolbar depth

Toolbar depth — это фильтр для начальной загрузки canvas (сколько хопов от start object загрузить при открытии). Expand buttons всегда добавляют ровно **1 слой**, без настройки. Никакой «индивидуальной памяти depth» у нод нет.

```
Toolbar depth = сколько хопов загрузить при смене start object
Expand buttons = всегда +1 слой, кнопки ↑ и ↓ независимы
```

**i18n:** `expand.*` — upstream, downstream, loading, partialCount, allLoaded, collapseUpstream, noNeighbors.

**Готово когда:** hover → кнопки с числом соседей; клик → ноды появляются, ELK пересчитывает; collapse → ноды исчезают.

---

## PROTO-028: StatementNode + StatementGroupNode
**Файл:** `src/components/canvas/nodes/proto/StatementNodesProto.tsx`  
**Показывает:**
1. `StatementNode` на L2 — тип (INSERT/SELECT/...), список READ/WRITE таблиц, кнопка «▸ expand»
2. `StatementGroupNode` collapsed — одна нода, badge «3 nested»
3. `StatementGroupNode` expanded — dashed рамка, дочерние Statement с lineage внутри, DATA_FLOW рёбра за границу  
**Хардкод:** `INSERT orders` с двумя дочерними SELECT  
**Статус:** ✅ GroupNode Вариант C утверждён, StatementNode на L2 нужно дополнить → прототип в проекте перед IMPL

## LOOM-028: StatementNode + StatementGroupNode (L2/L3)

**Приоритет:** 🟡 Важный | **Оценка:** 5–6 ч | **Зависимости:** LOOM-025

### User Story (L2 — StatementNode)
> Как **Data Engineer**, я хочу видеть на L2 **корневые SQL-операции** (INSERT, UPDATE, SELECT) внутри рутины — чтобы понять что именно пишет/читает рутина без перехода на L3.

### User Story (L3 — StatementGroupNode)
> Как **Data Analyst**, я хочу видеть **вложенные подзапросы сгруппированными** внутри родительского Statement — чтобы понять структуру сложного запроса не теряя визуальный контекст вложенности.

### Use Case: StatementNode на L2

```
Контекст: L2 схемы public, рутина calc_order_revenue() развёрнута

Видны корневые Statement (у которых нет parent statement):
  [INSERT orders] ← DaliStatement, border-left: --wrn
    Reads: order_items, products
    Writes: orders
    [▸ expand] → показывает дочерние Statement рядом

  Дочерние (после expand):
    [SELECT qty×price] — nested, показан рядом с родителем
    [SELECT discount] ⊂ — has subquery, badge
```

### Use Case: StatementGroupNode на L3

```
Контекст: drill-down в L3 для orders.total_amount

Canvas показывает:
  ┌── INSERT orders ─────────────────────────┐  ← GroupNode рамка (dashed)
  │                                          │
  │  [order_items.qty] → [MULTIPLY] → ─────→│→ [orders.total_amount]
  │  [order_items.price] ──────────↗        │
  │                                          │
  │  [SELECT base] ← nested Statement       │
  │    [order_items.*] → [FILTER active] →  │
  │                                          │
  │  [⊂ discount subquery]  ←── badge       │
  └──────────────────────────────────────────┘

  DATA_FLOW рёбра пересекают рамку наружу → другим нодам
```

### Правила StatementGroupNode (Вариант C)

| Свойство | Значение | Обоснование |
|----------|----------|-------------|
| React Flow тип | `group` | Специальный тип для визуальных контейнеров |
| `extent` | не задан | Дочерние ноды физически независимы |
| Граница | 1.5px dashed, --bd | Визуальная, не физическая |
| Label | имя Statement вверху-слева | Всегда видно |
| Collapsed | Одна нода с бейджем «N nested» | Меньше места |
| Expanded | Рамка + все дочерние ноды внутри | Полная структура |
| Рёбра | DATA_FLOW пересекает границу | React Flow поддерживает без ограничений |
| ELK | Считает ноды внутри как обычные | Не subgraph layout |

### Что показывается внутри GroupNode на L3

```
Для каждого output_column трансформации:
  DaliColumn (source) → DaliAtom (operation) → DaliOutputColumn (result)

Дополнительно:
  DaliJoin ноды — если есть JOIN условия
  DaliParameter — если есть параметры рутины
  SubQuery badge ⊂ — если DaliAtom содержит подзапрос
  DaliVariable — переменные PL/SQL блока
```

**i18n:** `statement.*` — types, rootStatement, nestedStatements, reads, writes, expandStatements, groupLabel.

**Готово когда:** L2 показывает StatementNode с expand; L3 показывает StatementGroupNode с полным lineage и рёбрами через границу.

---

## PROTO-029: SubQuery drill-down L3 → L3
**Файл:** `src/components/canvas/proto/SubQueryProto.tsx`  
**Показывает:**
1. `AtomNode` с badge `FROM ⊂` (--inf), `WHERE ⊂` (--wrn), `SELECT ⊂` (--acc) — три варианта рядом
2. Breadcrumb цепочку: `... › total_amount › SQ#1 › SQ#2` — хардкод
3. L3 canvas для SubQuery#1 с парой Column → Atom → OutputColumn  
**Хардкод:** статичная структура, никакой навигации — только визуал состояний  
**Статус:** ✅ Цветовая схема SubQuery типов нужна дополнительно → прототип в проекте перед IMPL

## LOOM-029: SubQuery drill-down (L3 → L3)

**Приоритет:** 🟡 Важный | **Оценка:** 3–4 ч | **Зависимости:** LOOM-028

### User Story
> Как **Data Analyst**, я хочу **погружаться в подзапросы** последовательно — каждый клик на ⊂ badge открывает следующий уровень с собственным lineage — чтобы распутать цепочку из 6 вложенных SELECT без потери контекста.

### Use Case: Навигация по 6 уровням подзапросов

```
Стартовая точка: L3 · orders.total_amount · INSERT orders

1. Видит AtomNode [SUM] с badge ⊂ subq
2. Кликает ⊂ → breadcrumb: «...orders › total_amount › SubQuery #1»
   Canvas перезагружается: L3 для SubQuery #1
   Показывает: order_items.* → FILTER active_orders → OutputColumn

3. Внутри SubQuery #1 видит ещё [FILTER] с ⊂
4. Кликает → breadcrumb: «...total_amount › SQ#1 › SQ#2»
   Canvas: nested субселект

5. Продолжает до глубины 6
6. В любой момент — клик на сегмент breadcrumb → возврат

7. Escape = назад на один уровень (keyboard navigation)
```

### Use Case: SubQuery из FROM vs WHERE

```
L3 canvas для сложного запроса:

[JOIN subquery] badge «FROM ⊂»  ← другой цвет/тип чем WHERE subquery
[filter subquery] badge «WHERE ⊂»

Разные цвета помогают понять роль подзапроса до drill-down:
  FROM subquery → --inf (синий) — это виртуальная таблица
  WHERE subquery → --wrn (оранжевый) — это фильтр
  SELECT subquery → --acc (зелёный) — это вычисляемое значение
```

### Таблица типов SubQuery и их визуализация

| Тип SubQuery | Badge | Цвет | Где встречается |
|-------------|-------|------|----------------|
| FROM subquery | `FROM ⊂` | --inf | `SELECT * FROM (SELECT...)` |
| WHERE IN subquery | `WHERE ⊂` | --wrn | `WHERE id IN (SELECT...)` |
| WHERE EXISTS | `EXISTS ⊂` | --wrn | `WHERE EXISTS (SELECT...)` |
| SELECT subquery | `SELECT ⊂` | --acc | `SELECT (SELECT AVG...) AS avg_val` |
| Scalar subquery | `scalar ⊂` | --acc | В выражении: `amount > (SELECT AVG...)` |
| CTE | `CTE` | --t2 | `WITH base AS (...)` — не drill-down, отдельная нода |

### Breadcrumb при глубоком нырянии

```
Overview › analytics.public › calc_order_revenue() › total_amount › SQ#1 › SQ#2 › SQ#3

Правила:
- Каждый SQ сегмент кликабельный → возврат на этот уровень
- Escape → назад на 1 уровень
- Максимум 5 видимых сегментов, остальные → «...» с dropdown
```

### BreadcrumbItem расширение
```typescript
interface BreadcrumbItem {
  level: ViewLevel;
  scope: string | null;
  label: string;
  subQueryDepth?: number;        // 0 = основной L3, 1+ = вложенные
  subQueryType?: SubQueryType;   // FROM | WHERE | SELECT | EXISTS | scalar
  subQueryIndex?: number;        // #1, #2, #3...
}
```

**i18n:** `subquery.*` — types (from/where/select/exists/scalar), badge, levels, tables, drilldown, cte.

**Готово когда:** клик ⊂ → новый L3 контекст; breadcrumb показывает путь; Escape возвращает назад; разные типы SubQuery визуально различимы.

---

## PROTO-030: Impact Analysis + KNOT Inspector
**Файл:** `src/components/panels/proto/KnotInspectorProto.tsx`  
**Показывает:**
1. Canvas с 4–5 нодами: центральная выбрана, upstream (--inf, градация opacity по глубине), downstream (--wrn)
2. KNOT Inspector в правой панели: секции Node / Impact / Stats / Actions — хардкод для `orders`
3. Impact кнопки «↑ Upstream (3)» / «↓ Downstream (5)» и список нод под ними
4. Depth control 1/2/3/∞ в двух состояниях рядом (depth=1 vs depth=2 — разное число нод)  
**Хардкод:** orders → fact_sales → report_monthly как downstream chain  
**Статус:** ⬜ Нужен перед реализацией LOOM-030

## LOOM-030: Impact Analysis + KNOT Inspector

**Приоритет:** 🟡 Важный | **Оценка:** 4–5 ч | **Зависимости:** LOOM-027

### User Story
> Как **Data Engineer**, я хочу нажать одну кнопку на таблице и увидеть **какие объекты затронет изменение** — подсвеченными прямо на canvas — чтобы оценить риск перед деплоем.

### Use Case: Downstream impact перед изменением

```
1. Engineer открыл L2, нашёл таблицу orders (через search)
2. В KNOT Inspector → кнопка «↓ Impact downstream»
3. Quarkus выполняет Cypher traversal:
   MATCH path=(t:DaliTable {name:'orders'})-[:READS_FROM|WRITES_TO*1..3]->(d)
   RETURN d, length(path) as depth ORDER BY depth
4. Canvas подсвечивает затронутые ноды: --wrn (#D4922A)
   Глубина 1: ярко-оранжевый
   Глубина 2: приглушённо-оранжевый
   Глубина 3+: очень тусклый оранжевый
5. KNOT Inspector показывает дерево:
   ↓ orders
     ↓ fact_sales (depth 1) · 3 columns affected
       ↓ report_monthly (depth 2)
     ↓ order_summary (depth 1)
6. Badge у каждой ноды: «affected depth 1»
7. Кнопка «Сбросить highlight»
```

### Use Case: Upstream trace — откуда данные

```
1. Аналитик видит подозрительные данные в fact_sales.total_revenue
2. В KNOT → «↑ Trace upstream»
3. Canvas подсвечивает upstream: --inf (#88B8A8)
4. Видит цепочку: orders → calc_revenue() → fact_sales
5. Понимает: проблема может быть в calc_revenue() — переходит туда
```

### KNOT Inspector — структура при выбранной ноде

```
┌──────────────────────────────────┐
│ KNOT Inspector    [orders]       │
├──────────────────────────────────┤
│ NODE                             │
│ Тип          DaliTable           │
│ Схема        analytics.public    │
│ Колонок      22                  │
│ Уровень      L2                  │
├──────────────────────────────────┤
│ IMPACT                           │
│ [↑ Upstream (3)]  [↓ Downstream (5)] │
│                                  │
│ Upstream sources                 │
│ ● order_items    direct          │
│ ● products       via subq depth 2│
│ ● customers      direct          │
│                                  │
│ STATS                            │
│ Читает из        2 tables        │
│ Пишется          1 routine       │
│ Используется в   2 routines      │
│ Подзапросов      6               │
├──────────────────────────────────┤
│ ДЕЙСТВИЯ                         │
│ [↳ Drill-down L3 column lineage] │
│ [⇅ Impact analysis]              │
│ [≠ Show diff]                    │
└──────────────────────────────────┘
```

### Depth control в KNOT Inspector

| Глубина | Что показывается | Нод примерно |
|---------|-----------------|-------------|
| 1 | Прямые зависимости | 2–10 |
| 2 | Через один промежуточный объект | 5–30 |
| 3 | Три хопа | 10–100 |
| ∞ | Весь граф зависимостей | Предупреждение если > 200 нод |

### Highlight цветовая схема

| Объект | Цвет | Opacity |
|--------|------|---------|
| Выбранная нода | --acc border | 100% |
| Upstream depth 1 | --inf border | 90% |
| Upstream depth 2 | --inf border | 50% |
| Downstream depth 1 | --wrn border | 90% |
| Downstream depth 2 | --wrn border | 50% |
| Незатронутые ноды | нет подсветки | 30% (dimmed) |

**i18n:** `impact.*`, `knot.*` — все секции и поля инспектора. `actions.impactAnalysis`, `actions.showDiff`.

**Готово когда:** кнопка impact подсвечивает ноды; KNOT показывает дерево; глубина настраивается; сброс highlight работает.

---

## PROTO-031: Типы нод L2/L3
**Файл:** `src/components/canvas/nodes/proto/AllNodesProto.tsx`  
**Показывает все типы нод на одном статичном экране:**
- `RoutineNode` — workflow иконка, имя, «3 root stmts», кнопка expand
- `AtomNode` × 6 типов рядом: SUM (sigma), RANK (layout-grid + badge «PARTITION BY»), FILTER (filter), CASE (git-branch), SubQuery FROM ⊂ (--inf), PASSTHROUGH (arrow-right, dim)
- `ColumnNode` — source колонка с типом данных
- `OutputColumnNode` — результирующая колонка, с nullable badge и без  
**Хардкод:** имена и типы фиктивные, главное — визуальное различие между типами  
**Статус:** ⬜ Нужен перед реализацией LOOM-031

## LOOM-031: Оставшиеся типы нод L2/L3

**Приоритет:** 🟡 Важный | **Оценка:** 4–5 ч | **Зависимости:** LOOM-028

### User Story
> Как **Data Engineer**, я хочу видеть **все типы объектов** lineage — рутины, атомы, колонки, JOIN — не только таблицы, чтобы понимать полную картину трансформаций.

### RoutineNode (L2)

```
┌── [workflow icon] calc_order_revenue()  ─────┐
│ Язык: PL/SQL · 3 root statements            │
│ Читает: order_items, products               │
│ Пишет: orders                               │
│ [▸ expand statements]  [⊙ set as start]     │
└──────────────────────────────────────────────┘
```

**Expand statements:** показывает StatementNode рядом через NESTED_IN рёбра.

### AtomNode (L3)

| Тип атома | Отображение | Пример |
|-----------|-------------|--------|
| Арифметика | `[× MULTIPLY]` | qty × price |
| Агрегат | `[Σ SUM]` | SUM(total) |
| Оконная функция | `[▦ RANK]` + badge «PARTITION BY region» | RANK() OVER() |
| Фильтр | `[⊗ FILTER]` | WHERE status = 'active' |
| EXTRACT | `[📅 EXTRACT]` | EXTRACT(YEAR FROM date) |
| CASE WHEN | `[? CASE]` | CASE WHEN ... END |
| Подзапрос-ref | `[⊂ SubQuery]` + тип badge | FROM / WHERE / SELECT |
| COALESCE | `[◈ COALESCE]` | COALESCE(a, b, 0) |
| CAST | `[→ CAST]` | CAST(x AS numeric) |
| GROUP BY | `[≡ GROUP BY]` | GROUP BY region, year |

### Таблица выбора: иконки для AtomNode

| Тип операции | Lucide иконка | Цвет акцента |
|-------------|--------------|-------------|
| SUM / AVG / MIN / MAX | `sigma` | --wrn |
| COUNT | `hash` | --wrn |
| RANK / ROW_NUMBER / DENSE_RANK | `layout-grid` | --dan |
| FILTER / WHERE | `filter` | --inf |
| JOIN | `git-merge` | --inf |
| EXTRACT | `calendar` | --t2 |
| CASE WHEN | `git-branch` | --wrn |
| SubQuery ref | `link-2` | зависит от типа |
| GROUP BY | `layers` | --t2 |
| PASSTHROUGH | `arrow-right` | --t3 тусклый |

### ColumnNode (L3)

```
┌── [columns-3] order_items.quantity ──┐
│ Тип: int4 · source                  │
│ Таблица: order_items                 │
│ PK: нет · FK: нет                   │
└──────────────────────────────────────┘
```

### OutputColumnNode (L3)

```
┌── [arrow-right-to-line] orders.total_amount ──┐
│ Тип: numeric(12,2) · target                  │
│ Маппинг: computed · SUM                      │
│ [nullable в YEARLY branch] ← badge если NULL │
└───────────────────────────────────────────────┘
```

**i18n:** `nodeTypes.*` для всех типов нод, `atom.operations.*` для каждой операции AtomNode, `atom.partitionBy`, `atom.orderBy`, `mapping.nullable`.

**Готово когда:** все типы нод рендерятся с правильными иконками и данными; AtomNode показывает тип операции; window function badge виден.

---

## PROTO-032: Search Panel
**Файл:** `src/components/panels/proto/SearchPanelProto.tsx`  
**Показывает два состояния левой панели рядом:**
1. Пустой поиск — дерево с секциями (Sources / Transformation / Target / Statements)
2. Поиск «total» — результаты с highlight совпадения, каждый результат: иконка + имя + тип + путь  
**Дополнительно:** секция «Скрытые ноды» с одним элементом и кнопкой восстановить  
**Хардкод:** 4–5 результатов поиска, статичный список  
**Статус:** ✅ Базовый layout утверждён, дополнения нужны → прототип в проекте перед IMPL

## LOOM-032: Search Panel + левая панель

**Приоритет:** 🟡 Важный | **Оценка:** 3–4 ч | **Зависимости:** LOOM-019, LOOM-021

### User Story
> Как **любой пользователь**, я хочу найти нужную таблицу или колонку **за 2–3 нажатия** — и сразу оказаться рядом с ней на canvas.

### Use Case: Быстрый поиск и навигация

```
1. Пользователь нажимает ⌘K или кликает в search input
2. Вводит «total_amount»
3. Левая панель показывает результаты:
   [columns-3] order_items.total_amount  · DaliColumn
   [columns-3] orders.total_amount       · DaliColumn  ← highlighted
   [workflow]  calc_order_revenue()       · DaliRoutine · mentions total_amount

4. Кликает на «orders.total_amount»
   → Canvas центрирует на таблице orders (fitView на эту ноду)
   → Нода выделяется (selected state)
   → KNOT Inspector открывает детали total_amount
   → Если нода не на текущем canvas — предлагает перейти на нужный уровень

5. Если total_amount не на текущем canvas:
   Появляется popup: «orders.total_amount находится на L2 · analytics.public
   [Перейти →]»
```

### Use Case: Фильтр по типу в дереве

```
1. Пользователь кликает чип «Рутины»
2. Дерево фильтруется: только DaliRoutine объекты
3. Показывает: calc_order_revenue(), get_sales_report(), ...
4. Кликает чип «Все» → сброс фильтра
```

### Структура левой панели

```
┌─────────────────────────┐
│ 🔍 ПОИСК                │
├─────────────────────────┤
│ [🔍 таблицы, колонки…]  │ ← search input
├─────────────────────────┤
│ [Все] [Таблицы] [Рут.] [Кол.] [Стейт.] │ ← type chips
├─────────────────────────┤
│ РЕЗУЛЬТАТЫ / ДЕРЕВО     │
│ ─── Источники ────      │
│ [table] order_items     │
│ [table] products        │
│ ─── Трансформация ────  │
│ [workflow] calc_rev ✓   │ ← start object badge
│ ─── Целевая таблица ─── │
│ [table] orders · 22     │
│ ─── Скрытые ноды ─────  │ ← если есть скрытые
│ [eye-off] products (1)  │ ← восстановить
└─────────────────────────┘
```

### Секция «Скрытые ноды»

Появляется в дереве если пользователь нажал 🔴 (закрыть ноду). Позволяет восстановить скрытые ноды не обновляя страницу.

**i18n:** `search.*` — placeholder, noResults, resultCount, filters.*, sections.*, hidden.*, notOnCanvas, goToLevel.

**Готово когда:** поиск находит объекты по всем уровням; клик центрирует canvas; чипы фильтруют; скрытые ноды восстанавливаются.

---

## PROTO-033: Role-based UI
**Файл:** `src/components/proto/RoleUIProto.tsx`  
**Показывает два варианта header + KNOT Inspector рядом:**
- Слева: `viewer` — нет кнопки аннотации, нет edit, статусбар «только чтение»
- Справа: `editor` — все кнопки видны  
**Хардкод:** два экземпляра одного компонента с разным prop `role`  
**Статус:** ⬜ Нужен перед реализацией LOOM-033 (небольшой, ~1 ч)

## LOOM-033: Role-based UI + Read-only режим

**Приоритет:** 🟢 Желательный | **Оценка:** 2–3 ч | **Зависимости:** LOOM-012

### User Story
> Как **viewer**, я хочу видеть только то что могу читать — без кнопок редактирования которые мне недоступны — чтобы интерфейс не вводил в заблуждение.

### Таблица прав по роли

| UI элемент | viewer | editor | admin |
|-----------|--------|--------|-------|
| Просмотр lineage | ✅ | ✅ | ✅ |
| Drill-down L1→L2→L3 | ✅ | ✅ | ✅ |
| Search | ✅ | ✅ | ✅ |
| Export PNG/SVG | ✅ | ✅ | ✅ |
| Добавить аннотацию | ❌ скрыто | ✅ | ✅ |
| Редактировать маппинг | ❌ скрыто | ✅ | ✅ |
| Скрыть ноду (🔴) | ✅ (только локально) | ✅ | ✅ |
| Удалить lineage запись | ❌ | ❌ | ✅ |
| Управление пользователями | ❌ | ❌ | ✅ |

### Реализация

```typescript
// src/hooks/useRBAC.ts
export function useRBAC() {
  const { user } = useAuthStore();
  return {
    canEdit: user?.role === 'editor' || user?.role === 'admin',
    canAdmin: user?.role === 'admin',
    isViewer: user?.role === 'viewer',
  };
}

// В компоненте:
const { canEdit } = useRBAC();
// ...
{canEdit && <AnnotationButton />}
```

**i18n:** `roles.*` — viewer, editor, admin, readOnly, readWrite. Использовать в header badge, statusbar, KNOT actions.

**Готово когда:** viewer не видит edit-кнопки; статусбар показывает «только чтение» для viewer.

---

## PROTO-034: Performance
**Прототип:** не нужен — чисто техническая задача, UI не меняется.

## LOOM-034: Performance — виртуализация и prefetch

**Приоритет:** 🟢 Желательный | **Оценка:** 2–3 ч | **Зависимости:** LOOM-031

### User Story
> Как **Data Engineer**, я хочу чтобы таблица с 200 колонками **не тормозила** интерфейс — даже в expanded state.

### Проблема

TableNode в expanded state с 200 колонками = 200 DOM узлов × N нод на canvas = лаг.

### Решение: виртуализация колонок

```typescript
// Только в expanded state при > 20 колонок
import { useVirtualizer } from '@tanstack/react-virtual';

const virtualizer = useVirtualizer({
  count: visibleColumns.length,
  getScrollElement: () => scrollRef.current,
  estimateSize: () => 22, // высота строки колонки
  overscan: 5,
});
```

### Prefetch при hover

```typescript
// При hover на ноду — prefetch следующего уровня
onMouseEnter={() => {
  queryClient.prefetchQuery({
    queryKey: ['explore', nodeId],
    queryFn: () => gqlClient.request(EXPLORE_QUERY, { scope: nodeId }),
    staleTime: 30_000,
  });
}}
```

### fitView после layout

```typescript
// После каждого ELK пересчёта
const { fitView } = useReactFlow();
// ...
const layoutedNodes = await applyELKLayout(rfNodes, rfEdges);
setNodes(layoutedNodes);
setEdges(rfEdges);
// Небольшая задержка чтобы React успел отрендерить
setTimeout(() => fitView({ padding: 0.12, duration: 300 }), 50);
```

**i18n:** `status.graphLoading` — показывать пока ELK считает layout. Остальных новых ключей нет.

**Готово когда:** 200 колонок в expanded — плавный скролл; hover на ноду — следующий уровень загружается заранее; layout всегда центрируется.

---


---

## BUG-001 + LOOM-035–037: Навигация L1→L2 с реальными данными

> **Контекст:** после подключения GraphQL (Phase 2) переход L1→L2 перестал работать.  
> `LoomCanvas.tsx` по-прежнему вызывает `fetchMockOverview/Explore` вместо реальных хуков.  
> `drillDown(nodeId)` передаёт ArcadeDB `@rid` вида `"#11:5"`, а `fetchMockExplore` ожидает `"schema-public"`.

---

### LOOM-035: Тестовый фреймворк
**Приоритет:** 🔴 Критический | **Оценка:** 3–4 ч | **Зависимости:** нет

#### User Story
> Как **разработчик**, я хочу иметь unit и E2E тесты для навигации, чтобы такие баги как сломанный drill-down **обнаруживались автоматически** — а не только когда подключат реальные данные.

#### Что установить

```bash
# Unit + integration (уже в стеке)
npm install -D vitest @vitest/ui jsdom
npm install -D @testing-library/react @testing-library/user-event @testing-library/jest-dom

# E2E
npm install -D @playwright/test
npx playwright install chromium
```

#### `vite.config.ts` — добавить:
```typescript
test: {
  globals: true,
  environment: 'jsdom',
  setupFiles: ['./src/test/setup.ts'],
}
```

#### `src/test/setup.ts`:
```typescript
import '@testing-library/jest-dom';
import { vi } from 'vitest';

// Mock ELK — тесты не должны ждать WASM
vi.mock('../utils/layoutGraph', () => ({
  applyELKLayout: (nodes: unknown[]) => Promise.resolve(nodes),
}));

// Mock GraphQL клиент
vi.mock('../services/lineage', () => ({
  fetchOverview: vi.fn(),
  fetchExplore:  vi.fn(),
  fetchLineage:  vi.fn(),
}));
```

#### Структура тестов:
```
src/
├── test/
│   ├── setup.ts
│   ├── unit/
│   │   ├── loomStore.test.ts        ← drillDown, navigateBack, navigateToLevel
│   │   ├── transformGraph.test.ts   ← GraphQL response → LoomNode/LoomEdge
│   │   └── authStore.test.ts        ← login, logout, checkSession
│   └── integration/
│       └── navigation.test.tsx      ← LoomCanvas drill-down L1→L2→L3
e2e/
└── navigation.spec.ts               ← Playwright: login → L1 → dblclick → L2
```

#### `package.json` — добавить scripts:
```json
"test":     "vitest run",
"test:ui":  "vitest --ui",
"test:e2e": "playwright test",
"test:all": "vitest run && playwright test"
```

**i18n:** нет новых ключей.

**Готово когда:** `npm test` проходит, `npm run test:e2e` запускает Playwright в headless-режиме.

---

### BUG-001 / LOOM-036: Исправить навигацию L1→L2
**Приоритет:** 🔴 Критический | **Оценка:** 2–3 ч | **Зависимости:** LOOM-017, LOOM-019

#### Root cause

`LoomCanvas.tsx` содержит функцию `fetchLevel()` которая всё ещё вызывает `fetchMockOverview/Explore/ColumnLineage`. Реальные GraphQL-функции из `services/lineage.ts` (LOOM-017) созданы, но не подключены к canvas.

Дополнительно: `drillDown(nodeId)` передаёт `node.id` из React Flow. При реальных данных `node.id` = ArcadeDB `@rid` = `"#11:5"`. `fetchMockExplore("schema-public")` этот формат не понимает — в реальности нужен `fetchExplore(rid: string)` → GraphQL `explore(scope: "#11:5")`.

```
Mock flow (рабочий):
  L1 SchemaNode.id = "schema-public"
  drillDown("schema-public") → fetchMockExplore("schema-public") ✅

Real flow (сломанный):
  L1 SchemaNode.id = "#11:5"  (ArcadeDB @rid)
  drillDown("#11:5") → fetchMockExplore("#11:5") → не то ✗
  нужно:           → fetchExplore("#11:5")        ✅
```

#### Фикс — `LoomCanvas.tsx`

Заменить `fetchLevel()` на вызовы реальных функций из `services/lineage.ts`:

```typescript
// БЫЛО (mock):
import { fetchMockOverview, fetchMockExplore, fetchMockColumnLineage } from '../../services/mockData';

async function fetchLevel(level: ViewLevel, scope: string | null) {
  if (level === 'L1') return fetchMockOverview();
  if (level === 'L2') return fetchMockExplore(scope ?? 'schema-public');
  return fetchMockColumnLineage(scope ?? 'tbl-orders');
}

// СТАЛО (реальные данные):
import { fetchOverview, fetchExplore, fetchLineage } from '../../services/lineage';

async function fetchLevel(level: ViewLevel, scope: string | null) {
  if (level === 'L1') return fetchOverview();
  if (level === 'L2') {
    if (!scope) throw new Error('[LOOM] L2 requires scope (rid of schema/package)');
    return fetchExplore(scope);
  }
  if (!scope) throw new Error('[LOOM] L3 requires scope (rid of table/column)');
  return fetchLineage(scope);
}
```

#### Фикс — `SchemaNode.tsx` / `PackageNode.tsx`

Убедиться что `childrenAvailable` корректно маппится из GraphQL-ответа в `transformGqlOverview`. Если `childrenAvailable: false` — `drillDown` не вызывается.

```typescript
// transformGraph.ts — проверить что поле маппится:
data: {
  ...n.data,
  childrenAvailable: n.data.childrenAvailable ?? true, // default true если API не вернул
}
```

#### Фикс — обработка 401 при drill-down

`fetchLevel` бросает при 401. В `LoomCanvasInner` `.catch` только логирует ошибку — нужно добавить auto-logout:

```typescript
.catch((err) => {
  console.error('[LOOM] Failed to load graph data', err);
  if (err?.status === 401 || err?.message?.includes('401')) {
    useAuthStore.getState().logout();
  }
})
```

**i18n:** `status.error` уже есть. Добавить показ ошибки на canvas если `fetchLevel` бросает.

**Готово когда:** double-click на SchemaNode на L1 с реальными данными → загружает L2; breadcrumb показывает правильный путь; 401 → редирект на /login.

---

### LOOM-037: Тесты навигации L1→L2→L3
**Приоритет:** 🔴 Критический | **Оценка:** 3–4 ч | **Зависимости:** LOOM-035, LOOM-036

#### User Story
> Как **разработчик**, я хочу чтобы навигация между уровнями была покрыта тестами — и следующее подключение реального API **не сломало** drill-down незаметно.

#### Unit тесты — `src/test/unit/loomStore.test.ts`

```typescript
import { describe, it, expect, beforeEach } from 'vitest';
import { useLoomStore } from '../../stores/loomStore';

describe('loomStore / drillDown', () => {
  beforeEach(() => {
    useLoomStore.setState({
      viewLevel: 'L1', currentScope: null, navigationStack: [],
    });
  });

  it('L1 → L2: viewLevel becomes L2, scope set to nodeId', () => {
    useLoomStore.getState().drillDown('#11:5', 'public');
    const { viewLevel, currentScope, navigationStack } = useLoomStore.getState();
    expect(viewLevel).toBe('L2');
    expect(currentScope).toBe('#11:5');
    expect(navigationStack).toHaveLength(1);
    expect(navigationStack[0].level).toBe('L1');
  });

  it('L2 → L3: pushes L2 onto stack', () => {
    useLoomStore.getState().drillDown('#11:5', 'public');   // L1→L2
    useLoomStore.getState().drillDown('#22:3', 'orders');   // L2→L3
    const { viewLevel, navigationStack } = useLoomStore.getState();
    expect(viewLevel).toBe('L3');
    expect(navigationStack).toHaveLength(2);
  });

  it('navigateBack(0) returns to L1 and clears stack', () => {
    useLoomStore.getState().drillDown('#11:5', 'public');
    useLoomStore.getState().navigateBack(0);
    const { viewLevel, currentScope, navigationStack } = useLoomStore.getState();
    expect(viewLevel).toBe('L1');
    expect(currentScope).toBeNull();
    expect(navigationStack).toHaveLength(0);
  });

  it('navigateToLevel resets scope and stack', () => {
    useLoomStore.getState().drillDown('#11:5', 'public');
    useLoomStore.getState().navigateToLevel('L1');
    const { viewLevel, currentScope, navigationStack } = useLoomStore.getState();
    expect(viewLevel).toBe('L1');
    expect(currentScope).toBeNull();
    expect(navigationStack).toHaveLength(0);
  });
});
```

#### Integration тест — `src/test/integration/navigation.test.tsx`

```typescript
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { vi } from 'vitest';
import * as lineage from '../../services/lineage';
import { LoomCanvas } from '../../components/canvas/LoomCanvas';

const mockOverview = {
  nodes: [{ id: '#11:5', type: 'DaliSchema', data: {
    label: 'public', nodeType: 'DaliSchema',
    tablesCount: 10, routinesCount: 3, childrenAvailable: true, metadata: {}
  }}],
  edges: [],
};

const mockExplore = {
  nodes: [{ id: '#22:1', type: 'DaliTable', data: {
    label: 'orders', nodeType: 'DaliTable', schema: 'public',
    columns: [], childrenAvailable: true, metadata: {}
  }}],
  edges: [],
};

describe('LoomCanvas / navigation', () => {
  it('double-click SchemaNode calls drillDown with real @rid', async () => {
    vi.mocked(lineage.fetchOverview).mockResolvedValue(mockOverview);
    vi.mocked(lineage.fetchExplore).mockResolvedValue(mockExplore);

    render(<LoomCanvas />);
    await waitFor(() => screen.getByText('public'));

    // double-click на SchemaNode
    fireEvent.doubleClick(screen.getByText('public').closest('.react-flow__node')!);

    await waitFor(() => {
      expect(lineage.fetchExplore).toHaveBeenCalledWith('#11:5');
      expect(screen.getByText('orders')).toBeInTheDocument();
    });
  });

  it('shows error state when fetchExplore throws', async () => {
    vi.mocked(lineage.fetchOverview).mockResolvedValue(mockOverview);
    vi.mocked(lineage.fetchExplore).mockRejectedValue(new Error('Network error'));

    render(<LoomCanvas />);
    await waitFor(() => screen.getByText('public'));
    fireEvent.doubleClick(screen.getByText('public').closest('.react-flow__node')!);

    await waitFor(() => {
      expect(screen.getByText(/failed to load/i)).toBeInTheDocument();
    });
  });
});
```

#### E2E тест — `e2e/navigation.spec.ts`

```typescript
import { test, expect } from '@playwright/test';

test.describe('L1 → L2 navigation', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.fill('[name=username]', 'admin');
    await page.fill('[name=password]', 'admin');
    await page.click('button[type=submit]');
    await page.waitForURL('/');
  });

  test('double-click SchemaNode loads L2', async ({ page }) => {
    // Ждём L1 canvas
    await expect(page.locator('.react-flow__node')).toHaveCount({ min: 1 }, { timeout: 10000 });

    // Находим SchemaNode и double-click
    const schemaNode = page.locator('.react-flow__node [data-node-type="DaliSchema"]').first();
    await schemaNode.dblclick();

    // L2 должен загрузиться — breadcrumb обновится
    await expect(page.locator('[data-testid="breadcrumb"]')).toContainText('public', { timeout: 8000 });

    // TableNode появился
    await expect(page.locator('.react-flow__node [data-node-type="DaliTable"]').first()).toBeVisible();
  });

  test('breadcrumb click returns to L1', async ({ page }) => {
    const schemaNode = page.locator('.react-flow__node [data-node-type="DaliSchema"]').first();
    await schemaNode.dblclick();
    await expect(page.locator('[data-testid="breadcrumb"]')).toContainText('public', { timeout: 8000 });

    // Кликаем на «Overview» в breadcrumb
    await page.locator('[data-testid="breadcrumb-overview"]').click();

    // Возвращаемся на L1
    await expect(page.locator('.react-flow__node [data-node-type="DaliSchema"]').first()).toBeVisible();
  });

  test('401 during drill-down redirects to login', async ({ page }) => {
    // Инвалидируем сессию
    await page.evaluate(() => document.cookie = 'token=invalid');

    const schemaNode = page.locator('.react-flow__node [data-node-type="DaliSchema"]').first();
    await schemaNode.dblclick();

    await expect(page).toHaveURL('/login', { timeout: 5000 });
  });
});
```

#### `data-testid` атрибуты — добавить в компоненты

```tsx
// Breadcrumb.tsx
<div data-testid="breadcrumb" ...>
  <span data-testid="breadcrumb-overview" ...>

// Nodes: добавить data-node-type для Playwright
<div data-node-type={data.nodeType} ...>
```

**i18n:** нет новых ключей.

**Готово когда:** `npm test` — все unit + integration тесты зелёные; `npm run test:e2e` — три E2E сценария проходят на живом стеке (Quarkus + rbac-proxy + Vite).

---

### Порядок выполнения

```
LOOM-035 (тестовый фреймворк)    ← установка, setup.ts, package.json scripts
    │
BUG-001 / LOOM-036 (bug fix)     ← заменить fetchMock* на fetchOverview/Explore/Lineage
    │                                childrenAvailable fallback, 401 handling
    │
LOOM-037 (тесты навигации)       ← unit (loomStore) + integration (LoomCanvas) + E2E
```

> **Приоритет:** три задачи блокируют весь Phase 3. Пока навигация сломана — новые фичи L2/L3 невозможно проверить вручную.

## Порядок выполнения Phase 3

```
LOOM-035 (тестовый фреймворк)    ← ПЕРВЫМ: установка Vitest + Playwright
    │
BUG-001 / LOOM-036 (фикс L1→L2) ← БЛОКЕР: заменить fetchMock* на реальные хуки
    │
LOOM-037 (тесты навигации)       ← unit + integration + E2E зелёные
    │
LOOM-023 (canvas read-only)      ← 30 мин
    │
LOOM-023b (filter toolbar)
    │
    ├── LOOM-024 (ApplicationNode + ServiceNode)
    │
    ├── LOOM-025 (Column Mapping на L2)
    │       │
    │       └── LOOM-026 (TableNode три состояния)
    │               │
    │               └── LOOM-027 (Expand buttons upstream/downstream)
    │
    ├── LOOM-028 (StatementNode + StatementGroupNode)
    │       │
    │       └── LOOM-029 (SubQuery drill-down L3→L3)
    │
    ├── LOOM-030 (Impact Analysis + KNOT Inspector)
    │
    ├── LOOM-031 (AtomNode + ColumnNode + OutputColumnNode + RoutineNode)
    │
    ├── LOOM-032 (Search Panel + левая панель)
    │
    ├── LOOM-033 (Role-based UI)
    │
    └── LOOM-034 (Performance: virtual + prefetch + fitView)
```

> **LOOM-035/036/037 блокируют все остальные задачи** — пока навигация сломана, L2/L3 фичи невозможно проверить вручную.

## Сводная таблица задач Phase 3

| # | Прототип | Задача | Приоритет | Оценка | Главный UC |
|---|----------|--------|-----------|--------|-----------|
| **035** | не нужен | **Тестовый фреймворк** | 🔴 | 3–4 ч | Автоматически ловить регрессии |
| **BUG/036** | не нужен | **Фикс навигации L1→L2** | 🔴 | 2–3 ч | Drill-down с реальными данными |
| **037** | не нужен | **Тесты навигации** | 🔴 | 3–4 ч | Unit + Integration + E2E |
| 023 | не нужен | Canvas read-only | 🔴 | 30 мин | Не сдвигать граф случайно |
| 023b | ✅ готов | Filter Toolbar | 🔴 | 3–4 ч | Фокус на одном поле |
| 024 | ⬜ нужен | ApplicationNode + ServiceNode | 🔴 | 4–5 ч | Карта приложений |
| 025 | ✅ готов | Column Mapping L2 | 🔴 | 4–5 ч | «Откуда это поле» |
| 026 | ✅ готов | TableNode три состояния | 🔴 | 3–4 ч | 200-колоночная таблица |
| 027 | ⬜ нужен | Expand upstream/downstream | 🟡 | 3–4 ч | Органический рост графа |
| 028 | ✅ частично | StatementNode + GroupNode | 🟡 | 5–6 ч | Вложенные SQL операции |
| 029 | ✅ частично | SubQuery drill-down | 🟡 | 3–4 ч | 6 уровней подзапросов |
| 030 | ⬜ нужен | Impact Analysis + KNOT | 🟡 | 4–5 ч | Что сломается? |
| 031 | ⬜ нужен | Типы нод L2/L3 | 🟡 | 4–5 ч | Полная картина |
| 032 | ✅ частично | Search Panel | 🟡 | 3–4 ч | Найти за 2 клика |
| 033 | ⬜ нужен (мал.) | Role-based UI | 🟢 | 2–3 ч | Не вводить в заблуждение |
| 034 | не нужен | Performance | 🟢 | 2–3 ч | Плавность |

**Прототипы нужны перед стартом:** LOOM-024, LOOM-027, LOOM-030, LOOM-031, LOOM-033  
**Прототипы готовы / частично готовы:** LOOM-023b, LOOM-025, LOOM-026, LOOM-028, LOOM-029, LOOM-032  
**Прототип не нужен:** LOOM-023, LOOM-034, LOOM-035/036/037

**Итого Phase 3: 15 задач · ~50–62 ч · ~5–6 недель solo-dev**
