import React, { useEffect, useState } from 'react';
import { useTranslation }              from 'react-i18next';
import { useControl }                  from '../hooks/useControl';
import type { SnapshotInfo }           from 'aida-shared';

const sectionStyle: React.CSSProperties = {
  background:   'var(--bg1)',
  border:       '1px solid var(--bd)',
  borderRadius: 'var(--seer-radius-md)',
  padding:      'var(--seer-space-4) var(--seer-space-6)',
  marginBottom: 'var(--seer-space-4)',
};

const labelStyle: React.CSSProperties = {
  fontSize:      '11px',
  color:         'var(--t3)',
  textTransform: 'uppercase',
  letterSpacing: '0.06em',
  marginBottom:  'var(--seer-space-3)',
};

function Btn({ onClick, disabled, danger, children }: {
  onClick: () => void;
  disabled?: boolean;
  danger?: boolean;
  children: React.ReactNode;
}) {
  return (
    <button
      onClick={onClick}
      disabled={disabled}
      style={{
        padding:      'var(--seer-space-2) var(--seer-space-4)',
        background:   danger ? 'rgba(248,81,73,0.12)' : 'var(--bg3)',
        border:       `1px solid ${danger ? 'var(--danger)' : 'var(--bdh)'}`,
        borderRadius: 'var(--seer-radius-sm)',
        color:        danger ? 'var(--danger)' : 'var(--t1)',
        fontSize:     '13px',
        fontFamily:   'var(--font)',
        cursor:       disabled ? 'not-allowed' : 'pointer',
        opacity:      disabled ? 0.5 : 1,
      }}
    >
      {children}
    </button>
  );
}

function Input({ value, onChange, placeholder }: {
  value: string;
  onChange: (v: string) => void;
  placeholder?: string;
}) {
  return (
    <input
      value={value}
      onChange={e => onChange(e.target.value)}
      placeholder={placeholder}
      style={{
        background:   'var(--bg2)',
        border:       '1px solid var(--bd)',
        borderRadius: 'var(--seer-radius-sm)',
        color:        'var(--t1)',
        padding:      'var(--seer-space-2) var(--seer-space-3)',
        fontSize:     '13px',
        fontFamily:   'var(--mono)',
        width:        '260px',
      }}
    />
  );
}

