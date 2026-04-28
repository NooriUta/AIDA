# AIDA — Руководство по ручному UI/UX тестированию

**Дата:** 26.04.2026 · **Ветка:** develop · **Версия:** v1.4.0-rc

## Окружение

| Сервис | URL | Логин |
|--------|-----|-------|
| Heimdall (admin UI) | http://localhost:5174 | admin / sae0611! |
| Verdandi (SQL inspector) | http://localhost:5173 | — |
| Keycloak Admin | http://localhost:18180/kc/admin | admin / sae0611! |
| JobRunr UI | http://localhost:9090/jobrunr | — |
| QA Dashboard | http://localhost:5201/index.html | — |

### Тестовые пользователи (Keycloak realm `aida`)

| Username | Email | Роль | Пароль |
|----------|-------|------|--------|
| superadmin | superadmin@seer.io | super-admin | sae0611! |
| admin | admin@seer.io | admin | sae0611! |
| local.admin | la@acme.com | local-admin | sae0611! |
| tenant.owner | owner@acme.com | tenant-owner | sae0611! |
| sergey.ivanov | s.ivanov@seer.io | auditor | sae0611! |
| viewer | dev@seer.io | viewer | sae0611! |
| editor | m.kuznetsova@seer.io | editor | sae0611! |

---

## Категория MT — Multitenant R1–R5 (P0)

> Войди как **admin** или **superadmin** перед этими тестами.

### MT-01 · Создание тенанта через UI `P0`

