import React, { Suspense, useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate, useNavigate } from 'react-router-dom';
import { useTranslation }    from 'react-i18next';
import { AidaNav }           from './components/AidaNav';
import { LoginPage }         from './components/LoginPage';
import { useShellStore }     from './stores/shellStore';
import { useShellAuthStore } from './stores/authStore';

// ── Lazy remote apps ──────────────────────────────────────────────────────────
const VerdandiApp  = React.lazy(() => import('verdandi/App'));
const HeimdallApp  = React.lazy(() => import('heimdall-frontend/App'));

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

  useEffect(() => {
    _setNavigate(navigate);
  }, [navigate, _setNavigate]);

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

// ── Authenticated shell layout ────────────────────────────────────────────────
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
                <RemoteErrorBoundary name="Heimðallr Control">
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

// ── Auth gate — checks session once on mount ──────────────────────────────────
// While checking: shows a centered spinner.
// Not authenticated: renders LoginPage (URL preserved — after login user lands
// exactly where they tried to go).
// Authenticated: renders the full shell with nav + app routes.
function AuthGate({ children }: { children: React.ReactNode }) {
  const checkSession       = useShellAuthStore(s => s.checkSession);
  const isAuthenticated    = useShellAuthStore(s => s.isAuthenticated);
  const isCheckingSession  = useShellAuthStore(s => s.isCheckingSession);

  useEffect(() => { void checkSession(); }, [checkSession]);

  if (isCheckingSession) {
    return (
      <div style={{
        minHeight: '100vh', background: 'var(--bg0)',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        color: 'var(--t3)', fontSize: '13px',
      }}>
        …
      </div>
    );
  }

  if (!isAuthenticated) return <LoginPage />;
  return <>{children}</>;
}

// ── App root ──────────────────────────────────────────────────────────────────
export default function App() {
  return (
    <BrowserRouter>
      <AuthGate>
        <NavigateBridge />
        <AppLayout />
      </AuthGate>
    </BrowserRouter>
  );
}
