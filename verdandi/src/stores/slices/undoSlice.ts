/**
 * undoSlice — undo/redo stack for visibility mutations (hide, restore, expand).
 *
 * Max stack depth: 50 entries.
 * Integrated with visibilitySlice via `pushUndo()` before each mutation.
 */

import type { LoomStore } from '../loomStore';

type Set = (partial: Partial<LoomStore> | ((s: LoomStore) => Partial<LoomStore>)) => void;
type Get = () => LoomStore;

// ── Entry types ──────────────────────────────────────────────────────────────

export type UndoEntry =
  | { type: 'hide';    nodeId: string }
  | { type: 'restore'; nodeId: string }
  | { type: 'showAll'; previousHidden: string[] }
  | { type: 'expand';  nodeId: string; previousState: 'collapsed' | 'partial' | 'expanded' | undefined };

const MAX_DEPTH = 50;

// ── Actions ──────────────────────────────────────────────────────────────────

export function undoActions(set: Set, get: Get) {
  return {
    // ── Push to undo stack (called before each mutation) ─────────────────────
    pushUndo: (entry: UndoEntry) =>
      set((s) => ({
        undoStack: [...s.undoStack.slice(-(MAX_DEPTH - 1)), entry],
        redoStack: [],   // new action clears redo
      })),

    // ── Undo last action ─────────────────────────────────────────────────────
    undo: () => {
      const s = get();
      if (s.undoStack.length === 0) return;
      const entry = s.undoStack[s.undoStack.length - 1];
      const newUndoStack = s.undoStack.slice(0, -1);
      const newRedoStack = [...s.redoStack, entry];

      switch (entry.type) {
        case 'hide': {
          // Undo hide → restore the node
          const next = new Set(s.hiddenNodeIds);
          next.delete(entry.nodeId);
          set({ hiddenNodeIds: next, undoStack: newUndoStack, redoStack: newRedoStack });
          break;
        }
        case 'restore': {
          // Undo restore → hide the node again
          const next = new Set(s.hiddenNodeIds);
          next.add(entry.nodeId);
          set({ hiddenNodeIds: next, undoStack: newUndoStack, redoStack: newRedoStack });
          break;
        }
        case 'showAll': {
          // Undo showAll → re-hide previously hidden nodes
          set({ hiddenNodeIds: new Set(entry.previousHidden), undoStack: newUndoStack, redoStack: newRedoStack });
          break;
        }
        case 'expand': {
          // Undo expand → revert to previous expansion state
          const exp = { ...s.nodeExpansionState };
          if (entry.previousState) {
            exp[entry.nodeId] = entry.previousState;
          } else {
            delete exp[entry.nodeId];
          }
          set({ nodeExpansionState: exp, undoStack: newUndoStack, redoStack: newRedoStack });
          break;
        }
      }
    },

    // ── Redo last undone action ───────────────────────────────────────────────
    redo: () => {
      const s = get();
      if (s.redoStack.length === 0) return;
      const entry = s.redoStack[s.redoStack.length - 1];
      const newRedoStack = s.redoStack.slice(0, -1);
      const newUndoStack = [...s.undoStack, entry];

      switch (entry.type) {
        case 'hide': {
          const next = new Set(s.hiddenNodeIds);
          next.add(entry.nodeId);
          set({ hiddenNodeIds: next, undoStack: newUndoStack, redoStack: newRedoStack });
          break;
        }
        case 'restore': {
          const next = new Set(s.hiddenNodeIds);
          next.delete(entry.nodeId);
          set({ hiddenNodeIds: next, undoStack: newUndoStack, redoStack: newRedoStack });
          break;
        }
        case 'showAll': {
          set({ hiddenNodeIds: new Set<string>(), undoStack: newUndoStack, redoStack: newRedoStack });
          break;
        }
        case 'expand': {
          const exp = { ...s.nodeExpansionState };
          // Re-apply the expansion that was originally done (we don't store the "new" state,
          // so we can't perfectly replay — but at least toggle it back to expanded)
          exp[entry.nodeId] = 'expanded';
          set({ nodeExpansionState: exp, undoStack: newUndoStack, redoStack: newRedoStack });
          break;
        }
      }
    },

    clearHistory: () => set({ undoStack: [], redoStack: [] }),
  };
}
