package com.skadi;

/**
 * Base checked exception для всех ошибок SKADI fetch.
 * Dali должен явно обрабатывать — не RuntimeException.
 */
public class SkadiFetchException extends Exception {

    private final String adapterName;
    private final String objectName;   // null если ошибка не связана с конкретным объектом

    public SkadiFetchException(String message, String adapterName, String objectName) {
        super(message);
        this.adapterName = adapterName;
        this.objectName  = objectName;
    }

    public SkadiFetchException(String message, String adapterName, String objectName, Throwable cause) {
        super(message, cause);
        this.adapterName = adapterName;
        this.objectName  = objectName;
    }

    public String getAdapterName() { return adapterName; }
    public String getObjectName()  { return objectName; }
}
