# Security Demo Sprint — 15.04.2026

**Ветка:** `fix/security-demo-apr15-2026`
**Цель:** закрыть все ❌ из QG-Security перед HighLoad-демо
**Дедлайн:** до запуска демо

---

## Контекст

Запуск scheduled-задачи `qg-security-demo` (15.04.2026) выявил 4 критических нарушения:

| # | Нарушение | Severity |
|---|-----------|----------|
| INV-1 | KC `:18180` открыт на `0.0.0.0` | 🔴 CRITICAL |
| INV-2 | YGG `:2480` открыт на `0.0.0.0` | 🔴 CRITICAL |
| INV-3 | FRIGG `:2481` открыт на `0.0.0.0` | 🔴 CRITICAL |
| INV-8 | Простые пароли `*123` в `seer-realm.json` | 🔴 CRITICAL |
| INV-10 | Token lifetime 300s (5 мин) — нужно ≥ 14400s | 🟠 HIGH |
| INV-7 | `.env` отсутствует — `ANTHROPIC_API_KEY` не настроен | 🟡 MEDIUM |

Прошедшие проверки: INV-4 ✅ INV-5 ✅ INV-6 ✅ INV-11 ✅

---

## Перед началом (выполнено)

- [x] Создать ветку: `git checkout -b fix/security-demo-apr15-2026`
- [x] Сохранить план: `docs/sprints/SECURITY_DEMO_SPRINT_APR15_2026.md`

---

## Задачи спринта

### TASK-1 — FIX-A: Bind портов на 127.0.0.1 в docker-compose.yml

**Файл:** `docker-compose.yml`

Изменить три port-маппинга:

```yaml
# Keycloak
- "127.0.0.1:18180:8080"   # было: "18180:8180"  ← и порт контейнера неверный!

# HoundArcade (YGG)
- "127.0.0.1:2480:2480"    # было: "2480:2480"
- "127.0.0.1:2424:2424"    # если есть — тоже bind

# frigg
- "127.0.0.1:2481:2480"    # было: "2481:2480"
```

**Проверка:**
```bash
grep -E "18180|2480|2481" docker-compose.yml | grep -v "127.0.0.1" | grep -v "#" | grep -v "http://"
# → пустой вывод = ✅
```

**После:** `docker compose down && docker compose up -d`

---

### TASK-2 — FIX-C: Убрать простые пароли из seer-realm.json

**Файл:** `infra/keycloak/seer-realm.json`

Удалить блок `"credentials"` у каждого пользователя:
```json
// УБРАТЬ у operator, auditor, owner, localadmin:
"credentials": [{ "type": "password", "value": "operator123", "temporary": true }]
```

Пользователи останутся без начального пароля — задать через KC Admin API после запуска:
```bash
# Инструкция в FIX-C раздела SKILL.md qg-security-demo
# Использовать KC Admin API: reset-password для каждого user_id
```

**Проверка:**
```bash
grep -E "operator123|auditor123|owner123|localadmin123" infra/keycloak/seer-realm.json
# → пустой вывод = ✅
```

---

### TASK-3 — FIX-D: Token lifetime ≥ 4 часа

**Файл:** `infra/keycloak/seer-realm.json`

Добавить/исправить в корне realm-объекта:
```json
{
  "accessTokenLifespan": 14400,
  "ssoSessionMaxLifespan": 36000,
  "ssoSessionIdleTimeout": 36000
}
```

**Проверка:**
```bash
node -e "const r=require('./infra/keycloak/seer-realm.json'); console.log('TTL:', r.accessTokenLifespan, r.accessTokenLifespan>=14400?'✅':'❌')"
```

**После:** переимпортировать realm (KC restart с `--import-realm`).

---

### TASK-4 — INV-7: Создать .env.example

**Файл:** `.env.example` (новый)

```env
# ANTHROPIC — использовать отдельный demo/HighLoad ключ, НЕ production!
# После демо — ротировать этот ключ на console.anthropic.com
ANTHROPIC_API_KEY=sk-ant-demo-...

# ArcadeDB
ARCADEDB_URL=http://localhost:2480
ARCADEDB_USER=root
ARCADEDB_PASSWORD=changeme

# Keycloak
KC_ADMIN_PASSWORD=changeme
```

Добавить в README или DEVELOPMENT.md напоминание: скопировать `.env.example` → `.env` и заполнить реальными значениями.

**Проверка:**
- `.env.example` не содержит реальных ключей
- `.env` по-прежнему не tracked (`git ls-files .env` → пусто)

---

## Порядок выполнения

```
TASK-1 (docker-compose) → TASK-3 (token TTL в realm JSON)
                        ↘
                         TASK-2 (убрать пароли из realm JSON)  → перезапустить KC
                                                                → TASK-4 (.env.example)
```

TASK-2 и TASK-3 правят один файл — делать последовательно, в одном коммите.

---

## Коммиты

```bash
git add docker-compose.yml
git commit -m "fix(infra): bind KC/YGG/FRIGG ports to 127.0.0.1"

git add infra/keycloak/seer-realm.json
git commit -m "fix(keycloak): remove plain passwords, set token TTL to 4h"

git add .env.example
git commit -m "chore: add .env.example with demo key instructions"
```

---

## Definition of Done

```
[ ] TASK-1: grep портов не выдаёт строк без 127.0.0.1
[ ] TASK-2: grep *123 в realm JSON пуст
[ ] TASK-3: accessTokenLifespan = 14400
[ ] TASK-4: .env.example существует, .env не tracked
[ ] QG-Security повторный запуск: все INV ✅
[ ] PR смержен в master
```

---

## История QG-Security

| Дата | Ports | .env safe | Пароли | Token TTL | CORS | Итог |
|------|-------|-----------|--------|-----------|------|------|
| 13.04.2026 | ❌ 0.0.0.0 | ⏳ | ❌ 123 | ⏳ | ⏳ | ❌ FAIL |
| 15.04.2026 | ❌ 0.0.0.0 | ✅ | ❌ 123 | ❌ 5min | ✅ | ❌ FAIL |
| _после спринта_ | _⏳_ | _⏳_ | _⏳_ | _⏳_ | _✅_ | _⏳_ |
