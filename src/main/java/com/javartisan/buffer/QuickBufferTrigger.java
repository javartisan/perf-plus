package com.javartisan.buffer;

import com.javartisan.concurrent.Locks;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * @author javartisan
 */
public class QuickBufferTrigger<T> implements BufferTrigger<T> {

    private final List<T> queue = new LinkedList<>();
    private Consumer<List<T>> consumer;
    private RejectHandler<T> rejectHandler;
    private final Lock lock = new ReentrantLock();
    private final Condition producerCondition = lock.newCondition();
    private final Condition consumerCondition = lock.newCondition();

    private int batch;
    private int bufferMaxSize;
    private int interval;
    private TimeUnit timeUnit;

    public QuickBufferTrigger(int batch, int bufferMaxSize, int interval,
                              TimeUnit timeUnit, Consumer<List<T>> consumer, RejectHandler<T> rejectHandler) {
        this.batch = batch;
        this.bufferMaxSize = bufferMaxSize;
        this.interval = interval;
        this.timeUnit = timeUnit;
        this.consumer = consumer;
        this.rejectHandler = rejectHandler;
        consumerThread.start();
    }


    private volatile boolean stop = false;

    private Thread consumerThread = new Thread(() -> {
        Locks.runWithLock(lock, () -> {
            while (!stop) {
                List<T> batchList = new ArrayList<>(batch);
                if (queue.size() < batch) {
                    consumerCondition.await(interval, timeUnit);
                }
                if (queue.size() > 0) {
                    int size = Math.min(batch, queue.size());
                    for (int i = 0; i < size; i++) {
                        batchList.add(queue.remove(0));
                    }
                    producerCondition.signalAll();
                }
                if (batchList.size() > 0) {
                    trigger(batchList);
                }
            }
            return null;
        });
    });

    @Override
    public void add(T e) throws InterruptedException {
        Locks.runWithLock(lock, () -> {
            while (queue.size() >= bufferMaxSize) {
                try {
                    producerCondition.await();
                } catch (InterruptedException ex) {
                    throw ex;
                }
            }
            queue.add(e);
            signalConsumer();
            return null;
        });

    }

    @Override
    public boolean add(T e, long timeout, TimeUnit unit) {
        return Locks.runWithLock(lock, () -> {
            if (queue.size() >= bufferMaxSize) {
                try {
                    producerCondition.await(timeout, unit);
                } catch (InterruptedException ex) {
                    throw ex;
                }
            }
            if (queue.size() < bufferMaxSize) {
                if (queue.add(e)) {
                    signalConsumer();
                    return true;
                }
            } else if (rejectHandler != null) {
                return rejectHandler.reject(e);
            }
            return false;
        });
    }

    @Override
    public boolean remove(T e) {
        return Locks.runWithLock(lock, () -> {
            return queue.remove(e);
        });
    }

    @Override
    public boolean contains(T e) {
        return Locks.runWithLock(lock, () -> queue.contains(e));
    }

    @Override
    public T remove() {
        return Locks.runWithLock(lock, () -> {
            if (queue.size() > 0) {
                return queue.remove(0);
            }
            return null;
        });
    }

    @Override
    public void force() {
        Locks.runWithLock(lock, () -> {
            partition().forEach(batchList -> {
                trigger(batchList);
            });
            return null;
        });
    }

    @Override
    public void trigger(List<T> batch) {
        consumer.accept(batch);
    }

    @Override
    public void close() {
        Locks.runWithLock(lock, () -> {
            stop = true;
            partition().forEach(batchList -> {
                trigger(batchList);
            });
            return null;
        });
    }

    private void signalConsumer() {
        if (queue.size() >= batch) {
            consumerCondition.signal();
        }
    }

    public List<List<T>> partition() {
        int size = queue.size();
        int batchCount = size / batch + (size % batch == 0 ? 0 : 1);
        int index = 0;
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < batchCount; i++) {
            List<T> batchList = new ArrayList<>();
            for (int j = 0; j < batch && index < size; j++, index++) {
                batchList.add(queue.remove(0));
            }
            partitions.add(batchList);
        }
        return partitions;
    }
}
