package com.hound.processor;

import com.hound.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Сканер директорий — тонкая обёртка над FileUtils.
 * Не содержит дублирующей логики.
 */
public class DirectoryScanner {

    private static final Logger logger = LoggerFactory.getLogger(DirectoryScanner.class);

    /**
     * Сканирует директорию или файл по списку расширений
     */
    public List<Path> scan(String startPath, List<String> extensions) throws IOException {
        return scan(Path.of(startPath), extensions);
    }

    public List<Path> scan(Path startPath, List<String> extensions) throws IOException {
        logger.info("Начало сканирования: {}", startPath);

        List<Path> files = FileUtils.findFiles(startPath, extensions, 100 * 1024 * 1024); // 100 МБ по умолчанию

        logger.info("Сканирование завершено. Найдено файлов: {}", files.size());
        return files;
    }

    /**
     * Сканирование с явным ограничением размера
     */
    public List<Path> scanWithSizeLimit(Path startPath, List<String> extensions, long maxSizeBytes) throws IOException {
        logger.info("Сканирование с лимитом {} МБ: {}", maxSizeBytes / (1024 * 1024), startPath);
        return FileUtils.findFiles(startPath, extensions, maxSizeBytes);
    }
}