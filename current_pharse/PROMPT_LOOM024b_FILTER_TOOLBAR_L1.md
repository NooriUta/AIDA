# LOOM-024b: L1 Filter Toolbar — Промт для реализации

**Задача:** Показать L1-специфический тулбар; подключить dim-эффект при scope-фильтре
**Размер:** S (~2–3 часа)
**Зависит от:** LOOM-024 (L1 hierarchy) — ✅ готово
**Независима от:** LOOM-025+ (Column Mapping, остальной спринт)

---

## Текущее состояние кода — что уже реализовано

Перед началом реализации убедись, что используешь уже готовые куски:

### `loomStore.ts` — L1 scope stack полностью работает

```typescript
// Уже есть в loomStore. НЕ ДУБЛИРОВАТЬ:
l1ScopeStack: L1ScopeItem[]          // стек { nodeId, label, nodeType }
pushL1Scope(nodeId, label, nodeType) // добавить скоуп (double-click на App)
popL1ScopeToIndex(index)             // вернуться к индексу
clearL1Scope()                       // сброс → все ноды видны
```

Текущий `activeScope` — это `l1ScopeStack[l1ScopeStack.length - 1]` или `null`.
→ Не добавлять отдельные `startObjectId`/`startObjectLabel` в стор — они уже в стеке.

### `ApplicationNode.tsx` — double-click подключён (строка 38)

```typescript
onDoubleClick={(e) => {
  e.stopPropagation();
  pushL1Scope(id, data.label, 'DaliApplication');  // ← уже работает
}}
```

### `filterGraph.ts` — утилиты готовы

```typescript
dimNodesOutsideScope(nodes, scopeNodeIds)   // → opacity 0.2 для вне скоупа
reachableNodeIds(edges, scopeNodeId)        // → Set<string> BFS по рёбрам
filterGraphByScope(nodes, edges, id)        // полный subgraph (альтернатива)
```

### `FilterToolbar.tsx` — строка 93

```typescript
if (viewLevel === 'L1') return null;   // ← меняем на <L1Toolbar />
```

---

## Архитектурное решение

Два компонента, два потока:

| Задача | Решение |
|--------|---------|
| Scope pill (какой App выбран) | Читаем `l1ScopeStack` — уже есть |
| Depth / Direction / System-level | Новый `l1Filter` объект в store |
| Dim-эффект на canvas | `dimNodesOutsideScope` в `LoomCanvas` |
| Fit view при scope | `useReactFlow().fitView()` в `LoomCanvas` |

---

## Шаг 1 — PROTO (без обработчиков)

Файл: `src/components/layout/proto/FilterToolbarL1Proto.tsx`

Статичный визуальный прототип. Хардкод данных. Без импортов из store/hooks.
Показывает **все состояния рядом** — три варианта toolbar:

```tsx
// src/components/layout/proto/FilterToolbarL1Proto.tsx
// PROTO ONLY — no event handlers, hardcoded data
// Удалить или рефакторить после утверждения → LOOM-024b IMPL

export function FilterToolbarL1Proto() {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 12, padding: 16, background: 'var(--bg1)' }}>

      {/* Вариант A: нет scope, все системы */}
      <FilterToolbarL1Display
        startLabel="все системы"
        startActive={false}
        depth={2}
        dirUp={true}
        dirDown={true}
        systemLevel={false}
        levelBadge="L1 · depth 2"
      />

      {/* Вариант B: выбрана конкретная система */}
      <FilterToolbarL1Display
        startLabel="OrderSystem"
        startActive={true}
        depth={3}
        dirUp={true}
        dirDown={false}
        systemLevel={false}
        levelBadge="L1 · depth 3"
      />

      {/* Вариант C: system-level view включён */}
      <FilterToolbarL1Display
        startLabel="OrderSystem"
        startActive={true}
        depth={2}
        dirUp={true}
        dirDown={true}
        systemLevel={true}
        levelBadge="L1 · system-level"
      />

    </div>
  );
}
```

