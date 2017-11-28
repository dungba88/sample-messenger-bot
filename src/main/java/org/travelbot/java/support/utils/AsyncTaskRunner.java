package org.travelbot.java.support.utils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.CompletableDeferredObject;

import lombok.Getter;

public class AsyncTaskRunner {

    private final @Getter Executor executor;
    
    public AsyncTaskRunner(int noThreads) {
        this.executor = Executors.newFixedThreadPool(noThreads);
    }

    public <T> Promise<T, Throwable> supplyTask(Supplier<T> supplier) {
        CompletableFuture<T> future = CompletableFuture.supplyAsync(supplier, executor);
        return new CompletableDeferredObject<>(future);
    }

    public Promise<Void, Throwable> runTask(Runnable runnable) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(runnable, executor);
        return new CompletableDeferredObject<>(future);
    }
}
