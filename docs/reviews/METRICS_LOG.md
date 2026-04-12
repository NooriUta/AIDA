# AIDA — Review Metrics Log

Append-only лог метрик. Одна строка на модуль на review.
Формат: DATE | SPRINT | MODULE | TEST_FILES | PROD_FILES | RATIO | MAX_FILE_LOC | OPEN_BUGS | STATUS

| DATE | SPRINT | MODULE | TEST_FILES | PROD_FILES | RATIO | MAX_FILE_LOC | OPEN_BUGS | STATUS |
|------|--------|--------|------------|------------|-------|--------------|-----------|--------|
| 12.04.2026 | S7 | verdandi | 23 | ~80 | ~0.29 | SearchPanel:516 | WARN-01,05,06 | 🟡 |
| 12.04.2026 | S7 | shuttle  | 4  | ~30 | ~0.13 | —               | C.2.1 pending | 🔴 |
| 12.04.2026 | S7 | chur     | 2  | ~10 | ~0.20 | —               | C.3.1-C.3.4   | 🟡 |
| 12.04.2026 | S7 | hound    | 24 | ~50 | ~0.48 | —               | C.1.0 B1-B7   | 🔴 |
