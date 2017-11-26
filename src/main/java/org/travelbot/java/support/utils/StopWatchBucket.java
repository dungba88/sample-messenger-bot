package org.travelbot.java.support.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.NonNull;

public class StopWatchBucket {
    
    private static final int BUCKET_SIZE_THRESHOLD = 10000;
    
    private static final StopWatchBucket instance = new StopWatchBucket();
    
    private Map<String, Long> map = new ConcurrentHashMap<>();
    
    private StopWatchBucket() {
        
    }
    
    public static final StopWatchBucket getInstance() {
        return StopWatchBucket.instance;
    }
    
    public void start(@NonNull String id) {
        if (map.size() > BUCKET_SIZE_THRESHOLD)
            map.clear();
        
        long start = System.nanoTime();
        map.put(id, start);
    }
    
    public Long stop(@NonNull String id) {
        Long start = map.get(id);
        if (start != null)
            return System.nanoTime() - map.get(id);
        return null;
    }
}
