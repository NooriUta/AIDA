# Performance Targets — AIDA

**Документ:** `PERFORMANCE_TARGETS`
**Версия:** 1.0
**Дата:** 16.04.2026
**Статус:** ACTIVE
**Горизонт:** M2 (апрель 2026) → M3 (июль 2026)

---

## 1. Текущий baseline (измерено 15.04.2026)

| Метрика | Значение | Условия |
|---------|----------|---------|
| Корпус | 203 PL/SQL файла | HR schema, ~70K LOC |
| Атомов извлечено | 143 000 | clearBeforeWrite=false |
| Resolution rate | **98.8%** | 141 588 resolved / 1 412 pending |
| Время полного парсинга | **261 сек** (4.4 мин) | 4 worker threads, REMOTE_BATCH |
| Пропускная способность | ~548 атомов/сек | Один поток записи в YGG |
| Вершин в YGG итого | ~300 000 | DaliTable + DaliColumn + DaliRoutine + … |
| Batch size (Hound) | 500 вершин | JsonlBatchBuilder |
| Worker threads (Dali) | 4 | `dali.jobrunr.worker-threads=4` |

---

## 2. LOOM (React Flow ELK layout) — baseline

Данные из `docs/architecture/loom/LOOM_PERFORMANCE_ANALYSIS.md`:

| Сценарий | Граф | ELK время | React render |
|----------|------|-----------|--------------|
| L1 schema overview | ~50N / ~100E | <100 ms | <50 ms |
| L2 routine aggregate (типичный) | 90–150N / 200–400E | 100–300 ms | <100 ms |
| L3 exploration (типичный) | 42–55N / 80–150E | 100–300 ms | <100 ms |
| L4 statement drill | 20–40N / 30–80E | <100 ms | <50 ms |
| L3 worst case | 430N / 709E | **524 ms** (cache) / **1 800 ms** (cold) | 200 ms |
| L2 pathological | 1600N / 17 000E | **15–60 сек** | 2–4 сек |

---

## 3. Цели (M2 — апрель 2026)

### 3.1 Парсинг (Dali + Hound)

| SLO | Метрика | Цель M2 | Текущее |
|-----|---------|---------|---------|
| P-1 | Resolution rate на PL/SQL корпусе | ≥ 98% | ✅ 98.8% |
| P-2 | Время парсинга 200 файлов / 70K LOC | ≤ 360 сек | ✅ 261 сек |
| P-3 | Preview-сессия (parse без записи в YGG) | ≤ 120 сек | ⏳ не измерено |
| P-4 | Сессия не теряет прогресс при restart | restart → FAILED + lог | ✅ |

### 3.2 API / Backend latency

| SLO | Эндпойнт | Цель p95 | Измерено |
|-----|----------|----------|---------|
| A-1 | `POST /api/sessions` (Dali) | ≤ 200 ms | ⏳ |
| A-2 | `GET /api/sessions/{id}` | ≤ 50 ms | ⏳ |
| A-3 | SHUTTLE GraphQL `startParseSession` | ≤ 500 ms | ⏳ |
| A-4 | SHUTTLE GraphQL `exploreSchema` | ≤ 1 000 ms | ⏳ |
| A-5 | SHUTTLE GraphQL `lineage(schema)` (10K атомов) | ≤ 1 000 ms | ⏳ |

### 3.3 LOOM Frontend

| SLO | Сценарий | Цель | Текущее |
|-----|----------|------|---------|
| L-1 | L1 schema overview (50N) | ≤ 200 ms (ELK + render) | ✅ |
| L-2 | L2 routine aggregate (< 200N / < 500E) | ≤ 500 ms | ✅ |
| L-3 | L3 exploration (< 100N / < 200E) | ≤ 500 ms | ✅ |
| L-4 | L4 statement drill (< 50N) | ≤ 200 ms | ✅ |
| L-5 | L3 worst case (430N / 709E) | ≤ 2 000 ms cold | ⚠️ 1 800 ms |
| L-6 | Time-to-first-render LOOM при открытии | ≤ 1 500 ms | ⏳ |

### 3.4 Надёжность

| SLO | Условие | Цель |
|-----|---------|------|
| R-1 | DALI при недоступном FRIGG — SHUTTLE graceful | SHUTTLE возвращает `status=UNAVAILABLE`, не 500 | ✅ |
| R-2 | SHUTTLE при недоступном DALI | `SessionInfo{status=UNAVAILABLE}` | ✅ |
| R-3 | Concurrent сессии (2 preview) | Обе завершаются COMPLETED | ⏳ |
| R-4 | HEIMDALL event stream при >1000 событий/сек | Ring buffer без OOM | ⏳ |

---

## 4. Цели (M3 — июль 2026)

### 4.1 PostgreSQL dialect

| SLO | Метрика | Цель M3 |
|-----|---------|---------|
| PG-1 | Resolution rate PostgreSQL корпус (базовые конструкции) | ≥ 90% |
| PG-2 | Время парсинга аналогичного корпуса | ≤ PL/SQL × 1.3 |

### 4.2 Масштабируемость

| SLO | Условие | Цель M3 |
|-----|---------|---------|
| S-1 | 1 000 файлов PL/SQL (~400K LOC) | ≤ 30 мин, resolution rate ≥ 97% |
| S-2 | YGG: 1M вершин | `lineage(schema)` ≤ 2 сек |
| S-3 | 5 concurrent parse sessions | Нет деградации rate |

### 4.3 LOOM pathological graphs

| SLO | Условие | Цель M3 |
|-----|---------|---------|
| L-7 | 1600N / 17K edges (pathological L2) | ≤ 5 сек (M-1+M-3 optimizations) |
| L-8 | Виртуализация (ReactFlow v12 + VegaRenderer) | Активирована при >500N |

---

## 5. Что НЕ измеряется (scope out)

- k8s / production latency (C.5.1 not started)
- Multi-tenant isolation (C.3 not started)
- ANVIL impact analysis (not implemented)
- MIMIR LLM (not implemented)

---

## 6. Как измерять

### 6.1 Время парсинга

```bash
# Через DALI API — поле durationMs в ответе
curl http://localhost:19090/api/sessions/<id> | jq '.durationMs'

# Через HEIMDALL events — SESSION_COMPLETED event содержит duration
curl -N http://localhost:19093/heimdall/events
```

### 6.2 Resolution rate

```bash
curl http://localhost:19090/api/sessions/<id> | jq '.resolutionRate'

# Прямо из YGG
curl -X POST "http://localhost:2480/api/v1/command/hound" \
  -u root:playwithdata -H "Content-Type: application/json" \
  -d '{"command":"SELECT count(*) as total, count(case when resolved=true then 1 end) as resolved FROM DaliAtom"}'
```

### 6.3 API latency

```bash
# p95 через curl + time (базовый способ)
for i in $(seq 20); do
  time curl -s http://localhost:19090/api/sessions/health > /dev/null
done 2>&1 | grep real | awk '{print $2}' | sort -n | tail -2
```

### 6.4 LOOM ELK время

DevTools Performance вкладка:
1. Открыть L3 граф (PKG_ORDERS → proc_create_order)
2. Profile → record → click on node → stop
3. Найти `ElkWorker.postMessage` duration

---

## История изменений

| Дата | Версия | Что |
|------|--------|-----|
| 16.04.2026 | 1.0 | Создан. Baseline 15.04.2026. SLO P/A/L/R/S для M2 и M3. Команды измерения. |
