package com.hound.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Утилиты для вычисления хешей
 */
public class HashUtils {

    private static final String DEFAULT_ALGORITHM = "MD5";

    /**
     * Вычисляет MD5 хеш строки
     */
    public static String md5(String input) {
        return hash(input, "MD5");
    }

    /**
     * Вычисляет SHA-256 хеш строки
     */
    public static String sha256(String input) {
        return hash(input, "SHA-256");
    }

    /**
     * Вычисляет хеш файла
     */
    public static String fileHash(Path path, String algorithm) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] buffer = new byte[8192];
            int bytesRead;

            try (InputStream is = Files.newInputStream(path)) {
                while ((bytesRead = is.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }

            return bytesToHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algorithm not found: " + algorithm, e);
        }
    }

    /**
     * Быстрый хеш файла (первые 1MB)
     */
    public static String fastFileHash(Path path) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalRead = 0;
            long maxBytes = 1024 * 1024; // 1 MB

            try (InputStream is = Files.newInputStream(path)) {
                while ((bytesRead = is.read(buffer)) != -1 && totalRead < maxBytes) {
                    digest.update(buffer, 0, bytesRead);
                    totalRead += bytesRead;
                }
            }

            return bytesToHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    /**
     * Вычисляет хеш строки
     */
    private static String hash(String input, String algorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algorithm not found: " + algorithm, e);
        }
    }

    /**
     * Конвертирует байты в hex строку
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}