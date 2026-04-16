# AIDA Demo Script — HighLoad++ 2026

**Документ:** `DEMO_SCRIPT`
**Версия:** 1.0
**Дата:** 16.04.2026
**Статус:** 📋 DRAFT — требует rehearsal-прогона
**Формат:** 40 минут доклад + живое демо

> ⚠️ Перед demo: выполни checklist из `docs/guides/STARTUP_SEQUENCE.md §4`

---

## Таймлайн

```
00:00 — Акт 1: Проблема           (7 мин)
07:00 — Акт 2: Как мы решили      (15 мин)
22:00 — Акт 3: Live Demo          (10 мин)
32:00 — Числа и выводы            (5 мин)
37:00 — Q&A                       (3 мин)
```

---

## Акт 1: Проблема (00:00–07:00)

### Слайд 1 — Открытие (00:00–01:30)

**Фраза:**
> «Представьте: вы пришли в компанию, где 15 лет ведётся Oracle база данных.
> 800 хранимых процедур. 3000 таблиц. И вам нужно понять — если мы изменим
> колонку `orders.status`, что сломается. За неделю.»

*Пауза 3 секунды.*

> «Мы потратили 2 месяца, чтобы больше не тратить эту неделю никогда.»

### Слайд 2 — Что такое lineage (01:30–03:30)

**Фраза:**
> «Column lineage — это ответ на вопрос: откуда взялось значение в этой колонке?
> Не на уровне "процедура читает таблицу", а на уровне конкретного SELECT,
> конкретного output column.»

*Показать схему: `DaliColumn → DATA_FLOW → DaliOutputColumn → WRITES_TO → DaliTable`*

> «Если вы видели dbt lineage — это то же самое, но для imperative PL/SQL.
> И это значительно сложнее.»

### Слайд 3 — Почему сложно (03:30–07:00)

**Фраза:**
> «PL/SQL — не декларативный SQL. В нём есть: %ROWTYPE, BULK COLLECT,
> RETURNING INTO, PIPE ROW, DBMS_SQL, 4 уровня вложенных subquery,
> WITH FUNCTION, JSON_TABLE поверх JSON-колонок, dynamic SQL.»

*Показать список конструкций — как BINGO-карточка:*

```
BULK COLLECT ✅    RETURNING INTO ✅    %ROWTYPE ✅
PIPE ROW ✅        DBMS_SQL stub ✅     JSON_TABLE 🔵
LATERAL S9 🔵      Nested records 🔵   Dynamic SQL ❌
```

> «Мы реализовали 14 из 19. 98.8% атомов разрешено.»

---

## Акт 2: Как мы решили (07:00–22:00)

### Слайд 4 — Стек (07:00–09:00)

**Фраза:**
> «Стек: ANTLR4-парсер на Java, Quarkus backend, ArcadeDB как граф-база,
> React Flow для визуализации. Всё в одном docker compose.»

*Показать архитектурную схему: VERDANDI → Chur → SHUTTLE → YGG (ArcadeDB)*

### Слайд 5 — ANTLR4 + 4 прохода (09:00–14:00)

**Фраза:**
> «Наивный парсер работает за один проход — он не решает forward references.
> `proc_A` вызывает `proc_B`, объявленную позже. Без двух проходов — PENDING.»

*Схема 4 проходов:*

```
Pass 1: объявления типов + таблицы
Pass 2: сигнатуры рутин (без тел)
Pass 3: тела рутин — основной resolve
Pass 4: PENDING atoms — batch LOOKUP по YGG
```

> «Pass 4 — ключевой. Мы не теряем 5% атомов, которые нельзя разрешить за один проход.»

### Слайд 6 — REMOTE_BATCH write (14:00–18:00)

**Фраза:**
> «Наивная запись в граф-базу: один HTTP-запрос на вершину.
> 143 000 вершин × 50 мс = больше двух часов. Неприемлемо.»

