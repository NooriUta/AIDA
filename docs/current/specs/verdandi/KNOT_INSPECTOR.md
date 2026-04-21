# KNOT Inspector — InspectorTable

**Версия:** 1.0  
**Дата:** 2026-04-20  
**Задача:** EK-02

---

## Структура InspectorTable

```
InspectorTable (Overview tab)
├── TableHeaderCard        — имя, схема, dataSource
├── TabBar                 — General | DDL
├── Properties section     — @rid
├── Columns section        — список ColumnRow
├── Routines section       — TableRoutinesSection
└── Statements section     — TableStatementsSection (EK-02)
```

---

## Statements секция (EK-02)

### Источник данных

`TableStatementsSection` использует тот же хук `useKnotTableRoutines`, что и Routines секция. Каждый `KnotTableUsage` содержит:

```typescript
interface KnotTableUsage {
  routineGeoid: string;
  routineName:  string;
  edgeType:     string;
  stmtGeoid:    string | null;
  stmtType:     string | null;
}
```

Уникальные `DaliStatement` — это дедупликация по `stmtGeoid`:

```typescript
const stmts = useMemo(() => {
  const seen = new Map<string, KnotTableUsage>();
  for (const u of data) {
    if (u.stmtGeoid && !seen.has(u.stmtGeoid)) seen.set(u.stmtGeoid, u);
  }
  return [...seen.values()];
}, [data]);
```

Новый backend-запрос не требуется — только дедупликация существующих данных.

### Навигация

Клик на строку statements → `jumpTo('L3', stmtGeoid, ...)` + `navigate('/')`:

```typescript
jumpTo('L3', usage.stmtGeoid, usage.stmtType || usage.stmtGeoid, 'DaliStatement', {
  focusNodeId: usage.stmtGeoid,
});
navigate('/');
```

### Поведение

- Секция по умолчанию **закрыта** (`defaultOpen={false}`) — данные загружаются по клику
- Счётчик показывается после загрузки данных: `Statements (N)`
- Пустое состояние: `inspector.noStatements`
- Загрузка: `status.loading`
