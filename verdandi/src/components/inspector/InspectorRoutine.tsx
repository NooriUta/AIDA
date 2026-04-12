import { memo } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import type { DaliNodeData } from '../../types/domain';
import { InspectorSection, InspectorRow } from './InspectorSection';

interface Props { data: DaliNodeData; nodeId: string }

// ── Routine kind badge (PROCEDURE / FUNCTION / PACKAGE) ──────────────────────
const KIND_COLORS: Record<string, string> = {
  PROCEDURE: '#D4922A',
  FUNCTION:  '#88B8A8',
  PACKAGE:   '#A8B860',
};

function KindBadge({ kind }: { kind: string }) {
  const color = KIND_COLORS[kind.toUpperCase()] ?? 'var(--t3)';
  return (
    <span style={{
      fontSize: '10px', fontWeight: 700, letterSpacing: '0.06em',
      padding: '1px 6px', borderRadius: 4,
      background: `color-mix(in srgb, ${color} 18%, transparent)`,
      color, border: `1px solid color-mix(in srgb, ${color} 40%, transparent)`,
    }}>
      {kind.toUpperCase()}
    </span>
  );
}

// ── Parameter row ────────────────────────────────────────────────────────────
interface ParamEntry { name: string; dataType?: string; direction?: string }

function ParamRow({ param }: { param: ParamEntry }) {
  return (
    <div style={{
      display: 'flex', alignItems: 'center',
      padding: '3px 10px', borderTop: '1px solid var(--bd)',
      fontSize: '11px', gap: 6,
    }}>
      <span style={{
        flex: 1, color: 'var(--t1)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
        fontFamily: 'var(--mono)',
      }}>
        {param.name}
      </span>
      {param.dataType && (
        <span style={{ color: 'var(--t3)', fontSize: '10px', flexShrink: 0 }}>{param.dataType}</span>
      )}
      {param.direction && (
        <span style={{
          fontSize: '9px', fontWeight: 600, letterSpacing: '0.05em',
          padding: '0 4px', borderRadius: 3,
          background: 'var(--bg3)', color: 'var(--t3)',
        }}>
          {param.direction}
        </span>
      )}
    </div>
  );
}

export const InspectorRoutine = memo(({ data, nodeId }: Props) => {
  const { t } = useTranslation();
  const navigate = useNavigate();

  const isPackage = data.nodeType === 'DaliPackage';
  const routineKind = typeof data.metadata?.routineKind === 'string'
    ? data.metadata.routineKind
    : (isPackage ? 'PACKAGE' : '');
  const parameters = Array.isArray(data.metadata?.parameters)
    ? (data.metadata.parameters as ParamEntry[])
    : [];
  const routinesCount = typeof data.routinesCount === 'number' ? data.routinesCount : null;

  const openInKnot = () => {
    const params = new URLSearchParams();
    if (isPackage) {
      params.set('pkg', data.label);
    } else {
      // Try to extract package name from metadata
      const pkgName = typeof data.metadata?.packageName === 'string'
        ? data.metadata.packageName
        : undefined;
      if (pkgName) params.set('pkg', pkgName);
    }
    navigate(`/knot?${params.toString()}`);
  };

  return (
    <>
      <InspectorSection title={t('inspector.properties')}>
        <InspectorRow label={t('inspector.label')}   value={data.label} />
        <InspectorRow
          label={t('inspector.type')}
          value={routineKind ? <KindBadge kind={routineKind} /> : data.nodeType}
        />
        {data.language && <InspectorRow label={t('inspector.language')} value={data.language} />}
        {data.schema && <InspectorRow label={t('inspector.schema')} value={data.schema} />}
        {routinesCount !== null && (
          <InspectorRow label={t('inspector.routines')} value={String(routinesCount)} />
        )}
        <InspectorRow label={t('inspector.id')} value={nodeId} />
        <div style={{ padding: '6px 10px 4px' }}>
          <button
            onClick={openInKnot}
            style={{
              display: 'inline-flex', alignItems: 'center', gap: 5,
              padding: '4px 10px',
              fontSize: 11, fontWeight: 500, fontFamily: 'inherit',
              background: 'var(--bg3)',
              border: '1px solid var(--bd)',
              borderRadius: 4,
              color: 'var(--acc)',
              cursor: 'pointer',
              transition: 'border-color 0.1s',
            }}
            onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.borderColor = 'var(--acc)'; }}
            onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.borderColor = 'var(--bd)'; }}
          >
            ◈ {t('inspector.openInKnot')}
          </button>
        </div>
      </InspectorSection>

      {/* Parameters section — only for routines with parameter metadata */}
      {!isPackage && (
        <InspectorSection
          title={`${t('inspector.parameters')} (${parameters.length})`}
          defaultOpen={parameters.length > 0}
        >
          {parameters.length === 0 ? (
            <div style={{ padding: '4px 10px', fontSize: '11px', color: 'var(--t3)' }}>
              {t('inspector.noParameters')}
            </div>
          ) : (
            <div style={{ marginTop: 2 }}>
              {parameters.map((p, i) => <ParamRow key={p.name || i} param={p} />)}
            </div>
          )}
        </InspectorSection>
      )}
    </>
  );
});

InspectorRoutine.displayName = 'InspectorRoutine';