### Внешний вид одного toolbar (FilterToolbarL1Display)

```
[⊙ OrderSystem ×] | [Глубина: 1] [2✓] [3] [∞] | [↑ Upstream] [↓ Downstream] | [⎇ System-level]   L1·depth 2
```

Все элементы слева направо, высота **32px** (компактнее L2/L3 toolbar 40px), padding 0 10px.

```tsx
interface FilterToolbarL1DisplayProps {
  startLabel:  string;          // "все системы" | "OrderSystem"
  startActive: boolean;         // true = показать × и цвет --acc
  depth:       1 | 2 | 3 | 99; // 99 = ∞
  dirUp:       boolean;
  dirDown:     boolean;
  systemLevel: boolean;         // toggle: скрыть СУБД, только системы
  levelBadge:  string;          // "L1 · depth 2" | "L1 · system-level"
}
```

### Детальный CSS каждого элемента

**Контейнер toolbar:**
```css
height: 32px;
background: var(--bg0);
border-bottom: 0.5px solid var(--bd);
display: flex;
align-items: center;
gap: 5px;
padding: 0 10px;
flex-shrink: 0;
```

**Start object pill:**
```css
/* inactive (нет scope) */
display: flex; align-items: center; gap: 4px;
padding: 2px 8px; border-radius: 4px;
border: 0.5px solid var(--bd);
font-size: 10px; color: var(--t2);
cursor: default; white-space: nowrap;

/* active (scope выбран) */
border-color: var(--acc);
color: var(--acc);
background: rgba(168,184,96,.07);
```

Иконка: `<Scan size={10} />` (Lucide) или инлайн SVG (grid 4 квадрата из ApplicationNode)
Если `startActive`: после имени показывать кнопку `×` в `color: var(--t3)`, font-size 9px

**Разделитель:**
```css
width: 0.5px; height: 16px; background: var(--bd); flex-shrink: 0; margin: 0 2px;
```

**Лейбл «Глубина:»:**
```css
font-size: 10px; color: var(--t3); white-space: nowrap;
```

**Кнопки глубины (1 / 2 / 3 / ∞):**
```css
/* base */
padding: 2px 6px; border-radius: 3px;
border: 0.5px solid var(--bd);
font-size: 9px; color: var(--t3);
cursor: pointer; background: transparent;

/* .on (активная) */
border-color: var(--acc); color: var(--acc);
background: rgba(168,184,96,.08);
```

**Direction кнопки:**
```css
/* base */
padding: 2px 7px; border-radius: 3px;
border: 0.5px solid var(--bd);
font-size: 10px; color: var(--t3);
cursor: pointer; background: transparent;
display: flex; align-items: center; gap: 3px;

/* ↑ Upstream active */
border-color: var(--inf); color: var(--inf);
background: rgba(136,184,168,.08);

/* ↓ Downstream active */
border-color: var(--wrn); color: var(--wrn);
background: rgba(212,146,42,.08);
```

**System-level toggle:**
```css
/* base */
padding: 2px 8px; border-radius: 3px;
border: 0.5px solid var(--bd);
font-size: 9px; color: var(--t3);
cursor: pointer; background: transparent;
display: flex; align-items: center; gap: 4px;
white-space: nowrap;

/* .on */
border-color: var(--wrn); color: var(--wrn);
background: rgba(212,146,42,.07);
```

**Level badge (right):**
```css
margin-left: auto;
font-size: 9px; padding: 2px 7px;
border: 0.5px solid var(--bd); border-radius: 3px;
color: var(--t3); font-family: var(--mono);
white-space: nowrap;
```

---

## Шаг 2 — IMPL

### 2.1 Новые поля в `loomStore.ts` (только то, чего ещё нет)

`l1ScopeStack` уже есть — не трогать. Добавить только параметры отображения:

