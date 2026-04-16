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
      // Auth goes to Chur as-is
      '/auth':     { target: 'http://localhost:3000', changeOrigin: true },
      '/prefs':    { target: 'http://localhost:3000', changeOrigin: true },
      // Heimdall API paths: dev server receives /health, /metrics etc. — rewrite to /heimdall/* on Chur
      '/health':   { target: 'http://localhost:3000', changeOrigin: true, rewrite: (p: string) => `/heimdall${p}` },
      '/metrics':  { target: 'http://localhost:3000', changeOrigin: true, rewrite: (p: string) => `/heimdall${p}` },
      '/control':  { target: 'http://localhost:3000', changeOrigin: true, rewrite: (p: string) => `/heimdall${p}` },
      '/users':    { target: 'http://localhost:3000', changeOrigin: true, rewrite: (p: string) => `/heimdall${p}` },
      '/services': { target: 'http://localhost:3000', changeOrigin: true, rewrite: (p: string) => `/heimdall${p}` },
      // WebSocket event stream — same rewrite
      '/ws':       { target: 'ws://localhost:3000',   changeOrigin: true, ws: true, rewrite: (p: string) => `/heimdall${p}` },
      '/dali':     { target: 'http://localhost:9090',  changeOrigin: true, rewrite: (p: string) => p.replace(/^\/dali/, '') },
    },
  },
});
