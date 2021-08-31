package com.javartisan.buffer;

import com.javartisan.watch.SimpleWatcher;
import com.javartisan.watch.Watcher;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class QuickBufferTriggerTests {

    @Test
    public void testQuickBufferTrigger() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger();
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        QuickBufferTrigger<String> bufferTrigger = new QuickBufferTrigger<>(200, 10000, 5, TimeUnit.SECONDS, (List<String> elements) -> {
            executorService.submit(() -> {
                counter.addAndGet(elements.size());
            });
        }, e -> {
            counter.addAndGet(1);
            return true;
        });

        Watcher watcher = new SimpleWatcher();

        int count = Integer.MAX_VALUE / 200;
        Thread[] threads = new Thread[10];
        watcher.start();
        for (int k = 0; k < 10; k++) {
            threads[k] = new Thread(() -> {
                for (int i = 0; i < count; i++) {
                    try {
                        bufferTrigger.add(String.valueOf(i));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            threads[k].start();
        }
        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }
        watcher.stop();
        long seconds = watcher.costTime(TimeUnit.SECONDS);
        System.out.println(seconds);
        System.out.println("QPS = " + ((count * 10.0) / (seconds)));
        bufferTrigger.close();
        executorService.shutdown();
        executorService.awaitTermination(2, TimeUnit.MINUTES);
        System.out.println(count * 10 + "  " + counter.get());
    }

    @Test
    public void testTrigger() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger();
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        QuickBufferTrigger<String> bufferTrigger = new QuickBufferTrigger<>(20, 10000, 5, TimeUnit.SECONDS, (List<String> elements) -> {
            executorService.submit(() -> {
                System.out.println(LocalDateTime.now() + " " + elements);
            });
        }, e -> {
            System.out.println(LocalDateTime.now() + " " + e);
            return true;
        });

        for (int i = 0; i < 10; i++) {
            Thread.sleep(5500);
            bufferTrigger.add(i + "");
        }

    }
}
