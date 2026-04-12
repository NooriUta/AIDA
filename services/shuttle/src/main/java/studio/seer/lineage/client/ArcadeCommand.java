package studio.seer.lineage.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ArcadeCommand(
    String language,
    String command,
    Map<String, Object> params
) {}
