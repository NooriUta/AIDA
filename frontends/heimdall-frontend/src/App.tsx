import React, { Suspense } from 'react';
import { BrowserRouter, Routes, Route, Navigate, NavLink } from 'react-router-dom';

const DashboardPage  = React.lazy(() => import('./pages/DashboardPage'));
const EventStreamPage = React.lazy(() => import('./pages/EventStreamPage'));
const ControlsPage   = React.lazy(() => import('./pages/ControlsPage'));

const navStyle: React.CSSProperties = {
  display: 'flex',
  alignItems: 'center',
  gap: 'var(--seer-space-4)',
  padding: 'var(--seer-space-3) var(--seer-space-6)',
  background: 'var(--bg1)',
  borderBottom: '1px solid var(--bd)',
  fontFamily: 'var(--font)',
};

const logoStyle: React.CSSProperties = {
  fontFamily: 'var(--font-display)',
  fontSize: '13px',
  letterSpacing: '0.08em',
  color: 'var(--aida-app-heimdall)',
  marginRight: 'var(--seer-space-4)',
  textTransform: 'uppercase',
};

function NavItem({ to, label }: { to: string; label: string }) {
  return (
    <NavLink
      to={to}
      style={({ isActive }) => ({
        fontSize: '13px',
        fontWeight: 500,
        color: isActive ? 'var(--acc)' : 'var(--t2)',
        borderBottom: isActive ? '2px solid var(--acc)' : '2px solid transparent',
        paddingBottom: '2px',
        transition: 'color 0.15s, border-color 0.15s',
        textDecoration: 'none',
      })}
    >
      {label}
    </NavLink>
  );
}

function AppLayout() {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <nav style={navStyle}>
        <span style={logoStyle}>HEIMDALL</span>
        <NavItem to="/dashboard"  label="Dashboard" />
        <NavItem to="/events"     label="Event Stream" />
        <NavItem to="/controls"   label="Controls" />
      </nav>

      <main style={{ flex: 1, overflow: 'hidden' }}>
        <Suspense fallback={
          <div style={{ padding: 'var(--seer-space-8)', color: 'var(--t3)' }}>Loading…</div>
        }>
          <Routes>
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/events"    element={<EventStreamPage />} />
            <Route path="/controls"  element={<ControlsPage />} />
            <Route path="*"          element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </Suspense>
      </main>
    </div>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AppLayout />
    </BrowserRouter>
  );
}
