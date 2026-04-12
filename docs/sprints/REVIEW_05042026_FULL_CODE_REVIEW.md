# VERDANDI — Полное ревью кодовой базы

**Дата:** 05.04.2026
**Автор:** Claude Code (автоматическое ревью)
**Коммит:** f8e31b8 (master)
**Период:** от init (b918f6e) до текущего состояния

---

## 1. Статус проекта

### Завершённые фазы

| Фаза | Статус | Задачи | Дата |
|------|--------|--------|------|
| Phase 1 — Skeleton + Mock Data | ✅ Завершён | LOOM-001 – LOOM-011 (11/11) | 03.04.2026 |
| Phase 1.5 — Auth + i18n | ✅ Завершён | LOOM-012 – LOOM-014 (3/3) | 04.04.2026 |
| Phase 2 — Quarkus + RBAC | ✅ Завершён | LOOM-015 – LOOM-022 (все) | 04.04.2026 |

### Phase 3 — Core Features (текущий этап)

| Задача | Статус | Коммит |
|--------|--------|--------|
| LOOM-023: Canvas read-only mode | ✅ | b31a52c |
| LOOM-023b: Filter Toolbar L2/L3 | ✅ | b12372b |
| LOOM-024: L1 ApplicationNode + 3-level canvas | ✅ | 3a76300 |
| LOOM-024b: FilterToolbarL1 (hierarchy filters) | ✅ | f456587 |
| LOOM-025: Column Mapping L2 + jumpTo | ✅ | a7f1fb5, f456587 |
| LOOM-026: TableNode три состояния | ✅ | b12372b |
| LOOM-027: Expand buttons upstream/downstream | ✅ | f8e31b8 |
| LOOM-028: KNOT Inspector (right panel) | ⬜ TODO | — |
| LOOM-029: Context menu (right-click) | ⬜ TODO | — |
| LOOM-030: Export PNG/SVG | ⬜ TODO | — |
| LOOM-031: Performance (virtualization) | ⬜ TODO | — |
| LOOM-032: SearchPanel | ✅ | b12372b, 19932d9 |
| HOUND-DB-001: Schema alignment | ⬜ TODO (Hound) | — |

**Итого Phase 3:** 8/12 задач завершено (67%). Остались KNOT, Context menu, Export, Performance.

---

## 2. Архитектура системы

### Общая схема

```
Браузер (LOOM SPA — React 19 + Vite 8)
    │  POST /auth/login, /auth/me, /auth/logout
    │  POST /graphql
    ▼
Chur (порт 3000) — Auth gateway (Fastify 4 + Node.js)
    │  JWT cookie → X-Seer-Role / X-Seer-User headers
    ├──→ POST /graphql  →  SHUTTLE (порт 8080)
    └──→ SQL/Cypher     →  ArcadeDB (порт 2480)
                              ▲
SHUTTLE (Quarkus 3.34.2)  ────┘
    │  SmallRye GraphQL API
    │  7 queries: overview, explore, lineage,
    │  upstream, downstream, search, me
    ▼
ArcadeDB (Hound database)
    - DaliSchema, DaliTable, DaliColumn, DaliRoutine,
      DaliStatement, DaliPackage, DaliDatabase, etc.
```

### Кодовая база — размеры

| Модуль | Файлов | LOC | Язык |
|--------|--------|-----|------|
| verdandi (frontend) | 45 | ~8,800 | TypeScript + React |
| SHUTTLE (backend) | 15 | ~1,110 | Java 21 + Quarkus |
| Chur (auth gateway) | 11 | ~600 | TypeScript + Fastify |
| **ИТОГО** | **71** | **~10,500** | — |

---

## 3. Ревью фронтенда (verdandi)

### 3.1 Сильные стороны

1. **Чёткая архитектура трёх уровней** (L1 → L2 → L3) с правильным разделением запросов и трансформаций
2. **Zustand store** хорошо организован: 11 категорий состояния с понятной семантикой действий
3. **React Query** (TanStack) правильно используется с auto-logout на 401
4. **Полная типизация** — TypeScript строгий, доменные типы покрывают все сущности Dali
5. **i18n** реализован (EN/RU) для всех UI-строк
6. **Дизайн-система** — CSS-переменные, 5 палитр, тёмная/светлая тема
7. **ELK.js layout** — автоматическое размещение нод с поддержкой compound groups

