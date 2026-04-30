package studio.seer.lineage.service;

import studio.seer.lineage.model.KnotParameter;
import studio.seer.lineage.model.KnotRoutine;
import studio.seer.lineage.model.KnotVariable;

import java.util.List;

/**
 * Internal holder: routines + parameters + variables loaded together for a KNOT report session.
 *
 * <p>Extracted from the inner record {@code KnotColumnLineageService.KnotParamVars}
 * (LOC refactor — QG-ARCH-INVARIANTS §2.4) to allow shared use by
 * {@link KnotBulkLoaders} and {@link KnotService}.
 */
record KnotParamVars(List<KnotRoutine> routines, List<KnotParameter> parameters, List<KnotVariable> variables) {}
