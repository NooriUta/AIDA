package com.hound.semantic.engine;

import com.hound.semantic.model.JoinInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * JoinProcessor — сбор и обработка JOIN-операций.
 * Аналог Python: _store_join_info, _get_join_type, _get_join_conditions.
 */
public class JoinProcessor {

    private static final Logger logger = LoggerFactory.getLogger(JoinProcessor.class);

    // statement geoid → list of JoinInfo
    private final Map<String, List<JoinInfo>> joinsByStatement = new LinkedHashMap<>();

    /**
     * Регистрирует JOIN-операцию.
     */
    public void registerJoin(String statementGeoid, JoinInfo joinInfo) {
        joinsByStatement.computeIfAbsent(statementGeoid, k -> new ArrayList<>()).add(joinInfo);
        logger.debug("JOIN registered: {} {} → {} ({})",
                joinInfo.joinType(),
                joinInfo.sourceTableAlias() != null ? joinInfo.sourceTableAlias() : joinInfo.sourceTableGeoid(),
                joinInfo.targetTableAlias() != null ? joinInfo.targetTableAlias() : joinInfo.targetTableGeoid(),
                statementGeoid);
    }

    /**
     * Возвращает все JOIN'ы для statement.
     */
    public List<JoinInfo> getJoinsForStatement(String statementGeoid) {
        return joinsByStatement.getOrDefault(statementGeoid, List.of());
    }

    /**
     * Возвращает все JOIN'ы.
     */
    public Map<String, List<JoinInfo>> getAllJoins() {
        return Collections.unmodifiableMap(joinsByStatement);
    }

    /**
     * Определяет тип JOIN из текста.
     * Аналог Python: _get_join_type(ctx)
     */
    public static String inferJoinType(String joinText) {
        if (joinText == null) return "INNER JOIN";
        String upper = joinText.toUpperCase().trim();

        if (upper.contains("NATURAL")) return "NATURAL JOIN";
        if (upper.contains("CROSS"))   return "CROSS JOIN";
        if (upper.contains("FULL"))    return "FULL OUTER JOIN";
        if (upper.contains("RIGHT"))   return "RIGHT OUTER JOIN";
        if (upper.contains("LEFT"))    return "LEFT OUTER JOIN";
        if (upper.contains("INNER"))   return "INNER JOIN";

        // Default для JOIN без модификатора
        return "INNER JOIN";
    }

    public void clear() {
        joinsByStatement.clear();
    }
}