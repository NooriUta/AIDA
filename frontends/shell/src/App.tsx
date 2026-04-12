import React, { Suspense, useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { AidaNav }        from './components/AidaNav';
import { useShellStore }  from './stores/shellStore';

// ── Lazy remote apps ──────────────────────────────────────────────────────────
// Each remote is loaded from Module Federation remoteEntry.js.
// If the remote is unavailable, the ErrorBoundary (below) catches the error.
const VerdandiApp    = React.lazy(() => import('verdandi/App'));
const HeimdallApp    = React.lazy(() => import('heimdall-frontend/App'));

// ── Error boundary for failed remote loads ────────────────────────────────────
class RemoteErrorBoundary extends React.Component<
  { name: string; children: React.ReactNode },
  { failed: boolean }
> {
  state = { failed: false };
  static getDerivedStateFromError() { return { failed: true }; }
  render() {
    if (this.state.failed) {
      return (
        <div style={{
          padding: 'var(--seer-space-8)', color: 'var(--danger)',
          fontSize: '13px', fontFamily: 'var(--mono)',
        }}>
          Failed to load {this.props.name}. Is the dev server running?
        </div>
      );
    }
    return this.props.children;
  }
}

// ── Route sync with shellStore ────────────────────────────────────────────────
function NavigateBridge() {
  const navigate       = useNavigate();
  const _setNavigate   = useShellStore(s => s._setNavigate);
  const navigateTo     = useShellStore(s => s.navigateTo);

  // Register navigate with the store so components outside the router can call it
  useEffect(() => {
    _setNavigate(navigate);
  }, [navigate, _setNavigate]);

  // Sync currentApp from URL on first load
  useEffect(() => {
    const path = window.location.pathname;
    if (path.startsWith('/heimdall')) navigateTo('heimdall');
    else                             navigateTo('verdandi');
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return null;
}

// ── Suspense fallback ─────────────────────────────────────────────────────────
function AppLoader() {
  const { t } = useTranslation();
  return (
    <div style={{
      flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center',
      color: 'var(--t3)', fontSize: '13px',
    }}>
      {t('status.loading')}
    </div>
  );
}

// ── Root layout ───────────────────────────────────────────────────────────────
function AppLayout() {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <AidaNav />
      <main style={{ flex: 1, overflow: 'hidden', display: 'flex', flexDirection: 'column' }}>
        <Suspense fallback={<AppLoader />}>
          <Routes>
            <Route
              path="/verdandi/*"
              element={
                <RemoteErrorBoundary name="Seiðr Studio (verdandi)">
                  <VerdandiApp />
                </RemoteErrorBoundary>
              }
            />
            <Route
              path="/heimdall/*"
              element={
                <RemoteErrorBoundary name="HEIMDALL Control">
                  <HeimdallApp />
                </RemoteErrorBoundary>
              }
            />
            <Route path="*" element={<Navigate to="/verdandi" replace />} />
          </Routes>
        </Suspense>
      </main>
    </div>
  );
}

// ── App root ──────────────────────────────────────────────────────────────────
export default function App() {
  return (
    <BrowserRouter>
      <NavigateBridge />
      <AppLayout />
    </BrowserRouter>
  );
}
