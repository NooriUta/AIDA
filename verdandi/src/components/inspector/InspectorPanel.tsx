import { memo } from 'react';
import { useTranslation } from 'react-i18next';
import { useLoomStore } from '../../stores/loomStore';
import { InspectorEmpty }     from './InspectorEmpty';
import { InspectorTable }     from './InspectorTable';
import { InspectorStatement } from './InspectorStatement';
import { InspectorSchema }    from './InspectorSchema';
import { InspectorRoutine }   from './InspectorRoutine';

// ── Header ────────────────────────────────────────────────────────────────────
function InspectorHeader({ label }: { label: string }) {
  return (
    <div style={{
      padding: '7px 10px 6px',
      borderBottom: '1px solid var(--bd)',
      fontSize: '11px', fontWeight: 600,
      color: 'var(--t2)', letterSpacing: '0.04em',
      overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
    }}>
      {label}
    </div>
  );
}

// ── Main panel ─────────────────────────────────────────────────────────────────
export const InspectorPanel = memo(() => {
  const { t } = useTranslation();
  const { selectedNodeId, selectedNodeData } = useLoomStore();

  if (!selectedNodeId || !selectedNodeData) {
    return <InspectorEmpty />;
  }

  const nodeType = selectedNodeData.nodeType;

  // DaliTable
  if (nodeType === 'DaliTable') {
    return (
      <>
        <InspectorHeader label={selectedNodeData.label} />
        <InspectorTable data={selectedNodeData} nodeId={selectedNodeId} />
      </>
    );
  }

  // DaliStatement
  if (nodeType === 'DaliStatement') {
    return (
      <>
        <InspectorHeader label={selectedNodeData.label} />
        <InspectorStatement data={selectedNodeData} nodeId={selectedNodeId} />
      </>
    );
  }

  // DaliSchema / DaliDatabase / DaliApplication / DaliService
  if (
    nodeType === 'DaliSchema'      ||
    nodeType === 'DaliDatabase'    ||
    nodeType === 'DaliApplication' ||
    nodeType === 'DaliService'
  ) {
    return (
      <>
        <InspectorHeader label={selectedNodeData.label} />
        <InspectorSchema data={selectedNodeData} nodeId={selectedNodeId} />
      </>
    );
  }

  // DaliRoutine / DaliPackage
  if (nodeType === 'DaliRoutine' || nodeType === 'DaliPackage') {
    return (
      <>
        <InspectorHeader label={selectedNodeData.label} />
        <InspectorRoutine data={selectedNodeData} nodeId={selectedNodeId} />
      </>
    );
  }

  // Fallback for other types (DaliColumn, DaliAtom, etc.)
  return (
    <>
      <InspectorHeader label={selectedNodeData.label} />
      <div style={{ padding: '8px 10px' }}>
        <div style={{ fontSize: '11px', color: 'var(--t3)' }}>
          {t('inspector.type')}: <span style={{ color: 'var(--t1)' }}>{nodeType}</span>
        </div>
        <div style={{ fontSize: '10px', color: 'var(--t3)', marginTop: 4, fontFamily: 'monospace' }}>
          {selectedNodeId}
        </div>
      </div>
    </>
  );
});

InspectorPanel.displayName = 'InspectorPanel';
