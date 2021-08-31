package com.javartisan.watch;

import java.util.concurrent.TimeUnit;

public interface Watcher {

    public long start();

    public long stop();

    public long costTime(TimeUnit timeUnit);

}
