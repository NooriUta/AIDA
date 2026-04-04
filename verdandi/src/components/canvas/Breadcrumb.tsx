import { memo } from 'react';
import { ChevronRight, LayoutGrid } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useLoomStore } from '../../stores/loomStore';

export const Breadcrumb = memo(() => {
  const {
    navigationStack,
    viewLevel,
    currentScope,
    l1ScopeStack,
    navigateBack,
    navigateToLevel,
    popL1ScopeToIndex,
    clearL1Scope,
  } = useLoomStore();
  const { t } = useTranslation();

  const hasL1Scope  = viewLevel === 'L1' && l1ScopeStack.length > 0;
  const hasL2L3Path = navigationStack.length > 0 || (viewLevel !== 'L1' && currentScope);

  // On pure L1 with no scope — hide breadcrumb entirely
  if (!hasL1Scope && !hasL2L3Path && viewLevel === 'L1') return null;

  return (
    <div style={{
      position: 'absolute',
      top: 'var(--seer-space-3)',
      left: 'var(--seer-space-3)',
      zIndex: 50,
      display: 'flex',
      alignItems: 'center',
      gap: '2px',
      padding: '4px var(--seer-space-3)',
      background: 'rgba(20,17,8,0.88)',
      backdropFilter: 'blur(8px)',
      border: '1px solid var(--bd)',
      borderRadius: 'var(--seer-radius-md)',
      fontSize: '12px',
      maxWidth: '640px',
      flexWrap: 'wrap',
    }}>

      {/* ── Root: Overview ────────────────────────────────────────────────── */}
      <BreadcrumbSegment
        label={t('breadcrumb.overview')}
        icon={<LayoutGrid size={12} />}
        onClick={() => {
          clearL1Scope();
          navigateToLevel('L1');
        }}
        isCurrent={viewLevel === 'L1' && !hasL1Scope}
      />

      {/* ── L1 scope stack (Application › Service) ────────────────────────── */}
      {hasL1Scope && l1ScopeStack.map((item, idx) => {
        const isLast = idx === l1ScopeStack.length - 1;
        return (
          <span key={`l1-${item.nodeId}`} style={{ display: 'contents' }}>
            <ChevronRight size={11} color="var(--t3)" style={{ flexShrink: 0 }} />
            <BreadcrumbSegment
              label={item.label}
              onClick={isLast ? undefined : () => popL1ScopeToIndex(idx + 1)}
              isCurrent={isLast}
            />
          </span>
        );
      })}

      {/* ── L2/L3 navigation stack ────────────────────────────────────────── */}
      {navigationStack.map((item, idx) => (
        <span key={`nav-${item.level}-${idx}`} style={{ display: 'contents' }}>
          <ChevronRight size={11} color="var(--t3)" style={{ flexShrink: 0 }} />
          <BreadcrumbSegment
            label={item.label}
            onClick={() => navigateBack(idx)}
            isCurrent={false}
          />
        </span>
      ))}

      {/* ── Current L2/L3 scope (not clickable) ──────────────────────────── */}
      {viewLevel !== 'L1' && (navigationStack.length > 0 || currentScope) && (
        <>
          <ChevronRight size={11} color="var(--t3)" style={{ flexShrink: 0 }} />
          <BreadcrumbSegment
            label={currentScopeLabel(viewLevel, currentScope)}
            isCurrent
          />
        </>
      )}
    </div>
  );
});

Breadcrumb.displayName = 'Breadcrumb';

// ── Helpers ───────────────────────────────────────────────────────────────────

function currentScopeLabel(viewLevel: string, scope: string | null): string {
  if (!scope) return viewLevel;
  const parts = scope.split('-');
  return parts.slice(1).join('-') || scope;
}

interface SegmentProps {
  label: string;
  icon?: React.ReactNode;
  onClick?: () => void;
  isCurrent: boolean;
}

function BreadcrumbSegment({ label, icon, onClick, isCurrent }: SegmentProps) {
  return (
    <span
      onClick={isCurrent ? undefined : onClick}
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        gap: '4px',
        padding: '1px 4px',
        borderRadius: '4px',
        cursor: isCurrent || !onClick ? 'default' : 'pointer',
        color: isCurrent ? 'var(--t1)' : 'var(--acc)',
        fontWeight: isCurrent ? 500 : 400,
        transition: 'background 0.1s',
      }}
      onMouseEnter={(e) => {
        if (!isCurrent && onClick) (e.currentTarget as HTMLElement).style.background = 'var(--bg3)';
      }}
      onMouseLeave={(e) => {
        (e.currentTarget as HTMLElement).style.background = 'transparent';
      }}
    >
      {icon}
      {label}
    </span>
  );
}
