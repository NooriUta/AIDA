import { useRef, useState, type Dispatch, type SetStateAction } from 'react';
import { ChevronDown } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useAuthStore } from '../../stores/authStore';
import { type Section, type SectionId } from './heimdallNavData';
import { HDivider, ThemeToggle, LanguageSwitcher, PaletteDropdown } from './HeimdallHeaderControls';
import { TenantPickerButton } from './HeimdallTenantPickerButton';

interface HeimdallDesktopNavProps {
  visibleSections: Section[];
  activeSectionId: SectionId;
  activeSection: Section;
  activeSubId: string;
  go: (route: string) => void;
  setCmdOpen: Dispatch<SetStateAction<boolean>>;
  setPresentationMode: Dispatch<SetStateAction<boolean>>;
  setProfileOpen: Dispatch<SetStateAction<boolean>>;
}

export function HeimdallDesktopNav({
  visibleSections, activeSectionId, activeSection, activeSubId, go,
  setCmdOpen, setPresentationMode, setProfileOpen,
}: HeimdallDesktopNavProps) {
  const { t }   = useTranslation();
  const user    = useAuthStore(s => s.user);
  const initials = user ? user.username.slice(0, 2).toUpperCase() : '??';

  const [seerMenuOpen, setSeerMenuOpen] = useState(false);
  const seerMenuRef = useRef<HTMLDivElement>(null);

  const pickDesktopSection = (sec: Section) => {
    if (sec.horizon) return;
    go(sec.subTabs[0]?.route ?? sec.route);
    setSeerMenuOpen(false);
  };

  return (
    <>
      {/* Logo + section switcher dropdown */}
      <div
        ref={seerMenuRef}
        style={{ position: 'relative', flexShrink: 0 }}
        onBlur={e => { if (!seerMenuRef.current?.contains(e.relatedTarget as Node)) setSeerMenuOpen(false); }}
      >
        <button
          onClick={() => setSeerMenuOpen(v => !v)}
          style={{
            display: 'flex', alignItems: 'center', gap: '6px',
            padding: '4px 8px 4px 6px',
            background: seerMenuOpen ? 'var(--bg2)' : 'transparent',
            border: '1px solid',
            borderColor: seerMenuOpen ? 'var(--bd)' : 'transparent',
            borderRadius: 'var(--seer-radius-md)', cursor: 'pointer',
            transition: 'background 0.12s, border-color 0.12s',
          }}
          onMouseEnter={e => {
            if (!seerMenuOpen) {
              (e.currentTarget as HTMLElement).style.background = 'var(--bg2)';
              (e.currentTarget as HTMLElement).style.borderColor = 'var(--bd)';
            }
          }}
          onMouseLeave={e => {
            if (!seerMenuOpen) {
              (e.currentTarget as HTMLElement).style.background = 'transparent';
              (e.currentTarget as HTMLElement).style.borderColor = 'transparent';
            }
          }}
        >
          <span style={{
            fontFamily: 'var(--font-display)', fontSize: '13px',
            letterSpacing: '0.08em', color: 'var(--aida-app-heimdall)',
            textTransform: 'uppercase',
          }}>
            HEIMÐALLR
          </span>
          <ChevronDown size={11} style={{
            color: 'var(--t3)',
            transform: seerMenuOpen ? 'rotate(180deg)' : 'rotate(0)',
            transition: 'transform 0.15s',
          }} />
        </button>

        {seerMenuOpen && (
          <div style={{
            position: 'absolute', top: 'calc(100% + 4px)', left: 0,
            zIndex: 300, minWidth: '260px',
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
              HEIMÐALLR ADMIN
            </div>
            {visibleSections.map(sec => {
              const isCurrent  = sec.id === activeSectionId;
              const isDisabled = !!sec.horizon;
              return (
                <button
                  key={sec.id}
                  onClick={() => pickDesktopSection(sec)}
                  disabled={isDisabled}
                  style={{
                    display: 'flex', alignItems: 'center', gap: '10px',
                    width: '100%', padding: '10px 12px',
                    background: isCurrent ? 'color-mix(in srgb, var(--acc) 8%, transparent)' : 'transparent',
                    border: 'none',
                    color: isCurrent ? 'var(--acc)' : isDisabled ? 'var(--t3)' : 'var(--t1)',
                    fontSize: '12px', fontWeight: 500,
                    cursor: isDisabled ? 'not-allowed' : 'pointer',
                    opacity: isDisabled ? 0.45 : 1,
                    textAlign: 'left',
                    transition: 'background 0.1s',
                  }}
                  onMouseEnter={e => {
                    if (!isCurrent && !isDisabled) (e.currentTarget as HTMLElement).style.background = 'var(--bg3)';
                  }}
                  onMouseLeave={e => {
                    if (!isCurrent && !isDisabled) (e.currentTarget as HTMLElement).style.background = 'transparent';
                  }}
                >
                  <div style={{
                    width: 5, height: 5, borderRadius: '50%', flexShrink: 0,
                    background: isCurrent ? 'var(--acc)' : 'transparent',
                  }} />
                  <div style={{ flex: 1 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <span style={{ fontWeight: 600, letterSpacing: '0.04em' }}>{sec.id}</span>
                      {sec.horizon && (
                        <span style={{
                          fontSize: '9px', fontWeight: 600, letterSpacing: '0.05em',
                          padding: '1px 5px', borderRadius: '3px',
                          background: 'color-mix(in srgb, var(--t3) 15%, transparent)',
                          color: 'var(--t3)',
                        }}>
                          {sec.horizon}
                        </span>
                      )}
                    </div>
                    <div style={{ fontSize: '10px', color: 'var(--t3)', marginTop: '2px' }}>
                      {t(sec.descKey)}
                    </div>
                  </div>
                </button>
              );
            })}
          </div>
        )}
      </div>

      <HDivider />

      {/* Active section name */}
      <button
        onClick={() => go(activeSection.subTabs[0]?.route ?? activeSection.route)}
        title={`${activeSection.id} — ${t(activeSection.descKey)}`}
        style={{
          padding: '5px 10px',
          fontSize: '11px', fontWeight: 700, letterSpacing: '0.1em',
          color: 'var(--t1)',
          background: 'transparent', border: 'none',
          cursor: 'pointer', borderRadius: 'var(--seer-radius-sm)',
          transition: 'color 0.12s', flexShrink: 0,
        }}
        onMouseEnter={e => { (e.currentTarget as HTMLElement).style.color = 'var(--acc)'; }}
        onMouseLeave={e => { (e.currentTarget as HTMLElement).style.color = 'var(--t1)'; }}
      >
        {activeSection.id}
      </button>

      {/* Tenant picker (admin + super-admin) */}
      {(user?.role === 'admin' || user?.role === 'super-admin') && (
        <>
          <HDivider />
          <TenantPickerButton />
        </>
      )}

      <HDivider />

      {/* Active section sub-tabs, inline */}
      <nav style={{ display: 'flex', gap: '2px', flex: 1, alignItems: 'center' }}>
        {activeSection.subTabs.map(sub => {
          const isSub = sub.id === activeSubId;
          return (
            <button
              key={sub.id}
              onClick={() => go(sub.route)}
              style={{
                padding: '6px 14px', fontSize: '12px',
                fontWeight: isSub ? 500 : 400,
                borderRadius: 'var(--seer-radius-sm)', border: 'none',
                cursor: 'pointer',
                background: isSub ? 'color-mix(in srgb, var(--acc) 12%, transparent)' : 'transparent',
                color: isSub ? 'var(--acc)' : 'var(--t2)',
                transition: 'background 0.12s, color 0.12s',
                letterSpacing: '0.06em',
              }}
              onMouseEnter={e => { if (!isSub) (e.currentTarget as HTMLElement).style.background = 'var(--bg2)'; }}
              onMouseLeave={e => { if (!isSub) (e.currentTarget as HTMLElement).style.background = 'transparent'; }}
            >
              {t(sub.labelKey)}
            </button>
          );
        })}
      </nav>

      {/* Desktop secondary tools */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
        <button
          onClick={() => setCmdOpen(v => !v)}
          title={t('commandPalette.title') + ' (⌘K)'}
          style={{
            display: 'flex', alignItems: 'center', gap: '6px', padding: '5px 10px',
            background: 'var(--bg2)', border: '1px solid var(--bd)',
            borderRadius: 'var(--seer-radius-md)', color: 'var(--t2)',
            fontSize: '11px', cursor: 'pointer', letterSpacing: '0.04em',
          }}
        >
          <span>⌘K</span>
        </button>

        <HDivider />
        <LanguageSwitcher />
        <PaletteDropdown />

        <button
          onClick={() => setPresentationMode(true)}
          title="Presentation mode (fullscreen)"
          style={{
            background: 'transparent', border: '1px solid var(--bd)',
            borderRadius: 'var(--seer-radius-md)', padding: '5px 7px',
            cursor: 'pointer', color: 'var(--t2)', display: 'flex', alignItems: 'center', fontSize: '13px',
          }}
        >
          ⛶
        </button>

        <HDivider />

        <ThemeToggle />

        {user && (
          <button
            onClick={() => setProfileOpen(true)}
            title={`${user.username} · ${user.role}`}
            className="hh-btn"
            style={{
              display: 'flex', alignItems: 'center', gap: '6px',
              padding: '4px 8px 4px 5px',
            }}
          >
            <div style={{
              width: 24, height: 24, borderRadius: '50%',
              background: 'color-mix(in srgb, var(--acc) 20%, transparent)',
              border: '1px solid color-mix(in srgb, var(--acc) 50%, transparent)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              fontSize: '10px', fontWeight: 600, color: 'var(--acc)', flexShrink: 0,
            }}>
              {initials}
            </div>
            <span style={{ fontSize: '12px', color: 'var(--t2)' }}>{user.username}</span>
          </button>
        )}
      </div>
    </>
  );
}
