import { memo, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Globe } from 'lucide-react';

export const LanguageSwitcher = memo(() => {
  const { i18n, t } = useTranslation();
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);
  const current = i18n.language.startsWith('ru') ? 'ru' : 'en';

  const toggle = () => setOpen((v) => !v);
  const pick = (lang: string) => {
    void i18n.changeLanguage(lang);
    setOpen(false);
  };

  // Close on outside click
  const handleBlur = (e: React.FocusEvent) => {
    if (!ref.current?.contains(e.relatedTarget as Node)) setOpen(false);
  };

  return (
    <div ref={ref} style={{ position: 'relative' }} onBlur={handleBlur}>
      <button
        onClick={toggle}
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: '5px',
          padding: '5px 8px',
          background: 'transparent',
          border: '1px solid var(--bd)',
          borderRadius: 'var(--seer-radius-md)',
          color: 'var(--t2)',
          fontSize: '11px',
          cursor: 'pointer',
          letterSpacing: '0.06em',
          transition: 'border-color 0.12s, color 0.12s',
        }}
        aria-haspopup="listbox"
        aria-expanded={open}
      >
        <Globe size={12} />
        {current.toUpperCase()}
      </button>

      {open && (
        <div
          role="listbox"
          style={{
            position: 'absolute',
            top: 'calc(100% + 4px)',
            right: 0,
            zIndex: 300,
            minWidth: '120px',
            background: 'var(--bg1)',
            border: '1px solid var(--bd)',
            borderRadius: 'var(--seer-radius-md)',
            boxShadow: '0 4px 16px rgba(0,0,0,0.4)',
            overflow: 'hidden',
          }}
        >
          {(['en', 'ru'] as const).map((lang) => (
            <button
              key={lang}
              role="option"
              aria-selected={current === lang}
              onClick={() => pick(lang)}
              style={{
                display: 'block',
                width: '100%',
                padding: '8px 12px',
                textAlign: 'left',
                background: current === lang ? 'var(--bg3)' : 'transparent',
                border: 'none',
                color: current === lang ? 'var(--t1)' : 'var(--t2)',
                fontSize: '12px',
                cursor: 'pointer',
                transition: 'background 0.1s',
              }}
              onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.background = 'var(--bg3)'; }}
              onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.background = current === lang ? 'var(--bg3)' : 'transparent'; }}
            >
              {t(`language.${lang}`)}
            </button>
          ))}
        </div>
      )}
    </div>
  );
});

LanguageSwitcher.displayName = 'LanguageSwitcher';
