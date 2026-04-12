> ⚠️ **УСТАРЕВШИЙ ДОКУМЕНТ (deprecated, 12.04.2026)**
> Терминология устарела после сессии v2.2 (11.04.2026):
> — везде «AIDA architecture» = **SEER Studio** (первый продукт AIDA платформы)
> — актуальные решения: `MODULES_TECH_STACK.md` v2.3, `DECISIONS_LOG.md` v1.1
> Документ сохранён как источник SWOT-анализа (§4), risk register (§5), и Track A/B (§5.4).

# AIDA — Архитектурное ревью к HighLoad++ 2026

**Документ:** `ARCH_11042026_06-30_REVIEW_AIDA_HIGHLOAD_DEMO`
**Тип:** Architecture review (Режим 1)
**Дата:** 11.04.2026
**Версия:** 1.0
**Автор:** strategic session output
**Связанные документы:**
- `active/AIDA_TARGET_ARCH_HIGHLOAD_2026.md` — стратегический roadmap (source of truth)
- `active/ARCH_11042026_06-30_SPEC_AIDA_HIGHLOAD_DEMO.md` — техническая спецификация (companion)
- `active/NAMING_MATRIX.md` — нейминг компонентов
- `active/DECISION_DEPLOYMENT_STRATEGY.md`, `DISCOVERY_ANVIL.md`, `DISCOVERY_FRIGG_SCHEMA.md`, `DISCOVERY_MULTIGRAPH.md`

---

## 0. Резюме

AIDA — платформа lineage intelligence для legacy SQL систем. Публичный дебют запланирован на HighLoad++ Moscow в октябре 2026, через ~6 месяцев от даты документа. Этот документ — архитектурное ревью текущего состояния платформы и предлагаемой целевой архитектуры под цель «убедительный live demo на HighLoad».

**Главный вывод ревью:** архитектура по сути готова и согласована между документами, но имеет три критические зоны риска (HEIMDALL как новый cross-cutting слой, MIMIR как самый рискованный компонент, и LOOM scaling от 10 узлов к 5–10K), и одну зону противоречия (MIMIR помечен как «teaser default» в decision section, но играет live в demo narrative). Этот документ фиксирует диагноз и рекомендации, не пересматривая стратегию.

**Основной target October 2026:**
- Hound: PL/SQL + PostgreSQL парсеры, ~85% resolution rate, <5 sec на 50K LoC
- LOOM: плавный рендеринг 5–10K узлов, presentation mode, split-screen
- HEIMDALL: cross-cutting observability layer (новый компонент), который превращает demo в engineering spectacle
- MIMIR MVP: tool calling по 5 готовым демо-запросам, fallback на cached responses
- ANVIL minimum: `find_downstream_impact()` как основной tool для MIMIR
- Полный demo end-to-end отрепетирован 5+ раз без сбоев

**Бюджет:** ~900K ₽ (~$9K) на 6 месяцев, founder-only, без зарплат.

**Команда:** 2 co-founder, переменная скорость до июля (family constraints), полная скорость июль–октябрь.

---

## 1. Текущее состояние (As-Is)

### 1.1 Что уже работает (production-ready)

| Компонент | Статус | Подтверждённые метрики |
|---|---|---|
| **Hound** (парсер PL/SQL) | Production-ready | 9920 atoms на реальном batch, 78.5% resolution rate |
| **YGG** (ArcadeDB) | Working | Реальные lineage-графы хранятся, multi-model доступ |
| **SHUTTLE** (GraphQL read API) | Working | Read API в проде, обслуживает LOOM/KNOT |
| **LOOM** (graph canvas) | Working | Реальные графы рендерятся, протестировано на 10 узлах |
| **KNOT** (inspector) | Working | Сессии, structure breakdown, статистика, query types |
| **LOOM ↔ KNOT integration** | Working | «Open in LOOM» button, navigation |
| **Chur** (auth) | Working | Базовая JWT validation для legacy deployment |
| **Dali Core** (data models) | Partially | Модели данных существуют, нужна проверка консистентности |

