import { useEffect, useRef, useState } from 'react';
import { useTranslation }               from 'react-i18next';
import {
  type DaliSource, type CreateSourceInput,
  getSources, createSource, updateSource, deleteSource,
  testConnection, uploadAndParse,
  type DaliDialect,
} from '../api/dali';
import { useAuthStore }  from '../stores/authStore';
import { usePageTitle }  from '../hooks/usePageTitle';
import css from '../components/dali/dali.module.css';

// ── Constants ─────────────────────────────────────────────────────────────────

const DIALECTS = ['oracle', 'postgresql', 'clickhouse'] as const;

const DEFAULT_EXCLUSIONS: Record<string, string[]> = {
  oracle:     ['SYS','SYSTEM','DBSNMP','OUTLN','MDSYS','ORDSYS','XDB','WMSYS',
               'CTXSYS','ANONYMOUS','APPQOSSYS','MGMT_VIEW','EXFSYS','DMSYS',
               'OJVMSYS','GSMADMIN_INTERNAL'],
  postgresql: ['information_schema','pg_catalog','pg_toast','pg_temp'],
  clickhouse: ['system','information_schema','INFORMATION_SCHEMA'],
};

// ── Toast ─────────────────────────────────────────────────────────────────────

interface Toast { id: number; msg: string; type: 'suc' | 'err' | 'inf' }
let _toastId = 0;

// ── Tag input ─────────────────────────────────────────────────────────────────

function TagInput({ tags, onChange, placeholder }: {
  tags: string[];
  onChange: (t: string[]) => void;
  placeholder?: string;
}) {
  const [input, setInput] = useState('');

  function addTag(value: string) {
    const trimmed = value.trim();
    if (trimmed && !tags.includes(trimmed)) onChange([...tags, trimmed]);
    setInput('');
  }

  function removeTag(tag: string) {
    onChange(tags.filter(t => t !== tag));
  }

  return (
    <div style={{
      display: 'flex', flexWrap: 'wrap', gap: 4, alignItems: 'center',
      padding: '4px 8px', minHeight: 34,
      background: 'var(--bg2)', border: '1px solid var(--bd)', borderRadius: 4,
    }}>
      {tags.map(tag => (
        <span key={tag} style={{
          display: 'inline-flex', alignItems: 'center', gap: 4,
          padding: '2px 8px', borderRadius: 3, fontSize: 11,
          background: 'color-mix(in srgb, var(--acc) 12%, transparent)',
          border: '1px solid color-mix(in srgb, var(--acc) 30%, transparent)',
          color: 'var(--t1)', fontFamily: 'var(--mono)',
        }}>
          {tag}
          <button
            onClick={() => removeTag(tag)}
            style={{ background: 'none', border: 'none', cursor: 'pointer',
              color: 'var(--t3)', padding: 0, lineHeight: 1, fontSize: 13 }}
          >×</button>
        </span>
      ))}
      <input
        value={input}
        onChange={e => setInput(e.target.value)}
        onKeyDown={e => {
          if (e.key === 'Enter' || e.key === ',') { e.preventDefault(); addTag(input); }
          if (e.key === 'Backspace' && !input && tags.length) onChange(tags.slice(0, -1));
        }}
        onBlur={() => { if (input.trim()) addTag(input); }}
        placeholder={tags.length === 0 ? placeholder : ''}
        style={{
          flex: 1, minWidth: 80, background: 'none', border: 'none', outline: 'none',
          fontSize: 12, color: 'var(--t1)', padding: '1px 2px',
        }}
      />
    </div>
  );
}

// ── Source modal (Add / Edit) ─────────────────────────────────────────────────

interface ModalProps {
  source?: DaliSource | null;
  onSave: (input: CreateSourceInput) => Promise<void>;
  onClose: () => void;
}

