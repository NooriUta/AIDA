import { memo, useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useAuthStore } from '../../../stores/authStore';

interface FriggProfile {
  title?:       string;
  department?:  string;
  phone?:       string;
  bio?:         string;
  avatarColor?: string;
  linkedinUrl?: string;
  githubUrl?:   string;
  avatarUrl?:   string;
}

export const ProfileTabProfile = memo(() => {
  const { t } = useTranslation();
  const { user } = useAuthStore();

  // ── Frigg profile state ──────────────────────────────────────────────────────
  const [frigg,          setFrigg]          = useState<FriggProfile>({});
  const [configVersion,  setConfigVersion]  = useState(0);
  const [loadingFrigg,   setLoadingFrigg]   = useState(true);
  const [saving,         setSaving]         = useState(false);
  const [saved,          setSaved]          = useState(false);
  const [friggError,     setFriggError]     = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    setLoadingFrigg(true);
    fetch('/me/profile', { credentials: 'include' })
      .then(r => {
        if (r.status === 404) return null;
        if (!r.ok) throw new Error(`${r.status}`);
        return r.json();
      })
      .then((data: { data?: FriggProfile; configVersion?: number } | null) => {
        if (cancelled) return;
        if (data) {
          setFrigg(data.data ?? {});
          setConfigVersion(data.configVersion ?? 0);
        }
      })
      .catch(() => { if (!cancelled) setFriggError(t('profile.loadError')); })
      .finally(() => { if (!cancelled) setLoadingFrigg(false); });
    return () => { cancelled = true; };
  }, [t]);

  async function handleSave() {
    setSaving(true);
    setFriggError(null);
    try {
      const res = await fetch('/me/profile', {
        method:      'PUT',
        credentials: 'include',
        headers:     { 'Content-Type': 'application/json' },
        body:        JSON.stringify({ data: frigg, expectedConfigVersion: configVersion }),
      });
      if (res.status === 409) { setFriggError(t('profile.conflictError')); return; }
      if (!res.ok) throw new Error(`${res.status}`);
      const body: { configVersion?: number } = await res.json();
      setConfigVersion(body.configVersion ?? configVersion + 1);
      setSaved(true);
      setTimeout(() => setSaved(false), 2000);
    } catch {
      setFriggError(t('profile.saveError'));
    } finally {
      setSaving(false);
    }
  }

  const initials = user ? user.username.slice(0, 2).toUpperCase() : '??';
  const displayName = [user?.firstName, user?.lastName].filter(Boolean).join(' ') || user?.username || '—';

  return (
    <div>
      <div style={{ fontSize: '14px', fontWeight: 600, color: 'var(--t1)', marginBottom: '18px', paddingBottom: '10px', borderBottom: '1px solid var(--bd)' }}>
        {t('profile.tabs.profile')}
      </div>

      {/* Hero */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '20px', marginBottom: '28px', padding: '16px 20px', background: 'var(--bg2)', border: '1px solid var(--bd)', borderRadius: '10px' }}>
        <div style={{ width: '52px', height: '52px', borderRadius: '50%', flexShrink: 0, background: frigg.avatarColor ? `color-mix(in srgb, ${frigg.avatarColor} 18%, var(--bg3))` : 'color-mix(in srgb, var(--acc) 18%, var(--bg3))', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '18px', fontWeight: 600, color: frigg.avatarColor ?? 'var(--acc)', border: `2px solid color-mix(in srgb, ${frigg.avatarColor ?? 'var(--acc)'} 30%, transparent)` }}>
          {initials}
        </div>
        <div style={{ flex: 1 }}>
          <div style={{ fontSize: '15px', fontWeight: 600, color: 'var(--t1)', marginBottom: '4px' }}>{displayName}</div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap' }}>
            <span style={{ display: 'inline-flex', alignItems: 'center', padding: '2px 8px', borderRadius: '3px', fontSize: '10px', fontWeight: 600, letterSpacing: '0.07em', textTransform: 'uppercase', background: 'color-mix(in srgb, var(--acc) 14%, transparent)', color: 'var(--acc)', border: '1px solid color-mix(in srgb, var(--acc) 30%, transparent)' }}>
              {user?.role ?? 'viewer'}
            </span>
            {user?.emailVerified === false && (
              <span style={{ fontSize: '10px', padding: '2px 7px', borderRadius: '3px', background: 'color-mix(in srgb, var(--warn, #f59e0b) 14%, transparent)', color: 'var(--warn, #f59e0b)', border: '1px solid color-mix(in srgb, var(--warn, #f59e0b) 30%, transparent)', fontWeight: 600, letterSpacing: '0.05em', textTransform: 'uppercase' }}>
                {t('profile.emailUnverified')}
              </span>
            )}
            <span style={{ fontSize: '11px', color: 'var(--t3)' }}>{user?.activeTenantAlias} · Seiðr Studio</span>
          </div>
        </div>
      </div>

      {/* KC identity — read-only */}
      <div style={{ marginBottom: '20px', padding: '12px 16px', background: 'var(--bg2)', border: '1px solid var(--bd)', borderRadius: 'var(--seer-radius-md)' }}>
        <div style={{ fontSize: '10px', fontWeight: 600, color: 'var(--t3)', letterSpacing: '0.05em', textTransform: 'uppercase', marginBottom: '10px' }}>{t('profile.identity')}</div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
          <Field label={t('profile.username')} value={user?.username ?? '—'} readOnly />
          <Field label={t('profile.email')} value={user?.email ?? '—'} readOnly />
          <Field label={t('profile.firstName')} value={user?.firstName ?? '—'} readOnly />
          <Field label={t('profile.lastName')} value={user?.lastName ?? '—'} readOnly />
        </div>
      </div>

      {/* Frigg profile — editable */}
      {loadingFrigg ? (
        <div style={{ color: 'var(--t3)', fontSize: '12px', marginBottom: '20px' }}>{t('profile.loading')}</div>
      ) : (
        <>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '16px' }}>
            <EditableField label={t('profile.title')} value={frigg.title ?? ''} onChange={v => setFrigg(f => ({ ...f, title: v }))} />
            <EditableField label={t('profile.department')} value={frigg.department ?? ''} onChange={v => setFrigg(f => ({ ...f, department: v }))} />
            <EditableField label={t('profile.phone')} value={frigg.phone ?? ''} onChange={v => setFrigg(f => ({ ...f, phone: v }))} />
            <EditableField label={t('profile.avatarColor')} value={frigg.avatarColor ?? ''} onChange={v => setFrigg(f => ({ ...f, avatarColor: v }))} type="color" />
          </div>

          <div style={{ marginBottom: '16px' }}>
            <label style={labelStyle}>{t('profile.bio')}</label>
            <textarea value={frigg.bio ?? ''} onChange={e => setFrigg(f => ({ ...f, bio: e.target.value }))} rows={3}
              style={{ ...inputStyle, resize: 'vertical', height: 'auto' }} placeholder={t('profile.bioPlaceholder')} />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '20px' }}>
            <EditableField label="LinkedIn URL" value={frigg.linkedinUrl ?? ''} onChange={v => setFrigg(f => ({ ...f, linkedinUrl: v }))} type="url" />
            <EditableField label="GitHub URL" value={frigg.githubUrl ?? ''} onChange={v => setFrigg(f => ({ ...f, githubUrl: v }))} type="url" />
          </div>
        </>
      )}

      {friggError && (
        <div style={{ fontSize: '12px', color: 'var(--danger)', marginBottom: '12px' }}>{friggError}</div>
      )}

      <div style={{ display: 'flex', gap: '10px', marginTop: '4px' }}>
        <button onClick={handleSave} disabled={saving || loadingFrigg} style={btnPrimary}>
          {saved ? `✓ ${t('profile.saved')}` : saving ? t('profile.saving') : t('profile.save')}
        </button>
        <button onClick={() => setFrigg({})} style={btnSecondary}>{t('profile.cancel')}</button>
      </div>
    </div>
  );
});

