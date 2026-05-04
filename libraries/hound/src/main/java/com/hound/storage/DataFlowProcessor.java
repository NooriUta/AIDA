package com.hound.storage;

import com.hound.semantic.model.AtomInfo;
import com.hound.semantic.model.StatementInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * H1.5 — DataFlowProcessor.
 *
 * Post-walk processor: for each resolved atom that has an output column sequence,
 * builds a DATA_FLOW record (DaliColumn → DaliOutputColumn).
 *
 * This class produces a flat list of flow records for Arrow serialization or
 * remote ArcadeDB insertion. The embedded ArcadeDB path writes DATA_FLOW edges
 * inline in ArcadeDBSemanticWriter.
 */
public class DataFlowProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DataFlowProcessor.class);

    /**
     * Builds DATA_FLOW records from resolved atoms across all statements.
     *
     * @param statements all statement infos keyed by geoid
     * @param sessionId  session identifier
     * @return list of DATA_FLOW record maps, ready for ArcadeDB or Arrow
     */
    public List<Map<String, Object>> buildDataFlowRecords(
            Map<String, StatementInfo> statements, String sessionId) {

        List<Map<String, Object>> records = new ArrayList<>();

        for (var stmtEntry : statements.entrySet()) {
            String stmtGeoid = stmtEntry.getKey();
            StatementInfo stmt = stmtEntry.getValue();

            for (var atomEntry : stmt.getAtoms().entrySet()) {
                String atomText = atomEntry.getKey();
                Map<String, Object> a = atomEntry.getValue();

                // Only resolved atoms with an output column association
                String ps = (String) a.get("primary_status");
                if (ps == null) ps = (String) a.get("status");
                if (!AtomInfo.STATUS_RESOLVED.equals(ps)) continue;
                Object outSeq = a.get("output_column_sequence");
                if (outSeq == null) continue;

                String tableGeoid  = (String) a.get("table_geoid");
                String columnName  = (String) a.get("column_name");
                if (tableGeoid == null || columnName == null) continue;

                String srcColGeoid = tableGeoid + "." + columnName.toUpperCase();
                String outColKey   = stmtGeoid + ":" + outSeq;

                Map<String, Object> record = new LinkedHashMap<>();
                record.put("session_id",       sessionId);
                record.put("statement_geoid",  stmtGeoid);
                record.put("src_col_geoid",    srcColGeoid);
                record.put("output_col_key",   outColKey);
                record.put("atom_text",        atomText);
                record.put("flow_type",        resolveFlowType(a, stmt));
                records.add(record);
            }
        }

        logger.debug("DataFlowProcessor: {} DATA_FLOW records built for session={}",
                records.size(), sessionId);
        return records;
    }

    private static String resolveFlowType(Map<String, Object> atom, StatementInfo stmt) {
        if (stmt != null && stmt.isHasAggregation()
                && "GROUP BY".equals(atom.get("parent_context"))) return "AGGREGATE";
        if (Boolean.TRUE.equals(atom.get("is_function_call")))    return "TRANSFORM";
        return "DIRECT";
    }
}
