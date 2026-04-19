package com.skadi;

/** Запрос превысил таймаут. */
public class SkadiFetchTimeoutException extends SkadiFetchException {

    private final long timeoutMs;

    public SkadiFetchTimeoutException(
            String message, String adapterName, String objectName,
            long timeoutMs, Throwable cause) {
        super(message, adapterName, objectName, cause);
        this.timeoutMs = timeoutMs;
    }

    public long getTimeoutMs() { return timeoutMs; }
}
