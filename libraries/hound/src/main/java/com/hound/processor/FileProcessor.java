// src/main/java/com/hound/processor/FileProcessor.java
package com.hound.processor;

import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.semantic.dialect.plsql.PlSqlSemanticListener;
import com.hound.semantic.model.SemanticResult;
import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import com.hound.parser.core.AstCollector;
import com.hound.graph.GraphWriter;
import com.hound.graph.UniversalAstNode;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

/**
 * FileProcessor — обработка одного SQL-файла с использованием новой семантической архитектуры.
 */
public class FileProcessor implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(FileProcessor.class);

    private final Path filePath;
    private final String dialect;
    private final GraphWriter graphWriter;

    public FileProcessor(Path filePath, String dialect, GraphWriter graphWriter) {
        this.filePath = filePath;
        this.dialect = dialect != null ? dialect.toLowerCase() : "plsql";
        this.graphWriter = graphWriter;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        String fileName = filePath.getFileName().toString();

        try {
            logger.info("Processing file: {}", fileName);

            // 1. Читаем файл (с fallback по кодировкам: UTF-8 → Windows-1251 → ISO-8859-1)
            String sqlContent = readFileWithFallback(filePath);

            // 2. ANTLR парсинг
            PlSqlLexer lexer = new PlSqlLexer(CharStreams.fromString(sqlContent));
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            PlSqlParser parser = new PlSqlParser(tokens);

            // 3. Парсим корневой rule
            PlSqlParser.Sql_scriptContext parseTree = parser.sql_script();

            // 4. Семантический анализ
            UniversalSemanticEngine engine = new UniversalSemanticEngine();
            PlSqlSemanticListener semanticListener = new PlSqlSemanticListener(engine);

            ParseTreeWalker.DEFAULT.walk(semanticListener, parseTree);

            SemanticResult semanticResult = engine.getResult();

            // 5. Построение AST для сохранения в граф
            UniversalAstNode astRoot = new UniversalAstNode("Program", "ROOT");
            astRoot.addProperty("fileName", fileName);
            astRoot.addProperty("dialect", dialect);

            // 6. Запись AST в граф
            if (graphWriter != null) {
                AstCollector.CollectionResult astResult = AstCollector.collect(astRoot);
                graphWriter.writeResult(astResult);
            }

            long duration = System.currentTimeMillis() - startTime;

            logger.info("Successfully processed {} | dialect: {} | time: {}",
                    fileName, dialect, formatTime(duration));

        } catch (IOException e) {
            logger.error("Failed to read file: {}", fileName, e);
        } catch (Exception e) {
            logger.error("Error processing file: {}", fileName, e);
        }
    }

    /**
     * Читает файл с попыткой нескольких кодировок.
     * UTF-8 → Windows-1251 (Кириллица/Oracle TOAD) → ISO-8859-1 (lossless fallback).
     */
    private static String readFileWithFallback(Path file) throws IOException {
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (MalformedInputException e) {
            logger.debug("UTF-8 decode failed for {}, trying Windows-1251", file.getFileName());
        }
        try {
            String content = Files.readString(file, Charset.forName("Windows-1251"));
            logger.warn("File read as Windows-1251 (non-UTF-8): {}", file.getFileName());
            return content;
        } catch (MalformedInputException e) {
            logger.debug("Windows-1251 decode failed for {}, falling back to ISO-8859-1", file.getFileName());
        }
        logger.warn("File read as ISO-8859-1 (fallback, may have garbled chars): {}", file.getFileName());
        return Files.readString(file, StandardCharsets.ISO_8859_1);
    }

    private static String formatTime(long ms) {
        if (ms < 1000) return ms + "ms";
        long totalSec = ms / 1000;
        long min = totalSec / 60;
        long sec = totalSec % 60;
        if (min > 0) return String.format("%dm %02ds", min, sec);
        return String.format("%d.%ds", sec, (ms % 1000) / 100);
    }
}