### 1.2 Чего ещё нет (не начато или ранняя стадия)

| Компонент | Статус | Что нужно сделать к октябрю |
|---|---|---|
| **HEIMDALL** | Не существует | Полностью построить (event bus, metrics, dashboard, stream viewer) |
| **MIMIR** | Не начато | Tool calling framework + 5 tools + fallback стратегия |
| **ANVIL** | Не начато | Минимум — `find_downstream_impact()` |
| **FRIGG** | Не начато | Saved LOOM views (cheap win, ~2-3 дня) |
| **PostgreSQL dialect в Hound** | Не начато | CTEs, window functions, JSON ops |
| **Performance/scale в LOOM** | Не начато | Виртуализация, прогрессивная загрузка для 5–10K узлов |
| **Benchmark infrastructure** | Не начато | Сравнение с sqlglot, sqllineage |

### 1.3 Что отложено до post-HighLoad (defer list)

Зафиксированный список того, что **сознательно** не делается до октября:

- **URD** — time-travel, history, audit trail
- **SKULD** — flow design, future state
- **MUNINN** — persistent metrics archive
- **BIFROST** — full production infrastructure stack (Prometheus, Grafana, Loki, Vault, Keycloak, OTel)
- **Multi-graph** в YGG — изоляция DEV/STAGE/PROD
- **HUGINN** — отдельный metrics collector (HEIMDALL покрывает demo needs)
- ClickHouse, MS SQL, MySQL диалекты — кроме случая, если PostgreSQL завершён раньше
- Multi-tenancy, SSO, billing, SOC2
- Native интеграции с dbt/Airflow/Snowflake
- Cloud deployment

### 1.4 Контекстная диаграмма (C4 Level 1)

Показывает AIDA как чёрный ящик в её окружении: пользователи, источники данных, внешние сервисы.

(см. диаграмму ниже)

### 1.5 Контейнерная диаграмма (C4 Level 2)

6-слойная архитектура с HEIMDALL как cross-cutting observability layer.

(см. диаграмму ниже)

---

## 2. Анализ по 8 архитектурным векторам

Каждый вектор оценивается по шкале 1–5 (текущее состояние / целевое к октябрю), с привязкой к конкретным NFR-метрикам и проблемам.

### 2.1 Производительность

**Текущее состояние:** ⭐⭐⭐ (3/5)
- Hound на PL/SQL: production-grade, точные числа есть (9920 atoms, 78.5% resolved)
- LOOM протестирован только на 10 узлах
- ANVIL/MIMIR не существуют — нечего измерять
- HEIMDALL не существует — overhead неизвестен

**Целевое (октябрь):** ⭐⭐⭐⭐ (4/5)

| NFR-метрика | Текущее | Целевое | Узкое место |
|---|---|---|---|
| Hound: парсинг 50K LoC файла | неизвестно | <5 сек | базовое измерение апрель |
| Hound: батч 500K LoC (10 файлов × 50K) | неизвестно | <30 сек | parallel workers |
| Hound: throughput | неизвестно | >10K atoms/sec | TBD после baseline |
| Hound: resolution rate | 78.5% | ≥85% | edge cases в Postgres |
| YGG: 5-hop traversal | неизвестно | <100 ms | demo-критично |
| YGG: 10-hop worst case | неизвестно | <500 ms | ANVIL impact analysis |
| MIMIR: end-to-end query | — | <5 сек | LLM latency dominant |
| LOOM: render 5K узлов | не пробовали | <3 сек до интерактивности | виртуализация |
| LOOM: render 10K узлов | не пробовали | <5 сек | decision LOOM lib |
| LOOM: pan/zoom FPS | неизвестно | 60 fps | критично для демо |
| HEIMDALL: event emission overhead | — | <1% CPU на источнике | бенчмарк в мае |

**Главный риск:** baseline performance измерения ещё не проведены. Невозможно понять, сколько работы нужно для оптимизации, пока нет точки отсчёта. Рекомендация: апрель — week 1, измерить baseline для Hound и LOOM.

