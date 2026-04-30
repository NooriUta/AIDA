import { useEffect, useRef, useState } from 'react';
import { useTranslation }               from 'react-i18next';
import {
  type DaliSource, type CreateSourceInput,
  getSources, createSource, updateSource, deleteSource,
} from '../api/dali';
import { useAuthStore }  from '../stores/authStore';
import { usePageTitle }  from '../hooks/usePageTitle';
import css from '../components/dali/dali.module.css';
import { SourceModal }     from './SourceModal';
import { FileUploadCard }  from './FileUploadCard';

// ── Toast ─────────────────────────────────────────────────────────────────────

interface Toast { id: number; msg: string; type: 'suc' | 'err' | 'inf' }
let _toastId = 0;

// ── Delete confirm modal ──────────────────────────────────────────────────────

function ConfirmDeleteModal({ name, onConfirm, onClose }: {
  name: string; onConfirm: () => void; onClose: () => void;
}) {
  const { t } = useTranslation();
  return (
    <div style={{
      position: 'fixed', inset: 0, zIndex: 400,
      background: 'rgba(0,0,0,0.6)',
      display: 'flex', alignItems: 'center', justifyContent: 'center',
    }} onClick={e => { if (e.target === e.currentTarget) onClose(); }}>
      <div style={{
        width: 360, background: 'var(--bg1)', border: '1px solid var(--bd)',
        borderRadius: 8, boxShadow: '0 8px 32px rgba(0,0,0,0.5)',
        padding: '20px 20px 16px',
      }}>
        <div style={{ fontSize: 13, fontWeight: 600, color: 'var(--t1)', marginBottom: 8 }}>
          {t('sources.deleteModal.title')}
        </div>
        <div style={{ fontSize: 12, color: 'var(--t2)', marginBottom: 18 }}>
          {t('sources.deleteModal.body', { name })}
        </div>
        <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
          <button className="btn btn-secondary btn-sm" onClick={onClose}>
            {t('sources.deleteModal.cancel')}
          </button>
          <button
            className="btn btn-sm"
            style={{ background: 'var(--danger)', color: '#fff', border: 'none' }}
            onClick={onConfirm}
          >
            {t('sources.deleteModal.confirm')}
          </button>
        </div>
      </div>
    </div>
  );
}

// ── DaliSourcesPage ───────────────────────────────────────────────────────────

