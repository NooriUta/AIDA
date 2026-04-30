import { useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { ChevronDown } from 'lucide-react';
import { useIsMobile } from '../../hooks/useIsMobile';
import { ToolbarDivider } from '../ui/ToolbarPrimitives';
import { type NornId, type NornDef } from './headerNavData';

// ── Verdandi navigation section (mobile + desktop) ────────────────────────────

interface HeaderNavSectionProps {
  activeNorn:      NornId;
  activeSubModule: string;
  currentNorn:     NornDef;
  go:              (route: string) => void;
}

export function HeaderNavSection({ activeNorn, activeSubModule, currentNorn, go }: HeaderNavSectionProps) {
  const { t } = useTranslation();
  const isMobile = useIsMobile();
  const [mobileVernOpen, setMobileVernOpen] = useState(false);
  const mobileVernRef = useRef<HTMLDivElement>(null);

  if (isMobile) {
    return (
      <div style={{ display: 'flex', alignItems: 'center', gap: 6, flex: 1 }}>
        <div
          ref={mobileVernRef}
          style={{ position: 'relative', flexShrink: 0 }}
          onBlur={(e) => {
            if (!mobileVernRef.current?.contains(e.relatedTarget as Node))
              setMobileVernOpen(false);
          }}
        >
          <button
            onClick={() => setMobileVernOpen((v) => !v)}
            title={activeNorn}
            style={{
              display: 'flex', alignItems: 'center', gap: '4px',
              padding: '4px 7px',
              background: mobileVernOpen ? 'var(--bg2)' : 'transparent',
              border: '1px solid',
              borderColor: mobileVernOpen ? 'var(--bd)' : 'transparent',
              borderRadius: 'var(--seer-radius-md)',
              cursor: 'pointer',
              fontSize: '11px', fontWeight: 700, letterSpacing: '0.1em',
              color: 'var(--t1)',
              transition: 'background 0.12s, border-color 0.12s',
            }}
          >
            {activeNorn[0]}
            <ChevronDown size={10} style={{
              color: 'var(--t3)',
              transform: mobileVernOpen ? 'rotate(180deg)' : 'rotate(0)',
              transition: 'transform 0.15s',
            }} />
          </button>

          {mobileVernOpen && (
            <div style={{
              position: 'absolute', top: 'calc(100% + 4px)', left: 0,
              zIndex: 300, minWidth: '160px',
              background: 'var(--bg1)', border: '1px solid var(--bd)',
              borderRadius: 'var(--seer-radius-lg)',
              boxShadow: '0 8px 24px rgba(0,0,0,0.35)',
              overflow: 'hidden',
            }}>
              <div style={{
                padding: '7px 12px 6px', fontSize: '10px', fontWeight: 600,
                color: 'var(--t3)', letterSpacing: '0.08em',
                borderBottom: '1px solid var(--bd)',
              }}>
                {activeNorn}
              </div>
              {currentNorn.subModules.map((sub) => {
                const isSub     = sub.id === activeSubModule;
                const isEnabled = sub.route !== null;
                return (
                  <button
                    key={sub.id}
                    onClick={() => {
                      if (isEnabled && sub.route) { go(sub.route); setMobileVernOpen(false); }
                    }}
                    disabled={!isEnabled}
                    style={{
                      display: 'flex', alignItems: 'center', gap: '10px',
                      width: '100%', padding: '10px 12px',
                      background: isSub ? 'color-mix(in srgb, var(--acc) 8%, transparent)' : 'transparent',
                      border: 'none',
                      color: isSub ? 'var(--acc)' : isEnabled ? 'var(--t1)' : 'var(--t3)',
                      fontSize: '12px', fontWeight: 500,
                      cursor: isEnabled ? 'pointer' : 'not-allowed',
                      opacity: isEnabled ? 1 : 0.35,
                      textAlign: 'left',
                    }}
                  >
                    <div style={{
                      width: 5, height: 5, borderRadius: '50%', flexShrink: 0,
                      background: isSub ? 'var(--acc)' : 'transparent',
                    }} />
                    <span style={{ flex: 1 }}>{t(sub.key)}</span>
                    {!isEnabled && sub.horizon && (
                      <span style={{
                        fontSize: '9px', fontWeight: 600, padding: '1px 5px',
                        borderRadius: 3,
                        background: 'color-mix(in srgb, var(--t3) 15%, transparent)',
                        color: 'var(--t3)',
                      }}>{sub.horizon}</span>
                    )}
                  </button>
                );
              })}
            </div>
          )}
        </div>

        {/* Active sub-module badge */}
        <span style={{
          fontSize: '12px', fontWeight: 500, letterSpacing: '0.06em',
          color: 'var(--acc)',
          padding: '4px 8px',
          background: 'color-mix(in srgb, var(--acc) 12%, transparent)',
          borderRadius: 'var(--seer-radius-sm)',
        }}>
          {t(`nav.${activeSubModule.toLowerCase()}`)}
        </span>

        <div style={{ flex: 1 }} />
      </div>
    );
  }

  // ── Desktop ──────────────────────────────────────────────────────────────────
  return (
    <>
      <button
        onClick={() => go(currentNorn.route)}
        title={`${currentNorn.id} — ${t(currentNorn.descKey)}`}
        style={{
          padding: '5px 10px',
          fontSize: '11px', fontWeight: 700, letterSpacing: '0.1em',
          color: 'var(--t1)',
          background: 'transparent', border: 'none',
          cursor: 'pointer', borderRadius: 'var(--seer-radius-sm)',
          transition: 'color 0.12s', flexShrink: 0,
        }}
        onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.color = 'var(--acc)'; }}
        onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.color = 'var(--t1)'; }}
      >
        {activeNorn}
      </button>

      <ToolbarDivider />

      <nav style={{ display: 'flex', gap: '2px', flex: 1, alignItems: 'center' }}>
        {currentNorn.subModules.length > 0 ? (
          currentNorn.subModules.map((sub) => {
            const isSub     = sub.id === activeSubModule;
            const isEnabled = sub.route !== null;
            return (
              <button
                key={sub.id}
                disabled={!isEnabled}
                aria-disabled={!isEnabled}
                tabIndex={isEnabled ? 0 : -1}
                onClick={() => isEnabled && sub.route && go(sub.route)}
                title={!isEnabled && sub.horizon ? t('nav.comingSoon', { horizon: sub.horizon }) : undefined}
                style={{
                  padding: '6px 14px', fontSize: '12px',
                  fontWeight: isSub ? 500 : 400,
                  borderRadius: 'var(--seer-radius-sm)', border: 'none',
                  cursor: isEnabled ? 'pointer' : 'not-allowed',
                  background: isSub ? 'color-mix(in srgb, var(--acc) 12%, transparent)' : 'transparent',
                  color: isSub ? 'var(--acc)' : isEnabled ? 'var(--t2)' : 'var(--t3)',
                  opacity: isEnabled ? 1 : 0.35,
                  transition: 'background 0.12s, color 0.12s',
                  letterSpacing: '0.06em',
                }}
              >
                {t(sub.key)}
              </button>
            );
          })
        ) : (
          <span style={{
            padding: '4px 12px', fontSize: '11px', fontWeight: 500,
            letterSpacing: '0.04em', color: 'var(--t3)',
          }}>
            {t('stub.underConstruction')}
          </span>
        )}
      </nav>
    </>
  );
}
