import { memo, useState, useRef, useEffect, useCallback, type ReactNode } from 'react';
import { CornerDownLeft, ArrowUp, ArrowDown } from 'lucide-react';
import { useTranslation } from 'react-i18next';

export interface PaletteSection {
  id: string;
  subTabs: Array<{ id: string; labelKey: string; route: string }>;
  horizon?: string;
}

interface PaletteItem {
  id: string;
  label: string;
  group: string;
  action: () => void;
}

interface Props {
  open: boolean;
  onClose: () => void;
  sections: PaletteSection[];
  onNavigate: (route: string) => void;
}

export const HeimdallCommandPalette = memo(({ open, onClose, sections, onNavigate }: Props) => {
  const { t } = useTranslation();
  const [activeIndex, setActiveIndex] = useState(0);
  const inputRef = useRef<HTMLInputElement>(null);
  const listRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (open) {
      setActiveIndex(0);
      requestAnimationFrame(() => inputRef.current?.focus());
    }
  }, [open]);

  const items: PaletteItem[] = sections.flatMap(sec =>
    sec.subTabs.map(sub => ({
      id: `${sec.id}-${sub.id}`,
      label: t(sub.labelKey).toUpperCase(),
      group: sec.id,
      action: () => { onNavigate(sub.route); onClose(); },
    }))
  );

  useEffect(() => { setActiveIndex(0); }, [items.length]);

  const onKeyDown = useCallback((e: React.KeyboardEvent) => {
    if (e.key === 'ArrowDown') {
      e.preventDefault();
      setActiveIndex(prev => (prev + 1) % Math.max(items.length, 1));
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      setActiveIndex(prev => (prev - 1 + items.length) % Math.max(items.length, 1));
    } else if (e.key === 'Enter') {
      e.preventDefault();
      items[activeIndex]?.action();
    } else if (e.key === 'Escape') {
      e.preventDefault();
      onClose();
    }
  }, [items, activeIndex, onClose]);

  useEffect(() => {
    const el = listRef.current?.children[activeIndex] as HTMLElement | undefined;
    el?.scrollIntoView({ block: 'nearest' });
  }, [activeIndex]);

  if (!open) return null;

  let lastGroup: string | null = null;
  // eslint-disable-next-line react/jsx-no-useless-fragment

  return (
    <>
      <div
        onClick={onClose}
        style={{
          position: 'fixed', inset: 0, zIndex: 9998,
          background: 'rgba(0,0,0,0.45)',
          backdropFilter: 'blur(2px)',
        }}
      />
      <div
        role="dialog"
        aria-label="Command Palette"
        onKeyDown={onKeyDown}
        style={{
          position: 'fixed',
          top: '18%', left: '50%',
          transform: 'translateX(-50%)',
          zIndex: 9999,
          width: '100%', maxWidth: 440,
          background: 'var(--bg1)',
          border: '1px solid var(--bd)',
          borderRadius: 'var(--seer-radius-xl)',
          boxShadow: '0 16px 48px rgba(0,0,0,0.5)',
          overflow: 'hidden',
          display: 'flex', flexDirection: 'column',
          animation: 'hhCmdIn 0.15s ease-out',
        }}
      >
        <style>{`
          @keyframes hhCmdIn {
            from { opacity: 0; transform: translateX(-50%) translateY(-6px) scale(0.98); }
            to   { opacity: 1; transform: translateX(-50%) translateY(0) scale(1); }
          }
        `}</style>

        <div style={{
          display: 'flex', alignItems: 'center', justifyContent: 'space-between',
          padding: '10px 14px',
          borderBottom: '1px solid var(--bd)',
        }}>
          <span style={{ fontSize: '11px', fontWeight: 600, color: 'var(--t2)', letterSpacing: '0.08em' }}>
            NAVIGATE
          </span>
          <div style={{
            display: 'flex', alignItems: 'center', gap: 4,
            fontSize: '9px', color: 'var(--t3)', letterSpacing: '0.04em',
          }}>
            <ArrowUp size={9} /> <ArrowDown size={9} />
            <span style={{ margin: '0 2px' }}>navigate</span>
            <CornerDownLeft size={9} />
            <span>select</span>
          </div>
        </div>

        <div
          ref={listRef}
          role="listbox"
          style={{ maxHeight: 320, overflowY: 'auto', padding: '4px 0' }}
        >
          {items.map((item, i) => {
            let header: ReactNode = null;
            if (item.group !== lastGroup) {
              lastGroup = item.group;
              header = (
                <div key={`g-${item.group}`} style={{
                  padding: '6px 14px 3px',
                  fontSize: '9px', fontWeight: 600,
                  color: 'var(--t3)', letterSpacing: '0.08em',
                  textTransform: 'uppercase',
                }}>
                  {item.group}
                </div>
              );
            }
            const isActive = i === activeIndex;
            return (
              <div key={item.id}>
                {header}
                <div
                  role="option"
                  aria-selected={isActive}
                  onClick={() => item.action()}
                  onMouseEnter={() => setActiveIndex(i)}
                  style={{
                    display: 'flex', alignItems: 'center',
                    padding: '8px 14px',
                    cursor: 'pointer',
                    background: isActive
                      ? 'color-mix(in srgb, var(--acc) 10%, transparent)'
                      : 'transparent',
                    color: isActive ? 'var(--acc)' : 'var(--t1)',
                    fontSize: '12px',
                    transition: 'background 0.06s',
                  }}
                >
                  <div style={{
                    width: 5, height: 5, borderRadius: '50%', flexShrink: 0, marginRight: 10,
                    background: isActive ? 'var(--acc)' : 'transparent',
                  }} />
                  {item.label}
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </>
  );
});

HeimdallCommandPalette.displayName = 'HeimdallCommandPalette';
