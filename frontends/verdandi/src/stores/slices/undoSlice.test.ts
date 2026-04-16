import { describe, it, expect } from 'vitest';
import { undoActions, type UndoEntry } from './undoSlice';

// ── Helpers ──────────────────────────────────────────────────────────────────

function makeState(overrides: Record<string, unknown> = {}) {
  return {
    undoStack: [] as UndoEntry[],
    redoStack: [] as UndoEntry[],
    hiddenNodeIds: new Set<string>(),
    nodeExpansionState: {} as Record<string, 'collapsed' | 'partial' | 'expanded'>,
    ...overrides,
  };
}

function setup(overrides: Record<string, unknown> = {}) {
  let state = makeState(overrides);
  const set = (partial: unknown) => {
    const p = typeof partial === 'function' ? partial(state) : partial;
    state = { ...state, ...p };
  };
  const get = () => state as any;
  const actions = undoActions(set, get);
  return { actions, getState: () => state };
}

// ── pushUndo ─────────────────────────────────────────────────────────────────

describe('pushUndo', () => {
  it('adds entry to undoStack', () => {
    const { actions, getState } = setup();
    actions.pushUndo({ type: 'hide', nodeId: 'n1' });
    expect(getState().undoStack).toHaveLength(1);
    expect(getState().undoStack[0]).toEqual({ type: 'hide', nodeId: 'n1' });
  });

  it('clears redoStack on new action', () => {
    const { actions, getState } = setup({
      redoStack: [{ type: 'hide', nodeId: 'old' }],
    });
    actions.pushUndo({ type: 'hide', nodeId: 'n1' });
    expect(getState().redoStack).toHaveLength(0);
  });

  it('caps at MAX_DEPTH (50)', () => {
    const existing = Array.from({ length: 50 }, (_, i) => ({
      type: 'hide' as const,
      nodeId: `n${i}`,
    }));
    const { actions, getState } = setup({ undoStack: existing });
    actions.pushUndo({ type: 'hide', nodeId: 'n50' });
    expect(getState().undoStack).toHaveLength(50);
    expect((getState().undoStack[49] as { type: 'hide'; nodeId: string }).nodeId).toBe('n50');
    expect((getState().undoStack[0] as { type: 'hide'; nodeId: string }).nodeId).toBe('n1'); // oldest evicted
  });
});

// ── undo ─────────────────────────────────────────────────────────────────────

describe('undo', () => {
  it('no-op on empty stack', () => {
    const { actions, getState } = setup();
    actions.undo();
    expect(getState().undoStack).toHaveLength(0);
    expect(getState().redoStack).toHaveLength(0);
  });

  it('hide entry — removes nodeId from hiddenNodeIds', () => {
    const { actions, getState } = setup({
      undoStack: [{ type: 'hide', nodeId: 'n1' }],
      hiddenNodeIds: new Set(['n1']),
    });
    actions.undo();
    expect(getState().hiddenNodeIds.has('n1')).toBe(false);
  });

  it('restore entry — re-adds nodeId to hiddenNodeIds', () => {
    const { actions, getState } = setup({
      undoStack: [{ type: 'restore', nodeId: 'n1' }],
      hiddenNodeIds: new Set<string>(),
    });
    actions.undo();
    expect(getState().hiddenNodeIds.has('n1')).toBe(true);
  });

  it('showAll entry — restores previousHidden set', () => {
    const { actions, getState } = setup({
      undoStack: [{ type: 'showAll', previousHidden: ['n1', 'n2'] }],
      hiddenNodeIds: new Set<string>(),
    });
    actions.undo();
    expect(getState().hiddenNodeIds).toEqual(new Set(['n1', 'n2']));
  });

  it('expand entry — reverts to previousState', () => {
    const { actions, getState } = setup({
      undoStack: [{ type: 'expand', nodeId: 'n1', previousState: 'collapsed' }],
      nodeExpansionState: { n1: 'expanded' },
    });
    actions.undo();
    expect(getState().nodeExpansionState.n1).toBe('collapsed');
  });

  it('expand entry with undefined previousState — deletes key', () => {
    const { actions, getState } = setup({
      undoStack: [{ type: 'expand', nodeId: 'n1', previousState: undefined }],
      nodeExpansionState: { n1: 'expanded' },
    });
    actions.undo();
    expect(getState().nodeExpansionState).not.toHaveProperty('n1');
  });

  it('moves entry to redoStack', () => {
    const entry: UndoEntry = { type: 'hide', nodeId: 'n1' };
    const { actions, getState } = setup({
      undoStack: [entry],
      hiddenNodeIds: new Set(['n1']),
    });
    actions.undo();
    expect(getState().undoStack).toHaveLength(0);
    expect(getState().redoStack).toHaveLength(1);
    expect(getState().redoStack[0]).toEqual(entry);
  });

  it('hiddenNodeIds is instanceof Set after undo', () => {
    const { actions, getState } = setup({
      undoStack: [{ type: 'hide', nodeId: 'n1' }],
      hiddenNodeIds: new Set(['n1']),
    });
    actions.undo();
    expect(getState().hiddenNodeIds).toBeInstanceOf(Set);
  });
});

