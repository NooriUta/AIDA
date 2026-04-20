import '@testing-library/jest-dom/vitest';
import { vi } from 'vitest';

// ── jsdom polyfills ─────────────────────────────────────────────────────────
// scrollIntoView is not implemented in jsdom
if (typeof HTMLElement !== 'undefined') {
  HTMLElement.prototype.scrollIntoView = function () {};
}

// ── window.matchMedia (jsdom doesn't implement it) ──────────────────────────
if (typeof window !== 'undefined') {
  Object.defineProperty(window, 'matchMedia', {
    writable: true,
    value: vi.fn().mockImplementation((query: string) => ({
      matches: false,
      media: query,
      onchange: null,
      addListener: vi.fn(),
      removeListener: vi.fn(),
      addEventListener: vi.fn(),
      removeEventListener: vi.fn(),
      dispatchEvent: vi.fn(),
    })),
  });
}

// ── Mock react-i18next ──────────────────────────────────────────────────────
vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => key,
    i18n: { language: 'en', changeLanguage: vi.fn() },
  }),
  Trans: ({ children }: { children: React.ReactNode }) => children,
}));

// react-router-dom is NOT globally mocked here.
// Tests that render components using useNavigate / useLocation must wrap in
// MemoryRouter — use the renderWithRouter() helper from src/test/router-utils.tsx.
