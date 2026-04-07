/// <reference lib="webworker" />
//
// ELK layout worker — runs elk.bundled.js off the main thread.
//
// Protocol:
//   Main → Worker  { id: number; graph: ElkGraph }
//   Worker → Main  { id: number; result: ElkGraph }  on success
//                  { id: number; error: string }      on failure
//

import ELK from 'elkjs/lib/elk.bundled.js';

const elk = new (ELK as unknown as new () => { layout: (g: unknown) => Promise<unknown> })();

self.onmessage = async (e: MessageEvent<{ id: number; graph: unknown }>) => {
  const { id, graph } = e.data;
  try {
    const result = await elk.layout(graph);
    self.postMessage({ id, result });
  } catch (err) {
    self.postMessage({ id, error: String(err) });
  }
};
