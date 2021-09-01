package com.javartisan.watch;

import java.util.concurrent.TimeUnit;

interface Watcher {

    public long start();

    public long stop();

    public long costTime(TimeUnit timeUnit);

}
