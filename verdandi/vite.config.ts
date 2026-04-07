/// <reference types="vitest" />
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';
import path from 'path';

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
    format: 'es',  // emit ES module workers (Vite 8 default; explicit for clarity)
  },
  optimizeDeps: {
    // Pre-bundle the browser-compatible ELK (CJS → ESM transform)
    include: ['elkjs/lib/elk.bundled.js'],
  },
  test: {
    globals:     true,
    environment: 'node',
    include:     ['src/**/*.{test,spec}.{ts,tsx}'],
    coverage:    { provider: 'v8', reporter: ['text', 'html'] },
  },
  server: {
    // Dev proxy: forward /graphql → SHUTTLE on 8080 (bypasses rbac-proxy which isn't running locally)
    proxy: {
      '/graphql': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/auth': {
        target: 'http://localhost:3000',
        changeOrigin: true,
      },
    },
  },
});
