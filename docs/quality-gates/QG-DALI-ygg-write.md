---
name: QG-DALI-ygg-write
description: "Качественный гейт: парсинг с preview=false записывает данные в YGG и HEIMDALL их видит. Запускай при: 'граф пустой', 'DaliTable пуст', 'atomsExtracted=0', изменениях в ParseJob.java или HoundConfig, перед demo-rehearsal. Запускай циклически после каждого прогона парсинга."
---

# QG-DALI-ygg-write

Проверяет полный путь: Dali ParseJob → Hound REMOTE_BATCH → YGG → HEIMDALL metrics.
После сессии с `preview=false` в YGG должны быть вертексы, HEIMDALL должен видеть атомы.

> ⚠️ **Перед запуском QG**: выполни Фазу 0 (FRIGG startup + edge types check).
> Пропуск Фазы 0 приводит к ложному FAILED из-за missing edge types, а не логики парсера.
> Детали: `docs/guides/STARTUP_SEQUENCE.md`

## Что ты делаешь при запуске

---

## Фаза 0 — FRIGG startup + Edge types check (НОВОЕ — 15.04.2026)

**Шаг 0.1** Проверить что FRIGG healthy (иначе Dali не стартует):

```bash
docker compose ps frigg
# Ожидается: STATUS = "healthy"
# Если не healthy → docker compose up -d frigg && sleep 15
```

**Шаг 0.2** Проверить что Dali стартовал с JobRunr (не завис в schemaReady deadlock):

```bash
curl -sf http://localhost:9090/api/sessions/health | python3 -c \
  "import sys,json; d=json.load(sys.stdin); print('Dali:', d)"
# Ожидается: {"frigg":"ok","sessions":0,...}
# Если зависание → см. docs/guides/STARTUP_SEQUENCE.md §3
```

**Шаг 0.3** Проверить наличие Sprint 2 edge types в YGG (критично!):

```bash
curl -s -X POST http://localhost:2480/api/v1/command/hound \
  -u root:playwithdata -H "Content-Type: application/json" \
  -d '{"language":"sql","command":"SELECT name FROM schema:types WHERE type = '\''EDGE'\''"}' \
  | python3 -c "
import sys, json
types = [r['name'] for r in json.load(sys.stdin).get('result', [])]
needed = ['HAS_RECORD_FIELD','RETURNS_INTO','DaliDDLModifiesTable','DaliDDLModifiesColumn']
for t in needed:
    print(f'{t}: {\"✅\" if t in types else \"❌ MISSING — выполни Фикс-ET\"}')"
```

Если есть ❌ → выполни **Фикс-ET** (Edge Types):

```bash
for EDGE_TYPE in HAS_RECORD_FIELD RETURNS_INTO DaliDDLModifiesTable DaliDDLModifiesColumn; do
  curl -s -X POST http://localhost:2480/api/v1/command/hound \
    -u root:playwithdata -H "Content-Type: application/json" \
    -d "{\"language\":\"sql\",\"command\":\"CREATE EDGE TYPE ${EDGE_TYPE} IF NOT EXISTS\"}"
  echo "Created ${EDGE_TYPE}"
done
```

После Фикс-ET → повторить Шаг 0.3, убедиться что все ✅, затем продолжить Фазу 1.

---

## Фаза 1 — Аудит buildConfig()

Открой `services/dali/src/main/java/**/ParseJob.java`. Найди метод `buildConfig()`.

Проверь логику условия:

**INV-1** `preview=true` → `ArcadeWriteMode.DISABLED`
**INV-2** `preview=false` → `ArcadeWriteMode.REMOTE_BATCH`

```bash
grep -A 15 "buildConfig\|ArcadeWriteMode" \
  services/dali/src/main/java/**/ParseJob.java
```

Если логика инвертирована или отсутствует → перейди к **FIX-A**, повтори фазу.

Добавь диагностический лог если его нет:
```java
Log.infof("[ParseJob] sid=%s preview=%s writeMode=%s clear=%s",
    session.getId(), session.isPreview(), config.writeMode(), session.isClearBeforeWrite());
```

---

## Фаза 2 — YGG доступен

```bash
curl -sf -u root:playwithdata http://localhost:2480/api/v1/server \
  | python3 -c "import sys,json; d=json.load(sys.stdin); print('YGG', d.get('version','?'))"
```
Если недоступен → `docker compose up -d HoundArcade`, подождать 5 сек.

Проверь что database `hound` существует:
```bash
curl -sf -u root:playwithdata http://localhost:2480/api/v1/server \
  | python3 -c "import sys,json; \
    dbs=json.load(sys.stdin).get('databases',[]); \
    print('hound ✅' if 'hound' in dbs else 'hound ❌ → нужно создать')"
```
Если database `hound` нет:
```bash
curl -X POST http://localhost:2480/api/v1/server \
  -u root:playwithdata -H "Content-Type: application/json" \
  -d '{"command":"create database hound"}'
```

---

## Фаза 3 — E2E прогон

**Шаг 3.1** Запусти сессию с `preview=false, clearBeforeWrite=true`:
```bash
SID=$(curl -s -X POST http://localhost:9090/api/sessions \
  -H "Content-Type: application/json" \
  -d '{"preview":false,"clearBeforeWrite":true}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['sessionId'])")
echo "SID=$SID"
```