*Показать JsonlBatchBuilder схему:*

```
[атом 1]  ─┐
[атом 2]  ─┤  JsonlBatchBuilder → JSON lines → HTTP POST /batch
...        ─┤  500 вершин за один запрос
[атом 500]─┘
```

> «В итоге: 143 000 атомов за 261 секунду. 4,4 минуты.
> Это примерно 550 атомов в секунду на одном потоке.»

### Слайд 7 — Геоид (18:00–22:00)

**Фраза:**
> «ArcadeDB назначает @rid — внутренний id, меняющийся при пересоздании базы.
> Нам нужен стабильный идентификатор для де-дупликации.»

*Показать пример:*

```
DaliAtom.geoid = "HR.PROC_INIT.3"
              ↑       ↑       ↑
           schema   routine  порядковый_номер
```

> «Геоид детерминирован. Пересоздайте базу — геоиды те же.
> Это позволяет инкрементальный пересчёт: меняем один файл, пересчитываем только его атомы.»

---

## Акт 3: Live Demo (22:00–32:00)

### Подготовка (за кулисами, до выхода)

```bash
# Убедиться что всё поднято:
docker compose ps --format "table {{.Name}}\t{{.Status}}"
# Открыть браузер: http://localhost:15173
# Открыть GraphQL UI: http://localhost:18080/graphql-ui (backup)
```

### Шаг Demo-1: L1 — Schema overview (22:00–23:30)

**Открыть** http://localhost:15173, нажать "HR" schema в списке.

**Фраза:**
> «Вот L1 — обзор схемы HR. 47 таблиц, 23 пакета.
> Каждый пакет — кружок. Размер пропорционален числу рутин.»

*Навести на пакет `PKG_ORDERS`.*

> «PKG_ORDERS — 12 рутин, работает с 8 таблицами.
> Кликаем — переходим на L2.»

### Шаг Demo-2: L2 — Routine aggregate (23:30–25:30)

*Перейти на L2 — `PKG_ORDERS`.*

**Фраза:**
> «L2 — агрегированный граф рутин. Каждое ребро — это несколько операторов,
> объединённых по паттерну: эта рутина читает 3 таблицы, пишет в 1.
> Число на ребре — сколько операторов.»

*Навести на ребро `proc_create_order → orders (3)`:*

> «3 оператора в proc_create_order читают таблицу orders.
> Если я меняю колонку orders.status — эта процедура под угрозой.»

### Шаг Demo-3: L3 — Statement graph (25:30–28:30)

*Кликнуть на `proc_create_order` — перейти на L3.*

**Фраза:**
> «L3 — полный граф. Видим конкретные операторы, колонки, потоки данных.
> Синие пунктирные рёбра — DATA_FLOW. Оранжевые — WRITES_TO.»

*Показать DaliRecord если он есть:*

> «Видите эту фиолетовую карточку? Это PL/SQL record — `order_rec %ROWTYPE`.
> Поле city приходит из address.city через RETURNING INTO.
> Раньше это было невидимо.»

### Шаг Demo-4: L4 — Statement drill (28:30–31:00)

*Кликнуть на крупный `DaliStatement` с несколькими subquery.*

**Фраза:**
> «Кликаем на сложный SELECT — переходим на L4.
> Теперь каждый output column — отдельный узел.
> Видно: total_amount приходит из CTE_sum.amount через DATA_FLOW.
> А dept_id — непосредственно из departments.dept_id.»

*Показать resolved (синий) vs pending (красный) output columns:*

> «Синий — разрешено. Красный — pending, не хватает данных.
> Таких у нас 1.2% от 143 000.»

### Шаг Demo-5: Запуск парсинга (31:00–32:00)

*Открыть HEIMDALL UI: http://localhost:25174*

**Фраза:**
> «Здесь — HEIMDALL, наш observability дашборд. Видим события в реальном времени.»

*В другой вкладке: POST запрос в GraphQL:*

