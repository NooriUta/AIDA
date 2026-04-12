import { memo, useState, useRef, useEffect, useCallback, useMemo } from 'react';
import { Search, CornerDownLeft, ArrowUp, ArrowDown } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { useLoomStore } from '../stores/loomStore';
import { useSearch } from '../services/hooks';
import type { SearchResult } from '../services/lineage';

// ─── Command item types ──────────────────────────────────────────────────────

interface CommandItem {
  id: string;
  label: string;
  hint?: string;
  group: 'search' | 'command' | 'navigation';
  action: () => void;
}

// ─── Props ───────────────────────────────────────────────────────────────────

interface CommandPaletteProps {
  open: boolean;
  onClose: () => void;
}

// ─── Component ───────────────────────────────────────────────────────────────

export const CommandPalette = memo(({ open, onClose }: CommandPaletteProps) => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { toggleTheme, requestFitView, selectNode, navigateToLevel } = useLoomStore();

  const [query, setQuery] = useState('');
  const [activeIndex, setActiveIndex] = useState(0);
  const inputRef = useRef<HTMLInputElement>(null);
  const listRef = useRef<HTMLDivElement>(null);

  // Focus input when opened
  useEffect(() => {
    if (open) {
      setQuery('');
      setActiveIndex(0);
      // Small delay to let the modal render
      requestAnimationFrame(() => inputRef.current?.focus());
    }
  }, [open]);

  // ── Search results from backend ────────────────────────────────────────────
  const searchQuery = useSearch(query, 8);
  const searchResults: SearchResult[] = searchQuery.data ?? [];

  // ── Static commands ────────────────────────────────────────────────────────
  const staticCommands: CommandItem[] = useMemo(() => [
    {
      id: 'cmd-fit-view',
      label: t('commandPalette.fitView'),
      hint: 'F',
      group: 'command',
      action: () => { requestFitView(); onClose(); },
    },
    {
      id: 'cmd-toggle-theme',
      label: t('commandPalette.toggleTheme'),
      hint: 'T',
      group: 'command',
      action: () => { toggleTheme(); onClose(); },
    },
    {
      id: 'cmd-deselect',
      label: t('commandPalette.deselectNode'),
      hint: 'Esc',
      group: 'command',
      action: () => { selectNode(null); onClose(); },
    },
    {
      id: 'nav-l1',
      label: t('commandPalette.goToL1'),
      group: 'navigation',
      action: () => { navigateToLevel('L1'); onClose(); },
    },
    {
      id: 'nav-knot',
      label: t('commandPalette.goToKnot'),
      group: 'navigation',
      action: () => { navigate('/knot'); onClose(); },
    },
  // eslint-disable-next-line react-hooks/exhaustive-deps
  ], [t, onClose]);

  // ── Merged item list ───────────────────────────────────────────────────────
  const items: CommandItem[] = useMemo(() => {
    const result: CommandItem[] = [];

    // Search results first
    for (const sr of searchResults) {
      result.push({
        id: `search-${sr.id}`,
        label: sr.label,
        hint: sr.type.replace('Dali', ''),
        group: 'search',
        action: () => {
          selectNode(sr.id);
          onClose();
        },
      });
    }

    // Static commands — filter by query
    const q = query.toLowerCase().trim();
    for (const cmd of staticCommands) {
      if (!q || cmd.label.toLowerCase().includes(q)) {
        result.push(cmd);
      }
    }

    return result;
  }, [searchResults, staticCommands, query, selectNode, onClose]);

  // Reset active index when items change
  useEffect(() => { setActiveIndex(0); }, [items.length]);

  // ── Keyboard navigation ────────────────────────────────────────────────────
  const onKeyDown = useCallback((e: React.KeyboardEvent) => {
    if (e.key === 'ArrowDown') {
      e.preventDefault();
      setActiveIndex((prev) => (prev + 1) % Math.max(items.length, 1));
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      setActiveIndex((prev) => (prev - 1 + items.length) % Math.max(items.length, 1));
    } else if (e.key === 'Enter') {
      e.preventDefault();
      items[activeIndex]?.action();
    } else if (e.key === 'Escape') {
      e.preventDefault();
      onClose();
    }
  }, [items, activeIndex, onClose]);

  // Scroll active item into view
  useEffect(() => {
    const el = listRef.current?.children[activeIndex] as HTMLElement | undefined;
    el?.scrollIntoView({ block: 'nearest' });
  }, [activeIndex]);

  if (!open) return null;

  // Group labels
  const groupLabel = (g: CommandItem['group']) => {
    switch (g) {
      case 'search':     return t('commandPalette.results');
      case 'command':    return t('commandPalette.commands');
      case 'navigation': return t('commandPalette.navigation');
    }
  };

  // Build grouped rendering
  let lastGroup: string | null = null;

  return (
    <>
      {/* Backdrop */}
      <div
        onClick={onClose}
        style={{
          position: 'fixed', inset: 0, zIndex: 9998,
          background: 'rgba(0,0,0,0.45)',
          backdropFilter: 'blur(2px)',
        }}
      />

      {/* Palette container */}
      <div
        role="dialog"
        aria-label={t('commandPalette.title')}
        onKeyDown={onKeyDown}
        style={{
          position: 'fixed',
          top: '18%', left: '50%',
          transform: 'translateX(-50%)',
          zIndex: 9999,
          width: '100%', maxWidth: 480,
          background: 'var(--bg1)',
          border: '1px solid var(--bd)',
          borderRadius: 'var(--seer-radius-xl)',
          boxShadow: '0 16px 48px rgba(0,0,0,0.5)',
          overflow: 'hidden',
          display: 'flex', flexDirection: 'column',
          animation: 'cmdPaletteIn 0.15s ease-out',
        }}
      >
        <style>{`
          @keyframes cmdPaletteIn {
            from { opacity: 0; transform: translateX(-50%) translateY(-6px) scale(0.98); }
            to   { opacity: 1; transform: translateX(-50%) translateY(0) scale(1); }
          }
        `}</style>

        {/* Search input */}
        <div style={{
          display: 'flex', alignItems: 'center', gap: 8,
          padding: '10px 14px',
          borderBottom: '1px solid var(--bd)',
        }}>
          <Search size={14} style={{ color: 'var(--t3)', flexShrink: 0 }} />
          <input
            ref={inputRef}
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder={t('commandPalette.placeholder')}
            style={{
              flex: 1, border: 'none', outline: 'none',
              background: 'transparent',
              color: 'var(--t1)', fontSize: '13px',
              fontFamily: 'var(--font)',
            }}
          />
          <div style={{
            display: 'flex', alignItems: 'center', gap: 3,
            fontSize: '9px', color: 'var(--t3)', letterSpacing: '0.04em',
          }}>
            <ArrowUp size={9} /> <ArrowDown size={9} />
            <span style={{ margin: '0 2px' }}>navigate</span>
            <CornerDownLeft size={9} />
            <span>select</span>
          </div>
        </div>

        {/* Results list */}
        <div
          ref={listRef}
          role="listbox"
          style={{ maxHeight: 320, overflowY: 'auto', padding: '4px 0' }}
        >
          {items.length === 0 && (
            <div style={{
              padding: '20px 14px', textAlign: 'center',
              fontSize: '12px', color: 'var(--t3)',
            }}>
              {searchQuery.isLoading
                ? t('status.loading')
                : t('commandPalette.noResults')}
            </div>
          )}

          {items.map((item, i) => {
            // Group header
            let header: React.ReactNode = null;
            if (item.group !== lastGroup) {
              lastGroup = item.group;
              header = (
                <div key={`g-${item.group}`} style={{
                  padding: '6px 14px 3px',
                  fontSize: '9px', fontWeight: 600,
                  color: 'var(--t3)', letterSpacing: '0.08em',
                  textTransform: 'uppercase',
                }}>
                  {groupLabel(item.group)}
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
                    display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                    padding: '7px 14px',
                    cursor: 'pointer',
                    background: isActive
                      ? 'color-mix(in srgb, var(--acc) 10%, transparent)'
                      : 'transparent',
                    color: isActive ? 'var(--acc)' : 'var(--t1)',
                    fontSize: '12px',
                    transition: 'background 0.06s',
                  }}
                >
                  <span style={{
                    overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
                  }}>
                    {item.label}
                  </span>
                  {item.hint && (
                    <span style={{
                      fontSize: '10px', color: 'var(--t3)',
                      padding: '1px 6px',
                      background: 'var(--bg2)',
                      borderRadius: '3px',
                      flexShrink: 0,
                      marginLeft: 8,
                      letterSpacing: '0.03em',
                    }}>
                      {item.hint}
                    </span>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </>
  );
});

CommandPalette.displayName = 'CommandPalette';