// ── redo ─────────────────────────────────────────────────────────────────────

describe('redo', () => {
  it('no-op on empty stack', () => {
    const { actions, getState } = setup();
    actions.redo();
    expect(getState().redoStack).toHaveLength(0);
  });

  it('hide entry — re-adds nodeId to hiddenNodeIds', () => {
    const { actions, getState } = setup({
      redoStack: [{ type: 'hide', nodeId: 'n1' }],
      hiddenNodeIds: new Set<string>(),
    });
    actions.redo();
    expect(getState().hiddenNodeIds.has('n1')).toBe(true);
  });

  it('restore entry — removes nodeId from hiddenNodeIds', () => {
    const { actions, getState } = setup({
      redoStack: [{ type: 'restore', nodeId: 'n1' }],
      hiddenNodeIds: new Set(['n1']),
    });
    actions.redo();
    expect(getState().hiddenNodeIds.has('n1')).toBe(false);
  });

  it('showAll entry — clears hiddenNodeIds', () => {
    const { actions, getState } = setup({
      redoStack: [{ type: 'showAll', previousHidden: ['n1', 'n2'] }],
      hiddenNodeIds: new Set(['n1', 'n2']),
    });
    actions.redo();
    expect(getState().hiddenNodeIds.size).toBe(0);
  });

  it('expand entry — sets state to expanded', () => {
    const { actions, getState } = setup({
      redoStack: [{ type: 'expand', nodeId: 'n1', previousState: 'collapsed' }],
      nodeExpansionState: { n1: 'collapsed' },
    });
    actions.redo();
    expect(getState().nodeExpansionState.n1).toBe('expanded');
  });

  it('moves entry back to undoStack', () => {
    const entry: UndoEntry = { type: 'hide', nodeId: 'n1' };
    const { actions, getState } = setup({
      redoStack: [entry],
      hiddenNodeIds: new Set<string>(),
    });
    actions.redo();
    expect(getState().redoStack).toHaveLength(0);
    expect(getState().undoStack).toHaveLength(1);
  });
});

// ── clearHistory ─────────────────────────────────────────────────────────────

describe('clearHistory', () => {
  it('empties both stacks', () => {
    const { actions, getState } = setup({
      undoStack: [{ type: 'hide', nodeId: 'n1' }],
      redoStack: [{ type: 'hide', nodeId: 'n2' }],
    });
    actions.clearHistory();
    expect(getState().undoStack).toHaveLength(0);
    expect(getState().redoStack).toHaveLength(0);
  });
});

// ── Round-trip ───────────────────────────────────────────────────────────────

describe('round-trip', () => {
  it('push → undo → redo restores original state', () => {
    const { actions, getState } = setup({
      hiddenNodeIds: new Set<string>(),
    });
    // Push a hide, then simulate the hide
    actions.pushUndo({ type: 'hide', nodeId: 'n1' });
    (getState().hiddenNodeIds as Set<string>).add('n1');

    // Undo reverses the hide
    actions.undo();
    expect(getState().hiddenNodeIds.has('n1')).toBe(false);

    // Redo re-applies the hide
    actions.redo();
    expect(getState().hiddenNodeIds.has('n1')).toBe(true);
  });
});
