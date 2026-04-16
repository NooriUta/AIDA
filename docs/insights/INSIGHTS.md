# AIDA — Insights

**Документ:** `INSIGHTS`
**Версия:** 1.0
**Дата:** 16.04.2026
**Статус:** Living document — наблюдения из реальных данных, гипотезы, выводы для будущих инструментов

Этот документ фиксирует паттерны, обнаруженные при анализе YGG-данных на реальном корпусе.
Цель: подготовить фундамент для ANVIL (impact analysis) и MIMIR (LLM tool calling).

---

## §1 — Phantom Columns: фантомные колонки из реконструкции

**Дата обнаружения:** 16.04.2026
**Объект:** `CRM.CA_DATA_EXPORTS` (@rid `#10:252`)
**Связанный баг:** Позиции атомов INSERT column list → `docs/plans/BACKLOG.md §Backend`

---

### Наблюдение

Таблица `CRM.CA_DATA_EXPORTS` имеет `column_count: 13` (из DDL, сессия `session-1776277023201`).
В YGG существует **15 вершин DaliColumn** с `table_geoid = CRM.CA_DATA_EXPORTS`.

Две лишние колонки — **фантомные**:

| Колонка | @rid | data_type | data_source | ordinal_position | Сессия |
|---|---|---|---|---|---|
| `EXPORT_DATE` | `#13:213137` | `null` | `reconstructed` | 3 (коллизия!) | `session-1776277222732` |
| `ROW_COUNT` | `#13:213138` | `null` | `reconstructed` | 4 (коллизия!) | `session-1776277222732` |

Обе колонки добавлены из файла `ERP_CORE/PKG_ETL_07_ANALYTICS.sql` процедурой
`DWH.PKG_ETL_ANALYTICS:PROCEDURE:EXPORT_ANALYTICS_RESULTS` (INSERT строки 5570–5574).

Реконструированный INSERT:
```sql
-- строки 5570–5574 PKG_ETL_07_ANALYTICS.sql
INSERT INTO CRM.CA_DATA_EXPORTS
  (EXPORT_ID, EXPORT_TYPE, EXPORT_DATE, ROW_COUNT, STATUS)
VALUES
  (V_EXPORT_ID, P_EXPORT_TYPE, V_ANALYSIS_DT, 0, 'IN_PROGRESS');
```

---

### Диагностика

**Парсер отработал корректно.** Колонки `EXPORT_DATE` и `ROW_COUNT` явно написаны
в column list INSERT-а. Это не артефакт алгоритма — это реальные токены в исходном коде.

Проблема в другом: эти колонки **не существуют в DDL** таблицы.

**Дополнительный баг:** все 5 атомов INSERT column list имеют одинаковую позицию `5571:0`
вместо индивидуальных column offsets — баг position tracking в `enterColumn_list`.

---

### Гипотеза

Phantom columns возникают в трёх сценариях:

| Сценарий | Признаки | Вероятность для CRM.CA_DATA_EXPORTS |
|---|---|---|
| **A. Синтетический пакет** — автогенерированный SQL ссылается на несуществующие колонки | Нумерованный файл (`PKG_ETL_07`), DWH пишет в CRM-таблицу (cross-schema) | 🔴 Высокая |
| **B. Устаревший DDL** — колонки реальны, но DDL файл в корпусе не обновлён | Колонки отсутствуют во всех DDL-сессиях | 🟡 Средняя |
| **C. Ошибка в коде** — INSERT указывает несуществующие колонки | Код бы падал на prod | 🟢 Низкая |

---

### Вывод

Phantom columns — **валидный сигнал для code validation**. Когда `data_source = 'reconstructed'`
и колонка отсутствует в DDL-сессиях той же таблицы — это потенциальный дефект кода или неполнота корпуса.

---

### Применение для ANVIL / MIMIR

**ANVIL — Phantom Column Detector (будущая возможность):**

```
Запрос: найти все DaliColumn с data_source='reconstructed'
         где column_geoid НЕ существует ни в одной DDL-сессии той же таблицы

Cypher:
MATCH (c:DaliColumn {data_source: 'reconstructed'})
WHERE NOT EXISTS {
  MATCH (c2:DaliColumn)
  WHERE c2.table_geoid = c.table_geoid
    AND c2.column_name = c.column_name
    AND c2.data_source <> 'reconstructed'
}
RETURN c.table_geoid, c.column_name, c.used_in_statements
```

Результат: список `(таблица, колонка, [statement_geoid])` — потенциальные phantom columns по всему корпусу.

**MIMIR tool: `validate_columns(table_geoid)`**

```json
{
  "name": "validate_columns",
  "description": "Detect columns used in DML statements but absent in DDL for a given table",
  "parameters": {
    "table_geoid": "string"
  }
}
```

Ответ MIMIR: «В таблице `CRM.CA_DATA_EXPORTS` обнаружены 2 колонки (`EXPORT_DATE`, `ROW_COUNT`),
используемые в INSERT-ах (пакет `PKG_ETL_ANALYTICS`), но отсутствующие в DDL.
Вероятная причина: синтетический пакет или устаревшая схема.»

---

### Связанные объекты в YGG

| Объект | @rid / geoid |
|---|---|
| DaliTable | `#10:252` → `CRM.CA_DATA_EXPORTS` |
| DaliColumn EXPORT_DATE | `#13:213137` |
| DaliColumn ROW_COUNT | `#13:213138` |
| DaliStatement INSERT | `#25:4258` → `…EXPORT_ANALYTICS_RESULTS:INSERT:5570` |
| DaliRoutine | `#16:8419` → `DWH.PKG_ETL_ANALYTICS:PROCEDURE:EXPORT_ANALYTICS_RESULTS` |
| DaliSession | `#22:66` → `ERP_CORE/PKG_ETL_07_ANALYTICS.sql` |

---

## История изменений

| Дата | Версия | Что |
|---|---|---|
| 16.04.2026 | 1.0 | Создан. §1 Phantom Columns — CRM.CA_DATA_EXPORTS. Гипотеза + ANVIL/MIMIR применение. |
