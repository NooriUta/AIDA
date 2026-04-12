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
        react:              { singleton: true, requiredVersion: '^19.0.0' },
        'react-dom':        { singleton: true, requiredVersion: '^19.0.0' },
        'react-router-dom': { singleton: true, requiredVersion: '^7.0.0'  },
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
  },
  optimizeDeps: {
    // Exclude aida-shared from esbuild pre-bundling — it's TypeScript source resolved via alias
    exclude: ['aida-shared'],
    // Pre-bundle MF runtime packages so they aren't discovered at request time
    // (late discovery triggers a full page reload / dep-optimization loop)
    include: [
      '@module-federation/runtime',
      '@module-federation/runtime-core',
      '@module-federation/sdk',
    ],
  },
  server: {
    port: 5175,
    host: '0.0.0.0',
    cors: true,
    proxy: {
      '/auth': {
        target:       'http://localhost:3000',
        changeOrigin: true,
      },
    },
  },
});
