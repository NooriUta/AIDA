# AIDA — Port Mapping

**Документ:** `PORT_MAPPING`
**Версия:** 1.0
**Дата:** 16.04.2026
**Статус:** ✅ ACTIVE

---

## Внешние порты (host → container)

| Сервис | Host port | Container port | Bind | Назначение |
|--------|-----------|----------------|------|------------|
| **keycloak** | `18180` | `8180` | `127.0.0.1` | IAM / SSO |
| **shuttle** | `18080` | `8080` | `0.0.0.0` | GraphQL lineage API |
| **chur** | `13000` | `3000` | `0.0.0.0` | BFF / auth gateway |
| **verdandi** | `15173` | `5173` | `0.0.0.0` | Seiðr Studio frontend |
| **heimdall-frontend** | `25174` | `5174` | `0.0.0.0` | HEIMDALL Control Panel |
| **shell** | `25175` | `5175` | `0.0.0.0` | MFE shell (точка входа) |
| **dali** | `19090` | `9090` | `0.0.0.0` | Parse service REST API |
| **heimdall-backend** | `19093` | `9093` | `0.0.0.0` | Observability backend |
| **frigg** (ArcadeDB) | `2481` | `2480` | `127.0.0.1` | JobRunr + HEIMDALL DB |
| **houndarcade** (YGG) | `2480` | `2480` | `127.0.0.1` | Lineage graph DB |
| **houndarcade** binary | `2424` | `2424` | `127.0.0.1` | ArcadeDB binary protocol |

> **Правило offset +10000:** prod-порты сервисов (8080, 9090, 9093, …) смещены
> на +10000 снаружи (18080, 19090, 19093, …) чтобы не конфликтовать с dev-процессами.
> Фронтенды смещены на +10000 от 5173/5174/5175 → 15173/25174/25175.

---

## Внутренняя сеть `aida_net`

Внутри Docker-сети сервисы общаются по имени контейнера и **нативному** порту
(не хост-порту). Смещение +10000 применяется **только снаружи**.

```
Browser (localhost)
│
├── :25175  shell ──────────────── MFE host, загружает remoteEntry по localhost-ссылкам:
│    │         VITE_VERDANDI_URL  → http://localhost:15173/remoteEntry.js
│    │         VITE_HEIMDALL_URL  → http://localhost:25174/remoteEntry.js
│    │
│    ├── :15173  verdandi ─────── все API-запросы → chur:13000 (через localhost)
│    └── :25174  heimdall-frontend
│                 ├── API  → chur:13000 (через localhost)
│                 └── WS   → heimdall-backend:19093 (через localhost)
│
├── :13000  chur (BFF)             внутри контейнера:
│    ├── keycloak:8180             (auth — без хост-смещения)
│    ├── shuttle:8080              (GraphQL lineage)
│    ├── heimdall-backend:9093
│    └── HoundArcade:2480         (прямые Cypher/SQL к YGG)
│
├── :18080  shuttle                внутри контейнера:
│    ├── HoundArcade:2480         (Cypher — чтение/запись lineage)
│    ├── heimdall-backend:9093    (события парсинга)
│    └── dali:9090                (создание сессий / статус)
│
├── :19090  dali                   внутри контейнера:
│    ├── frigg:2480               (JobRunr persistence — БД "dali")
│    ├── HoundArcade:2480         (запись разобранного графа — БД "hound")
│    └── heimdall-backend:9093    (HoundHeimdallListener fire-and-forget)
│
├── :19093  heimdall-backend       внутри контейнера:
│    └── frigg:2480               (HEIMDALL snapshots — БД "heimdall")
│
├── :2481   frigg (ArcadeDB)       2 базы в одном инстансе:
│    ├── БД "dali"    ← dali
│    └── БД "heimdall" ← heimdall-backend
│
└── :2480   houndarcade (YGG)      1 база:
     └── БД "hound"  ← shuttle, dali, chur
```

---

## Два ArcadeDB — важно не перепутать

| | **FRIGG** | **HoundArcade (YGG)** |
|---|---|---|
| Host port | `localhost:2481` | `localhost:2480` |
| Внутренняя сеть | `frigg:2480` | `HoundArcade:2480` |
| Docker volume | `frigg_data` | `hound_databases` (external) |
| Базы данных | `dali`, `heimdall` | `hound` |
| Что хранит | JobRunr jobs, HEIMDALL события | Lineage граф (DaliTable, DaliStatement, …) |
| Кто пишет | dali, heimdall-backend | dali, shuttle, chur |
| Healthcheck | `http://127.0.0.1:2480/api/v1/ready` | `http://127.0.0.1:2480/api/v1/ready` |

> Dev-правило: `curl localhost:2481` = FRIGG, `curl localhost:2480` = YGG.

---

## depends_on — порядок запуска

```
frigg ──────────────────────────────┐
                                    ├─► dali
                                    └─► heimdall-backend

houndarcade ────────────────────────┐
                                    ├─► shuttle
                                    └─► chur

keycloak ───────────────────────────┐
shuttle ────────────────────────────┤
houndarcade ────────────────────────┴─► chur

chur ───────────────────────────────┬─► verdandi
                                    ├─► heimdall-frontend
                                    └─► shell (+ verdandi + heimdall-frontend)
```

> Правило из STARTUP_SEQUENCE: `service_healthy` (не `service_started`) для FRIGG и HoundArcade.
> Только `docker compose rm -f + up` (не `restart`) пересчитывает `depends_on`.

---

## Быстрый аудит

```bash
# Проверить все сервисы разом
docker compose ps --format "table {{.Name}}\t{{.Status}}\t{{.Ports}}"

# Ожидаемый результат (все healthy):
# aida-root-frigg-1           healthy   127.0.0.1:2481->2480/tcp
# aida-root-houndarcade-1     healthy   127.0.0.1:2480->2480/tcp, 127.0.0.1:2424->2424/tcp
# aida-root-dali-1            healthy   0.0.0.0:19090->9090/tcp
# aida-root-heimdall-backend-1 healthy  0.0.0.0:19093->9093/tcp
# aida-root-keycloak-1        healthy   127.0.0.1:18180->8180/tcp
# aida-root-chur-1            healthy   0.0.0.0:13000->3000/tcp
# aida-root-shuttle-1         healthy   0.0.0.0:18080->8080/tcp
# aida-root-verdandi-1        healthy   0.0.0.0:15173->5173/tcp
# aida-root-heimdall-frontend-1 healthy 0.0.0.0:25174->5174/tcp
# aida-root-shell-1           healthy   0.0.0.0:25175->5175/tcp
```

---

## История изменений

| Дата | Версия | Что |
|------|--------|-----|
| 16.04.2026 | 1.0 | Создан. Полная карта портов по docker-compose.yml. |
