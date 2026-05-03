import { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
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
            <div className="mimir-msg-text">{m.text}</div>

            {m.role === 'mimir' && m.toolCallsUsed && m.toolCallsUsed.length > 0 && (
              <div className="mimir-tool-bar">
                {m.toolCallsUsed.map((tool, i) => (
                  <span key={i} className="mimir-tool-chip">⚙ {tool}</span>
                ))}
              </div>
            )}

            {m.role === 'mimir' && m.highlightNodeIds && m.highlightNodeIds.length > 0 && (
              <div className="mimir-actions">
                <button
                  type="button"
                  className="mimir-action-btn"
                  onClick={() => highlightInGraph(m.highlightNodeIds!)}
                >
                  {t('mimir.showInGraph', { count: m.highlightNodeIds.length })}
                </button>
              </div>
            )}

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
