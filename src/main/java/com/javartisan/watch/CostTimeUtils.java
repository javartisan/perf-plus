package com.javartisan.watch;

import java.util.concurrent.TimeUnit;

public class CostTimeUtils {

    private static final SimpleWatcher WATCHER = new SimpleWatcher();

    public static long start() {
        return WATCHER.start();
    }

    public static long stop() {
        return WATCHER.stop();
    }

    public static long costTime(TimeUnit timeUnit) {
        return WATCHER.costTime(timeUnit);
    }
}
