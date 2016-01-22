package com.lange.twitterexercise.operations;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by lange on 22/1/16.
 */
public class OperationQueue {

    private static final Logger LOGGER = Logger.getLogger(OperationQueue.class.getName());

    private static final AtomicInteger enqueueCount = new AtomicInteger(0);

    public enum PoolType {
        IO_POOL(3.0f),
        PROCESSING_POOL(1.0f);

        private final float scaleFactor;
        private final ExecutorService executorService;
        private final BlockingQueue<Runnable> executables;

        PoolType(float scaleFactor) {
            this.scaleFactor = scaleFactor;

            int cpus = Runtime.getRuntime().availableProcessors();
            int maxThreads = Math.round(cpus * scaleFactor);
            maxThreads = (maxThreads > 0 ? maxThreads : 1);

            this.executorService = new ThreadPoolExecutor(
                    maxThreads,
                    maxThreads,
                    1,
                    TimeUnit.MINUTES,
                    new LinkedBlockingQueue<Runnable>()
            );

            this.executables = new LinkedBlockingQueue<>();

            Thread consumer = new Thread(() -> {
                while (true) {
                    while (!executables.isEmpty()) {
                        try {
                            executorService.submit(executables.take());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    Thread.yield();
                }
            });
            consumer.start();

        }
    }

    public static void enqueue(PoolType poolType, Runnable executable) {

        if (poolType == PoolType.IO_POOL) {
            enqueueCount.incrementAndGet();
            LOGGER.log(Level.INFO, String.format("ENQUEUED. Count [%s]", enqueueCount.get()));
        }

        poolType.executables.add(executable);
    }

    public static AtomicInteger getEnqueueCount() {
        return enqueueCount;
    }

}
