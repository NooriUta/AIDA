package studio.seer.dali.service;

/**
 * Result of a session cancellation request.
 *
 * @param status  One of: CANCELLING, NOT_FOUND, ALREADY_DONE
 * @param message Human-readable detail
 */
public record CancelResult(String status, String message) {}
