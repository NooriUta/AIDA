package com.hound.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Кэш обработанных файлов (IMP-3: буферизация saveCache).
 */
public class FileCache {

    private static final Logger logger = LoggerFactory.getLogger(FileCache.class);
    private static final String CACHE_FILE = "cache.json";
    private static final int SAVE_EVERY = 50;

    private final Path cacheDir;
    private final ConcurrentHashMap<String, CacheEntry> cacheMap;
    private final ReentrantReadWriteLock lock;
    private final ObjectMapper objectMapper;
    private final AtomicInteger dirtyCount = new AtomicInteger(0);

    public FileCache(String cacheDirPath) throws IOException {
        this.cacheDir = Paths.get(cacheDirPath);
        this.cacheMap = new ConcurrentHashMap<>();
        this.lock = new ReentrantReadWriteLock();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        Files.createDirectories(this.cacheDir);
        loadCache();
        logger.info("Кэш инициализирован. Загружено записей: {}", cacheMap.size());
    }

    public boolean isProcessed(String filePath, String fileHash) {
        lock.readLock().lock();
        try {
            CacheEntry entry = cacheMap.get(filePath);
            if (entry == null) return false;
            if (!entry.getFileHash().equals(fileHash)) return false;
            if (entry.isExpired()) return false;
            return true;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void markProcessed(String filePath, String fileHash) {
        lock.writeLock().lock();
        try {
            cacheMap.put(filePath, new CacheEntry(filePath, fileHash));
            if (dirtyCount.incrementAndGet() >= SAVE_EVERY) {
                saveCache();
                dirtyCount.set(0);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void invalidate(String filePath) {
        lock.writeLock().lock();
        try {
            cacheMap.remove(filePath);
            saveCache();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void cleanup() {
        lock.writeLock().lock();
        try {
            cacheMap.entrySet().removeIf(entry -> entry.getValue().isExpired());
            saveCache();
            logger.debug("Очистка кэша завершена");
        } finally {
            lock.writeLock().unlock();
        }
    }

    public FileCache.CacheStats getStats() {
        lock.readLock().lock();
        try {
            long active = cacheMap.values().stream().filter(e -> !e.isExpired()).count();
            long expired = cacheMap.values().stream().filter(CacheEntry::isExpired).count();
            return new FileCache.CacheStats(cacheMap.size(), active, expired);
        } finally {
            lock.readLock().unlock();
        }
    }

    private void loadCache() {
        Path cacheFile = cacheDir.resolve(CACHE_FILE);
        if (Files.exists(cacheFile)) {
            try {
                CacheEntry[] entries = objectMapper.readValue(cacheFile.toFile(), CacheEntry[].class);
                for (CacheEntry entry : entries) {
                    cacheMap.put(entry.getFilePath(), entry);
                }
                logger.info("Загружено {} записей из кэша", cacheMap.size());
            } catch (IOException e) {
                logger.warn("Не удалось загрузить кэш из файла", e);
            }
        }
    }

    private void saveCache() {
        Path cacheFile = cacheDir.resolve(CACHE_FILE);
        try {
            objectMapper.writeValue(cacheFile.toFile(), cacheMap.values().toArray());
        } catch (IOException e) {
            logger.error("Не удалось сохранить кэш", e);
        }
    }

    public static class CacheStats {
        private final long total;
        private final long active;
        private final long expired;

        public CacheStats(long total, long active, long expired) {
            this.total = total;
            this.active = active;
            this.expired = expired;
        }

        public long getTotal() { return total; }
        public long getActive() { return active; }
        public long getExpired() { return expired; }

        @Override
        public String toString() {
            return String.format("CacheStats{всего=%d, активных=%d, устаревших=%d}", total, active, expired);
        }
    }
}