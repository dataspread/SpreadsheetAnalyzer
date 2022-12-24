package org.dataspread.sheetanalyzer.util;

public class RedisGraphQueryException extends RuntimeException {
    public RedisGraphQueryException() {}
    public RedisGraphQueryException(String message) {
        super(message);
    }
}
