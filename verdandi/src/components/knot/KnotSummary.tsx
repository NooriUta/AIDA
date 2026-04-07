import { memo, useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import type { KnotSession, KnotTable, KnotStatement } from '../../services/lineage';

interface Props {
  session: KnotSession;
  tables: KnotTable[];
  statements: KnotStatement[];
}

export const KnotSummary = memo(({ session: s, tables, statements }: Props) => {
  const { t } = useTranslation();

  // Derive counts from actual data since backend session fields may be 0
  const derived = useMemo(() => {
    const tableCount = s.tableCount || tables.length;
    const columnCount = s.columnCount || tables.reduce((sum, tb) => sum + (tb.columns?.length || 0), 0);
    const schemas = new Set(tables.map(tb => tb.schema).filter(Boolean));
    const schemaCount = s.schemaCount || schemas.size;
    const packages = new Set<string>();
    const walkPkg = (list: KnotStatement[]) => {
      for (const st of list) {
        if (st.packageName) packages.add(st.packageName);
        if (st.children?.length) walkPkg(st.children);
      }
    };
    walkPkg(statements);
    const packageCount = s.packageCount || packages.size;
    const sourceTables = tables.filter(tb => tb.sourceCount > 0).length;
    const targetTables = tables.filter(tb => tb.targetCount > 0).length;
    const relationships = s.edgeReadsFrom + s.edgeWritesTo;
    return { tableCount, columnCount, schemaCount, packageCount, sourceTables, targetTables, relationships };
  }, [s, tables, statements]);

  const resolvedPct = s.atomTotal > 0
    ? ((s.atomResolved / s.atomTotal) * 100).toFixed(1) + '%'
    : '—';

  const stmtBars = [
    { label: 'SELECT', count: s.stmtSelect, color: 'var(--inf)' },
    { label: 'INSERT', count: s.stmtInsert, color: 'var(--suc)' },
    { label: 'UPDATE', count: s.stmtUpdate, color: 'var(--wrn)' },
    { label: 'MERGE',  count: s.stmtMerge,  color: 'var(--t2)' },
    { label: 'DELETE', count: s.stmtDelete, color: 'var(--dan, #C06060)' },
    { label: 'CURSOR', count: s.stmtCursor, color: 'var(--t3)' },
    { label: 'OTHER',  count: s.stmtOther,  color: 'var(--t3)' },
  ].filter(r => r.count > 0);

  const maxStmt = Math.max(...stmtBars.map(r => r.count), 1);

  return (
    <div style={{ padding: 24, overflowY: 'auto', height: '100%', boxSizing: 'border-box' }}>

      {/* ── Metric tiles ── */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(6, 1fr)', gap: 12, marginBottom: 16 }}>
        <Metric value={derived.tableCount || '—'} label={t('knot.session.tables')} />
        <Metric value={derived.columnCount || '—'} label={t('knot.session.columns')} />
        <Metric value={derived.schemaCount || '—'} label={t('knot.session.schemas')} />
        <Metric value={derived.packageCount || '—'} label={t('knot.session.packages')} />
        <Metric value={resolvedPct} label={t('knot.atoms.resolved')} accent />
        <Metric value={s.parameterCount || '—'} label={t('knot.session.parameters')} />
      </div>

      {/* ── 4-column stat sections ── */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr 1fr', gap: 12, marginBottom: 16 }}>

        {/* SQL Statements */}
        <StatSection title={t('knot.statements.sql')}>
          {stmtBars.length === 0
            ? <div style={{ fontSize: 12, color: 'var(--t3)' }}>—</div>
            : stmtBars.map(r => (
              <BarRow key={r.label} label={r.label} value={r.count} max={maxStmt} color={r.color} />
            ))
          }
        </StatSection>

        {/* DB Objects */}
        <StatSection title={t('knot.session.tables')}>
          <StatRow label={t('knot.stmt.total')} value={derived.tableCount} />
          <StatRow label={t('knot.session.sourceTables')} value={derived.sourceTables} color="var(--inf)" dot />
          <StatRow label={t('knot.session.targetTables')} value={derived.targetTables} color="var(--suc)" dot />
          <StatRow label={t('knot.session.schemas')} value={derived.schemaCount} />
          <StatRow label={t('knot.session.columns')} value={derived.columnCount} />
          <StatRow label={t('knot.session.relationships')} value={derived.relationships} />
        </StatSection>

        {/* Routines */}
        <StatSection title={t('knot.tabs.routines')}>
          <StatRow label={t('knot.session.routines')} value={s.routineCount} />
          <StatRow label={t('knot.session.packages')} value={derived.packageCount} />
          <StatRow label={t('knot.session.parameters')} value={s.parameterCount} />
          <StatRow label={t('knot.session.variables')} value={s.variableCount} />
        </StatSection>

        {/* Atoms */}
        <StatSection title={t('knot.atoms.title')}>
          <StatRow label={t('knot.stmt.total')} value={s.atomTotal} />
          <StatRow label={t('knot.atoms.resolved')} value={s.atomResolved} color="var(--suc)" dot />
          <StatRow label={t('knot.atoms.failed')} value={s.atomFailed} color="var(--dan, #C06060)" dot />
          <StatRow label={t('knot.atoms.columnRef')} value={s.atomFuncCall} />
          <StatRow label={t('knot.atoms.constant')} value={s.atomConstant} />
          <StatRow label={t('knot.atoms.funcCall')} value={s.atomFuncCall} />
        </StatSection>
      </div>

      {/* ── Processing info card ── */}
      <Card title={t('knot.session.processingInfo')}>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 16 }}>
          <InfoCell label={t('knot.session.sessionId')} value={s.sessionId} />
          <InfoCell label={t('knot.session.file')} value={extractFilename(s.filePath)} small />
          <InfoCell label={t('knot.session.dialect')} value={s.dialect.toUpperCase()} />
          <div>
            <div style={{ fontSize: 11, color: 'var(--t3)', marginBottom: 4 }}>{t('knot.session.processingMs')}</div>
            <div style={{
              fontSize: 14, fontWeight: 600, color: 'var(--acc)',
              fontFamily: "'Fira Code', monospace",
            }}>
              {s.processingMs > 0 ? `${s.processingMs.toLocaleString()} ms` : '—'}
            </div>
          </div>
        </div>
      </Card>
    </div>
  );
});

KnotSummary.displayName = 'KnotSummary';

// ── Helpers ──────────────────────────────────────────────────────────────────

function extractFilename(path: string | null): string {
  if (!path) return '—';
  const i = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
  return i >= 0 ? path.substring(i + 1) : path;
}

function Metric({ value, label, accent }: { value: number | string; label: string; accent?: boolean }) {
  return (
    <div style={{
      background: 'var(--bg2)',
      border: '1px solid var(--bd)',
      borderRadius: 8,
      padding: '12px 16px',
      display: 'flex',
      flexDirection: 'column',
      gap: 4,
    }}>
      <div style={{
        fontSize: 22, fontWeight: 600, lineHeight: 1,
        color: accent ? 'var(--acc)' : 'var(--t1)',
      }}>
        {typeof value === 'number' ? value.toLocaleString() : value}
      </div>
      <div style={{ fontSize: 10, color: 'var(--t3)', fontWeight: 400 }}>{label}</div>
    </div>
  );
}

function StatSection({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div style={{
      background: 'var(--bg2)',
      border: '1px solid var(--bd)',
      borderRadius: 8,
      padding: 16,
    }}>
      <div style={{
        fontSize: 10, fontWeight: 500, letterSpacing: '0.08em',
        textTransform: 'uppercase', color: 'var(--t3)',
        marginBottom: 12, paddingBottom: 8,
        borderBottom: '1px solid var(--bd)',
      }}>
        {title}
      </div>
      {children}
    </div>
  );
}

function StatRow({ label, value, color, dot }: {
  label: string; value: number; color?: string; dot?: boolean;
}) {
  return (
    <div style={{
      display: 'flex', justifyContent: 'space-between', alignItems: 'center',
      padding: '5px 0', borderBottom: '1px solid var(--bd)', fontSize: 12,
    }}>
      <span style={{ color: 'var(--t2)', display: 'flex', alignItems: 'center', gap: 5 }}>
        {dot && <span style={{
          width: 6, height: 6, borderRadius: '50%',
          background: color, display: 'inline-block',
        }} />}
        {label}
      </span>
      <span style={{
        fontWeight: 500, color: color || 'var(--t1)',
        fontFamily: "'Fira Code', monospace", fontSize: 11,
      }}>
        {value.toLocaleString()}
      </span>
    </div>
  );
}

function BarRow({ label, value, max, color }: {
  label: string; value: number; max: number; color: string;
}) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '4px 0' }}>
      <div style={{ width: 70, fontSize: 11, color: 'var(--t2)', flexShrink: 0 }}>{label}</div>
      <div style={{ flex: 1, height: 4, background: 'var(--bg3)', borderRadius: 2, overflow: 'hidden' }}>
        <div style={{
          height: '100%', borderRadius: 2,
          background: color, opacity: 0.7,
          width: `${(value / max) * 100}%`,
        }} />
      </div>
      <div style={{
        width: 40, textAlign: 'right', fontSize: 11,
        fontFamily: "'Fira Code', monospace", color: 'var(--t2)', flexShrink: 0,
      }}>
        {value}
      </div>
    </div>
  );
}

function Card({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div style={{
      background: 'var(--bg2)',
      border: '1px solid var(--bd)',
      borderRadius: 8,
    }}>
      <div style={{
        padding: '12px 16px',
        borderBottom: '1px solid var(--bd)',
        fontSize: 12, fontWeight: 500, color: 'var(--t1)',
      }}>
        {title}
      </div>
      <div style={{ padding: 16 }}>{children}</div>
    </div>
  );
}

function InfoCell({ label, value, small }: { label: string; value: string; small?: boolean }) {
  return (
    <div>
      <div style={{ fontSize: 11, color: 'var(--t3)', marginBottom: 4 }}>{label}</div>
      <div style={{
        fontFamily: "'Fira Code', monospace",
        fontSize: small ? 10 : 11,
        color: 'var(--t2)',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        whiteSpace: 'nowrap',
      }}>
        {value}
      </div>
    </div>
  );
}
