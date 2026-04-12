# VERDANDI LOOM — План на неделю 07–11 апреля 2026

**Создан:** 05.04.2026 (автоматически, по результатам code review)
**Обновлён:** 06.04.2026 — итоги воскресенья
**Контекст:** Phase 3 завершён на ~83% (10/12 задач). PR#2 (NooriUta/Verdandi#2) открыт — L2 FilterToolbar v2, dimming/fitView fix, column enrichment, CURSOR badge. Главные риски: SQL injection, error handling.

## Что сделано за 05–06 апреля (до начала рабочей недели)

| Задача | Статус |
|--------|--------|
| L2 FilterToolbar: Table/Stmt cascade dropdowns | ✅ |
| fitView при выборе таблицы/трансформации | ✅ (double-rAF, no ELK re-trigger) |
| CF edge toggle (showCfEdges) в FilterState + FilterToolbar | ✅ |
| Column cascade из выбранной ноды | ✅ |
| tableLevelView fix (strips column data, not just edges) | ✅ |
| Column enrichment: HAS_COLUMN + HAS_OUTPUT_COL + HAS_AFFECTED_COL unified query | ✅ |
| CURSOR/DINAMIC_CURSOR badge colours | ✅ |
| i18n: table/allTables/stmt/allStmts/showCfEdges/hideCfEdges в en+ru | ✅ |
| PR#2 открыт и pushed | ✅ |

---

## Приоритеты недели

### Блок 1: Стабилизация (обязательно)

> Перед добавлением новых фич — закрыть критические технические долги.

| # | Задача | Оценка | Приоритет | Файлы |
|---|--------|--------|-----------|-------|
| S-01 | **Error handling на бэкенде** — добавить `.onFailure()` во все Uni-цепочки, логирование ошибок, meaningful HTTP-ответы вместо stack traces | 2–3 ч | 🔴 | ExploreService, LineageService, SearchService, OverviewService |
| S-02 | **Параметризация SQL в SearchService** — заменить `String.format()` на параметризованные запросы ArcadeDB | 1–2 ч | 🔴 | SearchService.java |
| S-03 | **Error handling на фронте** — добавить error states в LoomCanvas (пустой граф, ошибка сети, ошибка layout), loading/error UI | 2–3 ч | 🔴 | LoomCanvas.tsx, hooks.ts |
| S-04 | **Удалить console.log()** из loomStore.ts (5 мест) | 15 мин | 🟡 | loomStore.ts |
| S-05 | **Защита от спама expand** — debounce requestExpand(), блокировка повторных запросов к одной ноде | 1 ч | 🟡 | loomStore.ts, NodeExpandButtons.tsx |

**Итого блок 1:** ~7–9 ч

---

### Блок 2: Новые фичи Phase 3

| # | Задача | Оценка | Зависимости | Описание |
|---|--------|--------|-------------|----------|
| F-01 | **LOOM-028: KNOT Inspector** — правая панель с деталями выбранной ноды | 4–6 ч | LOOM-026 | Показывает metadata, columns (для таблиц), SQL (для statements), dependencies (upstream/downstream count). Три секции: Properties, Columns/Fields, Relations. |
| F-02 | **LOOM-029: Context menu** — right-click меню на нодах | 2–3 ч | LOOM-028 | Действия: Open in KNOT, Expand upstream/downstream, Hide, Focus, Copy ID. Контекстно-зависимые пункты по типу ноды. |
| F-03 | **LOOM-030: Export PNG** — кнопка экспорта canvas | 2–3 ч | — | Использовать `@xyflow/react` toImage() API. Кнопка в Header или StatusBar. |

**Итого блок 2:** ~8–12 ч

---

### Блок 3: Рефакторинг (если останется время)

| # | Задача | Оценка | Описание |
|---|--------|--------|----------|
| R-01 | **Рефакторинг LoomCanvas** — извлечь useGraphTransform, useL1Layout, useExpansionMerge hooks | 3–4 ч | Уменьшить 663 LOC → ~200 LOC + 3 hook-файла |
| R-02 | **Рефакторинг transformGraph.ts** — split на 3 файла | 2–3 ч | transformExplore.ts, transformOverview.ts, transformHelpers.ts |
| R-03 | **Unit-тесты transformGraph** — Jest/Vitest, mock-данные | 4–6 ч | Покрыть: transformGqlOverview, transformGqlExplore, extractStatementType, parseStmtLabel |
| R-04 | **Удалить proto-файл** L1NodesProto.tsx (618 LOC) | 15 мин | Прототип больше не нужен |

**Итого блок 3:** ~10–14 ч

---

## Рекомендуемый порядок

```
Понедельник:   S-01 (BE error handling) + S-02 (SQL injection fix)
Вторник:       S-03 (FE error handling) + S-04 + S-05
Среда:         F-01 (KNOT Inspector)
Четверг:       F-01 (завершение) + F-02 (Context menu)
Пятница:       F-03 (Export) + R-04 + R-01 (если время)
```

---

## Блокеры и зависимости

| Блокер | Влияет на | Статус |
|--------|----------|--------|
| HOUND-DB-001 (schema alignment) | Полнота L1 данных — ad-hoc DaliDatabase невидимы | ⬜ Ожидает работы в Hound |
| ArcadeDB Cypher UNION баг | Качество данных в ExploreService | 🔄 Обходится parallel queries |
| ColumnInfo.type не заполняется | Отображение типов колонок в TableNode | ⬜ Требует изменений в Hound/SHUTTLE |

---

## Метрики на конец недели (цель)

| Метрика | Сейчас | Цель |
|---------|--------|------|
| Phase 3 задачи | 8/12 (67%) | 11/12 (92%) |
| Критические баги | 3 (SQL injection, error handling FE+BE) | 0 |
| Тест-покрытие | 0% | ≥ 20% (transformGraph) |
| LOC LoomCanvas | 663 | ≤ 300 (если R-01) |

---

## Не входит в план недели

- LOOM-031 (Performance / virtualization) — нет данных о проблемах на реальных объёмах
- Docker Compose — не блокирует разработку
- Refresh token в Chur — не критично для MVP
- Accessibility (ARIA) — средний приоритет, позже
- HOUND-DB-001 — отдельный проект (Hound), не VERDANDI
