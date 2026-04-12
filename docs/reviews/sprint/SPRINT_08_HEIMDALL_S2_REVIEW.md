# Sprint 8 — HEIMDALL Sprint 2 Review

**Дата:** 2026-04-12  
**Ветка:** `feature/heimdall-sprint2` → merged PR #2 → `master`  
**Merge commit:** `9f4c184`  
**Длительность:** 1 рабочая сессия  

---

## Цель спринта

Реализовать метрики, фильтрацию WebSocket-потока, контрольные эндпоинты и персистентность снапшотов для HEIMDALL Admin Control Panel (ADR-DA-010).

---

## Что сделано ✅

### HEIMDALL Backend (`services/heimdall-backend`)

| # | Компонент | Файл | Статус |
|---|-----------|------|--------|
| 1 | MetricsSnapshot | `metrics/MetricsSnapshot.java` | ✅ |
| 2 | MetricsCollector | `metrics/MetricsCollector.java` | ✅ |
| 3 | EventResource + metrics | `resource/EventResource.java` | ✅ |
| 4 | MetricsResource | `resource/MetricsResource.java` | ✅ |
| 5 | EventFilter | `ws/EventFilter.java` | ✅ |
| 6 | EventStreamEndpoint + filter | `ws/EventStreamEndpoint.java` | ✅ |
| 7 | SnapshotInfo | `snapshot/SnapshotInfo.java` | ✅ |
| 8 | FriggCommand | `snapshot/FriggCommand.java` | ✅ |
| 9 | FriggResponse | `snapshot/FriggResponse.java` | ✅ |
| 10 | FriggClient | `snapshot/FriggClient.java` | ✅ |
| 11 | FriggGateway | `snapshot/FriggGateway.java` | ✅ |
| 12 | SnapshotManager | `snapshot/SnapshotManager.java` | ✅ |
| 13 | ControlResource | `resource/ControlResource.java` | ✅ |
| 14 | build.gradle REST client | `build.gradle` | ✅ |
| 15 | application.properties FRIGG | `application.properties` | ✅ |

### SHUTTLE (`services/shuttle`)

| # | Компонент | Файл | Статус |
|---|-----------|------|--------|
| 16 | HeimdallEvent (копия) | `heimdall/model/HeimdallEvent.java` | ✅ |
| 17 | EventType (копия) | `heimdall/model/EventType.java` | ✅ |
| 18 | EventLevel (копия) | `heimdall/model/EventLevel.java` | ✅ |
| 19 | HeimdallClient | `heimdall/HeimdallClient.java` | ✅ |
| 20 | HeimdallEmitter | `heimdall/HeimdallEmitter.java` | ✅ |
| 21 | LineageResource + emit | `resource/LineageResource.java` | ✅ |
| 22 | application.properties heimdall | `application.properties` | ✅ |

### Chur BFF (`bff/chur`)

| # | Компонент | Файл | Статус |
|---|-----------|------|--------|
| 23 | heimdall routes | `src/routes/heimdall.ts` | ✅ |
| 24 | server.ts register | `src/server.ts` | ✅ |

### Инфраструктура

- FRIGG healthcheck: исправлен с `/dev/tcp` (bash-only) → `wget -q -O /dev/null` (Alpine-compatible)
- FRIGG port mapping: `2481:2480` (host → container), Docker-сеть `frigg:2480`
- `docker-compose.yml`: `HEIMDALL_URL` добавлен в Chur env
- `docs/internal/FRIGG.md`: документация доступов, портов, инициализации БД

---

## Баги найдены и исправлены в спринте 🔧

