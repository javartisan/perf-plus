package com.javartisan.concurrent;

@FunctionalInterface
public interface ThrowableRunnable<E extends Throwable, T> {
    public T run() throws E;
}
