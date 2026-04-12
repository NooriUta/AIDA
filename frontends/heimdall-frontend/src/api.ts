export const HEIMDALL_API = (import.meta.env.VITE_HEIMDALL_API as string | undefined) ?? 'http://localhost:9093';
export const HEIMDALL_WS  = (import.meta.env.VITE_HEIMDALL_WS  as string | undefined) ?? 'ws://localhost:9093/ws/events';
