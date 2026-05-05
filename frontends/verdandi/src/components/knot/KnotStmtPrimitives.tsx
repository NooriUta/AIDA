import React, { useState, useCallback } from 'react';
import type { KnotStatement, KnotAtom } from '../../services/lineage';

export const sectionHeaderStyle: React.CSSProperties = {
  fontSize: 10, fontWeight: 500, letterSpacing: '0.08em',
  textTransform: 'uppercase', color: 'var(--t3)',
  padding: '10px 0 6px', borderBottom: '1px solid var(--bd)',
  display: 'flex', alignItems: 'center', gap: 6,
  marginBottom: 8, marginTop: 14,
};

export const pTblTdStyle: React.CSSProperties = {
  padding: '5px 8px', fontSize: 11, color: 'var(--t2)',
  border: '1px solid var(--bd)', verticalAlign: 'top',
};

export function atomColor(a: KnotAtom): string {
  if (a.columnReference) return 'var(--inf)';
  if (a.constant) return 'var(--wrn)';
  if (a.functionCall) return 'var(--suc)';
  return 'var(--t3)';
}

export function atomStatusBg(status: string): string {
  const s = status.toUpperCase();
  switch (s) {
    case 'RESOLVED': case 'ОБРАБОТАНО': return 'color-mix(in srgb, var(--suc) 15%, transparent)';
    case 'RECONSTRUCT_DIRECT': case 'RECONSTRUCT_INVERSE': return 'color-mix(in srgb, var(--wrn) 15%, transparent)';
    case 'UNRESOLVED': case 'PARTIAL': return 'color-mix(in srgb, var(--danger) 15%, transparent)';
    case 'CONSTANT':      return 'color-mix(in srgb, var(--wrn) 12%, transparent)';
    case 'FUNCTION_CALL': return 'color-mix(in srgb, var(--inf) 15%, transparent)';
    default: return 'var(--bg3)';
  }
}

export function atomStatusColor(status: string): string {
  const s = status.toUpperCase();
  switch (s) {
    case 'RESOLVED': case 'ОБРАБОТАНО': return 'var(--suc)';
    case 'RECONSTRUCT_DIRECT': case 'RECONSTRUCT_INVERSE': return 'var(--wrn)';
    case 'UNRESOLVED': case 'PARTIAL': return 'var(--danger)';
    case 'CONSTANT':      return 'var(--wrn)';
    case 'FUNCTION_CALL': return 'var(--inf)';
    default: return 'var(--t3)';
  }
}

export function atomDisplayText(atomText: string): string {
  const tilde = atomText.lastIndexOf('~');
  return tilde >= 0 ? atomText.substring(0, tilde) : atomText;
}

export function SectionHeader({ children, count }: { children: React.ReactNode; count?: number }) {
  return (
    <div style={sectionHeaderStyle}>
      {children}
      {count != null && (
        <span style={{
          padding: '1px 5px', borderRadius: 8, fontSize: 10,
          background: 'var(--bg4, var(--bg3))', color: 'var(--t3)',
          border: '1px solid var(--bd)',
        }}>
          {count}
        </span>
      )}
    </div>
  );
}

export function CollapsibleSectionHeader({ children, count, open, onToggle }: {
  children: React.ReactNode;
  count?: number;
  open: boolean;
  onToggle: () => void;
}) {
  return (
    <div
      onClick={onToggle}
      style={{ ...sectionHeaderStyle, cursor: 'pointer', userSelect: 'none' }}
    >
      <span style={{
        fontSize: 8, display: 'inline-block',
        transition: 'transform 0.15s',
        transform: open ? 'rotate(90deg)' : 'none',
        color: 'var(--acc)',
      }}>
        {'▶'}
      </span>
      {children}
      {count != null && (
        <span style={{
          padding: '1px 5px', borderRadius: 8, fontSize: 10,
          background: 'var(--bg4, var(--bg3))', color: 'var(--t3)',
          border: '1px solid var(--bd)',
        }}>
          {count}
        </span>
      )}
    </div>
  );
}

