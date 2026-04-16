# ArcadeDB 26.3.1 — временно убрать Embedded mode из Hound + version audit

## Context

При апгрейде ArcadeDB 25.12.1 → 26.3.x API `arcadedb-engine` (embedded mode) изменился вместе
с переходом на native OpenCypher и ANTLR4 SQL parser. Чтобы не бороться одновременно с
engine-API-breaking-changes и remote-client-breaking-changes, временно убираем Embedded mode
из Hound: оставляем только REMOTE и REMOTE_BATCH. Одновременно поднимаем `arcadedb-network`
с `25.12.1` до `26.3.1` и убеждаемся, что нигде в репозитории не осталось старой версии.

Результат: Hound собирается, все remote-тесты проходят, embedded-тесты явно помечены
`@Disabled` с TODO-коментарием, старые версии arcadedb нигде не встречаются.

---

## Перед началом

1. `git checkout -b chore/arcadedb-26-no-embedded`
2. Сохранить этот план в `docs/sprints/arcadedb-26-no-embedded.md`

---

## Step 1 — Version audit (read-only)

Убедиться, что других мест с `25.12.1` нет:
```
grep -r "25\.12\.1" .
grep -r "arcadedb" . --include="*.gradle" --include="*.yml" --include="*.yaml" --include="*.properties"
```

Ожидаемые находки:
| Файл | Текущее значение | Целевое |
|------|-----------------|---------|
| `libraries/hound/build.gradle` L51 | `arcadedb-engine:25.12.1` | **удалить** |
| `libraries/hound/build.gradle` L52 | `arcadedb-network:25.12.1` | `26.3.1` |
| `docker-compose.yml` (FRIGG service) | `arcadedata/arcadedb:26.3.2` | оставить (уже выше) |

> SHUTTLE не имеет прямой зависимости на arcadedb — использует Quarkus REST HTTP client.
> Никакие другие Gradle-модули не подключают arcadedb.

---

## Step 2 — `libraries/hound/build.gradle`

**Файл:** `libraries/hound/build.gradle` строки 51-52

```gradle
// БЫЛО:
implementation 'com.arcadedb:arcadedb-engine:25.12.1'
implementation 'com.arcadedb:arcadedb-network:25.12.1'

// СТАЛО:
// arcadedb-engine удалён — Embedded mode временно отключён (TODO: restore after 26.x API stabilisation)
implementation 'com.arcadedb:arcadedb-network:26.3.1'
```

---

## Step 3 — Удалить `EmbeddedWriter.java`

**Файл:** `libraries/hound/src/main/java/com/hound/storage/EmbeddedWriter.java`

Целиком удалить. Импортирует `com.arcadedb.database.Database` и
`com.arcadedb.graph.MutableVertex` — оба из `arcadedb-engine`, которого больше нет.

---

## Step 4 — `ArcadeDBSemanticWriter.java`

**Файл:** `libraries/hound/src/main/java/com/hound/storage/ArcadeDBSemanticWriter.java`

Изменения:
1. Удалить imports `com.arcadedb.database.Database` и `com.arcadedb.database.DatabaseFactory`
2. Убрать enum value `EMBEDDED` из `Mode { EMBEDDED, REMOTE, REMOTE_BATCH }`
3. Удалить поля `embeddedDb` и `embedded` (строки 30-31)
4. Удалить embedded-constructor (строки 42-51)
5. В `saveResult` / `ensureCanonicalPool` / `writePerfStats` / `cleanAll` / `close`
   убрать все ветки `mode == Mode.EMBEDDED`
6. Добавить в начало класса комментарий:
   ```java
   // TODO(arcadedb-embed): EMBEDDED mode removed during 26.x upgrade.
   // Restore EmbeddedWriter + Mode.EMBEDDED after engine API stabilises.
   ```

Конструкторы после изменения:
- `(host, port, dbName, user, password)` → REMOTE (без изменений)
- `(host, port, dbName, user, password, useBatch)` → REMOTE / REMOTE_BATCH (без изменений)

