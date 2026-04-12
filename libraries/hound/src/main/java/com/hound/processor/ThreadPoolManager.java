package com.hound.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Менеджер пула потоков (DEF-2: исправлена утечка потока).
 */
public class ThreadPoolManager {
    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolManager.class);

    private final ExecutorService executorService;
    private final int threadCount;
    private final AtomicInteger activeTasks = new AtomicInteger(0);

    public ThreadPoolManager(int threadCount) {
        this.threadCount = threadCount;
        this.executorService = Executors.newFixedThreadPool(threadCount, r -> {
            Thread t = new Thread(r);
            t.setName("hound-worker-" + (new AtomicInteger(0).incrementAndGet()));
            t.setDaemon(false);
            t.setPriority(Thread.NORM_PRIORITY);
            return t;
        });
        logger.info("Thread pool initialized with {} threads", threadCount);
    }

    public void submit(Runnable task) {
        activeTasks.incrementAndGet();
        executorService.submit(() -> {
            try {
                task.run();
            } finally {
                activeTasks.decrementAndGet();
            }
        });
    }

    public <T> Future<T> submit(Callable<T> task) {
        activeTasks.incrementAndGet();
        return executorService.submit(() -> {
            try {
                return task.call();
            } finally {
                activeTasks.decrementAndGet();
            }
        });
    }

    public void shutdownAndWait() {
        logger.info("Shutting down thread pool...");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                executorService.awaitTermination(10, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("Thread pool shut down");
    }

    public void shutdownNow() {
        executorService.shutdownNow();
    }

    public int getActiveTaskCount() {
        return activeTasks.get();
    }

    public boolean isAllTasksCompleted() {
        return activeTasks.get() == 0;
    }

    public static ThreadPoolManager newFixedThreadPool(int threadCount) {
        return new ThreadPoolManager(threadCount);
    }
}