### 2.2 Масштабируемость

**Текущее состояние:** ⭐⭐ (2/5)
- Архитектура single-process на одном ноутбуке
- Нет горизонтального масштабирования
- Hound parallel workers есть, но не оптимизированы

**Целевое (октябрь):** ⭐⭐ (2/5) — **сознательно не масштабируем**

Это важная фиксация: целевая архитектура **намеренно** не делает горизонтального масштабирования. Demo проходит на одном ноутбуке, post-conference — Docker Compose для первого пилота. Масштабирование — это post-HighLoad work (см. defer list).

**То, что мы масштабируем — это размер графа на одной машине:**
- LOOM от 10 узлов к 5–10K (вертикальная виртуализация)
- Hound parallel workers (vertical concurrency)
- YGG индексы для быстрых traversals на 50K vertices / 100K edges

### 2.3 Отказоустойчивость

**Текущее состояние:** ⭐⭐ (2/5)
- Live demo сценарий не отрепетирован
- Нет formal recovery стратегии
- Нет fallback'ов

**Целевое (октябрь):** ⭐⭐⭐⭐ (4/5) — **demo safety**

Здесь fault tolerance переосмысляется: не как production uptime, а как **«demo не падает на сцене»**. Это конкретный set требований:

| NFR-метрика | Текущее | Целевое |
|---|---|---|
| MIMIR: LLM API failure recovery | нет | 3-tier fallback (Claude→GPT-4→cached) |
| Backup laptop ready | нет | identical setup, weekly sync |
| Демо session reset time | вручную | <5 сек через `make demo-reset` |
| Pre-recorded backup video | нет | готов до сентября, как ultimate fallback |
| Recovery from process crash | вручную | scripted restart <15 сек |
| Data integrity guarantee | none | snapshot-based reset |

**ADR-DA-006** (3-tier fallback) — критичное решение для безопасности демо. Все три уровня эмитят одинаковые HEIMDALL события, аудитория не отличает.

### 2.4 Безопасность

**Текущее состояние:** ⭐⭐ (2/5) — basic auth для legacy

**Целевое (октябрь):** ⭐⭐ (2/5) — **сознательно минимум**

Это **не** production. Demo deployment:
- Single admin user, pre-generated JWT
- API ключи в env vars, не в коде
- Local-only binding (нет внешней сети)
- Synthetic/anonymized demo dataset (нет реальных клиентских данных)

Полный set production security (RBAC, encryption at rest, audit log, secrets management, vulnerability scanning, SOC2 prep) — отложен до post-HighLoad, когда появится первый pilot customer.

**Единственный риск, который нужно держать в голове:** API ключи. Если ноутбук со сцены украдут или скомпрометируют, ключ Claude API может утечь. Митигация — separate API key только для demo, ротация после конференции.

### 2.5 Наблюдаемость

**Текущее состояние:** ⭐ (1/5) — практически нет

**Целевое (октябрь):** ⭐⭐⭐⭐⭐ (5/5) — **главный differentiator демо**

Это самый большой dimension transformation в плане. Из почти нуля → к лучшему наблюдаемому компоненту во всём документе. HEIMDALL как cross-cutting layer — это **главная архитектурная новация** v2 относительно v4.

**Логика:** HighLoad audience — инженеры, которые живут в Prometheus/Grafana/Jaeger. Показать им систему без visibility в её внутренности — значит показать чёрный ящик. С HEIMDALL каждая часть demo превращается в engineering spectacle: counters анимируются в real time, tool calls происходят transparently, parser decisions видны в event stream.

| Что | Текущее | Целевое |
|---|---|---|
| Event bus across components | нет | in-process ring buffer 10K events |
| Metrics aggregator | нет | counters, gauges, histograms |
| Real-time dashboard | нет | WebSocket streaming, presentation mode |
| Event stream viewer | нет | filtered, color-coded, real-time |
| Demo recording / replay | нет | для backup сценария |