ProfileTabProfile.displayName = 'ProfileTabProfile';

// ── Sub-components ────────────────────────────────────────────────────────────

function Field({ label, value, readOnly }: { label: string; value: string; readOnly?: boolean }) {
  return (
    <div>
      <label style={labelStyle}>{label}</label>
      <div style={{ ...inputStyle, color: readOnly ? 'var(--t2)' : 'var(--t1)', cursor: readOnly ? 'default' : undefined }}>{value}</div>
    </div>
  );
}

function EditableField({ label, value, onChange, type = 'text' }: { label: string; value: string; onChange: (v: string) => void; type?: string }) {
  return (
    <div>
      <label style={labelStyle}>{label}</label>
      <input type={type} value={value} onChange={e => onChange(e.target.value)} style={inputStyle} />
    </div>
  );
}

// ── Styles ────────────────────────────────────────────────────────────────────

const inputStyle: React.CSSProperties = {
  width: '100%', padding: '7px 10px',
  background: 'var(--bg2)', border: '1px solid var(--bd)',
  borderRadius: 'var(--seer-radius-md)', color: 'var(--t1)', fontSize: '13px',
  fontFamily: 'inherit', outline: 'none', boxSizing: 'border-box',
};
const labelStyle: React.CSSProperties = {
  display: 'block', fontSize: '11px', fontWeight: 600, color: 'var(--t2)',
  letterSpacing: '0.04em', marginBottom: '6px', textTransform: 'uppercase',
};
const btnPrimary: React.CSSProperties = {
  display: 'inline-flex', alignItems: 'center', gap: '6px',
  padding: '7px 14px', borderRadius: 'var(--seer-radius-md)',
  fontSize: '12px', fontWeight: 500, fontFamily: 'inherit',
  cursor: 'pointer', border: '1px solid var(--acc)',
  background: 'var(--acc)', color: 'var(--bg0)',
};
const btnSecondary: React.CSSProperties = {
  display: 'inline-flex', alignItems: 'center', gap: '6px',
  padding: '7px 14px', borderRadius: 'var(--seer-radius-md)',
  fontSize: '12px', fontWeight: 500, fontFamily: 'inherit',
  cursor: 'pointer', border: '1px solid var(--bd)',
  background: 'transparent', color: 'var(--t2)',
};
