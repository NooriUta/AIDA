// HEIMDALL_API: base URL for REST calls.
// In Docker (via nginx) VITE_HEIMDALL_API is injected as "/heimdall" at build time.
// In local dev (vite dev server) falls back to empty string — the vite proxy handles /metrics, /control, etc.
export const HEIMDALL_API = (import.meta.env.VITE_HEIMDALL_API as string | undefined) ?? '';

// HEIMDALL_WS: WebSocket URL for event stream.
// Computed at runtime so it works with any host/protocol (dev, Docker, prod).
// Path /heimdall/ws/events is proxied by nginx → chur → heimdall-backend.
// In local vite dev the vite proxy handles /heimdall/ws/ at ws://localhost:3000.
function resolveWsUrl(): string {
  if (import.meta.env.VITE_HEIMDALL_WS) {
    return import.meta.env.VITE_HEIMDALL_WS as string;
  }
  if (typeof window !== 'undefined') {
    const proto = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    return `${proto}//${window.location.host}/heimdall/ws/events`;
  }
  return 'ws://localhost:9093/ws/events';
}

export const HEIMDALL_WS = resolveWsUrl();
