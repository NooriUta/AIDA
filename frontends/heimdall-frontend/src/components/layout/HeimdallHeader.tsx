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
import { SECTIONS, type SectionId } from './heimdallNavData';
import { ThemeToggle, DropdownMenu, DropdownHeader, DropdownItem } from './HeimdallHeaderControls';
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

  // Mobile: one-step combined nav picker (HX-01)
  const [mobileNavOpen, setMobileNavOpen] = useState(false);
  const mobileNavRef = useRef<HTMLDivElement>(null);

  const [cmdOpen,          setCmdOpen]          = useState(false);
  const [profileOpen,      setProfileOpen]      = useState(false);
  const [presentationMode, setPresentationMode] = useState(false);

  useHotkeys([{ key: 'k', ctrl: true, action: () => setCmdOpen(v => !v), global: true }]);

  const { canManageUsers, isAdmin } = useTenantContext();

  // React Router basename ('/heimdall' in standalone, provided by Shell in MF
  // mode) already prepends the prefix, so pass absolute app-internal routes.
  const go = useCallback(
    (route: string) => navigate(route),
    [navigate],
  );

  // Show FENRIR section when user can manage users (local-admin+).
  // Filter subtabs by minRole: 'admin' tabs hidden for local-admin/tenant-owner.
  const visibleSections = SECTIONS
    .filter(sec => sec.id !== 'FENRIR' || canManageUsers)
    .map(sec => ({
      ...sec,
      subTabs: sec.subTabs.filter(tab => {
        if (tab.minRole === 'admin')       return isAdmin;
        if (tab.minRole === 'local-admin') return canManageUsers;
        return true;
      }),
    }))
    .filter(sec => sec.subTabs.length > 0);

  // Active section/subtab derived from pathname
  const activeSectionId: SectionId =
    pathname.includes('/admin/tenants') ? 'FENRIR' :
    pathname.includes('/users')         ? 'FENRIR' :
    pathname.includes('/analytics')     ? 'FENRIR' :
    pathname.includes('/dali')          ? 'DALI'   :
    pathname.includes('/docs')          ? 'SAGA'   :
    'BIFROST';

  const activeSubId: string =
    pathname.includes('/admin/tenants')  ? 'Tenants'   :
    pathname.includes('/users')          ? 'Users'     :
    pathname.includes('/analytics')      ? 'Analytics' :
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
             MOBILE LAYOUT (HX-01: 1-step nav)
             Single button: "B. Dashboard▾" opens combined dropdown:
               • sub-tabs of active section (top)
               • divider
               • other sections (bottom, section-switching)
             ══════════════════════════════════════════════════════ */
          <>
            {/* Combined 1-step nav picker */}
            <div
              ref={mobileNavRef}
              style={{ position: 'relative', flexShrink: 0 }}
              onBlur={e => {
                if (!mobileNavRef.current?.contains(e.relatedTarget as Node))
                  setMobileNavOpen(false);
              }}
            >
              <button
                onClick={() => setMobileNavOpen(v => !v)}
                style={{
                  display: 'flex', alignItems: 'center', gap: '4px',
                  padding: '4px 8px',
                  background: mobileNavOpen ? 'var(--bg2)' : 'transparent',
                  border: '1px solid',
                  borderColor: mobileNavOpen ? 'var(--bd)' : 'transparent',
                  borderRadius: 'var(--seer-radius-md)', cursor: 'pointer',
                  transition: 'background 0.12s, border-color 0.12s',
                }}
              >
                <span style={{ fontSize: '12px', fontWeight: 700, letterSpacing: '0.06em', color: 'var(--acc)' }}>
                  {activeSectionId[0]}.
                </span>
                <span style={{
                  fontSize: '11px', fontWeight: 500, color: 'var(--t2)',
                  maxWidth: 70, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
                }}>
                  {activeSubTab ? t(activeSubTab.labelKey) : activeSubId}
                </span>
                <ChevronDown size={9} style={{
                  color: 'var(--t3)',
                  transform: mobileNavOpen ? 'rotate(180deg)' : 'rotate(0)',
                  transition: 'transform 0.15s',
                }} />
              </button>

              {mobileNavOpen && (
                <DropdownMenu>
                  {/* Sub-tabs of active section */}
                  <DropdownHeader label={activeSectionId} />
                  {activeSection.subTabs.map(sub => (
                    <DropdownItem
                      key={sub.id}
                      label={t(sub.labelKey).toUpperCase()}
                      active={sub.id === activeSubId}
                      onClick={() => { go(sub.route); setMobileNavOpen(false); }}
                    />
                  ))}

                  {/* Divider + other sections for switching */}
                  {visibleSections.filter(s => s.id !== activeSectionId).length > 0 && (
                    <>
                      <div style={{ height: '1px', background: 'var(--bd)', margin: '4px 0' }} />
                      {visibleSections
                        .filter(s => s.id !== activeSectionId)
                        .map(sec => (
                          <DropdownItem
                            key={sec.id}
                            label={sec.id}
                            disabled={!!sec.horizon}
                            horizon={sec.horizon}
                            onClick={() => {
                              if (!sec.horizon) {
                                go(sec.subTabs[0]?.route ?? sec.route);
                                setMobileNavOpen(false);
                              }
                            }}
                          />
                        ))}
                    </>
                  )}
                </DropdownMenu>
              )}
            </div>

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
