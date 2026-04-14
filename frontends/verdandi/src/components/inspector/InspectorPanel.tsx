import { memo } from 'react';
import { useTranslation } from 'react-i18next';
import { useLoomStore } from '../../stores/loomStore';
import { InspectorEmpty }     from './InspectorEmpty';
import { InspectorTable }     from './InspectorTable';
import { InspectorStatement } from './InspectorStatement';
import { InspectorSchema }    from './InspectorSchema';
import { InspectorRoutine }   from './InspectorRoutine';
import { InspectorColumn }    from './InspectorColumn';
import { InspectorJoin }      from './InspectorJoin';
import { InspectorParameter } from './InspectorParameter';

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

  // Wrap content with ARIA complementary role
  const wrapPanel = (content: React.ReactNode) => (
    <div role="complementary" aria-label={t('panel.inspector')}>
      {content}
    </div>
  );

  // DaliTable — InspectorTable renders its own rich header (schema + table
  // name + type badge + column count), mirroring the canvas TableNode.
  if (nodeType === 'DaliTable') {
    return wrapPanel(
      <InspectorTable data={selectedNodeData} nodeId={selectedNodeId} />
    );
  }

  // DaliStatement — no InspectorHeader; InspectorStatement renders its own
  // rich header that mirrors the canvas StatementNode card (icon, groupPath,
  // title, op-badge, column-count subline).
  if (nodeType === 'DaliStatement') {
    return wrapPanel(
      <InspectorStatement data={selectedNodeData} nodeId={selectedNodeId} />
    );
  }

  // DaliSchema / DaliDatabase / DaliApplication / DaliService
  if (
    nodeType === 'DaliSchema'      ||
    nodeType === 'DaliDatabase'    ||
    nodeType === 'DaliApplication' ||
    nodeType === 'DaliService'
  ) {
    return wrapPanel(
      <>
        <InspectorHeader label={selectedNodeData.label} />
        <InspectorSchema data={selectedNodeData} nodeId={selectedNodeId} />
      </>
    );
  }

  // DaliRoutine / DaliPackage
  if (nodeType === 'DaliRoutine' || nodeType === 'DaliPackage') {
    return wrapPanel(
      <>
        <InspectorHeader label={selectedNodeData.label} />
        <InspectorRoutine data={selectedNodeData} nodeId={selectedNodeId} />
      </>
    );
  }

  // DaliColumn / DaliOutputColumn / DaliAtom / DaliAffectedColumn
  if (
    nodeType === 'DaliColumn'       ||
    nodeType === 'DaliOutputColumn' ||
    nodeType === 'DaliAtom'         ||
    nodeType === 'DaliAffectedColumn'
  ) {
    return wrapPanel(
      <>
        <InspectorHeader label={selectedNodeData.label} />
        <InspectorColumn data={selectedNodeData} nodeId={selectedNodeId} />
      </>
    );
  }

  // DaliParameter / DaliVariable
  if (nodeType === 'DaliParameter' || nodeType === 'DaliVariable') {
    return wrapPanel(
      <>
        <InspectorHeader label={selectedNodeData.label} />
        <InspectorParameter data={selectedNodeData} nodeId={selectedNodeId} />
      </>
    );
  }

  // DaliJoin
  if (nodeType === 'DaliJoin') {
    return wrapPanel(
      <>
        <InspectorHeader label={selectedNodeData.label} />
        <InspectorJoin data={selectedNodeData} nodeId={selectedNodeId} />
      </>
    );
  }

  // Fallback for unknown types
  return wrapPanel(
    <>
      <InspectorHeader label={selectedNodeData.label} />
      <div style={{ padding: '8px 10px' }}>
        <div style={{ fontSize: '11px', color: 'var(--t3)' }}>
          {t('inspector.type')}: <span style={{ color: 'var(--t1)' }}>{nodeType}</span>
        </div>
        <div style={{ fontSize: '10px', color: 'var(--t3)', marginTop: 4, fontFamily: 'var(--mono)' }}>
          {selectedNodeId}
        </div>
      </div>
    </>
  );
});

InspectorPanel.displayName = 'InspectorPanel';
