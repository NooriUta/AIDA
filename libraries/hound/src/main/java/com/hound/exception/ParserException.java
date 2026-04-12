package com.hound.exception;

/**
 * Исключение при парсинге кода
 */
public class ParserException extends HoundException {

    private final String fileName;
    private final int line;
    private final int column;
    private final String code;

    public ParserException(String message, String fileName, int line, int column) {
        super(message, ErrorCode.PARSE_ERROR);
        this.fileName = fileName;
        this.line = line;
        this.column = column;
        this.code = null;
    }

    public ParserException(String message, String fileName, int line, int column, String code) {
        super(message, ErrorCode.PARSE_ERROR);
        this.fileName = fileName;
        this.line = line;
        this.column = column;
        this.code = code;
    }

    public ParserException(String message, Throwable cause, String fileName, int line, int column) {
        super(message, cause, ErrorCode.PARSE_ERROR);
        this.fileName = fileName;
        this.line = line;
        this.column = column;
        this.code = null;
    }

    public String getFileName() { return fileName; }
    public int getLine() { return line; }
    public int getColumn() { return column; }
    public String getCode() { return code; }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder(super.getMessage());
        if (fileName != null) {
            sb.append(" [file: ").append(fileName);
            if (line > 0) {
                sb.append(", line: ").append(line);
                if (column > 0) {
                    sb.append(", column: ").append(column);
                }
            }
            sb.append("]");
        }
        if (code != null) {
            sb.append("\nCode snippet:\n").append(code);
        }
        return sb.toString();
    }
}