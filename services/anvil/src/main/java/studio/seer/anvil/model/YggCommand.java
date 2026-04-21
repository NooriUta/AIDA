package studio.seer.anvil.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record YggCommand(String language, String command, Map<String, Object> params) {}
