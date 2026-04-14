# Verdandi Correction Sprint — 14.04.2026

**Ветка:** `fix/verdandi-correction-apr14-2026`
**Охват:** SHUTTLE (`services/shuttle/`) + Docs (`docs/sprints/`)
**Дата:** 14.04.2026
**Статус:** ✅ COMPLETE (BUG-VC-001–008)

**Причина:** После рефакторинга Dali/Hound (commits 3849730, ef248cd) — добавление
ConstraintInfo, DaliPrimaryKey/DaliForeignKey vertex-типов и новых edge-типов —
ExploreService и LineageService стали возвращать неизвестные для фронтенда типы нод.
Дополнительно выявлен потенциальный баг в `exploreStmtColumns` (id IN $ids vs UNWIND).

---

## Bug Log

| ID | Severity | Компонент | Файл:строка | Описание | Статус |
|----|----------|-----------|-------------|----------|--------|
| BUG-VC-001 | CRITICAL | SHUTTLE / ExploreService | `ExploreService.java:447-475` | `exploreByRid` — открытый паттерн `MATCH (n)-[r]->(m)` возвращает DaliPrimaryKey/DaliForeignKey ноды при клике на таблицу с PK/FK → фронтенд получает неизвестный тип → canvas crashes | ✅ |
| BUG-VC-002 | CRITICAL | SHUTTLE / LineageService | `LineageService.java` | `lineage`/`upstream`/`downstream` — аналогичный открытый паттерн возвращает constraint-ноды в lineage-граф | ✅ |
| BUG-VC-003 | HIGH | SHUTTLE / ExploreService | `ExploreService.java:307-358` | `exploreStmtColumns` — переписан с `id(t) IN $ids` (LINK type-mismatch) на 3 параллельных UNWIND-запроса | ✅ |
| BUG-VC-004 | HIGH | SHUTTLE / ExploreService | `ExploreService.java:357` | `exploreStmtColumns` добавлен вызов `enrichDataSource` → DaliTable ноды получают `dataSource` → badge отображается | ✅ |
| BUG-VC-005 | MEDIUM | Docs | `SHUTTLE_QUERY_REFERENCE.md` | Документ обновлён: 10 веток exploreSchema, 8 параллельных exploreByRid, добавлены разделы stmtColumns, expandDeep, exploreByDatabase, constraint-фильтрация | ✅ |
| BUG-VC-006 | CRITICAL | SHUTTLE / ExploreService | `ExploreService.java:stmtColumns` | `exploreStmtColumns` — нет HAS_COLUMN рёбер в ArcadeDB (cnt=0); DaliColumn привязан к DaliTable через `col.table_geoid = t.table_geoid`; ArcadeDB не поддерживает chained MATCH без `WITH`-сепаратора → переписан на UNWIND+WITH+property lookup | ✅ |
| BUG-VC-007 | HIGH | SHUTTLE / ExploreService | `ExploreService.java:exploreByRid` | `exploreByRid` — 50858 DaliAtom-нод в ArcadeDB (ANTLR4 atom analysis) возвращались через HAS_ATOM рёбра → canvas засорялся atom-нодами | ✅ |
| BUG-VC-008 | HIGH | SHUTTLE / LineageService | `LineageService.java` | `lineage`/`upstream`/`downstream` — DaliAtom-ноды появлялись через ATOM_REF_COLUMN рёбра в lineage-графе → добавлен фильтр `OR m:DaliAtom` во все 4 паттерна | ✅ |

---

## Bug Details

### BUG-VC-001 — exploreByRid возвращает DaliPrimaryKey/DaliForeignKey (CRITICAL)

**Контекст:** Commits 3849730/ef248cd добавили в `RemoteSchemaCommands.propertyCommands()`
новые vertex-типы и edge-типы для constraint-модели:

```
DaliTable ──HAS_PRIMARY_KEY──► DaliPrimaryKey (extends DaliConstraint)
DaliTable ──HAS_FOREIGN_KEY──► DaliForeignKey (extends DaliConstraint)
DaliPrimaryKey ──IS_PK_COLUMN──► DaliColumn
DaliForeignKey ──IS_FK_COLUMN──► DaliColumn
DaliForeignKey ──REFERENCES_TABLE──► DaliTable
DaliForeignKey ──REFERENCES_COLUMN──► DaliColumn
```

