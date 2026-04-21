import { useState, useEffect, useCallback, useMemo, useRef } from 'react';
import { useParams, useNavigate, Link }                      from 'react-router-dom';
import { usePageTitle }                                      from '../hooks/usePageTitle';
import { useIsMobile }                                       from '../hooks/useIsMobile';
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

// ── Types ─────────────────────────────────────────────────────────────────────
interface DocFile     { path: string; mtime: string; }
interface DisplayFile { displayPath: string; apiPath: string; mtime: string; }
interface TreeNode    { files: DisplayFile[]; children: Record<string, TreeNode>; }

export type DocsTab = 'docs' | 'team-docs' | 'team-archive' | 'highload';

// ── Helpers ───────────────────────────────────────────────────────────────────
function normalizeFiles(data: (string | DocFile)[]): DocFile[] {
  return data.map(f => typeof f === 'string' ? { path: f, mtime: '' } : f);
}

function buildTree(files: DisplayFile[]): TreeNode {
  const root: TreeNode = { files: [], children: {} };
  for (const f of files) {
    const parts = f.displayPath.split('/');
    let node = root;
    for (let i = 0; i < parts.length - 1; i++) {
      if (!node.children[parts[i]]) node.children[parts[i]] = { files: [], children: {} };
      node = node.children[parts[i]];
    }
    node.files.push(f);
  }
  return root;
}

