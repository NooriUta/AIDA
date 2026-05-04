import { memo, useCallback } from 'react';
import { ChevronRight, LayoutGrid } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useLoomStore } from '../../stores/loomStore';
import { useIsMobile } from '../../hooks/useIsMobile';
import { useHeimdallEmitter } from '../../hooks/useHeimdallEmitter';

export const Breadcrumb = memo(() => {
  const {
    navigationStack,
    viewLevel,
    currentScope,
    currentScopeLabel,
    l1ScopeStack,
    navigateBack,
    navigateToLevel,
    popL1ScopeToIndex,
    clearL1Scope,
  } = useLoomStore();
  const { t } = useTranslation();
  const { emit: emitHeimdall } = useHeimdallEmitter();

  const isMobile = useIsMobile();
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
      background: 'color-mix(in srgb, var(--bg1) 92%, transparent)',
      backdropFilter: 'blur(8px)',
      border: '1px solid var(--bd)',
      borderRadius: 'var(--seer-radius-md)',
      fontSize: isMobile ? '11px' : '12px',
      maxWidth: isMobile ? 'calc(100vw - 32px)' : '640px',
      flexWrap: 'wrap',
      overflow: 'hidden',
    }}>

      {/* ── Root: Overview ────────────────────────────────────────────────── */}
      <BreadcrumbSegment
        label={t('breadcrumb.overview')}
        icon={<LayoutGrid size={12} />}
        onClick={() => {
          emitHeimdall('LOOM_BREADCRUMB_NAV', 'INFO', {
            target: 'overview', fromLevel: viewLevel,
          });
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
              onClick={isLast ? undefined : () => {
                emitHeimdall('LOOM_BREADCRUMB_NAV', 'INFO', {
                  target: 'l1Scope', scopeLabel: item.label, scopeIndex: idx,
                  fromLevel: viewLevel,
                });
                popL1ScopeToIndex(idx + 1);
              }}
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
            onClick={() => {
              emitHeimdall('LOOM_BREADCRUMB_NAV', 'INFO', {
                target: 'navStack', stackLabel: item.label, stackLevel: item.level,
                stackIndex: idx, fromLevel: viewLevel,
              });
              navigateBack(idx);
            }}
            isCurrent={false}
          />
        </span>
      ))}

      {/* ── Current L2/L3 scope (not clickable) ──────────────────────────── */}
      {viewLevel !== 'L1' && (navigationStack.length > 0 || currentScope) && (
        <>
          <ChevronRight size={11} color="var(--t3)" style={{ flexShrink: 0 }} />
          <BreadcrumbSegment
            label={currentScopeLabel || scopeFallbackLabel(viewLevel, currentScope)}
            isCurrent
          />
        </>
      )}
    </div>
  );
});

Breadcrumb.displayName = 'Breadcrumb';

// ── Helpers ───────────────────────────────────────────────────────────────────

/** Fallback label when store.currentScopeLabel is not set (e.g. jumpTo paths). */
function scopeFallbackLabel(viewLevel: string, scope: string | null): string {
  if (!scope) return viewLevel;
  // 'schema-ODS_BFR' → 'ODS_BFR', 'schema-ODS_BFR|ODS' → 'ODS_BFR'
  const dash = scope.indexOf('-');
  if (dash > 0) {
    const nameRaw = scope.slice(dash + 1);
    const pipe = nameRaw.indexOf('|');
    return pipe >= 0 ? nameRaw.slice(0, pipe) : nameRaw;
  }
  return scope;
}

interface SegmentProps {
  label: string;
  icon?: React.ReactNode;
  onClick?: () => void;
  isCurrent: boolean;
}

function BreadcrumbSegment({ label, icon, onClick, isCurrent }: SegmentProps) {
  const isClickable = !isCurrent && !!onClick;
  return (
    <span
      onClick={isCurrent ? undefined : onClick}
      style={{
        display:        'inline-flex',
        alignItems:     'center',
        gap:            '4px',
        padding:        '1px 4px',
        borderRadius:   '4px',
        cursor:         isClickable ? 'pointer' : 'default',
        color:          isCurrent ? 'var(--t1)' : 'var(--acc)',
        fontWeight:     isCurrent ? 500 : 400,
        textDecoration: isClickable ? 'underline' : 'none',
        textUnderlineOffset: '2px',
        transition:     'background 0.1s',
      }}
      onMouseEnter={(e) => {
        if (isClickable) (e.currentTarget as HTMLElement).style.background = 'color-mix(in srgb, var(--acc) 12%, transparent)';
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
