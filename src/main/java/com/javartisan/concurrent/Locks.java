package com.javartisan.concurrent;

import java.util.concurrent.locks.Lock;

public class Locks {

    private static final boolean allowCreate = false;

    private Locks() {
        if (!allowCreate) {
            throw new RuntimeException("not allow create Locks.");
        }
    }


    public static <T> T runWithLock(Lock lock, ThrowableRunnable<? extends Throwable, T> runnable) {
        lock.lock();
        try {
            return runnable.run();
        } catch (Throwable ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        } finally {
            lock.unlock();
        }
    }

}
