package studio.seer.lineage.client.dali.model;

/**
 * Response from {@code POST /api/sessions/{id}/cancel}.
 *
 * @param status  CANCELLING | NOT_FOUND | ALREADY_DONE | UNAVAILABLE
 * @param message Human-readable detail from Dali
 */
public record CancelResponse(String status, String message) {}
