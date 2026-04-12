package com.hound.parser.core;

import com.hound.graph.UniversalAstNode;
import com.hound.parser.AstListener;
import com.hound.parser.LanguageParser;
import com.hound.parser.UniversalTreeListener;
import com.hound.parser.registry.ParserRegistry;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Универсальный парсер для всех SQL-диалектов ANTLR (BUG-2 исправлен).
 */
public class UniversalParser implements LanguageParser {

    private static final Logger logger = LoggerFactory.getLogger(UniversalParser.class);

    @Override
    public UniversalAstNode parse(String code, String fileName, AstListener listener) {
        UniversalAstNode root = new UniversalAstNode("Program", "ROOT");
        root.addProperty("fileName", fileName);
        root.addProperty("size", code.length());

        String language = extractLanguageHint(fileName);
        if (language == null || language.isBlank()) {
            language = "sql";
        }

        try {
            logger.info("Parsing file: {} | Language: {}", fileName, language.toUpperCase());

            ParserRegistry.ParserInfo info = ParserRegistry.getParserInfo(language);

            if (info == null) {
                logger.warn("Parser info not found for language '{}'. Using raw text fallback.", language);
                return createRawNode(code, fileName, root);
            }

            CharStream input = CharStreams.fromString(code);
            Lexer lexer = (Lexer) info.lexerClass().getConstructor(CharStream.class).newInstance(input);
            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            Parser parser = (Parser) info.parserClass().getConstructor(TokenStream.class).newInstance(tokenStream);

            Method startMethod = findStartRuleMethod(parser, info.startRuleName());
            if (startMethod == null) {
                logger.warn("Start rule not found for language {}. Using raw fallback.", language);
                return createRawNode(code, fileName, root);
            }

            ParseTree tree = (ParseTree) startMethod.invoke(parser);

            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(new UniversalTreeListener(root, listener, tokenStream), tree);

            root.addProperty("parsed", true);
            root.addProperty("language", language.toUpperCase());
            root.addProperty("parser", info.parserClass().getSimpleName());

            logger.info("Successfully parsed: {} | Language: {} | Root children: {}",
                    fileName, language.toUpperCase(), root.getChildren().size());

        } catch (Exception e) {
            logger.error("Parse error for file: {}", fileName, e);
            root.addProperty("parsed", false);
            root.addProperty("error", e.getMessage());
            return createRawNode(code, fileName, root);
        }

        return root;
    }

    private String extractLanguageHint(String fileName) {
        if (fileName == null || fileName.isBlank()) return "sql";
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".plsql") || lower.endsWith(".pks") || lower.endsWith(".pkb")) return "plsql";
        if (lower.contains("plsql") || lower.contains("oracle")) return "plsql";
        if (lower.contains("postgresql") || lower.contains("postgres")) return "postgresql";
        if (lower.contains("tsql") || lower.contains("sqlserver")) return "tsql";
        return "sql";
    }

    private Method findStartRuleMethod(Parser parser, String preferredRule) {
        String[] candidates = {preferredRule, "stmt", "statement", "sql_script", "program", "root", "query", "start", "sql"};
        for (String ruleName : candidates) {
            try {
                return parser.getClass().getMethod(ruleName);
            } catch (NoSuchMethodException ignored) {}
        }
        return null;
    }

    private UniversalAstNode createRawNode(String code, String fileName, UniversalAstNode root) {
        UniversalAstNode raw = new UniversalAstNode("RawText", "UNPARSED");
        String snippet = code.length() > 3000 ? code.substring(0, 3000) + "..." : code;
        raw.addProperty("content", snippet);
        raw.addProperty("fileName", fileName);
        root.addChild(raw);
        return root;
    }

    @Override
    public String getLanguageName() {
        return "Universal";
    }

    @Override
    public boolean supports(String code) {
        return true;
    }
}