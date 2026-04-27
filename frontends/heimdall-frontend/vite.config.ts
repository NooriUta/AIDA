import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { federation } from '@module-federation/vite';
import type { ServerOptions } from 'http-proxy';

// Strip `Secure` from Set-Cookie + forward original Host so chur generates
// correct redirect_uri for Auth Code flow (must match :5174 vite dev port).
function stripSecureCookie() {
  return {
    configure(proxy: import('http-proxy').Server) {
      proxy.on('proxyReq', (proxyReq: import('http').ClientRequest, req: import('http').IncomingMessage) => {
        // Forward original host so chur knows the public-facing port (:5174)
        if (req.headers.host) {
          proxyReq.setHeader('X-Forwarded-Host', req.headers.host);
          proxyReq.setHeader('X-Forwarded-Proto', 'http');
        }
      });
      proxy.on('proxyRes', (proxyRes: import('http').IncomingMessage) => {
        const cookies = proxyRes.headers['set-cookie'];
        if (cookies) {
          proxyRes.headers['set-cookie'] = cookies.map((c: string) =>
            c.replace(/;\s*Secure/gi, ''),
          );
        }
      });
    },
  } satisfies ServerOptions;
}

export default defineConfig({
  plugins: [
    react(),
    federation({
      name: 'heimdall-frontend',
      filename: 'remoteEntry.js',
      exposes: { './App': './src/App.tsx' },
      shared: {
        react:              { singleton: true, eager: true, requiredVersion: '^19.0.0' },
        'react-dom':        { singleton: true, eager: true, requiredVersion: '^19.0.0' },
        'react-router-dom': { singleton: true, eager: true, requiredVersion: '^7.0.0' },
        'aida-shared':      { singleton: true },
        zustand:            { singleton: true, requiredVersion: '^5.0.0' },
      },
    }),
  ],
  resolve: {
    dedupe: ['react', 'react-dom', 'react-router-dom', 'zustand'],
  },
  optimizeDeps: {
    // Exclude react-router-dom so Vite does NOT pre-bundle it into a chunk.
    // The MF runtime can then redirect to Shell's singleton, preventing
    // duplicate instances in dev mode. Requires Vite 8 (Vite 6 had a bug
    // where the virtual loadShare module didn't expose all named exports).
    exclude: ['react-router-dom'],
  },
  // Module Federation generates top-level await — requires es2022+ target.
  // base is '/' (same as verdandi) — assets served from root of heimdall.* subdomain.
  // The old '/heimdall/' base caused MIME errors: files live at /assets/ in the
  // container but the browser requested /heimdall/assets/ → SPA fallback → text/html.
  build: {
    target: 'es2022',
    sourcemap: false,
  },
  server: {
    port: 5174,
    host: '0.0.0.0',
    cors: true,
    proxy: {
      ...(() => {
        const churTarget    = process.env.CHUR_PROXY_TARGET    ?? 'http://127.0.0.1:3000';
        const heimdallTarget = process.env.HEIMDALL_PROXY_TARGET ?? 'http://127.0.0.1:9093';
        const jobrunrTarget = process.env.JOBRUNR_PROXY_TARGET  ?? 'http://127.0.0.1:29091';
        return {
          // Auth + admin API + user self-service go to Chur as-is
          '/auth':     { target: churTarget, changeOrigin: true, ...stripSecureCookie() },
          '/prefs':    { target: churTarget, changeOrigin: true, ...stripSecureCookie() },
          '/api/admin': { target: churTarget, changeOrigin: true },
          '/chur':     { target: churTarget, changeOrigin: true,
                         rewrite: (p: string) => p.replace(/^\/chur/, ''),
                         ...stripSecureCookie() },
          // Heimdall API paths: dev server receives /health, /metrics etc. — rewrite to /heimdall/* on Chur
          '/health':   { target: churTarget, changeOrigin: true, rewrite: (p: string) => `/heimdall${p}` },
          '/metrics':  { target: churTarget, changeOrigin: true, rewrite: (p: string) => `/heimdall${p}` },
          '/control':  { target: churTarget, changeOrigin: true, rewrite: (p: string) => `/heimdall${p}` },
          '/users':     { target: churTarget, changeOrigin: true, rewrite: (p: string) => `/heimdall${p}` },
          '/services':  { target: churTarget, changeOrigin: true, rewrite: (p: string) => `/heimdall${p}` },
          '/databases': { target: churTarget, changeOrigin: true, rewrite: (p: string) => `/heimdall${p}` },
          '/team-docs': { target: churTarget, changeOrigin: true, rewrite: (p: string) => `/heimdall${p}` },
          '/docs':      { target: churTarget, changeOrigin: true, rewrite: (p: string) => `/heimdall${p}` },
          // WebSocket event stream — browser connects to /heimdall/ws/events (resolveWsUrl in api.ts)
          '/heimdall/ws': { target: churTarget.replace('http', 'ws'), changeOrigin: true, ws: true },
          '/dali/api':      { target: process.env.DALI_PROXY_TARGET ?? 'http://127.0.0.1:9090',  changeOrigin: true, rewrite: (p: string) => p.replace(/^\/dali/, '') },
          '/dali/q':        { target: process.env.DALI_PROXY_TARGET ?? 'http://127.0.0.1:9090',  changeOrigin: true, rewrite: (p: string) => p.replace(/^\/dali/, '') },
          '/jobrunr':       { target: jobrunrTarget, changeOrigin: true, rewrite: (p: string) => p.replace(/^\/jobrunr/, '') },
          '/highload-plan': { target: heimdallTarget, changeOrigin: true },
        };
      })(),
    },
  },
});
