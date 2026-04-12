# AIDA — Project Roadmap: April → October 2026

**Документ:** `PROJECT_ROADMAP`
**Версия:** 1.0
**Дата:** 12.04.2026
**Горизонт:** HighLoad++ Moscow, октябрь 2026
**Статус:** Working document

---

## 0. Контекст и цель

**Главная цель:** убедительный live demo на HighLoad++ 2026 (~6 месяцев от сегодня).

**Demo story (три акта):**
1. **Scale** — парсинг 500K+ LoC PL/SQL в реальном времени, lineage граф в LOOM
2. **Intelligence** — MIMIR отвечает на вопросы о зависимостях через tool calling (live event stream в HEIMDALL)
3. **Correctness** — сравнение с sqlglot/sqllineage: AIDA правильно, они падают

**Ключевые ограничения:**
- Переменная velocity до июля (family constraints)
- Проект в pet-project mode (не full-time)
- Demo на одном ноутбуке, не production
- CFP deadline — **ближайшие дни** (🔴 блокирует всё)

---

## 1. Milestones

| # | Milestone | Дата | Критерий готовности |
|---|---|---|---|
| **M0** | Infrastructure готова | 25 апреля | Repo мигрирован, C.0 + C.2.1 закрыты, CFP подан |
| **M1** | Core engines ready | 9 мая | Hound как library, Dali skeleton запускается, data models согласованы |
| **M2** | Dali функционален | 31 мая | UC1 + UC2b работают end-to-end через Dali → Hound → YGG |
| **M3** | Full stack integration | 31 июля | HEIMDALL backend + SHUTTLE mutations + VERDANDI ANVIL view |
| **M4** | Demo-ready | 31 августа | MIMIR работает на 3+ demo queries, все компоненты интегрированы |
| **M5** | Demo-safe | 30 сентября | 5+ rehearsals без сбоев, `make demo-reset` < 5 сек, backup готов |
| **M6** | HighLoad++ | Октябрь 2026 | 🎯 |

---

## 2. Недельный план

### АПРЕЛЬ — Infrastructure sprint

#### Week 1 (12–18 апреля) — 🔴 СЕЙЧАС
**Приоритет: два блокера немедленно**

| Задача | Кто | Effort | Блокирует |
|---|---|---|---|
| **🔴 CFP submission** | оба | ~4 ч | M6 вообще |
| **C.2.1 SQL injection fix** | Track B | ~2 ч | prod safety |
| **C.0 Hound тесты → network mode** | Track A | ~1-3 дня | Dali engineering |
| Repo cleanup (секция 3 REPO_MIGRATION_PLAN) | оба | ~2 ч | чистый старт |
| Performance baseline: Hound + LOOM измерения | Track A | ~1 день | знаем откуда стартуем |

#### Week 2 (19–25 апреля) — Repo migration + Hound refactor
**M0 checkpoint конец недели**

| Задача | Кто | Effort |
|---|---|---|
| REPO_MIGRATION_PLAN шаги 1-7 (skeleton → frontends) | оба | ~3-4 дня |
| C.1 Hound library refactor (C.1.1-C.1.4) | Track A | ~4 дня |
| C.5.2 Keycloak client rename | Track B | ~1 ч |
| **Q5: Co-founder track split — зафиксировать** | оба | ~1 ч |
| REPO шаги 8-11 (Makefile, CI/CD, Docker Compose) | оба | ~2 дня |

**M0 критерий (25 апр):** `aida-root/` работает, `make build` проходит, CFP подан.

#### Week 3 (26 апреля – 2 мая) — Dali + HEIMDALL skeleton
**Конец апреля: решения Q7, Q31**

| Задача | Кто | Effort |
|---|---|---|
| Dali Core skeleton (Quarkus project, JobRunr setup) | Track A | ~3 дня |
| FRIGG StorageProvider scaffold (~200-400 LoC) | Track A | ~2 дня |
| HEIMDALL backend skeleton (Quarkus, event collector, ring buffer) | Track B | ~3 дня |
| C.2.2 SHUTTLE mutation resolvers (начало) | Track B | ~2 дня |
| **Q7: HEIMDALL ↔ Dali API формат — решить** | оба | end April |
| **Q31: UC2a preview (6α vs 6β) — решить** | оба | mid May |

---

### МАЙ — Core engines

#### Week 4-5 (3–16 мая)
**Q29/Q30 evaluation после C.0 (MIMIR/ANVIL architecture decisions)**

| Задача | Кто | Effort |
|---|---|---|
| Dali UC1 (scheduled harvest) end-to-end | Track A | ~1 нед |
| C.2.2-C.2.3 SHUTTLE mutations + subscriptions | Track B | ~1 нед |
| HEIMDALL backend: event schema + ring buffer полный | Track B | ~1 нед |
| **Q29: MIMIR через ArcadeDB MCP? — evaluate** | Track A | ~2-3 дня |
| **Q30: ANVIL через 72 built-in algorithms? — evaluate** | Track A | ~2-3 дня |
| **Q3: HEIMDALL backend — confirm deployment** | оба | mid May |
| **Q6: HEIMDALL event schema — закрыть** | Track B | mid May |
| **Q25: Event bus transport — закрыть** | оба | mid May |