export function InfoRow({ label, value, mono }: { label: string; value: string; mono?: boolean }) {
  return (
    <tr>
      <th style={{
        padding: '5px 8px', fontSize: 11, color: 'var(--t3)',
        fontWeight: 500, width: 180, verticalAlign: 'top',
        borderBottom: '1px solid var(--bd)', textAlign: 'left',
      }}>
        {label}
      </th>
      <td style={{
        padding: '5px 8px', fontSize: 11, color: 'var(--t2)',
        borderBottom: '1px solid var(--bd)',
        ...(mono ? { fontFamily: 'var(--mono)' } : {}),
      }}>
        {value}
      </td>
    </tr>
  );
}

export function PTblTh({ children, title }: { children: React.ReactNode; title?: string }) {
  return (
    <th title={title} style={{
      padding: '5px 8px', fontSize: 10, fontWeight: 500,
      letterSpacing: '0.05em', textTransform: 'uppercase', color: 'var(--t3)',
      background: 'var(--bg3)', border: '1px solid var(--bd)', textAlign: 'left',
    }}>
      {children}
    </th>
  );
}

export function AtomFlag({ children, bg, color, title }: { children: React.ReactNode; bg: string; color: string; title?: string }) {
  return (
    <span style={{
      display: 'inline-block', padding: '1px 4px', borderRadius: 3,
      fontSize: 9, fontFamily: 'var(--mono)',
      background: bg, color,
    }} title={title}>
      {children}
    </span>
  );
}

export function AtomStat({ label, value, color }: { label: string; value: number; color: string }) {
  return (
    <span>
      <span style={{ color: 'var(--t3)', marginRight: 4 }}>{label}:</span>
      <span style={{ fontFamily: 'var(--mono)', fontWeight: 500, color }}>{value}</span>
    </span>
  );
}

export function AtomsBreakdown({ stmt, t }: { stmt: KnotStatement; t: (k: string) => string }) {
  const other = stmt.atomFuncCall ?? 0;
  return (
    <div style={{
      display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(120px, 1fr))',
      gap: '4px 16px', padding: '6px 0', fontSize: 11,
    }}>
      <AtomStat label={t('knot.stmt.total')} value={stmt.atomTotal} color="var(--t1)" />
      <AtomStat label={t('knot.atoms.resolved')} value={stmt.atomResolved} color="var(--suc)" />
      <AtomStat label={t('knot.atoms.failed')} value={stmt.atomFailed} color="var(--danger)" />
      <AtomStat label={t('knot.atoms.constant')} value={stmt.atomConstant} color="var(--t3)" />
      {other > 0 && <AtomStat label={t('knot.atoms.funcCall')} value={other} color="var(--inf)" />}
    </div>
  );
}

export function SqlBlock({ sql }: { sql: string }) {
  const [copied, setCopied] = useState(false);

  const handleCopy = useCallback(() => {
    navigator.clipboard.writeText(sql).then(() => {
      setCopied(true);
      setTimeout(() => setCopied(false), 1500);
    }).catch(() => {});
  }, [sql]);

  return (
    <div style={{
      position: 'relative',
      background: 'var(--bg0)',
      border: '1px solid var(--bd)',
      borderRadius: 6,
      overflow: 'hidden',
    }}>
      <button
        onClick={handleCopy}
        style={{
          position: 'absolute', top: 6, right: 6,
          padding: '3px 8px', fontSize: 10,
          background: copied ? 'var(--suc)' : 'var(--bg3)',
          border: '1px solid var(--bd)', borderRadius: 4,
          color: copied ? 'var(--bg0)' : 'var(--t3)',
          cursor: 'pointer', fontFamily: 'inherit',
          transition: 'background 0.15s, color 0.15s',
          zIndex: 1,
        }}
      >
        {copied ? 'Copied' : 'Copy'}
      </button>

      <pre style={{
        margin: 0,
        padding: '12px 14px',
        fontSize: 11,
        lineHeight: 1.55,
        fontFamily: 'var(--mono)',
        color: 'var(--t2)',
        overflowX: 'auto',
        overflowY: 'auto',
        maxHeight: 320,
        whiteSpace: 'pre',
        tabSize: 2,
      }}>
        {sql}
      </pre>
    </div>
  );
}
