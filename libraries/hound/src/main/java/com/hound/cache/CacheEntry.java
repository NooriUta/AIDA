package com.hound.cache;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Представляет одну запись в кэше
 */
public class CacheEntry {
    private static final long DEFAULT_TTL_HOURS = 24;

    private final String filePath;
    private final String fileHash;
    private final Instant timestamp;
    private final long ttlHours;

    public CacheEntry(String filePath, String fileHash) {
        this(filePath, fileHash, Instant.now(), DEFAULT_TTL_HOURS);
    }

    @JsonCreator
    public CacheEntry(
            @JsonProperty("filePath") String filePath,
            @JsonProperty("fileHash") String fileHash,
            @JsonProperty("timestamp") Instant timestamp,
            @JsonProperty("ttlHours") long ttlHours) {
        this.filePath = filePath;
        this.fileHash = fileHash;
        this.timestamp = timestamp;
        this.ttlHours = ttlHours;
    }

    public String getFilePath() { return filePath; }
    public String getFileHash() { return fileHash; }
    public Instant getTimestamp() { return timestamp; }
    public long getTtlHours() { return ttlHours; }

    /**
     * Проверяет, не истек ли срок действия записи
     */
    public boolean isExpired() {
        return Instant.now().isAfter(timestamp.plusSeconds(ttlHours * 3600));
    }

    @Override
    public String toString() {
        return String.format("CacheEntry{file='%s', hash='%s', timestamp=%s, ttl=%dh}",
                filePath, fileHash, timestamp, ttlHours);
    }
}