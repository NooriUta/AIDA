package com.skadi;

/** Источник недоступен (нет сети, неверный хост, порт закрыт). */
public class SkadiFetchConnectionException extends SkadiFetchException {

    public SkadiFetchConnectionException(String message, String adapterName, Throwable cause) {
        super(message, adapterName, null, cause);
    }
}
