package studio.seer.dali.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.seer.dali.rest.SourceDTO;
import studio.seer.dali.rest.SourceDTO.SchemaFilter;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** CRUD for JDBC sources stored in FRIGG ({@code dali_sources} document type). */
@ApplicationScoped
public class SourceRepository {

    private static final Logger log = LoggerFactory.getLogger(SourceRepository.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Inject FriggGateway frigg;

    public List<SourceDTO> findAll() {
        return frigg.sql("SELECT FROM dali_sources ORDER BY createdAt ASC")
                .stream().map(this::toDTO).toList();
    }

    public Optional<SourceDTO> findById(String id) {
        var rows = frigg.sql("SELECT FROM dali_sources WHERE id = :id", Map.of("id", id));
        return rows.isEmpty() ? Optional.empty() : Optional.of(toDTO(rows.get(0)));
    }

    public SourceDTO create(String name, String dialect, String jdbcUrl,
                            String username, String password, SchemaFilter schemaFilter) {
        String id = UUID.randomUUID().toString();
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("id",         id);
        p.put("name",       name);
        p.put("dialect",    dialect);
        p.put("jdbcUrl",    jdbcUrl);
        p.put("username",   username);
        p.put("password",   password);
        p.put("atomCount",  0);
        p.put("lastHarvest", null);
        p.put("schemaInclude", toJson(schemaFilter != null ? schemaFilter.include() : List.of()));
        p.put("schemaExclude", toJson(schemaFilter != null ? schemaFilter.exclude() : List.of()));
        p.put("createdAt",  Instant.now().toString());
        frigg.sql(
            "INSERT INTO dali_sources SET id = :id, name = :name, dialect = :dialect, " +
            "jdbcUrl = :jdbcUrl, username = :username, password = :password, " +
            "atomCount = :atomCount, lastHarvest = :lastHarvest, " +
            "schemaInclude = :schemaInclude, schemaExclude = :schemaExclude, " +
            "createdAt = :createdAt", p);
        return findById(id).orElseThrow(() -> new IllegalStateException("Source not found after insert: " + id));
    }

    public Optional<SourceDTO> update(String id, String name, String dialect, String jdbcUrl,
                                      String username, String password, SchemaFilter schemaFilter) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("id",      id);
        p.put("name",    name);
        p.put("dialect", dialect);
        p.put("jdbcUrl", jdbcUrl);
        p.put("username", username);
        p.put("schemaInclude", toJson(schemaFilter != null ? schemaFilter.include() : List.of()));
        p.put("schemaExclude", toJson(schemaFilter != null ? schemaFilter.exclude() : List.of()));
        String sql = "UPDATE dali_sources SET name = :name, dialect = :dialect, " +
            "jdbcUrl = :jdbcUrl, username = :username, " +
            "schemaInclude = :schemaInclude, schemaExclude = :schemaExclude WHERE id = :id";
        if (password != null && !password.isBlank()) {
            p.put("password", password);
            sql = "UPDATE dali_sources SET name = :name, dialect = :dialect, " +
                "jdbcUrl = :jdbcUrl, username = :username, password = :password, " +
                "schemaInclude = :schemaInclude, schemaExclude = :schemaExclude WHERE id = :id";
        }
        frigg.sql(sql, p);
        return findById(id);
    }

    public void delete(String id) {
        frigg.sql("DELETE FROM dali_sources WHERE id = :id", Map.of("id", id));
    }

    /** Update atom count and last harvest timestamp after a successful harvest. */
    public void recordHarvest(String id, int atomCount) {
        frigg.sql("UPDATE dali_sources SET atomCount = :atoms, lastHarvest = :ts WHERE id = :id",
            Map.of("id", id, "atoms", atomCount, "ts", Instant.now().toString()));
    }

    // ── Internal helpers ─────────────────────────────────────────────────────────

    private SourceDTO toDTO(Map<String, Object> row) {
        return new SourceDTO(
            str(row, "id"),
            str(row, "name"),
            str(row, "dialect"),
            str(row, "jdbcUrl"),
            str(row, "username"),
            str(row, "lastHarvest"),
            row.get("atomCount") instanceof Number n ? n.intValue() : 0,
            new SchemaFilter(
                fromJson(str(row, "schemaInclude")),
                fromJson(str(row, "schemaExclude"))
            )
        );
    }

    private static String str(Map<String, Object> row, String key) {
        Object v = row.get(key);
        return v != null ? v.toString() : null;
    }

    private String toJson(List<String> list) {
        try { return MAPPER.writeValueAsString(list != null ? list : List.of()); }
        catch (JsonProcessingException e) { return "[]"; }
    }

    private List<String> fromJson(String json) {
        if (json == null || json.isBlank()) return List.of();
        try { return MAPPER.readValue(json, new TypeReference<>() {}); }
        catch (Exception e) {
            log.warn("SourceRepository: failed to parse schemaFilter JSON '{}': {}", json, e.getMessage());
            return List.of();
        }
    }
}
