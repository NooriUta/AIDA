# FRIGG — Internal Connection Guide

**Документ:** `FRIGG internal`
**Дата:** 12.04.2026
**Статус:** ✅ Running (healthcheck green)

---

## Что такое FRIGG

FRIGG = отдельный ArcadeDB instance для unified SEER Studio state:
- HEIMDALL snapshots (сохранённые состояния demo)
- Saved LOOM views (будущее)
- User preferences (будущее)
- JobRunr persistence для Dali (будущее)

**Отдельный от YGG** — YGG хранит lineage graph, FRIGG хранит application state.

---

## Подключение

| Параметр | Значение |
|---|---|
| Host | `localhost` (dev) / `frigg` (docker) |
| Port | `:2481` |
| Protocol | HTTP REST (ArcadeDB HTTP API) |
| Database | `frigg` |
| Username | `root` |
| Password | см. `.env` / `FRIGG_ROOT_PASSWORD` |

**Dev URL:** `http://localhost:2481`
**Docker URL:** `http://frigg:2481`

---

## Docker Compose

```yaml
# docker-compose.yml
frigg:
  image: arcadedata/arcadedb:latest
  container_name: frigg
  ports:
    - "2481:2480"
  environment:
    - ARCADEDB_SERVER_ROOTPASSWORD=${FRIGG_ROOT_PASSWORD:-playwithdata}
  volumes:
    - frigg_data:/home/arcadedb/databases
  healthcheck:
    test: ["CMD", "wget", "-q", "-O", "/dev/null", "http://localhost:2480/api/v1/ready"]
    interval: 10s
    timeout: 5s
    retries: 5
    start_period: 20s

volumes:
  frigg_data:
```

> ⚠️ **Alpine `sh` lacks `/dev/tcp`** — используй `wget`, не `curl --fail` с TCP check.

---

## FriggGateway

`services/heimdall-backend/src/main/java/.../FriggGateway.java`

Паттерн: mirrors SHUTTLE's `ArcadeGateway`. Reactive (Uni) по всей цепочке.

```java
@ApplicationScoped
public class FriggGateway {
    // HTTP client → FRIGG :2481
    // Используется SnapshotManager для persist/load snapshots
    Uni<String> saveSnapshot(SnapshotData data) { ... }
    Uni<List<SnapshotData>> listSnapshots() { ... }
}
```

---

## Инициализация базы

При первом запуске HEIMDALL backend создаёт схему автоматически через FriggGateway.

Ручная инициализация (если нужно):
```bash
# Через ArcadeDB Studio: http://localhost:2481
# Или через curl:
curl -X POST http://localhost:2481/api/v1/server \
  -u root:playwithdata \
  -H "Content-Type: application/json" \
  -d '{"command":"create database frigg"}'
```

---

## Port mapping

| Компонент | Port | Описание |
|---|---|---|
| YGG (main lineage graph) | `:2480` | Основная БД Hound/SHUTTLE |
| FRIGG (app state) | `:2481` | Snapshots, saved views, job state |
| HEIMDALL backend | `:9093` | Event bus, metrics, control |

---

## Связанные файлы

- `services/heimdall-backend/src/.../FriggGateway.java`
- `services/heimdall-backend/src/.../SnapshotManager.java`
- `services/heimdall-backend/src/.../ControlResource.java` — использует SnapshotManager
- `docker-compose.yml` — frigg service
