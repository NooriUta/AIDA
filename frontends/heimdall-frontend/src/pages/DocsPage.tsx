import { useState, useEffect, useCallback, useMemo, useRef } from 'react';
import { useParams, useNavigate, Link }                      from 'react-router-dom';
import { usePageTitle }                                      from '../hooks/usePageTitle';
import { marked }                                            from 'marked';
import mermaid                                               from 'mermaid';
import { HEIMDALL_API }                                      from '../api';

// ── marked config — GFM + tables + breaks ─────────────────────────────────────
marked.setOptions({ gfm: true, breaks: false });

// ── mermaid config ────────────────────────────────────────────────────────────
mermaid.initialize({
  startOnLoad: false,
  theme:       'base',
  themeVariables: {
    primaryColor:      '#2d6a4f',
    primaryTextColor:  '#f0f4f0',
    primaryBorderColor:'#40916c',
    lineColor:         '#74c69d',
    background:        '#1a1e1a',
    mainBkg:           '#1e2a1e',
    nodeBorder:        '#40916c',
    clusterBkg:        '#1e2a1e',
    titleColor:        '#b7e4c7',
    edgeLabelBackground: '#1a1e1a',
    fontFamily:        'monospace',
    fontSize:          '13px',
  },
  securityLevel: 'loose',
});

// ── File tree helpers ─────────────────────────────────────────────────────────
function buildTree(paths: string[]): Record<string, string[]> {
  const tree: Record<string, string[]> = { '': [] };
  for (const p of paths) {
    const slash = p.lastIndexOf('/');
    const dir   = slash === -1 ? '' : p.slice(0, slash);
    const file  = slash === -1 ? p  : p.slice(slash + 1);
    if (!tree[dir]) tree[dir] = [];
    tree[dir].push(file);
  }
  return tree;
}

// ── Scoped styles injected once ───────────────────────────────────────────────
const DOCS_CSS = `
.docs-body h1 { font-size: 1.5em; font-weight: 700; margin: 0 0 0.6em; line-height: 1.25; }
.docs-body h2 { font-size: 1.2em; font-weight: 600; margin: 1.6em 0 0.5em; padding-bottom: 4px; border-bottom: 1px solid var(--bd); }
.docs-body h3 { font-size: 1em; font-weight: 600; margin: 1.3em 0 0.4em; }
.docs-body h4 { font-size: 0.9em; font-weight: 600; margin: 1em 0 0.3em; color: var(--t2); }
.docs-body p  { margin: 0 0 0.85em; line-height: 1.65; }
.docs-body ul, .docs-body ol { margin: 0 0 0.85em; padding-left: 1.6em; line-height: 1.65; }
.docs-body li { margin-bottom: 0.25em; }
.docs-body li > ul, .docs-body li > ol { margin-top: 0.25em; margin-bottom: 0; }
.docs-body hr { border: none; border-top: 1px solid var(--bd); margin: 1.4em 0; }
.docs-body strong { font-weight: 600; }
.docs-body em { font-style: italic; }
.docs-body a  { color: var(--acc); text-decoration: underline; text-underline-offset: 2px; }
.docs-body blockquote {
  border-left: 3px solid var(--acc);
  margin: 0 0 0.85em;
  padding: 4px 12px;
  color: var(--t2);
  background: var(--bg1);
  border-radius: 0 4px 4px 0;
}
.docs-body code {
  background: var(--bg2, var(--bg1));
  border: 1px solid var(--bd);
  border-radius: 3px;
  padding: 1px 5px;
  font-size: 0.87em;
  font-family: var(--font-mono, monospace);
}
.docs-body pre {
  background: var(--bg0);
  border: 1px solid var(--bd);
  border-radius: 6px;
  padding: 14px 16px;
  overflow-x: auto;
  margin: 0 0 1em;
  line-height: 1.5;
}
.docs-body pre code {
  background: none;
  border: none;
  padding: 0;
  font-size: 0.85em;
}
.docs-body table {
  border-collapse: collapse;
  width: 100%;
  margin: 0 0 1em;
  font-size: 0.9em;
}
.docs-body th {
  background: var(--bg1);
  border: 1px solid var(--bd);
  padding: 6px 12px;
  text-align: left;
  font-weight: 600;
}
.docs-body td {
  border: 1px solid var(--bd);
  padding: 5px 12px;
}
.docs-body tr:nth-child(even) td { background: var(--bg1); }
`;

function ensureDocsStyles() {
  if (!document.getElementById('docs-page-styles')) {
    const el = document.createElement('style');
    el.id = 'docs-page-styles';
    el.textContent = DOCS_CSS;
    document.head.appendChild(el);
  }
}