#### Week 6-7 (17–31 мая)
**M2 checkpoint конец мая**

| Задача | Кто | Effort |
|---|---|---|
| Dali UC2b (event-driven) + UC3 (local) | Track A | ~1 нед |
| HEIMDALL WebSocket streaming | Track B | ~3 дня |
| C.2.4-C.2.6 SHUTTLE REST clients (Dali/MIMIR/HEIMDALL) | Track B | ~2 дня |
| Dali + Hound integration тест (in-JVM call) | Track A | ~2 дня |
| ANVIL backend skeleton | Track A | ~3 дня |

**M2 критерий (31 май):** `startParseSession` → Dali → Hound → YGG → SHUTTLE отдаёт lineage. UC1 + UC2b работают.

---

### ИЮНЬ — Integration sprint

#### Week 8-9 (1–14 июня)

| Задача | Кто | Effort |
|---|---|---|
| MIMIR backend (tool calling framework или ArcadeDB MCP wrapper — зависит от Q29) | Track B | ~2 нед |
| ANVIL backend: find_downstream_impact (Cypher или built-in — зависит от Q30) | Track A | ~1 нед |
| C.3 Chur: HEIMDALL proxy routes + scope validation | Track B | ~4 дня |
| **Q8: MIMIR язык — решить** | оба | по Q29 |
| **Q15: ClickHouse dialect — держим или droppable** | Track A | June |

#### Week 10-11 (15–30 июня)

| Задача | Кто | Effort |
|---|---|---|
| C.4.1 VERDANDI WebSocket client | Track B | ~1 день |
| C.4.2 VERDANDI ANVIL UI view | Track B | ~3-4 дня |
| HEIMDALL frontend skeleton (новый React repo) | Track B | ~3 дня |
| PostgreSQL dialect в Hound | Track A | ~1 нед |
| Performance optimization: LOOM 5-10K nodes | Track B | ~1 нед |
| Integration testing: VERDANDI → ANVIL end-to-end | оба | ~2 дня |

---

### ИЮЛЬ — Variable velocity (family constraints)

> ⚠️ Планируем более консервативно. Основной фокус: полировка и стабилизация того, что уже есть. Новые компоненты — только если velocity позволяет.

| Задача | Кто | Приоритет |
|---|---|---|
| HEIMDALL frontend: metrics dashboard | Track B | 🔴 must |
| Hound: edge cases + polish | Track A | 🔴 must |
| ANVIL integration с MIMIR | Track A+B | 🔴 must |
| LOOM split-screen layout (LOOM + HEIMDALL side-by-side) | Track B | 🔴 must |
| Performance baseline validation (все таргеты §7) | Track A | 🟡 should |
| Benchmark dataset финализация (500K LoC demo) | Track A | 🟡 should |
| C.4.4 VERDANDI subscription handler | Track B | 🟢 nice |
| ClickHouse dialect (если velocity есть) | Track A | 🟢 nice |

**M3 критерий (31 июль):** HEIMDALL показывает live events, ANVIL отвечает на impact queries, LOOM рендерит 5K+ nodes без лагов.

---

### АВГУСТ — Demo integration

#### Week 16-17 (1–15 августа)

| Задача | Кто | Effort |
|---|---|---|
| C.4.3 VERDANDI MIMIR Chat view | Track B | ~4-5 дней |
| MIMIR integration полная (Anthropic + Ollama fallback) | Track B | ~1 нед |
| Demo dataset подготовка (anonymized, 500K LoC) | Track A | ~3 дня |
| `make demo-reset` + `make demo-start` | оба | ~2 дня |
| Первые demo rehearsals (внутренние) | оба | ongoing |

#### Week 18-19 (16–31 августа)

| Задача | Кто | Effort |
|---|---|---|
| C.5.1-C.5.4 Infrastructure finalization | оба | ~3 дня |
| Integration testing end-to-end полный цикл | оба | ~1 нед |
| Demo script финализация | оба | ~2 дня |
| Pre-recorded demo video (ultimate fallback) | Track B | ~1 день |
| **MIMIR go/no-go: Option A (live) vs Option B (teaser)** | оба | **31 авг — HARD DEADLINE** |

> **MIMIR go/no-go rule:** если к 31 августа MIMIR не отвечает надёжно на 3+ demo queries в rehearsal — переходим в Option B (MIMIR как teaser в конце demo) и переписываем narrative. Честный plan, не wishful thinking.

**M4 критерий (31 авг):** все компоненты интегрированы, MIMIR decision принято, demo script готов.

---

### СЕНТЯБРЬ — Demo safety

**Цель месяца: 5+ rehearsals без единого сбоя.**

| Неделя | Фокус |
|---|---|
| Week 20 (1-7 сент) | Rehearsal 1-2: найти все баги на сцене |
| Week 21 (8-14 сент) | Bug fixes + polish по результатам R1-R2 |
| Week 22 (15-21 сент) | Rehearsal 3-4: тест backup сценариев (LLM offline, network down) |
| Week 23 (22-30 сент) | Final polish. Rehearsal 5. Freeze code. |

