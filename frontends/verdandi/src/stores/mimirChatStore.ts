import { create } from 'zustand';
import type { MimirAnswer } from '../services/mimirApi';

export type Role = 'user' | 'mimir' | 'system';

export interface ChatMessage {
  id:        string;
  role:      Role;
  text:      string;
  timestamp: number;
  // Optional metadata from MimirAnswer for richer rendering:
  toolCallsUsed?:    string[];
  highlightNodeIds?: string[];
  awaitingApproval?: MimirAnswer['awaitingApproval'];
  quotaExceeded?:    MimirAnswer['quota'];
  provider?:         string | null;
  model?:            string | null;
  promptTokens?:     number;
  completionTokens?: number;
  durationMs?:       number;
}

interface MimirChatState {
  open:      boolean;
  sessionId: string;
  messages:  ChatMessage[];
  pending:   boolean;
  error:     string | null;

  setOpen:    (v: boolean) => void;
  toggle:     () => void;
  pushUser:   (text: string) => ChatMessage;
  pushAnswer: (answer: MimirAnswer) => ChatMessage;
  pushSystem: (text: string) => ChatMessage;
  setPending: (v: boolean) => void;
  setError:   (v: string | null) => void;
  reset:      () => void;
}

const newSessionId = () =>
  (crypto.randomUUID?.() ?? `mimir-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`);

export const useMimirChatStore = create<MimirChatState>((set, get) => ({
  open:      false,
  sessionId: newSessionId(),
  messages:  [],
  pending:   false,
  error:     null,

  setOpen:    (open) => set({ open }),
  toggle:     ()     => set({ open: !get().open }),
  setPending: (pending) => set({ pending }),
  setError:   (error) => set({ error }),

  pushUser: (text) => {
    const msg: ChatMessage = {
      id:        crypto.randomUUID?.() ?? String(Date.now()),
      role:      'user',
      text,
      timestamp: Date.now(),
    };
    set({ messages: [...get().messages, msg] });
    return msg;
  },

  pushAnswer: (a) => {
    const msg: ChatMessage = {
      id:               crypto.randomUUID?.() ?? String(Date.now()),
      role:             'mimir',
      text:             a.answer ?? '',
      timestamp:        Date.now(),
      toolCallsUsed:    a.toolCallsUsed ?? [],
      highlightNodeIds: a.highlightNodeIds ?? [],
      awaitingApproval: a.awaitingApproval ?? null,
      quotaExceeded:    a.quota ?? null,
      provider:         a.provider ?? null,
      model:            a.model ?? null,
      promptTokens:     a.promptTokens ?? 0,
      completionTokens: a.completionTokens ?? 0,
      durationMs:       a.durationMs ?? 0,
    };
    set({ messages: [...get().messages, msg] });
    return msg;
  },

  pushSystem: (text) => {
    const msg: ChatMessage = {
      id:        crypto.randomUUID?.() ?? String(Date.now()),
      role:      'system',
      text,
      timestamp: Date.now(),
    };
    set({ messages: [...get().messages, msg] });
    return msg;
  },

  reset: () => set({
    sessionId: newSessionId(),
    messages:  [],
    pending:   false,
    error:     null,
  }),
}));