// ── Tab button ────────────────────────────────────────────────────────────────
function TabBtn({ active, onClick, children }: { active: boolean; onClick: () => void; children: React.ReactNode }) {
  return (
    <button
      onClick={onClick}
      style={{
        background: 'transparent',
        border: 'none',
        borderBottom: active ? '2px solid var(--acc)' : '2px solid transparent',
        color: active ? 'var(--t1)' : 'var(--t3)',
        cursor: 'pointer',
        padding: '8px 16px',
        fontSize: '12px',
        fontWeight: active ? 600 : 400,
        fontFamily: 'var(--font-mono, monospace)',
        transition: 'color 0.12s, border-color 0.12s',
      }}
    >
      {children}
    </button>
  );
}

// ── DocsPage ──────────────────────────────────────────────────────────────────
export type DocsTab = 'docs' | 'team-docs' | 'team-archive';

interface Props { tab?: DocsTab; }

// Tab → { apiNs, prefix stripped from file paths, base route, sidebar label }
const TAB_META: Record<DocsTab, { apiNs: string; prefix: string; baseRoute: string; label: string }> = {
  'docs':         { apiNs: 'docs',      prefix: '',         baseRoute: '/docs',         label: 'Docs'    },
  'team-docs':    { apiNs: 'team-docs', prefix: 'current/', baseRoute: '/team-docs',    label: 'Current' },
  'team-archive': { apiNs: 'team-docs', prefix: 'archive/', baseRoute: '/team-archive', label: 'Archive' },
};