**Шаг 3.2** Polling до COMPLETED (5 мин максимум, опрос каждые 5 сек):
```bash
for i in $(seq 1 60); do
  STATUS=$(curl -s http://localhost:9090/api/sessions/$SID \
    | python3 -c "import sys,json; print(json.load(sys.stdin).get('status','?'))")
  echo "[$i/60] status=$STATUS"
  [ "$STATUS" = "COMPLETED" ] && break
  [ "$STATUS" = "FAILED" ] && echo "FAILED ❌ → §FIX-B" && break
  sleep 5
done
```

**Шаг 3.3** Прочитай лог Dali — убедись что есть строка `writeMode=REMOTE_BATCH`.

---

## Фаза 4 — Верификация данных

**INV-3** `DaliTable count > 0` в YGG:
```bash
curl -s -X POST http://localhost:2480/api/v1/query/hound \
  -u root:playwithdata -H "Content-Type: application/json" \
  -d '{"language":"sql","command":"SELECT count(*) as cnt FROM DaliTable"}' \
  | python3 -c "import sys,json; \
    cnt=json.load(sys.stdin)['result'][0]['cnt']; \
    print(f'DaliTable={cnt}'); print('✅' if cnt>0 else '❌ → FIX-A')"
```

**INV-4** `DaliAtom count > 0`:
```bash
curl -s -X POST http://localhost:2480/api/v1/query/hound \
  -u root:playwithdata -H "Content-Type: application/json" \
  -d '{"language":"sql","command":"SELECT count(*) as cnt FROM DaliAtom"}' \
  | python3 -c "import sys,json; \
    cnt=json.load(sys.stdin)['result'][0]['cnt']; \
    print(f'DaliAtom={cnt}'); print('✅' if cnt>0 else '❌ → FIX-A')"
```

**INV-5** Нет routine-дубликатов (должно быть 0):
```bash
curl -s -X POST http://localhost:2480/api/v1/query/hound \
  -u root:playwithdata -H "Content-Type: application/json" \
  -d '{"language":"sql","command":"SELECT count(*) as cnt FROM DaliAtom WHERE source_type = '\''routine'\''"}' \
  | python3 -c "import sys,json; \
    cnt=json.load(sys.stdin)['result'][0]['cnt']; \
    print(f'routine duplicates={cnt}'); print('✅' if cnt==0 else '❌ → FIX-C')"
```

**INV-6** HEIMDALL metrics обновились:
```bash
curl -s http://localhost:9093/metrics/snapshot \
  | python3 -c "import sys,json; d=json.load(sys.stdin); \
    atoms=d.get('atomsExtracted',0); rate=d.get('resolutionRate',0.0); \
    print(f'atomsExtracted={atoms} resolutionRate={rate:.1%}'); \
    print('atoms ✅' if atoms>0 else 'atoms ❌'); \
    print('rate ✅' if rate>0.0 else 'rate ❌')"
```

---

## FIX-A — buildConfig() логика

```java
// ParseJob.java — buildConfig()
private HoundConfig buildConfig(Session session) {
    ArcadeWriteMode mode = session.isPreview()
        ? ArcadeWriteMode.DISABLED      // preview=true → только анализ
        : ArcadeWriteMode.REMOTE_BATCH; // preview=false → писать в YGG
    return HoundConfig.builder()
        .writeMode(mode)
        .arcadeUrl(yggUrl).arcadeDatabase(yggDb)
        .arcadeUser(yggUser).arcadePassword(yggPassword)
        .build();
}
```

## FIX-B — Сессия FAILED

Прочитай лог Dali. Типичные причины:
- `yggUrl` неверный → проверь `application.properties: ygg.url=${YGG_URL:http://localhost:2480}`
- database `hound` не создана → см. Фаза 2
- SQL файлы не найдены → проверь `dali.sources.path`

## FIX-C — Routine дубликаты

Guard должен быть в 4 местах. Открой каждый файл и добавь если отсутствует:
```java
// JsonlBatchBuilder.java pool mode (~490), ad-hoc mode (~610)
// RemoteWriter.java write() (~310)
// HoundParserImpl.java toParseResult() (~233)
String sourceType = (String) cont.get("source_type");
if ("routine".equals(sourceType)) continue;  // дублирующий вид — пропустить
```

---

## Обновление истории

| Дата | buildConfig | DaliTable | DaliAtom | Duplicates | HEIMDALL | Итог |
|---|---|---|---|---|---|---|
| 13.04.2026 | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ не проверялось |
| 15.04.2026 | ✅ | ✅ | ✅ (143K) | ✅ | ✅ | ✅ GREEN — 98.8% resolution |
| 16.04.2026 | ✅ | ⏳ сервисы не запущены | ⏳ | ⏳ | ⏳ | ✅ PASS (static) — no code changes since 15.04 |

**15.04.2026 прогон:** 203 файла, 143K атомов, 261s, resolution 98.8%. Выявлено 2 проблемы:
- Missing edge types Sprint 2 (HAS_RECORD_FIELD etc.) → Фикс-ET добавлен в Фазу 0
- FRIGG startup race condition → зафиксировано в STARTUP_SEQUENCE.md