---

## Step 5 — `SchemaInitializer.java`

**Файл:** `libraries/hound/src/main/java/com/hound/storage/SchemaInitializer.java`

1. Удалить imports: `com.arcadedb.database.Database`, `com.arcadedb.query.sql.executor.ResultSet`,
   `com.arcadedb.schema.Schema`
2. Удалить метод `ensureSchema(Database db)` и все private-helpers, которые принимают
   `Schema` / `Database` (vtx, vtxExtends, edge, doc, prop, idx — если они используются
   только в `ensureSchema`).
3. Оставить: `remoteSchemaCommands()` (делегирует в `RemoteSchemaCommands.all()`) —
   он нужен для remote schema init в `ArcadeDBSemanticWriter`.

---

## Step 6 — `HoundApplication.java`

**Файл:** `libraries/hound/src/main/java/com/hound/HoundApplication.java`

1. В `createWriter()` (строки ~148-169): удалить embedded-ветку:
   ```java
   // Удалить:
   if (config.arcadeDbPath != null) {
       logger.info("ArcadeDB : EMBEDDED {}", config.arcadeDbPath);
       return new ArcadeDBSemanticWriter(config.arcadeDbPath);
   }
   ```
2. В `RunConfig` удалить поле `String arcadeDbPath = null;`
3. В CLI options (строка ~519): удалить строку:
   ```java
   options.addOption(null, "arcade-db", true, "ArcadeDB embedded path");
   ```
4. В разборе CLI: удалить обработку `arcade-db` опции (поиск по `config.arcadeDbPath`).

---

## Step 7 — Тесты: пометить embedded-тесты как @Disabled

Файлы:

**`SchemaInitializerTest.java`** и **`SchemaDbIsolationTest.java`**
(`libraries/hound/src/test/java/com/hound/storage/`)

В каждый класс добавить на уровне класса:
```java
@Disabled("Embedded mode removed during ArcadeDB 26.x upgrade — TODO: restore after engine API stabilisation")
```

**`EmbeddedVsBatchIT.java`**
(`libraries/hound/src/test/java/com/hound/storage/`)

Убрать тестовый кейс/блок, специфичный для EMBEDDED mode.
Оставить тесты для REMOTE и REMOTE_BATCH без изменений.

---

## Step 8 — Verify build

```bash
# Компиляция должна пройти без ошибок
./gradlew :libraries:hound:compileJava

# Тесты: embedded-тесты пропущены, остальные проходят
./gradlew :libraries:hound:test

# Финальная проверка: нет старой версии
grep -r "25\.12\.1" .          # → no output
grep -r "arcadedb-engine" .    # → no output
grep -r "arcadedb-network" .   # → только build.gradle со значением 26.3.1
```

Для smoke-test remote mode (требует запущенного ArcadeDB 26.3.1 контейнера):
```bash
./gradlew :libraries:hound:test --tests "*EmbeddedVsBatchIT*" -Dintegration=true
```

---

## Critical files

| Файл | Операция |
|------|---------|
| `libraries/hound/build.gradle` | bump network + remove engine dep |
| `libraries/hound/src/main/java/com/hound/storage/EmbeddedWriter.java` | **DELETE** |
| `libraries/hound/src/main/java/com/hound/storage/ArcadeDBSemanticWriter.java` | remove EMBEDDED mode |
| `libraries/hound/src/main/java/com/hound/storage/SchemaInitializer.java` | remove ensureSchema(Database) |
| `libraries/hound/src/main/java/com/hound/HoundApplication.java` | remove --arcade-db option |
| `libraries/hound/src/test/java/com/hound/storage/SchemaInitializerTest.java` | @Disabled |
| `libraries/hound/src/test/java/com/hound/storage/SchemaDbIsolationTest.java` | @Disabled |
| `libraries/hound/src/test/java/com/hound/storage/EmbeddedVsBatchIT.java` | remove EMBEDDED test case |
