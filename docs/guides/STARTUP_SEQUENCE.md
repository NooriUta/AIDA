# AIDA — Startup Sequence & Known Issues

**Документ:** `STARTUP_SEQUENCE`
**Версия:** 1.0
**Дата:** 16.04.2026
**Статус:** ✅ ACTIVE — критично для demo-rehearsal
**Источник:** QG прогон 15.04.2026

> ⚠️ Этот документ содержит **три известных проблемы**, которые воспроизвелись на QG-прогоне 15.04.
> Следуй секции по порядку. Отклонение от него приведёт к зависшему Dali или пустому YGG.

---

## 1. Правильный порядок запуска (CRITICAL)

### Rule 1: FRIGG должен быть healthy ДО Dali

Dali при старте регистрирует JobRunr на `ArcadeDbStorageProvider` → читает/пишет FRIGG.
Если FRIGG не готов → Dali падает с `schemaReady deadlock` и не поднимается автоматически.

**В docker-compose уже настроено:**

```yaml
dali:
  depends_on:
    frigg:
      condition: service_healthy   # ← обязательно service_healthy, не service_started
```

**Проверь вручную перед запуском:**

```bash
# Убедись что frigg healthy
docker compose ps frigg
# должно быть: STATUS = "healthy"
```

### Rule 2: HoundArcade (YGG) должен быть healthy ДО SHUTTLE

SHUTTLE при старте регистрирует ArcadeDB REST client; если YGG недоступен — GraphQL
запросы вернут 500 вместо деградации.

```bash
docker compose ps houndarcade
# должно быть: STATUS = "healthy"
```

### Рекомендуемый порядок ручного запуска

```bash
# Шаг 1: базы данных
docker compose up -d frigg houndarcade
# Дождаться healthy:
docker compose ps frigg houndarcade
# Шаг 2: backend-сервисы (Dali ждёт FRIGG, SHUTTLE ждёт YGG)
docker compose up -d dali heimdall-backend
# Шаг 3: frontend + BFF
docker compose up -d keycloak
docker compose up -d chur shuttle
docker compose up -d verdandi heimdall-frontend shell
```

Или всё вместе (compose сам соблюдёт depends_on):

```bash
docker compose up -d
# Следи за healthy-статусами:
watch -n 3 "docker compose ps --format 'table {{.Name}}\t{{.Status}}'"
```

---

## 2. Проблема: Edge types Sprint 2 не попали в live YGG schema

### Симптом

После пересоздания базы `hound` в YGG (или первого запуска) ArcadeDB не знает о типах рёбер,
добавленных в Sprint 2:

| Edge type | Где используется |
|---|---|
| `HAS_RECORD_FIELD` | DaliRecord → DaliRecordField |
| `RETURNS_INTO` | DaliStatement → DaliRecordField |
| `DaliDDLModifiesTable` | DaliDDLStatement → DaliTable |
| `DaliDDLModifiesColumn` | DaliDDLStatement → DaliColumn |

Если эти типы не созданы явно, первая попытка записи (`REMOTE_BATCH`) завершится ошибкой
`Class not found` и часть рёбер потеряется. Атом-счётчик будет занижен.

### Причина

`FriggSchemaInitializer` (или аналог в Hound) создавал схему при старте, но типы рёбер
Sprint 2 не были добавлены в initializer после их реализации в `RemoteSchemaCommands`.

### Диагностика

```bash
# Проверить какие edge types существуют в YGG:
curl -s -X POST http://localhost:2480/api/v1/command/hound \
  -u root:playwithdata -H "Content-Type: application/json" \
  -d '{"language":"sql","command":"SELECT name FROM schema:types WHERE type = '\''EDGE'\''"}' \
  | python3 -c "
import sys, json
types = [r['name'] for r in json.load(sys.stdin).get('result', [])]
needed = ['HAS_RECORD_FIELD','RETURNS_INTO','DaliDDLModifiesTable','DaliDDLModifiesColumn']
for t in needed:
    print(f'{t}: {\"✅\" if t in types else \"❌ MISSING\"}')"
```

### Фикс A — ручная миграция (быстро, для demo)

```bash
# Создать недостающие edge types напрямую в YGG:
for EDGE_TYPE in HAS_RECORD_FIELD RETURNS_INTO DaliDDLModifiesTable DaliDDLModifiesColumn; do
  curl -s -X POST http://localhost:2480/api/v1/command/hound \
    -u root:playwithdata -H "Content-Type: application/json" \
    -d "{\"language\":\"sql\",\"command\":\"CREATE EDGE TYPE ${EDGE_TYPE} IF NOT EXISTS\"}" \
    | python3 -c "import sys,json; print('${EDGE_TYPE}:', json.load(sys.stdin))"
done
```