**Причина поломки:** `ExploreService.exploreByRid()` использует открытый паттерн:
```cypher
MATCH (n)-[r]->(m) WHERE id(n) = $rid
```
Это возвращает ВСЕ исходящие рёбра, включая HAS_PRIMARY_KEY и HAS_FOREIGN_KEY.
Фронтенд получает ноды с `type = "DaliPrimaryKey"` / `"DaliForeignKey"`,
которых нет в `DaliNodeType` в `domain.ts`. Результат: ошибка рендеринга canvas.

**Кейс-трейс:**
- Открыть CRM schema → L2 → `exploreSchema` (safe, только известные edge-типы) → OK
- Кликнуть на CRM.CUSTOMERS (таблица с PK+FK) → `exploreByRid` → returns DaliPrimaryKey → CRASH

**Фикс:** Добавить `AND NOT (m:DaliConstraint OR m:DaliPrimaryKey OR m:DaliForeignKey)`
в оба паттерна (`outQ` и `inQ`) в `exploreByRid`.

---

### BUG-VC-002 — lineage/upstream/downstream возвращают constraint-ноды (CRITICAL)

Аналогичная проблема в `LineageService` — открытые Cypher-паттерны для lineage
могут вернуть DaliPrimaryKey/DaliForeignKey через IS_PK_COLUMN / IS_FK_COLUMN рёбра
при обходе DaliColumn-нод с PK/FK.

**Фикс:** Добавить аналогичный фильтр в Cypher-запросы lineage/upstream/downstream.

---

### BUG-VC-003 — exploreStmtColumns: `id(t) IN $ids` type-mismatch (HIGH)

**Противоречие в коде:**
- `enrichDataSource` (строки 621-627) явно объясняет: *"ArcadeDB Cypher type-mismatch:
  id() returns a LINK type in WHERE context, which does not compare equal to String list
  elements in the IN operator."* Поэтому используется UNWIND.
- `exploreStmtColumns` (строка 299) утверждает обратное: *"ArcadeDB Cypher supports
  id(n) IN $list where $list is a Java List<String>."*

Если `enrichDataSource` прав — `exploreStmtColumns` возвращает 0 строк для всех трёх
веток (HAS_COLUMN, HAS_OUTPUT_COL, HAS_AFFECTED_COL) → колонки не отображаются в L2.

**Фикс:** Переписать на 3 параллельных Uni-запроса с UNWIND — убирает неоднозначность
и одновременно решает BUG-VC-004.

---

### BUG-VC-004 — exploreStmtColumns: нет enrichDataSource (HIGH)

`exploreStmtColumns` возвращает `buildResult(...)` без `.flatMap(this::enrichDataSource)`.
Все DaliTable-ноды в результате имеют `dataSource = ""` → badge в TableNode.tsx не
показывается для таблиц, загруженных через второй проход.

**Фикс:** Включается в общий рефакторинг BUG-VC-003 (добавить `enrichDataSource`).

---

### BUG-VC-006 — exploreStmtColumns: HAS_COLUMN рёбра отсутствуют + ArcadeDB chained MATCH (CRITICAL)

**Выявлено:** Колонки в L2 LOOM не отображались несмотря на фикс BUG-VC-003.

**Корневые причины:**

1. **HAS_COLUMN рёбра отсутствуют в ArcadeDB** — `SELECT count(*) FROM HAS_COLUMN → {"cnt":0}`.
   DaliColumn привязан к DaliTable через свойство `col.table_geoid = t.table_geoid` (тот же
   путь, что использует `KnotService`), а не через рёбра.

2. **ArcadeDB Cypher не поддерживает chained MATCH** — паттерн
   `MATCH (t:DaliTable) WHERE id(t) = rid / MATCH (t)-[:REL]->(m)` возвращает пустой
   результат. Требуется разделение через `WITH t` между двумя MATCH-клаузами.

**Фикс:** `hasColQ` переписан на:
```cypher
UNWIND $ids AS rid
MATCH (t:DaliTable)
WHERE id(t) = rid
WITH t
MATCH (col:DaliColumn)
WHERE col.table_geoid = t.table_geoid
RETURN id(t) AS srcId, t.table_name AS srcLabel, 'DaliTable' AS srcType,
       id(col) AS tgtId, coalesce(col.column_name, '') AS tgtLabel, '' AS tgtScope,
       'DaliColumn' AS tgtType, 'HAS_COLUMN' AS edgeType
LIMIT 2000
```

