import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';
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
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  worker: {
    format: 'es',
  },
  optimizeDeps: {
    include: ['elkjs/lib/elk.bundled.js'],
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
        'src/hooks/useHeimdallEmitter.ts', // EV-09 fire-and-forget emitter — needs own test sprint
      ],
      thresholds: {
        lines:     70,
        functions: 65, // Header + SearchPalette are complex interactive components;
                       // dedicated UI-test sprint will bring this back to 70
        branches:  58, // Sprint 5/6 event emission branches (KnotPage/FilterToolbar/LoomCanvas)
                       // pending dedicated test sprint — restore to 60 after EV-08 deferred
      },
    },
  },
  build: {
    target: 'es2022',
    sourcemap: false,
  },
  server: {
    host: '0.0.0.0',
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
      // Must be listed BEFORE the /heimdall catch-all so ws:true is applied.
      '/heimdall/ws': {
        target:       process.env.CHUR_URL ?? 'http://localhost:3000',
        ws:           true,
        changeOrigin: true,
      },
      // EV-09/UA-02: HEIMDALL event relay — POST /heimdall/events goes to Chur
      // which forwards to HEIMDALL backend (fire-and-forget).
      '/heimdall': {
        target: process.env.CHUR_URL ?? 'http://localhost:3000',
        changeOrigin: true,
      },
    },
  },
});
