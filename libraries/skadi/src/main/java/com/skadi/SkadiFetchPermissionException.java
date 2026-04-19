package com.skadi;

/** Недостаточно привилегий для чтения объекта или системных таблиц. */
public class SkadiFetchPermissionException extends SkadiFetchException {

    public SkadiFetchPermissionException(
            String message, String adapterName, String objectName, Throwable cause) {
        super(message, adapterName, objectName, cause);
    }
}