| # | Проблема | Решение |
|---|----------|---------|
| **R1** | `HandshakeRequest.queryParam(String)` не существует в Quarkus 3.34.2 (Risk #1 из плана) | Парсим `handshakeRequest().query()` вручную через `URLDecoder` |
| **FRIGG Healthcheck** | Alpine `/bin/sh` не поддерживает `/dev/tcp` bash-расширение — `unhealthy` сразу после старта | Заменено на `wget -q -O /dev/null http://127.0.0.1:2480/api/v1/ready` |
| **FRIGG Port** | Попытка настроить ArcadeDB на порт 2481 через JVM flag (`-Darcadedb.server.httpPort=2481`) — игнорировалась | ArcadeDB всегда слушает 2480 внутри; используем host mapping `2481:2480` |

---

## Архитектурные решения 🏛️

| Решение | Обоснование |
|---------|-------------|
| `AtomicLong` вместо Micrometer `Counter` | `Counter#count()` возвращает double с накоплением погрешности; AtomicLong даёт точные целые значения |
| Один `key:value` в EventFilter (Sprint 2) | Комбинированные фильтры (AND нескольких пар) — Sprint 3; простота реализации важнее функциональности на этом этапе |
| `CREATE VERTEX TYPE IF NOT EXISTS` в SnapshotManager | ArcadeDB v24+ синтаксис; `CREATE CLASS ... EXTENDS V` — legacy alias, который тоже работает |
| Копии моделей в SHUTTLE (`heimdall.model.*`) | Docker multi-module build для SHUTTLE — отдельный рефакторинг-спринт; временное решение не блокирует Sprint 2 |
| `X-Seer-Role` forwarded header в ControlResource | Chur — единственная граница безопасности; HEIMDALL доверяет forwarded-заголовкам (паттерн как в SHUTTLE + SeerIdentity) |

---

## Метрики спринта 📊

| Метрика | Значение |
|---------|---------|
| Новых файлов | 18 |
| Изменённых файлов | 6 |
| Строк добавлено | ~764 |
| Компиляция | ✅ (обе сборки: heimdall + shuttle) |
| Тесты | ✅ `./gradlew :services:heimdall-backend:test` |
| TypeScript check | ✅ `tsc --noEmit` без ошибок |

---

## Definition of Done — чеклист

- [x] `./gradlew :services:heimdall-backend:compileJava` — SUCCESS
- [x] `./gradlew :services:heimdall-backend:test` — BUILD SUCCESSFUL
- [x] `./gradlew :services:shuttle:compileJava` — SUCCESS
- [x] TypeScript (`tsc --noEmit`) — без ошибок
- [x] FRIGG контейнер — `(healthy)`, порт `:2481` → `:2480`
- [x] `GET /metrics/snapshot` эндпоинт реализован
- [x] EventFilter (WebSocket `?filter=...`) реализован
- [x] `POST /control/reset` реализован (X-Seer-Role: admin)
- [x] `POST /control/snapshot` → SnapshotManager → FRIGG
- [x] HeimdallEmitter: fire-and-forget, SHUTTLE не падает при недоступном HEIMDALL
- [x] Chur proxy `/heimdall/*` реализован
- [x] PR #2 merged → master

---

## Открытые вопросы / Перенесено в Sprint 3

| # | Вопрос / Задача |
|---|----------------|
| **WS Proxy** | `/heimdall/ws/events` через Chur — требует `@fastify/websocket` (не установлен) |
| **Q7** | Реальная отмена сессий через Dali API — `POST /control/cancel/:id` сейчас заглушка |
| **Q13** | WebSocket протокол: native vs graphql-ws |
| **C.0** | ArcadeDB upgrade (Hound 25.12.1 → 26.x) — требует миграции embedded→network; деферред |
| **Docker SHUTTLE** | Multi-module Docker build SHUTTLE+dali-models — временные копии моделей убрать |
| **activeWorkers** | Монотонный счётчик WORKER_ASSIGNED в Sprint 2; реальный gauge (с SESSION_COMPLETED) — Sprint 3 |
| **HEIMDALL Frontend** | `frontends/heimdall-frontend/`, порт :14000/:24000 — июнь 2026 |

---

*Sprint 8 / HEIMDALL Sprint 2 review. Следующий: Sprint 9 / HEIMDALL Sprint 3.*
