// src/components/layout/proto/FilterToolbarProtoRu.tsx
// PROTO: статичный визуальный прототип Filter Toolbar — русскоязычная версия
// Все подписи взяты из словаря LOOM_I18N_DICTIONARY.md (секция RU)
// Три состояния рядом на одном экране:
//   1. Toolbar без фильтра
//   2. Toolbar с активным фильтром поля (сумма_заказа)
//   3. Toolbar в режиме «на уровне таблиц»
// Удалить или переработать после утверждения → LOOM-023b.

import type { CSSProperties, ReactNode } from 'react';

// ─── Design-system tokens (Amber Forest dark) ────────────────────────��────────
const S: Record<string, CSSProperties> = {
  page: {
    minHeight: '100vh',
    background: 'var(--bg1, #141108)',
    padding: '32px 24px',
    display: 'flex',
    flexDirection: 'column',
    gap: 32,
    fontFamily: 'var(--seer-font, system-ui, sans-serif)',
  },
  heading: {
    color: 'var(--t1, #e8dfc8)',
    fontSize: 14,
    margin: 0,
    fontWeight: 500,
  },
  stateLabel: {
    fontSize: 11,
    color: 'var(--t3, #665c48)',
    letterSpacing: '0.1em',
    textTransform: 'uppercase' as const,
    marginBottom: 8,
  },
  toolbar: {
    display: 'flex',
    alignItems: 'center',
    gap: 8,
    height: 40,
    background: 'var(--bg2, #1c1810)',
    border: '1px solid var(--bd, #2e2819)',
    borderRadius: 8,
    padding: '0 12px',
  },
  divider: {
    width: 1,
    height: 20,
    background: 'var(--bd, #2e2819)',
    flexShrink: 0,
    margin: '0 2px',
  },
  pill: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: 6,
    background: 'var(--bg3, #24201a)',
    border: '1px solid var(--bd, #2e2819)',
    borderRadius: 6,
    padding: '3px 8px',
    fontSize: 12,
    color: 'var(--t1, #e8dfc8)',
    cursor: 'default',
    flexShrink: 0,
  },
  pillActive: {
    borderColor: 'var(--acc, #A8B860)',
  },
  fieldLabel: {
    fontSize: 11,
    color: 'var(--t3, #665c48)',
    flexShrink: 0,
  },
  select: {
    background: 'var(--bg3, #24201a)',
    border: '1px solid var(--bd, #2e2819)',
    borderRadius: 6,
    color: 'var(--t1, #e8dfc8)',
    fontSize: 12,
    padding: '3px 8px',
    outline: 'none',
    cursor: 'pointer',
  },
  selectActive: {
    borderColor: 'var(--acc, #A8B860)',
    color: 'var(--acc, #A8B860)',
  },
  btn: {
    display: 'inline-flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 4,
    minWidth: 28,
    height: 26,
    padding: '0 8px',
    borderRadius: 5,
    border: '1px solid var(--bd, #2e2819)',
    background: 'transparent',
    color: 'var(--t2, #a89a7a)',
    fontSize: 11,
    cursor: 'pointer',
    flexShrink: 0,
  },
  btnActive: {
    background: 'var(--bg3, #24201a)',
    borderColor: 'var(--acc, #A8B860)',
    color: 'var(--acc, #A8B860)',
  },
  btnClear: {
    color: 'var(--wrn, #D4922A)',
    borderColor: 'transparent',
    background: 'transparent',
    minWidth: 20,
    height: 20,
    padding: '0 4px',
    fontSize: 13,
  },
  badge: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: 4,
    fontSize: 10,
    color: 'var(--t3, #665c48)',
    marginLeft: 'auto',
    flexShrink: 0,
  },
  spacer: {
    flex: '1 1 auto',
  },
};

// ─── Иконки ───────────────────────────���────────────────────────────���──────────
function IconRoutine() {
  return (
    <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
      <rect x="1" y="1" width="10" height="10" rx="2"
        stroke="var(--acc,#A8B860)" strokeWidth="1.2" fill="none" />
      <path d="M3 4h6M3 6h4M3 8h5" stroke="var(--acc,#A8B860)"
        strokeWidth="1" strokeLinecap="round" />
    </svg>
  );
}

function IconTable() {
  return (
    <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
      <rect x="1" y="1" width="10" height="10" rx="2"
        stroke="var(--inf,#88B8A8)" strokeWidth="1.2" fill="none" />
      <path d="M1 4.5h10M4 4.5v5.5" stroke="var(--inf,#88B8A8)"
        strokeWidth="1" />
    </svg>
  );
}

