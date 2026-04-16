# Skills — Автоматизация AIDA

Папка содержит документацию по Claude-скиллам (scheduled tasks), используемым для автоматизации ревью и quality gates проекта AIDA.

Скиллы хранятся в `~/.claude/scheduled-tasks/` и запускаются по расписанию или вручную через Claude Code CLI.

## Доступные скиллы

| Скилл | Описание | Когда запускать |
|-------|----------|-----------------|
| `adr-review` | Ежедневный архитектурный обзор | Ежедневно |
| `code-review` | Недельный отчёт по коду и архитектуре | Еженедельно |
| `code_review_hound` | Анализ зависимостей модулей Hound | После изменений в `libraries/hound/` |
| `qg-chur-resilience` | QG: Chur BFF устойчив к отказам зависимостей | После изменений Chur или Keycloak |
| `qg-dali-persistence` | QG: сессии Dali переживают рестарт | После каждого merge в `services/dali/` |
| `performance-weekly` | SLO-прогон: P/A/R метрики, обновление PERFORMANCE_TARGETS.md | **Каждый понедельник**, перед demo |
| `qg-dali-ygg-write` | QG: запись в YGG видна в HEIMDALL | После каждого прогона парсинга |
| `qg-heimdall-backend-validation` | QG: HEIMDALL backend не падает на плохих данных | После изменений `services/heimdall-backend/` |
| `qg-heimdall-frontend-ws` | QG: WebSocket автоматически переподключается | Перед demo-rehearsal |
| `qg-hound-listener-chain` | QG: HoundHeimdallListener корректно передаёт события | После реализации H3.8 — P0 |
| `qg-security-demo` | QG: demo-деплой безопасен для публичной сети | Обязателен за 24ч до публичного показа |
| `qg-verdandi-prefs-sync` | QG: UI-настройки синхронизируются с FRIGG | После изменений в `stores/` |
| `schedule` | Расписание и текущий статус всех QG | Справочник |
| `verdandi-plan-track` | Отслеживание плана разработки Verdandi | Еженедельно |

## Как запустить скилл вручную

```bash
# Через Claude Code CLI
claude --skill qg-security-demo

# Через /skill команду в сессии
/qg-security-demo
```

## Расписание автозапуска

Смотри [`docs/quality-gates/SCHEDULE.md`](../quality-gates/SCHEDULE.md) для текущего расписания.

## Добавить новый скилл

1. Создай папку `~/.claude/scheduled-tasks/<skill-name>/`
2. Добавь `SKILL.md` с frontmatter:
   ```yaml
   ---
   name: my-skill
   description: Краткое описание когда и зачем запускать
   ---
   # Текст промта
   ```
3. Задокументируй скилл в этом файле

## Связь с Quality Gates

Quality Gate скиллы (`qg-*`) проверяют конкретные свойства системы и создают отчёты в `docs/quality-gates/`. Каждый QG имеет:
- **Триггер** — условие запуска
- **Проверки** — список конкретных тестов
- **Критерий успеха** — что значит «прошёл»
- **Отчёт** — результат в `docs/quality-gates/`
