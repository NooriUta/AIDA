import { memo, useCallback, useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useReactFlow, getNodesBounds, getViewportForBounds } from '@xyflow/react';
import { Download, FileJson, ImageDown, Loader2 } from 'lucide-react';
import { useLoomStore } from '../../stores/loomStore';

interface Props {
  /** Ref to the outermost canvas wrapper — used to locate .react-flow__viewport */
  containerRef: React.RefObject<HTMLDivElement | null>;
}

const SPIN: React.CSSProperties = { animation: 'spin 0.8s linear infinite' };

export const ExportPanel = memo(({ containerRef }: Props) => {
  const { t } = useTranslation();
  const { getNodes, getEdges, getViewport } = useReactFlow();
  const viewLevel = useLoomStore((s) => s.viewLevel);

  const [open,      setOpen]      = useState(false);
  const [exporting, setExporting] = useState<'json' | 'png' | 'svg' | null>(null);
  const panelRef = useRef<HTMLDivElement>(null);

  // ── Close dropdown on outside click ──────────────────────────────────────
  useEffect(() => {
    if (!open) return;
    const handler = (e: MouseEvent) => {
      if (panelRef.current && !panelRef.current.contains(e.target as Node)) {
        setOpen(false);
      }
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, [open]);

  // ── Locate .react-flow__viewport inside the container ────────────────────
  const getViewportEl = useCallback((): HTMLElement | null =>
    (containerRef.current?.querySelector('.react-flow__viewport') as HTMLElement | null),
  [containerRef]);

  // ── JSON export ───────────────────────────────────────────────────────────
  const exportJson = useCallback(() => {
    setExporting('json');
    try {
      const payload = {
        version: 1,
        exportedAt: new Date().toISOString(),
        nodes: getNodes().map(({ id, type, position, data }) => ({ id, type, position, data })),
        edges: getEdges().map(({ id, source, target, data }) => ({ id, source, target, data })),
        viewport: getViewport(),
      };
      const blob = new Blob([JSON.stringify(payload, null, 2)], { type: 'application/json' });
      const url  = URL.createObjectURL(blob);
      const a    = document.createElement('a');
      a.href     = url;
      a.download = `seer-loom-${viewLevel.toLowerCase()}-${Date.now()}.json`;
      a.click();
      URL.revokeObjectURL(url);
    } finally {
      setExporting(null);
      setOpen(false);
    }
  }, [getNodes, getEdges, getViewport]);

  // ── PNG export via html-to-image (dynamic import) ─────────────────────────
  const exportPng = useCallback(async () => {
    const el = getViewportEl();
    if (!el) return;
    setExporting('png');
    try {
      const { toPng } = await import('html-to-image');
      const nodes  = getNodes();
      if (nodes.length === 0) return;
      const bounds = getNodesBounds(nodes);
      const pad    = 60;
      const imgW   = Math.max(1280, bounds.width  + pad * 2);
      const imgH   = Math.max(800,  bounds.height + pad * 2);
      const vp = getViewportForBounds(bounds, imgW, imgH, 0.1, 3, 0.04);
      const url = await toPng(el, {
        width: imgW, height: imgH,
        style: {
          width:     `${imgW}px`,
          height:    `${imgH}px`,
          transform: `translate(${vp.x}px, ${vp.y}px) scale(${vp.zoom})`,
        },
      });
      const a    = document.createElement('a');
      a.href     = url;
      a.download = `seer-loom-${viewLevel.toLowerCase()}-${Date.now()}.png`;
      a.click();
    } catch (err) {
      console.error('[LOOM] PNG export failed', err);
    } finally {
      setExporting(null);
      setOpen(false);
    }
  }, [getViewportEl, getNodes]);

  // ── SVG export via html-to-image ──────────────────────────────────────────
  const exportSvg = useCallback(async () => {
    const el = getViewportEl();
    if (!el) return;
    setExporting('svg');
    try {
      const { toSvg } = await import('html-to-image');
      const nodes  = getNodes();
      if (nodes.length === 0) return;
      const bounds = getNodesBounds(nodes);
      const pad    = 60;
      const imgW   = Math.max(1280, bounds.width  + pad * 2);
      const imgH   = Math.max(800,  bounds.height + pad * 2);
      const vp = getViewportForBounds(bounds, imgW, imgH, 0.1, 3, 0.04);
      const url = await toSvg(el, {
        width: imgW, height: imgH,
        style: {
          width:     `${imgW}px`,
          height:    `${imgH}px`,
          transform: `translate(${vp.x}px, ${vp.y}px) scale(${vp.zoom})`,
        },
      });
      const a    = document.createElement('a');
      a.href     = url;
      a.download = `seer-loom-${viewLevel.toLowerCase()}-${Date.now()}.svg`;
      a.click();
    } catch (err) {
      console.error('[LOOM] SVG export failed', err);
    } finally {
      setExporting(null);
      setOpen(false);
    }
  }, [getViewportEl, getNodes]);

  // ── Button styles ─────────────────────────────────────────────────────────
  const dropItem: React.CSSProperties = {
    display: 'flex', alignItems: 'center', gap: 7,
    width: '100%', padding: '6px 12px',
    background: 'transparent', border: 'none',
    color: 'var(--t1)', fontSize: '12px', cursor: 'pointer',
    transition: 'background 0.08s', textAlign: 'left',
  };

  const hoverOn  = (e: React.MouseEvent) => { (e.currentTarget as HTMLElement).style.background = 'var(--bg1)'; };
  const hoverOff = (e: React.MouseEvent) => { (e.currentTarget as HTMLElement).style.background = 'transparent'; };

  return (
    <div ref={panelRef} style={{ position: 'relative' }}>
      {/* ── Trigger button ─────────────────────────────────────────────── */}
      <button
        title={t('export.title')}
        onClick={() => setOpen((o) => !o)}
        style={{
          display: 'flex', alignItems: 'center', gap: 5,
          padding: '5px 8px',
          background: open ? 'var(--acc)' : 'var(--bg2)',
          border: '1px solid var(--bd)',
          borderRadius: 'var(--seer-radius-sm, 4px)',
          color: open ? 'var(--bg0)' : 'var(--t2, var(--t1))',
          fontSize: '11px', fontWeight: 500, letterSpacing: '0.04em',
          cursor: 'pointer',
          boxShadow: '0 1px 4px rgba(0,0,0,0.28)',
          transition: 'background 0.12s, color 0.12s',
        }}
      >
        <Download size={13} />
        {t('export.title')}
      </button>

      {/* ── Dropdown ───────────────────────────────────────────────────── */}
      {open && (
        <div style={{
          position: 'absolute', top: 'calc(100% + 5px)', right: 0,
          width: 158,
          background: 'var(--bg2)', border: '1px solid var(--bd)',
          borderRadius: 'var(--seer-radius-md, 6px)',
          boxShadow: '0 6px 22px rgba(0,0,0,0.42)',
          overflow: 'hidden', zIndex: 300,
          padding: '3px 0',
        }}>

          {/* JSON */}
          <button
            style={dropItem}
            disabled={!!exporting}
            onClick={exportJson}
            onMouseEnter={hoverOn}
            onMouseLeave={hoverOff}
          >
            {exporting === 'json'
              ? <Loader2 size={13} style={SPIN} />
              : <FileJson size={13} style={{ color: 'var(--acc)' }} />}
            {t('export.json')}
          </button>

          {/* PNG */}
          <button
            style={dropItem}
            disabled={!!exporting}
            onClick={exportPng}
            onMouseEnter={hoverOn}
            onMouseLeave={hoverOff}
          >
            {exporting === 'png'
              ? <Loader2 size={13} style={SPIN} />
              : <ImageDown size={13} style={{ color: 'var(--inf)' }} />}
            {t('export.png')}
          </button>

          {/* SVG */}
          <button
            style={dropItem}
            disabled={!!exporting}
            onClick={exportSvg}
            onMouseEnter={hoverOn}
            onMouseLeave={hoverOff}
          >
            {exporting === 'svg'
              ? <Loader2 size={13} style={SPIN} />
              : <ImageDown size={13} style={{ color: 'var(--wrn)' }} />}
            {t('export.svg')}
          </button>
        </div>
      )}
    </div>
  );
});

ExportPanel.displayName = 'ExportPanel';
