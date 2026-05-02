import { useEffect, useRef, useState } from 'react';
import { useMimirChat } from '../../hooks/useMimirChat';
import { useMimirChatStore } from '../../stores/mimirChatStore';
import { useLoomStore } from '../../stores/loomStore';
import './MimirSidebar.css';

/** Direct setState into loomStore — bypasses missing setter API. */
const highlightInGraph = (ids: string[]) => {
  if (ids.length === 0) return;
  // useLoomStore exposes highlightedNodes:Set<string>; we replace it wholesale.
  useLoomStore.setState({ highlightedNodes: new Set(ids) } as never);
};

/**
 * MIMIR Copilot sidebar v0 (TIER2 MT-04).
 *
 * Layout:
 *  – Header with «MIMIR Copilot» title, session reset and close buttons
 *  – Scrollable messages area (user / mimir / system bubbles)
 *  – Tool-bar inside MIMIR bubbles showing toolCallsUsed
 *  – HiL approve/reject controls when the answer is awaitingApproval
 *  – Quota-exceeded banner when the answer carries quota info
 *  – «Show in graph» button forwards highlightNodeIds to the LOOM store
 *  – Textarea + send button at the bottom; Enter sends, Shift+Enter newline
 */
export default function MimirSidebar() {
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

  const handleShowInGraph = (ids: string[]) => highlightInGraph(ids);

  return (
    <aside className="mimir-sidebar" aria-label="MIMIR Copilot">
      <header className="mimir-header">
        <span className="mimir-dot" aria-hidden="true" />
        <h2>MIMIR Copilot</h2>
        <button
          type="button"
          className="mimir-icon-btn"
          onClick={reset}
          title="New session"
          aria-label="New session"
        >
          ↻
        </button>
        <button
          type="button"
          className="mimir-icon-btn"
          onClick={() => setOpen(false)}
          title="Close"
          aria-label="Close"
        >
          ×
        </button>
      </header>

      <div className="mimir-messages" ref={scrollRef}>
        {messages.length === 0 && (
          <div className="mimir-empty">
            Ask about lineage, impact, source code or schema. MIMIR will call
            deterministic tools and summarise the result.
          </div>
        )}

        {messages.map((m) => (
          <div key={m.id} className={`mimir-msg mimir-msg-${m.role}`}>
            <div className="mimir-msg-text">{m.text}</div>

            {m.role === 'mimir' && m.toolCallsUsed && m.toolCallsUsed.length > 0 && (
              <div className="mimir-tool-bar">
                {m.toolCallsUsed.map((t, i) => (
                  <span key={i} className="mimir-tool-chip">⚙ {t}</span>
                ))}
              </div>
            )}

            {m.role === 'mimir' && m.highlightNodeIds && m.highlightNodeIds.length > 0 && (
              <div className="mimir-actions">
                <button
                  type="button"
                  className="mimir-action-btn"
                  onClick={() => handleShowInGraph(m.highlightNodeIds!)}
                >
                  Show in graph ({m.highlightNodeIds.length})
                </button>
              </div>
            )}

            {m.quotaExceeded && (
              <div className="mimir-quota-banner">
                Quota exceeded ({m.quotaExceeded.reason}).{' '}
                {m.quotaExceeded.resetAt && (
                  <>Resets at {new Date(m.quotaExceeded.resetAt).toLocaleString()}.</>
                )}
              </div>
            )}

            {m.awaitingApproval && (
              <div className="mimir-approval">
                <div className="mimir-approval-text">
                  Approval required ({m.awaitingApproval.reason}). Estimated cost: $
                  {m.awaitingApproval.estimatedCostUsd.toFixed(4)}.
                </div>
                <textarea
                  className="mimir-approval-comment"
                  placeholder="Comment (optional)"
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
                    Approve
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
                    Reject
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
            <span className="mimir-dot mimir-dot-anim" /> thinking…
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
          aria-label="Question"
          value={text}
          placeholder="Ask MIMIR…"
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
          Send
        </button>
      </form>
    </aside>
  );
}