function formatDate(iso: string): string {
  return iso ? iso.slice(0, 10) : '';
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

// ── Directory node (recursive collapsible) ────────────────────────────────────
interface DirNodeProps {
  name: string;
  node: TreeNode;
  depth: number;
  dirPath: string;
  activeFilePath: string;
  baseRoute: string;
  collapsedDirs: Set<string>;
  toggleDir: (path: string) => void;
}

function DirNode({ name, node, depth, dirPath, activeFilePath, baseRoute, collapsedDirs, toggleDir }: DirNodeProps) {
  const isExpanded = !collapsedDirs.has(dirPath);
  const indent = depth * 12;

  const hasActiveDescendant = (n: TreeNode, prefix: string): boolean => {
    if (n.files.some(f => f.displayPath === activeFilePath)) return true;
    return Object.entries(n.children).some(([seg, child]) =>
      hasActiveDescendant(child, prefix ? `${prefix}/${seg}` : seg)
    );
  };
  const isActive = hasActiveDescendant(node, '');

  return (
    <div>
      <button
        onClick={() => toggleDir(dirPath)}
        style={{
          display: 'flex', alignItems: 'center', gap: '5px',
          width: '100%', background: 'none', border: 'none',
          cursor: 'pointer',
          padding: `5px 12px 5px ${16 + indent}px`,
          color: isActive ? 'var(--acc)' : 'var(--t3)',
          fontSize: '11px', fontWeight: 600,
          letterSpacing: '0.04em', textAlign: 'left',
          fontFamily: 'var(--font-mono, monospace)',
        }}
      >
        <span style={{ width: '10px', flexShrink: 0, opacity: 0.7, fontSize: '10px' }}>
          {isExpanded ? '▾' : '▸'}
        </span>
        {name}/
      </button>

      {isExpanded && (
        <div>
          {node.files.map(f => {
            const fileName = f.displayPath.split('/').pop() ?? f.displayPath;
            const active   = f.displayPath === activeFilePath;
            const date     = formatDate(f.mtime);
            return (
              <Link
                key={f.displayPath}
                to={`${baseRoute}/${f.displayPath}`}
                style={{
                  display: 'flex', alignItems: 'baseline', justifyContent: 'space-between',
                  gap: '6px',
                  padding: `4px 12px 4px ${16 + indent + 14}px`,
                  color: active ? 'var(--acc)' : 'var(--t2)',
                  background: active ? 'color-mix(in srgb,var(--acc) 8%,var(--bg1))' : 'transparent',
                  textDecoration: 'none',
                  borderLeft: active ? '2px solid var(--acc)' : '2px solid transparent',
                  fontSize: '12px',
                  fontFamily: 'var(--font-mono, monospace)',
                }}
              >
                <span style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', minWidth: 0 }}>
                  {fileName}
                </span>
                {date && (
                  <span style={{ flexShrink: 0, fontSize: '10px', color: 'var(--t3)' }}>
                    {date}
                  </span>
                )}
              </Link>
            );
          })}

          {Object.entries(node.children)
            .sort(([a], [b]) => a.localeCompare(b))
            .map(([subName, subNode]) => (
              <DirNode
                key={subName}
                name={subName}
                node={subNode}
                depth={depth + 1}
                dirPath={`${dirPath}/${subName}`}
                activeFilePath={activeFilePath}
                baseRoute={baseRoute}
                collapsedDirs={collapsedDirs}
                toggleDir={toggleDir}
              />
            ))}
        </div>
      )}
    </div>
  );
}

// ── DocsPage ──────────────────────────────────────────────────────────────────
interface Props { tab?: DocsTab; }


export default function DocsPage({ tab = 'docs' }: Props) {
  const tabLabel = tab === 'team-archive' ? 'Archive' : tab === 'team-docs' ? 'Team' : tab === 'highload' ? 'HighLoad++' : 'Docs';
  usePageTitle(tabLabel);


  const { '*': splat } = useParams<{ '*': string }>();
  const navigate        = useNavigate();
  const filePath        = splat ?? '';

  const [pubFiles,  setPubFiles]  = useState<DocFile[]>([]);
  const [teamFiles, setTeamFiles] = useState<DocFile[]>([]);
  const [content,   setContent]   = useState<string | null>(null);
  const [error,     setError]     = useState<string | null>(null);
  const [loading,   setLoading]   = useState(false);

  // collapsed dir paths — empty = all expanded
  const [collapsedDirs, setCollapsedDirs] = useState<Set<string>>(new Set());

  const isMobile = useIsMobile();

  // resizable sidebar — collapsed by default on mobile
  const [sidebarWidth, setSidebarWidth] = useState(260);
  const [sidebarOpen,  setSidebarOpen]  = useState(() => window.innerWidth > 640);
  const dragging = useRef(false);
  const dragStartX = useRef(0);
  const dragStartW = useRef(0);

  const onDragStart = useCallback((e: React.MouseEvent) => {
    dragging.current  = true;
    dragStartX.current = e.clientX;
    dragStartW.current = sidebarWidth;
    e.preventDefault();
  }, [sidebarWidth]);

  useEffect(() => {
    const onMove = (e: MouseEvent) => {
      if (!dragging.current) return;
      const next = Math.max(160, Math.min(480, dragStartW.current + e.clientX - dragStartX.current));
      setSidebarWidth(next);
    };
    const onUp = () => { dragging.current = false; };
    window.addEventListener('mousemove', onMove);
    window.addEventListener('mouseup', onUp);
    return () => { window.removeEventListener('mousemove', onMove); window.removeEventListener('mouseup', onUp); };
  }, []);

  useEffect(() => { ensureDocsStyles(); }, []);

  // Reset collapsed state when switching tabs
  useEffect(() => { setCollapsedDirs(new Set()); }, [tab]);


  useEffect(() => {
    Promise.all([
      fetch(`${HEIMDALL_API}/docs`)
        .then(r => r.ok ? (r.json() as Promise<(string | DocFile)[]>) : Promise.resolve([])),
      fetch(`${HEIMDALL_API}/team-docs`)
        .then(r => r.ok ? (r.json() as Promise<(string | DocFile)[]>) : Promise.resolve([])),
    ]).then(([pub, team]) => {
      setPubFiles(normalizeFiles(pub));
      setTeamFiles(normalizeFiles(team));
    }).catch(() => {});
  }, []);

  // Clear content when switching tabs
  useEffect(() => {
    setContent(null);
    setError(null);
  }, [tab]);

  // Detect docs volume structure
  const teamPrefix    = useMemo(() => teamFiles.some(f => f.path.startsWith('current/')) ? 'current/' : '', [teamFiles]);
  const hasArchive    = useMemo(() => teamFiles.some(f => f.path.startsWith('archive/')), [teamFiles]);
  const hasHighload   = useMemo(() => teamFiles.some(f => f.path.startsWith('highload/')), [teamFiles]);
  const showTeamTab   = teamFiles.length > 0;
  const showArchive   = hasArchive;
  const showHighload  = hasHighload;

  // Per-tab config derived at runtime
  const apiNs = tab === 'docs' ? 'docs' : 'team-docs';
  const prefix =
    tab === 'docs'         ? '' :
    tab === 'team-docs'    ? teamPrefix :
    tab === 'team-archive' ? 'archive/' :
    /* highload */            'highload/';
  const baseRoute =
    tab === 'docs'         ? '/docs' :
    tab === 'team-docs'    ? '/team-docs' :
    tab === 'team-archive' ? '/team-archive' :
    /* highload */            '/highload';
  const rawFiles: DocFile[] = tab === 'docs' ? pubFiles : teamFiles;

  // Build display files (strip prefix, keep apiPath for fetching)
  const displayFiles: DisplayFile[] = useMemo(() => {
    if (prefix) {
      return rawFiles
        .filter(f => f.path.startsWith(prefix))
        .map(f => ({ displayPath: f.path.slice(prefix.length), apiPath: f.path, mtime: f.mtime }));
    }
    return rawFiles.map(f => ({ displayPath: f.path, apiPath: f.path, mtime: f.mtime }));
  }, [rawFiles, prefix]);

  const tree = useMemo(() => buildTree(displayFiles), [displayFiles]);

  const toggleDir = useCallback((path: string) => {
    setCollapsedDirs(prev => {
      const next = new Set(prev);
      if (next.has(path)) next.delete(path);
      else next.add(path);
      return next;
    });
  }, []);

  // Load a file by its display path
  const loadFile = useCallback((dp: string) => {
    const apiPath = prefix + dp;
    setLoading(true);
    setError(null);
    fetch(`${HEIMDALL_API}/${apiNs}/${apiPath}`)
      .then(r => r.ok ? r.text() : Promise.reject(`HTTP ${r.status}`))
      .then(text => { setContent(text); setLoading(false); })
      .catch(e  => { setError(String(e)); setLoading(false); });
  }, [apiNs, prefix]);

  useEffect(() => {
    if (filePath) loadFile(filePath);
    else setContent(null);
  }, [filePath, loadFile]);

  const html       = useMemo(() => content !== null ? marked.parse(content) as string : '', [content]);
  const contentRef = useRef<HTMLDivElement>(null);

  // Render mermaid diagrams after HTML injected into DOM
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
      }).catch(err => { console.warn('[mermaid] render error:', err); });
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
        {showTeamTab && (
          <TabBtn active={tab === 'team-docs'}  onClick={() => navigate('/team-docs')}>Team</TabBtn>
        )}
        {showArchive && (
          <TabBtn active={tab === 'team-archive'} onClick={() => navigate('/team-archive')}>Archive</TabBtn>
        )}
        {showHighload && (
          <TabBtn active={tab === 'highload'} onClick={() => navigate('/highload')}>HighLoad++</TabBtn>
        )}
      </div>

      <div style={{ display: 'flex', flex: 1, overflow: 'hidden', position: 'relative' }}>

        {/* Mobile backdrop — close sidebar when tapping outside */}
        {isMobile && sidebarOpen && (
          <div
            onClick={() => setSidebarOpen(false)}
            style={{
              position: 'absolute', inset: 0, zIndex: 19,
              background: 'rgba(0,0,0,0.45)',
            }}
          />
        )}

        {/* ── Sidebar ── */}
        <aside
          onClick={isMobile ? (e) => {
            if ((e.target as HTMLElement).closest('a')) setSidebarOpen(false);
          } : undefined}
          style={{
            position: isMobile ? 'absolute' : 'relative',
            top: 0, left: 0, bottom: 0,
            width: sidebarOpen ? (isMobile ? Math.min(sidebarWidth, 300) : sidebarWidth) : 0,
            minWidth: sidebarOpen ? (isMobile ? Math.min(sidebarWidth, 300) : sidebarWidth) : 0,
            flexShrink: 0,
            borderRight: sidebarOpen ? '1px solid var(--bd)' : 'none',
            background: 'var(--bg1)',
            overflowY: 'auto',
            overflowX: 'hidden',
            padding: sidebarOpen ? '12px 0' : 0,
            fontFamily: 'var(--font-mono, monospace)',
            transition: 'width 0.15s, min-width 0.15s, padding 0.15s',
            zIndex: isMobile ? 20 : 'auto',
          }}>
          <div style={{
            padding: '4px 16px 10px',
            fontSize: '10px', fontWeight: 700,
            letterSpacing: '0.08em', textTransform: 'uppercase',
            color: 'var(--t3)',
            borderBottom: '1px solid var(--bd)',
            marginBottom: '6px',
          }}>
            {tabLabel}
          </div>

          {/* Root-level files (no parent dir) */}
          {tree.files.map(f => {
            const active = f.displayPath === filePath;
            const date   = formatDate(f.mtime);
            return (
              <Link
                key={f.displayPath}
                to={`${baseRoute}/${f.displayPath}`}
                style={{
                  display: 'flex', alignItems: 'baseline', justifyContent: 'space-between',
                  gap: '6px',
                  padding: `4px 12px 4px 16px`,
                  color: active ? 'var(--acc)' : 'var(--t2)',
                  background: active ? 'color-mix(in srgb,var(--acc) 8%,var(--bg1))' : 'transparent',
                  textDecoration: 'none',
                  borderLeft: active ? '2px solid var(--acc)' : '2px solid transparent',
                  fontSize: '12px',
                  fontFamily: 'var(--font-mono, monospace)',
                }}
              >
                <span style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', minWidth: 0 }}>
                  {f.displayPath}
                </span>
                {date && (
                  <span style={{ flexShrink: 0, fontSize: '10px', color: 'var(--t3)' }}>
                    {date}
                  </span>
                )}
              </Link>
            );
          })}

          {/* Directories */}
          {Object.entries(tree.children)
            .sort(([a], [b]) => a.localeCompare(b))
            .map(([name, node]) => (
              <DirNode
                key={name}
                name={name}
                node={node}
                depth={0}
                dirPath={name}
                activeFilePath={filePath}
                baseRoute={baseRoute}
                collapsedDirs={collapsedDirs}
                toggleDir={toggleDir}
              />
            ))}

          {displayFiles.length === 0 && !error && (
            <div style={{ padding: '12px 16px', color: 'var(--t3)' }}>Loading…</div>
          )}
          {error && (
            <div style={{ padding: '12px 16px', color: 'var(--danger)', fontSize: '12px' }}>{error}</div>
          )}
        </aside>

        {/* ── Resize handle + toggle — desktop only ── */}
        {!isMobile && (
          <div style={{
            display: sidebarOpen ? undefined : 'none',
            position: 'relative', flexShrink: 0, width: '8px',
            background: 'transparent', cursor: 'col-resize', zIndex: 10,
          }}
            onMouseDown={sidebarOpen ? onDragStart : undefined}
          >
            <div style={{
              position: 'absolute', top: 0, bottom: 0, left: '3px', width: '2px',
              background: 'var(--bd)',
              transition: 'background 0.12s',
            }} />
            <button
              title={sidebarOpen ? 'Свернуть навигацию' : 'Развернуть навигацию'}
              onClick={() => setSidebarOpen(v => !v)}
              style={{
                position: 'absolute', top: '50%', left: '50%',
                transform: 'translate(-50%, -50%)',
                width: '16px', height: '32px',
                background: 'var(--bg1)',
                border: '1px solid var(--bd)',
                borderRadius: '4px',
                cursor: 'pointer',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                color: 'var(--t3)', fontSize: '9px', padding: 0,
                zIndex: 11,
              }}
            >
              {sidebarOpen ? '‹' : '›'}
            </button>
          </div>
        )}

        {/* ── Content pane ── */}
        <main style={{
          flex: 1, position: 'relative',
          overflow:  tab === 'highload' ? 'hidden' : 'auto',
          padding:   tab === 'highload' ? 0 : isMobile ? '16px' : '24px 40px',
          background: 'var(--bg0)',
          color: 'var(--t1)',
        }}>
          {/* Floating reopen button — only when sidebar is fully hidden */}
          {!sidebarOpen && (
            <button
              title="Открыть навигацию"
              onClick={() => setSidebarOpen(true)}
              style={{
                position: 'absolute', top: '10px', left: '10px', zIndex: 5,
                width: '26px', height: '26px',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                background: 'var(--bg1)', border: '1px solid var(--bd)',
                borderRadius: 'var(--seer-radius-md)',
                cursor: 'pointer', color: 'var(--t3)', fontSize: '12px',
                transition: 'color 0.12s, border-color 0.12s',
              }}
              onMouseEnter={e => {
                (e.currentTarget as HTMLElement).style.color = 'var(--t1)';
                (e.currentTarget as HTMLElement).style.borderColor = 'var(--acc)';
              }}
              onMouseLeave={e => {
                (e.currentTarget as HTMLElement).style.color = 'var(--t3)';
                (e.currentTarget as HTMLElement).style.borderColor = 'var(--bd)';
              }}
            >
              ›
            </button>
          )}

          {!filePath && tab !== 'highload' && (
            <div style={{ color: 'var(--t3)', marginTop: '40px', textAlign: 'center', padding: '0 16px' }}>
              {isMobile ? 'Tap › to open the file list' : 'Select a document from the sidebar'}
            </div>
          )}

          {!filePath && tab === 'highload' && (
            <iframe
              src="/highload-plan"
              style={{ width: '100%', height: '100%', border: 'none', display: 'block' }}
              title="AIDA Plan"
            />
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
                  onClick={() => navigate(baseRoute)}
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
