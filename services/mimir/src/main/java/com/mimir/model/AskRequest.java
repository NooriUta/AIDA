package com.mimir.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AskRequest(
    @NotBlank String question,
    @NotBlank String sessionId,
    String dbName,
    @Size(max = 10) int maxToolCalls
) {
    public AskRequest {
        if (maxToolCalls <= 0) maxToolCalls = 5;
    }
}
