package studio.seer.dali.rest;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/** JDBC source record returned by the REST API (password is never included). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SourceDTO(
        String id,
        String name,
        String dialect,
        String jdbcUrl,
        String username,
        String lastHarvest,
        Integer atomCount,
        SchemaFilter schemaFilter
) {
    public record SchemaFilter(List<String> include, List<String> exclude) {
        public SchemaFilter {
            include = include != null ? include : List.of();
            exclude = exclude != null ? exclude : List.of();
        }
    }
}
