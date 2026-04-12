import { memo, useState } from 'react';
import { useTranslation } from 'react-i18next';

type GraphPrefs = {
  autoLayout:       boolean;
  drillAnimation:   boolean;
  hoverHighlight:   boolean;
  showEdgeLabels:   boolean;
  colLevelDefault:  boolean;
  startLevel:       string;
  nodeLimit:        string;
};

function loadPrefs(): GraphPrefs {
  try {
    const raw = localStorage.getItem('seer-graph-prefs');
    return raw ? { ...DEFAULT_PREFS, ...JSON.parse(raw) } : DEFAULT_PREFS;
  } catch { return DEFAULT_PREFS; }
}

const DEFAULT_PREFS: GraphPrefs = {
  autoLayout:       true,
  drillAnimation:   true,
  hoverHighlight:   true,
  showEdgeLabels:   false,
  colLevelDefault:  false,
  startLevel:       'L2',
  nodeLimit:        '400',
};

export const ProfileTabGraph = memo(() => {
  const { t } = useTranslation();
  const [prefs, setPrefs] = useState<GraphPrefs>(loadPrefs);
  const [saved, setSaved] = useState(false);

  function toggle(key: keyof GraphPrefs) {
    setPrefs((p) => ({ ...p, [key]: !p[key] }));
  }

  function handleSave() {
    localStorage.setItem('seer-graph-prefs', JSON.stringify(prefs));
    setSaved(true);
    setTimeout(() => setSaved(false), 2000);
  }

  function handleReset() {
    setPrefs(DEFAULT_PREFS);
    localStorage.removeItem('seer-graph-prefs');
  }

  const SETTINGS = [
    { key: 'autoLayout',      name: t('profile.graph.autoLayout'),      desc: t('profile.graph.autoLayoutDesc') },
    { key: 'drillAnimation',  name: t('profile.graph.drillAnimation'),  desc: t('profile.graph.drillAnimationDesc') },
    { key: 'hoverHighlight',  name: t('profile.graph.hoverHighlight'),  desc: t('profile.graph.hoverHighlightDesc') },
    { key: 'showEdgeLabels',  name: t('profile.graph.showEdgeLabels'),  desc: t('profile.graph.showEdgeLabelsDesc') },
    { key: 'colLevelDefault', name: t('profile.graph.colLevelDefault'), desc: t('profile.graph.colLevelDefaultDesc') },
  ] as const;

  return (
    <div>
      <div style={{ fontSize: '14px', fontWeight: 600, color: 'var(--t1)', marginBottom: '18px', paddingBottom: '10px', borderBottom: '1px solid var(--bd)' }}>
        {t('profile.tabs.graph')}
      </div>

      {SETTINGS.map(({ key, name, desc }) => (
        <div key={key} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '12px 0', borderBottom: '1px solid var(--bd)' }}>
          <div style={{ flex: 1 }}>
            <div style={{ fontSize: '12px', fontWeight: 500, color: 'var(--t1)', marginBottom: '2px' }}>{name}</div>
            <div style={{ fontSize: '11px', color: 'var(--t3)' }}>{desc}</div>
          </div>
          <Toggle on={prefs[key] as boolean} onClick={() => toggle(key)} />
        </div>
      ))}

      {/* Start level */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '12px 0', borderBottom: '1px solid var(--bd)' }}>
        <div style={{ flex: 1 }}>
          <div style={{ fontSize: '12px', fontWeight: 500, color: 'var(--t1)', marginBottom: '2px' }}>{t('profile.graph.startLevel')}</div>
          <div style={{ fontSize: '11px', color: 'var(--t3)' }}>{t('profile.graph.startLevelDesc')}</div>
        </div>
        <select
          value={prefs.startLevel}
          onChange={(e) => setPrefs((p) => ({ ...p, startLevel: e.target.value }))}
          style={{ padding: '5px 8px', background: 'var(--bg2)', border: '1px solid var(--bd)', borderRadius: 'var(--seer-radius-sm)', color: 'var(--t1)', fontSize: '12px', fontFamily: 'inherit', cursor: 'pointer', outline: 'none' }}
        >
          <option value="L1">L1 — {t('profile.graph.levelSchema')}</option>
          <option value="L2">L2 — {t('profile.graph.levelTables')}</option>
          <option value="L3">L3 — {t('profile.graph.levelColumns')}</option>
        </select>
      </div>

      {/* Node limit */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '12px 0', borderBottom: '1px solid var(--bd)' }}>
        <div style={{ flex: 1 }}>
          <div style={{ fontSize: '12px', fontWeight: 500, color: 'var(--t1)', marginBottom: '2px' }}>{t('profile.graph.nodeLimit')}</div>
          <div style={{ fontSize: '11px', color: 'var(--t3)' }}>{t('profile.graph.nodeLimitDesc')}</div>
        </div>
        <select
          value={prefs.nodeLimit}
          onChange={(e) => setPrefs((p) => ({ ...p, nodeLimit: e.target.value }))}
          style={{ padding: '5px 8px', background: 'var(--bg2)', border: '1px solid var(--bd)', borderRadius: 'var(--seer-radius-sm)', color: 'var(--t1)', fontSize: '12px', fontFamily: 'inherit', cursor: 'pointer', outline: 'none' }}
        >
          {['100', '200', '400', '∞'].map((v) => <option key={v} value={v}>{v}</option>)}
        </select>
      </div>

      <div style={{ display: 'flex', gap: '10px', marginTop: '20px' }}>
        <button onClick={handleSave} style={btnPrimary}>{saved ? `✓ ${t('profile.saved')}` : t('profile.save')}</button>
        <button onClick={handleReset} style={btnSecondary}>{t('profile.graph.reset')}</button>
      </div>
    </div>
  );
});

ProfileTabGraph.displayName = 'ProfileTabGraph';

function Toggle({ on, onClick }: { on: boolean; onClick: () => void }) {
  return (
    <div
      onClick={onClick}
      style={{
        width: '36px', height: '20px', borderRadius: '10px', flexShrink: 0,
        background: on ? 'var(--acc)' : 'var(--bg3)',
        border: `1px solid ${on ? 'var(--acc)' : 'var(--bd)'}`,
        position: 'relative', cursor: 'pointer',
        transition: 'background 0.15s, border-color 0.15s',
      }}
    >
      <div style={{
        width: '14px', height: '14px', borderRadius: '50%',
        background: on ? 'var(--bg0)' : 'var(--t3)',
        position: 'absolute', top: '2px', left: '2px',
        transform: on ? 'translateX(16px)' : 'translateX(0)',
        transition: 'transform 0.15s, background 0.15s',
      }} />
    </div>
  );
}

const btnPrimary:   React.CSSProperties = { display: 'inline-flex', alignItems: 'center', padding: '7px 14px', borderRadius: 'var(--seer-radius-md)', fontSize: '12px', fontWeight: 500, fontFamily: 'inherit', cursor: 'pointer', border: '1px solid var(--acc)', background: 'var(--acc)', color: 'var(--bg0)' };
const btnSecondary: React.CSSProperties = { display: 'inline-flex', alignItems: 'center', padding: '7px 14px', borderRadius: 'var(--seer-radius-md)', fontSize: '12px', fontWeight: 500, fontFamily: 'inherit', cursor: 'pointer', border: '1px solid var(--bd)', background: 'transparent', color: 'var(--t2)' };