```typescript
// src/stores/loomStore.ts — добавить в interface LoomStore:

// ── L1 Toolbar display params (LOOM-024b) ────────────────────────────────────
l1Filter: {
  depth:       1 | 2 | 3 | 99;   // 99 = ∞, default: 2
  dirUp:       boolean;            // default: true
  dirDown:     boolean;            // default: true
  systemLevel: boolean;            // default: false — скрыть DB, показать только App
};

// ── L1 Toolbar actions ───────────────────────────────────────────────────────
setL1Depth:         (depth: 1 | 2 | 3 | 99) => void;
toggleL1DirUp:      () => void;
toggleL1DirDown:    () => void;
toggleL1SystemLevel:() => void;
```

```typescript
// начальные значения:
l1Filter: { depth: 2, dirUp: true, dirDown: true, systemLevel: false },

// implementations:
setL1Depth:          (depth)  => set(s => ({ l1Filter: { ...s.l1Filter, depth } })),
toggleL1DirUp:       ()       => set(s => ({ l1Filter: { ...s.l1Filter, dirUp: !s.l1Filter.dirUp } })),
toggleL1DirDown:     ()       => set(s => ({ l1Filter: { ...s.l1Filter, dirDown: !s.l1Filter.dirDown } })),
toggleL1SystemLevel: ()       => set(s => ({ l1Filter: { ...s.l1Filter, systemLevel: !s.l1Filter.systemLevel } })),
```

Также сбросить `l1Filter` в `navigateToLevel`:
```typescript
navigateToLevel: (level) => {
  set({
    // ... existing resets ...
    l1Filter: { depth: 2, dirUp: true, dirDown: true, systemLevel: false },
  });
},
```

### 2.2 Компонент `FilterToolbarL1.tsx` (новый файл)

Не использует `lucide-react` — только inline SVG как в остальных нодах.