**Scope discipline:** HEIMDALL **только demo observability**. Не Prometheus exposition, не distributed tracing, не Jaeger, не persistent storage. Production observability — post-HighLoad. Это критически важная граница, чтобы scope не explode.

### 2.6 Сопровождаемость

**Текущее состояние:** ⭐⭐⭐ (3/5)
- Working code base (~20K LoC/week velocity)
- Modular структура с чёткими boundaries
- Tests существуют, но coverage не измерен

**Целевое (октябрь):** ⭐⭐⭐ (3/5) — **сознательно держим минимум**

Это работа в pet project mode перед демо. Целевое coverage 30–40% **только для critical paths**, не comprehensive. Полный testing strategy (E2E, fuzzing, mutation testing) — post-HighLoad.

Что улучшится:
- Документация компонентов через ARCH-spec и DISCOVERY docs
- Component interfaces matrix как контракт (см. ARCH-spec § 5)
- Parallel development через track A / track B split (см. § 5 этого ревью)
- Demo rehearsal как форма integration testing

Что **не** улучшится до октября:
- Comprehensive unit tests
- Code review process
- CI/CD pipeline
- Static analysis tooling

### 2.7 Расширяемость

**Текущее состояние:** ⭐⭐⭐⭐ (4/5) — это unexpected strength

**Целевое (октябрь):** ⭐⭐⭐⭐ (4/5)

Архитектура изначально модульная: Hound dialects как plugin'ы, MIMIR tools как registry, HEIMDALL events как stream type. Добавление нового диалекта в Hound, нового tool в MIMIR registry, или нового event type в HEIMDALL — это explicit extension points, не requirement to refactor.

**ADR-DA-002** (custom MIMIR framework) — даёт контроль над расширением. **ADR-DA-007** (algorithms-as-tools, унаследован из v4 ADR-014) — фиксирует, что бизнес-логика остаётся в ANVIL/URD/SKULD, MIMIR только маршрутизирует. Это означает, что post-HighLoad добавление новых tools — это работа в существующей extension point, не архитектурный пересмотр.

**Точки расширения:**
1. **Hound dialects** — `dialects/{plsql,postgresql,clickhouse,...}/` — изолированы по интерфейсу
2. **MIMIR tools** — JSON registry + Python implementations — изолированы
3. **HEIMDALL event types** — typed events с schema, добавление нового — не breaking
4. **YGG vertex/edge types** — schema-evolution friendly через ArcadeDB
5. **SHUTTLE GraphQL schema** — additive changes без breaking

### 2.8 Стоимость

**Текущее состояние:** очень низкая (pet project mode)

**Целевое (октябрь):** ~900K ₽ за 6 месяцев

| Статья | Apr–Jun | Jul–Sep | Oct | Total |
|---|---|---|---|---|
| Infrastructure | 90K ₽ | 90K ₽ | 30K ₽ | 210K ₽ |
| LLM API costs (testing + demo) | 30K ₽ | 90K ₽ | 30K ₽ | 150K ₽ |
| HEIMDALL infra | 10K ₽ | 20K ₽ | 5K ₽ | 35K ₽ |
| Software subscriptions | 60K ₽ | 60K ₽ | 20K ₽ | 140K ₽ |
| Travel в Moscow | 0 | 0 | 100K ₽ | 100K ₽ |
| Booth materials | 0 | 20K ₽ | 0 | 20K ₽ |
| Landing page | 0 | 10K ₽ | 5K ₽ | 15K ₽ |
| Second monitor для split-screen | 0 | 30K ₽ | 0 | 30K ₽ |
| Contingency | 50K ₽ | 100K ₽ | 50K ₽ | 200K ₽ |
| **Итого** | **240K ₽** | **420K ₽** | **240K ₽** | **900K ₽** |

Зарплаты не включены. Founder-only до первой выручки.