export default function DocsPage({ tab = 'docs' }: Props) {
  const meta = TAB_META[tab];
  usePageTitle(tab === 'team-archive' ? 'Archive' : tab === 'team-docs' ? 'Team Docs' : 'Docs');
  const { '*': splat } = useParams<{ '*': string }>();
  const navigate        = useNavigate();
  const filePath        = splat ?? '';

  const [pubFiles,  setPubFiles]  = useState<string[]>([]);
  const [teamFiles, setTeamFiles] = useState<string[]>([]);
  const [content,   setContent]   = useState<string | null>(null);
  const [error,     setError]     = useState<string | null>(null);
  const [loading,   setLoading]   = useState(false);

  useEffect(() => { ensureDocsStyles(); }, []);

  useEffect(() => {
    Promise.all([
      fetch(`${HEIMDALL_API}/docs`)
        .then(r => r.ok ? (r.json() as Promise<string[]>) : Promise.resolve([])),
      fetch(`${HEIMDALL_API}/team-docs`)
        .then(r => r.ok ? (r.json() as Promise<string[]>) : Promise.resolve([])),
    ]).then(([pub, team]) => {
      setPubFiles(pub);
      setTeamFiles(team);
    }).catch(() => {});
  }, []);

  useEffect(() => {
    setContent(null);
    setError(null);
  }, [tab]);

  // Files for this tab — team tabs filter & strip their prefix for clean sidebar paths
  const files = useMemo(() => {
    if (tab === 'docs') return pubFiles;
    const pfx = meta.prefix;
    return teamFiles.filter(f => f.startsWith(pfx)).map(f => f.slice(pfx.length));
  }, [tab, pubFiles, teamFiles, meta.prefix]);

  const showTeamTab    = teamFiles.some(f => f.startsWith('current/'));
  const showArchiveTab = teamFiles.some(f => f.startsWith('archive/'));

  // filePath in URL has prefix stripped; reconstruct full path for API
  const loadFile = useCallback((path: string) => {
    setLoading(true);
    setError(null);
    const apiPath = `${meta.apiNs}/${meta.prefix}${path}`;
    fetch(`${HEIMDALL_API}/${apiPath}`)
      .then(r => r.ok ? r.text() : Promise.reject(`HTTP ${r.status}`))
      .then(text => { setContent(text); setLoading(false); })
      .catch(e  => { setError(String(e)); setLoading(false); });
  }, [meta]);

  useEffect(() => {
    if (filePath) loadFile(filePath);
    else setContent(null);
  }, [filePath, loadFile]);

  const html       = useMemo(() => content !== null ? marked.parse(content) as string : '', [content]);
  const contentRef = useRef<HTMLDivElement>(null);
  const tree       = buildTree(files);

  // ── Render mermaid diagrams after HTML is injected into DOM ──────────────────
  useEffect(() => {
    if (!contentRef.current || !html) return;

    const blocks = contentRef.current.querySelectorAll<HTMLElement>('code.language-mermaid');
    if (blocks.length === 0) return;

    blocks.forEach((block, i) => {
      const definition = block.textContent ?? '';
      const id = `mermaid-${Date.now()}-${i}`;
      const wrapper = document.createElement('div');
      wrapper.style.cssText = 'margin:0 0 1em;overflow-x:auto;';

      mermaid.render(id, definition).then(({ svg }) => {
        wrapper.innerHTML = svg;
        block.parentElement?.replaceWith(wrapper);
      }).catch(err => {
        console.warn('[mermaid] render error:', err);
      });
    });
  }, [html]);

  return (
    <div style={{ display: 'flex', height: '100%', flexDirection: 'column', fontSize: '13px' }}>

      {/* ── Tab bar ── */}
      <div style={{
        display: 'flex',
        borderBottom: '1px solid var(--bd)',
        background: 'var(--bg1)',
        padding: '0 12px',
        flexShrink: 0,
      }}>
        <TabBtn active={tab === 'docs'}         onClick={() => navigate('/docs')}>Documentation</TabBtn>
        {showTeamTab    && <TabBtn active={tab === 'team-docs'}    onClick={() => navigate('/team-docs')}>Team</TabBtn>}
        {showArchiveTab && <TabBtn active={tab === 'team-archive'} onClick={() => navigate('/team-archive')}>Archive</TabBtn>}
      </div>

      <div style={{ display: 'flex', flex: 1, overflow: 'hidden' }}>

        {/* ── Sidebar ──────────────────────────────────────────────────────── */}
        <aside style={{
          width: '260px', flexShrink: 0,
          borderRight: '1px solid var(--bd)',
          background: 'var(--bg1)',
          overflowY: 'auto',
          padding: '12px 0',
          fontFamily: 'var(--font-mono, monospace)',
        }}>
          <div style={{
            padding: '4px 16px 12px',
            fontSize: '10px', fontWeight: 700,
            letterSpacing: '0.08em', textTransform: 'uppercase',
            color: 'var(--t3)',
            borderBottom: '1px solid var(--bd)',
            marginBottom: '8px',
          }}>
            {meta.label}
          </div>

          {Object.keys(tree).sort().map(dir => (
            <div key={dir}>
              {dir && (
                <div style={{
                  padding: '6px 16px 2px',
                  fontSize: '11px', fontWeight: 600,
                  color: 'var(--t3)', letterSpacing: '0.04em',
                }}>
                  {dir}/
                </div>
              )}
              {tree[dir].map(file => {
                const full = dir ? `${dir}/${file}` : file;
                const isActive = filePath === full;
                return (
                  <Link
                    key={full}
                    to={`${meta.baseRoute}/${full}`}
                    style={{
                      display: 'block',
                      padding: '3px 16px 3px ' + (dir ? '28px' : '16px'),
                      color: isActive ? 'var(--acc)' : 'var(--t2)',
                      background: isActive ? 'var(--bg3)' : 'transparent',
                      textDecoration: 'none',
                      borderLeft: isActive ? '2px solid var(--acc)' : '2px solid transparent',
                      overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
                      fontSize: '12px',
                    }}
                  >
                    {file}
                  </Link>
                );
              })}
            </div>
          ))}

          {files.length === 0 && !error && (
            <div style={{ padding: '12px 16px', color: 'var(--t3)' }}>Loading…</div>
          )}
          {error && (
            <div style={{ padding: '12px 16px', color: 'var(--danger)', fontSize: '12px' }}>{error}</div>
          )}
        </aside>

        {/* ── Content pane ─────────────────────────────────────────────────── */}
        <main style={{
          flex: 1, overflowY: 'auto',
          padding: '24px 40px',
          background: 'var(--bg0)',
          color: 'var(--t1)',
        }}>
          {!filePath && (
            <div style={{ color: 'var(--t3)', marginTop: '40px', textAlign: 'center' }}>
              Select a document from the sidebar
            </div>
          )}

          {loading && (
            <div style={{ color: 'var(--t3)', padding: '8px 0' }}>Loading…</div>
          )}

          {!loading && content !== null && (
            <>
              <div style={{
                fontSize: '11px', color: 'var(--t3)',
                marginBottom: '20px',
                display: 'flex', alignItems: 'center', gap: '8px',
                fontFamily: 'var(--font-mono, monospace)',
              }}>
                <button
                  onClick={() => navigate(meta.baseRoute)}
                  style={{
                    background: 'transparent', border: 'none',
                    cursor: 'pointer', color: 'var(--acc)',
                    fontSize: '11px', padding: 0,
                  }}
                >
                  ← back
                </button>
                <span>{filePath}</span>
              </div>
              {/* eslint-disable-next-line react/no-danger */}
              <div
                ref={contentRef}
                className="docs-body"
                dangerouslySetInnerHTML={{ __html: html }}
                style={{ maxWidth: '860px' }}
              />
            </>
          )}
        </main>
      </div>
    </div>
  );
}
