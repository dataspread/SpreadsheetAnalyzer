package org.dataspread.sheetanalyzer.util;

public class RedisGraphFailedException extends RuntimeException {
    public RedisGraphFailedException() {}

    public RedisGraphFailedException(String message) {
        super(message);
    }
}