**LLM cost детализация:** ~5 demo queries × ~3K tokens × ~10 rehearsals = ~150K tokens. Claude Sonnet ~$0.45 input + $2.25 output = $3-5 за полный rehearsal цикл. Бюджет 150K ₽ — с большим запасом на эксперименты в development.

---

## 3. Сводная оценочная карта по векторам

| Вектор | Текущее | Целевое | Δ | Критичность для демо |
|---|---|---|---|---|
| Производительность | 3/5 | 4/5 | +1 | **Высокая** (Scale story) |
| Масштабируемость | 2/5 | 2/5 | 0 | Низкая (single laptop) |
| Отказоустойчивость | 2/5 | 4/5 | +2 | **Критичная** (live demo safety) |
| Безопасность | 2/5 | 2/5 | 0 | Низкая (demo only) |
| Наблюдаемость | 1/5 | **5/5** | **+4** | **Критичная** (HEIMDALL = differentiator) |
| Сопровождаемость | 3/5 | 3/5 | 0 | Низкая (pet mode) |
| Расширяемость | 4/5 | 4/5 | 0 | Низкая (post-event) |
| Стоимость | низкая | ~900K ₽ | — | Средняя |

**Главный сигнал из таблицы:** наблюдаемость — это самая большая трансформация (+4), и она же самая критичная для демо. Это значит, что **HEIMDALL — это не optional add-on, а самый большой single bet** в плане. И это не риск, который можно митигировать через «откатимся к pre-recorded video» (Risk 8 в TARGET_ARCH) — сам факт отката убирает основной differentiator.

---

## 4. SWOT-анализ

|  | **Полезные** | **Вредные** |
|---|---|---|
| **Внутренние** | **S — Сильные стороны** <br><br> • Hound уже production-ready на PL/SQL с конкретными числами (9920 atoms, 78.5%) <br>• LOOM, KNOT, SHUTTLE-read working и интегрированы <br>• Чёткая модульная архитектура с extension points <br>• Согласованность TARGET_ARCH ↔ DETAILED_ARCH ↔ DISCOVERY (нет противоречий между документами) <br>• Decisions фиксируются как ADR (6 штук в § 6 detailed arch + ADR-014 наследуется) <br>• Defer list жёсткий и явный — scope creep заблокирован <br>• Track A / Track B split позволяет parallel development | **W — Слабые стороны** <br><br> • Baseline performance не измерен — все performance targets пока «hope» <br>• HEIMDALL — большой новый компонент (~4-6 weeks full velocity, 8-10 weeks calendar в variable mode) <br>• MIMIR — самый рискованный, нет ничего готового <br>• LOOM scale gap огромный (10 узлов → 5-10K) <br>• Нет benchmark infrastructure для Correctness story <br>• Нет benchmark dataset (что именно — 500K LoC?) <br>• Co-founder split не зафиксирован на персональном уровне <br>• Нет CI/CD, тесты неполные |
| **Внешние** | **O — Возможности** <br><br> • HighLoad audience — техническая, ценит observability и наблюдаемость <br>• Slot на HighLoad — concrete forcing function для команды <br>• Booth + talk = доступ к 100+ потенциальным leads за 2-3 дня <br>• Russian enterprise SQL legacy market — реально underserved <br>• Anti-RAG narrative актуален, выделяется на фоне vector RAG hype <br>• ArcadeDB как полигон для multi-model — мало кто использует, тех. interest от audience | **T — Угрозы** <br><br> • CFP может быть отвергнут (митигация: backup conferences) <br>• Family constraints могут продлиться за июль <br>• Live demo crash on stage — единственный публичный point of failure <br>• LLM API outage во время демо <br>• Travel logistics (Варшава → Москва в 2026) — политические/визовые риски <br>• Co-founder burnout при 6 месяцах sustained intensity <br>• Конкурент (Atlan, dbt docs, Manta) объявит column-level lineage до октября <br>• ArcadeDB community/vendor risk |

---

## 5. Зоны риска и противоречий

### 5.1 Зона риска №1 — HEIMDALL как новый critical-path компонент

