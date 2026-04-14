# Code Review Fixes — Stabilizing Sprint 2026-04-13

## Context
Automated code review of 23 modified files on `fix/stabilizing-sprint-apr13-2026` identified 8 actionable issues across 5 files (TypeScript/React + Java). The fixes address O(n²) performance bugs, a Java geoid double-schema bug, a React useEffect race condition, and several null-safety/defensive coding gaps. No architectural changes — all fixes are surgical and localized.

## Перед началом

1. **Ветка уже создана:** `fix/stabilizing-sprint-apr13-2026` — работаем на ней.
2. **Сохранить план:** `docs/sprints/code-review-fixes-2026-04-13.md` (скопировать этот файл).

---

## Fixes (по приоритету)

### MEDIUM-1 — transformExplore.ts:112 — использование find() вместо уже построенного nodeById

**Файл:** `frontends/verdandi/src/utils/transformExplore.ts`

Функция `transformSchemaExplore` строит `nodeById` на строке 99, но на строке 112 в цикле по рёбрам всё равно вызывает `result.nodes.find()` — O(n²) вместо O(1).

**Правка:**
```typescript
// Строка 112: заменить
const colNode = result.nodes.find(nd => nd.id === e.target && nd.type === 'DaliColumn');
// На:
const colNode = nodeById.get(e.target);
if (!colNode || colNode.type !== 'DaliColumn') continue;
```
Убрать `if (!colNode) continue;` которое шло после старой строки (теперь guard встроен).

---

### MEDIUM-2 — transformExplore.ts:369–390 — O(n²) в transformGqlExplore

**Файл:** `frontends/verdandi/src/utils/transformExplore.ts`

Функция `transformGqlExplore` выполняет `result.nodes.find()` в двух циклах по рёбрам (HAS_COLUMN и HAS_OUTPUT_COL/HAS_AFFECTED_COL) без предварительно построенного Map.

**Правка:** Добавить перед первым `for`-циклом (~строка 368):
```typescript
const nodeById = new Map(result.nodes.map(n => [n.id, n]));
```
Затем заменить оба вызова `result.nodes.find(...)`:
- Строка 371: `const colNode = nodeById.get(e.target); if (!colNode || colNode.type !== 'DaliColumn') continue;`
- Строка 383: `const colNode = nodeById.get(e.target); if (!colNode || !['DaliOutputColumn','DaliColumn','DaliAffectedColumn'].includes(colNode.type)) continue;`

---

### MEDIUM-3 — StructureAndLineageBuilder.java:126–144 — ensureTableWithType: асимметрия strip-схемы

**Файл:** `libraries/hound/src/main/java/com/hound/semantic/engine/StructureAndLineageBuilder.java`

`ensureTable()` стрипает schema-prefix **безусловно** (даже если `resolvedSchema` задан), чтобы не образовывался geoid `DWH.DWH.MY_VIEW`. `ensureTableWithType()` стрипает только если `resolvedSchema == null || isBlank()` — баг.

**Правка:** строки 128–133 в `ensureTableWithType`:
```java
// Заменить:
if (upperName.contains(".") && (resolvedSchema == null || resolvedSchema.isBlank())) {
    String[] parts = upperName.split("\\.");
    upperName = parts[parts.length - 1];
    resolvedSchema = String.join(".", Arrays.copyOf(parts, parts.length - 1));
    ensureSchema(resolvedSchema, null);
}
// На:
if (upperName.contains(".")) {
    String[] parts = upperName.split("\\.");
    upperName = parts[parts.length - 1];
    String embeddedSchema = String.join(".", Arrays.copyOf(parts, parts.length - 1));
    if (resolvedSchema == null || resolvedSchema.isBlank()) {
        resolvedSchema = embeddedSchema;
        ensureSchema(resolvedSchema, null);
    }
    // else: resolvedSchema задан явно — сохраняем его, просто используем чистое имя
}
```

---

### MEDIUM-4 — SessionList.tsx:422–427 — race condition с onUpdate

**Файл:** `frontends/heimdall-frontend/src/components/dali/SessionList.tsx`

`onUpdate` в deps useEffect провоцирует повторный запуск эффекта при каждом ре-рендере родителя (если родитель не оборачивает коллбэк в useCallback). Паттерн: переместить `onUpdate` в ref.

**Правка:** внутри компонента `SessionRow` (перед проблемным useEffect):
```typescript
const onUpdateRef = useRef(onUpdate);
useEffect(() => { onUpdateRef.current = onUpdate; });

useEffect(() => {
  if (!live) return;
  if (live.updatedAt === reportedAtRef.current) return;
  reportedAtRef.current = live.updatedAt;
  onUpdateRef.current(live);
}, [live]); // onUpdate убран из deps
```

---