export default function ControlsPage() {
  const { t } = useTranslation();
  const { loading, error, resetBuffer, saveSnapshot, listSnapshots, deleteSnapshot } = useControl();
  const [snapshotName, setSnapshotName] = useState('');
  const [snapshots, setSnapshots]       = useState<SnapshotInfo[]>([]);
  const [resetDone, setResetDone]       = useState(false);
  const [saveDone, setSaveDone]         = useState(false);

  useEffect(() => {
    listSnapshots().then(setSnapshots);
  }, [listSnapshots]);

  async function handleReset() {
    if (!window.confirm(t('controls.resetConfirm'))) return;
    const ok = await resetBuffer();
    if (ok) setResetDone(true);
    setTimeout(() => setResetDone(false), 3000);
  }

  async function handleSave() {
    if (!snapshotName.trim()) return;
    const ok = await saveSnapshot(snapshotName.trim());
    if (ok) {
      setSaveDone(true);
      setSnapshotName('');
      setTimeout(() => setSaveDone(false), 3000);
      const updated = await listSnapshots();
      setSnapshots(updated);
    }
  }

  async function handleDelete(id: string) {
    if (!window.confirm(t('controls.deleteConfirm'))) return;
    const ok = await deleteSnapshot(id);
    if (ok) {
      const updated = await listSnapshots();
      setSnapshots(updated);
    }
  }

  return (
    <div style={{ padding: 'var(--seer-space-6)', overflowY: 'auto', height: '100%' }}>
      {error && (
        <div style={{ marginBottom: 'var(--seer-space-4)', padding: 'var(--seer-space-3) var(--seer-space-4)', background: 'rgba(248,81,73,0.08)', border: '1px solid var(--danger)', borderRadius: 'var(--seer-radius-sm)', fontSize: '13px', color: 'var(--danger)' }}>
          {error}
        </div>
      )}

      {/* Reset */}
      <div style={sectionStyle}>
        <div style={labelStyle}>{t('controls.resetTitle')}</div>
        <p style={{ fontSize: '13px', color: 'var(--t2)', marginBottom: 'var(--seer-space-3)' }}>
          {t('controls.resetDesc')}
        </p>
        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--seer-space-3)' }}>
          <Btn onClick={handleReset} disabled={loading} danger>
            {t('controls.resetButton')}
          </Btn>
          {resetDone && <span style={{ fontSize: '12px', color: 'var(--suc)' }}>{t('controls.resetDone')}</span>}
        </div>
      </div>

      {/* Save Snapshot */}
      <div style={sectionStyle}>
        <div style={labelStyle}>{t('controls.saveTitle')}</div>
        <p style={{ fontSize: '13px', color: 'var(--t2)', marginBottom: 'var(--seer-space-3)' }}>
          {t('controls.saveDesc')}
        </p>
        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--seer-space-3)' }}>
          <Input value={snapshotName} onChange={setSnapshotName} placeholder={t('controls.savePlaceholder')} />
          <Btn onClick={handleSave} disabled={loading || !snapshotName.trim()}>
            {t('controls.saveButton')}
          </Btn>
          {saveDone && <span style={{ fontSize: '12px', color: 'var(--suc)' }}>{t('controls.saveDone')}</span>}
        </div>
      </div>

      {/* Snapshot list */}
      <div style={sectionStyle}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--seer-space-3)' }}>
          <div style={labelStyle}>{t('controls.snapshotsTitle')}</div>
          <button
            onClick={() => listSnapshots().then(setSnapshots)}
            style={{ fontSize: '12px', color: 'var(--inf)', background: 'none', border: 'none', cursor: 'pointer', fontFamily: 'var(--font)' }}
          >
            {t('controls.refresh')}
          </button>
        </div>
        {snapshots.length === 0 ? (
          <div style={{ fontSize: '13px', color: 'var(--t3)' }}>{t('controls.noSnapshots')}</div>
        ) : (
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '13px', fontFamily: 'var(--mono)' }}>
            <thead>
              <tr style={{ color: 'var(--t3)', textAlign: 'left', borderBottom: '1px solid var(--bd)' }}>
                <th style={{ padding: '4px 8px', fontWeight: 500 }}>{t('controls.colName')}</th>
                <th style={{ padding: '4px 8px', fontWeight: 500 }}>{t('controls.colEvents')}</th>
                <th style={{ padding: '4px 8px', fontWeight: 500 }}>{t('controls.colTimestamp')}</th>
                <th style={{ padding: '4px 8px', fontWeight: 500 }}>{t('controls.colId')}</th>
                <th style={{ padding: '4px 8px', fontWeight: 500 }}>{t('controls.colActions')}</th>
              </tr>
            </thead>
            <tbody>
              {snapshots.map(s => (
                <tr key={s.id} style={{ borderBottom: '1px solid var(--bd)', color: 'var(--t2)' }}>
                  <td style={{ padding: '6px 8px', color: 'var(--t1)' }}>{s.name}</td>
                  <td style={{ padding: '6px 8px' }}>{s.eventCount}</td>
                  <td style={{ padding: '6px 8px', color: 'var(--t3)' }}>
                    {new Date(s.timestamp).toISOString().replace('T', ' ').substring(0, 19)}
                  </td>
                  <td style={{ padding: '6px 8px', color: 'var(--t3)', fontSize: '11px' }}>{s.id}</td>
                  <td style={{ padding: '6px 8px' }}>
                    <button
                      onClick={() => { void handleDelete(s.id); }}
                      disabled={loading}
                      style={{
                        padding:      '2px 8px',
                        background:   'rgba(248,81,73,0.08)',
                        border:       '1px solid var(--danger)',
                        borderRadius: 'var(--seer-radius-sm)',
                        color:        'var(--danger)',
                        fontSize:     '12px',
                        fontFamily:   'var(--font)',
                        cursor:       loading ? 'not-allowed' : 'pointer',
                        opacity:      loading ? 0.5 : 1,
                      }}
                    >
                      {t('controls.deleteButton')}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