function SourceModal({ source, onSave, onClose }: ModalProps) {
  const { t } = useTranslation();
  const isEdit = !!source;

  const [name,     setName]     = useState(source?.name     ?? '');
  const [dialect,  setDialect]  = useState(source?.dialect  ?? 'oracle');
  const [jdbcUrl,  setJdbcUrl]  = useState(source?.jdbcUrl  ?? '');
  const [username, setUsername] = useState(source?.username ?? '');
  const [password, setPassword] = useState('');
  const [include,  setInclude]  = useState<string[]>(source?.schemaFilter.include ?? []);
  const [exclude,  setExclude]  = useState<string[]>(
    source?.schemaFilter.exclude ?? DEFAULT_EXCLUSIONS[source?.dialect ?? 'oracle'] ?? []);

  const [testing,  setTesting]  = useState(false);
  const [testResult, setTestResult] = useState<{ ok: boolean; latencyMs?: number; error?: string } | null>(null);
  const [saving,   setSaving]   = useState(false);
  const [errors,   setErrors]   = useState<Record<string, string>>({});

  // Reset exclusions when dialect changes (only for new sources)
  function handleDialectChange(d: string) {
    setDialect(d);
    if (!isEdit) setExclude(DEFAULT_EXCLUSIONS[d] ?? []);
    setTestResult(null);
  }

  async function handleTest() {
    if (!jdbcUrl.trim()) return;
    setTesting(true); setTestResult(null);
    try {
      const result = await testConnection(jdbcUrl, username, password);
      setTestResult(result);
    } catch { setTestResult({ ok: false, error: 'Connection test failed' }); }
    finally { setTesting(false); }
  }

  async function handleSave() {
    const errs: Record<string, string> = {};
    if (!name.trim())    errs.name    = t('sources.modal.required');
    if (!jdbcUrl.trim()) errs.jdbcUrl = t('sources.modal.required');
    if (!isEdit && !password.trim()) errs.password = t('sources.modal.required');
    if (Object.keys(errs).length) { setErrors(errs); return; }
    setSaving(true);
    try {
      await onSave({
        name: name.trim(), dialect, jdbcUrl: jdbcUrl.trim(),
        username: username.trim(), password,
        schemaFilter: { include, exclude },
      });
      onClose();
    } catch (e: unknown) {
      setErrors({ global: e instanceof Error ? e.message : 'Save failed' });
    } finally { setSaving(false); }
  }

  return (
    <div style={{
      position: 'fixed', inset: 0, zIndex: 400,
      background: 'rgba(0,0,0,0.6)', backdropFilter: 'blur(2px)',
      display: 'flex', alignItems: 'center', justifyContent: 'center',
    }} onClick={e => { if (e.target === e.currentTarget) onClose(); }}>
      <div style={{
        width: 540, maxHeight: '90vh', overflowY: 'auto',
        background: 'var(--bg1)', border: '1px solid var(--bd)', borderRadius: 8,
        boxShadow: '0 8px 32px rgba(0,0,0,0.5)',
      }}>
        {/* Header */}
        <div style={{
          display: 'flex', alignItems: 'center', justifyContent: 'space-between',
          padding: '14px 18px', borderBottom: '1px solid var(--bd)',
        }}>
          <span style={{ fontSize: 13, fontWeight: 600, color: 'var(--t1)' }}>
            {isEdit ? t('sources.modal.editTitle') : t('sources.modal.addTitle')}
          </span>
          <button onClick={onClose} style={{
            background: 'none', border: 'none', cursor: 'pointer',
            color: 'var(--t3)', fontSize: 16, lineHeight: 1, padding: 2,
          }}>×</button>
        </div>

        {/* Body */}
        <div style={{ padding: '18px 18px 8px' }}>
          {errors.global && (
            <div style={{
              padding: '8px 12px', marginBottom: 12, borderRadius: 4, fontSize: 12,
              background: 'color-mix(in srgb, var(--danger) 10%, transparent)',
              border: '1px solid color-mix(in srgb, var(--danger) 30%, transparent)',
              color: 'var(--danger)',
            }}>{errors.global}</div>
          )}

          {/* Name + Dialect row */}
          <div style={{ display: 'flex', gap: 12, marginBottom: 12 }}>
            <div style={{ flex: 1 }}>
              <label className={css.fieldLabel}>{t('sources.modal.name')}</label>
              <input
                className="inp"
                value={name}
                onChange={e => setName(e.target.value)}
                placeholder="oracle-prod"
                style={{ width: '100%', marginTop: 4 }}
              />
              {errors.name && <div className={css.fieldError}>{errors.name}</div>}
            </div>
            <div style={{ width: 140 }}>
              <label className={css.fieldLabel}>{t('sources.modal.dialect')}</label>
              <select
                className="inp"
                value={dialect}
                onChange={e => handleDialectChange(e.target.value)}
                style={{ width: '100%', marginTop: 4 }}
              >
                {DIALECTS.map(d => <option key={d} value={d}>{d}</option>)}
              </select>
            </div>
          </div>

          {/* JDBC URL */}
          <div style={{ marginBottom: 12 }}>
            <label className={css.fieldLabel}>{t('sources.modal.jdbcUrl')}</label>
            <input
              className="inp"
              value={jdbcUrl}
              onChange={e => { setJdbcUrl(e.target.value); setTestResult(null); }}
              placeholder="jdbc:oracle:thin:@host:1521/XEPDB1"
              style={{ width: '100%', marginTop: 4, fontFamily: 'var(--mono)', fontSize: 12 }}
            />
            {errors.jdbcUrl && <div className={css.fieldError}>{errors.jdbcUrl}</div>}
          </div>

          {/* Username + Password row */}
          <div style={{ display: 'flex', gap: 12, marginBottom: 12 }}>
            <div style={{ flex: 1 }}>
              <label className={css.fieldLabel}>{t('sources.modal.username')}</label>
              <input
                className="inp"
                value={username}
                onChange={e => setUsername(e.target.value)}
                placeholder="dali_harvest"
                style={{ width: '100%', marginTop: 4 }}
              />
            </div>
            <div style={{ flex: 1 }}>
              <label className={css.fieldLabel}>
                {isEdit ? t('sources.modal.passwordEdit') : t('sources.modal.password')}
              </label>
              <input
                className="inp"
                type="password"
                value={password}
                onChange={e => { setPassword(e.target.value); setTestResult(null); }}
                placeholder={isEdit ? t('sources.modal.passwordPlaceholder') : ''}
                style={{ width: '100%', marginTop: 4 }}
              />
              {errors.password && <div className={css.fieldError}>{errors.password}</div>}
            </div>
          </div>

          {/* Schema filter */}
          <div style={{ marginBottom: 12 }}>
            <label className={css.fieldLabel}>{t('sources.modal.schemaInclude')}</label>
            <div style={{ marginTop: 4 }}>
              <TagInput
                tags={include}
                onChange={setInclude}
                placeholder={t('sources.modal.schemaIncludePlaceholder')}
              />
            </div>
            <div style={{ fontSize: 10, color: 'var(--t4)', marginTop: 3 }}>
              {t('sources.modal.schemaIncludeHint')}
            </div>
          </div>
          <div style={{ marginBottom: 16 }}>
            <label className={css.fieldLabel}>{t('sources.modal.schemaExclude')}</label>
            <div style={{ marginTop: 4 }}>
              <TagInput tags={exclude} onChange={setExclude} />
            </div>
            <div style={{ fontSize: 10, color: 'var(--t4)', marginTop: 3 }}>
              {t('sources.modal.schemaExcludeHint')}
            </div>
          </div>

          {/* Test connection */}
          <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 18 }}>
            <button
              className="btn btn-secondary btn-sm"
              onClick={handleTest}
              disabled={testing || !jdbcUrl.trim()}
            >
              {testing ? '…' : '⚡'} {t('sources.modal.testBtn')}
            </button>
            {testResult && (
              <span style={{
                fontSize: 11, fontFamily: 'var(--mono)',
                color: testResult.ok ? 'var(--suc)' : 'var(--danger)',
              }}>
                {testResult.ok
                  ? `✓ ${testResult.latencyMs}ms`
                  : `✗ ${testResult.error}`}
              </span>
            )}
          </div>
        </div>

        {/* Footer */}
        <div style={{
          display: 'flex', gap: 8, justifyContent: 'flex-end',
          padding: '12px 18px', borderTop: '1px solid var(--bd)',
        }}>
          <button className="btn btn-secondary btn-sm" onClick={onClose}>
            {t('sources.modal.cancel')}
          </button>
          <button
            className="btn btn-primary btn-sm"
            onClick={handleSave}
            disabled={saving}
          >
            {saving ? '…' : isEdit ? t('sources.modal.save') : t('sources.modal.add')}
          </button>
        </div>
      </div>
    </div>
  );
}

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

