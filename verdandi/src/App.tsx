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
