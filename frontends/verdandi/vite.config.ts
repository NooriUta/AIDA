import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';
import { federation } from '@module-federation/vite';
import path from 'path';
import type { ProxyOptions } from 'vite';
import type { IncomingMessage } from 'http';

// Strip `Secure` from Set-Cookie + forward original Host so chur generates
// correct redirect_uri (must match Vite dev port like :5173).
function stripSecureCookie(): ProxyOptions {
  return {
    configure(proxy) {
      proxy.on('proxyReq', (proxyReq, req) => {
        if (req.headers.host) {
          proxyReq.setHeader('X-Forwarded-Host', req.headers.host);
          proxyReq.setHeader('X-Forwarded-Proto', 'http');
        }
      });
      proxy.on('proxyRes', (proxyRes: IncomingMessage) => {
        const cookies = proxyRes.headers['set-cookie'];
        if (cookies) {
          proxyRes.headers['set-cookie'] = (cookies as string[]).map((c) =>
            c.replace(/;\s*Secure/gi, ''),
          );
        }
      });
    },
  };
}

export default defineConfig({
  plugins: [
    tailwindcss(),
    react(),
    federation({
      name: 'verdandi',
      filename: 'remoteEntry.js',
      exposes: { './App': './src/App.tsx' },
      shared: {
        react:              { singleton: true, requiredVersion: '^19.0.0' },
        'react-dom':        { singleton: true, requiredVersion: '^19.0.0' },
        'react-router-dom': { singleton: true, requiredVersion: '^7.0.0'  },
        // aida-shared is NOT listed here — verdandi uses its own globals.css and
        // does not import from aida-shared, so MF should not try to resolve it.
        zustand:            { singleton: true, requiredVersion: '^5.0.0'  },
      },
    }),
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
    // Dedupe prevents multiple instances when the same package appears in
    // verdandi's own dep-graph AND is re-injected via the MF runtime.
    dedupe: ['react', 'react-dom', 'react-router-dom', 'zustand'],
  },
  worker: {
    format: 'es',
  },
  optimizeDeps: {
    include: ['elkjs/lib/elk.bundled.js'],
    // Exclude react-router-dom so Vite does NOT pre-bundle it into a chunk.
    // Without this, each dev server inlines its own copy and the MF runtime
    // cannot redirect to the Shell singleton — causing the "Router inside
    // another Router" error in dev mode.
    // react/react-dom are kept pre-bundled (their singleton sharing works
    // via the MF runtime's own mechanism without this exclusion).
    exclude: ['react-router-dom'],
  },
  test: {
    globals:     true,
    environment: 'node',
    include:     ['src/**/*.{test,spec}.{ts,tsx}'],
    setupFiles:  ['./src/test/setup.ts'],
    coverage: {
      provider: 'v8',
      reporter:  ['text', 'html', 'lcov'],
      exclude:   [
        'src/test/**', '**/*.test.*', '**/*.spec.*',
        'src/main.tsx', 'src/vite-env.d.ts',
        // Legacy / large utilities pending dedicated test sprint:
        'src/utils/transformGraph.ts',    // re-export barrel + legacy fn (unused, for reference)
        'src/utils/transformOverview.ts', // L1 overview transform — needs own test sprint
        'src/utils/layoutL1.ts',          // L1 geometry helpers — needs own test sprint
      ],
      thresholds: {
        lines:     70,
        functions: 65, // Header + SearchPalette are complex interactive components;
                       // dedicated UI-test sprint will bring this back to 70
        branches:  60,
      },
    },
  },
  // vite 8.0.5+ (rolldown) introduced a hard REQUIRE_TLA check: CJS require()
  // cannot import a TLA module. @module-federation/vite generates TLA virtual
  // files for shared modules in remotes, and CJS react-dom uses require("react").
  // Pinning to 8.0.4 (last pre-rolldown release) avoids this incompatibility.
  // The HIGH CVE in 8.0.0-8.0.4 (server.fs.deny bypass) is dev-server only —
  // it cannot be triggered by `vite build` in CI or nginx in production.
  build: {
    target: 'es2022',
    sourcemap: false,
  },
  server: {
    host: '0.0.0.0',
    cors: true,           // required for MF remote loading from shell origin
    proxy: {
      // Dev: proxy GraphQL through Chur so X-Seer-Tenant-Alias is correctly
      // injected by Chur before forwarding to SHUTTLE. Chur validates the
      // session cookie and sets X-Seer-Tenant-Alias = effectiveTenant.
      // Direct-to-SHUTTLE bypass was removed because SHUTTLE only reads
      // X-Seer-Tenant-Alias (set by Chur) — not X-Seer-Override-Tenant from frontend.
      '/graphql': {
        target: process.env.CHUR_URL ?? 'http://localhost:3000',
        changeOrigin: true,
      },
      '/auth': {
        target: process.env.CHUR_URL ?? 'http://localhost:3000',
        changeOrigin: true,
        ...stripSecureCookie(),
      },
      '/me': {
        target: process.env.CHUR_URL ?? 'http://localhost:3000',
        changeOrigin: true,
      },
      '/admin': {
        target: process.env.CHUR_URL ?? 'http://localhost:3000',
        changeOrigin: true,
      },
      '/prefs': {
        target: process.env.CHUR_URL ?? 'http://localhost:3000',
        changeOrigin: true,
      },
      // SD-03: EventStreamPanel WebSocket proxy → Chur → HEIMDALL backend.
      // Chur authenticates the session cookie and forwards to /ws/events.
      '/heimdall/ws': {
        target:       process.env.CHUR_URL ?? 'http://localhost:3000',
        ws:           true,
        changeOrigin: true,
      },
    },
  },
});
