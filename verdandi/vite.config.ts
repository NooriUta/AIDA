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
  optimizeDeps: {
    // Pre-bundle the browser-compatible ELK (CJS → ESM transform)
    include: ['elkjs/lib/elk.bundled.js'],
  },
  server: {
    // Dev proxy: forward /graphql → SHUTTLE on 8080 (bypasses rbac-proxy which isn't running locally)
    proxy: {
      '/graphql': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
