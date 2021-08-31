package com.javartisan.watch;

import com.sun.tools.internal.ws.wsdl.document.soap.SOAPUse;

import java.util.concurrent.TimeUnit;

public class SimpleWatcher implements Watcher {

    private static final ThreadLocal<Long> START_TL = new ThreadLocal<>();
    private static final ThreadLocal<Long> STOP_TL = new ThreadLocal<>();

    @Override
    public long start() {
        long time = System.nanoTime();
        START_TL.set(time);
        return time;
    }

    /**
     * nanoTime
     *
     * @return
     */
    @Override
    public long stop() {
        long time = System.nanoTime();
        STOP_TL.set(time);
        return time;
    }

    @Override
    public long costTime(TimeUnit timeUnit) {
        try {
            return timeUnit.convert(STOP_TL.get() - START_TL.get(), TimeUnit.NANOSECONDS);
        } finally {
            START_TL.remove();
            STOP_TL.remove();
        }
    }
}
