import { memo } from 'react';
import { useTranslation } from 'react-i18next';
import { Header } from '../layout/Header';
import { StatusBar } from '../layout/StatusBar';

interface Props {
  /** Module name displayed in the hero area, e.g. "URD" or "SKULD". */
  module: string;
  /** Horizon tag, e.g. "H3". */
  horizon: string;
  /** Short description i18n key. */
  descriptionKey: string;
}

export const UnderConstructionPage = memo(({ module, horizon, descriptionKey }: Props) => {
  const { t } = useTranslation();

  return (
    <div style={{
      display: 'grid',
      gridTemplateRows: '42px 1fr 28px',
      height: '100%',
      overflow: 'hidden',
      background: 'var(--seer-bg)',
    }}>
      <Header />

      <div style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        flexDirection: 'column',
        gap: '20px',
        padding: '40px',
      }}>
        {/* Construction icon */}
        <div style={{
          width: '64px', height: '64px', borderRadius: '50%',
          background: 'color-mix(in srgb, var(--wrn, #D4922A) 12%, transparent)',
          border: '1px solid color-mix(in srgb, var(--wrn, #D4922A) 30%, transparent)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          fontSize: '28px',
        }}>
          {'\u{1F6A7}'}
        </div>

        {/* Module name */}
        <div style={{
          fontSize: '22px', fontWeight: 700, letterSpacing: '0.12em',
          color: 'var(--t1)',
        }}>
          {module}
        </div>

        {/* "Under Construction" label */}
        <div style={{
          fontSize: '14px', color: 'var(--t2)',
          letterSpacing: '0.04em',
        }}>
          {t('stub.underConstruction')}
        </div>

        {/* Description */}
        <div style={{
          fontSize: '12px', color: 'var(--t3)',
          maxWidth: '420px', textAlign: 'center', lineHeight: '1.6',
        }}>
          {t(descriptionKey)}
        </div>

        {/* Horizon badge */}
        <span style={{
          display: 'inline-flex', alignItems: 'center', gap: '6px',
          padding: '4px 12px', borderRadius: 'var(--seer-radius-sm)',
          fontSize: '11px', fontWeight: 600, letterSpacing: '0.06em',
          background: 'color-mix(in srgb, var(--acc) 10%, transparent)',
          color: 'var(--acc)',
          border: '1px solid color-mix(in srgb, var(--acc) 25%, transparent)',
        }}>
          {t('nav.comingSoon', { horizon })}
        </span>
      </div>

      <StatusBar />
    </div>
  );
});

UnderConstructionPage.displayName = 'UnderConstructionPage';
