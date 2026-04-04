# SEER Studio — i18n Dictionary Phase 3
# Файл: `src/i18n/locales/{lang}/common.json` — добавить к существующим ключам

## Существующие ключи (Phase 1–2, для справки)

```json
{
  "app":        { "title": "SEER Studio", "tagline": "Data Lineage Explorer" },
  "nav":        { "loom": "LOOM", "anvil": "ANVIL", "shuttle": "SHUTTLE", "knot": "KNOT" },
  "auth":       { "login": "Sign In", "logout": "Log Out", "username": "Username",
                  "password": "Password", "signingIn": "Signing in…",
                  "error": { "invalid": "...", "required": "..." } },
  "canvas":     { "nodes": "nodes", "edges": "edges", "level": "Level",
                  "zoom": "Zoom", "computingLayout": "computing layout…" },
  "nodes":      { "tables": "tables", "routines": "routines",
                  "columns": "columns", "moreColumns": "+{{count}} more" },
  "breadcrumb": { "overview": "Overview" },
  "theme":      { "dark": "Dark", "light": "Light" },
  "language":   { "en": "English", "ru": "Русский" }
}
```

---

## Новые ключи Phase 3 — полный словарь

### Структура добавляемых секций

```
toolbar.*          — Filter Toolbar (LOOM-023b)
search.*           — Search Panel (LOOM-032)
nodes.*            — расширение существующей секции
nodeTypes.*        — подписи типов нод (LOOM-024, 029, 031)
mapping.*          — Column Mapping (LOOM-025)
tableNode.*        — TableNode состояния (LOOM-026)
expand.*           — Expand buttons (LOOM-027)
statement.*        — StatementNode / GroupNode (LOOM-028)
subquery.*         — SubQuery типы (LOOM-029)
impact.*           — Impact Analysis (LOOM-030)
knot.*             — KNOT Inspector (LOOM-030)
atom.*             — AtomNode операции (LOOM-031)
roles.*            — Role-based UI (LOOM-033)
status.*           — расширение (уже частично есть)
actions.*          — общие кнопки действий
```

---

## EN · `src/i18n/locales/en/common.json`

