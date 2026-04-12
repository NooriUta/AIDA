import 'aida-shared/styles/tokens';
import { initTheme } from 'aida-shared/theme';

// Apply theme/palette to <html> before first render (shared key with verdandi + heimdall)
initTheme();

import './i18n/config';   // initialise i18next

import React from 'react';
import ReactDOM from 'react-dom/client';
import './styles/shell.css';
import App from './App';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
);
