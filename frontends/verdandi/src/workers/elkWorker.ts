/// <reference lib="webworker" />
//
// ELK layout worker — runs elkjs off the main thread.
//
// Protocol:
//   Main → Worker  { id: number; graph: ElkGraph }
//   Worker → Main  { id: number; result: ElkGraph }  on success
//                  { id: number; error: string }      on failure
//
// We lazy-import elk.bundled.js (pure JS) via dynamic import() so any CJS
// transform errors are caught inside the try/catch rather than crashing the
// Worker at module evaluation time.
//

interface ElkApi {
  layout: (g: unknown) => Promise<unknown>;
}

let elkInstance: ElkApi | null = null;

async function getElk(): Promise<ElkApi> {
  if (elkInstance) return elkInstance;
  // Prevent elkjs from trying to create nested Workers inside this Worker.
  // elk.bundled.js checks for `Worker` availability; Vite's CJS→ESM transform
  // renames it to `_Worker` which then fails as "not a constructor".
  // Stubbing it forces elkjs to use its synchronous (single-threaded) path.
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  (self as any).Worker = undefined;

  // Dynamic import — Vite transforms CJS → ESM at request time.
  // @ts-expect-error — elkjs doesn't ship proper ESM types for elk.bundled
  const mod = await import('elkjs/lib/elk.bundled.js');
  const ELK = (mod.default ?? mod) as unknown as new () => ElkApi;
  elkInstance = new ELK();
  return elkInstance;
}

self.onmessage = async (e: MessageEvent<{ id: number; graph: unknown }>) => {
  const { id, graph } = e.data;
  try {
    const elk = await getElk();
    const result = await elk.layout(graph);
    self.postMessage({ id, result });
  } catch (err: unknown) {
    self.postMessage({ id, error: String(err) });
  }
};
