import { useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { type DaliDialect, uploadAndParse } from '../api/dali';
import css from '../components/dali/dali.module.css';

// ── File Upload card ──────────────────────────────────────────────────────────

export function FileUploadCard() {
  const { t } = useTranslation();
  const inputRef = useRef<HTMLInputElement>(null);
  const [files,     setFiles]     = useState<File[]>([]);
  const [dialect,   setDialect]   = useState<DaliDialect>('plsql');
  const [preview,   setPreview]   = useState(false);
  const [clearBfw,  setClearBfw]  = useState(true);
  const [dragOver,  setDragOver]  = useState(false);
  const [uploading, setUploading] = useState(false);
  const [result,    setResult]    = useState<{ ok: boolean; msg: string } | null>(null);

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