**Проблема:** HEIMDALL — самый большой новый компонент в плане, и он на critical path. Без него split-screen demo невозможен. Без split-screen demo — теряется главный differentiator. Без differentiator — демо превращается в «ещё один lineage tool», что для HighLoad audience не достаточно.

**Размер работы:** 4–6 недель full velocity = 8–10 недель calendar time в variable mode апрель–июнь. Это **большая часть work budget одного из co-founder** до июля.

**Risk 8 в TARGET_ARCH** говорит «degradable to pre-recorded video». Но pre-recorded video убирает именно тот wow factor, ради которого HEIMDALL вообще нужен. Это **псевдо-fallback** — он как будто есть, но на самом деле его нет.

**Рекомендация:** не относиться к HEIMDALL как к деривативу — относиться как к first-class deliverable. В апреле — формальный design review HEIMDALL (event schema, протокол, layout). В мае — first running event bus emit/collect. В июне — first dashboard rendering. Если июнь milestone не достигнут — это **сигнал red flag**, а не «продолжаем как обычно».

### 5.2 Зона риска №2 — MIMIR противоречие в narrative vs decision

**Проблема:** В § 5.1 (Technical decisions) TARGET_ARCH написано:

> «Рекомендация: Option B как planning default, с возможностью upgrade к Option A если development ahead of schedule к концу августа»

Где Option B = «MIMIR как teaser в конце, не live», Option A = «MIMIR в demo, intelligence story full».

**Но в § 2.2 (Talk structure)** Intelligence demo полностью построен на live MIMIR с tool calling, event stream, contrast с ChatGPT, second query через ANVIL. Это **8 минут из 40-минутного talk** — пятая часть.

Если planning default = Option B, то 8 минут narrative должны быть переписаны заранее, а не в сентябре в режиме паники. Если planning default = Option A, то декларация «Option B по умолчанию» — лицемерие.

**Рекомендация:** прямо сейчас, в рамках апреля, **закрыть это решение жёстко** — выбрать одно. Я бы рекомендовал **Option A полностью** с trigger для downgrade в конце августа: если к 31 августа MIMIR не отвечает на 3+ demo queries reliably в rehearsal — переходим в Option B и переписываем narrative. Это honest planning, не двоемыслие.

### 5.3 Зона риска №3 — LOOM scale gap

**Проблема:** Текущий LOOM протестирован на **10 узлах**. Цель октября — **5000–10000 узлов**. Это разница в 500–1000 раз. Виртуализация, level-of-detail, прогрессивная загрузка — это всё **новая работа**, и решение о library (Sigma.js / Cytoscape / WebGL) ещё не принято (ADR-DA-004 — pending).

**Митигация:** в апреле — выбор библиотеки и первый профайл. Если в мае ясно, что выбранная библиотека не тянет 5K узлов — есть время переключиться. Если переключение нужно в августе — поздно.

**Конкретное действие:** ADR-DA-004 должен быть закрыт до конца апреля. Это блокирующий decision для начала heavy LOOM работ.

### 5.4 Зона риска №4 — Co-founder split не на уровне людей

**Проблема:** Detailed Architecture § 10 описывает Track A (data & algorithms) и Track B (UI & orchestration). Но **кто из co-founder какой track ведёт — не зафиксировано**. Это критично, потому что:

- Track A в основном существует (Hound, YGG, ANVIL новый, Dali Core verification) — relatively known territory
- Track B в основном новый (HEIMDALL, MIMIR, LOOM scaling, split-screen) — много unknowns

Если оба co-founder одинаково комфортны в обоих треках — split произволен. Если у одного из них преимущество в одном из треков — это должно определить распределение.

**Рекомендация:** за неделю выяснить и зафиксировать. Это не архитектурное решение, но без него parallel development на бумаге не превратится в parallel development в реальности.

### 5.5 Зона риска №5 — Benchmark dataset не определён

**Проблема:** TARGET_ARCH везде упоминает «realistic datasets», «real batch», «500K+ LoC». Но **что конкретно** — не зафиксировано:

- Откуда взять 500K LoC PL/SQL?
- Можно ли его публично показывать на сцене?
- Это синтетический? Реальный анонимизированный? Open source проект?
- Кто его готовит и к какой дате?

Без этого — Correctness story и Scale story зависят от данных, которые могут не появиться вовремя или оказаться непригодными для публики.

**Рекомендация:** в апреле — milestone «benchmark dataset selected and prepared». Кандидаты: e.g. Oracle sample HR/SH/OE schemas, public PostgreSQL benchmarks (TPC-H, TPC-DS), open-source проекты на GitHub (например, DataHub, Apache Atlas examples).

### 5.6 Зона риска №6 — Open questions § 14 не закрыты

В Detailed Architecture § 14 — 6 open questions с дедлайном «end of April»:

1. LOOM rendering library (ADR-DA-004)
2. MIMIR LLM provider (Claude confirmed, fallback?)
3. FRIGG storage (separate YGG instance vs JSON file)
4. Dialect scope (drop ClickHouse сейчас или ждать?)
5. Event bus transport (in-process vs Redis pub/sub)
6. HighLoad CFP deadline (когда?)

Из этих **CFP deadline** — самый блокирующий, потому что от него зависит, есть ли вообще demo. Если CFP deadline на самом деле — начало мая, остаётся 3 недели на abstract.

**Рекомендация:** CFP deadline закрыть в течение **48 часов** через прямой контакт с организаторами HighLoad++. Остальные 5 — до конца апреля, как и было запланировано.

---

## 6. ТРИЗ — главное архитектурное противоречие

**Противоречие:** Если **расширить scope HEIMDALL до production-grade observability** (Prometheus, Grafana, persistence), то получаем post-HighLoad readiness (полезно), **но это съедает >12 weeks calendar**, что ломает October timeline (вредно).

**Идеальный конечный результат:** HEIMDALL даёт максимальный demo wow factor + автоматически готовит компонент к production без дополнительной работы.

**Приём разрешения:** **разделение во времени** — HEIMDALL имеет два явно разделённых scope: «demo observability» (October) и «production observability» (post-HighLoad). С общей событийной моделью и общим event bus, но разными UI и backend.

**Конкретное решение:**
1. Event schema проектируется один раз и **намеренно как канонический контракт** (см. ARCH-spec § 2.8). Эта схема не меняется при переходе demo → production.
2. Ring buffer in-memory для demo (October) — потом заменяется на persistent backend (post-HighLoad), не трогая emitters.
3. Dashboard UI demo-режима делается отдельно от production grafana-style UI.
4. Это значит: эмиттеры (Hound, MIMIR, ANVIL, SHUTTLE, LOOM) пишутся **один раз**, а коллекторы и UI — эволюционируют независимо.

Этот ADR должен быть зафиксирован — см. **ADR-DA-001** (HEIMDALL in-process для demo), который уже принят, и его рекомендуется дополнить sub-decision «event schema = stable contract from day 1».

---

## 7. Gap-анализ As-Is → To-Be

| Вектор | As-Is | To-Be (October) | Gap | Действие |
|---|---|---|---|---|
| Производительность | Hound:78.5%/9920 atoms; LOOM:10 nodes; rest unknown | Hound 85%/Postgres; LOOM 5-10K; YGG <100ms 5-hop | Baseline неизвестен; LOOM scale; YGG perf | Apr: измерить baseline; ADR-DA-004 LOOM lib; YGG индексы |
| Масштабируемость | single process | single laptop, vertical scale | 0 (намеренно) | — |
| Отказоустойчивость | none | demo safety: 3-tier MIMIR fallback, backup laptop, scripted reset | Полный stack demo safety | Apr-Sep: scripted demo, ADR-DA-006 fallback, May–Sep rehearsals |
| Безопасность | basic JWT | demo single-user, env vars | 0 (намеренно) | rotated API key для демо |
| Наблюдаемость | none | HEIMDALL: event bus, metrics, dashboard, stream viewer | **Весь HEIMDALL** | **Critical path: design Apr, MVP Jun, integration Aug, polish Sep** |
| Сопровождаемость | working code, no formal tests | docs через ARCH-spec, 30-40% coverage critical paths | docs + minimum tests | Apr: parallel dev split; ongoing: weekly merges |
| Расширяемость | модульная structure | + extension points formalized: dialects, tools, events | минимум | Документировать в ARCH-spec |
| Стоимость | ~0 | ~900K ₽ | 900K ₽ за 6 месяцев | Personal savings или small external |