```json
{
  "toolbar": {
    "startObject":       "Start object",
    "changeObject":      "Change",
    "field":             "Field",
    "allColumns":        "all columns",
    "depth":             "Depth",
    "upstream":          "Upstream",
    "downstream":        "Downstream",
    "tableLevelView":    "Table-level view",
    "columnLevelView":   "Column-level view",
    "clearFilter":       "Clear filter",
    "depthSteps":        "{{n}} steps",
    "depthInfinity":     "∞ steps"
  },

  "search": {
    "placeholder":       "tables, columns, routines…",
    "noResults":         "Nothing found",
    "resultCount":       "{{count}} results",
    "filters": {
      "all":             "All",
      "tables":          "Tables",
      "routines":        "Routines",
      "columns":         "Columns",
      "statements":      "Statements"
    },
    "sections": {
      "sources":         "Sources",
      "transformation":  "Transformation",
      "target":          "Target table",
      "statements":      "Root statements",
      "hidden":          "Hidden nodes"
    },
    "hidden": {
      "label":           "hidden",
      "restore":         "Restore"
    },
    "openLevel":         "Open on {{level}}",
    "notOnCanvas":       "Located on {{level}} · {{scope}}",
    "goToLevel":         "Go there →"
  },

  "nodes": {
    "tables":            "tables",
    "routines":          "routines",
    "columns":           "columns",
    "moreColumns":       "+{{count}} more",
    "statements":        "statements",
    "services":          "services",
    "databases":         "databases",
    "nestedStmts":       "{{count}} nested",
    "outputColumns":     "{{count}} output columns",
    "filterColumns":     "filter columns…",
    "sourceTable":       "source",
    "targetTable":       "target",
    "depth":             "depth {{n}}"
  },

  "nodeTypes": {
    "DaliApplication":   "Application",
    "DaliService":       "Service",
    "DaliDatabase":      "Database",
    "DaliSchema":        "Schema",
    "DaliPackage":       "Package",
    "DaliTable":         "Table",
    "DaliColumn":        "Column",
    "DaliRoutine":       "Routine",
    "DaliStatement":     "Statement",
    "DaliAtom":          "Atom",
    "DaliOutputColumn":  "Output column",
    "DaliJoin":          "Join",
    "DaliParameter":     "Parameter",
    "DaliVariable":      "Variable",
    "DaliSession":       "Session"
  },

  "mapping": {
    "title":             "Column mapping",
    "allMappings":       "All {{count}} mappings",
    "types": {
      "passthrough":     "passthrough",
      "computed":        "computed",
      "subquery":        "subquery",
      "literal":         "literal",
      "caseWhen":        "CASE WHEN",
      "window":          "window function",
      "null":            "null"
    },
    "nullable":          "nullable",
    "expression":        "Expression",
    "drilldownHint":     "+{{count}} more — drill-down L3"
  },

  "tableNode": {
    "collapse":          "Collapse",
    "expand":            "Expand",
    "hide":              "Hide from canvas",
    "moreColumns":       "+{{count}} more columns",
    "allMapped":         "all mapped 1:1",
    "filterPlaceholder": "filter columns…",
    "pkLabel":           "PK",
    "fkLabel":           "FK"
  },

  "expand": {
    "upstream":          "Upstream",
    "downstream":        "Downstream",
    "loading":           "Loading…",
    "partialCount":      "+{{shown}}/{{total}}",
    "allLoaded":         "All loaded",
    "collapseUpstream":  "Collapse upstream",
    "collapseDownstream":"Collapse downstream",
    "noNeighbors":       "No neighbors"
  },

  "statement": {
    "types": {
      "query":           "Query",
      "insert":          "Insert",
      "update":          "Update",
      "delete":          "Delete",
      "cursor":          "Cursor",
      "refcursor":       "RefCursor",
      "merge":           "Merge",
      "call":            "Call"
    },
    "rootStatement":     "root statement",
    "nestedStatements":  "{{count}} nested",
    "reads":             "Reads",
    "writes":            "Writes",
    "expandStatements":  "Expand statements",
    "collapseGroup":     "Collapse",
    "groupLabel":        "{{name}} — statement scope"
  },

  "subquery": {
    "types": {
      "from":            "FROM",
      "where":           "WHERE",
      "select":          "SELECT",
      "exists":          "EXISTS",
      "scalar":          "scalar"
    },
    "badge":             "{{type}} ⊂",
    "levels":            "{{count}} levels",
    "tables":            "{{count}} tables",
    "drilldown":         "Drill-down into subquery →",
    "cte":               "CTE"
  },

  "impact": {
    "title":             "Impact analysis",
    "upstream":          "Upstream ({{count}})",
    "downstream":        "Downstream ({{count}})",
    "traceUpstream":     "↑ Trace upstream",
    "traceDownstream":   "↓ Impact downstream",
    "clearHighlight":    "Clear highlight",
    "depth":             "Depth",
    "depthWarning":      "More than {{limit}} nodes — performance may degrade",
    "affected": {
      "tables":          "{{count}} tables affected",
      "columns":         "{{count}} columns affected",
      "routines":        "{{count}} routines affected",
      "statements":      "{{count}} statements affected"
    },
    "depthLabel":        "depth {{n}}"
  },

  "knot": {
    "title":             "KNOT Inspector",
    "empty":             "Click any node to inspect",
    "sections": {
      "node":            "Node",
      "field":           "Field lineage",
      "impact":          "Impact",
      "upstream":        "Upstream sources",
      "downstream":      "Downstream consumers",
      "stats":           "Stats",
      "mappings":        "Mappings",
      "subqueryChain":   "Subquery chain",
      "actions":         "Actions"
    },
    "fields": {
      "type":            "Type",
      "schema":          "Schema",
      "columns":         "Columns",
      "level":           "Level",
      "language":        "Language",
      "expression":      "Expression",
      "dataType":        "Data type",
      "mappingType":     "Mapping",
      "readsFrom":       "Reads from",
      "writtenBy":       "Written by",
      "usedIn":          "Used in",
      "subqueries":      "Subqueries",
      "currentView":     "Current view"
    }
  },

  "atom": {
    "operations": {
      "SUM":             "SUM",
      "AVG":             "AVG",
      "COUNT":           "COUNT",
      "MIN":             "MIN",
      "MAX":             "MAX",
      "RANK":            "RANK",
      "ROW_NUMBER":      "ROW_NUMBER",
      "DENSE_RANK":      "DENSE_RANK",
      "FILTER":          "FILTER",
      "JOIN":            "JOIN",
      "EXTRACT":         "EXTRACT",
      "CASE":            "CASE WHEN",
      "COALESCE":        "COALESCE",
      "CAST":            "CAST",
      "GROUP_BY":        "GROUP BY",
      "ORDER_BY":        "ORDER BY",
      "UNION_ALL":       "UNION ALL",
      "UNION":           "UNION",
      "PASSTHROUGH":     "passthrough",
      "CTE_REF":         "CTE ref"
    },
    "partitionBy":       "PARTITION BY {{cols}}",
    "orderBy":           "ORDER BY {{cols}}"
  },

  "roles": {
    "viewer":            "viewer",
    "editor":            "editor",
    "admin":             "admin",
    "readOnly":          "read-only",
    "readWrite":         "read-write"
  },

  "actions": {
    "drilldownL3":       "Drill-down L3",
    "fullChain":         "Full lineage chain",
    "impactAnalysis":    "Impact analysis",
    "showDiff":          "Show diff",
    "fitView":           "Fit to view",
    "exportPng":         "Export PNG",
    "copyId":            "Copy ID",
    "openInSource":      "Open in source",
    "setAsStart":        "Set as start object"
  },

  "status": {
    "loading":           "Loading…",
    "error":             "Failed to load data",
    "empty":             "No nodes found",
    "unauthorized":      "Session expired. Please sign in again.",
    "forbidden":         "Access denied",
    "graphLoading":      "Building graph…",
    "noData":            "No lineage data for this object"
  }
}
```