**Страница:** [/admin/tenants](http://localhost:5174/admin/tenants)

**Шаги:**
1. Нажми кнопку **"+ Create"** в правом верхнем углу
2. В модале введи alias: `qa-test-01` (только строчные, без спецсимволов)
3. Нажми **Submit**

**Ожидается:**
- Тенант появился в таблице со статусом `PROVISIONING`
- Через ~10 сек статус меняется на `ACTIVE`
- Строка содержит колонки: Alias, Status, Users, Sources, Atoms, Harvest cron, Config v, Actions

---

### MT-02 · Таблица тенантов — колонки `P0`

**Страница:** [/admin/tenants](http://localhost:5174/admin/tenants)

**Шаги:**
1. Открой страницу тенантов
2. Найди только что созданный тенант `qa-test-01`

**Ожидается:**
- Видны все колонки: **Alias, Status, Users, Sources, Atoms, Harvest cron, Config v, Actions**
- Числа в колонках Users/Sources/Atoms корректны (0 для нового тенанта)

---

### MT-03 · Suspend тенанта `P0`

**Страница:** [/admin/tenants](http://localhost:5174/admin/tenants)

**Шаги:**
1. Найди тенант `qa-test-01` со статусом `ACTIVE`
2. Нажми **"Suspend"** в колонке Actions (или на детальной странице)
3. Подтверди в диалоге

**Ожидается:**
- Статус меняется на `SUSPENDED`
- Доступные кнопки: **Restore**, **Archive**
- ❌ Кнопки Harvest и Suspend — скрыты

---

### MT-04 · Unsuspend (Restore) тенанта `P0`

**Страница:** [/admin/tenants](http://localhost:5174/admin/tenants) или [/admin/tenants/qa-test-01](http://localhost:5174/admin/tenants/qa-test-01)

**Шаги:**
1. Найди тенант `qa-test-01` со статусом `SUSPENDED`
2. Нажми **"Restore"**

**Ожидается:**
- Статус → `ACTIVE`
- Кнопки: **Harvest**, **Suspend**, **Archive**

---

### MT-05 · Archive тенанта `P0`

**Страница:** [/admin/tenants/qa-test-01](http://localhost:5174/admin/tenants/qa-test-01)

**Шаги:**
1. Убедись что тенант ACTIVE или SUSPENDED
2. Нажми **"Archive now"**
3. Прочитай текст в диалоге подтверждения
4. Подтверди

**Ожидается:**
- Статус → `ARCHIVED`
- Доступна только кнопка **Restore**
- В диалоге было чёткое предупреждение об экспорте в S3

---

### MT-06 · Restore из архива `P0`

**Страница:** [/admin/tenants/qa-test-01](http://localhost:5174/admin/tenants/qa-test-01)

**Шаги:**
1. Тенант должен быть в статусе `ARCHIVED`
2. Нажми **"Restore"**

**Ожидается:**
- Статус → `ACTIVE`

---

### MT-07 · Trigger Harvest `P0`

**Страница:** [/admin/tenants](http://localhost:5174/admin/tenants)

**Шаги:**
1. Найди ACTIVE тенант `qa-test-01`
2. Нажми **"Harvest"** в строке таблицы

**Ожидается:**
- Alert/toast: `"Harvest queued: <harvestId>"`
- Job виден в [JobRunr UI](http://localhost:9090/jobrunr) со статусом ENQUEUED/PROCESSING

---

### MT-08 · PROVISIONING_FAILED — Retry `P0`

**Страница:** [/admin/tenants](http://localhost:5174/admin/tenants)

**Шаги:**
1. Найди тенант со статусом `PROVISIONING_FAILED`
2. Нажми **"Retry"**

**Ожидается:**
- Статус → `PROVISIONING` → `ACTIVE` (или снова `PROVISIONING_FAILED` если проблема не устранена)
- В процессе видно что провижн перезапустился

---

### MT-09 · PROVISIONING_FAILED — Cleanup `P0`

**Страница:** [/admin/tenants](http://localhost:5174/admin/tenants)

**Шаги:**
1. Найди тенант со статусом `PROVISIONING_FAILED`
2. Нажми **"Cleanup"**
3. Подтверди в диалоге

**Ожидается:**
- Тенант удалён из таблицы или перешёл в `PURGED`
- Частично созданные ресурсы в KC/ArcadeDB удалены

---

### MT-10 · Детальная страница тенанта `P0`

**Страница:** [/admin/tenants](http://localhost:5174/admin/tenants)

**Шаги:**
1. Кликни по строке тенанта `acme` или по его alias

**Ожидается:**
- Переход на [/admin/tenants/acme](http://localhost:5174/admin/tenants/acme)
- Видны все поля: alias, status, configVersion, keycloakOrgId, yggLineageDbName, friggDaliDbName, harvestCron, llmMode, dataRetentionDays, maxParseSessions, maxAtoms
- Кнопки: **Edit Config**, **Members**, **Refresh**

---

### MT-11 · Список участников тенанта `P0`

**Страница:** [/admin/tenants/acme/members](http://localhost:5174/admin/tenants/acme/members)

**Шаги:**
1. Открой детальную страницу тенанта `acme`
2. Нажми **"Members"** или открой URL напрямую

**Ожидается:**
- Список пользователей KC-организации `acme`
- Колонки: Username, Email, Role, Enabled
- В заголовке видно: **`acme · Members`**

---

### MT-12 · Инвайт участника в тенант `P0`

**Страница:** [/admin/tenants/acme/members](http://localhost:5174/admin/tenants/acme/members)

**Шаги:**
1. Нажми **"+ Invite"**
2. Проверь: в заголовке модала видно `→ acme`
3. Введи email: `testuser@example.com`, name: `Test User`, role: `viewer`
4. Submit

**Ожидается:**
- Модал показывает целевой тенант `acme` в заголовке
- Пользователь добавлен в список
- Появился в KC Organization `acme`

---

### MT-13 · Удаление участника из тенанта `P1`

**Страница:** [/admin/tenants/acme/members](http://localhost:5174/admin/tenants/acme/members)

**Шаги:**
1. Найди тестового пользователя
2. Нажми **"Remove from tenant"**
3. Подтверди в диалоге

**Ожидается:**
- Кнопка называется именно **"Remove from tenant"** (не просто "Remove")
- Диалог: "Remove `name (email)` from this tenant?"
- Участник удалён из списка (аккаунт в KC НЕ удалён)

---

### MT-14 · Редактирование конфига тенанта `P0`

> Требует роль **super-admin** (только superadmin видит кнопку Edit Config)

**Страница:** [/admin/tenants/acme](http://localhost:5174/admin/tenants/acme) → [/admin/tenants/acme/config](http://localhost:5174/admin/tenants/acme/config)

**Шаги:**
1. Открой детальную страницу `acme`
2. Нажми кнопку **"Edit Config"** (верхний правый угол)
3. Измени `harvestCron`: введи `0 0 * * 1` (понедельники в 00:00)
4. Нажми **Save**

**Ожидается:**
- Сохранено без ошибок
- `configVersion` увеличился на 1 (было N, стало N+1)

---

### MT-15 · Optimistic lock — конфликт версии `P1`

**Страница:** [/admin/tenants/acme/config](http://localhost:5174/admin/tenants/acme/config)

**Шаги:**
1. Открой конфиг тенанта в **двух вкладках** браузера
2. В первой вкладке измени и **Save**
3. Во второй вкладке (данные устарели) измени и нажми **Save**

**Ожидается:**
- `409 Conflict` — сообщение об ошибке версии конфига
- Предложение обновить страницу

---

### MT-16 · EventStream фильтр по тенанту `P1`

**Страница:** [/overview/events](http://localhost:5174/overview/events)

**Шаги:**
1. Открой Event Stream
2. Выбери `acme` в дропдауне фильтра по тенанту

**Ожидается:**
- Поток событий фильтруется: видны только события с `tenantAlias=acme`
- При выборе "Все" — видны все события

---

### MT-17 · События тегируются tenantAlias `P1`

**Страница:** [/overview/events](http://localhost:5174/overview/events)

**Шаги:**
1. Запусти Harvest для тенанта `acme`
2. Открой Event Stream без фильтра

**Ожидается:**
- События harvest содержат `tenantAlias: acme` в payload/tags
- Можно отфильтровать только их

---

### MT-18 · Изоляция: user тенанта A не видит данные B `P0`

**Шаги:**
1. Войди как `local.admin` (la@acme.com) — принадлежит тенанту `acme`
2. Попробуй открыть [/admin/tenants/other-tenant/members](http://localhost:5174/admin/tenants/other-tenant/members)

**Ожидается:**
- `403 Forbidden` или redirect
- Данные тенанта `other-tenant` недоступны

---

### MT-19 · Фильтр по статусу `P1`

**Страница:** [/admin/tenants](http://localhost:5174/admin/tenants)

**Шаги:**
1. В строке фильтров выбери статус **"SUSPENDED"**

**Ожидается:**
- Таблица показывает только SUSPENDED тенанты
- Остальные скрыты

---

### MT-20 · Фильтр по alias `P1`

**Страница:** [/admin/tenants](http://localhost:5174/admin/tenants)

**Шаги:**
1. Введи `acm` в строку поиска

**Ожидается:**
- Таблица фильтруется в реальном времени
- Показывает только тенанты с alias, содержащим `acm`

---

### MT-21 · Пагинация тенантов `P2`

**Страница:** [/admin/tenants](http://localhost:5174/admin/tenants)

**Шаги:**
1. Создай более 20 тенантов (или пропусти если их меньше)
2. Нажми **→** (следующая страница)

**Ожидается:**
- Следующая страница загружается
- Кнопки ← → видны

---

### MT-22 · Статус PROVISIONING_FAILED в фильтре `P1`

**Страница:** [/admin/tenants](http://localhost:5174/admin/tenants)

**Шаги:**
1. Выбери **"PROVISIONING_FAILED"** в фильтре статуса

**Ожидается:**
- Видны только тенанты с ошибкой провижна

---

### MT-23 · Кнопка Refresh `P2`

**Страница:** [/admin/tenants/acme](http://localhost:5174/admin/tenants/acme)

**Шаги:**
1. Нажми **"Refresh"** в правом верхнем углу

**Ожидается:**
- Данные обновляются без полной перезагрузки страницы
- Виден spinner во время загрузки

---

## Категория SEC — Security UI/UX (P0–P1)

### SEC-03 · Неаутентифицированный запрос → redirect login `P0`

**Шаги:**
1. Открой [/admin/tenants](http://localhost:5174/admin/tenants) в режиме **инкогнито** (без cookie)

**Ожидается:**
- Redirect на [/login](http://localhost:5174/login)
- Страница тенантов НЕ отображается

---

### SEC-04 · Сессия истекает → redirect login `P1`

**Шаги:**
1. Войди как admin
2. Открой DevTools → Application → Cookies
3. Удали cookie `sid`
4. Обнови страницу

**Ожидается:**
- Redirect на [/login](http://localhost:5174/login)

---

### SEC-06 · viewer/editor/operator → экран 403 в Heimdall `P0`

**Шаги:**
1. Войди как `viewer` (dev@seer.io)
2. Открой [http://localhost:5174](http://localhost:5174)

**Ожидается:**
- Экран **"Доступ запрещён 🚫"**
- Написано: _"Heimdall доступен только ролям: admin, super-admin, tenant-owner, local-admin, auditor"_
- Показана текущая роль пользователя
- Повтори для `editor` и `operator`

---

### SEC-07 · Rate limiting на /auth/login `P1`

**Страница:** [/login](http://localhost:5174/login)

**Шаги:**
1. Введи неверный пароль **10+ раз подряд**

**Ожидается:**
- Сообщение: **"Too Many Requests"** или `429`
- Дальнейшие попытки блокируются

---

### SEC-08 · API Key аутентификация `P1`

**Инструмент:** curl / [Keycloak Admin](http://localhost:18180/kc/admin)

**Шаги:**
1. Сгенерируй API Key для пользователя
2. Отправь: `curl -H "X-API-Key: <key>" http://localhost:3000/chur/api/admin/tenants`

**Ожидается:**
- `200 OK` с данными

---

### SEC-09 · Невалидный API Key → 401 `P1`

**Шаги:**
1. `curl -H "X-API-Key: invalid" http://localhost:3000/chur/api/admin/tenants`

**Ожидается:**
- `401 Unauthorized`

---

### SEC-10 · XSS: alias с `<script>` `P1`

**Страница:** [/admin/tenants](http://localhost:5174/admin/tenants) → "+ Create"

**Шаги:**
1. Попробуй создать тенант с alias: `<script>alert(1)</script>`

**Ожидается:**
- Валидация на фронте отклоняет (regex `/^[a-z0-9-]+$/`)
- Или бэкенд возвращает `400 Bad Request`
- Никакой JS не исполняется

---

## Категория CC — Cross-cutting UI (P1–P2)

### CC-01 · DEV секция для admin `P1`

**Страница:** [/overview/services](http://localhost:5174/overview/services)

**Шаги:**
1. Войди как `admin` (dev-режим: `npm run dev`)
2. Открой Services

**Ожидается:**
- Вкладка **"Dev"** видна
- Список dev-сервисов отображается

---

### CC-02 · DEV секция скрыта в prod `P1`

**Шаги:**
1. Открой staging-окружение (prod build)
2. Войди как admin

**Ожидается:**
- Вкладки "Dev" нет

---

### CC-03 · ServiceTopology — collapse/expand `P1`

**Страница:** [/overview/services](http://localhost:5174/overview/services)

**Шаги:**
1. По умолчанию граф свёрнут
2. Нажми **"▶ expand"**
3. Нажми **"▼ collapse"**

**Ожидается:**
- Граф разворачивается (LR направление — слева направо)
- Collapse сворачивает обратно

---

### CC-04 · ServiceTopology — ELK LR направление `P1`

**Страница:** [/overview/services](http://localhost:5174/overview/services)

**Шаги:**
1. Раскрой Service Topology

**Ожидается:**
- Граф идёт **слева направо**
- Входящие стрелки слева, исходящие справа

---

### CC-05 · i18n RU→EN `P2`

**Шаги:**
1. Переключи язык на **EN** (в профиле или заголовке)
2. Открой [/admin/tenants](http://localhost:5174/admin/tenants)

**Ожидается:**
- Все надписи на английском, включая статусы тенантов

---

### CC-06 · i18n EN→RU `P2`

**Шаги:**
1. Переключи обратно на **RU**
2. Проверь страницу тенантов

**Ожидается:**
- Все надписи на русском

---

### CC-07 · RoleGuard: admin видит "+ Create" `P1`

**Страница:** [/admin/tenants](http://localhost:5174/admin/tenants)

**Шаги:**
1. Войди как `admin`
2. Открой список тенантов

**Ожидается:**
- Кнопка **"+ Create"** видна в правом верхнем углу

---

### CC-08 · RoleGuard: local-admin не видит Create `P1`

**Страница:** [/admin/tenants](http://localhost:5174/admin/tenants)

**Шаги:**
1. Войди как `local.admin` (la@acme.com)
2. Открой список тенантов

**Ожидается:**
- Кнопки **"+ Create"** НЕТ

---

### CC-10 · ConsentModal при первом входе `P2`

**Шаги:**
1. Зайди под новым пользователем впервые
2. Или сбрось consent в настройках (DevTools: `localStorage.removeItem('seer-consent')`)

**Ожидается:**
- Модал T&C / Privacy появляется перед доступом к приложению

---

### CC-11 · Page title при навигации `P3`

**Шаги:**
1. Перейди на [/admin/tenants](http://localhost:5174/admin/tenants)
2. Смотри title вкладки браузера

**Ожидается:**
- Title: **"Tenants — Heimðallr"**
- При переходе на другую страницу title меняется

---

### CC-12 · viewer/editor/operator → 403 Heimdall `P0`

**Шаги:**
1. Войди как `viewer` (dev@seer.io / sae0611!)
2. Открой [http://localhost:5174](http://localhost:5174)
3. Повтори для `editor` (m.kuznetsova@seer.io)
4. Повтори для `alexey.petrov` (operator)

**Ожидается:**
- Все три роли видят экран **"🚫 Доступ запрещён"**
- Роль пользователя указана явно на экране

---

### CC-13 · Допущенные роли → доступ в Heimdall `P0`

**Шаги:**
1. Войди поочерёдно: `admin`, `superadmin`, `tenant.owner`, `local.admin`, `sergey.ivanov` (auditor)
2. Каждый раз открывай [/admin/tenants](http://localhost:5174/admin/tenants) или [/admin/users](http://localhost:5174/admin/users)

**Ожидается:**
- Все 5 ролей получают доступ — экрана 403 нет

---

### CC-14 · auditor — только чтение `P1`

**Страница:** [/admin/users](http://localhost:5174/admin/users)

**Шаги:**
1. Войди как `sergey.ivanov` (auditor)
2. Открой список пользователей

**Ожидается:**
- Список пользователей виден
- Кнопок **"Редакт."** и **"Заблокировать"** НЕТ

---

## Категория DL — Dali UI/Upload (P1–P2)

### DL-01 · Upload SQL файла `P1`

**Страница:** [/dali/sources](http://localhost:5174/dali/sources)

**Шаги:**
1. Выбери существующий источник
2. Нажми **Upload**
3. Выбери `.sql` файл

**Ожидается:**
- Файл загружен
- Job создан — виден в [JobRunr](http://localhost:9090/jobrunr)

---

### DL-02 · Job COMPLETED в JobRunr `P1`

**Страница:** [JobRunr UI](http://localhost:9090/jobrunr)

**Шаги:**
1. После upload подожди 30–120 сек
2. Найди job в списке

**Ожидается:**
- Статус job: **SUCCEEDED**

---

### DL-03 · Создание DaliSource `P1`

**Страница:** [/dali/sources](http://localhost:5174/dali/sources)

**Шаги:**
1. Нажми **"+ Add Source"**
2. Введи имя и параметры подключения
3. Save

**Ожидается:**
- Источник создан
- Появился в списке

---

### DL-04 · Список сессий Dali `P2`

**Страница:** [/dali/sessions](http://localhost:5174/dali/sessions)

**Шаги:**
1. Открой страницу сессий

**Ожидается:**
- Таблица с колонками: ID, Source, Status, Atoms, Resolution

---

### DL-06 · Retry failed Dali job `P2`

**Страница:** [JobRunr UI](http://localhost:9090/jobrunr)

**Шаги:**
1. Найди job в статусе **FAILED**
2. Нажми **Requeue**

**Ожидается:**
- Job перезапущен, статус → PROCESSING

---

## Категория VD — Verdandi UI (P1–P3)

> Открой Verdandi: [http://localhost:5173](http://localhost:5173)

### VD-01 · SQL инспектор — открыть сессию `P1`

**Шаги:**
1. Выбери сессию Dali из списка

**Ожидается:**
- Граф запроса отображается в канвасе

---

### VD-02 · Command Palette `P2`

**Шаги:**
1. Нажми **Cmd+K** (Mac) или **Ctrl+K** (Win)
2. Введи текст поиска

**Ожидается:**
- Palette открывается
- Результаты появляются в реальном времени

---

### VD-03 · Undo/Redo в канвасе `P2`

**Шаги:**
1. Переместим ноду в канвасе
2. Нажми **Ctrl+Z**
3. Нажми **Ctrl+Y**

**Ожидается:**
- Undo возвращает ноду на место
- Redo двигает снова

---

### VD-04 · Фильтрация нод `P2`

**Шаги:**
1. Введи текст в строку фильтра нод

**Ожидается:**
- Подходящие ноды подсвечены
- Остальные скрыты или приглушены

---

### VD-05 · Профиль — сохранение настроек `P3`

**Шаги:**
1. Открой ProfileModal (аватар → профиль)
2. Измени тему
3. Закрой и открой снова

**Ожидается:**
- Тема сохранилась
- Настройки персистируются после перезагрузки

---

## Результаты

Фиксируй результаты в **[QA Dashboard](http://localhost:5201/index.html)** → вкладка **Ручные тесты**.

| Статус | Значение |
|--------|----------|
| ✓ PASS | Тест прошёл |
| ✗ FAIL | Тест упал — опиши в заметках |
| ⊘ SKIP | Пропущен (нет данных / окружение) |
| ? UNCLEAR | Непонятные шаги или ожидаемый результат |
