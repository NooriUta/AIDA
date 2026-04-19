import { useTranslation } from 'react-i18next';

const JOBRUNR_URL = 'http://localhost:29091';

export default function DaliJobRunrPage() {
  const { t } = useTranslation();

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%', padding: 'var(--seer-space-6)' }}>
      {/* Header bar */}
      <div style={{
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        marginBottom: 'var(--seer-space-4)',
        flexShrink: 0,
      }}>
        <div>
          <h2 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--t1)', margin: 0 }}>
            {t('daliJobrunr.title')}
          </h2>
          <p style={{ fontSize: '12px', color: 'var(--t3)', margin: '3px 0 0' }}>
            {t('daliJobrunr.port', { port: '29091' })}
          </p>
        </div>
        <a
          href={JOBRUNR_URL}
          target="_blank"
          rel="noopener noreferrer"
          style={{
            display: 'inline-flex', alignItems: 'center', gap: '6px',
            padding: '8px 16px',
            fontSize: '13px', fontWeight: 600,
            background: 'color-mix(in srgb, var(--acc) 12%, transparent)',
            border: '1px solid color-mix(in srgb, var(--acc) 40%, transparent)',
            borderRadius: 'var(--seer-radius-md)',
            color: 'var(--acc)', textDecoration: 'none',
            transition: 'background 0.12s',
          }}
          onMouseEnter={e => { (e.currentTarget as HTMLElement).style.background = 'color-mix(in srgb, var(--acc) 20%, transparent)'; }}
          onMouseLeave={e => { (e.currentTarget as HTMLElement).style.background = 'color-mix(in srgb, var(--acc) 12%, transparent)'; }}
        >
          {t('daliJobrunr.openExternal')} ↗
        </a>
      </div>

      {/* Embedded iframe (best-effort — may be blocked by X-Frame-Options in prod) */}
      <div style={{
        flex: 1, borderRadius: 'var(--seer-radius-lg)',
        border: '1px solid var(--bd)', overflow: 'hidden',
        background: 'var(--bg1)',
      }}>
        <iframe
          src={JOBRUNR_URL}
          title="JobRunr Dashboard"
          style={{ width: '100%', height: '100%', border: 'none' }}
        />
      </div>
    </div>
  );
}
