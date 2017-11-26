package org.travelbot.java.support.utils;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.CompletableDeferredObject;

public final class AsyncUtils {
    
    public static <T> Promise<T, Throwable> supplyTask(Supplier<T> supplier) {
        CompletableFuture<T> future = CompletableFuture.supplyAsync(supplier);
        return new CompletableDeferredObject<>(future);
    }
}
