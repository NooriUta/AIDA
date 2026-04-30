import { useRef, useState }   from 'react';
import { useTranslation }      from 'react-i18next';
import { type DaliDialect, type DaliSession, postSession, uploadAndParse } from '../../api/dali';
import css from './dali.module.css';

interface ParseFormProps {
  onSessionCreated: (session: DaliSession) => void;
  tenantAlias?: string;
}

const DIALECTS: { value: DaliDialect; label: string }[] = [
  { value: 'plsql',      label: 'PL/SQL'      },
  { value: 'postgresql', label: 'PostgreSQL'   },
  { value: 'clickhouse', label: 'ClickHouse'   },
];

type Mode = 'path' | 'upload';

export function ParseForm({ onSessionCreated, tenantAlias }: ParseFormProps) {
  const { t } = useTranslation();
  const [mode,             setMode]             = useState<Mode>('path');
  const [dialect,          setDialect]          = useState<DaliDialect>('plsql');
  const [source,           setSource]           = useState('');
  const [file,             setFile]             = useState<File | null>(null);
  const [dragOver,         setDragOver]         = useState(false);
  const [preview,          setPreview]          = useState(false);
  const [clearBeforeWrite, setClearBeforeWrite] = useState(true);
  const [dbName,           setDbName]           = useState('');
  const [error,            setError]            = useState('');
  const [loading,          setLoading]          = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  function switchMode(next: Mode) {
    setMode(next);
    setError('');
    setFile(null);
    setSource('');
  }

  async function handleSubmit() {
    setError('');
    setLoading(true);
    try {
      let session: DaliSession;
      const resolvedDbName = dbName.trim() || undefined;
      if (mode === 'upload') {
        if (!file) { setError(t('dali.form.errSelectFile')); return; }
        session = await uploadAndParse(file, dialect, preview, clearBeforeWrite, resolvedDbName, undefined, tenantAlias);
        setFile(null);
        if (fileInputRef.current) fileInputRef.current.value = '';
      } else {
        const src = source.trim().replace(/[\t\r\n\u00a0\ufeff]+/g, '');
        if (!src) { setError(t('dali.form.errSourceRequired')); return; }
        session = await postSession({ dialect, source: src, preview, clearBeforeWrite, dbName: resolvedDbName }, tenantAlias);
        setSource('');
      }
      onSessionCreated(session);
    } catch (err: unknown) {
      const msg = (err as Error).message ?? 'request failed';
      if (msg.includes('clearBeforeWrite') || msg.includes('active')) {
        setError(t('dali.form.errAnotherActive'));
      } else if (msg.includes('HTTP 413') || msg.includes('size exceeds') || msg.includes('too large')) {
        setError(t('dali.form.err413'));
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

  function handleDrop(e: React.DragEvent) {
    e.preventDefault();
    setDragOver(false);
    const dropped = e.dataTransfer.files[0];
    if (dropped) { setFile(dropped); setError(''); }
  }

  return (
    <div className={css.panel}>
      <div className={css.panelHeader}>
        <span className={css.panelTitle}>
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <polygon points="5 3 19 12 5 21 5 3"/>
          </svg>
          {t('dali.form.title')}
        </span>
        {/* Mode toggle */}
        <div className={css.modeToggle}>
          <button
            className={mode === 'path' ? css.modeBtnActive : css.modeBtn}
            onClick={() => switchMode('path')}
          >{t('dali.form.modePathBtn')}</button>
          <button
            className={mode === 'upload' ? css.modeBtnActive : css.modeBtn}
            onClick={() => switchMode('upload')}
          >{t('dali.form.modeUploadBtn')}</button>
        </div>
      </div>

      <div className={css.panelBody}>
        {/* Row 1: Dialect + Source / Upload area */}
        <div className={css.formRowTop}>
          <div className={css.fieldGroup}>
            <label className={css.fieldLabel}>{t('dali.form.dialectLabel')}</label>
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

          {mode === 'path' ? (
            <div className={css.fieldGroup} style={{ flex: 1 }}>
              <label className={css.fieldLabel}>{t('dali.form.sourceLabel')}</label>
              <input
                className="field-input"
                style={{ fontFamily: 'var(--mono)', fontSize: '13px', padding: '6px 10px' }}
                type="text"
                value={source}
                onChange={e => { setSource(e.target.value.replace(/[\t\r\n]+/g, '')); if (error) setError(''); }}
                onKeyDown={handleKeyDown}
                placeholder={t('dali.form.sourcePlaceholder')}
              />
              <div className={css.fieldError}>{error}</div>
            </div>
          ) : (
            <div className={css.fieldGroup} style={{ flex: 1 }}>
              <label className={css.fieldLabel}>{t('dali.form.fileLabel')}</label>
              <div
                className={`${css.uploadArea} ${dragOver ? css.uploadAreaOver : ''} ${file ? css.uploadAreaFilled : ''}`}
                onClick={() => fileInputRef.current?.click()}
                onDragOver={e => { e.preventDefault(); setDragOver(true); }}
                onDragLeave={() => setDragOver(false)}
                onDrop={handleDrop}
              >
                {file ? (
                  <span className={css.uploadFileName}>{file.name}</span>
                ) : (
                  <span className={css.uploadPlaceholder}>{t('dali.form.dropPlaceholder')}</span>
                )}
              </div>
              <input
                ref={fileInputRef}
                type="file"
                accept=".zip,.rar,.sql,.pck,.prc,.pkb,.pks,.fnc,.trg,.vw"
                style={{ display: 'none' }}
                onChange={e => {
                  const f = e.target.files?.[0] ?? null;
                  setFile(f);
                  if (error) setError('');
                }}
              />
              <div className={css.fieldError}>{error}</div>
            </div>
          )}
        </div>

        {/* Row 2: DB name (Schema namespace) */}
        <div className={css.formRowTop} style={{ marginTop: 0 }}>
          <div className={css.fieldGroup} style={{ flex: 1 }}>
            <label className={css.fieldLabel} style={{ display: 'flex', alignItems: 'center', gap: 5 }}>
              {t('dali.form.dbNameLabel')}
              <span
                title={t('dali.form.dbNameTooltip')}
                style={{
                  display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
                  width: 14, height: 14, borderRadius: '50%',
                  background: 'var(--bg3)', border: '1px solid var(--bd)',
                  fontSize: 9, color: 'var(--t3)', cursor: 'help', flexShrink: 0,
                }}
              >?</span>
            </label>
            <input
              className="field-input"
              style={{ fontFamily: 'var(--mono)', fontSize: '13px', padding: '6px 10px' }}
              type="text"
              value={dbName}
              onChange={e => setDbName(e.target.value)}
              placeholder={t('dali.form.dbNamePlaceholder')}
            />
          </div>
        </div>

        {/* Row 3: Options + Parse button */}
        <div className={css.formRowBottom}>
          <div className={css.checkboxStack}>
            <div className={css.previewRow}>
              <input
                type="checkbox"
                id="dali-preview"
                checked={preview}
                onChange={e => setPreview(e.target.checked)}
              />
              <label htmlFor="dali-preview">{t('dali.form.previewLabel')}</label>
            </div>
            <div className={css.previewRow} style={{ opacity: preview ? 0.4 : 1 }}>
              <input
                type="checkbox"
                id="dali-clear"
                checked={clearBeforeWrite}
                disabled={preview}
                onChange={e => setClearBeforeWrite(e.target.checked)}
              />
              <label htmlFor="dali-clear">{t('dali.form.clearLabel')}</label>
              {clearBeforeWrite && !preview && (
                <span style={{
                  fontSize: 10, color: 'var(--wrn)', fontFamily: 'var(--mono)',
                  marginLeft: 4, whiteSpace: 'nowrap',
                }}>
                  {t('dali.form.clearWarning')}
                </span>
              )}
            </div>
          </div>

          <button
            className="btn btn-primary"
            style={{ fontSize: '12px', padding: '7px 14px', alignSelf: 'center' }}
            onClick={handleSubmit}
            disabled={loading}
          >
            {loading ? t('dali.form.submitting') : (
              <>
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                  <polygon points="5 3 19 12 5 21 5 3"/>
                </svg>
                {mode === 'upload' ? t('dali.form.submitUpload') : t('dali.form.submitParse')}
              </>
            )}
          </button>
        </div>
      </div>
    </div>
  );
}