---

## 8. Рекомендации

Приоритизированы по MoSCoW и горизонтам.

### 8.1 Must have (Apr–May, блокирующие)

| # | Действие | Вектор | Срок | Зависит от |
|---|---|---|---|---|
| M1 | Уточнить CFP HighLoad deadline через прямой контакт | (мета) | 48 часов | — |
| M2 | Зафиксировать co-founder split track A / track B на уровне людей | Сопровождаемость | 1 неделя | — |
| M3 | Закрыть ADR-DA-004 (LOOM rendering library) | Производительность | до 30 апреля | M5 |
| M4 | MIMIR decision: Option A или Option B fix one (см. § 5.2) | (стратегия) | до 30 апреля | — |
| M5 | Hound + LOOM baseline performance измерения | Производительность | до 30 апреля | — |
| M6 | Benchmark dataset выбран и подготовлен | Производительность | до 15 мая | — |
| M7 | HEIMDALL event schema спроектирован (см. § 6 выше — stable contract) | Наблюдаемость | до 15 мая | — |
| M8 | CFP submission | (мета) | май (зависит M1) | M1 |

### 8.2 Should have (Jun–Aug)

| # | Действие | Вектор | Срок |
|---|---|---|---|
| S1 | HEIMDALL event bus MVP — events from Hound | Наблюдаемость | конец мая |
| S2 | PostgreSQL dialect Hound основные cases | Производительность | конец июня |
| S3 | HEIMDALL dashboard первая итерация | Наблюдаемость | конец июня |
| S4 | LOOM virtualization на 1-5K узлах | Производительность | конец июня |
| S5 | MIMIR tool calling framework + 3 первых tools | (Intelligence) | конец июля |
| S6 | ANVIL `find_downstream_impact` рабочий | (Intelligence) | конец июля |
| S7 | Split-screen LOOM + HEIMDALL отрепетирован | Отказоустойчивость | конец августа |
| S8 | Demo end-to-end отрепетирован 3 раза без сбоев | Отказоустойчивость | конец августа |

### 8.3 Could have (Sep–Oct)

| # | Действие | Приоритет drop |
|---|---|---|
| C1 | ClickHouse dialect | Drop если PostgreSQL не finished |
| C2 | FRIGG saved views | Drop если HEIMDALL behind |
| C3 | KNOT export capabilities | Drop без потери |
| C4 | 4-й blog post про observability | Drop без потери |

### 8.4 Won't have (явный defer post-HighLoad)

См. § 1.3.

---

## 9. Что дальше

1. **Сегодня:** прочитать этот документ с co-founder. Согласиться или поправить
2. **Эта неделя:** закрыть M1 (CFP deadline) и M2 (co-founder split)
3. **Этот месяц:** закрыть M3, M4, M5 (три ADR-блокирующих решения)
4. **К 15 мая:** M6, M7, M8 (готово для May milestone)
5. **Дальше:** следовать roadmap по месяцам в TARGET_ARCH

Этот документ — review snapshot на дату 11.04.2026. Обновляется ежеквартально (конец июня, конец сентября) или при существенных изменениях в плане.

---

*Companion документ: `ARCH_11042026_06-30_SPEC_AIDA_HIGHLOAD_DEMO.md` содержит детальную техническую спецификацию архитектуры — компоненты, интерфейсы, data flows, ADR, performance budgets.*