```tsx
// src/components/layout/FilterToolbarL1.tsx
import { memo } from 'react';
import { useTranslation } from 'react-i18next';
import { useLoomStore } from '../../stores/loomStore';

const DEPTHS = [1, 2, 3, 99] as const;
type Depth = typeof DEPTHS[number];

function Divider() {
  return (
    <div style={{
      width: '0.5px', height: 16,
      background: 'var(--bd)', flexShrink: 0, margin: '0 2px',
    }} />
  );
}

function IconApp() {
  return (
    <svg width="10" height="10" viewBox="0 0 16 16" fill="none" style={{ flexShrink: 0 }}>
      <rect x="1" y="1" width="6" height="6" rx="1.5" fill="var(--acc)" opacity="0.8" />
      <rect x="9" y="1" width="6" height="6" rx="1.5" fill="var(--acc)" opacity="0.5" />
      <rect x="1" y="9" width="6" height="6" rx="1.5" fill="var(--acc)" opacity="0.5" />
      <rect x="9" y="9" width="6" height="6" rx="1.5" fill="var(--acc)" opacity="0.25" />
    </svg>
  );
}

function IconLayers() {
  return (
    <svg width="10" height="10" viewBox="0 0 12 12" fill="none" style={{ flexShrink: 0 }}>
      <path d="M1 4l5-3 5 3-5 3-5-3z" stroke="currentColor" strokeWidth="1.2" strokeLinejoin="round" fill="none" />
      <path d="M1 7l5 3 5-3"           stroke="currentColor" strokeWidth="1.2" strokeLinecap="round" />
    </svg>
  );
}

export const FilterToolbarL1 = memo(() => {
  const { t } = useTranslation();
  const {
    l1ScopeStack, clearL1Scope,
    l1Filter, setL1Depth, toggleL1DirUp, toggleL1DirDown, toggleL1SystemLevel,
  } = useLoomStore();

  const activeScope = l1ScopeStack.length > 0
    ? l1ScopeStack[l1ScopeStack.length - 1]
    : null;

  const { depth, dirUp, dirDown, systemLevel } = l1Filter;
  const depthLabel = (d: Depth) => d === 99 ? '∞' : String(d);

  const badgeText = systemLevel
    ? `L1 · ${t('l1.systemLevel')}`
    : `L1 · depth ${depthLabel(depth)}`;

  return (
    <div style={{
      height: 32,
      background: 'var(--bg0)',
      borderBottom: '0.5px solid var(--bd)',
      display: 'flex',
      alignItems: 'center',
      gap: 5,
      padding: '0 10px',
      flexShrink: 0,
      overflow: 'hidden',
    }}>

      {/* ── Scope pill ───────────────────────────────────────────────────── */}
      <div style={{
        display: 'inline-flex',
        alignItems: 'center',
        gap: 4,
        padding: '2px 8px',
        borderRadius: 4,
        border: `0.5px solid ${activeScope ? 'var(--acc)' : 'var(--bd)'}`,
        background: activeScope ? 'rgba(168,184,96,.07)' : 'transparent',
        fontSize: 10,
        color: activeScope ? 'var(--acc)' : 'var(--t2)',
        whiteSpace: 'nowrap',
        flexShrink: 0,
      }}>
        <IconApp />
        <span style={{ maxWidth: 160, overflow: 'hidden', textOverflow: 'ellipsis' }}>
          {activeScope ? activeScope.label : t('l1.allSystems')}
        </span>
        {activeScope && (
          <button
            onClick={clearL1Scope}
            title={t('l1.clearScope')}
            style={{
              display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
              width: 14, height: 14,
              border: 'none', background: 'transparent',
              color: 'var(--t3)', fontSize: 10,
              cursor: 'pointer', padding: 0, lineHeight: 1,
            }}
          >
            ✕
          </button>
        )}
      </div>

      <Divider />

      {/* ── Depth ────────────────────────────────────────────────────────── */}
      <span style={{ fontSize: 10, color: 'var(--t3)', whiteSpace: 'nowrap' }}>
        {t('toolbar.depth')}:
      </span>
      {DEPTHS.map(d => {
        const isOn = d === depth;
        return (
          <button
            key={d}
            onClick={() => setL1Depth(d)}
            style={{
              padding: '2px 6px', borderRadius: 3,
              border: `0.5px solid ${isOn ? 'var(--acc)' : 'var(--bd)'}`,
              fontSize: 9,
              color: isOn ? 'var(--acc)' : 'var(--t3)',
              cursor: 'pointer',
              background: isOn ? 'rgba(168,184,96,.08)' : 'transparent',
              fontWeight: isOn ? 600 : 400,
            }}
          >
            {depthLabel(d)}
          </button>
        );
      })}

      <Divider />

      {/* ── Direction ────────────────────────────────────────────────────── */}
      <button
        onClick={toggleL1DirUp}
        style={{
          padding: '2px 7px', borderRadius: 3,
          border: `0.5px solid ${dirUp ? 'var(--inf)' : 'var(--bd)'}`,
          fontSize: 10,
          color: dirUp ? 'var(--inf)' : 'var(--t3)',
          cursor: 'pointer',
          background: dirUp ? 'rgba(136,184,168,.08)' : 'transparent',
          display: 'flex', alignItems: 'center', gap: 3,
        }}
      >
        ↑ {t('toolbar.upstream')}
      </button>
      <button
        onClick={toggleL1DirDown}
        style={{
          padding: '2px 7px', borderRadius: 3,
          border: `0.5px solid ${dirDown ? 'var(--wrn)' : 'var(--bd)'}`,
          fontSize: 10,
          color: dirDown ? 'var(--wrn)' : 'var(--t3)',
          cursor: 'pointer',
          background: dirDown ? 'rgba(212,146,42,.08)' : 'transparent',
          display: 'flex', alignItems: 'center', gap: 3,
        }}
      >
        ↓ {t('toolbar.downstream')}
      </button>

      <Divider />

      {/* ── System-level toggle ──────────────────────────────────────────── */}
      <button
        onClick={toggleL1SystemLevel}
        title={t('l1.systemLevelHint')}
        style={{
          padding: '2px 8px', borderRadius: 3,
          border: `0.5px solid ${systemLevel ? 'var(--wrn)' : 'var(--bd)'}`,
          fontSize: 9,
          color: systemLevel ? 'var(--wrn)' : 'var(--t3)',
          cursor: 'pointer',
          background: systemLevel ? 'rgba(212,146,42,.07)' : 'transparent',
          display: 'flex', alignItems: 'center', gap: 4,
          whiteSpace: 'nowrap',
        }}
      >
        <IconLayers />
        {t('l1.systemLevel')}
      </button>

      {/* ── Spacer ───────────────────────────────────────────────────────── */}
      <div style={{ flex: '1 1 auto' }} />

      {/* ── Level badge ──────────────────────────────────────────────────── */}
      <span style={{
        fontSize: 9, padding: '2px 7px',
        border: '0.5px solid var(--bd)', borderRadius: 3,
        color: 'var(--t3)', fontFamily: 'var(--mono)',
        whiteSpace: 'nowrap',
      }}>
        {badgeText}
      </span>

    </div>
  );
});

FilterToolbarL1.displayName = 'FilterToolbarL1';
```

