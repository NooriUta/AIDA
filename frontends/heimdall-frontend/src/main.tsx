import 'aida-shared/styles/tokens';
import { initTheme } from 'aida-shared/theme';

// Apply theme/palette to <html> before first render
initTheme();

import './i18n/config';   // initialise i18next (side-effect import, must be before React)

import React from 'react';
import ReactDOM from 'react-dom/client';
import './styles/heimdall.css';
import App from './App';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
);