### 3.2 Критические проблемы

#### P1 — LoomCanvas.tsx (663 LOC) — слишком большой компонент

**Файл:** `verdandi/src/components/canvas/LoomCanvas.tsx`

Компонент совмещает 6 обязанностей:
- React Flow setup + state management
- Оркестрация трёх уровней визуализации
- Слияние expansion-данных (LOOM-027)
- 6-фазный pipeline трансформации и фильтрации графа
- L1 DB expansion auto-trigger
- Управление viewport (fitView)

**Рекомендация:** Извлечь custom hooks:
- `useGraphTransform()` — pipeline трансформации
- `useL1Layout()` — L1-специфичная логика
- `useExpansionMerge()` — слияние upstream/downstream данных

#### P2 — transformGraph.ts (789 LOC) — монолитный трансформер

**Файл:** `verdandi/src/utils/transformGraph.ts`

Содержит 4 отдельных трансформера и 2 L1-билдера в одном файле. Сложно навигировать и поддерживать.

**Рекомендация:** Разделить на:
- `transformExplore.ts` — L2/L3 трансформации
- `transformOverview.ts` — L1 билдеры (Real + Synthetic)
- `transformHelpers.ts` — общие утилиты (getEdgeStyle, parseStmtLabel и т.д.)

#### P3 — Отсутствие обработки ошибок

- **services/lineage.ts**: нет try-catch в fetch-функциях
- **transformGraph.ts**: parseStmtLabel() и extractStatementType() падают молча на невалидных данных
- **LoomCanvas.tsx**: нет UI-состояния для ошибок ELK layout
- **Нет UI для пустых результатов** (explore/lineage возвращает 0 нод)

### 3.3 Высокий приоритет

| # | Проблема | Файл | Рекомендация |
|---|----------|------|-------------|
| H1 | console.log() в продакшн-коде | loomStore.ts (5 мест) | Удалить или заменить на logging service |
| H2 | Нет loading state для expansion queries | LoomCanvas.tsx | Добавить spinner при requestExpand() |
| H3 | Нет защиты от спама expand-кнопок | loomStore.ts | Добавить debounce или блокировку повторных запросов |
| H4 | Нет retry-логики в React Query hooks | hooks.ts | Добавить `retry: 2` для сетевых ошибок |
| H5 | FilterToolbar.tsx + FilterToolbarL1.tsx дублируют UI | layout/ | Извлечь общие компоненты (toggle-кнопки, select) |

### 3.4 Средний приоритет

| # | Проблема | Детали |
|---|----------|--------|
| M1 | ColumnInfo.type всегда пустая строка | Отображается в TableNode/StatementNode, но не заполняется API |
| M2 | Нет accessibility (ARIA) | Кнопки без label, цветовая индикация без текста |
| M3 | Нет виртуализации ReactFlow | Потенциально проблема при 500+ нод |
| M4 | Magic numbers | L2_MAX_COLS=5, MAX_PARTIAL_COLS=7 и т.д. — вынести в config |
| M5 | Рекурсия walkTree() без защиты от циклов | transformGraph.ts — может зациклиться на битых данных |
| M6 | SearchPanel.tsx (466 LOC) — много обязанностей | Разделить на SearchInput, SearchResults, HiddenNodesPanel |

### 3.5 Файловая карта компонентов

