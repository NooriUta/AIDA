import { useState, useEffect } from 'react';
import { HEIMDALL_API } from '../api';

export type ServiceStatus = 'UP' | 'DOWN' | 'UNKNOWN' | 'LOADING';

export interface ServiceHealth {
  name:         string;
  displayName:  string;
  status:       ServiceStatus;
  details?:     string;
  responseTime?: number;
}

const STUB_SERVICES: ServiceHealth[] = [
  { name: 'chur',     displayName: 'Chur (BFF)',        status: 'UNKNOWN' },
  { name: 'verdandi', displayName: 'Seiðr Studio',      status: 'UNKNOWN' },
  { name: 'shuttle',  displayName: 'Shuttle (GraphQL)',  status: 'UNKNOWN' },
  { name: 'hound',    displayName: 'Hound (Parser)',     status: 'UNKNOWN' },
  { name: 'dali',     displayName: 'Dali (ArcadeDB)',    status: 'UNKNOWN' },
  { name: 'mimir',    displayName: 'Mímir (Memory)',     status: 'UNKNOWN' },
  { name: 'keycloak', displayName: 'Keycloak (Auth)',    status: 'UNKNOWN' },
  { name: 'elk',      displayName: 'ELK Stack',          status: 'UNKNOWN' },
];

export function useServices(): ServiceHealth[] {
  const [services, setServices] = useState<ServiceHealth[]>([
    { name: 'heimdall', displayName: 'Heimdall (Monitor)', status: 'LOADING' },
    ...STUB_SERVICES,
  ]);

  useEffect(() => {
    let cancelled = false;

    const fetchHealth = async () => {
      const start = Date.now();
      try {
        const res  = await fetch(`${HEIMDALL_API}/q/health`);
        const data = await res.json() as { status: string };
        const rt   = Date.now() - start;
        const status: ServiceStatus = data.status === 'UP' ? 'UP' : 'DOWN';
        if (!cancelled) {
          setServices(prev => prev.map(s =>
            s.name === 'heimdall'
              ? { ...s, status, responseTime: rt, details: data.status }
              : s,
          ));
        }
      } catch {
        if (!cancelled) {
          setServices(prev => prev.map(s =>
            s.name === 'heimdall' ? { ...s, status: 'DOWN', responseTime: undefined } : s,
          ));
        }
      }
    };

    void fetchHealth();
    const id = setInterval(() => { void fetchHealth(); }, 10_000);
    return () => { cancelled = true; clearInterval(id); };
  }, []);

  return services;
}
