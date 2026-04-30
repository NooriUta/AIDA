import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import {
  type DaliSource, type CreateSourceInput,
  testConnection,
} from '../api/dali';
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

export function SourceModal({ source, onSave, onClose }: ModalProps) {
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

  const [testing,    setTesting]    = useState(false);
  const [testResult, setTestResult] = useState<{ ok: boolean; latencyMs?: number; error?: string } | null>(null);
  const [saving,     setSaving]     = useState(false);
  const [errors,     setErrors]     = useState<Record<string, string>>({});

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
