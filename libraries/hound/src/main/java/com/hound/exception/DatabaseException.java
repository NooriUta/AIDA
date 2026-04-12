package com.hound.exception;

/**
 * Исключение при работе с базой данных
 */
public class DatabaseException extends HoundException {

    private final String databaseType;
    private final String query;

    public DatabaseException(String message, String databaseType) {
        super(message, ErrorCode.DATABASE_QUERY_ERROR);
        this.databaseType = databaseType;
        this.query = null;
    }

    public DatabaseException(String message, String databaseType, String query) {
        super(message, ErrorCode.DATABASE_QUERY_ERROR);
        this.databaseType = databaseType;
        this.query = query;
    }

    public DatabaseException(String message, Throwable cause, String databaseType) {
        super(message, cause, ErrorCode.DATABASE_QUERY_ERROR);
        this.databaseType = databaseType;
        this.query = null;
    }

    public DatabaseException(String message, Throwable cause, String databaseType, String query) {
        super(message, cause, ErrorCode.DATABASE_QUERY_ERROR);
        this.databaseType = databaseType;
        this.query = query;
    }

    public String getDatabaseType() { return databaseType; }
    public String getQuery() { return query; }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder(super.getMessage());
        if (databaseType != null) {
            sb.append(" [database: ").append(databaseType).append("]");
        }
        if (query != null) {
            sb.append("\nQuery: ").append(query);
        }
        return sb.toString();
    }
}