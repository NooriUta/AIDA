import { lazy, Suspense, useEffect } from 'react';
import { BrowserRouter, Routes, Route, useLocation } from 'react-router-dom';
import { Shell } from './components/layout/Shell';
import { LoginPage } from './components/auth/LoginPage';
import { ProtectedRoute } from './components/auth/ProtectedRoute';
import { ErrorBoundary } from './components/ErrorBoundary';
import { ToastContainer } from './components/Toast';
import { useAuthStore } from './stores/authStore';

const KnotPage = lazy(() =>
  import('./components/knot/KnotPage').then((m) => ({ default: m.KnotPage })),
);

// ── Dev-only proto routes ─────────────────────────────────────────────────────
// lazy() keeps proto code out of the main chunk; Vite tree-shakes on build.
const FilterToolbarProto = lazy(() =>
  import('./components/layout/proto/FilterToolbarProto').then((m) => ({
    default: m.FilterToolbarProto,
  })),
);
const FilterToolbarProtoRu = lazy(() =>
  import('./components/layout/proto/FilterToolbarProtoRu').then((m) => ({
    default: m.FilterToolbarProtoRu,
  })),
);
const FilterToolbarL1Proto = lazy(() =>
  import('./components/layout/proto/FilterToolbarL1Proto').then((m) => ({
    default: m.FilterToolbarL1Proto,
  })),
);

export default function App() {
  const checkSession = useAuthStore((s) => s.checkSession);

  // Verify the httpOnly cookie is still valid after page reload.
  // If the 8h token expired, checkSession clears state → ProtectedRoute redirects.
  useEffect(() => { checkSession(); }, []); // eslint-disable-line react-hooks/exhaustive-deps

  return (
    <BrowserRouter>
      <AppRoutes />
      <ToastContainer />
    </BrowserRouter>
  );
}

/** Separated so useLocation() is inside BrowserRouter. */
function AppRoutes() {
  const { pathname } = useLocation();

  return (
    <ErrorBoundary resetKey={pathname}>
      <Routes>
        <Route path="/login" element={<LoginPage />} />

        {/* ── Dev-only prototype viewer at /__proto__/* ── */}
        {import.meta.env.DEV && (
          <>
            <Route
              path="/__proto__/filter-toolbar"
              element={
                <Suspense fallback={null}>
                  <FilterToolbarProto />
                </Suspense>
              }
            />
            <Route
              path="/__proto__/filter-toolbar-ru"
              element={
                <Suspense fallback={null}>
                  <FilterToolbarProtoRu />
                </Suspense>
              }
            />
            <Route
              path="/__proto__/l1-filter-toolbar"
              element={
                <Suspense fallback={null}>
                  <FilterToolbarL1Proto />
                </Suspense>
              }
            />
          </>
        )}

        <Route
          path="/knot"
          element={
            <ProtectedRoute>
              <Suspense fallback={null}>
                <KnotPage />
              </Suspense>
            </ProtectedRoute>
          }
        />

        <Route
          path="/*"
          element={
            <ProtectedRoute>
              <Shell />
            </ProtectedRoute>
          }
        />
      </Routes>
    </ErrorBoundary>
  );
}
