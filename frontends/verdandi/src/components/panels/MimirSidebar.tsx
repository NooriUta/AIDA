import { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useMimirChat } from '../../hooks/useMimirChat';
import { useMimirChatStore } from '../../stores/mimirChatStore';
import { useLoomStore } from '../../stores/loomStore';
import './MimirSidebar.css';

/** Tiny Markdown renderer — just enough for MIMIR answers (bold, italic,
 *  inline code, headers, bullet/numbered lists, fenced code blocks). No
 *  third-party dep — react-markdown adds 30+KB which we don't need for the
 *  shapes MIMIR actually produces. */
function renderMarkdown(src: string): React.ReactNode[] {
  const lines = src.replace(/\r\n/g, '\n').split('\n');
  const out: React.ReactNode[] = [];
  let i = 0;
  let key = 0;

  const inline = (text: string): React.ReactNode[] => {
    // Order: code (greedy fences first), then bold/italic
    const tokens: React.ReactNode[] = [];
    let rest = text;
    let pos = 0;
    const RE = /(`[^`]+`)|(\*\*[^*]+\*\*)|(__[^_]+__)|(\*[^*\n]+\*)|(_[^_\n]+_)/g;
    let m: RegExpExecArray | null;
    while ((m = RE.exec(rest)) !== null) {
      if (m.index > pos) tokens.push(rest.substring(pos, m.index));
      const seg = m[0];
      if (seg.startsWith('`')) {
        tokens.push(<code key={key++} className="mimir-md-code">{seg.slice(1, -1)}</code>);
      } else if (seg.startsWith('**') || seg.startsWith('__')) {
        tokens.push(<strong key={key++}>{seg.slice(2, -2)}</strong>);
      } else {
        tokens.push(<em key={key++}>{seg.slice(1, -1)}</em>);
      }
      pos = m.index + seg.length;
    }
    if (pos < rest.length) tokens.push(rest.substring(pos));
    return tokens;
  };

  while (i < lines.length) {
    const line = lines[i];

    // Fenced code block ```
    if (line.startsWith('```')) {
      const lang = line.substring(3).trim();
      const buf: string[] = [];
      i++;
      while (i < lines.length && !lines[i].startsWith('```')) {
        buf.push(lines[i]);
        i++;
      }
      i++; // skip closing fence
      out.push(
        <pre key={key++} className="mimir-md-pre" data-lang={lang}>
          <code>{buf.join('\n')}</code>
        </pre>,
      );
      continue;
    }

    // Headers
    const h = /^(#{1,6})\s+(.+)$/.exec(line);
    if (h) {
      const level = Math.min(h[1].length, 4); // map h5/h6 → h4 visually
      const Tag = `h${level + 2}` as 'h3' | 'h4' | 'h5' | 'h6'; // sidebar baseline = h3
      out.push(<Tag key={key++} className="mimir-md-h">{inline(h[2])}</Tag>);
      i++;
      continue;
    }

    // GFM table — header row + alignment row + body rows.
    // Detect: line contains "|" AND next line is the alignment separator
    //   |---|---|  or  |:---:|---:|
    if (line.includes('|') && i + 1 < lines.length &&
        /^\s*\|?\s*:?-{3,}:?\s*(\|\s*:?-{3,}:?\s*)+\|?\s*$/.test(lines[i + 1])) {
      const splitRow = (row: string): string[] => {
        let s = row.trim();
        if (s.startsWith('|')) s = s.substring(1);
        if (s.endsWith('|')) s = s.substring(0, s.length - 1);
        return s.split('|').map(c => c.trim());
      };
      const headers = splitRow(line);
      const alignSpec = splitRow(lines[i + 1]);
      const aligns = alignSpec.map(a => {
        const left  = a.startsWith(':');
        const right = a.endsWith(':');
        if (left && right) return 'center';
        if (right) return 'right';
        return 'left';
      });
      i += 2;
      const rows: string[][] = [];
      while (i < lines.length && lines[i].includes('|')) {
        // Stop on a blank line (table end)
        if (lines[i].trim() === '') break;
        rows.push(splitRow(lines[i]));
        i++;
      }
      out.push(
        <table key={key++} className="mimir-md-table">
          <thead>
            <tr>
              {headers.map((h, hi) => (
                <th key={hi} style={{ textAlign: aligns[hi] as 'left' | 'right' | 'center' }}>
                  {inline(h)}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {rows.map((row, ri) => (
              <tr key={ri}>
                {row.map((cell, ci) => (
                  <td key={ci} style={{ textAlign: aligns[ci] as 'left' | 'right' | 'center' }}>
                    {inline(cell)}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>,
      );
      continue;
    }

    // Bullet list (- / *)
    if (/^\s*[-*]\s+/.test(line)) {
      const items: React.ReactNode[] = [];
      while (i < lines.length && /^\s*[-*]\s+/.test(lines[i])) {
        const itemText = lines[i].replace(/^\s*[-*]\s+/, '');
        items.push(<li key={key++}>{inline(itemText)}</li>);
        i++;
      }
      out.push(<ul key={key++} className="mimir-md-ul">{items}</ul>);
      continue;
    }

    // Numbered list (1. 2. ...)
    if (/^\s*\d+\.\s+/.test(line)) {
      const items: React.ReactNode[] = [];
      while (i < lines.length && /^\s*\d+\.\s+/.test(lines[i])) {
        const itemText = lines[i].replace(/^\s*\d+\.\s+/, '');
        items.push(<li key={key++}>{inline(itemText)}</li>);
        i++;
      }
      out.push(<ol key={key++} className="mimir-md-ol">{items}</ol>);
      continue;
    }

    // Blank line — paragraph break
    if (line.trim() === '') {
      out.push(<br key={key++} />);
      i++;
      continue;
    }

    // Plain paragraph
    out.push(<p key={key++} className="mimir-md-p">{inline(line)}</p>);
    i++;
  }
  return out;
}

/** Parse "NodeType:Label" hint MIMIR can put in highlightNodeIds. Returns
 *  null when the entry is just an opaque id (legacy / unknown shape). */
type ParsedTarget = { type: string; label: string; raw: string };

const KNOWN_TYPES = new Set([
  'DaliSchema', 'DaliTable', 'DaliColumn', 'DaliRoutine',
  'DaliPackage', 'DaliView', 'DaliAtom', 'DaliStmt', 'DaliService',
]);

const parseTarget = (s: string): ParsedTarget | null => {
  const idx = s.indexOf(':');
  if (idx < 1) return null;
  const type = s.substring(0, idx);
  const label = s.substring(idx + 1);
  if (!KNOWN_TYPES.has(type) || !label) return null;
  return { type, label, raw: s };
};

const labelsOnly = (ids: string[]): string[] =>
  ids.map((id) => {
    const t = parseTarget(id);
    return t ? t.label : id;
  });

/** Direct setState into loomStore — bypasses missing setter API. */
const highlightInGraph = (ids: string[]) => {
  if (ids.length === 0) return;
  // Strip "NodeType:" prefix before pushing into the highlight set —
  // canvas uses bare ids/labels.
  const labels = labelsOnly(ids);
  useLoomStore.setState({ highlightedNodes: new Set(labels) } as never);
};

/** Open the routine / package in SKULD (deep investigation view).
 *  v0 stub: SKULD module is on the post-HighLoad roadmap (Q3 2026 — sprints/
 *  SPRINT_SKULD_V1.md). For now we navigate to the placeholder route — the
 *  UnderConstructionPage shows a "coming soon" panel and offers a deep-link
 *  back into LOOM. When SKULD lands, this function stays as-is — only the
 *  route content changes. */
const openInSkuld = (target: ParsedTarget) => {
  // We deliberately use window.location instead of react-router navigate
  // here — sidebar may live outside the router (App.tsx mounts it under
  // QueryClientProvider). Server-side routes are absolute, browser handles them.
  const param = encodeURIComponent(`${target.type}:${target.label}`);
  window.location.href = `/skuld?node=${param}&returnTo=/verdandi`;
};

/** Drill into the node by jumping the LOOM canvas straight to the target.
 *  jumpTo() resets navigationStack + l1ScopeStack and sets viewLevel +
 *  currentScope + filter.startObjectId atomically — exactly what we need
 *  to land on a fresh L2 (schema → tables) or L3 (table → columns) view.
 *
 *  Why not drillDown(): drillDown's L1→L2 path expects the user to be on
 *  L1 already, and its scope semantics depend on the current viewLevel.
 *  jumpTo is level-agnostic — works regardless of where the user is.
 *
 *  schema_geoid = schema_name in our YGG corpus (verified live), so the
 *  human-readable label doubles as the scope id. If schemas ever get a
 *  separate composite geoid, MIMIR's @Tool should put the geoid into the
 *  "DaliSchema:<geoid>" payload — frontend stays unchanged. */
const openInLoom = (target: ParsedTarget) => {
  const store = useLoomStore.getState() as unknown as {
    jumpTo?: (
      level: string,
      scope: string | null,
      label: string,
      nodeType?: string,
      opts?: { focusNodeId?: string },
    ) => void;
  };
  const level =
    target.type === 'DaliSchema' ? 'L2' :
    target.type === 'DaliTable'  ? 'L3' :
    target.type === 'DaliRoutine' || target.type === 'DaliPackage' ? 'L3' :
    'L2';
  store.jumpTo?.(level, target.label, target.label, target.type);
};

/**
 * MIMIR Copilot sidebar v0 (TIER2 MT-04). All user-visible strings come from
 * the `mimir.*` i18n namespace (en/ru) so it tracks the language switcher.
 */
export default function MimirSidebar() {
  const { t } = useTranslation();
  const open    = useMimirChatStore((s) => s.open);
  const setOpen = useMimirChatStore((s) => s.setOpen);
  const { messages, pending, error, send, approve, reject, reset } = useMimirChat();

  const [text, setText] = useState('');
  const [decisionComment, setDecisionComment] = useState('');
  const scrollRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  }, [messages, pending]);

  if (!open) return null;

  const handleSend = async () => {
    if (!text.trim() || pending) return;
    const q = text;
    setText('');
    await send(q);
  };

  return (
    <aside className="mimir-sidebar" aria-label={t('mimir.title')}>
      <header className="mimir-header">
        <span className="mimir-dot" aria-hidden="true" />
        <h2>{t('mimir.title')}</h2>
        <button
          type="button"
          className="mimir-icon-btn"
          onClick={reset}
          title={t('mimir.newSession')}
          aria-label={t('mimir.newSession')}
        >
          ↻
        </button>
        <button
          type="button"
          className="mimir-icon-btn"
          onClick={() => setOpen(false)}
          title={t('mimir.close')}
          aria-label={t('mimir.close')}
        >
          ×
        </button>
      </header>

      <div className="mimir-messages" ref={scrollRef}>
        {messages.length === 0 && (
          <div className="mimir-empty">{t('mimir.empty')}</div>
        )}

        {messages.map((m) => (
          <div key={m.id} className={`mimir-msg mimir-msg-${m.role}`}>
            <div className="mimir-msg-text">
              {m.role === 'mimir' ? renderMarkdown(m.text) : m.text}
            </div>

            {m.role === 'mimir' && m.toolCallsUsed && m.toolCallsUsed.length > 0 && (
              <div className="mimir-tool-bar">
                {m.toolCallsUsed.map((tool, i) => {
                  // Type strip — Q (Query/data) vs AI (synthesis/llm) chips by tool family
                  const isQuery = /^(search_|count_|describe_|list_|query_|find_|get_)/.test(tool);
                  const cls = isQuery ? 'mimir-tool-q' : 'mimir-tool-ai';
                  const icon = isQuery ? 'Q' : 'AI';
                  return (
                    <span key={i} className={`mimir-tool-chip ${cls}`} title={tool}>
                      {icon} {tool} ✓
                    </span>
                  );
                })}
              </div>
            )}

            {m.role === 'mimir' && m.highlightNodeIds && m.highlightNodeIds.length > 0 && (() => {
              const targets = m.highlightNodeIds!
                .map(parseTarget)
                .filter((x): x is ParsedTarget => x !== null);
              const drillTarget = targets.find(t => t.type === 'DaliSchema')
                                ?? targets.find(t => t.type === 'DaliTable')
                                ?? targets[0];
              const skuldTarget = targets.find(t => t.type === 'DaliRoutine' || t.type === 'DaliPackage');
              return (
                <div className="mimir-actions">
                  <button
                    type="button"
                    className="mimir-action-btn"
                    onClick={() => highlightInGraph(m.highlightNodeIds!)}
                  >
                    {t('mimir.showInGraph', { count: m.highlightNodeIds.length })}
                  </button>
                  {drillTarget && (
                    <button
                      type="button"
                      className="mimir-action-btn mimir-action-primary"
                      onClick={() => {
                        openInLoom(drillTarget);
                        setOpen(false);
                      }}
                      title={t('mimir.openInLoomTitle', { type: drillTarget.type, label: drillTarget.label })}
                    >
                      {t('mimir.openInLoom', { type: drillTarget.type, label: drillTarget.label })}
                    </button>
                  )}
                  {skuldTarget && (
                    <button
                      type="button"
                      className="mimir-action-btn mimir-action-skuld"
                      onClick={() => openInSkuld(skuldTarget)}
                      title={t('mimir.openInSkuldTitle', { label: skuldTarget.label })}
                    >
                      {t('mimir.openInSkuld', { label: skuldTarget.label })}
                    </button>
                  )}
                </div>
              );
            })()}

            {m.quotaExceeded && (
              <div className="mimir-quota-banner">
                {t('mimir.quotaExceeded', { reason: m.quotaExceeded.reason })}{' '}
                {m.quotaExceeded.resetAt && (
                  t('mimir.quotaResetsAt', {
                    time: new Date(m.quotaExceeded.resetAt).toLocaleString(),
                  })
                )}
              </div>
            )}

            {m.awaitingApproval && (
              <div className="mimir-approval">
                <div className="mimir-approval-text">
                  {t('mimir.approval.required', {
                    reason: m.awaitingApproval.reason,
                    cost:   m.awaitingApproval.estimatedCostUsd.toFixed(4),
                  })}
                </div>
                <textarea
                  className="mimir-approval-comment"
                  placeholder={t('mimir.approval.comment')}
                  value={decisionComment}
                  onChange={(e) => setDecisionComment(e.target.value)}
                />
                <div className="mimir-approval-buttons">
                  <button
                    type="button"
                    className="mimir-action-btn mimir-approve"
                    onClick={() => {
                      approve(m.awaitingApproval!.approvalId, decisionComment);
                      setDecisionComment('');
                    }}
                    disabled={pending}
                  >
                    {t('mimir.approval.approve')}
                  </button>
                  <button
                    type="button"
                    className="mimir-action-btn mimir-reject"
                    onClick={() => {
                      reject(m.awaitingApproval!.approvalId, decisionComment);
                      setDecisionComment('');
                    }}
                    disabled={pending}
                  >
                    {t('mimir.approval.reject')}
                  </button>
                </div>
              </div>
            )}

            {m.role === 'mimir' && (m.promptTokens! + m.completionTokens! > 0) && (
              <div className="mimir-meta">
                {m.provider}/{m.model} · {(m.promptTokens! + m.completionTokens!)} tokens · {m.durationMs}ms
              </div>
            )}
          </div>
        ))}

        {pending && (
          <div className="mimir-msg mimir-msg-mimir mimir-thinking">
            <span className="mimir-dot mimir-dot-anim" /> {t('mimir.thinking')}
          </div>
        )}

        {error && !pending && (
          <div className="mimir-error">{error}</div>
        )}
      </div>

      <form
        className="mimir-input"
        onSubmit={(e) => {
          e.preventDefault();
          handleSend();
        }}
      >
        <textarea
          aria-label={t('mimir.placeholder')}
          value={text}
          placeholder={t('mimir.placeholder')}
          rows={2}
          disabled={pending}
          onChange={(e) => setText(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
              e.preventDefault();
              handleSend();
            }
          }}
        />
        <button type="submit" className="mimir-send-btn" disabled={pending || !text.trim()}>
          {t('mimir.send')}
        </button>
      </form>
    </aside>
  );
}
