/**
 * Round 5 — T&C / Privacy consent interruptor.
 *
 * Check on mount: compare current policy version (from env config or
 * /me/consents/latest) against user's last accepted version. If user
 * hasn't accepted current — block navigation with a modal. POST
 * /me/consents {scope, version} on accept, then close.
 *
 * Version policy ships via Vite env `VITE_TC_VERSION` + `VITE_PRIVACY_VERSION`.
 * When backend MTN-63 adds /api/admin/tc-version endpoint (future), switch
 * to that.
 */
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';

const CHUR_BASE = '/chur';

const CURRENT_TC      = import.meta.env.VITE_TC_VERSION      ?? '2026-04-22';
const CURRENT_PRIVACY = import.meta.env.VITE_PRIVACY_VERSION ?? '2026-04-22';

interface Consent {
  scope:      'tos' | 'privacy' | string;
  version:    string;
  acceptedAt: number;
}

export function ConsentModal() {
  const { t } = useTranslation();
  const [pending, setPending] = useState<Array<'tos' | 'privacy'>>([]);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const res = await fetch(`${CHUR_BASE}/me/consents`, { credentials: 'include' });
        if (!res.ok) return;  // unauthenticated or backend offline — silent
        const body = await res.json() as { consents: Consent[] };
        if (cancelled) return;
        const latestByScope: Record<string, string> = {};
        for (const c of body.consents ?? []) {
          if (!latestByScope[c.scope] || c.version > latestByScope[c.scope]) {
            latestByScope[c.scope] = c.version;
          }
        }
        const needed: Array<'tos' | 'privacy'> = [];
        if (latestByScope.tos !== CURRENT_TC)         needed.push('tos');
        if (latestByScope.privacy !== CURRENT_PRIVACY) needed.push('privacy');
        setPending(needed);
      } catch {
        /* backend offline — show no modal, don't block user */
      }
    })();
    return () => { cancelled = true; };
  }, []);

  const onAccept = async () => {
    setSaving(true);
    try {
      for (const scope of pending) {
        const version = scope === 'tos' ? CURRENT_TC : CURRENT_PRIVACY;
        await fetch(`${CHUR_BASE}/me/consents`, {
          method:      'POST',
          credentials: 'include',
          headers:     { 'Content-Type': 'application/json', Origin: window.location.origin },
          body:        JSON.stringify({ scope, version }),
        });
      }
      setPending([]);
    } finally {
      setSaving(false);
    }
  };

  if (pending.length === 0) return null;

  return (
    <div style={{
      position: 'fixed', inset: 0, zIndex: 9999,
      background: 'rgba(0,0,0,0.72)',
      display: 'flex', alignItems: 'center', justifyContent: 'center',
    }}>
      <div style={{
        background: 'var(--bg0)', color: 'var(--t1)',
        padding: 24, maxWidth: 520, borderRadius: 'var(--seer-radius-md)',
        boxShadow: '0 20px 60px rgba(0,0,0,0.5)',
      }}>
        <h3 style={{ margin: 0, marginBottom: 12 }}>
          {t('consent.title', 'Требуется согласие')}
        </h3>
        <p style={{ color: 'var(--t3)', margin: 0, marginBottom: 16 }}>
          {t('consent.desc', 'Опубликована новая версия документов платформы. Подтвердите согласие чтобы продолжить работу.')}
        </p>
        <ul style={{ margin: 0, marginBottom: 20, paddingLeft: 20 }}>
          {pending.includes('tos') && (
            <li>{t('consent.tos',     'Условия использования')} · <code>v{CURRENT_TC}</code></li>
          )}
          {pending.includes('privacy') && (
            <li>{t('consent.privacy', 'Политика конфиденциальности')} · <code>v{CURRENT_PRIVACY}</code></li>
          )}
        </ul>
        <div style={{ display: 'flex', gap: 12, justifyContent: 'flex-end' }}>
          <button className="btn-primary" onClick={onAccept} disabled={saving}>
            {saving
              ? t('status.saving', 'Saving…')
              : t('consent.accept', 'Принимаю')}
          </button>
        </div>
      </div>
    </div>
  );
}
