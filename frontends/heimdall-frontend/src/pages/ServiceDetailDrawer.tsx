import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { BY_ID } from '../config/services';
import {
  type Health, type RawService,
  mapStatus, healthColor, latencyColor, healthWord,
} from './servicesPageTypes';

// ── Action button style (private) ─────────────────────────────────────────────
const ACTION_BTN: React.CSSProperties = {
  padding: '6px 12px',
  background: 'var(--bg2)',
  border: '1px solid var(--bd)',
  borderRadius: 'var(--seer-radius-sm, 4px)',
  color: 'var(--t1)',
  fontSize: 12,
  fontWeight: 500,
  cursor: 'pointer',
  textDecoration: 'none',
};

// ── Detail row (private) ──────────────────────────────────────────────────────
function DetailRow({ label, value, valueStyle, mono }: {
  label:       string;
  value:       string;
  valueStyle?: React.CSSProperties;
  mono?:       boolean;
}) {
  return (
    <div style={{ display: 'flex', alignItems: 'baseline', gap: 10 }}>
      <span style={{ color: 'var(--t3)', fontSize: 11, width: 110, flexShrink: 0 }}>{label}</span>
      <span style={{
        color: 'var(--t1)', fontSize: 13,
        fontFamily: mono ? 'var(--mono)' : 'inherit',
        ...(valueStyle ?? {}),
      }}>{value}</span>
    </div>
  );
}

// ── Service detail drawer ─────────────────────────────────────────────────────
export function ServiceDetailDrawer({ svc, onClose }: {
  svc:     RawService;
  onClose: () => void;
}) {
  const navigate = useNavigate();
  const spec     = BY_ID[svc.name];
  const health: Health = mapStatus(svc.status);
  const url      = svc.mode === 'dev' ? spec?.devUrl : spec?.dockerUrl;
  const border   = healthColor(health);
  const { t }    = useTranslation();

  return (
    <>
      <div
        onClick={onClose}
        style={{
          position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.35)',
          zIndex: 90,
        }}
      />
      <aside style={{
        position:   'fixed', top: 0, right: 0, bottom: 0, width: 400,
        background: 'var(--bg1)', borderLeft: '1px solid var(--bd)',
        zIndex:     91, display: 'flex', flexDirection: 'column',
        boxShadow:  '-2px 0 16px rgba(0,0,0,0.35)',
      }}>
        <header style={{
          padding: '14px 16px', borderBottom: '1px solid var(--bd)',
          display: 'flex', alignItems: 'center', gap: 10,
        }}>
          <span style={{
            width: 10, height: 10, borderRadius: '50%', background: border,
            boxShadow: health === 'up' || health === 'deg'
              ? `0 0 0 3px color-mix(in srgb, ${border} 18%, transparent)` : 'none',
          }} />
          <span style={{ fontSize: 15, fontWeight: 600, color: 'var(--t1)' }}>
            {spec?.label ?? svc.name}
          </span>
          <span style={{
            fontSize: 10, fontFamily: 'var(--mono)', color: 'var(--t3)',
            background: 'var(--bg2)', padding: '2px 6px', borderRadius: 3,
          }}>{svc.mode}</span>
          <span style={{ flex: 1 }} />
          <button onClick={onClose} style={{
            background: 'transparent', border: 'none', color: 'var(--t2)',
            cursor: 'pointer', fontSize: 18, lineHeight: 1,
          }}>×</button>
        </header>

        <div style={{ padding: 16, display: 'flex', flexDirection: 'column', gap: 14, overflowY: 'auto' }}>
          <DetailRow label="Status" value={healthWord(health, t)} valueStyle={{ color: border, fontWeight: 600 }} />
          <DetailRow label="Latency"
                     value={svc.status === 'self' ? '—' : `${svc.latencyMs} ms`}
                     valueStyle={{ color: latencyColor(svc.status === 'self' ? null : svc.latencyMs), fontWeight: 600 }} />
          <DetailRow label="Port (active)" value={`:${svc.port}`} mono />
          {spec?.portDev    && <DetailRow label="Dev port"    value={`:${spec.portDev}`}    mono />}
          {spec?.portDocker && <DetailRow label="Docker port" value={`:${spec.portDocker}`} mono />}
          <DetailRow label="Category" value={spec?.category ?? 'unknown'} />
          {svc.version && <DetailRow label="Build" value={`v${svc.version}`} mono />}

          <div style={{ display: 'flex', gap: 8, marginTop: 8, flexWrap: 'wrap' }}>
            {url && (
              <a href={url} target="_blank" rel="noopener noreferrer" style={ACTION_BTN}>
                Open UI ↗
              </a>
            )}
            <button
              onClick={() => { navigate(`../events?comp=${svc.name}`); onClose(); }}
              style={ACTION_BTN}
            >
              View events
            </button>
          </div>
        </div>
      </aside>
    </>
  );
}
