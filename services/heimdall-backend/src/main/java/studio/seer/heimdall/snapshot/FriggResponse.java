package studio.seer.heimdall.snapshot;

import java.util.List;
import java.util.Map;

/**
 * ArcadeDB HTTP response wrapper.
 */
public record FriggResponse(List<Map<String, Object>> result) {}
