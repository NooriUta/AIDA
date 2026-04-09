import { useEffect, useState, useCallback, useSyncExternalStore } from 'react';

// ── Types ────────────────────────────────────────────────────────────────────

type ToastLevel = 'error' | 'success' | 'info';

interface ToastEntry {
  id: number;
  message: string;
  level: ToastLevel;
}

// ── Tiny external store (framework-agnostic, no context needed) ──────────────

let nextId = 0;
let toasts: ToastEntry[] = [];
const listeners = new Set<() => void>();

function emit() { listeners.forEach((fn) => fn()); }

export function showToast(message: string, level: ToastLevel = 'error') {
  toasts = [...toasts, { id: ++nextId, message, level }];
  emit();
}

function dismiss(id: number) {
  toasts = toasts.filter((t) => t.id !== id);
  emit();
}

function subscribe(cb: () => void) {
  listeners.add(cb);
  return () => { listeners.delete(cb); };
}

function getSnapshot() { return toasts; }

// ── Auto-dismiss duration per level ──────────────────────────────────────────

const DURATION: Record<ToastLevel, number> = { error: 5000, success: 3000, info: 4000 };

// ── <ToastContainer /> — mount once in App or Shell ──────────────────────────

export function ToastContainer() {
  const items = useSyncExternalStore(subscribe, getSnapshot);

  if (items.length === 0) return null;

  return (
    <div style={{
      position: 'fixed', bottom: 20, right: 20, zIndex: 9999,
      display: 'flex', flexDirection: 'column-reverse', gap: 8,
      pointerEvents: 'none',
    }}>
      {items.map((t) => (
        <ToastItem key={t.id} entry={t} />
      ))}
    </div>
  );
}

// ── Single toast item ────────────────────────────────────────────────────────

const LEVEL_STYLES: Record<ToastLevel, { bg: string; border: string; icon: string }> = {
  error:   { bg: 'color-mix(in srgb, #D44 18%, var(--bg2, #2a2a2a))', border: '#D44', icon: '\u2716' },
  success: { bg: 'color-mix(in srgb, #6A4 18%, var(--bg2, #2a2a2a))', border: '#6A4', icon: '\u2714' },
  info:    { bg: 'color-mix(in srgb, #48C 18%, var(--bg2, #2a2a2a))', border: '#48C', icon: '\u2139' },
};

function ToastItem({ entry }: { entry: ToastEntry }) {
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    // Trigger enter animation on next frame
    requestAnimationFrame(() => setVisible(true));
    const timer = setTimeout(() => dismiss(entry.id), DURATION[entry.level]);
    return () => clearTimeout(timer);
  }, [entry.id, entry.level]);

  const handleDismiss = useCallback(() => dismiss(entry.id), [entry.id]);
  const s = LEVEL_STYLES[entry.level];

  return (
    <div
      role="alert"
      onClick={handleDismiss}
      style={{
        pointerEvents: 'auto', cursor: 'pointer',
        display: 'flex', alignItems: 'center', gap: 8,
        padding: '10px 16px',
        background: s.bg,
        border: `1px solid ${s.border}`,
        borderRadius: 'var(--seer-radius-md, 6px)',
        boxShadow: '0 4px 16px rgba(0,0,0,0.4)',
        color: 'var(--t1, #ddd)',
        fontSize: 12, letterSpacing: '0.03em',
        maxWidth: 360,
        opacity: visible ? 1 : 0,
        transform: visible ? 'translateY(0)' : 'translateY(12px)',
        transition: 'opacity 0.2s, transform 0.2s',
      }}
    >
      <span style={{ fontSize: 14, flexShrink: 0 }}>{s.icon}</span>
      <span style={{ flex: 1 }}>{entry.message}</span>
    </div>
  );
}
