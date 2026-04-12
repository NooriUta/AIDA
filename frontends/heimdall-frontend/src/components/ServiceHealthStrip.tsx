import { useEffect, useState } from 'react';
import { useNavigate }         from 'react-router-dom';
import { HEIMDALL_API }        from '../api';

interface ServiceStatus {
  name:      string;
  port:      number;
  mode:      string;
  status:    string;
  latencyMs: number;
}

// Order is fixed so cards never jump between polls.
// Show dev instances only — avoids duplicates (api returns dev + docker per service).
const STRIP_SERVICES = ['shuttle', 'chur', 'heimdall-backend', 'ygg'];

const DISPLAY: Record<string, string> = {
  shuttle:          'Shuttle',
  chur:             'Chur',
  'heimdall-backend': 'Heimdall',
  ygg:              'Ygg',
};

function dotColor(status: string): string {
  if (status === 'up' || status === 'self') return 'var(--suc)';
  if (status === 'degraded')               return 'var(--wrn)';
  return 'var(--danger)';
}

export function ServiceHealthStrip() {
  const navigate = useNavigate();
  const [services, setServices] = useState<ServiceStatus[]>([]);

  useEffect(() => {
    let cancelled = false;

    const fetch_ = () => {
      fetch(`${HEIMDALL_API}/services/health`)
        .then(r => r.ok ? r.json() as Promise<ServiceStatus[]> : Promise.reject())
        .then(all => {
          if (!cancelled) {
            const filtered = all.filter(s => STRIP_SERVICES.includes(s.name) && s.mode === 'dev');
            filtered.sort((a, b) => STRIP_SERVICES.indexOf(a.name) - STRIP_SERVICES.indexOf(b.name));
            setServices(filtered);
          }
        })
        .catch(() => {/* silently skip on error */});
    };

    fetch_();
    const id = setInterval(fetch_, 10_000);
    return () => { cancelled = true; clearInterval(id); };
  }, []);

  if (services.length === 0) return null;

  return (
    <div
      onClick={() => navigate('../services')}
      title="Click to open Services"
      style={{
        display:      'flex',
        alignItems:   'center',
        gap:          'var(--seer-space-3)',
        padding:      'var(--seer-space-2) var(--seer-space-6)',
        background:   'var(--bg1)',
        borderBottom: '1px solid var(--bd)',
        cursor:       'pointer',
        flexWrap:     'wrap',
      }}
    >
      <span style={{ fontSize: '10px', color: 'var(--t3)', textTransform: 'uppercase', letterSpacing: '0.07em', flexShrink: 0 }}>
        Services
      </span>
      {services.map(svc => (
        <div key={svc.name} style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
          <div style={{ width: 6, height: 6, borderRadius: '50%', background: dotColor(svc.status), flexShrink: 0 }} />
          <span style={{ fontSize: '11px', color: 'var(--t2)', fontFamily: 'var(--mono)' }}>
            {DISPLAY[svc.name] ?? svc.name}
          </span>
          {svc.status !== 'down' && (
            <span style={{ fontSize: '10px', color: 'var(--t3)', fontFamily: 'var(--mono)' }}>
              {svc.latencyMs}ms
            </span>
          )}
        </div>
      ))}
    </div>
  );
}
