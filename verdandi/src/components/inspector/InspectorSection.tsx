import { memo, useState } from 'react';
import { ChevronRight } from 'lucide-react';

interface Props {
  title: string;
  children: React.ReactNode;
  defaultOpen?: boolean;
}

export const InspectorSection = memo(({ title, children, defaultOpen = true }: Props) => {
  const [open, setOpen] = useState(defaultOpen);

  return (
    <div style={{ borderBottom: '1px solid var(--bd)' }}>
      <button
        onClick={() => setOpen((o) => !o)}
        style={{
          display: 'flex', alignItems: 'center', gap: 4,
          width: '100%', padding: '6px 10px',
          background: 'transparent', border: 'none',
          color: 'var(--t2)', fontSize: '10px', fontWeight: 600,
          letterSpacing: '0.08em', textTransform: 'uppercase',
          cursor: 'pointer', textAlign: 'left',
          transition: 'background 0.08s',
        }}
        onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.background = 'var(--bg1)'; }}
        onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.background = 'transparent'; }}
      >
        <ChevronRight
          size={11}
          style={{
            transform: open ? 'rotate(90deg)' : 'rotate(0deg)',
            transition: 'transform 0.12s',
            color: 'var(--t3)',
            flexShrink: 0,
          }}
        />
        {title}
      </button>
      {open && <div style={{ padding: '2px 0 6px' }}>{children}</div>}
    </div>
  );
});

InspectorSection.displayName = 'InspectorSection';

// ── Shared row component ─────────────────────────────────────────────────────
export const InspectorRow = memo(({ label, value }: { label: string; value: React.ReactNode }) => (
  <div style={{
    display: 'flex', alignItems: 'flex-start', gap: 6,
    padding: '3px 10px',
    fontSize: '11px',
  }}>
    <span style={{ color: 'var(--t3)', minWidth: 72, flexShrink: 0 }}>{label}</span>
    <span style={{
      color: 'var(--t1)', wordBreak: 'break-word',
      fontFamily: 'var(--seer-font-mono, monospace)',
    }}>{value}</span>
  </div>
));

InspectorRow.displayName = 'InspectorRow';
