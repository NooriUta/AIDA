# AIDA — Repository Migration Plan

**Документ:** `REPO_MIGRATION_PLAN`
**Версия:** 2.1
**Дата:** 12.04.2026
**Статус:** Working document — план реконструкции репозиториев

> **v2.0 vs v1.0:** полный пересмотр на основе детального анализа кодовой базы. Зафиксированы конкретные design decisions (D1-D10), git-стратегия, принцип self-contained modules, критические файловые изменения с конкретными строками.

---

## 0. Контекст и цели

**Текущая проблема:**
- Код разбит по двум несвязанным директориям (`Dali4/HOUND/Hound/` и `SEERStudio/VERDANDI/`)
- Два вложенных git репозитория (`SEERStudio/.git` и `SEERStudio/VERDANDI/.git`)
- Невозможно открыть все модули в одном IDEA проекте

**Цель:** единый `aida-root/` monorepo с Gradle multi-module структурой, где каждый JVM-модуль остаётся самостоятельным Gradle-проектом (открывается и собирается отдельно в IDEA).

**Scope строго ограничен — только структурная миграция:**

| Входит ✅ | Не входит ❌ |
|---|---|
| Перенос файлов в целевую структуру | Upgrade ArcadeDB (остаётся 25.12.1) |
| Git стратегия (новый repo + история) | Пустые модули-заглушки (dali, mimir, anvil) |
| Обновление build paths | Смена GHCR namespace (verdandi/*) |
| CI/CD адаптация | Feature-работа любого рода |
| Документация | Новые зависимости |

---

## 1. Целевая структура

```
aida-root/                          ← git init, новый GitHub repo (NooriUta/aida)
├── settings.gradle                 ← include 'libraries:hound', 'services:shuttle'
├── build.gradle                    ← ТОЛЬКО Node.js оркестрация (devAll, churDev, verdandiDev)
├── gradle.properties               ← daemon settings (НЕ Quarkus vars — они в shuttle/)
├── gradlew / gradlew.bat
├── Makefile                        ← make dev | build | test | docker-up
├── docker-compose.yml              ← dev stack (keycloak + shuttle + chur + verdandi)
├── docker-compose.prod.yml         ← prod stack (ghcr.io images)
├── .env.example
├── .gitignore
├── .github/workflows/              ← CI (hound + shuttle + chur + verdandi), CD
│
├── libraries/
│   └── hound/                      ← самостоятельный Gradle project
│       ├── settings.gradle         ← rootProject.name = 'hound'
│       ├── build.gradle            ← java-library + application, ANTLR, fatJar, все задачи
│       ├── gradle.properties       ← -Xmx12g JVM args
│       ├── gradlew / gradlew.bat   ← свой wrapper
│       ├── lib/                    ← ANTLR complete JAR
│       └── src/
│
├── services/
│   └── shuttle/                    ← самостоятельный Gradle project
│       ├── settings.gradle         ← rootProject.name = 'shuttle' + pluginManagement
│       ├── build.gradle            ← Quarkus 3, GraphQL, все зависимости
│       ├── gradle.properties       ← Quarkus BOM vars
│       ├── gradlew / gradlew.bat   ← свой wrapper
│       ├── Dockerfile              ← self-contained (свой gradlew внутри)
│       └── src/
│
├── bff/
│   └── chur/
│       ├── package.json
│       ├── tsconfig.json
│       ├── Dockerfile
│       └── src/
│
├── frontends/
│   ├── verdandi/                   ← LOOM + KNOT + ANVIL UI + MIMIR Chat
│   │   ├── package.json
│   │   ├── vite.config.ts
│   │   ├── Dockerfile
│   │   └── src/
│   ├── heimdall-frontend/          ← Admin panel (создаётся в рамках HEIMDALL sprint)
│   ├── urd/                        ← Time-travel / history (planned, post-HighLoad)
│   └── skuld/                      ← Proposals / future state (planned, post-HighLoad)
│
├── packages/                        ← shared npm packages (NEW — ADR-DA-013)
│   └── aida-shared/
│       ├── package.json
│       └── src/
│           ├── navigation.ts        ← navigateTo, buildAppUrl, useAppContext
│           ├── auth.ts              ← useUser, ChurAuthProvider
│           ├── tokens.css           ← design tokens
│           └── index.ts
│
├── infra/
│   ├── keycloak/
│   │   └── seer-realm.json
│   └── nginx/
│       └── seer-studio.conf        ← path routing (§2.4)
│
├── scripts/
│   └── rollback.sh
│
└── docs/
    ├── DEVELOPMENT.md
    ├── ARCHITECTURE.md
    ├── sprints/                    ← из current_pharse/
    ├── internal/                   ← из internal_docs/
    └── hound/                      ← BENCH/DETAIL отчёты
```

---

## 2. Принцип: каждый JVM-модуль self-contained (D10)

| Модуль | settings.gradle | gradlew | build.gradle | gradle.properties | Открытие в IDEA |
|---|---|---|---|---|---|
| `libraries/hound/` | `rootProject.name='hound'` | Свой | Полный (repos, java, deps, tasks) | `-Xmx12g` | Открыть папку → standalone |
| `services/shuttle/` | `rootProject.name='shuttle'` + pluginMgmt | Свой | Полный (repos, java, Quarkus, deps) | Quarkus BOM | Открыть папку → standalone |
| `bff/chur/` | — | — | — | — | package.json → Node project |
| `frontends/verdandi/` | — | — | — | — | package.json → Node project |

**Как это работает:**
- Открыть `aida-root/` в IDEA → root `settings.gradle` → все модули видны
- Открыть `libraries/hound/` в IDEA → local `settings.gradle` → только Hound
- Открыть `services/shuttle/` в IDEA → local `settings.gradle` → только SHUTTLE
- Gradle игнорирует `settings.gradle` подпроектов при сборке из root — конфликтов нет

---

## 3. Design Decisions

### D1 — Git: новый repo, сохранить историю VERDANDI

- VERDANDI: 86 значимых коммитов → **сохранить** через `git merge --allow-unrelated-histories`
- SEERStudio wrapper: 10 коммитов → **бросить**
- Hound: git-истории нет → добавить как fresh commit
- Предусловие: смержить `brandbook/seer-studio` → `master` в VERDANDI repo
- Расположение: `C:\AIDA\aida-root\` — рядом с `Dali4/` и `SEERStudio/`
- GitHub: новый repo `NooriUta/aida`, архивировать `NooriUta/Verdandi`

### D2 — Gradle wrapper: 9.0.0 везде

Все три существующих wrapper уже на Gradle 9.0.0. Конфликтов нет.

### D3 — ArcadeDB: остаётся 25.12.1

Upgrade до 26.x — отдельная задача (C.0) после миграции репо.

### D4 — Hound build.gradle: критический fix одна строка

405-строчный `build.gradle` — оставить как есть. **Единственное обязательное изменение — строка 84:**

```groovy
// БЫЛО (сломается в monorepo — rootDir будет aida-root/):
file("${rootDir}/lib/antlr-4.13.2-complete.jar")

// СТАЛО:
file("${projectDir}/lib/antlr-4.13.2-complete.jar")
```

> ⚠️ Предварительно: в `Dali4/HOUND/Hound/lib/` лежит `antlr-4.13.1-complete.jar`, а build.gradle ссылается на `4.13.2`. Исправить до миграции (Phase 0.2).

### D5 — root build.gradle: только Node.js оркестрация

Портировать строки 25-132 из `SEERStudio/VERDANDI/build.gradle`. **Без `subprojects {}` блока** — каждый JVM модуль полностью self-contained.

```groovy
def churDir     = file('bff/chur')       // было: file('Chur')
def verdandiDir = file('frontends/verdandi')  // было: file('verdandi')
```

### D6 — SHUTTLE pom.xml: удалить

Ссылается на устаревший Quarkus 3.17.7, build.gradle использует 3.34.2. CI/CD и Dockerfile используют Gradle. Удалить.

### D7 — SHUTTLE Dockerfile: self-contained, без изменений

Сохраняет свой gradlew, settings.gradle, gradle.properties. Context остаётся `./services/shuttle`.

### D8 — Placeholder modules: не создавать

Не создавать `dali/`, `mimir/`, `anvil/`, `heimdall-*`. Добавление займёт одну строку `include` + `build.gradle` когда понадобится. Исключение: `shared/dali-models/` — только когда реально нужен первый cross-module dependency.

### D9 — GHCR namespace: изменить отдельным PR

Оставить `ghcr.io/nooriuta/verdandi/*`. Смена в том же PR рискует сломать production.

### D10 — Standalone IDEA opening

(см. раздел 2)

---

## 4. Критические файловые изменения

| Файл | Действие | Ключевое изменение |
|---|---|---|
| `Dali4/HOUND/Hound/build.gradle` (405 строк) | Адаптировать → `libraries/hound/build.gradle` | Строка 84: `${rootDir}` → `${projectDir}`; добавить `java-library` plugin |
| `SEERStudio/VERDANDI/build.gradle` (132 строки) | Портировать Node.js tasks → новый root `build.gradle` | Обновить `churDir`, `verdandiDir`; без `subprojects {}` |
| `SEERStudio/VERDANDI/settings.gradle` (15 строк) | Заменить → новый root `settings.gradle` | Добавить модули с `projectDir` маппингами |
| `SEERStudio/VERDANDI/docker-compose.yml` (89 строк) | Обновить build contexts + сеть | `./SHUTTLE` → context + dockerfile; rename `verdandi_net` → `aida_net` |
| `SEERStudio/VERDANDI/SHUTTLE/Dockerfile` (18 строк) | Без изменений | Context остаётся `./services/shuttle` |
| `SEERStudio/VERDANDI/SHUTTLE/build.gradle` (44 строки) | Оставить как есть | Только удалить `pom.xml` |
| `SEERStudio/VERDANDI/SHUTTLE/settings.gradle` | Переписать | Standalone root: `rootProject.name = 'shuttle'` |
| `SEERStudio/VERDANDI/.github/workflows/ci.yml` (71 строка) | Обновить + добавить Hound job | `working-directory` updates, новый hound job |
| `SEERStudio/VERDANDI/.github/workflows/cd.yml` | Обновить | Matrix service paths |

---

## 5. Пошаговая реализация (6 фаз)

### Phase 0 — Pre-migration

```bash
# 0.1 Верификация всех билдов (read-only)
cd Dali4/HOUND/Hound && ./gradlew test
cd SEERStudio/VERDANDI && ./gradlew :SHUTTLE:test
cd SEERStudio/VERDANDI/Chur && npm ci && npm test
cd SEERStudio/VERDANDI/verdandi && npm ci && npm test

# 0.2 Fix ANTLR JAR mismatch
# lib/ имеет antlr-4.13.1, build.gradle ссылается на 4.13.2
# Скачать нужный JAR или обновить ссылку в build.gradle:84

# 0.3 Merge brandbook → master в VERDANDI repo
cd SEERStudio/VERDANDI
git checkout master && git merge brandbook/seer-studio && git push origin master

# 0.4 Backup
git tag pre-migration    # в VERDANDI repo (на смерженном master)
# Zip C:\AIDA как restore point
```

---

### Phase 1 — Monorepo skeleton

```powershell
# 1.1 Init
mkdir C:\AIDA\aida-root && cd C:\AIDA\aida-root && git init

# 1.2 Copy gradle wrapper из SEERStudio/VERDANDI/
```

**`settings.gradle`:**
```groovy
pluginManagement {
    repositories { mavenCentral(); gradlePluginPortal() }
}
rootProject.name = 'aida-root'

include 'libraries:hound'
include 'services:shuttle'
// include 'shared:dali-models'  // раскомментировать когда понадобится
```

**`gradle.properties`:**
```properties
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=512m
org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.configureondemand=true
# НЕ Quarkus BOM vars (они в services/shuttle/)
# НЕ -Xmx12g (она в libraries/hound/)
```

```bash
# 1.7 Directories
mkdir -p libraries/hound services/shuttle bff/chur frontends/verdandi \
         infra/keycloak docs/sprints docs/internal docs/hound scripts
```

Верификация: `./gradlew tasks` без ошибок.

---

### Phase 2 — Hound → `libraries/hound/`

```bash
# 2.1 Source
cp -r Dali4/HOUND/Hound/src/ libraries/hound/src/
cp -r Dali4/HOUND/Hound/lib/ libraries/hound/lib/

# 2.2 Build infrastructure (standalone)
cp Dali4/HOUND/Hound/gradlew* libraries/hound/
cp -r Dali4/HOUND/Hound/gradle/ libraries/hound/gradle/
cp Dali4/HOUND/Hound/gradle.properties libraries/hound/gradle.properties
```

**`libraries/hound/settings.gradle`:**
```groovy
rootProject.name = 'hound'
```

**`libraries/hound/build.gradle`** — на основе существующего, изменить:
```groovy
plugins {
    id 'java-library'   // ← добавить
    id 'application'    // ← оставить
    // остальные как есть
}

// Строка 84:
antlrJar = file("${projectDir}/lib/antlr-4.13.2-complete.jar")
//                ^^^^^^^^^^^^ было rootDir
```

Верификация:
```bash
./gradlew :libraries:hound:test    # 24 теста проходят
cd libraries/hound && ./gradlew build  # standalone
```

---

### Phase 3 — Import VERDANDI + SHUTTLE

```bash
# 3.1 Импорт истории
cd aida-root
git remote add verdandi-import <VERDANDI_REPO_PATH>
git fetch verdandi-import master
git merge verdandi-import/master --allow-unrelated-histories \
    -m "Import VERDANDI history (86 commits)"
git remote remove verdandi-import
# При конфликте build.gradle/settings.gradle — оставить наши версии (Phase 1)

# 3.2 Переместить SHUTTLE
git mv SHUTTLE services/shuttle

# 3.3 Очистить
git rm services/shuttle/pom.xml   # stale Quarkus 3.17.7
```

Обновить `services/shuttle/settings.gradle`:
```groovy
pluginManagement {
    repositories { mavenCentral(); gradlePluginPortal() }
}
rootProject.name = 'shuttle'
```

Обновить `docker-compose.yml` для SHUTTLE:
```yaml
shuttle:
  build:
    context: ./services/shuttle   # было: ./SHUTTLE
    dockerfile: Dockerfile
```

Верификация:
```bash
./gradlew :services:shuttle:test
cd services/shuttle && ./gradlew build  # standalone
```

---

### Phase 4 — Node.js + infra

```bash
git mv Chur bff/chur
git mv verdandi frontends/verdandi
git mv keycloak infra/keycloak

# Очистить мусор от импорта
git rm -r .claude/
# Прочие stale root-файлы от старого VERDANDI root
```

**docker-compose.yml обновления:**
```yaml
networks:
  aida_net:         # было: verdandi_net

services:
  chur:
    build:
      context: ./bff/chur           # было: ./Chur
  verdandi:
    build:
      context: ./frontends/verdandi  # было: ./verdandi
  keycloak:
    volumes:
      - ./infra/keycloak/seer-realm.json:...  # было: ./keycloak/...
```

Верификация:
```bash
cd bff/chur && npm ci && npm test
cd frontends/verdandi && npm ci && npm test
./gradlew devAll
docker compose build && docker compose up -d
```

---

### Phase 5 — CI/CD + Makefile

**`.github/workflows/ci.yml`:**

```yaml
jobs:
  hound:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: 21, cache: gradle }
      - run: ./gradlew :libraries:hound:test

  shuttle:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: 21, cache: gradle }
      - run: ./gradlew :services:shuttle:test
      - run: ./gradlew :services:shuttle:build -x test

  chur:
    runs-on: ubuntu-latest
    defaults: { run: { working-directory: bff/chur } }
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with: { node-version: 24, cache: npm, cache-dependency-path: bff/chur/package-lock.json }
      - run: npm ci && npm test && npm run build

  verdandi:
    runs-on: ubuntu-latest
    defaults: { run: { working-directory: frontends/verdandi } }
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with: { node-version: 24, cache: npm, cache-dependency-path: frontends/verdandi/package-lock.json }
      - run: npm ci && npm test && npm run test:coverage && npx vite build
```

**`Makefile`:**

```makefile
.PHONY: dev build test docker-up docker-down clean

dev:
	./gradlew devAll

build:
	./gradlew :libraries:hound:build :services:shuttle:build
	cd bff/chur && npm ci && npm run build
	cd frontends/verdandi && npm ci && npm run build

test:
	./gradlew :libraries:hound:test :services:shuttle:test
	cd bff/chur && npm test
	cd frontends/verdandi && npm test

docker-up:
	docker compose up -d --build

docker-down:
	docker compose down

clean:
	./gradlew clean
	rm -rf bff/chur/node_modules frontends/verdandi/node_modules
	rm -rf frontends/verdandi/dist frontends/verdandi/coverage
```

---

### Phase 6 — Документация и финал

```bash
# 6.1 Docs
git mv current_pharse/* docs/sprints/
git mv internal_docs/*  docs/internal/
# BENCH_REPORT_*.md, DETAIL_REPORT_*.md → docs/hound/

# 6.5 Push
git add -A
git commit -m "Complete monorepo migration to aida-root structure"
git remote add origin https://github.com/NooriUta/aida.git
git push -u origin main

# 6.6 Archive
# GitHub: Settings → Archive NooriUta/Verdandi
```

Создать:
- `docs/MIGRATION.md` — маппинг путей old → new, что удалено, как откатиться
- `docs/DEVELOPMENT.md` — setup, запуск, IDEA, Makefile targets
- `docs/ARCHITECTURE.md` — data flow, порты, auth

---

## 6. Verification Checklist

### Monorepo mode (из `aida-root/`):

- [ ] `./gradlew :libraries:hound:test` — 24 теста проходят
- [ ] `./gradlew :libraries:hound:fatJar` — standalone JAR
- [ ] `./gradlew :services:shuttle:test` — тесты проходят
- [ ] `./gradlew :services:shuttle:build` — uber-jar
- [ ] `cd bff/chur && npm ci && npm test`
- [ ] `cd frontends/verdandi && npm ci && npm test`
- [ ] `cd frontends/verdandi && npx vite build`
- [ ] `./gradlew devAll` — все 3 сервиса стартуют
- [ ] `docker compose build` — все образы
- [ ] `docker compose up -d` — полный стек, health checks
- [ ] `make test` — Makefile оркестрация

### Standalone IDEA mode:

- [ ] `cd libraries/hound && ./gradlew build` — независимо
- [ ] `cd services/shuttle && ./gradlew build` — независимо
- [ ] Открыть `libraries/hound/` в IDEA → standalone Gradle project
- [ ] Открыть `services/shuttle/` в IDEA → standalone Gradle project
- [ ] Открыть `bff/chur/` в IDEA → Node.js project
- [ ] Открыть `frontends/verdandi/` в IDEA → Node.js project

---

## 7. После миграции — что следует дальше

| Задача | Связь | Когда |
|---|---|---|
| C.0 Hound тесты → network mode (ArcadeDB 26.x) | Hound уже в `libraries/hound/` | Сразу после Phase 2 |
| C.1 Hound library refactor (HoundConfig, API) | `java-library` plugin уже добавлен | Week 2 |
| Dali Core scaffold в `services/dali/` | `aida-root/settings.gradle` — добавить одну строку | Week 3 |
| HEIMDALL backend scaffold | То же | Week 3 |
| Смена GHCR namespace | Отдельный PR, не блокирует | После стабилизации |

---

## История изменений

| Дата | Версия | Что |
|---|---|---|
| 12.04.2026 | 1.0 | Initial. As-Is из реального индекса проекта. |
| 12.04.2026 | 2.0 | Полный пересмотр. D1-D10 design decisions. Git стратегия: новый repo NooriUta/aida + история VERDANDI через merge. Self-contained modules (D10). Критический fix build.gradle:84. Без ArcadeDB upgrade, без placeholder modules. Конкретные команды по 6 фазам. |

| 12.04.2026 | 2.1 | **Frontend architecture добавлена.** ADR-DA-012/013. `packages/aida-shared/` добавлена в структуру (navigation utils + auth + tokens). `frontends/` расширен: `urd/`, `skuld/` (planned), `heimdall-frontend/`. `infra/nginx/` для path routing конфига. |
