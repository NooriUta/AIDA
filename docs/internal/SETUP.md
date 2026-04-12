# VERDANDI — Установка и запуск

## Структура проекта

```
(Ваш путь)/VERDANDI/
├── verdandi/       — LOOM frontend       (React 19 + Vite)
├── SHUTTLE/        — GraphQL API         (Quarkus 3.34.2 + Java 21)
├── Chur/           — Auth gateway        (Fastify 4 + Node.js)
├── gradlew         — Gradle wrapper      (запуск из корня)
└── settings.gradle — Monorepo config
```

---

## Требования

| Инструмент | Версия | Проверка |
|------------|--------|----------|
| Java (Eclipse Temurin) | 21+ | `java --version` |
| Node.js | 18+ | `node --version` |
| ArcadeDB | любая актуальная | `curl http://localhost:2480/api/v1/ready` |

> **Node.js** нужен только для `Chur` и `verdandi`.
> Gradle скачивается автоматически через `gradlew` при первом запуске.

---

## Установка зависимостей

### Chur (один раз)

```bash
cd (Ваш путь)/VERDANDI/Chur
npm install
```

### verdandi (один раз)

```bash
cd (Ваш путь)/VERDANDI/verdandi
npm install
```

### SHUTTLE

Gradle сам скачает все зависимости при первом запуске `quarkusDev`.
Требует доступ к Maven Central (https://repo1.maven.org).

---

## Первый запуск: создать пользователя в БД

Перед первым входом нужно создать пользователя `admin` в ArcadeDB.

ArcadeDB должен быть запущен на порту **2480** (проект Hound).

```bash
# Создать тип User (один раз)
curl -u root:playwithdata -X POST http://localhost:2480/api/v1/command/hound \
  -H "Content-Type: application/json" \
  -d '{"language":"sql","command":"CREATE DOCUMENT TYPE User IF NOT EXISTS"}'

# Вставить пользователя admin (один раз)
# Хэш ниже соответствует паролю "admin" (bcrypt, 12 rounds)
curl -u root:playwithdata -X POST http://localhost:2480/api/v1/command/hound \
  -H "Content-Type: application/json" \
  -d '{"language":"sql","command":"INSERT INTO User SET username='\''admin'\'', password_hash='\''$2a$12$/1FUXE56Or60mn8vwCKJPOc5BBwInhbLkDoUV94mskoutC0JfrqSa'\'', role='\''admin'\''"}'
```

> Чтобы сменить пароль — сгенерируй новый хэш через `bcrypt` и обнови запись в БД.

---

## Запуск

Запускать сервисы **в следующем порядке**:

```
1. ArcadeDB (Hound)   → порт 2480
2. SHUTTLE            → порт 8080
3. Chur               → порт 3000
4. verdandi           → порт 5173
```

---

### 1. ArcadeDB

Запускается отдельно как часть проекта **Hound**.
Убедись что доступен: `curl http://localhost:2480/api/v1/ready`

---

### 2. SHUTTLE — Quarkus GraphQL API (порт 8080)

Из корня VERDANDI:

```bash
cd (Ваш путь)/VERDANDI
./gradlew :SHUTTLE:quarkusDev
```

**Или из IntelliJ IDEA:**
Gradle panel → `VERDANDI` → `:SHUTTLE` → `Tasks` → `quarkus` → `quarkusDev`

Готово когда в консоли появится:
```
lineage-api 1.0.0-SNAPSHOT on JVM (powered by Quarkus 3.34.2) started in X.Xs.
Listening on: http://0.0.0.0:8080
```

---

### 3. Chur — Auth gateway (порт 3000)

```bash
cd (Ваш путь)/VERDANDI/Chur
node node_modules/tsx/dist/cli.mjs src/server.ts
```

Или через npm:

```bash
cd (Ваш путь)/VERDANDI/Chur
npm run dev
```

Готово когда видишь:
```
Server listening at http://0.0.0.0:3000
```

---

### 4. verdandi — LOOM frontend (порт 5173)

```bash
cd (Ваш путь)/VERDANDI/verdandi
npm run dev
```

Готово когда видишь:
```
VITE v8.x.x  ready in Xs
➜  Local:   http://localhost:5173/
```

---

## Открыть в браузере

**http://localhost:5173**

Логин: `admin` / `admin`

---

## Переменные окружения

По умолчанию все сервисы работают с localhost без дополнительной конфигурации.
Для изменения адресов создай `.env` файлы:

### verdandi/.env

```env
VITE_AUTH_URL=http://localhost:3000/auth
VITE_GRAPHQL_URL=http://localhost:3000/graphql
```

### Chur — src/config.ts

Настройки порта, JWT-секрета и адреса ArcadeDB хранятся в `Chur/src/config.ts`.

### SHUTTLE — SHUTTLE/src/main/resources/application.properties

```properties
quarkus.http.port=8080
arcade.url=http://localhost:2480
arcade.db=hound
arcade.user=root
arcade.password=playwithdata
```

---

## Проверка работоспособности

```bash
# ArcadeDB
curl http://localhost:2480/api/v1/ready

# SHUTTLE (напрямую, без авторизации)
curl http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -H "X-Seer-Role: admin" \
  -H "X-Seer-User: admin" \
  -d '{"query":"{ overview { id name tableCount } }"}'

# Chur health
curl http://localhost:3000/health

# Полная цепочка (через Chur, с авторизацией)
curl -c /tmp/cookies.txt -X POST http://localhost:3000/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'

curl -b /tmp/cookies.txt http://localhost:3000/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ overview { id name tableCount } }"}'
```

---

## Остановка

Остановить все сервисы: `Ctrl+C` в каждом терминале.

Или убить процессы:

```bash
# Windows
taskkill /F /IM java.exe      # SHUTTLE
taskkill /F /IM node.exe      # Chur + verdandi
```
