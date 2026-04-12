import 'aida-shared/styles/tokens';
import { initTheme } from 'aida-shared/theme';

// Apply theme/palette to <html> before first render
initTheme();

import './i18n/config';   // initialise i18next (side-effect import, must be before React)

import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import './styles/heimdall.css';
import App from './App';

// BrowserRouter is provided here for standalone mode (npm run dev at :5174).
// When running as an MF remote inside Shell, App is loaded directly and
// Shell's BrowserRouter provides the router context — main.tsx is not executed.
ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </React.StrictMode>,
);
