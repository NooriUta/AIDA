---
name: SCHEDULE
description: "Расписание запуска Quality Gates для проекта AIDA. Читай этот файл когда нужно понять: какие гейты запускать сейчас, в каком порядке, и какой текущий статус каждого."
---

# SCHEDULE — Quality Gates AIDA

## Текущий статус (16.04.2026)

| Гейт | Частота | Статус | Последний прогон |
|------|---------|--------|-----------------|
| QG-DALI-persistence | after dali merge | ✅ PASS (static) | 16.04.2026 — INV-1/2/3 OK; @Singleton≡@ApplicationScoped |
| QG-DALI-ygg-write | after parse run | ✅ GREEN | 15.04.2026 — 98.8% / 143K atoms; static OK 16.04 |
| QG-CHUR-resilience | after chur merge | ✅ PASS (static) | 16.04.2026 — 5/5 timeout, catch+[], Array.isArray scope |
| QG-SECURITY-demo | 24h before demo | ✅ PASS (static) | 16.04.2026 — ports 127.0.0.1, TTL 4h, CORS OK; INV-7/9 runtime |
| QG-HEIMDALL-backend-validation | after heimdall merge | ⚠️ PARTIAL | 16.04.2026 — /events @Valid OK; **/events/batch без @Valid на List** |
| QG-HEIMDALL-frontend-ws | before demo | ✅ PASS (static) | 16.04.2026 — reconnect + mountedRef + clearInterval OK |
| QG-VERDANDI-prefs-sync | after stores/ merge | ✅ PASS (static) | 16.04.2026 — catch no-throw, debounce 1.5s, fetchPrefs in auth |
| QG-HOUND-listener-chain | after hound merge | ⏳ BLOCKED | H3.8 не реализован (HoundHeimdallListener.java отсутствует) |
| **QG-PERFORMANCE-weekly** | **каждый понедельник** | ⏳ | baseline 15.04 |
| **QG-ARCH-daily** | **ежедневно** | ⏳ | не запускался |

**Demo-ready: ⚠️ — 7/8 QG static PASS, 1 блокер: /events/batch без @Valid на List**

> **16.04.2026 еженедельный прогон (статический):** Runtime не проверялся — сервисы не запущены.
> Блокер до следующего demo-rehearsal: добавить `@Valid` на `List<HeimdallEvent>` в `/events/batch` + поля `@NotNull`/`@NotBlank` в `HeimdallEvent` record.

---

## Когда запускать какой гейт

### После любого merge в master

```
services/dali/ изменён        → QG-DALI-persistence + QG-DALI-ygg-write
bff/chur/src/ изменён          → QG-CHUR-resilience
infra/keycloak/ изменён        → QG-SECURITY-demo
docker-compose.yml изменён     → QG-SECURITY-demo
services/heimdall-backend/     → QG-HEIMDALL-backend-validation
useEventStream.ts изменён      → QG-HEIMDALL-frontend-ws
stores/prefsStore.ts изменён   → QG-VERDANDI-prefs-sync
libraries/hound/ изменён       → QG-HOUND-listener-chain
```

### Еженедельно (понедельник)

```
Скилл /performance-weekly — полный SLO-прогон (15 мин):
  □ QG-PERFORMANCE-weekly  — P/A/R метрики (resolution rate, latency, надёжность)
  □ QG-ARCH-daily          — архитектурный аудит + diff от прошлой недели
  □ QG-DALI-persistence    — сессии переживают restart
  □ QG-DALI-ygg-write      — E2E парсинг → YGG
  □ Обновить колонку "Последний прогон" в таблице выше
```

### Ежедневно

```
Скилл /adr-review или /QG-ARCH-daily:
  □ QG-ARCH-daily — быстрый аудит (5-10 мин): git diff, TS errors, build status
```

### За 24 часа до demo-rehearsal

```
P0 — обязательно, иначе rehearsal отменить:
  1. QG-SECURITY-demo
  2. QG-DALI-persistence
  3. QG-DALI-ygg-write
  4. QG-PERFORMANCE-weekly  ← новый P0

P1 — важно:
  5. QG-HEIMDALL-frontend-ws
  6. QG-CHUR-resilience
```

### Перед HighLoad++ (октябрь 2026)

```
Полный прогон всех 10 гейтов.
Дополнительно вручную:
  □ Прогон 300 файлов preview=false → DaliTable count совпадает с ожидаемым
  □ QG-PERFORMANCE-weekly → все SLO GREEN (resolution ≥98%, parse ≤360s)
  □ docker compose down && up → все сервисы healthy без ручного вмешательства
  □ Presentation mode ⛶ → fullscreen с живыми данными ≥ 4 минуты
  □ make demo-reset → < 5 секунд
  □ Backup ноутбук: все гейты GREEN
```

---

## Файлы QG

| Файл | Скилл | Описание |
|------|-------|----------|
| `QG-DALI-persistence.md` | `qg-dali-persistence` | Сессии переживают restart Dali |
| `QG-DALI-ygg-write.md` | `qg-dali-ygg-write` | Парсинг пишет в YGG, HEIMDALL видит |
| `QG-CHUR-resilience.md` | `qg-chur-resilience` | BFF устойчив к отказам зависимостей |
| `QG-SECURITY-demo.md` | `qg-security-demo` | Безопасно для публичной сети |
| `QG-HEIMDALL-backend-validation.md` | `qg-heimdall-backend-validation` | Backend не падает на плохих данных |
| `QG-HEIMDALL-frontend-ws.md` | `qg-heimdall-frontend-ws` | WebSocket переподключается |
| `QG-VERDANDI-prefs-sync.md` | `qg-verdandi-prefs-sync` | UI настройки синхронизируются |
| `QG-HOUND-listener-chain.md` | `qg-hound-listener-chain` | Listener chain корректно передаёт события |
| `QG-PERFORMANCE-weekly.md` | `performance-weekly` | **SLO: resolution rate, latency, надёжность** |
| `QG-ARCH-daily.md` | `adr-review` | **Ежедневный архитектурный аудит** |

---

## Как использовать в Claude Code

```
Запустить один гейт:
  /qg-dali-persistence
  /qg-security-demo
  /performance-weekly
  /adr-review

Запустить P0 перед demo:
  "Запусти все P0 quality gates"
  → QG-SECURITY-demo + QG-DALI-persistence + QG-DALI-ygg-write + QG-PERFORMANCE-weekly

Еженедельный цикл:
  /performance-weekly
  → Полный SLO-прогон, обновление истории в QG-PERFORMANCE-weekly.md

Ежедневный аудит:
  /adr-review
  → QG-ARCH-daily: git diff, build, TS, архитектурный анализ
```