function IconSwap() {
  return (
    <svg width="11" height="11" viewBox="0 0 12 12" fill="none">
      <path d="M2 4h8M8 2l2 2-2 2M10 8H2M4 6l-2 2 2 2"
        stroke="currentColor" strokeWidth="1.2"
        strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

function IconLayers() {
  return (
    <svg width="11" height="11" viewBox="0 0 12 12" fill="none">
      <path d="M1 4l5-3 5 3-5 3-5-3z" stroke="currentColor"
        strokeWidth="1.2" strokeLinejoin="round" fill="none" />
      <path d="M1 7l5 3 5-3" stroke="currentColor"
        strokeWidth="1.2" strokeLinecap="round" />
    </svg>
  );
}

// ─── Вспомогательные ────────────────────────��─────────────────────────────────
function Divider() {
  return <div style={S.divider} />;
}

function ToolbarWrapper({ children }: { children: ReactNode }) {
  return <div style={S.toolbar}>{children}</div>;
}

const DEPTHS = [1, 2, 3, 5, Infinity] as const;

function DepthButtons({ active }: { active: number }) {
  return (
    <div style={{ display: 'flex', gap: 3 }}>
      {DEPTHS.map((d) => (
        <button
          key={String(d)}
          style={{ ...S.btn, ...(active === d ? S.btnActive : {}) }}
        >
          {d === Infinity ? '∞' : d}
        </button>
      ))}
    </div>
  );
}

function DirectionToggle({ up, down }: { up: boolean; down: boolean }) {
  return (
    <div style={{ display: 'flex', gap: 3 }}>
      <button style={{ ...S.btn, ...(up ? S.btnActive : {}) }}>↑ Источники</button>
      <button style={{ ...S.btn, ...(down ? S.btnActive : {}) }}>↓ Приёмники</button>
    </div>
  );
}

// ─── Состояние 1: без фильтра ─────────────────────────────────────────────────
function ToolbarПустой() {
  return (
    <ToolbarWrapper>
      {/* Пилюля начального объекта */}
      <div style={S.pill}>
        <IconRoutine />
        <span>расчёт_выручки_заказа()</span>
        <span style={{ color: 'var(--t3)', display: 'flex' }}>
          <IconSwap />
        </span>
      </div>

      <Divider />

      {/* Выбор поля */}
      <span style={S.fieldLabel}>Поле:</span>
      <select style={S.select} defaultValue="">
        <option value="">все колонки</option>
        <option>сумма_заказа</option>
        <option>id_заказа</option>
        <option>id_клиента</option>
      </select>

      <Divider />

      {/* Глубина */}
      <span style={S.fieldLabel}>Глубина:</span>
      <DepthButtons active={Infinity} />

      <Divider />

      {/* Направление */}
      <DirectionToggle up down />

      <div style={S.spacer} />

      {/* Уровень таблиц (выкл.) */}
      <button style={S.btn}>
        <IconLayers /> На уровне таблиц
      </button>

      {/* Бейдж */}
      <div style={S.badge}>L2 · ∞ шагов</div>
    </ToolbarWrapper>
  );
}

// ─── Состояние 2: активный фильтр поля (сумма_заказа) ────────────────────────
function ToolbarСПолем() {
  return (
    <ToolbarWrapper>
      {/* Пилюля — подсвечена т.к. есть фильтр */}
      <div style={{ ...S.pill, ...S.pillActive }}>
        <IconRoutine />
        <span>расчёт_выручки_заказа()</span>
        <span style={{ color: 'var(--t3)', display: 'flex' }}>
          <IconSwap />
        </span>
      </div>

      <Divider />

      {/* Выбор поля — активен */}
      <span style={S.fieldLabel}>Поле:</span>
      <select style={{ ...S.select, ...S.selectActive }} defaultValue="сумма_заказа">
        <option value="">все колонки</option>
        <option value="сумма_заказа">сумма_заказа</option>
        <option>id_заказа</option>
        <option>id_клиента</option>
      </select>
      {/* Кнопка сброса поля */}
      <button style={{ ...S.btn, ...S.btnClear }} title="Сбросить">✕</button>

      <Divider />

      {/* Глубина */}
      <span style={S.fieldLabel}>Глубина:</span>
      <DepthButtons active={2} />

      <Divider />

      {/* Направление */}
      <DirectionToggle up down />

      <div style={S.spacer} />

      {/* Уровень таблиц (выкл.) */}
      <button style={S.btn}>
        <IconLayers /> На уровне таблиц
      </button>

      {/* Бейдж */}
      <div style={S.badge}>L2 · сумма_заказа · 2 шага</div>
    </ToolbarWrapper>
  );
}

// ─── Состояние 3: режим «на уровне таблиц» ───────────────────────────────────
function ToolbarУровеньТаблиц() {
  return (
    <ToolbarWrapper>
      {/* Пилюля — таблица заказов */}
      <div style={S.pill}>
        <IconTable />
        <span>заказы</span>
        <span style={{ color: 'var(--t3)', display: 'flex' }}>
          <IconSwap />
        </span>
      </div>

      <Divider />

      {/* Выбор поля — заблокирован в режиме таблиц */}
      <span style={{ ...S.fieldLabel, opacity: 0.4 }}>Поле:</span>
      <select style={{ ...S.select, opacity: 0.4 }} disabled defaultValue="">
        <option value="">все колонки</option>
      </select>

      <Divider />

      {/* Глубина */}
      <span style={S.fieldLabel}>Глубина:</span>
      <DepthButtons active={3} />

      <Divider />

      {/* Направление ��� только Downstream */}
      <DirectionToggle up={false} down />

      <div style={S.spacer} />

      {/* Уровень таблиц — АКТИВЕН */}
      <button style={{ ...S.btn, ...S.btnActive }}>
        <IconLayers /> На уровне колонок
      </button>

      {/* Бейдж */}
      <div style={S.badge}>L2 · таблицы · ↓ · 3 шага</div>
    </ToolbarWrapper>
  );
}

// ─── Экспорт ────────────────────────────���─────────────────────────────────────
export function FilterToolbarProtoRu() {
  return (
    <div style={S.page}>
      <h2 style={S.heading}>
        PROTO-023b — Filter Toolbar · три состояния · RU
      </h2>

      {/* Состояние 1 */}
      <div>
        <div style={S.stateLabel}>Состояние 1 — фильтр не активен</div>
        <ToolbarПустой />
      </div>

      {/* Состояние 2 */}
      <div>
        <div style={S.stateLabel}>Состояние 2 — фильтр поля: сумма_заказа · глубина 2</div>
        <ToolbarСПолем />
      </div>

      {/* Состояние 3 */}
      <div>
        <div style={S.stateLabel}>
          Состояние 3 — режим таблиц · объект: заказы · только downstream · глубина 3
        </div>
        <ToolbarУровеньТаблиц />
      </div>
    </div>
  );
}
