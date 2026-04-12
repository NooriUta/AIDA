import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import './i18n/config';
import './styles/globals.css';
import '@xyflow/react/dist/style.css';
import App from './App.tsx';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      retry: 1,
      refetchOnWindowFocus: false,
      networkMode: 'always',   // don't pause on navigator.onLine=false (dev/isolated envs)
    },
  },
  // Fire meta.onError if defined — used by hooks to trigger logout on 401
  mutationCache: undefined,
});

queryClient.getQueryCache().subscribe((event) => {
  if (event.type === 'updated' && event.action.type === 'error') {
    const onError = event.query.meta?.onError as ((e: unknown) => void) | undefined;
    onError?.(event.action.error);
  }
});

// Apply saved theme + palette before first render to avoid flash
const savedTheme = localStorage.getItem('seer-theme') ?? 'dark';
document.documentElement.setAttribute('data-theme', savedTheme);
const savedPalette = localStorage.getItem('seer-palette');
if (savedPalette && savedPalette !== 'amber-forest') {
  document.documentElement.setAttribute('data-palette', savedPalette);
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <App />
    </QueryClientProvider>
  </StrictMode>,
);
