import { useCallback } from 'react';
import {
  askMimir,
  decideMimirSession,
  type MimirAnswer,
} from '../services/mimirApi';
import { useMimirChatStore } from '../stores/mimirChatStore';

/**
 * MIMIR sidebar chat hook (TIER2 MT-04).
 *
 * Wraps the zustand store with three high-level actions:
 *  – `send(question)` — dispatches the question through Chur, appends both
 *    bubbles, and surfaces approval/quota states from the answer.
 *  – `approve(approvalId, comment?)` and `reject(approvalId, comment?)` —
 *    operator decisions on a HiL-paused turn.
 *
 * Pending state is tracked so the input can disable while a turn is in flight.
 */
export function useMimirChat() {
  const sessionId  = useMimirChatStore((s) => s.sessionId);
  const messages   = useMimirChatStore((s) => s.messages);
  const pending    = useMimirChatStore((s) => s.pending);
  const error      = useMimirChatStore((s) => s.error);
  const pushUser   = useMimirChatStore((s) => s.pushUser);
  const pushAnswer = useMimirChatStore((s) => s.pushAnswer);
  const pushSystem = useMimirChatStore((s) => s.pushSystem);
  const setPending = useMimirChatStore((s) => s.setPending);
  const setError   = useMimirChatStore((s) => s.setError);
  const reset      = useMimirChatStore((s) => s.reset);

  const send = useCallback(
    async (question: string) => {
      const trimmed = question.trim();
      if (!trimmed || pending) return null as MimirAnswer | null;
      pushUser(trimmed);
      setPending(true);
      setError(null);
      try {
        const answer = await askMimir({
          question:  trimmed,
          sessionId,
          maxToolCalls: 5,
        });
        pushAnswer(answer);
        return answer;
      } catch (e: unknown) {
        const msg = e instanceof Error ? e.message : 'request failed';
        setError(msg);
        pushSystem(`MIMIR error: ${msg}`);
        return null;
      } finally {
        setPending(false);
      }
    },
    [pending, sessionId, pushUser, pushAnswer, pushSystem, setPending, setError],
  );

  const decide = useCallback(
    async (approvalId: string, approve: boolean, comment?: string) => {
      try {
        setPending(true);
        const result = await decideMimirSession(sessionId, { approvalId, approve, comment });
        if ((result as MimirAnswer).answer) {
          pushAnswer(result as MimirAnswer);
        } else {
          pushSystem(`Decision recorded: approve=${approve}`);
        }
      } catch (e: unknown) {
        const msg = e instanceof Error ? e.message : 'decision failed';
        setError(msg);
        pushSystem(`MIMIR decision error: ${msg}`);
      } finally {
        setPending(false);
      }
    },
    [sessionId, pushAnswer, pushSystem, setPending, setError],
  );

  return {
    sessionId,
    messages,
    pending,
    error,
    send,
    approve: (id: string, comment?: string) => decide(id, true,  comment),
    reject:  (id: string, comment?: string) => decide(id, false, comment),
    reset,
  };
}
