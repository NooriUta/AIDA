/// <reference types="vitest" />
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';
import { federation } from '@module-federation/vite';
import path from 'path';

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
      exclude:   ['src/test/**', '**/*.test.*', '**/*.spec.*', 'src/main.tsx', 'src/vite-env.d.ts'],
      thresholds: {
        lines:     70,
        functions: 70,
        branches:  60,
      },
    },
  },
  // Module Federation generates top-level await — requires es2022+ target.
  build: {
    target: 'es2022',
  },
  server: {
    host: '0.0.0.0',
    cors: true,           // required for MF remote loading from shell origin
    proxy: {
      // Dev: proxy GraphQL directly to SHUTTLE (bypasses Chur auth — dev only).
      // In production the reverse-proxy / Chur handles /graphql.
      '/graphql': {
        target: process.env.SHUTTLE_URL ?? 'http://localhost:8080',
        changeOrigin: true,
      },
      '/auth': {
        target: 'http://localhost:3000',
        changeOrigin: true,
      },
    },
  },
});