`hasOutColQ` и `hasAffColQ` также переписаны с `WITH`-сепаратором. Добавлен `.flatMap(enrichDataSource)`.

**Верификация:** `stmtColumns(ids:["#10:6394"])` → 22 DaliColumn + 22 HAS_COLUMN edges + `dataSource: "master"` ✓

---

### BUG-VC-007 — exploreByRid возвращает DaliAtom-ноды (HIGH)

**Контекст:** В ArcadeDB 50,858 DaliAtom-нод (ANTLR4 atom analysis из commits ef248cd).

**Причина поломки:** `exploreByRid` использует открытый паттерн:
```cypher
MATCH (n)-[r]->(m) WHERE id(n) = $rid
```
DaliStatement → DaliAtom через HAS_ATOM рёбра. Frontend `transformGqlExplore` не фильтровал
DaliAtom (`NODE_TYPE_MAP: DaliAtom → 'atomNode'`). HAS_ATOM не был в `SUPPRESSED_EDGES`.
Результат: canvas засорялся "atom"-нодами.

**Фикс (backend):** Добавлен `OR m:DaliAtom` в outQ и inQ фильтр `exploreByRid`.
**Фикс (frontend):** `transformExplore.ts` — добавлены `HAS_ATOM`, `ATOM_REF_STMT`,
`ATOM_REF_OUTPUT_COL` в `SUPPRESSED_EDGES`; добавлен `n.type !== 'DaliAtom'` в фильтр нод
`transformGqlExplore`.

---

### BUG-VC-008 — LineageService возвращает DaliAtom-ноды (HIGH)

**Причина:** `lineage`/`upstream`/`downstream` паттерны в `LineageService.java` фильтровали
`DaliConstraint/DaliPrimaryKey/DaliForeignKey` но не `DaliAtom`. DaliAtom попадал через
ATOM_REF_COLUMN рёбра (DaliAtom → DaliColumn).

**Верификация до фикса:** `lineage(nodeId: "#13:43")` (COUNTRY_ID PK-колонка) → 11 DaliAtom узлов.

**Фикс:** Добавлен `OR m:DaliAtom` в фильтр всех 4 Cypher-паттернов (`outQ`, `inQ`, `upstream`, `downstream`).

**Верификация после:** `lineage(nodeId: "#13:43")` → 0 DaliAtom, только DaliColumn/DaliStatement/DaliOutputColumn ✓

---

### BUG-VC-005 — SHUTTLE_QUERY_REFERENCE.md устарел (MEDIUM)

Документ написан 2026-04-06 и не отражает:
- exploreSchema: 10 веток (убраны HAS_COLUMN/HAS_OUTPUT_COL/HAS_AFFECTED_COL из Фазы 3)
- exploreByRid: 8 параллельных запросов (добавлены hoistReadsQ, hoistWritesQ)
- Новые методы: `stmtColumns`, `expandDeep`, `exploreByDatabase`
- Constraint-фильтрация в ExploreService и LineageService

**Фикс:** Полное обновление документа.

---

## Верификация

| Тест | Результат |
|------|-----------|
| `./gradlew :services:shuttle:test` | ✅ BUILD SUCCESSFUL |
| `stmtColumns(ids:["#10:6394"])` (DIM_CUSTOMER) | ✅ 22 DaliColumn + 22 HAS_COLUMN edges + `dataSource:"master"` |
| `stmtColumns(ids:["#10:13"])` (CRM.COUNTRIES) | ✅ 13 DaliColumn + 13 HAS_COLUMN edges |
| `stmtColumns(stmtIds)` DWH (DaliStatement IDs) | ✅ HAS_OUTPUT_COL + HAS_AFFECTED_COL edges |
| `explore(scope:"rid-#10:6394")` (DIM_CUSTOMER) | ✅ BAD nodes: 0 (нет DaliAtom/DaliPrimaryKey) |
| `explore(scope:"rid-#10:13")` (CRM.COUNTRIES с PK) | ✅ BAD nodes: 0 |
| `lineage(nodeId:"#13:43")` (COUNTRY_ID PK-колонка) | ✅ BAD nodes: 0 после фикса BUG-VC-008 |
| `explore(scope:"schema-DWH")` | ✅ BAD nodes: 0 (только DaliSchema/Package/Routine/Statement) |
| KNOT detail panel | isPk/isFk/isRequired через knotTableDetail SQL (не затронуто) |
