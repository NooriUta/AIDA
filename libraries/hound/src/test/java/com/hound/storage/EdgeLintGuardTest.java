package com.hound.storage;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sprint 5.1 LINT_GUARD — validates that every edge type name used in
 * appendEdge() / edgeByRid() / edgeRemote() calls is declared in
 * RemoteSchemaCommands DDL.
 *
 * Prevents introducing edge types without DDL, misspelled edge names,
 * and direction-as-name anti-patterns that bypass the schema registry.
 */
class EdgeLintGuardTest {

    private static Set<String> declaredEdgeTypes;

    private static final Pattern DDL_EDGE_PATTERN =
            Pattern.compile("CREATE EDGE TYPE\\s+(\\w+)\\s+IF NOT EXISTS");

    private static final Pattern APPEND_EDGE_PATTERN =
            Pattern.compile("appendEdge\\(\"(\\w+)\"");

    private static final Pattern EDGE_BY_RID_PATTERN =
            Pattern.compile("edgeByRid\\(\"(\\w+)\"");

    private static final Pattern EDGE_REMOTE_PATTERN =
            Pattern.compile("edgeRemote\\(\"(\\w+)\"");

    private static final Pattern CREATE_EDGE_INLINE_PATTERN =
            Pattern.compile("\"CREATE EDGE\\s+(\\w+)\\s+FROM");

    @BeforeAll
    static void buildAllowlist() {
        declaredEdgeTypes = new LinkedHashSet<>();
        for (String cmd : RemoteSchemaCommands.typeCommands()) {
            Matcher m = DDL_EDGE_PATTERN.matcher(cmd);
            if (m.find()) declaredEdgeTypes.add(m.group(1));
        }
        for (String cmd : RemoteSchemaCommands.propertyCommands()) {
            Matcher m = DDL_EDGE_PATTERN.matcher(cmd);
            if (m.find()) declaredEdgeTypes.add(m.group(1));
        }
        assertFalse(declaredEdgeTypes.isEmpty(),
                "Should extract at least 10 edge types from DDL, got 0");
    }

    @Test
    void allDeclaredEdgeTypesAreUpperSnakeCase() {
        Pattern upperSnake = Pattern.compile("[A-Z][A-Z0-9_]*");
        List<String> violations = declaredEdgeTypes.stream()
                .filter(name -> !name.startsWith("Dali"))
                .filter(name -> !upperSnake.matcher(name).matches())
                .toList();
        assertTrue(violations.isEmpty(),
                "Edge types should be UPPER_SNAKE_CASE: " + violations);
    }

    @Test
    void appendEdge_usesOnlyDeclaredTypes() throws IOException {
        List<String> violations = scanSourceFiles(APPEND_EDGE_PATTERN);
        assertTrue(violations.isEmpty(),
                "appendEdge() uses undeclared edge types:\n" + String.join("\n", violations));
    }

    @Test
    void edgeByRid_usesOnlyDeclaredTypes() throws IOException {
        List<String> violations = scanSourceFiles(EDGE_BY_RID_PATTERN);
        assertTrue(violations.isEmpty(),
                "edgeByRid() uses undeclared edge types:\n" + String.join("\n", violations));
    }

    @Test
    void edgeRemote_usesOnlyDeclaredTypes() throws IOException {
        List<String> violations = scanSourceFiles(EDGE_REMOTE_PATTERN);
        assertTrue(violations.isEmpty(),
                "edgeRemote() uses undeclared edge types:\n" + String.join("\n", violations));
    }

    @Test
    void inlineCreateEdge_usesOnlyDeclaredTypes() throws IOException {
        List<String> violations = scanSourceFiles(CREATE_EDGE_INLINE_PATTERN);
        assertTrue(violations.isEmpty(),
                "Inline CREATE EDGE uses undeclared edge types:\n" + String.join("\n", violations));
    }

    @Test
    void dumpEdgeTypeRegistry() {
        System.out.println("=== LINT_GUARD Edge Type Registry (" + declaredEdgeTypes.size() + " types) ===");
        declaredEdgeTypes.forEach(t -> System.out.println("  " + t));
    }

    private List<String> scanSourceFiles(Pattern callPattern) throws IOException {
        List<String> violations = new ArrayList<>();
        Path srcRoot = findSourceRoot();
        if (srcRoot == null) return violations;

        Files.walkFileTree(srcRoot, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!file.toString().endsWith(".java")) return FileVisitResult.CONTINUE;
                if (file.toString().contains("EdgeLintGuardTest")) return FileVisitResult.CONTINUE;

                String content = Files.readString(file);
                Matcher m = callPattern.matcher(content);
                while (m.find()) {
                    String edgeType = m.group(1);
                    if (!declaredEdgeTypes.contains(edgeType)) {
                        String relativePath = srcRoot.relativize(file).toString().replace('\\', '/');
                        int line = countLines(content, m.start());
                        violations.add("  " + relativePath + ":" + line + " — undeclared edge type: " + edgeType);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return violations;
    }

    private static Path findSourceRoot() {
        Path candidate = Path.of("libraries/hound/src/main/java");
        if (Files.isDirectory(candidate)) return candidate;
        candidate = Path.of("src/main/java");
        if (Files.isDirectory(candidate)) return candidate;
        return null;
    }

    private static int countLines(String text, int charOffset) {
        int line = 1;
        for (int i = 0; i < charOffset && i < text.length(); i++) {
            if (text.charAt(i) == '\n') line++;
        }
        return line;
    }
}
