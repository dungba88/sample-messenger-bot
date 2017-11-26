package org.travelbot.java.support.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.NonNull;

public class StopWatchBucket {
    
    private final static int BUCKET_SIZE_THRESHOLD = 10000;
    
    private final static StopWatchBucket instance = new StopWatchBucket();
    
//    private ThreadLocal<Map<String, Long>> buckets = new ThreadLocal<Map<String, Long>>() {
//
//        public Map<String, Long> initialValue() {
//            return new HashMap<>();
//        }
//    };
    
    private Map<String, Long> map = new ConcurrentHashMap<>();
    
    private StopWatchBucket() {
        
    }
    
    public final static StopWatchBucket getInstance() {
        return StopWatchBucket.instance;
    }
    
    public void start(@NonNull String id) {
//        Map<String, Long> map = buckets.get();
        if (map.size() > BUCKET_SIZE_THRESHOLD)
            map.clear();
        
        long start = System.nanoTime();
        map.put(id, start);
    }
    
    public Long stop(@NonNull String id) {
//        Map<String, Long> map = buckets.get();
        Long start = map.get(id);
        if (start != null)
            return System.nanoTime() - map.get(id);
        return null;
    }
}
