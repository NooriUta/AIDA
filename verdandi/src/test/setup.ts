import '@testing-library/jest-dom/vitest';
import { vi } from 'vitest';

// ── jsdom polyfills ─────────────────────────────────────────────────────────
// scrollIntoView is not implemented in jsdom
if (typeof HTMLElement !== 'undefined') {
  HTMLElement.prototype.scrollIntoView = function () {};
}

// ── Mock react-i18next ──────────────────────────────────────────────────────
vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => key,
    i18n: { language: 'en', changeLanguage: vi.fn() },
  }),
  Trans: ({ children }: { children: React.ReactNode }) => children,
}));

// ── Mock react-router-dom ───────────────────────────────────────────────────
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<Record<string, unknown>>('react-router-dom');
  return {
    ...actual,
    useNavigate: () => vi.fn(),
    useLocation: () => ({ pathname: '/', search: '', hash: '', state: null }),
  };
});
