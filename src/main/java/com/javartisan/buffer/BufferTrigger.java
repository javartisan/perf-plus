package com.javartisan.buffer;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author javartisan
 */
public interface BufferTrigger<T> extends AutoCloseable {

    void add(T e) throws InterruptedException;

    boolean add(T e, long timeout, TimeUnit unit);

    boolean remove(T e);

    boolean contains(T e);

    T remove();

    void force();

    void trigger(List<T> batch);

    void close();

    @FunctionalInterface
    public static interface RejectHandler<T> {
        boolean reject(T e);
    }

}