---

## RU · `src/i18n/locales/ru/common.json`

```json
{
  "toolbar": {
    "startObject":       "Начальный объект",
    "changeObject":      "Изменить",
    "field":             "Поле",
    "allColumns":        "все колонки",
    "depth":             "Глубина",
    "upstream":          "Upstream",
    "downstream":        "Downstream",
    "tableLevelView":    "На уровне таблиц",
    "columnLevelView":   "На уровне колонок",
    "clearFilter":       "Сбросить",
    "depthSteps":        "{{n}} шагов",
    "depthInfinity":     "∞ шагов"
  },

  "search": {
    "placeholder":       "таблицы, колонки, процедуры…",
    "noResults":         "Ничего не найдено",
    "resultCount":       "{{count}} результатов",
    "filters": {
      "all":             "Все",
      "tables":          "Таблицы",
      "routines":        "Процедуры",
      "columns":         "Колонки",
      "statements":      "Стейтменты"
    },
    "sections": {
      "sources":         "Источники",
      "transformation":  "Трансформация",
      "target":          "Целевая таблица",
      "statements":      "Корневые стейтменты",
      "hidden":          "Скрытые ноды"
    },
    "hidden": {
      "label":           "скрыт",
      "restore":         "Восстановить"
    },
    "openLevel":         "Открыть на {{level}}",
    "notOnCanvas":       "Находится на {{level}} · {{scope}}",
    "goToLevel":         "Перейти →"
  },

  "nodes": {
    "tables":            "таблиц",
    "routines":          "процедур",
    "columns":           "колонок",
    "moreColumns":       "+{{count}} ещё",
    "statements":        "стейтментов",
    "services":          "сервисов",
    "databases":         "баз данных",
    "nestedStmts":       "{{count}} вложенных",
    "outputColumns":     "{{count}} выходных колонок",
    "filterColumns":     "фильтр колонок…",
    "sourceTable":       "источник",
    "targetTable":       "цель",
    "depth":             "глубина {{n}}"
  },

  "nodeTypes": {
    "DaliApplication":   "Приложение",
    "DaliService":       "Сервис",
    "DaliDatabase":      "База данных",
    "DaliSchema":        "Схема",
    "DaliPackage":       "Пакет",
    "DaliTable":         "Таблица",
    "DaliColumn":        "Колонка",
    "DaliRoutine":       "Процедура",
    "DaliStatement":     "Стейтмент",
    "DaliAtom":          "Атом",
    "DaliOutputColumn":  "Выходная колонка",
    "DaliJoin":          "JOIN",
    "DaliParameter":     "Параметр",
    "DaliVariable":      "Переменная",
    "DaliSession":       "Сессия"
  },

  "mapping": {
    "title":             "Маппинг колонок",
    "allMappings":       "Все {{count}} маппингов",
    "types": {
      "passthrough":     "напрямую",
      "computed":        "вычисляемое",
      "subquery":        "подзапрос",
      "literal":         "литерал",
      "caseWhen":        "CASE WHEN",
      "window":          "оконная функция",
      "null":            "null"
    },
    "nullable":          "nullable",
    "expression":        "Выражение",
    "drilldownHint":     "+{{count}} ещё — drill-down L3"
  },

  "tableNode": {
    "collapse":          "Свернуть",
    "expand":            "Развернуть",
    "hide":              "Убрать с canvas",
    "moreColumns":       "+{{count}} колонок",
    "allMapped":         "все mapped 1:1",
    "filterPlaceholder": "фильтр колонок…",
    "pkLabel":           "PK",
    "fkLabel":           "FK"
  },

  "expand": {
    "upstream":          "Upstream",
    "downstream":        "Downstream",
    "loading":           "Загрузка…",
    "partialCount":      "+{{shown}}/{{total}}",
    "allLoaded":         "Всё загружено",
    "collapseUpstream":  "Свернуть upstream",
    "collapseDownstream":"Свернуть downstream",
    "noNeighbors":       "Нет связанных объектов"
  },

  "statement": {
    "types": {
      "query":           "Query",
      "insert":          "Insert",
      "update":          "Update",
      "delete":          "Delete",
      "cursor":          "Cursor",
      "refcursor":       "RefCursor",
      "merge":           "Merge",
      "call":            "Call"
    },
    "rootStatement":     "корневой стейтмент",
    "nestedStatements":  "{{count}} вложенных",
    "reads":             "Читает",
    "writes":            "Пишет",
    "expandStatements":  "Раскрыть стейтменты",
    "collapseGroup":     "Свернуть",
    "groupLabel":        "{{name}} — scope стейтмента"
  },

  "subquery": {
    "types": {
      "from":            "FROM",
      "where":           "WHERE",
      "select":          "SELECT",
      "exists":          "EXISTS",
      "scalar":          "scalar"
    },
    "badge":             "{{type}} ⊂",
    "levels":            "{{count}} уровней",
    "tables":            "{{count}} таблиц",
    "drilldown":         "Drill-down в подзапрос →",
    "cte":               "CTE"
  },

  "impact": {
    "title":             "Анализ влияния",
    "upstream":          "Upstream ({{count}})",
    "downstream":        "Downstream ({{count}})",
    "traceUpstream":     "↑ Откуда данные",
    "traceDownstream":   "↓ Что затронет изменение",
    "clearHighlight":    "Сбросить подсветку",
    "depth":             "Глубина",
    "depthWarning":      "Больше {{limit}} нод — возможны тормоза",
    "affected": {
      "tables":          "{{count}} таблиц затронуто",
      "columns":         "{{count}} колонок затронуто",
      "routines":        "{{count}} процедур затронуто",
      "statements":      "{{count}} стейтментов затронуто"
    },
    "depthLabel":        "глубина {{n}}"
  },

  "knot": {
    "title":             "KNOT Inspector",
    "empty":             "Кликните на ноду для просмотра",
    "sections": {
      "node":            "Нода",
      "field":           "Lineage поля",
      "impact":          "Влияние",
      "upstream":        "Источники",
      "downstream":      "Потребители",
      "stats":           "Статистика",
      "mappings":        "Маппинги",
      "subqueryChain":   "Цепочка подзапросов",
      "actions":         "Действия"
    },
    "fields": {
      "type":            "Тип",
      "schema":          "Схема",
      "columns":         "Колонок",
      "level":           "Уровень",
      "language":        "Язык",
      "expression":      "Выражение",
      "dataType":        "Тип данных",
      "mappingType":     "Маппинг",
      "readsFrom":       "Читает из",
      "writtenBy":       "Пишется",
      "usedIn":          "Используется в",
      "subqueries":      "Подзапросов",
      "currentView":     "Текущий вид"
    }
  },

  "atom": {
    "operations": {
      "SUM":             "SUM",
      "AVG":             "AVG",
      "COUNT":           "COUNT",
      "MIN":             "MIN",
      "MAX":             "MAX",
      "RANK":            "RANK",
      "ROW_NUMBER":      "ROW_NUMBER",
      "DENSE_RANK":      "DENSE_RANK",
      "FILTER":          "FILTER",
      "JOIN":            "JOIN",
      "EXTRACT":         "EXTRACT",
      "CASE":            "CASE WHEN",
      "COALESCE":        "COALESCE",
      "CAST":            "CAST",
      "GROUP_BY":        "GROUP BY",
      "ORDER_BY":        "ORDER BY",
      "UNION_ALL":       "UNION ALL",
      "UNION":           "UNION",
      "PASSTHROUGH":     "сквозной",
      "CTE_REF":         "CTE ссылка"
    },
    "partitionBy":       "PARTITION BY {{cols}}",
    "orderBy":           "ORDER BY {{cols}}"
  },

  "roles": {
    "viewer":            "наблюдатель",
    "editor":            "редактор",
    "admin":             "администратор",
    "readOnly":          "только чтение",
    "readWrite":         "чтение и запись"
  },

  "actions": {
    "drilldownL3":       "Drill-down L3",
    "fullChain":         "Полная цепочка lineage",
    "impactAnalysis":    "Анализ влияния",
    "showDiff":          "Показать diff",
    "fitView":           "По размеру экрана",
    "exportPng":         "Экспорт PNG",
    "copyId":            "Копировать ID",
    "openInSource":      "Открыть в источнике",
    "setAsStart":        "Сделать начальным объектом"
  },

  "status": {
    "loading":           "Загрузка…",
    "error":             "Не удалось загрузить данные",
    "empty":             "Нет нод для отображения",
    "unauthorized":      "Сессия истекла. Войдите снова.",
    "forbidden":         "Доступ запрещён",
    "graphLoading":      "Строится граф…",
    "noData":            "Нет данных lineage для этого объекта"
  }
}
```

