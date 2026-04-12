package com.hound.util;

import java.util.regex.Pattern;

/**
 * Утилиты для валидации данных
 */
public class ValidationUtils {

    private static final Pattern VALID_IDENTIFIER = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_$#]*$");
    private static final Pattern VALID_HOST = Pattern.compile("^[a-zA-Z0-9.-]+$");
    private static final Pattern VALID_PORT = Pattern.compile("^\\d+$");

    /**
     * Проверка валидности идентификатора
     */
    public static boolean isValidIdentifier(String identifier) {
        if (identifier == null) return false;
        return VALID_IDENTIFIER.matcher(identifier).matches();
    }

    /**
     * Проверка хоста
     */
    public static boolean isValidHost(String host) {
        if (host == null) return false;
        return VALID_HOST.matcher(host).matches();
    }

    /**
     * Проверка порта
     */
    public static boolean isValidPort(int port) {
        return port > 0 && port <= 65535;
    }

    /**
     * Проверка размера файла
     */
    public static boolean isValidFileSize(long size, long maxSize) {
        return size > 0 && size <= maxSize;
    }

    /**
     * Проверка конфигурации БД
     */
    public static void validateDatabaseConfig(String host, int port, String database) {
        if (!isValidHost(host)) {
            throw new IllegalArgumentException("Invalid host: " + host);
        }
        if (!isValidPort(port)) {
            throw new IllegalArgumentException("Invalid port: " + port);
        }
        if (isBlank(database)) {
            throw new IllegalArgumentException("Database name cannot be empty");
        }
    }

    /**
     * Экранирование для логов
     */
    public static String sanitizeForLog(String value) {
        if (value == null) return "";
        return value.replaceAll("[\\r\\n]", " ")
                .replaceAll("[^\\x20-\\x7E]", "?");
    }

    private static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}