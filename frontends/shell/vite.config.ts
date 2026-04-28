import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { federation } from '@module-federation/vite';
import path from 'path';

export default defineConfig({
  plugins: [
    react(),
    federation({
      name: 'shell',
      remotes: {
        // type: 'module' forces the MF runtime to use import() instead of loadScript()
        // Required for Vite-generated ES module remoteEntry.js (which uses export {} syntax)
        verdandi: {
          type: 'module',
          name: 'verdandi',
          entry: process.env.VITE_VERDANDI_URL ?? 'http://localhost:5173/remoteEntry.js',
        },
        'heimdall-frontend': {
          type: 'module',
          name: 'heimdall-frontend',
          entry: process.env.VITE_HEIMDALL_URL ?? 'http://localhost:5174/remoteEntry.js',
        },
      },
      shared: {
        react:              { singleton: true, eager: true, requiredVersion: '^19.0.0' },
        'react-dom':        { singleton: true, eager: true, requiredVersion: '^19.0.0' },
        'react-router-dom': { singleton: true, eager: true, requiredVersion: '^7.0.0'  },
        'aida-shared':      { singleton: true },
        zustand:            { singleton: true, requiredVersion: '^5.0.0'  },
      },
      // Disable type archive downloading — remotes aren't accessible from the
      // Vite dev-server process (different network namespace in preview env)
      dts: false,
    }),
  ],
  resolve: {
    alias: {
      // Resolve aida-shared sub-paths explicitly to avoid Vite dep-optimisation loop
      // (aida-shared uses .ts as main, which confuses esbuild pre-bundler)
      'aida-shared/theme':         path.resolve(__dirname, '../../packages/aida-shared/src/theme.ts'),
      'aida-shared/styles/tokens': path.resolve(__dirname, '../../packages/aida-shared/styles/tokens.css'),
      'aida-shared':               path.resolve(__dirname, '../../packages/aida-shared/src/index.ts'),
    },
    // Dedupe ensures Shell serves one canonical copy of each singleton.
    // Remotes (verdandi, heimdall-frontend) exclude these from their own
    // optimizeDeps so the MF runtime redirects to Shell's pre-bundled copies.
    dedupe: ['react', 'react-dom', 'react-router-dom', 'zustand'],
  },
  optimizeDeps: {
    // Exclude aida-shared from esbuild pre-bundling — it's TypeScript source resolved via alias
    exclude: ['aida-shared'],
    // Pre-bundle MF runtime packages so they aren't discovered at request time
    // (late discovery triggers a full page reload / dep-optimization loop).
    // Also pre-bundle shared singletons so Shell can serve them to remotes.
    include: [
      '@module-federation/runtime',
      '@module-federation/runtime-core',
      '@module-federation/sdk',
      'react',
      'react-dom',
      'react-router-dom',
      'zustand',
    ],
  },
  build: {
    target: 'es2022',
    sourcemap: false,
  },
  server: {
    port: 5175,
    host: '0.0.0.0',
    cors: true,
    proxy: {
      // verdandi's GraphQL requests arrive at the Shell origin when verdandi runs
      // as an MF remote — the browser fetches /graphql relative to localhost:5175,
      // not to verdandi's own :5173.  Forward to SHUTTLE directly (dev only).
      '/graphql': {
        target:       process.env.SHUTTLE_URL ?? 'http://localhost:8080',
        changeOrigin: true,
      },
      '/auth': {
        target:       process.env.CHUR_PROXY_TARGET ?? 'http://localhost:3000',
        changeOrigin: true,
        configure: (proxy: import('http-proxy').Server) => {
          proxy.on('proxyReq', (proxyReq, req) => {
            // Forward original host so chur generates correct redirect_uri
            if (req.headers.host) {
              proxyReq.setHeader('X-Forwarded-Host', req.headers.host);
              proxyReq.setHeader('X-Forwarded-Proto', 'http');
            }
          });
          proxy.on('proxyRes', (proxyRes) => {
            const cookies = proxyRes.headers['set-cookie'];
            if (cookies) {
              proxyRes.headers['set-cookie'] = (cookies as string[]).map(c =>
                c.replace(/;\s*Secure/gi, ''));
            }
          });
        },
      },
    },
  },
});