export default function DaliSourcesPage() {
  const { t } = useTranslation();
  const sessionUser = useAuthStore(s => s.user);

  const [tenantAlias, setTenantAlias] = useState<string>(() => {
    const stored = localStorage.getItem('seer-active-tenant');
    if (stored && stored !== '__all__') return stored;
    return sessionUser?.activeTenantAlias ?? 'default';
  });

  useEffect(() => {
    if (sessionUser && sessionUser.role !== 'super-admin') {
      setTenantAlias(sessionUser.activeTenantAlias ?? 'default');
    }
  }, [sessionUser?.id, sessionUser?.role]);

  useEffect(() => {
    const handler = (e: Event) => {
      const alias = (e as CustomEvent<{ activeTenant: string }>).detail?.activeTenant;
      if (alias && alias !== '__all__') setTenantAlias(alias);
    };
    window.addEventListener('aida:tenant', handler);
    return () => window.removeEventListener('aida:tenant', handler);
  }, []);

  const [sources,    setSources]    = useState<DaliSource[]>([]);
  const [loading,    setLoading]    = useState(true);
  const [modal,      setModal]      = useState<'add' | 'edit' | null>(null);
  const [editTarget, setEditTarget] = useState<DaliSource | null>(null);
  const [delTarget,  setDelTarget]  = useState<DaliSource | null>(null);
  const [toasts,     setToasts]     = useState<Toast[]>([]);

  usePageTitle('Sources');

  const addToast = useRef<(msg: string, type: Toast['type']) => void>(null!);
  addToast.current = (msg, type) => {
    const id = ++_toastId;
    setToasts(prev => [...prev, { id, msg, type }]);
    setTimeout(() => setToasts(prev => prev.filter(t => t.id !== id)), 3200);
  };

  async function load() {
    setLoading(true);
    try { setSources(await getSources(tenantAlias)); }
    catch { addToast.current(t('sources.loadError'), 'err'); }
    finally { setLoading(false); }
  }

  useEffect(() => { void load(); }, [tenantAlias]);

  async function handleSave(input: CreateSourceInput) {
    if (modal === 'edit' && editTarget) {
      const updated = await updateSource(editTarget.id, input, tenantAlias);
      setSources(prev => prev.map(s => s.id === updated.id ? updated : s));
      addToast.current(t('sources.savedToast', { name: updated.name }), 'suc');
    } else {
      const created = await createSource(input, tenantAlias);
      setSources(prev => [...prev, created]);
      addToast.current(t('sources.createdToast', { name: created.name }), 'suc');
    }
  }

  async function handleDelete() {
    if (!delTarget) return;
    await deleteSource(delTarget.id, tenantAlias);
    setSources(prev => prev.filter(s => s.id !== delTarget.id));
    addToast.current(t('sources.deletedToast', { name: delTarget.name }), 'inf');
    setDelTarget(null);
  }

  function openEdit(source: DaliSource) {
    setEditTarget(source);
    setModal('edit');
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%', background: 'var(--bg0)', overflow: 'hidden' }}>
      <div style={{ flex: 1, overflowY: 'auto', padding: '24px 28px 0' }}>

        {/* Header */}
        <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', marginBottom: 20 }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 3 }}>
              <span style={{ fontSize: 15, fontWeight: 700, color: 'var(--t1)' }}>{t('sources.title')}</span>
              <span className="comp comp-dali">DALI :9090</span>
            </div>
            <div style={{ fontSize: 12, color: 'var(--t3)' }}>{t('sources.description')}</div>
          </div>
          <div style={{ display: 'flex', gap: 8 }}>
            <button className="btn btn-secondary btn-sm" onClick={load}>
              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/>
              </svg>
              {t('dali.page.refreshBtn')}
            </button>
            <button className="btn btn-primary btn-sm" onClick={() => { setEditTarget(null); setModal('add'); }}>
              + {t('sources.addBtn')}
            </button>
          </div>
        </div>

        {/* Sources table */}
        <div className={css.panel} style={{ marginBottom: 20 }}>
          <div className={css.panelHeader}>
            <span className={css.panelTitle}>
              {t('sources.jdbcTitle')}
              <span style={{ fontWeight: 400, color: 'var(--t4)', fontSize: 10 }}>UC-1a</span>
            </span>
            <span style={{ fontFamily: 'var(--mono)', fontSize: 10, color: 'var(--t4)' }}>
              {sources.length} {t('sources.sourcesCount')}
            </span>
          </div>

          {loading ? (
            <div style={{ padding: '32px', textAlign: 'center', color: 'var(--t4)', fontSize: 12 }}>
              {t('status.loading')}
            </div>
          ) : sources.length === 0 ? (
            <div className={css.empty}>
              <div className={css.emptyTitle}>{t('sources.emptyTitle')}</div>
              <div className={css.emptySub}>{t('sources.emptySub')}</div>
            </div>
          ) : (
            <table className={css.sessionTable}>
              <thead>
                <tr>
                  <th>{t('sources.col.name')}</th>
                  <th>{t('sources.col.dialect')}</th>
                  <th>{t('sources.col.jdbcUrl')}</th>
                  <th>{t('sources.col.lastHarvest')}</th>
                  <th>{t('sources.col.atoms')}</th>
                  <th style={{ width: 120 }}>{t('sources.col.actions')}</th>
                </tr>
              </thead>
              <tbody>
                {sources.map(src => (
                  <tr key={src.id} className={css.dataRow}>
                    <td>
                      <span style={{ fontWeight: 600, color: 'var(--t1)' }}>{src.name}</span>
                    </td>
                    <td>
                      <span style={{
                        fontFamily: 'var(--mono)', fontSize: 11, padding: '2px 7px',
                        borderRadius: 3, background: 'var(--bg3)', color: 'var(--t2)',
                      }}>{src.dialect}</span>
                    </td>
                    <td>
                      <span style={{
                        fontFamily: 'var(--mono)', fontSize: 11, color: 'var(--t3)',
                        maxWidth: 220, overflow: 'hidden', textOverflow: 'ellipsis',
                        whiteSpace: 'nowrap', display: 'block',
                      }}>{src.jdbcUrl}</span>
                    </td>
                    <td style={{ fontFamily: 'var(--mono)', fontSize: 11, color: 'var(--t3)' }}>
                      {src.lastHarvest ? new Date(src.lastHarvest).toLocaleString('ru-RU') : '—'}
                    </td>
                    <td style={{ fontFamily: 'var(--mono)', fontSize: 12, color: 'var(--acc)' }}>
                      {src.atomCount?.toLocaleString() ?? 0}
                    </td>
                    <td>
                      <div style={{ display: 'flex', gap: 4 }}>
                        <button
                          className="btn btn-secondary btn-sm"
                          style={{ padding: '3px 8px', fontSize: 11 }}
                          onClick={() => openEdit(src)}
                          title={t('sources.editBtn')}
                        >✎</button>
                        <button
                          className="btn btn-sm"
                          style={{
                            padding: '3px 8px', fontSize: 11,
                            background: 'color-mix(in srgb, var(--danger) 10%, transparent)',
                            color: 'var(--danger)',
                            border: '1px solid color-mix(in srgb, var(--danger) 25%, transparent)',
                          }}
                          onClick={() => setDelTarget(src)}
                          title={t('sources.deleteBtn')}
                        >🗑</button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        {/* File Upload */}
        <FileUploadCard />
      </div>

      {/* Footer */}
      <div className={css.footer}>
        <div className={css.footerItem}>
          <div className={css.footerDot} style={{ background: 'var(--suc)' }}/>
          dali :9090
        </div>
        <div className={css.footerSep}/>
        <div className={css.footerItem}>
          {sources.length} {t('sources.sourcesCount')}
        </div>
      </div>

      {/* Toast stack */}
      <div className={css.toastStack}>
        {toasts.map(toast => (
          <div key={toast.id} className={`${css.toastItem} ${css[toast.type as keyof typeof css] ?? ''}`}>
            {toast.msg}
          </div>
        ))}
      </div>

      {/* Modals */}
      {(modal === 'add' || modal === 'edit') && (
        <SourceModal
          source={modal === 'edit' ? editTarget : null}
          onSave={handleSave}
          onClose={() => { setModal(null); setEditTarget(null); }}
        />
      )}
      {delTarget && (
        <ConfirmDeleteModal
          name={delTarget.name}
          onConfirm={handleDelete}
          onClose={() => setDelTarget(null)}
        />
      )}
    </div>
  );
}