```
verdandi/src/
├── components/
│   ├── canvas/
│   │   ├── LoomCanvas.tsx          (663)  ← основной canvas
│   │   ├── Breadcrumb.tsx          (139)
│   │   └── nodes/
│   │       ├── TableNode.tsx       (285)  ← L2 основная нода
│   │       ├── StatementNode.tsx   (183)  ← SQL statements
│   │       ├── DatabaseNode.tsx    (169)  ← L1 container
│   │       ├── ApplicationNode.tsx (137)  ← L1 app group
│   │       ├── L1SchemaNode.tsx    (117)  ← L1 chip
│   │       ├── ServiceNode.tsx     (116)  ← L1 service
│   │       ├── NodeExpandButtons.tsx (98) ← LOOM-027
│   │       ├── SchemaGroupNode.tsx  (72)  ← L2 container
│   │       ├── RoutineGroupNode.tsx (98)
│   │       ├── SchemaNode.tsx, ColumnNode.tsx, PackageNode.tsx, RoutineNode.tsx
│   │       └── proto/L1NodesProto.tsx (618) — можно удалить
│   ├── layout/
│   │   ├── Header.tsx              (336)
│   │   ├── FilterToolbar.tsx       (345)  ← L2/L3 toolbar
│   │   ├── FilterToolbarL1.tsx     (346)  ← L1 toolbar
│   │   ├── ResizablePanel.tsx      (143)
│   │   ├── Shell.tsx                (69)
│   │   ├── StatusBar.tsx
│   │   └── LanguageSwitcher.tsx     (93)
│   ├── panels/
│   │   └── SearchPanel.tsx         (466)
│   └── auth/
│       ├── LoginPage.tsx           (192)
│       └── ProtectedRoute.tsx
├── stores/
│   ├── loomStore.ts                (537)
│   └── authStore.ts                 (93)
├── services/
│   ├── lineage.ts                  (169)  ← GraphQL queries
│   └── hooks.ts                    (105)  ← React Query hooks
├── utils/
│   ├── transformGraph.ts           (789)  ← ⚠️ самый большой файл
│   ├── layoutGraph.ts              (183)  ← ELK layout
│   ├── layoutL1.ts                 (204)  ← L1 positions
│   └── filterGraph.ts
├── types/
│   ├── domain.ts                   (134)
│   └── graph.ts
└── i18n/
    ├── config.ts
    └── locales/{en,ru}/common.json
```

---

## 4. Ревью бэкенда (SHUTTLE)

### 4.1 Сильные стороны

1. **Чистые record-модели** — immutable, с GraphQL @Description
2. **Cypher 100% параметризован** — нет string concatenation, безопасно
3. **Reactive Uni<T>** — все сервисы неблокирующие
4. **Parallel query execution** — Uni.combine().all().unis() для независимых запросов
5. **Обходные решения ArcadeDB багов** задокументированы в коде

### 4.2 Критические проблемы

#### P1 — SQL инъекция в SearchService (Medium severity)

**Файл:** `SHUTTLE/src/main/java/studio/seer/lineage/service/SearchService.java:86-87`

```java
private Uni<List<Map<String, Object>>> q(String template, String like, int n) {
    return arcade.sql(String.format(template, like, n));
}
```

User input вставляется через `String.format()`, не через параметризованные запросы. Есть escape одинарных кавычек через `esc()`, но это не полноценная защита.

**Рекомендация:** Перейти на параметризованные ArcadeDB SQL запросы.

#### P2 — Полное отсутствие обработки ошибок

Все 4 сервиса (ExploreService, LineageService, SearchService, OverviewService) не имеют `.onFailure()` на Uni-цепочках. Ошибки ArcadeDB (timeout, syntax error) приводят к:
- Утечке Java stack traces клиенту (security risk)
- Отсутствию логирования ошибок
- Нет retry-логики

**Рекомендация:** Добавить `.onFailure().invoke(log::error)` + `.onFailure().recoverWithItem()` с meaningful HTTP-ошибками.

#### P3 — ExploreService.exploreSchema() — 169 строк одним методом

11-branch UNION ALL Cypher-запрос на 202 строки. Хрупкий — баги ArcadeDB в Cypher UNION обходятся в Java-коде.

**Рекомендация:** Декомпозировать по паттернам доступа (schema→table, schema→pkg, schema→session).

### 4.3 Высокий приоритет

| # | Проблема | Детали |
|---|----------|--------|
| H1 | Hardcoded LIMIT | 200–500 в Cypher, клиент не знает о truncation |
| H2 | Search score weights hardcoded | DaliTable=1.0, DaliStatement=0.6 — нет конфигурации |
| H3 | Нет rate limiting | Произвольный `limit` до 100, но нет per-user quota |
| H4 | Нет query timeout | ArcadeGateway не ограничивает время запроса |
| H5 | SeerIdentity не валидирует роли | Доверяет rbac-proxy полностью |

---

## 5. Ревью auth-gateway (Chur)

### 5.1 Сильные стороны

