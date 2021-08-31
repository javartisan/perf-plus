package com.javartisan.watch;

import java.util.concurrent.TimeUnit;

public class SimpleWatcher implements Watcher {

    private static final ThreadLocal<Long> THREAD_LOCAL = ThreadLocal.withInitial(() -> System.nanoTime());

    @Override
    public long start() {
        return 0;
    }

    @Override
    public long stop() {
        return 0;
    }

    @Override
    public long to(TimeUnit timeUnit) {
        return 0;
    }
}
