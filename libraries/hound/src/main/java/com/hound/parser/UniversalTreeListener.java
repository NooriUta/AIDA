package com.hound.parser;

import com.hound.graph.UniversalAstNode;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Универсальный слушатель ANTLR (IMP-2 + IMP-4: предкомпилированные regex + фильтр шумовых терминалов).
 */
public class UniversalTreeListener implements ParseTreeListener {

    private static final Logger logger = LoggerFactory.getLogger(UniversalTreeListener.class);

    private static final int MAX_SNIPPET_LENGTH = 500;
    private static final Pattern NUMERIC = Pattern.compile("-?\\d+(\\.\\d+)?([eE][+-]?\\d+)?");
    private static final Set<String> SKIP_TERMINAL_TYPES = Set.of("punctuation", "keyword");

    private final Deque<UniversalAstNode> nodeStack = new ArrayDeque<>();
    private final AstListener listener;
    private final TokenStream tokenStream;
    private final boolean filterNoiseTerminals = true; // можно вынести в конфиг позже

    public UniversalTreeListener(UniversalAstNode root, AstListener listener, TokenStream tokenStream) {
        this.nodeStack.push(root);
        this.listener = listener;
        this.tokenStream = tokenStream;
    }

    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
        try {
            String ruleName = getRuleName(ctx);
            UniversalAstNode node = new UniversalAstNode(ruleName, "RULE");

            Token start = ctx.getStart();
            Token stop = ctx.getStop();

            if (start != null) {
                node.addProperty("line", start.getLine());
                node.addProperty("column", start.getCharPositionInLine());
                node.addProperty("startIndex", start.getStartIndex());
            }
            if (stop != null) {
                node.addProperty("stopIndex", stop.getStopIndex());
                node.addProperty("endLine", stop.getLine());
                node.addProperty("endColumn", stop.getCharPositionInLine());
            }

            node.addProperty("qualifiedName", buildQualifiedName(start != null ? start.getStartIndex() : -1,
                    stop != null ? stop.getStopIndex() : -1, ruleName));
            node.addProperty("childCount", ctx.getChildCount());
            node.addProperty("depth", nodeStack.size());
            node.addProperty("ruleIndex", ctx.getRuleIndex());

            String snippet = extractSnippet(ctx);
            if (snippet != null && !snippet.isBlank()) {
                node.addProperty("snippet",
                        snippet.length() > MAX_SNIPPET_LENGTH
                                ? snippet.substring(0, MAX_SNIPPET_LENGTH) + "..."
                                : snippet);
            }

            UniversalAstNode parent = nodeStack.peek();
            if (parent != null) {
                parent.addChild(node);
            }

            nodeStack.push(node);

            if (listener != null) listener.onNodeEnter(node);

        } catch (Exception e) {
            logger.debug("Error in enterEveryRule", e);
        }
    }

    @Override
    public void exitEveryRule(ParserRuleContext ctx) {
        if (!nodeStack.isEmpty()) {
            UniversalAstNode node = nodeStack.pop();
            if (listener != null) listener.onNodeExit(node);
        }
    }

    @Override
    public void visitTerminal(TerminalNode terminalNode) {
        try {
            Token symbol = terminalNode.getSymbol();
            if (symbol == null || symbol.getType() == Token.EOF) return;

            String text = symbol.getText();
            if (text == null || text.isBlank()) return;

            if (symbol.getChannel() != Token.DEFAULT_CHANNEL) return;

            String terminalType = classifyByTokenType(symbol.getType(), text);

            if (filterNoiseTerminals && SKIP_TERMINAL_TYPES.contains(terminalType)) {
                return;
            }

            UniversalAstNode node = new UniversalAstNode(terminalType, "TERMINAL");

            node.addProperty("text", text);
            node.addProperty("line", symbol.getLine());
            node.addProperty("column", symbol.getCharPositionInLine());
            node.addProperty("startIndex", symbol.getStartIndex());
            node.addProperty("stopIndex", symbol.getStopIndex());
            node.addProperty("tokenType", symbol.getType());
            node.addProperty("qualifiedName", buildQualifiedName(
                    symbol.getStartIndex(), symbol.getStopIndex(), terminalType));

            UniversalAstNode parent = nodeStack.peek();
            if (parent != null) {
                parent.addChild(node);
            }

            if (listener != null) {
                listener.onNodeEnter(node);
                listener.onNodeExit(node);
            }

        } catch (Exception e) {
            logger.debug("Error in visitTerminal", e);
        }
    }

    @Override
    public void visitErrorNode(ErrorNode node) {
        try {
            Token symbol = node.getSymbol();
            int line = symbol != null ? symbol.getLine() : 0;
            int column = symbol != null ? symbol.getCharPositionInLine() : 0;

            UniversalAstNode errorNode = new UniversalAstNode("error_node", "ERROR");
            errorNode.addProperty("text", node.getText());
            errorNode.addProperty("line", line);
            errorNode.addProperty("column", column);

            UniversalAstNode parent = nodeStack.peek();
            if (parent != null) parent.addChild(errorNode);

            if (listener != null) {
                listener.onError("Parse error", line, column, "unknown");
            }
        } catch (Exception e) {
            logger.debug("Error in visitErrorNode", e);
        }
    }

    private String classifyByTokenType(int tokenType, String text) {
        if (NUMERIC.matcher(text).matches()) {
            return "numeric_literal";
        }
        if ((text.startsWith("'") && text.endsWith("'")) ||
                (text.startsWith("\"") && text.endsWith("\""))) {
            return "string_literal";
        }
        if (text.matches("[+\\-*/<>=!|&^~%]+") || "||".equals(text) || "<>".equals(text)) {
            return "operator";
        }
        if (text.length() == 1 && "(),;.[]{}:".indexOf(text.charAt(0)) >= 0) {
            return "punctuation";
        }
        return "identifier";
    }

    private String extractSnippet(ParserRuleContext ctx) {
        if (tokenStream != null && ctx.getStart() != null && ctx.getStop() != null) {
            try {
                return tokenStream.getText(ctx.getStart(), ctx.getStop());
            } catch (Exception ignored) {}
        }
        return ctx.getText();
    }

    private String buildQualifiedName(int start, int stop, String name) {
        if (start >= 0 && stop >= 0) return start + ":" + stop + ":" + name;
        if (start >= 0) return start + ":" + name;
        return name;
    }

    private String getRuleName(ParserRuleContext ctx) {
        try {
            String className = ctx.getClass().getSimpleName();
            if (className.endsWith("Context")) {
                String name = className.substring(0, className.length() - 7);
                return toSnakeCase(name);
            }
            return className.toLowerCase();
        } catch (Exception e) {
            return "unknown_rule";
        }
    }

    private String toSnakeCase(String input) {
        if (input == null || input.isEmpty()) return input;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                sb.append('_');
            }
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }
}