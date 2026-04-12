package com.hound.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Утилиты для работы с файлами — основной класс для сканирования и чтения.
 */
public class FileUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    private static final long DEFAULT_MAX_FILE_SIZE = 100 * 1024 * 1024; // 100 МБ

    /**
     * Рекурсивный поиск файлов по расширениям с ограничением размера
     */
    public static List<Path> findFiles(Path startPath, List<String> extensions, long maxSize) throws IOException {
        List<Path> files = new ArrayList<>();

        if (!Files.exists(startPath)) {
            throw new IllegalArgumentException("Путь не существует: " + startPath);
        }

        try (Stream<Path> stream = Files.walk(startPath)) {
            stream
                    .filter(Files::isRegularFile)
                    .filter(path -> matchesExtension(path, extensions))
                    .filter(path -> isWithinSizeLimit(path, maxSize))
                    .forEach(files::add);
        }

        logger.info("Найдено {} файлов в директории {}", files.size(), startPath);
        return files;
    }

    /**
     * Проверка, соответствует ли файл одному из расширений
     */
    public static boolean matchesExtension(Path path, List<String> extensions) {
        if (extensions == null || extensions.isEmpty()) {
            return true;
        }

        String fileName = path.toString().toLowerCase();
        return extensions.stream().anyMatch(ext -> fileName.endsWith(ext.toLowerCase()));
    }

    /**
     * Проверка размера файла
     */
    public static boolean isWithinSizeLimit(Path path, long maxSize) {
        try {
            long size = Files.size(path);
            if (size > maxSize) {
                logger.warn("Файл {} превышает ограничение размера: {} > {}", path, size, maxSize);
                return false;
            }
            return true;
        } catch (IOException e) {
            logger.error("Не удалось получить размер файла: {}", path, e);
            return false;
        }
    }

    /**
     * Безопасное чтение файла с указанной кодировкой и проверкой размера
     */
    public static String readFileSafe(Path path, String encoding, long maxSize) throws IOException {
        long size = Files.size(path);
        if (size > maxSize) {
            throw new IOException(String.format("Файл слишком большой: %d байт (лимит %d байт)", size, maxSize));
        }
        return Files.readString(path, java.nio.charset.Charset.forName(encoding));
    }

    /**
     * Чтение файла (упрощённый вариант)
     */
    public static String readFile(Path path, String encoding) throws IOException {
        return Files.readString(path, java.nio.charset.Charset.forName(encoding));
    }

    /**
     * Получение относительного пути
     */
    public static String getRelativePath(Path file, Path baseDir) {
        try {
            return baseDir.relativize(file).toString();
        } catch (IllegalArgumentException e) {
            return file.toString();
        }
    }

    /**
     * Удаление директории рекурсивно
     */
    public static void deleteDirectory(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }
}