### 2.3 Подключение в `Shell.tsx`

```tsx
// src/components/layout/Shell.tsx
// Добавить импорт:
import { FilterToolbarL1 } from './FilterToolbarL1';

// Уже есть FilterToolbar (L2/L3).
// Добавить перед или вместо слота FilterToolbar:
{viewLevel === 'L1'
  ? <FilterToolbarL1 />
  : <FilterToolbar />
}
```

> Сейчас `FilterToolbar` рендерит `null` на L1 — но высота тулбара прыгает.
> После перехода на условный рендер высота 32px (L1) / 40px (L2/L3) — это ожидаемо.
> Если нужна фиксированная высота, унифицировать оба компонента до 36px.

### 2.4 Dim-эффект в `LoomCanvas.tsx`

```typescript
// Добавить импорты:
import { dimNodesOutsideScope, reachableNodeIds } from '../../utils/filterGraph';

// В компоненте после вычисления layoutNodes:
const { l1ScopeStack, l1Filter } = useLoomStore();
const activeScopeId = viewLevel === 'L1' && l1ScopeStack.length > 0
  ? l1ScopeStack[l1ScopeStack.length - 1].nodeId
  : null;

const displayNodes = useMemo(() => {
  if (viewLevel !== 'L1' || !activeScopeId) return layoutNodes;

  // BFS по edges
  const inScope = reachableNodeIds(rawEdges, activeScopeId);

  // RF group-nodes связаны через parentId, не через edges — добавить вручную
  for (const n of layoutNodes) {
    if (n.parentId && inScope.has(n.parentId)) inScope.add(n.id);
  }

  return dimNodesOutsideScope(layoutNodes, inScope);
}, [layoutNodes, activeScopeId, rawEdges, viewLevel]);

// Передавать displayNodes в <ReactFlow nodes={displayNodes} ...>
```

### 2.5 Fit view при смене scope (опционально, но рекомендуется)

```typescript
// В LoomCanvas.tsx:
const { fitView } = useReactFlow();

useEffect(() => {
  if (!activeScopeId) return;
  const t = setTimeout(() => fitView({ padding: 0.15, duration: 300 }), 60);
  return () => clearTimeout(t);
}, [activeScopeId, fitView]);
```

---

## i18n — новые ключи

Добавить в блок `"l1"` в обоих файлах:

**`en/common.json`:**
```json
"l1": {
  "schemas":         "schemas",
  "dbUnit":          "DB",
  "appBadge":        "App",
  "allSystems":      "all systems",
  "clearScope":      "Clear scope",
  "systemLevel":     "System-level",
  "systemLevelHint": "Show applications only, hide databases"
}
```

**`ru/common.json`:**
```json
"l1": {
  "schemas":         "схемы",
  "dbUnit":          "СУБД",
  "appBadge":        "App",
  "allSystems":      "все системы",
  "clearScope":      "Сбросить скоуп",
  "systemLevel":     "Системный вид",
  "systemLevelHint": "Показать только приложения, скрыть СУБД"
}
```

---

