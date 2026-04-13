package studio.seer.dali.storage;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * ArcadeDB command body — mirrors the pattern in heimdall-backend.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FriggCommand(String language, String command, Map<String, Object> params) {}