**Demo safety checklist (сентябрь):**
- [ ] `make demo-start` — один команд, < 30 сек
- [ ] `make demo-reset` — восстанавливает baseline, < 5 сек
- [ ] Tier 3 fallback: demo query cache работает при offline LLM
- [ ] Backup laptop с identical setup, weekly sync
- [ ] Pre-recorded video готово (ultimate fallback)
- [ ] API key ротирован (separate key only for demo)
- [ ] 5 rehearsals без сбоев ✓

**M5 критерий (30 сент):** всё вышеперечисленное. Code freeze.

---

## 3. Критический путь

```
🔴 CFP (сейчас!)
    │
    ▼
C.2.1 SQL injection  ──►  C.0 Hound тесты → network
                               │
                               ▼
                          C.1 Hound library refactor
                               │
                               ▼
                          Dali Core skeleton  ──►  HEIMDALL skeleton
                               │
                               ▼
                          Dali UC1+UC2b  ──►  SHUTTLE mutations (C.2.2-C.2.3)
                               │
                               ▼
                          MIMIR backend  ──►  ANVIL backend
                               │
                               ▼
                          C.4.3 MIMIR Chat view  ──►  Integration testing
                               │
                               ▼
                          MIMIR go/no-go (31 авг)
                               │
                               ▼
                          Demo rehearsals (сентябрь)
                               │
                               ▼
                          🎯 HighLoad++ (октябрь)
```

**Самые рискованные задачи (по убыванию риска):**

| # | Задача | Риск | Митигация |
|---|---|---|---|
| 1 | **MIMIR** | Самый неопределённый компонент | Option A/B decision trigger 31 авг |
| 2 | **LOOM 5-10K nodes** | Performance gap огромный (сейчас ~10 → нужно ~5K) | ELK Web Worker spike в апреле |
| 3 | **HEIMDALL** | Большой новый компонент, на critical path | Skeleton в апреле, не ждать мая |
| 4 | **Dali FRIGG StorageProvider** | Кастомный ~200-400 LoC, нет примеров | Писать сразу на ArcadeDB 26.x |
| 5 | **Demo dataset** | Нет anonymized 500K LoC источника | Определить источник в апреле |

---

## 4. Открытые решения — по срокам

| Срок | Вопрос | Статус |
|---|---|---|
| **Немедленно** | Q4: CFP submission | 🔴 БЛОКИРУЕТ |
| **Эта неделя** | Q5: Co-founder track split | 🔴 нужно |
| **Конец апреля** | Q7: HEIMDALL ↔ Dali API формат | 🟡 важно |
| **Mid-May** | Q3: HEIMDALL backend deployment | 🟡 важно |
| **Mid-May** | Q6: HEIMDALL event schema | 🟡 важно |
| **Mid-May** | Q25: Event bus transport (HTTP POST vs ...) | 🟡 важно |
| **Mid-May** | Q31: UC2a preview (6α vs 6β) | 🟡 важно |
| **May-June** | Q29: MIMIR через ArcadeDB MCP? | 🟡 evaluate |
| **May-June** | Q30: ANVIL через 72 built-in algorithms? | 🟡 evaluate |
| **June** | Q8: MIMIR язык (Python vs Java) | зависит Q29 |
| **31 августа** | MIMIR Option A (live) vs Option B (teaser) | 🔴 HARD |

---

## 5. Track split (когда будет зафиксирован)

По архитектурным документам предложено разделение:

**Track A — Data & Algorithms**
Hound, YGG/ArcadeDB, ANVIL backend, Dali Core, benchmarks, performance

**Track B — UI & Orchestration**
LOOM/KNOT/VERDANDI, HEIMDALL frontend, MIMIR, SHUTTLE, Chur, integration

> ⚠️ Персональное назначение Track A / Track B не зафиксировано — открытый вопрос Q5. Решить до 19 апреля.

---

## 6. Что откладывается (defer list, не трогать до HighLoad)

| Что | Когда |
|---|---|
| URD / SKULD (frontend + backend + middleware) | post-HighLoad |
| MUNINN, HUGINN, BIFROST | post-HighLoad |
| Storage Automation (второй продукт AIDA) | post-HighLoad |
| Production observability (Prometheus, Grafana, Loki, Jaeger) | post-HighLoad |
| Multi-tenancy, billing, SOC2 | commercial post-MVP |
| Cloud deployment | после first pilot customer |
| ygg.db fork с Arrow Flight | H2 2027+ |
| Горизонтальное масштабирование | post-HighLoad |

---

## История изменений

| Дата | Версия | Что |
|---|---|---|
| 12.04.2026 | 1.0 | Initial roadmap. Горизонт апрель–октябрь 2026. 6 milestones, недельный план, критический путь, MIMIR go/no-go rule (31 авг). Построен на ARCH_SPEC §8, ARCH_REVIEW §5, DECISIONS_LOG, REFACTORING_PLAN. |
