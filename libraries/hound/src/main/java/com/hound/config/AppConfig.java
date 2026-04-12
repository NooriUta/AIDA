package com.hound.config;

import java.util.List;

/**
 * Конфигурация приложения (IMP-5: добавлена валидация).
 */
public class AppConfig {
    private String inputPath;
    private String dbType = "neo4j";
    private String dbHost = "localhost";
    private int dbPort = 7687;
    private String dbName = "hound";
    private String dbUser;
    private String dbPassword;
    private int threads = Runtime.getRuntime().availableProcessors();
    private int batchSize = 1000;
    private String cachePath = ".hound-cache";
    private List<String> fileExtensions = List.of(".sql", ".plsql");
    private boolean incrementalMode = false;
    private int maxFileSizeMB = 100;
    private String forcedLanguage = null;

    public void validate() {
        if (inputPath == null || inputPath.isBlank())
            throw new IllegalArgumentException("Input path is required");
        if (dbPort < 1 || dbPort > 65535)
            throw new IllegalArgumentException("Invalid port: " + dbPort);
        if (threads <= 0)
            throw new IllegalArgumentException("Threads must be > 0");
        if (batchSize <= 0)
            throw new IllegalArgumentException("Batch size must be > 0");
    }

    // Getters and Setters (без изменений)
    public String getInputPath() { return inputPath; }
    public void setInputPath(String inputPath) { this.inputPath = inputPath; }

    public String getDbType() { return dbType; }
    public void setDbType(String dbType) { this.dbType = dbType; }

    public String getDbHost() { return dbHost; }
    public void setDbHost(String dbHost) { this.dbHost = dbHost; }

    public int getDbPort() { return dbPort; }
    public void setDbPort(int dbPort) { this.dbPort = dbPort; }

    public String getDbName() { return dbName; }
    public void setDbName(String dbName) { this.dbName = dbName; }

    public String getDbUser() { return dbUser; }
    public void setDbUser(String dbUser) { this.dbUser = dbUser; }

    public String getDbPassword() { return dbPassword; }
    public void setDbPassword(String dbPassword) { this.dbPassword = dbPassword; }

    public int getThreads() { return threads; }
    public void setThreads(int threads) { this.threads = threads; }

    public int getBatchSize() { return batchSize; }
    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }

    public String getCachePath() { return cachePath; }
    public void setCachePath(String cachePath) { this.cachePath = cachePath; }

    public List<String> getFileExtensions() { return fileExtensions; }
    public void setFileExtensions(List<String> fileExtensions) { this.fileExtensions = fileExtensions; }

    public boolean isIncrementalMode() { return incrementalMode; }
    public void setIncrementalMode(boolean incrementalMode) { this.incrementalMode = incrementalMode; }

    public int getMaxFileSizeMB() { return maxFileSizeMB; }
    public void setMaxFileSizeMB(int maxFileSizeMB) { this.maxFileSizeMB = maxFileSizeMB; }

    public String getForcedLanguage() { return forcedLanguage; }
    public void setForcedLanguage(String forcedLanguage) { this.forcedLanguage = forcedLanguage; }
}