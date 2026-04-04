import { lazy, Suspense, useEffect } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { Shell } from './components/layout/Shell';
import { LoginPage } from './components/auth/LoginPage';
import { ProtectedRoute } from './components/auth/ProtectedRoute';
import { useAuthStore } from './stores/authStore';

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
const L1NodesProto = lazy(() =>
  import('./components/canvas/nodes/proto/L1NodesProto').then((m) => ({
    default: m.L1NodesProto,
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
              path="/__proto__/l1-nodes"
              element={
                <Suspense fallback={null}>
                  <L1NodesProto />
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
          path="/*"
          element={
            <ProtectedRoute>
              <Shell />
            </ProtectedRoute>
          }
        />
      </Routes>
    </BrowserRouter>
  );
}
