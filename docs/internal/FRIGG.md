# FRIGG — ArcadeDB для HEIMDALL (доступы и инструкции)

> **FRIGG** — выделенный инстанс ArcadeDB исключительно для хранения снапшотов HEIMDALL.
> **Не путать** с **HoundArcade** (основная БД Hound-данных, порт :2480).

---

## Порты и адреса

| Контекст | Адрес | Примечание |
|---|---|---|
| Локальная разработка (dev) | `http://localhost:2481` | host port 2481 → container 2480 |
| Внутри Docker-сети | `http://frigg:2480` | имя контейнера `frigg`, внутренний порт |
| HEIMDALL dev (`application.properties`) | `http://localhost:2481` | дефолт без профиля |
| HEIMDALL prod (Docker env) | `http://frigg:2480` | `QUARKUS_REST_CLIENT_FRIGG_API_URL` |

---

## Учётные данные

| Параметр | Значение |
|---|---|
| Пользователь | `root` |
| Пароль | `playwithdata` |
| База данных | `heimdall` |

---

## Запуск контейнера

### Docker Compose (рекомендуется)

```bash
# Запустить только FRIGG
docker compose up frigg -d

# Проверить здоровье
docker ps --filter "name=frigg" --format "{{.Names}}: {{.Status}}"

# Ожидаемый вывод:
# aida-root-frigg-1: Up X seconds (healthy)
```

### Проверка доступности

```bash
# HTTP 204 = FRIGG готов
curl -s -o /dev/null -w "%{http_code}" http://localhost:2481/api/v1/ready
# → 204

# Список баз данных (должен вернуть пустой список до инициализации)
curl -u root:playwithdata http://localhost:2481/api/v1/databases
```

---

## Инициализация БД `heimdall`

HEIMDALL автоматически создаёт базу данных `heimdall` при первом старте через `SnapshotManager`.
Если нужно инициализировать вручную:

```bash
# Создать базу данных heimdall
curl -u root:playwithdata -X POST \
  "http://localhost:2481/api/v1/create/heimdall"

# Проверить (должна появиться в списке)
curl -u root:playwithdata http://localhost:2481/api/v1/databases
```

---

## Структура данных (схема создаётся HEIMDALL автоматически)

```sql
-- Вершина для хранения снапшотов
CREATE VERTEX TYPE Snapshot IF NOT EXISTS;

-- Поля:
--   snapshotId   : STRING (UUID)
--   name         : STRING (имя, например "baseline")
--   createdAt    : LONG   (unix ms)
--   eventCount   : INTEGER
--   eventsJson   : STRING (сжатый JSON массив событий)
```

---

## Веб-интерфейс ArcadeDB (Studio)

ArcadeDB поставляется со встроенным Studio:

```
http://localhost:2481/
```

Войти: `root` / `playwithdata` → выбрать базу `heimdall`.

---

## Конфигурация в сервисах

### HEIMDALL Backend (`application.properties`)

```properties
# Dev (локальная разработка)
quarkus.rest-client.frigg-api.url=http://localhost:2481
frigg.db=heimdall
frigg.user=root
frigg.password=playwithdata

# Prod (Docker — переопределяется через env)
%prod.quarkus.rest-client.frigg-api.url=http://frigg:2480
%prod.frigg.db=${FRIGG_DB:heimdall}
%prod.frigg.user=${FRIGG_USER:root}
%prod.frigg.password=${FRIGG_PASSWORD:playwithdata}
```

### Docker Compose (`docker-compose.yml`)

```yaml
heimdall-backend:
  environment:
    QUARKUS_REST_CLIENT_FRIGG_API_URL: http://frigg:2480
    FRIGG_DB:       heimdall
    FRIGG_USER:     root
    FRIGG_PASSWORD: playwithdata
  depends_on:
    frigg:
      condition: service_healthy
```

---

## Persistence

FRIGG хранит данные в Docker volume `frigg_data`:

```bash
# Посмотреть volume
docker volume inspect aida-root_frigg_data

# Очистить все данные FRIGG (ОСТОРОЖНО — удалит все снапшоты!)
docker compose down frigg
docker volume rm aida-root_frigg_data
docker compose up frigg -d
```

---

## Отличие от HoundArcade

| | HoundArcade | FRIGG |
|---|---|---|
| Назначение | Граф кода (Hound-данные) | Снапшоты HEIMDALL |
| База данных | `hound` | `heimdall` |
| Внешний порт (host) | `2480` | `2481` |
| Внутренний порт (Docker) | `2480` | `2480` |
| Docker-имя | `HoundArcade` | `frigg` |
| Управляется | Hound / внешний контейнер | docker-compose (этот репо) |
| Image | внешний | `arcadedata/arcadedb:26.3.2` |