1. **JWT в httpOnly cookies** — защита от XSS
2. **Ручной CORS** — обход несовместимости Fastify 4/5
3. **Write-operation detection** — regex для INSERT/UPDATE/DELETE требует admin role
4. **Чистый TypeScript** с strict mode

### 5.2 Проблемы

| # | Проблема | Severity | Детали |
|---|----------|----------|--------|
| 1 | Пароли ArcadeDB в plaintext | Low | В config.ts, не в secrets manager |
| 2 | Нет refresh token | Medium | JWT-сессия не продлевается автоматически |
| 3 | Нет rate limiting на /auth/login | Medium | Bruteforce возможен |

---

## 6. Тестирование

### Текущее состояние: ⚠️ Тесты отсутствуют

Не обнаружено ни одного тестового файла:
- verdandi: нет `*.test.tsx`, `*.spec.ts`
- SHUTTLE: нет `src/test/java/`
- Chur: нет `*.test.ts`

**Рекомендация (приоритезированная):**
1. **Unit-тесты transformGraph.ts** — критически важная бизнес-логика трансформации
2. **Integration-тесты SHUTTLE** — проверка Cypher запросов на реальной ArcadeDB
3. **E2E тест** — drill-down L1→L2→L3 и обратно

---

## 7. Документация — текущее состояние

| Документ | Путь | Актуальность |
|----------|------|-------------|
| SETUP.md | internal_docs/SETUP.md | ✅ Актуален |
| LOOM_TASKS.md | current_pharse/ | ✅ Актуален (Phase 1-2 history) |
| LOOM_PHASE3_TASKS.md | current_pharse/ | ⚠️ Частично актуален — не отражает реальный статус завершённых задач |
| ARCH_04042026_UX_LOOM_NAVIGATION.md | current_pharse/ | ✅ Актуален |
| HOUND_TASK_DB_SCHEMA_ALIGN.md | current_pharse/ | ✅ Актуален, задача не начата |
| LOOM_I18N_DICTIONARY.md | current_pharse/ | ⚠️ Может быть неполным — новые ключи из LOOM-027/032 |

**Отсутствует:**
- Архитектурная документация (ADR-формат) с текущим состоянием
- API reference для SHUTTLE GraphQL-эндпоинтов
- Описание фильтрационного pipeline (6 фаз)
- Описание L1 layout алгоритма

---

## 8. Технический долг — сводка

### По критичности

| Приоритет | Кол-во | Примеры |
|-----------|--------|---------|
| 🔴 Critical | 3 | SQL injection в SearchService, отсутствие error handling (FE+BE), LoomCanvas 663 LOC |
| 🟡 High | 8 | console.log в проде, expand spam, нет retry, дублирование toolbar, hardcoded limits |
| 🟢 Medium | 6 | Accessibility, виртуализация, magic numbers, ColumnInfo.type пустой |
| ⚪ Low | 3 | Proto-файл на удаление, пароли в plaintext config, refresh token |

### По трудозатратам

| Группа работ | Оценка | Приоритет |
|-------------|--------|-----------|
| Error handling (FE + BE) | 4–6 ч | 🔴 |
| Рефакторинг LoomCanvas → hooks | 3–4 ч | 🔴 |
| Рефакторинг transformGraph → split | 2–3 ч | 🟡 |
| Параметризация SQL в SearchService | 1–2 ч | 🔴 |
| Unit-тесты transformGraph | 4–6 ч | 🟡 |
| Accessibility (ARIA labels) | 2–3 ч | 🟢 |

---

## 9. Выводы

**Проект в хорошем состоянии для MVP.** За 3 дня (03–05.04.2026) реализованы Phase 1–3 (core), покрывающие полный цикл от L1 Overview до L2 Explore с реальными данными из ArcadeDB. Архитектура трёхуровневая, хорошо типизированная, с правильным разделением фронтенда (React Flow + Zustand + TanStack Query) и бэкенда (Quarkus GraphQL + Cypher).

**Главные риски:**
1. Отсутствие тестов — любой рефакторинг рискованный
2. SQL injection в SearchService — единственная реальная уязвимость
3. Без error handling пользователь видит белый экран при ошибках ArcadeDB
4. Монолитные файлы (LoomCanvas, transformGraph) усложняют параллельную работу
