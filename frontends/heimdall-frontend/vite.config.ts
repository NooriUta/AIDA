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
    // dedupe prevents multiple instances within the same server's dep graph.
    dedupe: ['react', 'react-dom', 'react-router-dom', 'zustand'],
  },
  // Module Federation generates top-level await — requires es2022+ target.
  build: {
    target: 'es2022',
  },
  server: {
    port: 5174,
    host: '0.0.0.0',
    cors: true,
    proxy: {
      '/auth': { target: 'http://localhost:3000', changeOrigin: true },
    },
  },
});
