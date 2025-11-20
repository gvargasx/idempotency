package org.example.idempotency.storage;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.example.idempotency.model.IdempotencyEntry;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class IdempotencyStorage {

    private final Cache<String, IdempotencyEntry> cache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(5))
            .maximumSize(10_000)
            .build();

    public IdempotencyEntry get(String key) {
        return cache.getIfPresent(key);
    }

    public void put(String key, IdempotencyEntry entry) {
        cache.put(key, entry);
    }
}
