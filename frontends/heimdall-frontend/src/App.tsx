// i18n must be initialised before any component renders — importing here
// ensures it runs when App is loaded as an MF remote (main.tsx is not
// executed in that case).
import './i18n/config';
// heimdall.css must also be imported here so styles load in MF-remote mode
// (main.tsx is not executed when Shell imports heimdall-frontend/App).
import './styles/heimdall.css';
import React, { Suspense, useEffect } from 'react';
import { Routes, Route, Navigate, Outlet } from 'react-router-dom';
import { useTranslation }  from 'react-i18next';
import { LoginPage }       from './components/auth/LoginPage';
import { HeimdallHeader }  from './components/layout/HeimdallHeader';
import { useAuthStore }    from './stores/authStore';
import { usePrefsStore }   from './stores/prefsStore';
import { RoleGuard }       from './components/RoleGuard';
import { applyPrefs }      from './stores/sharedPrefsStore';
import type { SharedPrefs } from './stores/sharedPrefsStore';

const ServicesPage    = React.lazy(() => import('./pages/ServicesPage'));
const DashboardPage   = React.lazy(() => import('./pages/DashboardPage'));
const DaliPage        = React.lazy(() => import('./pages/DaliPage'));
const DaliSourcesPage = React.lazy(() => import('./pages/DaliSourcesPage'));
const DaliJobRunrPage = React.lazy(() => import('./pages/DaliJobRunrPage'));
const EventStreamPage = React.lazy(() => import('./pages/EventStreamPage'));
const ControlsPage    = React.lazy(() => import('./pages/ControlsPage'));
const UsersPage       = React.lazy(() => import('./pages/UsersPage'));
const DocsPage        = React.lazy(() => import('./pages/DocsPage'));

// ── App layout (shell around the routed page) ─────────────────────────────────
function AppLayout() {
  const { t } = useTranslation();
  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <HeimdallHeader />
      <main style={{ flex: 1, overflow: 'hidden' }}>
        <Suspense fallback={
          <div style={{ padding: 'var(--seer-space-8)', color: 'var(--t3)' }}>
            {t('status.loading')}
          </div>
        }>
          <Outlet />
        </Suspense>
      </main>
    </div>
  );
}

// ── Protected layout route ────────────────────────────────────────────────────
// In Shell mode: Shell's AuthGate has already validated the session, so
// isAuthenticated is already true when this renders.
// In standalone mode: waits for checkSession to complete before redirecting.
function ProtectedRoute() {
  const isAuthenticated   = useAuthStore(s => s.isAuthenticated);
  const isCheckingSession = useAuthStore(s => s.isCheckingSession);

  if (isCheckingSession) return (
    <div style={{
      flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center',
      color: 'var(--t3)', fontSize: '13px',
    }}>
      …
    </div>
  );
  if (!isAuthenticated) return <Navigate to="login" replace />;
  return <Outlet />;
}

// ── Session check on mount + prefs load on auth ───────────────────────────────
function SessionGuard({ children }: { children: React.ReactNode }) {
  const checkSession = useAuthStore(s => s.checkSession);
  const isAuthenticated = useAuthStore(s => s.isAuthenticated);
  const loadPrefs = usePrefsStore(s => s.load);

  useEffect(() => { void checkSession(); }, [checkSession]);

  // Load server-side prefs whenever the user becomes authenticated
  useEffect(() => {
    if (isAuthenticated) void loadPrefs();
  }, [isAuthenticated, loadPrefs]);

  return <>{children}</>;
}

/**
 * Heimdall-frontend root — exported as MF remote (heimdall-frontend/App).
 *
 * Does NOT include BrowserRouter — the Router context is provided by the
 * host (Shell) when running as a remote, or by main.tsx when running standalone.
 *
 * Uses React Router v6 inline nested routes so NavLinks resolve correctly
 * in both standalone (base "/") and Shell (base "/heimdall/") contexts.
 */
export default function App() {
  useEffect(() => {
    const h = (e: Event) => applyPrefs((e as CustomEvent<Partial<SharedPrefs>>).detail);
    window.addEventListener('aida:prefs', h);
    return () => window.removeEventListener('aida:prefs', h);
  }, []);

  return (
    <SessionGuard>
      <Routes>
        <Route path="login" element={<LoginPage />} />

        {/* Protected area */}
        <Route element={<ProtectedRoute />}>
          <Route element={<AppLayout />}>
            {/* Default redirect */}
            <Route index element={<Navigate to="overview/services" replace />} />

            {/* Overview section: Services · Dashboard · Events */}
            <Route path="overview">
              <Route index element={<Navigate to="services" replace />} />
              <Route path="services"  element={<ServicesPage />} />
              <Route path="dashboard" element={<DashboardPage />} />
              <Route path="events"    element={<EventStreamPage />} />
            </Route>

            {/* Dali section: Sessions · Sources · JobRunr */}
            <Route path="dali">
              <Route index element={<Navigate to="sessions" replace />} />
              <Route path="sessions" element={<DaliPage />} />
              <Route path="sources"  element={<DaliSourcesPage />} />
              <Route path="jobrunr"  element={<DaliJobRunrPage />} />
            </Route>

            {/* Standalone pages */}
            <Route path="users" element={
              <RoleGuard require="local-admin"><UsersPage /></RoleGuard>
            } />
            <Route path="demodebug"      element={<ControlsPage />} />
            <Route path="docs/*"         element={<DocsPage tab="docs" />} />
            <Route path="team-docs/*"    element={<DocsPage tab="team-docs" />} />
            <Route path="team-archive/*" element={<DocsPage tab="team-archive" />} />
            <Route path="highload/*"     element={<DocsPage tab="highload" />} />

            {/* Backward compat: old flat routes redirect to new paths */}
            <Route path="services"  element={<Navigate to="../overview/services"  replace />} />
            <Route path="dashboard" element={<Navigate to="../overview/dashboard" replace />} />
            <Route path="events"    element={<Navigate to="../overview/events"    replace />} />

            <Route path="*" element={<Navigate to="overview/services" replace />} />
          </Route>
        </Route>
      </Routes>
    </SessionGuard>
  );
}
