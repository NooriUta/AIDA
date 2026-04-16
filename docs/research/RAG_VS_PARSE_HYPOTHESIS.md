# RAG vs Parse — Исследовательская гипотеза

**Документ:** `RAG_VS_PARSE_HYPOTHESIS`
**Версия:** 1.0
**Дата:** 16.04.2026
**Статус:** HYPOTHESIS — не протестирована
**Горизонт:** post-M3 (август 2026+)

---

## 1. Гипотеза

> «Сможет ли LLM с RAG (Retrieval-Augmented Generation) дать сравнимое
> качество column lineage за меньшее время разработки, чем детерминированный
> ANTLR4-парсер?»

---

## 2. Мотивация

### Что стоит Hound

| Артефакт | Стоимость |
|----------|-----------|
| ANTLR4 grammar (PL/SQL) | Сторонняя (open source) |
| PlSqlSemanticListener | 4 прохода, ~3 000 строк Java |
| Pass 4 PENDING resolution | Отдельный механизм |
| 19 KI-конструкций | 14 реализовано, 5 pending |
| Тестовый корпус | 203 файла, 70K LOC |
| **Итого потрачено** | **~6 недель инженерной работы** |

Для каждого нового диалекта (PostgreSQL, TSQL, Hive) — минимум **2–4 недели**.

### Что обещает LLM-подход

- Zero-shot или few-shot lineage extraction без написания grammar
- Горизонтальная масштабируемость на новые диалекты через промпт
- Потенциально: понимание комментариев, implicit business logic

---

## 3. Подходы для сравнения

### 3.1 Подход A: Детерминированный парсер (current — Hound)

```
SQL файл → ANTLR4 → AST → SemanticListener (4 passes) → DaliAtom[] → YGG
```

**Характеристики:**
- Resolution rate: **98.8%**
- Скорость: **548 атомов/сек** на одном потоке
- Детерминизм: **100%** (одинаковый вход → одинаковый выход)
- Стоимость инференса: $0 (локально, CPU)
- Разработка нового диалекта: 2–4 недели

### 3.2 Подход B: LLM с промптом (zero-shot)

```
SQL файл → chunking → LLM(prompt + chunk) → JSON lineage → DaliAtom[] → YGG
```

**Гипотезы:**
- Resolution rate на простых конструкциях: 80–90%?
- Resolution rate на сложных (BULK COLLECT, PIPE ROW): 50–70%?
- Скорость: зависит от токенов/сек (Anthropic Sonnet: ~200 tok/сек)
- Стоимость инференса: Sonnet-3.5 ≈ $3/M input tokens
- Разработка нового диалекта: 1–3 дня (промпт)

**Открытый вопрос:** Консистентность при повторном запросе одного файла.

### 3.3 Подход C: LLM + RAG (hybrid)

```
SQL файл + schema context (из YGG) → LLM → lineage JSON → DaliAtom[] → YGG
```

YGG используется как база знаний о существующих таблицах/колонках.
LLM не угадывает schema — получает её как контекст.

**Гипотеза:**
- Resolution rate: сравнимо с подходом A для стандартных конструкций
- Корость: медленнее из-за RAG retrieval + LLM latency

### 3.4 Подход D: Parse → LLM enrichment (cascade)

```
Hound (детерминированный) → PENDING atoms → LLM(focused prompt) → resolved atoms
```

LLM используется только для PENDING (1.2% атомов) — там, где парсер не справляется.
Применить к: Dynamic SQL, JSON_TABLE, nested records.

**Гипотеза:**
- Самый реалистичный путь к 99.9% resolution rate
- Стоимость инференса минимальна (только на PENDING)

---

## 4. Метрики для сравнения

| Метрика | Вес | Метод измерения |
|---------|-----|-----------------|
| Resolution rate | Высокий | % корректно разрешённых column refs |
| Precision | Высокий | False positive lineage edges |
| Recall | Высокий | Missed lineage edges |
| Детерминизм | Средний | Δ при 5 повторных запросах одного файла |
| Скорость | Средний | Атомов/сек |
| Стоимость | Средний | $/1000 файлов |
| Время разработки нового диалекта | Средний | Дни |

---

## 5. Тестовый план

### Фаза 1: Benchmark (2 недели)

1. Взять **50 PL/SQL файлов** из корпуса HR schema
2. Запустить Hound → получить эталонный lineage (Ground Truth)
3. Запустить LLM zero-shot (Claude Sonnet 3.5) на тех же файлах
4. Вычислить precision/recall относительно эталона

### Фаза 2: RAG augmentation (2 недели)

5. Добавить schema context из YGG (таблицы + колонки) в промпт
6. Измерить delta precision/recall vs zero-shot

### Фаза 3: Cascade (1 неделя)

7. Взять PENDING атомы из Hound (1.2% = ~1 700 атомов)
8. Отправить в LLM с узким промптом
9. Измерить resolution rate PENDING → resolved

### Критерий продолжения

| Результат | Решение |
|-----------|---------|
| LLM precision > 95% на базовых конструкциях | Продолжить — разработать MIMIR tools |
| LLM precision 80–95% | Cascade only (подход D) |
| LLM precision < 80% | Отказаться от LLM для lineage |

---

## 6. Архитектурные последствия

### Если LLM viable (≥ 95% precision)

- MIMIR получает инструмент `extract_lineage(sql_chunk)` → производит DaliAtom[]
- Hound остаётся для performance-critical парсинга
- Новые диалекты: промпт-first, парсер-fallback

### Если только Cascade viable

- MIMIR получает инструмент `resolve_pending_atoms(pending_list, schema_context)`
- Hound остаётся основным инструментом
- ~0.2–0.5% прирост resolution rate

### Если LLM не годится для lineage

- LLM ограничен: `askMimir` (NL вопросы), поиск по графу
- Lineage: только детерминированный Hound

---

## 7. Зависимости

| Зависимость | Статус |
|-------------|--------|
| MIMIR service spec | `docs/plans/MIMIR_SPEC.md` — DRAFT |
| Anthropic API ключ | Нужен для тестирования |
| Ollama + Qwen (local) | Альтернатива для privacy-sensitive корпусов |
| Q29: ArcadeDB MCP Server | Влияет на архитектуру MIMIR (re-eval май 2026) |

---

## История изменений

| Дата | Версия | Что |
|------|--------|-----|
| 16.04.2026 | 1.0 | HYPOTHESIS. 4 подхода. Метрики. Тестовый план 5 недель. Критерии принятия решения. |
