package com.hound.parser;

import com.hound.graph.UniversalAstNode;  // Добавляем импорт

/**
 * Слушатель событий парсинга AST
 */
public interface AstListener {

    /**
     * Вызывается при входе в узел AST
     * @param node узел AST
     */
    void onNodeEnter(UniversalAstNode node);

    /**
     * Вызывается при выходе из узла AST
     * @param node узел AST
     */
    void onNodeExit(UniversalAstNode node);

    /**
     * Вызывается при ошибке парсинга
     * @param message сообщение об ошибке
     * @param line строка ошибки
     * @param column колонка ошибки
     * @param fileName имя файла
     */
    void onError(String message, int line, int column, String fileName);
}