// ── File Upload card ──────────────────────────────────────────────────────────

function FileUploadCard() {
  const { t } = useTranslation();
  const inputRef = useRef<HTMLInputElement>(null);
  const [files,    setFiles]    = useState<File[]>([]);
  const [dialect,  setDialect]  = useState<DaliDialect>('plsql');
  const [preview,  setPreview]  = useState(false);
  const [clearBfw, setClearBfw] = useState(true);
  const [dragOver, setDragOver] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [result,   setResult]   = useState<{ ok: boolean; msg: string } | null>(null);

  function handleFiles(fl: FileList | null) {
    if (!fl) return;
    setFiles(Array.from(fl));
    setResult(null);
  }

  async function handleUpload() {
    if (!files.length) return;
    setUploading(true); setResult(null);
    try {
      const session = await uploadAndParse(files[0], dialect, preview, clearBfw);
      setResult({ ok: true, msg: t('sources.upload.success', { id: session.id.slice(0, 8) }) });
      setFiles([]);
    } catch (e: unknown) {
      setResult({ ok: false, msg: e instanceof Error ? e.message : 'Upload failed' });
    } finally { setUploading(false); }
  }

  return (
    <div className={css.panel}>
      <div className={css.panelHeader}>
        <span className={css.panelTitle}>
          {t('sources.upload.title')}
          <span style={{ fontWeight: 400, color: 'var(--t4)', fontSize: 10, marginLeft: 6 }}>
            UC-1b
          </span>
        </span>
      </div>
      <div className={css.panelBody}>
        {/* Drop zone */}
        <div
          className={`${css.uploadArea} ${dragOver ? css.uploadAreaOver : ''} ${files.length ? css.uploadAreaFilled : ''}`}
          style={{ flexDirection: 'column', gap: 4, minHeight: 64, justifyContent: 'center' }}
          onClick={() => inputRef.current?.click()}
          onDragOver={e => { e.preventDefault(); setDragOver(true); }}
          onDragLeave={() => setDragOver(false)}
          onDrop={e => { e.preventDefault(); setDragOver(false); handleFiles(e.dataTransfer.files); }}
        >
          {files.length ? (
            <>
              {files.map(f => (
                <div key={f.name} className={css.uploadFileName}>{f.name} ({(f.size / 1024).toFixed(1)} KB)</div>
              ))}
            </>
          ) : (
            <>
              <div className={css.uploadPlaceholder}>{t('sources.upload.dropHint')}</div>
              <div style={{ fontSize: 10, color: 'var(--t4)' }}>.sql .pck .prc .pkb .pks .fnc .trg .vw .zip .rar</div>
            </>
          )}
        </div>
        <input
          ref={inputRef}
          type="file"
          multiple
          accept=".sql,.pck,.prc,.pkb,.pks,.fnc,.trg,.vw,.zip,.rar"
          style={{ display: 'none' }}
          onChange={e => handleFiles(e.target.files)}
        />

        {/* Options row */}
        <div style={{ display: 'flex', gap: 16, alignItems: 'center', marginTop: 10, flexWrap: 'wrap' }}>
          <div>
            <div className={css.fieldLabel} style={{ marginBottom: 4 }}>{t('sources.modal.dialect')}</div>
            <select
              className="inp"
              value={dialect}
              onChange={e => setDialect(e.target.value as DaliDialect)}
              style={{ width: 150 }}
            >
              <option value="plsql">Oracle / PL-SQL</option>
              <option value="postgresql">PostgreSQL</option>
              <option value="clickhouse">ClickHouse</option>
            </select>
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 4, marginTop: 14 }}>
            <label className={css.previewRow}>
              <input type="checkbox" checked={preview} onChange={e => setPreview(e.target.checked)} />
              {t('sources.upload.previewMode')}
            </label>
            <label className={css.previewRow}>
              <input type="checkbox" checked={clearBfw} onChange={e => setClearBfw(e.target.checked)} />
              {t('sources.upload.clearBeforeWrite')}
            </label>
          </div>
        </div>

        {result && (
          <div style={{
            marginTop: 10, padding: '7px 10px', borderRadius: 4, fontSize: 12,
            fontFamily: 'var(--mono)',
            background: result.ok
              ? 'color-mix(in srgb, var(--suc) 10%, transparent)'
              : 'color-mix(in srgb, var(--danger) 10%, transparent)',
            color: result.ok ? 'var(--suc)' : 'var(--danger)',
            border: `1px solid ${result.ok
              ? 'color-mix(in srgb, var(--suc) 25%, transparent)'
              : 'color-mix(in srgb, var(--danger) 25%, transparent)'}`,
          }}>{result.msg}</div>
        )}

        <button
          className="btn btn-primary"
          style={{ marginTop: 12, width: '100%', justifyContent: 'center' }}
          onClick={handleUpload}
          disabled={uploading || !files.length}
        >
          {uploading ? '…' : '↑'} {t('sources.upload.uploadBtn')}
        </button>
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
