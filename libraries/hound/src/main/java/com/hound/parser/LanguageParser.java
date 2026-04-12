package com.hound.parser;

import com.hound.graph.UniversalAstNode;

/**
 * Интерфейс для парсера языка (обновлено для BUG-2)
 * listener теперь передаётся явно в parse() — это решает race condition в многопотоке
 */
public interface LanguageParser {

    /**
     * Парсит код и возвращает AST дерево
     * listener передаётся явно (thread-safe)
     */
    UniversalAstNode parse(String code, String fileName, AstListener listener);

    /**
     * Возвращает имя языка
     */
    String getLanguageName();

    /**
     * Проверяет, поддерживает ли парсер данный код
     */
    boolean supports(String code);
}