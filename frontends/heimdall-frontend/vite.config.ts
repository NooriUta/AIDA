import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { federation } from '@module-federation/vite';

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
      // Auth + admin API + user self-service go to Chur as-is
      '/auth':     { target: 'http://127.0.0.1:3000', changeOrigin: true },
      '/prefs':    { target: 'http://127.0.0.1:3000', changeOrigin: true },
      '/api/admin': { target: 'http://127.0.0.1:3000', changeOrigin: true },  // MTN-63 + tenant admin
      // NOTE: `/me` is deliberately NOT proxied as a top-level path — it shadows
      // the SPA route /me/profile etc. FE code uses `/chur/me/*` which is
      // routed through the `/chur` proxy below.
      // Shell-style routing: '/chur/*' is prod path via shell:5175. In standalone
      // heimdall-frontend dev, strip '/chur' prefix so /chur/api/admin/tenants →
      // http://127.0.0.1:3000/api/admin/tenants. Fixes TenantsPage/UsersPage HTML
      // fallback (was: Vite SPA fallback returned index.html → JSON parse error).
      '/chur':     { target: 'http://127.0.0.1:3000', changeOrigin: true,
                     rewrite: (p: string) => p.replace(/^\/chur/, '') },
      // Heimdall API paths: dev server receives /health, /metrics etc. — rewrite to /heimdall/* on Chur
      '/health':   { target: 'http://127.0.0.1:3000', changeOrigin: true, rewrite: (p: string) => `/heimdall${p}` },
      '/metrics':  { target: 'http://127.0.0.1:3000', changeOrigin: true, rewrite: (p: string) => `/heimdall${p}` },
      '/control':  { target: 'http://127.0.0.1:3000', changeOrigin: true, rewrite: (p: string) => `/heimdall${p}` },
      '/users':     { target: 'http://127.0.0.1:3000', changeOrigin: true, rewrite: (p: string) => `/heimdall${p}` },
      '/services':  { target: 'http://127.0.0.1:3000', changeOrigin: true, rewrite: (p: string) => `/heimdall${p}` },
      '/databases': { target: 'http://127.0.0.1:3000', changeOrigin: true, rewrite: (p: string) => `/heimdall${p}` },
      '/team-docs': { target: 'http://127.0.0.1:3000', changeOrigin: true, rewrite: (p: string) => `/heimdall${p}` },
      '/docs':      { target: 'http://127.0.0.1:3000', changeOrigin: true, rewrite: (p: string) => `/heimdall${p}` },
      // WebSocket event stream — browser connects to /heimdall/ws/events (resolveWsUrl in api.ts)
      '/heimdall/ws': { target: 'ws://127.0.0.1:3000', changeOrigin: true, ws: true },
      '/dali/api':      { target: 'http://127.0.0.1:9090',  changeOrigin: true, rewrite: (p: string) => p.replace(/^\/dali/, '') },
      '/dali/q':        { target: 'http://127.0.0.1:9090',  changeOrigin: true, rewrite: (p: string) => p.replace(/^\/dali/, '') },
      '/jobrunr':       { target: 'http://127.0.0.1:29091', changeOrigin: true, rewrite: (p: string) => p.replace(/^\/jobrunr/, '') },
      '/highload-plan': { target: 'http://127.0.0.1:9093', changeOrigin: true },
    },
  },
});
