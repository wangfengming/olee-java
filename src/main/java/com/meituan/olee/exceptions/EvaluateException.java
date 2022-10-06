package com.meituan.olee.exceptions;

public class EvaluateException extends RuntimeException {
    public EvaluateException() {
    }

    public EvaluateException(String message) {
        super(message);
    }
}
