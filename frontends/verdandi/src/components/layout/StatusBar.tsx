import { memo } from 'react';
import { useTranslation } from 'react-i18next';
import { useLoomStore } from '../../stores/loomStore';
import { useAuthStore } from '../../stores/authStore';
import { useIsMobile } from '../../hooks/useIsMobile';

export const StatusBar = memo(() => {
  const { viewLevel, nodeCount, edgeCount, zoom, graphTruncated } = useLoomStore();
  const user = useAuthStore((s) => s.user);
  const { t } = useTranslation();
  const isMobile = useIsMobile();

  const zoomPct = Math.round(zoom * 100);

  const levelColour =
    viewLevel === 'L1' ? 'var(--t3)' :
    viewLevel === 'L2' ? 'var(--inf)' :
    'var(--acc)';

  return (
    <footer style={{
      height: '28px',
      background: 'var(--bg0)',
      borderTop: '1px solid var(--bd)',
      display: 'flex',
      alignItems: 'center',
      padding: '0 var(--seer-space-3)',
      flexShrink: 0,
      fontSize: '11px',
      color: 'var(--t3)',
      userSelect: 'none',
      letterSpacing: '0.02em',
    }}>
      <Chip colour={levelColour}>{viewLevel}</Chip>
      <Divider />
      {isMobile
        ? <span title={t('canvas.nodes')}>{nodeCount}</span>
        : <span>{nodeCount} {t('canvas.nodes')}</span>}
      <Divider />
      {isMobile
        ? <span title={t('canvas.edges')}>{edgeCount}</span>
        : <span>{edgeCount} {t('canvas.edges')}</span>}
      <Divider />
      {isMobile
        ? <span title={t('canvas.zoom')}>{zoomPct}%</span>
        : <span>{t('canvas.zoom')}: {zoomPct}%</span>}

      {/* Truncation warning */}
      {graphTruncated && (
        <>
          <Divider />
          <span style={{
            display: 'inline-flex', alignItems: 'center', gap: 4,
            color: 'var(--wrn)',
            fontSize: '10px', fontWeight: 500,
            letterSpacing: '0.03em',
          }}
            title={t('canvas.hasMore', { count: nodeCount })}
          >
            <span style={{ fontSize: '11px', lineHeight: 1 }}>⚠</span>
            {!isMobile && t('canvas.hasMore', { count: nodeCount })}
          </span>
        </>
      )}

      <div style={{ flex: 1 }} />
      {user && !isMobile && <span>{user.role}</span>}
      {!isMobile && <Divider />}
      {!isMobile && <span style={{ color: 'var(--bd)', letterSpacing: '0.07em' }}>Verðanði · Loom</span>}
    </footer>
  );
});

StatusBar.displayName = 'StatusBar';

function Chip({ colour, children }: { colour: string; children: React.ReactNode }) {
  return (
    <span style={{
      display: 'inline-flex',
      alignItems: 'center',
      padding: '1px 6px',
      borderRadius: 'var(--seer-radius-sm)',
      fontSize: '10px',
      fontWeight: 500,
      letterSpacing: '0.08em',
      background: `color-mix(in srgb, ${colour} 15%, transparent)`,
      color: colour,
      border: `1px solid color-mix(in srgb, ${colour} 35%, transparent)`,
    }}>
      {children}
    </span>
  );
}

function Divider() {
  return (
    <span style={{
      display: 'inline-block',
      width: '1px',
      height: '11px',
      background: 'var(--bd)',
      margin: '0 10px',
      flexShrink: 0,
    }} />
  );
}