---

## Правила локализации для Phase 3

### Что ДОЛЖНО локализоваться
- Все видимые надписи в UI: лейблы, кнопки, placeholder, секции, tooltip
- Типы нод (`nodeTypes.*`) — в KNOT Inspector и левой панели
- Типы маппинга (`mapping.types.*`) — в KNOT Inspector
- Типы стейтментов (`statement.types.*`) — в StatementNode header
- Типы подзапросов (`subquery.types.*`) — в badge ⊂
- Имена операций атомов (`atom.operations.*`) — в AtomNode
- Роли (`roles.*`) — в header badge и statusbar
- Статусы (`status.*`) — в loading/error состояниях

### Что НЕ локализуется
- Имена таблиц, колонок, схем, рутин — это данные из БД
- SQL-выражения и типы данных (uuid, varchar, numeric, ...) — технические термины
- Имена операций SQL (SUM, RANK, GROUP BY) — одинаковы на всех языках
- Технические идентификаторы (@rid, @type, ...)
- Аббревиатуры LOOM / ANVIL / SHUTTLE / KNOT — бренд

### Правило interpolation
Все динамические значения через `{{variable}}`:
```tsx
// ✅ правильно
t('nodes.moreColumns', { count: 13 })      // "+13 more"
t('impact.upstream', { count: 5 })          // "Upstream (5)"
t('expand.partialCount', { shown: 20, total: 47 }) // "+20/47"

// ❌ неправильно
`+${count} more`  // хардкод
```

### Требование к задачам Phase 3
Каждый новый компонент — прототип и реализация — обязан:
1. Использовать `useTranslation()` и `t()` для всех строк
2. Не содержать хардкод строк на любом языке (ESLint rule: `i18next/no-literal-string` — опционально)
3. Проверяться в обоих языках перед закрытием задачи
4. Добавлять новые ключи в **оба** файла локалей одновременно

### Ключи-исключения (допустимый хардкод)
```tsx
// Иконки Lucide — не текст
<Table2 size={12} />

// CSS-переменные — не текст
style={{ color: 'var(--acc)' }}

// Технические значения из API — не текст
<span>{node.data.dataType}</span>  // "uuid", "varchar"

// Имена из данных — не текст
<span>{table.name}</span>  // "orders", "order_items"
```
