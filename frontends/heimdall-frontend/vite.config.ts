import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import type { ServerOptions } from 'http-proxy';

// Strip `Secure` from Set-Cookie + forward original Host so chur generates
// correct redirect_uri for Auth Code flow (must match :5174 vite dev port).
function stripSecureCookie() {
  return {
    configure(proxy: import('http-proxy').Server) {
      proxy.on('proxyReq', (proxyReq: import('http').ClientRequest, req: import('http').IncomingMessage) => {
        // Forward original host so chur knows the public-facing port (:5174)
        if (req.headers.host) {
          proxyReq.setHeader('X-Forwarded-Host', req.headers.host);
          proxyReq.setHeader('X-Forwarded-Proto', 'http');
        }
      });
      proxy.on('proxyRes', (proxyRes: import('http').IncomingMessage) => {
        const cookies = proxyRes.headers['set-cookie'];
        if (cookies) {
          proxyRes.headers['set-cookie'] = cookies.map((c: string) =>
            c.replace(/;\s*Secure/gi, ''),
          );
        }
      });
    },
  } satisfies ServerOptions;
}

export default defineConfig({
  plugins: [
    react(),
  ],
  resolve: {
    dedupe: ['react', 'react-dom', 'react-router-dom', 'zustand'],
  },
  build: {
    target: 'es2022',
    sourcemap: false,
  },
  server: {
    port: 5174,
    host: '0.0.0.0',
    cors: true,
    proxy: {
      ...(() => {
        const churTarget     = process.env.CHUR_PROXY_TARGET     ?? 'http://127.0.0.1:3000';
        const heimdallTarget = process.env.HEIMDALL_PROXY_TARGET ?? 'http://127.0.0.1:9093';
        const jobrunrTarget  = process.env.JOBRUNR_PROXY_TARGET  ?? 'http://127.0.0.1:29091';
        return {
          // Auth + admin API + user self-service go to Chur as-is
          '/auth':      { target: churTarget, changeOrigin: true, ...stripSecureCookie() },
          '/prefs':     { target: churTarget, changeOrigin: true, ...stripSecureCookie() },
          '/api/admin': { target: churTarget, changeOrigin: true },
          '/chur':      { target: churTarget, changeOrigin: true,
                          rewrite: (p: string) => p.replace(/^\/chur/, ''),
                          ...stripSecureCookie() },
          // Heimdall API paths rewrite to /heimdall/* on Chur
          '/health':    { target: churTarget, changeOrigin: true, rewrite: (p: string) => `/heimdall${p}` },
          '/metrics':   { target: churTarget, changeOrigin: true, rewrite: (p: string) => `/heimdall${p}` },
          '/control':   { target: churTarget, changeOrigin: true, rewrite: (p: string) => `/heimdall${p}` },
          '/users':     { target: churTarget, changeOrigin: true, rewrite: (p: string) => `/heimdall${p}` },
          '/services':  { target: churTarget, changeOrigin: true, rewrite: (p: string) => `/heimdall${p}` },
          '/databases': { target: churTarget, changeOrigin: true, rewrite: (p: string) => `/heimdall${p}` },
          '/team-docs': { target: churTarget, changeOrigin: true, rewrite: (p: string) => `/heimdall${p}` },
          '/docs':      { target: churTarget, changeOrigin: true, rewrite: (p: string) => `/heimdall${p}` },
          // WebSocket event stream
          '/heimdall/ws': { target: churTarget.replace('http', 'ws'), changeOrigin: true, ws: true },
          '/dali/api':    { target: process.env.DALI_PROXY_TARGET ?? 'http://127.0.0.1:9090', changeOrigin: true, rewrite: (p: string) => p.replace(/^\/dali/, '') },
          '/dali/q':      { target: process.env.DALI_PROXY_TARGET ?? 'http://127.0.0.1:9090', changeOrigin: true, rewrite: (p: string) => p.replace(/^\/dali/, '') },
          '/jobrunr':     { target: jobrunrTarget, changeOrigin: true, rewrite: (p: string) => p.replace(/^\/jobrunr/, '') },
          '/highload-plan': { target: heimdallTarget, changeOrigin: true },
        };
      })(),
    },
  },
});