### Фикс B — постоянный (после demo)

Добавить в `HoundSchemaInitializer.java` (или аналог) явное создание этих типов при старте:

```java
// В методе ensureEdgeTypes() или аналоге:
List.of(
    "HAS_RECORD_FIELD", "RETURNS_INTO",
    "DaliDDLModifiesTable", "DaliDDLModifiesColumn"
).forEach(name ->
    arcadeClient.command("hound",
        "CREATE EDGE TYPE " + name + " IF NOT EXISTS"));
```

**TODO (Sprint 3):** добавить этот вызов в startup-инициализатор → `INV-YGG-01`.

---

## 3. Проблема: schemaReady deadlock при FRIGG down

### Симптом

Dali запускается, FRIGG недоступен (ещё не поднялся или упал). Dali пишет в лог:

```
WARN  [s.s.d.s.ArcadeDbStorageProvider] FRIGG not available, retrying...
```

...и зависает. JobRunr background server не стартует. Сессии принимаются в очередь, но
не выполняются. Restart Dali через `docker compose restart dali` не помогает — он снова
пытается подключиться и снова зависает если FRIGG ещё down.

### Kill + restart sequence (проверено 15.04)

```bash
# Шаг 1: убедиться что FRIGG поднялся и healthy
docker compose up -d frigg
until [ "$(docker compose ps frigg --format '{{.Status}}')" = "healthy" ]; do
  echo "Waiting for FRIGG..."; sleep 5
done
echo "FRIGG is healthy ✅"

# Шаг 2: полный stop + rm Dali (не просто restart — нужен clean start)
docker compose stop dali
docker compose rm -f dali

# Шаг 3: поднять заново
docker compose up -d dali

# Шаг 4: дождаться Dali healthy
sleep 10
docker compose ps dali
# Ожидается: healthy
```

### Почему `restart` не помогает

`docker compose restart` не пересчитывает `depends_on`. Контейнер стартует без ожидания
FRIGG. Только `rm + up` или `up --force-recreate` заново применяет условие `service_healthy`.

### Превентивная мера

В `application.properties` Dali уже есть retry на FRIGG-подключение, но при длительном
недоступности retry-бюджет исчерпывается. Рекомендуется:

```properties
# services/dali/src/main/resources/application.properties
quarkus.datasource.jdbc.acquisition-timeout=30
# или для ArcadeDB custom client — увеличить retry count:
arcade.startup.retry-count=10
arcade.startup.retry-delay-ms=3000
```

---

## 4. Demo-rehearsal checklist

Выполни перед каждой rehearsal:

```
□ docker compose ps — все сервисы healthy (не just running)
□ FRIGG edge types — Фикс A выше (HAS_RECORD_FIELD etc.)
□ YGG database 'hound' существует
□ curl http://localhost:9090/api/sessions/health → {"frigg":"ok"}
□ curl http://localhost:8080/q/health → {"status":"UP"}
□ Открыть http://localhost:15173 — LOOM рендерит L1 граф
□ Запустить тестовую сессию preview=true → status=COMPLETED
```

---

## 5. Полный вывод статусов (быстрый аудит)

```bash
docker compose ps --format "table {{.Name}}\t{{.Status}}\t{{.Ports}}"
```

Ожидаемый вывод (все сервисы):

| Service | Status | Key port |
|---|---|---|
| aida-root-frigg-1 | healthy | 127.0.0.1:2481→2480 |
| aida-root-houndarcade-1 | healthy | 127.0.0.1:2480→2480 |
| aida-root-dali-1 | healthy | 0.0.0.0:19090→9090 |
| aida-root-heimdall-backend-1 | healthy | 0.0.0.0:19093→9093 |
| aida-root-keycloak-1 | healthy | 127.0.0.1:18180→8180 |
| aida-root-chur-1 | healthy | 0.0.0.0:13000→3000 |
| aida-root-shuttle-1 | healthy | 0.0.0.0:18080→8080 |
| aida-root-verdandi-1 | healthy | 0.0.0.0:15173→5173 |
| aida-root-heimdall-frontend-1 | healthy | 0.0.0.0:25174→5174 |
| aida-root-shell-1 | healthy | 0.0.0.0:25175→5175 |

---

## История изменений

| Дата | Версия | Что |
|---|---|---|
| 16.04.2026 | 1.0 | Создан по итогам QG-прогона 15.04. Три известных проблемы зафиксированы: FRIGG-Dali порядок, missing edge types Sprint 2, schemaReady deadlock kill+restart sequence. |
