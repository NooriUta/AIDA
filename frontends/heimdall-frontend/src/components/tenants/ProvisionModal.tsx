import { useState } from 'react';
import { useTranslation } from 'react-i18next';

export const PROVISION_STEPS = [
  'Pre-flight: KC + YGG + FRIGG reachability',
  'Keycloak: create organization',
  'FRIGG: register tenant config (PROVISIONING)',
  'YGG: create lineage database hound_{alias}',
  'YGG: create source archive hound_src_{alias}',
  'FRIGG: create Dali session store dali_{alias}',
  'Heimdall: register harvest cron schedule',
  'FRIGG: activate tenant (ACTIVE)',
] as const;

type ProvisionPhase = 'form' | 'running' | 'success' | 'error';

interface Props {
  onDone:  (alias: string) => void;
  onClose: (alias?: string) => void;
}

export function ProvisionModal({ onDone, onClose }: Props) {
  const { t } = useTranslation();
  const [alias, setAlias]             = useState('');
  const [phase, setPhase]             = useState<ProvisionPhase>('form');
  const [visibleStep, setVisibleStep] = useState(-1);
  const [failedStep, setFailedStep]   = useState<number | null>(null);
  const [cause, setCause]             = useState<string | null>(null);
  const [result, setResult]           = useState<{ keycloakOrgId?: string } | null>(null);
  const [validationErr, setValidationErr] = useState<string | null>(null);

  const submit = async () => {
    const a = alias.trim().toLowerCase().replace(/[^a-z0-9-]/g, '');
    if (a.length < 4) {
      setValidationErr('Alias должен быть не менее 4 символов');
      return;
    }
    setValidationErr(null);
    setPhase('running');
    setVisibleStep(-1);
    setFailedStep(null);
    setCause(null);

    let stepIdx = 0;
    const ticker = setInterval(() => {
      if (stepIdx < PROVISION_STEPS.length - 1) {
        setVisibleStep(stepIdx++);
      }
    }, 700);

    try {
      const res = await fetch('/chur/api/admin/tenants', {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json', Origin: window.location.origin },
        body: JSON.stringify({ alias: a }),
      });
      clearInterval(ticker);
      const body = await res.json() as {
        keycloakOrgId?: string; lastStep?: number;
        failedStep?: number; cause?: string; message?: string; error?: string;
      };
      if (!res.ok) {
        setFailedStep(body.failedStep ?? null);
        setCause(body.cause ?? body.message ?? body.error ?? `HTTP ${res.status}`);
        setVisibleStep((body.failedStep ?? 1) - 2);
        setPhase('error');
        return;
      }
      setVisibleStep(PROVISION_STEPS.length - 1);
      setResult({ keycloakOrgId: body.keycloakOrgId });
      setPhase('success');
      onDone(a);
    } catch (e) {
      clearInterval(ticker);
      setCause(e instanceof Error ? e.message : String(e));
      setPhase('error');
    }
  };

  const stepColor = (i: number) => {
    if (phase === 'error' && i === failedStep) return 'var(--danger)';
    if (i <= visibleStep) return 'var(--suc)';
    if (phase === 'running' && i === visibleStep + 1) return 'var(--inf)';
    return 'var(--t3)';
  };
  const stepIcon = (i: number) => {
    if (phase === 'error' && i === failedStep) return '✗';
    if (i <= visibleStep) return '✓';
    if (phase === 'running' && i === visibleStep + 1) return '…';
    return '○';
  };

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-label={t('tenants.provision.title', 'Создать тенант')}
      style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.6)',
               display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 300 }}
    >
      <div style={{ background: 'var(--bg1)', border: '1px solid var(--border)', borderRadius: 8,
                    padding: 24, width: 440, boxShadow: '0 8px 32px rgba(0,0,0,0.4)',
                    maxHeight: '90vh', overflowY: 'auto' }}>

        <div style={{ fontWeight: 600, fontSize: 14, marginBottom: 16 }}>
          {t('tenants.provision.title', 'Создать тенант')}
        </div>

        {phase === 'form' && (
          <>
            <label style={{ fontSize: 12, color: 'var(--t3)', display: 'block', marginBottom: 4 }}>
              {t('tenants.provision.aliasLabel', 'Alias (a-z, 0-9, дефис)')}
            </label>
            <input
              data-testid="alias-input"
              className="field-input"
              style={{ width: '100%' }}
              value={alias}
              onChange={e => { setAlias(e.target.value); setValidationErr(null); }}
              placeholder="my-tenant"
              autoFocus
              onKeyDown={e => e.key === 'Enter' && void submit()}
            />
            {validationErr && (
              <p data-testid="validation-error" style={{ color: 'var(--danger)', fontSize: 11, marginTop: 6 }}>
                {validationErr}
              </p>
            )}
            <p style={{ fontSize: 11, color: 'var(--t3)', marginTop: 6 }}>
              4–32 символа · только строчные буквы, цифры, дефис · нельзя зарезервированные имена
            </p>
            <div style={{ display: 'flex', gap: 8, marginTop: 16, justifyContent: 'flex-end' }}>
              <button className="btn btn-secondary" onClick={() => { onClose(); }}>
                {t('action.cancel', 'Отмена')}
              </button>
              <button data-testid="submit-btn" className="btn btn-secondary"
                onClick={submit} disabled={!alias.trim()}>
                {t('tenants.provision.confirm', 'Создать')}
              </button>
            </div>
          </>
        )}

        {phase !== 'form' && (
          <>
            <div style={{ marginBottom: 12 }}>
              <span style={{ fontSize: 12, color: 'var(--t3)' }}>Тенант: </span>
              <span data-testid="tenant-alias" style={{ fontFamily: 'var(--mono)', fontSize: 12, fontWeight: 600 }}>
                {alias}
              </span>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
              {PROVISION_STEPS.map((label, i) => (
                <div key={i} data-testid={`step-${i}`}
                  style={{ display: 'flex', gap: 8, alignItems: 'flex-start', fontSize: 12 }}>
                  <span data-testid={`step-icon-${i}`} style={{ fontFamily: 'var(--mono)', fontSize: 13,
                    width: 16, flexShrink: 0, color: stepColor(i) }}>
                    {stepIcon(i)}
                  </span>
                  <span style={{ color: stepColor(i),
                    fontWeight: (phase === 'error' && i === failedStep) ? 600 : undefined }}>
                    {label.replace('{alias}', alias)}
                    {phase === 'error' && i === failedStep && cause && (
                      <div data-testid="error-cause" style={{ fontSize: 11, color: 'var(--danger)', marginTop: 2, fontWeight: 400 }}>
                        {cause}
                      </div>
                    )}
                  </span>
                </div>
              ))}
            </div>

            {phase === 'success' && (
              <div data-testid="success-banner" style={{ marginTop: 12, padding: '8px 10px',
                background: 'color-mix(in srgb, var(--suc) 10%, transparent)',
                border: '1px solid color-mix(in srgb, var(--suc) 25%, transparent)',
                borderRadius: 4, fontSize: 11 }}>
                <div style={{ fontWeight: 600, color: 'var(--suc)' }}>Тенант создан</div>
                {result?.keycloakOrgId && (
                  <div data-testid="kc-org-id" style={{ color: 'var(--t3)', marginTop: 2 }}>
                    KC org: <span style={{ fontFamily: 'var(--mono)' }}>{result.keycloakOrgId}</span>
                  </div>
                )}
              </div>
            )}

            <div style={{ display: 'flex', gap: 8, marginTop: 16, justifyContent: 'flex-end' }}>
              {phase === 'error' && (
                <button data-testid="back-btn" className="btn btn-secondary" style={{ fontSize: 12 }}
                  onClick={() => { setPhase('form'); setVisibleStep(-1); }}>
                  ← Назад
                </button>
              )}
              <button data-testid="close-btn"
                className={phase === 'success' ? 'btn btn-primary' : 'btn btn-secondary'}
                onClick={() => { onClose(phase === 'success' ? alias : undefined); }}>
                {phase === 'success' ? t('tenants.provision.viewTenant', 'Открыть тенант →') : t('action.cancel', 'Отмена')}
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