```graphql
mutation {
  startParseSession(input: {
    dialect: "PLSQL",
    source: "/opt/sql/corpus",
    preview: true
  }) {
    id
    status
  }
}
```

> «Запускаем preview-прогон. Статус: QUEUED → PROCESSING.
> В HEIMDALL видим события: FILE_PARSING_STARTED, ATOM_EXTRACTED...»

---

## Акт 4: Числа и выводы (32:00–37:00)

### Слайд 8 — Результаты

**Таблица:**

| Метрика | Значение |
|---|---|
| Файлов | 203 PL/SQL |
| Атомов | **143 000** |
| Resolution rate | **98.8%** |
| Время парсинга | **261 сек** (4.4 мин) |
| Типов вершин в YGG | 12 |
| Типов рёбер | 18 |

**Фраза:**
> «98.8% — это 1412 атомов pending из 143 тысяч.
> Большинство — JSON_TABLE и dynamic SQL. Это следующий спринт.»

### Слайд 9 — Что дальше

**Фраза:**
> «Три направления: PostgreSQL dialect — тот же ANTLR4 grammar, другой semantic listener.
> RAG vs Parse — мы проверяем гипотезу: сможет ли LLM дать сравнимое качество за меньше времени.
> И LOOM L5 — разложить expression в атомы: `CASE WHEN a > 0 THEN b ELSE c END` → 3 source columns.»

---

## Backup сценарии

### Если YGG упал во время demo

```bash
docker compose up -d houndarcade
# Граф загрузится из volumes — данные не теряются
```

**Фраза:**
> «Небольшой технический момент — перезапускаем базу, данные в томах.»
> *(30 сек ожидания)*

### Если LOOM не рендерит граф

→ Переключиться на GraphQL UI (backup):

**URL:** http://localhost:18080/graphql-ui

```graphql
{ exploreSchema(scope: "HR") {
    nodes { id type label }
    edges { src tgt type }
  }
}
```

**Фраза:**
> «Покажу то же самое через raw GraphQL API — это то, что рисует наш frontend.»

### Если совсем всё упало

→ Показать скриншоты в слайдах (backup_screenshots/).

---

## make demo-reset

Процедура сброса demo в начальное состояние:

```bash
# 1. Очистить граф (оставить схему)
curl -X POST http://localhost:2480/api/v1/command/hound \
  -u root:playwithdata -H "Content-Type: application/json" \
  -d '{"language":"sql","command":"DELETE FROM DaliAtom"}'

# 2. Запустить свежий парсинг (5 мин)
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"mutation { startParseSession(input:{dialect:\"PLSQL\",source:\"/opt/sql/corpus\",preview:false,clearBeforeWrite:true}){id status}}"}'

# 3. Ждать COMPLETED
watch -n 5 'curl -s http://localhost:9090/api/sessions | python3 -c \
  "import sys,json; [print(s[\"id\"][:8], s[\"status\"],s.get(\"atomCount\",0)) for s in json.load(sys.stdin)]"'
```

---

## Репетиция-чеклист

```
□ docker compose ps — все 9 сервисов healthy
□ http://localhost:15173 — LOOM открывается, граф L1 есть
□ Кликнуть HR → L1 граф
□ Кликнуть PKG_ORDERS → L2 граф
□ Кликнуть proc_create_order → L3 граф
□ Кликнуть DaliStatement → L4 граф
□ HEIMDALL http://localhost:25174 — открывается
□ GraphQL UI http://localhost:18080/graphql-ui — открывается
□ Backup screenshots в папке demo/screenshots/
□ Таймер на телефоне: 40 мин
```

---

## История изменений

| Дата | Версия | Что |
|---|---|---|
| 16.04.2026 | 1.0 | DRAFT. 40-минутный скрипт: 3 акта + числа + Q&A. Все фразы. Demo шаги L1→L4. Backup сценарии. make demo-reset. |
