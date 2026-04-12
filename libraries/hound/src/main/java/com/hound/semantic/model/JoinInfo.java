package com.hound.semantic.model;

/**
 * Информация о JOIN-операции.
 * Аналог Python: join_info dict в PlSqlAnalyzerListener.
 *
 * STAB-5: добавлены resolvedSourceGeoid / resolvedTargetGeoid —
 * заполняются в postProcessJoins() после walk, когда scope ещё позволяет
 * искать алиасы. Это позволяет не терять resolved-результаты в отличие
 * от старой версии где postProcessJoins только логировал без сохранения.
 */
public record JoinInfo(
        String joinType,             // INNER JOIN, LEFT OUTER JOIN, RIGHT JOIN, FULL JOIN, CROSS JOIN, NATURAL JOIN
        String targetTableGeoid,
        String targetTableAlias,
        String targetType,           // "table" или "subquery"
        String sourceTableGeoid,
        String sourceTableAlias,
        String sourceType,
        String conditions,           // текст ON-условия
        String statementGeoid,
        int    lineStart,
        // STAB-5: resolved geoids заполняются в postProcessJoins
        String resolvedSourceGeoid,
        String resolvedTargetGeoid
) {
    /** Конструктор обратной совместимости (без resolved полей) */
    public JoinInfo(String joinType, String targetTableGeoid, String targetTableAlias,
                    String targetType, String sourceTableGeoid, String sourceTableAlias,
                    String sourceType, String conditions, String statementGeoid, int lineStart) {
        this(joinType, targetTableGeoid, targetTableAlias, targetType,
             sourceTableGeoid, sourceTableAlias, sourceType, conditions,
             statementGeoid, lineStart, null, null);
    }

    /**
     * Возвращает новый JoinInfo с заполненными resolved геоидами.
     * Используется в postProcessJoins() вместо просто логирования.
     */
    public JoinInfo withResolved(String srcGeoid, String tgtGeoid) {
        return new JoinInfo(
            joinType, targetTableGeoid, targetTableAlias, targetType,
            sourceTableGeoid, sourceTableAlias, sourceType,
            conditions, statementGeoid, lineStart,
            srcGeoid, tgtGeoid
        );
    }

    /** Эффективный source: resolvedSourceGeoid ?? sourceTableGeoid */
    public String effectiveSourceGeoid() {
        return resolvedSourceGeoid != null ? resolvedSourceGeoid : sourceTableGeoid;
    }

    /** Эффективный target: resolvedTargetGeoid ?? targetTableGeoid */
    public String effectiveTargetGeoid() {
        return resolvedTargetGeoid != null ? resolvedTargetGeoid : targetTableGeoid;
    }
}
