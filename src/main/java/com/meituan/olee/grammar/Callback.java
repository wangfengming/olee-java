package com.meituan.olee.grammar;

@FunctionalInterface
public interface Callback {
    Object apply(Object... args);
}
