package com.hound.exception;

/**
 * Базовое исключение для всего приложения
 */
public class HoundException extends RuntimeException {

    private final ErrorCode errorCode;

    public HoundException(String message) {
        super(message);
        this.errorCode = ErrorCode.GENERAL_ERROR;
    }

    public HoundException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.GENERAL_ERROR;
    }

    public HoundException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public HoundException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * Коды ошибок приложения
     */
    public enum ErrorCode {
        GENERAL_ERROR(1000, "General application error"),
        CONFIGURATION_ERROR(1001, "Configuration error"),

        // Parser errors (2000-2099)
        PARSE_ERROR(2000, "Parse error"),
        SYNTAX_ERROR(2001, "Syntax error"),
        UNSUPPORTED_LANGUAGE(2002, "Unsupported language"),
        GRAMMAR_NOT_FOUND(2003, "Grammar not found"),

        // Database errors (3000-3099)
        DATABASE_CONNECTION_ERROR(3000, "Database connection error"),
        DATABASE_QUERY_ERROR(3001, "Database query error"),
        TRANSACTION_ERROR(3002, "Transaction error"),
        BATCH_WRITE_ERROR(3003, "Batch write error"),

        // File errors (4000-4099)
        FILE_NOT_FOUND(4000, "File not found"),
        FILE_READ_ERROR(4001, "File read error"),
        FILE_TOO_LARGE(4002, "File too large"),

        // Cache errors (5000-5099)
        CACHE_READ_ERROR(5000, "Cache read error"),
        CACHE_WRITE_ERROR(5001, "Cache write error"),

        // Processing errors (6000-6099)
        PROCESSING_ERROR(6000, "Processing error"),
        THREAD_ERROR(6001, "Thread pool error"),
        TIMEOUT_ERROR(6002, "Timeout error");

        private final int code;
        private final String message;

        ErrorCode(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() { return code; }
        public String getMessage() { return message; }
    }
}