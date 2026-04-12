import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import './i18n/config';
import './styles/globals.css';
import '@xyflow/react/dist/style.css';
import App from './App.tsx';

// Apply saved theme + palette before first render to avoid flash.
// Mirrors the initTheme() logic in aida-shared so the theme is consistent
// when verdandi runs standalone (direct :5173) without the shell.
const savedTheme = localStorage.getItem('seer-theme') ?? 'dark';
document.documentElement.setAttribute('data-theme', savedTheme);
const savedPalette = localStorage.getItem('seer-palette');
if (savedPalette && savedPalette !== 'amber-forest') {
  document.documentElement.setAttribute('data-palette', savedPalette);
}

// Standalone entry — wraps App in BrowserRouter for direct :5173 access.
// When loaded as an MF remote inside Shell, the Shell provides the Router context
// and this file is NOT executed (App.tsx is imported directly via MF).
createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </StrictMode>,
);
