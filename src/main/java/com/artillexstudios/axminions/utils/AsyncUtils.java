package com.artillexstudios.axminions.utils;

import com.artillexstudios.axminions.config.Config;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class AsyncUtils {
    private static final Logger log = LoggerFactory.getLogger(AsyncUtils.class);
    private static ExecutorService executorService;

    public static void setup() {
        executorService = Executors.newFixedThreadPool(Math.max(1, Config.ASYNC_PROCESSOR_POOL_SIZE), new ThreadFactory() {
            private static final AtomicInteger counter = new AtomicInteger(1);

            @Override
            public Thread newThread(@NotNull Runnable runnable) {
                return new Thread(null, runnable, "AxMinions-Async-Processor-Thread-" + counter.getAndIncrement());
            }
        });
    }

    public static void run(Runnable runnable, boolean async) {
        if (async) {
            executorService.submit(runnable);
        } else {
            runnable.run();
        }
    }

    public static Future<?> submit(Runnable runnable, boolean async) {
        if (async) {
            return executorService.submit(runnable);
        }

        CompletableFuture<?> future = new CompletableFuture<>();
        runnable.run();
        future.complete(null);
        return future;
    }

    public static Future<?> submit(Runnable runnable) {
        return executorService.submit(runnable);
    }

    public static ExecutorService executor() {
        return executorService;
    }

    public static void stop() {
        try {
            executorService.shutdown();
            executorService.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException exception) {
            log.error("An unexpected error occurred while stopping DataHandler!", exception);
        }
    }
}
