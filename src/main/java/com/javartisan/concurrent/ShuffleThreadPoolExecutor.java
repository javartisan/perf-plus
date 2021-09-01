package com.javartisan.concurrent;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class ShuffleThreadPoolExecutor extends AbstractExecutorService {

    private final int parallelism;
    private final int size;

    private final BlockingQueue[] queues;

    public ShuffleThreadPoolExecutor(int parallelism, int size) {
        this.parallelism = parallelism;
        this.queues = new ArrayBlockingQueue[parallelism];
        this.size = size;
        for (int i = 0; i < queues.length; i++) {
            queues[i] = new ArrayBlockingQueue(size);
        }
    }

    @Override
    public void shutdown() {

    }

    @Override
    public List<Runnable> shutdownNow() {
        return null;
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void execute(Runnable command) {
        queues[partition(command)].add(command);
    }

    /**
     * @param runnable
     * @return
     */
    private int partition(Runnable runnable) {
        int partition = 0;
        if (runnable instanceof Partitioner) {
            partition = ((Partitioner) runnable).partition();
        } else {
            partition = runnable.hashCode();
        }
        return Math.abs(partition) % parallelism;
    }

}
