import { memo, useRef, useState, useCallback } from 'react';
import { ChevronDown } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore }        from '../../stores/authStore';
import { useDashboardStore }   from '../../stores/dashboardStore';
import { ProfileModal }        from '../profile/ProfileModal';
import { PresentationMode }    from '../PresentationMode';
import { useIsMobile }         from '../../hooks/useIsMobile';
import { useTenantContext }    from '../../hooks/useTenantContext';
import { useHotkeys }          from '../../hooks/useHotkeys';
import { HeimdallCommandPalette } from '../HeimdallCommandPalette';
import { SECTIONS, type SectionId, type Section } from './heimdallNavData';
import { HDivider, ThemeToggle, DropdownMenu, DropdownHeader, DropdownItem } from './HeimdallHeaderControls';
import { TenantPickerButton } from './HeimdallTenantPickerButton';
import { HeimdallDesktopNav } from './HeimdallDesktopNav';

// ── Main header ────────────────────────────────────────────────────────────────
export const HeimdallHeader = memo(() => {
  const { t }        = useTranslation();
  const navigate     = useNavigate();
  const { pathname } = useLocation();
  const user         = useAuthStore(s => s.user);
  const events       = useDashboardStore(s => s.events);
  const metrics      = useDashboardStore(s => s.metrics);
  const isMobile     = useIsMobile();

  // Mobile: two-step picker state
  const [sectionPickerOpen, setSectionPickerOpen] = useState(false);
  const [subPickerOpen,     setSubPickerOpen]     = useState(false);
  const sectionPickerRef = useRef<HTMLDivElement>(null);
  const subPickerRef     = useRef<HTMLDivElement>(null);

  const [cmdOpen,          setCmdOpen]          = useState(false);
  const [profileOpen,      setProfileOpen]      = useState(false);
  const [presentationMode, setPresentationMode] = useState(false);

  useHotkeys([{ key: 'k', ctrl: true, action: () => setCmdOpen(v => !v), global: true }]);

  const { canManageUsers } = useTenantContext();

  // React Router basename ('/heimdall' in standalone, provided by Shell in MF
  // mode) already prepends the prefix, so pass absolute app-internal routes.
  const go = useCallback(
    (route: string) => navigate(route),
    [navigate],
  );

  // Only show FENRIR section when user can manage users
  const visibleSections = SECTIONS.filter(
    sec => sec.id !== 'FENRIR' || canManageUsers,
  );

  // Active section/subtab derived from pathname
  const activeSectionId: SectionId =
    pathname.includes('/admin/tenants') ? 'FENRIR' :
    pathname.includes('/users')    ? 'FENRIR' :
    pathname.includes('/dali')     ? 'DALI'   :
    pathname.includes('/docs')     ? 'SAGA'   :
    'BIFROST';

  const activeSubId: string =
    pathname.includes('/admin/tenants')  ? 'Tenants'   :
    pathname.includes('/users')          ? 'Users'     :
    pathname.includes('/dali/sources')   ? 'Sources'   :
    pathname.includes('/dali')           ? 'Sessions'  :
    pathname.includes('/docs')           ? 'Docs'      :
    pathname.includes('/dashboard')      ? 'Dashboard' :
    pathname.includes('/events')         ? 'Events'    :
    'Services';

  const activeSection = visibleSections.find(s => s.id === activeSectionId)
    ?? visibleSections[0]!;
  const activeSubTab  = activeSection?.subTabs.find(s => s.id === activeSubId);

  const initials = user ? user.username.slice(0, 2).toUpperCase() : '??';

  // Navigate to a section (mobile section picker)
  const pickSection = (sec: Section) => {
    if (sec.horizon) return;
    go(sec.subTabs[0]?.route ?? sec.route);
    setSectionPickerOpen(false);
  };

  return (
    <>
      <header style={{
        height: '42px', background: 'var(--bg0)',
        borderBottom: '1px solid var(--bd)',
        display: 'flex', alignItems: 'center',
        padding: '0 12px', gap: '6px', flexShrink: 0, zIndex: 100,
      }}>

        {isMobile ? (
          /* ══════════════════════════════════════════════════════
             MOBILE LAYOUT
             Step 1: HEIMÐALLR▾ → section picker (full names)
             Step 2: X.▾ → sub-page picker + current page label
             ══════════════════════════════════════════════════════ */
          <>
            {/* Step 1 — section picker */}
            <div
              ref={sectionPickerRef}
              style={{ position: 'relative', flexShrink: 0 }}
              onBlur={e => { if (!sectionPickerRef.current?.contains(e.relatedTarget as Node)) setSectionPickerOpen(false); }}
            >
              <button
                onClick={() => { setSectionPickerOpen(v => !v); setSubPickerOpen(false); }}
                style={{
                  display: 'flex', alignItems: 'center', gap: '4px',
                  padding: '4px 6px',
                  background: sectionPickerOpen ? 'var(--bg2)' : 'transparent',
                  border: '1px solid',
                  borderColor: sectionPickerOpen ? 'var(--bd)' : 'transparent',
                  borderRadius: 'var(--seer-radius-md)', cursor: 'pointer',
                  transition: 'background 0.12s, border-color 0.12s',
                }}
              >
                <span style={{
                  fontFamily: 'var(--font-display)', fontSize: '12px',
                  letterSpacing: '0.08em', color: 'var(--aida-app-heimdall)',
                  textTransform: 'uppercase',
                }}>
                  HEIMÐALLR
                </span>
                <ChevronDown size={10} style={{
                  color: 'var(--t3)',
                  transform: sectionPickerOpen ? 'rotate(180deg)' : 'rotate(0)',
                  transition: 'transform 0.15s',
                }} />
              </button>

              {sectionPickerOpen && (
                <DropdownMenu>
                  <DropdownHeader label="HEIMÐALLR" />
                  {visibleSections.map(sec => (
                    <DropdownItem
                      key={sec.id}
                      label={sec.id}
                      active={sec.id === activeSectionId}
                      disabled={!!sec.horizon}
                      horizon={sec.horizon}
                      onClick={() => pickSection(sec)}
                    />
                  ))}
                </DropdownMenu>
              )}
            </div>

            <HDivider />

            {/* Step 2 — section-letter dropdown (sub-pages of active section) */}
            <div
              ref={subPickerRef}
              style={{ position: 'relative', flexShrink: 0 }}
              onBlur={e => { if (!subPickerRef.current?.contains(e.relatedTarget as Node)) setSubPickerOpen(false); }}
            >
              <button
                onClick={() => {
                  if (activeSection.subTabs.length > 1) setSubPickerOpen(v => !v);
                }}
                title={activeSectionId}
                style={{
                  display: 'flex', alignItems: 'center', gap: '3px',
                  padding: '4px 6px',
                  background: subPickerOpen ? 'var(--bg2)' : 'transparent',
                  border: '1px solid',
                  borderColor: subPickerOpen ? 'var(--bd)' : 'transparent',
                  borderRadius: 'var(--seer-radius-md)', cursor: 'pointer',
                  transition: 'background 0.12s, border-color 0.12s',
                }}
              >
                <span style={{ fontSize: '12px', fontWeight: 700, letterSpacing: '0.06em', color: 'var(--acc)' }}>
                  {activeSectionId[0]}.
                </span>
                {activeSection.subTabs.length > 1 && (
                  <ChevronDown size={9} style={{
                    color: 'var(--t3)',
                    transform: subPickerOpen ? 'rotate(180deg)' : 'rotate(0)',
                    transition: 'transform 0.15s',
                  }} />
                )}
              </button>

              {subPickerOpen && activeSection.subTabs.length > 1 && (
                <DropdownMenu>
                  <DropdownHeader label={activeSectionId} />
                  {activeSection.subTabs.map(sub => (
                    <DropdownItem
                      key={sub.id}
                      label={t(sub.labelKey).toUpperCase()}
                      active={sub.id === activeSubId}
                      onClick={() => { go(sub.route); setSubPickerOpen(false); }}
                    />
                  ))}
                </DropdownMenu>
              )}
            </div>

            {/* Current sub-page name */}
            <span style={{
              fontSize: '12px', fontWeight: 600, letterSpacing: '0.06em',
              color: 'var(--t1)', whiteSpace: 'nowrap',
            }}>
              {activeSubTab ? t(activeSubTab.labelKey).toUpperCase() : activeSubId.toUpperCase()}
            </span>

            {(user?.role === 'admin' || user?.role === 'super-admin') && (
              <TenantPickerButton />
            )}

            <div style={{ flex: 1 }} />

            {/* Always visible: theme + avatar */}
            <ThemeToggle />
            {user && (
              <button
                onClick={() => setProfileOpen(true)}
                title={`${user.username} · ${user.role}`}
                className="hh-btn"
                style={{ display: 'flex', alignItems: 'center', padding: '4px 5px' }}
              >
                <div style={{
                  width: 24, height: 24, borderRadius: '50%',
                  background: 'color-mix(in srgb, var(--acc) 20%, transparent)',
                  border: '1px solid color-mix(in srgb, var(--acc) 50%, transparent)',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  fontSize: '10px', fontWeight: 600, color: 'var(--acc)',
                }}>
                  {initials}
                </div>
              </button>
            )}
          </>
        ) : (
          /* ══════════════════════════════════════════════════════
             DESKTOP LAYOUT — delegated to HeimdallDesktopNav
             ══════════════════════════════════════════════════════ */
          <HeimdallDesktopNav
            visibleSections={visibleSections}
            activeSectionId={activeSectionId}
            activeSection={activeSection}
            activeSubId={activeSubId}
            go={go}
            setCmdOpen={setCmdOpen}
            setPresentationMode={setPresentationMode}
            setProfileOpen={setProfileOpen}
          />
        )}
      </header>

      {cmdOpen && (
        <HeimdallCommandPalette
          open={cmdOpen}
          onClose={() => setCmdOpen(false)}
          sections={visibleSections}
          onNavigate={go}
        />
      )}
      {profileOpen && <ProfileModal onClose={() => setProfileOpen(false)} />}
      {presentationMode && (
        <PresentationMode events={events} metrics={metrics} onExit={() => setPresentationMode(false)} />
      )}
    </>
  );
});

HeimdallHeader.displayName = 'HeimdallHeader';
