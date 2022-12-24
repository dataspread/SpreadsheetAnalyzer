package org.dataspread.sheetanalyzer.util;

public class RedisGraphLoadingException extends RuntimeException {
    public RedisGraphLoadingException() {}

    public RedisGraphLoadingException(String message) {
        super(message);
    }
}
