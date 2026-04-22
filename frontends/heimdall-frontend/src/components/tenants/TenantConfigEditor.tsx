import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import type { DaliTenantConfig, TenantConfigPatch } from '../../api/admin';

interface Props {
  tenant:  DaliTenantConfig;
  onSave:  (patch: TenantConfigPatch) => Promise<void>;
  saving?: boolean;
}

type NumField = 'maxParseSessions' | 'maxAtoms' | 'maxSources' | 'maxConcurrentJobs' | 'dataRetentionDays';
type StrField = 'harvestCron' | 'llmMode';

const NUM_FIELDS: NumField[] = ['maxParseSessions', 'maxAtoms', 'maxSources', 'maxConcurrentJobs', 'dataRetentionDays'];
const STR_FIELDS: StrField[] = ['harvestCron', 'llmMode'];

export function TenantConfigEditor({ tenant, onSave, saving }: Props) {
  const { t } = useTranslation();
  const [patch, setPatch] = useState<TenantConfigPatch>({});
  const [error, setError] = useState<string | null>(null);

  function setNum(field: NumField, raw: string) {
    if (raw === '') { const { [field]: _, ...rest } = patch; setPatch(rest); return; }
    const n = Number(raw);
    if (!Number.isFinite(n)) return;
    setPatch({ ...patch, [field]: n });
  }

  function setStr(field: StrField, raw: string) {
    if (raw === '') { const { [field]: _, ...rest } = patch; setPatch(rest); return; }
    setPatch({ ...patch, [field]: raw });
  }

  const current = (f: NumField | StrField) =>
    (patch[f] as string | number | undefined) ??
    (tenant[f] as string | number | undefined) ?? '';

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    if (Object.keys(patch).length === 0) {
      setError(t('config.noChanges', 'No changes to save.'));
      return;
    }
    setError(null);
    try {
      await onSave(patch);
      setPatch({});
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    }
  }

  return (
    <form onSubmit={submit} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
      <section>
        <h3 style={{ fontSize: '0.95rem', marginBottom: 8 }}>
          {t('config.readOnly', 'Read-only')}
        </h3>
        <table style={{ width: '100%', fontSize: '0.85rem' }}>
          <tbody>
            <ReadOnlyRow label="yggLineageDbName"       value={tenant.yggLineageDbName} />
            <ReadOnlyRow label="yggSourceArchiveDbName" value={tenant.yggSourceArchiveDbName} />
            <ReadOnlyRow label="friggDaliDbName"        value={tenant.friggDaliDbName} />
            <ReadOnlyRow label="keycloakOrgId"          value={tenant.keycloakOrgId} />
            <ReadOnlyRow label="configVersion"          value={tenant.configVersion} />
          </tbody>
        </table>
      </section>

      <section>
        <h3 style={{ fontSize: '0.95rem', marginBottom: 8 }}>
          {t('config.quotas', 'Quotas & limits')}
        </h3>
        <div style={{ display: 'grid', gridTemplateColumns: '200px 1fr', gap: 8, alignItems: 'center' }}>
          {NUM_FIELDS.map(f => (
            <label key={f} style={{ display: 'contents' }}>
              <span style={{ fontFamily: 'monospace', fontSize: '0.85rem' }}>{f}</span>
              <input
                type="number"
                min={0}
                value={current(f)}
                onChange={e => setNum(f, e.target.value)}
                style={{ fontFamily: 'monospace' }}
              />
            </label>
          ))}
        </div>
      </section>

      <section>
        <h3 style={{ fontSize: '0.95rem', marginBottom: 8 }}>
          {t('config.scheduling', 'Scheduling & mode')}
        </h3>
        <div style={{ display: 'grid', gridTemplateColumns: '200px 1fr', gap: 8, alignItems: 'center' }}>
          {STR_FIELDS.map(f => (
            <label key={f} style={{ display: 'contents' }}>
              <span style={{ fontFamily: 'monospace', fontSize: '0.85rem' }}>{f}</span>
              <input
                type="text"
                value={current(f) as string}
                onChange={e => setStr(f, e.target.value)}
                style={{ fontFamily: 'monospace' }}
              />
            </label>
          ))}
        </div>
      </section>

      {error && <div style={{ color: 'var(--color-danger)', fontSize: 12 }}>{error}</div>}

      <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8 }}>
        <button
          type="button"
          className="btn-secondary"
          disabled={saving || Object.keys(patch).length === 0}
          onClick={() => { setPatch({}); setError(null); }}
        >
          {t('config.reset', 'Reset')}
        </button>
        <button
          type="submit"
          className="btn-primary"
          disabled={saving || Object.keys(patch).length === 0}
        >
          {saving ? t('config.saving', 'Saving…') : t('config.save', 'Save')}
        </button>
      </div>
    </form>
  );
}

function ReadOnlyRow({ label, value }: { label: string; value?: string | number | null }) {
  if (value == null || value === '') return null;
  return (
    <tr>
      <td style={{ padding: '4px 8px', color: 'var(--color-muted)', whiteSpace: 'nowrap' }}>{label}</td>
      <td style={{ padding: '4px 8px', fontFamily: 'monospace', wordBreak: 'break-all' }}>{String(value)}</td>
    </tr>
  );
}
