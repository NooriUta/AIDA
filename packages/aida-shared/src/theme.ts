/**
 * initTheme() — применяет сохранённую тему/палитру перед первым рендером.
 * Вызывать в main.tsx ПЕРЕД ReactDOM.createRoot().
 * Ключи localStorage идентичны verdandi (единый UX для пользователя).
 */
export function initTheme(): void {
  const root    = document.documentElement;
  const theme   = localStorage.getItem('seer-theme')   ?? 'dark';
  const palette = localStorage.getItem('seer-palette') ?? 'amber-forest';
  root.setAttribute('data-theme',   theme);
  root.setAttribute('data-palette', palette);
}
