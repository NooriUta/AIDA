package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;

import java.util.List;

/**
 * KNOT — extra details for one DaliStatement beyond what the L2 explore query
 * returns. Loaded lazily when the LOOM Inspector "Дополнительно" tab is opened.
 *
 * <ul>
 *   <li>{@code descendants} — all recursive CHILD_OF descendants (sub-queries,
 *     CTEs, inline views, FOR-cursors). Ordered by a BFS/DFS traversal of the
 *     CHILD_OF tree, depth is not explicitly returned — the frontend can rebuild
 *     indentation from {@code parentStmtGeoid}.</li>
 *   <li>{@code atomContexts} — DaliAtom counts for the root statement grouped
 *     by {@code parent_context} (JOIN / SELECT / WHERE / SUBQUERY / CTE / …).</li>
 *   <li>{@code totalAtomCount} — sum of all {@code atomContexts[*].count}.</li>
 *   <li>{@code sourceTables} — every DaliTable this stmt reads from, tagged
 *     with DIRECT (root READS_FROM) vs SUBQUERY (a descendant reads it and
 *     we surface it here because the root INSERT-SELECT conceptually owns
 *     the source). Lets the user answer "what feeds this stmt" regardless
 *     of whether the reads happen at the root or in nested CTEs.</li>
 * </ul>
 */
@Description("KNOT — recursive subquery tree + atom statistics + source tables for one DaliStatement")
public record StatementExtras(
    List<SubqueryInfo>     descendants,
    List<AtomContextCount> atomContexts,
    int                    totalAtomCount,
    List<SourceTableRef>   sourceTables
) {}