## Отличие от LOOM-023b (L2/L3 `FilterToolbar`)

| Параметр | LOOM-023b (L2/L3) | LOOM-024b (L1) |
|----------|------------------|----------------|
| Высота | 40px | 32px |
| Файл | `FilterToolbar.tsx` | `FilterToolbarL1.tsx` |
| Start object | Рутина / таблица | Приложение (из `l1ScopeStack`) |
| Фильтр поля | ✅ column name select | ❌ не применимо |
| Table-level view | ✅ скрыть column edges | — |
| System-level view | — | ✅ скрыть DB-ноды (`l1Filter.systemLevel`) |
| Глубина | 1/2/3/5/∞ | 1/2/3/∞ |
| Direction | ↑↓ | ↑↓ |
| Store (scope) | `filter.startObjectId` | `l1ScopeStack` (уже есть) |
| Store (params) | `filter.depth` etc. | `l1Filter.depth` etc. (новое) |

---

## Что НЕ менять

| Файл | Причина |
|------|---------|
| `loomStore.ts` → `l1ScopeStack` и его actions | Полностью реализовано |
| `filterGraph.ts` | Утилиты готовы — только импортировать |
| `ApplicationNode.tsx` | `double-click → pushL1Scope` уже подключён |
| `FilterToolbar.tsx` → L2/L3 логика | Не трогать ветку L2/L3 |

---

## Acceptance Criteria

```gherkin
Scenario: L1 canvas — нет активного scope

  Given: viewLevel === 'L1', l1ScopeStack.length === 0
  Then:
    - FilterToolbarL1 рендерит 32px контейнер
    - Pill показывает "все системы" (t('l1.allSystems'))
    - Кнопка × не отображается
    - Все ноды на canvas — полная яркость (нет dim)

Scenario: Double-click на ApplicationNode

  Given: L1 canvas с несколькими App-нодами (OrderService, ReportingApp, …)
  When:  пользователь double-click на "OrderService"
  Then:
    - Pill меняется на "[⊞ OrderService ×]" с border var(--acc)
    - DatabaseNode и L1SchemaNode "OrderService" — полная яркость
    - Остальные App + их дочерние ноды — opacity 0.2

Scenario: Сброс scope через ×

  Given: Активный scope "OrderService"
  When:  пользователь нажимает ✕ в pill
  Then:
    - Pill → "все системы", border var(--bd)
    - Все ноды возвращаются к полной яркости
    - l1ScopeStack.length === 0

Scenario: System-level view toggle

  Given: L1 canvas
  When:  пользователь нажимает "Системный вид"
  Then:
    - Кнопка подсвечивается var(--wrn)
    - DatabaseNode и L1SchemaNode скрыты / hidden
    - ApplicationNode остаются видны
    - Level badge: "L1 · Системный вид"

Scenario: i18n

  Given: язык переключён на RU
  Then:
    - "all systems" → "все системы"
    - "System-level" → "Системный вид"
    - Кнопки ↑↓ используют t('toolbar.upstream') / t('toolbar.downstream')
```

---

## Файлы для изменения

| Файл | Действие |
|------|----------|
| `src/stores/loomStore.ts` | Добавить `l1Filter` + 4 action'а (только параметры, не scope) |
| `src/components/layout/FilterToolbarL1.tsx` | Новый файл |
| `src/components/layout/Shell.tsx` | `{viewLevel === 'L1' ? <FilterToolbarL1 /> : <FilterToolbar />}` |
| `src/components/canvas/LoomCanvas.tsx` | Читать `l1ScopeStack`, применять `dimNodesOutsideScope` |
| `src/i18n/locales/en/common.json` | Добавить `l1.allSystems`, `l1.clearScope`, `l1.systemLevel`, `l1.systemLevelHint` |
| `src/i18n/locales/ru/common.json` | То же на русском |

**Опционально:**
| Файл | Действие |
|------|----------|
| `src/components/layout/proto/FilterToolbarL1Proto.tsx` | Прото (рекомендуется для согласования до IMPL) |
