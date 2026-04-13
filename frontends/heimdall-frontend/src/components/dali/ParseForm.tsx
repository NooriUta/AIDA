import { useState } from 'react';
import { type DaliDialect, type DaliSession, postSession } from '../../api/dali';
import css from './dali.module.css';

interface ParseFormProps {
  onSessionCreated: (session: DaliSession) => void;
}

const DIALECTS: { value: DaliDialect; label: string }[] = [
  { value: 'plsql',      label: 'PL/SQL'      },
  { value: 'postgresql', label: 'PostgreSQL'   },
  { value: 'clickhouse', label: 'ClickHouse'   },
];

export function ParseForm({ onSessionCreated }: ParseFormProps) {
  const [dialect,          setDialect]          = useState<DaliDialect>('plsql');
  const [source,           setSource]           = useState('');
  const [preview,          setPreview]          = useState(false);
  const [clearBeforeWrite, setClearBeforeWrite] = useState(true);
  const [error,            setError]            = useState('');
  const [loading,          setLoading]          = useState(false);

  async function handleSubmit() {
    const src = source.trim().replace(/[\t\r\n\u00a0\ufeff]+/g, '');
    if (!src) {
      setError('source path is required');
      return;
    }
    setError('');
    setLoading(true);
    try {
      const session = await postSession({ dialect, source: src, preview, clearBeforeWrite });
      setSource('');
      onSessionCreated(session);
    } catch (err: unknown) {
      const msg = (err as Error).message ?? 'request failed';
      // Make concurrency-conflict messages more readable
      if (msg.includes('clearBeforeWrite') || msg.includes('active')) {
        setError('Another session is active — wait for it to finish before starting a new one');
      } else {
        setError(msg);
      }
    } finally {
      setLoading(false);
    }
  }

  function handleKeyDown(e: React.KeyboardEvent<HTMLInputElement>) {
    if (e.key === 'Enter') handleSubmit();
  }

  return (
    <div className={css.panel}>
      <div className={css.panelHeader}>
        <span className={css.panelTitle}>
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <polygon points="5 3 19 12 5 21 5 3"/>
          </svg>
          New parse session
        </span>
      </div>
      <div className={css.panelBody}>
        <div className={css.formRow}>
          {/* Dialect */}
          <div className={css.fieldGroup}>
            <label className={css.fieldLabel}>Dialect</label>
            <select
              className="field-input"
              value={dialect}
              onChange={e => setDialect(e.target.value as DaliDialect)}
              style={{ fontSize: '13px', padding: '6px 10px' }}
            >
              {DIALECTS.map(d => (
                <option key={d.value} value={d.value}>{d.label}</option>
              ))}
            </select>
          </div>

          {/* Source path */}
          <div className={css.fieldGroup}>
            <label className={css.fieldLabel}>Source path</label>
            <input
              className="field-input"
              style={{ fontFamily: 'var(--mono)', fontSize: '13px', padding: '6px 10px' }}
              type="text"
              value={source}
              onChange={e => { setSource(e.target.value.replace(/[\t\r\n]+/g, '')); if (error) setError(''); }}
              onKeyDown={handleKeyDown}
              placeholder="/data/sql-sources/my_package.pck"
            />
            <div className={css.fieldError}>{error}</div>
          </div>

          {/* Options: Preview + Clear before write */}
          <div className={css.fieldGroup}>
            <span className={css.fieldLabel}>Options</span>
            <div className={css.checkboxStack}>
              <div className={css.previewRow}>
                <input
                  type="checkbox"
                  id="dali-preview"
                  checked={preview}
                  onChange={e => setPreview(e.target.checked)}
                />
                <label htmlFor="dali-preview">Preview (dry-run)</label>
              </div>
              <div className={css.previewRow} style={{ opacity: preview ? 0.4 : 1 }}>
                <input
                  type="checkbox"
                  id="dali-clear"
                  checked={clearBeforeWrite}
                  disabled={preview}
                  onChange={e => setClearBeforeWrite(e.target.checked)}
                />
                <label htmlFor="dali-clear">Clear YGG before write</label>
              </div>
            </div>
          </div>

          {/* Parse button */}
          <div className={css.fieldGroup}>
            <span className={css.fieldLabel} style={{ opacity: 0 }}>x</span>
            <button
              className="btn btn-primary"
              style={{ fontSize: '12px', padding: '7px 14px' }}
              onClick={handleSubmit}
              disabled={loading}
            >
              {loading ? '...' : (
                <>
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                    <polygon points="5 3 19 12 5 21 5 3"/>
                  </svg>
                  Parse
                </>
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
