package com.meituan.olee;

@FunctionalInterface
public interface Callback {
    Object apply(Object... args);
}
