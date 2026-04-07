import { useEffect } from 'react';

import { useLoomStore }                                      from '../../stores/loomStore';
import { useUpstream, useDownstream, useExpandDeep }         from '../../services/hooks';

/**
 * Handles upstream / downstream / deep-expand query lifecycle.
 * Writes results into the store (addExpansionData) and clears the request
 * when the query resolves or errors — preventing the expand button from
 * freezing in a loading state.
 */
export function useExpansion(): void {
  const {
    expandRequest,
    addExpansionData,
    clearExpandRequest,
    deepExpandRequest,
    clearDeepExpandRequest,
    requestFocusNode,
  } = useLoomStore();

  const upstreamExpandId   = expandRequest?.direction === 'upstream'   ? expandRequest.nodeId : null;
  const downstreamExpandId = expandRequest?.direction === 'downstream' ? expandRequest.nodeId : null;

  const {
    data: upstreamExpandData,
    isSuccess: upstreamOk,
    isError: upstreamExpandError,
  } = useUpstream(upstreamExpandId);

  const {
    data: downstreamExpandData,
    isSuccess: downstreamOk,
    isError: downstreamExpandError,
  } = useDownstream(downstreamExpandId);

  useEffect(() => {
    if (upstreamOk && upstreamExpandData && upstreamExpandId) {
      addExpansionData(upstreamExpandId, 'upstream', upstreamExpandData.nodes, upstreamExpandData.edges);
      clearExpandRequest();
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [upstreamOk, upstreamExpandData, upstreamExpandId]);

  useEffect(() => {
    if (downstreamOk && downstreamExpandData && downstreamExpandId) {
      addExpansionData(downstreamExpandId, 'downstream', downstreamExpandData.nodes, downstreamExpandData.edges);
      clearExpandRequest();
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [downstreamOk, downstreamExpandData, downstreamExpandId]);

  // Clear stuck request when expand query fails (button would freeze in loading state)
  useEffect(() => {
    if (upstreamExpandError && upstreamExpandId) clearExpandRequest();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [upstreamExpandError, upstreamExpandId]);

  useEffect(() => {
    if (downstreamExpandError && downstreamExpandId) clearExpandRequest();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [downstreamExpandError, downstreamExpandId]);

  // ── Deep expand (search auto-expand: multi-hop READS_FROM/WRITES_TO) ────────
  const deepExpandNodeId = deepExpandRequest?.nodeId ?? null;
  const deepExpandDepth  = deepExpandRequest?.depth  ?? 5;

  const {
    data: deepExpandData,
    isSuccess: deepExpandOk,
    isError: deepExpandError,
  } = useExpandDeep(deepExpandNodeId, deepExpandDepth);

  useEffect(() => {
    if (deepExpandOk && deepExpandData && deepExpandNodeId) {
      addExpansionData(deepExpandNodeId, 'upstream', deepExpandData.nodes, deepExpandData.edges);
      // Re-focus the originating node after the second ELK layout that addExpansionData triggers.
      requestFocusNode(deepExpandNodeId);
      clearDeepExpandRequest();
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [deepExpandOk, deepExpandData, deepExpandNodeId]);

  useEffect(() => {
    if (deepExpandError && deepExpandNodeId) clearDeepExpandRequest();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [deepExpandError, deepExpandNodeId]);
}
