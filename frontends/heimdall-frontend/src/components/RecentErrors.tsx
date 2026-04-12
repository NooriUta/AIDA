import { useNavigate }        from 'react-router-dom';
import { useTranslation }     from 'react-i18next';
import type { HeimdallEvent } from 'aida-shared';
import { formatPayload }      from '../utils/eventFormat';

interface RecentErrorsProps {
  events: HeimdallEvent[];
}

function formatTime(ts: number): string {
  return new Date(ts).toISOString().substring(11, 23);
}

export function RecentErrors({ events }: RecentErrorsProps) {
  const { t }    = useTranslation();
  const navigate = useNavigate();

  const errors = events.filter(e => e.level === 'ERROR').slice(-5);

  return (
    <div style={{
      background:    'var(--bg1)',
      border:        '1px solid color-mix(in srgb, var(--danger) 25%, var(--bd))',
      borderRadius:  'var(--seer-radius-md)',
      overflow:      'hidden',
      display:       'flex',
      flexDirection: 'column',
    }}>
      {/* Header */}
      <div style={{
        display:        'flex',
        justifyContent: 'space-between',
        alignItems:     'center',
        padding:        '6px 14px',
        background:     'var(--bg2)',
        borderBottom:   '1px solid var(--bd)',
        fontSize:       '11px',
        color:          errors.length > 0 ? 'var(--danger)' : 'var(--t3)',
        textTransform:  'uppercase',
        letterSpacing:  '0.05em',
        flexShrink:     0,
      }}>
        <span>{t('dashboard.recentErrors', { count: errors.length })}</span>
        <button
          onClick={() => navigate('../events')}
          style={{ background: 'none', border: 'none', color: 'var(--inf)', cursor: 'pointer', fontSize: '11px', fontFamily: 'var(--font)' }}
        >
          {t('nav.events')} →
        </button>
      </div>

      {/* Body */}
      {errors.length === 0 ? (
        <div style={{ padding: '12px 14px', fontSize: '12px', color: 'var(--t3)' }}>
          No errors
        </div>
      ) : (
        <div style={{ overflow: 'hidden' }}>
          {errors.map((e, i) => {
            const comp = (e.sourceComponent ?? '').toLowerCase();
            return (
              <div key={i} style={{
                display:     'grid',
                gridTemplateColumns: '88px 72px 1fr',
                gap:         '8px',
                padding:     '5px 14px',
                borderBottom: i < errors.length - 1 ? '1px solid var(--bd)' : 'none',
                fontSize:    '11px',
                fontFamily:  'var(--mono)',
                alignItems:  'center',
              }}>
                <span style={{ color: 'var(--t3)' }}>{formatTime(e.timestamp)}</span>
                <span className={`comp comp-${comp}`}>{e.sourceComponent}</span>
                <span style={{ color: 'var(--danger)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                  {formatPayload(e)}
                </span>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
