import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { federation } from '@module-federation/vite';

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
    }),
  ],
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
