package com.meituan.olee.exceptions;

public class ParseException extends RuntimeException {
    public ParseException(String message) {
        super(message);
    }

    public ParseException() {
    }
}