### LOW-1 — authStore.ts:111–117 — checkSession не восстанавливает user

**Файл:** `frontends/verdandi/src/stores/authStore.ts`

При `isAuthenticated=true, user=null` (edge case после ручной манипуляции storage) `checkSession` получает ответ `/me` успешно, но не восстанавливает `user`. Также: любой статус кроме 401 (включая 500) воспринимается как валидная сессия.

**Правка:** строки 111–118:
```typescript
// Заменить блок else:
} else if (res.ok) {
  try {
    const data = await res.json();
    set(s => ({ user: data ?? s.user }));
  } catch { /* keep existing user */ }
  startRefreshTimer(() => get().refreshToken());
  if (!usePrefsStore.getState().synced) {
    usePrefsStore.getState().fetchPrefs().catch(() => {});
  }
}
// Если res не ok и не 401 — catch сверху сохранит состояние (network-down path)
```

---

### LOW-2 — SessionList.tsx:325 — арифметика без null-guard

**Файл:** `frontends/heimdall-frontend/src/components/dali/SessionList.tsx`

`s.inserted + s.duplicate` → `NaN` если бэкенд вернёт `null`.

**Правка:** строка 325:
```typescript
{((s.inserted ?? 0) + (s.duplicate ?? 0)).toLocaleString()}
```

---

### LOW-3 — ServicesPage.tsx:54–70 — switch без default

**Файл:** `frontends/heimdall-frontend/src/pages/ServicesPage.tsx`

`dotColor()` и `statusBadgeClass()` вернут `undefined` при неизвестном статусе.

**Правка:** добавить `default` в оба switch:
```typescript
// dotColor — после case 'down':
default: return 'var(--t3)';

// statusBadgeClass — после case 'down':
default: return 'badge';
```

---

### LOW-4 — StructureAndLineageBuilder.java:614 — hardcoded depth limit без warning

**Файл:** `libraries/hound/src/main/java/com/hound/semantic/engine/StructureAndLineageBuilder.java`

`while (current != null && depth < 50)` тихо обрезает при глубине > 50.

**Правка:** после цикла добавить:
```java
if (depth >= 50 && current != null) {
    logger.warn("STAB: computeDepth hit limit of 50 starting from '{}' — possible cycle or deep nesting", parentGeoid);
}
```

---

### PERF — StructureAndLineageBuilder.java:697 — deep copy с неоптимальной ёмкостью

**Файл:** `libraries/hound/src/main/java/com/hound/semantic/engine/StructureAndLineageBuilder.java`

`new LinkedHashMap<>(atomEntry.getValue())` создаёт копию с дефолтной load factor, не учитывая что мы добавим ещё 2 поля.

**Правка:** строка 697:
```java
// Заменить:
Map<String, Object> a = new LinkedHashMap<>(atomEntry.getValue());
// На (preallocate для исходных + 2 новых поля):
Map<String, Object> source = atomEntry.getValue();
Map<String, Object> a = new LinkedHashMap<>(source.size() + 2);
a.putAll(source);
```

---

## Файлы к изменению

| Файл | Изменений |
|------|-----------|
| `frontends/verdandi/src/utils/transformExplore.ts` | 2 (MEDIUM-1, MEDIUM-2) |
| `frontends/heimdall-frontend/src/components/dali/SessionList.tsx` | 2 (MEDIUM-4, LOW-2) |
| `frontends/heimdall-frontend/src/pages/ServicesPage.tsx` | 1 (LOW-3) |
| `frontends/verdandi/src/stores/authStore.ts` | 1 (LOW-1) |
| `libraries/hound/src/main/java/com/hound/semantic/engine/StructureAndLineageBuilder.java` | 3 (MEDIUM-3, LOW-4, PERF) |

**Не трогать:** dead code в SessionList.tsx:65 (`total > 0` внутри `total > 1`) — не баг, только стиль; оставить без изменений.

---

## Верификация

1. **TS компиляция:** `cd frontends/verdandi && npx tsc --noEmit` и `cd frontends/heimdall-frontend && npx tsc --noEmit` — ноль ошибок.
2. **Java сборка:** `cd libraries/hound && mvn compile -q` — BUILD SUCCESS.
3. **Тест MEDIUM-3:** вызвать `ensureTableWithType("DWH.MY_VIEW", "DWH", "VIEW")` → geoid должен быть `DWH.MY_VIEW`, не `DWH.DWH.MY_VIEW`.
4. **Тест MEDIUM-2:** открыть Verdandi LOOM с большим графом (500+ нод), убедиться что граф отрисовывается без заметного фриза.
5. **Тест LOW-1:** в DevTools очистить `user` из sessionStorage при `isAuthenticated=true`, перезагрузить → `user` должен восстановиться из `/me`.
