# Plan: Dali Instance Isolation (multi-worker dev)

**Статус**: OPEN  
**Приоритет**: Medium  
**Контекст**: обнаружено 2026-04-19 при тестировании procedure/spec merge

## Проблема

Dali использует JobRunr с общей очередью задач в Frigg (ArcadeDB).
При одновременном запуске Docker-контейнера dali и локального `quarkusDev` оба
инстанса регистрируются как воркеры (`jobrunr_servers`) и конкурируют за задачи.

Docker-воркер забирает задачу с локальным путём (`C:\Dali_tests\...`),
который недоступен внутри контейнера → задача зависает в QUEUED или падает.

## Варианты решения

### Вариант A — `DALI_INSTANCE_ID` + фильтрация задач (рекомендован)

1. Добавить env-переменную `DALI_INSTANCE_ID` (пустая = "общий пул").
2. При постановке задачи в очередь записывать `targetInstanceId` в метаданные задачи.
3. Воркер при поллинге фильтрует по `targetInstanceId IS NULL OR targetInstanceId = MY_ID`.
4. Docker-compose задаёт `DALI_INSTANCE_ID=docker`, локальный dev — пустой (берёт всё)
   или `DALI_INSTANCE_ID=local`.

**Плюсы**: полная изоляция, можно держать оба инстанса одновременно.  
**Минусы**: требует изменения схемы `jobrunr_jobs` или кастомного поля в jobData.

### Вариант B — отдельная Frigg для Docker

Поднять второй ArcadeDB-инстанс только для Docker-стека (отдельный порт).
Локальный dev использует основной Frigg.

**Плюсы**: полная независимость, нет изменений в коде.  
**Минусы**: два ArcadeDB процесса, больше ресурсов.

### Вариант C — документация + конвенция (текущий workaround)

Только один инстанс dali активен в каждый момент времени.
Документировано в `docs/guides/DEVELOPMENT.md`.

## Рекомендуемые шаги (Вариант A)

- [ ] Изучить JobRunr API для кастомных фильтров воркера (`JobFilter` / `ElectStateFilter`)
- [ ] Добавить `dali.instance-id` в `application.properties`
- [ ] Передавать `instanceId` при enqueue через Chur API
- [ ] Фильтровать при поллинге в `FriggGateway` или через кастомный `BackgroundJobServer`
- [ ] Обновить `docker-compose.yml`: `DALI_INSTANCE_ID=docker`
- [ ] Тесты: два воркера, задача уходит нужному

## Ссылки

- `services/dali/` — основной сервис
- `docker-compose.yml` — `DALI_INSTANCE_ID` (env-заглушка, не активна)
- `docs/guides/DEVELOPMENT.md` — раздел "Dali — IDE vs Docker"
