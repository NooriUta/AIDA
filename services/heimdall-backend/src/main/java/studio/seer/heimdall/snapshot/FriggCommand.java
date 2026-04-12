package studio.seer.heimdall.snapshot;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * ArcadeDB command body — same structure as ArcadeCommand in SHUTTLE.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FriggCommand(String language, String command, Map<String, Object> params) {}
