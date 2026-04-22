import 'aida-shared/styles/tokens';
import { initSharedPrefs } from './stores/sharedPrefsStore';

// Apply saved prefs to <html> before first render (no FOUC)
initSharedPrefs();

import './i18n/config';   // initialise i18next (side-effect import, must be before React)

import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import './styles/heimdall.css';
import App from './App';

// BrowserRouter is provided here for standalone mode (npm run dev at :5174).
// When running as an MF remote inside Shell, App is loaded directly and
// Shell's BrowserRouter provides the router context — main.tsx is not executed.
//
// basename='/heimdall' matches how nginx routes to this frontend. In standalone
// dev the browser URL is http://localhost:5174/heimdall/..., so React Router
// must strip the prefix to match the route tree (which is rooted at /).
ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <BrowserRouter basename="/heimdall">
      <App />
    </BrowserRouter>
  </React.StrictMode>,
);
