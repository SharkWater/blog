package com.shark.react.multithread.processor;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WorkerThreadPool {
    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(4, 8, 5, TimeUnit.SECONDS, new LinkedBlockingDeque<>(32));

    public static void submitTask(Runnable task) {
        executor.submit(task);
    }

    public static void shutdown() {
        executor.shutdown();
    }
}
